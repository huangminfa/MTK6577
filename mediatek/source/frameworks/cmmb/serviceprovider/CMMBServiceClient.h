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

#ifndef ANDROID_CMMBSEVICECLIENT_H
#define ANDROID_CMMBSEVICECLIENT_H
#include <utils/threads.h>
#include "ICmmbSp.h"
#include "ICmmbSpDeathNotifier.h"
#include "CmmbUamPlugin.h"
namespace android {
enum clientState{
	STATE_UNKNOWN 		= 0,
	STATE_INIT    		= 1 << 0,
	STATE_READY			= 1 << 1,
	STATE_RUNNING		= 1 << 2,
	STATE_TERMINATED    = 1 << 3
};

enum error_typeinfo{
	CMMB_ERR_UNKNOWN 	= 300,
	CMMB_ERR_INIT		= 301,
	CMMB_ERR_TERM  		= 302,
	CMMB_ERR_TUNE		= 303,
	CMMB_ERR_STARTSERVICE = 304,
	CMMB_ERR_STOPSERVICE = 305,
	CMMB_ERR_SETSALT 	= 306,
	CMMB_ERR_UAM		= 307,
	CMMB_ERR_DIED		= 308
};
enum client_event{
	CMMB_ERROR 		= 	1,
	CMMB_INFO		=   2,
	CMMB_EBM		=	3,
	CMMB_MSK		= 	4
};
class CMMBServiceClientListener : virtual public RefBase{
public:
	virtual void notify(int msg, int ext1, int ext2, void* object, long length) = 0;
};
class CMMBServiceClient : virtual public RefBase,
						  virtual public ICmmbSpDeathNotifier{
private:
	clientState mState;
	sp<ICmmbSp> mCMMBSp;
	sp<CMMBServiceClientListener> mListener;
	sp<ICmmbSpObserver> mObserver;
	sp<CmmbUamPlugin> mUamPlugin;
	uint32_t mServiceHandle;
	Mutex mLock;
	uint32_t mCurrentFrequency;
	Mutex mNotifyLock;
	bool  mStoppingFlag;
public:
	CMMBServiceClient();
	~CMMBServiceClient();
	void notify(int msg, int ext1 , int ext2 , void* content , int length);
	status_t initCMMB(int simType, bool initUam,bool zeroSpiBuf);
	status_t terminate();
	status_t CMMBTune(uint32_t frequencyNo, bool isAutoScan);
	status_t startService(uint32_t serviceID, uint16_t caSystemID, uint32_t videoKILen, uint32_t audioKILen, bool saveMfsFile, uint32_t dataServceID);
	status_t stopService();
	status_t setClientListener(const sp<CMMBServiceClientListener>& listener);
	bool setCaSaltKeys(
							uint8_t videoSalt[CMMB_CA_SALT_SIZE],
							uint8_t audioSalt[CMMB_CA_SALT_SIZE],
							uint8_t dataSalt[CMMB_CA_SALT_SIZE]
						);

	bool GetESGFile(char filePath[CMMB_FILE_PATH_MAX]);
	bool GetCmmbSn(uint8_t cmmbsn[UAM_CMMBSN_LEN]);
	bool GetVersion (uint8_t version[UAM_USER_AUTH_VERION_LEN]);
	bool IsGBANeeded ();
	bool GetBTID (uint8_t btid[UAM_BTID_LEN]);
	bool SaveBTID (uint8_t btid[UAM_BTID_LEN],uint8_t ks[UAM_KS_LEN]);
	bool SaveMSK (uint8_t msk[UAM_MSK_LEN], uint8_t verMsg[UAM_VER_MSG_LEN]);
	bool IsMSKValid (uint8_t domainid[UAM_DOMAIN_ID_LEN], uint8_t mskid[UAM_MSK_ID_LEN]);
	bool GetControlWords (uint8_t* mtk, uint8_t cw[UAM_CW_LEN]);
	bool GetMRK (uint8_t nafid[UAM_NAF_ID_LEN], uint8_t impi[UAM_IMPI_LEN],uint8_t mrk[UAM_MRK_LEN]);
	bool G2Authenticate (TCmmbUam2GAuthReq req, TCmmbUamAuthRsp& rsp);
	bool G3Authenticate (TCmmbUam3GAuthReq req, TCmmbUamAuthRsp& rsp);
	bool getProp(int keyId, int& value);
	bool getServiceFrequency(uint32_t frequency);
	bool autoScan();
	bool modeSwitch(int simType, bool isSGMode);
	void died();
};

class CMMBServiceObserver: public BnCmmbSpObserver{
public:
	void HandleCmmbEvent(UINT32 eventId, UINT8* payload, UINT32 length);
	CMMBServiceObserver(const sp<CMMBServiceClient>& client);
private:
	sp<CMMBServiceClient> mClient;
};
};
#endif
