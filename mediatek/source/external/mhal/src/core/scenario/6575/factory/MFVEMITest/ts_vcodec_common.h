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

#ifndef _TS_VCODEC_COMMON_H_
#define _TS_VCODEC_COMMON_H_

#ifndef ANDROID_LOAD
#include "CTP_mem.h"
#include "common.h"
#define printf dbg_print

extern unsigned int file_rd_idx;
extern unsigned int file_wr_offset;
extern unsigned int file_rd_offset[3];
extern unsigned int vcodec_idx;
#if 0
#define malloc(size) MEM_Allocate(size, MEM_USER_VCODEC)
#else
#define malloc(size) MEM_Allocate_Align(size, 32, MEM_USER_VCODEC)
#endif


#define malloc_align(size,align) MEM_Allocate_Align(size,align,MEM_USER_VCODEC)
#define free(addr)	MEM_Release(addr)

extern char __DVT_MEM_TEST;
extern char __DVT_MEM_TEST_END;

#define VCODEC_PAT1_LEN		0x400
#define VCODEC_PAT2_LEN		0x20F5800  // 320x240x1.5x300
#define VCODEC_PAT3_LEN		0x20F5800
#define VCODEC_PAT4_LEN		0x1000000
#define VCODEC_PAT5_LEN		0x1000000

extern unsigned int VCODEC_PAT_LEN[4];

extern unsigned char * vcodec_pat1_addr;
extern unsigned char * vcodec_pat2_addr;
extern unsigned char * vcodec_pat3_addr;
extern unsigned char * vcodec_pat4_addr;
extern unsigned char * vcodec_pat5_addr;

extern void * fopen_ctp (void *filename, const char * mode);
extern int fread_ctp(void * ptr, size_t size, size_t count, void * stream);
extern void fclose_ctp(void * stream);
extern int fseek_ctp(void * stream, long int offset, int origin);
extern int fwrite_ctp(const void * ptr, size_t size, size_t count, void * stream);
extern int ftell_ctp(void *stream);
extern void rewind_ctp(void *stream);
extern void remove_ctp(void *stream);
extern int sprintf_ctp( char * str, const char * format, ... );


#define fopen(filename, mode)	fopen_ctp(filename, mode)
#define fclose(stream)	fclose_ctp(stream)
#define fread(ptr, size, count, stream)	fread_ctp(ptr, size, count, stream)
#define fseek(stream, offset, origin)	fseek_ctp(stream, offset, origin)
#define fwrite(ptr, size, count, stream)	fwrite_ctp(ptr, size, count, stream)
#define ftell(stream)	ftell_ctp(stream)
#define rewind(stream)	rewind_ctp(stream)
#define remove(filename)	remove_ctp(filename)
#define sprintf 	sprintf_ctp
#endif // #ifndef ANDROID_LOAD

#define INPUT_YUV_SIZE      4
#define INPUT_BTM_BUF_SIZE  4
#define MAX_INSTANCE_NUMBER 4

typedef struct 
{
	int				YUVBufferSize;
}DRIVER_HANDLER_T;

typedef struct 
{
	unsigned char	*vos_ptr;
	unsigned int	vos_size;
	unsigned int	frame_count;
}mp4_dec_input_struct;

#include "vcodec_OAL.h"
#include "vcodec_if.h"

#define MAX_CACHE_TABLE_SIZE 512
extern int       NonCachableBufferTableSize;
extern unsigned  NonCachableBufferTable[MAX_CACHE_TABLE_SIZE];


void VCodecMalloc(	IN HANDLE				hDrv, 
					IN unsigned int			u4Size, 
					IN VCODEC_MEMORY_TYPE_T	fgCacheable, 
					OUT VCODEC_BUFFER_T		*prBuf
				 );

void VCodecFree(	IN HANDLE				hDrv, 
					IN VCODEC_BUFFER_T		*prBuf
			   );

void VCodecSetYUVBuffer(	IN HANDLE		hDrv, 
							IN VCODEC_DEC_YUV_BUFFER_PARAM_T	*prYUVParam
					   );

void VCodecGetYUVBuffer(	IN HANDLE		hDrv,
							OUT VCODEC_BUFFER_T	*prYUVBuf
					   );

int VCodecRefFreeYUVBuffer(	IN HANDLE		hDrv,                                                       
							OUT VCODEC_BUFFER_T	*prYUVBuf
						  );

unsigned int VCodecGetAvailableYUV(	IN HANDLE	hDrv);

unsigned int VCodecGetTotalYUV(	IN HANDLE	hDrv);
                                                      
unsigned int VCodecGetAvailableDisplay(	IN HANDLE	hDrv);
                                                      
unsigned int VCodecGetRealAvailableDisplay(	IN HANDLE	hDrv);

unsigned int VCodecQueryInfo(IN HANDLE hDrv, IN VCODEC_DEC_QUERY_INFO_TYPE_T ID,void *pvQueryData);

void VCodecReturnBitstream(	IN HANDLE			hDrv,                                                       
							IN VCODEC_BUFFER_T	*prBuffer,
							IN unsigned int		u4BuffSize
                           );

static void CONSTRUCT_VIDEO_BUFFER_TEST(VCODEC_BUFFER_T *buffer, void *addr, unsigned status)
{
#ifdef ANDROID_LOAD
  buffer->pu1Buffer_VA = (unsigned char *) ((unsigned int) addr); 
#else
  buffer->pu1Buffer_VA = buffer->pu1Buffer_PA = (unsigned char *) ((unsigned int) addr | 0x80000000); 
#endif
  buffer->eBufferStatus = status;
}

static void CONSTRUCT_VIDEO_BUFFER_TEST_VA_PA(VCODEC_BUFFER_T *buffer, void *addr_va, void *addr_pa, unsigned status)
{
  buffer->pu1Buffer_VA = (unsigned char *) ((unsigned int) addr_va);
  buffer->pu1Buffer_PA = (unsigned char *) ((unsigned int) addr_pa);

  buffer->eBufferStatus = status;
}

#if 0
#define BITSTREAM_BUFFER_SIZE     ((int)200000/(int)128*(int)128)
#else
#define BITSTREAM_BUFFER_SIZE     (1920*1088*3/2)  
#endif
typedef struct
{
  unsigned char *lum;
  unsigned char *cb;
  unsigned char *cr;
  unsigned char *lum_pa;
  int width, height;
} yuv_buffer_t;

typedef struct
{

    unsigned int BitstreamBuffer;
    unsigned int BitstreamWritePtr;
    unsigned int BitstreamReadPtr;
    unsigned int BitstreamBuffer_pa;
} BITSTREAM_BUFFER_T;

typedef struct
{
#ifdef ANDROID_LOAD
  char InYUVFileName[256];
  char BtmFileName[256];
  char GoldenBtmFileName[256];
  FILE   *input_file;
  FILE   *output_file;
  FILE   *crc_file;
#else
  unsigned char   *input_file;
  unsigned char   *output_file;
#endif

#ifdef ANDROID_LOAD
  yuv_buffer_t       input_yuv[INPUT_YUV_SIZE];
  BITSTREAM_BUFFER_T BtmBuf[INPUT_BTM_BUF_SIZE];
  VCODEC_BUFFER_T    BtmMem[INPUT_BTM_BUF_SIZE];
  VCODEC_BUFFER_T    YUVMem[INPUT_YUV_SIZE];     
#else
  unsigned int  BitstreamBuffer[INPUT_BTM_BUF_SIZE];
  unsigned int  BitstreamWritePtr[INPUT_BTM_BUF_SIZE];
  unsigned int  BitstreamReadPtr[INPUT_BTM_BUF_SIZE];
#endif
  unsigned int  yuv_read_ptr;
  unsigned int  yuv_send_ptr;
  unsigned int  yuv_release_ptr;
  unsigned int  btm_ptr;
  unsigned int  ut_width;
  unsigned int  ut_height;
  unsigned int  encoded_bytes;
  unsigned int  prev_encoded_bytes;
  void          *EncoderCompHandle; 
  HANDLE        codecHandle;  
  int           frm_cnt_1;
  int           frm_cnt_2;
} resource_t;

//extern resource_t    resource;

#define MAX_YUV_BUFFER    16

static unsigned int YUVTable[MAX_YUV_BUFFER];
static unsigned int YUVTableSize = 0;
                           
void VCodecQueryMemType(void            *pBuffer_VA,
                        unsigned int    u4Size,
                        VCODEC_MEMORY_TYPE_T *peMemType);


void VCodecFlushCachedBuffer(void         *pBuffer_VA,
                             unsigned int u4Size);

// VCodecInvalidateCachedBuffer - u4Size is in byte                  
void VCodecInvalidateCachedBuffer(void         *pBuffer_VA, 
                                  unsigned int u4Size);


void VideoEncoderMallocAligned(HANDLE handle, unsigned int size, VCODEC_MEMORY_TYPE_T cachable, VCODEC_BUFFER_T *pBuf);

void VideoEncoderIntMalloc(HANDLE handle, unsigned int size, void **ptr);
                           
void VideoEncoderFree(HANDLE handle, VCODEC_BUFFER_T *pBuf);

void VideoEncoderIntFree(HANDLE handle, void *ptr);

void VideoEncoderReleaseYUV(HANDLE handle, VCODEC_BUFFER_T *pBuf);

void VideoEncoderPaused(HANDLE handle, VCODEC_BUFFER_T *pBuf);

void VideoEncoderAllocateBitstreamBuffer(HANDLE handle, VCODEC_ENC_BUFFER_INFO_T *pBuffer);

void VideoEncoderUpdateBitstreamWP(HANDLE handle, VCODEC_ENC_UPDATE_WP_INTO_T *pInfo);

#endif // _TS_VCODEC_COMMON_H_