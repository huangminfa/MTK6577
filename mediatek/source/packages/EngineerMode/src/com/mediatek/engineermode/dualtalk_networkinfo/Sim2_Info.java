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
import android.view.View.OnClickListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.mediatek.engineermode.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.featureoption.FeatureOption;

import android.os.SystemProperties;
import android.os.AsyncResult;
import android.app.AlertDialog;
public class Sim2_Info extends Activity  implements OnClickListener{

	private final String TAG = "Sim2_Info";
	
	private int mIsChecked[];
	private Button mPageUp;
	private Button mPageDown;
	private TextView mInfo;
	private int mItemCount = 0;
	private int mCurrentItem = 0;
	private int mItem[];
	//private static final int TOTAL_ITEM_NUM = 66;
	
	Sim2Info_URCParser NWInfoParser;
	private GeminiPhone phone = null;
	private static final int EVENT_NW_INFO = 1;
	private static final int EVENT_NW_INFO_AT = 2;
	private static final int EVENT_NW_INFO_INFO_AT = 3;
	private static final int EVENT_NW_INFO_CLOSE_AT = 4;
	
	private final int BUF_SIZE_2G = 2204;
	private final int BUF_SIZE_3G_FDD = 2192;
	private final int BUF_SIZE_3G_TDD = 364;
	private final int BUF_SIZE_XG_IDX48 = 456*2;
	private int TOTAL_BUF_SIZE = 0;
	private final String NWInfoFileName= "DualTalkSim2NetworkInfo.urc";
	
	private static final Boolean mAlignmentEnable = true;
	private static final Boolean mEGPRSModeEnable = true;
	private static final Boolean mAMRSupprotEnable = true;
	private static final Boolean mFwpNcLaiInfoEnable = false;
	
	
	//should be offset.,.
	private int mCellSelSize = 0;
	private int mChDscrSize = 0;
	private int mCtrlchanSize = 0;
	private int mRACHCtrlSize = 0;
	private int mLAIInfoSize = 0;
	private int mRadioLinkSize = 0;
	private int mMeasRepSize = 0;
	private int mCaListSize = 0;
	private int mControlMsgSize = 0;
	private int mSI2QInfoSize = 0;
	private int mMIInfoSize = 0;
	private int mBLKInfoSize = 0;
	private int mTBFInfoSize = 0;
	private int mGPRSGenSize = 0;
	
	//LXO, continue these stupid code.
	//FDD TDD code
	private int m3GMmEmInfoSize = 0;
	private int m3GTcmMmiEmInfoSize = 0;
	private int m3GCsceEMServCellSStatusIndSize = 0;
	private int m3GCsceEmInfoMultiPlmnSize = 0;
	private int m3GMemeEmInfoUmtsCellStatusSize = 0;
	private int m3GMemeEmPeriodicBlerReportIndSize = 0;
	private int m3GUrrUmtsSrncIdSize = 0;
	private int m3GSlceEmPsDataRateStatusIndSize = 0;
	private int m3GMemeEmInfoHServCellIndSize = 0;
	
	//TDD code
	private int m3GHandoverSequenceIndStuctSize = 0;  //alignment enabled
	private int m3GUl2EmAdmPoolStatusIndStructSize = 0;
	private int m3GUl2EmPsDataRateStatusIndStructSize = 0;
	private int m3Gul2EmHsdschReconfigStatusIndStructSize = 0;
	private int m3GUl2EmUrlcEventStatusIndStructSize = 0;
	private int m3GUl2EmPeriodicBlerReportIndSize = 0;
	
	private int mxGCsceEMNeighCellSStatusIndStructSize = 0;
	private int flag = 0;
	
	public int calcBufferSize() //simple and naive, get max buffer.
	{
		int size = 0;
		
		size = BUF_SIZE_2G + BUF_SIZE_3G_FDD + BUF_SIZE_3G_TDD + BUF_SIZE_XG_IDX48;		
		
		return size;
	}
	
	private void calcOffset()
	{
		 mCellSelSize += 6;
		 mChDscrSize += 308 + mCellSelSize;
		 mCtrlchanSize += 14 + mChDscrSize;
		 mRACHCtrlSize += 14 + mCtrlchanSize;
		 mLAIInfoSize += 28 + mRACHCtrlSize;
		 mRadioLinkSize += 16 + mLAIInfoSize;
		 mMeasRepSize += 1368 + mRadioLinkSize;
		 mCaListSize += 260 + mMeasRepSize;
		 mControlMsgSize += 4 + mCaListSize;
		 mSI2QInfoSize += 10 + mControlMsgSize;
		 mMIInfoSize += 8 + mSI2QInfoSize;
		 mBLKInfoSize += 80 + mMIInfoSize;
		 mTBFInfoSize += 56 + mBLKInfoSize;
		 mGPRSGenSize += 32 + mTBFInfoSize;
		
		//union
		 mxGCsceEMNeighCellSStatusIndStructSize += 456*2 + mGPRSGenSize;
		 
		//LXO, continue these stupid code.
		//FDD TDD code
		 m3GMmEmInfoSize += 26*2 + mxGCsceEMNeighCellSStatusIndStructSize;
		 m3GTcmMmiEmInfoSize += 7*2 + m3GMmEmInfoSize;
		 m3GCsceEMServCellSStatusIndSize += 44*2 + m3GTcmMmiEmInfoSize;
		 m3GCsceEmInfoMultiPlmnSize += 37*2 + m3GCsceEMServCellSStatusIndSize;
		 m3GMemeEmInfoUmtsCellStatusSize += 772*2 + m3GCsceEmInfoMultiPlmnSize;
		 m3GMemeEmPeriodicBlerReportIndSize += 100*2 + m3GMemeEmInfoUmtsCellStatusSize;
		 m3GUrrUmtsSrncIdSize += 2*2 + m3GMemeEmPeriodicBlerReportIndSize;
		 m3GSlceEmPsDataRateStatusIndSize += 100*2 + m3GUrrUmtsSrncIdSize;
		 m3GMemeEmInfoHServCellIndSize += 8*2 + m3GSlceEmPsDataRateStatusIndSize;
		
		//TDD code
		 m3GHandoverSequenceIndStuctSize += 16*2 + mxGCsceEMNeighCellSStatusIndStructSize;  //alignment enabled
		 m3GUl2EmAdmPoolStatusIndStructSize += 32*2 + m3GHandoverSequenceIndStuctSize;
		 m3GUl2EmPsDataRateStatusIndStructSize += 8*2 + m3GUl2EmAdmPoolStatusIndStructSize;
		 m3Gul2EmHsdschReconfigStatusIndStructSize += 8*2 + m3GUl2EmPsDataRateStatusIndStructSize;
		 m3GUl2EmUrlcEventStatusIndStructSize += 18*2 + m3Gul2EmHsdschReconfigStatusIndStructSize;
		 m3GUl2EmPeriodicBlerReportIndSize += 100*2 + m3GUl2EmUrlcEventStatusIndStructSize;
		 
		
		 
	}
	
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
        setContentView(R.layout.networkinfo_info);        
       
        TOTAL_BUF_SIZE = calcBufferSize();
        byte initial[] = new byte[TOTAL_BUF_SIZE];
        for(int r = 0; r < TOTAL_BUF_SIZE; r ++)
        {
        	initial[r] = '0';
        }
        try {  
        	//due to use of file data storage, must follow the following way
        	FileOutputStream outputStream = this.openFileOutput(NWInfoFileName, MODE_PRIVATE);
			outputStream.write(initial);
			outputStream.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //dynamic calculate the block size
        if(false == mAlignmentEnable)
		{
			mChDscrSize -= 4;
			mLAIInfoSize -= 2;
			mMeasRepSize -= 14;
			mBLKInfoSize -= 4;
			mTBFInfoSize -= 4;
			
			//LXO
			m3GMmEmInfoSize -= 1*2;
			m3GCsceEMServCellSStatusIndSize -= 5*2;
			m3GMemeEmInfoUmtsCellStatusSize -= 4*2;
			m3GMemeEmPeriodicBlerReportIndSize -= 6*2;
			m3GSlceEmPsDataRateStatusIndSize -= 5*2;
			
			m3GHandoverSequenceIndStuctSize -= 3*2;
			m3GUl2EmUrlcEventStatusIndStructSize -= 1*2;
			m3GUl2EmPeriodicBlerReportIndSize -= 27*2;
			
			mxGCsceEMNeighCellSStatusIndStructSize -= 0;// must do alignment.
			
		}
		if(false == mEGPRSModeEnable)
		{
			mBLKInfoSize -= 36;
			mTBFInfoSize -= 2;
		}
		if(false == mAMRSupprotEnable)
		{
			mChDscrSize -= 24;
			mMeasRepSize -= 14;
		}
		if(true == mFwpNcLaiInfoEnable)
		{
			mMeasRepSize += 134;
		}
		int modemType = SIM2.GetModemType();
		if (modemType == SIM2.MODEM_TD) {
			m3GCsceEMServCellSStatusIndSize -= 4 * 2;
		}
		calcOffset();
        
	Log.i(TAG, "The total data size is : " + mGPRSGenSize);

        //get the selected item and store its ID into the mItem array
        mItem = new int[SIM2.TOTAL_ITEM_NUM];              
        Intent intent = getIntent();
        mIsChecked = intent.getIntArrayExtra("mChecked");
        if(mIsChecked == null)
        {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Error");
					builder.setMessage("intent.getIntArrayExtra() return null.");
					builder.setPositiveButton("OK" , null);
					builder.create().show();
        	mIsChecked = new int[1];
        	mIsChecked[0] = 1;		
        }
        
        for(int i = 0; i < mIsChecked.length; i ++)
        {
        	if(1 == mIsChecked[i])
        	{
        		mItem[mItemCount] = i;
        		mItemCount ++;
        	}
        }
        
        //send AT Cmd and register the event
        phone =(GeminiPhone) PhoneFactory.getDefaultPhone();
        if(phone == null)
        {
        	Log.e(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
        phone.registerForNetworkInfoGemini(mResponseHander, EVENT_NW_INFO, null, Phone.GEMINI_SIM_2);
        
        String ATCmd[] = new String[2];
        ATCmd[0] = "AT+EINFO?";
        ATCmd[1] = "+EINFO";
        phone.invokeOemRilRequestStringsGemini(ATCmd, mATCmdHander.obtainMessage(EVENT_NW_INFO_AT), Phone.GEMINI_SIM_2);

        
        mInfo = (TextView) findViewById(R.id.NetworkInfo_Info);
        mPageUp = (Button) findViewById(R.id.NetworkInfo_PageUp);
        mPageDown = (Button) findViewById(R.id.NetworkInfo_PageDown);
        if(mInfo == null
        		|| mPageUp == null
        		|| mPageDown == null
        		)
        {
        	Log.e(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
        
        mPageUp.setOnClickListener(this);
        mPageDown.setOnClickListener(this);
        
        //initial info display
        NWInfoParser = new Sim2Info_URCParser(this);
        mInfo.setText("<" + (mCurrentItem + 1) + "/" + mItemCount + ">\n" + NWInfoParser.GetInfo(mItem[mCurrentItem]));
        
	}

	@Override
	public void onStop(){
		super.onStop();
		phone.unregisterForNetworkInfo(mResponseHander);
		
		flag = flag & 0xF7;
		Log.i(TAG, "The close flag is :" + flag);
		String ATCloseCmd[] = new String[2];
		ATCloseCmd[0] = "AT+EINFO=" + flag;
		ATCloseCmd[1] = "";
        phone.invokeOemRilRequestStringsGemini(ATCloseCmd, mATCmdHander.obtainMessage(EVENT_NW_INFO_CLOSE_AT),Phone.GEMINI_SIM_2);
	}
    
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(arg0.getId() == mPageUp.getId())
		{
			mCurrentItem = (mCurrentItem - 1 + mItemCount) % mItemCount;
			NWInfoParser = new Sim2Info_URCParser(this);
			mInfo.setText("<" + (mCurrentItem + 1) + "/" + mItemCount + ">\n" + NWInfoParser.GetInfo(mItem[mCurrentItem]));
		}
		if(arg0.getId() == mPageDown.getId())
		{
			mCurrentItem = (mCurrentItem + 1) % mItemCount;
			NWInfoParser = new Sim2Info_URCParser(this);
			mInfo.setText("<" + (mCurrentItem + 1) + "/" + mItemCount + ">\n" + NWInfoParser.GetInfo(mItem[mCurrentItem]));
		}
	}
	
	private Handler mATCmdHander = new Handler() {
		public void handleMessage(Message msg){
			AsyncResult ar;
			switch(msg.what){
				case EVENT_NW_INFO_AT:
					ar= (AsyncResult) msg.obj;
	                if (ar.exception == null) {
	                	String data[] = (String [])ar.result;
	                	Log.i(TAG, "data[0] is : " + data[0]);
	                	Log.i(TAG, "flag is : " + data[0].substring(8));
                    try {
	                	flag = Integer.valueOf(data[0].substring(8));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(Sim2_Info.this, "NW AT cmd failed.", Toast.LENGTH_LONG);
                        return;
                    }
	                	flag = flag | 0x08;
	                	Log.i(TAG, "flag change is : " + flag);
	                	for(int j = 0; j < mItemCount; j ++)
	                    {
	                    	String ATCmd[] = new String[2];
	                    	ATCmd[0] = "AT+EINFO=" + flag + "," + mItem[j] + ",0";
	                    	ATCmd[1] = "+EINFO";
	                    	phone.invokeOemRilRequestStringsGemini(ATCmd, mATCmdHander.obtainMessage(EVENT_NW_INFO_INFO_AT), Phone.GEMINI_SIM_2);
	                    	Log.d(TAG, "ATCmd[0]="+ATCmd[0]);	
	                    }
	                	
	                } else {
	                	Toast.makeText(Sim2_Info.this, "NW AT cmd failed.", Toast.LENGTH_LONG);
	                }
					break;
				case EVENT_NW_INFO_INFO_AT:
					ar= (AsyncResult) msg.obj;
	                if (ar.exception == null) {
	                	
	                } else {
	                	Toast.makeText(Sim2_Info.this, "NW INFO AT cmd failed.", Toast.LENGTH_LONG);
	                }
					break;
				case EVENT_NW_INFO_CLOSE_AT:
					ar= (AsyncResult) msg.obj;
	                if (ar.exception == null) {
	                	
	                } else {
	                	Toast.makeText(Sim2_Info.this, "NW INFO Close AT cmd failed.", Toast.LENGTH_LONG);
	                }
					break;
			}
		}
	};
	
	private Handler mResponseHander = new Handler() {
		public void handleMessage(Message msg){
			AsyncResult ar;
			switch(msg.what){
				case EVENT_NW_INFO:
					ar= (AsyncResult) msg.obj;
					String data[] = (String[])ar.result;
					
					byte[] FileData = new byte[TOTAL_BUF_SIZE];
					try {
						//due to use of file data storage, must follow the following way
						FileInputStream inputStream = Sim2_Info.this.openFileInput(NWInfoFileName);
						int mTotalDataSize = inputStream.read(FileData, 0, TOTAL_BUF_SIZE);
						inputStream.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String FileString = new String(FileData);
					
					Log.d(TAG, "Ret Type: "+data[0]);
					Log.d(TAG, "Ret Data: "+data[1]);
					switch(Integer.valueOf(data[0])){
						case 0:
							FileString = data[1] + FileString.substring(mCellSelSize, TOTAL_BUF_SIZE);							
							break;
						case 1:
							FileString = FileString.substring(0, mCellSelSize) 
										+ data[1] + FileString.substring(mChDscrSize, TOTAL_BUF_SIZE);
							break;
						case 2:
							FileString = FileString.substring(0, mChDscrSize) 
										+ data[1] + FileString.substring(mCtrlchanSize, TOTAL_BUF_SIZE);
							break;
						case 3:
							FileString = FileString.substring(0, mCtrlchanSize) 
										+ data[1] + FileString.substring(mRACHCtrlSize, TOTAL_BUF_SIZE);
							break;
						case 4:
							FileString = FileString.substring(0, mRACHCtrlSize) 
										+ data[1] + FileString.substring(mLAIInfoSize, TOTAL_BUF_SIZE);
							break;
						case 5:
							FileString = FileString.substring(0, mLAIInfoSize) 
										+ data[1] + FileString.substring(mRadioLinkSize, TOTAL_BUF_SIZE);
							break;
						case 6:
							FileString = FileString.substring(0, mRadioLinkSize) 
										+ data[1] + FileString.substring(mMeasRepSize, TOTAL_BUF_SIZE);
							break;
						case 7:
							FileString = FileString.substring(0, mMeasRepSize) 
										+ data[1] + FileString.substring(mCaListSize, TOTAL_BUF_SIZE);
							break;
						case 8:
							FileString = FileString.substring(0, mCaListSize) 
										+ data[1] + FileString.substring(mControlMsgSize, TOTAL_BUF_SIZE);
							break;
						case 9:
							FileString = FileString.substring(0, mControlMsgSize) 
										+ data[1] + FileString.substring(mSI2QInfoSize, TOTAL_BUF_SIZE);
							break;
						case 10:
							FileString = FileString.substring(0, mSI2QInfoSize) 
										+ data[1] + FileString.substring(mMIInfoSize, TOTAL_BUF_SIZE);
							break;
						case 11:
							FileString = FileString.substring(0, mMIInfoSize) 
										+ data[1] + FileString.substring(mBLKInfoSize, TOTAL_BUF_SIZE);
							break;
						case 12:
							FileString = FileString.substring(0, mBLKInfoSize) 
										+ data[1] + FileString.substring(mTBFInfoSize, TOTAL_BUF_SIZE);
							break;
						case 13:
							FileString = FileString.substring(0, mTBFInfoSize) + data[1]
							            + FileString.substring(mGPRSGenSize, TOTAL_BUF_SIZE);
							break;
						case 21://Get3GMmEmInfo
							FileString = FileString.substring(0, mGPRSGenSize) + data[1]
							       + FileString.substring(m3GMmEmInfoSize, TOTAL_BUF_SIZE);
							break;
						case 27: //Get3GTcmMmiEmInfo
							FileString = FileString.substring(0, m3GMmEmInfoSize)
							+ data[1] + FileString.substring(m3GTcmMmiEmInfoSize, TOTAL_BUF_SIZE);
							break;
						case 47://Get3GCsceEMServCellSStatusInd
							Log.d(TAG, "data[1].length()="+data[1].length());							
							Log.d(TAG, "start offset "+m3GTcmMmiEmInfoSize);
							Log.d(TAG, "end offset "+m3GCsceEMServCellSStatusIndSize);
							FileString = FileString.substring(0, m3GTcmMmiEmInfoSize)
							+ data[1] + FileString.substring(m3GCsceEMServCellSStatusIndSize, TOTAL_BUF_SIZE);
							break;							
						case 52://Get3GCsceEmInfoMultiPlmn
							FileString = FileString.substring(0, m3GCsceEMServCellSStatusIndSize)
							+ data[1] + FileString.substring(m3GCsceEmInfoMultiPlmnSize, TOTAL_BUF_SIZE);
							break;
						case 61://Get3GMemeEmPeriodicBlerReportInd
							FileString = FileString.substring(0, m3GMemeEmInfoUmtsCellStatusSize)
							+ data[1] + FileString.substring(m3GMemeEmPeriodicBlerReportIndSize, TOTAL_BUF_SIZE);
							break;
						case 63://Get3GUrrUmtsSrncId
							FileString = FileString.substring(0, m3GMemeEmPeriodicBlerReportIndSize)
							+ data[1] + FileString.substring(m3GUrrUmtsSrncIdSize, TOTAL_BUF_SIZE);						
							break;						
						case 48://GetxGCsceEMNeighCellSStatusIndStructSize
							Log.d(TAG, "data[1].length()="+data[1].length());
							Log.d(TAG, "2G size should be "+456*2);
							Log.d(TAG, "start offset "+mGPRSGenSize);
							Log.d(TAG, "end offset "+mxGCsceEMNeighCellSStatusIndStructSize);
							if(data[1].length() >= 456*2)//2G size > 3G size, padding size.
							{
								FileString = FileString.substring(0, mGPRSGenSize)
								+ data[1] + FileString.substring(mxGCsceEMNeighCellSStatusIndStructSize, TOTAL_BUF_SIZE);
							}
							else
							{								
								char[] padding = new char[456*2 - data[1].length()];
								
								FileString = FileString.substring(0, mGPRSGenSize)
								+ data[1] +new String(padding)+ FileString.substring(mxGCsceEMNeighCellSStatusIndStructSize, TOTAL_BUF_SIZE);
							}
							
							break;
					}
					
					int modemType = SIM2.GetModemType();
					if(modemType == SIM2.MODEM_FDD)
					{
						switch(Integer.valueOf(data[0]))
						{						
						
						case 53://Get3GMemeEmInfoUmtsCellStatus
							FileString = FileString.substring(0, m3GCsceEmInfoMultiPlmnSize)
							+ data[1] + FileString.substring(m3GMemeEmInfoUmtsCellStatusSize, TOTAL_BUF_SIZE);
							break;
						
						case 64://Get3GSlceEmPsDataRateStatusInd
							FileString = FileString.substring(0, m3GUrrUmtsSrncIdSize)
							+ data[1] + FileString.substring(m3GSlceEmPsDataRateStatusIndSize, TOTAL_BUF_SIZE);
							break;					
						
						}
					}
					else if(modemType == SIM2.MODEM_TD)
					{
						switch(Integer.valueOf(data[0]))
						{						
						case 64://Get3GHandoverSequenceIndStuct
							FileString = FileString.substring(0, mGPRSGenSize) + data[1]
							       + FileString.substring(m3GHandoverSequenceIndStuctSize, TOTAL_BUF_SIZE);
							break;
						case 67: //Get3GUl2EmAdmPoolStatusIndStructSize
							FileString = FileString.substring(0, m3GHandoverSequenceIndStuctSize)
							+ data[1] + FileString.substring(m3GUl2EmAdmPoolStatusIndStructSize, TOTAL_BUF_SIZE);
							break;
						case 68://Get3GUl2EmPsDataRateStatusIndStruct
							FileString = FileString.substring(0, m3GUl2EmAdmPoolStatusIndStructSize)
							+ data[1] + FileString.substring(m3GUl2EmPsDataRateStatusIndStructSize, TOTAL_BUF_SIZE);
							break;
						case 69://Get3Gul2EmHsdschReconfigStatusIndStruct
							FileString = FileString.substring(0, m3GUl2EmPsDataRateStatusIndStructSize)
							+ data[1] + FileString.substring(m3Gul2EmHsdschReconfigStatusIndStructSize, TOTAL_BUF_SIZE);
							break;
						case 70://Get3GUl2EmUrlcEventStatusIndStruct
							FileString = FileString.substring(0, m3Gul2EmHsdschReconfigStatusIndStructSize)
							+ data[1] + FileString.substring(m3GUl2EmUrlcEventStatusIndStructSize, TOTAL_BUF_SIZE);
							break;						
						case 71://Get3GUl2EmPeriodicBlerReportInd
							FileString = FileString.substring(0, m3GUl2EmUrlcEventStatusIndStructSize)
							+ data[1];
							break;
						
						}
					}
					
					try {
						//due to use of file data storage, must follow the following way
						FileOutputStream outputStream = Sim2_Info.this.openFileOutput(NWInfoFileName, MODE_PRIVATE);
						byte[] modifyData = FileString.getBytes();
						outputStream.write(modifyData);
						outputStream.close();
					}catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//if the data update is the current page, then update the display content at once
					if(mCurrentItem == Integer.valueOf(data[0]))
					{
						NWInfoParser = new Sim2Info_URCParser(Sim2_Info.this);
						mInfo.setText("<" + (mCurrentItem + 1) + "/" + mItemCount + ">\n" + NWInfoParser.GetInfo(mItem[mCurrentItem]));
					}
					break;
			}
			
		}
	};
	

}
