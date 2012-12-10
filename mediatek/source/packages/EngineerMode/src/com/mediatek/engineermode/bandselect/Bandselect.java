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

package com.mediatek.engineermode.bandselect;

import com.mediatek.engineermode.R;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.mediatek.xlog.Xlog;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.PhoneFactory;
import android.os.AsyncResult;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.ITelephony;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;

public class Bandselect extends Activity implements OnClickListener{
		
	private static final int EVENT_QUERY_SUPPORTED = 1;
	private static final int EVENT_QUERY_CURRENT = 2;
	private static final int EVENT_SET = 3;

	private GeminiPhone geminiPhone = null;
	private Phone phone = null;

    static final String LOG_TAG = "BandMode";

	private boolean isThisAlive = false;
	@Override
	public
	void onDestroy()
	{
		isThisAlive = false;
		super.onDestroy();
	}	
	
	private class ModeMap
	{
		ModeMap(CheckBox c, int b)
		{
			chkBox = c;
			bit = b;
		}
		CheckBox chkBox;
		int bit;
	}
	private ArrayList<ModeMap> GSMModeArray = new ArrayList<ModeMap>();
	private ArrayList<ModeMap> UMTSModeArray = new ArrayList<ModeMap>();
	private Button btnSet;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bandselect);
        
        
        Xlog.v(LOG_TAG, "onCreate");
        isThisAlive = true;
        
    	     
                              
        //get the object of the controls
    	btnSet = (Button) findViewById(R.id.BandSel_Btn_Set);
    	
    	GSMModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_GSM_EGSM900), 1));
    	GSMModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_GSM_DCS1800), 3));
    	GSMModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_GSM_PCS1900), 4));
    	GSMModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_GSM_GSM850), 7));
    	
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_I), 0));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_II), 1));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_III), 2));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_IV), 3));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_V), 4));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_VI), 5));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_VII), 6));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_VIII), 7));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_IX), 8));
    	UMTSModeArray.add(new ModeMap((CheckBox) findViewById(R.id.BandSel_UMTS_BAND_X), 9));
    	
    	
    	btnSet.setOnClickListener(this); 

    }
	
	@Override
	protected void onResume() {
		if(FeatureOption.MTK_GEMINI_SUPPORT)
		{
		Xlog.v(LOG_TAG, "geminiPhone");
		geminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
		
		}else {
			Xlog.v(LOG_TAG, "phone");
			phone = PhoneFactory.getDefaultPhone();
		}
		QuerySupport();
    	QueryCurrent();
		super.onResume();
	}
	
	
	private Handler mResponseHander = new Handler() {
		public void handleMessage(Message msg){
			
			if(!isThisAlive)
			{
				return;
			}
			AsyncResult ar;
			switch(msg.what)
			{
			case EVENT_QUERY_SUPPORTED:
				ar= (AsyncResult) msg.obj;
                if (ar.exception != null) {
                	AlertDialog.Builder builder = new AlertDialog.Builder(Bandselect.this);
    				builder.setTitle("Query Failed");
    				builder.setMessage("Query Supported Mode Failed."+ar.exception.toString());
    				builder.setPositiveButton("Close" , null);
    				builder.create().show();
    				setSupportedMode(0, 0);
    				return;
                } else {
                	String s = (String)(ar.result.getClass().getName());                	
                	String[] ss = (String[])ar.result;
                	for(String m: ss)
                	{
                		Xlog.v(LOG_TAG, "--.>"+m);
                		String sss = m.substring("+EPBSE:".length());
                		String[] val = sss.split(",");
                        if (val != null && val.length > 1) {
                            try {
                		setSupportedMode(Integer.valueOf(val[0].trim()), Integer.valueOf(val[1].trim()));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(Bandselect.this, "AT cmd failed.", Toast.LENGTH_LONG);
                            }
                        }
                	}
                	Xlog.v(LOG_TAG, "EVENT_QUERY_SUPPORTED"+s);
                	//setSupportedMode(255, 9);
                }
				break;
			case EVENT_QUERY_CURRENT:
				ar= (AsyncResult) msg.obj;
                if (ar.exception != null) {
                	AlertDialog.Builder builder = new AlertDialog.Builder(Bandselect.this);
    				builder.setTitle("Query Failed");
    				builder.setMessage("Query Current Mode Failed."+ar.exception.toString());
    				builder.setPositiveButton("Close" , null);
    				builder.create().show();
    				setCurrentMode(0, 0);
    				return;
                } else {
                	String s = (String)(ar.result.getClass().getName());
                	
                	String[] ss = (String[])ar.result;
                	for(String m: ss)
                	{
                		Xlog.v(LOG_TAG, "--.>"+m);
                		String sss = m.substring("+EPBSE:".length());
                		String[] val = sss.split(",");
                        if (val != null && val.length > 1) {
                            try {
                		setCurrentMode(Integer.valueOf(val[0].trim()), Integer.valueOf(val[1].trim()));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                	}
                	Xlog.v(LOG_TAG, "EVENT_QUERY_CURRENT"+s);
                	//setCurrentMode(5, 9);
                }
				break;
			case EVENT_SET:
				ar= (AsyncResult) msg.obj;
                if (ar.exception != null) {
                	AlertDialog.Builder builder = new AlertDialog.Builder(Bandselect.this);
    				builder.setTitle("Failed.");
    				builder.setMessage("Set Failed."+ar.exception.toString());
    				builder.setPositiveButton("Close" , null);
    				builder.create().show();
    				return;
                } else {
                	AlertDialog.Builder builder = new AlertDialog.Builder(Bandselect.this);
    				builder.setTitle("Success");
    				builder.setMessage("Set OK.");
    				builder.setPositiveButton("OK" , null);
    				builder.create().show();
                	
                }
				break;
				        
			default:
				break;
			}
			
		}
	};
	

	private void QuerySupport()
	{
		String modeString[] = {"AT+EPBSE=?", "+EPBSE:"};
		Xlog.v(LOG_TAG, "AT String:" + modeString[0]);
		if(FeatureOption.MTK_GEMINI_SUPPORT){
		geminiPhone.invokeOemRilRequestStringsGemini(modeString,  mResponseHander.obtainMessage(EVENT_QUERY_SUPPORTED),Phone.GEMINI_SIM_1); 
		}else{
		phone.invokeOemRilRequestStrings(modeString,  mResponseHander.obtainMessage(EVENT_QUERY_SUPPORTED));	
		}
	}
	private void QueryCurrent()
	{
		String modeString[] = {"AT+EPBSE?", "+EPBSE:"};
		Xlog.v(LOG_TAG, "AT String:" + modeString[0]);
		if(FeatureOption.MTK_GEMINI_SUPPORT){
		geminiPhone.invokeOemRilRequestStringsGemini(modeString, mResponseHander.obtainMessage(EVENT_QUERY_CURRENT),Phone.GEMINI_SIM_1); 
		}else{
		phone.invokeOemRilRequestStrings(modeString, mResponseHander.obtainMessage(EVENT_QUERY_CURRENT)); 
		}
	}
	private boolean SetBandMode(int gsm, int umts)
	{
		if(gsm > 0xFF || umts > 0xFFFF)
		{
			return false;
		}
		if(gsm == 0)// null select is not allowed.
		{
			gsm = 0xFF;			
		}
		if(umts == 0)
		{
			umts = 0xFFFF;
		}
		
		String modeString[] = {"AT+EPBSE="+gsm+","+umts, ""};
		Xlog.v(LOG_TAG, "AT String:" + modeString[0]);
		if(FeatureOption.MTK_GEMINI_SUPPORT){
		geminiPhone.invokeOemRilRequestStringsGemini(modeString, mResponseHander.obtainMessage(EVENT_SET),Phone.GEMINI_SIM_1); 
		}else{
		phone.invokeOemRilRequestStrings(modeString, mResponseHander.obtainMessage(EVENT_SET)); 
		}
				
		setCurrentMode(gsm, umts);
		return true;
	}
	private int getValFromGSMBox()
	{
		int val = 0;
		for(ModeMap m : GSMModeArray)
		{
			if(m.chkBox.isChecked())
			{
				val |= (1 << m.bit);
			}
		}
		return val;
	}
	
	private int getValFromUMTSBox()
	{
		int val = 0;
		for(ModeMap m : UMTSModeArray)
		{
			if(m.chkBox.isChecked())
			{
				val |= (1 << m.bit);
			}
		}
		return val;
	}

	private void setCurrentMode(int gsmVal, int umtsVal)
	{
		for(ModeMap m : GSMModeArray)
		{
			if((gsmVal & (1 << m.bit)) != 0)
			{
				if(m.chkBox.isEnabled())
					m.chkBox.setChecked(true);
			}
			else
			{
				m.chkBox.setChecked(false);
			}
		}		
		for(ModeMap m : UMTSModeArray)
		{
			if((umtsVal & (1 << m.bit)) != 0)
			{
				if(m.chkBox.isEnabled())
					m.chkBox.setChecked(true);
			}
			else
			{
				m.chkBox.setChecked(false);
			}
		}	
	}
	private void setSupportedMode(int gsmVal, int umtsVal)
	{		
		for(ModeMap m : GSMModeArray)
		{
			if((gsmVal & (1 << m.bit)) != 0)
			{
				m.chkBox.setEnabled(true);
			}
			else
			{
				m.chkBox.setEnabled(false);
			}
		}
		for(ModeMap m : UMTSModeArray)
		{
			if((umtsVal & (1 << m.bit)) != 0)
			{
				m.chkBox.setEnabled(true);
			}
			else
			{
				m.chkBox.setEnabled(false);
			}
		}	
	}
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

       Xlog.v(LOG_TAG, "onClick:" + arg0.getId());     
        
        if(arg0.getId() == btnSet.getId())
        {
        	SetBandMode(getValFromGSMBox(), getValFromUMTSBox());
        }        
       
	}
	

	
	
	
	
}
