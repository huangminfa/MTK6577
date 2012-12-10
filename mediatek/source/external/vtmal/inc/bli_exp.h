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

#ifndef BLI_EXP_H
#define BLI_EXP_H

#ifdef __cplusplus
extern "C"{
#endif 

typedef void     BLI_HANDLE;

#ifndef NULL
#define NULL    0
#endif

extern void BLI_ASSERT(int expression);

/*----------------------------------------------------------------------*/
/* Get required buffer size for BLI Software SRC                        */
/*----------------------------------------------------------------------*/
void BLI_GetMemSize(unsigned int inSR,                  /* Input, input sampling rate of the conversion */
                    unsigned int inChannel,             /* Input, input channel number of the conversion */
                    unsigned int outSR,                 /* Input, output sampling rate of the conversion */
                    unsigned int outChannel,            /* Input, output channel number of the conversion */
                    unsigned int *workBufSize);         /* Output, the required working buffer size in byte */


/*----------------------------------------------------------------------*/
/* Get the BLI Software SRC handler.                                    */
/* Return: the handle of current BLI Software SRC                       */
/*----------------------------------------------------------------------*/
BLI_HANDLE *BLI_Open(unsigned int inSR,                 /* Input, input sampling rate of the conversion */
                     unsigned int inChannel,            /* Input, input channel number of the conversion */
                     unsigned int outSR,                /* Input, output sampling rate of the conversion */
                     unsigned int outChannel,           /* Input, output channel number of the conversion */
                     char* buffer);                     /* Input, pointer to the working buffer */

/*----------------------------------------------------------------------*/
/* Decompress the bitstream to PCM data                                 */
/* Return: consumed input buffer size(byte)                             */
/*----------------------------------------------------------------------*/
unsigned int BLI_Convert(void *hdl,                  /* Input, handle of this conversion */
                         short *inBuf,               /* Input, pointer to input buffer */
                         unsigned int *inLength,     /* Input, length(byte) of input buffer */ 
                                                     /* Output, length(byte) left in the input buffer after conversion */ 
                         short *outBuf,              /* Input, pointer to output buffer */
                         unsigned int *outLength);   /* Input, length(byte) of output buffer */ 
                                                     /* Output, output data length(byte) */ 

/*----------------------------------------------------------------------*/
/* Close the process                                                    */
/*----------------------------------------------------------------------*/
void BLI_Close(BLI_HANDLE *hdl);

#ifdef __cplusplus
}
#endif 
#endif

