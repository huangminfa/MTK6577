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

package com.mediatek.bluetooth.share;

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TabHost;

import com.mediatek.bluetooth.R;
import com.mediatek.bluetooth.util.BtLog;

/**
 * @author Jerry Hsu
 * 
 * 1. History Notification Icon => size? / apply: share + bluetooth (appliedx1)
 * 2. History Tab Icon (in + out x state = 4) => size? / apply (appliedx4)
 * 3. History item icon (size is too small) => size? (bt_opp_xx_failure.png / bt_opp_xx_success.png should be replaced?)
 * 
 * 5. Add new res => drawable + string
 * 
 * 4. Remove unused String => complete / failed (verifing)
 * 
 * 6. History file => no file name (fixed)
 * 7. History Activity stack issue with Gallery (fixed)
 */
public class BluetoothShareMgmtActivity extends TabActivity {

	@Override
	protected void onCreate( Bundle savedInstanceState ){

		BtLog.d( "BluetoothShareMgmtActivity.onCreate()[+]" );

		super.onCreate(savedInstanceState);

		// init layout and get TabHost
		this.setContentView(R.layout.bt_share_mgmt);
		TabHost tabHost = this.getTabHost();

		// in tab
		TabHost.TabSpec spec = tabHost.newTabSpec("Incoming")
				.setIndicator(this.getString(R.string.bt_share_mgmt_tab_in_title), this.getResources().getDrawable(R.drawable.bt_share_mgmt_tab_in) )
				.setContent(BluetoothShareTabActivity.getIntent(this, false));
		tabHost.addTab(spec);

		// out tab
		spec = tabHost.newTabSpec("Outgoing")
				.setIndicator(this.getString(R.string.bt_share_mgmt_tab_out_title), this.getResources().getDrawable(R.drawable.bt_share_mgmt_tab_out) )
				.setContent(BluetoothShareTabActivity.getIntent(this, true));
		tabHost.addTab(spec);
	}
}
