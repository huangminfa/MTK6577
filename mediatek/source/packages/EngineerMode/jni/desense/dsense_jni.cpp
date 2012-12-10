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

#define LOG_TAG "DesenseActivity"
#include "jni.h"
#include "mtkfb.h"
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include "utils/Log.h"

#include <AudioSystem.h>
#include "utils/String8.h"

/*
 #define MTKFB_GET_DEFAULT_UPDATESPEED 1
 #define MTKFB_GET_CURR_UPDATESPEED 1
 #define MTKFB_CHANGE_UPDATESPEED 1
 */

using namespace android;

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_mediatek_engineermode_desense_EMDsense_ClassDSwitch(
		JNIEnv * env, jobject obj, jboolean on) {
	if (JNI_FALSE == on) {
		String8 const OFF = (String8)("ForceSpeakerOn=0");
		AudioSystem::setParameters(0, OFF);
		LOGW("OFF ForceSpeakerOn=0");
	} else {
		String8 const ON = (String8)("ForceSpeakerOn=1");
		AudioSystem::setParameters(0, ON);
		LOGW("ON ForceSpeakerOn=1");
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_com_mediatek_engineermode_desense_EMDsense_getClassDStatus(
		JNIEnv * env, jobject obj) {

	//	String8 CLASS_D_STATUS =(String8)("GetSpeakerEnable");
	//	String8 SpeakerStatus = AudioSystem::getParameters(0,CLASS_D_STATUS);
	//
	//        String8 const CLASS_D_STATUS_FALSE= (String8)("GetSpeakerEnable=false");
	//
	//	if(SpeakerStatus.compare(CLASS_D_STATUS_FALSE))
	//	{
	//		return 0;
	//	}
	//	else
	//	{
	//		return 1;
	//	}
	const String8 temp1("GetForceSpeakerEnable");
	String8 temp2 = AudioSystem::getParameters(0, temp1);
	const String8 temp3("GetForceSpeakerEnable=false");
	int result = temp2.compare(temp3);
	LOGW("GetSpeakerEnable = %s result = %d", temp2.string(), result);
	if (result == 0) {
		return 0;
	} else {
		return 1;
	}

}

#ifdef __cplusplus
}
#endif
