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

#ifndef _ASD_CORE_H_
#define _ASD_CORE_H_

#include "kal_release.h"
//#include "MTKAsd.h"		//#include "asd_comm_def.h"

#define ASD_IM_WIDTH    		(160)				// default max image width
#define ASD_IM_HEIGHT   		(120)				// default max image height
#define LAYER_SIZE (ASD_IM_WIDTH*ASD_IM_HEIGHT)	// 160x120

#define NUM_OF_ORT 4
#define ASD_HIST_SIZE 	g_max_dim*NUM_OF_ORT*NUM_OF_SCENE*2
#define MAX_SCORE 100
#define MIN_SCORE 0

#define NORMALIZATION_AVG (5<<SHIFT_BITS)
#define NORMALIZATION_MIN_NORM_1 0.005 //was 0.00001
#define NORMALIZATION_MIN_NORM NORMALIZATION_MIN_NORM_1 * SHIFT_ONE 

#define g_rad_q_no 1		// Q - Radius Quantization No.
#define g_th_q_no 8			// T - Angular quantization No. 
#define g_hist_th_q_no 8	// H - Histogram Quantization No.
#define g_hist_th_q_no_half g_hist_th_q_no/2

#define g_grid_point_number (g_rad_q_no *g_th_q_no +1)  //9
#define g_daisy_desc_size g_grid_point_number *g_hist_th_q_no //72
#define g_channel_bin_no 16
#define g_color_hist_size 48
#define g_desc_size (g_daisy_desc_size+g_color_hist_size) // 72+48

//daisy descriptor para
#define g_rad 3
#define g_grid_spacing 3
#define MAX_MRG_LONG 9
#define MAX_MRG_SHORT 7

// random clustering forest parameters
#define g_max_dim 256

//color histogram parameters
#define ASD_COLOR_VALUE_OFFSET 8
#define ASD_HIST_FEATURE_VALUE 0x14E5E0 // 2^26/49 (patch size)
#define ASD_BIN_NO(VALUE)   (((VALUE)>>4)&0xFF)


#define SHIFT_BITS 16
#define SHIFT_ONE (1<<SHIFT_BITS)
#define SHIFT_BITS_TRI 14

#define ASD_SVM_SUPPORT_VECTOR_NUM_L      237
#define ASD_SVM_VECTOR_DIMENSIONS_L     226
#define ASD_SVM_SUPPORT_VECTOR_NUM_B      285
#define ASD_SVM_VECTOR_DIMENSIONS_B     234

typedef enum
{
	ASD_SCENE_LANDSCAPE,	
	ASD_SCENE_BACKLIT,
	NUM_OF_SCENE
}ASD_SUPPORT_SCENE;

typedef struct
{
	kal_int32 rho;		// hyperplane off
	kal_int32 nsv;		// number of support vectors
	kal_int32 dim;        // number of dimensions of a vector
	kal_int32* coef;	// coefficients which applied to the kernel values
	kal_uint16* svData; // support vectors
}asd_svm_learning_data_struct;

typedef struct 
{	
	kal_uint16 m_dim; // the dimension of interest
	kal_uint8 m_threshold; 	// the threshold for the seperation
	kal_int16 m_clusterIndex;
	kal_uint16 m_left; 		// the left and right children
	kal_uint16 m_right;	
}asd_tree_node_struct;

typedef struct
{
	kal_int32 rho;		// hyperplane off
	kal_int32 nsv;		// number of support vectors
	kal_int32 dim;        // number of dimensions of a vector
	kal_int32 coef[ASD_SVM_SUPPORT_VECTOR_NUM_L];	// coefficients which applied to the kernel values
	kal_uint16 svData[ASD_SVM_SUPPORT_VECTOR_NUM_L][ASD_SVM_VECTOR_DIMENSIONS_L]; // support vectors
}ASD_SDATA_STRUCT_L,*P_ASD_SDATA_STRUCT_L; //asd_svm_learning_data_struct_L;

typedef struct
{
	kal_int32 rho;		// hyperplane off
	kal_int32 nsv;		// number of support vectors
	kal_int32 dim;        // number of dimensions of a vector
	kal_int32 coef[ASD_SVM_SUPPORT_VECTOR_NUM_B];	// coefficients which applied to the kernel values
	kal_uint16 svData[ASD_SVM_SUPPORT_VECTOR_NUM_B][ASD_SVM_VECTOR_DIMENSIONS_B]; // support vectors
}ASD_SDATA_STRUCT_B,*P_ASD_SDATA_STRUCT_B;//asd_svm_learning_data_struct_B;

typedef struct
{
	kal_int32 m_num_of_tree;		// number of trees of the forest
	kal_int32 m_max_depth;			// maximum depth of a tree
	kal_int32 m_min_num_of_features;	// minimum number features in a cluster
	kal_int32 m_T;					// maximum T
	float m_S;			// minimum S /
	kal_int32 m_num_of_cluster; 
	kal_int32 m_num_of_class; 
	kal_int32 m_num_of_feature_dim;
	asd_tree_node_struct tree_node[512];
}ASD_CDATA_STRUCT, *P_ASD_CDATA_STRUCT; //asd_cluster_forest_struct;	

typedef struct
{
	kal_uint16 *img_buffer_ptr;				// Pointer to RGB565 Image buffer
	kal_uint8 *working_buffer_ptr;				// Pointer to working buffer	
	kal_uint16 image_width;
	kal_uint16 image_height;
	kal_uint32 asd_start_time;
	kal_uint32 asd_end_time;
	kal_uint8 max_score[NUM_OF_SCENE];
	asd_svm_learning_data_struct svm_data_L;
	asd_svm_learning_data_struct svm_data_B;
	const ASD_CDATA_STRUCT* cluster_data_L;
	const ASD_CDATA_STRUCT* cluster_data_B;
	const asd_tree_node_struct* tree_node_ptr[NUM_OF_SCENE];
	const asd_svm_learning_data_struct* svm_data_ptr[NUM_OF_SCENE];	
} asd_cal_struct;

typedef struct
{
	kal_uint8 	num_of_ort;						/* 0 - 4 */    
} ASD_TUNING_PARA_STRUCT, *P_ASD_TUNING_PARA_STRUCT;

typedef struct
{
	kal_uint16 image_width;
	kal_uint16 image_height;
	kal_uint32 ext_mem_start_addr; //working buffer start address
	kal_uint32 ext_mem_size;
	kal_uint32 ImgFmtList;
	ASD_TUNING_PARA_STRUCT asd_tuning_data;	
} ASD_SET_ENV_INFO_STRUCT, *P_ASD_SET_ENV_INFO_STRUCT;


typedef struct
{
	kal_uint32 WorkingBufSize;
} ASD_GET_ENV_INFO_STRUCT, *P_ASD_GET_ENV_INFO_STRUCT;

typedef struct
{
	P_ASD_SET_ENV_INFO_STRUCT pAsdEnvInfo;	
	void* asd_sdata_L;
	void* asd_sdata_B;
	void* asd_cdata_L;
	void* asd_cdata_B;
} ASD_SET_INFO_STRUCT, *P_ASD_SET_INFO_STRUCT;

typedef struct
{	/* ASD results */
	kal_uint32 asd_start_time;
	kal_uint32 asd_end_time;	
	kal_uint8 asd_score[NUM_OF_SCENE]; // 0: landscape, 1: backlit
}ASD_RESULT_STRUCT,*P_ASD_RESULT_STRUCT;

typedef struct
{
    kal_uint32  SrcAddr;
    kal_uint32	DstAddr;
    kal_uint16  ImgWidth;
    kal_uint16  ImgHeight;
} ASD_IMAGE_INFO_STRUCT, *P_ASD_IMAGE_INFO_STRUCT;

/****************************************************************************
 *          Extern Time Log Interface
 ****************************************************************************/ 
//extern kal_uint32 drv_get_current_time(void);
//extern kal_uint32 drv_get_duration_ms(kal_uint32 previous_time);

#endif //_ASD_CORE_H_
