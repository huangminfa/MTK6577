/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#ifndef H_DEMUXFILE_SERVICE_INCLUDED_H
#define H_DEMUXFILE_SERVICE_INCLUDED_H

#ifdef __cplusplus
extern "C" {
#endif

#include<string.h>
#include<stdio.h>
#include "CmmbFATElements.h"
#include "utils/Log.h"

/********************/
/* service provider log  */
/********************/
#define debughelper() SP_LOGE("%s,%d", __func__, __LINE__)

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "CMMB_SP"
#define INNOADR_ENABLE_LOGS

#ifdef INNOADR_ENABLE_LOGS
#define SP_LOGE(fmt, args...) LOGE( "cmmbSP - %s(): " fmt "\n", __FUNCTION__, ##args)
#define SP_LOGW(fmt, args...) LOGW( "cmmbSP - %s(): " fmt "\n", __FUNCTION__, ##args)
#define SP_LOGI(fmt, args...) LOGI( "cmmbSP - %s(): " fmt "\n", __FUNCTION__, ##args)
#define SP_LOGD(fmt, args...) LOGD( "cmmbSP - %s(): " fmt "\n", __FUNCTION__, ##args)
#else
#define SP_LOGE(fmt, args...) 
#define SP_LOGW(fmt, args...)
#define SP_LOGI(fmt, args...)
#define SP_LOGD(fmt, args...)
#endif

#define ENABLE_LDGC_DEC
#define MAX_FAT_PIEC_NUM      (256)      /*Maximum FAT Slice Number*/
#define MAX_RES_DIR_NUM       (65536>>4) /*Maximum Directory Number is 65536, here just use 4096, should be enough for sake of RAM*/
#define MAX_PRE_RECVED_SLICE  (2048)     /*<SPEC> Recommended Value*/

struct CFileServiceDemuxer;
struct CFatPiece
{
    UINT16  id;
    UINT32 seq    :8; 
    UINT32 updSeq :5;
    UINT32 code   :3;
    UINT32 lastSeq:8;
    UINT32 len    :12;

    void ParseHeader(UINT8*& rlpIn);
};

struct  CFilePiece
{
    UINT16  id;
    UINT32 blockSeq:10;
    UINT32 pieceSeq:14;
    UINT32 updSeq  :4;
    UINT32 reserved:4;

    void ParseHeader(UINT8*& rlpIn);
};

// For FAT/FILE piece receiving process
struct CFSRecv
{
public:
    CFSRecv ()
    {
        Reset();
    }
    ~CFSRecv ()
    {
        Remove ();
    }
    void Remove ()
    {
        if (null != m_pData)
        {
            free (m_pData);
        }
        Reset ();
    }
    void Reset(void)
    {
        m_Ready = false;
        m_Sid   = -1;
        m_Len   = 0;
        m_pData = null;
    }
    bool New (INT32 bid, INT32 sid, INT32 sl)
    {
        if (m_Sid < sid)//if current seq of the FAT slice  does not exist, save it
        {
            m_Ready = false;
            m_Sid   = sid;
            m_Bid   = (UINT16)bid;
            m_Len   = (UINT16)sl;

            return true;
        }
        return true;
    }
    bool Add (UINT8* dest, INT32 len)
    {
        if (!m_Ready)
        {
            m_pData = (UINT8*)malloc (len);
            if (null == m_pData)
            {
                SP_LOGE("[FileParser]: [CFSRecv::Add]: NOT Enough Memory! ");
                return false;
            }

            memcpy(m_pData, dest, len);

            m_Ready = true;
            m_Len   = (UINT16)len;
	    // SP_LOGE("[FileParser]: [CFSRecv::Add]: Add peice len=%d!",m_Len);
            return true;
        }
        return false;
    }
    const UINT8* GetData () const 
    {
        return m_pData;
    }
    bool Update (UINT8* dest)
    {
        m_pData = (UINT8*)malloc (m_Len);
        if (null == m_pData)
        {
            //SP_LOGE("[CFSRecv::Add]: NOT Enough Memory! ");
            return false;
        }

        memcpy(m_pData, dest, m_Len);
        m_Ready = true;

        return true;
    }

    const bool&  Ready(void)   const { return m_Ready; }
    const UINT16&  Length (void) const { return m_Len;   }
    const UINT16&  GetBid (void) const { return m_Bid;   }
    void         Unready(void)       { m_Ready = false;}

private:
    bool  m_Ready;
    INT32   m_Sid;
    UINT16  m_Bid;
    UINT16  m_Len;
    UINT8* m_pData;
};

struct CFBRecv
{
public:
    CFBRecv ()
    {
        Reset ();
    }
    ~CFBRecv ()
    {
        RemoveAllPieces ();
    }
    void Reset (void)
    {
        m_Bid          = -1;
        m_nPieceN      = 0;
        m_nRecvPN      = 0;
        m_pstPieces    = null;

        m_nFecPN       = 0;
        m_nRecvFecPN   = 0;

        m_Ready        = false;
        m_Output       = false;
        m_LDGCDone     = false;
    }
    void RemoveAllPieces (void)
    {
        if (null != m_pstPieces)
        {
            for (INT32 i = 0; i < m_nPieceN+m_nFecPN; ++i)
            {
                m_pstPieces[i].Remove();
            }
            free(m_pstPieces);
        }
        Reset ();
    }
    bool New (INT32 bid, INT32 sl, INT32 sc, INT32 sc_fec)
    {
        if (m_Bid != bid)
        {
            //allocate pieces for this block
            m_Bid     = bid;
            m_nPieceN = (UINT16)sc;
            m_nFecPN  = (UINT16)sc_fec;
            m_pstPieces = (CFSRecv*)malloc ((m_nPieceN+m_nFecPN)*(sizeof *m_pstPieces));
            for (INT32 i = 0; i < m_nPieceN+m_nFecPN; ++ i)
            {
                m_pstPieces[i].Reset();
                m_pstPieces[i].New(bid, i, sl);
            }
            return true;
        }
        return false;
    }
    bool Add (INT32 sid, UINT8* dest, INT32 len)
    {
        bool ret = m_pstPieces[sid].Add (dest, len);
        if (ret)
        {
            if (sid < m_nPieceN)
            {
                ++ m_nRecvPN;
            }
            else
            {
                ++ m_nRecvFecPN;
            }
        }
        return ret;
    }   
    bool Ready ()
    {
        if (m_Ready) return true;
        else
        {
            for (INT32 i = 0; i < m_nPieceN; ++ i)
            {
                if(!m_pstPieces[i].Ready())
                {
                    return false;
                }
            }


            SetReady();
            return true;
        }
    }

    bool Integrate (CFileServiceDemuxer* demuxer);

    INT32 Length (void) const
    {
        INT32 len = 0;
        for (INT32 i = 0; i < m_nPieceN; ++ i)
        {
            len += m_pstPieces[i].Length();
        }
        return len;
    }
    const UINT16& GetK () const
    {
        return m_nPieceN;
    }
    UINT16 GetM (void) const
    {
        UINT16 len = m_nPieceN+m_nFecPN;
        return len; 
    }
    const UINT8* GetExtraBuffer (void) const
    { 
        return m_pExtraBuf;
    }
    void AllocateExtraBuffer (void)
    {
        INT32 len = 0;
        for (INT32 i = 0; i < m_nPieceN+m_nFecPN; ++ i)
        {
            len += m_pstPieces[i].Length();
        }
        m_pExtraBuf = (UINT8*)malloc (len);
    }
    void ReleaseExtraBuffer (void) const
    {
        free(m_pExtraBuf);
    }
    CFSRecv* GetPiece (INT32 sid)
    {
        return &(m_pstPieces[sid]);
    }

    bool Output (void)   { return m_Output; }
    void SetOutput(void) { m_Output= true;  }
    INT32  GetBid () const { return m_Bid;   }

    bool IsLDGCDone () const { return m_LDGCDone; }

private:
    void SetReady(void)  { m_Ready = true;  m_Output = false; }

private:
    bool  m_Ready;
    bool  m_Output;
    bool  m_LDGCDone;
    INT32   m_Bid;
    UINT16  m_nPieceN;
    UINT16  m_nRecvPN;
    UINT16  m_nFecPN;
    UINT16  m_nRecvFecPN;
    UINT8* m_pExtraBuf;

    CFSRecv* m_pstPieces;
};

struct CFRes
{
public:
    CFRes ()
    {
        Reset ();
    }
    void Reset ()
    {
        m_iFid        = -1;
        m_iLength     = 0;
        m_iBlockCount = 0;
        m_pBlocks     = null;
    }
    void RemoveAll()
    {
        for (INT32 i = 0; i < m_iBlockCount; ++i)
        {
            m_pBlocks[i].RemoveAllPieces ();
        }
        free(m_pBlocks);

        Reset ();
    }
    bool New (INT32 fid, INT32 length, INT32 sl, INT32 bc_a, INT32 bc_b, INT32 sc_a, INT32 sc_b, INT32 sc_fec_a, INT32 sc_fec_b)
    {
        if (fid != m_iFid)
        {
            m_iLength = length;
            m_iFid    = fid;
            m_iBlockCount = bc_a+bc_b;
            if (m_iBlockCount > 0)
            {
                INT32 i;
                m_pBlocks = (CFBRecv*)malloc (sizeof(*m_pBlocks)*m_iBlockCount);            
                for (i = 0; i < bc_a; ++ i)
                {
                    m_pBlocks[i].Reset ();
                    m_pBlocks[i].New (i, sl, sc_a, sc_fec_a);
                }
                for (; i < m_iBlockCount; ++ i)
                {
                    m_pBlocks[i].Reset ();
                    m_pBlocks[i].New (i, sl, sc_b, sc_fec_b);
                }
            
                return true;
            }
            else
            {
                SP_LOGE("[NewErr]: %d %d %d", fid, length, m_iBlockCount);
                return false;
            }
        }
        return false; 
    }
    void Update (INT32 blockSeq, INT32 sliceSeq, UINT8* dest, INT32 length)
    {        
        m_pBlocks[blockSeq].Add (sliceSeq, dest, length);
    }
    bool Ready ()
    {
        for (INT32 i = 0; i < m_iBlockCount; ++i)
        {
            if (!m_pBlocks[i].Ready())
            {
                return false;
            }
        }
        return true;
    }
    CFBRecv* GetBlock (INT32 blockSeq)
    {
        if (blockSeq < m_iBlockCount)
        {
            return &(m_pBlocks[blockSeq]);
        }
        else
        {
            return null;
        }
    }
    const INT32& GetFileLength () const { return m_iLength;     }
    const INT32& GetBlockCount () const { return m_iBlockCount; }

private:
    INT32   m_iFid;         //File Resource ID
    INT32   m_iLength;      //File Length
    INT32   m_iBlockCount;  //MAX: 2048*2[A+B]
    CFBRecv* m_pBlocks; 
};

struct FileInfo_t
{
    void Reset ()
    {
        m_pstNext = null;
        m_pstPrev = null;
        m_Ready   = false;
        m_nFid    = 0;
        m_aHandle = null;

        m_stFAI.Reset ();
    }

    void Destroy ()
    {
        m_stFAI.Destroy ();
        m_stFile.RemoveAll();
    }

    CFRes& GetFileResource () { return m_stFile; }
    bool New (INT32 length, INT32 sl, INT32 bc_a, INT32 bc_b, INT32 sc_a, INT32 sc_b, INT32 sc_fec_a, INT32 sc_fec_b)
    {
        return m_stFile.New (m_nFid, length, sl, bc_a, bc_b, sc_a, sc_b, sc_fec_a, sc_fec_b);
    }

    void  SetFAI (const FAIType& stFAI) { m_stFAI = stFAI; }
    const FAIType& GetFAI (void) const  { return m_stFAI;  }

    FileInfo_t *m_pstNext;
    FileInfo_t *m_pstPrev;
    bool        m_Ready;
    UINT16        m_nFid;

    //FAT Information
    FAIType     m_stFAI;

    CFRes       m_stFile;
    FILE*         m_aHandle;		
};

struct CFileServiceDemuxer 
{

public:
    CFileServiceDemuxer ();
    virtual ~CFileServiceDemuxer();

    //Output Options, true for a whole file output, false for incremental piece file output
    void SetOutputOption (bool opt = true) 
    {
        m_bUseWhole = opt;
    }

    //Downloaded File Version Control
    bool IsFATMajorVersionChanged(void)
    {
        return m_iLastFATMaxVer != m_iPrevFATMaxVer;
    }		
    bool IsFATMinorVersionChanged(void)
    {
        return m_iLastFATMinVer != m_iPrevFATMinVer;
    }

    //++
    // FAI and FDI Type Information 
    //++
    void OnFileInfo   (const FAIType &);
    void OnFolderInfo (const FDIType &);

    //++
    // Inspectors
    //++
    const INT32& GetResourceNumber (void) const { return m_nFileTotalN;  }
    const INT32& GetResourceRecved (void) const { return m_nRecvedFileN; }
    CFRes& GetFResById (INT32 iResId)
    {
        return InnerGetQ (iResId)->GetFileResource();
    }
    const char* GetDirName (INT32 iResId) 
    {
        return m_astFDI[GetFileTransferInfo(iResId).GetDirID()].GetDirName();
    }
    const char* GetFileName (INT32 iResId)
    {
        return (const char*) GetFileTransferInfo(iResId).GetFileName();
    }
    const FAIType& GetFAIType  (INT32 iResId)
    {
        return InnerGetQ (iResId)->GetFAI();
    }
    const TransferInfoType& GetFileTransferInfo (INT32 iResId)
    {
        return GetFAIType(iResId).TransferInfo;
    }
    const ContentInfoType& GetFileContentInfo (INT32 iResId)
    {
        return GetFAIType(iResId).ContentInfo;
    }
    const SegmentationInfoType& GetFileSegmentationInfo (INT32 iResId)
    {
        return GetFAIType(iResId).SegmentationInfo;
    }
    UINT32 GetBlockM (UINT16 id)
    {
        return GetFileSegmentationInfo(id).A_block_count + GetFileSegmentationInfo(id).B_block_count;
    }

public:
    //++
    // Reset CDemuxer & CEsgDemuxer & CFileServiceDemuxer Status
    //--
    virtual void Reset (void);

    //++
    // Below are what the user has to implement:
    //NOTE: overload this function to get current status    
    //++
    virtual void OnFileProgress (INT32 /*iResID*/, INT32 /*iRecvdN*/, INT32 /*iN*/)   { /*User Implement*/ }
    virtual void OnAllFileComplete(INT32 /*nTotal*/) ; //NOTE: overload this function to end the download process

    //Below are what the user has to implement:
    virtual void*  Open (const char* filepath, INT32)       { return null;  /*User Implement*/ }
    virtual bool  Read (void*, UINT8*, UINT32, UINT32* ) { return false; /*User Implement*/ }
    virtual bool  Write(void*, UINT8*, UINT32, UINT32* ) { return false; /*User Implement*/ }
    virtual UINT32 Tell (void*)                         { return 0;     /*User Implement*/ }
    virtual UINT32 Seek (void*, UINT32, INT32)              { return 0;     /*User Implement*/ }
    virtual bool  Close(void* )                        { return false; /*User Implement*/ }
    virtual bool  Delete(const char* filepath) ;         
    virtual bool  Rename(const char* filenamenew, const char* filenameold) { return false; /*User Implement*/ }

    //Directory Utilities
    virtual bool OnMakePathName (char* /*out*/, const char* const /*dir*/, const char* const /*fname*/, const char* const /*ext*/);
    int CreateDir(char *subdir);

    //XPE
    virtual void OnFileModeXpe (UINT8* dest, UINT16 length);
    virtual void OnStreamModeXpe(UINT8* /*dest*/, UINT16 /*length*/);

private:
   //Large File Downloading Procedures
    bool IsLFBIFileExisted (INT32 iResId);
    bool IsLTMPFileExisted (INT32 iResId);
    void OnNewLFBIFile     (INT32 iResId);
    void OnNewLTMPFile     (INT32 iResId);
    void OnDeleteLFBIFile  (INT32 iResId);
    void OnDeleteLTMPFile  (INT32 iResId);

    bool OnLoadLFBIFile   (const CFilePiece& filPiece);
    void OnUpdateLFBIFile (const CFilePiece& filPiece);
    void OnUpdateLTMPFile (const CFilePiece& filPiece, UINT8* dest, UINT16 length);

    void OnCacheFilePiece (CFBRecv* pBlock, const CFilePiece& filPiece, UINT8* dest, UINT16 length);

    bool IsCurrFileDone   (INT32 iResId);
    bool OutputFromMemory (FileInfo_t&);

    //pData: pointer to the fat data;
    //dtLen: length of the data. Be 0 if the file is completed 
    void OnWholeFileContent (const FileInfo_t&, UINT8* pData, UINT32 dtLen);

    //FAT table related procedures
    void OnFat (UINT8* /*pointer to the fat data*/, UINT32/*length of the data*/, bool/*true if the data is GZIPed*/);
    bool OnProcessFAT (UINT8* dest, UINT16 length);
    bool QueryFATSlicesStatus (INT32 nLast);
    int    Onuncompress(UINT8* dest, UINT32* destLen, UINT8 *source, UINT32 sourceLen);
	

    //FAT has updated, change internal parameters
    void OnFATUpdatedSet (void); 

    void OnProcessFileDownload (const CFilePiece& filPiece, UINT8* dest, UINT16 length);
    void OnProcessWithSlices   (const CFilePiece& filPiece, UINT8* dest, UINT16 length);

    bool OnCheckFileDownloadComplete (INT32);

    //File refresh indterface
    bool OnClearFileData (const FileInfo_t& /*fileInfo*/);

    //Internal List
    FileInfo_t* InnerGetQ (INT32 iResId);
    FileInfo_t* InnerUpdateQ (INT32 iResId);
    void        InnerRemoveAllQ (void);

private:
    FileInfo_t* m_pstHeader;
    FileInfo_t* m_pstTail;

#if defined (__TEST_PERF__)
    bool     m_bStarted;
    UINT32     m_uStartTime;
    UINT32     m_uEndTime;
#endif //#if defined (__TEST_PERF__)

    //{{++ Begin
    //DS_Serialized Information
    //Properties
    bool     m_bUseWhole;     //Config parameters NOTE: Output the Whole data from Memory to File
    bool     m_bFATDone;      //FAT Table Done
    bool     m_bResDone;      //All Resources Done

    INT32      m_FAT2UpdateSeq; //FAT: 取值范围0～31，从0开始取值，若FAT信息发生变化，本字段循环递增加1
    INT32      m_nRecvedFileN;
    INT32      m_nFileTotalN;

    INT32      m_iLastFATMaxVer;
    INT32      m_iPrevFATMaxVer;
    INT32      m_iLastFATMinVer;
    INT32      m_iPrevFATMinVer;
    //DS_Serialized Information
    //--}} End

    //FDI information
    INT32      m_iFDICount;
    FDIType  m_astFDI[MAX_RES_DIR_NUM];

    //All FAT slices
    CFSRecv  m_astFATS[MAX_FAT_PIEC_NUM]; 

    //Pre-receiving service
#ifdef DS_PRE_RECEIVING_ENABLE
    UINT8 m_aPRFlag[MAX_PRE_RECVED_SLICE];
#define  m_PRecvN (sizeof(m_aPRFlag)/sizeof(*m_aPRFlag))
#endif //#ifdef DS_PRE_RECEIVING_ENABLE
};

#ifdef __cplusplus
}
#endif

#endif //#ifndef H_DEMUXFILE_SERVICE_INCLUDED_H

/*End of File*/

