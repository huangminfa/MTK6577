/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS HP360 types, constants
 *
 *	Author: Zesen Zhuang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/

#ifndef __SRS_HP360_DEF_H__
#define __SRS_HP360_DEF_H__

#include "srs_typedefs.h"

/*Data type definition here:*/
typedef struct _SRSHp360Obj{int _;} * SRSHp360Obj;

#define SRS_HP360_OBJ_SIZE					(sizeof(_SRSHp360Obj_t)+26*4*sizeof(srs_int32)+32)
#define SRS_HP360_WORKSPACE_SIZE(blksize)	(sizeof(srs_int32)*(blksize)*5+8)

/////////////////////////////////////////////////////////////////////////////////////////////////
//SRS Internal Use:

typedef struct
{
	int							Enable;				//enable, 0: disabled, non-zero: enabled
	srs_int16					InputGain;
	srs_int16					OutputGain;
	srs_int16					BypassGain;	

	int							Delaylen;

	const srs_int16				*FrontFilter1Coef;
	const srs_int16				*FrontFilter2Coef;
	const srs_int16				*RearFilter1Coef;
	const srs_int16				*RearFilter2Coef;

} _SRSHp360Settings_t;


typedef struct
{
	srs_int32					LFrontFilter1State[2];
	srs_int32					LFrontFilter2State[2];
	srs_int32					RFrontFilter1State[2];
	srs_int32					RFrontFilter2State[2];
	srs_int32					LRearFilter1State[2];
	srs_int32					LRearFilter2State[2];
	srs_int32					RRearFilter1State[2];
	srs_int32					RRearFilter2State[2];
	
	srs_int32					*Ldelaybuf;
	srs_int32					*Rdelaybuf;
	srs_int32					*SLdelaybuf;
	srs_int32					*SRdelaybuf;

} _SRSHp360State_t;


typedef struct
{
	_SRSHp360Settings_t		Settings;
	_SRSHp360State_t		State;
} _SRSHp360Obj_t;

#endif //__SRS_HP360_DEF_H__
