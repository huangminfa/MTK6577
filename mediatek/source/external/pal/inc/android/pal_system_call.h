/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/

/*******************************************************************************
 * Filename:
 * ---------
 *  pal_system_call.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  Definition of system calls used in PAL
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
/* porting layer */
/* MAUI */

#ifndef _PAL_SYSTEM_CALL_H_
#define _PAL_SYSTEM_CALL_H_

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/

/*******************************************************************************
*                                 M A C R O S
********************************************************************************
*/
#ifndef _PAL_MAUI_   /* Not on MAUI */
typedef void(*kal_timer_func_ptr)(void*);
#endif

/*****************************************************************************
 * PAL TIMER Definition
 *****************************************************************************/
#ifdef _PAL_MAUI_   /* Not on MAUI */
typedef struct
{
    eventid event_id;
    kal_timer_func_ptr callback_func;
    void *arg;
} pal_timer_table_struct;

typedef struct
{
    stack_timer_struct  base_timer;
    event_scheduler*    event_scheduler_ptr;
} pal_context_struct;
#else
typedef struct
{
    kal_timer_func_ptr callback_func;
    void *arg;
} pal_timer_table_struct;
#endif

/* exported functions */
/*******************************************************************************
*                  F U N C T I O N   D E C L A R A T I O N S
********************************************************************************
*/
/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_mem_copy
 * DESCRIPTION
 *  Memory copy from source addr to dest addr
 * PARAMETERS
 *  dest        [IN]    Destination address
 *  src        	[IN]    Source address
 *  n        	[IN]    number of byte to copy
 * RETURNS
 *  void
 *****************************************************************************/
extern void* mtk_wcn_sys_mem_copy ( void *dest, const void *src, UINT32 n );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_mem_set
 * DESCRIPTION
 *  Memory copy from source addr to dest addr
 * PARAMETERS
 *  dest        [IN]    Destination address
 *  c        	[IN]    the content of memory setting
 *  n        	[IN]    number of byte to set memrory
 * RETURNS
 *  void
 *****************************************************************************/
extern void* mtk_wcn_sys_mem_set ( void *dest, INT32 c, UINT32 n );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_mem_cmp
 * DESCRIPTION
 *  Memory compare for source addr and dest addr
 * PARAMETERS
 *  dest        [IN]    Destination address
 *  src        	[IN]    Source address
 *  n        	[IN]    number of byte to compare
 * RETURNS
 *   0: content match
 *  -1: content mismatch
 *****************************************************************************/
extern INT32 mtk_wcn_sys_mem_cmp ( void *dest, const void *src, UINT32 n );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_mem_alloc
 * DESCRIPTION
 *  Memory allocation by the input size
 * PARAMETERS
 *  n        	[IN]    number of byte to allocate
 * RETURNS
 *   void*
 *****************************************************************************/
extern void* mtk_wcn_sys_mem_alloc ( UINT32 n );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_mem_free
 * DESCRIPTION
 *  Memory free of the memory address
 * PARAMETERS
 *  dest        [IN]    Destination address
 * RETURNS
 *   0: free success
 *  -1: already free
 *  -2: never allocated
 *****************************************************************************/
extern void mtk_wcn_sys_mem_free ( void *dest );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_timer_start
 * DESCRIPTION
 *  Start timer function
 * PARAMETERS
 *  timer_id    [IN]    Timer ID
 *  func        [IN]    callback function pointer
 *  arg        	[IN]    callback function arguments
 * RETURNS
 *  void
 *****************************************************************************/
extern void mtk_wcn_sys_timer_start ( UINT8 timer_id, UINT32 period, void (*func)(void*), void* arg );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_timer_stop
 * DESCRIPTION
 *  Stop timer function
 * PARAMETERS
 *  timer_id    [IN]    Timer ID
 * RETURNS
 *  void
 *****************************************************************************/
extern void mtk_wcn_sys_timer_stop ( UINT8 timer_id );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_core_timer_start
 * DESCRIPTION
 *  Start timer function
 * PARAMETERS
 *  timer_id    [IN]    Timer ID
 *  func        [IN]    callback function pointer
 *  arg        	[IN]    callback function arguments
 * RETURNS
 *  void
 *****************************************************************************/
extern void mtk_wcn_sys_core_timer_start ( UINT8 timer_id, UINT32 period, void (*func)(void*), void *arg );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_core_timer_stop
 * DESCRIPTION
 *  Stop timer function
 * PARAMETERS
 *  timer_id    [IN]    Timer ID
 * RETURNS
 *  void
 *****************************************************************************/
extern void mtk_wcn_sys_core_timer_stop ( UINT8 timer_id );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_core_timer_expiry_callback
 * DESCRIPTION
 *  Stop timer function
 * PARAMETERS
 *  timer_id    [IN]    Timer ID
 * RETURNS
 *  void
 *****************************************************************************/
extern void mtk_wcn_sys_core_timer_expiry_callback ( void *arg );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_get_system_time
 * DESCRIPTION
 *  Get system time (unit: ms)
 * PARAMETERS
 *  sys_time    [OUT]    system time value in ms
 * RETURNS
 *  void
 *****************************************************************************/
extern void mtk_wcn_sys_get_system_time ( UINT32* sys_time );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_core_timer_init
 * DESCRIPTION
 *  Find the earliest timer node
 * PARAMETERS
 *  none.
 * RETURNS
 *  timer_id of the earliest timer
 *****************************************************************************/
extern void mtk_wcn_sys_core_timer_init( void );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_mdelay
 * DESCRIPTION
 *  Function to delay (unit of msec.)
 * PARAMETERS
 *  msec: milli-sec.
 * RETURNS
 *  none.
 *****************************************************************************/
extern void mtk_wcn_sys_mdelay( UINT32 msec );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_wndrv_data_path_on
 * DESCRIPTION
 *  Turn on wndrv data interface
 * PARAMETERS
 *  none.
 * RETURNS
 *  -1: fail, >=0: success
 *****************************************************************************/
extern INT32 mtk_wcn_sys_wndrv_data_path_on( void );

/*****************************************************************************
 * FUNCTION
 *  mtk_wcn_sys_wndrv_data_path_off
 * DESCRIPTION
 *  Turn off wndrv data interface
 * PARAMETERS
 *  none.
 * RETURNS
 *  none.
 *****************************************************************************/
extern void mtk_wcn_sys_wndrv_data_path_off( void );


#ifndef _PAL_MAUI_  /* Not on MAUI */
extern ilm_struct* allocate_ilm( pal_mod_type_enum );
extern void pal_free_ilm( ilm_struct* ilm_ptr );
extern void SEND_ILM ( pal_mod_type_enum, pal_mod_type_enum, pal_sap_type_enum, ilm_struct* );
#endif

#endif
