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

#include "util_misc.h"

#define INTV_PROC_MIN 5

enum CHANNEL_STATE {
	CHANNEL_STATE_SLEEP = 0, 
	CHANNEL_STATE_NORMAL,
	CHANNEL_STATE_BG, 
};


struct channel_cfg {
	/* sensor provider */
	char const *sp_name;

	uint16_t availability:3;
	uint16_t calib_bg:1;

	/* NOTE: limitations */
	uint16_t interval_min;
	/* NOTE: limitations */
	uint16_t interval_max;

	hw_dep_set_t dep_hw;
};


struct channel {
	const char const *name;
	/* NOTE: limitations */
	const int32_t type;
	const int8_t handle;

	struct channel_cfg cfg;

	volatile int32_t prev_state:4;
	volatile int32_t state:4;

	int32_t data_status:4;
	/* NOTE: limitations */
	volatile int16_t interval;

	/* NOTE: limitations */
	uint32_t ts_last_ev;

	struct sensor_provider *sp;
	void *private_data;
	struct list_node client;
	pthread_mutex_t lock_state;

	int (*init)(struct channel *);
	void (*destroy)();
	/* mandatory */
	int (*get_data)(void *, int);
	/* some sensors might be enabled multiple times */
	/* optional */
};


extern int channel_cntl_init();
extern void channel_cntl_destroy();
extern int channel_get_state(int handle);
extern int channel_get_cfg(int handle, struct channel_cfg *cfg);
extern int channel_set_cfg(int handle, const struct channel_cfg *cfg);

extern void channel_cntl_dump();

#if SPT_SENSOR_A
extern int channel_init_a(struct channel *);
extern void channel_destroy_a();
extern int get_data_a(void *buf, int n);	/* acceleration */
extern int channel_enable_a(int);
extern void on_interval_changed_a(int *);
#endif

#if SPT_SENSOR_D
extern int channel_init_d(struct channel *);
extern void channel_destroy_d();
extern int get_data_d(void *buf, int n);	/* distance */
extern int channel_enable_d(int);
extern void on_interval_changed_d(int *);
extern void get_curr_data_nb_d(void *data);
#endif

#if SPT_SENSOR_G
extern int channel_init_g(struct channel *);
extern void channel_destroy_g();
extern int get_data_g(void *buf, int n);	/* gyro */
extern int channel_enable_g(int);
extern void on_interval_changed_g(int *);
#endif

#if SPT_SENSOR_L
extern int channel_init_l(struct channel *);
extern void channel_destroy_l();
extern int get_data_l(void *buf, int n);	/* light */
extern int channel_enable_l(int);
extern void on_interval_changed_l(int *);
extern void get_curr_data_nb_l(void *data);
#endif

#if SPT_SENSOR_M
extern int channel_init_m(struct channel *);
extern void channel_destroy_m();
extern int get_data_m(void *buf, int n);	/* magnetic */
extern int channel_enable_m(int);
extern void on_interval_changed_m(int *);
#endif

#if SPT_SENSOR_O
extern int channel_init_o(struct channel *);
extern void channel_destroy_o();
extern int get_data_o(void *buf, int n);	/* orientation */
extern void on_interval_changed_o(int *);
extern int channel_enable_o(int);
#endif

#if SPT_SENSOR_P
extern int channel_init_p(struct channel *);
extern void channel_destroy_p();
extern int get_data_p(void *buf, int n);	/* pressure */
extern int channel_enable_p(int);
extern void on_interval_changed_p(int *);
#endif

#if SPT_SENSOR_RO
extern int channel_init_ro(struct channel *);
extern void channel_destroy_ro();
extern int get_data_ro(void *buf, int n);	/* raw orientation, deprecated*/
extern int channel_enable_ro(int);
extern void on_interval_changed_ro(int *);
#endif

#if SPT_SENSOR_T
extern int channel_init_t(struct channel *);
extern void channel_destroy_t();
extern int get_data_t(void *buf, int n);	/* temperature */
extern int channel_enable_t(int);
extern void on_interval_changed_t(int *);
#endif

#if SPT_SENSOR_VG
extern int channel_init_vg(struct channel *);
extern void channel_destroy_vg();
extern int get_data_vg(void *buf, int n);	/* gravity */
extern int channel_enable_vg(int);
extern void on_interval_changed_vg(int *);
#endif

#if SPT_SENSOR_VLA
extern int channel_init_vla(struct channel *);
extern void channel_destroy_vla();
extern int get_data_vla(void *buf, int n);	/* linear acc */
extern int channel_enable_vla(int);
extern void on_interval_changed_vla(int *);
#endif

#if SPT_SENSOR_VRV
extern int channel_init_vrv(struct channel *);
extern void channel_destroy_vrv();
extern int get_data_vrv(void *buf, int n);	/* rotation vector */
extern int channel_enable_vrv(int);
extern void on_interval_changed_vrv(int *);
#endif


void fusion_proc_data(int ts);
int get_hint_interval_fusion();
#endif


int channel_get_num();
struct channel *channel_get_ch(int handle);
