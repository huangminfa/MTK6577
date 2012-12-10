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

/*
 * =====================================================================================
 * Copyright (C) 2011 Bosch Sensortec GmbH
 *
 *       Filename:  channel.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  05/09/2011 12:37:59 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Zhengguang.Guo@bosch-sensortec.com
 *        Company:  
 *
 * =====================================================================================
 */

/*************************************************************************************************/
/*  Disclaimer
*
* Common:
* Bosch Sensortec products are developed for the consumer goods industry. They may only be used
* within the parameters of the respective valid product data sheet.  Bosch Sensortec products are
* provided with the express understanding that there is no warranty of fitness for a particular purpose.
* They are not fit for use in life-sustaining, safety or security sensitive systems or any system or device
* that may lead to bodily harm or property damage if the system or device malfunctions. In addition,
* Bosch Sensortec products are not fit for use in products which interact with motor vehicle systems.
* The resale and/or use of products are at the purchasers own risk and his own responsibility. The
* examination of fitness for the intended use is the sole responsibility of the Purchaser.
*
* The purchaser shall indemnify Bosch Sensortec from all third party claims, including any claims for
* incidental, or consequential damages, arising from any product use not covered by the parameters of
* the respective valid product data sheet or not approved by Bosch Sensortec and reimburse Bosch
* Sensortec for all costs in connection with such claims.
*
* The purchaser must monitor the market for the purchased products, particularly with regard to
* product safety and inform Bosch Sensortec without delay of all security relevant incidents.
*
* Engineering Samples are marked with an asterisk (*) or (e). Samples may vary from the valid
* technical specifications of the product series. They are therefore not intended or fit for resale to third
* parties or for use in end products. Their sole purpose is internal client testing. The testing of an
* engineering sample may in no way replace the testing of a product series. Bosch Sensortec
* assumes no liability for the use of engineering samples. By accepting the engineering samples, the
* Purchaser agrees to indemnify Bosch Sensortec from all claims arising from the use of engineering
* samples.
*
* Special:
* This software module (hereinafter called "Software") and any information on application-sheets
* (hereinafter called "Information") is provided free of charge for the sole purpose to support your
* application work. The Software and Information is subject to the following terms and conditions:
*
* The Software is specifically designed for the exclusive use for Bosch Sensortec products by
* personnel who have special experience and training. Do not use this Software if you do not have the
* proper experience or training.
*
* This Software package is provided `` as is `` and without any expressed or implied warranties,
* including without limitation, the implied warranties of merchantability and fitness for a particular
* purpose.
*
* Bosch Sensortec and their representatives and agents deny any liability for the functional impairment
* of this Software in terms of fitness, performance and safety. Bosch Sensortec and their
* representatives and agents shall not be liable for any direct or indirect damages or injury, except as
* otherwise stipulated in mandatory applicable law.
*
* The Information provided is believed to be accurate and reliable. Bosch Sensortec assumes no
* responsibility for the consequences of use of such Information nor for any infringement of patents or
* other rights of third parties which may result from its use. No license is granted by implication or
* otherwise under any patent or patent rights of Bosch. Specifications mentioned in the Information are
* subject to change without notice.
*
* It is not allowed to deliver the source code of the Software to any third party without permission of
* Bosch Sensortec.
*/


#ifndef __CHANNEL_H
#define __CHANNEL_H

#include <unistd.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>

#include <linux/fs.h>

#include <fcntl.h>
#include <signal.h>
#include <pthread.h>

#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>

#include "configure.h"
#include "options.h"

enum CHANNEL_STATE {
	CHANNEL_STATE_SLEEP = 0, 
	CHANNEL_STATE_NORMAL,
	CHANNEL_STATE_BG, 
};

struct channel_cfg {
	uint16_t availability:3;
	uint16_t calib_bg:2;

	uint16_t rd_blk:2;

	volatile int16_t interval;
	int16_t interval_min;
	int16_t interval_max;

	/* a bitmap of dependencies */
	int32_t dep_hw;
};

struct channel {
	const char const *name;
	const int8_t handle;

	volatile int started:2;

	volatile int prev_state:4;
	volatile int state:4;

	int data_status:4;

	volatile int blocked:2;

	struct channel_cfg cfg;

	pthread_t ptid;
	int tid;

	pthread_mutex_t mutex_state;
	/* condition for thread */
	pthread_cond_t cond;
	/* mutex for condition */
	pthread_mutex_t mutex_cond;

	int (*init)();
	void (*destroy)();
	void (*get_data)(void *);
	/* some sensors might be enabled multiple times */
	int (*channel_enable)(int);
	void (*on_interval_changed)(int);
};

struct sensor_hw {
	const char const *name;

	const int8_t handle: 8;
	int8_t ref;

	int (*hw_enable)(int);

	pthread_mutex_t mutex_ref;
};


extern int channel_init();
extern void channel_destroy();
extern int channel_get_state(int handle);
extern int channel_get_cfg(int handle, struct channel_cfg *cfg);
extern int channel_set_cfg(int handle, const struct channel_cfg *cfg);

extern void channel_dump();

#if SPT_SENSOR_A
extern int channel_init_a();
extern void channel_destroy_a();
extern void get_data_a(void *buf);	/* acceleration */
extern int hw_enable_a();
extern int hw_enable_a(int);
extern void on_interval_changed_a(int);
#endif

#if SPT_SENSOR_D
extern int channel_init_d();
extern void channel_destroy_d();
extern void get_data_d(void *buf);	/* distance */
extern int hw_enable_d();
extern int channel_enable_d();
extern int hw_enable_d(int);
extern void on_interval_changed_d(int);
extern void get_curr_data_nb_d(void *data);
#endif

#if SPT_SENSOR_G
extern int channel_init_g();
extern void channel_destroy_g();
extern void get_data_g(void *buf);	/* gyro */
extern int hw_enable_g();
extern int hw_enable_g(int);
extern void on_interval_changed_g(int);
#endif

#if SPT_SENSOR_L
extern int channel_init_l();
extern void channel_destroy_l();
extern void get_data_l(void *buf);	/* light */
extern int hw_enable_l();
extern int hw_enable_l(int);
extern void on_interval_changed_l(int);
extern void get_curr_data_nb_l(void *data);
#endif

#if SPT_SENSOR_M
extern int channel_init_m();
extern void channel_destroy_m();
extern void get_data_m(void *buf);	/* magnetic */
extern int hw_enable_m();
extern int hw_enable_m(int);
extern void on_interval_changed_m(int);
#endif

#if SPT_SENSOR_O
extern int channel_init_o();
extern void channel_destroy_o();
extern void get_data_o(void *buf);	/* orientation */
extern int hw_enable_o();
extern int hw_enable_o(int);
extern void on_interval_changed_o(int);
extern int channel_enable_o(int);
#endif

#if SPT_SENSOR_P
extern int channel_init_p();
extern void channel_destroy_p();
extern void get_data_p(void *buf);	/* pressure */
extern int hw_enable_p();
extern int hw_enable_p(int);
extern void on_interval_changed_p(int);
#endif

#if SPT_SENSOR_RO
extern int channel_init_ro();
extern void channel_destroy_ro();
extern void get_data_ro(void *buf);	/* raw orientation, deprecated*/
extern int hw_enable_ro();
extern int hw_enable_ro(int);
extern void on_interval_changed_ro(int);
#endif

#if SPT_SENSOR_T
extern int channel_init_t();
extern void channel_destroy_t();
extern void get_data_t(void *buf);	/* temperature */
extern int hw_enable_t();
extern int hw_enable_t(int);
extern void on_interval_changed_t(int);
#endif

#if SPT_SENSOR_VG
extern int channel_init_vg();
extern void channel_destroy_vg();
extern void get_data_vg(void *buf);	/* gravity */
extern int hw_enable_vg();
extern int hw_enable_vg(int);
extern void on_interval_changed_vg(int);
#endif

#if SPT_SENSOR_VLA
extern int channel_init_vla();
extern void channel_destroy_vla();
extern void get_data_vla(void *buf);	/* linear acc */
extern int hw_enable_vla();
extern int hw_enable_vla(int);
extern void on_interval_changed_vla(int);
#endif

#if SPT_SENSOR_VRV
extern int channel_init_vrv();
extern void channel_destroy_vrv();
extern void get_data_vrv(void *buf);	/* rotation vector */
extern int hw_enable_vrv();
extern int hw_enable_vrv(int);
extern void on_interval_changed_vrv(int);
#endif

#endif
