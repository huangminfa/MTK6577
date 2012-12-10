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

package com.mediatek.engineermode.wifi;

public class EMWifi {
	public static boolean isIntialed = false;
	public static boolean mEMWifiWorked = false;
	public static boolean  is5GNeeded = true;
	// private static int u4DevHandle;
	public static native int initial();

	public static native int UnInitial();

	public static native int getXtalTrimToCr(long[] value);

	public static native int setTestMode();

	public static native int setNormalMode();

	public static native int setStandBy();

	public static native int setEEPRomSize(long i4EepromSz);

	public static native int setEEPRomFromFile(String atcFileName);

	public static native int readTxPowerFromEEPromEx(long i4ChnFreg,
			long i4Rate, long[] PowerStatus, int arraylen);

	public static native int setEEPromCKSUpdated();

	public static native int getPacketTxStatusEx(long[] PktStatus, int arraylen);

	public static native int getPacketRxStatus(long[] i4Init, int arraylen);

	public static native int setOutputPower(long i4Rate, long i4TxPwrGain,
			int i4TxAnt);

	public static native int setLocalFrequecy(long i4TxPwrGain, long i4TxAnt);

	public static native int setCarrierSuppression(long i4Modulation,
			long i4TxPwrGain, long i4TxAnt);

	public static native int setOperatingCountry(String acChregDomain);

	public static native int setChannel(long i4ChFreqkHz);

	public static native int getSupportedRates(int[] pu2RateBuf, long i4MaxNum);

	public static native int setOutputPin(long i4PinIndex, long i4OutputLevel);

	public static native int readEEPRom16(long u4Offset, long[] pu4Value);
	public static native int readSpecEEPRom16(long u4Offset, long[] pu4Value);

	public static native int writeEEPRom16(long u4Offset, long u4Value);

	public static native int eepromReadByteStr(long u4Addr, long u4Length,
			byte[] paucStr);

	public static native int eepromWriteByteStr(long u4Addr, long u4Length,
			String paucStr);

	public static native int setATParam(long u4FuncIndex, long u4FuncData);

	public static native int getATParam(long u4FuncIndex, long[] pu4FuncData);

	public static native int setXtalTrimToCr(long u4Value);

	public static native int queryThermoInfo(long[] pi4Enable, int len);

	public static native int setThermoEn(long i4Enable);

	public static native int getEEPRomSize(long[] value);
	public static native int getSpecEEPRomSize(long[] value);
	public static native int setPnpPower(long i4PowerMode);

	public static native int setAnritsu8860bTestSupportEn(long i4Enable);

	public static native int writeMCR32(long offset, long value);

	public static native int readMCR32(long offset, long[] value);
	//added by mtk80758 2010-11-5
	public static native int getDPDLength(long[] value);
	public static native int readDPD32(long offset,  long[] value);
	public static native int writeDPD32(long offset, long value);
	public static native int setDPDFromFile(String atcFileName);
	// Added by mtk54046 @ 2012-01-05 for get support channel list
	public static native int getSupportChannelList(long[] value);
	
	static {
		System.loadLibrary("emwifi_jni");
		// initial();
	}

}
