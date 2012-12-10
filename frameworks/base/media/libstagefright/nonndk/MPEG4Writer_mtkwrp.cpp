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

//#define LOG_NDEBUG 0
#define LOG_TAG "MPEG4Writer"
#include <utils/Log.h>

#include <arpa/inet.h>

#include <pthread.h>
#include <sys/prctl.h>
#include <sys/resource.h>

#include <media/stagefright/MPEG4Writer.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/MediaDebug.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaErrors.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/Utils.h>
#include <media/mediarecorder.h>
#include <cutils/properties.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include "include/ESDS.h"


using namespace android; 
#include <utils/KeyedVector.h> 
#include <utils/threads.h> 
static KeyedVector<int, MPEG4Writer_mtk*> mtkMPEG4WriterObjList; 
static Mutex mtkMPEG4WriterObjListLock; 

#include <media/stagefright/MPEG4Writer_mtkwrp.h>

namespace android { 

MPEG4Writer::MPEG4Writer(const char * filename)
{ 
    MPEG4Writer_mtk* mtkInst = new MPEG4Writer_mtk(filename); 
    mtkMPEG4WriterObjListLock.lock(); 
    mtkMPEG4WriterObjList.add((int)this, mtkInst); 
    mtkMPEG4WriterObjListLock.unlock(); 
} 
 
MPEG4Writer::MPEG4Writer(int fd)
{ 
    MPEG4Writer_mtk* mtkInst = new MPEG4Writer_mtk(fd); 
    mtkMPEG4WriterObjListLock.lock(); 
    mtkMPEG4WriterObjList.add((int)this, mtkInst); 
    mtkMPEG4WriterObjListLock.unlock(); 
} 
 
status_t    MPEG4Writer::addSource(const sp<MediaSource> & source)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (status_t)inst->addSource(source); 
} 
 
status_t    MPEG4Writer::start(MetaData * param)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (status_t)inst->start(param); 
} 
 
status_t    MPEG4Writer::stop()
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (status_t)inst->stop(); 
} 
 
status_t    MPEG4Writer::pause()
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (status_t)inst->pause(); 
} 
 
bool    MPEG4Writer::reachedEOS()
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (bool)inst->reachedEOS(); 
} 
 
status_t    MPEG4Writer::dump(int fd,const Vector<String16> & args)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (status_t)inst->dump(fd,args); 
} 
 
void    MPEG4Writer::beginBox(const char * fourcc)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->beginBox(fourcc); 
} 
 
void    MPEG4Writer::writeInt8(int8_t x)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->writeInt8(x); 
} 
 
void    MPEG4Writer::writeInt16(int16_t x)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->writeInt16(x); 
} 
 
void    MPEG4Writer::writeInt32(int32_t x)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->writeInt32(x); 
} 
 
void    MPEG4Writer::writeInt64(int64_t x)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->writeInt64(x); 
} 
 
void    MPEG4Writer::writeCString(const char * s)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->writeCString(s); 
} 
 
void    MPEG4Writer::writeFourcc(const char * fourcc)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->writeFourcc(fourcc); 
} 
 
void    MPEG4Writer::write(const void * data,size_t size)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->write(data,size); 
} 
 
void    MPEG4Writer::endBox()
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->endBox(); 
} 
 
uint32_t    MPEG4Writer::interleaveDuration()  const
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (uint32_t)inst->interleaveDuration(); 
} 
 
status_t    MPEG4Writer::setInterleaveDuration(uint32_t duration)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (status_t)inst->setInterleaveDuration(duration); 
} 
 
int32_t    MPEG4Writer::getTimeScale()  const
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (int32_t)inst->getTimeScale(); 
} 
 
status_t    MPEG4Writer::setGeoData(int latitudex10000,int longitudex10000)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (status_t)inst->setGeoData(latitudex10000,longitudex10000); 
} 
 
void    MPEG4Writer::setStartTimeOffsetMs(int ms)
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (void)inst->setStartTimeOffsetMs(ms); 
} 
 
int32_t    MPEG4Writer::getStartTimeOffsetMs()  const
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
    return (int32_t)inst->getStartTimeOffsetMs(); 
} 
 
MPEG4Writer::~MPEG4Writer()
{ 
    MPEG4Writer_mtk *inst = NULL; 
    mtkMPEG4WriterObjListLock.lock(); 
    inst = mtkMPEG4WriterObjList.valueFor((int)this); 
    mtkMPEG4WriterObjList.removeItem((int)this); 
    mtkMPEG4WriterObjListLock.unlock(); 
} 
 

}  // namespace android 
