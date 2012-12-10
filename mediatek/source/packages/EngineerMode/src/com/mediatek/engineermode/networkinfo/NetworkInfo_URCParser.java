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

package com.mediatek.engineermode.networkinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import com.mediatek.xlog.Xlog;

public class NetworkInfo_URCParser {
	
	private final String TAG = "NetworkInfo";
	private final int TOTAL_BUF_SIZE = 2204+2192+364+456*2;
	private int mTotalDataSize;
	private final String NWInfoFileName = "NetworkInfo.urc";
	private String dataString;
	
	private static final Boolean mAlignmentEnable = true;
	private static final Boolean mEGPRSModeEnable = true;
	private static final Boolean mAMRSupprotEnable = true;
	private static final Boolean mFwpNcLaiInfoEnable = false;
	
	private  int mCellSelSize = 6;
	private int mChDscrSize = 308;
	private  int mCtrlchanSize = 14;
	private  int mRACHCtrlSize = 14;
	private int mLAIInfoSize = 28;
	private  int mRadioLinkSize = 16;
	private int mMeasRepSize = 1368;
	private  int mCaListSize = 260;
	private  int mControlMsgSize = 4;
	private  int mSI2QInfoSize = 10;
	private  int mMIInfoSize = 8;
	private int mBLKInfoSize = 80;
	private int mTBFInfoSize = 56;
	private  int mGPRSGenSize = 32;
	
	//LXO, stupid code..
	private  int m3GMmEmInfoSize = 26*2;
	private  int m3GTcmMmiEmInfoSize = 7*2;
	private  int m3GCsceEMServCellSStatusIndSize = 44*2;	
	private  int m3GCsceEmInfoMultiPlmnSize = 37*2;
	private  int m3GMemeEmInfoUmtsCellStatusSize = 503*2;
	private  int m3GMemeEmPeriodicBlerReportIndSize = 100*2;
	private  int m3GUrrUmtsSrncIdSize = 2*2;
	private  int m3GSlceEmPsDataRateStatusIndSize = 100*2;
	private  int m3GMemeEmInfoHServCellIndSize = 8*2;
	
	private int m3GHandoverSequenceIndStuctSize = 16*2;  //alignment enabled
	private int m3GUl2EmAdmPoolStatusIndStructSize = 32*2;
	private int m3GUl2EmPsDataRateStatusIndStructSize = 8*2;
	private int m3Gul2EmHsdschReconfigStatusIndStructSize = 8*2;
	private int m3GUl2EmUrlcEventStatusIndStructSize = 18*2;
	private int m3GUl2EmPeriodicBlerReportIndSize = 100*2;

	private int mxGCsceEMNeighCellSStatusIndStructSize = 456*2;
	
	public NetworkInfo_URCParser(Context context)
	{
		byte[] data = new byte[TOTAL_BUF_SIZE];
		for(int i = 0; i < TOTAL_BUF_SIZE; i ++)
		{
			data[i] = 0;
		}
		try {
			//due to use of file data storage, must follow the following way, so we should have a Context parameter
			FileInputStream inputStream = context.openFileInput(NWInfoFileName);
			mTotalDataSize = inputStream.read(data, 0, TOTAL_BUF_SIZE);
			inputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Xlog.v(TAG, "FileNotFoundException");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Xlog.v(TAG, "IOException");
		}
		dataString = new String(data);
		Xlog.v(TAG, dataString);
		
		if(false == mAlignmentEnable)
		{
			mChDscrSize -= 4;
			mLAIInfoSize -= 2;
			mMeasRepSize -= 14;
			mBLKInfoSize -= 4;
			mTBFInfoSize -= 4;
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
		
		int modemType = NetworkInfo.GetModemType();
		if (modemType == NetworkInfo.MODEM_TD) {
			m3GCsceEMServCellSStatusIndSize -= 4 * 2;
		}	
		
	}
	
	private String GetValueFromByte(String data, int start, boolean signed)
	{
		try
		{
			String sub = data.substring(start, start + 2);		
			//String value = Integer.valueOf(sub,16).toString();
			String value = null;
			if (signed) {
				short s = Short.valueOf(sub, 16);
				Byte b = (byte) s;
				value = b.toString();
			} else {
				value = Short.valueOf(sub,16).toString();
			}
			return value;
		}
		catch(NumberFormatException e)
		{
			return "Error.";
		}	
	}
	
	private String GetValueFrom2Byte(String data, int start, boolean signed)
	{
		try
		{
			String low = data.substring(start, start + 2);
			String high = data.substring(start + 2, start + 4);
			String reverse = high + low;		
			//String value = Integer.valueOf(reverse,16).toString();
			String value = null;
			if (signed) {
				int i = Integer.valueOf(reverse, 16);
				Short s = (short) i;
				value = s.toString();
			} else {
				value = Integer.valueOf(reverse,16).toString();
			}
			return value;
		}
		catch(NumberFormatException e)
		{
			return "Error.";
		}	
	}
		
	private String GetValueFrom4Byte(String data, int start, boolean signed)
	{
		try
		{
			String byte1 = data.substring(start, start + 2);
			String byte2 = data.substring(start + 2, start + 4);
			String byte3 = data.substring(start + 4, start + 6);
			String byte4 = data.substring(start + 6, start + 8);
			String reverse = byte4 + byte3 + byte2 + byte1;
			//String value = Long.valueOf(reverse,16).toString();
			String value = null;
			if (signed) {
				long lg = Long.valueOf(reverse, 16);
				Integer i = (int) lg;
				value = i.toString();
			} else {
				value = Long.valueOf(reverse,16).toString();
			}
			return value;
		}
		catch(NumberFormatException e)
		{
			return "Error.";
		}	
	}	
	
	private String OneElementByte(String label, String data, int start, boolean signed)
	{
		String element = "";
		element += label;		
		element += GetValueFromByte(data, start, signed);
		element += "\n";
		return element;
	}
	
	private String OneElement2Byte(String label, String data, int start, boolean signed)
	{
		String element = "";
		element += label;
		element += GetValueFrom2Byte(data, start, signed);
		element += "\n";
		return element;
	}
	
	private String OneElement4Byte(String label, String data, int start, boolean signed)
	{
		String element = "";
		element += label;
		element += GetValueFrom4Byte(data, start, signed);
		element += "\n";
		return element;
	}
	
	private String OneBlockFromByte(String data, int start, int dataLength, boolean signed)
	{
		String block = "";
		for(int i = 0; i < dataLength; i ++)
		{
			if(0 == i % 7)
			{
				block += "\n";
			}
			block += GetValueFromByte(data, start, signed);
			start += 2;
			if(i != dataLength - 1)
			{
				block += ", ";
			}
		}
		block += "\n";
		return block;
	}
	
	private String OneBlockFrom2Byte(String data, int start, int dataLength, boolean signed)
	{
		String block = "";
		for(int i = 0; i < dataLength; i ++)
		{
			if(0 == i % 7)
			{
				block += "\n";
			}
			block += GetValueFrom2Byte(data, start, signed);
			start += 4;
			if(i != dataLength - 1)
			{
				block += ", ";
			}
		}
		block += "\n";
		return block;
	}
	
	private String OneBlockFrom4Byte(String data, int start, int dataLength, boolean signed)
	{
		String block = "";
		for(int i = 0; i < dataLength; i ++)
		{
			if(0 == i % 7)
			{
				block += "\n";
			}
			block += GetValueFrom4Byte(data, start, signed);
			start += 8;
			if(i != dataLength - 1)
			{
				block += ", ";
			}
		}
		block += "\n";
		return block;
	}
	
	public String GetInfo(int type)
	{
		Xlog.v(TAG, "Get Info type: "+ type);
		switch(type){
			case 0:
				return GetCellSelInfo();
			case 1:
				return GetChDscrInfo();
			case 2:
				return GetCtrlchanInfo();
			case 3:
				return GetRACHCtrlInfo();
			case 4:
				return GetLAIInfo();
			case 5:
				return GetRadioLinkInfo();
			case 6:
				return GetMeasRepInfo();
			case 7:
				return GetCaListInfo();
			case 8:
				return GetControlMsgInfo();
			case 9:
				return GetSI2QInfo();
			case 10:
				return GetMIInfo();
			case 11:
				return GetBLKInfo();
			case 12:
				return GetTBFInfo();
			case 13:
				return GetGPRSGenInfo();
			case 21:
				return Get3GMmEmInfo();
			case 27:
				return Get3GTcmMmiEmInfo();			
			case 52:
				return Get3GCsceEmInfoMultiPlmn();	
			case 61:
				return Get3GMemeEmPeriodicBlerReportInd();
			case 63:
				return Get3GUrrUmtsSrncId();	
			case 65:
				return Get3GMemeEmInfoHServCellInd();
			case 48:
				return GetxGCsceEMNeighCellSStatusIndStructSize();
			
		}
		
		int modemType = NetworkInfo.GetModemType();
		if (modemType == NetworkInfo.MODEM_FDD) {
			switch (type) 
			{	
			case 47:
				return Get3GCsceEMServCellSStatusIndFDD();
			case 53:
				return Get3GMemeEmInfoUmtsCellStatus();			
			case 64:
				return Get3GSlceEmPsDataRateStatusInd();			
			}
		}
		else if(modemType == NetworkInfo.MODEM_TD) {
			switch (type) 
			{
			case 47:
				return Get3GCsceEMServCellSStatusIndTDD();
			case 64:
				return Get3GHandoverSequenceIndStuct();
			case 67:
				return Get3GUl2EmAdmPoolStatusIndStruct();
			case 68:
				return Get3GUl2EmPsDataRateStatusIndStruct();
			case 69:
				return Get3Gul2EmHsdschReconfigStatusIndStruct();
			case 70:
				return Get3GUl2EmUrlcEventStatusIndStruct();
			case 71:
				return Get3GUl2EmPeriodicBlerReportInd();			
			}
		}
		
		return null;		
		
	}
	
	
	public String GetCellSelInfo()
	{
		int start = 0;
		int end = mCellSelSize;
		String CellSelData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------CellSelData is:\n");
		Xlog.v(TAG, CellSelData + "\n");
		Xlog.v(TAG, "NetworkInfo ------CellSelInfo---------------------");
		
		int index = 0;
		String CellInfoString = "[RR Cell Sel]\n";
		CellInfoString += OneElementByte("crh: ", CellSelData, index, false);index += 2;
		CellInfoString += OneElementByte("ms_txpwr: ", CellSelData, index, false);index += 2;
		CellInfoString += OneElementByte("rxlev_access_min: ", CellSelData, index, false);		
		return CellInfoString;
	}
	
	public String GetChDscrInfo()
	{
		int start = mCellSelSize;
		int end = start + mChDscrSize;
		String ChDscrData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------ChDscrData is:\n");
		Xlog.v(TAG, ChDscrData + "\n");
		Xlog.v(TAG, "NetworkInfo ------ChDscrInfo---------------------");
		
		int index = 0;
		String ChDscrString = "[RR Ch Dscr]\n";
		ChDscrString += OneElementByte("channel_type: ", ChDscrData, index, false);index += 2;
		ChDscrString += OneElementByte("tn: ", ChDscrData, index, false);index += 2;
		ChDscrString += OneElementByte("tsc: ", ChDscrData, index, false);index += 2;
		ChDscrString += OneElementByte("hopping_flag: ", ChDscrData, index, false);index += 2;
		ChDscrString += OneElementByte("maio: ", ChDscrData, index, false);index += 2;
		ChDscrString += OneElementByte("hsn: ", ChDscrData, index, false);index += 2;
		ChDscrString += OneElementByte("num_of_carriers: ", ChDscrData, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		ChDscrString += "arfcn:";
		final int arfcnSize = 64;
		ChDscrString += OneBlockFrom2Byte(ChDscrData, index, arfcnSize, false);index += 256;			
		ChDscrString += OneElementByte("is_BCCH_arfcn_valid: ", ChDscrData, index, true);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		ChDscrString += OneElement2Byte("BCCH_arfcn: ", ChDscrData, index, false);index += 4;
		ChDscrString += OneElementByte("cipher_algo: ", ChDscrData, index, false);index += 2;
		if(true == mAMRSupprotEnable)
		{
			ChDscrString += OneElementByte("amr_valid: ", ChDscrData, index, true);index += 2;
			ChDscrString += OneElementByte("mr_ver: ", ChDscrData, index, false);index += 2;
			ChDscrString += OneElementByte("nscb: ", ChDscrData, index, true);index += 2;
			ChDscrString += OneElementByte("icmi: ", ChDscrData, index, true);index += 2;
			ChDscrString += OneElementByte("start_codec_mode: ", ChDscrData, index, false);index += 2;
			ChDscrString += OneElementByte("acs: ", ChDscrData, index, false);index += 2;
			ChDscrString += "threshold:";
			final int thresholdSize = 3;
			ChDscrString += OneBlockFromByte(ChDscrData, index, thresholdSize, false);index += 6;
			ChDscrString += "hysteresis:";
			final int hysteresisSize = 3;
			ChDscrString += OneBlockFromByte(ChDscrData, index, hysteresisSize, false);
		}
		return ChDscrString;
	}
	
	public String GetCtrlchanInfo()
	{
		int start = mCellSelSize + mChDscrSize;
		int end = start + mCtrlchanSize;
		String CtrlchanData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------CtrlchanData is:\n");
		Xlog.v(TAG, CtrlchanData + "\n");
		Xlog.v(TAG, "NetworkInfo ------CtrlchanInfo---------------------");
		
		int index = 0;
		String CtrlchanString = "[RR Ctrl chan]\n";
		CtrlchanString += OneElementByte("mscr: ", CtrlchanData, index, false);index += 2;
		CtrlchanString += OneElementByte("att: ", CtrlchanData, index, false);index += 2;
		CtrlchanString += OneElementByte("bs_ag_blks_res: ", CtrlchanData, index, false);index += 2;
		CtrlchanString += OneElementByte("ccch_conf: ", CtrlchanData, index, false);index += 2;
		CtrlchanString += OneElementByte("cbq2: ", CtrlchanData, index, false);index += 2;
		CtrlchanString += OneElementByte("bs_pa_mfrms: ", CtrlchanData, index, false);index += 2;
		CtrlchanString += OneElementByte("t3212: ", CtrlchanData, index, false);
		return CtrlchanString;
	}
	
	public String GetRACHCtrlInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize;
		int end = start + mRACHCtrlSize;
		String RACHCtrlData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------RACHCtrlData is:\n");
		Xlog.v(TAG, RACHCtrlData + "\n");
		Xlog.v(TAG, "NetworkInfo ------RACHCtrlInfo---------------------");
		
		int index = 0;
		String RACHCtrlString = "[RR RACH Ctrl]\n";
		RACHCtrlString += OneElementByte("max_retrans: ", RACHCtrlData, index, false);index += 2;
		RACHCtrlString += OneElementByte("tx_integer: ", RACHCtrlData, index, false);index += 2;
		RACHCtrlString += OneElementByte("cba: ", RACHCtrlData, index, false);index += 2;
		RACHCtrlString += OneElementByte("re: ", RACHCtrlData, index, false);index += 2;
		RACHCtrlString += "acc_class:";
		final int accClassSize = 2;
		RACHCtrlString += OneBlockFromByte(RACHCtrlData, index, accClassSize, false);index += 4;
		RACHCtrlString += OneElementByte("CB_supported: ", RACHCtrlData, index, true);
		return RACHCtrlString;
	}
	
	public String GetLAIInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize;
		int end = start + mLAIInfoSize;
		String LAIInfoData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------LAIInfoData is:\n");
		Xlog.v(TAG, LAIInfoData + "\n");
		Xlog.v(TAG, "NetworkInfo ------LAIInfo---------------------");
		
		int index = 0;
		String LAIInfoString = "[RR LAI Info]\n";
		LAIInfoString += "mcc:";
		final int mccSize = 3;
		LAIInfoString += OneBlockFromByte(LAIInfoData, index, mccSize, false);index += 6;
		LAIInfoString += "mnc:";
		final int mncSize = 3;
		LAIInfoString += OneBlockFromByte(LAIInfoData, index, mncSize, false);index += 6;
		LAIInfoString += "lac:";
		final int lacSize = 2;
		LAIInfoString += OneBlockFromByte(LAIInfoData, index, lacSize, false);index += 4;
		LAIInfoString += OneElement2Byte("cell_id: ", LAIInfoData, index, false);index += 4;
		LAIInfoString += OneElementByte("nc_info_index: ", LAIInfoData, index, false);index += 2;
		LAIInfoString += OneElementByte("nmo: ", LAIInfoData, index, false);index += 2;
		LAIInfoString += OneElementByte("supported_Band: ", LAIInfoData, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		return LAIInfoString;
	}
	
	public String GetRadioLinkInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize;
		int end = start + mRadioLinkSize;
		String RadioLinkData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------RadioLinkData is:\n");
		Xlog.v(TAG, RadioLinkData + "\n");
		Xlog.v(TAG, "NetworkInfo ------RadioLinkInfo---------------------");
		
		int index = 0;
		String RadioLinkString = "[RR Radio Link]\n";
		RadioLinkString += OneElement2Byte("max_value: ", RadioLinkData, index, false);index += 4;
		RadioLinkString += OneElement2Byte("current_value: ", RadioLinkData, index, true);index += 4;
		RadioLinkString += OneElementByte("dtx_ind: ", RadioLinkData, index, false);index += 2;
		RadioLinkString += OneElementByte("dtx_used: ", RadioLinkData, index, false);index += 2;
		RadioLinkString += OneElementByte("is_dsf: ", RadioLinkData, index, true);
		return RadioLinkString;
	}
	
	public String GetMeasRepInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize
					+ mRadioLinkSize;
		int end = start + mMeasRepSize;
		String MeasRepData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------MeasRepData is:\n");
		Xlog.v(TAG, MeasRepData + "\n");
		Xlog.v(TAG, "NetworkInfo ------MeasRepInfo---------------------");
		
		int index = 0;
		String MeasRepString = "[RR Meas Rep]\n";
		MeasRepString += OneElementByte("rr_state: ", MeasRepData, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		MeasRepString += OneElement2Byte("serving_arfcn: ", MeasRepData, index, false);index += 4;
		MeasRepString += OneElementByte("serving_bsic: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElementByte("serving_current_band: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElementByte("serv_gprs_supported: ", MeasRepData, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		MeasRepString += OneElement2Byte("serv_rla_in_quarter_dbm: ", MeasRepData, index, true);index += 4;
		MeasRepString += OneElementByte("is_serv_BCCH_rla_valid: ", MeasRepData, index, true);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		MeasRepString += OneElement2Byte("serv_BCCH_rla_in_dedi_state: ", MeasRepData, index, true);index += 4;
		MeasRepString += OneElementByte("quality: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElementByte("gprs_pbcch_present: ", MeasRepData, index, true);index += 2;
		MeasRepString += OneElementByte("gprs_c31_c32_enable: ", MeasRepData, index, true);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		MeasRepString += "c31:";
		final int c31Size = 32;
		MeasRepString += OneBlockFrom2Byte(MeasRepData, index, c31Size, true);index += 128;
		MeasRepString += OneElement2Byte("c1_serv_cell: ", MeasRepData, index, true);index += 4;
		MeasRepString += OneElement2Byte("c2_serv_cell: ", MeasRepData, index, true);index += 4;
		MeasRepString += OneElement2Byte("c31_serv_cell: ", MeasRepData, index, true);index += 4;
		MeasRepString += OneElementByte("num_of_carriers: ", MeasRepData, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		MeasRepString += "nc_arfcn:";
		final int nc_arfcnSize = 32;
		MeasRepString += OneBlockFrom2Byte(MeasRepData, index, nc_arfcnSize, false);index += 128;
		MeasRepString += "rla_in_quarter_dbm:";
		final int rla_in_quarter_dbmSize = 32;
		MeasRepString += OneBlockFrom2Byte(MeasRepData, index, rla_in_quarter_dbmSize, true);index += 128;
		MeasRepString += "nc_info_status:";
		final int nc_info_statusSize = 32;
		MeasRepString += OneBlockFromByte(MeasRepData, index, nc_info_statusSize, false);index += 64;
		MeasRepString += "nc_bsic:";
		final int nc_bsicSize = 32;
		MeasRepString += OneBlockFromByte(MeasRepData, index, nc_bsicSize, false);index += 64;
		if(true == mAlignmentEnable)
		{
			index += 4;//alignment
		}
		MeasRepString += "frame_offset:";
		final int frame_offsetSize = 32;
		MeasRepString += OneBlockFrom4Byte(MeasRepData, index, frame_offsetSize, true);index += 256;
		MeasRepString += "ebit_offset:";
		final int ebit_offsetSize = 32;
		MeasRepString += OneBlockFrom4Byte(MeasRepData, index, ebit_offsetSize, true);index += 256;
		MeasRepString += "c1:";
		final int c1Size = 32;
		MeasRepString += OneBlockFrom2Byte(MeasRepData, index, c1Size, true);index += 128;
		MeasRepString += "c2:";
		final int c2Size = 32;
		MeasRepString += OneBlockFrom2Byte(MeasRepData, index, c2Size, true);index += 128;
		MeasRepString += OneElementByte("multiband_report: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElementByte("timing_advance: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElement2Byte("tx_power_level: ", MeasRepData, index, true);index += 4;
		MeasRepString += OneElement2Byte("serv_rla_full_value_in_quater_dbm: ", MeasRepData, index, true);index += 4;
		MeasRepString += OneElementByte("nco: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElementByte("rxqual_sub: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElementByte("rxqual_full: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElementByte("amr_info_valid: ", MeasRepData, index, true);index += 2;
		MeasRepString += OneElementByte("cmr_cmc_cmiu_cmid: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElementByte("c_i: ", MeasRepData, index, false);index += 2;
		MeasRepString += OneElement2Byte("icm: ", MeasRepData, index, false);index += 4;
		MeasRepString += OneElement2Byte("acs: ", MeasRepData, index, false);index += 4;
		if(true == mFwpNcLaiInfoEnable)
		{
			MeasRepString += OneElementByte("num_of_nc_lai: ", MeasRepData, index, false);index += 2;
			MeasRepString += "nc_lai:\n";
			for(int i = 0; i < 6; i ++)
			{
				MeasRepString += "nc_lai[" + i + "]:\n";
				String LAIInfoString = "";
				LAIInfoString += "mcc:";
				final int mccSize = 3;
				LAIInfoString += OneBlockFromByte(MeasRepData, index, mccSize, false);index += 6;
				LAIInfoString += "mnc:";
				final int mncSize = 3;
				LAIInfoString += OneBlockFromByte(MeasRepData, index, mncSize, false);index += 6;
				LAIInfoString += "lac:";
				final int lacSize = 2;
				LAIInfoString += OneBlockFromByte(MeasRepData, index, lacSize, false);index += 4;
				LAIInfoString += OneElement2Byte("cell_id: ", MeasRepData, index, false);index += 4;
				LAIInfoString += OneElementByte("nc_info_index: ", MeasRepData, index, false);index += 2;
				MeasRepString += LAIInfoString;
			}
		}
		return MeasRepString;
	}
	
	public String GetCaListInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize
					+ mRadioLinkSize + mMeasRepSize;
		int end = start + mCaListSize;
		String CaListData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------CaListData is:\n");
		Xlog.v(TAG, CaListData + "\n");
		Xlog.v(TAG, "NetworkInfo ------CaListInfo---------------------");
		
		int index = 0;
		String CaListString = "[RR Ca List]\n";
		CaListString += OneElementByte("valid: ", CaListData, index, false);index += 2;
		CaListString += OneElementByte("number_of_channels: ", CaListData, index, false);index += 2;
		CaListString += "arfcn_list:";
		final int arfcn_listSize = 64;
		CaListString += OneBlockFrom2Byte(CaListData, index, arfcn_listSize, false);
		return CaListString;
	}
	
	public String GetControlMsgInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize
					+ mRadioLinkSize + mMeasRepSize + mCaListSize;
		int end = start + mControlMsgSize;
		String ControlMsgData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------ControlMsgData is:\n");
		Xlog.v(TAG, ControlMsgData + "\n");
		Xlog.v(TAG, "NetworkInfo ------ControlMsgInfo---------------------");
		
		int index = 0;
		String ControlMsgString = "[RR Control Msg]\n";
		ControlMsgString += OneElementByte("msg_type: ", ControlMsgData, index, false);index += 2;
		ControlMsgString += OneElementByte("rr_cause: ", ControlMsgData, index, false);
		return ControlMsgString;
	}
	
	public String GetSI2QInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize
					+ mRadioLinkSize + mMeasRepSize + mCaListSize + mControlMsgSize;
		int end = start + mSI2QInfoSize;
		String SI2QInfoData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------SI2QInfoData is:\n");
		Xlog.v(TAG, SI2QInfoData + "\n");
		Xlog.v(TAG, "NetworkInfo ------SI2QInfo---------------------");
		
		int index = 0;
		String SI2QInfoString = "[RR SI2Q Info]\n";
		SI2QInfoString += OneElementByte("present: ", SI2QInfoData, index, true);index += 2;
		SI2QInfoString += OneElementByte("no_of_instance: ", SI2QInfoData, index, false);index += 2;
		SI2QInfoString += OneElementByte("emr_report: ", SI2QInfoData, index, true);index += 2;
		SI2QInfoString += OneElementByte("pemr_report: ", SI2QInfoData, index, true);index += 2;
		SI2QInfoString += OneElementByte("umts_parameter_exist: ", SI2QInfoData, index, true);
		return SI2QInfoString;
	}
	
	public String GetMIInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize
					+ mRadioLinkSize + mMeasRepSize + mCaListSize + mControlMsgSize + mSI2QInfoSize;
		int end = start + mMIInfoSize;
		String MIInfoData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------MIInfoData is:\n");
		Xlog.v(TAG, MIInfoData + "\n");
		Xlog.v(TAG, "NetworkInfo ------MIInfo---------------------");
		
		int index = 0;
		String MIInfoString = "[RR MI Info]\n";
		MIInfoString += OneElementByte("present: ", MIInfoData, index, true);index += 2;
		MIInfoString += OneElementByte("no_of_instance: ", MIInfoData, index, true);index += 2;
		MIInfoString += OneElementByte("emr_report: ", MIInfoData, index, true);index += 2;
		MIInfoString += OneElementByte("umts_parameter_exist: ", MIInfoData, index, true);
		return MIInfoString;
	}
	
	public String GetBLKInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize
					+ mRadioLinkSize + mMeasRepSize + mCaListSize + mControlMsgSize + mSI2QInfoSize
					+ mMIInfoSize;
		int end = start + mBLKInfoSize;
		String BLKInfoData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------BLKInfoData is:\n");
		Xlog.v(TAG, BLKInfoData + "\n");
		Xlog.v(TAG, "NetworkInfo ------BLKInfo---------------------");
		
		int index = 0;
		String BLKInfoString = "[RR BLK Info]\n";
		BLKInfoString += OneElementByte("ul_coding_scheme: ", BLKInfoData, index, false);index += 2;
		BLKInfoString += OneElementByte("ul_cv: ", BLKInfoData, index, false);index += 2;
		BLKInfoString += OneElementByte("ul_tlli: ", BLKInfoData, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		BLKInfoString += OneElement2Byte("ul_bsn1: ", BLKInfoData, index, false);index += 4;
		if(true == mEGPRSModeEnable)
		{
			BLKInfoString += OneElement2Byte("ul_bsn2: ", BLKInfoData, index, false);index += 4;
			BLKInfoString += OneElementByte("ul_cps: ", BLKInfoData, index, false);index += 2;
			BLKInfoString += OneElementByte("ul_rsb: ", BLKInfoData, index, false);index += 2;
			BLKInfoString += OneElementByte("ul_spb: ", BLKInfoData, index, false);index += 2;
		}
		BLKInfoString += OneElementByte("dl_c_value_in_rx_level: ", BLKInfoData, index, false);index += 2;
		BLKInfoString += OneElementByte("dl_rxqual: ", BLKInfoData, index, false);index += 2;
		BLKInfoString += OneElementByte("dl_sign_var: ", BLKInfoData, index, false);index += 2;
		BLKInfoString += OneElementByte("dl_coding_scheme: ", BLKInfoData, index, false);index += 2;
		BLKInfoString += OneElementByte("dl_fbi: ", BLKInfoData, index, false);index += 2;
		BLKInfoString += OneElement2Byte("dl_bsn1: ", BLKInfoData, index, false);index += 4;
		if(true == mEGPRSModeEnable)
		{
			BLKInfoString += OneElement2Byte("dl_bsn2: ", BLKInfoData, index, false);index += 4;
			BLKInfoString += OneElementByte("dl_cps: ", BLKInfoData, index, false);index += 2;
			BLKInfoString += OneElementByte("dl_gmsk_mean_bep_lev: ", BLKInfoData, index, false);index += 2;
			BLKInfoString += OneElementByte("dl_8psk_mean_bep_lev: ", BLKInfoData, index, false);index += 2;
			BLKInfoString += "dl_tn_mean_bep_lev:";
			final int dl_tn_mean_bep_levSize = 8;
			BLKInfoString += OneBlockFromByte(BLKInfoData, index, dl_tn_mean_bep_levSize, false);index += 16;
		}
		BLKInfoString += "dl_tn_interference_lev:";
		final int dl_tn_interference_levSize = 8;
		BLKInfoString += OneBlockFromByte(BLKInfoData, index, dl_tn_interference_levSize, false);
		return BLKInfoString;
	}
	
	public String GetTBFInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize
					+ mRadioLinkSize + mMeasRepSize + mCaListSize + mControlMsgSize + mSI2QInfoSize
					+ mMIInfoSize + mBLKInfoSize;
		int end = start + mTBFInfoSize;
		String TBFInfoData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------TBFInfoData is:\n");
		Xlog.v(TAG, TBFInfoData + "\n");
		Xlog.v(TAG, "NetworkInfo ------TBFInfo---------------------");
		
		int index = 0;
		String TBFInfoString = "[RR TBF Info]\n";
		TBFInfoString += OneElementByte("tbf_mode: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("ul_tbf_status: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("ul_rel_cause: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("ul_ts_allocation: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("ul_rlc_mode: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("ul_mac_mode: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElement2Byte("number_rlc_octect: ", TBFInfoData, index, false);index += 4;
		TBFInfoString += OneElementByte("ul_tfi: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("ul_granularity: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("ul_usf: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("ul_tai: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElement2Byte("ul_tqi: ", TBFInfoData, index, false);index += 4;
		TBFInfoString += OneElement2Byte("ul_window_size: ", TBFInfoData, index, false);index += 4;
		TBFInfoString += OneElementByte("dl_tbf_status: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("dl_rel_cause: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("dl_ts_allocation: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("dl_rlc_mode: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("dl_mac_mode: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("dl_tfi: ", TBFInfoData, index, false);index += 2;
		TBFInfoString += OneElementByte("dl_tai: ", TBFInfoData, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		TBFInfoString += OneElement2Byte("dl_window_size: ", TBFInfoData, index, false);index += 4;
		if(true == mEGPRSModeEnable)
		{
			TBFInfoString += OneElementByte("dl_out_of_memory: ", TBFInfoData, index, false);
		}
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment
		}
		return TBFInfoString;
	}
	
	public String GetGPRSGenInfo()
	{
		int start = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize + mLAIInfoSize
					+ mRadioLinkSize + mMeasRepSize + mCaListSize + mControlMsgSize + mSI2QInfoSize
					+ mMIInfoSize + mBLKInfoSize + mTBFInfoSize;
		int end = start + mGPRSGenSize;
		String GPRSGenData = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------GPRSGenData is:\n");
		Xlog.v(TAG, GPRSGenData + "\n");
		Xlog.v(TAG, "NetworkInfo ------GPRSGenInfo---------------------");
		
		int index = 0;
		String GPRSGenString = "[RR GPRS Gen]\n";
		GPRSGenString += OneElement4Byte("t3192: ", GPRSGenData, index, false);index += 8;
		GPRSGenString += OneElement4Byte("t3168: ", GPRSGenData, index, false);index += 8;
		GPRSGenString += OneElementByte("rp: ", GPRSGenData, index, false);index += 2;
		GPRSGenString += OneElementByte("gprs_support: ", GPRSGenData, index, false);index += 2;
		GPRSGenString += OneElementByte("egprs_support: ", GPRSGenData, index, false);index += 2;
		GPRSGenString += OneElementByte("sgsn_r: ", GPRSGenData, index, false);index += 2;
		GPRSGenString += OneElementByte("pfc_support: ", GPRSGenData, index, false);index += 2;
		GPRSGenString += OneElementByte("epcr_support: ", GPRSGenData, index, false);index += 2;
		GPRSGenString += OneElementByte("bep_period: ", GPRSGenData, index, false);index += 2;
		return GPRSGenString;
	}
	
	int calc2GSize()
	{
		int sz = mCellSelSize + mChDscrSize + mCtrlchanSize + mRACHCtrlSize 
		+ mLAIInfoSize + mRadioLinkSize + mMeasRepSize + mCaListSize
		+ mControlMsgSize + mSI2QInfoSize + mMIInfoSize + mBLKInfoSize
		+ mTBFInfoSize + mGPRSGenSize ;
		return sz;
	}
	//LXO=========================
	//type 21:
	public String Get3GMmEmInfo()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize;;
		
		int end = start + m3GMmEmInfoSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GMmEmInfo is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GMmEmInfo---------------------");
		
		int index = 0;
		String ss = "[RR 3G MM EM Info]\n";
		ss += OneElementByte("t3212: ", data, index, false);index += 2;
		ss += OneElementByte("ATT_flag: ", data, index, false);index += 2;
		ss += OneElementByte("MM_reject_cause: ", data, index, false);index += 2;		
		ss += OneElementByte("MM_state: ", data, index, false);index += 2;
		
		ss += "MCC:";
		final int mccSize = 3;
		ss += OneBlockFromByte(data, index, mccSize, false);index += 3 * 2;
		
		ss += "MNC:";
		final int mncSize = 3;
		ss += OneBlockFromByte(data, index, mncSize, false);index += 3 * 2;
		
		ss += "LOC:";
		final int locSize = 2;
		ss += OneBlockFromByte(data, index, locSize, false);index += 2 * 2;
		
		ss += OneElementByte("rac: ", data, index, false);index += 2;
		
		ss += "TMSI:";
		final int tmsiSize = 4;
		ss += OneBlockFromByte(data, index, tmsiSize, false);index += 4 * 2;
		
		ss += OneElementByte("is_t3212_running:", data, index, false);index += 2;
		
		ss += OneElement2Byte("t3212_timer_value:", data, index, false);index += 4;
		
		ss += OneElement2Byte("t3212_passed_time:", data, index, false);index += 4;
		
		ss += OneElementByte("common_access_class: ", data, index, false);index += 2;
		ss += OneElementByte("cs_access_class: ", data, index, false);index += 2;
		ss += OneElementByte("ps_access_class: ", data, index, false);index += 2;
		
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment 1 byte
		}
		
		return ss;
	}
	//type 27:
	public String Get3GTcmMmiEmInfo()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize;
		
		int end = start + m3GTcmMmiEmInfoSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GTcmMmiEmInfo is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GTcmMmiEmInfo---------------------");
		
		int index = 0;
		String ss = "[RR 3G TCM MMI EM Info]\n";
		ss += OneElementByte("num_of_valid_entries: ", data, index, false);index += 2;
		
		final int GPRS_MAX_PDP_SUPPORT=3;
		for(int i=0; i<GPRS_MAX_PDP_SUPPORT; i++)
		{
			ss += OneElementByte("nsapi"+i+":", data, index, false);index += 2;
			ss += OneElementByte("data_speed_value"+i+":", data, index, false);index += 2;
		}
		
		return ss;
	}
	//type 47:
	public String Get3GCsceEMServCellSStatusIndFDD()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize + m3GTcmMmiEmInfoSize;
		
		int end = start + m3GCsceEMServCellSStatusIndSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GCsceEMServCellSStatusInd is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GCsceEMServCellSStatusInd---------------------");
		
		int index = 0;
		String ss = "[RR 3G CsceEMServCellSStatusInd]\n";
		
		ss += OneElementByte("ref_count: ", data, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment 1 byte
		}
		
		ss += OneElement2Byte("msg_len: ", data, index, false);index += 4;
		
		
		
		ss += OneElementByte("cell_idx: ", data, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 2;//alignment 1 byte
		}
		
		ss += OneElement2Byte("uarfacn_DL: ", data, index, false);index += 4;
		
		
		
		ss += OneElement2Byte("psc: ", data, index, false);index += 4;
		
		ss += OneElementByte("is_s_criteria_satisfied: ", data, index, false);index += 2;
		
		ss += OneElementByte("qQualmin: ", data, index, true);index += 2;
		
		ss += OneElementByte("qRxlevmin: ", data, index, true);index += 2;
		
		if(true == mAlignmentEnable)
		{
			index += 2 * 3;//alignment 3 byte
		}
		
		ss += OneElement4Byte("srxlev: ", data, index, true);index += 8;
		
		ss += OneElement4Byte("spual: ", data, index, true);index += 8;
		
		String strRscp = GetValueFrom4Byte(data, index, true);index += 8;
		
		long rscp = 0;
		try
		{
			rscp = Long.valueOf(strRscp)/4096;		
		}
		catch (NumberFormatException e)
		{	
			Xlog.v(TAG, "rscp = Long.valueOf(strRscp)/4096 exp.");
		}
		ss += "rscp: "+ rscp+ "\n";
		
		String strEcno = GetValueFrom4Byte(data, index, true);index += 8;
		float ecno = 0;
		try
		{
			ecno = Float.valueOf(strEcno)/4096;		
		}
		catch (NumberFormatException e)
		{	
			Xlog.e(TAG, "ecno = Long.valueOf(strEcno)/4096 exp.");
		}
		ss += "ec_no: "+ ecno+ "\n";
		
		
		ss += OneElement2Byte("cycle_len: ", data, index, false);index += 4;
		
		ss += OneElementByte("quality_measure: ", data, index, false);index += 2;
		
		ss += OneElementByte("band: ", data, index, false);index += 2;
		
		ss += OneElement4Byte("rssi: ", data, index, true);index += 8;
		
		ss += OneElement4Byte("cell_identity: ", data, index, false);index += 8;
		
		return ss;
	}
	//type 47:
	public String Get3GCsceEMServCellSStatusIndTDD()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize + m3GTcmMmiEmInfoSize;
		
		int end = start + m3GCsceEMServCellSStatusIndSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "Read TDD start offset "+start);
		Xlog.v(TAG, "Read TDD end offset "+end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GCsceEMServCellSStatusIndTDD is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GCsceEMServCellSStatusIndTDD---------------------");
		
		int index = 0;
		String ss = "[RR 3G CsceEMServCellSStatusInd]\n";
		
		ss += OneElementByte("ref_count: ", data, index, false);index += 2;
		
		if(mAlignmentEnable)
		{
			index += 2;//alignment 1 byte
		}
		
		ss += OneElement2Byte("msg_len: ", data, index, false);index += 4;
		
		
		
		ss += OneElementByte("cell_idx: ", data, index, false);index += 2;
		if(mAlignmentEnable)
		{
			index += 2;//alignment 1 byte
		}
		
		ss += OneElement2Byte("uarfacn_DL: ", data, index, false);index += 4;
		
		
		
		ss += OneElement2Byte("psc: ", data, index, false);index += 4;
		
		ss += OneElementByte("is_s_criteria_satisfied: ", data, index, false);index += 2;
		
		ss += OneElementByte("qQualmin: ", data, index, true);index += 2;
		
		ss += OneElementByte("qRxlevmin: ", data, index, true);index += 2;
		
		if(mAlignmentEnable)
		{
			index += 2 * 3;//alignment 3 byte
		}
		
		
		
		ss += OneElement4Byte("srxlev: ", data, index, true);index += 8;
		
		ss += OneElement4Byte("spual: ", data, index, true);index += 8;
		
		String strRscp = GetValueFrom4Byte(data, index, true);index += 8;
		
		long rscp = 0;
		try
		{
			rscp = Long.valueOf(strRscp)/4096;		
		}
		catch (NumberFormatException e)
		{	
			Xlog.v(TAG, "rscp = Long.valueOf(strRscp)/4096 exp.");
		}
		ss += "rscp:" + rscp +"\n";
		
		//ss += OneElement4Byte("ec_no: ", data, index, true);index += 8;
		
		ss += OneElement2Byte("cycle_len: ", data, index, false);index += 4;
		
		//ss += OneElementByte("quality_measure: ", data, index, false);index += 2;
		
		ss += OneElementByte("band: ", data, index, false);index += 2;
		
		if(mAlignmentEnable)
		{
			index += 2 * 1;//alignment 1 byte
		}
		
		ss += OneElement4Byte("rssi: ", data, index, true);index += 8;
		
		ss += OneElement4Byte("cell_identity: ", data, index, false);index += 8;
		
		return ss;
	}
	//type 52:
	public String Get3GCsceEmInfoMultiPlmn()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize + m3GTcmMmiEmInfoSize + m3GCsceEMServCellSStatusIndSize;
		
		int end = start + m3GCsceEmInfoMultiPlmnSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GCsceEmInfoMultiPlmn is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GCsceEmInfoMultiPlmn---------------------");
		
		int index = 0;
		String ss = "[RR 3G CsceEmInfoMultiPlmn]\n";
		ss += OneElementByte("multi_plmn_count: ", data, index, false);index += 2;
		
		for(int i=0; i<6; i++)
		{
			ss += OneElementByte("mcc1_"+i+":", data, index, false);index += 2;
			ss += OneElementByte("mcc2_"+i+":", data, index, false);index += 2;
			ss += OneElementByte("mcc3_"+i+":", data, index, false);index += 2;
			ss += OneElementByte("mnc1_"+i+":", data, index, false);index += 2;
			ss += OneElementByte("mnc2_"+i+":", data, index, false);index += 2;
			ss += OneElementByte("mnc3_"+i+":", data, index, false);index += 2;
		}
		
		return ss;
	}
	//type 53:
	public String Get3GMemeEmInfoUmtsCellStatus()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize + m3GTcmMmiEmInfoSize + m3GCsceEMServCellSStatusIndSize
		+ m3GCsceEmInfoMultiPlmnSize;
		
		int end = start + m3GMemeEmInfoUmtsCellStatusSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GMemeEmInfoUmtsCellStatus is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GMemeEmInfoUmtsCellStatus---------------------");
		
		int index = 0;
		String ss = "[RR 3G MemeEmInfoUmtsCellStatus]\n";
		ss += OneElementByte("tx_power: ", data, index, true);index += 2;
		ss += OneElementByte("num_cells: ", data, index, false);index += 2;
		
		if(true == mAlignmentEnable)
		{
			index += 2*2;//alignment 2 byte
		}
		
		for(int i=0; i<19; i++)
		{
			ss += OneElement2Byte("UARFCN"+i+":", data, index, false);index += 4;
			ss += OneElement2Byte("PSC"+i+":", data, index, false);index += 4;
			ss += OneElement4Byte("RSCP"+i+":", data, index, true);index += 8;
			ss += OneElement4Byte("ECNO"+i+":", data, index, true);index += 8;
			ss += OneElementByte("cell_type"+i+":", data, index, false);index += 2;
			ss += OneElementByte("Band"+i+":", data, index, false);index += 2;
			
			if(true == mAlignmentEnable)
			{
				index += 2*2;//alignment 2 byte
			}
			ss += OneElement4Byte("RSSI"+i+":", data, index, true);index += 8;
			ss += OneElement4Byte("Cell_identity"+i+":", data, index, false);index += 8;
		}
		
		return ss;
		
	}
	//type 61:
	public String Get3GMemeEmPeriodicBlerReportInd()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize + m3GTcmMmiEmInfoSize + m3GCsceEMServCellSStatusIndSize
		+ m3GCsceEmInfoMultiPlmnSize + m3GMemeEmInfoUmtsCellStatusSize;
		
		int end = start + m3GMemeEmPeriodicBlerReportIndSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GMemeEmPeriodicBlerReportInd is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GMemeEmPeriodicBlerReportInd---------------------");
		
		int index = 0;
		String ss = "[RR 3G MemeEmPeriodicBlerReportInd]\n";
		ss += OneElementByte("num_trch: ", data, index, false);index += 2;		
		
		if(true == mAlignmentEnable)
		{
			index += 2*3;//alignment 3 byte
		}
		
		for(int i=0; i<8; i++)
		{
			ss += OneElementByte("TrCHId"+i+":", data, index, false);index += 2;
			
			if(true == mAlignmentEnable)
			{
				index += 2*3;//alignment 3 byte
			}			
			
			ss += OneElement4Byte("TotalCRC"+i+":", data, index, false);index += 8;
			ss += OneElement4Byte("BadCRC"+i+":", data, index, false);index += 8;			
		}
		
		return ss;
	}
	//type 63:
	public String Get3GUrrUmtsSrncId()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize + m3GTcmMmiEmInfoSize + m3GCsceEMServCellSStatusIndSize
		+ m3GCsceEmInfoMultiPlmnSize + m3GMemeEmInfoUmtsCellStatusSize
		+ m3GMemeEmPeriodicBlerReportIndSize;
		
		int end = start + m3GUrrUmtsSrncIdSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GUrrUmtsSrncId is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GUrrUmtsSrncId---------------------");
		
		int index = 0;
		String ss = "[RR 3G UrrUmtsSrncId]\n";
		ss += OneElement2Byte("srnc: ", data, index, false);index += 4;
		
		return ss;
	}
	//type 64:
	public String Get3GSlceEmPsDataRateStatusInd()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize + m3GTcmMmiEmInfoSize + m3GCsceEMServCellSStatusIndSize
		+ m3GCsceEmInfoMultiPlmnSize + m3GMemeEmInfoUmtsCellStatusSize
		+ m3GMemeEmPeriodicBlerReportIndSize + m3GUrrUmtsSrncIdSize;
		
		int end = start + m3GSlceEmPsDataRateStatusIndSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GSlceEmPsDataRateStatusInd is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GSlceEmPsDataRateStatusInd---------------------");
		
		int index = 0;
		String ss = "[RR 3G SlceEmPsDataRateStatusInd]\n";
		ss += OneElementByte("ps_number: ", data, index, false);index += 2;		
		
		if(true == mAlignmentEnable)
		{
			index += 2*3;//alignment 3 byte
		}
		
		for(int i=0; i<8; i++)
		{
			ss += OneElementByte("RAB_ID"+i+":", data, index, false);index += 2;
			ss += OneElementByte("RB_UD"+i+":", data, index, true);index += 2;
			
			if(true == mAlignmentEnable)
			{
				index += 2*2;//alignment 2 byte
			}			
			
			ss += OneElement4Byte("DL_rate"+i+":", data, index, false);index += 8;
			ss += OneElement4Byte("UL_rate"+i+":", data, index, false);index += 8;			
		}
		
		return ss;
	}
	//type 65:
	public String Get3GMemeEmInfoHServCellInd()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GMmEmInfoSize + m3GTcmMmiEmInfoSize + m3GCsceEMServCellSStatusIndSize
		+ m3GCsceEmInfoMultiPlmnSize + m3GMemeEmInfoUmtsCellStatusSize
		+ m3GMemeEmPeriodicBlerReportIndSize + m3GUrrUmtsSrncIdSize
		+ m3GSlceEmPsDataRateStatusIndSize ;
		
		int end = start + m3GMemeEmInfoHServCellIndSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GMemeEmInfoHServCellInd is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GMemeEmInfoHServCellInd---------------------");
		
		int index = 0;
		String ss = "[RR 3G MemeEmInfoHServCellInd]\n";
		ss += OneElement2Byte("HSDSCH_Serving_UARFCN: ", data, index, false);index += 4;	
		ss += OneElement2Byte("HSDSCH_Serving_PSC: ", data, index, false);index += 4;
		ss += OneElement2Byte("EDCH_Serving_UARFCN: ", data, index, false);index += 4;
		ss += OneElement2Byte("EDCH_Serving_PSC: ", data, index, false);index += 4;
		
		return ss;
		
		
	}
	
	public String Get3GHandoverSequenceIndStuct()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize;
		
		int end = start + m3GHandoverSequenceIndStuctSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GHandoverSequenceIndStuct is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GHandoverSequenceIndStuct---------------------");
		
		int index = 0;
		String ss = "[RR 3G HandoverSequenceIndStuct]\n";
		ss += OneElementByte("service_status: ", data, index, false);index += 2;		
		
		if(true == mAlignmentEnable)
		{
			index += 2*3;//alignment 3 byte
		}
		
		ss += "[old_cell_info:-----]\n";
		ss += OneElement2Byte("primary_uarfcn_DL: ", data, index, false);index += 4;
		ss += OneElement2Byte("working_uarfcn: ", data, index, false);index += 4;
		ss += OneElement2Byte("physicalCellId: ", data, index, false);index += 4;
		
		ss += "[target_cell_info:-----]\n";
		ss += OneElement2Byte("primary_uarfcn_DL: ", data, index, false);index += 4;
		ss += OneElement2Byte("working_uarfcn: ", data, index, false);index += 4;
		ss += OneElement2Byte("physicalCellId: ", data, index, false);index += 4;
		
		return ss;
	}
	public String Get3GUl2EmAdmPoolStatusIndStruct()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GHandoverSequenceIndStuctSize;
		
		int end = start + m3GUl2EmAdmPoolStatusIndStructSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GUl2EmAdmPoolStatusIndStruct is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GUl2EmAdmPoolStatusIndStruct---------------------");
		
		int index = 0;
		String ss = "[RR 3G Ul2EmAdmPoolStatusIndStruct]\n";
		ss += "[dl_adm_poll_info:-----]\n";
		
		for(int i=0; i<4; i++)
		{
			ss += OneElement2Byte("max_usage_kbytes"+i+":", data, index, false);index += 4;
			ss += OneElement2Byte("avg_usage_kbytes"+i+":", data, index, false);index += 4;				
		}
		
		ss += "[ul_adm_poll_info:-----]\n";
		
		for(int i=0; i<4; i++)
		{
			ss += OneElement2Byte("max_usage_kbytes"+i+":", data, index, false);index += 4;
			ss += OneElement2Byte("avg_usage_kbytes"+i+":", data, index, false);index += 4;				
		}
		return ss;
	}
	public String Get3GUl2EmPsDataRateStatusIndStruct()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GHandoverSequenceIndStuctSize
		+ m3GUl2EmAdmPoolStatusIndStructSize;
		
		int end = start + m3GUl2EmPsDataRateStatusIndStructSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GUl2EmPsDataRateStatusIndStruct is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GUl2EmPsDataRateStatusIndStruct---------------------");
		
		int index = 0;
		String ss = "[RR 3G Ul2EmPsDataRateStatusIndStruct]\n";
		ss += OneElement2Byte("rx_mac_data_rate:", data, index, false);index += 4;
		ss += OneElement2Byte("rx_pdcp_data_rate:", data, index, false);index += 4;
		ss += OneElement2Byte("tx_mac_data_rate:", data, index, false);index += 4;
		ss += OneElement2Byte("tx_pdcp_data_rate:", data, index, false);index += 4;		
		
		return ss;		
	}
	public String Get3Gul2EmHsdschReconfigStatusIndStruct()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GHandoverSequenceIndStuctSize
		+ m3GUl2EmAdmPoolStatusIndStructSize
		+ m3GUl2EmPsDataRateStatusIndStructSize;
		
		int end = start + m3Gul2EmHsdschReconfigStatusIndStructSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3Gul2EmHsdschReconfigStatusIndStruct is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3Gul2EmHsdschReconfigStatusIndStruct---------------------");
		
		int index = 0;
		String ss = "[RR 3G Ul2EmHsdschReconfigStatusIndStruct]\n";		
		for(int i=0; i<8; i++)
		{
			ss += OneElementByte("reconfig_info"+i+":", data, index, false);index += 2;						
		}
		return ss;
	}	
	public String Get3GUl2EmUrlcEventStatusIndStruct()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GHandoverSequenceIndStuctSize
		+ m3GUl2EmAdmPoolStatusIndStructSize
		+ m3GUl2EmPsDataRateStatusIndStructSize
		+ m3Gul2EmHsdschReconfigStatusIndStructSize;
		
		int end = start + m3GUl2EmUrlcEventStatusIndStructSize;
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------Get3GUl2EmUrlcEventStatusIndStruct is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GUl2EmUrlcEventStatusIndStruct---------------------");
		
		int index = 0;
		String ss = "[RR 3G Ul2EmUrlcEventStatusIndStruct]\n";	
		ss += OneElementByte("rb_id:", data, index, false);index += 2;	
		ss += OneElementByte("rlc_action:", data, index, false);index += 2;			
		
		ss += "rb_info:--- \n";
		ss += OneElementByte("is_srb:", data, index, false);index += 2;	
		ss += OneElementByte("cn_domain:", data, index, false);index += 2;	
		
		ss += "rlc_info:--- \n";
		ss += OneElementByte("rlc_mode:", data, index, false);index += 2;	
		ss += OneElementByte("direction:", data, index, false);index += 2;
		
		ss += "rlc_parameter:--- \n";
		ss += OneElement2Byte("pdu_Size:", data, index, false);index += 4;	
		ss += OneElement2Byte("tx_window_size:", data, index, false);index += 4;
		ss += OneElement2Byte("rx_window_size:", data, index, false);index += 4;
		ss += OneElementByte("discard_mode:", data, index, false);index += 2;
		if(true == mAlignmentEnable)
		{
			index += 1*2;//alignment 1 byte
		}
		ss += OneElement2Byte("discard_value:", data, index, false);index += 4;
		
		ss += OneElementByte("flush_data_indicator:", data, index, false);index += 2;
		ss += OneElementByte("reset_cause:", data, index, false);index += 2;
		
		return ss;
		
	}
	public String Get3GUl2EmPeriodicBlerReportInd()
	{
		int start = calc2GSize()+ mxGCsceEMNeighCellSStatusIndStructSize
		+ m3GHandoverSequenceIndStuctSize
		+ m3GUl2EmAdmPoolStatusIndStructSize
		+ m3GUl2EmPsDataRateStatusIndStructSize
		+ m3Gul2EmHsdschReconfigStatusIndStructSize
		+ m3GUl2EmUrlcEventStatusIndStructSize;
		
		int end = start + m3GUl2EmPeriodicBlerReportIndSize;
		String data = dataString.substring(start, end);
		
	        Xlog.v(TAG, "NetworkInfo ------Get3GUl2EmPeriodicBlerReportInd is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------Get3GUl2EmPeriodicBlerReportInd---------------------");
		
		int index = 0;
		String ss = "[RR 3G Ul2EmPeriodicBlerReportInd]\n";	
		
		ss += OneElementByte("num_trch:", data, index, false);index += 2;
		
		if(true == mAlignmentEnable)
		{
			index += 3*2;//alignment 3 byte
		}
		
		ss += "TrCHBler:--------";
		
		for(int i=0; i<8; i++)
		{
			ss += OneElementByte("TrCHId"+i+":", data, index, false);index += 2;
			if(true == mAlignmentEnable)
			{
				index += 3*2;//alignment 3 byte
			}			
			ss += OneElement4Byte("TotalCRC"+i+":", data, index, false);index += 8;
			ss += OneElement4Byte("BadCRC"+i+":", data, index, false);index += 8;
		}
		return ss;
	}
	public String GetxGCsceEMNeighCellSStatusIndStructSize()
	{
		int start = calc2GSize();
		
		int end = start + mxGCsceEMNeighCellSStatusIndStructSize;
		
		
		Xlog.v(TAG, "Read start offset "+start);
		Xlog.v(TAG, "Read end offset "+end);
		
		
		String data = dataString.substring(start, end);
		
		Xlog.v(TAG, "NetworkInfo ------GetxGCsceEMNeighCellSStatusIndStructSize is:\n");
		Xlog.v(TAG, data + "\n");
		Xlog.v(TAG, "NetworkInfo ------GetxGCsceEMNeighCellSStatusIndStructSize---------------------");
		
		int index = 0;
		String ss = "[RR xG CsceEMNeighCellSStatusIndStructSize]\n";	
		
		ss += OneElementByte("ref_count:", data, index, false);index += 2;
		
		if(mAlignmentEnable)
		{
			index += 1*2;//alignment 1 byte
		}
		
		ss += OneElement2Byte("msg_len", data, index, false);index += 4;
		
		ss += OneElementByte("neigh_cell_count:", data, index, false);index += 2;
		
		String xgType =  OneElementByte("RAT_type:", data, index, false);index += 2;
		
		ss += xgType;
		
		if(mAlignmentEnable)
		{
			index += 2*2;//alignment 2 byte
		}
		if(xgType.equalsIgnoreCase("1"))//2G info
		{
			ss += "----GSM_neigh_cells----";
			for(int i=0; i<16; i++)
			{
				ss += OneElementByte("cellidx"+i+":", data, index, false);index += 2;
				if(mAlignmentEnable)
				{
					index += 1*2;//alignment 1 byte
				}	
				ss += OneElement2Byte("arfcn", data, index, false);index += 4;
				ss += OneElementByte("bsic"+i+":", data, index, false);index += 2;
				ss += OneElementByte("is_bsic_verified"+i+":", data, index, false);index += 2;
				ss += OneElementByte("is_s_criteria_saticified"+i+":", data, index, false);index += 2;
				ss += OneElementByte("freq_band"+i+":", data, index, false);index += 2;
				ss += OneElementByte("qRxlevmin"+i+":", data, index, true);index += 2;
				if(mAlignmentEnable)
				{
					index += 3*2;//alignment 3 byte
				}
				
				ss += OneElement4Byte("srxlev"+i+":", data, index, true);index += 8;
				ss += OneElement4Byte("rssi"+i+":", data, index, true);index += 8;
			}
		}
		else  //3G
		{
			ss += "----3G_neigh_cells----";
			for(int i=0; i<16; i++)
			{
				ss += OneElementByte("cellidx"+i+":", data, index, false);index += 2;
				if(mAlignmentEnable)
				{
					index += 1*2;//alignment 1 byte
				}	
				ss += OneElement2Byte("arfcn_DL", data, index, false);index += 4;
				ss += OneElement2Byte("psc", data, index, false);index += 4;
				ss += OneElementByte("is_s_criteria_saticified"+i+":", data, index, false);index += 2;
				ss += OneElementByte("qQualmin"+i+":", data, index, true);index += 2;
				ss += OneElementByte("qRxlevmin"+i+":", data, index, true);index += 2;
				
				if(mAlignmentEnable)
				{
					index += 3*2;//alignment 3 byte
				}
				
				ss += OneElement4Byte("srxlev"+i+":", data, index, true);index += 8;
				ss += OneElement4Byte("squal"+i+":", data, index, true);index += 8;
				String strRscp = GetValueFrom4Byte(data, index, true);index += 8;
				
				long rscp = 0;
				try
				{
					rscp = Long.valueOf(strRscp)/4096;		
				}
				catch (NumberFormatException e)
				{	
					Xlog.v(TAG, "rscp = Long.valueOf(strRscp)/4096 exp.");
				}
				ss += "rscp: "+ rscp +"\n";
				
				ss += OneElement4Byte("ec_no"+i+":", data, index, true);index += 8;
			}
		}
	
		return ss;
	}
}
