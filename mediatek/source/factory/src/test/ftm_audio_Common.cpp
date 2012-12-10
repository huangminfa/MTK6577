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

/*******************************************************************************
 *
 * Filename:
 * ---------
 *   ftm_audio_Common.cpp
 *
 * Project:
 * --------
 *   MT6575/MT6573 + Android
 *
 * Description:
 * ------------
 *   Factory Mode Audio Test
 *
 * Author:
 * -------
 *
 *
 *------------------------------------------------------------------------------
 * $Revision:$
 * $Modtime:$
 * $Log:$
 *
 * 01 30 2012 donglei.ji
 * [ALPS00106007] [Need Patch] [Volunteer Patch]DMNR acoustic loopback feature
 * .
 *
 * 01 18 2012 donglei.ji
 * [ALPS00106007] [Need Patch] [Volunteer Patch]DMNR acoustic loopback feature
 * set dual mic input.
 *
 * 01 11 2012 donglei.ji
 * [ALPS00106007] [Need Patch] [Volunteer Patch]DMNR acoustic loopback feature
 * DMNR acoustic loopback check in.
 *
 * 12 27 2011 donglei.ji
 * [ALPS00106007] [Need Patch] [Volunteer Patch]DMNR acoustic loopback feature
 * change mic volume setting
 *
 * 12 26 2011 donglei.ji
 * [ALPS00106007] [Need Patch] [Volunteer Patch]DMNR acoustic loopback feature
 * DMNR Acoustic loopback check in.
 *
 * 12 14 2011 donglei.ji
 * [ALPS00101149] [Need Patch] [Volunteer Patch]AudioPlayer, AMR/AWB Playback ICS migration
 * Audio factory mode migration- remove mt6516 code.
 *
 * 10 12 2011 donglei.ji
 * [ALPS00079849] [Need Patch] [Volunteer Patch][Factory Mode] TF card test disturbs Ringtone test
 * adjust file handler correctly..
 *
 *
 *******************************************************************************/

#include "cust.h"
#include <fcntl.h>

#ifndef FEATURE_DUMMY_AUDIO



/*****************************************************************************
*                E X T E R N A L   R E F E R E N C E S
******************************************************************************
*/
#include "ftm_audio_Common.h"
#include <AudioYusuLad.h>
#include <AudioYusuCcci.h>
#include <AudioYusuVolumeController.h>
#include <AudioYusuDef.h>
#include <AudioYusuHeadsetMessage.h>
#include <AudioCustParam.h>
#include <AudioYusuLad.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/syscall.h>
#include <time.h>
#include <sched.h>
#include <pthread.h>
#include "SineWave_156Hz.h"
#include <AudioYusuHardware.h>

#include <AudioAfe.h>
#include <AudioAnalogAfe.h>
#include <AudioFtm.h>
#include <AudioIoctl.h>

/*****************************************************************************
*                     C O M P I L E R   F L A G S
******************************************************************************
*/



/*****************************************************************************
*                          C O N S T A N T S
******************************************************************************
*/
#define AUDIO_APPLY_MAX_GAIN (0xffff)
#define AUDIO_APPLY_BIG_GAIN (0xcccc)

/*****************************************************************************
*                         D A T A   T Y P E S
******************************************************************************
*/
enum audio_devices
{
    // output devices
    OUT_EARPIECE = 0,
    OUT_SPEAKER = 1,
    OUT_WIRED_HEADSET = 2,
    DEVICE_OUT_WIRED_HEADPHONE = 3,
    DEVICE_OUT_BLUETOOTH_SCO = 4
};

typedef struct
{
    uint32 rAUDIO_TOP_CON0;
    uint32 rAFE_DAC_CON0;
    uint32 rAFE_TOP_CONTROL_0;
    uint32 rAFE_DL_SRC1_1;
    uint32 rAFE_DL_SRC2_1;
    uint32 rAFE_DL_SRC2_2;
    uint32 rAFE_UL_SRC_1;
    uint32 rAFE_UL_SRC_0;
    uint32 rAFE_SDM_GAIN_STAGE;
    uint32 rAFE_DL_SDM_CON0;
    uint32 rAFE_CONN1;
    uint32 rAFE_CONN2;
    uint32 rAFE_CONN3;
} FTM_AFE_Reg;

typedef struct
{
    uint32 rAUDIO_CON0;
    uint32 rAUDIO_CON1;
    uint32 rAUDIO_CON2;
    uint32 rAUDIO_CON3;
    uint32 rAUDIO_CON4;
    uint32 rAUDIO_CON5;
    uint32 rAUDIO_CON6;
    uint32 rAUDIO_CON7;
    uint32 rAUDIO_CON8;
    uint32 rAUDIO_CON9;
    uint32 rAUDIO_CON10;
    uint32 rAUDIO_CON14;
    uint32 rAUDIO_CON17;
    uint32 rAUDIO_CON20;
    uint32 rAUDIO_CON21;
    uint32 rAUDIO_CON22;
    uint32 rAUDIO_CON23;
    uint32 rAUDIO_CON28;
    uint32 rAUDIO_CON29;
    uint32 rAUDIO_CON30;
    uint32 rAUDIO_CON31;
    uint32 rAUDIO_CON32;
    uint32 rAUDIO_CON33;
    uint32 rAUDIO_CON34;
    uint32 rAUDIO_NCP0;
    uint32 rAUDIO_NCP1;
    uint32 rAUDIO_LDO0;
    uint32 rAUDIO_LDO1;
    uint32 rAUDIO_LDO2;
    uint32 rAUDIO_GLB0;
    uint32 rAUDIO_GLB1;
    uint32 rAUDIO_REG1;
} FTM_ANA_Reg;

typedef struct
{
    uint32 rAUDIO_TOP_CON0;
    uint32 rAFE_DAC_CON0;
    uint32 rAFE_TOP_CONTROL_0;
    uint32 rAFE_DL_SRC1_1;
    uint32 rAFE_DL_SRC2_1;
    uint32 rAFE_DL_SRC2_2;
    uint32 rAFE_UL_SRC_1;
    uint32 rAFE_UL_SRC_0;
    uint32 rAFE_SDM_GAIN_STAGE;
    uint32 rAFE_DL_SDM_CON0;
    uint32 rAFE_CONN1;
    uint32 rAFE_CONN2;
    uint32 rAFE_CONN3;
    uint32 rAFE_CONN4;
    uint32 rAFE_UL_AGC5;
    uint32 rAFE_UL_AGC13;
} FTM_AFE_R;

typedef struct
{
    uint32 rAUDIO_CON0;
    uint32 rAUDIO_CON1;
    uint32 rAUDIO_CON2;
    uint32 rAUDIO_CON3;
    uint32 rAUDIO_CON4;
    uint32 rAUDIO_CON5;
    uint32 rAUDIO_CON6;
    uint32 rAUDIO_CON7;
    uint32 rAUDIO_CON8;
    uint32 rAUDIO_CON9;
    uint32 rAUDIO_CON10;
    uint32 rAUDIO_CON20;
    uint32 rAUDIO_CON21;
    uint32 rAUDIO_CON22;
    uint32 rAUDIO_CON23;
    uint32 rAUDIO_CON28;
    uint32 rAUDIO_CON29;
    uint32 rAUDIO_CON30;
    uint32 rAUDIO_CON31;
    uint32 rAUDIO_CON32;
    uint32 rVAUDP_CON2;
    uint32 rACIF_WR_PATH;
} FTM_Analog_R;

/*****************************************************************************
*                   G L O B A L      V A R I A B L E
******************************************************************************
*/
static android::AudioAfe *mAsmReg = NULL;
static android::AudioAnalog *mAnaReg = NULL;
static android::AudioFtm *mAudFtm = NULL;

static int mFd = -1;
static int bMetaAudioInited = false;
static android::LAD *mLad = NULL;
static android::AudioYusuVolumeController *mVolumeController = NULL;
static android::AudioYusuHeadSetMessager *mHeadSetMessager = NULL;

// for gen wave data
static bool WavegenInit = false;
static pthread_mutex_t WaveGenMutex;
static pthread_t hWabeGenThread ;
static int32 Vibratetime =0;
static bool VibrateTreadExit = false;
static FTM_ANA_Reg rFTM_ANA_Reg;
static FTM_AFE_Reg rFTM_AFE_Reg;

static FTM_Analog_R ftm_ana;
static FTM_AFE_R ftm_rec_reg;

#ifndef MTK_DUAL_MIC_SUPPORT
#define MAX_RECEIVER_GAIN_DB 12
#endif
/*****************************************************************************
*                        F U N C T I O N   D E F I N I T I O N
******************************************************************************
*/
#ifdef __cplusplus
extern "C" {
#endif

    void Audio_Set_Speaker_Vol(int level);
    void Audio_Set_Speaker_On(int Channel);
    void Audio_Set_Speaker_Off(int Channel);
    void Audio_Set_HeadPhone_On(int Channel);
    void Audio_Set_HeadPhone_Off(int Channel);
    void Audio_Set_Earpiece_On();
    void Audio_Set_Earpiece_Off();
    int Audio_Write_Vibrate_On(int millisecond);
    int Audio_Write_Vibrate_Off(void);

    static void *WaveGen_function(void *ptr)
    {
        LOGD("WaveGen_function");
        pthread_mutex_lock( &WaveGenMutex );
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        mAsmReg->Afe_Set_Stream_Attribute(1, 0x4|0x8,8000);  //PCM_16_BIT , CHANNEL_OUT_STEREO , 8k Hz
        mAsmReg->Afe_Set_Stream_Gain(0xffff);
        mAsmReg->Afe_Set_Timer(IRQ1_MCU, 1024); // time = 1024 sample (1024*2*2 = 4096 bytes/interrupt)
        mAsmReg->Afe_DL_Mute(AFE_MODE_DAC);
        mAsmReg->Afe_DL_Start(AFE_MODE_DAC);
        mAudFtm->Meta_Open_Analog(AUDIO_PATH);
        Audio_Set_Speaker_On(Channel_Stereo);
        mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();
        pthread_mutex_unlock( &WaveGenMutex );
        LOGD("AFE prepare finish");

        while( (!VibrateTreadExit) || (Vibratetime <= 0))
        {
            int bytes = ::write(mFd, Sine_156Hz, WaveSize);
            LOGD("VibrateTreadExit Vibratetime=%d",Vibratetime);
            usleep(250 * 1000);		// 250 ms
            pthread_mutex_lock( &WaveGenMutex);
            Vibratetime  -= 250;
            pthread_mutex_unlock( &WaveGenMutex);
            LOGD("WaveGen_function write bytes=%d, Vibratetime=%d",bytes,Vibratetime);
        }

        // handle for thread exit,close asm analog
        pthread_mutex_lock( &WaveGenMutex);
        VibrateTreadExit = false;
        WavegenInit = false;
        Vibratetime = 0;

        Audio_Set_Speaker_Off(Channel_Stereo);
        mAsmReg->Afe_DL_Stop(AFE_MODE_DAC);
        mAudFtm->Meta_Close_Analog();
        pthread_mutex_unlock( &WaveGenMutex);
        return 0;
    }

//start vibrator with millicseond
    int Audio_Write_Vibrate_On(int millisecond)
    {
        int ret =0;
        LOGD("Audio_Write_Vibrate_On with millisecond = %d",millisecond);
        pthread_mutex_lock( &WaveGenMutex );
        //create track and start output
        if(WavegenInit == false)
        {
            Vibratetime = millisecond;
            LOGD("WavegenInit == false  Vibratetime = %d",Vibratetime);
            pthread_mutex_unlock( &WaveGenMutex );
            Vibratetime += millisecond;
            WavegenInit = true;
            // create a thread for write to asm
            ret = pthread_create(&hWabeGenThread,NULL,WaveGen_function,NULL);
            pthread_mutex_lock( &WaveGenMutex );
        }
        //update for time need to vibrate.
        else
        {
            Vibratetime += millisecond;
            LOGD("WavegenInit == true Vibratetime = %d",Vibratetime);
        }
        pthread_mutex_unlock( &WaveGenMutex );
        return true;
    }
    int Audio_Write_Vibrate_Off(void)
    {
        LOGD("Audio_Write_Vibrate_Off");
        pthread_mutex_lock( &WaveGenMutex );
        if(WavegenInit == true)
        {
            VibrateTreadExit = true;
        }
        pthread_mutex_unlock( &WaveGenMutex );
        return true;
    }

    void Audio_Set_Speaker_Vol(int level)
    {
        mAudFtm->Audio_Set_Speaker_Vol(level);
    }

    void Audio_Set_Speaker_On(int Channel)
    {
        mAudFtm->Audio_Set_Speaker_On(Channel);
    }

    void Audio_Set_Speaker_Off(int Channel)
    {
        mAudFtm->Audio_Set_Speaker_Off(Channel);
    }

    void Audio_Set_HeadPhone_On(int Channel)
    {
        mAudFtm->Audio_Set_HeadPhone_On(Channel);
    }

    void Audio_Set_HeadPhone_Off(int Channel)
    {
        mAudFtm->Audio_Set_HeadPhone_Off(Channel);
    }

    void Audio_Set_Earpiece_On()
    {
        mAudFtm->Audio_Set_Earpiece_On();
    }

    void Audio_Set_Earpiece_Off()
    {
        mAudFtm->Audio_Set_Earpiece_Off();
    }

    int Common_Audio_init(void)
    {

        if(bMetaAudioInited == true)
        {
            LOGD("Common_Audio_init bMetaAudioInited == true, already init!!");

            if(mAnaReg != NULL)
            {
                //Request Analog clock before access analog hw
                mAnaReg->AnalogAFE_Request_ANA_CLK();
                mAnaReg->SetAnaReg(AUDIO_NCP0,0x102B,0xffff);
                mAnaReg->SetAnaReg(AUDIO_NCP1,0x0600,0xffff);
                mAnaReg->SetAnaReg(AUDIO_LDO0,0x1030,0xffff);
                mAnaReg->SetAnaReg(AUDIO_LDO1,0x3010,0xffff);
                mAnaReg->SetAnaReg(AUDIO_LDO2,0x0013,0xffff);
                mAnaReg->SetAnaReg(AUDIO_GLB0,0x2920,0xffff);
                mAnaReg->SetAnaReg(AUDIO_GLB1,0x0000,0xffff);
                mAnaReg->SetAnaReg(AUDIO_REG1,0x0001,0xffff);
                //Release Analog clock before after analog hw
                mAnaReg->AnalogAFE_Release_ANA_CLK();
            }

            ioctl(mFd,START_DL1_STREAM,0);  // init memory
            return true;
        }
        LOGD("Common_Audio_init bMetaAudioInited = %d",bMetaAudioInited);

        mFd = open("/dev/eac", O_RDWR);

        int err =0;
        /*  Init asm */
        mAsmReg = new android::AudioAfe(NULL);
        err = mAsmReg->Afe_Init(mFd);

        if(err == false)
        {
            LOGD("Afe_Init error");
            return false;
        }

        /* init analog  */
        mAnaReg = new android::AudioAnalog(NULL);
        err = mAnaReg->AnalogAFE_Init(mFd);

        if(err == false)
        {
            LOGD("AnalogAFE_Init error");
            return false;
        }

#ifdef  AUD_DL1_USE_SLAVE
        ioctl(mFd,SET_DL1_SLAVE_MODE, 1);  //Set Audio DL1 slave mode
        ioctl(mFd,INIT_DL1_STREAM,0x2700);    // init AFE
#else
        ioctl(mFd,SET_DL1_SLAVE_MODE, 0);  //Set Audio DL1 master mode
        ioctl(mFd,INIT_DL1_STREAM,0x3000);    // init AFE
#endif

        ioctl(mFd,START_DL1_STREAM,0);  // init memory


// Disable LAD related code for MT65xx and later chip
// for AP processor, no need to use LAD. (No need to control modem side.)
// Make the audio factory mode free from Modem side.
#ifdef FEATURE_FTM_ACSLB
        if(mLad == NULL)
        {
           mLad = new android::LAD(NULL);
           if( !mLad->LAD_Initial() ){
              LOGD("LAD_Initial error!");
              return false;
           }
           mLad->pCCCI->mFd = mFd;
           if(mLad->mHeadSetMessager->SetHeadInit() == false)
              LOGE("Common_Audio_init SetHeadInit error");

           fcntl(mLad->pCCCI->GetRxFd(), F_SETFD, FD_CLOEXEC);
           fcntl(mLad->pCCCI->GetTxFd(), F_SETFD, FD_CLOEXEC);
           LOGD("LAD create success!");
        }
#endif
        // make sure send speech parameters to modem side before loopback on
        mLad->LAD_SendSphParaProcedure();

        mAudFtm = new android::AudioFtm(NULL,mAsmReg,mAnaReg);
        err = mAudFtm->AudFtm_Init(mFd, mLad);

        mHeadSetMessager = new android::AudioYusuHeadSetMessager(NULL);
        if(mHeadSetMessager->SetHeadInit() == false)
            LOGE("Common_Audio_init SetHeadInit error");

        /* new an volume controller */
        mVolumeController = new android::AudioYusuVolumeController(mFd,NULL,mAsmReg,mAnaReg);
        mVolumeController->InitVolumeController ();
        mVolumeController->ApplyGain(AUDIO_APPLY_BIG_GAIN,OUT_SPEAKER);  //apply volume
        bMetaAudioInited = true;

        // inint thread mutex
        err = pthread_mutex_init(&WaveGenMutex, NULL);
        if ( err != 0 )
        {
            return -1;
        }
        Audio_Set_Speaker_Vol(12);
        Audio_Set_Speaker_Off(Channel_Stereo);
        //Turn-on PMU here
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        mAnaReg->SetAnaReg(AUDIO_NCP0,0x102B,0xffff);
        mAnaReg->SetAnaReg(AUDIO_NCP1,0x0600,0xffff);
        mAnaReg->SetAnaReg(AUDIO_LDO0,0x1030,0xffff);
        mAnaReg->SetAnaReg(AUDIO_LDO1,0x3010,0xffff);
        mAnaReg->SetAnaReg(AUDIO_LDO2,0x0013,0xffff);
        mAnaReg->SetAnaReg(AUDIO_GLB0,0x2920,0xffff);
        mAnaReg->SetAnaReg(AUDIO_GLB1,0x0000,0xffff);
        mAnaReg->SetAnaReg(AUDIO_REG1,0x0001,0xffff);
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();

        LOGD("-Common_Audio_init");
        return true;
    }


    int Common_Audio_deinit(void)
    {
        if(bMetaAudioInited == true)
        {
            LOGD("!Common_Audio_deinit bMetaAudioInited=%d",bMetaAudioInited);
            ioctl(mFd,STANDBY_DL1_STREAM,0);
            return true;
        }
        return true;
    }


    int PhoneMic_Receiver_Loopback(char echoflag)
    { 
        return mAudFtm->PhoneMic_Receiver_Loopback(echoflag);
    }

    int PhoneMic_EarphoneLR_Loopback(char echoflag)
    {
        return mAudFtm->PhoneMic_EarphoneLR_Loopback(echoflag);
    }

    int PhoneMic_SpkLR_Loopback(char echoflag)
    { 
        return mAudFtm->PhoneMic_SpkLR_Loopback(echoflag);
    }

    int HeadsetMic_EarphoneLR_Loopback(char bEnable, char bHeadsetMic)
    { 
        return mAudFtm->HeadsetMic_EarphoneLR_Loopback(bEnable, bHeadsetMic);
    }

    int HeadsetMic_SpkLR_Loopback(char echoflag)
    { 
        return mAudFtm->HeadsetMic_SpkLR_Loopback(echoflag);
    }

    int RecieverTest(char receiver_test)
    {
        LOGD("RecieverTest echoflag=%d",receiver_test);
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        if(receiver_test)
        {
            mAudFtm->FTM_AnaLpk_on();
            mAnaReg->SetAnaReg(AUDIO_CON3,0x01e0,0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON5,0x4400,0xffff); //Enable HS + HPL buffer
            mAnaReg->SetAnaReg(AUDIO_CON1,0x0404,0xffff); //adjust HS volume

            mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
            mAudFtm->Afe_Enable_SineWave(true);
            Audio_Set_Earpiece_On();
        }
        else
        {
            Audio_Set_Earpiece_Off();
            mAudFtm->Afe_Enable_SineWave(false);
            mAudFtm->FTM_AnaLpk_off();
        }
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();
        return true;
    }

    int LouderSPKTest(char left_channel, char right_channel)
    {
        LOGD("LouderSPKTest left_channel=%d, right_channel=%d",left_channel,right_channel);
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        int Speaker_Channel =0;
        if( left_channel == 0 && right_channel == 0)
        {
            mAudFtm->Afe_Enable_SineWave(false);
            mAudFtm->FTM_AnaLpk_off();
            Audio_Set_Speaker_Off(Channel_Stereo);
        }
        else
        {
            mAudFtm->FTM_AnaLpk_on();
            mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
            mAudFtm->Afe_Enable_SineWave(true);
            if(left_channel ==1 && right_channel == 1)
            {
                Audio_Set_Speaker_On(Channel_Stereo);
            }
            else if(right_channel ==1)
            {
                Audio_Set_Speaker_On(Channel_Right);
            }
            else if(left_channel == 1)
            {
                Audio_Set_Speaker_On(Channel_Left);
            }
        }
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();
        return true;
    }


#ifdef FEATURE_FTM_ACSLB
    int PhoneMic_Receiver_Acoustic_Loopback(int Acoustic_Type, int *Acoustic_Status_Flag, int bHeadset_Output)
    {
        LOGD("PhoneMic_Receiver_Acoustic_Loopback Acoustic_Type=%d, headset_available=%d",Acoustic_Type, bHeadset_Output);
        /*  Acoustic loopback
        *   0: Dual mic (w/o DMNR)acoustic loopback off
        *   1: Dual mic (w/o DMNR)acoustic loopback
        *   2: Dual mic (w/  DMNR)acoustic loopback off
        *   3: Dual mic (w/  DMNR)acoustic loopback
        */
        bool retval = true;
        android::LadInPutDevice_Line inputSource = android::LADIN_Microphone1;
        static int acoustic_status = 0;
        switch(Acoustic_Type) {
        case ACOUSTIC_STATUS:
            *Acoustic_Status_Flag = acoustic_status;
            break;
        case DUAL_MIC_WITHOUT_DMNR_ACS_OFF:
            // close single mic acoustic loopback
            mAudFtm->SetLoopbackOff();
            acoustic_status = DUAL_MIC_WITHOUT_DMNR_ACS_OFF;
            break;
        case DUAL_MIC_WITHOUT_DMNR_ACS_ON:
            // open dual mic acoustic loopback (w/o DMNR)
            mAudFtm->SetLoopbackOn(android::MD_DUAL_MIC_ACOUSTIC_LOOPBACK_WITHOUT_DMNR, bHeadset_Output);
            acoustic_status = DUAL_MIC_WITHOUT_DMNR_ACS_ON;
            break;
        case DUAL_MIC_WITH_DMNR_ACS_OFF:
            // close dual mic acoustic loopback
            mAudFtm->SetLoopbackOff();
            acoustic_status = DUAL_MIC_WITH_DMNR_ACS_OFF;
            break;
        case DUAL_MIC_WITH_DMNR_ACS_ON:
            // open dual mic acoustic loopback (w/ DMNR)
            mAudFtm->SetLoopbackOn(android::MD_DUAL_MIC_ACOUSTIC_LOOPBACK_WITH_DMNR, bHeadset_Output);
            acoustic_status = DUAL_MIC_WITH_DMNR_ACS_ON;
            break;
        default:
            break;
    }

    LOGD("PhoneMic_Receiver_Acoustic_Loopback out -");
    return retval;
    }
#endif

    int EarphoneTest(char bEnable)
    {
        LOGD("EarphoneTest bEnable=%d",bEnable);

        Audio_Set_Speaker_Off(Channel_Stereo);
        if(bEnable)
        {
            mAudFtm->FTM_AnaLpk_on();
            mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
            mAudFtm->Afe_Enable_SineWave(true);
            Audio_Set_HeadPhone_On(Channel_Stereo);
        }
        else
        {
            mAudFtm->Afe_Enable_SineWave(false);
            mAudFtm->FTM_AnaLpk_off();
            Audio_Set_Speaker_Off(Channel_Stereo);
            Audio_Set_HeadPhone_Off(Channel_Stereo);
        }
        return true;
    }


    int FMLoopbackTest(char bEnable)
    {
        LOGD("FMLoopbackTest bEnable=%d",bEnable);

        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        if(bEnable == true)
        {
            ::ioctl(mFd,START_DL1_STREAM,0);        // init DL1 Stream
            // enable Digital AFE
            // ...
            // enable Analog AFE
            mAnaReg->SetAnaReg(AUDIO_CON0,(0x4<<12),0xf000);
            mAnaReg->SetAnaReg(AUDIO_CON1,0x0C0C,MASK_ALL);
            mAnaReg->SetAnaReg(AUDIO_CON2,0x000C,MASK_ALL);
            mAnaReg->SetAnaReg(AUDIO_CON3,0x0070,MASK_ALL);  // enable voice buffer, audio left/right buffer
//      SetAnaReg(AUDIO_CON5,0x0220,MASK_ALL);  // FM mono playback (analog line in)
            mAnaReg->SetAnaReg(AUDIO_CON5,0x0110,MASK_ALL);  // FM stereo playback (analog line in)
#if 0
            //mAnaReg->AnalogAFE_Depop(FM_PATH_STEREO,true);
#else
            mAnaReg->SetAnaReg(AUDIO_NCP0,0x102B,0xffff);
            mAnaReg->SetAnaReg(AUDIO_NCP1,0x0600,0x0E00);
            mAnaReg->SetAnaReg(AUDIO_LDO0,0x1030,0x1fff);
            mAnaReg->SetAnaReg(AUDIO_LDO1,0x3010,0xffff);
            mAnaReg->SetAnaReg(AUDIO_LDO2,0x0013,0x0013);
            mAnaReg->SetAnaReg(AUDIO_GLB0,0x2920,0xffff);
            mAnaReg->SetAnaReg(AUDIO_GLB1,0x0000,0xffff);
            mAnaReg->SetAnaReg(AUDIO_REG1,0x0001,0x0001);

            mAnaReg->SetAnaReg(AUDIO_CON0,0x5000,0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON1,0x3f0C,0x3F3F);
            mAnaReg->SetAnaReg(AUDIO_CON2,0x000C,0x003F);
            mAnaReg->SetAnaReg(AUDIO_CON3,0x0030,0x01FF);
            mAnaReg->SetAnaReg(AUDIO_CON5,0x0110,MASK_ALL);

            mAnaReg->SetAnaReg(AUDIO_CON6,0x0533,0x0FFF);
            mAnaReg->SetAnaReg(AUDIO_CON7,0x003F,0x003F);
            mAnaReg->SetAnaReg(AUDIO_CON7,0x0000,0xC000);
            mAnaReg->SetAnaReg(AUDIO_CON8,0x0000,0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON9,0x0058,0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON14,0x0000,0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON17,0x0018,0xffff);
#endif
            mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
            Audio_Set_Speaker_On(Channel_Stereo);
        }
        else
        {
            mAsmReg->Afe_DL_Mute(AFE_MODE_DAC);
            //mAsmReg->ForceDisableSpeaker();
            Audio_Set_Speaker_Off(Channel_Stereo);
            // disable Digital AFE
            // ...
            // disable Analog AFE
            mAnaReg->SetAnaReg(AUDIO_CON0,0,0xf000);
            mAnaReg->SetAnaReg(AUDIO_CON3,0x0000,0x00f0);  // disable voice buffer, audio left/right buffer
            mAnaReg->SetAnaReg(AUDIO_CON5,0x0440,0x0ff0);  // disable FM mono playback (analog line in)

            mAnaReg->AnalogAFE_Depop(FM_PATH_STEREO,false);

            ::ioctl(mFd,STANDBY_DL1_STREAM,0);
        }
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();
        return true;
    }

    /*
    int EarphoneMicbiasEnable(int bMicEnable)
    {
       LOGD("EarphoneMicbiasEnable bEnable=%d",bMicEnable);
       mLad->LAD_SwitchMicBias((int32)bMicEnable);
       return true;
    }
    */

    unsigned int ATV_AudioWrite(void* buffer, unsigned int bytes)
    {
        return 0;
    }

    int ATV_AudAnalogPath(char bEnable)
    {
        LOGD("ATV_AudAnalogPath bEnable=%d",bEnable);
        FMLoopbackTest(bEnable);
        return true;
    }


    int ATV_AudPlay_On()
    {
        LOGD("ATV_AudPlay_On");
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        //mAnaReg->Meta_Open_Analog(AUDIO_PATH);
        //Temp solution
        mAnaReg->SetAnaReg(AUDIO_NCP0,0x102b,0xffff);
        mAnaReg->SetAnaReg(AUDIO_NCP1,0x0600,0xffff);
        //mAnaReg->SetAnaReg(AUDIO_NCP1,0x0000,0xffff);
        mAnaReg->SetAnaReg(AUDIO_LDO0,0x1030,0xffff);
        mAnaReg->SetAnaReg(AUDIO_LDO1,0x3010,0xffff);
        mAnaReg->SetAnaReg(AUDIO_LDO2,0x0013,0xffff);
        mAnaReg->SetAnaReg(AUDIO_GLB0,0x2920,0xffff);
        mAnaReg->SetAnaReg(AUDIO_GLB1,0x0000,0xffff);
        mAnaReg->SetAnaReg(AUDIO_REG1,0x0001,0xffff);
        mAnaReg->SetAnaReg(AUDIO_CON0,0x4000,0xffff); //Set Line-in gain
        mAnaReg->SetAnaReg(AUDIO_CON1,0x0c0c,0xffff); //Set HS/HPL gain
        mAnaReg->SetAnaReg(AUDIO_CON2,0x000c,0xffff); //Set HPR gain
        mAnaReg->SetAnaReg(AUDIO_CON3,0x01f0,0xffff); //Turn-on DAC and HP buffer
        mAnaReg->SetAnaReg(AUDIO_CON5,0x4440,0xffff); //Set HS/HPL/HPR MUX
        mAnaReg->SetAnaReg(AUDIO_CON6,0x0a44,0xffff); //Set buffer Ib/Iq current(thd=-91/-88dBc@16R/32R)
        mAnaReg->SetAnaReg(AUDIO_CON7,0x003f,0xffff); //Set ZCD bias current
        mAnaReg->SetAnaReg(AUDIO_CON8,0x0000,0xffff);
        mAnaReg->SetAnaReg(AUDIO_CON14,0x00c0,0xffff); //set DAC ref. gen.
        mAnaReg->SetAnaReg(AUDIO_CON17,0x0008,0xffff); //Enable HS pull-low
        mAnaReg->SetAnaReg(AUDIO_CON10,0x01a1,0xffff); //scrambler enable
        mAnaReg->SetAnaReg(AUDIO_CON9,0x0052,0xfffe);//AFIFO enable
        usleep(1);
        mAnaReg->SetAnaReg(AUDIO_CON9,0x0001,0x0001);
        //~Temp solution
        mAsmReg->Afe_DL_Start(AFE_MODE_FTM_I2S);
        //Temp solution
        mAsmReg->SetAfeReg(AFE_DAC_CON1,0x00000800,0x00000f00);//32kHz
        mAsmReg->SetAfeReg(AFE_DL_SRC2_1,0x60000000,0xf0000000);//32kHz
        //~Temp solution
        mAsmReg->Afe_Set_Stream_Gain(0xffff); // hardcore AFE gain
        mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
        Audio_Set_Speaker_On(Channel_Stereo);
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();
        return true;
    }

    int ATV_AudPlay_Off()
    {
        LOGD("ATV_AudPlay_Off");
        Audio_Set_Speaker_Off(Channel_Stereo);
        mAsmReg->Afe_DL_Stop(AFE_MODE_FTM_I2S);
        mAudFtm->Meta_Close_Analog();
        return true;
    }


    int Audio_I2S_Play(int enable_flag)
    {
        LOGD("Audio_I2S_Play : %d",enable_flag);
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();

        if(enable_flag == true)
        {
            LOGD("+Audio_I2S_Play true");
            //mAnaReg->Meta_Open_Analog(AUDIO_PATH);
            //Temp solution
            mAnaReg->SetAnaReg(AUDIO_NCP0,0x102b,0xffff);
            mAnaReg->SetAnaReg(AUDIO_NCP1,0x0600,0xffff);
            //mAnaReg->SetAnaReg(AUDIO_NCP1,0x0000,0xffff);
            mAnaReg->SetAnaReg(AUDIO_LDO0,0x1030,0xffff);
            mAnaReg->SetAnaReg(AUDIO_LDO1,0x3010,0xffff);
            mAnaReg->SetAnaReg(AUDIO_LDO2,0x0013,0xffff);
            mAnaReg->SetAnaReg(AUDIO_GLB0,0x2920,0xffff);
            mAnaReg->SetAnaReg(AUDIO_GLB1,0x0000,0xffff);
            mAnaReg->SetAnaReg(AUDIO_REG1,0x0001,0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON0,0x4000,0xffff); //Set Line-in gain
            mAnaReg->SetAnaReg(AUDIO_CON1,0x0c0c,0xffff); //Set HS/HPL gain
            mAnaReg->SetAnaReg(AUDIO_CON2,0x000c,0xffff); //Set HPR gain
            mAnaReg->SetAnaReg(AUDIO_CON3,0x01f0,0xffff); //Turn-on DAC and HP buffer
            mAnaReg->SetAnaReg(AUDIO_CON5,0x4440,0xffff); //Set HS/HPL/HPR MUX
            mAnaReg->SetAnaReg(AUDIO_CON6,0x0a44,0xffff); //Set buffer Ib/Iq current(thd=-91/-88dBc@16R/32R)
            mAnaReg->SetAnaReg(AUDIO_CON7,0x003f,0xffff); //Set ZCD bias current
            mAnaReg->SetAnaReg(AUDIO_CON8,0x0000,0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON14,0x00c0,0xffff); //set DAC ref. gen.
            mAnaReg->SetAnaReg(AUDIO_CON17,0x0008,0xffff); //Enable HS pull-low
            mAnaReg->SetAnaReg(AUDIO_CON10,0x01a1,0xffff); //scrambler enable
            mAnaReg->SetAnaReg(AUDIO_CON9,0x0052,0xfffe);  //AFIFO enable
            usleep(1);
            mAnaReg->SetAnaReg(AUDIO_CON9,0x0001,0x0001);
            //~Temp solution
            mAsmReg->Afe_DL_Start(AFE_MODE_FTM_I2S);
            //Temp solution
            mAsmReg->SetAfeReg(AFE_DAC_CON0,0x00000000,0x00000008);//Disable VUL
            mAsmReg->SetAfeReg(AFE_DAC_CON1,0x00000a00,0x00000f00);//48kHz
            mAsmReg->SetAfeReg(AFE_DL_SRC2_1,0x80000000,0xf0000000);//48kHz
            mAsmReg->SetAfeReg(AFE_CONN1,0x00200000,0xffffffff);//I00_O03_S
            mAsmReg->SetAfeReg(AFE_CONN2,0x00002000,0xffffffff);//I01_O04_S
            //~Temp solution
            mAsmReg->Afe_Set_Stream_Gain(0xffff); // hardcore AFE gain
            mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
            Audio_Set_Speaker_On(Channel_Stereo);
            ::ioctl(mFd,AUDDRV_SET_FM_I2S_GPIO,0);// enable FM use I2S
            LOGD("-Audio_I2S_Play true");
        }
        else
        {
            LOGD("-Audio_I2S_Play false");
            Audio_Set_Speaker_Off(Channel_Stereo);
            mAsmReg->Afe_DL_Stop(AFE_MODE_FTM_I2S);
            mAudFtm->Meta_Close_Analog();
            ::ioctl(mFd,AUDDRV_RESET_BT_FM_GPIO,0);// Reset GPIO pin mux
            LOGD("-Audio_I2S_Play false");
        }
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();

        return true;
    }

    int Audio_FMTX_Play(bool Enable, unsigned int  Freq)
    {
        LOGD("Audio_FMTX_Play : %d, Freq=%d ", Enable, Freq);
        LOGD("Audio_FMTX_Play : Enable =%d, Freq=%d ", Enable, Freq);
        if(Enable)
        {
            mAudFtm->FTM_AnaLpk_on();
            mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
        }
        else
        {
            mAudFtm->FTM_AnaLpk_off();
            mAsmReg->Afe_DL_Mute(AFE_MODE_DAC);
        }

        return mAudFtm->WavGen_SW_SineWave(Enable, Freq, 0); // 0: FM-Tx, 1: HDMI

        return true;
    }


    int Audio_HDMI_Play(bool Enable, unsigned int Freq)
    {
        LOGD("Audio_HDMI_Play(%d), Freq=%d ", Enable, Freq);
        LOGD("Audio_HDMI_Play : Enable =%d, Freq=%d ", Enable, Freq);
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        if(Enable)
        {
            mAnaReg->SetAnaReg(AUDIO_CON1, 0x000C, 0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON2, 0x000C, 0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON3, 0x01B0, 0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON9,0x0052,0xfffe);//AFIFO enable
            usleep(1);
            mAnaReg->SetAnaReg(AUDIO_CON9,0x0001,0x0001);
            mAnaReg->SetAnaReg(AUDIO_CON14, 0x00c0, 0xffff);
        }
        else
        {
            mAnaReg->SetAnaReg(AUDIO_CON1, 0, 0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON2, 0, 0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON3, 0, 0xffff);
        }
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();

        return mAudFtm->WavGen_SW_SineWave(Enable, Freq, 1); // 0: FM-Tx, 1: HDMI

        return true;
    }


    static void Audio_Wave_Afe_Set_Stereo(void)
    {
        uint32 reg_AFE_CONN1,reg_AFE_CONN2;
        LOGD("!!! Audio_Wave_Afe_Set_Stereo \n");

        mAsmReg->SetAfeReg(AFE_CONN1,0x0,0xffffffff);  // Connect -- DL1_L to DAC_L, bit26: I05-O03
        mAsmReg->SetAfeReg(AFE_CONN2,0x0,0xffffffff);  // Connect -- DL1_L to DAC_L, bit26: I05-O03

        mAsmReg->SetAfeReg(AFE_CONN1,(1<<26),(1<<26));  // Connect -- DL1_L to DAC_L, bit26: I05-O03
        mAsmReg->SetAfeReg(AFE_CONN2,(1<<18),(1<<18));  // Connect -- DL1_R to DAC_R, bit18: I06-O04
        mAsmReg->GetAfeReg(AFE_CONN1, &reg_AFE_CONN1);
        mAsmReg->GetAfeReg(AFE_CONN2, &reg_AFE_CONN2);
        mAsmReg->SetAfeReg(AFE_DAC_CON1,0x000000,0x100000); // bit20: DL1_MONO (memoryinterface)
        LOGD("Afe_Set_Stereo AFE_CONN1=%x, AFE_CONN2=%x,\n",reg_AFE_CONN1,reg_AFE_CONN2);
    }

//LCH: HW stereo to mono
//RCH: only RCH PCM
    static void Audio_Wave_Afe_Set_Mono(void)
    {

        uint32 reg_AFE_CONN1, reg_AFE_CONN2;
        LOGD("!!! Audio_Wave_Afe_Set_Mono \n");
        // Mix the stereo to mono to LCH(O3)
        // Need to check with HW designer
        mAsmReg->SetAfeReg(AFE_CONN1,0x0,0xffffffff);  // Connect -- DL1_L to DAC_L, bit26: I05-O03
        mAsmReg->SetAfeReg(AFE_CONN1,(1<<26),(1<<26));  // Connect -- DL1_L to DAC_L, bit26: I05-O03

        mAsmReg->SetAfeReg(AFE_CONN2,0x0,0xffffffff);  // Connect -- DL1_L to DAC_L, bit26: I05-O03
        mAsmReg->SetAfeReg(AFE_CONN2,(1<<17),(1<<17));  // Connect -- DL1_R to DAC_R, bit18: I05-O04
        mAsmReg->GetAfeReg(AFE_CONN1, &reg_AFE_CONN1);
        mAsmReg->GetAfeReg(AFE_CONN2, &reg_AFE_CONN2);
        mAsmReg->SetAfeReg(AFE_DAC_CON1,0x100000,0x100000); // bit20: DL1_MONO (memoryinterface)
        LOGD("HW S2M Afe_Set_Mono AFE_CONN1=%x, AFE_CONN2=%x,\n",reg_AFE_CONN1,reg_AFE_CONN2);
    }

// open file and write data to hardware
    static void *Audio_Wave_Playabck_routine(void *arg)
    {

        char *mAudioBuffer = NULL;
        short *mAudio8to16Buffer = NULL;
        short *mAudioFinalBuffer = NULL;
        int SizeByte=0;
        int Channels =0;
        int interruptinterval =0;

        WavePlayData *pWavePlaydata = (WavePlayData*)arg;
        if(pWavePlaydata == NULL || arg == NULL)
        {
            LOGD("Audio_Wave_Playabck_routine Exit \n");
            pthread_exit(NULL); // thread exit
            return NULL;
        }

        printf("pWavePlaydata open file %s \n",pWavePlaydata->FileName);
        pWavePlaydata->pFile =fopen(pWavePlaydata->FileName,"rb");
        //pWavePlaydata->pFile =fopen("/sdcard/testpattern1.wav","rb");
        if(pWavePlaydata->pFile == NULL)
        {
            printf("pWavePlaydata open file fail\n");
            pWavePlaydata->ThreadExit = true;
            pthread_exit(NULL); // thread exit
            return NULL;
        }

        // read wave header
        fread((void*)&pWavePlaydata->mWaveHeader,WAVE_HEADER_SIZE,1,pWavePlaydata->pFile);
        LOGD("BitsPerSample = %d",pWavePlaydata->mWaveHeader.BitsPerSample);
        LOGD("NumChannels = %d",pWavePlaydata->mWaveHeader.NumChannels);
        LOGD("SampleRate = %d",pWavePlaydata->mWaveHeader.SampleRate);
        // setting audiohardware

        mAudioBuffer = new char[WAVE_BUFFER_SIZE];
        int actualbuffersize =WAVE_BUFFER_SIZE;

        if(pWavePlaydata->mWaveHeader.BitsPerSample == 8)
        {
            actualbuffersize = actualbuffersize <<1;
            mAudio8to16Buffer = new short[actualbuffersize<<1];
        }
        if(pWavePlaydata->mWaveHeader.NumChannels == 1)
        {
            actualbuffersize = actualbuffersize <<1;
        }
        printf("pWavePlaydata open file %s \n",pWavePlaydata->FileName);
        printf("actualbuffersize = %d \n",actualbuffersize);
        mAudioFinalBuffer = new short[actualbuffersize<<1];

        Audio_Wave_Afe_Set_Stereo();

        bool mFlag_Aud_DL1_SlaveOn = ::ioctl(mFd, GET_DL1_SLAVE_MODE,0);
        if (mFlag_Aud_DL1_SlaveOn)
        {
            SizeByte = 9984;
        }
        else
        {
            SizeByte = 12288;
        }
        ::ioctl(mFd, AUD_SET_CLOCK, 1);
        ::ioctl(mFd, INIT_DL1_STREAM, SizeByte);    // init DL1
        ::ioctl(mFd, START_DL1_STREAM, 0);
        Channels = 0x1| 0x2; //CHANNEL_OUT_STEREO = (CHANNEL_OUT_FRONT_LEFT | CHANNEL_OUT_FRONT_RIGHT),
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        printf("ApplyGain \n");
        mVolumeController->ApplyGain(0xffff,OUT_SPEAKER);  //apply volume

        mAsmReg->Afe_Set_Stream_Attribute(1, Channels , pWavePlaydata->mWaveHeader.SampleRate);  //PCM_16_BIT , CHANNEL_OUT_STEREO , 8k Hz
        mAsmReg->Afe_Set_Stream_Gain(0xffff);
        pWavePlaydata->mWaveHeader.SampleRate>>6;
        mAsmReg->Afe_Set_Timer(IRQ1_MCU,pWavePlaydata->mWaveHeader.SampleRate>>6);
        mAudFtm->FTM_AnaLpk_on();
        mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
        mAsmReg->Afe_DL_Start(AFE_MODE_DAC);
        if (pWavePlaydata->i4Output == Output_HS)
        {
            mAnaReg->SetAnaReg(AUDIO_CON3,0x01e0,0xffff);
            mAnaReg->SetAnaReg(AUDIO_CON5,0x4400,0xffff); //Enable HS + HPL buffer
            mAnaReg->SetAnaReg(AUDIO_CON1,0x0404,0xffff); //adjust HS volume
            Audio_Set_Speaker_Off(Channel_Stereo);
            Audio_Set_Earpiece_On();
        }
        else if (pWavePlaydata->i4Output == Output_HP)
        {
            Audio_Set_Speaker_Off(Channel_Stereo);
            Audio_Set_HeadPhone_On(Channel_Stereo);
        }
        else if (pWavePlaydata->i4Output == Output_LPK)
        {
            Audio_Set_Speaker_On(Channel_Stereo);
        }
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();

        // continue render to hardware
        while(!feof(pWavePlaydata->pFile) && pWavePlaydata->ThreadExit == false)
        {
            int readdata = 0, writedata =0;
            readdata = fread(mAudioBuffer,WAVE_BUFFER_SIZE,1,pWavePlaydata->pFile);
            if(pWavePlaydata->mWaveHeader.BitsPerSample == 8 &&pWavePlaydata->mWaveHeader.NumChannels == 2)
            {
                int count = WAVE_BUFFER_SIZE;
                short *dst =mAudio8to16Buffer ;
                char  *src = mAudioBuffer;
                while(count --)
                {
                    *dst++ = (short)( *src++^0x80 ) << 8;
                }
                writedata = ::write(mFd, mAudio8to16Buffer, WAVE_BUFFER_SIZE*2); // write to asm hardware
                LOGV("8 2 Audio_Wave_Playabck_routine mAudio8to16Buffer write to hardware... read = %d writedata = %d",readdata,writedata);
            }
            else if(pWavePlaydata->mWaveHeader.BitsPerSample == 16 &&pWavePlaydata->mWaveHeader.NumChannels == 1)
            {
                int count = WAVE_BUFFER_SIZE;
                short  *src = (short*)mAudioBuffer;
                short *dst =mAudioFinalBuffer ;
                while(count--)
                {
                    *dst++ = *src ;
                    *dst++ = *src++ ;
                }
                writedata = ::write(mFd, mAudioFinalBuffer, WAVE_BUFFER_SIZE*2); // write to asm hardware
                LOGV("16 1 Audio_Wave_Playabck_routine mAudio8to16Buffer write to hardware... read = %d writedata = %d",readdata,writedata);
            }
            else if(pWavePlaydata->mWaveHeader.BitsPerSample == 8 &&pWavePlaydata->mWaveHeader.NumChannels == 1)
            {
                int count = WAVE_BUFFER_SIZE;
                short *dst =mAudio8to16Buffer ;
                char  *src = mAudioBuffer;
                while(count --)
                {
                    *dst++ = (short)( *src++^0x80 ) << 8;
                }
                count = WAVE_BUFFER_SIZE<<1;
                short *src1 = mAudio8to16Buffer;
                short *dst1 = mAudioFinalBuffer ;
                while(count--)
                {
                    *dst1++ = *src1;
                    *dst1++ = *src1;
                    src1++;
                }
                writedata = ::write(mFd, mAudioFinalBuffer, WAVE_BUFFER_SIZE*4); // write to asm hardware
                LOGV("8 1 Audio_Wave_Playabck_routine mAudio8to16Buffer write to hardware... read = %d writedata = %d",readdata,writedata);
            }
            else if(pWavePlaydata->mWaveHeader.BitsPerSample == 16 &&pWavePlaydata->mWaveHeader.NumChannels == 2)
            {
                writedata = ::write(mFd, mAudioBuffer, WAVE_BUFFER_SIZE); // write to asm hardware
                LOGV("16 2 Audio_Wave_Playabck_routine mAudioBuffer write to hardware... read = %d writedata = %d",readdata,writedata);
            }
        }
        //Request Analog clock before access analog hw
        mAnaReg->AnalogAFE_Request_ANA_CLK();
        mAsmReg->Afe_DL_Mute(AFE_MODE_DAC);
        Audio_Set_Speaker_Off(Channel_Stereo);
        Audio_Set_HeadPhone_Off(Channel_Stereo);
        Audio_Set_Earpiece_Off();
        mAsmReg->Afe_DL_Stop(AFE_MODE_DAC);
        mAudFtm->FTM_AnaLpk_off();
        ::ioctl(mFd, STANDBY_DL1_STREAM, 0);
        ::ioctl(mFd, AUD_SET_CLOCK, 0);
        //Release Analog clock after access analog hw
        mAnaReg->AnalogAFE_Release_ANA_CLK();

        if(mAudioBuffer)
        {
            delete[] mAudioBuffer;
            mAudioBuffer = NULL;
        }
        if(mAudio8to16Buffer)
        {
            delete[] mAudio8to16Buffer;
            mAudio8to16Buffer = NULL;
        }
        if(mAudioFinalBuffer)
        {
            delete[] mAudioFinalBuffer;
            mAudioFinalBuffer = NULL;
        }

        if(pWavePlaydata->pFile)
        {
            fclose(pWavePlaydata->pFile);
            pWavePlaydata->pFile = NULL;
        }

        // thread exit;
        pWavePlaydata->ThreadExit = true;
        pWavePlaydata->ThreadStart = false;

        printf("Audio_Wave_Playabck_routine Exit \n");
        pthread_exit(NULL); // thread exit
        return NULL;
    }


    int Audio_Wave_playback(void* arg)
    {
        printf("Audio_Wave_playback with arg = %x \n",arg);
        WavePlayData *pWavePlaydata = (WavePlayData*)arg;
        if(pWavePlaydata == NULL || arg == NULL)
        {
            LOGD("Audio_Wave_Playabck_routine Exit \n");
            pthread_exit(NULL); // thread exit
            return NULL;
        }

        if(pWavePlaydata->WavePlayThread == NULL)
        {
            // create playback thread
            printf("pthread_create WavePlayThread\n");
            pthread_create(&pWavePlaydata->WavePlayThread, NULL, Audio_Wave_Playabck_routine,arg);
            //pthread_join(pWavePlaydata->WavePlayThread, NULL);
        }
        printf("Audio_Wave_playback return \n");
        return true;
    }

    int Audio_READ_SPK_OC_STA(void)
    {
        int s4RegValue = 100;
        if (mFd>=0)
        {
            s4RegValue = ::ioctl(mFd,AUDDRV_AMP_OC_READ,0);// Set OC CFG
            printf("Audio_READ_SPK_OC_STA get status(%d) \n",s4RegValue);
        }
        else
        {
            printf("Audio_READ_SPK_OC_STA unable get mFd \n");
            s4RegValue = -1;
        }
        return s4RegValue;
    }

    int LouderSPKOCTest(char left_channel, char right_channel)
    {
        int Speaker_Channel =0;
        unsigned int u4RegValue = 0;
        if( left_channel == 0 && right_channel == 0)
        {
            mAudFtm->Afe_Enable_SineWave(false);
            mAudFtm->FTM_AnaLpk_off();
            u4RegValue = 0;
            //disable speaker and OC
            if (mFd>=0)
            {
                ::ioctl(mFd,AUDDRV_AMP_OC_CFG,u4RegValue);// Set OC CFG
                ::ioctl(mFd,AUDDRV_HQA_AMP_AMPEN,0);
            }
            else
            {
                printf("LouderSPKOCTest unable get mFd \n");
            }
        }
        else
        {
            mAudFtm->FTM_AnaLpk_on();
            mAsmReg->Afe_DL_Unmute(AFE_MODE_DAC);
            mAudFtm->Afe_Enable_SineWave(true);
            u4RegValue = ((unsigned int)0xC0 << 16 )+(unsigned int)0xC0;
            //enable speaker and OC
            if (mFd>=0)
            {
                ::ioctl(mFd,AUDDRV_HQA_AMP_MODESEL,1);//class-AB
                ::ioctl(mFd,AUDDRV_HQA_AMP_RECEIVER,0);//spk mode
                ::ioctl(mFd,AUDDRV_HQA_AMP_AMPEN,1);
                ::ioctl(mFd,AUDDRV_AMP_OC_CFG,u4RegValue);// Set OC CFG
            }
            else
            {
                printf("LouderSPKOCTest unable get mFd \n");
            }
        }
        return (true);
    }
#ifdef __cplusplus
};
#endif


#else  // dummy audio function   -->   #ifndef FEATURE_DUMMY_AUDIO


#ifdef __cplusplus
extern "C" {
#endif

    int Common_Audio_init(void)
    {
        return true;
    }

    int Common_Audio_deinit(void)
    {
        return true;
    }

    int PhoneMic_Receiver_Loopback(char echoflag)
    {
        return true;
    }

    int PhoneMic_EarphoneLR_Loopback(char echoflag)
    {
        return true;
    }

    int HeadsetMic_SpkLR_Loopback(char echoflag)
    {
        return true;
    }
    int PhoneMic_SpkLR_Loopback(char echoflag)
    {
        return true;
    }

    int RecieverTest(char receiver_test)
    {
        return true;
    }

    int LouderSPKTest(char left_channel, char right_channel)
    {
        return true;
    }

    int HeadsetMic_Receiver_Loopback(char bEnable, char bHeadsetMic)
    {
        return true;
    }

    int EarphoneTest(char bEnable)
    {
        return true;
    }

    int FMLoopbackTest(char bEnable)
    {
        return true;
    }
    /*
    int EarphoneMicbiasEnable(int bMicEnable)
    {
        return true;
    }
    */
    int Audio_I2S_Play(int enable_flag)
    {
        return true;
    }

    int Audio_Wave_playback(void* arg)
    {
        return true;
    }

    int Audio_READ_SPK_OC_STA(void)
    {
        return true;
    }

    int LouderSPKOCTest(char left_channel, char right_channel)
    {
        return true;
    }
#ifdef __cplusplus
};
#endif

#endif   // #ifndef FEATURE_DUMMY_AUDIO

