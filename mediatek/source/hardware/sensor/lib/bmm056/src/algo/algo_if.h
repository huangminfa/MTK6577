/*
 * =====================================================================================
 *
 *       Filename:  algo_if.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  01/06/2012 02:57:18 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Zhengguang.guo@bosch-sensortec.com
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

#ifndef __ALGO_IF_H
#define __ALGO_IF_H

#include "algo/inc/BSC_Library.h"
#include "algo/inc/feature.h"
#include "algo/inc/cust.h"

#define ALGO_CALIB_PROFILE_VER '2'

struct calib_profile {
	ts_calibProfile profile;
	int status;
};

#define ALGO_NUM_DR 8

#define ALGO_DR_1HZ BSC_DATARATE_1HZ
#define ALGO_DR_5HZ BSC_DATARATE_5HZ
#define ALGO_DR_10HZ BSC_DATARATE_10HZ
#define ALGO_DR_20HZ BSC_DATARATE_20HZ
#define ALGO_DR_25HZ BSC_DATARATE_25HZ
#define ALGO_DR_50HZ BSC_DATARATE_50HZ
#define ALGO_DR_100HZ BSC_DATARATE_100HZ
#define ALGO_DR_200HZ BSC_DATARATE_200HZ


#define ALGO_A_OPMODE_UNKNOWN (-1)
#define ALGO_A_OPMODE_SLEEP	BSC_ACCOPMODE_SLEEP
#define ALGO_A_OPMODE_LOWPOWER	BSC_ACCOPMODE_LOWPOWER
#define ALGO_A_OPMODE_REGULAR	BSC_ACCOPMODE_REGULAR

#define ALGO_A_RANGE_2G	BSC_ACCGRANGE_2G
#define ALGO_A_RANGE_4G	BSC_ACCGRANGE_4G
#define ALGO_A_RANGE_8G	BSC_ACCGRANGE_8G
#define ALGO_A_RANGE_16G BSC_ACCGRANGE_16G

#define ALGO_A_BW_7_81HZ 	0
#define ALGO_A_BW_15_63HZ	1
#define ALGO_A_BW_31_25HZ	2
#define ALGO_A_BW_62_50HZ	3
#define ALGO_A_BW_125HZ		4


#define ALGO_M_OPMODE_UNKNOWN (-1)
#define ALGO_M_OPMODE_SLEEP BSC_MAGOPMODE_SLEEP
#define ALGO_M_OPMODE_LOWPOWER BSC_MAGOPMODE_LOWPOWER
#define ALGO_M_OPMODE_REGULAR BSC_MAGOPMODE_REGULAR
#define ALGO_M_OPMODE_HIGHACCURACY BSC_MAGOPMODE_HIGHACCURACY
#define ALGO_M_OPMODE_ALWAYSON BSC_MAGOPMODE_ALWAYSON


void algo_bst_init_stk(void *);
int algo_bst_set_hwid(hw_id_t, hw_id_t, hw_id_t);

#define CFG_HW_DEP_A (1 << SENSOR_HW_TYPE_A)

#define CFG_HW_DEP_G ((1 << SENSOR_HW_TYPE_A)\
		| (1 << SENSOR_HW_TYPE_M))

#define CFG_HW_DEP_M (1 << SENSOR_HW_TYPE_M)

#define CFG_HW_DEP_O ((1 << SENSOR_HW_TYPE_A)\
		| (1 << SENSOR_HW_TYPE_M))

#define CFG_HW_DEP_RO CFG_HW_DEP_O

#define CFG_HW_DEP_VG (1 << SENSOR_HW_TYPE_A)
#define CFG_HW_DEP_VLA (1 << SENSOR_HW_TYPE_A)
#define CFG_HW_DEP_VRV ((1 << SENSOR_HW_TYPE_A)\
		| (1 << SENSOR_HW_TYPE_M))

#endif
