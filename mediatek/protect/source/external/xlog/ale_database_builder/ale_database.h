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

#if !defined(__ALE_DATABASE_H__)
#define __ALE_DATABASE_H__

#include <string>
#include <list>

#define ALE_ELF_SECTION ".ale_database"

#define ALE_HASH_LENGTH 32

class CAleMessageRec 
{
public:
    CAleMessageRec(const char *_hash, const char *_layer, const char *_level, const char *_tag, const char *_fmt, const char *_fmt_args, const char *filename);
  
    const std::string& get_hash() const
    { 
        return hash;
    }

    const std::string& get_layer() const 
    {
	return layer;
    }

    const std::string& get_level() const
    {
	return level;
    }

    const std::string& get_tag() const 
    {
        return tag;
    }

    const std::string& get_format() const
    {
        return fmt;
    }

    const std::string& get_args() const
    {
        return fmt_args;
    }

    const std::string& get_filename() const
    {
        return filename;
    }

    bool operator<(const CAleMessageRec& rec)
    {
        return hash < rec.hash;
    }

private:
    std::string hash;
    std::string layer;
    std::string level;
    std::string tag;
    std::string fmt;
    std::string fmt_args;
    std::string filename;
};

class CAleDatabase
{
public:
    CAleDatabase();
    ~CAleDatabase();

    bool add_entry(const char *hash, const char *_layer, const char *_level, const char *tag_hex,  const char *fmt_hex, const char *fmt_args, const char *filename);

    bool write(const char *filename);

    const char *get_last_error();

    void dump(void);

private:
    std::list<CAleMessageRec> mapping;

    std::string last_error_message;

    void set_error(const char *fmt, ...);
};

bool ale_elf_readfile(CAleDatabase *ale_db, const char *infile);

#endif /* __ALE_DATABASE__ */
