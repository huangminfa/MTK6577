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

#ifndef SAMPLE_TABLE_H__DEF

#define SAMPLE_TABLE_H__DEF

#include <sys/types.h>
#include <stdint.h>

#include <media/stagefright/MediaErrors.h>
#include <utils/RefBase.h>
#include <utils/threads.h>

#ifdef SampleTable 
#undef SampleTable 
#endif 
#define SampleTable    SampleTable 

namespace android {

class SampleTable : public RefBase {
public:

    enum {
        kFlagBefore,
        kFlagAfter,
        kFlagClosest
    };
public:
    SampleTable(const sp<DataSource> & source);
    bool isValid() const;
    status_t setChunkOffsetParams(uint32_t type,off64_t data_offset,size_t data_size);
    status_t setSampleToChunkParams(off64_t data_offset,size_t data_size);
    status_t setSampleSizeParams(uint32_t type,off64_t data_offset,size_t data_size);
    status_t setTimeToSampleParams(off64_t data_offset,size_t data_size);
    status_t setCompositionTimeToSampleParams(off64_t data_offset,size_t data_size);
    status_t setSyncSampleParams(off64_t data_offset,size_t data_size);
    uint32_t countChunkOffsets() const;
    uint32_t countSamples() const;
    status_t getMaxSampleSize(size_t * size);
    status_t getMetaDataForSample(uint32_t sampleIndex,off64_t * offset,size_t * size,uint32_t * compositionTime,bool * isSyncSample = NULL);
    status_t findSampleAtTime(uint32_t req_time,uint32_t * sample_index,uint32_t flags);
    status_t findSyncSampleNear(uint32_t start_sample_index,uint32_t * sample_index,uint32_t flags);
    status_t findThumbnailSample(uint32_t * sample_index);
protected:
    ~SampleTable();
private:
    SampleTable(const SampleTable &);

};

}  // namespace android 

#endif //SAMPLE_TABLE_H__DEF