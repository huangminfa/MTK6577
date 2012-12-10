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

#define LOG_TAG "MPEG4Extractor"
#include <utils/Log.h>

#include "include/MPEG4Extractor.h"
#include "include/SampleTable.h"
#include "include/ESDS.h"
#include "timedtext/TimedTextPlayer.h"

#include <arpa/inet.h>

#include <ctype.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/DataSource.h>
#include <media/stagefright/MediaBuffer.h>
#include <media/stagefright/MediaBufferGroup.h>
#include <media/stagefright/MediaDefs.h>
#include <media/stagefright/MediaSource.h>
#include <media/stagefright/MetaData.h>
#include <media/stagefright/Utils.h>
#include <utils/String8.h>
using namespace android; 
#include <utils/KeyedVector.h> 
#include <utils/threads.h> 
static KeyedVector<int, MPEG4Extractor_mtk*> mtkMPEG4ExtractorObjList; 
static Mutex mtkMPEG4ExtractorObjListLock; 

#include "include/MPEG4Extractor_mtkwrp.h"

namespace android { 

MPEG4Extractor::MPEG4Extractor(const sp<DataSource> & source)
{ 
    MPEG4Extractor_mtk* mtkInst = new MPEG4Extractor_mtk(source); 
    mtkMPEG4ExtractorObjListLock.lock(); 
    mtkMPEG4ExtractorObjList.add((int)this, mtkInst); 
    mtkMPEG4ExtractorObjListLock.unlock(); 
} 
 
size_t    MPEG4Extractor::countTracks()
{ 
    MPEG4Extractor_mtk *inst = NULL; 
    mtkMPEG4ExtractorObjListLock.lock(); 
    inst = mtkMPEG4ExtractorObjList.valueFor((int)this); 
    mtkMPEG4ExtractorObjListLock.unlock(); 
    return (size_t)inst->countTracks(); 
} 
 
sp <MediaSource >    MPEG4Extractor::getTrack(size_t index)
{ 
    MPEG4Extractor_mtk *inst = NULL; 
    mtkMPEG4ExtractorObjListLock.lock(); 
    inst = mtkMPEG4ExtractorObjList.valueFor((int)this); 
    mtkMPEG4ExtractorObjListLock.unlock(); 
    return (sp <MediaSource >)inst->getTrack(index); 
} 
 
sp <MetaData >    MPEG4Extractor::getTrackMetaData(size_t index,uint32_t flags)
{ 
    MPEG4Extractor_mtk *inst = NULL; 
    mtkMPEG4ExtractorObjListLock.lock(); 
    inst = mtkMPEG4ExtractorObjList.valueFor((int)this); 
    mtkMPEG4ExtractorObjListLock.unlock(); 
    return (sp <MetaData >)inst->getTrackMetaData(index,flags); 
} 
 
sp <MetaData >    MPEG4Extractor::getMetaData()
{ 
    MPEG4Extractor_mtk *inst = NULL; 
    mtkMPEG4ExtractorObjListLock.lock(); 
    inst = mtkMPEG4ExtractorObjList.valueFor((int)this); 
    mtkMPEG4ExtractorObjListLock.unlock(); 
    return (sp <MetaData >)inst->getMetaData(); 
} 
 
char *    MPEG4Extractor::getDrmTrackInfo(size_t trackID,int * len)
{ 
    MPEG4Extractor_mtk *inst = NULL; 
    mtkMPEG4ExtractorObjListLock.lock(); 
    inst = mtkMPEG4ExtractorObjList.valueFor((int)this); 
    mtkMPEG4ExtractorObjListLock.unlock(); 
    return (char *)inst->getDrmTrackInfo(trackID,len); 
} 
 
MPEG4Extractor::~MPEG4Extractor()
{ 
    MPEG4Extractor_mtk *inst = NULL; 
    mtkMPEG4ExtractorObjListLock.lock(); 
    inst = mtkMPEG4ExtractorObjList.valueFor((int)this); 
    mtkMPEG4ExtractorObjList.removeItem((int)this); 
    mtkMPEG4ExtractorObjListLock.unlock(); 
} 
 

}  // namespace android 
