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

/* EasyCASE V6.5 24/03/2011 11:15:11 */
/* EasyCASE O
If=horizontal
LevelNumbers=no
LineNumbers=no
Colors=16777215,0,12582912,12632256,0,0,0,16711680,8388736,0,33023,32768,0,0,0,0,0,32768,12632256,255,65280,255,255,16711935
ScreenFont=System,,100,1,-13,0,700,0,0,0,0,0,0,1,2,1,34,96,96
PrinterFont=Arial,,110,4,-89,0,400,0,0,0,0,0,0,3,2,1,34,600,600
LastLevelId=9 */
/* EasyCASE ( 1 */
/** \mainpage BMC050 eCompass Library API
* 
*       \section intro_sec Introduction
*       The BMC050 eCompass Library provides all necessary functionality for the integration 
*       of the bmc050 electronic compass on the target platform. The library has directly access 
*       to hadware and allows to use the best settings of the bmc050 to provide the optimal orientation data.
*       Furthermore, the customer dependend configuration of the library (like filtering level, per-defined offsets)
*       is possible. The sensor will be calibrated by internal functions. The current status of the data can be read out.
*       
*       \section calibdocu_sec  Sensor Calibration
*       Here description of the principal sensor calibration
*
*       \section filterdocu_sec Orientation Filtering
*       Here description of the principal sensor
*
*       \section disclaimer_sec Disclaimer
*
* Common:
* Bosch Sensortec products are developed for the consumer goods industry. They may only be used
* within the parameters of the respective valid product data sheet.  Bosch Sensortec products are
* provided with the express understanding that there is no warranty of fitness for a particular purpose.
* They are not fit for use in life-sustaining, safety or security sensitive systems or any system or device
* that may lead to bodily harm or property damage if the system or device malfunctions. In addition,
* Bosch Sensortec products are not fit for use in products which interact with motor vehicle systems.
* The resale and/or use of products are at the purchaser’s own risk and his own responsibility. The
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
*               
*       \authors sergej.scheiermann@bosch-sensortec.com; david.job@in.bosch.com
*       \version 2.5
*       \date 2010 - 2011
*/

#ifndef __BMC050COMPASS_LIBRARY_H__
#define __BMC050COMPASS_LIBRARY_H__

/** includes
*/
#include "eCompassDataTypes.h"
#include "eCompassDefine.h"

/** \def Accelerometer Operation Modes 
*/
#define BMC050COMPASS_ACCOPMODE_SLEEP                   0
#define BMC050COMPASS_ACCOPMODE_LOWPOWER                1
#define BMC050COMPASS_ACCOPMODE_REGULAR                 2

/** \def Accelerometer G-Range Configuration
*/
#define BMC050COMPASS_ACCGRANGE_2G                              0
#define BMC050COMPASS_ACCGRANGE_4G                              1
#define BMC050COMPASS_ACCGRANGE_8G                              2
#define BMC050COMPASS_ACCGRANGE_16G                             3

/** \def Sensor Data Output Rate Configuration
*/
#define BMC050COMPASS_DATARATE_1HZ                           0
#define BMC050COMPASS_DATARATE_5HZ                           1
#define BMC050COMPASS_DATARATE_10HZ                          2
#define BMC050COMPASS_DATARATE_20HZ                          3
#define BMC050COMPASS_DATARATE_25HZ                          4
#define BMC050COMPASS_DATARATE_50HZ                          5
#define BMC050COMPASS_DATARATE_100HZ                         6
#define BMC050COMPASS_DATARATE_200HZ                         7

/** \def Magnetometer Operation Modes
*/
#define BMC050COMPASS_MAGOPMODE_SLEEP                   0
#define BMC050COMPASS_MAGOPMODE_LOWPOWER                1
#define BMC050COMPASS_MAGOPMODE_REGULAR                 2
#define BMC050COMPASS_MAGOPMODE_HIGHACCURACY    3
#define BMC050COMPASS_MAGOPMODE_ALWAYSON                4

/** \def Orientation Sensor Operation Modes
*/
#define BMC050COMPASS_ORIENTOPMODE_SLEEP                0
#define BMC050COMPASS_ORIENTOPMODE_REGULAR              1

/** \def Magnetic Gyro Sensor Operation Modes
*/
#define BMC050COMPASS_MGYROOPMODE_SLEEP                0
#define BMC050COMPASS_MGYROOPMODE_REGULAR              1

/** \def Orientation Adaptive Filter Operation Modes
*/
#define BMC050COMPASS_ORIENTFILTERMODE_BYPASS           0
#define BMC050COMPASS_ORIENTFILTERMODE_LOW              1
#define BMC050COMPASS_ORIENTFILTERMODE_MEDIUM           2
#define BMC050COMPASS_ORIENTFILTERMODE_HIGH             3

/** \def Orientation Adaptive Filter Operation Modes
*/
#define BMC050COMPASS_MGYROFILTERMODE_BYPASS           0
#define BMC050COMPASS_MGYROFILTERMODE_NORMAL           1

/** \def Sensor Accuracy Status
*/
#define SENSOR_STATUS_UNRELIABLE        0
#define SENSOR_STATUS_ACCURACY_LOW      1
#define SENSOR_STATUS_ACCURACY_MEDIUM   2
#define SENSOR_STATUS_ACCURACY_HIGH     3

#define SET_ACCURACY_PROFILE_SKIP_SAMPLES	30

/** \struct bmc050compass_t
        \brief BMC050 Compass Device Data Type
*/
typedef struct
   {
   ECOMPASS_U8 accOpmode;  /**< accelerometer operation mode */
   ECOMPASS_U8 accRange;   /**< accelerometer g-range */
   ECOMPASS_U8 accDatarate;        /**< accelerometer output data rate */
   ECOMPASS_U8 accAxisConfig;  /**< accelerometer axis configuration for remap */
   ECOMPASS_U8 accAxisSign;  /**< accelerometer axis sign for remap */
   ECOMPASS_U8 accCorMethod;       /**< sensor parameter correction method: 
                                                                                   0 = no;
                                                                                   1 = offset;
                                                                                   2 = offset + sensitivity; */
   ECOMPASS_U8 accNoiThres;  /**< accelerometer noise threshold */
   
   ECOMPASS_U8 magOpmode;  /**< magnetometer operation mode */
   ECOMPASS_U8 magDatarate;        /**< magnetometer output data rate */
   ECOMPASS_U8 magAxisConfig;  /**< magnetometer axis configuration for remap */
   ECOMPASS_U8 magAxisSign;  /**< magnetometer axis sign for remap */
   ECOMPASS_U8 magCorMethod;       /**< sensor parameter correction method: 
                                                                                   0 = no;
                                                                                   1 = offset;
                                                                                   2 = offset + sensitivity; */
   ECOMPASS_U8 magNoiThres;  /**< magnetometer noise threshold */
   ECOMPASS_U8 orientOpmode;       /**< orientation sensor operation mode */
   ECOMPASS_U8 orientFiltermode;       /**< orientation filter operation mode */
   ECOMPASS_U8 orientHeadNoiThres; /**< heading noise threshold for orientation*/
   ECOMPASS_U8 orientAccNoiThres; /**< accelerometer noise threshold for orientation*/
   ECOMPASS_U8 orientMagNoiThres;  /**< magnetometer noise threshold for orientation*/ 
   
   ECOMPASS_U8 mgyroOpmode;       /**< magnetic gyro sensor operation mode */
   ECOMPASS_U8 mgyroFiltermode;       /**< magnetic gyro filter operation mode */

   ECOMPASS_U8 (*acc_read_xyzdata_func)(ECOMPASS_S16 *x,ECOMPASS_S16 *y,ECOMPASS_S16 *z);  /**< function pointer to accelerometer read data function */
   ECOMPASS_U8 (*acc_get_opmode_func)(ECOMPASS_U8 *opmode);        /**< function pointer to accelerometer read operation mode */
   ECOMPASS_U8 (*acc_set_opmode_func)(ECOMPASS_U8 opmode); /**< function pointer to accelerometer set operation mode function */
   ECOMPASS_U8 (*acc_get_grange_func)(ECOMPASS_U8 *range); /**< function pointer to accelerometer read operation mode */
   ECOMPASS_U8 (*acc_set_grange_func)(ECOMPASS_U8 range);  /**< function pointer to accelerometer set operation mode function */
   ECOMPASS_U8 (*acc_get_bandwidth_func)(ECOMPASS_U8 *bw); /**< function pointer to accelerometer read bandwidth settigns */
   ECOMPASS_U8 (*acc_set_bandwidth_func)(ECOMPASS_U8 bw);  /**< function pointer to accelerometer set bandwidth */
   
   ECOMPASS_U8 (*mag_read_xyzdata_func)(ECOMPASS_S16 *x,ECOMPASS_S16 *y,ECOMPASS_S16 *z);  /**< function pointer to magnetometer read data function */
   ECOMPASS_U8 (*mag_get_opmode_func)(ECOMPASS_U8 *opmode);        /**< function pointer to magnetometer read operation mode */
   ECOMPASS_U8 (*mag_set_opmode_func)(ECOMPASS_U8 opmode); /**< function pointer to magnetometer set operation mode function */
   
   ECOMPASS_U8 (*mag_read_temperature_func)(ECOMPASS_S16 *temp);   /**< function pointer to read temperature data */
   } bmc050compass_t;

/** \struct orientdata_t
        \brief Orientation Data Type
*/
typedef struct
{
   ECOMPASS_S16 heading;   /**< contains heading data scaled by SCALINGFACTOR */
   ECOMPASS_S16 pitch;             /**< contains pitch data scaled by SCALINGFACTOR */
   ECOMPASS_S16 roll;              /**< contains roll data scaled by SCALINGFACTOR */
   
   ECOMPASS_S16 qinc[4];   /**< inclination quaternion: [0] = angle, [1] = x,[2] = y,[3] = z */
   ECOMPASS_S16 qfull[4];  /**< full orientation quaternion: [0] = angle, [1] = x,[2] = y,[3] = z */
} orientdata_t;

/** \struct bmc050compass_data_t 
        \brief BMC050 Compass Data Type
*/
typedef struct
   {
   dataxyz_t raxyz;        /**< raw accelerometer data x,y,z */
   dataxyz_t caxyz;        /**< corrected accelerometer data x,y,z */
   dataxyz_t faxyz;        /**< filtered accelerometer data x,y,z */
   dataxyz_t inphase_faxyz;        /**< filtered accelerometer data x,y,z */        
   
   dataxyz_t rmxyz;        /**< raw magnetometer data x,y,z */
   dataxyz_t cmxyz;        /**< corrected magnetometer data x,y,z */
   dataxyz_t fmxyz;        /**< filtered magnetometer data x,y,z */ 
   
   orientdata_t  rorient;  /**< raw orientation data */
   orientdata_t  forient;  /**< filtered orientation data */
   orientdata_t  mgorient;  /**< filtered magnetic gyro orientation data */
   
   /**     \todo [not supported ] 
   */
   dataxyz_t mgyro;        /**< angular rates based on the magnetic gyro */
   
   /**     \todo [not supported]
   */
   dataxyz_t linacc;       /**< linear acceleration data x,y,z */
} bmc050compass_data_t;

/** \struct sensorProfile_t 
        \brief Accelerometer & Magnetometer Configuration Profile Structure
*/
typedef struct
{
	ECOMPASS_U8 axisConfig; /**< axis re-mapping configuration: 
                                                                                   xxxx xx00 = no; xxxx xx01 = (x = y); xxxx xx10 = (x = z); 
                                                                                   xxxx 00xx = no; xxxx 01xx = (y = x); xxxx 10xx = (y = z);
                                                                                   xx00 xxxx = no; xx01 xxxx = (z = x); xx10 xxxx = (z = y); */
	ECOMPASS_U8 axisSign;   /**< axis sign configuration:
                                                                                   xxxx xxx0 = (x positive); xxxx xxx1 = (x negative);
                                                                                   xxxx xx0x = (y positive); xxxx xx1x = (y negative);
                                                                                   xxxx x0xx = (z positive); xxxx x1xx = (z negative); */
   	ECOMPASS_U8 corMethod;  /**< sensorn parameter correction method: 
                                                                                   0 = no;
                                                                                   1 = offset;
                                                                                   2 = offset + sensitivity; */
   	ECOMPASS_U8 noiThres;           /**< sensor system noise threshold for calibration & accuracy */
} sensorConfigProfile_t;

/** \struct sensorProfile_t 
        \brief Accelerometer & Magnetometer Calibration Profile Structure
*/
typedef struct
{
	ECOMPASS_S16 field;             /**< sensor field: magnetometer = magnetic field (uT); accelerometer = gravity (mg) */
	ECOMPASS_S16 offx;              /**< sensor x-axis offset (mg) / (uT) */
	ECOMPASS_S16 offy;              /**< sensor y-axis offset (mg) / (uT) */
	ECOMPASS_S16 offz;              /**< sensor z-axis offset (mg) / (uT) */
	ECOMPASS_U8 sensx;              /**< sensor x-axis sensitivity correction factor (0..255) */
	ECOMPASS_U8 sensy;              /**< sensor y-axis sensitivity correction factor (0..255) */
	ECOMPASS_U8 sensz;              /**< sensor z-axis sensitivity correction factor (0..255) */
	ECOMPASS_U8 calAccur;           /**< sensor calibration accuracy: see sensor status definition */
}sensorCalibProfile_t;

/** \struct orientProfile_t
        \brief Orientation Sensor Configuration Profile
*/
typedef struct
{
	ECOMPASS_U8 headNoiThres;       /**< heading noise threshold */
   	ECOMPASS_U8 accNoiThres;        /**< accelerometer noise system threshold */
   	ECOMPASS_U8 magNoiThres;        /**< magnetometer noise system threshold */
}orientProfile_t;

/** Global Variables & Objects
 \todo will be part of bmc050compasslib.c
*/
// bmc050compass_t *p_bmc050compass;    /**> bmc050 compass object*/
// bmc050compass_data_t *p_bmc050compass_data;  /**> contains bmc050 compass data */

/************************************************************************************
                                                ECOMPASS LIBRARY API CALLS
*************************************************************************************/

/** \fn ECOMPASS_U8 bmc050compass_init()
        \brief eCompass Lib: Initialization of Library
*/
ECOMPASS_U8 bmc050compass_init(void);

/** \example eCompass Library Initialization
*       // 1. Step: initialize library
*       bmc050compass_init();
*       // 2. Step: initialize the sensors access function pointers with your OS functions
*       p_bmc050compass->acc_read_xyzdata_func = your_OS_func_read_accel_xyzdata;
*       p_bmc050compass->acc_get_opmode_func = your_OS_func_read_accel_opmode;
*       p_bmc050compass->acc_set_opmode_func = your_OS_func_write_accel_opmode;
*       p_bmc050compass->acc_get_grange_func = your_OS_func_read_accel_range;
*       p_bmc050compass->acc_set_grange_func = your_OS_func_write_accel_range;
*       p_bmc050compass->acc_get_bandwidth_func = your_OS_func_read_accel_bandwidth;
*       p_bmc050compass->acc_set_bandwidth_func = your_OS_func_write_accel_bandwidth;
*       
*       p_bmc050compass->mag_read_xyzdata_func = your_OS_func_read_mag_xyzdata;
*       p_bmc050compass->mag_get_opmode_func = your_OS_func_read_mag_opmode;
*       p_bmc050compass->mag_set_opmode_func = your_OS_func_write_mag_opmode;
*       
*       p_bmc050compass->mag_read_temperature_func = your_OS_func_read_mag_temperature;
*       
*       // 3. Configure Operation Mode of Accelerometer, Magnetometer, Orientation Sensor etc...
*       bmc050compass_set_accopmode(mode);
*       bmc050compass_set_accdatarate(rate);
*       bmc050compass_set_magopmode(mode);
*       bmc050compass_set_orientopmode(mode);   
*/

/** \fn ECOMPASS_U8 bmc050compass_run(ECOMPASS_S32 timerValueInMSec)
        \brief Main Library Process (up to 5ms calling rate = 200Hz polling rate for accelerometer)
        \param timerValueInMSec  timer value in microseconds
        \return error code
 */
ECOMPASS_U8 bmc050compass_run(ECOMPASS_S32 timerValueInMSec);   /**< timer value in microseconds */

/**     \section accelerometer_sensor_api BMC050 Compass Library: Accelerometer Interface
        This section contains all function for control of accelerometer & accessing of the related data.
*/

/** \fn ECOMPASS_U8 bmc050compass_set_accopmode(ECOMPASS_U8 opmode)
        \brief Set Accelerometer Operation Mode
        \param opmode operation mode of accelerometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_accopmode(ECOMPASS_U8 opmode);

/** \fn ECOMPASS_U8 bmc050compass_get_accopmode(ECOMPASS_U8 *opmode)
        \brief Get Accelerometer Operation Mode
        \param *opmode operation mode of accelerometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_accopmode(ECOMPASS_U8 *opmode);

/** \fn ECOMPASS_U8 bmc050compass_set_accrange(ECOMPASS_U8 range)
        \brief Set Accelerometer Range
        \param range measurement g-range of accelerometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_accrange(ECOMPASS_U8 range);

/** \fn ECOMPASS_U8 bmc050compass_get_accrange(ECOMPASS_U8 *range);
        \brief Get Accelerometer Range
        \param *range of accelerometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_accrange(ECOMPASS_U8 *range);

/*! \fn ECOMPASS_U8 bmc050compass_set_accdatarate(ECOMPASS_U8 rate)
        \brief Set Accelerometer Data Output Rate
        \param rate data output rate of accelerometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_accdatarate(ECOMPASS_U8 rate);

/*! \fn ECOMPASS_U8 bmc050compass_get_accdatarate(ECOMPASS_U8 *rate)
        \brief Returns Current Data Output Rate of Accelerometer
        \param *rate data output rate of accelerometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_accdatarate(ECOMPASS_U8 *rate);

/*! \fn ECOMPASS_U8 bmc050compass_get_accconfigprofile(sensorConfigProfile_t *configProfile)
    \brief Returns Current Configuration Profile of Accelerometer
    \param *configProfile pointer to configuration profile
        \see sensorProfile_t
*/
ECOMPASS_U8 bmc050compass_get_accconfigprofile(sensorConfigProfile_t *configProfile);

/*! \fn ECOMPASS_U8 bmc050compass_set_accconfigprofile(sensorConfigProfile_t configProfile)
    \brief Set Current Configuration Profile of Accelerometer
    \param *configProfile pointer to configuration profile
        \see sensorProfile_t
*/
ECOMPASS_U8 bmc050compass_set_accconfigprofile(sensorConfigProfile_t configProfile);

/*! \fn ECOMPASS_U8 bmc050compass_get_acccalibprofile(sensorCalibProfile_t *configProfile)
    \brief Returns Current Calibration Profile of Accelerometer
    \param *configProfile pointer to configuration profile
        \see sensorProfile_t
*/
ECOMPASS_U8 bmc050compass_get_acccalibprofile(sensorCalibProfile_t *calibProfile);

/*! \fn ECOMPASS_U8 bmc050compass_set_acccalibprofile(sensorCalibProfile_t configProfile)
    \brief Set Current Calibration Profile of Accelerometer
    \param *configProfile pointer to configuration profile
        \see sensorProfile_t
*/
ECOMPASS_U8 bmc050compass_set_acccalibprofile(sensorCalibProfile_t calibProfile);

/*! \fn ECOMPASS_U8 bmc050compass_acc_reset()
    \brief Reset Accelerometer Sensor & Configuration Parameter
*/
ECOMPASS_U8 bmc050compass_acc_reset(void);

/*! \fn ECOMPASS_U8 bmc050compass_get_accrawdata(dataxyz_t *xyz)
        \brief Returns Raw Accelerometer Data
        \param *xyz raw accelerometer data of bmc050
*/
ECOMPASS_U8 bmc050compass_get_accrawdata(dataxyz_t *xyz);

/*! \fn ECOMPASS_U8 bmc050compass_get_acccordata(dataxyz_t *xyz)
        \brief Returns Corrected (Offset) Accelerometer Data
        \param *xyz corrected accelerometer data of bmc050
*/
ECOMPASS_U8 bmc050compass_get_acccordata(dataxyz_t *xyz);

/*! \fn ECOMPASS_U8 bmc050compass_get_accfiltdata(dataxyz_t *xyz)
        \brief Returns Filtered Accelerometer Data
        \param *xyz filtered accelerometer data of bmc050
*/
ECOMPASS_U8 bmc050compass_get_accfiltdata(dataxyz_t *xyz);

/*! \fn ECOMPASS_U8 bmc050compass_get_accdatastatus(ECOMPASS_U8 *status)
        \brief Returns Calibration Accuracy Status of Accelerometer Data
        \param *status calibration accuracy of accelerometer
        \see sensor_calib_accur_status
*/
ECOMPASS_U8 bmc050compass_get_accdatastatus(ECOMPASS_U8 *status);

/*! \fn ECOMPASS_U8 bmc050compass_get_accremapparam(ECOMPASS_U8 *axisConfig,ECOMPASS_U8 *axisSign);
        \brief Returns Axis Configuration & sign of Accelerometer Data
        \param *axisConfig -> axis configuration parameter for accelerometer data
                   *axisSign   -> sign configuration parameter for accelerometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_accremapparam(ECOMPASS_U8 *v_accaxisConfig_u8r,ECOMPASS_U8 *v_accaxisSign_u8r);


/*! \fn ECOMPASS_U8 bmc050compass_set_accremapparam(ECOMPASS_U8 axisConfig,ECOMPASS_U8 axisSign);
        \brief Set axis Configuration & sign of Accelerometer Data
        \param axisConfig -> axis configuration parameter for accelerometer data
                   axisSign   -> sign configuration parameter for accelerometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_accremapparam(ECOMPASS_U8 v_accaxisConfig_u8r,ECOMPASS_U8 v_accaxisSign_u8r);

/*! \fn ECOMPASS_U8 bmc050compass_get_acccorrectionmethod(ECOMPASS_U8 *method)
        \brief Returns Correction Method used for accelerometer data (bypass/offset/sensitivity&offset)
        \param *method -> correction method used for accelerometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_acccorrectionmethod(ECOMPASS_U8 *v_method_u8r);

/*! \fn ECOMPASS_U8 bmc050compass_set_acccorrectionmethod(ECOMPASS_U8 method)
        \brief Set the Correction Method to be used for accelerometer data (bypass/offset/sensitivity&offset)
        \param method -> correction method to be used for accelerometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_acccorrectionmethod(ECOMPASS_U8 v_method_u8r);

/*! \fn ECOMPASS_U8 bmc050compass_get_accnoisethreshold(ECOMPASS_U8 *thres)
        \brief Returns noise threshold set for the accelerometer data
        \param *thres -> noise threshold for the accelerometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_accnoisethreshold(ECOMPASS_U8 *v_thres_u8r);

/*! \fn ECOMPASS_U8 bmc050compass_set_accnoisethreshold(ECOMPASS_U8 thres)
        \brief Set the noise threshold for the accelerometer data
        \param thres -> noise threshold to be used for the accelerometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_accnoisethreshold(ECOMPASS_U8 v_thres_u8r);
/*! \fn ECOMPASS_U8 bmc050compass_set_acccalibmode(ECOMPASS_U8 mode)
        \brief set the conrols active for observer, calib and the method according to the mode
        \param mode -> the set mode to be used for calibration activation for accelerometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_acccalibmode(ECOMPASS_U8 v_mode_s8r);
/*! \fn ECOMPASS_U8 bmc050compass_get_acccalibparam(dataxyz_t *off,dataxyz_t *sens)
        \brief get the estimated offset and sensitivity parameters of the accelerometer
        \param dataxyz_t *off -> returns the estimated offset of the accelerometer
                   dataxyz_t *sens -> returns the estimated sensitivity of the accelerometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_acccalibparam(dataxyz_t *off,dataxyz_t *sens);
/*! \fn ECOMPASS_U8 bmc050compass_acccalib_reset()
        \brief reset the accelerometer calibration module including calibration parameter
           observation objects, calibration objects, internal states
        \param none
        \return error code
*/
ECOMPASS_U8 bmc050compass_acccalib_reset(void);

/**     \section magnetometer_sensor_api BMC050 Compass Library: Magnetometer Interface
        This section contains all functions for control of magnetometer & accessing of the related data.
        \todo [configuration profile is to be done!]
*/

/** \fn ECOMPASS_U8 bmc050compass_set_magopmode(ECOMPASS_U8 opmode)
        \brief Set Magnetometer Operational Mode
        \param opmode magnetometer operation mode
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_magopmode(ECOMPASS_U8 opmode);

/** \fn ECOMPASS_U8 bmc050compass_get_magopmode(ECOMPASS_U8 *opmode)
        \brief Get Magnetometer Operational Mode
        \param *opmode magnetometer operation mode
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_magopmode(ECOMPASS_U8 *opmode);

/*! \fn ECOMPASS_U8 bmc050compass_set_magdatarate(ECOMPASS_U8 rate)
        \brief Set Magnetometer Data Output Rate
        \param rate data output rate of magnetometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_magdatarate(ECOMPASS_U8 rate);

/*! \fn ECOMPASS_U8 bmc050compass_get_magdatarate(ECOMPASS_U8 *rate)
        \brief Returns Current Data Output Rate of magnetometer
        \param *rate data output rate of magnetometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_magdatarate(ECOMPASS_U8 *rate);

/** \fn ECOMPASS_U8 bmc050compass_get_magprofile(sensorConfigProfile_t *configProfile)
        \brief Get Magnetometer Configuration Profile
        \param *configProfile pointer to magnetometer configuration profile
        \return error code
        \see sensorProfile_t
*/
ECOMPASS_U8 bmc050compass_get_magconfigprofile(sensorConfigProfile_t *configProfile);

/** \fn ECOMPASS_U8 bmc050compass_set_magconfigprofile(sensorConfigProfile_t configProfile)
        \brief Set Magnetometer Configuration Profile
        \param configProfile magnetometer configuration profile
        \return error code
        \see sensorProfile_t
*/
ECOMPASS_U8 bmc050compass_set_magconfigprofile(sensorConfigProfile_t configProfile);

/** \fn ECOMPASS_U8 bmc050compass_get_magprofile(sensorCalibProfile_t *calibProfile)
        \brief Get Magnetometer Calibration Profile
        \param *configProfile pointer to magnetometer configuration profile
        \return error code
        \see sensorProfile_t
*/
ECOMPASS_U8 bmc050compass_get_magcalibprofile(sensorCalibProfile_t *calibProfile);

/** \fn ECOMPASS_U8 bmc050compass_set_magcalibprofile(sensorCalibProfile_t calibProfile)
        \brief Set Magnetometer Calibration Profile
        \param configProfile magnetometer configuration profile
        \return error code
        \see sensorProfile_t
*/
ECOMPASS_U8 bmc050compass_set_magcalibprofile(sensorCalibProfile_t calibProfile);


/** \fn ECOMPASS_U8 bmc050compass_mag_reset()
        \brief Reset Magnetometer Sensor & Configuration
        \return error code
*/
ECOMPASS_U8 bmc050compass_mag_reset(void);

/** \fn ECOMPASS_U8 bmc050compass_get_magrawdata(dataxyz_t *xyz)
        \brief Returns Magnetometer Raw Data
        \param *xyz pointer to raw data
        \return error code
        \see dataxyz_t
*/
ECOMPASS_U8 bmc050compass_get_magrawdata(dataxyz_t *xyz);

/** \fn ECOMPASS_U8 bmc050compass_get_magcordata(dataxyz_t *xyz)
        \brief Returns Magnetometer Corrected (Offset) Data
        \param *xyz pointer to corrected data
        \return error code
        \see dataxyz_t
*/
ECOMPASS_U8 bmc050compass_get_magcordata(dataxyz_t *xyz);

/** \fn ECOMPASS_U8 bmc050compass_get_magfiltdata(dataxyz_t *xyz)
        \brief Returns Magnetometer Filtered Data
        \param *xyz pointer to raw data
        \return error code
        \see dataxyz_t
*/
ECOMPASS_U8 bmc050compass_get_magfiltdata(dataxyz_t *xyz);

/** \fn ECOMPASS_U8 bmc050compass_get_magdatastatus(ECOMPASS_U8 *status)
        \brief Returns Magnetometer Calibration Accuracy Status
        \param *xyz pointer to raw data
        \return error code
        \see sensor_calib_accur_status
*/
ECOMPASS_U8 bmc050compass_get_magdatastatus(ECOMPASS_U8 *status);

/*! \fn ECOMPASS_U8 bmc050compass_get_magremapparam(ECOMPASS_U8 *axisConfig,ECOMPASS_U8 *axisSign);
        \brief Returns Axis Configuration & sign of magnetometer Data
        \param *axisConfig -> axis configuration parameter for magnetometer data
                   *axisSign   -> sign configuration parameter for magnetometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_magremapparam(ECOMPASS_U8 *v_magAxisConfig_u8r,ECOMPASS_U8 *v_magAxisSign_u8r);

/*! \fn ECOMPASS_U8 bmc050compass_set_magremapparam(ECOMPASS_U8 axisConfig,ECOMPASS_U8 axisSign);
        \brief Set axis Configuration & sign of magnetometer Data
        \param axisConfig -> axis configuration parameter for magnetometer data
                   axisSign   -> sign configuration parameter for magnetometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_magremapparam(ECOMPASS_U8 v_magAxisConfig_u8r,ECOMPASS_U8 v_magAxisSign_u8r);

/*! \fn ECOMPASS_U8 bmc050compass_get_magcorrectionmethod(ECOMPASS_U8 *method)
        \brief Returns Correction Method used for magnetometer data (bypass/offset/sensitivity&offset)
        \param *method -> correction method used for magnetometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_magcorrectionmethod(ECOMPASS_U8 *v_method_u8r);

/*! \fn ECOMPASS_U8 bmc050compass_set_magcorrectionmethod(ECOMPASS_U8 method)
        \brief Set the Correction Method to be used for magnetometer data (bypass/offset/sensitivity&offset)
        \param method -> correction method to be used for magnetometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_magcorrectionmethod(ECOMPASS_U8 v_method_u8r);

/*! \fn ECOMPASS_U8 bmc050compass_get_magnoisethreshold(ECOMPASS_U8 *thres)
        \brief Returns noise threshold set for the magnetometer data
        \param *thres -> noise threshold for the magnetometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_magnoisethreshold(ECOMPASS_U8 *thres);

/*! \fn ECOMPASS_U8 bmc050compass_set_magnoisethreshold(ECOMPASS_U8 thres)
        \brief Set the noise threshold for the magnetometer data
        \param thres -> noise threshold to be used for the magnetometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_magnoisethreshold(ECOMPASS_U8 v_MagNoiThres_s8r);

/*! \fn ECOMPASS_U8 bmc050compass_get_magaccuracythreshold(ECOMPASS_U8 *thres)
        \brief Returns accuracy threshold set for the magnetometer data monitoring
        \param *thres -> accuracy threshold for the magnetometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_magaccuracythreshold(ECOMPASS_U8 *accurthres);

/*! \fn ECOMPASS_U8 bmc050compass_set_magaccuracythreshold(ECOMPASS_U8 accurthres)
        \brief Set the accuracy threshold for the magnetometer data	monitoring
        \param thres -> accuracy threshold to be used for the magnetometer data
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_magaccuracythreshold(ECOMPASS_U8 accurthres);

/*! \fn ECOMPASS_U8 bmc050compass_set_magcalibmode(ECOMPASS_U8 mode)
        \brief set the conrols active for observer, calib and the method according to the mode
        \param mode -> the set mode to be used for calibration activation for magnetometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_magcalibmode(ECOMPASS_U8 v_magmode_u8r);
/*! \fn ECOMPASS_U8 bmc050compass_get_magcalibparam(dataxyz_t *off,dataxyz_t *sens)
        \brief get the estimated offset and sensitivity parameters of the magnetometer
        \param dataxyz_t *off -> returns the estimated offset of the magnetometer
                   */

/*dataxyz_t *sens -> returns the estimated sensitivity of the magnetometer
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_magcalibparam(dataxyz_t *off,dataxyz_t *sens);

/*! \fn ECOMPASS_U8 bmc050compass_magcalib_reset()
        \brief reset the magnetometer calibration module including calibration parameter
           observation objects, calibration objects, internal states
        \param none
        \return error code
*/
ECOMPASS_U8 bmc050compass_magcalib_reset(void);


/** \section orientation_sensor_api eCompass Lib: Orientation Sensor (Virtual) 
*/

/** \fn ECOMPASS_U8 bmc050compass_set_orientopmode(ECOMPASS_U8 opmode)
        \brief Set Orientation Sensor Operation Mode
        \param opmode operation mode of orientation sensor
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_orientopmode(ECOMPASS_U8 opmode);

/** \fn ECOMPASS_U8 bmc050compass_get_orientopmode(ECOMPASS_U8 *opmode)
        \brief Get Orientation Sensor Operation Mode
        \param *opmode operation mode of orientation sensor
        \return error code
*/
ECOMPASS_U8 bmc050compass_get_orientopmode(ECOMPASS_U8 *opmode);

/** \fn ECOMPASS_U8 bmc050compass_get_raworientation(orientdata_t *data)
        \brief Returns Raw Orientation of the Compass
        \param *data orientation values
        \return error code
        \see orientdata_t
*/
ECOMPASS_U8 bmc050compass_get_raworientation(orientdata_t *data);

/** \fn ECOMPASS_U8 bmc050compass_get_filtorientation(orientdata_t *data)
        \brief Returns Filtered Orientation of the Compass
        \param *data orientation values
        \return error code
        \see orientdata_t
*/
ECOMPASS_U8 bmc050compass_get_filtorientation(orientdata_t *data);

/** \fn ECOMPASS_U8 bmc050compass_get_orientdatastatus(ECOMPASS_U8 *status)
        \brief Returns Orientation data Accuracy Status
        \param *xyz pointer to raw data
        \return error code
        \see sensor_calib_accur_status
*/
ECOMPASS_U8 bmc050compass_get_orientdatastatus(ECOMPASS_U8 *status);

/** \fn ECOMPASS_U8 bmc050compass_set_orientfiltermode(ECOMPASS_U8 mode)
        \brief sets the mode of adaptive filtering for orientation 
        \param *mode filtering mode like bypass,low,medium,high
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_orientfiltermode(ECOMPASS_U8 mode);

/** \fn ECOMPASS_U8 bmc050compass_set_orientfilternoiselevel(ECOMPASS_U8 accNoiseLevel,ECOMPASS_U8 magNoiseLevel)
        \brief sets noise level of the adaptive filtering for orientation 
        \param accNoiseLevel -> noise level of accelerometer
			   magNoiseLevel -> noise level of magnetometer	
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_orientfilternoiselevel(ECOMPASS_U8 accNoiseLevel,ECOMPASS_U8 magNoiseLevel);

/* eCompass Lib: Magnetic Gyro Sensor (Virtual) */ 

/** \fn ECOMPASS_U8 bmc050compass_set_mgyroopmode(ECOMPASS_U8 opmode)
        \brief set magnetic gyro operation mode: M4G
        \param operation mode of magnetic gyro sensor
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_mgyroopmode(ECOMPASS_U8 opmode);

/** \fn ECOMPASS_U8 bmc050compass_get_mgyroopmode(ECOMPASS_U8 *opmode)
        \brief Get magnetic gyro Sensor Operation Mode
        \param *opmode operation mode of magnetic gyro sensor
        \return error code
*/ 
ECOMPASS_U8 bmc050compass_get_mgyroopmode(ECOMPASS_U8 *opmode); 

/** \fn ECOMPASS_U8 bmc050compass_set_mgyrofiltermode(ECOMPASS_U8 mode)
        \brief sets the mode of adaptive filtering for mgyro orientation 
        \param *mode filtering mode like bypass,normal
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_mgyrofiltermode(ECOMPASS_U8 mode);

/** \fn ECOMPASS_U8 bmc050compass_set_mgyrofilternoiselevel(ECOMPASS_U8 accNoiseLevel,ECOMPASS_U8 magNoiseLevel)
        \brief sets noise level of the adaptive filtering for mgyro orientation 
        \param accNoiseLevel -> noise level of accelerometer
			   magNoiseLevel -> noise level of magnetometer	
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_mgyrofilternoiselevel(ECOMPASS_U8 accNoiseLevel,ECOMPASS_U8 magNoiseLevel);

/** \fn ECOMPASS_U8 bmc050compass_get_mgyrodatastatus(ECOMPASS_U8 *status)
        \brief Returns magnetic gyro data Accuracy Status
        \param *xyz pointer to raw data
        \return error code
        \see sensor_calib_accur_status
*/
ECOMPASS_U8 bmc050compass_get_mgyrodatastatus(ECOMPASS_U8 *status);

/** \fn ECOMPASS_U8 bmc050compass_get_mgyroorientation(orientdata_t *data)
        \brief Returns magnetic gyro Orientation of the Compass
        \param *data orientation values
        \return error code
        \see orientdata_t
*/
ECOMPASS_U8 bmc050compass_get_mgyroorientation(orientdata_t *data);

ECOMPASS_U8 bmc050compass_get_mgyrodata(dataxyz_t *data);       /** \todo {not supported now} */

/* eCompass Lib: Linear Acceleration Sensor (Virtual)*/
ECOMPASS_U8 bmc050compass_set_linaccopmode(ECOMPASS_U8 opmode);
ECOMPASS_U8 bmc050compass_get_linaccopmode(ECOMPASS_U8 *opmode);
ECOMPASS_U8 bmc050compass_get_linaccdata(dataxyz_t *data);

/****** EXTENDED LIBRARY FUNCTION FOR ADVANCED FILTERING OF THE RAW SENSOR DATA *********/

/** \fn ECOMPASS_U8 bmc050compass_accAdvPreFilterMode(ECOMPASS_U8 mode);
        \brief operation mode of the accelerometer pre-filter
        \param ECOMPASS_U8 mode is the operation mode of the filter: 0 - deactivated; 1 - active
        \return error code
*/
ECOMPASS_U8 bmc050compass_accAdvPreFilterMode(ECOMPASS_U8 mode);

/** \fn ECOMPASS_U8 bmc050compass_magAdvPreFilterMode(ECOMPASS_U8 mode);
        \brief operation mode of the magnetometer pre-filter
        \param ECOMPASS_U8 mode is the operation mode of the filter: 0 - deactivated; 1 - active
        \return error code
*/
ECOMPASS_U8 bmc050compass_magAdvPreFilterMode(ECOMPASS_U8 mode);

/** \fn ECOMPASS_U8 bmc050compass_set_accAdvPreFilterParam(ECOMPASS_U8 coef, ECOMPASS_U8 noiLevel);
        \brief configuration of the accelerometer filtering parameter
        \param ECOMPASS_U8 coef - filtering coefficient : 0 .. 99 (default: 60)
		\param ECOMPASS_U8 noiLevel - noise level to suppress the output: 0 .. 50 (lsb)
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_accAdvPreFilterParam(ECOMPASS_U8 coef, ECOMPASS_U8 noiLevel);

/** \fn ECOMPASS_U8 bmc050compass_set_magAdvPreFilterParam(ECOMPASS_U8 coef, ECOMPASS_U8 noiLevel);
        \brief configuration of the magnetometer filtering parameter
        \param ECOMPASS_U8 coef - filtering coefficient : 0 .. 99 (default: 60)
		\param ECOMPASS_U8 noiLevel - noise level to suppress the output: 0 .. 99 (lsb)
        \return error code
*/
ECOMPASS_U8 bmc050compass_set_magAdvPreFilterParam(ECOMPASS_U8 coef, ECOMPASS_U8 noiLevel);

#endif // end of compass library
/* EasyCASE ) */
