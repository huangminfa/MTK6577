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

/**************************************************************************
 +------------------------------------------------------------------------+
 | Copyright (c) 2007-2009                                                |
 | AMIT Technology Inc.                                                   |
 | All rights reserved.                                                   |
 +------------------------------------------------------------------------+
 | This is source code in Software of AMIT 6-axis Middleware.             |
 | This software is the confidential and proprietary information of       |
 | AMIT Technology Inc.("Confidential Information")                       |
 | You shall not disclose such Confidential Information and shall         |
 | use it only in accordance with the terms of the license agreement.     |
 +------------------------------------------------------------------------+
 **************************************************************************/
/**
 * @file	AMI_Middle.h
 * @brief	Middleware header file
 */
#ifndef __AMI_MIDDLE_H__
#define __AMI_MIDDLE_H__

#ifdef __cplusplus
extern "C" {
#endif

#ifndef END_USER
//**** use test pattern *********************************************************
//#define OPEN_TEST_PATTERN
//*******************************************************************************
#endif	// #ifndef END_USER

/*********************************************************************
 dev define
 *********************************************************************/
#define AMI_DEV      1

/*********************************************************************
 constant definition
 *********************************************************************/
#define	AMI_OK				0x00		/**< normal */
#define	AMI_PARAM_ERR		0x01		/**< parameter error */
#define	AMI_SEQ_ERR			0x02		/**< sequence error */
#define	AMI_TIMEOUT_ERR		0x03		/**< time out error */
#define	AMI_SYSTEM_ERR		0x10		/**< system error */
#define	AMI_ERROR			0x99		/**< other errors */
#define	MODE1               0x00	// 602
#define	MODE2               0x01	// 304+G

#ifdef OPEN_TEST_PATTERN
#define AMI_SENSOR_602_EVK			 0x00  //for device choose  //Po
#define AMI_SENSOR_602_TESTPATTERN	 0x01  //for device choose  //Po
#define AMI_602_TESTPATTERN_OFFSET	 0x02  //for device choose  //Po
#define AMI_602_MULTI_PATTERN        0x03  //for device choose  //Po
#define AMI_602_MULTI_OFFSET         0x04  //for device choose  //Po
#define AMI_602_AUTO_RUN             0x05  //for device choose  //Po
#endif
/*********************************************************************
 type definition
 *********************************************************************/
typedef signed char		ami_sint8;			/**< signed char */
typedef unsigned char	ami_uint8;			/**< unsigned char */
typedef	signed short	ami_sint16;			/**< signed short */
typedef	unsigned short	ami_uint16;			/**< unsigned short */
typedef	signed long		ami_sint32;			/**< signed long */
typedef	unsigned long	ami_uint32;			/**< unsigned long */

/*********************************************************************
 struct denition
 *********************************************************************/
/** Acceleration  information */
typedef struct {
	ami_sint16	x;		/**< Acceleration of X axis. (unit: mg) */
	ami_sint16	y;		/**< Acceleration of Y axis. (unit: mg) */
	ami_sint16	z;		/**< Acceleration of Z axis. (unit: mg) */
} AMI_ACCELERATION;

/** geomagnetic_field information */
typedef struct {
	ami_sint16	x;		/**< geomagnetic_field of X axis. (unit: mGauss) */
	ami_sint16	y;		/**< geomagnetic_field of Y axis. (unit: mGauss) */
	ami_sint16	z;		/**< geomagnetic_field of Z axis. (unit: mGauss) */
} AMI_MAGNETISM;

/** axis calibration parameter information */
typedef struct {
	ami_sint16	x;		/**< X axis */
	ami_sint16	y;		/**< Y axis */
	ami_sint16	z;		/**< Z axis */
} AMI_VECTOR3D;

typedef struct {
	ami_uint16	x;		/**< X axis */
	ami_uint16	y;		/**< Y axis */
	ami_uint16	z;		/**< Z axis */
} AMI_U_VECTOR3D;

/** user  calibration parameter information */
typedef struct {
	AMI_VECTOR3D	m_offset;		/**< geomagnetic_fieldsensor offset */
	AMI_VECTOR3D	a_offset;		/**< Acceleration sensor offset */
} AMI_USRPARAMETER;

typedef struct {
	ami_uint16	x;		/**< Acceleration of X axis. (unit: mg) */
	ami_uint16	y;		/**< Acceleration of Y axis. (unit: mg) */
	ami_uint16	z;		/**< Acceleration of Z axis. (unit: mg) */
} AMI_U_OFFSET;


/**  calibration parameter information */
typedef struct {
	AMI_U_VECTOR3D		m_gain;		/**< geomagnetic_fieldsensor gain */
	AMI_U_OFFSET        m_offset;
	AMI_U_VECTOR3D		a_gain;		/**< Acceleration sensor gain */
	AMI_U_OFFSET        a_offset;
	AMI_USRPARAMETER	usr;		/**< user  calibration information */
	ami_uint8			Mdir;		/**< sensor orientation  */
	ami_uint8			Gdir;		/**< sensor orientation  */
	ami_sint32			mag_radius;		//2009/08/26 Abnormal magnetic check
	AMI_U_VECTOR3D      gain_cor;   /**< mag gain correct*/
    ami_uint8           gain_para_XY; /**< mag gain compensate*/
    ami_uint8           gain_para_XZ; /**< mag gain compensate*/
    ami_uint8           gain_para_YX; /**< mag gain compensate*/
    ami_uint8           gain_para_YZ; /**< mag gain compensate*/
    ami_uint8           gain_para_ZX; /**< mag gain compensate*/
    ami_uint8           gain_para_ZY; /**< mag gain compensate*/
    ami_uint8           sensor_type;
} AMI_PARAMETER;

/** G2-Sensor measurement value (voltage value) */
typedef struct {
	ami_uint16	mx;		/**< geomagnetic_field sensor measurement value. X axis value from sensor mount orientation as reference */
	ami_uint16	my;		/**< geomagnetic_field sensor measurement value. Y axis value from sensor mount orientation as reference */
	ami_uint16	mz;		/**< geomagnetic_field sensor measurement value. Z axis value from sensor mount orientation as reference */
	ami_uint16	ax;		/**< Acceleration  sensor measurement value. X axis value from sensor mount orientation as reference */
	ami_uint16	ay;		/**< Acceleration  sensor measurement value. Y axis value from sensor mount orientation as reference */
	ami_uint16	az;		/**< Acceleration  sensor measurement value. Z axis value from sensor mount orientation as reference */
} AMI_SENSORVALUE;

/** basic attitude information */
typedef struct {
	ami_sint16			roll;			/**< roll angle. -179�`+180 degree */
	ami_sint16			pitch;			/**< pitch angle. -90�`+90 degree */
	ami_sint16			yaw;			/**< yaw angle. 0�`359 degree */
	ami_sint16			xtilt;			/**< X axis tilt angle. -179�`+180 degree */
	ami_sint16			ytilt;			/**< Y axis tilt angle. -179�`+180 degree */
	ami_sint16			azimuth;		/**< azimuth direction angle. 0�`359 degree */
	AMI_ACCELERATION	acc;			/**< Acceleration information */
	AMI_MAGNETISM		mag;			/**< geomagnetic_field information */
	AMI_ACCELERATION	acc_raw;		/**< Acceleration information without filter operation */
	AMI_MAGNETISM		mag_raw;		/**< geomagnetic_field information without filter operation */
	AMI_ACCELERATION	acc_mg;			/**< Acceleration information with mini-g unit and filtering, i.e. 1000 acc_mg = 1 g = 9.81 m/s^2 */
	AMI_MAGNETISM		mag_mGauss;		/**< geomagnetic_field information with mini-Gauss unit and filtering, i.e. 1000 mag_mGauss = 1 Gauss */
	ami_sint16			mobile_roll;	/**< roll angle.-179�`+180 degree */
	ami_sint16			mobile_pitch;	/**< pitch angle.-90�`+90 degree */
	ami_sint16			mobile_yaw;		/**< yaw angle.0�`359 degree */
	ami_sint16			general_azimuth;		/**< general_azimuth angle .0�`359 degree */   // Jerry
    ami_sint16			Android_roll;
    ami_sint16			Android_pitch;
    ami_sint16			Android_yaw;
} AMI_MOTION;

/** attitude(posture) information */
typedef struct {
	ami_sint16			roll;		/**< roll angle. -179�`+180 degree. */
	ami_sint16			pitch;		/**< pitch angle. -90�`+90 degree. */
	ami_sint16			yaw;		/**< yaw angle. 0�`359 degree. */
	ami_sint16			xtilt;		/**< X axis tilt angle. -179�`+180 degree. */
	ami_sint16			ytilt;		/**< Y axis tilt angle. -179�`+180 degree. */
	ami_sint16			azimuth;	/**< azimuth direction angle. 0�`359 degree. */
	AMI_ACCELERATION	d_acc;		/**< dynamic Acceleration information. */
	AMI_ACCELERATION	s_acc;		/**< static Acceleration information. */
	AMI_MAGNETISM		mag;		/**< geomagnetic_field information. */
	AMI_ACCELERATION	acc_raw;	/**< Acceleration information without filter operation. */
	AMI_MAGNETISM		mag_raw;	/**< geomagnetic_field information without filter operation. */
	ami_sint16			fieldck;	/**< field check result. */
} AMI_MOTIONINFO;


typedef struct {
	ami_uint8			mag_cal_sta;		
} AMI_STATE;

typedef struct {
	AMI_MAGNETISM        mag,mag_raw;
	AMI_ACCELERATION     acc,acc_raw;
}AMI_SENSORDATA;


/*********************************************************************
 callback function definition
 *********************************************************************/
/**
 *  callback function for notification of calibration_end
 *
 * @param	result		geomagnetic_field sensor calibration process result
 * @param	cont		flag for continue.
 * @param	parm		pointer of struct which contains calibration parameter.
 */
typedef void (*AMI_CalibrationCallback)(ami_uint8 result, ami_uint8 cont, AMI_PARAMETER *parm);

/*********************************************************************
 prototype definition
 *********************************************************************/
/*--------------------------------------------------------------------
   G2-Sensor device control API
 --------------------------------------------------------------------*/
ami_uint8 AMI_InitSensor(AMI_PARAMETER *parm);

ami_uint8 AMI_InitStructureByDefaultValue(AMI_PARAMETER *parm);

/*--------------------------------------------------------------------
   data control API
 --------------------------------------------------------------------*/
ami_uint16 AMI_GetMotionInfoLatest(AMI_MOTION* info);

/*--------------------------------------------------------------------
   posture calculation API
 --------------------------------------------------------------------*/
ami_uint8 AMI_InputSensorValue(AMI_SENSORVALUE val);


/*--------------------------------------------------------------------
    calibration(sensor calibration) API
 --------------------------------------------------------------------*/
ami_uint8 AMI_CalibrationStart( AMI_CalibrationCallback callback);      
ami_uint8 AMI_CalibrationCancel(void);

ami_uint8 AMI_AccCalibrationStart(AMI_CalibrationCallback callback);   


ami_sint8 AMI_GetStatusInfoLatest(void);


ami_sint8 AMI_GetStatusAbnormalCheck(void);      

#if AMI_DEV 
ami_sint8 AMI_GetStatusDipRadiusCheck(void);      
#endif   // #if AMI_DEV 

ami_sint8      AMI_SetIIRStrength(ami_uint8 MF, ami_uint8 GF);  

ami_sint8 AMI_GetMagneticAbnormal(void);

ami_sint8 AMI_SetTimeout(ami_uint16   TimeoutCnt); 

ami_sint8 AMI_CheckMagneticInterference(void);	
 
ami_uint8 AMI_GetMWBuildNo(void);

#define  TOTALBIT              1024                                 
#define  CONVBIT2DEG(x)         ( (x)*((double)90/TOTALBIT) )    


#ifdef __cplusplus
}	/* extern */
#endif

#endif /* __AMI_MIDDLE_H__ */
