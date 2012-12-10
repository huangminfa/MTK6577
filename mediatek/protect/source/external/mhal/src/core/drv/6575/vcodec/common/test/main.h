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

/**
 * @file
 *   main.h
 *
 * @par Project:
 *   MFlexVideo
 *
 * @par Description:
 *   Test Framework Declaration Type Definitions
 *
 * @par Author:
 *   Jackal Chen (mtk02532)
 *
 * @par $Revision: #24 $
 * @par $Modtime:$
 * @par $Log:$
 *
 */

#ifndef _MAIN_H_
#define _MAIN_H_

#ifdef __cplusplus
 extern "C" {
#endif

// Choose Platfrom Type
//#include "mfv_config.h"
//~

#define MAX_BUF_LEN (518400) //720x480x1.5
#define MAX_FILE_NAME_SIZE  (1024)
#define MAX_GMV_NUMBER      (1024)

//////////////////////////////////////////////////////////////////////////////
typedef struct {
    VAL_DRIVER_TYPE_T   eCodecType;
    VAL_CHAR_T          cInFile[MAX_FILE_NAME_SIZE];
    VAL_CHAR_T          cOutFile[MAX_FILE_NAME_SIZE];
    VAL_CHAR_T          cGoldenPath[MAX_FILE_NAME_SIZE];
    VAL_BOOL_T          bUseGolden;
    float               fTestErrorRate;
    VAL_VOID_T          *pvExtraParam;
} INPUT_CONFIG_T;

typedef struct {
    VAL_UINT32_T    u4FrameNum;
    VAL_UINT32_T    u4IntraPeriod;                  // DMEM[231]: iPeriodOfIntraFrames
    VAL_UINT32_T    u4FrameRate;                    // DMEM[24]:  frame_rate
    VAL_BOOL_T      fgpmv;                          // DMEM[113]: pmv
    VAL_UINT32_T    u4FrameWidth;                   // MFV_ENC_CONFIG, MFV_CONFIG
    VAL_UINT32_T    u4FrameHeight;                  // MFV_ENC_CONFIG, MFV_CONFIG
    VAL_UINT32_T    u4BitRate;                      // DMEM[23]:  bitrate
    VAL_BOOL_T      fgnewMD;                        // DMEM[80]:  newmd
    VAL_BOOL_T      fgHdrCbr;                       // DMEM[230]: hdr_cbr
    VAL_BOOL_T      fgShortHeaderMode;              // DMEM[1]:   short_video_header
    VAL_UINT32_T    u4VOPTimeIncrementResolution;   // DMEM[74]:  vop_time_increment_resolution
    VAL_UINT32_T    u4fcode;                        // DMEM[2]:   vop_fcode
    VAL_BOOL_T      fgResyncMarkerEn;               // DMEM[70]:  resync_en
    VAL_BOOL_T      fgResyncMarkerInsertMode;       // DMEM[71]:  resync_marker_insert_mode
    VAL_UINT32_T    u4ResyncMarkerPeriod;           // DMEM[72]:  resync_marker_period
    VAL_BOOL_T      fgHeaderExtensionCode;          // DMEM[73]:  header_extension_code
    VAL_BOOL_T      fgVOPRoundingType;              // DMEM[3]:   vop_rounding_type
    VAL_UINT32_T    u4StepLimit;                    // DMEM[114]: step_limit
    VAL_BOOL_T      fgInsertIntra;                  // DMEM[100]: insert_intra
    VAL_UINT32_T    u4InsertPeriod;                 // DMEM[101]: insert_period
    /*EIS Support*/
    VAL_BOOL_T      fgEISEnable;                    // Enable EIS feature
    VAL_UINT32_T    u4EISFrameWidth;                // EIS Frame Width
    VAL_UINT32_T    u4EISFrameHeight;               // EIS Frame Height
    VAL_UINT32_T    u4EIS_GMV_X[MAX_GMV_NUMBER];    // MFV_EIS_CONFIG, GMV_X
    VAL_UINT32_T    u4EIS_GMV_Y[MAX_GMV_NUMBER];    // MFV_EIS_CONFIG, GMV_Y
} INPUT_CONFIG_MP4ENC_T;

typedef struct {
    VAL_CHAR_T          cOutDir[MAX_FILE_NAME_SIZE];
} INPUT_CONFIG_H264DEC_T;

typedef struct {
    VAL_CHAR_T          cOutDir[MAX_FILE_NAME_SIZE];
} INPUT_CONFIG_VC1DEC_T;

typedef struct {
    VAL_UINT32_T    u4FrameNum;                 // DMEM[0]: iNumberOfFrames
    VAL_UINT32_T    u4FrameWidth;
    VAL_UINT32_T    u4FrameHeight;
    VAL_UINT32_T    u4PeriodOfIntraFrames;      // DMEM[1]: iPeriodOfIntraFrames
    VAL_UINT32_T    u4QPforIFrame;              // DMEM[3]: QP for I-frame
    VAL_BOOL_T      fgRateControlEn;            // DMEM[4]: rate control enable, when rate_control = 1, QP is unavailable
    VAL_UINT32_T    u4TargetBitRate;            // DMEM[5]: bit rate config
    VAL_UINT32_T    u4FrameRate;                // DMEM[6]: frame rate config
    VAL_UINT32_T    u4MVXLimit;                 // DMEM[7]: mvx search range
    VAL_UINT32_T    u4MVYLimit;                 // DMEM[8]: mvy search range
    /*EIS Support*/
    VAL_BOOL_T      fgEISEnable;                    // Enable EIS feature
    VAL_UINT32_T    u4EISFrameWidth;                // EIS Frame Width
    VAL_UINT32_T    u4EISFrameHeight;               // EIS Frame Height
    VAL_UINT32_T    u4EIS_GMV_X[MAX_GMV_NUMBER];    // MFV_EIS_CONFIG, GMV_X
    VAL_UINT32_T    u4EIS_GMV_Y[MAX_GMV_NUMBER];    // MFV_EIS_CONFIG, GMV_Y
} INPUT_CONFIG_H264ENC_T;

typedef enum {
    GOLDEN_TYPE_YUV,
    GOLDEN_TYPE_DEBLOCK_YUV,
    GOLDEN_TYPE_BS,
    GOLDEN_TYPE_YUV_CRC
} GOLDEN_TYPE_T;

//////////////////////////////////////////////////////////////////////////////
VAL_BOOL_T fgReadConfigFileList(VAL_CHAR_T *a_pcFileList, VAL_UINT32_T *a_pu4FileListPos, VAL_CHAR_T *a_pucConfigFileName);
VAL_BOOL_T fgReadConfigFile(VAL_CHAR_T *a_pcConfigFileName, INPUT_CONFIG_T *a_prConfig);

#if 1 //defined(_6573_MFLEX_SIM) || defined(_6573_LINUX)
VAL_BOOL_T fgCheckGolden(INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_uWidth, VAL_UINT32_T a_u4Height);
VAL_BOOL_T fgReadConfigFileMp4Dec(VAL_UINT32_T  *fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileMp4Enc(VAL_UINT32_T  *fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileH264Dec(VAL_UINT32_T *fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileH264Enc(VAL_UINT32_T *fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileRV9Dec (VAL_UINT32_T *fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileVC1Dec(VAL_UINT32_T  *fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileVP8Dec(VAL_UINT32_T  *fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgCheckGoldenPatternMp4Dec(INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_uWidth, VAL_UINT32_T a_u4Height);
VAL_BOOL_T fgCheckGoldenPatternMp4Enc(INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_uWidth, VAL_UINT32_T a_u4Height);
VAL_BOOL_T fgCheckGoldenPatternH264Dec(INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_uWidth, VAL_UINT32_T a_u4Height);
VAL_BOOL_T fgCheckGoldenPatternH264Enc(INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_uWidth, VAL_UINT32_T a_u4Height);
VAL_BOOL_T fgCheckGoldenPatternRV9Dec (INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_uWidth, VAL_UINT32_T a_u4Height);
VAL_BOOL_T fgCheckGoldenPatternVC1Dec(INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_uWidth, VAL_UINT32_T a_u4Height);
VAL_BOOL_T fgCheckGoldenPatternVP8Dec(INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_uWidth, VAL_UINT32_T a_u4Height);

//////////////////////////////////////////////////////////////////////////////
extern VAL_BOOL_T LocalMainMp4Dec(INPUT_CONFIG_T *a_prConfig);
extern VAL_BOOL_T LocalMainMp4Enc(INPUT_CONFIG_T *a_prConfig);
extern VAL_BOOL_T LocalMainH264Dec(INPUT_CONFIG_T *a_prConfig);
extern VAL_BOOL_T LocalMainH264Enc(INPUT_CONFIG_T *a_prConfig);
extern VAL_BOOL_T LocalMainRV9Dec (INPUT_CONFIG_T *a_prConfig);
extern VAL_BOOL_T LocalMainVC1Dec(INPUT_CONFIG_T *a_prConfig);
extern VAL_BOOL_T LocalMainVP8Dec (INPUT_CONFIG_T *a_prConfig);

#elif 0 //defined(_6573_FPGA) || defined(_6573_REAL_CHIP)

extern VAL_BOOL_T fgCheckGolden(INPUT_CONFIG_T *a_prConfig, GOLDEN_TYPE_T a_eGoldenType, VAL_UINT32_T a_u4FrmNum, VAL_UINT32_T *frame, VAL_UINT32_T a_u4Size);
VAL_BOOL_T fgReadConfigFileMp4Dec(VAL_UINT32_T fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileMp4Enc(VAL_UINT32_T fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileH264Dec(VAL_UINT32_T fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileH264Enc(VAL_UINT32_T fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileRV9Dec (VAL_UINT32_T fpInFile, INPUT_CONFIG_T *a_prConfig);
VAL_BOOL_T fgReadConfigFileVC1Dec(VAL_UINT32_T fpInFile, INPUT_CONFIG_T *a_prConfig);

#endif
//////////////////////////////////////////////////////////////////////////////
extern const VAL_UINT32_T crc32_table[256];
size_t ScrambleRead(void *buf, size_t size, size_t count, FILE *fp, float errorRate);
void PutErrorPatterns(VAL_UINT8_T *pBuffer, VAL_INT32_T length, float errorRate);



#ifdef __cplusplus
}
#endif

#endif // #ifndef _MAIN_H_
