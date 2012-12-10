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
 *	fs_internal.h
 *
 * Project:
 * --------
 *   Maui
 *
 * Description:
 * ------------
 *    This file defines the internals of file system abstraction layer
 *
 * Author:
 * -------
 *	Karen Hsu (mtk00681)
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by PVCS VM. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision:   1.27  $ 
 * $Modtime:   Jul 29 2005 15:10:38  $ 
 * $Log:   //mtkvs01/vmdata/Maui_sw/archives/mcu/kal/Efs/include/fs_internal.h-arc  $
 *
 * 06 19 2012 archilis.wang
 * [ALPS00303520] [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 * [Merge] Level 3 SW modules 提升至 Level 2 on ALPS.ICS2.MP
 *
 * Jun 29 2010 mtk02187
 * [MAUI_02572526] [System Service][File System][New Feature][MT6255] LPMP3 support
 * 
 *
 * May 15 2010 mtk02187
 * [MAUI_02524592] [System Service][File System][Change Feature] Let Internal Hint mechanism accepts pa
 * 
 *
 * Feb 24 2010 mtk02187
 * [MAUI_02362073] [System Service][File System][Change Feature] Support Internal Open Hint for FS_XFin
 * 
 *
 * Jul 7 2009 mtk02187
 * [MAUI_01716012] [System Service][File System][New Feature] Add FS_CompactDir()
 * 
 *
 * Apr 16 2009 mtk02187
 * [MAUI_01669677] [System Service][File System][New Feature] MTP
 * 
 *
 * Mar 20 2009 mtk02187
 * [MAUI_01652034] [System Service][File System][New Feature] Add new API: FS_IOCtrl
 * 
 *
 * Sep 5 2008 mtk01892
 * [MAUI_00652188] [File manager] couldn't del too deep path folder
 * support move without lock
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
 * Jul 5 2008 MTK01892
 * [MAUI_00799193] [File System][Change Feature] Enable FS_PROPRIETARY_SET in ULC project.
 * enable __FS_PROPRITARY_SET__ while ULC is enabled.
 *
 * Oct 29 2007 mtk01077
 * [MAUI_00567666] [FileSystem] RECCONF_NAMEBUF_PACKAGE_SIZE 2 more characters for \* append
 * 
 *
 * Sep 1 2007 mtk01077
 * [MAUI_00541094] [FS] New API FS_RecoverDevice()
 * 
 *
 * Aug 27 2007 mtk01500
 * [MAUI_00538310] [FS] [Enhancement] FS device busy check for long time operations and other related e
 * 
 *
 * Oct 3 2006 mtk01077
 * [MAUI_00334562] [FS] Code-Review , remove redundancy ReleaseFH() and re-write unnecessary SafeUnlock
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
 * [MAUI_00210678] [FS] Boot Up Time System Drive Quota,  Public Drive Mount Success Check and Message
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
 * Jul 10 2006 mtk01077
 * [MAUI_00207335] [FS] New Mechanism, File System APIs  FS_SetDirCache() FS_GetDirCache()
 * 
 *
 * Mar 21 2006 mtk01077
 * [MAUI_00180993] [FS] New Algorithm apply on FS_CheckDrive() to be adaptive with disk size and buffer
 * 
 *
 * Mar 9 2006 mtk01077
 * [MAUI_00219709] File manager_T flash card disappear
 * 
 *
 * Jan 25 2006 mtk01077
 * [MAUI_00170740] [FS] USB OTG Support
 * 
 *
 * Jan 17 2006 mtk01077
 * [MAUI_00169103] [FS] USB Normal Mode Access Support
 * 
 *
 * Dec 4 2005 mtk01077
 * [MAUI_00159707] [EFS] Remove SYSTEM_DRIVE_ON_NAND and NAND_FLASH_BOOTING option dependency
 * 
 *
 * Nov 13 2005 mtk01077
 * [MAUI_00124403] [FS] align all path string over len case to (-49) FS_PATH_OVER_LEN_ERROR
 * revised for peer-review comment, place it to right section
 *
 * Sep 30 2005 mtk01077
 * [MAUI_00126551] [FS][Add Feature] File System Support Virtual File (aka File Map File) for Content Packaged File Processing
 *   
 * 
 *    Rev 1.27   Jul 29 2005 15:10:52   mtk00681
 * Support system drive on NAND
 * Resolution for 12103: [FS][Enhance] Support system drive on NAND (with NOR)
 * 
 *    Rev 1.26   Jul 29 2005 14:37:28   mtk00681
 * Add abort flag to support FS_XDelete abortion
 * Resolution for 12102: [FS][Enhance] Support abortion for FS_XDelete
 * 
 *    Rev 1.25   Jul 07 2005 10:51:30   mtk00681
 * Modify FS_LockFAT for single bank NOR design
 * Resolution for 11851: [FS][AddFeature]Support single bank NOR design
 * 
 *    Rev 1.24   Jul 05 2005 22:02:36   mtk00681
 * Add new API: FS_GetFirstCluster
 * Resolution for 11832: [FS][AddFeature]Add new API: FS_GetFirstCluster
 * 
 *    Rev 1.23   Jun 23 2005 18:40:02   mtk00681
 * Add MTMakeFileName to get SFN/LFN via RTFDirLocation
 * Resolution for 11657: [FS][Enhance]Modify FS_MakeFileName to support SFN/LFN look up
 * 
 *    Rev 1.22   Jun 02 2005 17:16:58   mtk00681
 * Add sanity check for NFB
 * Resolution for 11336: [FS][Enhance]Modify sanity check and GetDrive for NFB support
 * 
 *    Rev 1.21   May 17 2005 00:32:56   BM_Trunk
 * Karlos:
 * add copyright and disclaimer statement
 * 
 *    Rev 1.20   May 12 2005 13:35:08   mtk00681
 * Add proprietary copyright protection mechanism
 * Resolution for 11028: [FS][AddFeature]Add proprietary copyright protection mechanism
 * 
 *    Rev 1.19   Jan 18 2005 00:37:04   BM
 * append new line in W05.04
 * 
 *    Rev 1.18   Nov 03 2004 17:36:52   mtk00681
 * Remove RVCT warning
 * Resolution for 8571: [FS][BugFix]Remove RVCT warning
 * 
 *    Rev 1.17   Oct 25 2004 17:23:56   mtk00681
 * Remove QM in USB mode
 * Resolution for 7227: [FS][AddFeature]Add Quota Management
 * 
 *    Rev 1.16   Oct 15 2004 23:00:26   mtk00681
 * Refine QM
 * Resolution for 7227: [FS][AddFeature]Add Quota Management
 * 
 *    Rev 1.15   Sep 06 2004 09:45:54   mtk00681
 * Add index for FS_GetFilePosition
 * Resolution for 7458: [FS][Add Feature]Add/modify API for MMTF
 * 
 *    Rev 1.14   Aug 31 2004 19:11:16   mtk00681
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
 *    Rev 1.11   Aug 20 2004 15:34:20   mtk00681
 * Add NVRAM check for QM
 * Resolution for 7227: [FS][AddFeature]Add Quota Management
 * 
 *    Rev 1.10   Aug 20 2004 14:36:02   mtk00681
 * Add FS_GetClusterSize API
 * Resolution for 7227: [FS][AddFeature]Add Quota Management
 * 
 *    Rev 1.9   Aug 20 2004 12:04:52   mtk00681
 * Add sanity check for NAND
 * Resolution for 7266: [FS][AddFeature]Add sanity check for NAND flash
 * 
 *    Rev 1.8   Aug 17 2004 18:04:22   mtk00681
 * Add Quota Management
 * Resolution for 7227: [FS][AddFeature]Add Quota Management
 * 
 *    Rev 1.7   Jul 30 2004 18:13:44   mtk00681
 * Add FS_ParseFH to parse file handle
 * Resolution for 6929: [FS][AddFeature]Add API: FS_ParseFH
 * 
 *    Rev 1.6   Jul 08 2004 22:11:22   mtk00681
 * Add error checking for path over SPEC rule
 * Resolution for 6577: [FS][BugFix]Recursively delete cannot delete path length with 259
 * 
 *    Rev 1.5   Jul 06 2004 19:23:16   mtk00681
 * Add file check for MMTF
 * Resolution for 6523: [FS][AddFeature]Add file check function for MMTF
 * 
 *    Rev 1.4   Jun 14 2004 11:20:58   mtk00681
 * Add trace mechansim
 * Resolution for 6084: [FS][AddFeature]Add FS trace mechanism
 * 
 *    Rev 1.3   May 27 2004 09:04:32   mtk00681
 * Add choice flag for FIND series.
 * Resolution for 5556: [FS][Enhance]Support for encoding display issue
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
#ifndef _FS_INTERNAL_H
#define _FS_INTERNAL_H

/************************* Internal Define *************************/
#define MT_REC_RMDIR             0x00000001           //MTExtDelete
#define MT_REC_SAVE_NAME         0x00000002           //MTExtFindReset
#define MT_MAX_MSDC_TYPE         3                    //MTTestMSDC, MTCloseMSDC
#define MT_MAX_FILE_NUM          256                  //MTExtFindReset: max file/folder number under one folder
#define CopyBufferSize           512*4                //CopyFile, GetSaveNameByIndex
#define MT_PGS_PERCENTAGE        5                    //CopyFile
#define MT_PGS_PERIOD_MASK       0xfffffff0           //CopyFileLightWeight(), trigger user call back every 16 frame ~ 72ms
#define MT_MAX_WIDE_NAME         512
#define MT_MAX_WIDE_PATH         (RTF_MAX_PATH+4)     //+4 for recursive action
#define MT_MAX_QUOTA_ENTRY       40                   //For gFS_ExtQuotaSet entry 
#define MT_MAXPATH_IN_WCHAR_UNIT (RTF_MAX_PATH/2)                  //RTF_MAX_PATH = 520 char = 260 wide-char
   //FS_Move internal flag to distinguish status
#define MT_XMOVE_SRC_EXIST       0x00000001           //will be forbidden initial if fail
#define MT_XMOVE_SRC_IS_FOLDER   0x00000002
#define MT_XMOVE_SRC_IS_VIRTUAL  0x00000004
#define MT_XMOVE_SRC_ATTR_RO     0x00000008
#define MT_XMOVE_SRC_DEVICE_RO   0x00000010
#define MT_XMOVE_DEST_EXIST      0x00000100
#define MT_XMOVE_DEST_IS_FOLDER  0x00000200
#define MT_XMOVE_DEST_ATTR_RO    0x00000400
#define MT_XMOVE_DEST_DEVICE_RO  0x00000800           //will be forbidden initial if fail
#define MT_XMOVE_SAME_DRIVE      0x00001000
#define MT_XMOVE_SAME_DEVICE     0x00002000
#define MT_XMOVE_COPYSECTOR      0x00004000
   // Recursive Config
#define RECCONF_NAMEBUF_GUARD_PRINT     (0xF5F5FAFA)
#define RECCONF_NAMEBUF_PACKAGE_SIZE    (RTF_MAX_PATH + 4 /* additional null */ + sizeof(int) /* guard */)
#define RECCONF_DIRSTACK_SIZE           (39 * sizeof(RTFDirEntry)) // 39 * 52 = 2028

typedef enum
{
   API_ENUM_OPEN = 1,
   API_ENUM_OPENHINT,
   API_ENUM_CLOSE,
   API_ENUM_CLOSEALL,
   API_ENUM_READ,
   API_ENUM_WRITE,
   API_ENUM_SETSEEKHINT,
   API_ENUM_SEEK,
   API_ENUM_COMMIT,
   API_ENUM_RELEASEFH,
   API_ENUM_ABORT,
   API_ENUM_PARSEFH,
   API_ENUM_GETFILEINFO,
   API_ENUM_GETFILESIZE,
   API_ENUM_GETFILEPOSITION,
   API_ENUM_SETFILETIME,
   API_ENUM_GETATTRIBUTES,
   API_ENUM_GETFIRSTCLUSTER,
   API_ENUM_SETATTRIBUTES,
   API_ENUM_DELETE,
   API_ENUM_PSEUDOMERGE,
   API_ENUM_CHECKFILE,
   API_ENUM_GETCURRENTDIR,
   API_ENUM_SETCURRENTDIR,
   API_ENUM_CREATEDIR,
   API_ENUM_REMOVEDIR,
   API_ENUM_GETFOLDERSIZE,
   API_ENUM_EXTEND,
   API_ENUM_TRUNCATE,
   API_ENUM_MAKEFILENAME,
   API_ENUM_RENAME,
   API_ENUM_MOVE,
   API_ENUM_COUNT,
   API_ENUM_XDELETE,
   API_ENUM_FINDFIRST,
   API_ENUM_FINDNEXT,
   API_ENUM_FINDFIRSTN,
   API_ENUM_FINDNEXTN,
   API_ENUM_FINDCLOSE,
   API_ENUM_XFINDRESET,
   API_ENUM_XFINDSTART,
   API_ENUM_XFINDCLOSE,
   API_ENUM_CREATEBOOTSECTOR,
   API_ENUM_GETPARTITIONINFO,
   API_ENUM_COMMITALL,
   API_ENUM_GETDRIVE,
   API_ENUM_MAPPINGDRIVE,   
   API_ENUM_GENERALFORMAT,
   API_ENUM_QMGETFREE,
   API_ENUM_GETCLUSTERSIZE,
   API_ENUM_CHECKDRIVE,
   API_ENUM_CREATEMASTERBOOTRECORD,
   API_ENUM_SPLITPARTITION,
   API_ENUM_GETDISKINFO,
   API_ENUM_SHUTDOWN,
   API_ENUM_UNLOCKALL,
   API_ENUM_SANITYCHECK,
   API_ENUM_SETDISKFLAG,
   API_ENUM_CHECKDISKFLAG,
   API_ENUM_CLEARDISKFLAG,
   API_ENUM_GETDEVTYPE,
   API_ENUM_GETDEVSTATUS,
   API_ENUM_LOCKFAT,
   API_ENUM_TESTMSDC,
   API_ENUM_CLOSEMSDC,
   API_ENUM_SETTRACE,
   API_ENUM_DUMPFHTABLE,
   API_ENUM_GENVIRTUALFILENAME,
   API_ENUM_CONFIGEXTDEVICE,
   API_ENUM_GETDIRCACHE,
   API_ENUM_SETDIRCACHE,
   API_ENUM_EXPIREDIRCACHE,
   API_ENUM_OTPREAD,
   API_ENUM_OTPWRITE,
   API_ENUM_OTPLOCK,
   API_ENUM_OTPQUERYLENGTH,
   API_ENUM_SWEEPDEVICE,
   API_ENUM_COUNTUSEDFH,
   API_ENUM_GETDEVPARTITIONS,
   API_ENUM_RECOVERDEVICE,
   API_ENUM_SETVOLUMELABEL,
   API_ENUM_SWITCHDRIVEMODE,
   API_ENUM_IOCTRL,
   API_ENUM_COMPACTDIR
}FS_API_ENUM;

/* ---  ScanDrive Engine Internal Usage */
typedef RTFCluster MTRAWClusterValue(RTFDrive * Drive, RTFCluster Cluster);

typedef struct __InternScanDataStruct {
   RTFDrive             * Drive;
   MTRAWClusterValue    * RAWCluster;
   BYTE                 * ClusterMap;
   RTFDOSDirEntry         D;
   RTFCluster             ClsOffset;
   RTFCluster             ClsSpan;
   RTFCluster             ClsRange;
}InternScanDataStruct;

/* ---  Recursive Engine Internal Usage, Act priviate data */
typedef struct __InternCallBackParameter {
   // Configured parameter
   DWORD                  Flag;
   FS_ProgressCallback  * Progress;
   UINT                   Total;
   UINT                   ProgInfo;
   void                 * PrivateData;
   // Final Result and Status
   int                    Result;
   int                    ErrorCode;
   // Run-Time Self-Use Context, Optional for temporal / sparital coherence
   RTFDrive             * Drive;
   MTRAWClusterValue    * RAWCluster;
}InternCallBackData;

typedef struct __InternRecursiveEngineStruct {
   // Run-Time Used Resource
   WCHAR                * NameBuf;
   WCHAR                * CurrPath;
   WCHAR                * DestPath;         /* 3rd Name Buffer used by XDelete or XCopy */
   RTFDirEntry          * LevelStack;
   // Boundary Check
   int                    LevelLimit;
   UINT                   CurrLeftLen;
   // Final Result and Status
   int                    TravStatus;
   // Configure-Time Setup , Generic CallBack Functions
   UINT                   PrefixPathLen; /* Length of the Prefix Path in CurrPath */
   UINT                   DestPrefixPathLen; /* Length of the Prefix Path in DestPath */
   InternCallBackData     Parameters;
}InternRecursiveEngineStruct;

typedef int MTGenericCallBack(InternRecursiveEngineStruct *RES, WCHAR *ObjName, RTFDOSDirEntry *ObjInfo);

#ifdef __FS_SORT_SUPPORT__

extern const UINT FS_SORTING_MAX_ENTRY;

#define ABS(x)    (x>0? x: -x)
#define FS_SORTING_SIGNATURE           "FSST"
#define SWAP(x, y) { x^=y; y^=x; x^=y; }

#define SWAP_SORTING_ENTRY(x, y)                                    \
{                                                                   \
        SWAP(HintList[x], HintList[y]);                             \
        SWAP(PosList[x].Cluster, PosList[y].Cluster);               \
        SWAP(PosList[x].Index, PosList[y].Index);                   \
}

#define SWAP_SORTING_ENTRY_FILE(x, y)                               \
{                                                                   \
        SWAP(HintList_File[x], HintList_File[y]);                   \
        SWAP(PosList_File[x].Cluster, PosList_File[y].Cluster);     \
        SWAP(PosList_File[x].Index, PosList_File[y].Index);         \
}

#define FS_SORTING_IS_FLAG_HINT_DISABLED  (0x80)

typedef struct
{
    BYTE                Signature[4];
    FS_SORT_PGS_ENUM    Status;
    BYTE                Flag;

    int                 TotalCount;
    int                 ReadyCount;
    int                 FolderCount;
    int                 FileCount;
    RTFDirLocation      *PosList;
    RTFDirLocation      *PosList_File;

    int                 MaxCount;

    int                 UserLock;

    // Working Buffer
    UINT                *HintList;
    UINT                *HintList2;
    UINT                *HintList_File;
    UINT                *HintList2_File;
    BYTE                *FileName;
    BYTE                *TmpName1;
    BYTE                *TmpName2;

    RTFDOSDirEntry      *FileInfo;

    char                *FreeCacheList;

    UINT                MaxFileNameLength;

    UINT                DirCluster;
} FSSortingInternalS; /* ! MUST SYNC WITH FS_SORT_SORTING_BUFFER_SIZE_FOR_FILES ! */

#endif /* __FS_SORT_SUPPORT__ */

/************************* Internal APIs *************************/
extern void SweepDrive(BYTE DriveIdx);
extern int  ChkQuotaConfig(BYTE DriveIdx);
extern void CloseDevice(RTFDevice * Dev);
extern int  ReleaseFH(void * TaskId);
extern int  CountUsedFH(void * TaskId);

extern int  CopyFileLightWeight(const WCHAR * SrcFullPath, const WCHAR * DstFullPath, FS_ProgressCallback Progress, BYTE * Buffer, int BufferLen);
extern int  CopyFileOnSameDrive(const WCHAR * SrcFullPath, const WCHAR * DstFullPath, FS_ProgressCallback Progress, BYTE * Buffer, int BufferLen);
extern int  CreateCopyDestPath(const WCHAR * SrcFullPath, const WCHAR * DstFullPath);

#ifdef __FS_SORT_SUPPORT__
extern int  GetFindByPos(const WCHAR * Pattern, RTFDOSDirEntry * FileInfo, WCHAR * FileName, DWORD MaxLength, RTFDirLocation * Pos, DWORD Flag);
extern int  Sort(const WCHAR * FileName, WCHAR * TmpName, RTFDOSDirEntry * FileInfo, DWORD Flag, RTFDirLocation * PosList, DWORD * Hex, DWORD Index, const WCHAR * Pattern, RTFDirLocation * Pos);
extern int  CompareFileName(WCHAR *FileName1, UINT*Hint1, WCHAR *FileName2, UINT *Hint2);
extern int  CreateHeap(FS_SortingParam *Param, FSSortingInternalS *SortingData);
extern int  HeapSort(FS_SortingParam *Param, FSSortingInternalS *SortingData);
#endif

extern int  FindFirst(const WCHAR * NamePattern, FS_Pattern_Struct * PatternArray, DWORD PatternNum, BYTE ArrayMask, BYTE Attr, BYTE AttrMask, RTFDOSDirEntry * FileInfo, WCHAR * FileName, UINT MaxLength, RTFDirLocation * Pos_Hint, UINT *DirCluster);
extern int  FindNext(RTFHANDLE Handle, FS_Pattern_Struct * PatternArray, DWORD PatternNum, BYTE ArrayMask, RTFDOSDirEntry * FileInfo, WCHAR * FileName, DWORD MaxLength, RTFDirLocation * Pos_Hint);

#ifdef __FS_QM_SUPPORT__
extern void SweepDrive(BYTE DriveIdx);
#endif

#ifdef __FS_CHECKDRIVE_SUPPORT__
extern int  ScanDrive(RTFDrive * Drive, void * Buffer, unsigned int BufferLen);
#endif

#ifdef __P_PROPRIETARY_COPYRIGHT__
extern int CopyrightListCallBack(WCHAR * target1, WCHAR * target2, DWORD Flag, FS_ProgressCallback Progress);
#endif

extern int MTMakeFileName(RTFDrive * Drive, RTFDirLocation * Pos, WCHAR * FileName, UINT MaxLength);
extern int MTGenVirtualFileName(FS_HANDLE F, WCHAR * VFNBuf, UINT BufLen, UINT Offset, UINT Length);
extern int FindDeviceNumberByDriveIdx(int DriveIdx);
extern RTFDevice * FindFirstDeviceByType(FS_DEVICE_TYPE_ENUM);
extern int MountRemovableDevice(RTFDevice *Dev);
extern int ReMountDriveAndCountFreeClusters(RTFDrive *Drive);

      /* Object (File/Folder) Identify */
extern int GetFirstClusterByFileHandle(RTFHANDLE FileHandle, UINT * Cluster);
extern int GetFirstClusterByFileName(const WCHAR *FileName, UINT * Cluster);

      /* Recursive Class -- Engine */
RTFHANDLE RecTravStart(WCHAR * PathNamePattern);
int RecTravClose(RTFHANDLE Handle);

      /* Recursive Class -- Search */
void RecTravCore_DFS(InternRecursiveEngineStruct *TravCB, RTFHANDLE TravFH, MTGenericCallBack *Act);
void RecTravCore_BFS(InternRecursiveEngineStruct *TravCB, RTFHANDLE TravFH, MTGenericCallBack *Act);
void RecTravCore_Flat(InternRecursiveEngineStruct *TravCB, RTFHANDLE TravFH, MTGenericCallBack *Act);
void RecTravCore_CloseAndRootNodeAct(InternRecursiveEngineStruct *TravCB, RTFHANDLE TravFH, MTGenericCallBack *Act);
#ifdef __P_PROPRIETARY_COPYRIGHT__
void RecTravCore_DFS_CR(InternRecursiveEngineStruct *TravCB, RTFHANDLE TravFH, MTGenericCallBack *Act, RTFCluster Skip);
#endif

      /* Recursive Class -- Action */
int RecAct_List(InternRecursiveEngineStruct *RES, WCHAR *ObjName, RTFDOSDirEntry *ObjInfo);
int RecAct_CountNum(InternRecursiveEngineStruct *RES, WCHAR *ObjName, RTFDOSDirEntry *ObjInfo);
int RecAct_CountSize(InternRecursiveEngineStruct *RES, WCHAR *ObjName, RTFDOSDirEntry *ObjInfo);
#ifdef __P_PROPRIETARY_COPYRIGHT__
int RecAct_CopyrightDeletion(InternRecursiveEngineStruct *RES, WCHAR *ObjName, RTFDOSDirEntry *ObjInfo);
int RecAct_CopyrightList(InternRecursiveEngineStruct *RES, WCHAR *ObjName, RTFDOSDirEntry *ObjInfo);
#endif

      /* Recursive Class -- Config */
int RecConf_Alloc(InternRecursiveEngineStruct **RES, BYTE *RecursiveStack, const UINT StackSize);
void RecConf_Free(InternRecursiveEngineStruct **RES, BYTE *RecursiveStack, const UINT StackSize);

      /* Recursive Class -- API auxilary */
int RecAUX_IsFolder(const WCHAR * PathName);
int RecAUX_IsFolderRW(const WCHAR * PathName);
int RecAUX_TestSrcAndDestPath(const WCHAR * SrcPathName, const WCHAR * DestPathName, UINT Flag);
int RecAUX_CountNumOfObjUnderFolderTree(const WCHAR * FullPath, UINT Flag, BYTE *RecursiveStack, const UINT StackSize);
int RecAUX_CountSumOfSizeUnderFolderTree(const WCHAR * FullPath, UINT Flag, BYTE *RecursiveStack, const UINT StackSize);
int RecAUX_XDeleteFolder(const WCHAR * FullPath, UINT Flag, FS_ProgressCallback Progress, BYTE *RecursiveStack, const UINT StackSize);
int RecAUX_XCopyFolder(const WCHAR * FullPath, const WCHAR * DstPath, int Status,
                       FS_ProgressCallback Progress, UINT Total,
                       BYTE *RecursiveStack, const UINT StackSize);

#ifdef __AUDIO_DSP_LOWPOWER__
int MTQueryPhysicalMap(FS_PMapInfo* PMapInfo);
#endif

/************* Internal Data *************/
#ifdef __FS_CHECKDRIVE_SUPPORT__
extern kal_bool g_CheckDrive;
#endif

extern kal_bool g_Xdelete;
extern kal_bool g_ExternalDevice;

extern UINT FS_MAX_COPY_CLUSTER;

#ifdef __P_PROPRIETARY_COPYRIGHT__
extern UINT EFS_MAX_CHECK_HAS_PROTECT;
extern UINT EFS_MAX_CHECK_NON_PROTECT;

extern const WCHAR *EFS_Copyright_Folder;
extern const FS_Pattern_Struct *EFS_Copyright_Pattern;
extern UINT *EFS_Copyright_List1;
extern UINT *EFS_Copyright_List2;

extern kal_bool CRP_IsMediaFile(const unsigned char *pBuf);
#endif

#endif //_FS_INTERNAL_H


