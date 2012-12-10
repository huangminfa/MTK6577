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

#ifndef _RVBUFFER_H_
#define _RVBUFFER_H_

#include "alg_info.h"
#include "rvtypes.h"
#include "decdefs.h"

#define MAX_VDEC_SCRATCH_L1_SIZE_NOT_SUPPORT     0xFFFFFFFF
#define MAX_VDEC_SCRATCH_EXT_SIZE_NOT_SUPPORT     0xFFFFFFFF
#if defined(MT6236) || defined(MT6236B)

	#define MAX_VDEC_SCRATCH_L1_SIZE_128x96           89* 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_176x144          89* 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_320x240          89* 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_400x240          89* 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_352x288          89* 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_432x240          89* 1024    
	#define MAX_VDEC_SCRATCH_L1_SIZE_480x320          89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_640x368          89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_640x480          89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_720x480          89* 1024   
	#define MAX_VDEC_SCRATCH_L1_SIZE_800x480          89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_848x480          89* 1024     
	#define MAX_VDEC_SCRATCH_L1_SIZE_720x576          89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_800x608          89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1024x768         89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x720         89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x960         89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x1024        89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1408x1152        89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1600x1200        89* 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1920x1088        89* 1024  


    #define MAX_VDEC_SCRATCH_EXT_SIZE_128x96             21 * 1024                  
	#define MAX_VDEC_SCRATCH_EXT_SIZE_176x144            42 * 1024              
	#define MAX_VDEC_SCRATCH_EXT_SIZE_320x240            125 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_400x240            156 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_352x288            165 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_432x240            168 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_480x320            249 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_640x368            382 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_640x480            498 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_720x480            560 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_800x480            622 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_848x480            659 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_720x576            671 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_800x608            787 * 1024             
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1024x768          1282 * 1024            
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x720          1502 * 1024            
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x960          2003 * 1024            
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x1024         2136 * 1024            
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1408x1152         2643 * 1024            
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1600x1200         3129 * 1024            
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1920x1088         3404 * 1024 
#elif (defined(MT6253T)||defined(MT6253))

	#define MAX_VDEC_SCRATCH_L1_SIZE_128x96           30* 1024   
	#define MAX_VDEC_SCRATCH_L1_SIZE_176x144          30* 1024   
	#define MAX_VDEC_SCRATCH_L1_SIZE_320x240          30* 1024   
	#define MAX_VDEC_SCRATCH_L1_SIZE_400x240          30* 1024   
	#define MAX_VDEC_SCRATCH_L1_SIZE_352x288          30* 1024   
	#define MAX_VDEC_SCRATCH_L1_SIZE_432x240          30* 1024   
	#define MAX_VDEC_SCRATCH_L1_SIZE_480x320          30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_640x368          30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_640x480          30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_720x480          30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_800x480          30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_848x480          30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_720x576          30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_800x608          30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_1024x768         30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x720         30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x960         30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x1024        30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_1408x1152        30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_1600x1200        30 * 1024  
	#define MAX_VDEC_SCRATCH_L1_SIZE_1920x1088        30 * 1024  



    #define MAX_VDEC_SCRATCH_EXT_SIZE_128x96           21 * 1024                   
	#define MAX_VDEC_SCRATCH_EXT_SIZE_176x144          42 * 1024                 
	#define MAX_VDEC_SCRATCH_EXT_SIZE_320x240          125 * 1024                
	#define MAX_VDEC_SCRATCH_EXT_SIZE_400x240          156 * 1024                
	#define MAX_VDEC_SCRATCH_EXT_SIZE_352x288          165 * 1024                
	#define MAX_VDEC_SCRATCH_EXT_SIZE_432x240          168 * 1024                
	#define MAX_VDEC_SCRATCH_EXT_SIZE_480x320          249 * 1024                
	#define MAX_VDEC_SCRATCH_EXT_SIZE_640x368          382 * 1024                
	#define MAX_VDEC_SCRATCH_EXT_SIZE_640x480          498 * 1024                
	#define MAX_VDEC_SCRATCH_EXT_SIZE_720x480          564 * 1024  
	#define MAX_VDEC_SCRATCH_EXT_SIZE_800x480          627 * 1024  
	#define MAX_VDEC_SCRATCH_EXT_SIZE_848x480          664 * 1024  
	#define MAX_VDEC_SCRATCH_EXT_SIZE_720x576          677 * 1024  
	#define MAX_VDEC_SCRATCH_EXT_SIZE_800x608          793 * 1024  
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1024x768         1282 * 1024 
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x720         1502 * 1024 
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x960         2003 * 1024 
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x1024        2136 * 1024 
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1408x1152        2643 * 1024 
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1600x1200        3129 * 1024 
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1920x1088        3404 * 1024 

#elif defined(MT6253E)||defined(MT6253L)|| defined(MT6252H) || defined(MT6252)|| defined(MT6276) || defined(MT6575) || defined(MT6577)

	
	#define MAX_VDEC_SCRATCH_L1_SIZE_128x96           6 * 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_176x144          6 * 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_320x240          8 * 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_400x240          8 * 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_352x288          8 * 1024            
	#define MAX_VDEC_SCRATCH_L1_SIZE_432x240          9 * 1024 //LC       
	#define MAX_VDEC_SCRATCH_L1_SIZE_480x320          10 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_640x368          12 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_640x480          12 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_720x480          13 * 1024 //LC      
	#define MAX_VDEC_SCRATCH_L1_SIZE_800x480          14 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_848x480          15 * 1024 //LC      
	#define MAX_VDEC_SCRATCH_L1_SIZE_720x576          14 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_800x608          16 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1024x768         21 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x720         24 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x960         27 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1280x1024        28 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1408x1152        33 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1600x1200        38 * 1024           
	#define MAX_VDEC_SCRATCH_L1_SIZE_1920x1088        42 * 1024 




	#define MAX_VDEC_SCRATCH_EXT_SIZE_128x96           21 * 1024        
	#define MAX_VDEC_SCRATCH_EXT_SIZE_176x144          42 * 1024      
	#define MAX_VDEC_SCRATCH_EXT_SIZE_320x240          125 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_400x240          156 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_352x288          165 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_432x240          168 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_480x320          249 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_640x368          382 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_640x480          498 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_720x480          560 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_800x480          622 * 1024+64*1024
	#define MAX_VDEC_SCRATCH_EXT_SIZE_848x480          659 * 1024+64*1024
	#define MAX_VDEC_SCRATCH_EXT_SIZE_720x576          671 * 1024+64*1024
	#define MAX_VDEC_SCRATCH_EXT_SIZE_800x608          787 * 1024     
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1024x768         1273 * 1024
    #if defined(USE_FRANK_DECODER)
    #define MAX_VDEC_SCRATCH_EXT_SIZE_1280x720         1491 * 1024+512000
    #else
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x720         1491 * 1024+144000
    #endif // frank
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x960         1988 * 1024    
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1280x1024        2121 * 1024    
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1408x1152        2624 * 1024    
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1600x1200        3106 * 1024
	#define MAX_VDEC_SCRATCH_EXT_SIZE_1920x1088        3379 * 1024

#else

	#error not supported chip!, please check the chip defined!

#endif


#if (defined(MT6253T)||defined(MT6253))
	//For ARM7TCM chip only!!
	extern U8 rv9_vdec_buffer[MAX_VDEC_SCRATCH_L1_SIZE + (MAX_NUM_VDEC_HANDLES * MAX_VDEC_CONTEXT_SIZE)];
	
#else
	// not ARM7 TCM chip 
#endif

// for 16x16 MB size
//#define MAX_MB_FRAME_WIDTH	(RV9DEC_MAX_WIDTH >> 4)
//#define MAX_MB_FRAME_HEIGHT	(RV9DEC_MAX_HEIGHT >> 4)

// for 8x8 block size
//#define MAX_SUB_BLK_WIDTH	(MAX_MB_FRAME_WIDTH << 1)
//#define MAX_SUB_BLK_HEIGHT	(MAX_MB_FRAME_HEIGHT << 1)


//#define MAX_MV_BUF_SIZE		(MAX_SUB_BLK_WIDTH * MAX_SUB_BLK_HEIGHT)
			
#define MAX_DIRB_TMP_BUF_SIZE	128
extern void VideoGetTCMBuffer_Int(U32 *offset);
extern void *VideoGetTCMBuffer(U32 GetSize, U32 *offset);
#endif /* _RVBUFFER_H_ */