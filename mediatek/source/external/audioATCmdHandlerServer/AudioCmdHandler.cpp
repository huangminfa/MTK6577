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
 *   This file implements the  handling about audio command comming from AT Command Service.
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
 * 05 26 2011 changqing.yan
 * [ALPS00050318] [Need Patch] [Volunteer Patch]Remove fmaudioplayer and matvaudiopath path from mediaplayer.
 * .
 *
 * 05 26 2011 changqing.yan
 * [ALPS00050318] [Need Patch] [Volunteer Patch]Remove fmaudioplayer and matvaudiopath path from mediaplayer.
 * .
 *
 *******************************************************************************/

/*=============================================================================
 *                              Include Files
 *===========================================================================*/
#include <cutils/xlog.h>
#include <utils/String8.h>
#include <sys/types.h> //for opendir, readdir closdir
#include <dirent.h>    //for opendir, readdir closdir
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>

#include <media/AudioSystem.h>
#include <audiocustparam/AudioCustParam.h>
#include <AudioCompensationFilter/AudioCompFltCustParam.h>
#include <HeadphoneCompensationFilter/HeadphoneCompFltCustParam.h>

#include "AudioCmdHandler.h"

#include <binder/IServiceManager.h>
#include <media/IAudioPolicyService.h>

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "AudioCmdHandler"

#undef LOGV
#undef LOGD
#undef LOGE

#define LOGV SXLOGV
#define LOGD SXLOGD
#define LOGE SXLOGE

#define AUDIO_TEST_ROOT "/sdcard/"
#define RECORD_DIR_UPLINK "/sdcard/Up_link_ADC/"
#define RECORD_DIR_FM "/sdcard/FM_Play_Record/"
#define RECORD_DIR_I2S "/sdcard/I2SRecord/"

#define MAX_FILE_NAME 512
#define TEMP_ARRAY_SIZE 64

using namespace android;

const char * findFileName(const char *pFilePath, const char *pFileName, char *pFileWholeName)
{
	char *pSearchFileNameSrc = NULL;
	DIR *pSearchPath;
	struct dirent * pFileHandle;

	if ((pSearchPath = opendir(pFilePath))==NULL) {
		LOGE("open file path: %s error", pFilePath);
		return NULL;
	}

	while ((pFileHandle = readdir(pSearchPath))!=NULL){
		pSearchFileNameSrc = pFileHandle->d_name;
		if (strncmp(pSearchFileNameSrc, pFileName, 2/*strlen(pFileName)*/)==0){
			LOGD("Find the file: %s",  pFileHandle->d_name);
			if (pFileWholeName==NULL) {
				LOGE("the pointer pFileWholeName is NULL");
				return NULL;
			}
			
			strcpy(pFileWholeName, pFileHandle->d_name);
			closedir(pSearchPath);
			return pFileWholeName;
		}
	}

	LOGE("there are not the file: %s", pFileName);
	return NULL;
}

// AT Command for Speech calibration
static void dataEncode(char *pPara, int length)
{
	char *pParaTemp = pPara+length;
	memcpy((void *)pParaTemp, (void *)pPara, length);

	for(int i=0;i<length;i++)
	{
		*(pPara+2*i) = ((*(pParaTemp+i)>>4)&0x0F)|0x30;
		*(pPara+2*i+1) = (*(pParaTemp+i)&0x0F)|0x30;
	}
}

static void dataDecode(char *pPara, int length)
{
	char *pParaTemp = pPara+length;

	for(int i=0;i<length;i++)
	{
		*(pPara+i) = ((*(pPara+2*i)<<4)&0xF0)|(*(pPara+2*i+1)&0x0F);
	}

	memset(pParaTemp, 0, length);
}

/*=============================================================================
 *                             Public Function
 *===========================================================================*/

AudioCmdHandler::AudioCmdHandler() :
	m_RecordMaxDur(-1),
	m_RecordChns(1),
	m_RecordSampleRate(8000),
	m_RecordBitsPerSample(16),
	m_fd(-1),
	m_bRecording(false),
	m_RecordAudioSource(Analog_MIC1_Single)
{
	LOGD("Constructor--AudioCmdHandler::AudioCmdHandler()");
	m_pMediaRecorderListenner = new PCMRecorderListener(this);
}

AudioCmdHandler::~AudioCmdHandler()
{
	LOGD("Deconstructor--AudioCmdHandler::AudioCmdHandler()");

	if (m_MediaRecorderClient.get()!=NULL)
		m_MediaRecorderClient->release();

}

ACHStatus AudioCmdHandler::setRecorderParam(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::setRecorderParam() in");

	if (audioCmdParams.param1<0) {
		LOGE("Fail to setRecorderParam: the duration <0");
		return ACHParamError;
	}
	
	m_RecordMaxDur = audioCmdParams.param1;

	if (audioCmdParams.param2!=1 && audioCmdParams.param2!=2) {
		LOGE("Fail to setRecorderParam: the channels is not equal to 1");
		return ACHParamError;
	}
	
	m_RecordChns = audioCmdParams.param2;

	if (audioCmdParams.param3<8000 || audioCmdParams.param3>48000) {
		LOGE("Fail to setRecorderParam: the sample rate is invalid");
		return ACHParamError;
	}
	
	m_RecordSampleRate = audioCmdParams.param3;

	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::startRecorderFrMIC(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::startRecorderFrMIC() in");

	int ret = ACHSucceeded;
	char recordFilepath[MAX_FILE_NAME];
	char recordFileName[TEMP_ARRAY_SIZE];
	char pRecordSrc[TEMP_ARRAY_SIZE];
	AudioSourceType audioSourceType = Analog_MIC1_Single;

	if (audioCmdParams.param1<0 || audioCmdParams.param1>4)
		return ACHParamError;
	
	sprintf(pRecordSrc, "HQA_RDMIC_P1=%d", audioCmdParams.param1);
	AudioSystem::setParameters(0, String8(pRecordSrc));
/*
	switch (audioCmdParams.param1) {
	  case 0:
	  	audioSourceType = Analog_MIC1_Single;
		break;
	  case 1:
	  	audioSourceType = Analog_MIC2_Single;
		break;
	  case 3:
	  	audioSourceType = Analog_MIC_Dual;
		break;
	  default:
//	  	LOGE("AudioCmdHandler::startRecorderFrMIC-the audio source type is not supported");
//		return ACHParamError;
		break;
	}
*/
	if (audioCmdParams.param2!=0)
		return ACHParamError;

	if (audioCmdParams.param3<1 || audioCmdParams.param3>999)
		return ACHParamError;

	strcpy(recordFilepath, RECORD_DIR_UPLINK);
	if (access(recordFilepath, F_OK)<0) {
		LOGE("startRecorderFrMIC() the path %s is not exit", recordFilepath);
		ret = mkdir(recordFilepath, S_IRWXU|S_IRWXG|S_IRWXO);
		if (-1==ret) {
			LOGE("startRecorderFrMIC() create path %s failed", recordFilepath);
			return ACHFailed;
		}
	}
	
	sprintf(recordFileName, "%03d.wav", audioCmdParams.param3);
	strcat(recordFilepath, recordFileName);

	ret = startRecorder(recordFilepath, audioSourceType);
	if (ret!=ACHSucceeded)
		return ACHFailed;
	
	return ACHSucceeded;
}
                                    
ACHStatus AudioCmdHandler::setMICGain(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::setMICGain() in");

	if (audioCmdParams.param1<0 || audioCmdParams.param1>63)
		return ACHParamError;

	if (audioCmdParams.param2<0 || audioCmdParams.param2>62)
		return ACHParamError;

	char pPGAGain[TEMP_ARRAY_SIZE];
	sprintf(pPGAGain, "HQA_PGAGAIN_P1=%d", audioCmdParams.param1);
	AudioSystem::setParameters(0, String8(pPGAGain));

	char minPGAGain[TEMP_ARRAY_SIZE];
	sprintf(minPGAGain, "HQA_PGAGAIN_P2=%d", audioCmdParams.param2);
	AudioSystem::setParameters(0, String8(minPGAGain));

	return ACHSucceeded;
	
}

ACHStatus AudioCmdHandler::startRecorderFrFM(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::startRecorderFrFM() in");

	int ret = ACHSucceeded;
	char recordFilepath[MAX_FILE_NAME];
	char recordFileName[TEMP_ARRAY_SIZE];

	if (audioCmdParams.param2!=0)
		return ACHParamError;

	if (audioCmdParams.param3<1 || audioCmdParams.param3>999)
		return ACHParamError;

	strcpy(recordFilepath, RECORD_DIR_FM);
	if (access(recordFilepath, F_OK)<0) {
		LOGE("startRecorderFrI2S() the path %s is not exit", recordFilepath);
		ret = mkdir(recordFilepath, S_IRWXU|S_IRWXG|S_IRWXO);
		if (-1==ret) {
			LOGE("startRecorderFrI2S() create path %s failed", recordFilepath);
			return ACHFailed;
		}
	}
	
	sprintf(recordFileName, "%03d.wav", audioCmdParams.param3);
	strcat(recordFilepath, recordFileName);

	ret = startRecorder(recordFilepath, Audio_FM_IN);
	if (ret!=ACHSucceeded) {
		AudioSystem::setParameters(0, String8("HQA_FMREC_LINE_IN=0"));
		return ACHFailed;
	}
	
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::playFM()
{
	LOGD("AudioCmdHandler::playFM() in");

	AudioSystem::setParameters(0, String8("HQA_FMPLY_LINE_IN=1"));
	
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::stopPlayingFM()
{
	LOGD("AudioCmdHandler::stopPlayingFM() in");

	AudioSystem::setParameters(0, String8("HQA_FMPLY_LINE_IN=0"));
	
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::setVDPG(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::setVDPG() in");

	if (audioCmdParams.param1<0 || audioCmdParams.param1>44)
		return ACHParamError;

	if (audioCmdParams.param2<0 || audioCmdParams.param2>44)
		return ACHParamError;

	if (audioCmdParams.param3<0 || audioCmdParams.param3>44)
		return ACHParamError;

	char pVDPG1[TEMP_ARRAY_SIZE];
	sprintf(pVDPG1, "HQA_VDPG_P1=%d", audioCmdParams.param1);
	AudioSystem::setParameters(0, String8(pVDPG1));

	char pVDPG2[TEMP_ARRAY_SIZE];
	sprintf(pVDPG2, "HQA_VDPG_P2=%d", audioCmdParams.param2);
	AudioSystem::setParameters(0, String8(pVDPG2));

	char pVDPG3[TEMP_ARRAY_SIZE];
	sprintf(pVDPG3, "HQA_VDPG_P3=%d", audioCmdParams.param3);
	AudioSystem::setParameters(0, String8(pVDPG3));
	
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::setAUDLINEPG(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::setAUDLINEPG() in");

	if (audioCmdParams.param1<0 || audioCmdParams.param1>8)
		return ACHParamError;

	char pAUDLINEPG[TEMP_ARRAY_SIZE];
	sprintf(pAUDLINEPG, "HQA_AUDLINEPG_P1=%d", audioCmdParams.param1);
	AudioSystem::setParameters(0, String8(pAUDLINEPG));
	
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::setFMorMICVUPG(AudioCmdParam & audioCmdParams, bool bFMGain)
{
	LOGD("AudioCmdHandler::setFMVUPG() in");

	if (audioCmdParams.param1<0 || audioCmdParams.param1>63)
		return ACHParamError;

	if (audioCmdParams.param2<0 || audioCmdParams.param2>63)
		return ACHParamError;

	char pFMorMICVUPG1[TEMP_ARRAY_SIZE];
	char pFMorMICVUPG2[TEMP_ARRAY_SIZE];

	if (bFMGain) {
		sprintf(pFMorMICVUPG1, "HQA_FMVUPG_P1=%d", audioCmdParams.param1);
		sprintf(pFMorMICVUPG2, "HQA_FMVUPG_P2=%d", audioCmdParams.param2);
	} else {
		sprintf(pFMorMICVUPG1, "HQA_MICVUPG_P1=%d", audioCmdParams.param1);
		sprintf(pFMorMICVUPG2, "HQA_MICVUPG_P2=%d", audioCmdParams.param2);
	}
	
	AudioSystem::setParameters(0, String8(pFMorMICVUPG1));
	AudioSystem::setParameters(0, String8(pFMorMICVUPG2));

	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::startAudioPlayer(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::startAudioPlayer() in");

	char filePath[MAX_FILE_NAME];
	char fileName[TEMP_ARRAY_SIZE];
	char fileDir[TEMP_ARRAY_SIZE];
	char fileDirSub[TEMP_ARRAY_SIZE];
	char fileWholeName[TEMP_ARRAY_SIZE];

	strcpy(filePath, AUDIO_TEST_ROOT);
	sprintf(fileName, "%02d", audioCmdParams.param3);
	sprintf(fileDir, "%02d/%02d/", audioCmdParams.param1,audioCmdParams.param2);
	strcat(filePath, fileDir);

	findFileName(filePath, fileName, fileWholeName);
	strcat(filePath, fileWholeName); 
	LOGD("AudioCmdHandler::startAudioPlayer:Audio file is %s", filePath);
	
    if (m_MediaPlayerClient.get()==NULL) {
		m_MediaPlayerClient = new MediaPlayer();
		m_MediaPlayerClient->setListener(this);
	}else if (m_MediaPlayerClient->isPlaying()){
			m_MediaPlayerClient->stop();
			m_MediaPlayerClient->reset();
	}

	if (m_MediaPlayerClient->setDataSource(filePath, NULL/* headers*/)!=NO_ERROR) {
		m_MediaPlayerClient->reset();
		LOGE("Fail to load the audio file");
		return ACHLoadFileFailed;
	}

        m_MediaPlayerClient->setAudioStreamType(AUDIO_STREAM_MUSIC);
	if (m_MediaPlayerClient->prepare()!=NO_ERROR) {
		m_MediaPlayerClient->reset();
		LOGE("Fail to play the audio file, prepare failed");
		return ACHFailed;
	}

	if (m_MediaPlayerClient->start()!=NO_ERROR) {
		m_MediaPlayerClient->reset();
		LOGE("Fail to play the audio file, start failed");
		return ACHFailed;
	}

	if (2==audioCmdParams.param1) {
		AudioSystem::setParameters(0, String8("HQA_MEDPLY_P1=2"));
	} else {
		AudioSystem::setParameters(0, String8("HQA_MEDPLY_P1=1"));
	}
        LOGD("AudioCmdHandler::startAudioPlayer() out");
    return ACHSucceeded;
}

ACHStatus AudioCmdHandler::stopAudioPlayer()
{
	LOGD("AudioCmdHandler::stopAudioPlayer() in");

	if (m_MediaPlayerClient.get()!=NULL && m_MediaPlayerClient->isPlaying()) {
		if (m_MediaPlayerClient->stop()!=NO_ERROR) {
			LOGE("Fail to stop playing the audio file");
			return ACHFailed;
		}
		m_MediaPlayerClient->reset();
		return ACHSucceeded;
	}

	return ACHFailed;
}

ACHStatus AudioCmdHandler::startRecorderFrI2S(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::startRecorderFrI2S() in");

	int ret = ACHSucceeded;
	char recordFilepath[MAX_FILE_NAME];
	char recordFileName[TEMP_ARRAY_SIZE];

	if (audioCmdParams.param3<1 || audioCmdParams.param3>99)
		return ACHParamError;

	strcpy(recordFilepath, RECORD_DIR_I2S);
	if (access(recordFilepath, F_OK)<0) {
		LOGE("startRecorderFrI2S() the path %s is not exit", recordFilepath);
		ret = mkdir(recordFilepath, S_IRWXU|S_IRWXG|S_IRWXO);
		if (-1==ret) {
			LOGE("startRecorderFrI2S() create path %s failed", recordFilepath);
			return ACHFailed;
		}
	}
	
	sprintf(recordFileName, "%02d.wav", audioCmdParams.param3);
	strcat(recordFilepath, recordFileName);

	m_RecordSampleRate = audioCmdParams.param4;

	ret = startRecorder(recordFilepath, Audio_I2S_IN);
	if (ret!=ACHSucceeded) {
		stopFMAudioPlayer();
		AudioSystem::setParameters(0, String8("HQA_I2SREC=0"));
		return ACHFailed;
	}
	
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::writeRegister(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::writeRegister() in");

	char pRegWrite1[TEMP_ARRAY_SIZE];
	sprintf(pRegWrite1, "HQA_REGWRITE_P1=%x", audioCmdParams.param1);
	AudioSystem::setParameters(0, String8(pRegWrite1));

	char pRegWrite2[TEMP_ARRAY_SIZE];
	sprintf(pRegWrite2, "HQA_REGWRITE_P2=%x", audioCmdParams.param2);
	AudioSystem::setParameters(0, String8(pRegWrite2));

	char pRegWrite3[TEMP_ARRAY_SIZE];
	sprintf(pRegWrite3, "HQA_REGWRITE_P3=%x", audioCmdParams.param3);
	AudioSystem::setParameters(0, String8(pRegWrite3));

	return ACHSucceeded;
}

String8 AudioCmdHandler::readRegister(AudioCmdParam &audioCmdParams)
{
	LOGD("AudioCmdHandler::readRegister() in");

	char pRegRead[TEMP_ARRAY_SIZE];
	String8 returnValue = String8("");
	
	sprintf(pRegRead, "HQA_REGREAD_P1=%x,%x", audioCmdParams.param1, audioCmdParams.param2);
	returnValue = AudioSystem::getParameters(0, String8(pRegRead));
	return returnValue;
}

void AudioCmdHandler::notify(int msg, int ext1, int ext2, const Parcel *obj)
{
	LOGD("AudioCmdHandler received message: msg=%d, ext1=%d, ext2=%d", msg, ext1, ext2);
	switch(msg)
	{
	  case MEDIA_PLAYBACK_COMPLETE:
	  	if (m_MediaPlayerClient.get()!=NULL) {
			m_MediaPlayerClient->stop();
	  		m_MediaPlayerClient->reset();
	  	}
		
		LOGD("AudioCmdHandler::notify -- audio playback complete");
	  	break;
	  case MEDIA_ERROR:
	  	if (m_MediaPlayerClient.get()!=NULL) {
	  		m_MediaPlayerClient->reset();
	  	}
		
		LOGE("AudioCmdHandler::notify -- audio playback error, exit");
	  	break;
	  default:
	  	break;
	}
}

// add for mt6575 HQA
void AudioCmdHandler::setParameters(const String8& keyValuePaires)
{
	AudioSystem::setParameters(0, keyValuePaires);
}

// add for speech parameters calibration-2/6/2012
ACHStatus AudioCmdHandler::DLCustSPHParamToNV(void *pParam, int block)
{
	int write_size = 0;
	int size = 0;

	LOGD("AudioCmdHandler::DLCustSPHParamToNV() in");
	AUD_SPH_PARAM_STRUCT *pCustomPara = NULL;
	AUDIO_PARAM_MED_STRUCT *pSPHMedPara = (AUDIO_PARAM_MED_STRUCT *)malloc(sizeof(AUDIO_PARAM_MED_STRUCT));
	if (pSPHMedPara==NULL) {
		return ACHFailed;
	}else if(pParam==NULL) {
		free(pSPHMedPara);
		pSPHMedPara = NULL;
		return ACHFailed;
	}
	
	size = GetMedParamFromNV(pSPHMedPara);
	LOGD("DLCustSPHParamToNV- GetMedParamFromNV read size=%d", size);
	if (size!=sizeof(AUDIO_PARAM_MED_STRUCT)) {
		LOGD("DLCustSPHParamToNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_PARAM_MED_STRUCT),size);
		free(pSPHMedPara);
		pSPHMedPara = NULL;
		return ACHFailed;
	}
	
	if (block==0) {
		pCustomPara = (AUD_SPH_PARAM_STRUCT *)(pParam + sizeof(int));
		AUDIO_CUSTOM_PARAM_STRUCT *pSPHPara = (AUDIO_CUSTOM_PARAM_STRUCT *)malloc(sizeof(AUDIO_CUSTOM_PARAM_STRUCT));
		if (pSPHPara==NULL) {
			free(pSPHMedPara);
			pSPHMedPara = NULL;
			return ACHFailed;
		}
		
		LOGD("DLCustSPHParamToNV customer size=%d,sph_param size=%d",size ,sizeof(AUDIO_CUSTOM_PARAM_STRUCT));
		size = GetCustParamFromNV(pSPHPara);
		if (size!=sizeof(AUDIO_CUSTOM_PARAM_STRUCT)) {
			LOGD("DLCustSPHParamToNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_CUSTOM_PARAM_STRUCT),size);
			free(pSPHPara);
			pSPHPara = NULL;
			free(pSPHMedPara);
			pSPHMedPara = NULL;
			return ACHFailed;
		}
		
		size = sizeof(pCustomPara->sph_com_param)+sizeof(pCustomPara->sph_mode_param)+sizeof(pCustomPara->sph_in_fir)+sizeof(pCustomPara->sph_out_fir);
		dataDecode((char *)pParam, size+sizeof(int));
		if (*((int *)pParam) != size) {
			LOGE("DLCustSPHParamToNV miss data !!");
			free(pSPHPara);
			pSPHPara = NULL;
			free(pSPHMedPara);
			pSPHMedPara = NULL;
			return ACHFailed;
		}
		
		for(int i=0; i < FIR_NUM_NB ;  i++)
		{
			LOGV("Received FIR Coefs sph_out_fir[0][%d]=%d",i, pCustomPara->sph_out_fir[0][i]);
		}

		for(int i=0; i < FIR_NUM_NB ;  i++)
		{
			LOGV("Speech Out FIR Coefs ori sph_out_fir[0][%d]=%d", i, pSPHPara->sph_out_fir[0][i]);
		}

		LOGD("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		LOGD("Speech Param bSupportVM_ori = %d",pSPHPara->bSupportVM);
		LOGD("Speech Param bAutoVM_ori = %d",pSPHPara->bAutoVM);
		LOGD("Speech Param uMicbiasVolt_ori = %d",pSPHPara->uMicbiasVolt);

		for(int i=0; i < SPH_ENHANCE_PARAM_NUM ;  i++)
		{
			LOGV("Received speech mode parameters ori sph_mode_param[0][%d]=%d", i, pCustomPara->sph_mode_param[0][i]);
		}
		
		for(int i=0; i < SPH_ENHANCE_PARAM_NUM ;  i++)
		{
			LOGV("Speech mode parameters ori speech_mode_para[0][%d]=%d", i, pSPHPara->speech_mode_para[0][i]);
		}
		
		memcpy((void *)pSPHPara->speech_common_para, (void *)pCustomPara->sph_com_param, sizeof(pCustomPara->sph_com_param));
		memcpy((void *)pSPHPara->speech_mode_para, (void *)pCustomPara->sph_mode_param, sizeof(pCustomPara->sph_mode_param));
		memcpy((void *)pSPHPara->sph_in_fir, (void *)pCustomPara->sph_in_fir, sizeof(pCustomPara->sph_in_fir));
		memcpy((void *)pSPHPara->sph_out_fir, (void *)pCustomPara->sph_out_fir, sizeof(pCustomPara->sph_out_fir));
		
		memcpy((void *)pSPHMedPara->speech_mode_para, (void *)pCustomPara->sph_mode_param, sizeof(pCustomPara->sph_mode_param));
		memcpy((void *)pSPHMedPara->speech_input_FIR_coeffs, (void *)pCustomPara->sph_in_fir, sizeof(pCustomPara->sph_in_fir));
		
		for(int i=0; i < FIR_NUM_NB ;  i++)
		{
			LOGV("Speech Out FIR Coefs new sph_out_fir[0][%d]=%d", i, pSPHPara->sph_out_fir[0][i]);
		}	

		LOGV("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		LOGV("Speech Param bSupportVM_new = %d",pSPHPara->bSupportVM);
		LOGV("Speech Param bAutoVM_new = %d",pSPHPara->bAutoVM);
		LOGV("Speech Param uMicbiasVolt_new = %d",pSPHPara->uMicbiasVolt);

		for(int i=0; i < SPH_ENHANCE_PARAM_NUM ;  i++)
		{
			LOGV("Speech mode parameters new speech_mode_para[0][%d]=%d", i, pSPHPara->speech_mode_para[0][i]);
		}
		
		write_size = SetCustParamToNV(pSPHPara);
		if (write_size!=sizeof(AUDIO_CUSTOM_PARAM_STRUCT)) {
			LOGD("DLCustSPHParamToNV Down load to NVRAM fail, structure size=%d,writed_size=%d",sizeof(AUDIO_CUSTOM_PARAM_STRUCT),write_size);
			free(pSPHPara);
			pSPHPara = NULL;
			free(pSPHMedPara);
			pSPHMedPara = NULL;
			return ACHFailed;
		}
		
		free(pSPHPara);
		pSPHPara = NULL;
	}else if(block==1){
		size = 2*sizeof(pCustomPara->sph_output_FIR_coeffs[0]);
		dataDecode((char *)pParam, size+sizeof(int));

		if (*((int *)pParam) != size) {
			LOGE("DLCustSPHParamToNV miss data !! block = %d, received size = %d", block, *((int *)pParam));
			free(pSPHMedPara);
			pSPHMedPara = NULL;
			return ACHFailed;
		}

		for(int i=0; i < SPH_MODE_NUM ;  i++)
		{
			LOGV("selected Speech Out FIR index ori select_FIR_output_index[%d]=%d", i, pSPHMedPara->select_FIR_output_index[i]);
		}	

		for(int i=0;i<SPH_MODE_NUM;i++)
		{
			for(int j=0;j<SPH_ENHANCE_PARAM_NUM;j++)
			{
				LOGV("MED Speech Out FIR index ori speech_output_FIR_coeffs[%d][0][%d]=%d",i,j,pSPHMedPara->speech_output_FIR_coeffs[i][0][j]);
			}
		}
		
		memcpy((void *)pSPHMedPara->speech_output_FIR_coeffs, pParam+sizeof(int), size);
	}else {
		size = 2*sizeof(pCustomPara->sph_output_FIR_coeffs[0]) + sizeof(pCustomPara->selected_FIR_output_index);
		dataDecode((char *)pParam, size+sizeof(int));

		if (*((int *)pParam) != size) {
			LOGE("DLCustSPHParamToNV miss data !! block = %d, received size = %d", block, *((int *)pParam));
			free(pSPHMedPara);
			pSPHMedPara = NULL;
			return ACHFailed;
		}
		
		memcpy((void *)pSPHMedPara->speech_output_FIR_coeffs[2], pParam+sizeof(int), 2*sizeof(pCustomPara->sph_output_FIR_coeffs[0]));
		size = 2*sizeof(pCustomPara->sph_output_FIR_coeffs[0]) + sizeof(int);
		memcpy((void *)pSPHMedPara->select_FIR_output_index, pParam+size, sizeof(pCustomPara->selected_FIR_output_index));
		
		for(int i=0; i < SPH_MODE_NUM ;  i++)
		{
			LOGV("selected Speech Out FIR index new select_FIR_output_index[%d]=%d", i, pSPHMedPara->select_FIR_output_index[i]);
		}	

		for(int i=0;i<SPH_MODE_NUM;i++)
		{
			for(int j=0;j<SPH_ENHANCE_PARAM_NUM;j++)
			{
				LOGV("MED Speech Out FIR index new speech_output_FIR_coeffs[%d][0][%d]=%d",i,j,pSPHMedPara->speech_output_FIR_coeffs[i][0][j]);
			}
		}
		
		AudioSystem::setParameters(0, String8("UpdateSpeechParameter=0"));
	}

	write_size = SetMedParamToNV(pSPHMedPara);
	if (write_size!=sizeof(AUDIO_PARAM_MED_STRUCT)) {
		LOGD("DLCustSPHParamToNV Down load to NVRAM fail, structure size=%d,writed_size=%d",sizeof(AUDIO_PARAM_MED_STRUCT),write_size);
		free(pSPHMedPara);
		pSPHMedPara = NULL;
		return ACHFailed;
	}

	free(pSPHMedPara);
	pSPHMedPara = NULL;
	
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::ULCustSPHParamFromNV(void *pParam, int *len, int block)
{
	int size = 0;
	int dataLen = 0;

	LOGD("AudioCmdHandler::ULCustSPHParamFromNV() in");
	AUDIO_CUSTOM_PARAM_STRUCT *pSPHPara = (AUDIO_CUSTOM_PARAM_STRUCT *)malloc(sizeof(AUDIO_CUSTOM_PARAM_STRUCT));
	AUDIO_PARAM_MED_STRUCT *pSPHMedPara = (AUDIO_PARAM_MED_STRUCT *)malloc(sizeof(AUDIO_PARAM_MED_STRUCT));
	if (pSPHMedPara==NULL||pSPHPara==NULL||pParam==NULL)
		return ACHFailed;

	size = GetCustParamFromNV(pSPHPara);
	if (size!=sizeof(AUDIO_CUSTOM_PARAM_STRUCT)) {
		LOGD("ULCustSPHParamFromNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_CUSTOM_PARAM_STRUCT),size);
		free(pSPHPara);
		pSPHPara = NULL;
		free(pSPHMedPara);
		pSPHPara = NULL;
		return ACHFailed;
	}

	size = GetMedParamFromNV(pSPHMedPara);
	if (size!=sizeof(AUDIO_PARAM_MED_STRUCT)) {
		LOGD("ULCustSPHParamFromNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_PARAM_MED_STRUCT),size);
		free(pSPHPara);
		pSPHPara = NULL;
		free(pSPHMedPara);
		pSPHMedPara = NULL;
		return ACHFailed;
	}

	for(int i=0; i < FIR_NUM_NB ;  i++)
	{
		LOGV("Speech Out FIR Coefs ori sph_out_fir[0][%d]=%d", i, pSPHPara->sph_out_fir[0][i]);
	}

	LOGV("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	LOGV("Speech Param bSupportVM_ori = %d",pSPHPara->bSupportVM);
	LOGV("Speech Param bAutoVM_ori = %d",pSPHPara->bAutoVM);
	LOGV("Speech Param uMicbiasVolt_ori = %d",pSPHPara->uMicbiasVolt);

	for(int i=0; i < SPH_ENHANCE_PARAM_NUM ;  i++)
	{
		LOGV("Speech mode parameters ori speech_mode_para[0][%d]=%d", i, pSPHPara->speech_mode_para[0][i]);
	}

	for(int i=0; i < 8 ;  i++)
	{
		LOGV("selected Speech Out FIR index ori select_FIR_output_index[%d]=%d", i, pSPHMedPara->select_FIR_output_index[i]);
	}	

	for(int i=0; i < 5 ;  i++)
	{
		LOGV("MED Speech Out FIR index ori speech_output_FIR_coeffs[1][0][%d]=%d", i, pSPHMedPara->speech_output_FIR_coeffs[1][0][i]);
	}

	if (block==0) {
		dataLen = sizeof(AUDIO_CUSTOM_PARAM_STRUCT);
		memcpy(pParam+sizeof(int), (void *)pSPHPara, dataLen);
	}else if(block==1) {
		dataLen = sizeof(pSPHMedPara->speech_input_FIR_coeffs);
		memcpy(pParam+sizeof(int), (void *)pSPHMedPara->speech_input_FIR_coeffs, dataLen);
	}else if(block==2 || block==3) {
		dataLen = 3*sizeof(pSPHMedPara->speech_output_FIR_coeffs[0]);
		memcpy(pParam+sizeof(int), (void *)pSPHMedPara->speech_output_FIR_coeffs[3*block-6], dataLen);
	}else {
		dataLen = 2*sizeof(pSPHMedPara->speech_output_FIR_coeffs[0]) + sizeof(pSPHMedPara->speech_mode_para);
		dataLen += sizeof(pSPHMedPara->select_FIR_intput_index) + sizeof(pSPHMedPara->select_FIR_output_index);
		memcpy(pParam+sizeof(int), (void *)pSPHMedPara->speech_output_FIR_coeffs[6], dataLen);
	}

	LOGD("ULCustSPHParamFromNV the data sent to PC is %d", dataLen);

	*((int *)pParam) = dataLen;
	*len = 2*(dataLen + sizeof(int));
	
	dataEncode((char *)pParam, dataLen+sizeof(int));

	free(pSPHPara);
	pSPHPara = NULL;
	free(pSPHMedPara);
	pSPHMedPara = NULL;
	return ACHSucceeded;
}
 
ACHStatus AudioCmdHandler::DLCustSPHVolumeParamToNV(void *pParam)
{
	int write_size = 0;
	int size = 0;
	int dataLen = 0;

	LOGD("AudioCmdHandler::DLCustSPHVolumeParamToNV() in");

	AUDIO_VOLUME_CUSTOM_STRUCT *pSPHVolPara = (AUDIO_VOLUME_CUSTOM_STRUCT *)malloc(sizeof(AUDIO_VOLUME_CUSTOM_STRUCT));
	if (pSPHVolPara==NULL||pParam==NULL)
		return ACHFailed;
		
	dataDecode((char *)pParam, sizeof(AUD_SPH_VOL_STRUCT)+sizeof(int));
	dataLen = *((int *)pParam);
	if (dataLen!=sizeof(AUD_SPH_VOL_STRUCT)) {
		SXLOGE("DLCustSPHVolumeParamToNV miss data !!");
		free(pSPHVolPara);
		pSPHVolPara = NULL;
		return ACHFailed;
	}

	AUD_SPH_VOL_STRUCT *pCustomPara = (AUD_SPH_VOL_STRUCT *)(pParam + sizeof(int));
	size = GetAudioCustomParamFromNV(pSPHVolPara);
	if (size!=sizeof(AUDIO_VOLUME_CUSTOM_STRUCT)) {
		LOGD("DLCustSPHVolumeParamToNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_VOLUME_CUSTOM_STRUCT),size);
		free(pSPHVolPara);
		pSPHVolPara = NULL;
		return ACHFailed;
	}

	LOGV("~~~~~~~~~~~~~~~~DL mic volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0; i<MAX_VOL_CATE; i++) {
		for(int j=0;j<CUSTOM_VOL_STEP;j++){
			LOGV("ori data - mic volume audiovolume_mic[%d][%d] = %d", i, j, pSPHVolPara->audiovolume_mic[i][j]);
		}
	}
	LOGV("~~~~~~~~~~~~~~~~DL sid volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0;i<MAX_VOL_CATE;i++) {
		for(int j=0;j<CUSTOM_VOL_STEP;j++){
			LOGV("ori data - sid volume audiovolume_sid[%d][%d]=%d", i, j, pSPHVolPara->audiovolume_sid[i][j]);
		}
	}
	LOGV("~~~~~~~~~~~~~~~~DL sph volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0;i<MAX_VOL_CATE;i++) {
		for(int j=0;j<CUSTOM_VOL_STEP;j++){
			LOGV("ori data - sph volume audiovolume_sph[%d][%d]=%d", i, j, pSPHVolPara->audiovolume_sph[i][j]);
		}
	}

	memcpy((void *)pSPHVolPara->audiovolume_mic, (void *)pCustomPara->audio_vol_mic, sizeof(pCustomPara->audio_vol_mic));
	memcpy((void *)pSPHVolPara->audiovolume_sph, (void *)pCustomPara->audio_vol_sph, sizeof(pCustomPara->audio_vol_sph));
	memcpy((void *)pSPHVolPara->audiovolume_sid, (void *)pCustomPara->audio_vol_sid, sizeof(pCustomPara->audio_vol_sid));
	memcpy((void *)pSPHVolPara->audiovolume_ring, (void *)pCustomPara->audio_vol_ring, sizeof(pCustomPara->audio_vol_ring));
	memcpy((void *)pSPHVolPara->audiovolume_key, (void *)pCustomPara->audio_vol_key, sizeof(pCustomPara->audio_vol_key));
	memcpy((void *)pSPHVolPara->audiovolume_fmr, (void *)pCustomPara->audio_vol_fmr, sizeof(pCustomPara->audio_vol_fmr));
	memcpy((void *)pSPHVolPara->audiovolume_media, (void *)pCustomPara->audio_vol_media, sizeof(pCustomPara->audio_vol_media));
	memcpy((void *)pSPHVolPara->audiovolume_matv, (void *)pCustomPara->audio_vol_matv, sizeof(pCustomPara->audio_vol_matv));

	LOGV("~~~~~~~~~~~~~~~~mic volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0;i<MAX_VOL_CATE; i++) {
		for(int j=0;j<CUSTOM_VOL_STEP;j++){
			LOGV("new data - mic volume audiovolume_mic[%d][%d]=%d", i, j, pSPHVolPara->audiovolume_mic[i][j]);
		}
	}
	LOGV("~~~~~~~~~~~~~~~~sid volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0;i<MAX_VOL_CATE;i++) {
		for(int j=0;j<CUSTOM_VOL_STEP;j++){
			LOGV("new data - sid volume audiovolume_sid[%d][%d]=%d", i, j, pSPHVolPara->audiovolume_sid[i][j]);
		}
	}
	LOGV("~~~~~~~~~~~~~~~~sph volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0;i<MAX_VOL_CATE;i++) {
		for(int j=0;j<CUSTOM_VOL_STEP;j++){
			LOGV("new data - sph volume audiovolume_sph[%d][%d]=%d", i, j, pSPHVolPara->audiovolume_sph[i][j]);
		}
	}

	write_size = SetAudioCustomParamToNV(pSPHVolPara);
	if (write_size!=sizeof(AUDIO_VOLUME_CUSTOM_STRUCT)) {
		LOGD("DLCustSPHVolumeParamToNV Down load to NVRAM fail, structure size=%d,writed_size=%d",sizeof(AUDIO_VOLUME_CUSTOM_STRUCT),write_size);
		free(pSPHVolPara);
		pSPHVolPara = NULL;
		return ACHFailed;
	}

	free(pSPHVolPara);
	pSPHVolPara = NULL;
	
	int volumeIndex[AUDIO_STREAM_CNT];
	for(int i=0;i<AUDIO_STREAM_CNT;i++)
	{
		AudioSystem::getStreamVolumeIndex((audio_stream_type_t)i,&volumeIndex[i]);
	}
#ifdef MTK_AUDIO
	const sp<IAudioPolicyService>& aps = AudioSystem::get_audio_policy_service();
	if (aps == 0) return ACHFailed;
	aps->SetPolicyManagerParameters(LOAD_VOLUME_POLICY, 0, 0, 0);
#endif
	AudioSystem::setParameters(0, String8("UpdateSphVolumeParameter=0"));
	for(int i=0;i<AUDIO_STREAM_CNT;i++)
	{
		AudioSystem::setStreamVolumeIndex((audio_stream_type_t)i,volumeIndex[i]);
	}
	
	return ACHSucceeded;
	
}

ACHStatus AudioCmdHandler::ULCustSPHVolumeParamFromNV(void *pParam, int *len)
{
	int size = 0;

	LOGD("AudioCmdHandler::ULCustSPHVolumeParamFromNV() in");
	
	AUDIO_VOLUME_CUSTOM_STRUCT *pSPHVolPara = (AUDIO_VOLUME_CUSTOM_STRUCT *)malloc(sizeof(AUDIO_VOLUME_CUSTOM_STRUCT));
	if (pSPHVolPara==NULL||pParam==NULL)
		return ACHFailed;

	*len = 2*(sizeof(AUD_SPH_VOL_STRUCT) + sizeof(int));
	size = GetAudioCustomParamFromNV(pSPHVolPara);
	if (size!=sizeof(AUDIO_VOLUME_CUSTOM_STRUCT)) {
		LOGD("ULCustSPHVolumeParamFromNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_VOLUME_CUSTOM_STRUCT),size);
		free(pSPHVolPara);
		pSPHVolPara = NULL;
		return ACHFailed;
	}

	LOGV("~~~~~~~~~~~~~~~~UL mic volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0;i<MAX_VOL_CATE;i++) {
		for(int j=0;j<CUSTOM_VOL_STEP ;j++){
			LOGV("ori data - mic volume audiovolume_mic[%d][%d]=%d", i, j, pSPHVolPara->audiovolume_mic[i][j]);
		}
	}
	LOGV("~~~~~~~~~~~~~~~~UL sid volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0;i<MAX_VOL_CATE;i++) {
		for(int j=0;j<CUSTOM_VOL_STEP;j++){
			LOGV("ori data - sid volume audiovolume_sid[%d][%d]=%d", i, j, pSPHVolPara->audiovolume_sid[i][j]);
		}
	}
	LOGV("~~~~~~~~~~~~~~~~UL sph volume~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	for(int i=0;i<MAX_VOL_CATE; i++) {
		for(int j=0;j<CUSTOM_VOL_STEP;j++){
			LOGV("ori data - sph volume audiovolume_sph[%d][%d]=%d", i, j, pSPHVolPara->audiovolume_sph[i][j]);
		}
	}

	*((int *)pParam) = sizeof(AUD_SPH_VOL_STRUCT);
	AUD_SPH_VOL_STRUCT *pCustomPara = (AUD_SPH_VOL_STRUCT *)(pParam + sizeof(int));
	
	memcpy((void *)pCustomPara->audio_vol_mic, (void *)pSPHVolPara->audiovolume_mic, sizeof(pCustomPara->audio_vol_mic));
	memcpy((void *)pCustomPara->audio_vol_sph, (void *)pSPHVolPara->audiovolume_sph, sizeof(pCustomPara->audio_vol_sph));
	memcpy((void *)pCustomPara->audio_vol_sid, (void *)pSPHVolPara->audiovolume_sid, sizeof(pCustomPara->audio_vol_sid));
	memcpy((void *)pCustomPara->audio_vol_ring, (void *)pSPHVolPara->audiovolume_ring, sizeof(pCustomPara->audio_vol_ring));
	memcpy((void *)pCustomPara->audio_vol_key, (void *)pSPHVolPara->audiovolume_key, sizeof(pCustomPara->audio_vol_key));
	memcpy((void *)pCustomPara->audio_vol_fmr, (void *)pSPHVolPara->audiovolume_fmr, sizeof(pCustomPara->audio_vol_fmr));
	memcpy((void *)pCustomPara->audio_vol_media, (void *)pSPHVolPara->audiovolume_media, sizeof(pCustomPara->audio_vol_media));
	memcpy((void *)pCustomPara->audio_vol_matv, (void *)pSPHVolPara->audiovolume_matv, sizeof(pCustomPara->audio_vol_matv));
	
	dataEncode((char *)pParam, sizeof(AUD_SPH_VOL_STRUCT)+sizeof(int));

	free(pSPHVolPara);
	pSPHVolPara = NULL;
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::DLCustSPHWBParamToNV(void *pParam, int block)
{
	int write_size = 0;
	int size = 0;
	int dataLen = 0;

	LOGD("AudioCmdHandler::DLCustSPHWBParamToNV() in");
#ifdef MTK_WB_SPEECH_SUPPORT
	AUD_SPH_WB_PARAM_STRUCT *pCustomPara = NULL;
	AUDIO_CUSTOM_WB_PARAM_STRUCT *pSPHWBPara = (AUDIO_CUSTOM_WB_PARAM_STRUCT *)malloc(sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT));
	if (pSPHWBPara==NULL) {
		return ACHFailed;
	}
		
	LOGD("DLCustSPHWBParamToNV ,wb sph param size=%d", sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT));
	size = GetCustWBParamFromNV(pSPHWBPara);
	if (size!=sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT)) {
		LOGD("DLCustSPHWBParamToNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT),size);
		free(pSPHWBPara);
		pSPHWBPara = NULL;
		return ACHFailed;
	}
	
	if (block==0) {
		size = sizeof(pCustomPara->sph_mode_wb_param) + sizeof(pCustomPara->sph_wb_in_fir) + sizeof(int);
		dataDecode((char *)pParam, size);
		
		dataLen = *((int *)pParam);
		pCustomPara = (AUD_SPH_WB_PARAM_STRUCT *)(pParam + sizeof(int));
		
		if (dataLen!=(size-sizeof(int))) {
			LOGE("DLCustSPHWBParamToNV data miss !!");
			free(pSPHWBPara);
			pSPHWBPara = NULL;
			return ACHFailed;
		}
		
		for(int i=0;i < SPH_ENHANCE_PARAM_NUM ;i++)
		{
			LOGV("Received speech mode parameters sph_mode_wb_param[0][i]=%d", i, pCustomPara->sph_mode_wb_param[0][i]);
		}
		
		for(int i=0;i < FIR_NUM_NB;i++)
		{
			LOGV("Received WB FIR Coefs sph_wb_in_fir[0][%d]=%d",i, pCustomPara->sph_wb_in_fir[0][i]);
		}
		
		memcpy((void *)pSPHWBPara->speech_mode_wb_para, (void *)pCustomPara->sph_mode_wb_param, sizeof(pCustomPara->sph_mode_wb_param));
		memcpy((void *)pSPHWBPara->sph_wb_in_fir, (void *)pCustomPara->sph_wb_in_fir, sizeof(pCustomPara->sph_wb_in_fir));

		for(int i=0; i < SPH_ENHANCE_PARAM_NUM ;  i++)
		{
			LOGV("WB speech mode parameters new=%d",pSPHWBPara->speech_mode_wb_para[0][i]);
		}
		
		for(int i=0; i<FIR_NUM_NB ;  i++)
		{
			LOGV("WB speech in FIR Coefs new=%d",pSPHWBPara->sph_wb_in_fir[0][i]);
		}	
		
		write_size = SetCustWBParamToNV(pSPHWBPara);
		if (write_size!=sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT)) {
			LOGD("DLCustSPHWBParamToNV Down load to NVRAM fail, structure size=%d,writed_size=%d",sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT),write_size);
			free(pSPHWBPara);
			pSPHWBPara = NULL;
			return ACHFailed;
		}
	}else{
		dataDecode((char *)pParam, sizeof(pCustomPara->sph_wb_out_fir)+sizeof(int));
		
		dataLen = *((int *)pParam);	
		if (dataLen!=sizeof(pCustomPara->sph_wb_out_fir)) {
			LOGE("DLCustSPHWBParamToNV data miss !!");
			free(pSPHWBPara);
			pSPHWBPara = NULL;
			return ACHFailed;
		}

		for(int i=0; i<FIR_NUM_NB ;  i++)
		{
			LOGV("WB speech out FIR Coefs ori=%d",pSPHWBPara->sph_wb_out_fir[0][i]);
		}
		
		memcpy((void *)pSPHWBPara->sph_wb_out_fir, pParam+sizeof(int), sizeof(pSPHWBPara->sph_wb_out_fir));
		
		for(int i=0; i<FIR_NUM_NB ;  i++)
		{
			LOGV("WB speech out FIR Coefs new=%d",pSPHWBPara->sph_wb_out_fir[0][i]);
		}
		
		write_size = SetCustWBParamToNV(pSPHWBPara);
		if (write_size!=sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT)) {
			LOGD("DLCustSPHWBParamToNV Down load to NVRAM fail, structure size=%d,writed_size=%d",sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT),write_size);
			free(pSPHWBPara);
			pSPHWBPara = NULL;
			return ACHFailed;
		}
		
		AudioSystem::setParameters(0, String8("UpdateSpeechParameter=1"));
	}

	free(pSPHWBPara);
	pSPHWBPara = NULL;
#else
	returen ACHFailed;
#endif	
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::ULCustSPHWBParamFromNV(void *pParam, int *len, int block)
{
	int size = 0;

	LOGD("AudioCmdHandler::ULCustSPHWBParamFromNV() in");
#ifdef MTK_WB_SPEECH_SUPPORT
	AUD_SPH_WB_PARAM_STRUCT *pCustomPara = (AUD_SPH_WB_PARAM_STRUCT *)(pParam + sizeof(int));
	AUDIO_CUSTOM_WB_PARAM_STRUCT *pSPHWBPara = (AUDIO_CUSTOM_WB_PARAM_STRUCT *)malloc(sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT));

	if (pSPHWBPara==NULL)
		return ACHFailed;
		
	size = GetCustWBParamFromNV(pSPHWBPara);
	if (size!=sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT)) {
		LOGD("ULCustSPHWBParamFromNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_CUSTOM_WB_PARAM_STRUCT),size);
		free(pSPHWBPara);
		pSPHWBPara = NULL;
		return ACHFailed;
	}

	if (block==0) {
		*len = 2*(sizeof(pCustomPara->sph_mode_wb_param) + sizeof(pCustomPara->sph_wb_in_fir) + sizeof(int));
		*((int *)pParam) = sizeof(pCustomPara->sph_mode_wb_param) + sizeof(pCustomPara->sph_wb_in_fir);

		for(int i=0;i<FIR_NUM_NB ;i++)
		{
			LOGV("Speech In FIR Coefs ori=%d",pSPHWBPara->sph_wb_in_fir[0][i]);
		}

		for(int i=0;i<SPH_ENHANCE_PARAM_NUM ;i++)
		{
			LOGV("Speech mode parameters ori=%d",pSPHWBPara->speech_mode_wb_para[0][i]);
		}
	
		memcpy((void *)pCustomPara->sph_mode_wb_param, (void *)pSPHWBPara->speech_mode_wb_para, sizeof(pCustomPara->sph_mode_wb_param));
		memcpy((void *)pCustomPara->sph_wb_in_fir, (void *)pSPHWBPara->sph_wb_in_fir, sizeof(pCustomPara->sph_wb_in_fir));
	
		dataEncode((char *)pParam, (*len)/2);
	}else if(block==1) {
		*len = 2*(sizeof(pCustomPara->sph_wb_out_fir) + sizeof(int));
		*((int *)pParam) = sizeof(pCustomPara->sph_wb_out_fir);

		for(int i=0;i<FIR_NUM_NB ;i++)
		{
			LOGV("Speech Out FIR Coefs ori=%d",pSPHWBPara->sph_wb_out_fir[0][i]);
		}
	
		memcpy((void *)pCustomPara, (void *)pSPHWBPara->sph_wb_out_fir, sizeof(pCustomPara->sph_wb_out_fir));
	
		dataEncode((char *)pParam, (*len)/2);
	}

	free(pSPHWBPara);
	pSPHWBPara = NULL;
#else
	return ACHFailed;
#endif
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::DLCustACFParamToNV(void *pParam)
{
	int write_size = 0;
	int size = 0;
	int dataLen = 0;

	LOGD("AudioCmdHandler::DLCustACFParamToNV() in");

	AUD_ACF_PARAM_STRUCT *pCustomPara = NULL;
	AUDIO_ACF_CUSTOM_PARAM_STRUCT *pACFPara = (AUDIO_ACF_CUSTOM_PARAM_STRUCT *)malloc(sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
	if (pACFPara==NULL) {
		return ACHFailed;
	}
		
	LOGD("DLCustACFParamToNV ,acf param size=%d", sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
	size = GetAudioCompFltCustParamFromNV(pACFPara);
	if (size!=sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT)) {
		LOGD("DLCustACFParamToNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT),size);
		free(pACFPara);
		pACFPara = NULL;
		return ACHFailed;
	}
	
	size = sizeof(AUD_ACF_PARAM_STRUCT) + sizeof(int);
	dataDecode((char *)pParam, size);
		
	dataLen = *((int *)pParam);
	pCustomPara = (AUD_ACF_PARAM_STRUCT *)(pParam + sizeof(int));
		
	if (dataLen!=sizeof(AUD_ACF_PARAM_STRUCT)) {
		LOGE("DLCustACFParamToNV data miss !!");
		free(pACFPara);
		pACFPara = NULL;
		return ACHFailed;
	}
		
	for(int i=0; i<9; i++) {
		for(int j=0;j<4 ;j++){
			LOGV("Received  - loudness hsf coeffs bes_loudness_hsf_coeff[%d][%d] = %d", i, j, pCustomPara->bes_loudness_hsf_coeff[i][j]);
		}
	}
	
	for(int i=0;i<3;i++)
	{
		LOGV("Received - loudness bpf coeffs bes_loudness_bpf_coeff[0][0][%d]=%d", i, pCustomPara->bes_loudness_bpf_coeff[0][0][i]);
	}

	LOGD("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	LOGD("Received - ACF Param bes_loudness_DRC_Forget_Table[0][0] = %d",pCustomPara->bes_loudness_DRC_Forget_Table[0][0]);
	LOGD("Received - ACF Param bes_loudness_WS_Gain_Max = %d",pCustomPara->bes_loudness_WS_Gain_Max);
	LOGD("Received - ACF Param bes_loudness_WS_Gain_Min = %d",pCustomPara->bes_loudness_WS_Gain_Min);
	LOGD("Received - ACF Param bes_loudness_Filter_First = %d",pCustomPara->bes_loudness_Filter_First);
	LOGD("Received - ACF Param bes_loudness_Filter_First = %d",pCustomPara->bes_loudness_Filter_First);
	for(int i=0;i<5;i++)
	{
		LOGV("Received - gain map in bes_loudness_Gain_Map_In[%d]=%d", i, pCustomPara->bes_loudness_Gain_Map_In[i]);
	}

	for(int i=0;i<5;i++)
	{
		LOGV("Received - gain map out bes_loudness_Gain_Map_Out[%d]=%d", i, pCustomPara->bes_loudness_Gain_Map_Out[i]);
	}
	
	memcpy((void *)pACFPara, (void *)pCustomPara, sizeof(AUD_ACF_PARAM_STRUCT));
	
	write_size = SetAudioCompFltCustParamToNV(pACFPara);
	if (write_size!=sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT)) {
		LOGD("DLCustACFParamToNV Down load to NVRAM fail, structure size=%d,writed_size=%d",sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT),write_size);
		free(pACFPara);
		pACFPara = NULL;
		return ACHFailed;
	}
		
	AudioSystem::setParameters(0, String8("UpdateACFHCFParameters=0"));

	free(pACFPara);
	pACFPara = NULL;
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::ULCustACFParamFromNV(void *pParam, int *len)
{
	int size = 0;

	LOGD("AudioCmdHandler::ULCustACFParamFromNV() in");

	AUD_ACF_PARAM_STRUCT *pCustomPara = NULL;
	AUDIO_ACF_CUSTOM_PARAM_STRUCT *pACFPara = (AUDIO_ACF_CUSTOM_PARAM_STRUCT *)malloc(sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
	if (pACFPara==NULL) {
		return ACHFailed;
	}
		
	LOGD("ULCustACFParamFromNV ,acf param size=%d", sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
	size = GetAudioCompFltCustParamFromNV(pACFPara);
	if (size!=sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT)) {
		LOGD("ULCustACFParamFromNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT),size);
		free(pACFPara);
		pACFPara = NULL;
		return ACHFailed;
	}
		
	for(int i=0; i<9; i++) {
		for(int j=0;j<4 ;j++){
			LOGV("Ori  - loudness hsf coeffs bes_loudness_hsf_coeff[%d][%d] = %d", i, j, pACFPara->bes_loudness_hsf_coeff[i][j]);
		}
	}
	
	for(int i=0;i<3;i++)
	{
		LOGV("Ori - loudness bpf coeffs bes_loudness_bpf_coeff[0][0][%d]=%d", i, pACFPara->bes_loudness_bpf_coeff[0][0][i]);
	}

	LOGD("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	LOGD("Ori - ACF Param bes_loudness_DRC_Forget_Table[0][0] = %d", pACFPara->bes_loudness_DRC_Forget_Table[0][0]);
	LOGD("Ori - ACF Param bes_loudness_WS_Gain_Max = %d", pACFPara->bes_loudness_WS_Gain_Max);
	LOGD("Ori - ACF Param bes_loudness_WS_Gain_Min = %d", pACFPara->bes_loudness_WS_Gain_Min);
	LOGD("Ori - ACF Param bes_loudness_Filter_First = %d", pACFPara->bes_loudness_Filter_First);
	LOGD("Ori - ACF Param bes_loudness_Filter_First = %d", pACFPara->bes_loudness_Filter_First);
	for(int i=0;i<5;i++)
	{
		LOGV("Ori - gain map in bes_loudness_Gain_Map_In[%d]=%d", i, pACFPara->bes_loudness_Gain_Map_In[i]);
	}

	for(int i=0;i<5;i++)
	{
		LOGV("Ori - gain map out bes_loudness_Gain_Map_Out[%d]=%d", i, pACFPara->bes_loudness_Gain_Map_Out[i]);
	}

	*((int *)pParam) = sizeof(AUD_ACF_PARAM_STRUCT);
	pCustomPara = (AUD_ACF_PARAM_STRUCT *)(pParam + sizeof(int));

	memcpy((void *)pCustomPara, (void *)pACFPara, sizeof(AUD_ACF_PARAM_STRUCT));

	dataEncode((char *)pParam, sizeof(AUD_ACF_PARAM_STRUCT)+sizeof(int));
	*len = 2*(sizeof(AUD_ACF_PARAM_STRUCT)+sizeof(int));
	
	free(pACFPara);
	pACFPara = NULL;
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::DLCustHCFParamToNV(void *pParam)
{
	int write_size = 0;
	int size = 0;
	int dataLen = 0;

	LOGD("AudioCmdHandler::DLCustHCFParamToNV() in");

	AUD_ACF_PARAM_STRUCT *pCustomPara = NULL;
	AUDIO_ACF_CUSTOM_PARAM_STRUCT *pHCFPara = (AUDIO_ACF_CUSTOM_PARAM_STRUCT *)malloc(sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
	if (pHCFPara==NULL) {
		return ACHFailed;
	}
		
	LOGD("DLCustHCFParamToNV ,acf param size=%d", sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
	size = GetHeadphoneCompFltCustParamFromNV(pHCFPara);
	if (size!=sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT)) {
		LOGD("DLCustHCFParamToNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT),size);
		free(pHCFPara);
		pHCFPara = NULL;
		return ACHFailed;
	}
	
	size = sizeof(AUD_ACF_PARAM_STRUCT) + sizeof(int);
	dataDecode((char *)pParam, size);
		
	dataLen = *((int *)pParam);
	pCustomPara = (AUD_ACF_PARAM_STRUCT *)(pParam + sizeof(int));
		
	if (dataLen!=sizeof(AUD_ACF_PARAM_STRUCT)) {
		LOGE("DLCustHCFParamToNV data miss !!");
		free(pHCFPara);
		pHCFPara = NULL;
		return ACHFailed;
	}
		
	for(int i=0; i<9; i++) {
		for(int j=0;j<4 ;j++){
			LOGV("Received  - loudness hsf coeffs bes_loudness_hsf_coeff[%d][%d] = %d", i, j, pCustomPara->bes_loudness_hsf_coeff[i][j]);
		}
	}
	
	for(int i=0;i<3;i++)
	{
		LOGV("Received - loudness bpf coeffs bes_loudness_bpf_coeff[0][0][%d]=%d", i, pCustomPara->bes_loudness_bpf_coeff[0][0][i]);
	}

	LOGV("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	LOGV("Received - HCF Param bes_loudness_DRC_Forget_Table[0][0] = %d",pCustomPara->bes_loudness_DRC_Forget_Table[0][0]);
	LOGV("Received - HCF Param bes_loudness_WS_Gain_Max = %d",pCustomPara->bes_loudness_WS_Gain_Max);
	LOGV("Received - HCF Param bes_loudness_WS_Gain_Min = %d",pCustomPara->bes_loudness_WS_Gain_Min);
	LOGV("Received - HCF Param bes_loudness_Filter_First = %d",pCustomPara->bes_loudness_Filter_First);
	LOGV("Received - HCF Param bes_loudness_Filter_First = %d",pCustomPara->bes_loudness_Filter_First);
	for(int i=0;i<5;i++)
	{
		LOGV("Received - gain map in bes_loudness_Gain_Map_In[%d]=%d", i, pCustomPara->bes_loudness_Gain_Map_In[i]);
	}

	for(int i=0;i<5;i++)
	{
		LOGV("Received - gain map out bes_loudness_Gain_Map_Out[%d]=%d", i, pCustomPara->bes_loudness_Gain_Map_Out[i]);
	}
	
	memcpy((void *)pHCFPara, (void *)pCustomPara, sizeof(AUD_ACF_PARAM_STRUCT));
		
	write_size = SetHeadphoneCompFltCustParamToNV(pHCFPara);
	if (write_size!=sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT)) {
		LOGD("DLCustHCFParamToNV Down load to NVRAM fail, structure size=%d,writed_size=%d",sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT),write_size);
		free(pHCFPara);
		pHCFPara = NULL;
		return ACHFailed;
	}
		
	AudioSystem::setParameters(0, String8("UpdateACFHCFParameters=1"));

	free(pHCFPara);
	pHCFPara = NULL;
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::ULCustHCFParamFromNV(void *pParam, int *len)
{
	int size = 0;

	LOGD("AudioCmdHandler::ULCustHCFParamFromNV() in");

	AUD_ACF_PARAM_STRUCT *pCustomPara = NULL;
	AUDIO_ACF_CUSTOM_PARAM_STRUCT *pHCFPara = (AUDIO_ACF_CUSTOM_PARAM_STRUCT *)malloc(sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
	if (pHCFPara==NULL) {
		return ACHFailed;
	}
		
	LOGD("ULCustHCFParamFromNV ,acf param size=%d", sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT));
	size = GetHeadphoneCompFltCustParamFromNV(pHCFPara);
	if (size!=sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT)) {
		LOGD("ULCustHCFParamFromNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_ACF_CUSTOM_PARAM_STRUCT),size);
		free(pHCFPara);
		pHCFPara = NULL;
		return ACHFailed;
	}
		
	for(int i=0; i<9; i++) {
		for(int j=0;j<4 ;j++){
			LOGV("Ori  - loudness hsf coeffs bes_loudness_hsf_coeff[%d][%d] = %d", i, j, pHCFPara->bes_loudness_hsf_coeff[i][j]);
		}
	}
	
	for(int i=0;i<3;i++)
	{
		LOGV("Ori - loudness bpf coeffs bes_loudness_bpf_coeff[0][0][%d]=%d", i, pHCFPara->bes_loudness_bpf_coeff[0][0][i]);
	}

	LOGV("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	LOGV("Ori - HCF Param bes_loudness_DRC_Forget_Table[0][0] = %d", pHCFPara->bes_loudness_DRC_Forget_Table[0][0]);
	LOGV("Ori - HCF Param bes_loudness_WS_Gain_Max = %d", pHCFPara->bes_loudness_WS_Gain_Max);
	LOGV("Ori - HCF Param bes_loudness_WS_Gain_Min = %d", pHCFPara->bes_loudness_WS_Gain_Min);
	LOGV("Ori - HCF Param bes_loudness_Filter_First = %d", pHCFPara->bes_loudness_Filter_First);
	LOGV("Ori - HCF Param bes_loudness_Filter_First = %d", pHCFPara->bes_loudness_Filter_First);
	for(int i=0;i<5;i++)
	{
		LOGV("Ori - gain map in bes_loudness_Gain_Map_In[%d]=%d", i, pHCFPara->bes_loudness_Gain_Map_In[i]);
	}

	for(int i=0;i<5;i++)
	{
		LOGV("Ori - gain map out bes_loudness_Gain_Map_Out[%d]=%d", i, pHCFPara->bes_loudness_Gain_Map_Out[i]);
	}

	*((int *)pParam) = sizeof(AUD_ACF_PARAM_STRUCT);
	pCustomPara = (AUD_ACF_PARAM_STRUCT *)(pParam + sizeof(int));
	
	memcpy((void *)pCustomPara, (void *)pHCFPara, sizeof(AUD_ACF_PARAM_STRUCT));

	dataEncode((char *)pParam, sizeof(AUD_ACF_PARAM_STRUCT)+sizeof(int));
	*len = 2*(sizeof(AUD_ACF_PARAM_STRUCT)+sizeof(int));
	
	free(pHCFPara);
	pHCFPara = NULL;
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::DLCustDualMicParamToNV(void *pParam)
{
	LOGD("AudioCmdHandler::DLCustDualMicParamToNV() in");
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::ULCustDualMicParamFromNV(void *pParam, int *len)
{
	LOGD("AudioCmdHandler::ULCustDualMicParamFromNV() in");
	int size = 0;
	
	AUDIO_CUSTOM_EXTRA_PARAM_STRUCT *pDualMicPara = (AUDIO_CUSTOM_EXTRA_PARAM_STRUCT *)malloc(sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT));
	if (pDualMicPara==NULL||pParam==NULL) {
		return ACHFailed;
	}
		
	LOGD("ULCustDualMicParamFromNV ,dual mic param size=%d", sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT));
	size = Read_DualMic_CustomParam_From_NVRAM(pDualMicPara);
	if (size!=sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT)) {
		LOGD("ULCustDualMicParamFromNV Up load from NVRAM fail, structure size=%d,read_size=%d",sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT),size);
		free(pDualMicPara);
		pDualMicPara = NULL;
		return ACHFailed;
	}
#ifdef MTK_WB_SPEECH_SUPPORT
	for(int i=0;i<NUM_ABF_PARAM+NUM_ABFWB_PARAM;i++)
#else
	for(int i=0;i<NUM_ABF_PARAM;i++)
#endif
	{
		LOGV("Dual Mic parameters ABF[%d]=%d", i, pDualMicPara->ABF_para[i]);
	}
	
	*((int *)pParam) = sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT);	
	memcpy(pParam + sizeof(int), (void *)pDualMicPara, sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT));

	dataEncode((char *)pParam, sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT)+sizeof(int));
	*len = 2*(sizeof(AUDIO_CUSTOM_EXTRA_PARAM_STRUCT)+sizeof(int));
	
	free(pDualMicPara);
	pDualMicPara = NULL;
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::getPhoneSupportInfo(unsigned int *supportInfo)
{
	LOGD("AudioCmdHandler::getPhoneSupportInfo() in");
#ifdef MTK_WB_SPEECH_SUPPORT
	*supportInfo = *supportInfo | SUPPORT_WB_SPEECH;
#endif

#ifdef MTK_DUAL_MIC_SUPPORT
	*supportInfo = *supportInfo | SUPPORT_DUAL_MIC;
#endif

#ifdef MTK_AUDIO_HD_REC_SUPPORT
	*supportInfo = *supportInfo | SUPPORT_HD_RECORD;
#endif
	LOGD("AudioCmdHandler::getPhoneSupportInfo() supportInfo=%d", *supportInfo);
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::startRecorder(const char *filePath, int recordAudioSource)
{
	LOGD("AudioCmdHandler::startRecorder() in");
	int ret = OK;
	
	if (m_MediaRecorderClient.get()==NULL) {
		m_MediaRecorderClient = new MediaRecorder();
		if (m_pMediaRecorderListenner!=NULL)
			m_MediaRecorderClient->setListener(m_pMediaRecorderListenner);
	}else if(isRecording()) {
		stopRecorder();
    }
	
	ret = m_MediaRecorderClient->setAudioSource(recordAudioSource);
	if (ret != OK) {
		m_MediaRecorderClient->reset();
		LOGE("AudioCmdHandler::startRecorder-Fail to setAudioSource");
		return ACHFailed;
	}
	
	ret = m_MediaRecorderClient->setOutputFormat(OUTPUT_FORMAT_WAV);
	if (ret != OK) {
		m_MediaRecorderClient->reset();
		LOGE("AudioCmdHandler::startRecorder-Fail to setOutputFormat");
		return ACHFailed;
	}
	
	ret = m_MediaRecorderClient->setAudioEncoder(AUDIO_ENCODER_PCM);
	if (ret != OK) {
		m_MediaRecorderClient->reset();
		LOGE("AudioCmdHandler::startRecorder-Fail to setAudioEncoder");
		return ACHFailed;
	}

//	ret = m_MediaRecorderClient->setOutputFile(filePath);   //for opencore
	LOGD("AudioCmdHandler::filePath=%s",filePath);
	m_fd = open(filePath, O_RDWR|O_CREAT|O_TRUNC, S_IRWXU|S_IRWXG|S_IRWXO);
	if (m_fd==-1){
		m_MediaRecorderClient->reset();
		LOGE("AudioCmdHandler::Create file failed  errno = %d, m_fd =%d", errno, m_fd);
		return ACHFailed;
	}
	
	ret = m_MediaRecorderClient->setOutputFile(m_fd, 0, 0);
	if (ret != OK) {
		m_MediaRecorderClient->reset();
		close(m_fd);
		LOGE("AudioCmdHandler::startRecorder-Fail to setOutputFile");
		return ACHFailed;
	}

	if (recordAudioSource==Audio_I2S_IN) {
		AudioSystem::setParameters(0, String8("HQA_I2SREC=1"));
		ret = m_MediaRecorderClient->setParameters(String8("audio-param-number-of-channels=2"));
		if (ret != OK) {
			m_MediaRecorderClient->reset();
			close(m_fd);
			LOGE("AudioCmdHandler::startRecorder-Fail to set audio-param-number-of-channels");
			return ACHFailed;
		}
	} else {
		char param1[TEMP_ARRAY_SIZE];
		sprintf(param1, "max-duration=%d", m_RecordMaxDur);
		ret = m_MediaRecorderClient->setParameters(String8(param1));
		if (ret != OK) {
			m_MediaRecorderClient->reset();
			close(m_fd);
			LOGE("AudioCmdHandler::startRecorder-Fail to set max-duration");
			return ACHFailed;
		}

		char param2[TEMP_ARRAY_SIZE];
		sprintf(param2, "audio-param-number-of-channels=%d", m_RecordChns);
		ret = m_MediaRecorderClient->setParameters(String8(param2));
		if (ret != OK) {
			m_MediaRecorderClient->reset();
			close(m_fd);
			LOGE("AudioCmdHandler::startRecorder-Fail to set audio-param-number-of-channels");
			return ACHFailed;
		}
		
		if (recordAudioSource==Audio_FM_IN)
			AudioSystem::setParameters(0, String8("HQA_FMREC_LINE_IN=1"));
	}

	char param4[TEMP_ARRAY_SIZE];
	sprintf(param4, "audio-param-sampling-rate=%d", m_RecordSampleRate);
	ret = m_MediaRecorderClient->setParameters(String8(param4));
	if (ret != OK) {
		m_MediaRecorderClient->reset();
		close(m_fd);
		LOGE("AudioCmdHandler::startRecorder-Fail to set audio-param-sampling-rate");
		return ACHFailed;
	}

	ret = m_MediaRecorderClient->prepare();
	if (ret != OK) {
		m_MediaRecorderClient->reset();
		close(m_fd);
		LOGE("AudioCmdHandler::startRecorder-Fail to prepare");
		return ACHFailed;
	}
	
	ret = m_MediaRecorderClient->start();
	if (ret != OK) {
		m_MediaRecorderClient->reset();
		close(m_fd);
		LOGE("AudioCmdHandler::startRecorder-Fail to start");
		return ACHFailed;
	}

	if (recordAudioSource==Audio_I2S_IN) {
		ret = startFMAudioPlayer();
		if (ret!=ACHSucceeded) {
			m_MediaRecorderClient->reset();
			close(m_fd);
			LOGE("AudioCmdHandler::startRecorder-Fail to startFMAudioPlayer");
			return ACHFailed;
		}
	}

	m_bRecording = true;
	m_RecordAudioSource = (AudioSourceType)recordAudioSource;
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::stopRecorder()
{
	LOGD("AudioCmdHandler::stopRecorder() in");

	int ret = OK;
	if (m_MediaRecorderClient.get()==NULL) {
		LOGE("AudioCmdHandler::stopRecorder-have not start recording");
		return ACHFailed;
	}

	if (!isRecording())
		return ACHFailed;
	
	if (m_RecordAudioSource==Audio_I2S_IN)
		stopFMAudioPlayer();
	
//	ret = m_MediaRecorderClient->stop();
//	if (ret!=OK && ret!=INVALID_OPERATION)
//		LOGE("AudioCmdHandler::stopRecorder-fail to stop recorder");

	m_MediaRecorderClient->reset();
	if (m_RecordAudioSource==Audio_I2S_IN) {
		AudioSystem::setParameters(0, String8("HQA_I2SREC=0"));
	} else if (m_RecordAudioSource==Audio_FM_IN) {
		AudioSystem::setParameters(0, String8("HQA_FMREC_LINE_IN=0"));
	}
		
	m_bRecording = false;
	close(m_fd);
	return ACHSucceeded;
}

ACHStatus AudioCmdHandler::startFMAudioPlayer()
{
	LOGD("AudioCmdHandler::startFMAudioPlayer() in");

	if (m_FMMediaPlayerClient.get()==NULL) {
		m_FMMediaPlayerClient = new MediaPlayer();
	}else if (m_FMMediaPlayerClient->isPlaying()) {
		m_FMMediaPlayerClient->stop();
		m_FMMediaPlayerClient->reset();
	}

	if (m_FMMediaPlayerClient->setDataSource("MEDIATEK://MEDIAPLAYER_PLAYERTYPE_FM",0)!=NO_ERROR) {
		m_FMMediaPlayerClient->reset();
		LOGE("AudioCmdHandler::startFMAudioPlayer-fail to create FM Audio player");
		return ACHFailed;
	}
	
//    m_MediaPlayerClient->setAudioStreamType(AUDIO_STREAM_MUSIC);
	if (m_FMMediaPlayerClient->prepare()!=NO_ERROR) {
		m_FMMediaPlayerClient->reset();
		LOGE("AudioCmdHandler::startFMAudioPlayer-fail to play I2S data, FMAudioPlayer prepare failed");
		return ACHFailed;
	}
	
	if (m_FMMediaPlayerClient->start()!=NO_ERROR) {
		m_FMMediaPlayerClient->reset();
		LOGE("AudioCmdHandler::startFMAudioPlayer-fail to play I2S data, FMAudioPlayer start failed");
		return ACHFailed;
	}

	return ACHSucceeded;
}


ACHStatus AudioCmdHandler::stopFMAudioPlayer()
{
	LOGD("AudioCmdHandler::stopAudioPlayer() in");

	if (m_FMMediaPlayerClient.get()!=NULL && m_FMMediaPlayerClient->isPlaying()) {
		if (m_FMMediaPlayerClient->stop()!=NO_ERROR) {
			LOGE("AudioCmdHandler::stopFMAudioPlayer-fail to stop playing I2S data");
			return ACHFailed;
		}
		m_FMMediaPlayerClient->reset();
		return ACHSucceeded;
	}

	LOGE("AudioCmdHandler::stopFMAudioPlayer-fail to stop playing I2S data");
	return ACHFailed;
}


//*****************Media Record Listener*********************//
PCMRecorderListener::PCMRecorderListener(AudioCmdHandler *pListener) :
	m_pListener(pListener)
{
}

void PCMRecorderListener::notify(int msg, int ext1, int ext2)
{
	LOGD("AudioCmdHandler::PCMRecorderListener::notify-received message: msg=%d, ext1=%d, ext2=%d", msg, ext1, ext2);
	switch(msg)
	{
	  case MEDIA_RECORDER_EVENT_INFO:
		switch (ext1) 
		{
	  	  case MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
	  	        if (m_pListener!=NULL)
			    m_pListener->stopRecorder();
		        break;
		  default:
	  	        break;
		}
		break;
	  case MEDIA_RECORDER_EVENT_ERROR:
		if (m_pListener!=NULL)
		    m_pListener->stopRecorder();
		break;
	  default:
	  	break;
	}
}


