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

#define LOG_TAG "GraphicBufferMapper"

#include <stdint.h>
#include <errno.h>

#include <utils/Errors.h>
#include <utils/Log.h>

#include <ui/GraphicBufferMapper.h>
#include <ui/Rect.h>

#include <hardware/gralloc.h>

#include <utils/String8.h>
#include <cutils/properties.h>
#include <cutils/xlog.h>

namespace android {
// ---------------------------------------------------------------------------

ANDROID_SINGLETON_STATIC_INSTANCE( GraphicBufferMapper )

// added by Ryan 
// for recording current buffer usage
Mutex GraphicBufferMapper::sLock;
KeyedVector<buffer_handle_t,
    GraphicBufferMapper::reg_rec_t> GraphicBufferMapper::sRegList;

GraphicBufferMapper::GraphicBufferMapper()
    : mAllocMod(0)
{
    hw_module_t const* module;
    int err = hw_get_module(GRALLOC_HARDWARE_MODULE_ID, &module);
    LOGE_IF(err, "FATAL: can't find the %s module", GRALLOC_HARDWARE_MODULE_ID);
    if (err == 0) {
        mAllocMod = (gralloc_module_t const *)module;
    }
}

status_t GraphicBufferMapper::registerBuffer(buffer_handle_t handle)
{
    status_t err;

    err = mAllocMod->registerBuffer(mAllocMod, handle);

    LOGW_IF(err, "registerBuffer(%p) failed %d (%s)",
            handle, err, strerror(-err));

    //XLOGD("[%s], handle: %p", __func__, handle);
    if (err != NO_ERROR)
    {
        Mutex::Autolock _l(sLock);
        dumpRegistrationToSystemLog();
    }

    return err;
}

status_t GraphicBufferMapper::unregisterBuffer(buffer_handle_t handle)
{
    status_t err;

    err = mAllocMod->unregisterBuffer(mAllocMod, handle);

    LOGW_IF(err, "unregisterBuffer(%p) failed %d (%s)",
            handle, err, strerror(-err));

    // added by Ryan
    // for recording current buffer usage
    if (err == NO_ERROR) {
        //XLOGD("[%s], handle: %p", __func__, handle);
        Mutex::Autolock _l(sLock);
        KeyedVector<buffer_handle_t, reg_rec_t>& list(sRegList);
        list.removeItem(handle);
    }
    
    return err;
}

status_t GraphicBufferMapper::lock(buffer_handle_t handle, 
        int usage, const Rect& bounds, void** vaddr)
{
    status_t err;

    err = mAllocMod->lock(mAllocMod, handle, usage,
            bounds.left, bounds.top, bounds.width(), bounds.height(),
            vaddr);

    LOGW_IF(err, "lock(...) failed %d (%s)", err, strerror(-err));
    return err;
}

status_t GraphicBufferMapper::unlock(buffer_handle_t handle)
{
    status_t err;

    err = mAllocMod->unlock(mAllocMod, handle);

    LOGW_IF(err, "unlock(...) failed %d (%s)", err, strerror(-err));
    return err;
}

void GraphicBufferMapper::dump(String8& result) const
{
    //Mutex::Autolock _l(sLock);
    KeyedVector<buffer_handle_t, reg_rec_t>& list(sRegList);
    size_t total = 0;
    //const size_t SIZE = 4096;
    //char buffer[SIZE];
    //snprintf(buffer, SIZE, "Registered buffers:\n");
    //result.append(buffer);
    const size_t c = list.size();
    XLOGI("Registered buffers:, length(%d)\n", c);
    for (size_t i=0 ; i<c ; i++) {
        const reg_rec_t& rec(list.valueAt(i));
        if (rec.size) {
            //snprintf(buffer, SIZE, "%10p: %7.2f KiB | %4u (%4u) x %4u | %8X | 0x%08x\n",
            XLOGI("%10p: %7.2f KiB | %4u (%4u) x %4u | %8X | 0x%08x\n",
                    list.keyAt(i), rec.size/1024.0f,
                    rec.w, rec.s, rec.h, rec.format, rec.usage);
        } else {
            //snprintf(buffer, SIZE, "%10p: unknown     | %4u (%4u) x %4u | %8X | 0x%08x\n",
            XLOGI("%10p: unknown     | %4u (%4u) x %4u | %8X | 0x%08x\n",
                    list.keyAt(i),
                    rec.w, rec.s, rec.h, rec.format, rec.usage);
        }
        //result.append(buffer);
        total += rec.size;
    }
    //snprintf(buffer, SIZE, "Total registered (estimate): %.2f KB\n", total/1024.0f);
    XLOGI("Total registered (estimate): %.2f KB\n", total/1024.0f);
    //result.append(buffer);
}
    
// added by Ryan
// for recording current buffer usage
status_t GraphicBufferMapper::registerBuffer(buffer_handle_t handle,
    int w, int h, int stride, int format, int usage)
{
    Mutex::Autolock _l(sLock);

    //XLOGD("[%s], w=%d, h=%d, s=%d, f=%d, usage=%d", __func__, w, h, stride, format, usage);
    
    KeyedVector<buffer_handle_t, reg_rec_t>& list(sRegList);
    int bpp = bytesPerPixel(format);
    if (bpp < 0) {
        // probably a HAL custom format. in any case, we don't know
        // what its pixel size is.
        bpp = 0;
    }
    reg_rec_t rec;
    rec.w = w;
    rec.h = h;
    rec.s = stride;
    rec.format = format;
    rec.usage = usage;
    rec.size = h * stride * bpp;
    list.add(handle, rec);   

    // debugging stuff...
    char value[PROPERTY_VALUE_MAX];
    property_get("debug.sf.dumpmem", value, "0");
    bool bDumpMemUsage = atoi(value);

    if (bDumpMemUsage)
    {
        dumpRegistrationToSystemLog();
    }   
    return NO_ERROR;
}

void GraphicBufferMapper::dumpRegistrationToSystemLog()
{
    String8 s;
    dump(s);
    XLOGI("%s", s.string());
}

// ---------------------------------------------------------------------------
}; // namespace android
