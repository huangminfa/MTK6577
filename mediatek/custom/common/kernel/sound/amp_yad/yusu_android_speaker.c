/*****************************************************************************
*                E X T E R N A L      R E F E R E N C E S
******************************************************************************
*/

#include <linux/kernel.h>
#include <linux/module.h>
#include <linux/init.h>
#include <linux/device.h>
#include <linux/slab.h>
#include <linux/fs.h>
#include <linux/mm.h>
#include <linux/interrupt.h>
#include <linux/vmalloc.h>
#include <linux/platform_device.h>
#include <linux/miscdevice.h>
#include <linux/wait.h>
#include <linux/spinlock.h>
#include <linux/semaphore.h>
#include <asm/uaccess.h>
#include <linux/pmic6326_sw.h>
#include <linux/delay.h>
#include "yusu_android_speaker.h"

#if defined(MT6575)
#include <mach/mt6575_gpio.h>
#include <mach/mt6575_typedefs.h>
#include <mach/mt6575_clock_manager.h>
#include <mach/mt6575_pmic_feature_api.h>
#elif defined(MT6577)
#include <mach/mt6577_gpio.h>
#include <mach/mt6577_typedefs.h>
#include <mach/mt6577_clock_manager.h>
#include <mach/mt6577_pmic_feature_api.h>
#endif
/*****************************************************************************
*                C O M P I L E R      F L A G S
******************************************************************************
*/
//#define CONFIG_DEBUG_MSG
#ifdef CONFIG_DEBUG_MSG
#define PRINTK(format, args...) printk( KERN_EMERG format,##args )
#else
#define PRINTK(format, args...)
#endif


/*****************************************************************************
*                          C O N S T A N T S
******************************************************************************
*/
#ifndef 	GPIO_SPEAKER_EN_PIN
#define		GPIO_SPEAKER_EN_PIN	104
#endif

#ifdef 	__DRV_AMP_DUAL_SPEAKER__
#ifndef 	GPIO_SPEAKER1_EN_PIN
#define		GPIO_SPEAKER1_EN_PIN	109
#endif
#endif

#define SPK_WARM_UP_TIME        (10) //unit is ms
/*****************************************************************************
*                         D A T A      T Y P E S
******************************************************************************
*/
static int Speaker_Volume=0;
static bool gsk_on=false; // speaker is open?
static bool gsk_resume=false;
static bool gsk_forceon=false;
/*****************************************************************************
*                  F U N C T I O N        D E F I N I T I O N
******************************************************************************
*/
extern void Yusu_Sound_AMP_Switch(BOOL enable);

#if defined(__DRV_AMP_CLASS_K_AW8733__) || defined(__DRV_AMP_CLASS_K_AW8155__)
enum aw8733_mode
{
	MODE_1 = 1,	//  input 1 high pulse.
	MODE_2,			//  input 2 high pulse
	MODE_3,			//  input 3 high pulse
	MODE_4,			//  input 4 high pulse
};

#if defined(__DRV_AMP_CLASS_K_AW8733__)
void custom_aw8733_speaker_turn_on(enum aw8733_mode mode, int channel)
{
	unsigned char pulse_count = mode;
	unsigned char k, i;
	unsigned long flag;

	mt_set_gpio_dir(GPIO_SPEAKER_EN_PIN, GPIO_DIR_OUT);
	
	local_irq_save(flag); 
	for (k = 0; k < pulse_count; k++)
	{		
		mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ZERO);
		//usleep(2);
		for (i = 0; i < 120; i++);
		mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ONE);
		//usleep(2);
		for (i = 0; i < 120; i++);
	}
	local_irq_restore(flag);
	//mdelay(1);
	mdelay(20);
}

void custom_aw8733_speaker_turn_off(int channel)
{
	mt_set_gpio_dir(GPIO_SPEAKER_EN_PIN, GPIO_DIR_OUT); // output
    mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ZERO); // high

	// delay 500 us at least
    msleep(1);
}

void custom_aw8733_louder_speaker_turn_on(int channel)
{
    PRINTK("custom_aw8733_louder_speaker_turn_on@@@ channel = %d\n",channel);
	if(gsk_on)
		return;	

	custom_aw8733_speaker_turn_on(MODE_4, channel);	

    gsk_on = true;
}
#elif defined(__DRV_AMP_CLASS_K_AW8155__)
void custom_aw8155_speaker_turn_on(enum aw8733_mode mode, int channel)
{
	unsigned char pulse_count = mode;
	unsigned char k, i;
	unsigned long flag;

	mt_set_gpio_dir(GPIO_SPEAKER_EN_PIN, GPIO_DIR_OUT);
	
	#ifdef 	__DRV_AMP_DUAL_SPEAKER__		
	if (1 == channel)
	{
		
		local_irq_save(flag); 
		for (k = 0; k < pulse_count; k++)
		{		
			mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ZERO);
			
			udelay(10);
			//for (i = 0; i < 120; i++);
			mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ONE);
			
			udelay(10);
			//for (i = 0; i < 120; i++);
		}
		local_irq_restore(flag);
	}
	else
	{
		mt_set_gpio_dir(GPIO_SPEAKER1_EN_PIN, GPIO_DIR_OUT);
		
		local_irq_save(flag); 
		for (k = 0; k < pulse_count; k++)
		{		
			mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ZERO);
			mt_set_gpio_out(GPIO_SPEAKER1_EN_PIN, GPIO_OUT_ZERO);
			
			udelay(10);
			//for (i = 0; i < 120; i++);
			mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ONE);
			mt_set_gpio_out(GPIO_SPEAKER1_EN_PIN, GPIO_OUT_ONE);
			
			udelay(10);
			//for (i = 0; i < 120; i++);
		}
		local_irq_restore(flag);
	}
	#else
	local_irq_save(flag); 
	for (k = 0; k < pulse_count; k++)
	{		
		mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ZERO);
		
		udelay(10);
		//for (i = 0; i < 120; i++);
		mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ONE);
		
		udelay(10);
		//for (i = 0; i < 120; i++);
	}
	local_irq_restore(flag);
	#endif
	//mdelay(1);
	mdelay(20);
}

void custom_aw8155_speaker_turn_off(int channel)
{
	mt_set_gpio_dir(GPIO_SPEAKER_EN_PIN, GPIO_DIR_OUT); // output
    mt_set_gpio_out(GPIO_SPEAKER_EN_PIN, GPIO_OUT_ZERO); // high

	#ifdef 	__DRV_AMP_DUAL_SPEAKER__
	// in speech, don't open the 2nd speaker.
	// by chu, zewei on 2012/11/08
	if (1 != channel)
	{
		mt_set_gpio_dir(GPIO_SPEAKER1_EN_PIN, GPIO_DIR_OUT); // output
		mt_set_gpio_out(GPIO_SPEAKER1_EN_PIN, GPIO_OUT_ZERO);
	}
	#endif

	// delay 500 us at least
    msleep(5);
}

void custom_aw8155_louder_speaker_turn_on(int channel)
{
    PRINTK("custom_aw8155_louder_speaker_turn_on@@@ channel = %d\n",channel);
	if(gsk_on)
		return;	

	custom_aw8155_speaker_turn_on(MODE_4, channel);	

    gsk_on = true;
}
#endif

void custom_external_speaker_turn_on(enum aw8733_mode mode, int channel)
{
	#if defined(__DRV_AMP_CLASS_K_AW8733__)
	custom_aw8733_speaker_turn_on(mode, channel);
	#elif defined(__DRV_AMP_CLASS_K_AW8155__)
	custom_aw8155_speaker_turn_on(mode, channel);
	#endif
}

void custom_external_speaker_turn_off(int channel)
{
	#if defined(__DRV_AMP_CLASS_K_AW8733__)
	custom_aw8733_speaker_turn_off(channel);
	#elif defined(__DRV_AMP_CLASS_K_AW8155__)
	custom_aw8155_speaker_turn_off(channel);
	#endif
}

void custom_external_louder_speaker_turn_on(int channel)
{
    #if defined(__DRV_AMP_CLASS_K_AW8733__)
	custom_aw8733_louder_speaker_turn_on(channel);
	#elif defined(__DRV_AMP_CLASS_K_AW8155__)
	custom_aw8155_louder_speaker_turn_on(channel);
	#endif
}

#endif

bool Speaker_Init(void)
{
   PRINTK("+Speaker_Init Success");
   mt_set_gpio_mode(GPIO_SPEAKER_EN_PIN,GPIO_MODE_00);  // gpio mode
   mt_set_gpio_pull_enable(GPIO_SPEAKER_EN_PIN,GPIO_PULL_ENABLE);

   #ifdef 	__DRV_AMP_DUAL_SPEAKER__
	mt_set_gpio_mode(GPIO_SPEAKER1_EN_PIN,GPIO_MODE_00);  // gpio mode
    mt_set_gpio_pull_enable(GPIO_SPEAKER1_EN_PIN,GPIO_PULL_ENABLE);
	#endif
   PRINTK("-Speaker_Init Success");
   return true;
}

bool Speaker_Register(void)
{
    return false;
}

int ExternalAmp(void)
{
	return 0;
}

bool Speaker_DeInit(void)
{
	return false;
}

void Sound_SpeakerL_SetVolLevel(int level)
{
   PRINTK(" Sound_SpeakerL_SetVolLevel level=%d\n",level);
}

void Sound_SpeakerR_SetVolLevel(int level)
{
   PRINTK(" Sound_SpeakerR_SetVolLevel level=%d\n",level);
}

void Sound_Speaker_Turnon(int channel)
{
    PRINTK("Sound_Speaker_Turnon channel = %d\n",channel);
	if(gsk_on)
		return;

	#if defined(__DRV_AMP_CLASS_K_AW8733__) || defined(__DRV_AMP_CLASS_K_AW8155__)
	custom_external_speaker_turn_on(MODE_4, channel);
	#else
    mt_set_gpio_dir(GPIO_SPEAKER_EN_PIN,GPIO_DIR_OUT); // output
    mt_set_gpio_out(GPIO_SPEAKER_EN_PIN,GPIO_OUT_ONE); // high
    msleep(SPK_WARM_UP_TIME);
    #endif
    
    gsk_on = true;
}

void Sound_Speaker_Turnoff(int channel)
{
    PRINTK("Sound_Speaker_Turnoff channel = %d\n",channel);
	if(!gsk_on)
		return;
    mt_set_gpio_dir(GPIO_SPEAKER_EN_PIN,GPIO_DIR_OUT); // output
    
	#if defined(__DRV_AMP_CLASS_K_AW8733__)  || defined(__DRV_AMP_CLASS_K_AW8155__)
	custom_external_speaker_turn_off(channel);
	#else
    mt_set_gpio_out(GPIO_SPEAKER_EN_PIN,GPIO_OUT_ZERO); // high
    #endif
	gsk_on = false;
}

void Sound_Speaker_SetVolLevel(int level)
{
    Speaker_Volume =level;
}


void Sound_Headset_Turnon(void)
{

}

void Sound_Headset_Turnoff(void)
{

}

//kernal use
void AudioAMPDevice_Suspend(void)
{
	PRINTK("AudioDevice_Suspend\n");
	if(gsk_on)
	{
		Sound_Speaker_Turnoff(Channel_Stereo);
		gsk_resume = true;
	}

}
void AudioAMPDevice_Resume(void)
{
	PRINTK("AudioDevice_Resume\n");
	if(gsk_resume)
		Sound_Speaker_Turnon(Channel_Stereo);
	gsk_resume = false;
}
void AudioAMPDevice_SpeakerLouderOpen(void)
{
	PRINTK("AudioDevice_SpeakerLouderOpen\n");
	gsk_forceon = false;
	if(gsk_on)
		return;
	Sound_Speaker_Turnon(Channel_Stereo);
	gsk_forceon = true;
	return ;

}
void AudioAMPDevice_SpeakerLouderClose(void)
{
	PRINTK("AudioDevice_SpeakerLouderClose\n");

	if(gsk_forceon)
		Sound_Speaker_Turnoff(Channel_Stereo);
	gsk_forceon = false;

}
void AudioAMPDevice_mute(void)
{
	PRINTK("AudioDevice_mute\n");
	if(gsk_on)
		Sound_Speaker_Turnoff(Channel_Stereo);
}

int Audio_eamp_command(unsigned int type, unsigned long args, unsigned int count)
{
	return 0;
}
static char *ExtFunArray[] =
{
    "InfoMATVAudioStart",
    "InfoMATVAudioStop",
    "End",
};

kal_int32 Sound_ExtFunction(const char* name, void* param, int param_size)
{
	int i = 0;
	int funNum = -1;

	//Search the supported function defined in ExtFunArray
	while(strcmp("End",ExtFunArray[i]) != 0 ) {		//while function not equal to "End"

	    if (strcmp(name,ExtFunArray[i]) == 0 ) {		//When function name equal to table, break
	    	funNum = i;
	    	break;
	    }
	    i++;
	}

	switch (funNum) {
	    case 0:			//InfoMATVAudioStart
	        printk("RunExtFunction InfoMATVAudioStart \n");
	        break;

	    case 1:			//InfoMATVAudioStop
	        printk("RunExtFunction InfoMATVAudioStop \n");
	        break;

	    default:
	    	 break;
	}

	return 1;
}


