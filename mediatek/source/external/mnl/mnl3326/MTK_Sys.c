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

//*****************************************************************************
// [File] MTK_Sys.c
// [Version] v1.0
// [Revision Date] 2008-03-31
// [Author] YC Chien, yc.chien@mediatek.com, 21558
// [Description]
//*****************************************************************************

#include <stdio.h>
#include <stdlib.h>
#include "MTK_Sys.h"

#ifdef _WINDOWS
#include "windows.h"

#define MTK_SYS_SEMAPHORE_NUM   12

CRITICAL_SECTION CriticalSection[MTK_SYS_SEMAPHORE_NUM];

#else
#include <time.h>
#include <pthread.h>

#define MTK_SYS_SEMAPHORE_NUM   12

static pthread_mutex_t bee_sys_smphr[MTK_SYS_SEMAPHORE_NUM];
#endif

#ifdef BEE_HEAP_DEBUG
unsigned int _u4HeapTotalSize = 0;
unsigned int _u4MaxHeapSize = 0;
int _i4HeapIdx = 0;
unsigned int _HeapSize[65536];
#endif

//*****************************************************************************
// Semaphore Functions
//*****************************************************************************

//*****************************************************************************
// MTK_Sys_Init_Smphr : Initialize semaphore
//
// PARAMETER : u4SmphrNum [IN] - semaphore number to be initizlized
//
// RETURN : void

void MTK_Sys_Init_Smphr (MTK_UINT32 u4SmphrNum)
{
#ifdef _WINDOWS
    if ( u4SmphrNum < MTK_SYS_SEMAPHORE_NUM )
    {
        InitializeCriticalSection( &(CriticalSection[u4SmphrNum]) );
    }
#else
    if ( u4SmphrNum < MTK_SYS_SEMAPHORE_NUM )
    {
        pthread_mutex_init(&bee_sys_smphr[u4SmphrNum], NULL);
    }
#endif
}


//*****************************************************************************
// MTK_Sys_Delete_Smphr : Delete semaphore
//
// PARAMETER : u4SmphrNum [IN] - semaphore number to be deleted
//
// RETURN : void

void MTK_Sys_Delete_Smphr (MTK_UINT32 u4SmphrNum)
{
#ifdef _WINDOWS
    if ( u4SmphrNum < MTK_SYS_SEMAPHORE_NUM )
    {
        DeleteCriticalSection( &(CriticalSection[u4SmphrNum]) );
    }
#else
    if ( u4SmphrNum < MTK_SYS_SEMAPHORE_NUM )
    {
        pthread_mutex_destroy(&bee_sys_smphr[u4SmphrNum]);
    }
#endif
}


//*****************************************************************************
// MTK_Sys_Reserve_Smphr : Reserve semaphore
//
// PARAMETER : u4SmphrNum [IN] - semaphore number to reserve
//
// RETURN : void

void MTK_Sys_Reserve_Smphr (MTK_UINT32 u4SmphrNum)
{
#ifdef _WINDOWS
    if (u4SmphrNum < MTK_SYS_SEMAPHORE_NUM)
    {
        EnterCriticalSection( &(CriticalSection[u4SmphrNum]) );
    }
#else
    if (u4SmphrNum < MTK_SYS_SEMAPHORE_NUM)
    {
        pthread_mutex_lock(&bee_sys_smphr[u4SmphrNum]);
    }
#endif
}


//*****************************************************************************
// MTK_Sys_Release_Smphr : Release semaphore
//
// PARAMETER : u4SmphrNum [IN] - semaphore number to release
//
// RETURN : void

void MTK_Sys_Release_Smphr (MTK_UINT32 u4SmphrNum)
{
#ifdef _WINDOWS
    if (u4SmphrNum < MTK_SYS_SEMAPHORE_NUM)
    {
        LeaveCriticalSection( &(CriticalSection[u4SmphrNum]) );
    }
#else
    if (u4SmphrNum < MTK_SYS_SEMAPHORE_NUM)
    {
        pthread_mutex_unlock(&bee_sys_smphr[u4SmphrNum]);
    }
#endif
}


//*****************************************************************************
// Memory Functions
//*****************************************************************************

//*****************************************************************************
// MTK_Sys_Memory_Alloc : Allocate a block of memory
//
// PARAMETER : u4Size [IN] - size of memory (bytes) to be allocated
//
// RETURN : On success, return the pointer to the allocated memory
//          If fail, return NULL

void* MTK_Sys_Memory_Alloc (MTK_UINT32 u4Size)
{
    //void *p;
    //FILE *fp = fopen("malloc.txt", "a");
    //p = malloc(u4Size);
    //fprintf(fp, "MTK_Sys_Memory_Alloc: %u, %u\n", p, u4Size);
    //fclose(fp);

    //return p;


    #ifdef BEE_HEAP_DEBUG
    if ( _i4HeapIdx < 0 || _i4HeapIdx >= 65536 )
    {
        FILE *fp = fopen("error.txt", "a");
        fprintf(fp, "_i4HeapIdx : %d", _i4HeapIdx);
        fclose(fp);

        _i4HeapIdx = 0;
    }
    else
    {
        _HeapSize[_i4HeapIdx] = u4Size;
        _i4HeapIdx++;
        _u4HeapTotalSize += u4Size;

        if ( _u4HeapTotalSize > _u4MaxHeapSize )
        {
            _u4MaxHeapSize = _u4HeapTotalSize;
            {
                FILE *fp = fopen("heapsize.txt", "a");
                fprintf(fp, "+ , %d , %u , %u\n", u4Size, _u4HeapTotalSize, _u4MaxHeapSize);
                fclose(fp);
            }
        }
    }

    #endif

    return malloc(u4Size);
}


//*****************************************************************************
// MTK_Sys_Memory_Free : Deallocate memory previosly allocated by MTK_Sys_Memory_Alloc()
//
// PARAMETER : pMemory [IN] - pointer to memory to be freed
//
// RETURN : void

void MTK_Sys_Memory_Free (void *pMemory)
{
    //FILE *fp = fopen("free.txt", "a");
    //fprintf(fp, "MTK_Sys_Memory_Free: %u\n", pMemory);
    //fclose(fp);

   #ifdef BEE_HEAP_DEBUG
   if ( _i4HeapIdx <= 0 || _i4HeapIdx > 65536 )
   {
       FILE *fp = fopen("error.txt", "a");
       fprintf(fp, "_i4HeapIdx : %d", _i4HeapIdx);
       fclose(fp);
   
       _i4HeapIdx = 0;
   }
   else
   {
       _i4HeapIdx--;
       _u4HeapTotalSize -= _HeapSize[_i4HeapIdx];
   
       #if 0
       {
           FILE *fp = fopen("heapsize.txt", "a");
           fprintf(fp, "- , %d , %u , %u\n", _HeapSize[_i4HeapIdx], _u4HeapTotalSize, _u4MaxHeapSize);
           fclose(fp);
       }
       #endif
   
       _HeapSize[_i4HeapIdx] = 0;
   }
   #endif

    free(pMemory);
}


//*****************************************************************************
// File Functions
//*****************************************************************************

//*****************************************************************************
// MTK_Sys_File_Open : Open a file
//
// PARAMETER : szFileName [IN] - name of the file to be opened 
//             i4Mode     [IN] - file access mode (read / write / read + write)
//                               0 -- open file for reading (r)
//                               1 -- create file for writing,
//                                    discard previous contents if any (w)
//                               2 -- open or create file for writing at end of file (a)
//                               3 -- open file for reading and writing (r+)
//                               4 -- create file for reading and writing,
//                                    discard previous contents if any (w+)
//                               5 -- open or create file for reading and writing at end of file (a+)
//
// NOTE : For system which treats binary mode and text mode differently,
//        such as Windows / DOS, please make sure to open file in BINARY mode
//
// RETURN : On success, return the file handle
//          If fail, return 0

MTK_FILE MTK_Sys_File_Open (const char *szFileName, MTK_INT32 i4Mode)
{
    FILE *fp;
    char szMode[4];

    // For system which treats binary mode and text mode differently,
    // such as Windows / DOS, please make sure to open file in BINARY mode

    switch (i4Mode)
    {
    case MTK_FS_READ:       // 0
        sprintf(szMode, "rb");
        break;
    case MTK_FS_WRITE:      // 1
        sprintf(szMode, "wb");
        break;
    case MTK_FS_APPEND:     // 2
        sprintf(szMode, "ab");
        break;
    case MTK_FS_RW:         // 3
        sprintf(szMode, "r+b");
        break;
    case MTK_FS_RW_DISCARD: // 4
        sprintf(szMode, "w+b");
        break;
    case MTK_FS_RW_APPEND:  // 5
        sprintf(szMode, "a+b");
        break;
    default:
        return 0;
    }
    
    fp = fopen(szFileName, szMode);

    if (fp != NULL)
    {
        return (MTK_FILE)fp;
    }

    return 0;
}


//*****************************************************************************
// MTK_Sys_File_Close : Close a file
//
// PARAMETER : hFile [IN] - handle of file to be closed
//
// RETURN : void

void MTK_Sys_File_Close (MTK_FILE hFile)
{
    fclose((FILE *)hFile);
}


//*****************************************************************************
// MTK_Sys_File_Read : Read a block of data from file
//
// PARAMETER : hFile    [IN]  - handle of file
//             DstBuf   [OUT] - pointer to data buffer to be read
//             u4Length [IN]  - number of bytes to read
//
// RETURN : Number of bytes read

MTK_UINT32 MTK_Sys_File_Read (MTK_FILE hFile, void *DstBuf, MTK_UINT32 u4Length)
{
    if (hFile != 0)
    {
        return (MTK_UINT32)fread(DstBuf, 1, u4Length, (FILE *)hFile);
    }

    return 0;
}


//*****************************************************************************
// MTK_Sys_File_Write : Write a block of data from file
//
// PARAMETER : hFile    [IN] - handle of file
//             SrcBuf   [IN] - pointer to data buffer to be written
//             u4Length [IN] - number of bytes to write
//
// RETURN : Number of bytes written

MTK_UINT32 MTK_Sys_File_Write (MTK_FILE hFile, void *SrcBuf, MTK_UINT32 u4Length)
{
    if (hFile != 0)
    {
        return (MTK_UINT32)fwrite(SrcBuf, 1, u4Length, (FILE *)hFile);
    }

    return 0;
}


//*****************************************************************************
// MTK_Sys_File_Seek : Set the position indicator associated with file handle 
//                     to a new position defined by adding offset to a reference
//                     position specified by origin
//
// PARAMETER : hFile    [IN] - handle of file
//             u4OffSet [IN] - number of bytes to offset from origin
//             u4Origin [IN] - position from where offset is added
//                             0 -- seek from beginning of file
//                             1 -- seek from current position
//                             2 -- seek from end of file
//
// RETURN : On success, return a zero value
//          Otherwise, return a non-zero value

MTK_INT32 MTK_Sys_File_Seek (MTK_FILE hFile, MTK_UINT32 u4OffSet, MTK_UINT32 u4Origin)
{
    return fseek((FILE *)hFile, u4OffSet, u4Origin);
}


//*****************************************************************************
// Time Functions
//*****************************************************************************

//*****************************************************************************
// MTK_Sys_Time : Get the current system time
//
// PARAMETER : pUTCTime [OUT] - UTC time
//
// RETURN : Success (MTK_TRUE) or Fail (MTK_FAIL)

MTK_BOOL MTK_Sys_Time (MTK_TIME *pUTCTime)
{
#ifdef _WINDOWS
    SYSTEMTIME st;

    GetSystemTime(&st);
    pUTCTime->Year = st.wYear;
    pUTCTime->Month = st.wMonth;
    pUTCTime->Day = st.wDay;
    pUTCTime->Hour = st.wHour;
    pUTCTime->Min = st.wMinute;
    pUTCTime->Sec = st.wSecond;
    pUTCTime->Msec = st.wMilliseconds;

    return MTK_TRUE;

#else
    time_t t;
    struct tm *pGMTime;

    if (pUTCTime == NULL)
    {
        return MTK_FALSE;
    }

    t = time(NULL);
    pGMTime = gmtime(&t);

    pUTCTime->Year = (MTK_UINT16)(pGMTime->tm_year + 1900); // tm_year counts from 1900
    pUTCTime->Month = (MTK_UINT16)(pGMTime->tm_mon + 1); // range of tm_month is 0-11
    pUTCTime->Day = (MTK_UINT16)pGMTime->tm_mday;
    pUTCTime->Hour = (MTK_UINT16)pGMTime->tm_hour;
    pUTCTime->Min = (MTK_UINT16)pGMTime->tm_min;
    pUTCTime->Sec = (MTK_UINT16)pGMTime->tm_sec;
    pUTCTime->Msec = 0; // no millisecond field in struct tm

    return MTK_TRUE;        

#endif
}
