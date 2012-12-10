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

/* EasyCASE V6.5 11/04/2011 10:21:54 */
/* EasyCASE O
If=vertical
LevelNumbers=no
LineNumbers=no
Colors=16777215,0,12582912,12632256,0,0,0,16711680,8388736,0,33023,32768,0,0,0,0,0,32768,12632256,255,65280,255,255,16711935
ScreenFont=Courier New,,80,4,-11,0,400,0,0,0,0,0,0,3,2,1,49,96,96
PrinterFont=Courier New,,80,4,-66,0,400,0,0,0,0,0,0,3,2,1,49,600,600
LastLevelId=2397 */
/* EasyCASE ( 1
   bmc050.h */
#ifndef __BMC050_H__
#define __BMC050_H__

typedef unsigned short  BMC050_U16;       // 16 bit achieved with short
typedef signed short    BMC050_S16;
typedef signed int      BMC050_S32;        // 32 bit achieved with int
/* EasyCASE - */
/*
 ***************************************************************************************************
 *
 * (C) All rights reserved by ROBERT BOSCH GMBH
 *
 **************************************************************************************************/
/*  $Date: 2010/05/31
 *  $Revision: 1.3 $
 *
 */

/**************************************************************************************************
* Copyright (C) 2007 Bosch Sensortec GmbH
*
* BMC050.h
*
* Usage:        This header file which includes all function declaration
*
* Author:       gokul.rajendran@in.bosch.com
**************************************************************************************************/
/* EasyCASE ( 72
   Disclaimer */
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
*/
/*************************************************************************************************/
/* EasyCASE ) */
/* EasyCASE ( 913
   File Name For Doxy */
/*! \file BMC050.h
    \brief Header for BMC050 API */
/* EasyCASE ) */
/* EasyCASE ( 75
   #Define Constants */
/* EasyCASE ( 919
   BMC050_BUS_WRITE_FUNC */
/**\brief defines the calling parameter types of the BMC050_WR_FUNCTION */
#define BMC050_BUS_WR_RETURN_TYPE char 

/**\brief links the order of parameters defined in BMC050_BUS_WR_PARAM_TYPE to function calls used inside the API*/
#define BMC050_BUS_WR_PARAM_TYPES unsigned char, unsigned char, unsigned char *, unsigned char

/**\brief links the order of parameters defined in AK8974_BUS_WR_PARAM_TYPE to function calls used inside the API*/
#define BMC050_BUS_WR_PARAM_ORDER device_addr, register_addr, register_data, wr_len

/* never change this line */
#define BMC050_BUS_WRITE_FUNC(device_addr, register_addr, register_data, wr_len )\
           bus_write(device_addr, register_addr, register_data, wr_len )
/* EasyCASE ) */
/* EasyCASE ( 920
   BMC050_BUS_READ_FUNC */
/**\brief defines the return parameter type of the BMC050_RD_FUNCTION
*/
#define BMC050_BUS_RD_RETURN_TYPE char
/**\brief defines the calling parameter types of the BMC050_RD_FUNCTION
*/
#define BMC050_BUS_RD_PARAM_TYPES unsigned char, unsigned char, unsigned char *, unsigned char
/**\brief links the order of parameters defined in BMC050_BUS_RD_PARAM_TYPE to function calls used inside the API
*/
#define BMC050_BUS_RD_PARAM_ORDER device_addr, register_addr, register_data
/* never change this line */
#define BMC050_BUS_READ_FUNC(device_addr, register_addr, register_data, rd_len )\
           bus_read(device_addr, register_addr, register_data, rd_len )
/* EasyCASE ) */
/* EasyCASE ( 921
   BMC050_DELAY */
/**\brief defines the return parameter type of the BMC050_DELAY_FUNCTION
*/
#define BMC050_DELAY_RETURN_TYPE void
/**\brief defines the calling parameter types of the BMC050_DELAY_FUNCTION
*/
#define BMC050_DELAY_PARAM_TYPES unsigned int
/* never change this line */
#define BMC050_DELAY_FUNC(delay_in_msec)\
           delay_func(delay_in_msec)
/* EasyCASE ( 2155
   Mode Switching delays */
#define BMC050_DELAY_POWEROFF_SUSPEND      1                /**<      Delay for PowerOFF to Suspend mode transition  */
#define BMC050_DELAY_SUSPEND_SLEEP         2                /**<      Delay for Suspend to Sleep mode transition     */ 
#define BMC050_DELAY_SLEEP_ACTIVE          1                /**<      Delay for Sleep to Active mode transition      */ 
#define BMC050_DELAY_ACTIVE_SLEEP          1                /**<      Delay for Active to Sleep mode transition      */ 
#define BMC050_DELAY_SLEEP_SUSPEND         1                /**<      Delay for Sleep to Suspend mode transition     */ 
#define BMC050_DELAY_ACTIVE_SUSPEND        1                /**<      Delay for Active to Suspend mode transition    */ 
#define BMC050_DELAY_SLEEP_POWEROFF        1                /**<      Delay for Sleep to PowerOFF mode transition    */ 
#define BMC050_DELAY_ACTIVE_POWEROFF       1                /**<      Delay for Active to PowerOFF mode transition   */
#define BMC050_DELAY_SETTLING_TIME         2                /**<      Delay for Setting from PowerOFF mode transition*/
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 923
   REGISTER ADDRESS */
#define BMC050_RETURN_FUNCTION_TYPE        char            /**< This refers BMC050 return type as char */
#define BMC050_I2C_ADDRESS                 0x10            /**<      I2C device Address for BMC050 shuttle board  */

/*General Info datas*/
#define BMC050_SOFT_RESET7_ON              1               /**<        Value for Soft reset 7th bit              */
#define BMC050_SOFT_RESET1_ON              1               /**<        Value for Soft reset 1st bit              */
#define BMC050_SOFT_RESET7_OFF             0               /**<        Value for Soft reset 7th bit              */
#define BMC050_SOFT_RESET1_OFF             0               /**<        Value for Soft reset 1st bit              */
#define BMC050_DELAY_SOFTRESET             1               /**<            Delay value for BMC050 Soft Reset */
/* EasyCASE ( 2202
   MEMORY MAP */
/* Fixed Data Registers */
#define BMC050_CHIP_ID                     0x40            /**<        Address of Chip ID Register                 */
#define BMC050_REVISION_ID                 0x41            /**<        Address of Revision ID Register             */

/* Data Registers */
#define BMC050_DATAX_LSB                   0x42            /**<        Address of X axis Data LSB Register         */
#define BMC050_DATAX_MSB                   0x43            /**<        Address of X axis Data MSB Register         */
#define BMC050_DATAY_LSB                   0x44            /**<        Address of Y axis Data LSB Register         */
#define BMC050_DATAY_MSB                   0x45            /**<        Address of Y axis Data MSB Register         */
#define BMC050_DATAZ_LSB                   0x46            /**<        Address of Z axis Data LSB Register         */
#define BMC050_DATAZ_MSB                   0x47            /**<        Address of Z axis Data MSB Register         */
#define BMC050_R_LSB                       0x48            /**<        Address of Resistance Data LSB Register     */
#define BMC050_R_MSB                       0x49            /**<        Address of Resistance Data MSB Register     */

/* Status Registers */
#define BMC050_INT_STAT                    0x4A            /**<        Address of Interrupt status Register        */

/* Control Registers */
#define BMC050_POWER_CNTL                  0x4B            /**<        Address of Power control Register           */
#define BMC050_CONTROL                     0x4C            /**<        Address of Control Register                 */
#define BMC050_INT_CNTL                    0x4D            /**<        Address of Interrupt control Register       */
#define BMC050_SENS_CNTL                   0x4E            /**<        Address of Sensor control Register          */
#define BMC050_LOW_THRES                   0x4F            /**<        Address of Low Threshold Register           */
#define BMC050_HIGH_THRES                  0x50            /**<        Address of High Threshold Register          */
#define BMC050_NO_REPETITIONS_XY           0x51            /**<        Address of No of Repetitions XY Register    */
#define BMC050_NO_REPETITIONS_Z            0x52            /**<        Address of No of Repetitions Z Register     */

/* Trim Extended Registers */
#define BMC050_DIG_X1                      0x5D            /**<        Address of DIG X1 Register                  */
#define BMC050_DIG_Y1                      0x5E            /**<        Address of DIG X2 Register                  */
#define BMC050_DIG_Z4_LSB                  0x62            /**<        Address of DIG Z4 LSB Register              */
#define BMC050_DIG_Z4_MSB                  0x63            /**<        Address of DIG Z4 MSB Register              */
#define BMC050_DIG_X2                      0x64            /**<        Address of DIG X2 Register                  */
#define BMC050_DIG_Y2                      0x65            /**<        Address of DIG Y2 Register                  */
#define BMC050_DIG_Z2_LSB                  0x68            /**<        Address of DIG Z2 LSB Register              */
#define BMC050_DIG_Z2_MSB                  0x69            /**<        Address of DIG Z2 MSB Register              */
#define BMC050_DIG_Z1_LSB                  0x6A            /**<        Address of DIG Z1 LSB Register              */
#define BMC050_DIG_Z1_MSB                  0x6B            /**<        Address of DIG Z1 MSB Register              */
#define BMC050_DIG_XYZ1_LSB                0x6C            /**<        Address of DIG XYZ1 LSB Register            */
#define BMC050_DIG_XYZ1_MSB                0x6D            /**<        Address of DIG XYZ1 MSB Register            */
#define BMC050_DIG_Z3_LSB                  0x6E            /**<        Address of DIG Z3 LSB Register              */
#define BMC050_DIG_Z3_MSB                  0x6F            /**<        Address of DIG Z3 MSB Register              */
#define BMC050_DIG_XY2                     0x70            /**<        Address of DIG XY2 Register                 */
#define BMC050_DIG_XY1                     0x71            /**<        Address of DIG XY1 Register                 */
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 924
   BIT WISE REGISTER */
/* EasyCASE ( 925
   Data Registers */
/* Data X LSB Regsiter */
#define BMC050_DATAX_LSB_VALUEX__POS        3                       /**< Last 5 bits of DataX LSB Registers */ 
#define BMC050_DATAX_LSB_VALUEX__LEN        5
#define BMC050_DATAX_LSB_VALUEX__MSK        0xF8
#define BMC050_DATAX_LSB_VALUEX__REG        BMC050_DATAX_LSB

#define BMC050_DATAX_LSB_TESTX__POS         0                       /**<  0th bit of of DataX LSB Registers */ 
#define BMC050_DATAX_LSB_TESTX__LEN         1
#define BMC050_DATAX_LSB_TESTX__MSK         0x01
#define BMC050_DATAX_LSB_TESTX__REG         BMC050_DATAX_LSB

/* Data Y LSB Regsiter */
#define BMC050_DATAY_LSB_VALUEY__POS        3                       /**<  Last 5 bits of DataY LSB Registers */ 
#define BMC050_DATAY_LSB_VALUEY__LEN        5
#define BMC050_DATAY_LSB_VALUEY__MSK        0xF8
#define BMC050_DATAY_LSB_VALUEY__REG        BMC050_DATAY_LSB

#define BMC050_DATAY_LSB_TESTY__POS         0                       /**< 0th bit of DataY LSB Registers */ 
#define BMC050_DATAY_LSB_TESTY__LEN         1
#define BMC050_DATAY_LSB_TESTY__MSK         0x01
#define BMC050_DATAY_LSB_TESTY__REG         BMC050_DATAY_LSB

/* Data Z LSB Regsiter */
#define BMC050_DATAZ_LSB_VALUEZ__POS        1                       /**< Last 7 bits of DataZ LSB Registers */ 
#define BMC050_DATAZ_LSB_VALUEZ__LEN        7
#define BMC050_DATAZ_LSB_VALUEZ__MSK        0xFE
#define BMC050_DATAZ_LSB_VALUEZ__REG        BMC050_DATAZ_LSB

#define BMC050_DATAZ_LSB_TESTZ__POS         0                       /**< 0th bit of DataZ LSB Registers */ 
#define BMC050_DATAZ_LSB_TESTZ__LEN         1
#define BMC050_DATAZ_LSB_TESTZ__MSK         0x01
#define BMC050_DATAZ_LSB_TESTZ__REG         BMC050_DATAZ_LSB

/* Hall Resistance LSB Regsiter */
#define BMC050_R_LSB_VALUE__POS             2                       /**< Last 6 bits of Hall Resistance LSB Register */ 
#define BMC050_R_LSB_VALUE__LEN             6
#define BMC050_R_LSB_VALUE__MSK             0xFC
#define BMC050_R_LSB_VALUE__REG             BMC050_R_LSB

#define BMC050_DATA_RDYSTAT__POS            0                       /**< 0th bit of Hall Resistance LSB Register */ 
#define BMC050_DATA_RDYSTAT__LEN            1
#define BMC050_DATA_RDYSTAT__MSK            0x01
#define BMC050_DATA_RDYSTAT__REG            BMC050_R_LSB
/* EasyCASE ) */
/* EasyCASE ( 926
   Status Register */
/* Interupt Status Register */
#define BMC050_INT_STAT_DOR__POS            7                       /**< 7th bit of Interrupt Status Registers */ 
#define BMC050_INT_STAT_DOR__LEN            1
#define BMC050_INT_STAT_DOR__MSK            0x80
#define BMC050_INT_STAT_DOR__REG            BMC050_INT_STAT

#define BMC050_INT_STAT_OVRFLOW__POS        6                       /**< 6th bit of Interrupt Status Registers */ 
#define BMC050_INT_STAT_OVRFLOW__LEN        1
#define BMC050_INT_STAT_OVRFLOW__MSK        0x40
#define BMC050_INT_STAT_OVRFLOW__REG        BMC050_INT_STAT

#define BMC050_INT_STAT_HIGH_THZ__POS       5                       /**< 5th bit of Interrupt Status Registers */ 
#define BMC050_INT_STAT_HIGH_THZ__LEN       1
#define BMC050_INT_STAT_HIGH_THZ__MSK       0x20
#define BMC050_INT_STAT_HIGH_THZ__REG       BMC050_INT_STAT

#define BMC050_INT_STAT_HIGH_THY__POS       4                       /**< 4th bit of Interrupt Status Registers */ 
#define BMC050_INT_STAT_HIGH_THY__LEN       1
#define BMC050_INT_STAT_HIGH_THY__MSK       0x10
#define BMC050_INT_STAT_HIGH_THY__REG       BMC050_INT_STAT

#define BMC050_INT_STAT_HIGH_THX__POS       3                       /**< 3rd bit of Interrupt Status Registers */ 
#define BMC050_INT_STAT_HIGH_THX__LEN       1
#define BMC050_INT_STAT_HIGH_THX__MSK       0x08
#define BMC050_INT_STAT_HIGH_THX__REG       BMC050_INT_STAT

#define BMC050_INT_STAT_LOW_THZ__POS        2                       /**< 2nd bit of Interrupt Status Registers */ 
#define BMC050_INT_STAT_LOW_THZ__LEN        1
#define BMC050_INT_STAT_LOW_THZ__MSK        0x04
#define BMC050_INT_STAT_LOW_THZ__REG        BMC050_INT_STAT

#define BMC050_INT_STAT_LOW_THY__POS        1                       /**< 1st bit of Interrupt Status Registers */ 
#define BMC050_INT_STAT_LOW_THY__LEN        1
#define BMC050_INT_STAT_LOW_THY__MSK        0x02
#define BMC050_INT_STAT_LOW_THY__REG        BMC050_INT_STAT

#define BMC050_INT_STAT_LOW_THX__POS        0                       /**< 0th bit of Interrupt Status Registers */ 
#define BMC050_INT_STAT_LOW_THX__LEN        1
#define BMC050_INT_STAT_LOW_THX__MSK        0x01
#define BMC050_INT_STAT_LOW_THX__REG        BMC050_INT_STAT
/* EasyCASE ) */
/* EasyCASE ( 927
   Control Register */
/* Power Control Register */
#define BMC050_POWER_CNTL_SRST7__POS       7                       /**< 7th bit of Power Control Registers */ 
#define BMC050_POWER_CNTL_SRST7__LEN       1
#define BMC050_POWER_CNTL_SRST7__MSK       0x80
#define BMC050_POWER_CNTL_SRST7__REG       BMC050_POWER_CNTL

#define BMC050_POWER_CNTL_SPI3_EN__POS     2                       /**< 2nd bit of Power Control Registers */ 
#define BMC050_POWER_CNTL_SPI3_EN__LEN     1
#define BMC050_POWER_CNTL_SPI3_EN__MSK     0x04
#define BMC050_POWER_CNTL_SPI3_EN__REG     BMC050_POWER_CNTL

#define BMC050_POWER_CNTL_SRST1__POS       1                       /**< 1st bit of Power Control Registers */ 
#define BMC050_POWER_CNTL_SRST1__LEN       1
#define BMC050_POWER_CNTL_SRST1__MSK       0x02
#define BMC050_POWER_CNTL_SRST1__REG       BMC050_POWER_CNTL

#define BMC050_POWER_CNTL_PCB__POS         0                       /**< 0th bit of Power Control Registers */ 
#define BMC050_POWER_CNTL_PCB__LEN         1
#define BMC050_POWER_CNTL_PCB__MSK         0x01
#define BMC050_POWER_CNTL_PCB__REG         BMC050_POWER_CNTL

/* Control Register */
#define BMC050_CNTL_ADV_ST__POS            6                       /**< Last 2 bits of Control Registers */ 
#define BMC050_CNTL_ADV_ST__LEN            2
#define BMC050_CNTL_ADV_ST__MSK            0xC0
#define BMC050_CNTL_ADV_ST__REG            BMC050_CONTROL

#define BMC050_CNTL_DR__POS                3                       /**< 3 bits of Control Registers */ 
#define BMC050_CNTL_DR__LEN                3
#define BMC050_CNTL_DR__MSK                0x38
#define BMC050_CNTL_DR__REG                BMC050_CONTROL

#define BMC050_CNTL_OPMODE__POS            1                       /**< 2 bits of Control Registers */ 
#define BMC050_CNTL_OPMODE__LEN            2
#define BMC050_CNTL_OPMODE__MSK            0x06
#define BMC050_CNTL_OPMODE__REG            BMC050_CONTROL

#define BMC050_CNTL_S_TEST__POS            0                       /**< 0th bit of Control Registers */ 
#define BMC050_CNTL_S_TEST__LEN            1
#define BMC050_CNTL_S_TEST__MSK            0x01
#define BMC050_CNTL_S_TEST__REG            BMC050_CONTROL

/* Interupt Control Register */
#define BMC050_INT_CNTL_DOR_EN__POS            7                       /**< 7th bit of Interrupt control Registers */ 
#define BMC050_INT_CNTL_DOR_EN__LEN            1
#define BMC050_INT_CNTL_DOR_EN__MSK            0x80
#define BMC050_INT_CNTL_DOR_EN__REG            BMC050_INT_CNTL

#define BMC050_INT_CNTL_OVRFLOW_EN__POS        6                       /**< 6th bit of Interrupt control Registers */ 
#define BMC050_INT_CNTL_OVRFLOW_EN__LEN        1
#define BMC050_INT_CNTL_OVRFLOW_EN__MSK        0x40
#define BMC050_INT_CNTL_OVRFLOW_EN__REG        BMC050_INT_CNTL

#define BMC050_INT_CNTL_HIGH_THZ_EN__POS       5                       /**< 5th bit of Interrupt control Registers */ 
#define BMC050_INT_CNTL_HIGH_THZ_EN__LEN       1
#define BMC050_INT_CNTL_HIGH_THZ_EN__MSK       0x20
#define BMC050_INT_CNTL_HIGH_THZ_EN__REG       BMC050_INT_CNTL

#define BMC050_INT_CNTL_HIGH_THY_EN__POS       4                       /**< 4th bit of Interrupt control Registers */ 
#define BMC050_INT_CNTL_HIGH_THY_EN__LEN       1
#define BMC050_INT_CNTL_HIGH_THY_EN__MSK       0x10
#define BMC050_INT_CNTL_HIGH_THY_EN__REG       BMC050_INT_CNTL

#define BMC050_INT_CNTL_HIGH_THX_EN__POS       3                       /**< 3rd bit of Interrupt control Registers */ 
#define BMC050_INT_CNTL_HIGH_THX_EN__LEN       1
#define BMC050_INT_CNTL_HIGH_THX_EN__MSK       0x08
#define BMC050_INT_CNTL_HIGH_THX_EN__REG       BMC050_INT_CNTL

#define BMC050_INT_CNTL_LOW_THZ_EN__POS        2                       /**< 2nd bit of Interrupt control Registers */ 
#define BMC050_INT_CNTL_LOW_THZ_EN__LEN        1
#define BMC050_INT_CNTL_LOW_THZ_EN__MSK        0x04
#define BMC050_INT_CNTL_LOW_THZ_EN__REG        BMC050_INT_CNTL

#define BMC050_INT_CNTL_LOW_THY_EN__POS        1                       /**< 1st bit of Interrupt control Registers */ 
#define BMC050_INT_CNTL_LOW_THY_EN__LEN        1
#define BMC050_INT_CNTL_LOW_THY_EN__MSK        0x02
#define BMC050_INT_CNTL_LOW_THY_EN__REG        BMC050_INT_CNTL

#define BMC050_INT_CNTL_LOW_THX_EN__POS        0                       /**< 0th bit of Interrupt control Registers */ 
#define BMC050_INT_CNTL_LOW_THX_EN__LEN        1
#define BMC050_INT_CNTL_LOW_THX_EN__MSK        0x01
#define BMC050_INT_CNTL_LOW_THX_EN__REG        BMC050_INT_CNTL

/* Sensor Control Register */
#define BMC050_SENS_CNTL_DRDY_EN__POS          7                        /**< 7th bit of Sensor control Registers */ 
#define BMC050_SENS_CNTL_DRDY_EN__LEN          1
#define BMC050_SENS_CNTL_DRDY_EN__MSK          0x80
#define BMC050_SENS_CNTL_DRDY_EN__REG          BMC050_SENS_CNTL

#define BMC050_SENS_CNTL_IE__POS               6                        /**< 6th bit of sensor control Registers */ 
#define BMC050_SENS_CNTL_IE__LEN               1
#define BMC050_SENS_CNTL_IE__MSK               0x40
#define BMC050_SENS_CNTL_IE__REG               BMC050_SENS_CNTL

#define BMC050_SENS_CNTL_CHANNELZ__POS         5                        /**< 5th bit of sensor control Registers */ 
#define BMC050_SENS_CNTL_CHANNELZ__LEN         1
#define BMC050_SENS_CNTL_CHANNELZ__MSK         0x20
#define BMC050_SENS_CNTL_CHANNELZ__REG         BMC050_SENS_CNTL

#define BMC050_SENS_CNTL_CHANNELY__POS         4                        /**< 4th bit of sensor control Registers */ 
#define BMC050_SENS_CNTL_CHANNELY__LEN         1
#define BMC050_SENS_CNTL_CHANNELY__MSK         0x10
#define BMC050_SENS_CNTL_CHANNELY__REG         BMC050_SENS_CNTL

#define BMC050_SENS_CNTL_CHANNELX__POS         3                        /**< 3rd bit of sensor control Registers */ 
#define BMC050_SENS_CNTL_CHANNELX__LEN         1
#define BMC050_SENS_CNTL_CHANNELX__MSK         0x08
#define BMC050_SENS_CNTL_CHANNELX__REG         BMC050_SENS_CNTL

#define BMC050_SENS_CNTL_DR_POLARITY__POS      2                        /**< 2nd bit of sensor control Registers */ 
#define BMC050_SENS_CNTL_DR_POLARITY__LEN      1
#define BMC050_SENS_CNTL_DR_POLARITY__MSK      0x04
#define BMC050_SENS_CNTL_DR_POLARITY__REG      BMC050_SENS_CNTL

#define BMC050_SENS_CNTL_INTERRUPT_LATCH__POS            1                       /**< 1st bit of sensor control Registers */ 
#define BMC050_SENS_CNTL_INTERRUPT_LATCH__LEN            1
#define BMC050_SENS_CNTL_INTERRUPT_LATCH__MSK            0x02
#define BMC050_SENS_CNTL_INTERRUPT_LATCH__REG            BMC050_SENS_CNTL

#define BMC050_SENS_CNTL_INTERRUPT_POLARITY__POS         0                       /**< 0th bit of sensor control Registers */ 
#define BMC050_SENS_CNTL_INTERRUPT_POLARITY__LEN         1
#define BMC050_SENS_CNTL_INTERRUPT_POLARITY__MSK         0x01
#define BMC050_SENS_CNTL_INTERRUPT_POLARITY__REG         BMC050_SENS_CNTL
/* EasyCASE ) */
/* EasyCASE ( 929
   Trim Ext Register */
/* Register 6D */
#define BMC050_DIG_XYZ1_MSB__POS         0                          /**< 7 bits of Register BMC050_DIG_XYZ1_MSB */ 
#define BMC050_DIG_XYZ1_MSB__LEN         7
#define BMC050_DIG_XYZ1_MSB__MSK         0x7F
#define BMC050_DIG_XYZ1_MSB__REG         BMC050_DIG_XYZ1_MSB
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 2024
   Common */
#define BMC050_X_AXIS               0                            /**< It refers BMC050 X-axis */
#define BMC050_Y_AXIS               1                            /**< It refers BMC050 Y-axis */
#define BMC050_Z_AXIS               2                            /**< It refers BMC050 Z-axis */
#define BMC050_RESISTANCE           3                            /**< It refers BMC050 Resistance  */
#define BMC050_X                    1                            /**< It refers BMC050 X-axis */
#define BMC050_Y                    2                            /**< It refers BMC050 Y-axis */
#define BMC050_Z                    4                            /**< It refers BMC050 Z-axis */
#define BMC050_XYZ                  7                            /**< It refers BMC050 Z-axis */

/* Constants */                  
#define BMC050_NULL                             0                /**< constant declaration of NULL */
#define BMC050_INTPIN_DISABLE                   1                /**< It refers BMC050 interrupt pin disable - Active low */
#define BMC050_INTPIN_ENABLE                    0                /**< It refers BMC050 interrupt pin enable  - Active low*/
#define BMC050_DISABLE                          0                /**< It refers BMC050 disable - Active high*/
#define BMC050_ENABLE                           1                /**< It refers BMC050 enable - Active high*/
#define BMC050_CHANNEL_DISABLE                  1                /**< It refers BMC050 channel disable - Active low*/
#define BMC050_CHANNEL_ENABLE                   0                /**< It refers BMC050 channel enable - Active low*/
#define BMC050_INTPIN_LATCH_ENABLE              1                /**< It refers BMC050 interrupt pin latch enable - Active high*/
#define BMC050_INTPIN_LATCH_DISABLE             0                /**< It refers BMC050 interrupt pin latch disable - Active high*/
#define BMC050_OFF                              0                /**< It refers BMC050 OFF state */
#define BMC050_ON                               1                /**< It refers BMC050 ON state  */

#define BMC050_NORMAL_MODE                      0x00             /**< It refers BMC050 Normal state  */
#define BMC050_FORCED_MODE                      0x01             /**< It refers BMC050 Forced mode */
#define BMC050_SUSPEND_MODE                     0x02             /**< It refers BMC050 Suspend state */
#define BMC050_SLEEP_MODE                       0x03             /**< It refers BMC050 Sleep mode   */

#define BMC050_ADVANCED_SELFTEST_OFF            0                /** disable advanced self test **/
#define BMC050_ADVANCED_SELFTEST_NEGATIVE       2                /** negative field from coil-on-chip **/
#define BMC050_ADVANCED_SELFTEST_POSITIVE       3                /** positive field from coil-on-chip  **/

#define BMC050_NEGATIVE_SATURATION_Z            -32767           /** Negative Saturation for Z Axis **/
#define BMC050_POSITIVE_SATURATION_Z            32767            /** Positive Saturation for Z Axis **/

#define BMC050_SPI_RD_MASK                      0x80             /**< Read mask **/
#define BMC050_READ_SET                         0x01             /**< Setting for rading data **/

#define E_BMC050_NULL_PTR                       (char)-127
#define E_BMC050_COMM_RES                       (char)-1
#define E_BMC050_OUT_OF_RANGE                   (char)-2
#define E_BMC050_UNDEFINED_MODE                 0                /**< Error in setting or reading Sensor preset modes */

#define BMC050_WR_FUNC_PTR char (* bus_write)(unsigned char, unsigned char, unsigned char *, unsigned char)
#define BMC050_RD_FUNC_PTR char (* bus_read)( unsigned char, unsigned char, unsigned char *, unsigned char)
#define BMC050_MDELAY_DATA_TYPE unsigned int

/*Shifting Constants*/
#define SHIFT_RIGHT_1_POSITION                  1                /**< Shifts Right the variable by 1 position  */
#define SHIFT_RIGHT_2_POSITION                  2                /**< Shifts Right the variable by 2 position  */
#define SHIFT_RIGHT_3_POSITION                  3                /**< Shifts Right the variable by 3 position  */
#define SHIFT_RIGHT_4_POSITION                  4                /**< Shifts Right the variable by 4 position  */
#define SHIFT_RIGHT_5_POSITION                  5                /**< Shifts Right the variable by 5 position  */
#define SHIFT_RIGHT_6_POSITION                  6                /**< Shifts Right the variable by 6 position  */
#define SHIFT_RIGHT_7_POSITION                  7                /**< Shifts Right the variable by 7 position  */
#define SHIFT_RIGHT_8_POSITION                  8                /**< Shifts Right the variable by 8 position  */

#define SHIFT_LEFT_1_POSITION                   1                /**< Shifts Left the variable by 1 position   */
#define SHIFT_LEFT_2_POSITION                   2                /**< Shifts Left the variable by 2 position   */
#define SHIFT_LEFT_3_POSITION                   3                /**< Shifts Left the variable by 3 position   */
#define SHIFT_LEFT_4_POSITION                   4                /**< Shifts Left the variable by 4 position   */
#define SHIFT_LEFT_5_POSITION                   5                /**< Shifts Left the variable by 5 position   */
#define SHIFT_LEFT_6_POSITION                   6                /**< Shifts Left the variable by 6 position   */
#define SHIFT_LEFT_7_POSITION                   7                /**< Shifts Left the variable by 7 position   */
#define SHIFT_LEFT_8_POSITION                   8                /**< Shifts Left the variable by 8 position   */

/* Conversion factors*/
#define BMC050_CONVFACTOR_LSB_UT                6                /**< Conversion factor from LSB to uT */
/* EasyCASE - */
/* get bit slice  */
#define BMC050_GET_BITSLICE(regvar, bitname)\
                        (regvar & bitname##__MSK) >> bitname##__POS

/* Set bit slice */
#define BMC050_SET_BITSLICE(regvar, bitname, val)\
                  (regvar & ~bitname##__MSK) | ((val<<bitname##__POS)&bitname##__MSK)
/* EasyCASE ) */
/* EasyCASE ( 2372
   ADC OVERFLOW MACROS */
#define BMC050_OVERFLOW_OUTPUT       -32768 // compensated output value returned if sensor had overflow
#define BMC050_FLIP_OVERFLOW_ADCVAL  -4096 // Flipcore overflow ADC value
#define BMC050_HALL_OVERFLOW_ADCVAL  -16384 // Hall overflow 1 ADC value
/* EasyCASE ) */
/* EasyCASE ( 2397
   OPERATIONAL MODES */
#define BMC050_PRESETMODE_LOWPOWER                  1             /**< Refers BMC050 LowPower mode       */
#define BMC050_PRESETMODE_REGULAR                   2             /**< Refers BMC050 Regular mode        */
#define BMC050_PRESETMODE_HIGHACCURACY              3             /**< Refers BMC050 High Accuracy mode  */

/* PRESET MODES - DATA RATES */
#define BMC050_LOWPOWER_DR                       BMC050_DR_10HZ /**< Refers Data rate for BMC050 LowPower mode      */
#define BMC050_REGULAR_DR                        BMC050_DR_10HZ /**< Refers Data rate for BMC050 Regular mode       */
#define BMC050_HIGHACCURACY_DR                   BMC050_DR_20HZ /**< Refers Data rate for BMC050 High Accuracy mode */

/* PRESET MODES - REPETITIONS-XY RATES */
#define BMC050_LOWPOWER_REPXY                     2             /**< Refers Repetitions-XY for BMC050 LowPower mode      */
#define BMC050_REGULAR_REPXY                      5             /**< Refers Repetitions-XY for BMC050 Regular mode       */
#define BMC050_HIGHACCURACY_REPXY                40             /**< Refers Repetitions-XY for BMC050 High Accuracy mode */

/* PRESET MODES - REPETITIONS-Z RATES */
#define BMC050_LOWPOWER_REPZ                      4             /**< Refers Repetitions-Z for BMC050 LowPower mode      */
#define BMC050_REGULAR_REPZ                      13             /**< Refers Repetitions-Z for BMC050 Regular mode       */
#define BMC050_HIGHACCURACY_REPZ                 89             /**< Refers Repetitions-Z for BMC050 High Accuracy mode */

/* Data Rates */

#define BMC050_DR_10HZ                     0             /**< Refers 10 HZ Output Data rate */
#define BMC050_DR_02HZ                     1             /**< Refers 10 HZ Output Data rate */
#define BMC050_DR_06HZ                     2             /**< Refers 10 HZ Output Data rate */
#define BMC050_DR_08HZ                     3             /**< Refers 10 HZ Output Data rate */
#define BMC050_DR_15HZ                     4             /**< Refers 10 HZ Output Data rate */
#define BMC050_DR_20HZ                     5             /**< Refers 10 HZ Output Data rate */
#define BMC050_DR_25HZ                     6             /**< Refers 10 HZ Output Data rate */
#define BMC050_DR_30HZ                     7             /**< Refers 10 HZ Output Data rate */
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 76
   ENUM and struct Definitions */
/*user defined Structures*/
/* EasyCASE - */
/* EasyCASE < */
typedef struct
{
        BMC050_S16 datax;
        BMC050_S16 datay;
        BMC050_S16 dataz;
        BMC050_U16 resistance;
}bmc050_mdata_t;
/* EasyCASE > */
/* EasyCASE - */
/* EasyCASE < */
typedef struct
{
        BMC050_S16 datax;
        BMC050_S16 datay;
        BMC050_S16 dataz;
}bmc050_offset_t;
/* EasyCASE > */
/* EasyCASE - */
/* EasyCASE < */
typedef struct
{
        unsigned char company_id;
        unsigned char revision_info;
        unsigned char dev_addr;
        
        BMC050_WR_FUNC_PTR;
        BMC050_RD_FUNC_PTR;
        void(*delay_msec)( BMC050_MDELAY_DATA_TYPE );

        signed char dig_x1;
        signed char dig_y1;

        signed char dig_x2;
        signed char dig_y2;

        BMC050_U16 dig_z1;
        BMC050_S16 dig_z2;
        BMC050_S16 dig_z3;
        BMC050_S16 dig_z4;
                
        unsigned char dig_xy1;
        signed char dig_xy2;
        
        BMC050_U16 dig_xyz1;
}bmc050_t;
/* EasyCASE > */
/* EasyCASE ) */
/* EasyCASE ( 79
   Public API Declarations */
/* EasyCASE ( 980
   Data Register */
/* EasyCASE ( 985
   bmc050_init */
/*******************************************************************************
 * Description: *//**\brief API Initialization routine
 *
 *
 *
 * 
 *  \param *bmc050 pointer to bmc050 structured type
 *
 *
 *  
 *
 *
 *  \return result of communication routines 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_init(bmc050_t *p_bmc050);
/* EasyCASE ) */
/* EasyCASE ( 995
   bmc050_get_flipdataX */
/*******************************************************************************
 * Description: *//**\brief Reads Magnetic sensor raw Data X from 42h and 43h
 *  
 *
 *
 *
 *  \param 
 *       BMC050_S16  *mdata_x : Address of mdata_x
 *       
 *                     
 *
 *
 *  \return Result of bus communication function 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_flipdataX(BMC050_S16 *mdata_x);
/* EasyCASE ) */
/* EasyCASE ( 996
   bmc050_get_flipdataY */
/*******************************************************************************
 * Description: *//**\brief Reads Magnetic sensor raw Data Y from location 44h and 45h
 *  
 *
 *
 *
 *  \param 
 *             BMC050_S16  *mdata_y : Address of mdata_y
 *
 *
 *  \return Result of bus communication function 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_flipdataY(BMC050_S16 *mdata_y);
/* EasyCASE ) */
/* EasyCASE ( 997
   bmc050_get_halldataZ */
/*******************************************************************************
 * Description: *//**\brief Reads Magnetic sensor raw Data Z from location 46h and 47h
 *  
 *
 *
 *
 *  \param 
 *              BMC050_S16  *mdata_z : Address of mdata_z
 *
 *
 *  \return Result of bus communication function 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_halldataZ(BMC050_S16 *mdata_z);
/* EasyCASE ) */
/* EasyCASE ( 2109
   bmc050_get_raw_xyz */
/*******************************************************************************
 * Description: *//**\brief Reads raw data for x,y,z from location 42h to 47h
 *  
 *
 *
 *
 *  \param      bmc050_mdata_t *mdata     :  Address of structure
 *              
 *      
 *              
 *  \return     Result of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_raw_xyz(bmc050_mdata_t *mdata);
/* EasyCASE ) */
/* EasyCASE ( 2057
   bmc050_get_raw_xyzr */
/*******************************************************************************
 * Description: *//**\brief Reads raw data for x,y,z and resistance from location 42h to 49h
 *  
 *
 *
 *
 *  \param      bmc050_mdata_t *mdata     :  Address of structure
 *              
 *      
 *              
 *  \return     Result of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_raw_xyzr(bmc050_mdata_t *mdata);
/* EasyCASE ) */
/* EasyCASE ( 989
   bmc050_read_mdataXYZ */
/*******************************************************************************
 * Description: *//**\brief
 *  1.  Reads Magnetic sensor Data X 
 *  2.  Reads Magnetic sensor Data Y 
 *  3.  Reads Magnetic sensor Data Z 
 *
 *  \param mdata pointer to  bmc050_mdata_t 
 *  
 *
 *
 *  \return result of communication routines 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_read_mdataXYZ(bmc050_mdata_t *mdata);
/* EasyCASE ) */
/* EasyCASE ( 957
   bmc050_read_mdataX */
/*******************************************************************************
 * Description: *//**\brief Reads Magnetic sensor Data X 
 *  
 *
 *
 *
 *  \param 
 *       BMC050_S16  *mdata_x : Address of mdata_x
 *       
 *                     
 *
 *
 *  \return Result of bus communication function 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_read_mdataX(BMC050_S16 *mdata_x);
/* EasyCASE ) */
/* EasyCASE ( 958
   bmc050_read_mdataY */
/*******************************************************************************
 * Description: *//**\brief Reads Magnetic sensor Data Y
 *  
 *
 *
 *
 *  \param 
 *             BMC050_S16  *mdata_y : Address of mdata_y
 *
 *
 *  \return Result of bus communication function 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_read_mdataY(BMC050_S16 *mdata_y);
/* EasyCASE ) */
/* EasyCASE ( 959
   bmc050_read_mdataZ */
/*******************************************************************************
 * Description: *//**\brief Reads Magnetic sensor Data Z
 *  
 *
 *
 *
 *  \param 
 *              BMC050_S16  *mdata_z : Address of mdata_z
 *
 *
 *  \return Result of bus communication function 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_read_mdataZ(BMC050_S16 *mdata_z);
/* EasyCASE ) */
/* EasyCASE ( 971
   bmc050_get_mdataResistance */
/*******************************************************************************
 * Description: *//**\brief Reads Magnetic sensor Data Resistance from location 48h and 49h
 *  
 *
 *
 *
 *  \param 
 *             BMC050_U16  *mdata_resistance : Address of mdata_resistance
 *
 *
 *  \return Result of bus communication function 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_mdataResistance(BMC050_U16  *mdata_resistance);
/* EasyCASE ) */
/* EasyCASE ( 1059
   bmc050_read_register */
/*******************************************************************************
 * Description: *//**\brief This API reads the data from the given register
 *  
 *
 *
 *
 *  \param unsigned char addr, unsigned char *data
 *                       addr -> Address of the register
 *                       data -> address of the variable, read value will be kept
 *  \return  results of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_read_register(unsigned char addr, unsigned char *data, unsigned char len);
/* EasyCASE ) */
/* EasyCASE ( 1060
   bmc050_write_register */
/*******************************************************************************
 * Description: *//**\brief This API given data to the given register
 *  
 *
 *
 *
 *  \param unsigned char addr, unsigned char data
 *                   addr -> Address of the register       
 *                   data -> Data to be written to the register
 *
 *  \return Results of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_write_register(unsigned char addr, unsigned char *data, unsigned char len);
/* EasyCASE ) */
/* EasyCASE ( 2006
   Self test X */
/*******************************************************************************
 * Description: *//**\brief Reads self test X bit from location 42h
 *
 *
 *
 * 
 *  \param 
 *      unsigned char *self_testx : Address of self_testx
 *
 *  
 *
 *
 *  \return result of communication routines 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_self_test_X (unsigned char *self_testx);
/* EasyCASE ) */
/* EasyCASE ( 2016
   Self test Y */
/*******************************************************************************
 * Description: *//**\brief Reads self test Y bit from location 44h
 *
 *
 *
 * 
 *  \param 
 *      unsigned char *self_testy : Address of self_testy
 *
 *  
 *
 *
 *  \return result of communication routines 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_self_test_Y (unsigned char *self_testy);
/* EasyCASE ) */
/* EasyCASE ( 2018
   Self test Z */
/*******************************************************************************
 * Description: *//**\brief Reads self test Z bit from location 46h
 *
 *
 *
 * 
 *  \param 
 *      unsigned char *self_testz : Address of self_testz
 *
 *  
 *
 *
 *  \return result of communication routines 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_self_test_Z (unsigned char *self_testz);
/* EasyCASE ) */
/* EasyCASE ( 2053
   Self test XYZ */
/*******************************************************************************
 * Description: *//**\brief Reads self test X,Y and Z bits from location 42h, 44h and 46h
 *
 *
 *
 * 
 *  \param 
 *      unsigned char *self_testxyz : Address of self test bits
 *
 *  
 *
 *
 *  \return result of communication routines 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_self_test_XYZ (unsigned char *self_testxyz);
/* EasyCASE ) */
/* EasyCASE ( 2020
   Data Ready status */
/*******************************************************************************
 * Description: *//**\brief Reads Data Ready Status bit from location 48h
 *
 *
 *
 * 
 *  \param 
 *      unsigned char *rdy_status : Address of Data ready Status
 *
 *  
 *
 *
 *  \return result of communication routines 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_rdy_status (unsigned char *rdy_status );
/* EasyCASE ) */
/* EasyCASE ( 2088
   bmc050_compensate_X */
/*******************************************************************************
 * Description: *//**\brief Converts raw data for X axis to magnetic data for X axis
 *
 *
 *
 * 
 *  \param 
 *      BMC050_S16 mdata_x  : Value of raw data for X axis
 *      BMC050_U16 data_R   : Measured hall resistance
 *  
 *
 *
 *  \return Compensated magnetic data
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_S16 bmc050_compensate_X(BMC050_S16 mdata_x,BMC050_U16 data_R);
/* EasyCASE ) */
/* EasyCASE ( 2090
   bmc050_compensate_Y */
/*******************************************************************************
 * Description: *//**\brief Converts raw data for Y axis to magnetic data for Y axis
 *
 *
 *
 * 
 *  \param 
 *      BMC050_S16 mdata_y  : Value of raw data for Y axis
 *      BMC050_U16 data_R   : Measured hall resistance
 *  
 *
 *
 *  \return Compensated magnetic data
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_S16 bmc050_compensate_Y(BMC050_S16 mdata_y,BMC050_U16 data_R);
/* EasyCASE ) */
/* EasyCASE ( 2092
   bmc050_compensate_Z */
/*******************************************************************************
 * Description: *//**\brief Converts raw data for Z axis to magnetic data for Z axis
 *
 *
 *
 * 
 *  \param 
 *      BMC050_S16 mdata_z   : Value of raw data for Z axis
 *      BMC050_U16 data_R    : Measured hall resistance
 *  
 *
 *
 *  \return Compensated magnetic data
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_S16 bmc050_compensate_Z(BMC050_S16 mdata_z, BMC050_U16 data_R);
/* EasyCASE ) */
/* EasyCASE ( 2105
   Init Calculation Registers */
/*******************************************************************************
 * Description: *//**\brief Reads Trim Ext registers for calculation
 *
 *
 *
 * 
 *  \param 
 *      
 *      
 *  
 *
 *
 *  \return Result of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_init_trim_registers(void);
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 1225
   Status Register */
/* EasyCASE ( 1321
   bmc050_get_status_reg */
/*******************************************************************************
 * Description: *//**\brief Reads interrupt status register byte from 4Ah
 *  
 *
 *
 *
 *  \param
 *      unsigned char *status_data : Address of status register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/

BMC050_RETURN_FUNCTION_TYPE bmc050_get_status_reg( unsigned char *status_data);
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 983
   Control Register */
/* EasyCASE ( 1132
   Power Ctrl Reg */
/* EasyCASE ( 1131
   bmc050_get_power_control_reg */
/*******************************************************************************
 * Description: *//**\brief Reads powercontrol register byte from 4Bh
 *  
 *
 *
 *
 *  \param
 *      unsigned char pwr_cntl_data : Address of power control register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_power_control_reg( unsigned char *pwr_cntl_data);
/* EasyCASE ) */
/* EasyCASE ( 979
   bmc050_soft_reset */
/*******************************************************************************
 * Description: *//**\brief Enable or Disable of Soft reset
 *  
 *
 *
 *
 *  \param     unsigned char data :  0 -> Disable
 *                                   1 -> Enable
 *
 *
 *  \return     Result of bus communication function 
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_soft_reset( void );
/* EasyCASE ) */
/* EasyCASE ( 2241
   bmc050_set_spi3 */
/*******************************************************************************
 * Description: *//**\brief Writes SPI3 mode select bit at 4Bh
 *  
 *
 *
 *
 *  \param
 *      unsigned char value : Value for SPI3 mode select bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_spi3(unsigned char value);
/* EasyCASE ) */
/* EasyCASE ( 2371
   bmc050_get_powermode */
/*******************************************************************************
 * Description: *//**\brief Reads powermode bit at 4Bh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *mode : Address for powermode bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_powermode( unsigned char *mode);
/* EasyCASE ) */
/* EasyCASE ( 1127
   bmc050_set_powermode */
/*******************************************************************************
 * Description: *//**\brief Writes powermode bit at 4Bh
 *  
 *
 *
 *
 *  \param
 *      unsigned char mode : Value for powermode bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_powermode( unsigned char mode);
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 2324
   Control Reg */
/* EasyCASE ( 2325
   bmc050_set_adv_selftest */
/*******************************************************************************
 * Description: *//**\brief Cofiguration for advanced self test
 *  
 *
 *
 *
 *  \param      unsigned char adv_selftest : Value for advanced self test bits
 *  
 *
 *
 *  \return     Result of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_adv_selftest( unsigned char adv_selftest);
/* EasyCASE ) */
/* EasyCASE ( 2326
   bmc050_get_adv_selftest */
/*******************************************************************************
 * Description: *//**\brief Cofiguration for advanced self test
 *  
 *
 *
 *
 *  \param      unsigned char adv_selftest : Value for advanced self test bits
 *  
 *
 *
 *  \return     Result of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_adv_selftest( unsigned char *adv_selftest);
/* EasyCASE ) */
/* EasyCASE ( 2327
   bmc050_set_datarate */
/*******************************************************************************
 * Description: *//**\brief Writes data rate to location 4Ch
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_rate : Value for data rate
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_datarate( unsigned char data_rate);
/* EasyCASE ) */
/* EasyCASE ( 2328
   bmc050_get_datarate */
/*******************************************************************************
 * Description: *//**\brief Read data rate from location 4Ch
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_rate : Value for data rate
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_datarate( unsigned char *data_rate);
/* EasyCASE ) */
/* EasyCASE ( 2329
   bmc050_set_functional_state */
/*******************************************************************************
 * Description: *//**\brief Writes Opmode bits at 4Ch
 *  
 *
 *
 *
 *  \param
 *      unsigned char functional_state : Value for operational mode
 *
 *
 *  \return     Result of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_functional_state( unsigned char functional_state);
/* EasyCASE ) */
/* EasyCASE ( 2330
   bmc050_get_functional_state */
/*******************************************************************************
 * Description: *//**\brief Writes Opmode bits at 4Ch
 *  
 *
 *
 *
 *  \param
 *      unsigned char functional_state : Value for operational mode
 *
 *
 *  \return     Result of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_functional_state( unsigned char *functional_state);
/* EasyCASE ) */
/* EasyCASE ( 2331
   bmc050_set_selftest */
/*******************************************************************************
 * Description: *//**\brief Writes self test bit at 4Ch
 *  
 *
 *
 *
 *  \param      unsigned char selftest : 1 -> Enable
 *                                       0 -> Disable
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_selftest(unsigned char selftest);
/* EasyCASE ) */
/* EasyCASE ( 2332
   bmc050_get_selftest */
/*******************************************************************************
 * Description: *//**\brief Reads self test bit at 4Ch
 *  
 *
 *
 *
 *  \param      unsigned char selftest : 1 -> Enable
 *                                       0 -> Disable
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_selftest(unsigned char *selftest);
/* EasyCASE ) */
/* EasyCASE ( 2333
   bmc050_perform_advanced_selftest */
/*******************************************************************************
 * Description: *//**\brief To check advanced selt test
 *  
 *
 *
 *
 *  \param
 *      BMC050_S16 *diff_z   :  value of current between positive and negative voltage for Z axis
 *      
 *
 *  \return Result of bus communication function
 *  
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_perform_advanced_selftest(BMC050_S16 *diff_z);
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 2334
   Interrupt Ctrl Reg */
/* EasyCASE ( 2335
   bmc050_set_data_overrun_function */
/*******************************************************************************
 * Description: *//**\brief Writes data over run bit in register 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_overrun _function_state : value for data overrun enable bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_overrun_function( unsigned char data_overrun_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2336
   bmc050_get_data_overrun_function */
/*******************************************************************************
 * Description: *//**\brief Reads data over run bit in register 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_overrun _function_state : value for data overrun enable bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_overrun_function( unsigned char *data_overrun_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2337
   bmc050_set_data_overflow_function */
/*******************************************************************************
 * Description: *//**\brief Writes data ready overflow bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_overflow : Value for data overflow bits
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_overflow_function(unsigned char data_overflow);
/* EasyCASE ) */
/* EasyCASE ( 2338
   bmc050_get_data_overflow_function */
/*******************************************************************************
 * Description: *//**\brief Reads data ready overflow bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *data_overflow : Address for data overflow bits
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_overflow_function(unsigned char *data_overflow);
/* EasyCASE ) */
/* EasyCASE ( 2339
   bmc050_set_data_highthreshold_Z_function */
/*******************************************************************************
 * Description: *//**\brief Writes high threshold Z bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_highthreshold_z_function_state : Value for high threshold Z
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_highthreshold_Z_function( unsigned char data_highthreshold_z_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2340
   bmc050_get_data_highthreshold_Z_function */
/*******************************************************************************
 * Description: *//**\brief Reads high threshold Z bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_highthreshold_z_function_state : Value for high threshold Z
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_highthreshold_Z_function( unsigned char *data_highthreshold_z_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2341
   bmc050_set_data_highthreshold_Y_function */
/*******************************************************************************
 * Description: *//**\brief Writes high threshold Y bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_highthreshold_y_function_state : Value for high threshold Y
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_highthreshold_Y_function( unsigned char data_highthreshold_y_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2342
   bmc050_get_data_highthreshold_Y_function */
/*******************************************************************************
 * Description: *//**\brief Reads high threshold Y bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_highthreshold_y_function_state : Value for high threshold Y
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_highthreshold_Y_function( unsigned char *data_highthreshold_y_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2343
   bmc050_set_data_highthreshold_X_function */
/*******************************************************************************
 * Description: *//**\brief Writes high threshold X bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_highthreshold_x_function_state : Value for high threshold X
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_highthreshold_X_function( unsigned char data_highthreshold_x_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2344
   bmc050_get_data_highthreshold_X_function */
/*******************************************************************************
 * Description: *//**\brief Reads high threshold X bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *data_highthreshold_x_function_state : Address for high threshold X
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_highthreshold_X_function( unsigned char *data_highthreshold_x_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2345
   bmc050_set_data_lowthreshold_Z_function */
/*******************************************************************************
 * Description: *//**\brief Writes low threshold Z bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_lowthreshold_z_function_state : Value for low threshold Z
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_lowthreshold_Z_function( unsigned char data_lowthreshold_z_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2346
   bmc050_get_data_lowthreshold_Z_function */
/*******************************************************************************
 * Description: *//**\brief Reads low threshold Z bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_lowthreshold_z_function_state : Value for low threshold Z
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_lowthreshold_Z_function( unsigned char *data_lowthreshold_z_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2347
   bmc050_set_data_lowthreshold_Y_function */
/*******************************************************************************
 * Description: *//**\brief Writes low threshold Y bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_lowthreshold_y_function_state : Value for low threshold Y
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_lowthreshold_Y_function( unsigned char data_lowthreshold_y_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2348
   bmc050_get_data_lowthreshold_Y_function */
/*******************************************************************************
 * Description: *//**\brief Reads low threshold Y bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *data_lowthreshold_y_function_state : Address for low threshold Y
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_lowthreshold_Y_function( unsigned char *data_lowthreshold_y_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2349
   bmc050_set_data_lowthreshold_X_function */
/*******************************************************************************
 * Description: *//**\brief Writes low threshold X bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_lowthreshold_x_function_state : Value for low threshold X
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_lowthreshold_X_function( unsigned char data_lowthreshold_x_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2350
   bmc050_get_data_lowthreshold_X_function */
/*******************************************************************************
 * Description: *//**\brief Reads low threshold X bit at 4Dh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *data_lowthreshold_x_function_state : Address for low threshold X
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_lowthreshold_X_function( unsigned char *data_lowthreshold_x_function_state);
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 2351
   Sensor Ctrl Reg */
/* EasyCASE ( 2353
   bmc050_get_data_ready_function */
/*******************************************************************************
 * Description: *//**\brief Reads Data Ready Pin from 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *data_ready_function_state : Address of Data Ready Pin
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_data_ready_function( unsigned char *data_ready_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2352
   bmc050_set_data_ready_function */
/*******************************************************************************
 * Description: *//**\brief Writes Data ready bit to 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char data_ready_function_state : Value for Data Ready Pin
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_data_ready_function( unsigned char data_ready_function_state);
/* EasyCASE ) */
/* EasyCASE ( 2355
   bmc050_get_interrupt_func */
/*******************************************************************************
 * Description: *//**\brief Reads interrupt pin en bit from 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *sensor_control_data : Address of sensor control register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_interrupt_func(unsigned char *int_func);
/* EasyCASE ) */
/* EasyCASE ( 2354
   bmc050_set_interrupt_func */
/*******************************************************************************
 * Description: *//**\brief Writes Interrupt pin enable bit at location 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char int_func : Enable and disable interrupts
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_interrupt_func( unsigned char int_func);
/* EasyCASE ) */
/* EasyCASE ( 2356
   bmc050_get_control_measurement_z */
/*******************************************************************************
 * Description: *//**\brief Reads channelZ enable bits at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *enable_disable : Address for channel z bits
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_control_measurement_z (unsigned char *enable_disable);
/* EasyCASE ) */
/* EasyCASE ( 2357
   bmc050_set_control_measurement_z */
/*******************************************************************************
 * Description: *//**\brief Writes channelZ enable bits at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char enable_disable : Value for channel z bits
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_control_measurement_z (unsigned char enable_disable);
/* EasyCASE ) */
/* EasyCASE ( 2358
   bmc050_set_control_measurement_y */
/*******************************************************************************
 * Description: *//**\brief Writes channelY enable bits at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *enable_disable : Value for channel y bits
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_control_measurement_y (unsigned char enable_disable);
/* EasyCASE ) */
/* EasyCASE ( 2359
   bmc050_get_control_measurement_y */
/*******************************************************************************
 * Description: *//**\brief Writes channelY enable bits at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char enable_disable : Value for channel y bits
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_control_measurement_y (unsigned char *enable_disable);
/* EasyCASE ) */
/* EasyCASE ( 2360
   bmc050_set_control_measurement_x */
/*******************************************************************************
 * Description: *//**\brief Writes channelx enable bits at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char enable_disable : Value for channel x bits
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_control_measurement_x (unsigned char enable_disable);
/* EasyCASE ) */
/* EasyCASE ( 2361
   bmc050_get_control_measurement_x */
/*******************************************************************************
 * Description: *//**\brief Reads channelx enable bits at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *enable_disable : Address for channel x bits
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_control_measurement_x (unsigned char *enable_disable);
/* EasyCASE ) */
/* EasyCASE ( 2362
   bmc050_set_DR_polarity */
/*******************************************************************************
 * Description: *//**\brief Writes DR_polarity bit at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char dr_polarity_select : Value for dr polarity select bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_DR_polarity( unsigned char dr_polarity_select);
/* EasyCASE ) */
/* EasyCASE ( 2363
   BMC050_get_DR_polarity */
/*******************************************************************************
 * Description: *//**\brief Reads DR_polarity bit at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char dr_polarity_select : Address of sensor control register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_DR_polarity( unsigned char *dr_polarity_select);
/* EasyCASE ) */
/* EasyCASE ( 2364
   bmc050_set_interrupt_latch */
/*******************************************************************************
 * Description: *//**\brief Writes interrupt latch select bit at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char interrupt_latch_select : Value for interrupt latch select bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_interrupt_latch( unsigned char interrupt_latch_select);
/* EasyCASE ) */
/* EasyCASE ( 2365
   bmc050_get_interrupt_latch */
/*******************************************************************************
 * Description: *//**\brief Reads interrupt latch select bit at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *interrupt_latch_select : Address for interrupt latch select bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_interrupt_latch( unsigned char *interrupt_latch_select);
/* EasyCASE ) */
/* EasyCASE ( 2366
   bmc050_set_intpin_polarity */
/*******************************************************************************
 * Description: *//**\brief Writes intpin polarity bit at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char int_polarity_select : Value for intpin polarity bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_intpin_polarity( unsigned char int_polarity_select);
/* EasyCASE ) */
/* EasyCASE ( 2367
   bmc050_get_intpin_polarity */
/*******************************************************************************
 * Description: *//**\brief Reads intpin polarity bit at 4Eh
 *  
 *
 *
 *
 *  \param
 *      unsigned char *int_polarity_select : Address for intpin polarity bit
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_intpin_polarity( unsigned char *int_polarity_select);
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 1160
   Low threshold Reg */
/* EasyCASE ( 1161
   bmc050_get_low_threshold_reg */
/*******************************************************************************
 * Description: *//**\brief Reads low threshold value from 4Fh
 *  
 *
 *
 *
 *  \param
 *      unsigned char low_threshold : Address of low threshold register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_low_threshold (BMC050_S16 *low_threshold );
/* EasyCASE ) */
/* EasyCASE ( 1163
   bmc050_set_low_threshold_reg */
/*******************************************************************************
 * Description: *//**\brief Writes low threshold value at 4Fh
 *  
 *
 *
 *
 *  \param
 *      unsigned char low_threshold : Value for low threshold
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_low_threshold (BMC050_S16 low_threshold );
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 1168
   High threshold Reg */
/* EasyCASE ( 1186
   bmc050_get_high_threshold_reg */
/*******************************************************************************
 * Description: *//**\brief Reads high threshold value from 50h
 *  
 *
 *
 *
 *  \param
 *      unsigned char high_threshold : Address of high threshold register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_high_threshold (BMC050_S16 *high_threshold );
/* EasyCASE ) */
/* EasyCASE ( 1187
   bmc050_set_high_threshold_reg */
/*******************************************************************************
 * Description: *//**\brief Writes high threshold value at 50h
 *  
 *
 *
 *
 *  \param
 *      unsigned char high_threshold : Value for high threshold
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_high_threshold (BMC050_S16 high_threshold );
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 2247
   Repetition XY Reg */
/* EasyCASE ( 2256
   bmc050_get_repetitions_XY */
/*******************************************************************************
 * Description: *//**\brief Reads No of repetitions register from 51h
 *  
 *
 *
 *
 *  \param
 *      unsigned char *no_repetitions_xy : Address of no of repetitions xy register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_repetitions_XY(unsigned char *no_repetitions_xy );
/* EasyCASE ) */
/* EasyCASE ( 2257
   bmc050_set_repetitions_XY */
/*******************************************************************************
 * Description: *//**\brief Writes No of repetitions register at 51h
 *  
 *
 *
 *
 *  \param
 *      unsigned char no_repetitions_xy : Value of no of repetitions xy register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_repetitions_XY(unsigned char no_repetitions_xy );
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 2250
   Repetition Z Reg */
/* EasyCASE ( 2263
   bmc050_get_repetitions_Z */
/*******************************************************************************
 * Description: *//**\brief Reads No of repetitions register from 52h
 *  
 *
 *
 *
 *  \param
 *      unsigned char *no_repetitions_z : Address of no of repetitions z register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_repetitions_Z(unsigned char *no_repetitions_z );
/* EasyCASE ) */
/* EasyCASE ( 2264
   bmc050_set_repetitions_Z */
/*******************************************************************************
 * Description: *//**\brief Writes No of repetitions register at 52h
 *  
 *
 *
 *
 *  \param
 *      unsigned char no_repetitions_z : Value of no of repetitions z register
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_set_repetitions_Z(unsigned char no_repetitions_z );
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ) */
/* EasyCASE ( 2393
   bmc050_get_presetmode */
/*******************************************************************************
 * Description: *//**\brief Get the preset mode of operation
 *  
 *
 *
 *
 *  \param
 *      unsigned char *mode : Variable to hold mode
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
BMC050_RETURN_FUNCTION_TYPE bmc050_get_presetmode(unsigned char *mode );
/* EasyCASE ) */
/* EasyCASE ( 2392
   bmc050_set_presetmode */
/*******************************************************************************
 * Description: *//**\brief Select the preset mode of operation
 *  
 *
 *
 *
 *  \param
 *      unsigned char mode : Value of modes to be selected
 *
 *
 *  \return 
 *      Result of bus communication function
 *
 ******************************************************************************/
/* Scheduling:
 *
 *
 *
 * Usage guide:
 *
 *
 * Remarks:
 *
 ******************************************************************************/
 BMC050_RETURN_FUNCTION_TYPE bmc050_set_presetmode(unsigned char mode );
/* EasyCASE ) */
/* EasyCASE ) */
#endif
/* EasyCASE ) */
