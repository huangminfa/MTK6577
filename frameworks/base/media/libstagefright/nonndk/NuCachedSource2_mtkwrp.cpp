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

//#define LOG_NDEBUG 0
#define LOG_TAG "NuCachedSource2"
#include <utils/Log.h>

#include "include/NuCachedSource2.h"
#include "include/HTTPBase.h"

#include <cutils/properties.h>
#include <media/stagefright/foundation/ADebug.h>
#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/MediaErrors.h>

using namespace android; 
#include <utils/KeyedVector.h> 
#include <utils/threads.h> 
static KeyedVector<int, NuCachedSource2_mtk*> mtkNuCachedSource2ObjList; 
static Mutex mtkNuCachedSource2ObjListLock; 

#include "include/NuCachedSource2_mtkwrp.h"

namespace android { 

NuCachedSource2::NuCachedSource2(const sp<DataSource> & source,const char * cacheConfig,bool disconnectAtHighwatermark)
{ 
    NuCachedSource2_mtk* mtkInst = new NuCachedSource2_mtk(source,cacheConfig,disconnectAtHighwatermark); 
    mtkNuCachedSource2ObjListLock.lock(); 
    mtkNuCachedSource2ObjList.add((int)this, mtkInst); 
    mtkNuCachedSource2ObjListLock.unlock(); 
} 
 
status_t    NuCachedSource2::initCheck()  const
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (status_t)inst->initCheck(); 
} 
 
ssize_t    NuCachedSource2::readAt(off64_t offset,void * data,size_t size)
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (ssize_t)inst->readAt(offset,data,size); 
} 
 
status_t    NuCachedSource2::getSize(off64_t * size)
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (status_t)inst->getSize(size); 
} 
 
uint32_t    NuCachedSource2::flags()
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (uint32_t)inst->flags(); 
} 
 
sp <DecryptHandle >    NuCachedSource2::DrmInitialization()
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (sp <DecryptHandle >)inst->DrmInitialization(); 
} 
 
void    NuCachedSource2::getDrmInfo(sp<DecryptHandle> & handle,DrmManagerClient * * client)
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (void)inst->getDrmInfo(handle,client); 
} 
 
String8    NuCachedSource2::getUri()
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (String8)inst->getUri(); 
} 
 
String8    NuCachedSource2::getMIMEType()  const
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (String8)inst->getMIMEType(); 
} 
 
size_t    NuCachedSource2::cachedSize()
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (size_t)inst->cachedSize(); 
} 
 
size_t    NuCachedSource2::approxDataRemaining(status_t * finalStatus)
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (size_t)inst->approxDataRemaining(finalStatus); 
} 
 
void    NuCachedSource2::resumeFetchingIfNecessary()
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (void)inst->resumeFetchingIfNecessary(); 
} 
 
status_t    NuCachedSource2::getEstimatedBandwidthKbps(int32_t * kbps)
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (status_t)inst->getEstimatedBandwidthKbps(kbps); 
} 
 
status_t    NuCachedSource2::setCacheStatCollectFreq(int32_t freqMs)
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
    return (status_t)inst->setCacheStatCollectFreq(freqMs); 
} 
 
void    NuCachedSource2::RemoveCacheSpecificHeaders(KeyedVector <String8,String8> * headers,String8 * cacheConfig,bool * disconnectAtHighwatermark)
{ 
    return NuCachedSource2_mtk::RemoveCacheSpecificHeaders(headers,cacheConfig,disconnectAtHighwatermark); 
} 
 
NuCachedSource2::~NuCachedSource2()
{ 
    NuCachedSource2_mtk *inst = NULL; 
    mtkNuCachedSource2ObjListLock.lock(); 
    inst = mtkNuCachedSource2ObjList.valueFor((int)this); 
    mtkNuCachedSource2ObjList.removeItem((int)this); 
    mtkNuCachedSource2ObjListLock.unlock(); 
} 
 

}  // namespace android 
