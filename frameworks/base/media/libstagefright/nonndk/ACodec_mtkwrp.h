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

#ifndef A_CODEC_H__DEF

#define A_CODEC_H__DEF

#include <stdint.h>
#include <android/native_window.h>
#include <media/IOMX.h>
#include <media/stagefright/foundation/AHierarchicalStateMachine.h>

#ifdef ACodec 
#undef ACodec 
#endif 
#define ACodec    ACodec 

namespace android {

struct ACodec : public AHierarchicalStateMachine {
public:

    enum {
        kWhatFillThisBuffer      = 'fill',
        kWhatDrainThisBuffer     = 'drai',
        kWhatEOS                 = 'eos ',
        kWhatShutdownCompleted   = 'scom',
        kWhatFlushCompleted      = 'fcom',
        kWhatOutputFormatChanged = 'outC',
        kWhatError               = 'erro',
    };
public:
    ACodec();
    void setNotificationMessage(const sp<AMessage> & msg);
    void initiateSetup(const sp<AMessage> & msg);
    void signalFlush();
    void signalResume();
    void initiateShutdown();
protected:
    virtual ~ACodec();
private:

};

}  // namespace android 

#endif //A_CODEC_H__DEF