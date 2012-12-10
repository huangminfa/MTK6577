/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes ANSI 10-band graphic EQ APIs
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_graphiceq/std_fxp/include/srs_10band_geq_api.h#6 $
 *  $Author: oscarh $
 *  $Date: 2010/11/16 $
 *	
********************************************************************************/

#ifndef __SRS_10BAND_GRAPHICEQ_API_H__
#define __SRS_10BAND_GRAPHICEQ_API_H__

#include "srs_graphiceq_def.h"
#include "srs_graphiceq_ver_api.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:
SRS10BandGraphicEqObj		SRS_Create10BandGraphicEqObj(void *pBuf);

void	SRS_Init10BandGraphicEqObj8k(SRS10BandGraphicEqObj geqObj);
void	SRS_Init10BandGraphicEqObj11k(SRS10BandGraphicEqObj geqObj);
void	SRS_Init10BandGraphicEqObj16k(SRS10BandGraphicEqObj geqObj);
void	SRS_Init10BandGraphicEqObj22k(SRS10BandGraphicEqObj geqObj);
void	SRS_Init10BandGraphicEqObj24k(SRS10BandGraphicEqObj geqObj);
void	SRS_Init10BandGraphicEqObj32k(SRS10BandGraphicEqObj geqObj);
void	SRS_Init10BandGraphicEqObj44k(SRS10BandGraphicEqObj geqObj);
void	SRS_Init10BandGraphicEqObj48k(SRS10BandGraphicEqObj geqObj);

void	SRS_Set10BandGraphicEqControlDefaults(SRS10BandGraphicEqObj geqObj);


void	SRS_Set10BandGraphicEqEnable(SRS10BandGraphicEqObj geqObj, int enable);
int		SRS_Get10BandGraphicEqEnable(SRS10BandGraphicEqObj geqObj);

SRSResult	SRS_Set10BandGraphicEqBandGain(SRS10BandGraphicEqObj geqObj, int bandIndex, srs_int16 gain);
srs_int16	SRS_Get10BandGraphicEqBandGain(SRS10BandGraphicEqObj geqObj, int bandIndex);

void	SRS_Set10BandGraphicEqLimiterEnable(SRS10BandGraphicEqObj geqObj, int enable);
int		SRS_Get10BandGraphicEqLimiterEnable(SRS10BandGraphicEqObj geqObj);

void	SRS_10BandGraphicEqProcess(SRS10BandGraphicEqObj geqObj, srs_int32 *audioIO, int blockSize, void *ws);


#ifdef __cplusplus
}
#endif /*__cplusplus*/


/////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////


#endif /*__SRS_10BAND_GRAPHICEQ_API_H__*/
