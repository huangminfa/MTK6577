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

import com.mediatek.GoogleOta.Util.DownloadDescriptor;

public interface IDownloadStatus {
    static final int MSG_NETWORKERROR           = 0;
    static final int MSG_NEWVERSIONDETECTED     = 1;
    static final int MSG_NONEWVERSIONDETECTED   = 2;
    static final int MSG_DLPKGCOMPLETE          = 3;
    static final int MSG_DLPKGUPGRADE           = 4;
    static final int MSG_NOTSUPPORT             = 5;
    static final int MSG_NOTSUPPORT_TEMP        = 6;
    static final int MSG_NOVERSIONINFO          = 7;
    static final int MSG_DELTADELETED           = 8;
    static final int MSG_SDCARDCRASHORUNMOUNT   = 9;
    static final int MSG_SDCARDUNKNOWNERROR     = 10;
    static final int MSG_SDCARDINSUFFICENT      = 11;
    static final int MSG_UNKNOWERROR            = 12;
    
    static final int MSG_OTA_PACKAGEERROR       = 13;
    static final int MSG_OTA_RUNCHECKERROR      = 14;
    static final int MSG_OTA_NEEDFULLPACKAGE    = 15;
    static final int MSG_OTA_USERDATAERROR      = 16;
    static final int MSG_OTA_USERDATAINSUFFICENT= 17;
    static final int MSG_OTA_CLOSECLIENTUI      = 18;
    static final int MSG_OTA_SDCARDINFUFFICENT  = 19;
    static final int MSG_OTA_SDCARDERROR        = 20;
    
    static final int MSG_UNZIP_ERROR            = 21;
    static final int MSG_CKSUM_ERROR            = 22;
    static final int MSG_UNZIP_LODING           = 23;
    
    static final int STATE_QUERYNEWVERSION  = 0;
    static final int STATE_NEWVERSION_READY = 1;
    static final int STATE_DOWNLOADING      = 2;
    static final int STATE_CANCELDOWNLOAD   = 3;
    static final int STATE_PAUSEDOWNLOAD    = 4;
    static final int STATE_DLPKGCOMPLETE    = 5;
    static final int STATE_PACKAGEERROR     = 6;


    public long getUpdateImageSize();
    public String getVersion();
    public String getVersionNote();
    public DownloadDescriptor getDownloadDescriptor();
    public void setDownloadDesctiptor(DownloadDescriptor savedDd);

    public int getDLSessionStatus();
    public void setDLSessionStatus(int status);
}
