/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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

package com.mediatek.engineermode.audio;

import com.mediatek.engineermode.R;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import com.mediatek.xlog.Xlog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Audio extends Activity implements OnItemClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio);

		ListView Audio_listView = (ListView) findViewById(R.id.ListView_Audio);
		if (Audio_listView == null) {
			Xlog.e("EM/Audio", "clocwork worked...");
			// not return and let exception happened.
		}
		List<String> items = new ArrayList<String>();
		// items.add("K Tone");
		items.add("Normal Mode");
		items.add("Headset Mode");
		items.add("LoudSpeaker Mode");
		items.add("Speech Enhancement");
		items.add("Debug Info");
		items.add("Speech Logger");
		// Added by mtk54045 2012.05.22, to make user mode can dump audio data
		items.add("Audio Logger");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);
		Audio_listView.setAdapter(adapter);
		Audio_listView.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		Intent intent = new Intent();
		switch (arg2) {
		// case 0:
		// intent.setClass(this, Audio_KTone.class);
		// this.startActivity(intent);
		// break;
		case 0:
			intent.setClass(this, Audio_ModeSetting.class);
			intent.putExtra("CurrentMode", 0);
			this.startActivity(intent);
			break;
		case 1:
			intent.setClass(this, Audio_ModeSetting.class);
			intent.putExtra("CurrentMode", 1);
			this.startActivity(intent);
			break;
		case 2:
			intent.setClass(this, Audio_ModeSetting.class);
			intent.putExtra("CurrentMode", 2);
			this.startActivity(intent);
			break;
		case 3:
			intent.setClass(this, Audio_SpeechEnhancement.class);
			this.startActivity(intent);
			break;
		case 4:
			intent.setClass(this, Audio_DebugInfo.class);
			this.startActivity(intent);
			break;
		case 5:
			intent.setClass(this, Audio_SpeechLoggerX.class);
			this.startActivity(intent);
			break;
		case 6:
			intent.setClass(this, Audio_AudioLogger.class);
			this.startActivity(intent);			
			break;
		default:
			break;
		}
	}

}
