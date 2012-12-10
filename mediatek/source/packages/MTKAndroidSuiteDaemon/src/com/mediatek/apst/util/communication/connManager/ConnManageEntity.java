/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.util.communication.connManager;

import com.mediatek.apst.util.communication.common.TransportEntity;

/**
 * Class Name: ConnManageEntity
 * <p>Package: com.mediatek.apst.util.communication.connManager
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Special entity contains connection management information.
 * <p>
 * @author mtk80251
 * @version V1.0
 */
public class ConnManageEntity extends TransportEntity {
    
	private static final long serialVersionUID = 1L;
	private int infoID;
	private int status;
	
	/**
	 * Get the ID of connection management info. 
	 * @return The info ID. Valid value is defined as 
	 * <b>expt_info_id</b> and <b>disconnect_info_id</b>.
	 */
	public int getInfoID() {
		return infoID;
	}
	
	/**
	 * Get the status.
	 * @return The status. Valid value is defined as 
     * <b>connection_status_success</b>, 
     * <b>connection_status_failed</b> and <b>connection_status_exception</b>.
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * This feature ID is a reserved value for connection management.
	 */
	public static final int ConnManageFeatureID = 0;
	
	/**
	 * Represents connection successful status.
	 */
	public static final int connection_status_success = 1;
    /**
     * Represents connection failed status.
     */
	public static final int connection_status_failed = 0;
    /**
     * Represents connection exception status.
     */
	public static final int connection_status_exception = -1;
	
    /**
     * Represents abnormal connection exception info.
     */
	public static final int expt_info_id = 1;
    /**
     * Represents normal disconnection info.
     */
	public static final int disconnect_info_id = 0;

	/**
	 * Constructor.
	 */
	public ConnManageEntity() {
        super(ConnManageFeatureID);
		infoID = -1;
		status = -1;
	}

	/**
	 * Constructor with parameters.
	 * @param infoID The info ID. It typically represents which kind of 
	 * connection management it is. Valid value is defined as 
     * <b>expt_info_id</b> and <b>disconnect_info_id</b>.
	 * @param status The status. Valid value is defined as 
     * <b>connection_status_success</b>, 
     * <b>connection_status_failed</b> and <b>connection_status_exception</b>.
	 */
	public ConnManageEntity(int infoID, int status) {
		super(ConnManageFeatureID);
		this.infoID = infoID;
		this.status = status;
	}
	
}
