#include <asm/arch/custom/inc/cust_leds.h>
#include <asm/arch/mt6577_pwm.h>

#include <asm/arch/mt6577_gpio.h>
#include "cust_gpio_usage.h"
extern int DISP_SetBacklight(int level);

#if 1//defined(BL_MODE_GPIO)
static int debug_enable = 0;

#if BUILD_UBOOT
#define LEDS_DEBUG(format, args...) do{ \
	if(debug_enable) \
	{\
		printf(format,##args);\
	}\
}while(0)
#else
#include <linux/kernel.h>

#define LEDS_DEBUG(format, args...) do{ \
	if(debug_enable) \
	{\
		printk(format,##args);\
	}\
}while(0)
#endif

#define		DELAY_TIME		160

static void mt65xx_aw9656_pwm(int gpio_num,int level)
{
	static unsigned int pre_pulse_main = 0;
	volatile unsigned int j, k, pulse_count, pre_pulse;
	unsigned int pulse[] = {0, 15, 13, 10, 8, 5, 3, 2, 1, 1, 1};	//	Number of pulse ,   1 -- 16
	int backlight_port = gpio_num;
	unsigned long flag;
	
	LEDS_DEBUG("mt65xx_aw9656_pwm: GPIO#%d: %d\n", gpio_num, level);
	
	mt_set_gpio_mode(backlight_port, GPIO_MODE_GPIO);
	mt_set_gpio_dir(backlight_port, GPIO_DIR_OUT);

	if (0 == level)
	{
		mt_set_gpio_out(backlight_port, GPIO_OUT_ZERO);
		mdelay(4);
		
		pre_pulse_main =  pulse[0];		
	}
	else
	{
		level = (level >> 5) + 1;
		pre_pulse = pre_pulse_main;
		if (pulse[level] == pre_pulse)			//  The  current  has NO  change.
		{
			return;
		}

		if (pre_pulse == pulse[0])
		{
			pulse_count = pulse[level] - 1;
		}
		else if (pre_pulse < pulse[level])
		{
			pulse_count = pulse[level] - pre_pulse;
		}
		else
		{
			pulse_count = pulse[level] + 16 - pre_pulse;
		}
		mt_set_gpio_out(backlight_port, GPIO_OUT_ONE);
		udelay(25);

		//  count the number of rising edge.
		local_irq_save(flag); 
		for (j = 0;  j < pulse_count; j++)
		{
			mt_set_gpio_out(backlight_port, GPIO_OUT_ZERO);
			for (k = 0; k < 120; k++);		//   MUST  be greaterthan  0.5 us and  less than 500 us.

			mt_set_gpio_out(backlight_port, GPIO_OUT_ONE);
            for (k = 0; k < 120; k++);		//   Be greater than 0.5 us.			
            LEDS_DEBUG("mt65xx_aw9656_pwm: j = %d, pulse_count = %d\n", j, pulse_count);
		}
		local_irq_restore(flag);
		mdelay(2);

		pre_pulse_main = pulse[level];
	}
}

static void mt65xx_cp2124_pwm(int gpio_num,int level)
{
	static unsigned int pre_pulse_main = 0;
	volatile unsigned int j, k, pulse_count, pre_pulse;

	//  Current( % ):        0  12.5   25  43.75   56.25   75   87.25  93.75   100             
	unsigned int pulse[] = {0, 15,   13,   10,    8,      5,   3,      2,     1,    1,     1};	//	Number of pulse ,   1 -- 16
	int backlight_port = gpio_num;
	unsigned long flag;
	
	LEDS_DEBUG("mt65xx_cp2124_pwm: GPIO#%d: %d\n", gpio_num, level);
	
	mt_set_gpio_mode(backlight_port, GPIO_MODE_GPIO);
	mt_set_gpio_dir(backlight_port, GPIO_DIR_OUT);

	if (0 == level)
	{
		mt_set_gpio_out(backlight_port, GPIO_OUT_ZERO);
		mdelay(2);
		
		pre_pulse_main =  pulse[0];		
	}
	else
	{
		level = (level >> 5) + 1;
		pre_pulse = pre_pulse_main;
		if (pulse[level] == pre_pulse)			//  The  current  has NO  change.
		{
			return;
		}

		pulse_count = pulse[level] - 1;
		mt_set_gpio_out(backlight_port, GPIO_OUT_ZERO);
		mdelay(2);

		//  count the number of rising edge.
		local_irq_save(flag); 
		mt_set_gpio_out(backlight_port, GPIO_OUT_ONE);
//		for (k = 0; k < DELAY_TIME; k++);
		udelay(1);
		for (j = 0;  j < pulse_count; j++)
		{
			mt_set_gpio_out(backlight_port, GPIO_OUT_ZERO);
//			for (k = 0; k < DELAY_TIME; k++);		//   MUST  be greaterthan  0.5 us and  less than 500 us.
			udelay(1);

			mt_set_gpio_out(backlight_port, GPIO_OUT_ONE);
//            for (k = 0; k < DELAY_TIME; k++);		//   Be greater than 0.5 us.			
            udelay(1);
            LEDS_DEBUG("mt65xx_cp2124_pwm: j = %d, pulse_count = %d\n", j, pulse_count);
		}
		local_irq_restore(flag);

		pre_pulse_main = pulse[level];
	}
}

static void mt65xx_et9378_pwm(int gpio_num,int level)
{
    LEDS_DEBUG(" mt65xx_et9378_pwm  [LED]GPIO#%d:%d\n", gpio_num, level);
	mt_set_gpio_mode(gpio_num, GPIO_MODE_GPIO);
	mt_set_gpio_dir(gpio_num, GPIO_DIR_OUT);

	if (level)
		mt_set_gpio_out(gpio_num, GPIO_OUT_ONE);
	else
		mt_set_gpio_out(gpio_num, GPIO_OUT_ZERO);
}

static int custom_mt65xx_pwm(int gpio_num, int level)
{
	#if 0//defined(BL_MODE_AW9656)
	mt65xx_aw9656_pwm(gpio_num, level);
	#elif 1//defined(BL_MODE_CP2124)
	mt65xx_cp2124_pwm(gpio_num, level);
	#elif 0//defined(BL_MODE_ET9378)
	mt65xx_et9378_pwm(gpio_num, level);	
	#else	// only pull up/down backlight control gpio port simply!
	mt_set_gpio_mode(gpio_num, GPIO_MODE_GPIO);
	mt_set_gpio_dir(gpio_num, GPIO_DIR_OUT);

	if (level)
		mt_set_gpio_out(gpio_num, GPIO_OUT_ONE);
	else
		mt_set_gpio_out(gpio_num, GPIO_OUT_ZERO);
	#endif

	return 0;
}
#endif

#ifndef	GPIO_PWM_1_PIN
#define	GPIO_PWM_1_PIN	68
#endif
unsigned int Cust_SetBacklight(int level, int div)
{
	custom_mt65xx_pwm(GPIO_PWM_1_PIN, level);

    return 0;
}



static struct cust_mt65xx_led cust_led_list[MT65XX_LED_TYPE_TOTAL] = {
	{"red",               MT65XX_LED_MODE_NONE, -1},
	{"green",             MT65XX_LED_MODE_NONE, -1},
	{"blue",              MT65XX_LED_MODE_NONE, -1},
	{"jogball-backlight", MT65XX_LED_MODE_NONE, -1},
	{"keyboard-backlight",MT65XX_LED_MODE_NONE, -1},
	{"button-backlight",  MT65XX_LED_MODE_PMIC, MT65XX_LED_PMIC_BUTTON},
	{"lcd-backlight",     MT65XX_LED_MODE_CUST, (int)Cust_SetBacklight},
};

struct cust_mt65xx_led *get_cust_led_list(void)
{
	return cust_led_list;
}
