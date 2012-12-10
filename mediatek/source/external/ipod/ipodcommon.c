/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#include <stdio.h>
#include <stdlib.h>
#include <utils/Log.h>
#include <fcntl.h>
#include "ipodmain.h"
#include <cutils/properties.h>
#include <linux/android_alarm.h>
#include <sys/reboot.h>
#include <errno.h>
#include <time.h>

#define RADIO_TIMEOUT_SEC 30
#define IPOD_POWER_DOWN_CAP "sys.ipo.pwrdncap"
#define IPOD_RADIO_OFF_STATE "ril.ipo.radiooff"
#define IPOD_RADIO_WAKELOCK "IPOD_RADIO_WAKELOCK"

#define WAKELOCK_ACQUIRE_PATH "/sys/power/wake_lock"
#define WAKELOCK_RELEASE_PATH "/sys/power/wake_unlock"

#define CHARGER_OVER_VOLTAGE 7000
#define CHARGER_VOLTAGE_PATH "/sys/class/power_supply/battery/ChargerVoltage"

/*
  * IPOD_RADIO_OFF_STATE
  * value:
  *       0: default value, don¡¦t bypass
  *       1: ShutdownThread ignores mountservice.shutdown only
  *       2: ShutdownThread ignores wait radio off. IPO will take over this job.
  *       3: ShutdownThread ignores both mountservice.shutdown and wait radio off. IPO will take over this job.
  */

#ifdef IPO_GOTO_POWEROFF
#define POWEROFF_ALARM_CHECK_TOLERANCE 60 //sec
#define POWEROFF_ALARM_TRIGGER_TOLERANCE 60 //sec
struct timespec ts_pwroff, ts_setOff;
int setOff = true;
#endif

int inCharging;

#define VERBOSE_OUTPUT

void set_str_value(const char * path, const char * str)
{
	write_to_file(path, str, strlen(str));
}

void acquire_wakelock()
{

	set_str_value(WAKELOCK_ACQUIRE_PATH, IPOD_RADIO_WAKELOCK);
}

void release_wakelock()
{
	set_str_value(WAKELOCK_RELEASE_PATH, IPOD_RADIO_WAKELOCK);
}

int write_to_file(const char* path, const char* buf, int size)
{
    if (!path) {
		SXLOGE("null path to write");
        return 0;
    }
#ifdef VERBOSE_OUTPUT
	SXLOGI("%s: path: %s, buf: %s, size: %d\n",__FUNCTION__, path ,buf, size);
#endif

    int fd = open(path, O_RDWR);
    if (fd == -1) {
        SXLOGE("Could not open '%s'\n", path);
		return 0;
    }
    
    int count = write(fd, buf, size); 
	if (count != size) {
        SXLOGE("write file (%s) fail, count: %d\n", path, count);
		return 0;
    }

    close(fd);
    return count;
}


void set_int_value(const char * path, const int value)
{
	char buf[32];
	sprintf(buf, "%d", value);
#ifdef VERBOSE_OUTPUT
	SXLOGI("%s: %s, %s \n",__FUNCTION__, path ,buf);
#endif
	write_to_file(path, buf, strlen(buf));
}

 /*   return value:
  *         0, error or read nothing
  *        !0, read counts
  */
int read_from_file(const char* path, char* buf, int size)
{
    if (!path) {	
		return 0;
    }

    int fd = open(path, O_RDONLY);
    if (fd == -1) {
		return 0;
    }
    
    int count = read(fd, buf, size); 
    if (count > 0) {
        count = (count < size) ? count : size - 1;
        while (count > 0 && buf[count-1] == '\n') count--;
        buf[count] = '\0';
    } else {
        buf[0] = '\0';
    }

    close(fd);
    return count;
}

int get_int_value(const char * path)
{
	int size = 32;
	char buf[size];
	if(!read_from_file(path, buf, size))
		return 0;
	return atoi(buf);
}


static void* test_thread_routine(void *arg)
{	
	while (1) 
	{
		if (ipod_trigger_chganim == NULL )
		{
			SXLOGE("ipod_trigger_chganim is NULL, test abort");
			return 0;
		}

		SXLOGI(" trigger charge animation without reset timer after 4 sec");
		usleep(4*1000*1000);
		SXLOGI(" trigger charge animation without reset timer");
		ipod_trigger_chganim(0);
		usleep(20*1000*1000);

		SXLOGI(" trigger charge animation with reset timer after 4 sec");
		usleep(4*1000*1000);
		SXLOGI(" trigger charge animation with reset timer");
		ipod_trigger_chganim(1);
		usleep(20*1000*1000);
	}
	return 0;
}

void test_trigger()
{
	int ret = 0;
	pthread_attr_t attr;
	pthread_t test_thread;

	pthread_attr_init(&attr);
	
	ret = pthread_create(&test_thread, &attr, test_thread_routine, NULL);
	if (ret != 0) 
	{
		SXLOGE("create test pthread failed.\n");
	}
}

#ifdef IPO_GOTO_POWEROFF
#define IPOD_POWER_OFF_ALARM "sys.power_off_alarm"

int getPowerOffAlarm()
{
	char buf[PROPERTY_VALUE_MAX];
	unsigned long time = 0;
	if(property_get(IPOD_POWER_OFF_ALARM,buf,"0")) {
		time = atol(buf);
		if(time > 0) {
			ts_pwroff.tv_sec = time;
			ts_pwroff.tv_nsec = 0;
			SXLOGI("found power off alarm: %ld \n",time);
			return true;
		}
	}
	return false;
}

int clearPowerOffAlarmProperty()
{
	char buf[PROPERTY_VALUE_MAX];
	unsigned long time = 0;
	if(property_get(IPOD_POWER_OFF_ALARM,buf,"0")) {
		time = atol(buf);
		if(time > 0) {
			SXLOGI("reset power off alarm systemproperty\n");
			property_set(IPOD_POWER_OFF_ALARM,"0");
			return true;
		}
	}
	return false;
}

int getTime(struct timespec *ts)
{
	time_t t;

	time(&t);
	ts->tv_sec = t;

	SXLOGI("getTime: %ld", ts->tv_sec);
	return true;
}

void enablePowerOff(int offTimeSec)
{
	setOff = true;

	if (getTime(&ts_setOff)) {
		if (getPowerOffAlarm()) {
			// we have power off alarm set from AlarmManagerService.
			if ((ts_pwroff.tv_sec - ts_setOff.tv_sec) < (offTimeSec - POWEROFF_ALARM_CHECK_TOLERANCE)) {
				// If the power on alarm is set before the offTimeSec-60sec,
				// no need to set extra alarm to power off device because we will get power on
				// before we want to real power off in IPO.
				// The 60sec is the tolerance time. Suggest >= 60sec.
				setOff = false;
				ts_setOff.tv_sec = 0;
			}
		}
		if (setOff) {
			ts_setOff.tv_sec += offTimeSec;
			params[PARAM_PWROFF_TIME] = ts_setOff.tv_sec;
			SXLOGI("set power off time: %ld", params[PARAM_PWROFF_TIME]);
		}
	}
}

void checkPowerOff()
{
	struct timespec ts;
	if (setOff) {
		if (getTime(&ts)) {
			SXLOGI("checkPowerOff, now: %ld, set %ld", ts.tv_sec, ts_setOff.tv_sec);
			if (labs(ts.tv_sec - ts_setOff.tv_sec) < POWEROFF_ALARM_TRIGGER_TOLERANCE) {
				// If alarm is triggered and the trigger time is +-POWEROFF_ALARM_CHECK_TOLERANCE 
				// sec to the expected real power off time, do the power off procedure.
				SXLOGI("IPO shutdown device...");
				//reboot(RB_POWER_OFF);
				property_set("ctl.start", "shutdown"); // use shutdown thead to power down.
				while(1) usleep(1000*1000);
			}
		}
	}
}
#endif

void updateTbWifiOnlyMode()
{
	params[PARAM_TB_WIFI_ONLY] = 1;
}

int status_cb(int event, int data1, int data2)
{
	/*
	 * DO NOT BLOCK THIS FUNCTION!
	 */
	 
	SXLOGI("status_cb: %d, %d, %d", event, data1, data2); 

	switch (event) {
            case EVENT_PREBOOT_IPO:
#ifdef IPO_GOTO_POWEROFF
                if (data1 == 1) //after preboot_ipo intent is sent.
                    clearPowerOffAlarmProperty();			
#endif
                break;

            case EVENT_BOOT_IPO:
                break;

            case EVENT_ALARM_RTC:
#ifdef IPO_GOTO_POWEROFF		
                checkPowerOff();
#endif
                break;

            case EVENT_DRAW_CHARGING_ANIM:
                inCharging = data1; // 1: in charging
                break;
			
            case EVENT_KEY_PRESS:
#if 0
                if (data1 == KEY_HP && 
                    data2 == 1 &&
                    inCharging)
                    ipod_trigger_chganim(TRIGGER_ANIM_STOP);
#endif
                break;

            case EVENT_UEVENT_IN:
#if 0
            // example when USB cable is, boot up the device
#define USB_ONLINE_PATH "/sys/class/power_supply/usb/online"
                if (get_int_value(USB_ONLINE_PATH)) {
                    ipod_trigger_chganim(TRIGGER_NORMAL_BOOT);
                }
#endif
                break;
            default:
                break;
	}
	return 0;
}

int getPowerDownCap()
{
	char buf[PROPERTY_VALUE_MAX];
	unsigned int value = 0;
	if(property_get(IPOD_POWER_DOWN_CAP, buf, "0")) {
		value = atoi(buf);
		if(value == 2 || value == 3) {
			SXLOGI("radio off check is on (%d) \n",value);
			return true;
		}
	}
	SXLOGI("radio off check is off (%d) \n",value);
	return false;
}

int getRadioOffState()
{
	char buf[PROPERTY_VALUE_MAX];
	unsigned int value = 0;
	if(property_get(IPOD_RADIO_OFF_STATE, buf, "0")) {
		value = atoi(buf);
		return value;
	}
	return false;
}

static void* radiooff_check_routine(void *arg)
{	
	int i=0;
	do {
		sleep(1);
		if (getRadioOffState()) {
			SXLOGE("radio off done (%d sec).\n", i);
			release_wakelock();
			pthread_exit(NULL);
		}
	}while(++i < RADIO_TIMEOUT_SEC);
	SXLOGE("radio off timeout (%d sec).\n", RADIO_TIMEOUT_SEC);
	property_set("ctl.start", "shutdown");
	return 0;
}

void radiooff_check()
{
	int ret = 0;
	pthread_attr_t attr;
	pthread_t checkradiooff_thread;

	if (!getPowerDownCap())
		return;

	acquire_wakelock();
	pthread_attr_init(&attr);
	
	ret = pthread_create(&checkradiooff_thread, &attr, radiooff_check_routine, NULL);
	if (ret != 0) 
	{
		SXLOGE("create radio check pthread failed.\n");
		property_set("ctl.start", "shutdown");
	}
}

/*
 * return value:
 *     1: over voltage
 *     0: normal voltage
 */
int get_ov_status()
{
	int voltage = get_int_value(CHARGER_VOLTAGE_PATH);
	SXLOGI("charger voltage: %d\n",voltage);

	if (voltage >= CHARGER_OVER_VOLTAGE) {
		return 1;
	}
	return 0;
}

