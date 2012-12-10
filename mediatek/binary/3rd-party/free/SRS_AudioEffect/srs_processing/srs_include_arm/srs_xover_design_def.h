/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2011 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS audio crossover filters (Linkwitz¨CRiley crossover) design types, constants
 *
 *  Authour: Oscar Huang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/
#ifndef __SRS_XOVER_DESIGN_DEF_H__
#define __SRS_XOVER_DESIGN_DEF_H__

#include "srs_iir_design_def.h"

//The order of xover filter must be even!
#define SRS_XOVER_FLOAT_COEFFICIENT_ARRAY_LEN(order)	(5*(order)/2+1)	//in double type elements
#define SRS_XOVER_FXP_COEFFICIENT_ARRAY_LEN(order)		(6*(order)/2+2)	//in srs_int32 or srs_int16 elements

#define SRS_XOVER_DESIGN_WORKSPACE_SIZE(order)			((((order)/2)*5+1)*sizeof(double)*2+(5*((order)/2)+3*ANALYSIS_BLOCKSIZE)*sizeof(double)+8) //in bytes


#endif //__SRS_XOVER_DESIGN_DEF_H__
