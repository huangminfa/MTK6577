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

package com.mediatek.omacp.message;

public class OmacpApplicationCapability {
	
	//browser capability
	public static boolean browser = false;
	public static boolean browser_bookmark_folder = false;
	public static boolean browser_to_proxy = false;
	public static boolean browser_to_napid = false;
	public static boolean browser_bookmark_name = false;
	public static boolean browser_bookmark = false;
	public static boolean browser_username = false;
	public static boolean browser_password = false;
	public static boolean browser_homepage = false;
	
	//mms capability
	public static boolean mms = false;
	public static boolean mms_mmsc_name = false;
	public static boolean mms_to_proxy = false;
	public static boolean mms_to_napid = false;
	public static boolean mms_mmsc = false;
	public static boolean mms_cm = false;
	public static boolean mms_rm = false;
	public static boolean mms_ms = false;
	public static boolean mms_pc_addr = false;
	public static boolean mms_ma = false;

	//dm capability
	public static boolean dm = false;
	public static boolean dm_provider_id = false;
	public static boolean dm_server_name = false;
	public static boolean dm_to_proxy = false;
	public static boolean dm_to_napid = false;
	public static boolean dm_server_address = false;
	public static boolean dm_addr_type = false;
	public static boolean dm_port_number = false;
	public static boolean dm_auth_level = false;
	public static boolean dm_auth_type = false;
	public static boolean dm_auth_name = false;
	public static boolean dm_auth_secret = false;
	public static boolean dm_auth_data = false;
	public static boolean dm_init = false;

	//email capability
	public static boolean email = false;
	public static boolean email_provider_id = false;
	public static boolean email_setting_name = false;
	public static boolean email_to_napid = false;
	public static boolean email_outbound_addr = false;
	public static boolean email_outbound_addr_type = false;
	public static boolean email_outbound_port_number = false;
	public static boolean email_outbound_secure = false;
	public static boolean email_outbound_auth_type = false;
	public static boolean email_outbound_user_name = false;
	public static boolean email_outbound_password = false;
	public static boolean email_from = false;
	public static boolean email_rt_addr = false;
	public static boolean email_inbound_addr = false;
	public static boolean email_inbound_addr_type = false;
	public static boolean email_inbound_port_number = false;
	public static boolean email_inbound_secure = false;
	public static boolean email_inbound_auth_type = false;
	public static boolean email_inbound_user_name = false;
	public static boolean email_inbound_password = false;
	
	//rtsp capability
	public static boolean rtsp = false;
	public static boolean rtsp_provider_id = false;
	public static boolean rtsp_name = false;
	public static boolean rtsp_to_proxy = false;
	public static boolean rtsp_to_napid = false;
	public static boolean rtsp_max_bandwidth = false;
	public static boolean rtsp_net_info = false;
	public static boolean rtsp_min_udp_port = false;
	public static boolean rtsp_max_udp_port = false;
	
	//supl
	public static boolean supl = false;
	public static boolean supl_provider_id = false;
	public static boolean supl_server_name = false;
	public static boolean supl_to_napid = false;
	public static boolean supl_server_addr = false;
	public static boolean supl_addr_type = false;
	
	//ds
	public static boolean ds = false;
	public static boolean ds_server_name = false;
	public static boolean ds_to_proxy = false;
	public static boolean ds_to_napid = false;
	public static boolean ds_provider_id = false;
	public static boolean ds_server_address = false;
	public static boolean ds_address_type = false;
	public static boolean ds_port_number = false;
	public static boolean ds_auth_level = false;
	public static boolean ds_auth_type = false;
	public static boolean ds_auth_name = false;
	public static boolean ds_auth_secret = false;
	public static boolean ds_auth_data = false;
	public static boolean ds_database_content_type = false;
	public static boolean ds_database_url = false;
	public static boolean ds_database_name = false;
	public static boolean ds_database_auth_type = false;
	public static boolean ds_database_auth_name = false;
	public static boolean ds_database_auth_secret = false;
	public static boolean ds_client_database_url = false;
	public static boolean ds_sync_type = false;
		
	//imps
	public static boolean imps = false;
	public static boolean imps_provider_id = false;
	public static boolean imps_server_name = false;
	public static boolean imps_content_type = false;
	public static boolean imps_server_address = false;
	public static boolean imps_address_type = false;
	public static boolean imps_to_proxy = false;
	public static boolean imps_to_napid = false;
	public static boolean imps_auth_level = false;
	public static boolean imps_auth_name = false;
	public static boolean imps_auth_secret = false;
	public static boolean imps_services = false;
	public static boolean imps_client_id_prefix = false;	

}
