/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef MPEG4_WRITER_H__DEF

#define MPEG4_WRITER_H__DEF

#include <stdio.h>

#include <media/stagefright/MediaWriter.h>
#include <utils/List.h>
#include <utils/threads.h>


#ifdef MPEG4Writer 
#undef MPEG4Writer 
#endif 
#define MPEG4Writer    MPEG4Writer 

namespace android {

class MPEG4Writer : public MediaWriter {
public:
    MPEG4Writer(const char * filename);
    MPEG4Writer(int fd);
    virtual status_t addSource(const sp<MediaSource> & source);
    virtual status_t start(MetaData * param = NULL);
    virtual status_t stop();
    virtual status_t pause();
    virtual bool reachedEOS();
    virtual status_t dump(int fd,const Vector<String16> & args);
    void beginBox(const char * fourcc);
    void writeInt8(int8_t x);
    void writeInt16(int16_t x);
    void writeInt32(int32_t x);
    void writeInt64(int64_t x);
    void writeCString(const char * s);
    void writeFourcc(const char * fourcc);
    void write(const void * data,size_t size);
    void endBox();
    uint32_t interleaveDuration() const;
    status_t setInterleaveDuration(uint32_t duration);
    int32_t getTimeScale() const;
    status_t setGeoData(int latitudex10000,int longitudex10000);
    void setStartTimeOffsetMs(int ms);
    int32_t getStartTimeOffsetMs() const;
protected:
    virtual ~MPEG4Writer();
private:
    MPEG4Writer(const MPEG4Writer &);

};

}  // namespace android 

#endif //MPEG4_WRITER_H__DEF