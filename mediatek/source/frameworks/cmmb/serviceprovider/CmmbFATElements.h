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

/*++ FAT Element Structure Definition Header File

Copyright (C) 2007-2010 Innofidei, Inc.

    Module Name: FATElements.hpp

Abstract:

    FAT Element Structure Definition Header File
--*/

#ifndef H_FAT_ELEMENT_INCLUDED_H
#define H_FAT_ELEMENT_INCLUDED_H

#ifdef __cplusplus
extern "C" {
#endif

//*******************************************************************************************
// FAT XML Schema
//*******************************************************************************************
#include <stdlib.h>
#include <string.h>
#include "CmmbSPCommon.h"


static inline char* InnoStrNDup (const char* s, int n)
{
    if (NULL == s || 0 == n) return NULL;
    char* dst = (char*)malloc (n+2);
    if (NULL == dst) return NULL;

    memcpy (dst, s, n);
    dst[n]   = '\0';
    dst[n+1] = '\0';

    return dst;
}

static inline void InnoStrFree (void* s)
{
    free (s);
}

struct LifePeriod
{
    void Reset () { time_unit_indic = 0; show_time = 0; }

    const UINT32& GetTimeUnit () const { return time_unit_indic; }
    const UINT32& GetShowTime () const { return show_time; }

    void SetTimeUnit (UINT32 t) { time_unit_indic = t; }
    void SetShowTime (UINT32 t) { show_time = t; }

private:
    //0: hour
    //1: day
    //2: week
    //3: month
    UINT32 time_unit_indic;

    //0: un limited
    //1-60: 1-60 time unit
    //61-63: reserved
    UINT32 show_time;
};
struct SegmentationInfoType
{
    void Reset ()
    {
        slice_length = 0;
        A_block_count= 0;
        B_block_count= 0;
        total_block_count = 0;
        slice_count_for_A = 0;
        slice_count_for_B = 0;
        fec_slice_count_for_A = 0;
        fec_slice_count_for_B = 0;
    }

    UINT32 slice_length ;
    UINT32 A_block_count;
    UINT32 B_block_count;
    UINT32 total_block_count;
    UINT16 slice_count_for_A;
    UINT16 slice_count_for_B;
    UINT16 fec_slice_count_for_A;
    UINT16 fec_slice_count_for_B;
};
struct FECInfoType
{
    void Reset () { algorithm = 0xFF; }

    const UINT8& GetAlgorithm() const    { return algorithm; }
    void SetAlgorithm (UINT8 _algorithm) { algorithm = _algorithm; }

private:
    UINT8    algorithm; //0: LDGC
    UINT8    reserved1;
    UINT16    reserved2;
};
struct TransferInfoType
{
public:
    void Reset ()
    {
        DirID = 0xFFFF;       
        file_name_len = 0;
        file_name = NULL;
        life_period.Reset ();
    }
    void Init(int _DirID, const char* fname, int fnamelen, UINT32 tu = 0, UINT32 su = 0)
    {
        SetDirID((UINT16)_DirID);
        SetFileName(fname, fnamelen);

        SetTimeUnit (tu);
        SetShowTime (su);
    }
    void Destroy ()
    {
        if (NULL != file_name)
        {
            InnoStrFree (file_name);
            file_name = NULL;
        }
    }

    const UINT16& GetDirID ()    const { return DirID;     }
    const char* GetFileName () const { return file_name; }
    const UINT16& GetFileNameLen () const { return file_name_len; }
    const UINT32& GetTimeUnit () const { return life_period.GetTimeUnit(); }
    const UINT32& GetShowTime () const { return life_period.GetShowTime(); }

    void SetTimeUnit (UINT32 t)        { life_period.SetTimeUnit (t); }
    void SetShowTime (UINT32 t)        { life_period.SetShowTime (t); }

    void SetDirID  (const UINT16& dID) { DirID = dID; }
    void SetFileName (const char* f_name, int n) 
    {
        if ((NULL != f_name) && (0 != n))
        {
            file_name = InnoStrNDup ((const char*)f_name, n);
            file_name_len = (UINT16)n;
        }
    }

    TransferInfoType& operator = (const TransferInfoType& rhs)
    {
        if (&rhs != this)
        {
            SetDirID (rhs.GetDirID());
            SetFileName (rhs.GetFileName(), rhs.GetFileNameLen());
            life_period.SetTimeUnit (rhs.GetTimeUnit());
            life_period.SetShowTime (rhs.GetShowTime());
        }

        return *this;
    }

private:
    UINT16        DirID;       //in accordance with FDI::DirID 
    UINT16        file_name_len;
    char*       file_name;
    LifePeriod  life_period;
};
struct ContentInfoType
{
public:
    void Reset ()
    {
        container_file = 0;
        file_encoding  = 0;
        file_length    = 0;
        file_encoding_length = 0;
        MIME_type_len  = 0;
        MIME_type = NULL;

        content_lingual_special = NULL;
        private_info = NULL;
    }
    void Init(UINT32  fl, UINT8  cf, UINT8  fe, UINT32  fel, char* mime, int mimelen, char* l)
    {
        SetContainerFile (cf);
        SetFileLength (fl);
        SetFileEncoding (fe);
        SetFileEncodingLength (fel);
        SetMIME_Type (mime, mimelen);
        SetLang (l);
    }
    void Destroy ()
    {
        if (NULL != MIME_type)
        {
            InnoStrFree (MIME_type);
            MIME_type = NULL;
        }
    }
    const UINT8& GetContainerFile (void) const { return container_file; }
    const UINT32& GetFileLength (void) const    { return file_length; }
    const UINT8& GetFileEncoding (void) const  { return file_encoding; }
    const UINT32& GetFileEncodingLength (void) const { return file_encoding_length; }
    const char* GetMIME_Type (void) const          { return MIME_type; }
    const UINT32& GetMIME_TypeLen (void) const       { return MIME_type_len; }
    const char* GetLang (void) const               { return lang; }

    void  SetContainerFile (const UINT8& flg)  { container_file = flg; }
    void  SetFileLength (const UINT32& fl)      { file_length    = fl;  }
    void  SetFileEncoding (const UINT8& fe)    { file_encoding  = fe;  }
    void  SetFileEncodingLength (const UINT32& fel)      { file_encoding_length = fel; }
    void  SetLang (const char* l) { lang[0] = l[0], lang[1] = l[1], lang[2] = l[2], lang[3] = '\0'; }    
    void  SetMIME_Type (const char* type, int n) 
    {
        if ((NULL != type) && (0 != n))
        {
            MIME_type = InnoStrNDup (type, n); 
            MIME_type_len = n;
        }
    }
    ContentInfoType& operator = (const ContentInfoType& rhs)
    {
        if (&rhs != this)
        {
            container_file = rhs.GetContainerFile ();
            file_encoding  = rhs.GetFileEncoding ();
            file_length    = rhs.GetFileLength ();
            file_encoding_length = rhs.GetFileEncodingLength ();

            SetMIME_Type (rhs.GetMIME_Type(), rhs.GetMIME_TypeLen());
            SetLang (rhs.GetLang());

            ///TODO: Copy content description information
        }

        return *this;
    }
private:
    UINT8  container_file;//0 is not a container, 1 is not
    UINT8  file_encoding; //file encoding
    UINT32  file_length;   //plain file length
    UINT32  file_encoding_length; //encoding file length
    UINT32  MIME_type_len;
    char* MIME_type;     //MIME, IETF RFC 2046

    char  lang[4];       //language information

    // content description information
    struct inner_s
    {
        UINT8 title[128];
        UINT8 keyword[128]; 
        UINT8 digest[1024];
    }* content_lingual_special;
    void* private_info;
};
struct ProtectionInfoType
{
public:
    //Protection Level
    enum PROTECTION_LEVEL_E
    {
        PROTECTION_LEVEL_0 = 0, //0£ºno copyright, freely access
        PROTECTION_LEVEL_1,     //1£ºlow level copyright, no editing
        PROTECTION_LEVEL_2,     //2£ºhigh level copyright, no editing or transmission

        //Others: reserved

        PROTECTION_LEVEL_Last
    };

public:
    void Reset ()
    {
        protection_level      = 0xFFFFFFFF;
        level_description_len = 0;
        level_description     = NULL;
    }

    void Init (UINT32 level, const char* level_desc, int desc_len)
    {
        SetProtectionLevel (level);
        SetLevelDesc (level_desc, desc_len);
    }
    void Destroy (void)
    {
        if (NULL != level_description)
        {
            InnoStrFree (level_description);
            level_description = NULL;
        }
    }
    const UINT32& GetProtectionLevel () const { return protection_level;  }
    const char* GetLevelDesc () const       { return level_description; }
    const UINT32& GetLevelDescLen () const    { return level_description_len; }
    void  SetProtectionLevel (const UINT32& flg) { protection_level = flg; }
    void  SetLevelDesc (const char* levelDisc, int n)  
    {
        if ((NULL != levelDisc) && (0 != n))
        {
            level_description = InnoStrNDup (levelDisc, n); 
            level_description_len = n;
        }
    }

    ProtectionInfoType& operator = (const ProtectionInfoType& rhs)
    {
        if (&rhs != this)
        {
            SetProtectionLevel (rhs.GetProtectionLevel());
            SetLevelDesc (rhs.GetLevelDesc(), rhs.GetLevelDescLen());
        }

        return *this;
    }

private:
    UINT32  protection_level;
    UINT32  level_description_len;
    char* level_description;
};
struct  FDIType
{
public:
    void Reset ()
    {
        DirName    = NULL;
        DirNameLen = 0;
        DirID      = 0xFFFF;
        DirLevel   = 0xFF;
    }
    void Init (int _DirID, int _DirLevel, char* _DirName, int _DirNameLen)
    {
        SetDirID ((UINT16)_DirID);
        SetDirLevel ((UINT8)_DirLevel);
        SetDirName (_DirName, _DirNameLen);
    }
    void Destroy ()
    {
        if (NULL != DirName)
        {
            InnoStrFree (DirName);
            DirName = 0;
        }
    }
    const char* GetDirName () const       { return DirName;  }
    const UINT16& GetDirNameLen () const    { return DirNameLen; }
    const UINT16& GetDirID () const         { return DirID;    }
    const UINT8& GetDirLevel () const      { return DirLevel; }

    void SetDirID (const UINT16& dID)           { DirID = dID;   }
    void SetDirLevel (const UINT8& dLevel)     { DirLevel = dLevel; }
    void SetDirName (const char* dName, int n) 
    { 
        if ((NULL != dName) && (0 != n))
        {
            DirName = InnoStrNDup (dName, n); 
            DirNameLen = (UINT16)n;
        }
    }

    FDIType& operator = (const FDIType& rhs)
    {
        if (&rhs != this)
        {
            SetDirID (rhs.GetDirID());
            SetDirLevel (rhs.GetDirLevel());
            SetDirName (rhs.GetDirName(), rhs.GetDirNameLen());
        }

        return *this;
    }

private:
    char*  DirName;
    UINT16   DirNameLen;
    UINT16   DirID;
    UINT8   DirLevel;
    UINT8   Reserved;
} ;
struct  FAIType
{
    void Reset ()
    {
        TransferInfo.Reset ();
        ContentInfo.Reset ();
        SegmentationInfo.Reset();
        FECInfo.Reset ();
        ProtectionInfo.Reset ();

        Resource_ID = 0xFFFF;
        UpdateIndex = 0;
    }
    void Destroy ()
    {
        TransferInfo.Destroy();
        ContentInfo.Destroy();
        ProtectionInfo.Destroy();
    }
    FAIType & operator = (const FAIType& rhs)
    {
        if (&rhs != this)
        {
            TransferInfo    = rhs.TransferInfo;
            ContentInfo     = rhs.ContentInfo;
            SegmentationInfo= rhs.SegmentationInfo;
            FECInfo         = rhs.FECInfo;
            ProtectionInfo  = rhs.ProtectionInfo;
            Resource_ID     = rhs.Resource_ID;
            UpdateIndex     = rhs.UpdateIndex;
        }

        return *this;
    }

    TransferInfoType     TransferInfo;     
    ContentInfoType      ContentInfo;      
    SegmentationInfoType SegmentationInfo; 
    FECInfoType          FECInfo;          
    ProtectionInfoType   ProtectionInfo;   
    UINT16                 Resource_ID;      
    UINT8                 UpdateIndex;      
    UINT8                 Reserved;
};

#ifdef __cplusplus
}
#endif

#endif //H_FAT_ELEMENT_INCLUDED_H

//End of File
