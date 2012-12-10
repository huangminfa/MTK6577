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
 * AudioCmdHandler.h
 *
 * Project:
 * --------
 *   Android
 *
 * Description:
 * ------------
 *   The audio command handling interface API.
 *
 * Author:
 * -------
 *   Donglei Ji(mtk80823)
 *
 *******************************************************************************/

#ifndef _AUDIO_CMD_HANDLER_H_
#define _AUDIO_CMD_HANDLER_H_

/*=============================================================================
 *                              Include Files
 *===========================================================================*/

#include <pthread.h>
#include <media/mediaplayer.h>
#include <media/mediarecorder.h>

using namespace android;

//<--- for speech parameters calibration
#define MAX_VOL_CATE 3
#define CUSTOM_VOL_STEP 7

#define SPH_MODE_NUM 4
#define FIR_INDEX_NUM 6
#define FIR_NUM_NB 45
#define SPH_ENHANCE_PARAM_NUM 16
#define SPH_COMMON_NUM 12

#define FIR_NUM_WB 90
//--->

typedef struct{
    int param1;
	int param2;
	int param3;
	int param4;
	unsigned int recvDataLen;
}AudioCmdParam;

 typedef enum {
 	ACHFailed = -3,
	ACHLoadFileFailed,
	ACHParamError,
	ACHSucceeded
 }ACHStatus;

 typedef enum {
	Analog_MIC1_Single = 1,
	Analog_MIC2_Single = 2,
	Analog_MIC_Dual = 9,
	Audio_I2S_IN = 98,
	Audio_FM_IN= 99,
	
	AUDIO_SOURCE_LIST_END
 }AudioSourceType;

  typedef enum {
	SUPPORT_WB_SPEECH = 0x1,
	SUPPORT_DUAL_MIC = 0x2,
	SUPPORT_HD_RECORD = 0x4,
	
	SUPPORT_INFO_LIST_END
 }PhoneSupportInfo;
 //<--- for speech parameters calibration
 typedef enum {
  LOAD_VOLUME_POLICY=    0,
  SET_FM_SPEAKER_POLICY,
  
  AUDIO_POLICY_CNT
}SetPolicyParameters;

 typedef struct{
 	unsigned short sph_com_param[SPH_COMMON_NUM];
	unsigned short sph_mode_param[SPH_MODE_NUM][SPH_ENHANCE_PARAM_NUM];
	short sph_in_fir[FIR_INDEX_NUM][FIR_NUM_NB];
	short sph_out_fir[FIR_INDEX_NUM][FIR_NUM_NB];
	short sph_output_FIR_coeffs[SPH_MODE_NUM][FIR_INDEX_NUM][FIR_NUM_NB];
	short selected_FIR_output_index[SPH_MODE_NUM];
 }AUD_SPH_PARAM_STRUCT;

typedef struct{
	unsigned char audio_vol_mic[MAX_VOL_CATE][CUSTOM_VOL_STEP];
	unsigned char audio_vol_sph[MAX_VOL_CATE][CUSTOM_VOL_STEP];
	unsigned char audio_vol_sid[MAX_VOL_CATE][CUSTOM_VOL_STEP];
	unsigned char audio_vol_ring[MAX_VOL_CATE][CUSTOM_VOL_STEP];
	unsigned char audio_vol_key[MAX_VOL_CATE][CUSTOM_VOL_STEP];
	unsigned char audio_vol_fmr[MAX_VOL_CATE][CUSTOM_VOL_STEP];
	unsigned char audio_vol_media[MAX_VOL_CATE][CUSTOM_VOL_STEP];
	unsigned char audio_vol_matv[MAX_VOL_CATE][CUSTOM_VOL_STEP];
}AUD_SPH_VOL_STRUCT;

// for WB Speech tuning
typedef struct{
	unsigned short sph_mode_wb_param[SPH_MODE_NUM][SPH_ENHANCE_PARAM_NUM]; //WB speech enhancement 
	short sph_wb_in_fir[FIR_INDEX_NUM][FIR_NUM_WB]; // WB speech input FIR 
	short sph_wb_out_fir[FIR_INDEX_NUM][FIR_NUM_WB];// WB speech output FIR 
} AUD_SPH_WB_PARAM_STRUCT;

// for ACF/HCF tuning
typedef struct{
    unsigned int bes_loudness_hsf_coeff[9][4];     // Compensation Filter HSF coeffs
    unsigned int bes_loudness_bpf_coeff[4][6][3];  // Compensation Filter BPF coeffs
    unsigned int bes_loudness_DRC_Forget_Table[9][2];
    unsigned int bes_loudness_WS_Gain_Max;
    unsigned int bes_loudness_WS_Gain_Min;
    unsigned int bes_loudness_Filter_First;
    char bes_loudness_Gain_Map_In[5];
    char bes_loudness_Gain_Map_Out[5];
} AUD_ACF_PARAM_STRUCT;

//--->

/*=============================================================================
 *                              Class definition
 *===========================================================================*/

class AudioCmdHandler;

class PCMRecorderListener : public MediaRecorderListener
{
public:
	PCMRecorderListener(AudioCmdHandler *pListener);
	~PCMRecorderListener() {}
	
	void notify(int msg, int ext1, int ext2);

private:
	AudioCmdHandler *m_pListener;
};

class AudioCmdHandler : public MediaPlayerListener
{
public:
	AudioCmdHandler();
 	~AudioCmdHandler();
 	AudioCmdHandler(const AudioCmdHandler &);               
 	AudioCmdHandler & operator=(const AudioCmdHandler &); 

	// The  functions of processing audio cmds
	ACHStatus setRecorderParam(AudioCmdParam &audioCmdParams);
	ACHStatus startRecorderFrMIC(AudioCmdParam &audioCmdParams);
	ACHStatus setMICGain(AudioCmdParam &audioCmdParams);
	ACHStatus startRecorderFrFM(AudioCmdParam &audioCmdParams);
	ACHStatus playFM();
	ACHStatus stopPlayingFM();
	ACHStatus setVDPG(AudioCmdParam &audioCmdParams);
	ACHStatus setAUDLINEPG(AudioCmdParam &audioCmdParams);
	ACHStatus setFMorMICVUPG(AudioCmdParam &audioCmdParams, bool bFMGain);
	ACHStatus startAudioPlayer(AudioCmdParam &audioCmdParams);
	ACHStatus stopAudioPlayer();
	ACHStatus startRecorderFrI2S(AudioCmdParam &audioCmdParams);
	ACHStatus writeRegister(AudioCmdParam &audioCmdParams);
	String8 readRegister(AudioCmdParam &audioCmdParams);
	ACHStatus stopRecorder();
	void notify(int msg, int ext1, int ext2, const Parcel *obj = NULL);

	// add for mt6575 HQA
	void setParameters(const String8& keyValuePaires);

	//<--- for speech parameters calibration
	ACHStatus ULCustSPHParamFromNV(void *pParam, int *len, int block);
	ACHStatus DLCustSPHParamToNV(void *pParam, int block);
	
	ACHStatus ULCustSPHVolumeParamFromNV(void *pParam, int *len);
	ACHStatus DLCustSPHVolumeParamToNV(void *pParam);
	
	ACHStatus ULCustSPHWBParamFromNV(void *pParam, int *len, int block);
	ACHStatus DLCustSPHWBParamToNV(void *pParam, int block);

	ACHStatus ULCustACFParamFromNV(void *pParam, int *len);
	ACHStatus DLCustACFParamToNV(void *pParam);

	ACHStatus ULCustHCFParamFromNV(void *pParam, int *len);
	ACHStatus DLCustHCFParamToNV(void *pParam);
	
	ACHStatus ULCustDualMicParamFromNV(void *pParam, int *len);
	ACHStatus DLCustDualMicParamToNV(void *pParam);
	
	ACHStatus getPhoneSupportInfo(unsigned int *supportInfo);
	//--->

private:
	int m_RecordMaxDur;
	int m_RecordChns;
	int m_RecordSampleRate;
	int m_RecordBitsPerSample;
	int m_fd;
	bool m_bRecording;

  sp <MediaPlayer> m_MediaPlayerClient;
	sp <MediaPlayer> m_FMMediaPlayerClient;
	sp <MediaRecorder> m_MediaRecorderClient;
	sp <PCMRecorderListener> m_pMediaRecorderListenner;
	AudioSourceType m_RecordAudioSource;
	
	ACHStatus startRecorder(const char *filePath, int recordAudioSource);  
	ACHStatus startFMAudioPlayer();
	ACHStatus stopFMAudioPlayer();
	bool isRecording() {return m_bRecording; } 
};

#endif  //_AUDIO_COMP_FLT_CUST_PARAM_H_




