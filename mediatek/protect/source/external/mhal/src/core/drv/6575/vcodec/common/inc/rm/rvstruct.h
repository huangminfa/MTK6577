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

/* ***** BEGIN LICENSE BLOCK ***** 
 * Version: RCSL 1.0 and Exhibits. 
 * REALNETWORKS CONFIDENTIAL--NOT FOR DISTRIBUTION IN SOURCE CODE FORM 
 * Portions Copyright (c) 1995-2002 RealNetworks, Inc. 
 * All Rights Reserved. 
 * 
 * The contents of this file, and the files included with this file, are 
 * subject to the current version of the RealNetworks Community Source 
 * License Version 1.0 (the "RCSL"), including Attachments A though H, 
 * all available at http://www.helixcommunity.org/content/rcsl. 
 * You may also obtain the license terms directly from RealNetworks. 
 * You may not use this file except in compliance with the RCSL and 
 * its Attachments. There are no redistribution rights for the source 
 * code of this file. Please see the applicable RCSL for the rights, 
 * obligations and limitations governing use of the contents of the file. 
 * 
 * This file is part of the Helix DNA Technology. RealNetworks is the 
 * developer of the Original Code and owns the copyrights in the portions 
 * it created. 
 * 
 * This file, and the files included with this file, is distributed and made 
 * available on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER 
 * EXPRESS OR IMPLIED, AND REALNETWORKS HEREBY DISCLAIMS ALL SUCH WARRANTIES, 
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS 
 * FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. 
 * 
 * Technology Compatibility Kit Test Suite(s) Location: 
 * https://rarvcode-tck.helixcommunity.org 
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK ***** */ 

/*/////////////////////////////////////////////////////////////////////////// */
/*    RealNetworks, Inc. Confidential and Proprietary Information. */
/* */
/*    Copyright (c) 1995-2002 RealNetworks, Inc. */
/*    All Rights Reserved. */
/* */
/*    Do not redistribute. */
/* */
/*/////////////////////////////////////////////////////////////////////////// */
/* */
/*    Various structure definitions. */
/* */
/*/////////////////////////////////////////////////////////////////////////// */

/*/////////////////////////////////////////////////////////////////////////// */
/*    INTEL Corporation Proprietary Information */
/* */
/*    This listing is supplied under the terms of a license */
/*    agreement with INTEL Corporation and may not be copied */
/*    nor disclosed except in accordance with the terms of */
/*    that agreement. */
/* */
/*    Copyright (c) 1995 - 2000 Intel Corporation. */
/*    All Rights Reserved. */
/* */
/*////////////////////////////////////////////////////////////////////////// */
/* $Header: /cvsroot/rarvcode-video/codec/rv89combo/rv89combo_c/cdeclib/rvstruct.h,v 1.8 2005/02/04 00:11:09 hfrederickson Exp $ */
/*////////////////////////////////////////////////////////////////////////// */

#ifndef RVSTRUCT_H__
#define RVSTRUCT_H__

#include "rvtypes.h"
#include "decdefs.h"
#ifdef __cplusplus
extern "C" {
#endif




struct RV_Dimensions
{
    U32     width;
    U32     height;
};

//struct RV_Dimensions_MC
//{
//	U32     width;
//	U32     height;
//	U32     width_UV;
//	U32     height_UV;
//};

typedef enum 
{
	INTRAPIC	= 0,
	INTERPIC	= 1,
	TRUEBPIC	= 2,
	FRUPIC  	= 3
}RV_EnumPicCodType;

typedef enum
{
	SKIP_NEVER		= 0,
	SKIP_ALWAYS		= 1,
	SKIP_SOMETIMES	= 2,
	SKIP_SEEK_MODE	= 3
}RV_SKIP_MODE;

/* Macroblock type definitions */
/* Keep these ordered such that intra types are first, followed by */
/* inter types.  Otherwise you'll need to change the definitions */
/* of IS_INTRA_MBTYPE and IS_INTER_MBTYPE. */
/* */
/* WARNING:  Because the decoder exposes macroblock types to the application, */
/* these values cannot be changed without affecting users of the decoder. */
/* If new macro block types need to be inserted in the middle of the list, */
/* then perhaps existing types should retain their numeric value, the new */
/* type should be given a new value, and for coding efficiency we should */
/* perhaps decouple these values from the ones that are encoded in the */
/* bitstream. */
/* */
/* */

typedef enum {
    MBTYPE_INTRA,			/* 0 */
    MBTYPE_INTRA_16x16,		/* 1 */
    MBTYPE_INTER,			/* 2 */
    MBTYPE_INTER_4V,		/* 3 */
    MBTYPE_FORWARD,			/* 4 */
    MBTYPE_BACKWARD,		/* 5 */
    MBTYPE_SKIPPED,			/* 6 */
    MBTYPE_DIRECT,			/* 7 */
    MBTYPE_INTER_16x8V,		/* 8 */
    MBTYPE_INTER_8x16V,		/* 9  */
    MBTYPE_BIDIR,			/* 10 */
	MBTYPE_INTER_16x16,		/* 11 */
    NUMBER_OF_MBTYPES		/* 12 */
} MB_Type;


/* macro - yields TRUE if a given MB type is INTRA */
#define IS_INTRA_MBTYPE(mbtype) ((mbtype) < MBTYPE_INTER)

/* macro - yields TRUE if a given MB type is INTER */
#define IS_INTER_MBTYPE(mbtype) ((mbtype) >= MBTYPE_INTER)

#define IS_FORWARD_OR_BIDIR(mbtype)(((mbtype) == MBTYPE_FORWARD) || \
                                    ((mbtype) == MBTYPE_BIDIR))

#define IS_BACKWARD_OR_BIDIR(mbtype)(((mbtype) == MBTYPE_BACKWARD) || \
                                     ((mbtype) == MBTYPE_BIDIR))


/* Reference Picture Resampling Parameter (RPRP) definitions */

/* RPR displacement parameters */
/* Each field contains a luma edge displacement in 1/2 pel units. The */
/* displacements are interpreted as the edge displacements to be applied */
/* to the reference frame independent of any resizing. Each edge displacement */
/* is independent of the others, as opposed to the RPR warping parameters */
/* defined in Annex P. */
/* The relationship between the warping parameters and these edge parameters */
/* is: */
/* iDeltaLeft = wx0 */
/* iDeltaRight = wx0 + wxx */
/* iDeltaTop = wy0 */
/* iDeltaBottom = wy0 + wyy */

typedef struct
{
	I32 iDeltaLeft;
	I32 iDeltaRight;
	I32 iDeltaTop;
	I32 iDeltaBottom;
} T_RPR_EdgeParams;

typedef struct YUVFrame
{
	/* Pointers to Y, V, and U planes */
    U8 *pYPlane;
    U8 *pVPlane;
    U8 *pUPlane;
} T_YUVFrame;


/* Warning: The FillMode enum value assignments here match the bit assignments */
/* used in the bitstream and therefore must not be changed. */

enum EnumRPRFillModeType 
{ 
	FILL_COLOR = 0, 
	FILL_BLACK = 1, 
	FILL_GRAY  = 2, 
	FILL_CLIP  = 3
};

/* RPR fill mode parameter */
typedef struct
{
	enum EnumRPRFillModeType uMode;
	U8 uColor [3];		/* Y, Cb, Cr fill color: only valid when uMode == COLOR */
} T_RPR_FillParam;


typedef struct
{
	RV_Boolean			bParamsPresent;	/* true if following params in header */
	T_RPR_EdgeParams	RPREdgeParams;
	T_RPR_FillParam		RPRFillParam;
} T_RPRP;


/* The PictureHeader structure represents the information that */
/* goes into the picture header of a single bitstream picture. */

#define ISBFRAME(hdr) \
	((hdr).PicCodType == TRUEBPIC)
#define ISFRUFRAME(hdr) \
	((hdr).PicCodType == FRUPIC)
#define ISPFRAME(hdr) \
	((hdr).PicCodType == INTERPIC)
#define ISKEYFRAME(hdr) \
	((hdr).PicCodType == INTRAPIC)

struct PictureHeader
{
	RV_EnumPicCodType	PicCodType;
	struct RV_Dimensions  dimensions;
	U32				TR;

	RV_Boolean		RPR;		/* Reference-picture resampling (RPR Annex P) */

	T_RPRP			RPRP;		/* RPR parameters */

	RV_Boolean		Deblocking_Filter_Passthrough;
	/* This is a non-standard option used in RealVideo */
	/* that indicates that the deblocking filter in annex J */
	/* should not be called. The option will be ignored if */
	/* Annex J is off or if the bitstream is standards-based. */

	//U32           pixel_aspect_ratio;

	U8				PQUANT;
	U8				OSVQUANT;
#ifdef _RV_VAR_DIM_MODE_
	U32            RESIZE;
#endif

};

#define         D_LEFT_EDGE     0x1
#define         D_RIGHT_EDGE    0x2
#define         D_TOP_EDGE      0x4
#define         D_BOTTOM_EDGE   0x8

/* A DecoderMBInfo structure describes one decoded macroblock. */
/* */
/* We should keep this structure reasonably small and well aligned, */
/* since we need to allocate one per luma macroblock, twice (one set */
/* for B frames and one set for non-B frames). */
/* Technically, we don't need two sets of the 'missing' and 'edge_type' */
/* members.  But they are relatively small, and keeping them along with */
/* QP and mbtype will help localize data cache accesses. */

struct DecoderMBInfo
{
#if defined(USE_FRANK_DECODER)
	U8      edge_type;
	U8      mbtype;
	short	mbxpos;
	short	mbypos;
	int		mvpos;
	int		pixpos;
	int		cpixpos;
	int		intratypepos;
#endif // frank
	//U8      edge_type;
	/* Indicates whether the MB falls on a picture or slice boundary. */
	/* */
	/* WARNING: Do not change the following EDGE value definitions */
	/* without making corresponding changes to dscalup.cpp, */
	/* specifically the jump table in ExtendBlockEdges(). */
	/* These bit masks are also used by error concealment. */

	U8      QP;

	//U8      mbtype;

	//Bool8   missing;
	/* Indicates whether the MB was absent from the bitstream. */
	/* Error concealment is performed on such MBs. */

	U32		cbpcoef;
	U32		mvdelta;
	/* Indicates coded blocks with coefficients. */
};




struct RV_Rectangle
{
	U32     width;
	U32     height;
	U32     x;
	U32     y;
};

/* A RV_Image_Format object describes the *type* of an image.  The intent */
/* is that it contains enough descriptive information so that a codec can */
/* perform capability negotiations (i.e., advertise and agree on input and */
/* output format types) and also allocate any dimension-dependent memory. */

struct RV_Image_Format
{
        struct RV_Dimensions  dimensions;
            /* Generally, this "dimensions" member specifies the dimensions */
            /* of the image at hand (or perhaps the desired dimensions, if */
            /* this format is describing a request for zoomed output). */
            /* However, for DCI, this is not the case. */
            /* For DCI (and only DCI), when a RV_Image_Format is describing */
            /* a RV_Decoder's output image, then this "dimensions" member */
            /* specifies the dimensions of the display screen, and the desired */
            /* dimensions of the image are found in the "rectangle" member . */

        struct RV_Rectangle   rectangle;
            /* In general, this "rectangle" member has origin (0, 0) and */
            /* dimensions equivalent to the image at hand. */
            /* However, when this RV_Image_Format is describing a */
            /* RV_Decoder's output image for DCI, then this "rectangle" */
            /* member specifies a rectangle within the screen.  In this */
            /* case, it will have a non-zero origin, though its dimensions */
            /* will still reflect those of the image at hand (or its desired */
            /* dimensions, it zooming is being requested). */
            /* */
            /* In all cases (DCI and not DCI), the dimensions in the */
            /* "rectangle" member are the dimensions of the image at hand */
            /* (or its desired dimensions, if requesting zoomed output). */
            /* In the non-DCI case, the rectangle's origin is zero, and its */
            /* dimensions match the "dimensions" member. */
            /* In the DCI case, the rectangle's origin may be non-zero, its */
            /* dimensions represent the image at hand, and the above */
            /* "dimensions" member reflects the display screen size. */

            /* The yuv_info struct represents the pitch values for YUV */
            /* formats, e.g. YUV12, YVU9 et.al. */
            struct {
                U32     y_pitch;
                U32     u_pitch;
                U32     v_pitch;
            } yuv_info;
};

struct RV_Ref_Image_Format
{
        struct RV_Dimensions  dimensions;
            /* Generally, this "dimensions" member specifies the dimensions */
            /* of reference frame */
};


/* A RV_Image object represents an actual instance of an image.  Thus it */
/* includes a RV_Image_Format that describes the image, as well as pointers */
/* to the image's data, among other information. */
struct RV_Image {
        struct RV_Image_Format    format;
        U32                 size;
            /* "size" gives the total length, in bytes, of the image's data. */

        U32                 sequence_number;
            /* This number indicates the temporal position of this image */
            /* with respect to previous and future images.  Its value is */
            /* specific to each video environment. */

            /* The yuv_info struct points to the Y, U and V planes' data. */
            /* It is used for YUV formats, e.g. YUV12, YVU9, et. al. */
            struct {
                U8         *y_plane;
                U8         *u_plane;
                U8         *v_plane;
            } yuv_info;
};

/* !export begin! */
typedef enum 
{
	RV_DDF_DONT_DRAW   = 0x1,
	RV_DDF_KEY_FRAME   = 0x2,
	RV_DDF_MORE_FRAMES = 0x4,
	RV_DDF_LAST_FRAME  = 0x200
}RV_DECODER_FLAGS;	

typedef enum 
{
	RV_DDN_MORE_FRAMES   		= 0x1,
	RV_DDN_DONT_DRAW   			= 0x2,
	RV_DDN_KEY_FRAME 			= 0x4,
	RV_DDN_B_FRAME  			= 0x8,
	RV_DDN_DEBLOCKING_FILTER 	= 0x10,
    RV_DDN_FRU_FRAME 			= 0x20	
}RV_DECODER_NOTES;	
/* !export end! */

#if defined(RV_HW_DEBLOCK)
typedef struct
{
    I16 mvx;
    I16 mvy;
} RV_MVInfo_T;

typedef struct
{
    U8 edgetype;
    U8 mbtype;
    U8 refMbtype;
    U8 qp;
    U32 cbp;
    RV_MVInfo_T mv[4];
    RV_MVInfo_T mvleft[2];
    RV_MVInfo_T mvabove[2];
} RV_DebMBInfo_T;
#endif // HW_DEBLOCK

#ifdef __cplusplus
}
#endif




#endif /* RVSTRUCT_H__ */

