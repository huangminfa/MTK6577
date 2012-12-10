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

//-------------------------------------------------------------------------------
//-- Title		: The imaGPU library header
//
//-- Creator	: Eddie Tsao
//
//-- Version	: v0
//
//-- Description : imaGPU is a GLES2.0 base framework for image processing
//
//-------------------------------------------------------------------------------
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

// Index to bind the attributes to vertex shaders
#define VERTEX_ARRAY	0
#define TEXTURE_ARRAY	1
#define REF_ARRAY		2

// ImaGPU YUV planar format definition
#define IG_YUV422P		0x8000 
#define IG_YUV420P		0x8001
#define IG_YUV420SP     0x8002       
#define IG_RGB          0x8003

#define IG_API extern

typedef struct
{
	GLfloat x;
	GLfloat y;
	GLfloat u;
	GLfloat v;
} vtx_fmt;

//-------------------------------------------------------------------------------
//filename	: for shader file
//type		: GL_VERTEX_SHADER, GL_FRAGMENT_SHADER
//Return	: the shader handle id
//IG_API GLuint igLoadShader( const char *filename, GLenum type );
IG_API GLuint igLoadShader( const char *filestr, GLenum type );
//-------------------------------------------------------------------------------
//vs		: vertex shader handle
//fs		: fragment shader handle
//ref_buffer: reference buffer for vpg_ref. Set 0 if there is no ref_buffer
//Return	: the program handle id
IG_API GLuint igCreateProgram(GLuint vs, GLuint fs, GLfloat * ref_buffer );
//-------------------------------------------------------------------------------
//data		: for image data pointer
//mode		: GL_REPEAT, GL_CLAMP_TO_EDGE, GL_MIRRORED_REPEAT
//format	: GL_RGB, GL_RGBA, GL_LUMINANCE
//width		: image's width
//height	: image's height
//Return	: the image handle id
IG_API GLuint igLoadImage( void * data, GLint mode, GLenum format, int width, int height);
//-------------------------------------------------------------------------------
//data		: for planar image data pointer
//mode		: GL_REPEAT, GL_CLAMP_TO_EDGE, GL_MIRRORED_REPEAT
//format	: IG_YUV422P, IG_YUV420P, IG_YUV420SP, IG_RGB
//width		: image's width
//height	: image's height
//plan		: the image plan handle pointer
//Return	: the image plan number
IG_API GLuint igLoadImage(void * data, GLint mode, GLenum format, int width, int height, GLuint *plan);
//-------------------------------------------------------------------------------
//target_width	: target image width
//target_height : target image height
//vpg_width		: virtual processing group width
//vpg_height	: virtual processing group height
//buffers		: The buffer pointer indicates both VPE and VPG handle id
//Return		: the VPE element number for processing 
//IG_API GLuint igCreateVPE( int target_width, int target_height, int vpg_width, int vpg_height, GLuint *buffers );
GLuint igCreateVPE(int *vpg_size, vtx_fmt *vtx_data, GLushort *ele_data, GLuint *buffers);
//-------------------------------------------------------------------------------
//target_width	: target image width
//target_height : target image height
//elenum		: the VPE element number for processing 
GLvoid igProc(int target_width, int target_height, GLuint elenum);
//-------------------------------------------------------------------------------
//program		: the program handle id
//img_name		: the image name in sahder
//tex_unit		: the texture unit to bind, GL_TEXTURE0, GL_TEXTURE1, GL_TEXTURE2, GL_TEXTURE3,...
//texture		: the image handle id
IG_API GLvoid igProgramBindImage(GLuint program, const char * img_name, GLuint tex_unit, GLuint image);
//-------------------------------------------------------------------------------
//shader		: the shader handle id
IG_API GLvoid igFreeShader(GLuint shader);
//-------------------------------------------------------------------------------
//program		: the program handle id
IG_API GLvoid igFreeProgram(GLuint program);
//-------------------------------------------------------------------------------
//n				: number of the texture handle to be free
//buffers		: image handle id pointer
IG_API GLvoid igFreeImage(GLuint n, const GLuint* image);
//-------------------------------------------------------------------------------
//buffers		: The buffer pointer indicates both VPE and VPG handle id
IG_API GLvoid igFreeVPE(const GLuint* buffers);
//-------------------------------------------------------------------------------
//target_width	: target image width
//target_height : target image height
//target_ptr	: The pointer for target data
IG_API GLvoid igReadTarget(GLuint target_width, GLuint target_height, void* target_ptr);
//-------------------------------------------------------------------------------
//shader	: shader handle
//Return	: the status if the shader bad been compiled successfully; ture: success, false: Fail
IG_API bool	igCheckCompile(GLuint shader);
//-------------------------------------------------------------------------------
//program	: program handle
//Return	: the status if the program bad been linked successfully; ture: success, false: Fail
IG_API bool	igCheckLink(GLuint program);
//-------------------------------------------------------------------------------
//target_width	: target image width
//target_height : target image height
//fbo		    : return the frame buffer object id
//rbo			: return the render buffer object id
IG_API bool igCreateBufferObject(int target_width, int target_height, GLuint &fbo, GLuint &rbo);
//-------------------------------------------------------------------------------
//fbo		    : input the frame buffer resource id
//rbo			: input the render buffer resource id
IG_API GLvoid igFreeBufferObject(GLuint &fbo, GLuint &rbo);
