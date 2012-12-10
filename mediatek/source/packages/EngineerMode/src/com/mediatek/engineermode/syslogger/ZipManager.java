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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.util.Log;

public class ZipManager {
    private static final String TAG = "Syslog_taglog";
 
	/**
	 * Zip a single file or folder into zip file
	 * @param srcFilePath    source file/folder path
	 * @param zipFilePath    destination  zip file name(include file path)
	 * @return  true for success and false for fail
	 */
	public static boolean zipFileOrFolder(String srcFilePath, String zipFilePath) {
		Log.v(TAG, "zipFolder(), srcFolderPath="+srcFilePath+", zipFilePath");
		boolean result = false;
		ZipOutputStream outZip = null;
		try{
			outZip = new ZipOutputStream(new FileOutputStream(zipFilePath));
			
			File file = new File(srcFilePath);
			
			result = zipFile(file.getParent(), file.getName(), outZip);
			
			outZip.flush();
			outZip.finish();
			outZip.close();
		}catch (FileNotFoundException e) {
			result = false;
			Log.e(TAG, "FileNotFoundException", e);
		}catch (IOException e) {
			result = false;
			Log.e(TAG, "FileNotFoundException", e);
		}finally{
			if(outZip!=null){
				try {
					outZip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/**
	 * zip a file in root folder to output stream
	 * @param srcRootPath  root folder path
	 * @param fileRelativePath   file path and name, relative root path
	 * @param zout  zip file stream
	 * @return true for success, false for fail
	 */
	public static boolean zipFile(String srcRootPath, String fileRelativePath, ZipOutputStream zout) {
		Log.v(TAG, "zipFile(), srcRootPath="+srcRootPath+", fileRelativePath="+fileRelativePath);
		if(zout == null){
			Log.e(TAG, "Can not zip file into a null stream");
			return false;
		}
		boolean result = false;
		File file = new File(srcRootPath + File.separator + fileRelativePath);
		if(file.exists()){
			if(file.isFile()){
				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					ZipEntry entry = new ZipEntry(fileRelativePath);
					zout.putNextEntry(entry);
					
					int len = 0;
					byte[] buffer = new byte[512];
					while((len = in.read(buffer))>-1){
						zout.write(buffer, 0, len);
					}
					zout.closeEntry();
					result = true;
				} catch (FileNotFoundException e) {
					Log.e(TAG, "FileNotFoundException", e);
				} catch (IOException e) {
					Log.e(TAG, "IOException", e);
				}finally{
					if(in !=null){
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				return result;
			}else{
				result = true;
				String[] fileList = file.list();
				if(fileList.length<=0){
					ZipEntry entry = new ZipEntry(fileRelativePath + File.separator);
					try {
						zout.putNextEntry(entry);
						zout.closeEntry();
					} catch (IOException e) {
						result = false;
						e.printStackTrace();
					}
				}
				
				for(String subFileName : fileList){
					if(!zipFile(srcRootPath, fileRelativePath+File.separator+subFileName, zout)){
						result = false;
						break;
					}
				}
				return result;
			}
		}else{
			Log.e(TAG, "File ["+file.getPath()+"] does not exitst");
			return false;
		}
	}
	
	
	public static List<String> getZipContentList(String zipFilePath){
		Log.v(TAG, "getZipContentList(), zipFilePath="+zipFilePath);
		List<String> list = new ArrayList<String>();
		ZipInputStream zin = null;
		ZipEntry entry = null;
		
		try {
			zin = new ZipInputStream(new FileInputStream(zipFilePath));
			while((entry=zin.getNextEntry())!=null){
				String name = entry.getName();
				long size = entry.getSize();
				boolean isDir = entry.isDirectory();
				list.add(name+", size:"+size+", ("+(isDir?"folder":"file")+")");
			}
		} catch (FileNotFoundException e) {
			list = null;
			Log.e(TAG, "FileNotFoundException", e);
		} catch (IOException e) {
			list = null;
			Log.e(TAG, "IOException", e);
		}finally{
			if(zin!=null){
				try {
					zin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}
	
	public static void unzipFile(String zipFilePath, String targetPath, boolean allowOverwrite){
		Log.v(TAG, "-->unzipFile(), zipFilePath="+zipFilePath+", targetPath="+targetPath+", allowOverwrite?"+allowOverwrite);
		ZipInputStream zin = null;
		FileOutputStream out = null;
		ZipEntry entry = null;
		String entryName = null;
		
		//Create target folder first
		File targetFolder = new File(targetPath);
		if(targetFolder.exists()){
			Log.e(TAG, "Target folder ["+targetPath+"] already exist. It will be overwrite!");
			if(!allowOverwrite){
				return;
			}
		}
		targetFolder.mkdirs();
		
		try {
			zin = new ZipInputStream(new FileInputStream(zipFilePath));
			
			while ((entry = zin.getNextEntry())!=null) {
				entryName = entry.getName();
				Log.v(TAG, "Encounter entry name = "+entryName);
				if(entry.isDirectory()){
					File folder = new File(targetPath+File.separator+entryName);
					folder.mkdirs();
				}else{
					File file = new File(targetPath+File.separator+entryName);
					File parent = file.getParentFile();
					if(parent!=null){
						parent.mkdirs();
					}
					file.createNewFile();
					out = new FileOutputStream(file);
					
					byte[] buffer = new byte[512];
					int len = 0;
					while((len = zin.read(buffer))>-1){
						out.write(buffer, 0, len);
						out.flush();
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}finally{
			if(zin!=null){
				try {
					zin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
     * transfer long time to time string
     * @param time
     * @return ex: 2012/12/21 23:59
     */
    public static String translateTime(long time){
        GregorianCalendar calendar = new GregorianCalendar();
        DecimalFormat df = new DecimalFormat();
        String pattern = "00";
        df.applyPattern(pattern);
        calendar.setTime(new Date(time));
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minu = calendar.get(Calendar.MINUTE);
        return ""+year+"/"+df.format(month)+"/"+df.format(day)+"  "+df.format(hour) + ":" + df.format(minu);
    }
    
    /**
     * transfer long time to time string
     * @param time
     * @return ex: 2012_1221_2359
     */
    public static String translateTime2(long time){
        GregorianCalendar calendar = new GregorianCalendar();
        DecimalFormat df = new DecimalFormat();
        String pattern = "00";
        df.applyPattern(pattern);
        calendar.setTime(new Date(time));
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minu = calendar.get(Calendar.MINUTE);
        return ""+year+"_"+df.format(month)+df.format(day)+"_"+df.format(hour) + "_" + df.format(minu);
    }
}
