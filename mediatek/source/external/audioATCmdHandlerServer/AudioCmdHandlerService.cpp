/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2009
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/


/*******************************************************************************
 *
 * Filename:
 * ---------
 * AudioCmdHandler.cpp
 *
 * Project:
 * --------
 *   Android
 *
 * Description:
 * ------------
 *   This file implements the handling about audio command comming from AT Command Service.
 *
 * Author:
 * -------
 *   Donglei Ji (mtk80823)
 *
 *------------------------------------------------------------------------------
 * $Revision: #1 $
 * $Modtime:$
 * $Log:$
 *
 * 04 27 2012 donglei.ji
 * [ALPS00247341] [Need Patch] [Volunteer Patch] AT Command for Speech Tuning Tool feature
 * add two case:
 * 1. get phone support info
 * 2. read dual mic parameters if phone support dual mic
 *
 * 04 20 2012 donglei.ji
 * [ALPS00272538] [Need Patch] [Volunteer Patch] AT Command for ACF/HCF calibration
 * ACF/HCF calibration feature.
 *
 * 04 12 2012 donglei.ji
 * [ALPS00247341] [Need Patch] [Volunteer Patch] AT Command for Speech Tuning Tool feature
 * Add wide band speech tuning.
 *
 * 03 16 2012 donglei.ji
 * [ALPS00247341] [Need Patch] [Volunteer Patch] AT Command for Speech Tuning Tool feature
 * add speech parameters encode and decode for transfer.
 *
 * 03 09 2012 donglei.ji
 * [ALPS00247341] [Need Patch] [Volunteer Patch] AT Command for Speech Tuning Tool feature
 * add a set parameters setting.
 *
 * 03 06 2012 donglei.ji
 * [ALPS00247341] [Need Patch] [Volunteer Patch] AT Command for Speech Tuning Tool feature
 * AT Command for Speech Tuning tool feature check in.
 *
 * 12 27 2011 donglei.ji
 * [ALPS00107090] [Need Patch] [Volunteer Patch][ICS Migration] MM Command Handler Service Migration
 * MM Command Handler Service check in.
 *
 * 11 21 2011 donglei.ji
 * [ALPS00094843] [Need Patch] [Volunteer Patch] XLOG enhance
 * log enhance -- SXLOG.
 *
 * 07 15 2011 donglei.ji
 * [ALPS00053673] [Need Patch] [Volunteer Patch][Audio HQA]Add Audio HQA test cases
 * check in HQA code.
 *
 * 06 15 2011 donglei.ji
 * [ALPS00053673] [Need Patch] [Volunteer Patch][Audio HQA]Add Audio HQA test cases
 * MM Cmd Handler code check in for MT6575.
 *
 *******************************************************************************/

/*=============================================================================
 *                              Include Files
 *===========================================================================*/

#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <cutils/xlog.h>
#include <utils/String8.h>
#include <sys/socket.h>
#include <cutils/sockets.h>
#include <netinet/in.h>
#include <pthread.h>

#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>


#include "AudioCmdHandler.h"
#include "TvoutCmdHandler.h"


#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "AudioCmdHandlerService"

#ifdef ENABLE_XLOG
#undef LOGV
#undef LOGD
#undef LOGE

#define LOGV SXLOGV
#define LOGD SXLOGD
#define LOGE SXLOGE
#endif

#define SOCKET_NAME_AUDIO "atci-audio"
#define MAX_BUFSIZE_RECV 4*1024
#define MAX_CMD_LEN 128

static int s_fdAudioCmdHdlSrv_listen  = -1;
static int s_fdAudioCmdHdlSrv_command = -1;

using namespace android;
typedef enum{
	UNKNOWN_CMD = -1,
	RDPRM_CMD,
	RDMIC_CMD,
	PGAGAIN_CMD,
	RDSTP_CMD,
	FMREC_CMD,
	FMPLY_CMD,
	VDPG_CMD,
	AUDLINEPG_CMD,
	FMVUPG_CMD,
	MICVUPG_CMD,
	MEDPLY_CMD,
	MEDSTOP_CMD,
	I2SREC_CMD,
	I2SRECSTP_CMD,
	REGWRITE_CMD,
	REGREAD_CMD,
	//<--- Add for mt6575 HQA
	I2SPLY_CMD,
	I2SPLYSTP_CMD,
	MODESEL_CMD,
	AMPEN_CMD,
	AMPVOL_CMD,
	RECEIVER_CMD,
	RECGAIN_CMD,
	//--->Add for mt6575 
    /* Add for TV out HQA -- below*/
    TVOPEN_CMD,
    TVOPEND_CMD,
    TVSYS_CMD,
    TVSHOWPAT_CMD,
    TVLEAVEPAT_CMD,
    /*Add for TV out --up*/
	//<---Add for speech parameters calibration
	EADP_CMD,
	EAPS_CMD,
	EACF_CMD,
	EHCF_CMD,
	EDUALMIC_CMD,
	GPSI_CMD,
	SLBK_CMD,
	//--->Add for speech tuning tool
    AUDISC_CMD
}AudioCmdType;

//********* function declaration********************//
static void sendResponseToATCI(const char *pSendData, int length);


//********* function definition************************//
static void DoSetRecorderParam(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHParamError;
	if ((pAudioHandle!=NULL) && (audioCmdParams.recvDataLen>=strlen("AUD+RDPRM=1,1,1")))
		ret = pAudioHandle->setRecorderParam(audioCmdParams);

	if (ret!=ACHSucceeded) {
		sendResponseToATCI("AUD+RDPRM=Failed,params error",strlen("AUD+RDPRM=Failed,params error"));
	} else {
		sendResponseToATCI("AUD+RDPRM=Succeeded",strlen("AUD+RDPRM=Succeeded"));
	}
}

static void DoStartRecorderFrMIC(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	if (audioCmdParams.recvDataLen>=strlen("AUD+RDMIC=1,1,1") && pAudioHandle!=NULL)
		ret = pAudioHandle->startRecorderFrMIC(audioCmdParams);

	if (ret==ACHSucceeded) {
		sendResponseToATCI("AUD+RDMIC=Succeeded",strlen("AUD+RDMIC=Succeeded"));
	} else {
		sendResponseToATCI("AUD+RDMIC=Failed",strlen("AUD+RDMIC=Failed"));
	}
}


static void DoStopRecorderFrMIC(AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	if (pAudioHandle!=NULL)
		ret = pAudioHandle->stopRecorder();

	if (ret==ACHSucceeded) {
		sendResponseToATCI("AUD+RDSTP=Succeeded",strlen("AUD+RDSTP=Succeeded"));
	} else {
		sendResponseToATCI("AUD+RDSTP=Failed",strlen("AUD+RDSTP=Failed"));
	}
}

static void DoSetMICGain(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHParamError;
	if (audioCmdParams.recvDataLen>=strlen("AUD+PGAGAIN=1,1") && pAudioHandle!=NULL)
		ret = pAudioHandle->setMICGain(audioCmdParams);

	if (ret!=ACHSucceeded) {
		sendResponseToATCI("AUD+PGAGAIN=Failed,params error",strlen("AUD+PGAGAIN=Failed,params error"));
	} else {
		sendResponseToATCI("AUD+PGAGAIN=Succeeded",strlen("AUD+PGAGAIN=Succeeded"));
	}
}

static void DoRecordFrFM(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;	
	if (audioCmdParams.recvDataLen>=strlen("AUD+FMREC=1,1,1") && pAudioHandle!=NULL) {
		ret = pAudioHandle->startRecorderFrFM(audioCmdParams);
	} else if((audioCmdParams.recvDataLen==strlen("AUD+FMREC=1") && audioCmdParams.param1==0 && pAudioHandle!=NULL)) {
		ret = pAudioHandle->stopRecorder();
	}

	if (ret==ACHSucceeded) {
		sendResponseToATCI("AUD+FMREC=Succeeded",strlen("AUD+FMREC=Succeeded"));
	} else {
		sendResponseToATCI("AUD+FMREC=Failed",strlen("AUD+FMREC=Failed"));
	}
}

static void DoPlayFM(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	if (audioCmdParams.recvDataLen>=strlen("AUD+FMPLY=1") && pAudioHandle!=NULL) {
		if (audioCmdParams.param1==1) {
			ret = pAudioHandle->playFM();
		} else if (audioCmdParams.param1==0) {
			ret = pAudioHandle->stopPlayingFM();
		}
	}

	if (ret==ACHSucceeded) {
		sendResponseToATCI("AUD+FMPLY=Succeeded",strlen("AUD+FMPLY=Succeeded"));
	} else {
		sendResponseToATCI("AUD+FMPLY=Failed",strlen("AUD+FMPLY=Failed"));
	}
}

static void DoSetVDPG(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHParamError;
	if (audioCmdParams.recvDataLen>=strlen("AUD+VDPG=1,1,1") && pAudioHandle!=NULL)
		ret = pAudioHandle->setVDPG(audioCmdParams);

	if (ret!=ACHSucceeded) {
		sendResponseToATCI("AUD+VDPG=Failed,params error",strlen("AUD+VDPG=Failed,params error"));
	} else {
		sendResponseToATCI("AUD+VDPG=Succeeded",strlen("AUD+VDPG=Succeeded"));
	}
}

static void DoSetAUDLINEPG(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHParamError;
	if (audioCmdParams.recvDataLen>=strlen("AUD+AUDLINEPG=1") && pAudioHandle!=NULL)
		ret = pAudioHandle->setAUDLINEPG(audioCmdParams);

	if (ret!=ACHSucceeded) {
		sendResponseToATCI("AUD+AUDLINEPG=Failed,params error",strlen("AUD+AUDLINEPG=Failed,params error"));
	} else {
		sendResponseToATCI("AUD+AUDLINEPG=Succeeded",strlen("AUD+AUDLINEPG=Succeeded"));
	}
}

static void DoSetFMVUPG(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHParamError;
	if (audioCmdParams.recvDataLen>=strlen("AUD+FMVUPG=1,1") && pAudioHandle!=NULL)
		ret = pAudioHandle->setFMorMICVUPG(audioCmdParams, true);

	if (ret!=ACHSucceeded) {
		sendResponseToATCI("AUD+FMVUPG=Failed,params error",strlen("AUD+FMVUPG=Failed,params error"));
	} else {
		sendResponseToATCI("AUD+FMVUPG=Succeeded",strlen("AUD+FMVUPG=Succeeded"));
	}
}

static void DoSetMICVUPG(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHParamError;
	if (audioCmdParams.recvDataLen>=strlen("AUD+MICVUPG=1,1") && pAudioHandle!=NULL)
		ret = pAudioHandle->setFMorMICVUPG(audioCmdParams, false);

	if (ret!=ACHSucceeded) {
		sendResponseToATCI("AUD+MICVUPG=Failed,params error",strlen("AUD+MICVUPG=Failed,params error"));
	} else {
		sendResponseToATCI("AUD+MICVUPG=Succeeded",strlen("AUD+MICVUPG=Succeeded"));
	}
}
	
static void DoStartAudioPlayer(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	if ((pAudioHandle!=NULL) && (strlen("AUD+MEDPLY=1,1,1")<=audioCmdParams.recvDataLen)) {
		ret = pAudioHandle->startAudioPlayer(audioCmdParams);
	}

	if (ret==ACHSucceeded) {
		sendResponseToATCI("AUD+MEDPLY=Succeeded",strlen("AUD+MEDPLY=Succeeded"));
	} else {
		sendResponseToATCI("AUD+MEDPLY=Failed",strlen("AUD+MEDPLY=Failed"));
	}
}
static void DoStopAudioPlayer(AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	if (pAudioHandle!=NULL)
		ret = pAudioHandle->stopAudioPlayer();

	if (ret==ACHSucceeded) {
		sendResponseToATCI("AUD+MEDSTOP=Succeeded",strlen("AUD+MEDSTOP=Succeeded"));
	} else {
		sendResponseToATCI("AUD+MEDSTOP=Failed",strlen("AUD+MEDSTOP=Failed"));
	}
}
static void DoStartRecorderFrI2S(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	if (audioCmdParams.recvDataLen>=strlen("AUD+I2SREC=1,1,1,1") && pAudioHandle!=NULL)
		ret = pAudioHandle->startRecorderFrI2S(audioCmdParams);

	if (ret==ACHSucceeded) {
		sendResponseToATCI("AUD+I2SREC=Succeeded",strlen("AUD+I2SREC=Succeeded"));
	} else {
		sendResponseToATCI("AUD+I2SREC=Failed",strlen("AUD+I2SREC=Failed"));
	}
}

static void DoStopRecorderFrI2S(AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	if (pAudioHandle!=NULL)
		ret = pAudioHandle->stopRecorder();

	if (ret==ACHSucceeded) {
		sendResponseToATCI("AUD+I2SRECSTP=Succeeded",strlen("AUD+I2SRECSTP=Succeeded"));
	} else {
		sendResponseToATCI("AUD+I2SRECSTP=Failed",strlen("AUD+I2SRECSTP=Failed"));
	}
}

static void DoWriteRegister(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHParamError;
	if (audioCmdParams.recvDataLen>=strlen("AUD+REGWRITE=1,1,1") && pAudioHandle!=NULL)
		ret = pAudioHandle->writeRegister(audioCmdParams);

	if (ret!=ACHSucceeded) {
		sendResponseToATCI("AUD+REGWRITE=Failed,params error",strlen("AUD+REGWRITE=Failed,params error"));
	} else {
		sendResponseToATCI("AUD+REGWRITE=Succeeded",strlen("AUD+REGWRITE=Succeeded"));
	}
}

static void DoReadRegister(AudioCmdParam &audioCmdParams, AudioCmdHandler *pAudioHandle)
{
	String8 returnValue = String8("");	
	char responceValue[MAX_BUFSIZE_RECV];

	if (audioCmdParams.recvDataLen>=strlen("AUD+REGREAD=1,1") && pAudioHandle!=NULL)
		returnValue = pAudioHandle->readRegister(audioCmdParams);

	sprintf(responceValue, "AUD+REGREAD=%s", returnValue.string());
	sendResponseToATCI(responceValue,strlen(responceValue));
}

//<--- for speech parameters calibration
static void DoCustSPHParam(AudioCmdParam &audioCmdParams, void *pParam, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	int dataLen = 0;
	int strLen = sizeof("MM+EAPS=0,0,0");
	void *pSpeechParam = NULL;

	if (pAudioHandle==NULL||pParam==NULL)
		return;

	// extract command string
	char responseStr[32];

	if (audioCmdParams.param1==0){
		strncpy(responseStr, (char *)pParam, strLen-1);
		responseStr[strLen-1] = '\0';
	
		pSpeechParam = malloc(MAX_BUFSIZE_RECV);
		strcpy((char *)pSpeechParam, responseStr);

		if (audioCmdParams.param2==0) {
			ret = pAudioHandle->ULCustSPHParamFromNV(pSpeechParam+strLen, &dataLen, audioCmdParams.param3);
		}else if(audioCmdParams.param2==1) {
			ret = pAudioHandle->ULCustSPHWBParamFromNV(pSpeechParam+strLen, &dataLen, audioCmdParams.param3);
		}

		if (ret==ACHSucceeded) {
			sendResponseToATCI((char *)pSpeechParam,strLen+dataLen);
		}else {
			strcat(responseStr, " Fail");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}

		free(pSpeechParam);
	} else {
		strncpy(responseStr, (char *)pParam, strLen-1);
		responseStr[strLen-1] = '\0';
			
		pSpeechParam = pParam + strLen;
		if (audioCmdParams.param2==0) {
			ret = pAudioHandle->DLCustSPHParamToNV(pSpeechParam, audioCmdParams.param3);
		}else if(audioCmdParams.param2==1) {
			ret = pAudioHandle->DLCustSPHWBParamToNV(pSpeechParam, audioCmdParams.param3);
		}

		if (ret==ACHSucceeded) {
			strcat(responseStr, " OK");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}else {
			strcat(responseStr, " Fail");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}
	}
}

static void DoCustSPHVolumeParam(AudioCmdParam &audioCmdParams, void *pParam, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	int dataLen = 0;
	int strLen = sizeof("MM+EADP=0");
	void *pSPHVolumeParam = NULL;

	if (pAudioHandle==NULL||pParam==NULL)
		return;

	// extract command string
	char responseStr[32];
	strncpy(responseStr, (char *)pParam, strLen-1);
	responseStr[strLen-1] = '\0';

	if (audioCmdParams.param1==0){
		pSPHVolumeParam = malloc(MAX_BUFSIZE_RECV);
		strcpy((char *)pSPHVolumeParam, responseStr);

		ret = pAudioHandle->ULCustSPHVolumeParamFromNV(pSPHVolumeParam+strLen, &dataLen);

		if (ret==ACHSucceeded) {
			sendResponseToATCI((char *)pSPHVolumeParam,strLen+dataLen);
		}else {
			strcat(responseStr, " Fail");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}

		free(pSPHVolumeParam);
	} else {	
		pSPHVolumeParam = pParam + strLen;
		ret = pAudioHandle->DLCustSPHVolumeParamToNV(pSPHVolumeParam);

		if (ret==ACHSucceeded) {
			strcat(responseStr, " OK");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}else {
			strcat(responseStr, " Fail");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}
	}
}

static void DoCustACFHCFParam(AudioCmdParam &audioCmdParams, void *pParam, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	int dataLen = 0;
	int strLen = sizeof("MM+EACF=0");
	void *pACFHCFParam = NULL;

	if (pAudioHandle==NULL||pParam==NULL)
		return;

	// extract command string
	char responseStr[32];

	if (audioCmdParams.param1==0){
		strncpy(responseStr, (char *)pParam, strLen-1);
		responseStr[strLen-1] = '\0';
	
		pACFHCFParam = malloc(MAX_BUFSIZE_RECV);
		strcpy((char *)pACFHCFParam, responseStr);

		if (audioCmdParams.param2==0) {
			ret = pAudioHandle->ULCustACFParamFromNV(pACFHCFParam+strLen, &dataLen);
		}else if(audioCmdParams.param2==1) {
			ret = pAudioHandle->ULCustHCFParamFromNV(pACFHCFParam+strLen, &dataLen);
		}

		if (ret==ACHSucceeded) {
			sendResponseToATCI((char *)pACFHCFParam, strLen+dataLen);
		}else {
			strcat(responseStr, " Fail");
			sendResponseToATCI(responseStr, strlen(responseStr));
		}

		free(pACFHCFParam);
	} else {
		strncpy(responseStr, (char *)pParam, strLen-1);
		responseStr[strLen-1] = '\0';
			
		pACFHCFParam = pParam + strLen;
		
		if (audioCmdParams.param2==0) {
			ret = pAudioHandle->DLCustACFParamToNV(pACFHCFParam);
		}else if(audioCmdParams.param2==1) {
			ret = pAudioHandle->DLCustHCFParamToNV(pACFHCFParam);
		}

		if (ret==ACHSucceeded) {
			strcat(responseStr, " OK");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}else {
			strcat(responseStr, " Fail");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}
	}
}

static void DoCustDUALMICParam(AudioCmdParam &audioCmdParams, void *pParam, AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	int dataLen = 0;
	int strLen = sizeof("MM+EDUALMIC=0");
	void *pDUALMICParam = NULL;

	if (pAudioHandle==NULL||pParam==NULL)
		return;

	// extract command string
	char responseStr[32];

	if (audioCmdParams.param1==0){
		strncpy(responseStr, (char *)pParam, strLen-1);
		responseStr[strLen-1] = '\0';
	
		pDUALMICParam = malloc(MAX_BUFSIZE_RECV);
		strcpy((char *)pDUALMICParam, responseStr);
		ret = pAudioHandle->ULCustDualMicParamFromNV(pDUALMICParam+strLen, &dataLen);

		if (ret==ACHSucceeded) {
			sendResponseToATCI((char *)pDUALMICParam, strLen+dataLen);
		}else {
			strcat(responseStr, " Fail");
			sendResponseToATCI(responseStr, strlen(responseStr));
		}

		free(pDUALMICParam);
	} else {
		strncpy(responseStr, (char *)pParam, strLen-1);
		responseStr[strLen-1] = '\0';
			
		pDUALMICParam = pParam + strLen;
		ret = pAudioHandle->DLCustDualMicParamToNV(pDUALMICParam);

		if (ret==ACHSucceeded) {
			strcat(responseStr, " OK");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}else {
			strcat(responseStr, " Fail");
			sendResponseToATCI(responseStr,strlen(responseStr));
		}
	}
}

static void DoGetPhoneSupportInfo(AudioCmdHandler *pAudioHandle)
{
	int ret = ACHFailed;
	unsigned int supportInfo = 0;
	char paramsKeyPaires[MAX_CMD_LEN];
	
	if (pAudioHandle!=NULL)
		ret = pAudioHandle->getPhoneSupportInfo(&supportInfo);

	if (ret==ACHSucceeded) {
		sprintf(paramsKeyPaires, "MM+GPSI=%d\r\n", supportInfo);
		sendResponseToATCI(paramsKeyPaires, strlen(paramsKeyPaires));
	} else {
		sendResponseToATCI("MM+GPSI=Failed\r\n",strlen("MM+GPSI=Failed\r\n"));
	}
}
//--->

//parse command received from ATCI
static AudioCmdType parseCmd(char *pRecvData, AudioCmdParam &mAudioCmdParams)
{
	char tempData[MAX_CMD_LEN];
	if (strlen(pRecvData)>(MAX_CMD_LEN-1))
		return UNKNOWN_CMD;
		
	strcpy(tempData, pRecvData);
	char * pTempData = tempData;
	char *pRecvCmd = NULL;
	char *pRecvParams = NULL;
	char *token = NULL;
	int mParams[4];
	AudioCmdType mReturnVal = UNKNOWN_CMD;

	LOGD("Audio Cmd Handler start to parse command in, received data %s, len %d", pRecvData, strlen(pRecvData));
	
	memset(&mAudioCmdParams, 0, sizeof(mAudioCmdParams));
	memset(mParams, 0, sizeof(mParams));

	pRecvCmd = strsep(&pTempData, "=");
	if (NULL == pRecvCmd) {
		pRecvCmd = pTempData;
	}else if(strlen(pRecvData)==strlen(pRecvCmd)+1){
		pRecvParams = NULL;
	}else {
	    pRecvParams = pTempData;
	}

    if (pRecvParams) {
		int i = 0;
	    do {
			token = strsep(&pRecvParams, ",");

			if (!token) {
				mParams[i] = atoi(pRecvParams);
			} else {
			mParams[i] = atoi(token);
			}
//			LOGD("The parameters is mParams[%d]=%d, token=%d, pRecvParams=%d", i, mParams[i], token, pRecvParams);
			i++;
		}while(pRecvParams && i<4);
		
		mAudioCmdParams.param1 = mParams[0];
		mAudioCmdParams.param2 = mParams[1];
		mAudioCmdParams.param3 = mParams[2];
		mAudioCmdParams.param4 = mParams[3];
		mAudioCmdParams.recvDataLen = strlen(pRecvData);
	}

	if (0==strcmp(pRecvCmd, "AUD+RDPRM")) {
		mReturnVal = RDPRM_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+RDMIC")) {
		mReturnVal = RDMIC_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+PGAGAIN")) {
		mReturnVal = PGAGAIN_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+RDSTP")) {
		mReturnVal = RDSTP_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+FMREC")) {
		mReturnVal = FMREC_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+FMPLY")) {
		mReturnVal = FMPLY_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+VDPG")) {
		mReturnVal = VDPG_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+AUDLINEPG")) {
		mReturnVal = AUDLINEPG_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+FMVUPG")) {
		mReturnVal = FMVUPG_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+MICVUPG")) {
		mReturnVal = MICVUPG_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+MEDPLY")) {
		mReturnVal = MEDPLY_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+MEDSTOP")) {
		mReturnVal = MEDSTOP_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+I2SREC")) {
		mReturnVal = I2SREC_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+I2SRECSTP")) {
		mReturnVal = I2SRECSTP_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+REGWRITE")) {
		mReturnVal = REGWRITE_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+REGREAD")) {
		mReturnVal = REGREAD_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+I2SPLY")) {
		mReturnVal = I2SPLY_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+I2SPLYSTP")) {
		mReturnVal = I2SPLYSTP_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+MODESEL")) {
		mReturnVal = MODESEL_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+AMPEN")) {
		mReturnVal = AMPEN_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+AMPVOL")) {
		mReturnVal = AMPVOL_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+RECEIVER")) {
		mReturnVal = RECEIVER_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+RECGAIN")) {
		mReturnVal = RECGAIN_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+TVOPEN")) {
		mReturnVal = TVOPEN_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+TVOPEND")) {
		mReturnVal = TVOPEND_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+TVSYS")) {
		mReturnVal = TVSYS_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+TVSHOWPAT")) {
		mReturnVal = TVSHOWPAT_CMD;
	}else if(0==strcmp(pRecvCmd, "AUD+TVLEAVEPAT")) {
		mReturnVal = TVLEAVEPAT_CMD;
	}else if(0==strcmp(pRecvCmd, "MM+EADP")) {
		mReturnVal = EADP_CMD;
	}else if(0==strcmp(pRecvCmd, "MM+EAPS")) {
		mReturnVal = EAPS_CMD;
	}else if(0==strcmp(pRecvCmd, "MM+EACF")) {
		mReturnVal = EACF_CMD;
	}else if(0==strcmp(pRecvCmd, "MM+EHCF")) {
		mReturnVal = EHCF_CMD;
	}else if(0==strcmp(pRecvCmd, "MM+EDUALMIC")) {
		mReturnVal = EDUALMIC_CMD;
	}else if(0==strcmp(pRecvCmd, "MM+GPSI")) {
		mReturnVal = GPSI_CMD;
	}else if(0==strcmp(pRecvCmd, "MM+SLBK")) {
		mReturnVal = SLBK_CMD;
	}else if(0==strcmp(pRecvCmd, "DISC")) {
		mReturnVal = AUDISC_CMD;
	}else {
		mReturnVal = UNKNOWN_CMD;
		LOGE("Unknown command %s", pRecvCmd);
	}

	LOGD("Parse command success, the command is %s", pRecvCmd);
  return mReturnVal;
}

void sendResponseToATCI(const char *pSendData, int length)
{
	int mSendLen = 0;
	LOGD("The data that Audio Cmd Handler sent to ATCI  is %s, data length is %d", pSendData, length);

	if (s_fdAudioCmdHdlSrv_command>=0)
	{
		mSendLen = send(s_fdAudioCmdHdlSrv_command, pSendData, length, 0);
		if (mSendLen != length) {
			LOGE("Lose data when Audio Cmd Handler send to ATCI. errno = %d", errno);
		}
	} else {
		LOGE("Fail to send response to ATCI, the connection is disconnect");
	}
}

static void * audioCmdHandleLoop(void *pParam)
{
	int ret =0;
	struct sockaddr_in ATCI_addr;
	int ATCI_len = sizeof(ATCI_addr);
	fd_set rfds;
	AudioCmdType mCmdType = UNKNOWN_CMD;
	AudioCmdHandler *pAudioCmdHandler = NULL;

	LOGD("Audio Cmd Handler data processing loop in");
	while(1)
	{
		if (s_fdAudioCmdHdlSrv_command<0) {
			FD_ZERO(&rfds);
			FD_SET(s_fdAudioCmdHdlSrv_listen, &rfds);
			ret = select(s_fdAudioCmdHdlSrv_listen+1, &rfds, NULL, NULL, NULL);

			if (ret<0) {
				if (errno == EINTR) continue;
					LOGE("Fail to select. error (%d)", errno);
					exit(-1);
			}

			if (FD_ISSET(s_fdAudioCmdHdlSrv_listen, &rfds)) {
				s_fdAudioCmdHdlSrv_command = accept(s_fdAudioCmdHdlSrv_listen, (sockaddr *)&ATCI_addr, &ATCI_len);
				
				if (s_fdAudioCmdHdlSrv_command < 0) {
					LOGE("Error on accept(s_fdAudioCmdHdlSrv_command) errno:%d", errno);
					exit(-1);
				}
				LOGD("Connect to ATCI server success.");

				ret = fcntl(s_fdAudioCmdHdlSrv_command, F_SETFL, O_NONBLOCK);
				if (ret < 0) {
					LOGE("Fail to set audio-atci server socket O_NONBLOCK. errno: %d", errno);
				}

				if (pAudioCmdHandler == NULL) {
					pAudioCmdHandler = new AudioCmdHandler();
				}
			}
		}else {
			FD_ZERO(&rfds);
			FD_SET(s_fdAudioCmdHdlSrv_command, &rfds);
			ret = select(s_fdAudioCmdHdlSrv_command+1, &rfds, NULL, NULL, NULL);

			if (ret < 0) {
				if (errno == EINTR) continue;
					LOGE("Fail to select. error (%d)", errno);

				if (pAudioCmdHandler!=NULL) {
					delete pAudioCmdHandler;
					pAudioCmdHandler = NULL;
				}
				close(s_fdAudioCmdHdlSrv_command);
				s_fdAudioCmdHdlSrv_command = -1;
				exit(-1);
			}

			if (FD_ISSET(s_fdAudioCmdHdlSrv_command, &rfds)){
				int recvLen = 0;
				AudioCmdParam mAduioCmdParams;
				char pRecvData[MAX_BUFSIZE_RECV];
				char paramsKeyPaires[MAX_CMD_LEN];
				
				memset(pRecvData, 0, MAX_BUFSIZE_RECV);
				recvLen = recv(s_fdAudioCmdHdlSrv_command, pRecvData, MAX_BUFSIZE_RECV, 0);
				LOGD("Receive data from audio-atci client socket. length = %d", recvLen);
				if (recvLen<=0) {
					LOGE("Fail to receive data from audio-atci client socket. errno = %d", errno);
					continue;
				}else {
					mCmdType = parseCmd(pRecvData, mAduioCmdParams);
				}

				switch(mCmdType) {
					case RDPRM_CMD:
						DoSetRecorderParam(mAduioCmdParams, pAudioCmdHandler);
						break;
					case RDMIC_CMD:
						DoStartRecorderFrMIC(mAduioCmdParams, pAudioCmdHandler);
						break;
					case PGAGAIN_CMD:
						DoSetMICGain(mAduioCmdParams, pAudioCmdHandler);
						break;
					case RDSTP_CMD:
						DoStopRecorderFrMIC(pAudioCmdHandler);
						break;
					case FMREC_CMD:
						DoRecordFrFM(mAduioCmdParams, pAudioCmdHandler);
						break;
					case FMPLY_CMD:
						DoPlayFM(mAduioCmdParams, pAudioCmdHandler);
						break;
					case VDPG_CMD:
						DoSetVDPG(mAduioCmdParams, pAudioCmdHandler);
						break;
					case AUDLINEPG_CMD:
						DoSetAUDLINEPG(mAduioCmdParams, pAudioCmdHandler);
						break;
					case FMVUPG_CMD:
						DoSetFMVUPG(mAduioCmdParams, pAudioCmdHandler);
						break;
					case MICVUPG_CMD:
						DoSetMICVUPG(mAduioCmdParams, pAudioCmdHandler);
						break;
					case MEDPLY_CMD:
						DoStartAudioPlayer(mAduioCmdParams, pAudioCmdHandler);
						break;
					case MEDSTOP_CMD:
						DoStopAudioPlayer(pAudioCmdHandler);
						break;
					case I2SREC_CMD:
						DoStartRecorderFrI2S(mAduioCmdParams, pAudioCmdHandler);
						break;
					case I2SRECSTP_CMD:
						DoStopRecorderFrI2S(pAudioCmdHandler);
						break;
					case REGWRITE_CMD:
						DoWriteRegister(mAduioCmdParams, pAudioCmdHandler);
						break;
					case REGREAD_CMD:
						DoReadRegister(mAduioCmdParams, pAudioCmdHandler);
						break;
					case I2SPLY_CMD:
						if (mAduioCmdParams.recvDataLen>=strlen("AUD+I2SPLY=1,1,1,8000") && pAudioCmdHandler!=NULL && mAduioCmdParams.param4>=8000 && mAduioCmdParams.param4<=48000) {
							sprintf(paramsKeyPaires, "HQA_I2S_OUTPUT_PLAY=%d", mAduioCmdParams.param4);
							pAudioCmdHandler->setParameters(String8(paramsKeyPaires));
							sendResponseToATCI("AUD+I2SPLY=Succeeded",strlen("AUD+I2SPLY=Succeeded"));
						} else {
							sendResponseToATCI("AUD+I2SPLY=failed",strlen("AUD+I2SPLY=failed"));
						}
						break;
					case I2SPLYSTP_CMD:
						if (pAudioCmdHandler!=NULL) {
							pAudioCmdHandler->setParameters(String8("HQA_I2S_OUTPUT_STOP=0"));
							sendResponseToATCI("AUD+I2SPLYSTP=Succeeded",strlen("AUD+I2SPLYSTP=Succeeded"));
						} else {
							sendResponseToATCI("AUD+I2SPLYSTP=failed",strlen("AUD+I2SPLYSTP=failed"));
						}
						break;
					case MODESEL_CMD:
						if (mAduioCmdParams.recvDataLen>=strlen("AUD+MODESEL=1") && pAudioCmdHandler!=NULL && mAduioCmdParams.param1>=0 && mAduioCmdParams.param1<2) {
							sprintf(paramsKeyPaires, "HQA_AMP_MODESEL=%d", mAduioCmdParams.param1);
							pAudioCmdHandler->setParameters(String8(paramsKeyPaires));
							sendResponseToATCI("AUD+MODESEL=Succeeded",strlen("AUD+MODESEL=Succeeded"));
						}else {
							sendResponseToATCI("AUD+MODESEL=failed",strlen("AUD+MODESEL=failed"));
						}
						break;
					case AMPEN_CMD:
						if (mAduioCmdParams.recvDataLen>=strlen("AUD+AMPEN=1") && pAudioCmdHandler!=NULL && mAduioCmdParams.param1>=0 && mAduioCmdParams.param1<2) {
							sprintf(paramsKeyPaires, "HQA_AMP_AMPEN=%d", mAduioCmdParams.param1);
							pAudioCmdHandler->setParameters(String8(paramsKeyPaires));
							sendResponseToATCI("AUD+AMPEN=Succeeded",strlen("AUD+AMPEN=Succeeded"));
						}else {
							sendResponseToATCI("AUD+AMPEN=failed",strlen("AUD+AMPEN=failed"));
						}
						break;
					case AMPVOL_CMD:
						if (mAduioCmdParams.recvDataLen>=strlen("AUD+AMPVOL=1") && pAudioCmdHandler!=NULL && mAduioCmdParams.param1>=0 && mAduioCmdParams.param1<8) {
							sprintf(paramsKeyPaires, "HQA_AMP_AMPVOL=%d", mAduioCmdParams.param1);
							pAudioCmdHandler->setParameters(String8(paramsKeyPaires));
							sendResponseToATCI("AUD+AMPVOL=Succeeded",strlen("AUD+AMPVOL=Succeeded"));
						}else {
							sendResponseToATCI("AUD+AMPVOL=failed",strlen("AUD+AMPVOL=failed"));
						}
						break;
					case RECEIVER_CMD:
						if (mAduioCmdParams.recvDataLen>=strlen("AUD+RECEIVER=1") && pAudioCmdHandler!=NULL && mAduioCmdParams.param1>=0 && mAduioCmdParams.param1<2) {
							sprintf(paramsKeyPaires, "HQA_AMP_RECEIVER=%d", mAduioCmdParams.param1);
							pAudioCmdHandler->setParameters(String8(paramsKeyPaires));
							sendResponseToATCI("AUD+RECEIVER=Succeeded",strlen("AUD+RECEIVER=Succeeded"));
						}else {
							sendResponseToATCI("AUD+RECEIVER=failed",strlen("AUD+RECEIVER=failed"));
						}
						break;
					case RECGAIN_CMD:
						if (mAduioCmdParams.recvDataLen>=strlen("AUD+RECGAIN=1") && pAudioCmdHandler!=NULL && mAduioCmdParams.param1>=0 && mAduioCmdParams.param1<4) {
							sprintf(paramsKeyPaires, "HQA_AMP_RECGAIN=%d", mAduioCmdParams.param1);
							pAudioCmdHandler->setParameters(String8(paramsKeyPaires));
							sendResponseToATCI("AUD+RECGAIN=Succeeded",strlen("AUD+RECGAIN=Succeeded"));
						}else {
							sendResponseToATCI("AUD+RECGAIN=failed",strlen("AUD+RECGAIN=failed"));
						}
						break;
					case TVOPEN_CMD:
						if (DoTVoutOpen(mAduioCmdParams) == 0) {
							sendResponseToATCI("AUD+TVOPEN=Succeeded",strlen("AUD+TVOPEN=Succeeded"));
						} else {
							sendResponseToATCI("AUD+TVOPEN=Failed",strlen("AUD+TVOPEN=Failed"));
						}
						break;
					case TVOPEND_CMD:
						if (DoTVoutOpenDirectly(mAduioCmdParams) == 0) {
							sendResponseToATCI("AUD+TVOPEND=Succeeded",strlen("AUD+TVOPEND=Succeeded"));
						} else {
							sendResponseToATCI("AUD+TVOPEND=Failed",strlen("AUD+TVOPEND=Failed"));
						}
						break;
					case TVSYS_CMD:
						if (DoTVoutSetSystem(mAduioCmdParams) == 0) {
							sendResponseToATCI("AUD+TVSYS=Succeeded",strlen("AUD+TVSYS=Succeeded"));
						} else {
							sendResponseToATCI("AUD+TVSYS=Failed",strlen("AUD+TVSYS=Failed"));
						}
						break;
					case TVSHOWPAT_CMD:
						if (DoTVoutShowPattern(mAduioCmdParams) == 0) {
							sendResponseToATCI("AUD+TVSHOWPAT=Succeeded",strlen("AUD+TVSHOWPAT=Succeeded"));
						} else {
							sendResponseToATCI("AUD+TVSHOWPAT=Failed",strlen("AUD+TVSHOWPAT=Failed"));
						}
						break;
					case TVLEAVEPAT_CMD:
						if (DoTVoutLeavePattern(mAduioCmdParams) == 0) {
							sendResponseToATCI("AUD+TVLEAVEPAT=Succeeded",strlen("AUD+TVLEAVEPAT=Succeeded"));
						} else {
							sendResponseToATCI("AUD+TVLEAVEPAT=Failed",strlen("AUD+TVLEAVEPAT=Failed"));
						}
						break;
					case EAPS_CMD:
						DoCustSPHParam(mAduioCmdParams, (void *)pRecvData, pAudioCmdHandler);
						break;
					case EADP_CMD:
						DoCustSPHVolumeParam(mAduioCmdParams, (void *)pRecvData, pAudioCmdHandler);
						break;
					case EACF_CMD:
						mAduioCmdParams.param2 = 0;
						DoCustACFHCFParam(mAduioCmdParams, (void *)pRecvData, pAudioCmdHandler);
						break;
					case EHCF_CMD:
						mAduioCmdParams.param2 = 1;
						DoCustACFHCFParam(mAduioCmdParams, (void *)pRecvData, pAudioCmdHandler);
						break;
					case EDUALMIC_CMD:
						DoCustDUALMICParam(mAduioCmdParams, (void *)pRecvData, pAudioCmdHandler);
						break;
					case GPSI_CMD:
						DoGetPhoneSupportInfo(pAudioCmdHandler);
						break;
					case SLBK_CMD:
						if (mAduioCmdParams.recvDataLen>=strlen("MM+SLBK=1") && pAudioCmdHandler!=NULL) {
							sprintf(paramsKeyPaires, "SET_LOOPBACK_TYPE=%d", mAduioCmdParams.param1);
							pAudioCmdHandler->setParameters(String8(paramsKeyPaires));
							sendResponseToATCI("MM+SLBK=Succeeded\r\n",strlen("MM+SLBK=Succeeded\r\n"));
						}else {
							sendResponseToATCI("MM+SLBK=failed\r\n",strlen("MM+SLBK=failed\r\n"));
						}
						break;
					case UNKNOWN_CMD:
						sendResponseToATCI("AUD+=Unknown",strlen("AUD+=Unknown"));
						break;
					case AUDISC_CMD: {
						if (pAudioCmdHandler!=NULL) {
							delete pAudioCmdHandler;
							pAudioCmdHandler = NULL;
					  }
						sendResponseToATCI("DISC",strlen("DISC"));
						close(s_fdAudioCmdHdlSrv_command);
						s_fdAudioCmdHdlSrv_command = -1;
						break;
					}
				}
			}
		}
	}
	LOGD("Audio Cmd Handler data processing loop out");
}

int main(int argc, char *argv[])
{
	int ret = 0;
	pthread_t s_tid_audio;
	LOGD("AudioCmdHandlerService main() in");

	// set up thread-pool
	sp<ProcessState> proc(ProcessState::self());
	ProcessState::self()->startThreadPool();
	
	s_fdAudioCmdHdlSrv_listen = android_get_control_socket(SOCKET_NAME_AUDIO);
	LOGD("Get socket '%d': %s", s_fdAudioCmdHdlSrv_listen, strerror(errno));
	if (s_fdAudioCmdHdlSrv_listen < 0) {
		LOGE("Failed to get socket '" SOCKET_NAME_AUDIO "'");
		exit(-1);
	}

	ret = listen(s_fdAudioCmdHdlSrv_listen, 4);
	if (ret < 0) {
		LOGE("Failed to listen on control socket '%d': %s", s_fdAudioCmdHdlSrv_listen, strerror(errno));
		exit(-1);
	}

	ret = fcntl(s_fdAudioCmdHdlSrv_listen, F_SETFL, O_NONBLOCK);
	if (ret < 0) {
		LOGE("Fail to set audio-atci server socket O_NONBLOCK. errno: %d", errno);
	}
	
	ret = pthread_create(&s_tid_audio, NULL, audioCmdHandleLoop, NULL);
	if (ret != 0) {
		LOGE("Fail to create audio-handler thread errno:%d", errno);            
		exit(-1);
	}

	pthread_join(s_tid_audio, NULL);
	return 0;
}

