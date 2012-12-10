/*
 * =====================================================================================
 * Copyright (C) 2011 Bosch Sensortec GmbH
 *
 *       Filename:  sensor_hw.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  07/23/2012 10:10:19 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Zhengguang.Guo@bosch-sensortec.com
 *
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


#ifndef __SENSOR_HW_H
#define __SENSOR_HW_H
#include <stdint.h>

/* a bitmap of dependencies */
/* NOTE: limitations: hw number limited to 16 */
typedef int16_t hw_dep_set_t;

typedef struct axis_remap {
	unsigned int rx: 2;
	unsigned int ry: 2;
	unsigned int rz: 2;

	unsigned int sx: 2;
	unsigned int sy: 2;
	unsigned int sz: 2;
} axis_remap_t;


struct sensor_hw {
	const char const *name;

	const int32_t type;
	const int32_t id;

	uint32_t available:1;
	uint32_t fd_pollable:1;
	/* NOTE: limitations: */
	uint32_t wakeup_time:10;

	int32_t ref:8;
	uint32_t enabled:1;
	uint32_t drdy:1;
	int32_t delay:10;

	int fd_poll;
	uint32_t ts_last_update;

	void *private_data;

	int (*init)(struct sensor_hw *);
	int (*enable)(struct sensor_hw *, int);
	int (*restore_cfg)(struct sensor_hw *);
	int (*set_delay)(struct sensor_hw *, int);

	int (*get_drdy_status)(struct sensor_hw *);

	int (*get_data_nb)(void *val);
	int (*get_data)(void *val);

	pthread_mutex_t lock_ref;
};


struct sensor_hw_a {
	struct sensor_hw hw;

	uint32_t bw:12;
	uint32_t range:6;
	uint32_t data_bits:5;
	uint32_t place:6;

	int (*set_bw)(struct sensor_hw_a *, uint32_t bw);
	int (*set_range)(struct sensor_hw_a *, uint32_t range);
};


struct sensor_hw_m {
	struct sensor_hw hw;

	uint32_t data_bits:5;
	uint32_t place:6;
};


struct sensor_hw_g {
	struct sensor_hw hw;

	uint32_t bw:12;
	uint32_t range:6;
	uint32_t data_bits:5;
	uint32_t place:6;

	int (*set_bw)(struct sensor_hw_g *, uint32_t bw);
	int (*set_range)(struct sensor_hw_g *, uint32_t range);
};
#endif
