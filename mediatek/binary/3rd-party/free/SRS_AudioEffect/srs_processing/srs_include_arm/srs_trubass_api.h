/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_trubass APIs
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_trubass/std_fxp/include/srs_trubass_api.h#8 $
 *  $Author: oscarh $
 *  $Date: 2011/01/12 $
 *	
********************************************************************************/

#ifndef __SRS_TRUBASS_API_H__
#define __SRS_TRUBASS_API_H__

#include "srs_trubass_def.h"



#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

SRSTruBassObj	SRS_CreateTruBassObj(void* pBuf);

void			SRS_InitTruBassObj8k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj11k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj16k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj22k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj24k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj32k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj44k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj48k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj88k(SRSTruBassObj tbObj);
void			SRS_InitTruBassObj96k(SRSTruBassObj tbObj);

void			SRS_TruBassProcess(SRSTruBassObj tbObj, SRSStereoCh *audioIO, int blockSize, void* ws);
void			SRS_TruBass(SRSTruBassObj tbObj, SRSStereoCh *audioIO, int blockSize, void* ws);

void			SRS_SetTruBassControlDefaults(SRSTruBassObj tbObj);

void			SRS_SetTruBassEnable(SRSTruBassObj tbObj, int enable);

void			SRS_SetTruBassOutputOption(SRSTruBassObj tbObj, SRSTruBassOutputOption opt);

SRSResult		SRS_SetTruBassInputGain(SRSTruBassObj tbObj, srs_int16 gain);	//I16.SRS_TB_GAIN_IWL
SRSResult		SRS_SetTruBassOutputGain(SRSTruBassObj tbObj, srs_int16 gain);	//I16.SRS_TB_GAIN_IWL
SRSResult		SRS_SetTruBassBypassGain(SRSTruBassObj tbObj, srs_int16 gain);	//I16.SRS_TB_GAIN_IWL
SRSResult		SRS_SetTruBassControl(SRSTruBassObj tbObj, srs_int16 ctrl);		//I16.SRS_TB_CTRL_IWL

SRSResult		SRS_SetTruBassSpeakerSize(SRSTruBassObj tbObj, SRSTruBassSpeakerSize sz);
SRSResult		SRS_SetTruBassCustomSpeakerFilterCoefs(SRSTruBassObj tbObj, const SRSTruBassCustomSpeakerCoefs *coefs);

SRSResult		SRS_SetTruBassMode(SRSTruBassObj tbObj, SRSTruBassMode mode);


int				SRS_GetTruBassEnable(SRSTruBassObj tbObj);
srs_int16		SRS_GetTruBassInputGain(SRSTruBassObj tbObj);
srs_int16		SRS_GetTruBassOutputGain(SRSTruBassObj tbObj);
srs_int16		SRS_GetTruBassBypassGain(SRSTruBassObj tbObj);
srs_int16		SRS_GetTruBassControl(SRSTruBassObj tbObj);


SRSTruBassMode	SRS_GetTruBassMode(SRSTruBassObj tbObj);
SRSTruBassSpeakerSize  SRS_GetTruBassSpeakerSize(SRSTruBassObj tbObj);
SRSTruBassCustomSpeakerCoefs SRS_GetTruBassCustomSpeakerFilteCoefs(SRSTruBassObj tbObj);
SRSTruBassOutputOption	SRS_GetTruBassOutputOption(SRSTruBassObj tbObj);


unsigned char	SRS_GetTruBassTechVersion(SRSVersion which);
unsigned char	SRS_GetTruBassLibVersion(SRSVersion which);

#ifdef __cplusplus
}
#endif /*__cplusplus*/



#endif /*__SRS_TRUBASS_API_H__*/
