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

package com.mediatek.GoogleOta;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;

public class GoogleOtaUnzipChecksum {	
    private static String TAG = "GoogleOta";
    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte
    public static final int UNZIP_FAILED = 0;
    public static final int CKSUM_ERROR  = 1;
    public static final int UNZIP_SUCCESS= 2;

    public static int unzipDelta(String zipFile){
    	Util.logInfo(TAG, "unzipDelta");
        File resFile = new File(zipFile);
        boolean result = false;
        String deltaPath = resFile.getParent();
        if (deltaPath == null) {
            return UNZIP_FAILED;
        }
        result = unZipFile(resFile, deltaPath);
        if (!result) {
        	Util.logInfo(TAG, "unzipDelta:result = "+result);
            return UNZIP_FAILED;
        }
    	
		if(checkPackage(deltaPath) == false){
			Util.logInfo(TAG, "unzipDelta: cksum error");
			return CKSUM_ERROR;
		}
		return UNZIP_SUCCESS;
	}

    public static void deleteUnuseFile(String filePath){
    	Util.logInfo(TAG, "deleteUnuseFile");
        File pkg = new File(filePath+"/"+"package.zip");
        if (pkg != null && pkg.exists()) {
            pkg.delete();
        }
        File chf = new File(filePath+"/"+"md5sum");
        if (chf != null && chf.exists()) {
            chf.delete();
        }
	}
    
    public static void deleteCrashFile(String filePath){
    	Util.logInfo(TAG, "deleteUnuseFile");
        File pkg = new File(filePath+"/"+"update.zip");
        if (pkg != null && pkg.exists()) {
            pkg.delete();
        }
        File chf = new File(filePath+"/"+"md5sum");
        if (chf != null && chf.exists()) {
            chf.delete();
        }
	}

	private static boolean unZipFile(File zipFile, String folderPath){
		Util.logInfo(TAG, "unZipFile");
		InputStream in = null;
		OutputStream out = null;
        ZipFile zf = null;
        try {
            zf = new ZipFile(zipFile);
            if (zf == null) {
                return false;
            }
            for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
            	if (entries == null) {
            	    return false;
            	}
                ZipEntry entry = ((ZipEntry)entries.nextElement());
                in = zf.getInputStream(entry);
                String str = folderPath + File.separator + entry.getName();
                Util.logInfo(TAG, "unZipFile:file = "+str);
                if (entry == null || in == null || str == null) {
                    return false;
                }
                File desFile = new File(str);
                if (desFile == null) {
                    return false;
                }
                if (desFile.exists()) {
                    desFile.delete();
                }
                desFile.createNewFile();
                out = new FileOutputStream(desFile);
                if (out == null) {
                    return false;
                }
                byte buffer[] = new byte[BUFF_SIZE];
                int realLength = 0;
                while ((realLength = in.read(buffer)) > 0) {
                    out.write(buffer, 0, realLength);
                }
                out.close();
                out = null;
                in.close();
                in = null;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(in != null){
                    in.close();
                }
                if(out != null){
                    out.close();
                }
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
	
    private static String getFileMD5(String file) {
        Util.logInfo(TAG, "getFileMD5"); 
        FileInputStream fis = null;
        StringBuffer buf = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[BUFF_SIZE];
            int length = -1;
            Util.logInfo(TAG, "getFileMD5:GenMd5 start");
            long s = System.currentTimeMillis();
            if (fis == null || md == null) {
                return null;
            }
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] bytes = md.digest();
            if (bytes == null) {
                return null;
            }
            for(int i = 0; i < bytes.length; i++) {
                String md5s = Integer.toHexString(bytes[i] & 0xff);
                if (md5s == null || buf == null) {
                    return null;
                }
                if(md5s.length()==1) {
                    buf.append("0");
                }
                buf.append(md5s);
            }
            Util.logInfo(TAG, "getFileMD5:GenMd5 success! spend the time: "+ (System.currentTimeMillis() - s) + "ms");
            return buf.toString();
        } catch (Exception ex) {
            Util.logInfo(TAG, "getFileMD5:Exception");
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if(fis != null){
                    fis.close();
                }
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
	}

    @SuppressWarnings("unused")
    private static String getMD5sum(String file){
        Util.logInfo(TAG, "getMD5sum"); 
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            if (fis == null) {
                return null;
            }
            byte[] buffer = new byte[64];
            int length = -1;
            length = fis.read(buffer);
            Util.logInfo(TAG, "getMD5sum:length = "+length);
            return new String(buffer, 0, length);
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        } finally {
            try {
                if(fis != null){
                    fis.close();
                }
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    private static boolean checkFilesEntities(String path){
    	Util.logInfo(TAG, "checkFilesEntities:path = "+path);
        File updf = new File(path+"/"+"update.zip");
        if (updf == null || !updf.exists()) {
            return false;
        }
        File chf = new File(path+"/"+"md5sum");
        if (chf == null || !chf.exists()) {
            return false;
        }
        return true;
    }

    private static boolean checkPackage(String path){
        if (!checkFilesEntities(path)) {
        	Util.logInfo(TAG, "checkPackage:lost file from net"); 
            return false;
        }
        String filemd5 = getFileMD5(path+"/"+"update.zip");
        String md5sum  = getMD5sum(path+"/"+"md5sum");
        Util.logInfo(TAG, "checkPackage:filemd5="+filemd5);
        Util.logInfo(TAG, "checkPackage:md5sum ="+md5sum+"end");
        if (filemd5 == null) {
            return false;
        }
        if (filemd5.equals(md5sum)) {
            return true;
        }
        return false;
    }

	public static int checkUnzipSpace(Context context, String zipFile){
    	Util.logInfo(TAG, "checkUnzipSpace");
        long unzipSize = (long)(Util.getFileSize(zipFile) * 1.5);
        return Util.checkSdcardIsAvailable(context, unzipSize);
    }
}

