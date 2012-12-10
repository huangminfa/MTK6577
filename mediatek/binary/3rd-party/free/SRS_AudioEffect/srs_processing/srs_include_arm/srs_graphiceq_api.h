/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_graphiceq APIs
 *
 *  RCS keywords:
 *	$Id: //users/oscarh/srstech/srs_graphiceq/std_fxp/include/srs_graphiceq_api.h#1 $
 *  $Author: oscarh $
 *  $Date: 2010/06/25 $
 *	
********************************************************************************/

#ifndef __SRS_GRAPHICEQ_API_H__
#define __SRS_GRAPHICEQ_API_H__

#include "srs_fxp.h"

//Integer word length of band gains:
#define SRS_GEQ_BAND_GAIN_IWL		3

//dB constants for GEQ band gains:
#define SRS_GEQ_MINUS_12DB			SRS_FXP16(0.251, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_11DB			SRS_FXP16(0.282, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_10DB			SRS_FXP16(0.316, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_09DB			SRS_FXP16(0.355, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_08DB			SRS_FXP16(0.40, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_07DB			SRS_FXP16(0.45, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_06DB			SRS_FXP16(0.50, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_05DB			SRS_FXP16(0.56, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_04DB			SRS_FXP16(0.63, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_03DB			SRS_FXP16(0.71, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_02DB			SRS_FXP16(0.79, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_MINUS_01DB			SRS_FXP16(0.89, SRS_GEQ_BAND_GAIN_IWL)

#define SRS_GEQ_0DB					SRS_FXP16(1.00, SRS_GEQ_BAND_GAIN_IWL)

#define SRS_GEQ_PLUS_01DB			SRS_FXP16(1.12, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_02DB			SRS_FXP16(1.26, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_03DB			SRS_FXP16(1.41, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_04DB			SRS_FXP16(1.58, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_05DB			SRS_FXP16(1.78, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_06DB			SRS_FXP16(2.00, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_07DB			SRS_FXP16(2.24, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_08DB			SRS_FXP16(2.51, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_09DB			SRS_FXP16(2.82, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_10DB			SRS_FXP16(3.16, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_11DB			SRS_FXP16(3.55, SRS_GEQ_BAND_GAIN_IWL)
#define SRS_GEQ_PLUS_12DB			SRS_FXP16(4.00, SRS_GEQ_BAND_GAIN_IWL)

#endif /*__SRS_GRAPHICEQ_API_H__*/
