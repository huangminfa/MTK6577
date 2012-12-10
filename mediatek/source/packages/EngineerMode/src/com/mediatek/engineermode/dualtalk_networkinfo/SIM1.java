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

package com.mediatek.engineermode.dualtalk_networkinfo;

import com.mediatek.engineermode.R;
import com.mediatek.engineermode.Settings;
import com.mediatek.engineermode.networkinfo.NetworkInfo_Info;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import android.os.SystemProperties;

public class SIM1 extends Activity {
static final String LOG_TAG = "Dual_Talk_sim1NetworkInfo";
	
	public static final int CheckInfomation_ID = Menu.FIRST;
	
	private CheckBox mCheckBox[];
	private int mChecked[];
	public static final int TOTAL_ITEM_NUM = 72;//max
	
	public static final int MODEM_FDD = 1;
	public static final int MODEM_TD = 2;
	public static final int MODEM_NO3G = 3;
	
	public static final int MODEM_MASK_GPRS = 0x01;
	public static final int MODEM_MASK_EDGE = 0x02;
	public static final int MODEM_MASK_WCDMA = 0x04;
	public static final int MODEM_MASK_TDSCDMA = 0x08;
	public static final int MODEM_MASK_HSDPA = 0x10;
	public static final int MODEM_MASK_HSUPA = 0x20;
	
	private int modemType = MODEM_NO3G;
	
	public static int GetModemType()
	{
		String mt = SystemProperties.get("gsm.baseband.capability");
		Log.i(LOG_TAG, "gsm.baseband.capability " + mt);
		int mode = MODEM_NO3G;
		if (mt != null) {
			try {
				int mask = Integer.valueOf(mt);
				if ((mask & MODEM_MASK_TDSCDMA) != 0) {
					mode = MODEM_TD;
				} else if ((mask & MODEM_MASK_WCDMA) != 0) {
					mode = MODEM_FDD;
				} else {
					mode = MODEM_NO3G;
				}
			} catch (NumberFormatException e) {
				mode = MODEM_NO3G;
			}
		}
		else
		{
			mode = MODEM_NO3G;
		}
		return mode;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sim1_networkinfo);
        
        modemType = GetModemType();
        mCheckBox = new CheckBox[TOTAL_ITEM_NUM]; // may increase..
        mChecked = new int[TOTAL_ITEM_NUM];
        
        for(int i=0; i<TOTAL_ITEM_NUM; i++)
        {
        	mCheckBox[i] = null;
        	mChecked[i] = 0;
        }
        
        mCheckBox[0] = (CheckBox) findViewById(R.id.NetworkInfo_Cell);
        mCheckBox[1] = (CheckBox) findViewById(R.id.NetworkInfo_Ch);
        mCheckBox[2] = (CheckBox) findViewById(R.id.NetworkInfo_Ctrl);
        mCheckBox[3] = (CheckBox) findViewById(R.id.NetworkInfo_RACH);
        mCheckBox[4] = (CheckBox) findViewById(R.id.NetworkInfo_LAI);
        mCheckBox[5] = (CheckBox) findViewById(R.id.NetworkInfo_Radio);
        mCheckBox[6] = (CheckBox) findViewById(R.id.NetworkInfo_Meas);
        mCheckBox[7] = (CheckBox) findViewById(R.id.NetworkInfo_Ca);
        mCheckBox[8] = (CheckBox) findViewById(R.id.NetworkInfo_Control);
        mCheckBox[9] = (CheckBox) findViewById(R.id.NetworkInfo_SI2Q);
        mCheckBox[10] = (CheckBox) findViewById(R.id.NetworkInfo_MI);
        mCheckBox[11] = (CheckBox) findViewById(R.id.NetworkInfo_BLK);
        mCheckBox[12] = (CheckBox) findViewById(R.id.NetworkInfo_TBF);
        mCheckBox[13] = (CheckBox) findViewById(R.id.NetworkInfo_GPRS); 
        
        mCheckBox[21] = (CheckBox) findViewById(R.id.NetworkInfo_3GMmEmInfo);
        mCheckBox[27] = (CheckBox) findViewById(R.id.NetworkInfo_3GTcmMmiEmInfo);
        //47
        mCheckBox[47] = (CheckBox) findViewById(R.id.NetworkInfo_3GCsceEMServCellSStatusInd);//FDD != TDD
        mCheckBox[48] = (CheckBox) findViewById(R.id.NetworkInfo_xGCsceEMNeighCellSStatusInd);//2G != 3G
        mCheckBox[52] = (CheckBox) findViewById(R.id.NetworkInfo_3GCsceEmInfoMultiPlmn);
        //53
        mCheckBox[61] = (CheckBox) findViewById(R.id.NetworkInfo_3GMemeEmPeriodicBlerReportInd);
        mCheckBox[63] = (CheckBox) findViewById(R.id.NetworkInfo_3GUrrUmtsSrncId);
        mCheckBox[65] = (CheckBox) findViewById(R.id.NetworkInfo_3GMemeEmInfoHServCellInd);
        
		View view3GFDD = (View)findViewById(R.id.View_3G_FDD);
		View view3GTDD = (View)findViewById(R.id.View_3G_TDD);
		View view3GCommon = (View)findViewById(R.id.View_3G_COMMON);
		if(modemType == MODEM_FDD)
		{
			view3GTDD.setVisibility(View.GONE);
			mCheckBox[53] = (CheckBox) findViewById(R.id.NetworkInfo_3GMemeEmInfoUmtsCellStatus);
			mCheckBox[64] = (CheckBox) findViewById(R.id.NetworkInfo_3GSlceEmPsDataRateStatusInd);
		}
		else if(modemType == MODEM_TD)
		{
			view3GFDD.setVisibility(View.GONE);
			mCheckBox[64] = (CheckBox) findViewById(R.id.NetworkInfo_3GHandoverSequenceIndStuct);
			mCheckBox[67] = (CheckBox) findViewById(R.id.NetworkInfo_3GUl2EmAdmPoolStatusIndStruct);
			mCheckBox[68] = (CheckBox) findViewById(R.id.NetworkInfo_3GUl2EmPsDataRateStatusIndStruct);
			mCheckBox[69] = (CheckBox) findViewById(R.id.NetworkInfo_3Gul2EmHsdschReconfigStatusIndStruct);
			mCheckBox[70] = (CheckBox) findViewById(R.id.NetworkInfo_3GUl2EmUrlcEventStatusIndStruct);
			mCheckBox[71] = (CheckBox) findViewById(R.id.NetworkInfo_3GUl2EmPeriodicBlerReportInd);
		}
		else
		{
			view3GCommon.setVisibility(View.GONE);
			view3GTDD.setVisibility(View.GONE);
			view3GFDD.setVisibility(View.GONE);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, CheckInfomation_ID, 0, "Check Information");
		return true;		
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem aMenuItem) {   
        
        switch (aMenuItem.getItemId()) {   
            case CheckInfomation_ID:
            	Boolean isAnyChechked = false;
            	for(int i = 0; i < TOTAL_ITEM_NUM; i ++)
            	{
            		if(mCheckBox[i] == null)
            		{
            			continue;
            		}
            		if(true == mCheckBox[i].isChecked())
            		{
            			mChecked[i] = 1;
            			isAnyChechked = true;
            		}
            		else
            		{
            			mChecked[i] = 0;
            		}
            	}
            	if(false == isAnyChechked)
            	{
            		Toast.makeText(this, "Please select the items you want to check.", Toast.LENGTH_LONG).show();
            		break;
            	}
                Intent intent = new Intent(this, Sim1_Info.class);
                intent.putExtra("mChecked", mChecked);
                this.startActivity(intent);
                break;   
 
        }   
        return super.onOptionsItemSelected(aMenuItem);
	}
}
