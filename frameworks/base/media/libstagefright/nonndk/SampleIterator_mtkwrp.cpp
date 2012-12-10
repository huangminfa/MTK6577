/*
 * Copyright (C) 2010 The Android Open Source Project
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

#define LOG_TAG "SampleIterator"
//#define LOG_NDEBUG 0
#include <utils/Log.h>

#include "include/SampleIterator.h"

#include <arpa/inet.h>

#include <media/stagefright/DataSource.h>
#include <media/stagefright/MediaDebug.h>
#include <media/stagefright/Utils.h>

#include "include/SampleTable.h"

using namespace android; 
#include <utils/KeyedVector.h> 
#include <utils/threads.h> 
static KeyedVector<int, SampleIterator_mtk*> mtkSampleIteratorObjList; 
static Mutex mtkSampleIteratorObjListLock; 

#include "include/SampleIterator_mtkwrp.h"

namespace android { 

SampleIterator::SampleIterator(SampleTable * table)
{ 
    SampleIterator_mtk* mtkInst = new SampleIterator_mtk(table); 
    mtkSampleIteratorObjListLock.lock(); 
    mtkSampleIteratorObjList.add((int)this, mtkInst); 
    mtkSampleIteratorObjListLock.unlock(); 
} 
 
status_t    SampleIterator::seekTo(uint32_t sampleIndex)
{ 
    SampleIterator_mtk *inst = NULL; 
    mtkSampleIteratorObjListLock.lock(); 
    inst = mtkSampleIteratorObjList.valueFor((int)this); 
    mtkSampleIteratorObjListLock.unlock(); 
    return (status_t)inst->seekTo(sampleIndex); 
} 
 
uint32_t    SampleIterator::getChunkIndex()  const
{ 
    SampleIterator_mtk *inst = NULL; 
    mtkSampleIteratorObjListLock.lock(); 
    inst = mtkSampleIteratorObjList.valueFor((int)this); 
    mtkSampleIteratorObjListLock.unlock(); 
    return (uint32_t)inst->getChunkIndex(); 
} 
 
uint32_t    SampleIterator::getDescIndex()  const
{ 
    SampleIterator_mtk *inst = NULL; 
    mtkSampleIteratorObjListLock.lock(); 
    inst = mtkSampleIteratorObjList.valueFor((int)this); 
    mtkSampleIteratorObjListLock.unlock(); 
    return (uint32_t)inst->getDescIndex(); 
} 
 
off64_t    SampleIterator::getSampleOffset()  const
{ 
    SampleIterator_mtk *inst = NULL; 
    mtkSampleIteratorObjListLock.lock(); 
    inst = mtkSampleIteratorObjList.valueFor((int)this); 
    mtkSampleIteratorObjListLock.unlock(); 
    return (off64_t)inst->getSampleOffset(); 
} 
 
size_t    SampleIterator::getSampleSize()  const
{ 
    SampleIterator_mtk *inst = NULL; 
    mtkSampleIteratorObjListLock.lock(); 
    inst = mtkSampleIteratorObjList.valueFor((int)this); 
    mtkSampleIteratorObjListLock.unlock(); 
    return (size_t)inst->getSampleSize(); 
} 
 
uint32_t    SampleIterator::getSampleTime()  const
{ 
    SampleIterator_mtk *inst = NULL; 
    mtkSampleIteratorObjListLock.lock(); 
    inst = mtkSampleIteratorObjList.valueFor((int)this); 
    mtkSampleIteratorObjListLock.unlock(); 
    return (uint32_t)inst->getSampleTime(); 
} 
 
status_t    SampleIterator::getSampleSizeDirect(uint32_t sampleIndex,size_t * size)
{ 
    SampleIterator_mtk *inst = NULL; 
    mtkSampleIteratorObjListLock.lock(); 
    inst = mtkSampleIteratorObjList.valueFor((int)this); 
    mtkSampleIteratorObjListLock.unlock(); 
    return (status_t)inst->getSampleSizeDirect(sampleIndex,size); 
} 
 

}  // namespace android 
