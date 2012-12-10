/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS IIR filter design APIs
 *
 *  Authour: Zesen Zhuang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/
#ifndef __SRS_IIR_DESIGN_API_H__
#define __SRS_IIR_DESIGN_API_H__

#include "srs_typedefs.h"
#include "srs_iir_design_def.h"

#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

/*************************************************************************************************
 * Design floating point IIR filters
 * Parameters:
 *		peqSpec: [IN] PEQ specification: sampling rate, the number of bands, bands specs
 *		coefs:	[OUT] The designed filter coefficients. The filter coefficients are
 *                    returned in the array in the following pattern:

					//{
					//Band 0:
					//Coefficient B0
					//Coefficient B1
					//Coefficient B2
					//Coefficient A1
					//Coefficient A2
					               
					//Band 1:
					//Coefficient B0
					//Coefficient B1
					//Coefficient B2
					//Coefficient A1
					//Coefficient A2

					//...(more bands)
					      
					//Gain
					}
 
 * Return: SRS_NO_ERROR if design succeeds.Otherwise, an error code.

**************************************************************************************************/
SRSResult SRS_IirFilterDesignFloat(double cutoff_freq, int order, int sampleRate, SRSIirDesignType type, double *coefs);


/*************************************************************************************************
 * Design 32-bit/16-bit fixed point IIR filters
 * Parameters:
 *		coefs:	[OUT] The designed filter coefficients.The filter coefficients are
 *                    returned in the array in the following pattern:

					//{
					//Band 0:
					//iwl
					//Coefficient B0
					//Coefficient B1
					//Coefficient B2
					//Coefficient A1
					//Coefficient A2
					               
					//Band 1:
					//iwl
					//Coefficient B0
					//Coefficient B1
					//Coefficient B2
					//Coefficient A1
					//Coefficient A2

					//...(more bands)
					    
					//Gain iwl    
					//Gain
					}
		ws:	[IN] workspace scratch memory, whose size must be at least SRS_IIR_DESIGN_WORKSPACE_SIZE(order)
 
 * Return: SRS_NO_ERROR if design succeeds.Otherwise, an error code.

**************************************************************************************************/
SRSResult SRS_IirFilterDesignFxp16(double cutoff_freq, int order, int sampleRate, SRSIirDesignType type, srs_int16 *coefs, void *ws);
SRSResult SRS_IirFilterDesignFxp32(double cutoff_freq, int order, int sampleRate, SRSIirDesignType type, srs_int32 *coefs, void *ws);

#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif //__SRS_IIR_DESIGN_API_H__
