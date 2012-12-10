/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS ParametricEQ filter design types, constants
 *
 *  Authour: Oscar Huang
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_designer/std_fxp/include/srs_parametriceq_design_def.h#1 $
 *  $Author: oscarh $
 *  $Date: 2010/09/26 $
 *	
********************************************************************************/
#ifndef __SRS_PARAMETRICEQ_DESIGN_DEF_H__
#define __SRS_PARAMETRICEQ_DESIGN_DEF_H__

#include "srs_filter_design_def.h"

typedef struct
{
	double CenterFreq;	//in Hz, physical frequency
	double QFactor;		//ratio of band width/CenterFreq
	double Gain;		//gain of the band in dB
} SRSParametriceqBandSpec;

typedef struct
{
	int						NumOfBands;		//The number of bands
	SRSParametriceqBandSpec *BandSpecs;		//Specification array of all bands
	double					SamplingRate;	//Sampling rate in Hz
} SRSParametriceqSpec;

#define SRS_PEQ_FLOAT_COEFFICIENT_ARRAY_LEN(nBands)		(5*(nBands)+1)	//in double type elements
#define SRS_PEQ_FXP32_COEFFICIENT_ARRAY_LEN(nBands)		(6*(nBands)+2)	//in srs_int32 type elements
#define SRS_PEQ_DESIGN_WORKSPACE_SIZE(nBands)	(((nBands)*5+1)*sizeof(double)+(5*(nBands)+3*ANALYSIS_BLOCKSIZE)*sizeof(double)+8) //in bytes


#endif //__SRS_PARAMETRICEQ_DESIGN_DEF_H__

