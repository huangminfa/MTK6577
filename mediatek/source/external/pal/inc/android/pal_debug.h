/*******************************************************************************
* Copyright (c) 2007 MediaTek Inc.
*
* All rights reserved. Copying, compilation, modification, distribution
* or any other use whatsoever of this material is strictly prohibited
* except in accordance with a Software License Agreement with
* MediaTek Inc.
********************************************************************************
*/

/*******************************************************************************
* LEGAL DISCLAIMER
*
* BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND
* AGREES THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK
* SOFTWARE") RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE
* PROVIDED TO BUYER ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY
* DISCLAIMS ANY AND ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT
* LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
* PARTICULAR PURPOSE OR NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE
* ANY WARRANTY WHATSOEVER WITH RESPECT TO THE SOFTWARE OF ANY THIRD PARTY
* WHICH MAY BE USED BY, INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK
* SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY
* WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE
* FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S SPECIFICATION OR TO
* CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
* BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
* LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL
* BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT
* ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY
* BUYER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
* THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
* WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT
* OF LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING
* THEREOF AND RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN
* FRANCISCO, CA, UNDER THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE
* (ICC).
********************************************************************************
*/

/*******************************************************************************
 * Filename:
 * ---------
 *  pal_debug.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  PAL debug macro
 *
 * Author:
 * -------
 *  Nelson Chang (mtk02783)
 *
 *==============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Log$
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *==============================================================================
 *******************************************************************************/

#ifndef _PAL_DEBUG_H
#define _PAL_DEBUG_H

/*******************************************************************************
*                         C O M P I L E R   F L A G S
********************************************************************************
*/

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/
#ifdef _PAL_MAUI_
#include "kal_trace.h"
#include "pal_trc.h"
#else
#define LOG_TAG "[BT3.0+HS]PAL"
#include <utils/Log.h>
#include <stdarg.h>
#include <cutils/xlog.h>
#endif
#include "mtkpal_porting.h"

/*******************************************************************************
*                              C O N S T A N T S
********************************************************************************
*/
//const kal_uint8	g_pal_kal_trace = 0;

/*******************************************************************************
*                             D A T A   T Y P E S
********************************************************************************
*/

/*******************************************************************************
*                            P U B L I C   D A T A
********************************************************************************
*/


/*******************************************************************************
*                           P R I V A T E   D A T A
********************************************************************************
*/

/*******************************************************************************
*                                 M A C R O S
********************************************************************************
*/
/* TRACE MACRO */
#ifdef _PAL_MAUI_
#define PAL_TRACE0( tst_cls, tst_id, str ) 								\
{ 																		\
		kal_wap_trace( MOD_PAL, tst_cls, str ); 						\
}																		
 
#define PAL_TRACE1( tst_cls, tst_id, str, a )		                    \
{                                                                     	\
		kal_wap_trace( MOD_PAL, tst_cls, str, a );						\
}																		

#define PAL_TRACE2( tst_cls, tst_id, str, a, b )                     	\
{																		\
		kal_wap_trace( MOD_PAL, tst_cls, str, a, b );					\
}

#define PAL_TRACE3( tst_cls, tst_id, str, a, b, c )                  	\
{																		\
		kal_wap_trace( MOD_PAL, tst_cls, str, a, b, c );				\
}
 
#define PAL_TRACE4( tst_cls, tst_id, str, a, b, c, d )               	\
{																		\
		kal_wap_trace( MOD_PAL, tst_cls, str, a, b, c, d );				\
}
 
#define PAL_TRACE5( tst_cls, tst_id, str, a, b, c, d, e )            	\
{																		\
		kal_wap_trace( MOD_PAL, tst_cls, str, a, b, c,  d, e );			\
}

#define PAL_TRACE6( tst_cls, tst_id, str, a, b, c, d, e, f )         	\
{																		\
		kal_wap_trace( MOD_PAL, tst_cls, str, a, b, c, d, e, f );		\
}

#define PAL_TRACE7( tst_cls, tst_id, str, a, b, c, d, e, f, g )      	\
{																		\
		kal_wap_trace( MOD_PAL, tst_cls, str, a, b, c, d, e, f, g );	\
}

#define PAL_TRACE8( tst_cls, tst_id, str, a, b, c, d, e, f, g, h )     	\
{																		\
		kal_wap_trace( MOD_PAL, tst_cls, str, a, b, c, d, e, f, g, h );	\
}

#define PAL_TRACE9( tst_cls, tst_id, str, a, b, c, d, e, f, g, h, i )	\
{																		\
		kal_wap_trace( MOD_PAL, tst_cls, str, a, b, c, d, e, f, g, h, i );	\
}

#else
#if 0
#define PAL_TRACE0( tst_cls, tst_id, str )                              \
{                                                                       \
}

#define PAL_TRACE1( tst_cls, tst_id, str, s... )                        \
{                                                                       \
}

#define PAL_TRACE2( tst_cls, tst_id, str, s... )                        \
{                                                                       \
}

#define PAL_TRACE3( tst_cls, tst_id, str, s... )                        \
{                                                                       \
}

#define PAL_TRACE4( tst_cls, tst_id, str, s... )                        \
{                                                                       \
}

#define PAL_TRACE5( tst_cls, tst_id, str, s... )                        \
{                                                                       \
}

#define PAL_TRACE6( tst_cls, tst_id, str, s... )                        \
{                                                                       \
}

#define PAL_TRACE7( tst_cls, tst_id, str, s... )                        \
{                                                                       \    
}

#define PAL_TRACE8( tst_cls, tst_id, str, s... )                        \
{                                                                       \
}

#define PAL_TRACE9( tst_cls, tst_id, str, s... )                        \
{                                                                       \
}
#else
#define PAL_TRACE0( tst_cls, tst_id, str )                              \
{                                                                       \
    XLOGD(str);                                                         \
    XLOGD("\n");                                                        \
}

#define PAL_TRACE1( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    XLOGD(str , ## s);                                                  \    
    XLOGD("\n");                                                        \
}

#define PAL_TRACE2( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    XLOGD(str , ## s);                                                  \    
    XLOGD("\n");                                                        \
}

#define PAL_TRACE3( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    XLOGD(str , ## s);                                                  \    
    XLOGD("\n");                                                        \
}

#define PAL_TRACE4( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    XLOGD(str , ## s);                                                  \
    XLOGD("\n");                                                        \
}

#define PAL_TRACE5( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    XLOGD(str , ## s);                                                  \
    XLOGD("\n");                                                        \
}

#define PAL_TRACE6( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    XLOGD(str , ## s);                                                  \
    XLOGD("\n");                                                        \
}

#define PAL_TRACE7( tst_cls, tst_id, str, s... )                        \
{                                                                       \    
    XLOGD(str , ## s);                                                  \
    XLOGD("\n");                                                        \
}

#define PAL_TRACE8( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    XLOGD(str , ## s);                                                  \
    XLOGD("\n");                                                        \
}

#define PAL_TRACE9( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    XLOGD(str , ## s);                                                  \
    XLOGD("\n");                                                        \
}
#endif
#endif


/* Compress trace definition */
#ifdef _PAL_MAUI
#define PAL_COM_TRACE0( tst_cls, tst_id, str ) 								\
{ 																		    \
		kal_trace( tst_cls, tst_id ); 						                \
}																		
 
#define PAL_COM_TRACE1( tst_cls, tst_id, str, a )		                    \
{                                                                     	    \
		kal_trace( tst_cls, tst_id, a );						            \
}																		    \

#define PAL_COM_TRACE2( tst_cls, tst_id, str, a, b )                     	\
{																		    \
		kal_trace( tst_cls, tst_id, a, b );					                \
}

#define PAL_COM_TRACE3( tst_cls, tst_id, str, a, b, c )                  	\
{																		    \
		kal_trace( tst_cls, tst_id, a, b, c );				                \
}
 
#define PAL_COM_TRACE4( tst_cls, tst_id, str, a, b, c, d )               	\
{																		    \
		kal_trace( tst_cls, tst_id, a, b, c, d );				            \
}
 
#define PAL_COM_TRACE5( tst_cls, tst_id, str, a, b, c, d, e )            	\
{																		    \
		kal_trace( tst_cls, tst_id, a, b, c,  d, e );			            \
}

#define PAL_COM_TRACE6( tst_cls, tst_id, str, a, b, c, d, e, f )         	\
{																		    \
		kal_trace( tst_cls, tst_id, a, b, c, d, e, f );		                \
}

#define PAL_COM_TRACE7( tst_cls, tst_id, str, a, b, c, d, e, f, g )      	\
{																		    \
		kal_trace( tst_cls, tst_id, a, b, c, d, e, f, g );	                \
}

#define PAL_COM_TRACE8( tst_cls, tst_id, str, a, b, c, d, e, f, g, h )     	\
{																		    \
		kal_trace( tst_cls, tst_id, a, b, c, d, e, f, g, h );	            \
}

#define PAL_COM_TRACE9( tst_cls, tst_id, str, a, b, c, d, e, f, g, h, i )	\
{																		    \
		kal_trace( tst_cls, tst_id, a, b, c, d, e, f, g, h, i );	        \
}

#else
#if 1

#define PAL_COM_TRACE0( tst_cls, tst_id, str )

#define PAL_COM_TRACE1( tst_cls, tst_id, str, s... )

#define PAL_COM_TRACE2( tst_cls, tst_id, str, s... )

#define PAL_COM_TRACE3( tst_cls, tst_id, str, s... )

#define PAL_COM_TRACE4( tst_cls, tst_id, str, s... )

#define PAL_COM_TRACE5( tst_cls, tst_id, str, s... )

#define PAL_COM_TRACE6( tst_cls, tst_id, str, s... )

#define PAL_COM_TRACE7( tst_cls, tst_id, str, s... )

#define PAL_COM_TRACE8( tst_cls, tst_id, str, s... )

#define PAL_COM_TRACE9( tst_cls, tst_id, str, s... )

#else
#define PAL_COM_TRACE0( tst_cls, tst_id, str )                              \
{                                                                           \
    LOGD(str);                                                              \    
    LOGD("\n");                                                             \
    printf(str);                                                            \    
    printf("\n");                                                           \
}

#define PAL_COM_TRACE1( tst_cls, tst_id, str, s... )                        \
{                                                                           \
    LOGD(str , ## s);                                                       \    
    LOGD("\n");                                                             \
    printf(str , ## s);                                                     \
    printf("\n");                                                           \
}

#define PAL_COM_TRACE2( tst_cls, tst_id, str, s... )                    \
{                                                                       \
    LOGD(str , ## s);                                                   \    
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}

#define PAL_COM_TRACE3( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    LOGD(str , ## s);                                                   \    
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}

#define PAL_COM_TRACE4( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    LOGD(str , ## s);                                                   \    
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}

#define PAL_COM_TRACE5( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    LOGD(str , ## s);                                                   \    
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}

#define PAL_COM_TRACE6( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    LOGD(str , ## s);                                                   \    
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}

#define PAL_COM_TRACE7( tst_cls, tst_id, str, s... )                        \
{                                                                       \    
    LOGD(str , ## s);                                                   \    
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}

#define PAL_COM_TRACE8( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    LOGD(str , ## s);                                                   \    
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}

#define PAL_COM_TRACE9( tst_cls, tst_id, str, s... )                        \
{                                                                       \
    LOGD(str , ## s);                                                   \
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}
#endif
#endif


/* WAP and prompt trace definition */
#ifndef _PAL_MAUI
#define kal_wap_trace( tst_cls, tst_id, str, s... )                     \
{                                                                       \
    LOGD(str , ## s);                                                   \
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}

#define kal_prompt_trace( tst_cls, str, s... )                          \
{                                                                       \
    LOGD(str , ## s);                                                   \
    LOGD("\n");                                                         \
    printf(str , ## s);                                                 \
    printf("\n");                                                       \
}
#endif


/* ASSERT MACRO */
#ifdef _PAL_MAUI_
#define PAL_ASSERT(a)						       									\
	do {							       											\
		if (!(a)) {					       											\
			pal_printf(MOD_PAL,"PAL_ASSERT FAILED '" #a "' "	       				\
						"%s:%d\n", __FILE__, __LINE__);      						\
			ASSERT(0);				       											\
		}						       												\
	} while (0)
	
	/*	pal_printf(MOD_PAL,"PAL_ASSERT FAILED '" #a "' "	       	\ */
	/*				       "%s %s:%d\n",			       						\ */
	/*			       __FUNCTION__, __FILE__, __LINE__);      				\*/
	/* LOG_FUNC("Assertion failed: %s:%d %s\n", __FILE__, __LINE__, #_a); 		\ */
	/*			exit(1);				       											*/
#else
#define PAL_ASSERT(a)                                                           \
    do{                                                                         \
        if (!(a)) {					       										\
            PAL_TRACE2(TRACE_ERROR, 0, "PAL_ASSERT FAILED '" #a "' "	       	\
						"%s:%d\n", __FILE__, __LINE__);      					\
			*((int *)3) = 1;                                                           \
            while(1);                                                           \
        }                                                                       \
    }while(0)
#endif
//#define PAL_ASSERT(a) do { } while (0);

/*
BEGIN_TRACE_MAP(MOD_PAL)
	/ ----------------- TRACE_FUNC trace class ------------------- 
	/ ----------------- TRACE_STATE trace class ------------------ 
   TRC_MSG( INFO_PAL_DISCONNECTED_STATE, "[PHYSICAL LINK Manager] Disconnected State" )
END_TRACE_MAP(MOD_PAL)
*/

/*******************************************************************************
*                  F U N C T I O N   D E C L A R A T I O N S
********************************************************************************
*/

/*******************************************************************************
*                              F U N C T I O N S
********************************************************************************
*/
#endif /* _PAL_DEBUG_H */

