/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_maxvf APIs
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_maxvf/std_fxp/include/srs_maxvf_api.h#16 $
 *  $Author: huixiz $
 *  $Date: 2010/09/21 $
 *	
********************************************************************************/

#ifndef __SRS_MAXVF_API_H__
#define __SRS_MAXVF_API_H__

#include "srs_maxvf_def.h"
#include "srs_fxp.h"

/*Data type definition here:*/

//currently only support windowsize = 1024, fftsize = 512, blocksize = 256

#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:


SRSMaxVFObj			SRS_CreateMaxVFObj (void* pBuf, SRSIOMode mode);

SRSResult				SRS_InitMaxVFObj8k(SRSMaxVFObj maxvfObj);
SRSResult				SRS_InitMaxVFObj11k(SRSMaxVFObj maxvfObj);
SRSResult				SRS_InitMaxVFObj16k(SRSMaxVFObj maxvfObj);
SRSResult				SRS_InitMaxVFObj22k(SRSMaxVFObj maxvfObj);
SRSResult				SRS_InitMaxVFObj24k(SRSMaxVFObj maxvfObj);
SRSResult				SRS_InitMaxVFObj44k(SRSMaxVFObj maxvfObj);
SRSResult				SRS_InitMaxVFObj48k(SRSMaxVFObj maxvfObj);
SRSResult				SRS_InitMaxVFObj32k(SRSMaxVFObj maxvfObj);


SRSResult			SRS_MaxVFStereo(SRSMaxVFObj maxvfObj,SRSStereoCh *input, SRSStereoCh *output,void *ws);   //blocksize should be 256,input and output should not point to the same memory
SRSResult			SRS_MaxVFMono(SRSMaxVFObj maxvfObj,srs_int32 *input, srs_int32 *output,void *ws);   //blocksize should be 256,input and output should not point to the same memory

SRSResult			SRS_MaxVFStereoProcess(SRSMaxVFObj maxvfObj,SRSStereoCh *input, SRSStereoCh *output,void *ws);   //blocksize should be 256,input and output should not point to the same memory
SRSResult			SRS_MaxVFMonoProcess(SRSMaxVFObj maxvfObj,srs_int32 *input, srs_int32 *output,void *ws);   //blocksize should be 256,input and output should not point to the same memory


void				SRS_SetMaxVFControlDefaults(SRSMaxVFObj maxvfObj);

void				SRS_SetMaxVFEnable(SRSMaxVFObj maxvfObj,int enable);
void				SRS_SetMaxVFAntiClipEnable(SRSMaxVFObj maxvfObj,int enable);					//enable anti-clip or not: 1:enable 0:disable
void				SRS_SetMaxVFHighPassFilterEnable(SRSMaxVFObj maxvfObj,int enable);				//enable high pass filtering or not
SRSResult			SRS_SetMaxVFInputGain(SRSMaxVFObj maxvfObj,srs_int16 gain);						//gain: I16.SRS_MAXVF_GAIN_IWL (I16.6)
SRSResult			SRS_SetMaxVFOutputGain(SRSMaxVFObj maxvfObj,srs_int16 gain);					//gain: I16.SRS_MAXVF_GAIN_IWL (I16.6)
SRSResult			SRS_SetMaxVFBypassGain(SRSMaxVFObj maxvfObj,srs_int16 gain);					//gain: I16.SRS_MAXVF_GAIN_IWL (I16.1)
SRSResult			SRS_SetMaxVFBoost(SRSMaxVFObj maxvfObj,srs_int32 boost);						//MaxVF boost: I32.SRS_MAXVF_BOOST_IWL (I32.2)
void				SRS_SetMaxVFHighPassFilterCoef(SRSMaxVFObj maxvfObj,srs_int32* coef, int order);	//coef: in SRS filter format, 2<< order << 8

int					SRS_GetMaxVFEnable(SRSMaxVFObj maxvfObj);
int					SRS_GetMaxVFAntiClipEnable(SRSMaxVFObj maxvfObj);
int					SRS_GetMaxVFHighPassFilterEnable(SRSMaxVFObj maxvfObj);
srs_int16			SRS_GetMaxVFInputGain(SRSMaxVFObj maxvfObj);
srs_int16			SRS_GetMaxVFOutputGain(SRSMaxVFObj maxvfObj);
srs_int16			SRS_GetMaxVFBypassGain(SRSMaxVFObj maxvfObj);
srs_int32			SRS_GetMaxVFBoost(SRSMaxVFObj maxvfObj);


#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif /*__SRS_MAXVF_API_H__*/
