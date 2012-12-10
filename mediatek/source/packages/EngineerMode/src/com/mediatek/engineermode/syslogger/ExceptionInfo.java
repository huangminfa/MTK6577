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

package com.mediatek.engineermode.syslogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import android.util.Log;

/**
 * Keep base information of exception in its object.
 * @author MTK80766
 *
 */
public class ExceptionInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String mType; //e.g.JE/NE
	private String mDiscription; //e.g. NullPointer
	private String mLevel; //e.g. FATAL/EXCEPTION
	private String mProcess; //module
	private String mTime;
	private String mPath;
	private String mBuildVersion; //build version of load. e.g. W10.48.MP
	private String mDeviceName;
	private String mToolVersion = "2.0";
	
	private String TAG = "Syslog_taglog";
	

	/**
	 * Empty constructor for init build version and device name.
	 */
	public ExceptionInfo(){
		setmBuildVersion(android.os.Build.DISPLAY);
		setmDeviceName(android.os.Build.DEVICE);
	}
	
	/**
	 * Init fields from ZZ_INTERNAL file which created by AEE, In which record 
	 * base information about exception such as name, time,level etc. 
	 * The ZZ_INTERNAL file is in the same folder of exception bin file.
	 * @param zzPath path of ZZ_INTERNAL file.
	 * @throws IOException IOException Operations of file may cause this exception
	 */
	public void initFieldsFromZZ(String zzPath) throws IOException {
		Log.d(TAG,"ZZ_INTERNAL's Path:"+zzPath);
		File zz_file = new File(zzPath);
		if (!zz_file.exists()) {
			throw new IOException("ZZ_INTERNAL file is not exist!");
		}
		if (!zz_file.isFile()) {
			throw new IOException("ZZ_INTERNAL file is not a file!");
		}
		FileInputStream fis = new FileInputStream(zz_file);
		byte[] buf = new byte[1024];
		StringBuilder sb = new StringBuilder();
		int len = 0;
		while ((len = fis.read(buf)) != -1) {
			sb.append(new String(buf, 0, len));
		}
		fis.close();
		String[] arr = sb.toString().split(",");
		if (arr.length != SysUtils.ZZ_INTERNAL_LENGTH) {
			throw new IOException("fields count in ZZ_INTERNAL file are not "+SysUtils.ZZ_INTERNAL_LENGTH);
		}

		setmType(arr[0]);// exception type in ZZ_INTERNAL
		setmLevel(arr[5]);
		setmDiscription(arr[6]);
		setmProcess(arr[7]);
		setmTime(arr[8]);
		
	}

	public String getmType() {
		return mType;
	}

	public String getmDiscription() {
		return mDiscription;
	}

	public String getmLevel() {
		return mLevel;
	}

	public String getmProcess() {
		return mProcess;
	}

	public String getmTime() {
		return mTime;
	}

	public String getmPath() {
		return mPath;
	}
//
//	public String getmIndex() {
//		return mIndex;
//	}

	public String getmBuildVersion() {
		return mBuildVersion;
	}

	public String getmDeviceName() {
		return mDeviceName;
	}

	public String getmToolVersion() {
		return mToolVersion;
	}

	protected void setmDiscription(String mDiscription) {
		this.mDiscription = mDiscription;
	}

	protected void setmLevel(String mLevel) {
		if(mLevel.trim().equals("0")){
			this.mLevel="FATAL";
		}else if(mLevel.trim().equals("1")){
			this.mLevel="EXCEPTION";
		}else{
			Log.e(TAG,"mLevel is not a valid value:"+mLevel);
			this.mLevel = mLevel;
		}
		
	}

	protected void setmProcess(String mProcess) {
		this.mProcess = mProcess;
	}

    protected void setmType(String mType){
    	this.mType=mType;
    }
	
	protected void setmTime(String mTime) {
		this.mTime = mTime;
	}

	protected void setmPath(String mPath) {
		this.mPath = mPath;
	}

	protected void setmBuildVersion(String mBuildVersion) {
		this.mBuildVersion = mBuildVersion;
	}

	protected void setmDeviceName(String mDeviceName) {
		this.mDeviceName = mDeviceName;
	}

	protected void setmToolVersion(String mToolVersion) {
		this.mToolVersion = mToolVersion;
	}

	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("[Device Name]: "+mDeviceName+"\n\n");
		sb.append("[Build Version]: "+mBuildVersion+"\n\n");
		sb.append("[Exception Level]: "+mLevel+"\n\n");
		sb.append("[Exception Class]: "+mType+"\n\n");
		sb.append("[Exception Type]: "+mDiscription+"\n\n");
		sb.append("[Process]: "+mProcess+"\n\n");
		sb.append("[Datetime]: "+mTime+"\n");
		return sb.toString();
		
	}	

}
