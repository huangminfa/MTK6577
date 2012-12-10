/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_fft APIs
 *
 *	Author: Zesen Zhuang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/

#ifndef __SRS_FFT_DEF_H__
#define __SRS_FFT_DEF_H__

#include "srs_typedefs.h"

/*Data type definition here:*/
typedef enum
{
	SRS_CFFT_32C16_RDX2,
	SRS_RFFT_32C16_RDX2,
	SRS_RFFT_16C16_RDX2
} SRSFftType;

struct tSRSFftTbl;
typedef struct tSRSFftTbl SRSFftTbl;	/* SRS Fixed Point FFT Library specific structure */

/* table sizes constants of SRSFftTbl, needed if call <SRS_Cfft/SRS_InvCfft>_32c16_rdx2  */
#define SRS_CFFT_32C16_RDX2_32_SIZE (84+8+4)
#define SRS_CFFT_32C16_RDX2_64_SIZE (148+8+4)
#define SRS_CFFT_32C16_RDX2_128_SIZE (276+8+4)
#define SRS_CFFT_32C16_RDX2_256_SIZE (532+8+4)
#define SRS_CFFT_32C16_RDX2_512_SIZE (1044+8+4)
#define SRS_CFFT_32C16_RDX2_1024_SIZE (2068+8+4)
#define SRS_CFFT_32C16_RDX2_2048_SIZE (4116+8+4)
#define SRS_CFFT_32C16_RDX2_4096_SIZE (8212+8+4)

/* table sizes constants of SRSFftTbl, needed if call SRS_Rfft_32c16_rdx2 or SRS_InvRfft_32c16_rdx2 */
#define SRS_RFFT_32C16_RDX2_32_SIZE (84+8+4)
#define SRS_RFFT_32C16_RDX2_64_SIZE (148+8+4)
#define SRS_RFFT_32C16_RDX2_128_SIZE (276+8+4)
#define SRS_RFFT_32C16_RDX2_256_SIZE (532+8+4)
#define SRS_RFFT_32C16_RDX2_512_SIZE (1044+8+4)
#define SRS_RFFT_32C16_RDX2_1024_SIZE (2068+8+4)
#define SRS_RFFT_32C16_RDX2_2048_SIZE (4116+8+4)
#define SRS_RFFT_32C16_RDX2_4096_SIZE (8212+8+4)

/* table sizes constants of SRSFftTbl, needed if call SRS_Rfft_16c16_rdx2 or SRS_InvRfft_16c16_rdx2  */
#define SRS_RFFT_16C16_RDX2_32_SIZE (84+8+4)
#define SRS_RFFT_16C16_RDX2_64_SIZE (148+8+4)
#define SRS_RFFT_16C16_RDX2_128_SIZE (276+8+4)
#define SRS_RFFT_16C16_RDX2_256_SIZE (532+8+4)
#define SRS_RFFT_16C16_RDX2_512_SIZE (1044+8+4)
#define SRS_RFFT_16C16_RDX2_1024_SIZE (2068+8+4)
#define SRS_RFFT_16C16_RDX2_2048_SIZE (4116+8+4)
#define SRS_RFFT_16C16_RDX2_4096_SIZE (8212+8+4)


#define SRS_RFFT_32C16_RDX2_160_SIZE (SRS_RFFT_32C16_RDX2_32_SIZE+160*sizeof(srs_int32)+8)
#define SRS_RFFT_32C16_RDX2_320_SIZE (SRS_RFFT_32C16_RDX2_64_SIZE+320*sizeof(srs_int32)+8)

#endif //__SRS_FFT_DEF_H__
