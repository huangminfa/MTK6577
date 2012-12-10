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

package com.mediatek.bluetooth.opp.adp;

/**
 * TODO change parameters type (using java.nio to replace String[])
 */
public interface OppConstants {

	// buffer size for reading in vCard from ContactContentProvider
	public static final int VCARD_BUF_SIZE = 512;

	// OPPC / OPPS operation timeout
	public static final int OPPC_OPERATION_TIMEOUT = 20000;
	public static final int OPPC_OPERATION_RETURN_THRESHOLD = 4;
	public static final int OPPS_OPERATION_TIMEOUT = OPPC_OPERATION_TIMEOUT;
	public static final int OPPS_OPERATION_RETURN_THRESHOLD = OPPC_OPERATION_RETURN_THRESHOLD;
	
	public interface OppService {

		public static final String ACTION_OPPC_START = "com.mediatek.bluetooth.opp.action.OPPC_START";
		public static final String ACTION_OPPS_START = "com.mediatek.bluetooth.opp.action.OPPS_START";
	}

	public interface OppsAccessRequest {

		public static final String ACTION_PUSH_REQUEST = "com.mediatek.bluetooth.opp.action.PUSH_REQUEST";
		public static final String ACTION_PULL_REQUEST = "com.mediatek.bluetooth.opp.action.PULL_REQUEST";

		public static final String EXTRA_PEER_NAME = "com.mediatek.bluetooth.opp.extra.PEER_NAME";
		public static final String EXTRA_OBJECT_NAME = "com.mediatek.bluetooth.opp.extra.OBJECT_NAME";
		public static final String EXTRA_TOTAL_BYTES = "com.mediatek.bluetooth.opp.extra.TOTAL_BYTES";
		public static final String EXTRA_TASK_ID = "com.mediatek.bluetooth.opp.extra.TASK_ID";
	}


	public interface GOEP {

		public static final int STATUS_SUCCESS = 0;		// The function call was successful.
		public static final int STATUS_FAILED = 1;		// The operation has failed to start.
		public static final int UNAUTHORIZED = 0x41;		// Unauthorized
		public static final int FORBIDDEN = 0x43;		// Forbidden - operation is understood but refused
		public static final int NOT_FOUND = 0x44;		// Not Found
		public static final int UNSUPPORT_MEDIA_TYPE = 0x4f;	// Unsupported Media Type
		public static final int SERVICE_UNAVAILABLE = 0x53;	// Service Unavailable
		public static final int DATABASE_FULL = 0x60;		// Database Full
		public static final int INTERNAL_SERVER_ERR = 0x50;	// Internal Server Error
		public static final int NOT_IMPLEMENTED = 0x51;		// Not Implemented
	}
}
