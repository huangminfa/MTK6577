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
 * @author Jerry Hsu
 * 
 * 1. BT_OPPC / BT_OPPS must be sync with C header file: ext/include/bt_opp_comm.h
 */
public class OppEvent {

	// client event (from external-adaptation)
	public static final int
	BT_OPPC_GROUP_START		= 0,
	BT_OPPC_ENABLE_SUCCESS		= BT_OPPC_GROUP_START +  1,
	BT_OPPC_ENABLE_FAIL		= BT_OPPC_GROUP_START +  2,
	BT_OPPC_DISABLE_SUCCESS		= BT_OPPC_GROUP_START +  3,
	BT_OPPC_DISABLE_FAIL		= BT_OPPC_GROUP_START +  4,
	BT_OPPC_CONNECTED		= BT_OPPC_GROUP_START +  5,
	BT_OPPC_PROGRESS_UPDATE		= BT_OPPC_GROUP_START +  6,
	BT_OPPC_PUSH_START		= BT_OPPC_GROUP_START +  7,
	BT_OPPC_PUSH_SUCCESS		= BT_OPPC_GROUP_START +  8,
	BT_OPPC_PUSH_FAIL		= BT_OPPC_GROUP_START +  9,
	BT_OPPC_PULL_START		= BT_OPPC_GROUP_START + 10,
	BT_OPPC_PULL_SUCCESS		= BT_OPPC_GROUP_START + 11,
	BT_OPPC_PULL_FAIL		= BT_OPPC_GROUP_START + 12,
	BT_OPPC_EXCH_START		= BT_OPPC_GROUP_START + 13,
	BT_OPPC_EXCH_SUCCESS		= BT_OPPC_GROUP_START + 14,
	BT_OPPC_EXCH_FAIL		= BT_OPPC_GROUP_START + 15,
	BT_OPPC_DISCONNECT		= BT_OPPC_GROUP_START + 16,
	BT_OPPC_GROUP_END		= BT_OPPC_GROUP_START + 30;

	// server event (from external-adaptation)
	public static final int
	BT_OPPS_GROUP_START		= 100,
	// CNF
	BT_OPPS_ENABLE_SUCCESS		= BT_OPPS_GROUP_START +  1,
	BT_OPPS_ENABLE_FAIL		= BT_OPPS_GROUP_START +  2,
	BT_OPPS_DISABLE_SUCCESS		= BT_OPPS_GROUP_START +  3,
	BT_OPPS_DISABLE_FAIL		= BT_OPPS_GROUP_START +  4,
	BT_OPPS_PROGRESS_UPDATE		= BT_OPPS_GROUP_START +  5,
	BT_OPPS_PUSH_START		= BT_OPPS_GROUP_START +  6,
	BT_OPPS_PUSH_SUCCESS		= BT_OPPS_GROUP_START +  7,
	BT_OPPS_PUSH_FAIL		= BT_OPPS_GROUP_START +  8,
	BT_OPPS_PULL_START		= BT_OPPS_GROUP_START +  9,
	BT_OPPS_PULL_SUCCESS		= BT_OPPS_GROUP_START + 10,
	BT_OPPS_PULL_FAIL		= BT_OPPS_GROUP_START + 11,
	BT_OPPS_DISCONNECT		= BT_OPPS_GROUP_START + 12,
	// IND
	BT_OPPS_PUSH_ACCESS_REQUEST	= BT_OPPS_GROUP_START + 13,	// bdaddr / object-name / mime-type / size
	BT_OPPS_PULL_ACCESS_REQUEST	= BT_OPPS_GROUP_START + 14,
	BT_OPPS_GROUP_END		= BT_OPPS_GROUP_START + 30;


	/**
	 * get event-name from event-code
	 * 
	 * @param event
	 * @return
	 */
	public static String getEventName( int event ){

		switch( event ){
			case BT_OPPC_ENABLE_SUCCESS:
				return "BT_OPPC_ENABLE_SUCCESS";
			case BT_OPPC_ENABLE_FAIL:
				return "BT_OPPC_ENABLE_FAIL";
			case BT_OPPC_DISABLE_SUCCESS:
				return "BT_OPPC_DISABLE_SUCCESS";
			case BT_OPPC_DISABLE_FAIL:
				return "BT_OPPC_DISABLE_FAIL";
			case BT_OPPC_PROGRESS_UPDATE:
				return "BT_OPPC_PROGRESS_UPDATE";
			case BT_OPPC_PUSH_START:
				return "BT_OPPC_PUSH_START";
			case BT_OPPC_PUSH_SUCCESS:
				return "BT_OPPC_PUSH_SUCCESS";
			case BT_OPPC_PUSH_FAIL:
				return "BT_OPPC_PUSH_FAIL";
			case BT_OPPC_PULL_START:
				return "BT_OPPC_PULL_START";
			case BT_OPPC_PULL_SUCCESS:
				return "BT_OPPC_PULL_SUCCESS";
			case BT_OPPC_PULL_FAIL:
				return "BT_OPPC_PULL_FAIL";
			case BT_OPPC_EXCH_START:
				return "BT_OPPC_EXCH_START";
			case BT_OPPC_EXCH_SUCCESS:
				return "BT_OPPC_EXCH_SUCCESS";
			case BT_OPPC_EXCH_FAIL:
				return "BT_OPPC_EXCH_FAIL";
			case BT_OPPC_DISCONNECT:
				return "BT_OPPC_DISCONNECT";
			case BT_OPPS_ENABLE_SUCCESS:
				return "BT_OPPS_ENABLE_SUCCESS";
			case BT_OPPS_ENABLE_FAIL:
				return "BT_OPPS_ENABLE_FAIL";
			case BT_OPPS_DISABLE_SUCCESS:
				return "BT_OPPS_DISABLE_SUCCESS";
			case BT_OPPS_DISABLE_FAIL:
				return "BT_OPPS_DISABLE_FAIL";
			case BT_OPPS_PROGRESS_UPDATE:
				return "BT_OPPS_PROGRESS_UPDATE";
			case BT_OPPS_PUSH_START:
				return "BT_OPPS_PUSH_START";
			case BT_OPPS_PUSH_SUCCESS:
				return "BT_OPPS_PUSH_SUCCESS";
			case BT_OPPS_PUSH_FAIL:
				return "BT_OPPS_PUSH_FAIL";
			case BT_OPPS_PULL_START:
				return "BT_OPPS_PULL_START";
			case BT_OPPS_PULL_SUCCESS:
				return "BT_OPPS_PULL_SUCCESS";
			case BT_OPPS_PULL_FAIL:
				return "BT_OPPS_PULL_FAIL";
			case BT_OPPS_DISCONNECT:
				return "BT_OPPS_DISCONNECT";
			case BT_OPPS_PUSH_ACCESS_REQUEST:
				return "BT_OPPS_PUSH_ACCESS_REQUEST";
			case BT_OPPS_PULL_ACCESS_REQUEST:
				return "BT_OPPS_PULL_ACCESS_REQUEST";
			default:
				return "Unknow event: " + event;
		}
	}

	// event id
	public int event;

	// event parameters
	public String[] parameters;

	public OppEvent( int event, String[] parameters ){

		this.event = event;
		this.parameters = parameters;
	}

	@Override
	public boolean equals( Object obj ){

		if( obj == null || !(obj instanceof OppEvent) )	return false;
		return this.event == ((OppEvent)obj).event;
	}

	@Override
	public String toString() {

		StringBuilder res = new StringBuilder().append( "[" ).append( getEventName(this.event) ).append( "," );
		if( this.parameters != null ){

			for( String p : this.parameters ){

				res.append( p ).append( "," );
			}
		}
		res.append( "]" ).toString();
		return res.toString();
	}
}
