/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS Trubass filter design types, constants
 *
 *  Authour: Oscar Huang
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_designer/std_fxp/include/srs_trubass_design_def.h#2 $
 *  $Author: oscarh $
 *  $Date: 2011/01/12 $
 *	
********************************************************************************/
#ifndef __SRS_TRUBASS_DESIGN_DEF_H__
#define __SRS_TRUBASS_DESIGN_DEF_H__


#define SRS_SA_TRUBASS_COEFFICIENT_ARRAY_LEN	16		//in elements
#define SRS_SA_TRUBASS_DESIGN_WORKSPACE_SIZE	(sizeof(double)*SRS_SA_TRUBASS_COEFFICIENT_ARRAY_LEN+8)	//in bytes

//#define SRS_TRUBASS_DESIGN_WORKSPACE_SIZE	(sizeof(double)*8+8)	//in bytes

#endif //__SRS_TRUBASS_DESIGN_DEF_H__
