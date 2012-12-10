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

#define LOG_TAG "MHAL_JNI"

#include <jni.h>

#include <utils/Log.h>
#include <cutils/xlog.h>
#include <utils/threads.h>

#include "MediaHal.h"
#include "pipe_types.h"
#include "display_ispif_mt6575.h"
#include "isp_reg.h"
#include "isp_drv.h"
#include "isp_sysram_drv.h"
#include "display_isp_tuning_if.h"

using namespace NSDisplayIspTuning;
using namespace android;
DisplayIspTuningIFBase * g_pISPTuning = NULL;
static jboolean getISPInstance(void)
{
    if(NULL == g_pISPTuning)
    {
        g_pISPTuning = DisplayIspTuningIFBase::createInstance();

        if(NULL == g_pISPTuning)
        {

            XLOGE("Fail to get isp tuning interface base");

            return JNI_FALSE;
        }

        g_pISPTuning->init();

     }

    return JNI_TRUE;
}

typedef enum {
    PQ_SkinTone = 0,
    PQ_GrassTone,
    PQ_SkyTone,
    PQ_Color,
    PQ_Sharpness,
    PQ_TuningDimension
} PQ_TuningIndex_t;

const static jint g_u4Range[PQ_TuningDimension] = {DISPLAY_ISP_PCA_SKIN_TBL_NUM , DISPLAY_ISP_PCA_GRASS_TBL_NUM , 
        DISPLAY_ISP_PCA_SKY_TBL_NUM , DISPLAY_ISP_YCCGO_TBL_NUM , DISPLAY_ISP_PRZ_TBL_NUM};

static jint getRange(PQ_TuningIndex_t a_eIndex)
{
    return g_u4Range[a_eIndex];
}

static int getIndex(PQ_TuningIndex_t a_eIndex)
{
    unsigned int u4Indics[PQ_TuningDimension];

    if(JNI_TRUE != getISPInstance())
    {
        XLOGE("Fail to get ISP tuning interface when get Index");

        return -1;
    }

    g_pISPTuning->getISPParamIndex(u4Indics[0] , u4Indics[1] , u4Indics[2] , u4Indics[3] , u4Indics[4]);

    return (int)u4Indics[a_eIndex];
}

static jboolean setIndex(PQ_TuningIndex_t a_eIndex , unsigned int a_u4Index)
{
    unsigned int u4Indics[PQ_TuningDimension];

    if(JNI_TRUE != getISPInstance())
    {
        XLOGE("Fail to get ISP tuning interface when get Index");

        return JNI_FALSE;
    }

    g_pISPTuning->getISPParamIndex(u4Indics[0] , u4Indics[1] , u4Indics[2] , u4Indics[3] , u4Indics[4]);

//Add for log
//XLOGE("Get PQIndex %d,%d,%d,%d,%d Set Index %d to %d" , u4Indics[0] , u4Indics[1] , u4Indics[2] , u4Indics[3] , u4Indics[4] , a_eIndex , a_u4Index);

    u4Indics[a_eIndex] = a_u4Index;

    if(MHAL_NO_ERROR != g_pISPTuning->setISPParamIndex(u4Indics[0] , u4Indics[1] , u4Indics[2] , u4Indics[3] , u4Indics[4]))
    {
        XLOGE("Fail to set ISP tuning interface when get Index");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static jint getSkinToneRange(JNIEnv *env, jobject thiz)
{
    return getRange(PQ_SkinTone);
}

static jint getSkinToneIndex(JNIEnv *env, jobject thiz)
{
    return getIndex(PQ_SkinTone);
}

static jboolean setSkinToneIndex(JNIEnv *env, jobject thiz , int index)
{
    return setIndex(PQ_SkinTone , index);
}

static jint getGrassToneRange(JNIEnv *env, jobject thiz)
{
    return getRange(PQ_GrassTone);
}

static jint getGrassToneIndex(JNIEnv *env, jobject thiz)
{
    return getIndex(PQ_GrassTone);
}

static jboolean setGrassToneIndex(JNIEnv *env, jobject thiz , int index)
{
    return setIndex(PQ_GrassTone , index);
}

static jint getSkyToneRange(JNIEnv *env, jobject thiz)
{
    return getRange(PQ_SkyTone);
}

static jint getSkyToneIndex(JNIEnv *env, jobject thiz)
{
    return getIndex(PQ_SkyTone);
}

static jboolean setSkyToneIndex(JNIEnv *env, jobject thiz , int index)
{
    return setIndex(PQ_SkyTone , index);
}

static jint getColorRange(JNIEnv *env, jobject thiz)
{
    return getRange(PQ_Color);
}

static jint getColorIndex(JNIEnv *env, jobject thiz)
{
    return getIndex(PQ_Color);
}

static jboolean setColorIndex(JNIEnv *env, jobject thiz , int index)
{
    return setIndex(PQ_Color , index);
}

static jint getSharpnessRange(JNIEnv *env, jobject thiz)
{
    return getRange(PQ_Sharpness);
}

static jint getSharpnessIndex(JNIEnv *env, jobject thiz)
{
    return getIndex(PQ_Sharpness);
}

static jboolean setSharpnessIndex(JNIEnv *env, jobject thiz , int index)
{
    return setIndex(PQ_Sharpness , index);
}

//JNI register
////////////////////////////////////////////////////////////////
static const char *classPathName = "com/android/pqtuningtool/pqjni/PictureQualityJni";

static JNINativeMethod g_methods[] = {
  {"nativeGetSkinToneRange",  "()I", (void*)getSkinToneRange },
  {"nativeGetSkinToneIndex",  "()I", (void*)getSkinToneIndex },
  {"nativeSetSkinToneIndex",  "(I)Z", (void*)setSkinToneIndex },
  {"nativeGetGrassToneRange",  "()I", (void*)getGrassToneRange },
  {"nativeGetGrassToneIndex",  "()I", (void*)getGrassToneIndex },
  {"nativeSetGrassToneIndex",  "(I)Z", (void*)setGrassToneIndex },
  {"nativeGetSkyToneRange",  "()I", (void*)getSkyToneRange },
  {"nativeGetSkyToneIndex",  "()I", (void*)getSkyToneIndex },
  {"nativeSetSkyToneIndex",  "(I)Z", (void*)setSkyToneIndex },
  {"nativeGetColorRange",  "()I", (void*)getColorRange },
  {"nativeGetColorIndex",  "()I", (void*)getColorIndex },
  {"nativeSetColorIndex",  "(I)Z", (void*)setColorIndex },
  {"nativeGetSharpnessRange",  "()I", (void*)getSharpnessRange },
  {"nativeGetSharpnessIndex",  "()I", (void*)getSharpnessIndex },
  {"nativeSetSharpnessIndex",  "(I)Z", (void*)setSharpnessIndex }
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        XLOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        XLOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

// ----------------------------------------------------------------------------

/*
 * This is called by the VM when the shared library is first loaded.
 */
 
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;
    
    LOGI("JNI_OnLoad");
    
    if (JNI_OK != vm->GetEnv((void **)&env, JNI_VERSION_1_4)) {
        XLOGE("ERROR: GetEnv failed");
        goto bail;
    }

    if (!registerNativeMethods(env, classPathName, g_methods, sizeof(g_methods) / sizeof(g_methods[0]))) {
        XLOGE("ERROR: registerNatives failed");
        goto bail;
    }
    
    result = JNI_VERSION_1_4;
    
bail:
    return result;
}

