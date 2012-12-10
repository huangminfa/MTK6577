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

package com.mediatek.bluetooth.map.cache;

import android.util.Log;
import android.content.Context;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import com.mediatek.bluetooth.map.MAP;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.SecurityException;
import com.mediatek.xlog.Xlog;
public class BMessageObject {
	private final String TAG		= "BMessageObject";
	private String 					mFileName;  //store the bmessage
	private File 					mFile;

	private int 					mVersion = 1; // the field seems no use
	private int 					mReadStatus;
	private int 					mMsgType;
	private String					mFolderPath;

	private int						mOrignatorSize;
	private String					mOrignator;

	//envelope
	private ArrayList<Integer>		mRecipientSize;
	private ArrayList<String>		mRecipient;
	
	private int 					mPartId;

	private int						mEncoding;
	private int						mCharset;
	private int						mLanguage;

	private ArrayList<Integer>		mContentSize;
	private long					mWholeSize;

	private Context					mContext;
	private String					mName;

	//only used in the case when message will be pushed
	private boolean					mTransparent;
	private boolean					mRetry;

	public BMessageObject(String path, String fileName){
		try {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdir();
				log("create dir");
			}
			
			mFile = new File(path,fileName);
			mFileName = mFile.getAbsolutePath();

			if (!mFile.exists()) {
				
				boolean result = mFile.createNewFile();
				if(!result) {
					log("the file exists");
				} else {
					log("create file succeed");
				}
			} else {
				if(mFile.isDirectory()) {
					mFile.delete();
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		initCache();
	}

	public BMessageObject(Context context, String fileName){
		mContext = context;
		try{
			FileOutputStream out = mContext.openFileOutput(fileName, Context.MODE_WORLD_READABLE);	
			out.close();
		} catch (FileNotFoundException e){
			log("fail to create file");
		} catch (IOException e) {
			log(e.toString());
		}
		mFile = mContext.getFileStreamPath(fileName);		
		mFileName = mFile.getAbsolutePath();		
		mName = fileName;
		initCache();	
		log("file path"+mFileName);
	}
	private void initCache(){
		mRecipient = new ArrayList<String>();
		mRecipientSize = new ArrayList<Integer>();
		mContentSize = new ArrayList<Integer>();
	}

	public void reset() {
		mReadStatus = -1;
		mMsgType = -1;
		mFolderPath = null;

		mOrignatorSize = 0;
		mOrignator = null;
		
		mRecipientSize.clear();
		mRecipient.clear();
	
		mPartId = 0;

		mEncoding = 0;
		mCharset = 0;
		mLanguage = 0;

		mContentSize.clear();
		mWholeSize = 0;

		//empty file
		try {
			FileOutputStream out = new FileOutputStream(mFile, false);
			out.close();
		} catch (IOException e) {	           			
           log(e.toString());
		}
	}

	public boolean setFolderPath(String folder) {
		mFolderPath = folder;
		return true;
	}

	

	public boolean setOrignator(String orignator){
		mOrignator = orignator;
		if (orignator == null) {
			mOrignatorSize = 0;
		} else {
			mOrignatorSize = orignator.length();
		}
		return true;
	}

	//note: the input is nest vcards
	public boolean addRecipient(String recipient) {
		if (recipient == null) {
			return true;
		}
		mRecipientSize.add(recipient.length());
		mRecipient.add(recipient);
		return true;
	}

	public boolean setPartId(int partId) {
		if (partId >= 0) {
			mPartId = partId;
			return true;
		}
		return false;
	}
	public boolean setMessageType(int type){
		mMsgType = type;
		switch(mMsgType) {
			case MAP.MSG_TYPE_EMAIL:
			case MAP.MSG_TYPE_MMS:
			case MAP.MSG_TYPE_SMS_CDMA:
			case MAP.MSG_TYPE_SMS_GSM:
				mMsgType = type;
				return true;
			default:
				log("error, invalid message type:" + type);				
		}
		return false;
	}

	public boolean setContentSize(int size){
		mWholeSize = size;
		mContentSize.add(size);
		return true;
	}
	public boolean setContentSize(File file){
		if (file == null){
			return false;
		}
		try {
			FileInputStream stream = new FileInputStream(file);
			int size = stream.available();
			mWholeSize = size; 
			mContentSize.add(size);
			return true;
		} catch (IOException e) {			
           log(e.toString());
		}
		return true;
	}
	public boolean setContent (byte[] content){
		FileOutputStream stream;
		try {
			if (mContext == null) {
				stream = new FileOutputStream(mFile);
			
			} else if (mFileName != null){
				stream = mContext.openFileOutput(mName, Context.MODE_WORLD_READABLE);			
			} else {
				log("fail to get content");
				return false;
			}		
			stream.write(content);
			stream.close();
			mContentSize.add(content.length);
				
		}catch (FileNotFoundException e) {
			log(e.toString());
			return false;
		} catch (IOException e) {			
          // throw e;		
           log(e.toString());
		}
		return true;
	}
	public boolean addContent(byte[] content){
		FileOutputStream stream;
		if (content == null || content.length == 0){
			return true;
		}
		try {
			if (mContext == null) {
				stream = new FileOutputStream(mFile);
			
			} else if (mFileName != null){
				stream = mContext.openFileOutput(mName, Context.MODE_WORLD_READABLE | Context.MODE_APPEND);			
			
			} else {
				log("fail to get content");
				return false;
			}
					
			stream.write(content);
			stream.close();
			mContentSize.add(content.length);
			mWholeSize +=content.length; 
			return true;		
		} catch (FileNotFoundException e) {		
           log(e.toString());			
		} catch (IOException e) {			
          // throw e;		
           log(e.toString());
		}
		return true;
	}

	//
	public boolean setEncoding(int encoding) {
		 mEncoding = encoding;
		 return true;
	}

	public boolean setCharset(int charset) {
		switch(charset) {
			case MAP.CHARSET_NATIVE:
			case MAP.CHARSET_UTF8:
				mCharset = charset;
				break;
			default:
				log("error, invalid charset");
				mCharset = MAP.CHARSET_NATIVE; 
		}
	//	return mEncoding == encoding;
		return true;
	}

	public void setReadStatus(int state) {
		switch(state) {
			case MAP.READ_STATUS:
			case MAP.UNREAD_STATUS:
				mReadStatus = state;
				break;
			default:
				log("error, invalid read status: "+ state);
				mReadStatus = MAP.READ_STATUS; 
		}
	}
	public boolean setLang(int lang) {
	/*	switch(lang){
			case MAP.LANG_ENGLISH:
			case MAP.LANG_CHINESE:
			case MAP.LANG_FRENCH:
			case MAP.LANG_HEBREW:
			case MAP.LANG_JAPANESE:
			case MAP.LANG_KOREAN:
			case MAP.LANG_PORTUGUESE:
			case MAP.LANG_SPANISH:
			case MAP.LANG_TURKISH:
			case MAP.LANG_UNKNOWN:
				mLanguage = lang;
				return true;
			default:
				log("invalid Language,"+lang);
				return false;
		}
		*/
		mLanguage = lang;
		return true;
	}

	public int getMessageType() {
		return mMsgType;
	}

	public boolean getContent(byte[] content){
		FileInputStream stream;
		int size;
		try{
			if (mContext == null) {
				stream = new FileInputStream(mFile);			
			} else if (mFileName != null){			
				stream = mContext.openFileInput(mName);					
			} else {
				log("fail to get content");
				return false;
			}				
			stream.read(content);
			stream.close();
		} catch (FileNotFoundException e){
			log("fail to find file");
		} catch (IOException e) {	           			
           log(e.toString());
		}
		
		return true;
	}

	public long getContentSize(){
		return mWholeSize;
	}
	public int getContentSize(int i){
		if (i < mContentSize.size()){
			return mContentSize.get(i).intValue();
		} else {
			return 0;
		}
	}	

	public String getOrignator(){
		return mOrignator;
	}

	public String getFinalRecipient(){
		if (mRecipient.size() > 0) {
			return mRecipient.get(mRecipient.size() - 1);
		} else {
			return null;
		}
	}

	public File getFile(){
		return mFile;
	}

	public String getFolder(){
		if (mFolderPath == null) {
			return null;
		}
		mFolderPath = mFolderPath.toLowerCase();
			
		int lastIndex = mFolderPath.lastIndexOf("/");
		//	lastIndex = (lastIndex < 0)? 0:lastIndex;
		return mFolderPath.substring(lastIndex+1);
	}

	public int getReadStatus(){
		return mReadStatus;
	}
	public int getCharset(){
		return mCharset;
	}

	public boolean releaseResource(){
		if (mFile != null && mFile.exists()){
			 mFile.delete();
		}
		return true;
	}
	//return:
	//@false: do not save message
	//@true: save message
	public boolean isTransparent(){
		return mTransparent;
	}
	
	public boolean isRetry(){
		return mRetry;
	}

	
	private void log(String info){
		Xlog.v(TAG,info);
	}
	
}