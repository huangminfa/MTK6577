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
 *  mtkpal_porting.h
 *
 * Project:
 * --------
 *  MAUI
 *
 * Description:
 * ------------
 *  PAL porting
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

#ifndef _MTKPAL_PORTING_H_
#define _MTKPAL_PORTING_H_

#ifndef __inline
#define __inline static
#endif /* __inline */

/*******************************************************************************
*                    E X T E R N A L   R E F E R E N C E S
********************************************************************************
*/
#ifdef _PAL_MAUI_
/*common header*/
#include "kal_release.h"
#include "stack_ltlcom.h"
#include "app_ltlcom.h"         /* Task message communiction */
#include "app_buff_alloc.h"
#endif
/*specific header*/

/*******************************************************************************
*                                 M A C R O S
********************************************************************************
*/
#ifndef __MTK_TARGET__
typedef signed char        INT8;
typedef signed short       INT16;
typedef signed long        INT32;
typedef unsigned char      UINT8;
typedef unsigned short     UINT16;
typedef unsigned long      UINT32;
#endif

#ifndef _PAL_MAUI_  /* Not on MAUI system */
/* kal function definitions */
#define kal_sprintf sprintf
//#define kal_wap_trace(...)
//#define kal_prompt_trace(...)
#define construct_local_para(size, TD_UL)   malloc( size )
#define construct_peer_buff(size, head, tail, TD_UL)  malloc( size )

#ifndef os_malloc
#ifndef _PAL_MAUI_
#define os_malloc(s) malloc((s))
#else
#define os_malloc(s) get_ctrl_buffer((s))
#endif
#endif
#ifndef os_realloc
#define os_realloc(p, s) realloc((p), (s))
#endif
#ifndef os_free
#ifndef _PAL_MAUI_
#define os_free(p) free((p))
#else
#define os_free(p)  free_ctrl_buffer((p))
#endif
#endif

#ifndef os_memcpy
#define os_memcpy(d, s, n) memcpy((d), (s), (n))
#endif
#ifndef os_memmove
#define os_memmove(d, s, n) memmove((d), (s), (n))
#endif
#ifndef os_memset
#define os_memset(s, c, n) memset(s, c, n)
#endif
#ifndef os_memcmp
#define os_memcmp(s1, s2, n) memcmp((s1), (s2), (n))
#endif

#ifndef os_strlen
#define os_strlen(s) strlen(s)
#endif


#define KAL_TRUE    (1)
#define KAL_FALSE   (0)
    typedef signed char             kal_int8;
    typedef signed short            kal_int16;
    typedef signed long             kal_int32;
    typedef unsigned char           kal_bool;
    typedef unsigned char           kal_uint8;
    typedef unsigned short          kal_uint16;
    typedef unsigned long           kal_uint32;
    typedef char                    kal_char;
    typedef void                    local_para_struct;
    typedef void                    peer_buff_struct;

    typedef unsigned int            u32;
    typedef unsigned short          u16;
    typedef unsigned char           u8;
    typedef int                     s32;
    typedef short                   s16;
    typedef char                    s8;
    typedef enum { FALSE = 0, TRUE = 1 } Boolean;

    /*   Define   NULL   pointer   value   */ 
    #ifndef   NULL 
        #ifdef     __cplusplus 
            #define   NULL         0 
        #else 
            #define   NULL         ((void   *)0) 
        #endif 
    #endif 

    /* PAL SAP enum */
    typedef enum
    {
        MSG_ID_PAL_MSG_CODE_BEGIN = 0x80000000,
        #include "pal_sap.h"
#if 0    
        /****************************************************************************
         *  Messages: WNDRV -> PAL
         ****************************************************************************/
        MSG_ID_PAL_MSG_CODE_BEGIN = 0x80000000,
        MSG_ID_PAL_WNDRV_MAC_START_IND = MSG_ID_PAL_MSG_CODE_BEGIN,
        MSG_ID_PAL_WNDRV_MAC_CONNECT_IND,
        MSG_ID_PAL_WNDRV_MAC_CONNECT_FAIL_IND,
        MSG_ID_PAL_WNDRV_MAC_DISCONNECT_IND,
        MSG_ID_PAL_WNDRV_MAC_CANCEL_IND,
        MSG_ID_PAL_WNDRV_QUERY_STATUS_IND,
        MSG_ID_PAL_WNDRV_SYNC_TSF_IND,
        /* For Data Link Manager */
        MSG_ID_PAL_WNDRV_DATA_IND,
        MSG_ID_PAL_WNDRV_DATA_BLOCK_FREE_NUM_IND,

        /****************************************************************************
         *  Messages: BT -> PAL
         ****************************************************************************/
        /* For PAL Manager */
        MSG_TAG_PAL_BT_SEND_START,
        MSG_ID_PAL_BT_READ_LOCAL_VERSION_INFO_COMMAND = MSG_TAG_PAL_BT_SEND_START,
        MSG_ID_PAL_BT_READ_LOCAL_AMP_INFO_COMMAND,
        MSG_ID_PAL_BT_READ_LOCAL_AMP_ASSOC_COMMAND,
        MSG_ID_PAL_BT_RESET_COMMAND,
        MSG_ID_PAL_BT_READ_DATA_BLOCK_SIZE_COMMAND,
		//MSG_ID_PAL_BT_SET_EVENT_MASK_PAGE2_COMMAND,     // new add (2010/05/18)
        /* For Physical Link Manager */
        MSG_ID_PAL_BT_READ_LINK_QUALITY_COMMAND,
        MSG_ID_PAL_BT_READ_RSSI_COMMAND,
        MSG_ID_PAL_BT_WRITE_REMOTE_AMP_ASSOC_COMMAND,
        MSG_ID_PAL_BT_CREATE_PHYSICAL_LINK_COMMAND,
        MSG_ID_PAL_BT_ACCEPT_PHYSICAL_LINK_COMMAND,
        MSG_ID_PAL_BT_READ_LINK_SUPERVISON_TIMEOUT_COMMAND,
        MSG_ID_PAL_BT_DISCONNECT_PHYSICAL_LINK_COMMAND,
        MSG_ID_PAL_BT_WRITE_LINK_SUPERVISON_TIMEOUT_COMMAND,
        /* For Logical Link Manager */
        MSG_ID_PAL_BT_CREATE_LOGICAL_LINK_COMMAND,
        MSG_ID_PAL_BT_ACCEPT_LOGICAL_LINK_COMMAND,
        MSG_ID_PAL_BT_FLOW_SPEC_MODIFY_COMMAND,
        MSG_ID_PAL_BT_DISCONNECT_LOGICAL_LINK_COMMAND,
        MSG_ID_PAL_BT_LOGICAL_LINK_CANCEL_COMMAND,
        MSG_ID_PAL_BT_READ_LOGICAL_LINK_ACCEPT_TIMEOUT_COMMAND,
        MSG_ID_PAL_BT_WRITE_LOGICAL_LINK_ACCEPT_TIMEOUT_COMMAND,
        /* For Data Link Manager */
//        MSG_ID_PAL_BT_ENHANCED_FLUSH_COMMAND,           // new add (2010/05/18)
        MSG_ID_PAL_BT_DATA_COMMAND,
        MSG_TAG_PAL_BT_SEND_END = MSG_ID_PAL_BT_DATA_COMMAND,

        /****************************************************************************
         *  Messages: PAL -> WNDRV
         ****************************************************************************/
        MSG_ID_PAL_WNDRV_MAC_START_REQ,
        MSG_ID_PAL_WNDRV_MAC_CONNECT_REQ,
        MSG_ID_PAL_WNDRV_ADD_KEY_REQ,
        MSG_ID_PAL_WNDRV_MAC_DISCONNECT_REQ,
        MSG_ID_PAL_WNDRV_MAC_CANCEL_REQ,
        MSG_ID_PAL_WNDRV_DATA_REQ,
        MSG_ID_PAL_WNDRV_QUERY_STATUS_REQ,
        MSG_ID_PAL_WNDRV_SET_ACTIVITY_REPORT_REQ,

        /****************************************************************************
        *  Messages: PAL -> BT
        ****************************************************************************/
        // For PAL Manager
        MSG_TAG_PAL_BT_RECV_START,
        MSG_ID_PAL_BT_READ_LOCAL_VERSION_COMPLETE_EVENT = MSG_TAG_PAL_BT_RECV_START,
        MSG_ID_PAL_BT_READ_LOCAL_AMP_INFO_COMPLETE_EVENT,
        MSG_ID_PAL_BT_READ_LOCAL_AMP_ASSOC_COMPLETE_EVENT,
        MSG_ID_PAL_BT_CHNAGE_AMP_STATUS_EVENT,
        MSG_ID_PAL_BT_RESET_COMPLETE_EVENT,
        MSG_ID_PAL_BT_READ_DATA_BLOCK_SIZE_COMPLETE_EVENT,
//        MSG_ID_PAL_BT_SET_EVENT_MASK_PAGE2_COMPLETE_EVENT,     // new add (2010/05/18)
        // For Physical Link Manager
        MSG_ID_PAL_BT_CHANNEL_SELECTED_EVENT,
        MSG_ID_PAL_BT_READ_LINK_QUALITY_COMPLETE_EVENT,
        MSG_ID_PAL_BT_READ_RSSI_COMPLETE_EVENT,
        MSG_ID_PAL_BT_WRITE_REMOTE_AMP_ASSOC_COMPLETE_EVENT,
        MSG_ID_PAL_BT_CREATE_PHYSICAL_LINK_STATUS_EVENT,
        MSG_ID_PAL_BT_ACCEPT_PHYSICAL_LINK_STATUS_EVENT,
        MSG_ID_PAL_BT_PHYSICAL_LINK_COMPLETE_EVENT,
        MSG_ID_PAL_BT_READ_LINK_SUPERVISON_TIMEOUT_COMPLETE_EVENT,
        MSG_ID_PAL_BT_DISCONNECT_PHYSICAL_LINK_STATUS_EVENT,
        MSG_ID_PAL_BT_DISCONNECT_PHYSICAL_LINK_COMPLETE_EVENT,
        MSG_ID_PAL_BT_PHYSICAL_LINK_LOSS_EARLY_WARNING_EVENT,
        MSG_ID_PAL_BT_WRITE_LINK_SUPERVISON_TIMEOUT_COMPLETE_EVENT,
        // For Logical Link Manager
        MSG_ID_PAL_BT_CREATE_LOGICAL_LINK_STATUS_EVENT,
        MSG_ID_PAL_BT_ACCEPT_LOGICAL_LINK_STATUS_EVENT,
        MSG_ID_PAL_BT_FLOW_SPEC_MODIFY_STATUS_EVENT,
        MSG_ID_PAL_BT_FLOW_SPEC_MODIFY_COMPLETE_EVENT,
        MSG_ID_PAL_BT_LOGICAL_LINK_COMPLETE_EVENT,
        MSG_ID_PAL_BT_DISCONNECT_LOGICAL_LINK_STATUS_EVENT,
        MSG_ID_PAL_BT_DISCONNECT_LOGICAL_LINK_COMPLETE_EVENT,
        MSG_ID_PAL_BT_LOGICAL_LINK_CANCEL_STATUS_EVENT,
        MSG_ID_PAL_BT_LOGICAL_LINK_CANCEL_COMPLETE_EVENT,
        MSG_ID_PAL_BT_READ_LOGICAL_LINK_ACCEPT_TIMEOUT_COMPLETE_EVENT,
        MSG_ID_PAL_BT_WRITE_LOGICAL_LINK_ACCEPT_TIMEOUT_COMPLETE_EVENT,
        // For Data Link Manager
        MSG_ID_PAL_BT_NUM_OF_COMPLETE_DATA_BLOCKS_EVENT,
//        MSG_ID_PAL_BT_ENHANCED_FLUSH_STATUS_EVENT,
//        MSG_ID_PAL_BT_ENHANCED_FLUSH_COMPLETE_EVENT,
        MSG_ID_PAL_BT_DATA_EVENT,
        MSG_TAG_PAL_BT_LAST_REQ_MSG = MSG_ID_PAL_BT_DATA_EVENT,
#endif
        /* For Timer and inject message */
        MSG_ID_TIMER_EXPIRY,
        MSG_ID_TST_INJECT_STRING,

        /* For HAL message */
        MSG_ID_PAL_HAL_WIFI_ON,
        MSG_ID_PAL_HAL_WIFI_OFF
    } pal_msg_type_enum;

    /* PAL module type enum */
    typedef enum 
    {   
        MOD_BT = 0,
        MOD_TIMER,
        MOD_MMI,
        MOD_BWCS,
        MOD_TST,
        MOD_SPP_DEFAULT,
        MOD_PAL,
        MOD_WNDRV,
        END_OF_MOD_ID
    } pal_mod_type_enum;
#if 0
    typedef enum
    {
        MOD_PAL = 0,
        MOD_BT,
        MOD_WNDRV
    } pal_mod_type_enum;
#endif

    /* PAL SAP type enum */
    typedef enum
    {
        PAL_WNDRV_SAP = 0,
        PAL_BT_SAP
    } pal_sap_type_enum;


    #define MAX_ILM_BUFFER_SIZE (2048*4)

    typedef struct _ilm_struct
    {
        unsigned char used;
        unsigned long msg_id;
        local_para_struct*   local_para_ptr; /* control message pointer */
        /* Belowed is not used in external platform */
        pal_mod_type_enum   src_mod_id;
        pal_mod_type_enum   dest_mod_id;
        unsigned char       sap_id;
        peer_buff_struct*   peer_buff_ptr;  /* data message pointer */
        unsigned char ilm_data[MAX_ILM_BUFFER_SIZE];
    } ilm_struct;

    typedef struct _tst_module_string_inject_struct 
    {
        UINT8   index;
        UINT8*  string;
    } tst_module_string_inject_struct;

    /* kal function definitions */
    __inline void * kal_mem_cpy(void* dest, const void* src, kal_uint32 size)
    {
        return memcpy( dest, src, size );    
    }

    __inline void * kal_mem_set(void* dest, kal_uint8 value, kal_uint32 size)
    {
        return memset( dest, value, size );
    }

    __inline kal_int32 kal_mem_cmp(const void* src1, const void* src2, kal_uint32 size)
    {
        return memcmp ( src1, src2, size );
    }

    __inline UINT8* get_pdu_ptr( peer_buff_struct* peer_p, UINT16* pdu_len)
    {
        *pdu_len = sizeof(peer_p);
        return peer_p;
    }

    __inline UINT8* get_ctrl_buffer( UINT8 byte_size )
    {
        UINT8* buf;
        buf = malloc(byte_size);
        return buf;
    }

    __inline void free_ctrl_buffer( void* buf )
    {
        free(buf);
        return;
    }

    __inline void free_peer_buff( void* buf )
    {
        return;
    }
#endif

/**
 * wpa_printf - conditional printf
 * @level: priority level (MSG_*) of the message
 * @fmt: printf format string, followed by optional arguments
 *
 * This function is used to print conditional debugging and error messages. The
 * output may be directed to stdout, stderr, and/or syslog based on
 * configuration.
 *
 * Note: New line '\n' is added to the end of the text when printing to stdout.
 */
#define pal_printf kal_prompt_trace
//void pal_printf(int module_id, char *fmt, ...);
/* add by saker for control buffer usage */
#define PAL_ALLOC_BUF(byte_size)     get_ctrl_buffer(byte_size)
#define PAL_FREE_BUF(buf)            free_ctrl_buffer(buf)

#endif
