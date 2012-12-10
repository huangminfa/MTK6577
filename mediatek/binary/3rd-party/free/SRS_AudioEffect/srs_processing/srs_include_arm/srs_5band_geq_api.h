/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes 5-band graphic EQ APIs
 *
 *  RCS keywords:
 *	$Id $
 *  $Author: oscarh $
 *  $Date: 2010/11/16 $
 *	
********************************************************************************/

#ifndef __SRS_5BAND_GRAPHICEQ_API_H__
#define __SRS_5BAND_GRAPHICEQ_API_H__

#include "srs_graphiceq_def.h"
#include "srs_graphiceq_ver_api.h"

#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:
SRS5BandGraphicEqObj		SRS_Create5BandGraphicEqObj(void *pBuf);

void	SRS_Init5BandGraphicEqObj8k(SRS5BandGraphicEqObj geqObj);
void	SRS_Init5BandGraphicEqObj11k(SRS5BandGraphicEqObj geqObj);
void	SRS_Init5BandGraphicEqObj16k(SRS5BandGraphicEqObj geqObj);
void	SRS_Init5BandGraphicEqObj22k(SRS5BandGraphicEqObj geqObj);
void	SRS_Init5BandGraphicEqObj24k(SRS5BandGraphicEqObj geqObj);
void	SRS_Init5BandGraphicEqObj32k(SRS5BandGraphicEqObj geqObj);
void	SRS_Init5BandGraphicEqObj44k(SRS5BandGraphicEqObj geqObj);
void	SRS_Init5BandGraphicEqObj48k(SRS5BandGraphicEqObj geqObj);

void	SRS_Set5BandGraphicEqControlDefaults(SRS5BandGraphicEqObj geqObj);

void	SRS_Set5BandGraphicEqExtraBandBehavior(SRS5BandGraphicEqObj geqObj, SRS5BandGeqExtraBandBehavior behavior);
SRS5BandGeqExtraBandBehavior	SRS_Get5BandGraphicEqExtraBandBehavior(SRS5BandGraphicEqObj geqObj);

void	SRS_Set5BandGraphicEqEnable(SRS5BandGraphicEqObj geqObj, int enable);
int		SRS_Get5BandGraphicEqEnable(SRS5BandGraphicEqObj geqObj);

SRSResult	SRS_Set5BandGraphicEqBandGain(SRS5BandGraphicEqObj geqObj, int bandIndex, srs_int16 gain);
srs_int16	SRS_Get5BandGraphicEqBandGain(SRS5BandGraphicEqObj geqObj, int bandIndex);

void	SRS_Set5BandGraphicEqLimiterEnable(SRS5BandGraphicEqObj geqObj, int enable);
int		SRS_Get5BandGraphicEqLimiterEnable(SRS5BandGraphicEqObj geqObj);

void	SRS_5BandGraphicEqProcess(SRS5BandGraphicEqObj geqObj, srs_int32 *audioIO, int blockSize, void *ws);


#ifdef __cplusplus
}
#endif /*__cplusplus*/


/////////////////////////////////////////////////////////////////////////////////////////////////////////



#endif /*__SRS_5BAND_GRAPHICEQ_API_H__*/
