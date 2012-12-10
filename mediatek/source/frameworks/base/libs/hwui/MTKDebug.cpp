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

#define LOG_TAG "OpenGLRenderer"

#include <cutils/xlog.h>
#include <cutils/properties.h>
#include <GLES2/gl2.h>
#include <SkImageEncoder.h>
#include "MTKDebug.h"

#if defined (MTK_DEBUG_RENDERER)

static bool getProcessName(char* psProcessName, int size)
{
    FILE *f;
    char *slash;

    if (!psProcessName)
	return false;

    f = fopen("/proc/self/cmdline", "r");
    if (!f)
    {
        XLOGE("Can't get application name");
        return false;
    }

    if (fgets(psProcessName, size, f) == NULL)
    {
        XLOGE("ame : fgets failed");
        fclose(f);
        return false;
    }

    fclose(f);

    if ((slash = strrchr(psProcessName, '/')) != NULL)
    {
        memmove(psProcessName, slash+1, strlen(slash));
    }

    return true;
}

static bool dumpImage(int width, int height, const char *filename)
{
    size_t size = width * height * 4;
    GLbyte *buf = (GLbyte*)malloc(size);
    GLenum error;
    bool bRet = true;

    if(!buf)
    {
        XLOGE("dumpFrame: failed to allocate buffer (%d bytes)\n", size);
        return false;
    }

    SkBitmap bitmap;
    bitmap.setConfig(SkBitmap::kARGB_8888_Config, width, height);
    bitmap.setPixels(buf, NULL);

    XLOGI("dumpFrame: %dx%d %s\n", width, height, filename);
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, bitmap.getPixels());

    if ((error = glGetError()) != GL_NO_ERROR)
    {
        XLOGE("dumpFrame: get GL error 0x%x \n", error);
        bRet = false;
        goto Exit;
    }

    if (!SkImageEncoder::EncodeFile(filename, bitmap, SkImageEncoder::kPNG_Type, 100))
    {
        XLOGE("dumpFrame: Failed to encode image %s\n", filename);
        bRet = false;
        goto Exit;
    }

Exit:
    free(buf);
    return bRet;
}

bool dumpDisplayList(int width, int height, int level)
{
    static int frame = 0;
    static int count = 0;
    char procName[256];
    char file[512];
    char value[PROPERTY_VALUE_MAX];

    if (!getProcessName(procName, sizeof(procName)))
	return false;

    property_get("debug.hwui.dump.displaylist", value, "");
    if(strcmp(procName, value) != 0)
	return false;

    if (level == 0)
    {
	count = 0;
	frame++;
    }
    sprintf(file, "/data/data/%s/dp_%04d_%04d.png", procName, frame, count++);
    return dumpImage(width, height, file);
}

bool dumpDraw(int width, int height, int level)
{
    static int frame = 0;
    static int count = 0;
    char procName[256];
    char file[512];
    char value[PROPERTY_VALUE_MAX];

    if (!getProcessName(procName, sizeof(procName)))
        return false;

    property_get("debug.hwui.dump.draw", value, "");
    if(strcmp(procName, value) != 0)
        return false;

    if (level == 0)
    {
        count = 0;
        frame++;
    }
    sprintf(file, "/data/data/%s/draw_%04d_%04d.png", procName, frame, count++);
    return dumpImage(width, height, file);
}

bool dumpTexture(int texture, int width, int height, SkBitmap *bitmap)
{
    char procName[256];
    char file[512];
    char value[PROPERTY_VALUE_MAX];

    if (!getProcessName(procName, sizeof(procName)))
        return false;

    property_get("debug.hwui.dump.tex", value, "");
    if(strcmp(procName, value) != 0)
        return false;

    sprintf(file, "/data/data/%s/tex_%d_%d_%d_%p.png", procName, texture, width, height, bitmap);
    if (!SkImageEncoder::EncodeFile(file, *bitmap, SkImageEncoder::kPNG_Type, 100))
    {
	XLOGE("Fail to dump texture: %s", file);
	return false;
    }
    else
    {    
	XLOGI("Dump texture(%dx%d): %s", width, height, file);
	return true;
    }
}

bool dumpAlphaTexture(int width, int height, uint8_t *data, const char *prefix)
{
    static int count = 0;
    char procName[256];
    char file[512];
    SkBitmap bitmap;
    SkBitmap bitmapCopy;

    if (!getProcessName(procName, sizeof(procName)))
        return false;

    sprintf(file, "/data/data/%s/%s_%04d.png", procName, prefix, count++);
    XLOGI("dumpAlphaTexture: %dx%d %s\n", width, height, file);

    bitmap.setConfig(SkBitmap::kA8_Config, width, height);
    bitmap.setPixels(data, NULL);

    if (!bitmap.copyTo(&bitmapCopy, SkBitmap::kARGB_8888_Config))
    {
	XLOGD("dumpAlphaTexture: Failed to copy data");
	return false;    	
    }

    if (!SkImageEncoder::EncodeFile(file, bitmapCopy, SkImageEncoder::kPNG_Type, 100))
    {
        XLOGE("dumpAlphaTexture: Failed to encode image %s\n", file);
        return false;
    }

    return true;
}

#endif
