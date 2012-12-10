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

#ifndef NU_CACHED_SOURCE_2_H__DEF

#define NU_CACHED_SOURCE_2_H__DEF

#include <media/stagefright/foundation/ABase.h>
#include <media/stagefright/foundation/AHandlerReflector.h>
#include <media/stagefright/DataSource.h>

#ifdef NuCachedSource2 
#undef NuCachedSource2 
#endif 
#define NuCachedSource2    NuCachedSource2 

namespace android {

struct NuCachedSource2 : public DataSource {
public:
    NuCachedSource2(const sp<DataSource> & source,const char * cacheConfig = NULL,bool disconnectAtHighwatermark = false);
    virtual status_t initCheck() const;
    virtual ssize_t readAt(off64_t offset,void * data,size_t size);
    virtual status_t getSize(off64_t * size);
    virtual uint32_t flags();
    virtual sp <DecryptHandle > DrmInitialization();
    virtual void getDrmInfo(sp<DecryptHandle> & handle,DrmManagerClient * * client);
    virtual String8 getUri();
    virtual String8 getMIMEType() const;
    size_t cachedSize();
    size_t approxDataRemaining(status_t * finalStatus);
    void resumeFetchingIfNecessary();
    status_t getEstimatedBandwidthKbps(int32_t * kbps);
    status_t setCacheStatCollectFreq(int32_t freqMs);
    static void RemoveCacheSpecificHeaders(KeyedVector <String8,String8> * headers,String8 * cacheConfig,bool * disconnectAtHighwatermark);
protected:
    virtual ~NuCachedSource2();
private:

};

}  // namespace android 

#endif //NU_CACHED_SOURCE_2_H__DEF