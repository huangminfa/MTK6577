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

//#define LOG_TAG "libmtkhdmi_jni"
#define LOG_TAG "hdmi"

#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>

#include <utils/misc.h>
#include <utils/Log.h>

#include "jni.h"
#include "JNIHelp.h"

#include "hdmitx.h"


using namespace android;

#ifdef __cplusplus
    extern "C" {
#endif

#if defined(MTK_HDMI_SUPPORT)
static jboolean hdmi_ioctl(int code, int value)
{
	int fd = open("/dev/hdmitx", O_RDONLY, 0);
	int ret;
    if (fd >= 0) {
        ret = ioctl(fd, code, value);
        if (ret == -1) {
            LOGE("[HDMI] [%s] failed. ioctlCode: %d, errno: %d",
                 __func__, code, errno);
            return 0;
        }
        close(fd);
    } else {
        LOGE("[HDMI] [%s] open mtkfb failed. errno: %d", __func__, errno);
        return 0;
    }
    return ret;
}
#endif


static jboolean enableHDMI(JNIEnv *env, jobject clazz, jboolean enable) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    ret = hdmi_ioctl(MTK_HDMI_AUDIO_VIDEO_ENABLE, enable);
#endif

	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.enableHDMI(%d)\n", enable);
    return ret;
}


static jboolean IPOPowerONHDMI(JNIEnv *env, jobject clazz, jboolean enable) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    if(enable)
    {
        ret = hdmi_ioctl(MTK_HDMI_IPO_POWERON, 1);
    }
    else
    {
        ret = hdmi_ioctl(MTK_HDMI_IPO_POWERON, 0);
    }    

#endif
	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.enableHDMIIPO(%d)\n", enable);

    return ret;
}

static jboolean hdmiPowerEnable(JNIEnv *env, jobject clazz, jboolean enable) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    if(enable)
    {
        ret = hdmi_ioctl(MTK_HDMI_POWER_ENABLE, 1);
    }
    else
    {
        ret = hdmi_ioctl(MTK_HDMI_POWER_ENABLE, 0);
    }    

#endif
	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.enableHDMIPOWER(%d)\n", enable);

    return ret;
}

static jboolean hdmiPortraitEnable(JNIEnv *env, jobject clazz, jboolean enable) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    if(enable)
    {
        ret = hdmi_ioctl(MTK_HDMI_PORTRAIT_ENABLE, 1);
    }
    else
    {
        ret = hdmi_ioctl(MTK_HDMI_PORTRAIT_ENABLE, 0);
    }    

#endif
	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.enableHDMIPortrait(%d)\n", enable);

    return ret;
}

static jboolean enableVideo(JNIEnv *env, jobject clazz, jboolean enable) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    ret = hdmi_ioctl(MTK_HDMI_VIDEO_ENABLE, enable);
#endif

	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.enableVideo(%d)\n", enable);
    return ret;
}


static jboolean enableAudio(JNIEnv *env, jobject clazz, jboolean enable) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    ret = hdmi_ioctl(MTK_HDMI_AUDIO_ENABLE, enable);
#endif

	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.enableAudio(%d)\n", enable);
    return ret;
}


static jboolean GetDeviceStatus(JNIEnv* env, jobject clazz, jboolean is_audio_enabled, jboolean is_video_enabled ) {
    bool ret = false;
	hdmi_device_status h;

#if defined (MTK_HDMI_SUPPORT)
    ret = hdmi_ioctl(MTK_HDMI_GET_DEVICE_STATUS, (int)&h);
#endif

	is_audio_enabled = h.is_audio_enabled;
	is_video_enabled = h.is_video_enabled;

	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMI.GetDeviceStatus(%d %d)\n", is_audio_enabled, is_video_enabled);
    return ret;
}


static jboolean setVideoConfig(JNIEnv* env, jobject clazz, jint vformat) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    ret = hdmi_ioctl(MTK_HDMI_VIDEO_CONFIG, vformat);
#endif
	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.setVideoConfig(%d)\n", vformat);
    return ret;
}

static jboolean setAudioConfig(JNIEnv* env, jobject clazz, jint aformat) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    ret = hdmi_ioctl(MTK_HDMI_AUDIO_CONFIG, aformat);
#endif
	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.setAudioConfig(%d)\n", aformat);
    return ret;
}

static jboolean ishdmiForceAwake(JNIEnv *env, jobject clazz) {
    bool ret = false;
#if defined (MTK_HDMI_SUPPORT)
    int isforceawake = 0;
    hdmi_ioctl(MTK_HDMI_IS_FORCE_AWAKE, (unsigned int)&isforceawake);
    ret = (isforceawake)? true : false;
#endif
	LOGI("[HDMI] JNI com.mediatek.hdmi.HDMINative.ishdmiForceAwake( )\n");
    return ret;
}

// --------------------------------------------------------------------------

static JNINativeMethod gNotify[] = {
    { "enableHDMI", 	"(Z)Z", 	(void*)enableHDMI},
    { "enableHDMIIPO", 	"(Z)Z", 	(void*)IPOPowerONHDMI},
    { "enableVideo", 	"(Z)Z", 	(void*)enableVideo},
    { "enableAudio", 	"(Z)Z", 	(void*)enableAudio},
//    { "GetDeviceStatus","(ZZ)Z", 	(void*)GetDeviceStatus},
    { "setVideoConfig", "(I)Z", 	(void*)setVideoConfig},
    { "setAudioConfig", "(I)Z",   	(void*)setAudioConfig},
    { "hdmiPowerEnable", "(Z)Z", (void*)hdmiPowerEnable},
    { "hdmiPortraitEnable", "(Z)Z", (void*)hdmiPortraitEnable},
    { "ishdmiForceAwake", "()Z", (void *)ishdmiForceAwake},
};

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("GetEnv failed!");
        return result;
    }
    LOG_ASSERT(env, "[HDMI] Could not retrieve the env!");

    int ret = jniRegisterNativeMethods(
        env, "com/mediatek/hdmi/HDMINative", gNotify, NELEM(gNotify));

    if (ret) {
        LOGE("[HDMI] call jniRegisterNativeMethods() failed, ret:%d\n", ret);
    }
    
    return JNI_VERSION_1_4;
}

#ifdef __cplusplus
    }
#endif

