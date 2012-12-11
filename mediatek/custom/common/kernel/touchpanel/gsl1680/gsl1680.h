#ifndef __GSL1680_H__
#define __GSL1680_H__

#if defined(__DRV_LCD_RESOLUTION_480X800__)
//touch coordinate range
#define TP_WIDTH    480
#define TP_LENTH    800
#elif defined(__DRV_LCD_RESOLUTION_480X854__)
#define TP_WIDTH    480
#define TP_LENTH    854
#else
//touch coordinate range
#define TP_WIDTH    320
#define TP_LENTH    480
#endif

//coordinate direction
#define TP_DIREC    1   // if 1 is (1,1), then 2(1,-1), 3(-1,-1), 4(-1,1)

//touch threshold
#define MAI_TRSD    200
#define SUB_TRSD    40
#define SUM_TRSD    (MAI_TRSD + SUB_TRSD + 20)

//touch tigger condition
#define TRIG_MOD    1   // 1 is edge, 0 is level
#define VOLT_LEV    0   // if trig mode is edge,
                        // 0 is IRQF_TRIGGER_RISING, 1 is IRQF_TRIGGER_FALLING
                        // if trig mode is level,
                        // 0 is IRQF_TRIGGER_HIGH, 1 is IRQF_TRIGGER_LOW

//touch sensitivity
#define TP_DACG     0x00100010  //9f/30
#define DAC_STEP    0x8e        //if TP_DACG=0x00180018,TP_DAC_STEP=0x61
                                //if TP_DACG=0x00100010,TP_DAC_STEP=0x8e
                                //if TP_DACG=0x000c000c,TP_DAC_STEP=0xbb
                                //if TP_DACG=0x000a000a,TP_DAC_STEP=0xdf
                                //if TP_DACG=0x00080008,TP_DAC_STEP=0x114
                                //if TP_DACG=0x00060006,TP_DAC_STEP=0x16e
#define CHANGE_CONDITION 0x0    //0--use average,1--use max

#define GSL_PAGE_REG    0xf0
#define GSL_CLOCK_REG   0xe4
#define GSL_START_REG   0xe0
#define POWE_FAIL_REG    0xbc
#define TOUCH_INFO_REG  0x80

#define DIS_MIN_X   0
#define DIS_MAX_X   TP_WIDTH
#define DIS_MIN_Y   0
#define DIS_MAX_Y   TP_LENTH

#define MIN_TOUCH   0
#define MAX_TOUCH   1
#define MIN_WIDTH   0
#define MAX_WIDTH   1
#define MIN_TRCKID  0
#define MAX_TRCKID  5

union gsl_point_data
{
    struct
    {
        u16 y;
        u16 x : 12;
        u16 id : 4;
    };
    u8 all[4];
	//u32 all;
};

union gsl_touch_info
{
    struct
    {
        u32 finger_num : 8;
        u32 : 0;
        union gsl_point_data point[5];
    };
    u8 all[24];
};

struct gsl_ts_data {
	union gsl_touch_info *ti;
    	union gsl_touch_info base;
	struct i2c_client *client;
	struct input_dev *idev;
	struct workqueue_struct *wq;
	struct work_struct work;
	unsigned int irq;
	struct early_suspend pm;
//	struct alsps_hw  *hw;
};

/* Fixme mem Alig */
struct fw_data
{
    u32 offset : 8;
    u32 : 0;
    u32 val;
};

#if defined(__DRV_LCD_RESOLUTION_480X800__)
#include "gsl1680_480x800.h"
#elif defined(__DRV_LCD_RESOLUTION_480X854__)
#include "gsl1680_480x854.h"
#else
#include "gsl1680_320x480.h"
#endif
#endif

