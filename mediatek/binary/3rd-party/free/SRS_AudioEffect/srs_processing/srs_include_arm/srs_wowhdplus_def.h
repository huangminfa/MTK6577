/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  srs_wowhdplus typedefs, constants
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/

#ifndef __SRS_WOWHDPLUS_DEF_H__
#define __SRS_WOWHDPLUS_DEF_H__

#include "srs_typedefs.h"
#include "srs_iir_def.h"
#include "srs_focus_def.h"
#include "srs_definition_def.h"
#include "srs_sa_trubass_def.h"
#include "srs_srs3d_def.h"
#include "srs_widesurround_def.h"



#define	 SRS_WOWHDPLUS_GAIN_IWL		1

typedef  struct _SRSWowhdPlusObj{int _;} *SRSWowhdPlusObj;

#define SRS_WOWHDPLUS_OBJ_SIZE			(sizeof(_SRSWowhdPlusObj_t) + \
											/*xo lpf*/SRS_IIR_OBJ_SIZE(SRS_WOWHDPLUS_MAX_CROSSOVER_ORDER)*2 + \
											/*xo hpf*/SRS_IIR_OBJ_SIZE(SRS_WOWHDPLUS_MAX_CROSSOVER_ORDER)*2 + \
													  SRS_SA_TRUBASS_OBJ_SIZE + \
										   /*ptb hpf*/SRS_IIR_OBJ_SIZE(SRS_WOWHDPLUS_MAX_HPF_ORDER)*2 + \
													  SRS_SRS3D_OBJ_SIZE + \
													  SRS_FOCUS_OBJ_SIZE*2 + \
													  SRS_DEFINITION_OBJ_SIZE*2 + \
													  SRS_WIDESURROUND_OBJ_SIZE + \
													  8)

#define SRS_WOWHDPLUS_STATBUF_SIZE		SRS_WIDESURROUND_STATBUF_SIZE


#define SRS_WOWHDPLUS_WORKSPACE_SIZE		(_SRS_WHDP_MAX(\
												_SRS_WHDP_MAX(sizeof(srs_int32)*SRS_WOWHDPLUS_BLK_SZ*2+8, SRS_SA_TRUBASS_WORKSPACE_SIZE(SRS_WOWHDPLUS_BLK_SZ)), \
												_SRS_WHDP_MAX(SRS_SRS3D_WORKSPACE_SIZE(SRS_WOWHDPLUS_BLK_SZ), SRS_WIDESURROUND_WORKSPACE_SIZE) \
											 ) + sizeof(srs_int32)*SRS_WOWHDPLUS_BLK_SZ*2)

#define	SRS_WOWHDPLUS_BLK_SZ			SRS_WDSRD_BLK_SZ

#define SRS_WOWHDPLUS_MAX_CROSSOVER_ORDER	6		//The maximum order that the cross over filters can be
#define SRS_WOWHDPLUS_MAX_HPF_ORDER			6		//The maximum order that the HPF after SATruBas can be


typedef struct
{
	int					XoverOrder;		//The order of the Crossover filters
	const srs_int16		*XoverLpfCoefs;	//The coefficients of xover LPF
	const srs_int16		*XoverHpfCoefs;	//The coefficients of xover HPF
	
	int					HpfOrder;		//The post-trubass HPF order
	const srs_int16		*HpfCoefs;		//The post-trubass HPF coefficients
} SRSWowhdPlusFilterConfig;


//////////////////////////////////////////////////////////////////////////////////////
//SRS Internal Use:
#define _SRS_WHDP_MAX(a, b)	((a)>=(b)? (a):(b))

typedef struct
{
	int			BassEnable;	//Enable bass enhancement
	int			Srs3d_ws;	//srs3d enabled or ws enabled (exclusive, 1 if srs3d enabled)
	int			FocusEnable;
	int			DefEnable;
	srs_int16	InputGain;
	srs_int16	OutputGain;
} _SRSWowhdPlusSettings_t;

typedef struct
{
	
} _SRSWowhdPlusState_t;

typedef struct
{
	_SRSWowhdPlusSettings_t			Settings;

	SRSIirObj						CrossOverLPF[2]; //left, right LPFs
	SRSIirObj						CrossOverHPF[2];
	SRSSATruBassObj					SATruBass;
	SRSIirObj						HpfPTB[2];		//HPF post-TruBass
	SRSSrs3dObj						SRS3D;			
	SRSFocusObj						Focus[2];
	SRSDefinitionObj				Definition[2];
	SRSWideSurroundObj				WideSurround;
} _SRSWowhdPlusObj_t;



#endif /*__SRS_WOWHDPLUS_DEF_H__*/
