/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_hardlimiter APIs
 *
 *	Author: Zesen Zhuang
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_hardlimiter/std_fxp/include/srs_hardlimiter_api.h#10 $
 *  $Author: oscarh $
 *  $Date: 2010/11/16 $
 *	
********************************************************************************/

#ifndef __SRS_HARDLIMITER_API_H__
#define __SRS_HARDLIMITER_API_H__

#include "srs_hardlimiter_def.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:

SRSHardLimiterObj	SRS_CreateHardLimiterObj(void* pBuf);
/* init and configuration APIs */
SRSResult			SRS_InitHardLimiterObj(SRSHardLimiterObj hlObj);

/* main APIs */
SRSResult	SRS_HardLimiter(SRSHardLimiterObj hlObj, SRSStereoCh *input, SRSStereoCh *output, int blockSize, void *ws);
SRSResult	SRS_HardLimiterProcess(SRSHardLimiterObj hlObj, SRSStereoCh *input, SRSStereoCh *output, int blockSize, void *ws);


void		SRS_SetHardLimiterControlDefaults(SRSHardLimiterObj hlObj);

int			SRS_GetHardLimiterEnable(SRSHardLimiterObj hlObj);
void		SRS_SetHardLimiterEnable(SRSHardLimiterObj hlObj, int enable);

srs_int16	SRS_GetHardLimiterInputGain(SRSHardLimiterObj hlObj);		//I32.3	 0.0-4.0
void		SRS_SetHardLimiterInputGain(SRSHardLimiterObj hlObj, srs_int16 gain);

srs_int16	SRS_GetHardLimiterOutputGain(SRSHardLimiterObj hlObj);		//I32.3	 0.0-4.0
void		SRS_SetHardLimiterOutputGain(SRSHardLimiterObj hlObj, srs_int16 gain);

srs_int16	SRS_GetHardLimiterBypassGain(SRSHardLimiterObj hlObj);		//I32.1	 0.0-1.0
void		SRS_SetHardLimiterBypassGain(SRSHardLimiterObj hlObj, srs_int16 gain);

int			SRS_GetHardLimiterDelaylen(SRSHardLimiterObj hlObj);
void		SRS_SetHardLimiterDelaylen(SRSHardLimiterObj hlObj, int len);

srs_int32	SRS_GetHardLimiterBoost(SRSHardLimiterObj hlObj);			//I32.9	 0.001-256.0
void		SRS_SetHardLimiterBoost(SRSHardLimiterObj hlObj, srs_int32 boost);

srs_int16	SRS_GetHardLimiterLimit(SRSHardLimiterObj hlObj);			//I32.1	 0.0-1.0
void		SRS_SetHardLimiterLimit(SRSHardLimiterObj hlObj, srs_int16 limit);

/* the following APIs are added to add flexability of hl IP */
srs_int32	SRS_GetHardLimiterLCoef(SRSHardLimiterObj hlObj);
void		SRS_SetHardLimiterLCoef(SRSHardLimiterObj hlObj, srs_int32 lmtcoef);

srs_int32	SRS_GetHardLimiterHLThresh(SRSHardLimiterObj hlObj);
void		SRS_SetHardLimiterHLThresh(SRSHardLimiterObj hlObj, srs_int32 hlthresh);

void		SRS_SetHardLimiterDecaySmoothEnable(SRSHardLimiterObj hlObj, int enable);
int			SRS_GetHardLimiterDecaySmoothEnable(SRSHardLimiterObj hlObj);

/* version info query function */
srs_uint8	SRS_GetHardLimiterTechVersion(SRSVersion which);
srs_uint8	SRS_GetHardLimiterLibVersion(SRSVersion which);

#ifdef __cplusplus
}
#endif /*__cplusplus*/


#endif /*__SRS_HARDLIMITER_API_H__*/
