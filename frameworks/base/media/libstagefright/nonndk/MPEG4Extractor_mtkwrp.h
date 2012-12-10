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

#ifndef MPEG4_EXTRACTOR_H__DEF

#define MPEG4_EXTRACTOR_H__DEF

#include <media/stagefright/MediaExtractor.h>
#include <utils/Vector.h>

#ifdef MPEG4Extractor 
#undef MPEG4Extractor 
#endif 
#define MPEG4Extractor    MPEG4Extractor 

namespace android {

class MPEG4Extractor : public MediaExtractor {
public:
    MPEG4Extractor(const sp<DataSource> & source);
    virtual size_t countTracks();
    virtual sp <MediaSource > getTrack(size_t index);
    virtual sp <MetaData > getTrackMetaData(size_t index,uint32_t flags);
    virtual sp <MetaData > getMetaData();
    virtual char * getDrmTrackInfo(size_t trackID,int * len);
protected:
    virtual ~MPEG4Extractor();
private:
    MPEG4Extractor(const MPEG4Extractor &);

};

}  // namespace android 

#endif //MPEG4_EXTRACTOR_H__DEF