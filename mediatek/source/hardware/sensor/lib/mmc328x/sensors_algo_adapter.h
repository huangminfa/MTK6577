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

/*****************************************************************************
 *  Copyright Statement:
 *  --------------------
 *  This software is protected by Copyright and the information and source code
 *  contained herein is confidential. The software including the source code
 *  may not be copied and the information contained herein may not be used or
 *  disclosed except with the written permission of MEMSIC Inc. (C) 2009
 *****************************************************************************/

/**
 * @file
 * @author  Robbie Cao<hjcao@memsic.cn>
 *
 * @brief
 * This file define eCompass algorithm adapter APIs.
 */

#ifndef __SENSORS_ALGO_ADAPTER_H__
#define __SENSORS_ALGO_ADAPTER_H__

#include <sensors_data_struct.h>
#include <sensors_algo_ids_util.h>

/**
 * @brief
 * eCompass algorithm
 */
struct algo_t {
	/**
	 * @brief Initialization first of all
	 */
	int (*init)(void);

	/**
	 * @brief Open eCompass algorithm
	 * @return 0 for success, others for failure
	 */
	int (*open)(void);
	/**
	 * @brief Close eCompass algorithm
	 * @return 0 for success, others for failure
	 */
	int (*close)(void);
	/**
	 * @brief Restart eCompass algorithm
	 * @return 0 for success, others for failure
	 */
	int (*restart)(void);
	/**
	 * @brief Get eCompass algorithm state
	 * @return the eCompass algorithm running state
	 */
	int (*get_state)(void);
	/**
	 * @brief Clear eCompass algorithm state
	 */
	void (*clear_state)(void);

	/**
	 * @brief Load eCompass algorithm parameters from NVM to RAM
	 * @return 0 for success, others for failure
	 */
	int (*nvm_load)(void);
	/**
	 * @brief Store eCompass algorithm parameters from RAM to NVM
	 * @return 0 for success, others for failure
	 */
	int (*nvm_store)(void);
	/**
	 * @brief Restore eCompass algorithm parameters on NVM to default
	 * @return 0 for success, others for failure
	 */
	int (*nvm_restore)(void);

	/**
	 * @brief Input sensor data to eCompass algorithm for calibration
	 * @param d contain data of acceleration sensor and magnetic sensor
	 */
	void (*data_in)(struct SensorData_Algo *d);
	/**
	 * @brief Input sensor data to eCompass algorithm to calculate 
	 *        current orientation(heading/pitch/roll)
	 * @param o is the calculating result
	 * @param d contain data of acceleration sensor and magnetic sensor
	 * @return 0 for success, others for failure
	 */
	int (*calc_orientation)(struct SensorData_Orientation *o, struct SensorData_Algo *d);
	/**
	 * @brief Calibrate magnetic sensor
	 * @param mag[3] contain data of magnetic sensor
	 * @param centre[3] contain center of magnetic sensor to return
	 * @return 0 for success, others for failure
	 */
	int (*calc_magcentre)(const float mag[3], float centre[3]);
	/**
	 * @brief Calculate calibrated magnetic data
	 * @param d contain data of magnetic sensor
	 * @param cald[3] contain calibrated magnetic data to return
	 * @return 0 for success, others for failure
	 */
	int (*calc_magcal_data)(struct SensorData_Algo *d, float cald[3]);
	/**
	 * @brief Calibrate eCompass
	 * @param d contain data of acceleration sensor and magnetic sensor
	 * @return 0 for success, others for failure
	 */
	int (*calibrate)(struct SensorData_Algo *d);
};

/**
 * @brief Get eCompass algorithm
 * @return the instance of eCompass algorithm
 */
struct algo_t *sensors_get_algorithm(void);

#endif /* __SENSORS_ALGO_ADAPTER_H__ */
