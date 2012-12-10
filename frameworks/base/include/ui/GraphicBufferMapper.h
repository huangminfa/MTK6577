/*
 * Copyright (C) 2007 The Android Open Source Project
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

#ifndef ANDROID_UI_BUFFER_MAPPER_H
#define ANDROID_UI_BUFFER_MAPPER_H

#include <stdint.h>
#include <sys/types.h>

#include <utils/Singleton.h>

#include <hardware/gralloc.h>

#include <utils/KeyedVector.h>
#include <ui/PixelFormat.h>

struct gralloc_module_t;

namespace android {

// ---------------------------------------------------------------------------

class Rect;
class String8;

class GraphicBufferMapper : public Singleton<GraphicBufferMapper>
{
public:
    static inline GraphicBufferMapper& get() { return getInstance(); }

    status_t registerBuffer(buffer_handle_t handle);

    status_t unregisterBuffer(buffer_handle_t handle);
    
    status_t lock(buffer_handle_t handle,
            int usage, const Rect& bounds, void** vaddr);

    status_t unlock(buffer_handle_t handle);
    
    // dumps information about the mapping of this handle
    void dump(String8& result) const;

private:
    friend class Singleton<GraphicBufferMapper>;
    GraphicBufferMapper();
    gralloc_module_t const *mAllocMod;


    // added by Ryan 
    // for recording current buffer usage
    struct reg_rec_t {
        uint32_t w;
        uint32_t h;
        uint32_t s;
        PixelFormat format;
        uint32_t usage;
        size_t size;
    };
    
    static Mutex sLock;
    static KeyedVector<buffer_handle_t, reg_rec_t> sRegList;
    void dumpRegistrationToSystemLog();

public:
    status_t registerBuffer(buffer_handle_t handle, 
        int w, int h, int s, int f, int usage);
};

// ---------------------------------------------------------------------------

}; // namespace android

#endif // ANDROID_UI_BUFFER_MAPPER_H

