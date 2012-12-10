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

#include <utils/Vector.h>

#ifdef SampleIterator 
#undef SampleIterator 
#endif 
#define SampleIterator    SampleIterator 

namespace android {

struct SampleIterator  {
public:
    SampleIterator(SampleTable * table);
    status_t seekTo(uint32_t sampleIndex);
    uint32_t getChunkIndex() const;
    uint32_t getDescIndex() const;
    off64_t getSampleOffset() const;
    size_t getSampleSize() const;
    uint32_t getSampleTime() const;
    status_t getSampleSizeDirect(uint32_t sampleIndex,size_t * size);
private:
    SampleIterator(const SampleIterator &);

};

}  // namespace android 
