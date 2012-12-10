/*******************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2010
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
*******************************************************************************/

/*******************************************************************************
 * Filename:
 * ---------
 *  pano_core.h
 *
 * Project:
 * --------
 *   MT6236
 *
 * Description:
 * ------------
 *   This file is intends for Panorama core algorithm.
 *
 * Author:
 * -------
 *   Eric Liu (mtk03608)
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 * 04 26 2011 eric.liu
 * [MAUI_xxxxxxxx] [HAL PostProc] Rearrange PP interface
 *
 *
 * 02 22 2011 shouchun.liao
 * [MAUI_02841005] [Camera HAL] Camera HAL first version check-in
 * .
 *
 * 02 18 2011 shouchun.liao
 * [MAUI_02871948] [HAL PostProc] Rearrange PP interface
 * .
 *
 * 02 09 2011 shouchun.liao
 * [MAUI_02841005] [Camera HAL] Camera HAL first version check-in
 * revise pause/resume for bg stitch
 *
 * 12 28 2010 shouchun.liao
 * [MAUI_02841005] [Camera HAL] Camera HAL first version check-in
 * Check-in PP save log interface
 *
 * 12 08 2010 shouchun.liao
 * [MAUI_02841005] [Camera HAL] Camera HAL first version check-in
 * Revise PPI interface
 *
 * 11 30 2010 shouchun.liao
 * [MAUI_02841005] [Camera HAL] Camera HAL first version check-in
 * .Revise PPI & Panorama control
 *
 * 11 23 2010 shouchun.liao
 * [MAUI_02841005] [Camera HAL] Camera HAL first version check-in
 * [HAL] Post Process Check-in
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef __PANO_CORE_H__
#define __PANO_CORE_H__

#include "kal_release.h"
#include "pano_comm_def.h"

//////////////////tunning parameters/////////////////////////////////////
#define IMAGE_NORMALIZATION        (3)    // 3:normalization with ev and gamma information, 2: only gamma information, 1 no ev and gamma.
#define SUBSAMPLE_D_X              (4)    // Horizontal subsample for motion estimation
#define SUBSAMPLE_D_Y              (4)    // Vertical subsample for motion estimation
#define OVLP_RATIO                 (32)   // overlap ratio (base = 64, OVLP_RATIO = 16=>16/64=1/4, OVLP_RATIO = 21=>21/64~=1/3, OVLP_RATIO = 26=>26/64~=0.4)
#define PANO_MAX_IMG_NUM           (9)
#define MAX_SEAM_SIZE              (720)  // maximum seam array size (VGA or WVGA)
#define MAX_VECTOR_Y               (50)   //Max vertical motion vector allowed when the sweep direction is left or right
#define MAX_VECTOR_X               (50)   //Max horizontal motion vector allowed when the sweep direction is up or down


//////////////////const parameters/////////////////////////////////////////////
#define LOCAL_JUMP_RANGE           (8)    //The distance of local jumper
#define SAD_LOCAL                  (0)    //If (SAD>SAD_LOCAL) --> start local optimizion
#define MAX_BLEND_WIDTH_THR        (800)  //The blending width is relative to EV difference. The larger the EV differecne, the larger the MAX_BLEND_WIDTH_BOUND. The adjusted level is positively relative to this parameter.  
#define MAX_BLEND_WIDTH_BOUND      (50)   //The bound of the blending width of image stitch.   
#define OVERFLOW_STRENGTH_THR      (430)  //The blending width is relative to EV difference. The larger the EV differecne, the larger the MAX_BLEND_WIDTH_BOUND. The adjusted level is positively relative to this parameter. 
#define OVERFLOW_STRENGTH_BOUND    (4)    //The bound of the overflow controller of image stitch.
//The final equation is: 
//If seam_difference < MAX_BLEND_WIDTH/2 --> blend_width = seam_difference * 2;
//Else --> blend_width = MAX_BLEND_WIDTH + (seam_difference-MAX_BLEND_WIDTH/2) / OVERFLOW_STRENGTH;
#define SEAM_DIFF_THRESHOLD        (1170) //1024 represents the EV gain between neighboring image is 1x, 2048 represents 2x, etc. If the EV gain is higher than this value, seam would be the straight line; otherwise, it can be the curve. 
#define BLEND_DISTANCE_THRESHOLD   (2)    //>0 (1, 2, 3...etc). the higher then value, the more the blending strength 
#define MAX_ENABLE_GAIN            (4096) //Max compensation gain (4x = 4096, 3x = 3072, 2x=2048)     
#define SATURATION_PENALTY         (0)    //Extra difference for seam slection if one frame is saturated but the other is not (the higher the value, the less the possibility to cut in saturated region)
#define SATURATION_RATIO1          (100)  //It is the threshold range from 0~100. If the percetage of the saturated pixels higher than this percetage in overlapped region, another seam selection mechenism is applied  
#define SATURATION_RATIO2          (10)   //It is the threshold range from 0~100. The seam will cut until remaining saturated region is lower than this percetage
#define INITIAL_X                  (0)    //The initial X position of motion estimation
#define INITIAL_Y                  (0)    //The initial Y position of motion estimation
#define INITIAL_FULL_SEARCH_ENABLE (1)    //Intial full search enable signal
#define FULL_SEARCH_STEP           (2)    //The step of the full search 
#define FULL_SEARCH_RANGE          (8)    //The full search range of motion estimation
#define AWB_ENABLE                 (0)    //If AWB is enabled, deghost would consider UV infomation; otherwise, only Y information is considered
#define INITIAL_BLEND_FACTOR_INI   (30)   //Control the deghost strength when EV difference is zero. The larger this value is, the larger the deghost strength.
#define INITIAL_BLEND_FACTOR_THR   (900)  //Control the deghost strength based on EV difference and INITIAL_BLEND_FACTOR_INI. The larger this value is, the lower the deghost ability to higher EV differecne.
#define BLEND_WIDTH_GAIN           (100)  //It is used to control the blending width of image stitch. 100 represents 1x, 110 represents 1.1x, 90 represents 0.9x, etc.
#define BLEND_AVG_HEIGHT           (60)   //The blending width and scale is relative the average seam difference within the range defined by this parameter.

/****************************************************************************
 *          PANO Core Macro
 ****************************************************************************/

#define BLEND_WIDTH_INI         (60)        // blending window width
#define BLEND_ENABLE            (1)         // blending enable
#define BLEND_SCALE             (8)
#define SEAM_COST_THRES         (5)         // clamp cost to 0 if cost below threshold
#define VERTICAL_MARGIN_RATIO   (10)        // means 1/10
#define HORIZONTAL_MARGIN_RATIO (10)        // means 1/10
#define PANO_DBG_EXIF_BASE      (150+73)
#define COMMON_GAMMA            (1)
/****************************************************************************
 *          PANO Core Data Structure
 ****************************************************************************/
typedef enum
{
    PANO_IDLE = 0,
    PANO_INIT_START,
    PANO_ADD_FIRST_IMAGE_START,
    PANO_CYLINDRICAL_WARPPING_START,
    PANO_CLIP_PHASE1_START,
    PANO_CONVERT_GRAY_START,
    PANO_MOTIONESTIMATION_START,
    PANO_SEAMSELECTION_START,
    PANO_ADD_IMAGE_END,
    PANO_CALCULATEPANOSIZE_START,   
    PANO_CLIP_PHASE2_START,
    PANO_IMAGESTITCH_START,
    PANO_FINISH,
    PANO_STATE_MAX  
} PANO_OPERATION_STATE_ENUM;

typedef enum
{
    PANO_RESUME_ADD_IMAGE_STATE,
    PANO_RESUME_STITCH_STATE
} PANO_RESUME_STATE_ENUM;

typedef enum
{
    PANO_PROCESS_OK,
    PANO_ERROR_FORMAT_TYPE,
    PANO_ERROR_ADD_IMG_STATE,
    PANO_ERROR_CORE_PROCESS_STATE

} PANO_ERROR_ENUM;

typedef struct
{
    kal_int32 op_v;
    kal_int32 op_h;
    const kal_int32 *search_pos_pt;
    kal_int32 dir;
    kal_int32 op_dir;
    kal_int32 v_ori; 
    kal_int32 h_ori;
    kal_int32 kk;
    kal_int32 op_kk;
    kal_uint32 total_step_count;
    float op_ts;
    kal_bool first_time;
    kal_bool first_round;
	kal_bool small_search;
	kal_uint8 count_jump_localmax;
	float localmax_op_ts;
	kal_int32 localmax_x;
	kal_int32 localmax_y;
	kal_uint8 zero_optimize_y;
	kal_uint8 zero_optimize_x;
	kal_uint16 localmax_count;
} pano_motion_estimation_struct;

typedef struct
{
	kal_uint8 count_jump_localmax;
	float localmax_op_ts;
	kal_int32 localmax_x;
	kal_int32 localmax_y;
	kal_uint8 zero_optimize_y;
	kal_uint8 zero_optimize_x;
} pano_local_optimizer_struct;


typedef struct
{
    kal_uint32 snapshot_number;             // the number of images to be stitched
    kal_uint32 work_mem_addr;               // the address of working memory
    kal_uint32 work_mem_size;               // working memory size
    kal_uint32 jpeg_dec_ext_mem_addr;       // jpeg dec ext. working memory
    kal_uint32 jpeg_dec_ext_mem_size;       // jpeg dec ext. working memory size
    kal_uint8  *image_src_buffer_addr[PANO_MAX_IMG_NUM];    // source image buffer address 
    kal_uint32  image_src_buffer_size[PANO_MAX_IMG_NUM];    // source image buffer size   
    kal_int32  ev[PANO_MAX_IMG_NUM];                        // source image ev
} panoinfo_struct;
    

/* PANO_FEATURE_GET_ENV_INFO */
typedef struct
{
    kal_uint32  AddImageWorkingBufSize;            // add image working buffer size
    kal_uint32  StitchWorkingBufSize;              // stitch images working buffer size
    kal_uint16  OverlapRatio;                      // blend overlap ratio
} PANO_GET_INFO_STRUCT, *P_PANO_GET_INFO_STRUCT;

/* PANO_ENV_INFO_STRUCT (for PPI) */
typedef struct
{
    kal_uint16  SrcImgWidth;                       // source image width
    kal_uint16  SrcImgHeight;                      // source image height
    kal_uint16  MaxPanoImgWidth;                   // max output image width
    kal_uint8   MaxSnapshotNumber;                 // max captured image number
    MM_IMAGE_FORMAT_ENUM SrcImgFormat;             // image format during processing, currently, only support YUV420
    MM_IMAGE_FORMAT_ENUM DstImgFormat;             // image format after processing, currently, only support YUV420
    PANO_DIRECTION_ENUM StitchDirection;           // stitch direction
    kal_bool    FixAe;                             // fixAe or not, if false, system should provide the ev information
    kal_uint32  FocalLength;                       // lens focal length depends on lens, normally, this value can be set 750
    kal_bool    GPUWarp;                           // enable gpu cylindrical projection or not.
    MM_IMAGE_FORMAT_ENUM InputImgFormat;           // input image format and out image format
} PANO_ENV_INFO_STRUCT, *P_PANO_ENV_INFO_STRUCT;
    
//#define PANO_TIME_LOG
#define TIME_LOG_BUFFER_SIZE 50
typedef struct 
{
    PANO_OPERATION_STATE_ENUM state;
    kal_uint32 time_ticks;
    kal_uint32 cur_img_num;
} pano_time_log_struct;


/****************************************************************************
 *          PANO Core Interface
 ****************************************************************************/ 
PANO_ERROR_ENUM PanoCoreSetInfo(P_PANO_ENV_INFO_STRUCT pPanoInfo);
void PanoCoreGetInfo(P_PANO_GET_INFO_STRUCT pPanoInfo);
PANO_ERROR_ENUM PanoCoreStitch(panoinfo_struct* panoinfo);
PANO_ERROR_ENUM PanoCoreAddImage(panoinfo_struct* panoinfo);
//kal_uint8 PanoCoreProcess(void);
PANO_ERROR_ENUM PanoCoreProcess(kal_uint32* proc_time);
PANO_OPERATION_STATE_ENUM PanoCoreGetState(void);
void PanoCoreExit(void);
void PanoCorePause(void);
PANO_RESUME_STATE_ENUM PanoCoreResume(panoinfo_struct* pano_info_data);
void PanoCoreGetResult(P_PANO_RESULT_STRUCT pResult);
void PanoCoreSaveLog(char* pLogBuf);

#endif /* __PANO_CORE_H__ */
