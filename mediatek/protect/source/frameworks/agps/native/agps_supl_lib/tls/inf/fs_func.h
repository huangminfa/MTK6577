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
 *	fs_func.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *    This file declares the exported APIs
 *
 * Author:
 * -------
 *	Karen Hsu (mtk00681)
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision:   1.28  $
 * $Modtime:   Jul 29 2005 15:10:12  $
 * $Log:   //mtkvs01/vmdata/Maui_sw/archives/mcu/kal/Efs/include/fs_func.h-arc  $
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 08 09 2010 stanley.chu
 * [MAUI_02562433] [System Service][File System][Debug] Transform FS Trace from SYS Trace mechanism to Primitive Trace (kal_trace)
 * FS Trace MAUI
 *
 * 08 04 2010 stanley.chu
 * [MAUI_02602345] [System Service][File System][New Feature] Smart File Seeker (FS_SetSeekHintEx)
 * SmartSeeker
 *
 * Jun 30 2010 mtk02187
 * [MAUI_02573987] [System Service][File System][Change Feature] Add FS_GetCurrentDirByDrive API
 * 
 *
 * May 25 2010 mtk02187
 * [MAUI_02534699] [System Service][File System][New Feature] New API: FS_GetFileInfoEx()
 * 
 *
 * Mar 31 2010 mtk02187
 * [MAUI_02165253] [Video recorder] the recorded video will save failed for a 4 hour video
 * Roll-back FS_Seek(), plan to check-in new FS_Seek in W10.17
 *
 * Mar 30 2010 mtk02187
 * [MAUI_02165253] [Video recorder] the recorded video will save failed for a 4 hour video
 * 
 *
 * Mar 8 2010 mtk02187
 * [MAUI_02365788] [System Service][File System] Modify definition for FS APIs if __SMART_PHONE_MODEM__
 * 
 *
 * Mar 4 2010 mtk02187
 * [MAUI_02365916] [Lipton35][X-download] provide PSN query function in T-Flash driver
 * 
 *
 * Mar 2 2010 mtk02187
 * [MAUI_02365788] [System Service][File System] Modify definition for FS APIs if __SMART_PHONE_MODEM__
 * 
 *
 * Jul 7 2009 mtk02187
 * [MAUI_01716012] [System Service][File System][New Feature] Add FS_CompactDir()
 * 
 *
 * Apr 20 2009 mtk02187
 * [MAUI_01669677] [System Service][File System][New Feature] MTP
 * 
 *
 * Mar 20 2009 mtk02187
 * [MAUI_01652034] [System Service][File System][New Feature] Add new API: FS_IOCtrl
 * 
 *
 * Oct 30 2008 mtk01892
 * [MAUI_01265151] [System Service][File System][Change Feature] Export file name and file type compari
 * export name and type comparision functions of fs sort 
 *
 * Aug 7 2008 MTK01892
 * [MAUI_00818773] [System Service][File System][Change Feature] Remove FS_MOVE_SUPPORT compiler option
 * unwarp FS_MOVE_SUPPORT
 *
 * Aug 5 2008 MTK01892
 * [MAUI_00816267] [System Service][File System][Change Feature] Prevent MoDIS divide by zero when coun
 * Add new API - FS_SetVolumeLabel
 *
 * Jul 9 2008 MTK01892
 * [MAUI_00800960] [System Service][Nwe Feature] FS Sorting Enhancement Phase In
 * FS Sorting Enhancement Phase In
 *
 * Apr 26 2008 MTK01892
 * [MAUI_00762588] [File System] Enhance FS_OpenHint
 * enhance the error handling
 *
 * Apr 8 2008 MTK01892
 * [MAUI_00751714] [System Service][Add Feauter] migrate TK6516 DVT to main trunk
 * migrate tk6516 to main trunk
 *
 * Sep 1 2007 mtk01077
 * [MAUI_00541094] [FS] New API FS_RecoverDevice()
 * 
 *
 * Mar 16 2007 mtk01500
 * [MAUI_00421824] [File Manager] Insert whitespaces to the end of folder name, display differently fro
 * 
 *
 * Mar 15 2007 mtk01500
 * [MAUI_00421824] [File Manager] Insert whitespaces to the end of folder name, display differently fro
 * 
 *
 * Oct 1 2006 mtk01077
 * [MAUI_00334147] [FS] Add 2 New APIs (FS_CountUsedFH, FS_GetDevPartitions) and Vanish 1 API (FS_GetCh
 * 
 *
 * Aug 8 2006 mtk01077
 * [MAUI_00210947] [New Feature][FDM & MTD] Support OTP function in FDM and MTD Layer
 * 
 *
 * Jul 27 2006 mtk01077
 * [MAUI_00223650] Slide show_All the image have empty name and can't show in the screen
 * 
 *
 * Jul 15 2006 mtk01077
 * [MAUI_00210765] [New Feature][FDM] NOR FDM5
 * 
 *
 * Jul 14 2006 mtk01077
 * [MAUI_00210408] [FS] New FS_XXX APIs for Flash OTP Access
 * 
 *
 * Jul 10 2006 mtk01077
 * [MAUI_00209443] [FS] New Improved Recursive Algorithm
 * 
 *
 * Jul 3 2006 mtk01077
 * [MAUI_00207335] [FS] New Mechanism, File System APIs  FS_SetDirCache() FS_GetDirCache()
 * 
 *
 * Mar 21 2006 mtk01077
 * [MAUI_00180993] [FS] New Algorithm apply on FS_CheckDrive() to be adaptive with disk size and buffer
 * 
 *
 * Jan 25 2006 mtk01077
 * [MAUI_00170740] [FS] USB OTG Support
 * 
 *
 * Dec 4 2005 mtk01077
 * [MAUI_00159707] [EFS] Remove SYSTEM_DRIVE_ON_NAND and NAND_FLASH_BOOTING option dependency
 * 
 *
 * Sep 30 2005 mtk01077
 * [MAUI_00126551] [FS][Add Feature] File System Support Virtual File (aka File Map File) for Content Packaged File Processing
 * 
 * 
 *    Rev 1.28   Jul 29 2005 15:10:52   mtk00681
 * Support system drive on NAND
 * Resolution for 12103: [FS][Enhance] Support system drive on NAND (with NOR)
 * 
 *    Rev 1.27   Jul 07 2005 10:51:30   mtk00681
 * Modify FS_LockFAT for single bank NOR design
 * Resolution for 11851: [FS][AddFeature]Support single bank NOR design
 * 
 *    Rev 1.26   Jul 05 2005 22:02:36   mtk00681
 * Add new API: FS_GetFirstCluster
 * Resolution for 11832: [FS][AddFeature]Add new API: FS_GetFirstCluster
 * 
 *    Rev 1.25   Jun 23 2005 18:42:06   mtk00681
 * Modify FS_MakeFileName
 * Resolution for 11657: [FS][Enhance]Modify FS_MakeFileName to support SFN/LFN look up
 * 
 *    Rev 1.24   Jun 02 2005 17:16:58   mtk00681
 * Add sanity check for NFB
 * Resolution for 11336: [FS][Enhance]Modify sanity check and GetDrive for NFB support
 * 
 *    Rev 1.23   May 17 2005 00:32:56   BM_Trunk
 * Karlos:
 * add copyright and disclaimer statement
 * 
 *    Rev 1.22   May 12 2005 13:35:08   mtk00681
 * Add proprietary copyright protection mechanism
 * Resolution for 11028: [FS][AddFeature]Add proprietary copyright protection mechanism
 * 
 *    Rev 1.21   Mar 18 2005 20:56:00   mtk00681
 * Remove __FS_CARD_SUPPORT__ restriction
 * Resolution for 10302: [FS][BugFix] Remove __FS_CARD_SUPPORT__ restriction
 *
 *    Rev 1.20   Jan 18 2005 00:37:04   BM
 * append new line in W05.04
 *
 *    Rev 1.19   Nov 03 2004 17:36:52   mtk00681
 * Remove RVCT warning
 * Resolution for 8571: [FS][BugFix]Remove RVCT warning
 *
 *    Rev 1.18   Oct 25 2004 17:00:48   mtk00681
 * Add a new API, FS_LockFAT, for NVRAM to lock FS in USB mode
 * Resolution for 8390: [FS][AddFeature]Provide API for NVRAM to lock FS in USB mode
 *
 *    Rev 1.17   Oct 01 2004 22:43:18   mtk00681
 * Add one more parameter, Flag, on FS_GetDevStatus
 * Resolution for 8003: [FS][Enhance]Provide disk mount state query
 *
 *    Rev 1.16   Sep 13 2004 09:35:32   mtk00681
 * Remove FS_SetTrace ifndef __FS_TRACE_SUPPORT__
 * Resolution for 7438: [FS][Enhancement]Optimize and customize
 *
 *    Rev 1.15   Sep 02 2004 15:57:24   mtk00681
 * Add FS_GetFilePosition & modify FS_SetSeekHint
 * Resolution for 7458: [FS][Add Feature]Add/modify API for MMTF
 *
 *    Rev 1.14   Aug 31 2004 19:11:14   mtk00681
 * Optimize and customize for low cost
 * Resolution for 7438: [FS][Enhancement]Optimize and customize
 *
 *    Rev 1.13   Aug 24 2004 16:16:26   mtk00681
 * Add abortion for check drive
 * Resolution for 7266: [FS][AddFeature]Add sanity check for NAND flash
 *
 *    Rev 1.12   Aug 23 2004 13:12:06   mtk00681
 * Add for check drive function
 * Resolution for 7266: [FS][AddFeature]Add sanity check for NAND flash
 *
 *    Rev 1.11   Aug 20 2004 14:36:02   mtk00681
 * Add FS_GetClusterSize API
 * Resolution for 7227: [FS][AddFeature]Add Quota Management
 *
 *    Rev 1.10   Aug 20 2004 12:04:52   mtk00681
 * Add sanity check for NAND
 * Resolution for 7266: [FS][AddFeature]Add sanity check for NAND flash
 *
 *    Rev 1.9   Aug 17 2004 18:04:22   mtk00681
 * Add Quota Management
 * Resolution for 7227: [FS][AddFeature]Add Quota Management
 *
 *    Rev 1.8   Jul 30 2004 18:13:44   mtk00681
 * Add FS_ParseFH to parse file handle
 * Resolution for 6929: [FS][AddFeature]Add API: FS_ParseFH
 *
 *    Rev 1.7   Jul 06 2004 19:23:16   mtk00681
 * Add file check for MMTF
 * Resolution for 6523: [FS][AddFeature]Add file check function for MMTF
 *
 *    Rev 1.6   Jun 18 2004 10:56:44   mtk00681
 * Unify DWORD to UINT
 *
 *    Rev 1.5   Jun 14 2004 11:07:24   mtk00681
 * Add trace mechanism
 * Resolution for 6084: [FS][AddFeature]Add FS trace mechanism
 *
 *    Rev 1.4   May 27 2004 09:02:32   mtk00681
 * Add choice flag for FIND series.
 * Resolution for 5556: [FS][Enhance]Support for encoding display issue
 *
 *    Rev 1.3   May 19 2004 18:22:30   mtk00681
 * Speedup seek function
 * Resolution for 5517: [FS][AddFeature]Speedup seek function
 *
 *    Rev 1.2   May 14 2004 12:25:32   mtk00681
 * Separate MTK developed function (II)
 * Resolution for 5317: [FS][AddFeature]Wrap File System
 *
 *    Rev 1.1   May 13 2004 23:20:16   mtk00681
 * Separate MTK developed function
 * Resolution for 5317: [FS][AddFeature]Wrap File System
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _FS_TYPE_H
#error "Please include fs_type.h first..."
#endif

#ifndef _FS_FUNC_H
#define _FS_FUNC_H

#if defined(__SMART_PHONE_MODEM__) && defined(__MTK_TARGET__)

#include "fs_ccci.h"

#define MD_FS_API(n, ...) MD##_##n(__VA_ARGS__)

// General I/O
#define FS_Open(...)					MD_FS_API(FS_Open, __VA_ARGS__)
#define FS_OpenHint(...)				MD_FS_API(FS_OpenHint, __VA_ARGS__)
#define FS_Close(...)					MD_FS_API(FS_Close, __VA_ARGS__)
#define FS_CloseAll(...)				MD_FS_API(FS_CloseAll, __VA_ARGS__)
#define FS_Read(...)					MD_FS_API(FS_Read, __VA_ARGS__)
#define FS_Write(...)					MD_FS_API(FS_Write, __VA_ARGS__)
#define FS_EnableSmartSeek(...)     -255
#define FS_SetSeekHint(...)			-255//MD_FS_API(FS_SetSeekHint, __VA_ARGS__)
#define FS_SetSeekHintEx(...)			-255//MD_FS_API(FS_SetSeekHint, __VA_ARGS__)
#define FS_Seek(...)					MD_FS_API(FS_Seek, __VA_ARGS__)
#define FS_Commit(...)				-255//MD_FS_API(FS_Commit, __VA_ARGS__)
#define FS_ReleaseFH(...)				-255//MD_FS_API(FS_ReleaseFH, __VA_ARGS__)
#define FS_Abort(...)					-255//MD_FS_API(FS_Abort, __VA_ARGS__)
#define FS_ParseFH(...)				-255//MD_FS_API(FS_ParseFH, __VA_ARGS__)
#define FS_GenVirtualFileName(...)		-255//MD_FS_API(FS_GenVirtualFileName, __VA_ARGS__)

//Information
#define FS_GetFileInfo(...)				-255//MD_FS_API(FS_GetFileInfo, __VA_ARGS__)
#define FS_GetFileInfoEx(...)				-255//MD_FS_API(FS_GetFileInfoEx, __VA_ARGS__)
#define FS_GetFileSize(...)				MD_FS_API(FS_GetFileSize, __VA_ARGS__)
#define FS_GetFilePosition(...)			-255//MD_FS_API(FS_GetFilePosition, __VA_ARGS__)
#define FS_SetFileTime(...)			-255//MD_FS_API(FS_SetFileTime, __VA_ARGS__)
#define FS_GetAttributes(...)			MD_FS_API(FS_GetAttributes, __VA_ARGS__) // Meta tool need this
#define FS_GetFirstCluster(...)			-255//MD_FS_API(FS_GetFirstCluster, __VA_ARGS__)

//File Only Operation
#define FS_SetAttributes(...)			-255//MD_FS_API(FS_SetAttributes, __VA_ARGS__)
#define FS_Delete(...)					MD_FS_API(FS_Delete, __VA_ARGS__)
#define FS_PseudoMerge(...)			-255//MD_FS_API(FS_PseudoMerge, __VA_ARGS__)
#define FS_CheckFile(...)				-255//MD_FS_API(FS_CheckFile, __VA_ARGS__)

//Folder Only Operation
#define FS_GetCurrentDir(...)			-255//MD_FS_API(FS_GetCurrentDir, __VA_ARGS__)
#define FS_GetCurrentDirByDrive(...) -255//MD_FS_API(FS_GetCurrentDirByDrive, __VA_ARGS__)
#define FS_SetCurrentDir(...)			-255//MD_FS_API(FS_SetCurrentDir, __VA_ARGS__)
#define FS_CreateDir(...)				MD_FS_API(FS_CreateDir, __VA_ARGS__)
#define FS_RemoveDir(...)				MD_FS_API(FS_RemoveDir, __VA_ARGS__)
#define FS_GetFolderSize(...)			MD_FS_API(FS_GetFolderSize, __VA_ARGS__)

//File and Folder Operations
#define FS_Extend(...)					-255//MD_FS_API(FS_Extend, __VA_ARGS__)
#define FS_Truncate(...)				-255//MD_FS_API(FS_Truncate, __VA_ARGS__)
#define FS_MakeFileName(...)			-255//MD_FS_API(FS_MakeFileName, __VA_ARGS__)
#define FS_Rename(...)				MD_FS_API(FS_Rename, __VA_ARGS__)

#define FS_Move(...)					MD_FS_API(FS_Move, __VA_ARGS__)

#define FS_Count(...)					MD_FS_API(FS_Count, __VA_ARGS__)
#define FS_XDelete(...)				MD_FS_API(FS_XDelete, __VA_ARGS__)
#define FS_XDeleteEx(...)				-255//MD_FS_API(FS_XDeleteEx, __VA_ARGS__)

#define FS_CompactDir(...)				-255//MD_FS_API(FS_XDeleteEx, __VA_ARGS__)

//Find File
#define FS_FindFirst(...)				MD_FS_API(FS_FindFirst, __VA_ARGS__)
#define FS_FindNext(...)				MD_FS_API(FS_FindNext, __VA_ARGS__)
#define FS_FindFirstN(...)				-255//MD_FS_API(FS_FindFirstN, __VA_ARGS__)
#define FS_FindNextN(...)				-255//MD_FS_API(FS_FindNextN, __VA_ARGS__)
#define FS_FindClose(...)				MD_FS_API(FS_FindClose, __VA_ARGS__)

#ifdef __FS_SORT_SUPPORT__
#define FS_SortCompareFileName(...)     -255
#define FS_SortCompareFileType(...)     -255
#define FS_XFindReset(...)				-255//MD_FS_API(FS_XFindReset, __VA_ARGS__)
#define FS_XFindStart(...)				-255//MD_FS_API(FS_XFindStart, __VA_ARGS__)
#define FS_XFindClose(...)				-255//MD_FS_API(FS_XFindClose, __VA_ARGS__)
#endif

//Drive Management
#define FS_GetDrive(...)				MD_FS_API(FS_GetDrive, __VA_ARGS__)
#define FS_GeneralFormat(...)			-255//MD_FS_API(FS_GeneralFormat, __VA_ARGS__)
#define FS_GetClusterSize(...)			MD_FS_API(FS_GetClusterSize, __VA_ARGS__)
#ifdef __FS_QM_SUPPORT__
#define FS_QmGetFree(...)				-255//MD_FS_API(FS_QmGetFree, __VA_ARGS__)
#endif
#define FS_CreateBootSector(...)		0//MD_FS_API(FS_CreateBootSector, __VA_ARGS__)
#define FS_GetPartitionInfo(...)		-255//MD_FS_API(FS_GetPartitionInfo, __VA_ARGS__)
#define FS_CommitAll(...)				-255//MD_FS_API(FS_CommitAll, __VA_ARGS__)
#define FS_MappingDrive(...)			-255//MD_FS_API(FS_MappingDrive, __VA_ARGS__)

//Power Lost Detection and Recovery
#define FS_SetDiskFlag(...)			MD_FS_API(FS_SetDiskFlag, __VA_ARGS__)
#define FS_CheckDiskFlag(...)			-255//MD_FS_API(FS_CheckDiskFlag, __VA_ARGS__)
#define FS_ClearDiskFlag(...)			MD_FS_API(FS_ClearDiskFlag, __VA_ARGS__)
#ifdef __FS_CHECKDRIVE_SUPPORT__
#define FS_CheckDrive(...)			-255//MD_FS_API(FS_CheckDrive, __VA_ARGS__)
#endif

//Disk Management
#define FS_CreateMasterBootRecord(...)	0//MD_FS_API(FS_CreateMasterBootRecord, __VA_ARGS__)
#define FS_SplitPartition(...)				0//MD_FS_API(FS_SplitPartition, __VA_ARGS__)
#define FS_GetDiskInfo(...)				MD_FS_API(FS_GetDiskInfo, __VA_ARGS__)
#define FS_GetDevType(...)				-255//MD_FS_API(FS_GetDevType, __VA_ARGS__)
#define FS_GetDevStatus(...)				-255//MD_FS_API(FS_GetDevStatus, __VA_ARGS__)
#define FS_GetDevPartitions(...)			-255//MD_FS_API(FS_GetDevPartitions, __VA_ARGS__)

//Card management
#define FS_TestMSDC(...)				-255//MD_FS_API(FS_TestMSDC, __VA_ARGS__)
#define FS_CloseMSDC(...)				-255//MD_FS_API(FS_CloseMSDC, __VA_ARGS__)

//OTG Card Reader Management
#define FS_ConfigExtDevice(...)		-255//MD_FS_API(FS_ConfigExtDevice, __VA_ARGS__)

//File System Run-Time LifeCycle
#define FS_ShutDown(...)				MD_FS_API(FS_ShutDown, __VA_ARGS__)
#define FS_UnlockAll(...)				MD_FS_API(FS_UnlockAll, __VA_ARGS__)
#define FS_SanityCheck(...)			-255//MD_FS_API(FS_SanityCheck, __VA_ARGS__)
#define FS_LockFAT(...)				MD_FS_API(FS_LockFAT, __VA_ARGS__)

//File System Run-Time Debug
#define FS_CountUsedFH(...)			-255//MD_FS_API(FS_CountUsedFH, __VA_ARGS__)
#ifdef __FS_TRACE_SUPPORT__
#define FS_SetTrace(...)				-255//MD_FS_API(FS_SetTrace, __VA_ARGS__)
#define FS_DumpFHTable(...)			-255//MD_FS_API(FS_DumpFHTable, __VA_ARGS__)
#endif

//DirCache
#define FS_SetDirCache(...)			-255//MD_FS_API(FS_SetDirCache, __VA_ARGS__)
#define FS_GetDirCache(...)			-255//MD_FS_API(FS_GetDirCache, __VA_ARGS__)
#define FS_ExpireDirCache(...)			-255//MD_FS_API(FS_ExpireDirCache, __VA_ARGS__)

//Flash Device Direct IO
#define FS_OTPWrite(...)				-255//MD_FS_API(FS_OTPWrite, __VA_ARGS__)
#define FS_OTPRead(...)				-255//MD_FS_API(FS_OTPRead, __VA_ARGS__)
#define FS_OTPQueryLength(...)		-255//MD_FS_API(FS_OTPQueryLength, __VA_ARGS__)
#define FS_OTPLock(...)				-255//MD_FS_API(FS_OTPLock, __VA_ARGS__)
#define FS_IOCtrl(...)              -255//MD_FS_API(FS_IOCtrl, __VA_ARGS__)

//Flash Sweep
#define FS_SweepDevice(...)			-255//MD_FS_API(FS_SweepDevice, __VA_ARGS__)

#if defined(__MTP_ENABLE__)
#define FS_MakeFilePathByHint(...)  -255//MD_FS_API(FS_MakeFilePathByHint, __VA_ARGS__)
#define FS_OpenFileByHint(...)      -255//MD_FS_API(FS_OpenFileByHint, __VA_ARGS__)
#define FS_DeleteByHint(...)        -255//MD_FS_API(FS_DeleteByHint, __VA_ARGS__)
#define FS_GetAttributesByHint(...) -255//MD_FS_API(FS_GetAttributesByHint, __VA_ARGS__)
#define FS_SetAttributesByHint(...) -255//MD_FS_API(FS_SetAttributesByHint, __VA_ARGS__)
#define FS_HintGetParent(...)       -255//MD_FS_API(FS_HintGetParent, __VA_ARGS__)
#endif // __MTP_ENABLE__

#else /* !__SMART_PHONE_MODEM__ || !__MTK_TARGET__ */
// General I/O
extern int FS_Open(const WCHAR * FileName, UINT Flag);
extern int FS_OpenHint(const WCHAR * FileName, UINT Flag, FS_FileOpenHint * DSR_Hint);
extern int FS_Close(FS_HANDLE FileHandle);
extern int FS_CloseAll(void);
extern int FS_Read(FS_HANDLE FileHandle, void * DataPtr, UINT Length, UINT * Read);
extern int FS_Write(FS_HANDLE FileHandle, void * DataPtr, UINT Length, UINT * Written);
extern int FS_SetSeekHint(FS_HANDLE FileHandle, UINT HintNum, FS_FileLocationHint * Hint);
extern int FS_SetSeekHintEx(FS_HANDLE FileHandle, UINT HintCount, UINT Flag, void *Buffer, UINT BufferSize);
extern void FS_EnableSmartSeek(kal_bool Option);
extern int FS_Seek(FS_HANDLE FileHandle, int Offset, int Whence);
extern int FS_Commit(FS_HANDLE FileHandle);
extern int FS_ReleaseFH(void * TaskId);
extern int FS_Abort(UINT ActionHandle);
extern int FS_ParseFH(FS_HANDLE FileHandle);
extern int FS_GenVirtualFileName(FS_HANDLE FileHandle, WCHAR * VFileNameBuf, UINT BufLength, UINT VFBeginOffset, UINT VFValidLength);

//Information
extern int FS_GetFileInfo(FS_HANDLE FileHandle, FS_FileInfo * FileInfo);
extern int FS_GetFileInfoEx(FS_HANDLE FileHandle, FS_FileInfo *FileInfo, UINT Flags);
extern int FS_GetFileSize(FS_HANDLE FileHandle, UINT * Size);
extern int FS_GetFilePosition(FS_HANDLE FileHandle, UINT * Position);
extern int FS_SetFileTime(FS_HANDLE FileHandle, const FS_DOSDateTime * Time);
extern int FS_GetAttributes(const WCHAR * FileName);
extern int FS_GetFirstCluster(FS_HANDLE FileHandle, UINT * Cluster);

//File Only Operation
extern int FS_SetAttributes(const WCHAR * FileName, BYTE Attributes);
extern int FS_Delete(const WCHAR * FileName);
extern int FS_PseudoMerge(const WCHAR * FileName1, const WCHAR * FileName2);
extern int FS_CheckFile(const WCHAR * FileName);

//Folder Only Operation
extern int FS_GetCurrentDir(WCHAR * DirName, UINT MaxLength);
extern int FS_GetCurrentDirByDrive(WCHAR * DirName, UINT MaxLength);
extern int FS_SetCurrentDir(const WCHAR * DirName);
extern int FS_CreateDir(const WCHAR * DirName);
extern int FS_RemoveDir(const WCHAR * DirName);
extern int FS_GetFolderSize(const WCHAR *DirName, UINT Flag, BYTE *RecursiveStack, const UINT StackSize);

//File and Folder Operations
extern int FS_Extend(FS_HANDLE FileHandle, UINT Length);
extern int FS_Truncate(FS_HANDLE FileHandle);
extern int FS_MakeFileName(FS_HANDLE FileHandle, UINT Flag, WCHAR * FileName, UINT MaxLength);
extern int FS_Rename(const WCHAR * FileName, const WCHAR * NewName);

extern int FS_Move(const WCHAR * SrcFullPath, const WCHAR * DstFullPath, UINT Flag, FS_ProgressCallback Progress, BYTE *RecursiveStack, const UINT StackSize);

extern int FS_Count(const WCHAR * FullPath, UINT Flag, BYTE *RecursiveStack, const UINT StackSize);
extern int FS_XDelete(const WCHAR * FullPath, UINT Flag, BYTE *RecursiveStack, const UINT StackSize);
extern int FS_XDeleteEx(const WCHAR * FullPath, UINT Flag, FS_ProgressCallback Progress, BYTE *RecursiveStack, const UINT StackSize);

extern int FS_CompactDir(const WCHAR* DirName, UINT Flags);

//Find File
extern int FS_FindFirst(const WCHAR * NamePattern, BYTE Attr, BYTE AttrMask, FS_DOSDirEntry * FileInfo, WCHAR * FileName, UINT MaxLength);
extern int FS_FindNext(FS_HANDLE FileHandle, FS_DOSDirEntry * FileInfo, WCHAR * FileName, UINT MaxLength);
extern int FS_FindFirstN(const WCHAR * NamePattern, FS_Pattern_Struct * PatternArray, UINT PatternNum, BYTE ArrayMask, BYTE Attr, BYTE AttrMask, FS_DOSDirEntry * FileInfo, WCHAR * FileName, UINT MaxLength, UINT EntryIndex, UINT Flag);
extern int FS_FindNextN(FS_HANDLE Handle, FS_Pattern_Struct * PatternArray, UINT PatternNum, BYTE ArrayMask, FS_DOSDirEntry * FileInfo, WCHAR * FileName, UINT MaxLength, UINT Flag);
extern int FS_FindClose(FS_HANDLE FileHandle);

#ifdef __FS_SORT_SUPPORT__
extern int FS_SortCompareFileName(WCHAR *FileName1, WCHAR *FileName2);
extern int FS_SortCompareFileType(WCHAR *FileName1, WCHAR *FileName2);
extern int FS_XFindReset(FS_SortingParam *Param);
extern int FS_XFindStart(const WCHAR * Pattern, FS_DOSDirEntry * FileInfo, WCHAR * FileName, UINT MaxLength, UINT Index, UINT * Position, UINT Flag);
extern int FS_XFindClose(UINT * Position);
#endif

//Drive Management
extern int FS_GetDrive(UINT Type, UINT Serial, UINT AltMask);
extern int FS_GeneralFormat(const WCHAR * DriveName, UINT Level, FS_FormatCallback Progress);
extern int FS_GetClusterSize(UINT DriveIdx);
#ifdef __FS_QM_SUPPORT__
extern int FS_QmGetFree(const BYTE * Path);
#endif
extern int FS_CreateBootSector(void * BootSector, const FS_PartitionRecord * Partition, BYTE MediaDescriptor, UINT MinSectorsPerCluster, UINT Flags);
extern int FS_GetPartitionInfo(const WCHAR * DriveName, FS_PartitionRecord * PartitionInfo);
extern int FS_CommitAll(const WCHAR * DriveName);
extern int FS_MappingDrive(UINT UpperOldDrv, UINT UpperNewDrv);
extern int FS_SetVolumeLabel(const WCHAR * DriveName, const WCHAR * Label);
extern int FS_SwitchDriveMode(UINT DriveIdx, UINT Mode);

//Power Lost Detection and Recovery
extern int FS_SetDiskFlag(void);
extern int FS_CheckDiskFlag(void);
extern int FS_ClearDiskFlag(void);
#ifdef __FS_CHECKDRIVE_SUPPORT__
extern int FS_CheckDrive(const UINT DriveIdx, BYTE * CheckBuffer, const UINT CheckBufferSize);
#endif

//Disk Management
extern int FS_CreateMasterBootRecord(void * SectorBuffer, const FS_PartitionRecord * DiskGeometry);
extern int FS_SplitPartition(void * MasterBootRecord, UINT Sectors);
extern int FS_GetDiskInfo(const WCHAR * DriveName, FS_DiskInfo * DiskInfo, int Flags);
extern int FS_GetDevType(const WCHAR * Path);
extern int FS_GetDevStatus(UINT DriveIdx, UINT Flag);
extern int FS_GetDevPartitions(UINT QueryTarget);

//Card management
int FS_TestMSDC(void * slot_id, BYTE * drive_list, UINT * drive_num);
extern int FS_CloseMSDC(UINT MSDCIndex, UINT Mode);

//OTG Card Reader Management
extern int FS_ConfigExtDevice(int Action, FS_Driver *DrvFuncs, int Slots, void **SlotIdArray, int *SlotsConfiged);

//File System Run-Time LifeCycle
extern void FS_ShutDown(void);
extern int FS_UnlockAll(void);
extern int FS_SanityCheck(void);
extern int FS_LockFAT(UINT Type);

//File System Run-Time Debug
extern int FS_CountUsedFH(int flag);
#ifdef __FS_TRACE_SUPPORT__
extern int FS_DumpFHTable(void);
#endif
extern int FS_SetTrace(UINT Flag, UINT Timeout);

//Copyright
#ifdef __P_PROPRIETARY_COPYRIGHT__
extern int FS_SweepCopyrightFile(void);
extern int FS_GetCopyrightList(void);
extern int FS_GetCopyrightConfig(UINT *Protect, UINT *NonProtect, const WCHAR **Folder,
                          const FS_Pattern_Struct **Pattern, UINT **List1, UINT **List2);
#endif

//DirCache
extern int FS_SetDirCache(FS_HANDLE FileHandle, UINT NewValue);
extern int FS_GetDirCache(FS_HANDLE FileHandle, UINT *CurrValue);
extern int FS_ExpireDirCache(FS_HANDLE FileHandle);

//Flash Device Direct IO
extern int FS_OTPWrite(int devtype, UINT Offset, void * BufferPtr, kal_uint32 Length);
extern int FS_OTPRead(int devtype, UINT Offset, void * BufferPtr, kal_uint32 Length);
extern int FS_OTPQueryLength(int devtype, UINT *Length);
extern int FS_OTPLock(int devtype);
extern int FS_IOCtrl(const WCHAR* DriveName, UINT CtrlAction, void* CtrlData);

//Flash Sweep or Recover
extern int FS_SweepDevice(const int devtype);
extern int FS_RecoverDevice(const int devtype);

#if defined(__MTP_ENABLE__)
extern int FS_MakeFilePathByHint(WCHAR DriveLetter, UINT DirCluster, UINT DirIndex, WCHAR * FileName, UINT MaxLength);
extern int FS_OpenFileByHint(WCHAR DriveLetter, UINT DirCluster, UINT DirIndex, UINT Flags);
extern int FS_DeleteByHint(WCHAR DriveLetter, UINT DirCluster, UINT DirIndex);
extern int FS_GetAttributesByHint(WCHAR DriveLetter, UINT DirCluster, UINT DirIndex);
extern int FS_SetAttributesByHint(WCHAR DriveLetter, UINT DirCluster, UINT DirIndex, BYTE Attributes);
extern int FS_HintGetParent(WCHAR DriveLetter, UINT DirCluster, UINT DirIndex, UINT *ParentCluster, UINT *ParentIndex);
#endif /* __MTP_ENABLE__ */

#endif /* __SMART_PHONE_MODEM__ && __MTK_TARGET__ */

#endif //_FS_FUNC_H

