/*
 * =====================================================================================
 * Copyright (C) 2011 Bosch Sensortec GmbH
 *
 *       Filename:  sensor_priv.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  04/21/2011 02:21:48 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Zhengguang.Guo@bosch-Sensortec.com, 
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


#ifndef __SENSOR_PRIV_H
#define __SENSOR_PRIV_H
#include "options.h"
#include "hw_if.h"
#include "hw_info.h"

#define FIFO_CMD "/data/local/tmp/fifo_cmd"
#define FIFO_DAT "/data/local/tmp/fifo_dat"

#define BMM_DRV_IO	"/dev/msensor"

/* definitions specific to hardware, need to be changed accordingly */
#ifdef CFG_DATA_INPUT_SRC_FILE
#define DEV_FILE_ACC "/data/local/tmp/log/" DEV_NAME_A
#define DEV_FILE_MAG "/data/local/tmp/log/" DEV_NAME_M
#define DEV_FILE_GYRO "/data/local/tmp/log/" DEV_NAME_G
#endif /* CFG_DATA_INPUT_SRC_FILE */


#if DAEMON_FOR_MTK
#define SYSFS_PATH_INPUT_DEV 	"/sys/devices/platform"
#else
#define SYSFS_PATH_INPUT_DEV "/sys/class/input"
#endif //DAEMON_FOR_MTK
#define MAX_INPUT_DEV_NUM 32

#define SENSOR_CFG_FILE_SYS_AXIS	"/system/etc/sensor/sensord_cfg_axis"
#define SENSOR_CFG_FILE_SYS_CALIB "/system/etc/sensor/sensord_cfg_calib"
#define SENSOR_CFG_FILE_SYS_PROFILE_CALIB_A "/data/local/tmp/profile_calib_a"
#define SENSOR_CFG_FILE_SYS_PROFILE_CALIB_M "/data/local/tmp/profile_calib_m"
#define SENSOR_CFG_FILE_SYS_PROFILE_CALIB_G "/data/local/tmp/profile_calib_g"
#define SENSOR_CFG_FILE_FAST_CALIB_A "/data/local/tmp/fast_calib_a"

#define DATA_LOG_MARK_A "[a]"
#define DATA_LOG_MARK_M "[m]"
#define DATA_LOG_MARK_G "[g]"
#define DATA_LOG_MARK_O "[o]"


#endif
