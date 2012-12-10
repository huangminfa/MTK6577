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

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.mediatek.omacp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.TextSize;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.xlog.Xlog;

import com.mediatek.omacp.provider.OmacpProviderDatabase;
import com.mediatek.omacp.parser.ApplicationClass;
import com.mediatek.omacp.parser.NapdefClass;
import com.mediatek.omacp.parser.OmacpParser;
import com.mediatek.omacp.parser.OmacpParserUtils;
import com.mediatek.omacp.parser.ProxyClass;
import com.mediatek.omacp.parser.ApplicationClass.AppAddr;
import com.mediatek.omacp.parser.ApplicationClass.AppAuth;
import com.mediatek.omacp.parser.ApplicationClass.Port;
import com.mediatek.omacp.parser.ApplicationClass.Resource;
import com.mediatek.omacp.parser.NapdefClass.NapAuthInfo;
import com.mediatek.omacp.parser.NapdefClass.Validity;
import com.mediatek.omacp.parser.ProxyClass.PxAuthInfo;
import com.mediatek.omacp.parser.ProxyClass.PxPhysical;

import com.mediatek.featureoption.FeatureOption;
import android.telephony.TelephonyManager;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class OmacpMessageSettingsDetail extends Activity implements OnClickListener{

	private static final String XLOG = "Omacp/OmacpMessageSettingsDetail";
	
	private static final boolean DEBUG = true;
	
	private static final String[] INSTALLATION_PROJECTION = {
    	OmacpProviderDatabase._ID, OmacpProviderDatabase.SIM_ID, OmacpProviderDatabase.INSTALLED, OmacpProviderDatabase.PIN_UNLOCK, 
    	OmacpProviderDatabase.SEC, OmacpProviderDatabase.MAC, OmacpProviderDatabase.BODY, OmacpProviderDatabase.CONTEXT,
    	OmacpProviderDatabase.MIME_TYPE
    };	
	
	private static final int ID               = 0;
	private static final int SIM_ID           = 1;
	private static final int INSTALLED        = 2;
	private static final int PIN_LOCK         = 3;
	private static final int SEC              = 4;
	private static final int MAC              = 5;
	private static final int BODY             = 6;
	private static final int CONTEXT          = 7;
	private static final int MIME_TYPE        = 8;
	
	private Object mMarkAsBlockedSyncer = new Object();
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	private boolean mMarkAsReadBlocked;
    
    private TextView mDetailText;
    private Button mFullInstallBtn;
    private Button mCustomInstallBtn;
    
    //IDs for alert dialog
    private static final int DIALOG_INSTALLING = 8000;
    private static final int DIALOG_RE_INSTALL_NOTIFY = 8001;
    private static final int DIALOG_UNLOCK_PIN = 8002;    
    private static final int DIALOG_UNLOCK_PIN_2 = 8003;
    
    //IDs for event
    private static final int EVENT_APPLICATION_INSTALL_TIME_OUT = 2000;
    private static final int EVENT_APN_INSTALL_TIME_OUT = 2001;
    private static final int INSTALL_TIME_OUT_LENGTH = 140000;
    private static final int EVENT_APN_SWITCH_TIME_OUT = 2002;
    private static final int APN_SWITCH_TIME_OUT_LENGTH = 30000;
    
    //Result constant
    private static final int RESULT_CONSTANT_SUCCEED = 1;
    private static final int RESULT_CONSTANT_NOT_RETURNED = 0;
    private static final int RESULT_CONSTANT_FAILED = -1;
    
    private static long mMessageId = -1;
    private static int mSimId = -1;
    private static boolean mInstalled = false;
    private boolean mReInstall = false;
    private static boolean mPinUnlock = false;
    private static int mSec = -1;
    private static String mMac;
    private static byte[] mBody;
    private static String mContextIdentifier; //currently not used
    private static String mMimeType;
    
    private static boolean mIsFullInstallation = true;
    
    private ArrayList<String> mApSettingsListName;
    private ArrayList<Boolean> mApSettingsListNameChecked = new ArrayList<Boolean>();
    private ArrayList<ResultType> mApplicationResultList = new ArrayList<ResultType>();
    private ResultType mApnResultObj;
    
    private ArrayList<ApplicationClass> mApList = null;
    private ArrayList<NapdefClass> mNapList = null;
    private ArrayList<ProxyClass> mPxList = null;
    
    private AlertDialog mCustomDialog;
    
	private static final String APP_ID_KEY = "appId";	
	private static final String APP_SETTING_ACTION = "com.mediatek.omacp.settings";
	private static final String APP_SETTING_RESULT_ACTION = "com.mediatek.omacp.settings.result";
    
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.message_settings_detail);
        
        mDetailText = (TextView)findViewById(R.id.message_settings_detail_info);
        mFullInstallBtn = (Button)findViewById(R.id.full_install_btn);
        mCustomInstallBtn = (Button)findViewById(R.id.custom_install_btn);
        
        mFullInstallBtn.setOnClickListener(this);
        mCustomInstallBtn.setOnClickListener(this);
        
        Intent intent = getIntent();
        
        if(DEBUG){
        	Xlog.i(XLOG, "OmacpMessageSettingsDetail onCreate savedInstanceState = " + savedInstanceState +
                    " intent = " + intent); 
        }
        
        initActivityState(intent);
        
        registerReceiver(mResultReceiver, new IntentFilter(APP_SETTING_RESULT_ACTION));
        
        setProgressBarIndeterminateVisibility(false);
	}
	
	@Override
    protected void onDestroy(){
	    super.onDestroy();
	    unregisterReceiver(mResultReceiver);
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
    	  switch (id) {
            case DIALOG_INSTALLING:
                ProgressDialog progressDialog = new ProgressDialog(this);                
                progressDialog.setMessage(getResources().getString(R.string.installing_progress_message));
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                return progressDialog;
                
            case DIALOG_RE_INSTALL_NOTIFY:
            	return new AlertDialog.Builder(this)
            	.setCancelable(true)
            	.setMessage(R.string.re_install_notify_message)
            	.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int whichButton) {
						// TODO Auto-generated method stub
						mReInstall = true;
						handleInstall();
					}
				})
				.setNegativeButton(R.string.no, null)
            	.setTitle(R.string.re_install_notify_title)
            	.create();
            	
            case DIALOG_UNLOCK_PIN:
            	LayoutInflater factory = LayoutInflater.from(this);
                final View textEntryView = factory.inflate(R.layout.unlock_pin_dialog_text_entry, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.unlock_pin_dialog_title)
                    .setCancelable(true)
                    .setView(textEntryView)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {        
                            /* User clicked OK so do some stuff */
                        	EditText editText = (EditText) ((LinearLayout)textEntryView).findViewById(R.id.pin_edit);
                        	String inputPin = editText.getText().toString();                        	
                        	if(isPinCorrect(inputPin.getBytes()) == false){
                        		if(DEBUG){
                        			Xlog.d(XLOG, "OmacpMessageSettingsDetail pin unlock failed, inputPin is : " + inputPin);
                        		}                        		
                        		showDialog(DIALOG_UNLOCK_PIN_2);
                        	}else{
                        		mPinUnlock = true;
                        		//write the database
                        		markMessageAsPinUnlock();
                        		handleInstall();
                        	}
                        	editText.setText("");
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {        
                            /* User clicked OK so do some stuff */
                        	EditText editText = (EditText) ((LinearLayout)textEntryView).findViewById(R.id.pin_edit);
                        	editText.setText("");
                        }
                    });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {					
					public void onCancel(DialogInterface arg0) {
						// TODO Auto-generated method stub
						EditText editText = (EditText) ((LinearLayout)textEntryView).findViewById(R.id.pin_edit);
                    	editText.setText("");
					}
				});
                AlertDialog pinDialog = builder.create();
                pinDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                return pinDialog;
                
            case DIALOG_UNLOCK_PIN_2:
            	LayoutInflater factory2 = LayoutInflater.from(this);
                final View textEntryView2 = factory2.inflate(R.layout.unlock_pin_dialog_text_entry, null);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this)
                    .setTitle(R.string.unlock_pin_dialog_title)
                    .setCancelable(true)
                    .setView(textEntryView2)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {        
                            /* User clicked OK so do some stuff */
                        	EditText editText = (EditText) ((LinearLayout)textEntryView2).findViewById(R.id.pin_edit);
                        	String inputPin = editText.getText().toString();
                        	if(isPinCorrect(inputPin.getBytes()) == false){
                        		if(DEBUG){
                        			Xlog.d(XLOG, "OmacpMessageSettingsDetail pin unlock failed, inputPin is : " + inputPin);
                        		}                        		
                        		showDialog(DIALOG_UNLOCK_PIN);
                        	}else{
                        		mPinUnlock = true;
                        		//write the database
                        		markMessageAsPinUnlock();
                        		handleInstall();
                        	}
                        	editText.setText("");
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {        
                            /* User clicked OK so do some stuff */
                        	EditText editText = (EditText) ((LinearLayout)textEntryView2).findViewById(R.id.pin_edit);
                        	editText.setText("");
                        }
                    });
                builder2.setOnCancelListener(new DialogInterface.OnCancelListener() {					
					public void onCancel(DialogInterface arg0) {
						// TODO Auto-generated method stub
						EditText editText = (EditText) ((LinearLayout)textEntryView2).findViewById(R.id.pin_edit);
                    	editText.setText("");
					}
				});
                
                AlertDialog pinDialog2 = builder2.create();
                pinDialog2.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                return pinDialog2;
            	
            default:
               return null;
        }
    }
	
	
	private void markMessageAsPinUnlock(){
		new Runnable() {
            public void run() {            	
            	synchronized (mMarkAsBlockedSyncer) {
					if (mMarkAsReadBlocked) {
						try {
							mMarkAsBlockedSyncer.wait();
						} catch (InterruptedException e) {
						}
					}
					
					ContentResolver resolver = OmacpMessageSettingsDetail.this.getContentResolver();

	                ContentValues values = new ContentValues(1);
	                values.put("pin_unlock", 1);
	                
	                Uri messageUri = ContentUris.withAppendedId(OmacpProviderDatabase.CONTENT_URI, mMessageId);

	                resolver.update(messageUri,
	                        values,
	                        null,
	                        null);
            	}
            }
        }.run();
	}
	
	private boolean isPinCorrect(byte[] pin){
		byte[] key;
		//if it is USERNETWPIN, then use imsi + user pin
		if(mSec == 2){
			String imsi = getSimImsi(mSimId);
			byte[] imsiKey = imsiToKey(imsi);
			
			int lenPin = pin.length;
			int lenImsiKey = imsiKey.length;
			int lenKey = lenPin + lenImsiKey;
			key = new byte[lenKey];
			for(int i = 0; i < lenKey; i ++){
				if(i < lenImsiKey){
					key[i] = imsiKey[i];
				}else{
					key[i] = pin[i - lenImsiKey];
				}
			}
		}else{
			key = pin;
		}
		return verifyPin(key, mSec, mBody, mMac);
	}
	
	public static String getSimImsi(int simId){
		String imsi;
    	if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            imsi = TelephonyManager.getDefault().getSubscriberIdGemini(simId);
        } else {
            imsi = TelephonyManager.getDefault().getSubscriberId();
        }
    	
    	Xlog.i(XLOG, "imsi is : " + imsi);
    	return imsi;
	}
	
	public static boolean verifyPin(byte[] pin, int sec, byte[] body, String mac){
		
		switch(sec){
		
		//NETWPIN, USERPIN, USERNETWPIN using the same authentication method: M = HMAC - SHA(K, A)
		case 0:
		case 1:
		case 2:
			String inputMac = null;
			try {
				inputMac = calculateRFC2104HMAC(body, pin);
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(inputMac != null && inputMac.equalsIgnoreCase(mac)){
				return true;
			}else{
				return false;
			}
			
		//USERPINMAC
		case 3:
			return verifyUSERPINMAC(pin);
			
		//error, then return false
		default:
			return false;
		}
	}
	
	private static boolean verifyUSERPINMAC(byte[] key){
		
		//calculate mMac's m(i)
		String mMacResult = "";
		char[] mMacCharArray = mMac.toCharArray();		
		int length = mMacCharArray.length;
		for(int i = 0; i < length; i ++){
			mMacResult += Integer.parseInt(mMacCharArray[i] + "") % 10 + 48;
		}
		
		//calculate user's m'(i)
		
		//first calculate the user's MAC
		String userMac = null;
		try {
			userMac = calculateRFC2104HMAC(mBody, key);
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//then calculate the user's m'(i) based on user's MAC
		String userMacResult = null;
		if(userMac == null){
			Xlog.e(XLOG, "OmacpMessageSettingsDetail verifyUSERPINMAC userMac is null.");
			return false;
		}
		
		char[] userMacCharArray = userMac.toCharArray();
		int lengthUser = userMacCharArray.length;
		for(int i = 0; i < lengthUser; i ++){
			userMacResult += Integer.parseInt(userMacCharArray[i] + "") % 10 + 48;
		}
		
		//compare m(i) and m'(i)
		if(mMacResult.equalsIgnoreCase(userMacResult)){
			return true;
		}else{
			return false;
		}		
	}
	
	/**
	*@param data
	* The data to be signed. (The wbxml as a string of Hex digits.)
	*@param key
	* The signing key. (E.g. USERPIN of '1234')
	*/
	public static String calculateRFC2104HMAC(byte[] data, byte[] pin) throws java.security.SignatureException {
		
		String result;
		
		try {
			
			//Get an hmac_sha1 key from the raw key bytes
			byte[] keyBytes = pin;		
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);
			 
			//Get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			//Compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data);
			
			//Convert raw bytes to Hex string
			result = bytesToHexString(rawHmac);

			if(DEBUG){
				Xlog.i(XLOG, "OmacpMessageSettingsDetail MAC is : " + result);
			}			
			
		}catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}
	
	/* Convert byte[] to hex string
	 * @param src byte[] data   
	 * @return hex string   
	 */      
	public static String bytesToHexString(byte[] src){   
	    StringBuilder stringBuilder = new StringBuilder("");   
	    if (src == null || src.length <= 0) {   
	        return null;   
	    }   
	    for (int i = 0; i < src.length; i++) {   
	        int v = src[i] & 0xFF;   
	        String hv = Integer.toHexString(v);   
	        if (hv.length() < 2) {   
	            stringBuilder.append(0);   
	        }   
	        stringBuilder.append(hv);   
	    }   
	    return stringBuilder.toString();   
	}
	
	/*
	 Network Pin is the IMSI.  The IMSI is required to be in the semi-octet format defined in GSM 11.11
     (ESTI: ETS 300 977) (See 10.3.2 page 54-55 of Version 5.10.1).
	 The IMSI description:
	
	  BYTE      Description          B8  B7 B6 B5    B4  B3 B2 B1
	   1        IMSI length
	   2        Digit 1 | Parity     M1  .. .. L1    P   0  0  1
	   3        Digit 3 | Digit 2    M3  .. .. L3    M2  .. .. L2
	   4        Digit 5 | Digit 4    M5  .. .. L5    M4  .. .. L4
	   5        Digit 7 | Digit 6    M7  .. .. L7    M6  .. .. L6
	   6        Digit 9 | Digit 8    M9  .. .. L9    M8  .. .. L8
	   7        Digit 11 | Digit 10  M11 .. .. L11   M10 .. .. L10
	   8        Digit 13 | Digit 12  M11 .. .. L11   M12 .. .. L12
	   9        Digit 15 | Digit 14  M11 .. .. L11   M14 .. .. L14
	   
	   Key:  P   - parity bit
	   Mx  - MSB of digit x
	   Lx  - LSB of digit x
	   
	   The IMSI length byte MUST NOT be used in the key.
	   
	   Any unused bytes (i.e. IMSI length less than 15) MUST NOT be
	   used in the key.
	   
	   If the IMSI length is an even number, the key MUST USE the
	   filler 0xF in the spare nibble.
	   
	   Example 1: IMSI Length is 15
	   
	   IMSI = 262022033864727
	   
	   GSM 11.11 Format:
	   
	   BYTE    Description               Value  Note
	   1      IMSI length               0x08   NOT USED IN THE KEY
	   2      Digit 1 | Parity          0x29
	   3      Digit 3 | Digit 2         0x26
	   4      Digit 5 | Digit 4         0x20
	   5      Digit 7 | Digit 6         0x02
	   6      Digit 9 | Digit 8         0x33
	   7      Digit 11 | Digit 10       0x68
	   8      Digit 13 | Digit 12       0x74
	   9      Digit 15 | Digit 14       0x72
	   
	   key should point to an array  {0x29, 0x26, 0x20, 0x02, 0x33, 0x68, 0x74, 0x72}
	   and keyLength should be 8
	   
	   Example 2: IMSI Length is 2
	   
	   IMSI = 26
	   
	   GSM 11.11 Format:
	   
	   BYTE    Description               Value  Note
	   1      IMSI length               0x02   NOT USED IN THE KEY
	   2      Digit 1 | Parity          0x29
	   3      Digit 3 | Digit 2         0xF6
	   4      Digit 5 | Digit 4         0xFF   UNUSED DATA
	   5      Digit 7 | Digit 6         0xFF   UNUSED DATA
	   6      Digit 9 | Digit 8         0xFF   UNUSED DATA
	   7      Digit 11 | Digit 10       0xFF   UNUSED DATA
	   8      Digit 13 | Digit 12       0xFF   UNUSED DATA
	   9      Digit 15 | Digit 14       0xFF   UNUSED DATA
	   
	   key should point to an array {0x21, 0xF6} and keyLength should be 2.
	 
	 */
	
	public static byte[] imsiToKey(String imsi){
		int len = imsi.length();
		int lenKey = len / 2 + 1;
		boolean even = len % 2 != 0;
		  
		byte[] key = new byte[lenKey];
		for(int i = 0; i < lenKey; i ++){
			if(i == 0){
				key[0] = (byte)(0x00 + (imsi.charAt(0) - '0') * 16 + 9);
			}else if(i == (lenKey - 1) && even == false){
				key[i] = (byte)(0x00 + 0xF0 + (imsi.charAt(len - 1) - '0'));
			}else{
				key[i] = (byte)(0x00 + (imsi.charAt(i * 2) - '0') * 16 + (imsi.charAt(i * 2 - 1) - '0'));
			}
		}
		
		Xlog.i(XLOG, "imsiToKey is : " + key);
		return key;
	}   
	
	
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view.getId() == mFullInstallBtn.getId()){
			if(DEBUG){
				Xlog.i(XLOG, "OmacpMessageSettingsDetail fullInstallBtn click.");		
			}
				
			mIsFullInstallation = true;
			handleInstall();
			
		}else if(view.getId() == mCustomInstallBtn.getId()){
			if(DEBUG){
				Xlog.i(XLOG, "OmacpMessageSettingsDetail customInstallBtn click.");
			}
			
			showCustomDialog();			
		}		
	}
	
	private void showCustomDialog(){
		int size = mApSettingsListName.size();
		CharSequence[] items = new CharSequence[size];
		boolean[] defaultValues = new boolean[size];
		for(int i = 0; i < size; i ++){
			items[i] = mApSettingsListName.get(i);
			defaultValues[i] = false;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMultiChoiceItems(items, defaultValues, new DialogInterface.OnMultiChoiceClickListener() {				
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked == true){
					mApSettingsListNameChecked.set(which, true);
				}else{
					mApSettingsListNameChecked.set(which, false);
				}
				
				if(DEBUG){
					Xlog.i(XLOG, "OmacpMessageSettingsDetail mApSettingsListNameChecked is : " + mApSettingsListNameChecked);
				}
				
				mCustomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				for(int i = 0; i < mApSettingsListNameChecked.size(); i ++){
					if(mApSettingsListNameChecked.get(i) == true){
						mCustomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
						break;
					}
				}
				
			}
		});
		
        builder.setTitle(R.string.custom_install_text);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.install_text, new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub					
				mIsFullInstallation = false;
				handleInstall();
			}
		});
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {					
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
				for(int i = 0; i < mApSettingsListNameChecked.size(); i ++){
					mApSettingsListNameChecked.set(i, false);
				}
			}
		});
        
        mCustomDialog = builder.create();
        mCustomDialog.getListView().clearChoices();
        for(int i = 0; i < size; i ++){
			mApSettingsListNameChecked.set(i, false);
		}	        
        mCustomDialog.show();
        mCustomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
	}
	
	private void initActivityState(Intent intent) {
		//If we have been passed a message_id, use that to find our message.
		Uri intentData = null;
	    long messageId = intent.getLongExtra("message_id", 0);
	    if (messageId > 0) {
	    	intentData = ContentUris.withAppendedId(OmacpProviderDatabase.CONTENT_URI, messageId);
	    } else {
	        intentData = intent.getData();
	    }
	    
	    if(DEBUG){
	    	Xlog.i(XLOG, "OmacpMessageSettingsDetail initActivityState intentData is : " + intentData);
	    }	    
	    
	    Cursor c = getContentResolver().query(intentData, INSTALLATION_PROJECTION, null, null, null);
	    if (c != null && c.moveToFirst()) {
        	try {
        		mMessageId = c.getLong(ID);
        		mSimId = c.getInt(SIM_ID);
        		mInstalled = (c.getInt(INSTALLED) != 0);        		
        		mPinUnlock = (c.getInt(PIN_LOCK) != 0);
        		mSec = c.getInt(SEC);
        		mMac = c.getString(MAC);
        		mBody = c.getBlob(BODY);
        		mContextIdentifier = c.getString(CONTEXT);
        		mMimeType = c.getString(MIME_TYPE);
            } finally {
                c.close();
            }            	
        }
	    
	    if(DEBUG){
	    	Xlog.i(XLOG, "OmacpMessageSettingsDetail initActivityState class variable is : \n"
		    		+ "mMessageId is : " + mMessageId + "\n"
		    		+ "mSimId is : " + mSimId + "\n"
		    		+ "mInstalled is : " + mInstalled + "\n"
		    		+ "mPinUnlock is : " + mPinUnlock + "\n"
		    		+ "mSec is : " + mSec + "\n"
		    		+ "mMac is : " + mMac + "\n"
		    		+ "mBody is : " + mBody + "\n"
		    		+ "mContextIdentifier is : " + mContextIdentifier + "\n"
		    		+ "mMimeType is : " + mMimeType + "\n");
	    }	    
	    
	    /*
         * Parse the omacp Message
         */
	    OmacpParser parser = new OmacpParser();
        if(mMimeType.equalsIgnoreCase("text/vnd.wap.connectivity-xml")){
        	parser.setParser(OmacpParser.getTextParser());
        }else if(mMimeType.equalsIgnoreCase("application/vnd.wap.connectivity-wbxml")){
        	parser.setParser(OmacpParser.getWbxmlParser());
        }
        parser.parse(mBody);
        mApList = parser.getApSectionList();
    	mNapList = parser.getNapList();
    	mPxList = parser.getPxList();
        
        if(DEBUG){
        	Xlog.i(XLOG, "OmacpMessageSettingsDetail mApList is : " + mApList);
            Xlog.i(XLOG, "OmacpMessageSettingsDetail mNapList is : " + mNapList);
            Xlog.i(XLOG, "OmacpMessageSettingsDetail mPxList is : " + mPxList);
        }        
	    
        mApSettingsListName = OmacpMessageUtils.getValidApplicationNameSet(this, mApList, mNapList);
        for(int i = 0; i < mApSettingsListName.size(); i ++){
        	mApSettingsListNameChecked.add(false);
        }
        
        MarkAsRead();
        
        SpannableStringBuilder settingsDetailInfo = OmacpMessageUtils.getSettingsDetailInfo(this, mApList, mNapList, mPxList);
        if(settingsDetailInfo == null || settingsDetailInfo.length() == 0){
        	showInvalidSettingDialog();
        }else{
        	mDetailText.setText(settingsDetailInfo);
            mDetailText.setTextSize(17.0f);
        }	    
	}
	
	private void showInvalidSettingDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.error)
	        .setMessage(this.getString(R.string.detail_invalid_setting_error_msg))
	        .setCancelable(true)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {				
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					OmacpMessageSettingsDetail.this.finish();
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface arg0) {
					// TODO Auto-generated method stub
					OmacpMessageSettingsDetail.this.finish();
				}
			})
	        .show();
	}

	private void MarkAsRead() {		
		new Thread(new Runnable() {
			public void run() {
				synchronized (mMarkAsBlockedSyncer) {
					if (mMarkAsReadBlocked) {
						try {
							mMarkAsBlockedSyncer.wait();
						} catch (InterruptedException e) {
							Xlog.e(XLOG, "OmacpMessageSettingDetail InterruptedException.");
						}
					}
					
					Uri messageUri = ContentUris.withAppendedId(OmacpProviderDatabase.CONTENT_URI, mMessageId);
					ContentValues mReadContentValues = new ContentValues(2);
		            mReadContentValues.put("read", 1);
		            mReadContentValues.put("seen", 1);
		            OmacpMessageSettingsDetail.this.getContentResolver().update(messageUri, mReadContentValues, null, null);
				}
				OmacpMessageNotification.updateAllNotifications(OmacpMessageSettingsDetail.this);
			}
		}).start();
    }
	
	
	private void handleInstall(){
		
		//If already installed, then notify the overwrite, else install directly
		if(mInstalled == true && mReInstall == false){
			showDialog(DIALOG_RE_INSTALL_NOTIFY);
			return;
		}
		
		//If pin is not unlocked, then notify the user to unlock the pin, else install directly
		if(mPinUnlock == false){
			showDialog(DIALOG_UNLOCK_PIN);			
			return;
		}
		
		//show progress dlg
		showDialog(DIALOG_INSTALLING);
		
		//Send the intent to apn first, when the apn's result is back, then sendIntentsToApplications
		sendIntentsToApn();
		
		//If there is no apn setting, then send the intent to the corresponding applications directly
		if(mApnResultObj == null){
			sendIntentsToApplications();
		}
	}
	
	private void sendIntentsToApn(){
		mApnResultObj = null;
		
		//If there is APN setting, in fact, it must be
		ArrayList<Intent> apnIntentList = new ArrayList<Intent>();
		if(mNapList != null && mNapList.size() > 0){
			
			int index = mApSettingsListName.indexOf(OmacpMessageUtils.getAppName(this, OmacpMessageUtils.APN_APPID));			
			if(!(mIsFullInstallation == false && mApSettingsListNameChecked.get(index) == false))
			{
				int napSize = mNapList.size();
				for(int i = 0; i < napSize; i ++){
					NapdefClass nap = mNapList.get(i);
					// BEARER parameter is optional
					// if(nap.BEARER.size() > 0 && nap.BEARER.get(0).equalsIgnoreCase("GSM-GPRS")){
						Intent it = new Intent();
						it.setAction(APP_SETTING_ACTION);
						String mimeType = "application/com.mediatek.omacp-apn";
						it.setType(mimeType);
						it.putExtra(APP_ID_KEY, OmacpMessageUtils.APN_APPID);
						it.putExtra("context", mContextIdentifier);
						it.putExtra("simId", mSimId);
						
						boolean flag = false;
						ProxyClass px = null;
						if(mPxList != null){
							int pxListSize = mPxList.size();
							for(int n = 0; n < pxListSize; n ++){
								px = mPxList.get(n);
								if(px.PXPHYSICAL != null && px.PXPHYSICAL.size() != 0){
									ArrayList<String> toNapIdList = px.PXPHYSICAL.get(0).TO_NAPID;
									if(toNapIdList != null){
										for(int m = 0; m < toNapIdList.size(); m ++){
											String toNapId = toNapIdList.get(m);
											if(nap.NAPID.equalsIgnoreCase(toNapId)){
												flag = true;
												break;
											}
										}
										if(flag == true){
											break;
										}
									}									
								}								
							}
						}
						
						if(DEBUG){
							Xlog.i(XLOG, "OmacpMessageSettingsDetail NAPID whether is in proxy, the flag is : " + flag);
						}						
						
						//add px parameters
						if(flag == true && px != null){
							//add application parameters
							it.putExtra("PROXY-ID", px.PROXY_ID);
							it.putExtra("PROXY-PW", px.PROXY_PW);
							it.putExtra("PPGAUTH-TYPE", px.PPGAUTH_TYPE);
							it.putExtra("PROXY-PROVIDER-ID", px.PROXY_PROVIDER_ID);
							it.putExtra("NAME", px.NAME);
//							it.putExtra("DOMAIN", px.DOMAIN);
							it.putExtra("TRUST", px.TRUST);
							it.putExtra("MASTER", px.MASTER);
							it.putExtra("STARTPAGE", px.STARTPAGE);
							it.putExtra("BASAUTH-ID", px.BASAUTH_ID);
							it.putExtra("BASAUTH-PW", px.BASAUTH_PW);
//							it.putExtra("WSP-VERSION", px.WSP_VERSION);
//							it.putExtra("PUSHENABLED", px.PUSHENABLED);
//							it.putExtra("PULLENBALED", px.PULLENBALED);
							//pxauthinfo
							int pxAuthInfoSize = px.PXAUTHINFO.size();
							ArrayList<HashMap<String, String>> pxAuthInfoMapList = new ArrayList<HashMap<String, String>>();
							ArrayList<PxAuthInfo> pxAuthInfoList = px.PXAUTHINFO;
							for(int j = 0; j < pxAuthInfoSize; j ++){
								HashMap<String, String> map = new HashMap<String, String>();
								map.put("PXAUTH-TYPE", pxAuthInfoList.get(j).PXAUTH_TYPE);
								map.put("PXAUTH-ID", pxAuthInfoList.get(j).PXAUTH_ID);
								map.put("PXAUTH-PW", pxAuthInfoList.get(j).PXAUTH_PW);
								
								pxAuthInfoMapList.add(map);
							}
							it.putExtra("PXAUTHINFO", pxAuthInfoMapList);
							
							//use only the first physical proxy, ignore others
							if(px.PXPHYSICAL != null && px.PXPHYSICAL.size() != 0){
								PxPhysical pxPhysical = px.PXPHYSICAL.get(0);
								it.putExtra("PHYSICAL-PROXY-ID", pxPhysical.PHYSICAL_PROXY_ID);
								it.putExtra("DOMAIN", pxPhysical.DOMAIN);
								it.putExtra("PXADDR", pxPhysical.PXADDR);
								it.putExtra("PXADDRTYPE", pxPhysical.PXADDRTYPE);
								it.putExtra("PXADDR-FQDN", pxPhysical.PXADDR_FQDN);
								it.putExtra("WSP-VERSION", pxPhysical.WSP_VERSION);
								it.putExtra("PUSHENABLED", pxPhysical.PUSHENABLED);
								it.putExtra("PULLENABLED", pxPhysical.PULLENABLED);
							}							
							
							//port
							if(px.PXPHYSICAL != null && px.PXPHYSICAL.size() > 0){
								int portSize = px.PXPHYSICAL.get(0).PORT.size();
								ArrayList<HashMap<String, String>> portMapList = new ArrayList<HashMap<String, String>>();
								ArrayList<Port> portList = px.PXPHYSICAL.get(0).PORT;
								for(int j = 0; j < portSize; j ++){
									HashMap<String, String> map = new HashMap<String, String>();
									map.put("PORTNBR", portList.get(j).PORTNBR);
									if(portList.get(j).SERVICE.size() != 0){
										map.put("SERVICE", portList.get(j).SERVICE.get(0)); //using the first one, ignore others
									}									
									portMapList.add(map);
								}
								it.putExtra("PORT", portMapList);
							}							
						}
						
						//napdef
						it.putExtra("NAPID", nap.NAPID);
						// Judge whether the BEARER size is 0.
						if (nap.BEARER.size() > 0 && nap.BEARER.get(0) != null) {
							it.putExtra("BEARER", nap.BEARER.get(0));
						}
						it.putExtra("NAP-NAME", nap.NAME);
						it.putExtra("INTERNET", nap.INTERNET);
						it.putExtra("NAP-ADDRESS", nap.NAP_ADDRESS);
						it.putExtra("NAP-ADDRTYPE", nap.NAP_ADDRTYPE);
						it.putExtra("DNS-ADDR", nap.DNS_ADDR);													
						it.putExtra("CALLTYPE", nap.CALLTYPE);
						it.putExtra("LOCAL-ADDR", nap.LOCAL_ADDR);
						it.putExtra("LOCAL-ADDRTYPE", nap.LOCAL_ADDRTYPE);
						it.putExtra("LINKSPEED", nap.LINKSPEED);
						it.putExtra("DNLINKSPEED", nap.DNLINKSPEED);
						it.putExtra("LINGER", nap.LINGER);
						it.putExtra("DELIVERY-ERR-SDU", nap.DELIVERY_ERR_SDU);
						it.putExtra("DELIVERY-ORDER", nap.DELIVERY_ORDER);
						it.putExtra("TRAFFIC-CLASS", nap.TRAFFIC_CLASS);
						it.putExtra("MAX-SDU-SIZE", nap.MAX_SDU_SIZE);
						it.putExtra("MAX-BITRATE-UPLINK", nap.MAX_BITRATE_UPLINK);
						it.putExtra("MAX-BITRATE-DNLINK", nap.MAX_BITRATE_DNLINK);
						it.putExtra("RESIDUAL-BER", nap.RESIDUAL_BER);
						it.putExtra("SDU-ERROR-RATIO", nap.SDU_ERROR_RATIO);
						it.putExtra("TRAFFIC-HANDL-PROI", nap.TRAFFIC_HANDL_PROI);
						it.putExtra("TRANSFER-DELAY", nap.TRANSFER_DELAY);
						it.putExtra("GUARANTEED-BITRATE-UPLINK", nap.GUARANTEED_BITRATE_UPLINK);
						it.putExtra("GUARANTEED-BITRATE-DNLINK", nap.GUARANTEED_BITRATE_DNLINK);
						it.putExtra("MAX-NUM-RETRY", nap.MAX_NUM_RETRY);
						it.putExtra("FIRST-RETRY-TIMEOUT", nap.FIRST_RETRY_TIMEOUT);
						it.putExtra("REREG-THRESHOLD", nap.REREG_THRESHOLD);
						it.putExtra("T-BIT", nap.T_BIT);
						//napauthinfo
						int napAuthInfoSize = nap.NAPAUTHINFO.size();
						ArrayList<HashMap<String, String>> napAuthInfoMapList = new ArrayList<HashMap<String, String>>();
						ArrayList<NapAuthInfo> napAuthInfoList = nap.NAPAUTHINFO;
						for(int j = 0; j < napAuthInfoSize; j ++){
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("AUTHTYPE", napAuthInfoList.get(j).AUTHTYPE);
							map.put("AUTHNAME", napAuthInfoList.get(j).AUTHNAME);
							map.put("AUTHSECRET", napAuthInfoList.get(j).AUTHSECRET);
							if(napAuthInfoList.get(j).AUTH_ENTITY.size() != 0){
								map.put("AUTH_ENTITY", napAuthInfoList.get(j).AUTH_ENTITY.get(0));
							}									
							map.put("SPI", napAuthInfoList.get(j).SPI);
								
							napAuthInfoMapList.add(map);
						}
						it.putExtra("NAPAUTHINFO", napAuthInfoMapList);
						
						//validity
						int validitySize = nap.VALIDITY.size();
						ArrayList<HashMap<String, String>> validityMapList = new ArrayList<HashMap<String, String>>();
						ArrayList<Validity> validityList = nap.VALIDITY;
						for(int j = 0; j < validitySize; j ++){
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("COUNTRY", validityList.get(j).COUNTRY);
							map.put("NETWORK", validityList.get(j).NETWORK);
							map.put("SID", validityList.get(j).SID);
							map.put("SOC", validityList.get(j).SOC);
							map.put("VALIDUNTIL", validityList.get(j).VALIDUNTIL);
								
							validityMapList.add(map);
						}
						it.putExtra("VALIDITY", validityMapList);
						
						//mtk apn type parameter
						String apnType = OmacpMessageUtils.getAPNType(mApList);
						it.putExtra("APN-TYPE", apnType);
						
						//mtk mms parameters						
						if(mApList != null){
							for(int d = 0; d < mApList.size(); d ++){
								if(mApList.get(d).APPID.equalsIgnoreCase(OmacpMessageUtils.MMS_APPID) || mApList.get(d).APPID.equalsIgnoreCase(OmacpMessageUtils.MMS_2_APPID)){
									ArrayList<String> addr = mApList.get(d).ADDR;
									if(addr != null && addr.size() > 0){
										it.putExtra("MMSC", addr.get(0));
										Xlog.i(XLOG, "apn MMSC is : " + addr.get(0));
										if(px != null && px.PXPHYSICAL != null && px.PXPHYSICAL.size() != 0){
											it.putExtra("MMS-PROXY", px.PXPHYSICAL.get(0).PXADDR);											
											Xlog.i(XLOG, "apn MMS PROXY is : " + px.PXPHYSICAL.get(0).PXADDR);
											if(px.PXPHYSICAL.get(0).PORT.size() != 0){
												it.putExtra("MMS-PORT", px.PXPHYSICAL.get(0).PORT.get(0).PORTNBR);												
												Xlog.i(XLOG, "apn MMS PORT is : " + px.PXPHYSICAL.get(0).PORT.get(0).PORTNBR);
											}
										}
										
										break;
									}else if(mApList.get(d).APPADDR.size() != 0 && mApList.get(d).APPADDR.get(0).ADDR != null){
										it.putExtra("MMSC", mApList.get(d).APPADDR.get(0).ADDR);
										Xlog.i(XLOG, "apn MMSC is : " + mApList.get(d).APPADDR.get(0).ADDR);
										if(px != null && px.PXPHYSICAL != null && px.PXPHYSICAL.size() != 0){
											it.putExtra("MMS-PROXY", px.PXPHYSICAL.get(0).PXADDR);											
											Xlog.i(XLOG, "apn MMS PROXY is : " + px.PXPHYSICAL.get(0).PXADDR);
											if(px.PXPHYSICAL.get(0).PORT.size() != 0){
												it.putExtra("MMS-PORT", px.PXPHYSICAL.get(0).PORT.get(0).PORTNBR);												
												Xlog.d(XLOG, "apn MMS PORT is : " + px.PXPHYSICAL.get(0).PORT.get(0).PORTNBR);
											}
										}
										
										break;
									}
								}							
							}
						}
						
						//apn id
						it.putExtra("APN-ID", String.valueOf(mMessageId));
						
						apnIntentList.add(it);
					}
				}
//			}
		}
		
		if(apnIntentList.size() > 0){
			Intent it = new Intent();
			it.setAction(APP_SETTING_ACTION);
			String mimeType = "application/com.mediatek.omacp-apn" ;
			it.setType(mimeType);
			it.putExtra("apn_setting_intent", apnIntentList);
			
			if(DEBUG){
				Xlog.i(XLOG, "OmacpMessageSettingsDetail sendBroadcast intent is : " + it);
			}
			
			this.sendBroadcast(it);
			
			mApnResultObj = new ResultType(OmacpMessageUtils.APN_APPID, RESULT_CONSTANT_NOT_RETURNED);
			
			timerHandler.sendEmptyMessageDelayed(EVENT_APN_INSTALL_TIME_OUT, INSTALL_TIME_OUT_LENGTH);
		}
	}
	
	//Send intents to applications except apn
	private void sendIntentsToApplications(){
		
		//reset, mApplicationResultList can not be null
		mApplicationResultList.clear();
		
		//In case mApList be null
		if(mApList != null){
			ArrayList<Intent> emailIntentList = new ArrayList<Intent>();
			
			int size = mApList.size(); 
			for(int i = 0; i < size; i ++){	
				ApplicationClass application = mApList.get(i);
				
				if(null == OmacpMessageUtils.getAppName(this, application.APPID)){
					Xlog.e(XLOG, "OmacpMessageSettingsDetail sendIntentsToApplications invalid application settings.");
					continue;
				}
				
				boolean isInclueded = false;
				for(int b = 0; b < mApplicationResultList.size(); b ++){
					if(OmacpMessageUtils.getAppName(this, mApplicationResultList.get(b).mAppId)
							.equalsIgnoreCase(OmacpMessageUtils.getAppName(this, application.APPID))){
						isInclueded = true;
						break;
					}
				}
				if(isInclueded == true){
					continue;
				}
				
				//if w4 mms setting only has mmsc, then ignore it, because it has been moved to apn
				if(application.APPID.equalsIgnoreCase(OmacpMessageUtils.MMS_APPID) && application.CM == null){
					continue;
				}else if(application.APPID.equalsIgnoreCase(OmacpMessageUtils.MMS_2_APPID) && application.CM == null && application.RM == null
						&& application.MS == null && application.PC_ADDR == null && application.Ma == null){
					//if ap0005 mms setting only has mmsc, then ignore it, because it has been moved to apn
					continue;
				}
				
				int index = mApSettingsListName.indexOf(OmacpMessageUtils.getAppName(this, application.APPID));
				if(mIsFullInstallation == false && mApSettingsListNameChecked.get(index) == false){
					continue;
				}
				
				Intent it = new Intent();
				it.setAction(APP_SETTING_ACTION);
				String mimeType = "application/com.mediatek.omacp-" + application.APPID;
				it.setType(mimeType);
				it.putExtra(APP_ID_KEY, application.APPID);
				it.putExtra("context", mContextIdentifier); //currently not used
				it.putExtra("simId", mSimId);
				//add application parameters
				it.putExtra("APPID", application.APPID);
				it.putExtra("PROVIDER-ID", application.PROVIDER_ID);
				it.putExtra("NAME", application.NAME);
				it.putExtra("AACCEPT", application.AACCEPT);
				it.putExtra("APROTOCOL", application.APROTOCOL);
				it.putExtra("TO-PROXY", application.TO_PROXY);
				it.putExtra("TO-NAPID", application.TO_NAPID);
				it.putExtra("ADDR", application.ADDR);
				//add application specific parameters
				it.putExtra("CM", application.CM);
				it.putExtra("RM", application.RM);
				it.putExtra("MS", application.MS);
				it.putExtra("PC-ADDR", application.PC_ADDR);
				it.putExtra("Ma", application.Ma);
				it.putExtra("INIT", application.INIT);
				it.putExtra("FROM", application.FROM);
				it.putExtra("RT-ADDR", application.RT_ADDR);
				it.putExtra("MAX-BANDWIDTH", application.MAX_BANDWIDTH);
				it.putExtra("NETINFO", application.NETINFO);
				it.putExtra("MIN-UDP-PORT", application.MIN_UDP_PORT);
				it.putExtra("MAX-UDP-PORT", application.MAX_UDP_PORT);
				it.putExtra("SERVICES", application.SERVICES);
				it.putExtra("CIDPREFIX", application.CIDPREFIX);
				
				//app addr
				int appAddrSize = application.APPADDR.size();
				ArrayList<HashMap<String, String>> appAddrMapList = new ArrayList<HashMap<String, String>>();
				ArrayList<AppAddr> appAddrList = application.APPADDR;
				for(int j = 0; j < appAddrSize; j ++){
					if(appAddrList.get(j).PORT.size() != 0){
						for(int n = 0; n < appAddrList.get(j).PORT.size(); n ++){
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("ADDR", appAddrList.get(j).ADDR);
							map.put("ADDRTYPE", appAddrList.get(j).ADDRTYPE);
							map.put("PORTNBR", appAddrList.get(j).PORT.get(n).PORTNBR);
							if(appAddrList.get(j).PORT.get(n).SERVICE.size() != 0){
								map.put("SERVICE", appAddrList.get(j).PORT.get(n).SERVICE.get(0)); //using the first one, ignore others
							}
							appAddrMapList.add(map);
						}
					}else{
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("ADDR", appAddrList.get(j).ADDR);
						map.put("ADDRTYPE", appAddrList.get(j).ADDRTYPE);
						appAddrMapList.add(map);
					}
					
				}
				it.putExtra("APPADDR", appAddrMapList);
				
				//app auth
				int appAuthSize = application.APPAUTH.size();
				ArrayList<HashMap<String, String>> appAuthMapList = new ArrayList<HashMap<String, String>>();
				ArrayList<AppAuth> appAuthList = application.APPAUTH;
				for(int j = 0; j < appAuthSize; j ++){
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("AAUTHLEVEL", appAuthList.get(j).AAUTHLEVEL);
					map.put("AAUTHTYPE", appAuthList.get(j).AAUTHTYPE);
					map.put("AAUTHNAME", appAuthList.get(j).AAUTHNAME);
					map.put("AAUTHSECRET", appAuthList.get(j).AAUTHSECRET);
					map.put("AAUTHDATA", appAuthList.get(j).AAUTHDATA);
					
					appAuthMapList.add(map);
				}
				it.putExtra("APPAUTH", appAuthMapList);
				
				//resource
				int resourceSize = application.RESOURCE.size();
				ArrayList<HashMap<String, String>> resourceMapList = new ArrayList<HashMap<String, String>>();
				ArrayList<Resource> resourceList = application.RESOURCE;
				for(int j = 0; j < resourceSize; j ++){
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("URI", resourceList.get(j).URI);
					map.put("NAME", resourceList.get(j).NAME);
					map.put("AACCEPT", resourceList.get(j).AACCEPT);
					map.put("AAUTHTYPE", resourceList.get(j).AAUTHTYPE);
					map.put("AAUTHNAME", resourceList.get(j).AAUTHNAME);
					map.put("AAUTHSECRET", resourceList.get(j).AAUTHSECRET);
					map.put("AAUTHDATA", resourceList.get(j).AAUTHDATA);
					map.put("STARTPAGE", resourceList.get(j).STARTPAGE);
					
					resourceMapList.add(map);
				}
				it.putExtra("RESOURCE", resourceMapList);
				
				if(application.APPID.equalsIgnoreCase(OmacpMessageUtils.SMTP_APPID) || application.APPID.equalsIgnoreCase(OmacpMessageUtils.POP3_APPID)
						|| application.APPID.equalsIgnoreCase(OmacpMessageUtils.IMAP4_APPID)){
					emailIntentList.add(it);
				}else{
					ResultType result = new ResultType(application.APPID, 0);					
					if(checkIfApplicationSupport(application.APPID) == true){
						if(DEBUG){
							Xlog.i(XLOG, "OmacpMessageSettingsDetail sendBroadcast intent is : " + it);
						}
						
						this.sendBroadcast(it);
					}else{
						result.mResult = RESULT_CONSTANT_FAILED; //If not support, then label it failed directly
					}
					mApplicationResultList.add(result);
				}
			}
			
			if(emailIntentList.size() > 0){
				Intent it = new Intent();
				it.setAction(APP_SETTING_ACTION);
				String mimeType = "application/com.mediatek.omacp-25" ;
				it.setType(mimeType);
				it.putExtra("email_setting_intent", emailIntentList);
				
				ResultType result = new ResultType(OmacpMessageUtils.SMTP_APPID, RESULT_CONSTANT_NOT_RETURNED);				
				if(checkIfApplicationSupport(OmacpMessageUtils.SMTP_APPID) == true){
					if(DEBUG){
						Xlog.i(XLOG, "OmacpMessageSettingsDetail sendBroadcast intent is : " + it);
					}
					
					this.sendBroadcast(it);
				}else{
					result.mResult = RESULT_CONSTANT_FAILED; //If not support, then label it failed directly
				}				
				mApplicationResultList.add(result);
			}
		}
		
		if(mApplicationResultList.size() > 0){
			boolean isNeedSendTimeoutMsg = false;
			for(int d = 0; d < mApplicationResultList.size(); d ++){
				if(mApplicationResultList.get(d).mResult == RESULT_CONSTANT_NOT_RETURNED){
					isNeedSendTimeoutMsg = true;
					break;
				}
			}
			//If all the application are not supported, then there setting result already set and can give the report directley
			if(isNeedSendTimeoutMsg == true){
				timerHandler.sendEmptyMessageDelayed(EVENT_APPLICATION_INSTALL_TIME_OUT, INSTALL_TIME_OUT_LENGTH);
			}else{
				handleFinishInstall();
			}
		}else{//If no application need to set, then give the report directly about apn setting result
			if(mApnResultObj != null){
				handleFinishInstall();
			}
		}
	}
	
	private static boolean checkIfApplicationSupport(String appId){
		
		if(appId.equalsIgnoreCase(OmacpMessageUtils.MMS_APPID)){
			return OmacpApplicationCapability.mms;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.BROWSER_APPID)){
			return OmacpApplicationCapability.browser;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.APN_APPID)){
			return true;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.IMAP4_APPID)){
			return OmacpApplicationCapability.email;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.POP3_APPID)){
			return OmacpApplicationCapability.email;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.SMTP_APPID)){
			return OmacpApplicationCapability.email;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.DM_APPID)){
			return OmacpApplicationCapability.dm;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.SUPL_APPID)){
			return OmacpApplicationCapability.supl;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.RTSP_APPID)){
			return OmacpApplicationCapability.rtsp;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.DS_APID)){
			return OmacpApplicationCapability.ds;
		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.IMPS_APPID)){
			return OmacpApplicationCapability.imps;
		}else{
			Xlog.e(XLOG, "OmacpMessageSettingsDetail getAppName unknown app.");
			return false;
		}		
	}
	
	private static class ResultType{
		public String mAppId;
		public int mResult;
		public ResultType(String appId, int result){
			mAppId = appId;
			mResult = result;
		}
	}
	
	
	
	private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {        
        @Override
        public void onReceive(Context context, Intent intent) {
        	if(intent.getAction().equals(APP_SETTING_RESULT_ACTION)){
        		String appId = intent.getStringExtra(APP_ID_KEY);        		
        		boolean result = intent.getBooleanExtra("result", false);        		

        		if(DEBUG){
        			Xlog.d(XLOG, "OmacpMessageSettingsDetail result received, appId is : " + appId + " " + "result is : " + result);
        		}        		
        		
        		if(appId.equalsIgnoreCase(OmacpMessageUtils.APN_APPID)){        			
        			if(mApnResultObj == null){
        				Xlog.e(XLOG, "OmacpMessageSettingsDetail mResultReceiver mApnResultObj is null.");
        			}else{
        				if(result == true){
        					mApnResultObj.mResult = RESULT_CONSTANT_SUCCEED;
        					//Due to GPRS switch, application install should wait until the GPRS network switch complete
        					//The following cases, no need
        					//1: no application to install
        					//2: WiFi
        					//3: APN install fail
        					if(mApList != null && mApList.size() != 0){
        						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            			        NetworkInfo info = cm.getActiveNetworkInfo();
            			        if(info != null /*&& info.isConnected() == false */&& info.getType() == ConnectivityManager.TYPE_MOBILE){
            			        	IntentFilter networkStateChangedFilter = new IntentFilter();
            			        	networkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            			        	mStickyIntent = OmacpMessageSettingsDetail.this.registerReceiver(mNetworkStateIntentReceiver, networkStateChangedFilter);
            			            timerHandler.sendEmptyMessageDelayed(EVENT_APN_SWITCH_TIME_OUT, APN_SWITCH_TIME_OUT_LENGTH);
            			            Xlog.d(XLOG, "OmacpMessageSettingsDetail mResultReceiver register apn switch receiver.");
            			        }else{
            			        	sendIntentsToApplications();
            			        }
        					}else{
        						sendIntentsToApplications();
        					}
        				}else{
        					mApnResultObj.mResult = RESULT_CONSTANT_FAILED;
        					sendIntentsToApplications();
        				}
//        				sendIntentsToApplications();
        			} 
        			timerHandler.removeMessages(EVENT_APN_INSTALL_TIME_OUT); 
        			
        		}else{
        			int size = mApplicationResultList.size();
            		
            		for(int i = 0; i < size; i ++){
            			ResultType obj = mApplicationResultList.get(i);
            			if(appId.equals(obj.mAppId)){
            				if(result == true){
            					obj.mResult = RESULT_CONSTANT_SUCCEED;
            				}else{
            					obj.mResult = RESULT_CONSTANT_FAILED;
            				}
            			}
            		}
            		
            		//check if all the results are returned
            		boolean flag = true;
            		for(int i = 0; i < mApplicationResultList.size(); i ++){
            			if(mApplicationResultList.get(i).mResult == RESULT_CONSTANT_NOT_RETURNED){
            				flag = false;
            				break;
            			}
            		}
            		
            		if(flag == true){
            			timerHandler.removeMessages(EVENT_APPLICATION_INSTALL_TIME_OUT);        			
            			handleFinishInstall();
            		}
        		}
        	}
        }
	};
	
	private Intent mStickyIntent = null;
	
	//network changed receiver to check if installed APN switch finished
	private BroadcastReceiver mNetworkStateIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                Xlog.d(XLOG, "Network Receiver info.getType():" + info.getType()
                            + "--info.isConnected():" + info.isConnected()
                            + "--info.isAvailable():" + info.isAvailable());
                if(mStickyIntent != null){
                	mStickyIntent = null;
                	Xlog.d(XLOG, "OmacpMessageSettingsDetail mNetworkStateIntentReceiver it is sticky intent, ignore it.");
                	return;
                }
                
                if (info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                	sendIntentsToApplications();
                	OmacpMessageSettingsDetail.this.unregisterReceiver(mNetworkStateIntentReceiver);  
                    timerHandler.removeMessages(EVENT_APN_SWITCH_TIME_OUT);
                }
            }
        }
    };
	
	private void markMessageAsInstalled(){
		new Runnable() {
            public void run() {
            	synchronized (mMarkAsBlockedSyncer) {
					if (mMarkAsReadBlocked) {
						try {
							mMarkAsBlockedSyncer.wait();
						} catch (InterruptedException e) {
						}
					}
					
					ContentResolver resolver = OmacpMessageSettingsDetail.this.getContentResolver();

	                ContentValues values = new ContentValues(1);
	                values.put("installed", 1);
	                
	                Uri messageUri = ContentUris.withAppendedId(OmacpProviderDatabase.CONTENT_URI, mMessageId);

	                resolver.update(messageUri,
	                        values,
	                        null,
	                        null);
            	}
            }
        }.run();
	}
	
	private void giveInstallationReport(){
		StringBuilder report = new StringBuilder();
		
		int size = mApplicationResultList.size();
		for(int i = 0; i < size; i ++){
			ResultType obj = mApplicationResultList.get(i);
			
			if(i > 0){
				report.append("\n");
			}
			
			//append app name
			report.append(OmacpMessageUtils.getAppName(this, obj.mAppId));
		
			//append ": "
			report.append(": ");
			
			//append installation result
			if(obj.mResult == RESULT_CONSTANT_SUCCEED){
				report.append(this.getString(R.string.result_success));
			}else if(obj.mResult == RESULT_CONSTANT_FAILED){
				report.append(this.getString(R.string.result_failed));
			}else{
				report.append(this.getString(R.string.unknown));
			}
			
		}
		
		if(mApnResultObj != null){
			if(report.length() > 0){
				report.append("\n");
			}
			
			//append apn name
			report.append(OmacpMessageUtils.getAppName(this, mApnResultObj.mAppId));
		
			//append ": "
			report.append(": ");
			
			//append installation result
			if(mApnResultObj.mResult == RESULT_CONSTANT_SUCCEED){
				report.append(this.getString(R.string.result_success));
			}else if(mApnResultObj.mResult == RESULT_CONSTANT_FAILED){
				report.append(this.getString(R.string.result_failed));
			}else{
				report.append(this.getString(R.string.unknown));
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.installation_report)
	        .setMessage(report)
	        .setCancelable(true)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {				
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					OmacpMessageSettingsDetail.this.finish();
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface arg0) {
					// TODO Auto-generated method stub
					OmacpMessageSettingsDetail.this.finish();
				}
			})
	        .show();
		
	}
	
	//Case: installing... and need more time, another omacp message comes, click the notification enter OmacpMessageList
	//When the installing time out to show the result, JE will happen because OmacpMessageList is the current activity
	//Solution: remove the messages of this activity when enter onStop function
	@Override
    protected void onStop() {
        super.onStop();
        timerHandler.removeMessages(EVENT_APPLICATION_INSTALL_TIME_OUT); 
        timerHandler.removeMessages(EVENT_APN_INSTALL_TIME_OUT); 
        timerHandler.removeMessages(EVENT_APN_SWITCH_TIME_OUT); 
    }
	
	private Handler timerHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_APPLICATION_INSTALL_TIME_OUT:
                    Xlog.e(XLOG, "OmacpMessageSettingsDetail application install time out......");                    

                    int size = mApplicationResultList.size();
            		for(int i = 0; i < size; i ++){
            			if(mApplicationResultList.get(i).mResult == RESULT_CONSTANT_NOT_RETURNED){
            				mApplicationResultList.get(i).mResult = RESULT_CONSTANT_FAILED;
            			}
            		}
            		
            		handleFinishInstall();            		
                    break;
                    
                case EVENT_APN_INSTALL_TIME_OUT:
                    Xlog.e(XLOG, "OmacpMessageSettingsDetail apn install time out......");                    

                    if(mApnResultObj.mResult == RESULT_CONSTANT_NOT_RETURNED){
                    	mApnResultObj.mResult = RESULT_CONSTANT_FAILED;
                    }
                    
                    sendIntentsToApplications();
                    break;
                case EVENT_APN_SWITCH_TIME_OUT:
                	Xlog.e(XLOG, "OmacpMessageSettingsDetail apn switch time out......");
                	sendIntentsToApplications();
                	break;
                default:
                	Xlog.e(XLOG, "OmacpMessageSettingsDetail no proper event type.");
                	break;
            }
        }
    };
    
    //when installation finished, handle the remaining things
    private void handleFinishInstall(){
    	removeDialog(DIALOG_INSTALLING);
		markMessageAsInstalled();
        mReInstall = false;
        mInstalled = true;
        giveInstallationReport();
    }
	
	public static Intent createIntent(Context context, long messageId) {
	    Intent intent = new Intent(context, OmacpMessageSettingsDetail.class);
	    if (messageId > 0) {
	        intent.setData(ContentUris.withAppendedId(OmacpProviderDatabase.CONTENT_URI, messageId));
	    }
	    return intent;
	}

}
