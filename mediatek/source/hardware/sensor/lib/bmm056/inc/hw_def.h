/*
 * =====================================================================================
 * Copyright (C) 2011 Bosch Sensortec GmbH
 *
 *       Filename:  hw_def.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  03/23/2011 05:02:19 PM
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


#ifndef __HW_DEF_H
#define __HW_DEF_H
#include <stdint.h>

#include "sensor_def.h"


typedef int hw_id_t;

#define HW_ID_PART_POS_MAGIC 24
#define HW_ID_PART_LEN_MAGIC 8

#define HW_ID_PART_POS_VENDOR	20
#define HW_ID_PART_LEN_VENDOR	4

#define HW_ID_PART_POS_PRIV	16
#define HW_ID_PART_LEN_PRIV	4

#define HW_ID_PART_POS_MAJOR	8
#define HW_ID_PART_LEN_MAJOR	8

#define HW_ID_PART_POS_MINOR 0
#define HW_ID_PART_LEN_MINOR 8


#define HW_ID_PART(name, val) ((val & ((1 << HW_ID_PART_LEN_##name) - 1)) << HW_ID_PART_POS_##name)

#define HW_ID_PART_MAGIC(magic)		HW_ID_PART(MAGIC, magic)
#define HW_ID_PART_VENDOR(vendor) 	HW_ID_PART(VENDOR, vendor)
#define HW_ID_PART_PRIV(priv) 		HW_ID_PART(PRIV, priv)
#define HW_ID_PART_MAJOR(major) 	HW_ID_PART(MAJOR, major)
#define HW_ID_PART_MINOR(minor) 	HW_ID_PART(MINOR, minor)


#define HW_ID(magic, vendor, priv, major, minor)\
	(HW_ID_PART_MAGIC(magic) | HW_ID_PART_VENDOR(vendor) | HW_ID_PART_PRIV(priv)\
	 | HW_ID_PART_MAJOR(major) | (HW_ID_PART_MINOR(minor)))


#define HW_ID_GET_PART(name, id)	(((1 << HW_ID_PART_LEN_##name) - 1) & (id >> HW_ID_PART_POS_##name))

#define HW_ID_GET_PART_MAGIC(id)	HW_ID_GET_PART(MAGIC, id)
#define HW_ID_GET_PART_VENDOR(id)	HW_ID_GET_PART(VENDOR, id)
#define HW_ID_GET_PART_PRIV(id)		HW_ID_GET_PART(PRIV, id)
#define HW_ID_GET_PART_MAJOR(id)	HW_ID_GET_PART(MAJOR, id)
#define HW_ID_GET_PART_MINOR(id)	HW_ID_GET_PART(MINOR, id)

#define	VENDOR_ID_BST	0
#define	VENDOR_ID_AKM	1
#define	VENDOR_ID_IVS	2


#define HW_ID_A_BMA150	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 3, 0)

#define HW_ID_A_BMA220	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 6, 2, 0)
#define HW_ID_A_BMA222	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 8, 2, 1)
#define HW_ID_A_BMA250	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 2, 2)
#define HW_ID_A_BMA255	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 12, 2, 3)
#define HW_ID_A_BMA280	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 14, 2, 4)
#define HW_ID_A_BMA222E	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 8, 2, 5)
#define HW_ID_A_BMA250E	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 2, 6)
#define HW_ID_A_BMA254	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 12, 2, 7)

#define HW_ID_A_BMC022	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 3, 0)
#define HW_ID_A_BMC022E	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 3, 1)
#define HW_ID_A_BMC050	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 3, 2)
#define HW_ID_A_BMC050E	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 3, 3)
#define HW_ID_A_BMC055	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 3, 4)
#define HW_ID_A_BMC056	HW_ID(SENSOR_MAGIC_A, VENDOR_ID_BST, 10, 3, 5)


#define HW_ID_M_BMC022 HW_ID(SENSOR_MAGIC_M, VENDOR_ID_BST, 13, 3, 0)
#define HW_ID_M_BMC022E HW_ID(SENSOR_MAGIC_M, VENDOR_ID_BST, 13, 3, 1)
#define HW_ID_M_BMC050 HW_ID(SENSOR_MAGIC_M, VENDOR_ID_BST, 13, 3, 2)
#define HW_ID_M_BMC050E HW_ID(SENSOR_MAGIC_M, VENDOR_ID_BST, 13, 3, 3)
#define HW_ID_M_BMC055 HW_ID(SENSOR_MAGIC_M, VENDOR_ID_BST, 13, 3, 4)
#define HW_ID_M_BMC056 HW_ID(SENSOR_MAGIC_M, VENDOR_ID_BST, 13, 3, 5)

#define HW_ID_M_BMM150 HW_ID(SENSOR_MAGIC_M, VENDOR_ID_BST, 13, 1, 0)

#define HW_ID_G_BMG160 HW_ID(SENSOR_MAGIC_G, VENDOR_ID_BST, 16, 0, 0)

#define HW_ID_G_MPU3050 HW_ID(SENSOR_MAGIC_G, VENDOR_ID_IVS, 16, 0, 0)


enum {
	HW_A_RANGE_INVALID = -1,
	/* 0 */
	HW_A_RANGE_2G,
	/* 1 */
	HW_A_RANGE_4G,
	/* 2 */
	HW_A_RANGE_8G,
	/* 3 */
	HW_A_RANGE_16G,
	/* 4 */
	HW_A_RANGE_32G,
	/* 5 */
	HW_A_RANGE_MAX
};


enum {
	HW_A_BW_INVALID = -1,
	/* 0 */
	HW_A_BW_8,
	/* 1 */
	HW_A_BW_16,
	/* 2 */
	HW_A_BW_31,
	/* 3 */
	HW_A_BW_63,
	/* 4 */
	HW_A_BW_125,
	/* 5 */
	HW_A_BW_250,
	/* 6 */
	HW_A_BW_500,
	/* 7 */
	HW_A_BW_1000,
	/* 8 */
	HW_A_BW_2000,
	/* 9 */
	HW_A_BW_MAX
};


enum {
	HW_G_RANGE_INVALID = -1,
	/* 0 */
	HW_G_RANGE_125,
	/* 1 */
	HW_G_RANGE_250,
	/* 2 */
	HW_G_RANGE_500,
	/* 3 */
	HW_G_RANGE_1000,
	/* 4 */
	HW_G_RANGE_2000,
	/* 5 */
	HW_G_RANGE_MAX
};
#endif
