#include <cust_leds.h>
#include <mach/mt6577_pwm.h>

#include <linux/kernel.h>
#include <mach/pmic_mt6329_hw_bank1.h> 
#include <mach/pmic_mt6329_sw_bank1.h> 
#include <mach/pmic_mt6329_hw.h>
#include <mach/pmic_mt6329_sw.h>
#include <mach/upmu_common_sw.h>
#include <mach/upmu_hw.h>

#include <mach/mt6577_gpio.h>
#include <linux/delay.h>
#include "cust_gpio_usage.h"

extern int mtkfb_set_backlight_level(unsigned int level);
extern int mtkfb_set_backlight_pwm(int div);

#if 1//defined(BL_MODE_GPIO)
static int debug_enable = 1;

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
        LEDS_DEBUG("mt65xx_cp2124_pwm: level = %d, pulse_count = %d\n", level, pulse_count);
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

/*
 * Notes: 
 * 		In the project M2, uses backlight chip  CP2124 .
 */
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

#ifndef	GPIO_PWM_1_PIN
#define	GPIO_PWM_1_PIN	68
#endif
unsigned int Cust_SetBacklight(int level, int div)
{
	#if 0
    mtkfb_set_backlight_pwm(div);
    mtkfb_set_backlight_level(brightness_mapping(level));
	#else
	custom_mt65xx_pwm(GPIO_PWM_1_PIN, level);
	#endif
    return 0;
}

#endif
#define ERROR_BL_LEVEL 0xFFFFFFFF

unsigned int brightness_mapping_backup(unsigned int level)
{
	if(level>=30 && level<=255) { // user changable by using Setting->Display->Brightness
		return (level-14)/16;
	}
	else if(level>0 && level<30) { // used to fade out for 7 seconds before shut down backlight
		return 0;
	}
	return ERROR_BL_LEVEL;
}

unsigned int brightness_mapping(unsigned int level)
{
	#if 0
	if(level>=75 && level<=255) { // user changable by using Setting->Display->Brightness
		return (level-39)/36;
	}
	else if(level>0 && level<75) { // used to fade out for 7 seconds before shutdown backlight
		return 0;
	}
	return ERROR_BL_LEVEL;
	#else
	return level;
	#endif
}

static struct cust_mt65xx_led cust_led_list[MT65XX_LED_TYPE_TOTAL] = {
//	{"red",               MT65XX_LED_MODE_PMIC, MT65XX_LED_PMIC_NLED_ISINK5},
//	{"green",             MT65XX_LED_MODE_PMIC, MT65XX_LED_PMIC_NLED_ISINK4},
	{"red",               MT65XX_LED_MODE_NONE, -1},
	{"green",             MT65XX_LED_MODE_NONE, -1},
	{"blue",              MT65XX_LED_MODE_NONE, -1},
	{"jogball-backlight", MT65XX_LED_MODE_NONE, -1},
	{"keyboard-backlight",MT65XX_LED_MODE_NONE, -1},
//	{"button-backlight",  MT65XX_LED_MODE_NONE, -1},
	{"button-backlight",  MT65XX_LED_MODE_PMIC, MT65XX_LED_PMIC_BUTTON},
//	{"lcd-backlight",     MT65XX_LED_MODE_PMIC, MT65XX_LED_PMIC_LCD_BOOST},
//	{"lcd-backlight",     MT65XX_LED_MODE_PMIC, MT65XX_LED_PMIC_LCD_ISINK},
//	{"lcd-backlight",     MT65XX_LED_MODE_PWM, PWM4}, //GPIO_PWM_1_PIN
	{"lcd-backlight",     MT65XX_LED_MODE_CUST, (int)Cust_SetBacklight},
};

struct cust_mt65xx_led *get_cust_led_list(void)
{
	return cust_led_list;
}

