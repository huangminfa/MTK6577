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

/*******************************************************************************
 * Filename:
 * ---------
 *   fs_trc.h
 *
 * Project:
 * --------
 *   MAUI
 *
 * Description:
 * ------------
 *   This is trace map definition for FS.
 *
 * Author:
 * -------
 *   Stanley Chu
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision$
 * $Modtime$
 * $Log$
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * 08 11 2010 stanley.chu
 * [MAUI_02562433] [System Service][File System][Debug] Transform FS Trace from SYS Trace mechanism to Primitive Trace (kal_trace)
 * Trace MAUI
 *
 * 08 09 2010 stanley.chu
 * [MAUI_02562433] [System Service][File System][Debug] Transform FS Trace from SYS Trace mechanism to Primitive Trace (kal_trace)
 * FS Trace MAUI
 *
 * Jun 15 2010 mtk02187
 * [MAUI_02562433] [System Service][File System][Debug] Transform FS Trace from SYS Trace mechanism to
 * 
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef _FS_TRC_H
#define _FS_TRC_H

#ifndef GEN_FOR_PC

   #ifndef _STACK_CONFIG_H
   #error "stack_config.h should be included before tst_config.h"
   #endif

#else
   #include "kal_trace.h"
#endif /* GEN_FOR_PC */

#ifndef _KAL_TRACE_H
   #error "kal_trace.h should be included before tst_trace.h"
#endif

BEGIN_TRACE_MAP(MOD_FS)

   /* FS APIs */
   TRC_MSG(FS_API_FS_OPEN_S,    "[FS API - %Mmodule_type] FS_Open -> Flag: 0x%X, FileName:")
   TRC_MSG(FS_API_FS_OPEN_E,    "[FS API - %Mmodule_type] FS_Open <- Return: 0x%X")
   TRC_MSG(FS_API_FS_CLOSE_S,   "[FS API - %Mmodule_type] FS_Close -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_CLOSE_E,   "[FS API - %Mmodule_type] FS_Close -> Return: %d, Duration: %d us")
   TRC_MSG(FS_API_FS_READ_S,    "[FS API - %Mmodule_type] FS_Read -> FileHandle: 0x%X, DataPtr: 0x%X, Length: %u, File(SFN): %c%c%c%c%c%c%c%c.%c%c%c, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_API_FS_READ_E,    "[FS API - %Mmodule_type] FS_Read <- Return: %d, Length: %u, Throughput: %u KB/s, Duration: %u us")
   TRC_MSG(FS_API_FS_WRITE_S,   "[FS API - %Mmodule_type] FS_Write -> FileHandle: 0x%X, DataPtr: 0x%X, Length: %d, File(SFN): %c%c%c%c%c%c%c%c.%c%c%c, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_API_FS_WRITE_E,   "[FS API - %Mmodule_type] FS_Write <- Return: %d, Length: %d, Throughput: %d KB/s, Duration: %d us")
   TRC_MSG(FS_API_FS_SEEK_S,    "[FS API - %Mmodule_type] FS_Seek -> FileHandle: 0x%X, Offset: %d, Whence: %MFS_SEEK_POS_ENUM")
   TRC_MSG(FS_API_FS_SEEK_E,    "[FS API - %Mmodule_type] FS_Seek <- Return: %d, Duration: %d us")
   TRC_MSG(FS_API_FS_COMMIT_S,  "[FS API - %Mmodule_type] FS_Commit -> FileHandle: 0x%X, File(SFN): %c%c%c%c%c%c%c%c.%c%c%c, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_API_FS_COMMIT_E,  "[FS API - %Mmodule_type] FS_Commit <- Return: %d")
   TRC_MSG(FS_API_FS_GETFILESIZE_S, "[FS API - %Mmodule_type] FS_GetFileSize -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_GETFILESIZE_E, "[FS API - %Mmodule_type] FS_GetFileSize <- Return (Size): %d")
   TRC_MSG(FS_API_FS_DELETEEX_S,    "[FS API - %Mmodule_type] FS_DeleteEx ->")
   TRC_MSG(FS_API_FS_DELETEEX_E,    "[FS API - %Mmodule_type] FS_DeleteEx <- Return: %d")
   TRC_MSG(FS_API_FS_DELETE_S,      "[FS API - %Mmodule_type] FS_Delete ->")
   TRC_MSG(FS_API_FS_DELETE_E,      "[FS API - %Mmodule_type] FS_Delete <- Return: %d")
   TRC_MSG(FS_API_FS_CREATEDIR_S,   "[FS API - %Mmodule_type] FS_CreateDir ->")
   TRC_MSG(FS_API_FS_CREATEDIR_E,   "[FS API - %Mmodule_type] FS_CreateDir <- Return: %d")
   TRC_MSG(FS_API_FS_GETCLUSTERSIZE_S,  "[FS API - %Mmodule_type] FS_GetClusterSize -> Drive Index: %d")
   TRC_MSG(FS_API_FS_GETCLUSTERSIZE_E,  "[FS API - %Mmodule_type] FS_GetClusterSize <- Return (Size): %d")
   TRC_MSG(FS_API_FS_SHUTDOWN_S,        "[FS API - %Mmodule_type] FS_ShutDown")
   TRC_MSG(FS_API_FS_CLEARDISKFLAG_S,   "[FS API - %Mmodule_type] FS_ClearDiskFlag")
   TRC_MSG(FS_API_FS_UNLOCKALL_S,       "[FS API - %Mmodule_type] FS_UnlockAll")
   TRC_MSG(FS_API_FS_GETDISKINFO_S,     "[FS API - %Mmodule_type] FS_GetDiskInfo -> DriveName: %c, Flag: 0x%X")
   TRC_MSG(FS_API_FS_GETDISKINFO_E,     "[FS API - %Mmodule_type] FS_GetDiskInfo <- Return: %d")
   TRC_MSG(FS_API_FS_GETDISKINFO_E_DEV, "[FS API - %Mmodule_type] FS_GetDiskInfo <- Return: %d, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_API_FS_OPENHINT_S,        "[FS API - %Mmodule_type] FS_OpenHint -> Flag: 0x%X, FileName:")
   TRC_MSG(FS_API_FS_OPENHINT_S_HINT,   "[FS API - %Mmodule_type] FS_OpenHint -> Flag: 0x%X, Cluster: %d, Index: %d, FileName:")
   TRC_MSG(FS_API_FS_OPENHINT_E,        "[FS API - %Mmodule_type] FS_OpenHint <- Return: 0x%X")
   TRC_MSG(FS_API_FS_FINDFIRST_S,       "[FS API - %Mmodule_type] FS_FindFirst -> Attr: 0x%X, AttrMask: 0x%X, MaxLength: %d")
   TRC_MSG(FS_API_FS_FINDFIRST_E,       "[FS API - %Mmodule_type] FS_FindFirst <- FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_FINDFIRST_E_INFO,  "[FS API - %Mmodule_type] FS_FindFirst <- FileHandle: 0x%X, Attr: 0x%X")
   TRC_MSG(FS_API_FS_FINDNEXT_S,        "[FS API - %Mmodule_type] FS_FindNext -> FileHandle: 0x%X, MaxLength: %d")
   TRC_MSG(FS_API_FS_FINDNEXT_E,        "[FS API - %Mmodule_type] FS_FindNext <- FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_FINDNEXT_E_INFO,   "[FS API - %Mmodule_type] FS_FindNext <- FileHandle: 0x%X, Attr: 0x%X")
   TRC_MSG(FS_API_FS_FINDCLOSE_S,       "[FS API - %Mmodule_type] FS_FindClose -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_GETDEVSTATUS_S_DRIVE,  "[FS API - %Mmodule_type] FS_GetDevStatus -> Target: %c, Type: %MFS_GET_DEV_STATUS_ENUM")
   TRC_MSG(FS_API_FS_GETDEVSTATUS_S_TYPE,   "[FS API - %Mmodule_type] FS_GetDevStatus -> Target: %d, Type: %MFS_GET_DEV_STATUS_ENUM")
   TRC_MSG(FS_API_FS_GETDEVSTATUS_E,        "[FS API - %Mmodule_type] FS_GetDevStatus <- Result: %d")
   TRC_MSG(FS_API_FS_XDELETEEX_S,       "[FS API - %Mmodule_type] FS_XDeleteEx -> Flag: 0x%X, FolderName:")
   TRC_MSG(FS_API_FS_XDELETEEX_E,       "[FS API - %Mmodule_type] FS_XDeleteEx <- Return: %d")
   TRC_MSG(FS_API_FS_GETDRIVE_S,        "[FS API - %Mmodule_type] FS_GetDrive -> Type: 0x%X, Serial: %d, AltMask: 0x%X")
   TRC_MSG(FS_API_FS_GETDRIVE_E,        "[FS API - %Mmodule_type] FS_GetDrive <- Return: %c")
   TRC_MSG(FS_API_FS_GETFILEPOSITION_S, "[FS API - %Mmodule_type] FS_GetFilePosition -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_GETFILEPOSITION_E, "[FS API - %Mmodule_type] FS_GetFilePosition <- Position: %d")
   TRC_MSG(FS_API_FS_GETATTRIBUTES_S,   "[FS API - %Mmodule_type] FS_GetAttributes ->")
   TRC_MSG(FS_API_FS_GETATTRIBUTES_E,   "[FS API - %Mmodule_type] FS_GetAttributes <- Attr: 0x%X")
   TRC_MSG(FS_API_FS_GETFILEINFO_S,     "[FS API - %Mmodule_type] FS_GetFileInfo -> FileHandle: 0x%X, FileInfo: 0x%X")
   TRC_MSG(FS_API_FS_GETFILEINFO_E,     "[FS API - %Mmodule_type] FS_GetFileInfo <-")
   TRC_MSG(FS_API_FS_GETDEVTYPE_S,      "[FS API - %Mmodule_type] FS_GetDevType ->")
   TRC_MSG(FS_API_FS_GETDEVTYPE_E,      "[FS API - %Mmodule_type] FS_GetDevType <- Type: %d")
   TRC_MSG(FS_API_FS_SETSEEKHINT_S,     "[FS API - %Mmodule_type] FS_SetSeekHint -> FileHandle: 0x%X, HintNum: %d")
   TRC_MSG(FS_API_FS_SETSEEKHINT_E,     "[FS API - %Mmodule_type] FS_SetSeekHint <- Return: %d")
   TRC_MSG(FS_API_FS_SETSEEKHINTEX_S,   "[FS API - %Mmodule_type] FS_SetSeekHintEx -> FileHandle: 0x%X, HintCount: %d, Flag: 0x%X, Buffer: 0x%X")
   TRC_MSG(FS_API_FS_SETSEEKHINTEX_E,   "[FS API - %Mmodule_type] FS_SetSeekHintEx <- Return: %d")
   TRC_MSG(FS_API_FS_ENABLESMARTSEEK_S, "[FS API - %Mmodule_type] FS_EnableSmartSeek -> Option: %d")
   TRC_MSG(FS_API_FS_TRUNCATE_S,        "[FS API - %Mmodule_type] FS_Truncate -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_TRUNCATE_E,        "[FS API - %Mmodule_type] FS_Truncate <-")
   TRC_MSG(FS_API_FS_RENAME_S_OLDNAME,  "[FS API - %Mmodule_type] FS_Rename -> Old Name:")
   TRC_MSG(FS_API_FS_RENAME_S_NEWNAME,  "[FS API - %Mmodule_type] FS_Rename -> New Name:")
   TRC_MSG(FS_API_FS_RENAME_E,          "[FS API - %Mmodule_type] FS_Rename <-")
   TRC_MSG(FS_API_FS_IOCTRL_S,          "[FS API - %Mmodule_type] FS_IOCtrl -> DriveName: %c, Action: %d")
   TRC_MSG(FS_API_FS_IOCTRL_E,          "[FS API - %Mmodule_type] FS_IOCtrl -> Result: %d")
   TRC_MSG(FS_API_FS_COMPACTDIR_S,      "[FS API - %Mmodule_type] FS_CompactDir -> Flags: %d, DirName:")
   TRC_MSG(FS_API_FS_COMPACTDIR_E,      "[FS API - %Mmodule_type] FS_CompactDir <- Result: %d")
   TRC_MSG(FS_API_FS_CLOSEALL_S,        "[FS API - %Mmodule_type] FS_CloseAll ->")
   TRC_MSG(FS_API_FS_CLOSEALL_E,        "[FS API - %Mmodule_type] FS_CloseAll <-")
   TRC_MSG(FS_API_FS_RELEASEFH_S_TASKID,    "[FS API - %Mmodule_type] FS_ReleaseFH -> TaskID: 0x%X")
   TRC_MSG(FS_API_FS_RELEASEFH_S_ALLTASK,   "[FS API - %Mmodule_type] FS_ReleaseFH -> TaskID: ALL")
   TRC_MSG(FS_API_FS_RELEASEFH_E,           "[FS API - %Mmodule_type] FS_ReleaseFH <- Return: 0")
   TRC_MSG(FS_API_FS_ABORT_S,           "[FS API - %Mmodule_type] FS_Abort -> ActionHandle: 0x%X")
   TRC_MSG(FS_API_FS_ABORT_E,           "[FS API - %Mmodule_type] FS_Abort <- Return: 0")
   TRC_MSG(FS_API_FS_PARSEFH_S,         "[FS API - %Mmodule_type] FS_ParseFH -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_PARSEFH_E,         "[FS API - %Mmodule_type] FS_ParseFH <- Return: %d")
   TRC_MSG(FS_API_FS_SETFILETIME_S,     "[FS API - %Mmodule_type] FS_SetFileTime -> FileHandle: 0x%X, Time: 0x%X")
   TRC_MSG(FS_API_FS_SETFILETIME_E,     "[FS API - %Mmodule_type] FS_SetFileTime <- Return: %d")
   TRC_MSG(FS_API_FS_PSEUDOMERGE_S_FILE1,   "[FS API - %Mmodule_type] FS_PseudoMerge -> File1:")
   TRC_MSG(FS_API_FS_PSEUDOMERGE_S_FILE2,   "[FS API - %Mmodule_type] FS_PseudoMerge -> File2:")
   TRC_MSG(FS_API_FS_PSEUDOMERGE_E,         "[FS API - %Mmodule_type] FS_PseudoMerge <- Return: %d")
   TRC_MSG(FS_API_FS_CHECKFILE_S,       "[FS API - %Mmodule_type] FS_CheckFile -> FileName:")
   TRC_MSG(FS_API_FS_CHECKFILE_E,       "[FS API - %Mmodule_type] FS_CheckFile <- Return: %d")
   TRC_MSG(FS_API_FS_GETCURRENTDIR_S,   "[FS API - %Mmodule_type] FS_GetCurrentDir -> MaxLength: %d")
   TRC_MSG(FS_API_FS_GETCURRENTDIR_E,   "[FS API - %Mmodule_type] FS_GetCurrentDir <- Return: %d, CurrDir:")
   TRC_MSG(FS_API_FS_GETCURRENTDIRBYDRIVE_S,   "[FS API - %Mmodule_type] FS_GetCurrentDirByDrive -> Drive: %c, MaxLength: %d")
   TRC_MSG(FS_API_FS_GETCURRENTDIRBYDRIVE_E,   "[FS API - %Mmodule_type] FS_GetCurrentDirByDrive <- Return: %d, CurrDir:")
   TRC_MSG(FS_API_FS_SETCURRENTDIR_S,   "[FS API - %Mmodule_type] FS_SetCurrentDir -> DirName:")
   TRC_MSG(FS_API_FS_SETCURRENTDIR_E,   "[FS API - %Mmodule_type] FS_SetCurrentDir <- Return: %d")
   TRC_MSG(FS_API_FS_EXTEND_S,          "[FS API - %Mmodule_type] FS_Extend -> FileHandle: 0x%X, Length: %d")
   TRC_MSG(FS_API_FS_EXTEND_E,          "[FS API - %Mmodule_type] FS_Extend <- Return: %d")
   TRC_MSG(FS_API_FS_MAKEFILENAME_S,    "[FS API - %Mmodule_type] FS_MakeFileName -> FileHandle: 0x%X, Flag: %d, MaxLength: %d")
   TRC_MSG(FS_API_FS_MAKEFILENAME_E,    "[FS API - %Mmodule_type] FS_MakeFileName <- Return: %d, FileName:")
   TRC_MSG(FS_API_FS_FINDFIRSTN_S,      "[FS API - %Mmodule_type] FS_FindFirstN -> PatternNum: %d, MaxLength: %d, EntryIndex: %d, Flag: 0x%X, Pattern:")
   TRC_MSG(FS_API_FS_FINDFIRSTN_E,      "[FS API - %Mmodule_type] FS_FindFirstN <- Attr: 0x%X, FileName:")
   TRC_MSG(FS_API_FS_FINDFIRSTN_E_HINT, "[FS API - %Mmodule_type] FS_FindFirstN <- Attr: %X, Cluster: %d, Index: %d, FileName:")
   TRC_MSG(FS_API_FS_FINDNEXTN_S,       "[FS API - %Mmodule_type] FS_FindNextN -> FileHandle: 0x%X, PatternNum: %d, MaxLength: %d, Flag: 0x%X")
   TRC_MSG(FS_API_FS_FINDNEXTN_E,       "[FS API - %Mmodule_type] FS_FindNextN <- Attr: 0x%X, FileName:")
   TRC_MSG(FS_API_FS_FINDNEXTN_E_HINT,  "[FS API - %Mmodule_type] FS_FindNextN <- Attr: 0x%X, Cluster: %d, Index: %d, FileName:")
   TRC_MSG(FS_API_FS_XFINDRESET_S,      "[FS API - %Mmodule_type] FS_XFindReset -> PatternNum: %d, Flag: 0x%X, Pattern:")
   TRC_MSG(FS_API_FS_XFINDRESET_E,      "[FS API - %Mmodule_type] FS_XFindReset <- Count: %d")
   TRC_MSG(FS_API_FS_XFINDSTART_S,      "[FS API - %Mmodule_type] FS_XFindStart -> Index: %d, MaxLength: %d, Flag: 0x%X, Pattern:")
   TRC_MSG(FS_API_FS_XFINDSTART_E_INFO, "[FS API - %Mmodule_type] FS_XFindStart <- Attr: 0x%X, FileName:")
   TRC_MSG(FS_API_FS_XFINDSTART_E,      "[FS API - %Mmodule_type] FS_XFindStart <- FileName:")
   TRC_MSG(FS_API_FS_XFINDCLOSE_S,      "[FS API - %Mmodule_type] FS_XFindClose -> Position: 0x%X")
   TRC_MSG(FS_API_FS_XFINDCLOSE_E,      "[FS API - %Mmodule_type] FS_XFindClose <- Return: 0")
   TRC_MSG(FS_API_FS_GETPARTITIONINFO_S,"[FS API - %Mmodule_type] FS_GetPartitionInfo -> DriveName: %c, PartitionInfo: 0x%X")
   TRC_MSG(FS_API_FS_GETPARTITIONINFO_E,"[FS API - %Mmodule_type] FS_GetPartitionInfo <- Return: %d")
   TRC_MSG(FS_API_FS_COMMITALL_S,       "[FS API - %Mmodule_type] FS_CommitAll -> DriveName: %c")
   TRC_MSG(FS_API_FS_COMMITALL_E,       "[FS API - %Mmodule_type] FS_CommitAll <- Return: %d")
   TRC_MSG(FS_API_FS_MAPPINGDRIVE_S,    "[FS API - %Mmodule_type] FS_MappingDrive -> Old Drive: %c, New Drive: %c")
   TRC_MSG(FS_API_FS_MAPPINGDRIVE_E,    "[FS API - %Mmodule_type] FS_MappingDrive -> Return: %d")
   TRC_MSG(FS_API_FS_GENERALFORMAT_S,   "[FS API - %Mmodule_type] FS_GeneralFormat -> DriveName: %c, Level: 0x%X")
   TRC_MSG(FS_API_FS_GENERALFORMAT_E,   "[FS API - %Mmodule_type] FS_GeneralFormat -> Return: %d")
   TRC_MSG(FS_API_FS_QMGETFREE_S,       "[FS API - %Mmodule_type] FS_QmGetFree -> Path:")
   TRC_MSG(FS_API_FS_QMGETFREE_E,       "[FS API - %Mmodule_type] FS_QmGetFree <- Return: %d")
   TRC_MSG(FS_API_FS_CHECKDISKFLAG_S,   "[FS API - %Mmodule_type] FS_CheckDiskFlag ->")
   TRC_MSG(FS_API_FS_CHECKDISKFLAG_E,   "[FS API - %Mmodule_type] FS_CheckDiskFlag <-")
   TRC_MSG(FS_API_FS_LOCKFAT_S,         "[FS API - %Mmodule_type] FS_LockFAT ->")
   TRC_MSG(FS_API_FS_LOCKFAT_E,         "[FS API - %Mmodule_type] FS_LockFAT <- Return: %d")
   TRC_MSG(FS_API_FS_TESTMSDC_S,            "[FS API - %Mmodule_type] FS_TestMSDC -> SlotID: 0x%X")
   TRC_MSG(FS_API_FS_TESTMSDC_S_DRIVELIST,  "[FS API - %Mmodule_type] FS_TestMSDC -> SlotID: 0x%X, DriveList: 0x%X, DriveNum: %d")
   TRC_MSG(FS_API_FS_TESTMSDC_E,            "[FS API - %Mmodule_type] FS_TestMSDC <- Return: %d")
   TRC_MSG(FS_API_FS_TESTMSDC_E_DRIVELIST,  "[FS API - %Mmodule_type] FS_TestMSDC <- Return, %d, DriveNum: %d")
   TRC_MSG(FS_API_FS_CLOSEMSDC_S,           "[FS API - %Mmodule_type] FS_CloseMSDC -> DriveLetter: %c, Mode: 0x%X")
   TRC_MSG(FS_API_FS_CLOSEMSDC_E,           "[FS API - %Mmodule_type] FS_CloseMSDC <- Result: %d")
   TRC_MSG(FS_API_FS_SWITCHDRIVEMODE_S,     "[FS API - %Mmodule_type] FS_SwitchDriveMode -> DriveLetter: %c, Mode: 0x%X")
   TRC_MSG(FS_API_FS_SWITCHDRIVEMODE_E,     "[FS API - %Mmodule_type] FS_SwitchDriveMode <- Result: %d")
   TRC_MSG(FS_API_FS_GENVIRTUALFILENAME_S,  "[FS API - %Mmodule_type] FS_GenVirtualFileName -> FileHandle: 0x%X, VFBeginOffset: %d, VFValidLength: %d, BufLength: %d")
   TRC_MSG(FS_API_FS_GENVIRTUALFILENAME_E,  "[FS API - %Mmodule_type] FS_GenVirtualFileName <- Result: %d, VFileName:")
   TRC_MSG(FS_API_FS_CONFIGEXTDEVICE_S,     "[FS API - %Mmodule_type] FS_ConfigExtDevice -> Action: 0x%X, DrvFuncs: %X, Slots: %d")
   TRC_MSG(FS_API_FS_CONFIGEXTDEVICE_E,     "[FS API - %Mmodule_type] FS_ConfigExtDevice <- Result: %d, VFileName:")
   TRC_MSG(FS_API_FS_CHECKDRIVE_S,      "[FS API - %Mmodule_type] FS_CheckDrive -> DriveLetter: %c, CheckBuffer: 0x%X, BufferSize: %d")
   TRC_MSG(FS_API_FS_CHECKDRIVE_E,      "[FS API - %Mmodule_type] FS_CheckDrive <- Result: %d")
   TRC_MSG(FS_API_FS_SETDIRCACHE_S,     "[FS API - %Mmodule_type] FS_SetDirCache -> FileHandle: 0x%X, NewValue: 0x%X")
   TRC_MSG(FS_API_FS_SETDIRCACHE_E,     "[FS API - %Mmodule_type] FS_SetDirCache <- Result: 0")
   TRC_MSG(FS_API_FS_GETDIRCACHE_S,     "[FS API - %Mmodule_type] FS_GetDirCache -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_GETDIRCACHE_E,     "[FS API - %Mmodule_type] FS_GetDirCache <- Result: %d, Value: 0x%X")
   TRC_MSG(FS_API_FS_EXPIREDIRCACHE_S,  "[FS API - %Mmodule_type] FS_ExpireDirCache -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_EXPIREDIRCACHE_E,  "[FS API - %Mmodule_type] FS_ExpireDirCache <- Return: 0")
   TRC_MSG(FS_API_FS_GETFOLDERSIZE_S,   "[FS API - %Mmodule_type] FS_GetFolderSize -> Flag: 0x%X, RecStack: 0x%X, StackSize: %d, Path:")
   TRC_MSG(FS_API_FS_GETFOLDERSIZE_E,   "[FS API - %Mmodule_type] FS_GetFolderSize <- Return: 0")
   TRC_MSG(FS_API_FS_MOVE_S_SRCFILE,    "[FS API - %Mmodule_type] FS_Move -> Src File:")
   TRC_MSG(FS_API_FS_MOVE_S_DSTFILE,    "[FS API - %Mmodule_type] FS_Move -> Dst File:")
   TRC_MSG(FS_API_FS_MOVE_E,            "[FS API - %Mmodule_type] FS_Move <- Return: %d")
   TRC_MSG(FS_API_FS_GETFIRSTCLUSTER_S, "[FS API - %Mmodule_type] FS_GetFirstCluster -> FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_GETFIRSTCLUSTER_E, "[FS API - %Mmodule_type] FS_GetFirstCluster <- Return: %d, Cluster: %d")
   TRC_MSG(FS_API_FS_OTPREAD_S,         "[FS API - %Mmodule_type] FS_OTPRead -> DevType: 0x%X, Offset: %u, BufferPtr: 0x%X, Length: %u")
   TRC_MSG(FS_API_FS_OTPREAD_E,         "[FS API - %Mmodule_type] FS_OTPRead <- Return: %d")
   TRC_MSG(FS_API_FS_OTPWRITE_S,        "[FS API - %Mmodule_type] FS_OTPWrite -> DevType: 0x%X, Offset: %u, BufferPtr: 0x%X, Length: %u")
   TRC_MSG(FS_API_FS_OTPWRITE_E,        "[FS API - %Mmodule_type] FS_OTPWrite <- Return: %d")
   TRC_MSG(FS_API_FS_OTPLOCK_S,         "[FS API - %Mmodule_type] FS_OTPLock -> DevType: 0x%X")
   TRC_MSG(FS_API_FS_OTPLOCK_E,         "[FS API - %Mmodule_type] FS_OTPLock <- Return: %d")
   TRC_MSG(FS_API_FS_OTPQUERYLENGTH_S,  "[FS API - %Mmodule_type] FS_OTPQueryLength -> DevType: 0x%X")
   TRC_MSG(FS_API_FS_OTPQUERYLENGTH_E,  "[FS API - %Mmodule_type] FS_OTPQueryLength <- Return: %d, Length: %d")
   TRC_MSG(FS_API_FS_SWEEPDEVICE_S,     "[FS API - %Mmodule_type] FS_SweepDevice -> DevType: 0x%X")
   TRC_MSG(FS_API_FS_SWEEPDEVICE_E_DONE,    "[FS API - %Mmodule_type] FS_SweepDevice <- Return: 0 (Done)")
   TRC_MSG(FS_API_FS_SWEEPDEVICE_E_RETRY,   "[FS API - %Mmodule_type] FS_SweepDevice <- Return: -41 (FS_CHECKDISK_RETRY)")
   TRC_MSG(FS_API_FS_COUNTUSEDFH_S,         "[FS API - %Mmodule_type] FS_CountUsedFH -> Flag: %d")
   TRC_MSG(FS_API_FS_COUNTUSEDFH_E,         "[FS API - %Mmodule_type] FS_CountUsedFH <- Return: %d, Length: %d")
   TRC_MSG(FS_API_FS_GETDEVPARTITIONS_S_DRIVE,  "[FS API - %Mmodule_type] FS_GetDevPartitions -> DriveLetter: %c")
   TRC_MSG(FS_API_FS_GETDEVPARTITIONS_S_TYPE,   "[FS API - %Mmodule_type] FS_GetDevPartitions -> DeviceType: %d, Type: %MFS_GET_DEV_STATUS_ENUM")
   TRC_MSG(FS_API_FS_GETDEVPARTITIONS_E,        "[FS API - %Mmodule_type] FS_GetDevPartitions <- Result: %d")
   TRC_MSG(FS_API_FS_RECOVERDEVICE_S,   "[FS API - %Mmodule_type] FS_RecoverDevice -> DeviceType: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_API_FS_RECOVERDEVICE_E,   "[FS API - %Mmodule_type] FS_RecoverDevice <- Return: %d")
   TRC_MSG(FS_API_FS_SETVOLUMELABEL_S,  "[FS API - %Mmodule_type] FS_SetVolumeLabel -> DriveLetter: %c, Label:")
   TRC_MSG(FS_API_FS_SETVOLUMELABEL_E,  "[FS API - %Mmodule_type] FS_SetVolumeLabel <- Return: %d")
   TRC_MSG(FS_API_FS_REMOVEDIR_S,       "[FS API - %Mmodule_type] FS_RemoveDir -> DirName:")
   TRC_MSG(FS_API_FS_REMOVEDIR_E,       "[FS API - %Mmodule_type] FS_RemoveDir <- Return: %d")
   TRC_MSG(FS_API_FS_SETATTRIBUTES_S,   "[FS API - %Mmodule_type] FS_SetAttributes -> Attr: 0x%X, FileName:")
   TRC_MSG(FS_API_FS_SETATTRIBUTES_E,   "[FS API - %Mmodule_type] FS_SetAttributes <- Return: %d")
   TRC_MSG(FS_API_FS_COUNT_S,           "[FS API - %Mmodule_type] FS_Count -> Flag: 0x%X, RecStack: 0x%X, StackSize: %u, FullPath:")
   TRC_MSG(FS_API_FS_COUNT_E,           "[FS API - %Mmodule_type] FS_Count <- Return: %d")
   TRC_MSG(FS_API_FS_MAKEFILEPATHBYHINT_S,  "[FS API - %Mmodule_type] FS_MakeFilePathByHint -> DriveLetter: %c, DirCluster: %u, DirIndex: %u, MaxLength: %d")
   TRC_MSG(FS_API_FS_MAKEFILEPATHBYHINT_E,  "[FS API - %Mmodule_type] FS_MakeFilePathByHint <- Result: %d, FileName:")
   TRC_MSG(FS_API_FS_OPENFILEPATHBYHINT_S,  "[FS API - %Mmodule_type] FS_OpenFileByHint -> DriveLetter: %c, DirCluster: %u, DirIndex: %u, Flags: 0x%X")
   TRC_MSG(FS_API_FS_OPENFILEPATHBYHINT_E,  "[FS API - %Mmodule_type] FS_OpenFileByHint <- FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_DELETEBYHINT_S,        "[FS API - %Mmodule_type] FS_DeleteByHint -> DriveLetter: %c, DirCluster: %u, DirIndex: %u")
   TRC_MSG(FS_API_FS_DELETEBYHINT_E,        "[FS API - %Mmodule_type] FS_DeleteByHint <- FileHandle: 0x%X")
   TRC_MSG(FS_API_FS_GETATTRIBUTESBYHINT_S, "[FS API - %Mmodule_type] FS_GetAttributesByHint -> DriveLetter: %c, DirCluster: %u, DirIndex: %u")
   TRC_MSG(FS_API_FS_GETATTRIBUTESBYHINT_E, "[FS API - %Mmodule_type] FS_GetAttributesByHint <- Attr: 0x%X")
   TRC_MSG(FS_API_FS_SETATTRIBUTESBYHINT_S, "[FS API - %Mmodule_type] FS_SetAttributesByHint -> DriveLetter: %c, DirCluster: %u, DirIndex: %u, Attr: 0x%X")
   TRC_MSG(FS_API_FS_SETATTRIBUTESBYHINT_E, "[FS API - %Mmodule_type] FS_SetAttributesByHint <- Attr: 0x%X")
   TRC_MSG(FS_API_FS_HINTGETPARENT_S,       "[FS API - %Mmodule_type] FS_HintGetParent -> DriveLetter: %c, DirCluster: %u, DirIndex: %u")
   TRC_MSG(FS_API_FS_HINTGETPARENT_E,       "[FS API - %Mmodule_type] FS_HintGetParent <- Return: %d, ParentCluster: %u, ParentCluster: %u")
   TRC_MSG(FS_API_FS_DUMPFHTABLE_S,         "[FS API - %Mmodule_type] FS_DumpFHTable ->")
   TRC_MSG(FS_API_FS_DUMPFHTABLE_E,         "[FS API - %Mmodule_type] FS_DumpFHTable <-")

   /* FS Errors */
   TRC_MSG(FS_API_FS_ERR,       "[FS ERR - %Mmodule_type] %MFS_ERROR_ENUM # %MFS_TRACE_SRC_FILE, line: %d")
   TRC_MSG(FS_API_FS_ERR_FILE,  "[FS ERR - %Mmodule_type] %MFS_ERROR_ENUM # %MFS_TRACE_SRC_FILE, line: %d, FileName(SFN): %c%c%c%c%c%c%c%c.%c%c%c, Dev: %MFS_DEVICE_TYPE_ENUM")

   /* FS Errors with Infomation*/
   TRC_MSG(FS_ERR_DRIVER_NOT_FOUND,     "[FS ERR] Driver function is NOT FOUND!")
   TRC_MSG(FS_ERR_MESSAGEACK_NOT_FOUND, "[FS ERR] MessageAck is required in driver!")
   TRC_MSG(FS_ERR_FS_MOVE_DELETE_FILE_ERROR,    "[FS ERR] Error happens when deleting file after FS_Move is failed! Target FileName:")
   TRC_MSG(FS_ERR_QMAX_OVER_DISK,       "[FS ERR] Quota Configuration Error! Qmax exceeds disk space! Disk Total Space: %u, Qmax: %u (Unit: Cluster)")
   TRC_MSG(FS_ERR_QMIN_OVER_DISK,       "[FS ERR] Quota Configuration Error! Quota MRS (Minimum Required Space) exceeds total disk space! Disk Total Space: %u, MRS: %u (Unit: Cluster)")
   TRC_MSG(FS_ERR_QRFS_OVER_DFS,        "[FS ERR] Quota Run-time Warning! Quota RFS (Required Free Space) exceeds total disk free space! Disk Free Space: %u, RFS: %u (Unit: Cluster)")
   TRC_MSG(FS_ERR_ACCESS_DENIED,        "[FS ERR] Access Denied! File index: %u")
   TRC_MSG(FS_ERR_NULL_PTR_1,           "[FS ERR] %MFS_TRACE_SRC_FILE, line: %u, NULL NamePtr! Could not find 0x005c!")
   TRC_MSG(FS_ERR_REC_TRAV_START,       "[FS ERR] Recursive Engine Error! RecTravStart Error!")
   TRC_MSG(FS_ERR_REC_TRAV_CORE,        "[FS ERR] Recursive Engine Error! RecTravCore Error!")
   TRC_MSG(FS_ERR_REC_TRAV_ACTION,      "[FS ERR] Recursive Engine Error! RecTravAction Error!")
   TRC_MSG(FS_ERR_MEDIA_NOT_PRESENT,    "[FS ERR] Media is not present! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDEVICE_DRIVER_MOUNT_FAIL,    "[FS ERR] MountDevice: Disk driver \"MountDevice\" is failed! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDEVICE_INVALID_SECTOR_SIZE,  "[FS ERR] MountDevice: Invalid sector size! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDEVICE_MSDC_NOT_PRESENT,     "[FS ERR] MountDevice: MSDC card is not present!")
   TRC_MSG(FS_ERR_READSECTORS_DEVICE_ERROR,         "[FS ERR] ReadSectors: Device ERROR! Sector: %u, Sectors: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_READSECTORS_DEV_NOT_READY,        "[FS ERR] ReadSectors: Device is NOT ready! Sector: %u, Sectors: %u, MountState: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_READSECTORS_OUT_OF_RANGE,         "[FS ERR] ReadSectors: Out of range! Sector: %u, Sectors: %u, Geometry: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_READSECTORS_DRIVER_FAIL,          "[FS ERR] ReadSectors: Driver read failed! Sector: %u, Sectors: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_WRITESECTORS_DEVICE_ERROR,        "[FS ERR] WriteSectors: Device ERROR! Sector: %u, Sectors: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_WRITESECTORS_DEV_NOT_READY,       "[FS ERR] WriteSectors: Device is NOT ready! Sector: %u, Sectors: %u, MountState: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_WRITESECTORS_OUT_OF_RANGE,        "[FS ERR] WriteSectors: Out of range! Sector: %u, Sectors: %u, Geometry: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_WRITESECTORS_NO_PROTECTION_MODE,  "[FS ERR] WriteSectors: Driver does not support RecoverableWrite, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_WRITESECTORS_BAD_SECTOR,          "[FS ERR] WriteSectors: Bad sector encountered! Sector: %u, Sectors: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_WRITESECTORS_DRIVER_FAIL,         "[FS ERR] WriteSectors: Driver write failed! Sector: %u, Sectors: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_WRITESECTORS_SYSTEM_CRASH,        "[FS ERR] WriteSectors: Bad sector found in FAT table region! Severe Damage! Sector: %u, Sectors: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_GETBUFFER_OUT_OF_BUFFERS,         "[FS ERR] GetBuffer: Out of buffer! Sector: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_LOCKDEVICE_DEVICE_BUSY,           "[FS ERR] LockDevice: Device is busy! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_NONBLOCKLOCKDEVICE_DEVICE_BUSY,   "[FS ERR] NonBlockLockDevice: Device is busy! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_NONBLOCKLOCKDEVICE_LOCK_DEV_FAIL, "[FS ERR] NonBlockLockDevice: Lock device failed! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_NONBLOCKLOCKDEVICE_LOCK_RTF_FAIL, "[FS ERR] NonBlockLockDevice: Lock native FS failed! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_PARSEFH_OUT_OF_RANGE,             "[FS ERR] ParseFileHandle: File Index: %d, FS_MAX_FILES: %u")
   TRC_MSG(FS_ERR_PARSEFH_UNLOCKED,                 "[FS ERR] ParseFileHandle: File (index: %d) is unlocked! It may be closed by others before!")
   TRC_MSG(FS_ERR_PARSEFH_UNIQUE_NOT_MATCHED,       "[FS ERR] ParseFileHandle: Unique is not matched. This file may be closed by others before! (File index: %d)")
   TRC_MSG(FS_ERR_PARSEFH_NULL_DEV,                 "[FS ERR] ParseFileHandle: NULL device! This file may be closed or freed before! (File index: %d)")
   TRC_MSG(FS_ERR_PARSEFH_INVALID_AFTER_DEV_LOCKED, "[FS ERR] ParseFileHandle: File (index: %d) is invalid after dev is locked! It may be closed by others just now!")
   TRC_MSG(FS_ERR_PARSEFH_EXPORTED,                 "[FS ERR] ParseFileHandle: Device is exported! This file will be closed automatically! (File index: %d)")
   TRC_MSG(FS_ERR_PARSEFH_MEDIA_CHANGED,            "[FS ERR] ParseFileHandle: Device is changed! This file will be closed automatically! (File index: %d)")
   TRC_MSG(FS_ERR_NBPARSEFH_OUT_OF_RANGE,               "[FS ERR] ParseFileHandle_NB: File Index: %d, FS_MAX_FILES: %u")
   TRC_MSG(FS_ERR_NBPARSEFH_UNLOCKED,                   "[FS ERR] ParseFileHandle_NB: File (index: %d) is unlocked! It may be closed by others before!")
   TRC_MSG(FS_ERR_NBPARSEFH_UNIQUE_NOT_MATCHED,         "[FS ERR] ParseFileHandle_NB: Unique is not matched. This file may be closed by others before! (File index: %d)")
   TRC_MSG(FS_ERR_NBPARSEFH_NULL_DEV,                   "[FS ERR] ParseFileHandle_NB: NULL device! This file may be closed or freed before! (File index: %d)")
   TRC_MSG(FS_ERR_NBPARSEFH_INVALID_AFTER_DEV_LOCKED,   "[FS ERR] ParseFileHandle_NB: File (index: %d) is invalid after dev is locked! It may be closed by others just now!")
   TRC_MSG(FS_ERR_NBPARSEFH_EXPORTED,                   "[FS ERR] ParseFileHandle_NB: Device is exported! This file will be closed automatically! (File index: %d)")
   TRC_MSG(FS_ERR_NBPARSEFH_MEDIA_CHANGED,              "[FS ERR] ParseFileHandle_NB: Device is changed! This file will be closed automatically! (File index: %d)")
   TRC_MSG(FS_ERR_MOUNTDRIVE_DEV_MOUNT_FAIL,        "[FS ERR] MountLogicalDrive: Device mount failed! Dev: %MFS_DEVICE_TYPE_ENUM, MountState(Dev): %MMountStates")
   TRC_MSG(FS_ERR_MOUNTDRIVE_DRV_MOUNT_FAIL,        "[FS ERR] MountLogicalDrive: Mount partition failed! Dev: %MFS_DEVICE_TYPE_ENUM, MountState(Drive): %MMountStates")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_SIG,        "[FS ERR] MountLogicalDrive: Invalid signature in PBR! Signature: 0x%X, NearJmp: 0x%X, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_SEC,        "[FS ERR] MountLogicalDrive: Invalid PBR's BytesPerSector! BytesPerSector: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_SPC,        "[FS ERR] MountLogicalDrive: Invalid PBR's SectorsPerCluster! SectorsPerCluster: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_FAT_TYPE,   "[FS ERR] MountLogicalDrive: We can't choose a suitable FAT type for this drive! Cluster: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_FAT32_VER,  "[FS ERR] MountLogicalDrive: FAT32's version should be 0! Ver: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_RESERVED_SEC,   "[FS ERR] MountLogicalDrive: ReservedSectors should NOT be 0! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_FAT_CNT,    "[FS ERR] MountLogicalDrive: Invalid number of FAT tables! Count %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_FAT_SIZE,   "[FS ERR] MountLogicalDrive: FAT size should NOT be 0! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_INVALID_FS_ZERO_SPC,   "[FS ERR] MountLogicalDrive: SectorsPerCluster should NOT be 0! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_TOO_MANY_LOGI_SEC,     "[FS ERR] MountLogicalDrive: Too many logical sectors! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOUNTDRIVE_MEDIA_CHANGED,         "[FS ERR] MountLogicalDrive: Device may be changed! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_SCANPTABLE_MBR_SIG,           "[FS ERR] ScanPartitionTable: Invalid signature in MBR! Signature: 0x%X, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_SCANPTABLE_BOOT_INDICATOR,    "[FS ERR] ScanPartitionTable: Invalid boot indicator in MBR! BootIndicator: 0x%X, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_SCANPTABLE_EXT_MBR_SIG,       "[FS ERR] ScanPartitionTable: Invalid signature in extended MBR! Signature: 0x%X, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_SCANPTABLE_RELATIVE_SECTOR,   "[FS ERR] ScanPartitionTable: Invalid relative sector MBR! RelativeSector: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_LOCATEDRIVE_DRVIDX_OUT_OF_RANGE,  "[FS ERR] LocateDrive: Drive index out of range! DriveIndex: %u")
   TRC_MSG(FS_ERR_LOCATEDRIVE_EXPORTED,             "[FS ERR] LocateDrive: Device is exported! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_LOCATEDRIVE_MEDIA_CHANGED,        "[FS ERR] LocateDrive: Device may be changed! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_GETCLUSTERVALUE_INVALID_IN_CLUSTER,   "[FS ERR] GetClusterValue: Invalid input cluster number! Cluster: %u")
   TRC_MSG(FS_ERR_GETCLUSTERVALUE_INVALID_OUT_CLUSTER,  "[FS ERR] GetClusterValue: Invalid output cluster value! Cluster: %u")
   TRC_MSG(FS_ERR_SETCLUSTERVALUE_INVALID_IN_CLUSTER,   "[FS ERR] SetClusterValue: Invalid cluster number! Cluster: %u")
   TRC_MSG(FS_ERR_SETCLUSTERVALUE_INVALID_OUT_CLUSTER,  "[FS ERR] SetClusterValue: Invalid cluster value! Cluster: %u")
   TRC_MSG(FS_ERR_ALLOCFATSEGPERMIT_DISK_FULL,  "[FS ERR] AllocateFATSegmentPermit: Disk full! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_ALLOCFATSEGPERMIT_APP_QERR,   "[FS ERR] AllocateFATSegmentPermit: APP quota full! Qidx: %u")
   TRC_MSG(FS_ERR_ALLOCFATSEG_DISK_FULL,        "[FS ERR] AllocateFATSegment: Disk full after traverse all FAT! Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_MOVEFILEPTR_WRAP_AROUND,          "[FS ERR] MoveFilePointer: File pointer wraps around! FilePointer: %u, Offset: %u")
   TRC_MSG(FS_ERR_MOVEFILEPTR_BEYOND_LAST_CLUSTER,  "[FS ERR] MoveFilePointer: Move beyond the last cluster! f->Cluster: %u")
   TRC_MSG(FS_ERR_STARTDIRSEARCH_BEYOND_LAST_CLUSTER,   "[FS ERR] StartDirSearch: Invalid dir entry! Cluster: %u, Index: %u, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_ERR_RTFRENAME_DRIVE_NOT_THE_SAME,     "[FS ERR] RTFRename: 2 files should be on the same drive!")
   TRC_MSG(FS_ERR_RTFREAD_FPTR_OUT_OF_RANGE,        "[FS ERR] RTFRead: File pointer is out of range! FilePointer: %u, FileSize: %u")
   TRC_MSG(FS_ERR_RTFREAD_FPTR_WRAP_AROUND,         "[FS ERR] RTFRead: File pointer will be wrapped around! FilePointer: %u, Read Length: %u")
   TRC_MSG(FS_ERR_RTFREAD_MUST_READ_SECTOR_ALIGNED, "[FS ERR] RTFRead: Physical or Volume file should be read sector-aligned length! Length: %u")
   TRC_MSG(FS_ERR_RTFREAD_INVALID_FMAPED_HANDLE,    "[FS ERR] RTFRead: Invalid mapped file handle (Virtual file)! Handle: 0x%X")
   TRC_MSG(FS_ERR_RTFWRITE_FPTR_OUT_OF_RANGE,       "[FS ERR] RTFWrite: File pointer is out of range! FilePointer: %u, FileSize: %u")
   TRC_MSG(FS_ERR_RTFWRITE_FPTR_WRAP_AROUND,        "[FS ERR] RTFWrite: File pointer will be wrapped around! FilePointer: %u, Write Length: %u")
   TRC_MSG(FS_ERR_RTFWRITE_MUST_READ_SECTOR_ALIGNED,"[FS ERR] RTFWrite: Physical or Volume file should be written sector-aligned length! Length: %u")
   TRC_MSG(FS_ERR_RTFWRITE_INVALID_FMAPED_HANDLE,   "[FS ERR] RTFWrite: Invalid mapped file handle (Virtual file)! Handle: 0x%X")
   TRC_MSG(FS_ERR_RTFTRUNCATE_NORMAL_FILE_ONLY,     "[FS ERR] RTFTruncate: Only normal file could be truncated!")
   TRC_MSG(FS_ERR_RTFTRUNCATE_FPTR_OUT_OF_RANGE,    "[FS ERR] RTFTruncate: File pointer is out of range! FilePointer: %u, FileSize: %u")
   TRC_MSG(FS_ERR_RTFSEEK_FPTR_OUT_OF_RANGE,        "[FS ERR] RTFSeek: File pointer is out of range! FilePointer: %u, FileSize: %u")
   TRC_MSG(FS_ERR_RTFSEEK_INVALID_WHENCE,           "[FS ERR] RTFSeek: Invalid Whence! Whence: %u")
   TRC_MSG(FS_ERR_RTFSEEK_FPTR_WRAP_AROUND,         "[FS ERR] RTFSeek: New position is wrapped around! StartPos: %u, Offset: %d, NewPos: %u")
   TRC_MSG(FS_ERR_RTFSEEK_MUST_READ_SECTOR_ALIGNED, "[FS ERR] RTFSeek: Physical or Volume file should be seeked to sector-aligned position! StartPos: %u, Offset: %d, NewPos: %u")
   TRC_MSG(FS_ERR_RTFSEEK_INVALID_FMAPED_HANDLE,    "[FS ERR] RTFSeek: Invalid mapped file handle (Virtual file)! Handle: 0x%X")
   TRC_MSG(FS_ERR_RTFSEEK_CANT_EXTEND_READ_ONLY_FILE,   "[FS ERR] RTFSeek: Can't extend Read Only file! NewPos: %u, FileSize: %u")
   TRC_MSG(FS_ERR_RTFSEEK_CANT_EXTEND_ROOT_DIR,     "[FS ERR] RTFSeek: Can't extend Root Dir! NewPos: %u, FileSize: %u")
   TRC_MSG(FS_ERR_RTFSEEK_INVALID_CLUSTER,          "[FS ERR] RTFSeek: Invalid cluster number found! Cluster: %u")
   TRC_MSG(FS_ERR_RTFEXTEND_FPTR_OUT_OF_RANGE,      "[FS ERR] RTFExtend: File pointer is out of range! FilePointer: %u, FileSize: %u")
   TRC_MSG(FS_ERR_RTFEXTEND_FSIZE_WRAP_AROUND,      "[FS ERR] RTFExtend: File size is wrapped around! FilePointer: %u, Length: %u")
   TRC_MSG(FS_ERR_RTFRESETDISK_FILE_NOT_CLOSED,     "[FS ERR] RTFResetDisk: All file should be closed! FileCount: %u")
   TRC_MSG(FS_ERR_COPYSECTORS_DEV_ERROR,            "[FS ERR] CopySectors: Device error! From: %u, To: %u, Sectors: %u")
   TRC_MSG(FS_ERR_COPYSECTORS_DEV_NOT_MOUNTED,      "[FS ERR] CopySectors: Device is not mounted! From: %u, To: %u, Sectors: %u")
   TRC_MSG(FS_ERR_COPYSECTORS_DRIVER_FAILED,        "[FS ERR] CopySectors: Driver failed! From: %u, To: %u, Sectors: %u")

   /* FS Information */
   TRC_MSG(FS_INFO_FT_TITLE,    "[FS INFO] ====== File Table ======")
   TRC_MSG(FS_INFO_FT_SLOT,     "[FS INFO] FileTable(%d) - Task: %c%c%c, Lock: %d, File(SFN): %c%c%c%c%c%c%c%c.%c%c%c")
   TRC_MSG(FS_INFO_FT_TAIL,     "[FS INFO] === End of File Table ===")
   TRC_MSG(FS_INFO_DIR_ENTRY_WALKED,            "[FS INFO] %d directory entries were walked!")
   TRC_MSG(FS_INFO_QUOTA_DELETE,                "[FS QUOTA] Delete: %d, Free: %d (FATDelete)")
   TRC_MSG(FS_INFO_QUOTA_STATUS_NOW_FATDELETE,  "[FS QUOTA] Status - Qidx: %u, Qnow: %u (FATDelete)")
   TRC_MSG(FS_INFO_QUOTA_STATUS_NOW_ALLOCFATSEG,"[FS QUOTA] Status - Qidx: %u, Qnow: %u (AllocateFATSegment)")
   TRC_MSG(FS_INFO_QUOTA_STATUS_NOW_RTFEXTEND,  "[FS QUOTA] Status - Qidx: %u, Qnow: %u (RTFExtend)")
   TRC_MSG(FS_INFO_QUOTA_BEFORE_ALLOC,          "[FS QUOTA] Before allocate %u, Free: %u")

   /* FS Info Group 1 - R/W sector breakdown */
   TRC_MSG(FS_INFO_GR1_READSECTORS,   "[FS INFO] ReadSectors - Sector: %u, Count: %u, Duration: %u us, Dev: %MFS_DEVICE_TYPE_ENUM")
   TRC_MSG(FS_INFO_GR1_WRITESECTORS,  "[FS INFO] WriteSectors - Sector: %u, Count: %u, Duration: %u us, Dev: %MFS_DEVICE_TYPE_ENUM")

END_TRACE_MAP(MOD_FS)

#endif // _FS_TRC_H
