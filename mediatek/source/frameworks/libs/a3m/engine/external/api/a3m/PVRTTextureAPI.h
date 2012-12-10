/******************************************************************************

 @File         PVRTTextureAPI.h

 @Title        OGLES2/PVRTTextureAPI

 @Version      

 @Copyright    Copyright (c) Imagination Technologies Limited.

 @Platform     ANSI compatible

 @Description  OGLES2 texture loading.

******************************************************************************/
#ifndef _PVRTTEXTUREAPI_H_
#define _PVRTTEXTUREAPI_H_
#include <GLES2/gl2.h>           /* for GLuint */
#include <a3m/base_types.h>      /* for A3M_BOOL */

typedef A3M_BOOL EPVRTError;

A3M_BOOL LoadPVR(	const void* pointer,
									GLuint &texName,
                  A3M_UINT32 &width,
                  A3M_UINT32 &height,
                  A3M_UINT32 &bitsPerPixel,
                  A3M_UINT32 &mipMapLevels);

/****************************************************************************
** Functions
****************************************************************************/

/*!***************************************************************************
 @Function		PVRTTextureLoadFromPointer
 @Input			pointer				Pointer to header-texture's structure
 @Modified		texName				the OpenGL ES texture name as returned by glBindTexture
 @Modified		psTextureHeader		Pointer to a PVR_Texture_Header struct. Modified to
									contain the header data of the returned texture Ignored if NULL.
 @Input			bAllowDecompress	Allow decompression if PVRTC is not supported in hardware.
 @Input			nLoadFromLevel		Which mipmap level to start loading from (0=all)
 @Input			texPtr				If null, texture follows header, else texture is here.
 @Return		PVR_SUCCESS on success
 @Description	Allows textures to be stored in C header files and loaded in. Can load parts of a
				mipmaped texture (ie skipping the highest detailed levels).
				Sets the texture MIN/MAG filter to GL_LINEAR_MIPMAP_NEAREST/GL_LINEAR
				if mipmaps are present, GL_LINEAR/GL_LINEAR otherwise.
*****************************************************************************/
EPVRTError PVRTTextureLoadFromPointer(	const void* pointer,
										                    GLuint *const texName,
										                    const void *psTextureHeader=0,
										                    bool bAllowDecompress = true,
										                    const unsigned int nLoadFromLevel=0,
										                    const void * const texPtr=0);

/*!***************************************************************************
 @Function		PVRTTextureLoadFromPVR
 @Input			filename			Filename of the .PVR file to load the texture from
 @Modified		texName				the OpenGL ES texture name as returned by glBindTexture
 @Modified		psTextureHeader		Pointer to a PVR_Texture_Header struct. Modified to
									contain the header data of the returned texture Ignored if NULL.
 @Input			bAllowDecompress	Allow decompression if PVRTC is not supported in hardware.
 @Input			nLoadFromLevel		Which mipmap level to start loading from (0=all)
 @Return		PVR_SUCCESS on success
 @Description	Allows textures to be stored in binary PVR files and loaded in. Can load parts of a
				mipmaped texture (ie skipping the highest detailed levels).
				Sets the texture MIN/MAG filter to GL_LINEAR_MIPMAP_NEAREST/GL_LINEAR
				if mipmaps are present, GL_LINEAR/GL_LINEAR otherwise.
*****************************************************************************/
EPVRTError PVRTTextureLoadFromPVR(	const char * const filename,
									                  GLuint * const texName,
									                  const void *psTextureHeader=0,
									                  bool bAllowDecompress = true,
									                  const unsigned int nLoadFromLevel=0);

/*!***************************************************************************
 @Function			PVRTTextureFormatGetBPP
 @Input				nFormat
 @Input				nType
 @Description		Returns the bits per pixel (BPP) of the format.
*****************************************************************************/
unsigned int PVRTTextureFormatGetBPP(const GLuint nFormat, const GLuint nType);


#endif /* _PVRTTEXTUREAPI_H_ */

/*****************************************************************************
 End of file (PVRTTextureAPI.h)
*****************************************************************************/

