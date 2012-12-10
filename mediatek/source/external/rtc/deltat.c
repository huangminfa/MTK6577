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

#include <stdio.h>
#include <pthread.h>

#define LOG_TAG		"DeltaT"
#include <cutils/log.h>

#define DELTAT_FPATH	"/data/misc/rtc/deltat"

static pthread_mutex_t deltat_mutex = PTHREAD_MUTEX_INITIALIZER;

int deltat_write(long diff_sec)
{
	long cnt, deltat;
	int r = 0;
	FILE *fp;

	pthread_mutex_lock(&deltat_mutex);
	fp = fopen(DELTAT_FPATH, "r+b");
	if (!fp) {
		fp = fopen(DELTAT_FPATH, "w+b");
		if (!fp) {
			LOGE("create %s failed\n", DELTAT_FPATH);
			r = -2;
			goto unlock;
		}
	}

	cnt = fread(&deltat, sizeof(long), 1, fp);
	if (cnt != 1) {
		deltat = 0;
		//LOGD("read deltat failed, use deltat = 0\n");
	}

	//LOGD("deltat = %ld, diff_sec = %ld\n", deltat, diff_sec);
	deltat += diff_sec;
	//LOGD("new deltat = %ld\n", deltat);

	rewind(fp);
	cnt = fwrite(&deltat, sizeof(long), 1, fp);
	if (cnt != 1) {
		LOGE("write deltat failed\n");
		r = -1;
	}

unlock:
	if (fp)
		fclose(fp);
	pthread_mutex_unlock(&deltat_mutex);
	return r;
}

int deltat_read_clear(long *diff_sec)
{
	long cnt, deltat;
	int r = 0;
	FILE *fp;

	pthread_mutex_lock(&deltat_mutex);
	fp = fopen(DELTAT_FPATH, "r+b");
	if (!fp) {
		fp = fopen(DELTAT_FPATH, "w+b");
		if (!fp) {
			LOGE("create %s failed\n", DELTAT_FPATH);
			r = -2;
			goto unlock;
		}
	}

	cnt = fread(&deltat, sizeof(long), 1, fp);
	if (cnt != 1) {
		deltat = 0;
		//LOGD("read deltat failed, use deltat = 0\n");
	}

	//LOGD("deltat = %ld\n", deltat);
	if (diff_sec)
		*diff_sec = deltat;
	deltat = 0;
	//LOGD("new deltat = %ld\n", deltat);

	rewind(fp);
	cnt = fwrite(&deltat, sizeof(long), 1, fp);
	if (cnt != 1) {
		LOGE("write deltat failed\n");
		r = -1;
	}

unlock:
	if (fp)
		fclose(fp);
	pthread_mutex_unlock(&deltat_mutex);
	return r;
}
