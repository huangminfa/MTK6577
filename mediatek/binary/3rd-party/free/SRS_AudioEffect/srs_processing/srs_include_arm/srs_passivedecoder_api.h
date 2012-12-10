/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_passivedecoder APIs
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_cshp/std_fxp/include/srs_passivedecoder_api.h#2 $
 *  $Author: oscarh $
 *  $Date: 2010/09/21 $
 *	
********************************************************************************/

#ifndef __SRS_PASSIVEDECODER_API_H__
#define __SRS_PASSIVEDECODER_API_H__

#include "srs_cshp_def.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

SRSPassiveDecoderObj	SRS_CreatePassiveDecoderObj(void *pBuf);

void	SRS_InitPassiveDecoderObj16k(SRSPassiveDecoderObj pdObj);
void	SRS_InitPassiveDecoderObj22k(SRSPassiveDecoderObj pdObj);
void	SRS_InitPassiveDecoderObj24k(SRSPassiveDecoderObj pdObj);
void	SRS_InitPassiveDecoderObj32k(SRSPassiveDecoderObj pdObj);
void	SRS_InitPassiveDecoderObj44k(SRSPassiveDecoderObj pdObj);
void	SRS_InitPassiveDecoderObj48k(SRSPassiveDecoderObj pdObj);

void	SRS_PassiveDecoder(SRSPassiveDecoderObj pdObj, SRSStereoCh *audioIn, SRS5_1Ch *audioOut, int blockSize);




#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif /*__SRS_PASSIVEDECODER_API_H__*/
