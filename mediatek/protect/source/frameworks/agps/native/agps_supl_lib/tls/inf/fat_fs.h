/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2005
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

/*****************************************************************************
 *
 * Filename:
 * ---------
 *	fs_release.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *    Include this file for using services of the fat file system
 *
 * Author:
 * -------
 *	Karen Hsu (mtk00681)
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision:   1.4  $ 
 * $Modtime:   May 16 2005 23:14:04  $ 
 * $Log:   //mtkvs01/vmdata/Maui_sw/archives/mcu/kal/Efs/include/fat_fs.h-arc  $
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Dec 18 2009 mtk02187
 * [MAUI_02017923] [System Service][File System][Change Feature] Rename fs_config.h to custom_fs.h (inc
 * 
 *
 * Dec 18 2009 mtk02187
 * [MAUI_02017923] [System Service][File System][Change Feature] Rename fs_config.h to custom_fs.h (inc
 * 
 *
 * Nov 30 2009 mtk02187
 * [MAUI_01966789] [System Service][File System][New Feature] Internal hint management
 *    
 * 
 *    Rev 1.4   May 17 2005 00:32:56   BM_Trunk
 * Karlos:
 * add copyright and disclaimer statement
 * 
 *    Rev 1.3   Jan 18 2005 00:37:04   BM
 * append new line in W05.04
 * 
 *    Rev 1.2   Nov 03 2004 17:36:52   mtk00681
 * Remove RVCT warning
 * Resolution for 8571: [FS][BugFix]Remove RVCT warning
 * 
 *    Rev 1.1   May 13 2004 23:19:36   mtk00681
 * Separate MTK developed function
 * Resolution for 5317: [FS][AddFeature]Wrap File System
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _FS_RELEASE_H
#define _FS_RELEASE_H

#ifndef _KAL_RELEASE_H
#include "kal_release.h"
#endif

#ifndef _CUSTOM_FS_H
//#include "custom_fs.h"
#endif

#ifndef _FS_ERRNO_H
#include "fs_errcode.h"
#endif

#ifndef _FS_TYPE_H
#include "fs_type.h"
#endif

#ifndef _FS_FUNC_H
#include "fs_func.h"
#endif

#endif //_FS_RELEASE_H


