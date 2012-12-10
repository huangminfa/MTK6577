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

package com.mediatek.engineermode.tvout;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.engineermode.R;
import android.app.Activity;
import android.os.Bundle;
import com.mediatek.xlog.Xlog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import com.mediatek.tvOut.TvOut;

//import com.mediatek.tvOut.TvOut;

public class TVOutActivity extends Activity implements OnItemClickListener {

	public final static String TAG = "EM/TVOut";
	private TvOut tvOut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Xlog.i(TAG, "onCreate TVOutActivity");
		setContentView(R.layout.tv_out);
		this.setTitle("Chinese Standard Pattern");
		tvOut = new TvOut();
		ListView tvOutListView = (ListView) this
				.findViewById(R.id.ListView_TVOut);

		List<String> items = new ArrayList<String>();
		items.add("0_white_level");
		items.add("1_luma_bw");
		items.add("2_luma_linear_distortion");
		items.add("3_chroma_snr");
		items.add("4_diff_gain");
		items.add("5_color_bar");
		items.add("6_luma_nonlinearity");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);

		tvOutListView.setAdapter(adapter);
		tvOutListView.setOnItemClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Xlog.i(TAG, "onDestroy TVOutActivity");
		boolean b = tvOut.leavePattern();
		Xlog.v(TAG, "leavePattern result is" + b);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Xlog.i(TAG, "onPause TVOutActivity");
		boolean b = false;
		try {
			b = tvOut.leavePattern();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Xlog.v(TAG, "leavePattern result is " + b);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Xlog.i(TAG, "onItemClick TVOutActivity");
		Xlog.v(TAG, "itemClick id is" + id);
		Xlog.v(TAG, "itemClick position is" + position);
		if (position < 0 || position > 6) {
			position = 0;
		}
		boolean b = tvOut.showPattern(position);
		Xlog.v(TAG, "onItemClick TVOutActivity result is " + b);
	}

}
