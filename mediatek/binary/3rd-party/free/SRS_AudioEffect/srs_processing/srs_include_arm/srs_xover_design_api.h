/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS audio crossover filters (Linkwitz¨CRiley crossover) filter design APIs
 *
 *  Authour: Oscar Huang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/
#ifndef __SRS_XOVER_DESIGN_API_H__
#define __SRS_XOVER_DESIGN_API_H__

#include "srs_typedefs.h"
#include "srs_xover_design_def.h"

#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

/*************************************************************************************************
 * Design floating point xover filters
 * Parameters:
 *		cutoff: [IN] The cutoff frequency
 *		order:	[IN] The order of the L-R filter
 *		sampleRate: [IN] The sampling rate
 *		lpfCoefs, hpfCoefs: [OUT] The designed LPF and HPF xover filter coefficients. The filter coefficients are
 *                    returned in the array in the following pattern respectively:

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
SRSResult SRS_XoverFilterDesignFloat(double cutoff, int order, int sampleRate, double *lpfCoefs, double *hpfCoefs);


/*************************************************************************************************
 * Design 32-bit/16-bit fixed point xover filters
 * Parameters:
 *		cutoff: [IN] The cutoff frequency
 *		order:	[IN] The order of the L-R filter
 *		sampleRate: [IN] The sampling rate
 *		lpfCoefs, hpfCoefs: [OUT] The designed LPF and HPF xover filter coefficients. The filter coefficients are
 *                    returned in the array in the following pattern respectively:


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
		ws:	[IN] workspace scratch memory, whose size must be at least SRS_XOVER_DESIGN_WORKSPACE_SIZE(order)
 
 * Return: SRS_NO_ERROR if design succeeds.Otherwise, an error code.

**************************************************************************************************/
SRSResult SRS_XoverFilterDesignFxp16(double cutoff, int order, int sampleRate, srs_int16 *lpfCoefs, srs_int16 *hpfCoefs, void *ws);
SRSResult SRS_XoverFilterDesignFxp32(double cutoff, int order, int sampleRate, srs_int32 *lpfCoefs, srs_int32 *hpfCoefs, void *ws);

#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif //__SRS_XOVER_DESIGN_API_H__
