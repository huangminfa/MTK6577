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

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>

#include <utils/Errors.h>
#include <utils/String8.h>
#include <utils/Vector.h>

#include <hardware/hardware.h>

#include <cutils/log.h>

#include <EGL/egl.h>

#include "LayerBase.h"
#include "HWComposer.h"
#include "SurfaceFlinger.h"

#include <cutils/xlog.h>

namespace android {
// ---------------------------------------------------------------------------
#if 1
void freeList(hwc_layer_list* list)
{    
    if (list != NULL)
    {
        XLOGD("free list, numHwLayers=%d", list->numHwLayers);
        size_t count = list->numHwLayers;
        for (size_t i=0 ; i<count ; i++) {
            hwc_layer& l(list->hwLayers[i]);
            if (l.graphicBuffer != NULL)
            {                
                l.graphicBuffer.clear();
            }
        }
    }
    free(list);
}
#endif
HWComposer::HWComposer(const sp<SurfaceFlinger>& flinger)
    : mFlinger(flinger),
      mModule(0), mHwc(0), mList(0), mCapacity(0),
      mNumOVLayers(0), mNumFBLayers(0),
      mDpy(EGL_NO_DISPLAY), mSur(EGL_NO_SURFACE)
{
    int err = hw_get_module(HWC_HARDWARE_MODULE_ID, &mModule);
    LOGW_IF(err, "%s module not found", HWC_HARDWARE_MODULE_ID);
    if (err == 0) {
        err = hwc_open(mModule, &mHwc);
        LOGE_IF(err, "%s device failed to initialize (%s)",
                HWC_HARDWARE_COMPOSER, strerror(-err));
        if (err == 0) {
            if (mHwc->registerProcs) {
                mCBContext.hwc = this;
                mCBContext.procs.invalidate = &hook_invalidate;
                mHwc->registerProcs(mHwc, &mCBContext.procs);
            }
        }
    }
}

HWComposer::~HWComposer() {
    freeList(mList);
    //delete [] mList->hwLayers;
    //delete mList;
                
    if (mHwc) {
        hwc_close(mHwc);
    }
}

status_t HWComposer::initCheck() const {
    return mHwc ? NO_ERROR : NO_INIT;
}

void HWComposer::hook_invalidate(struct hwc_procs* procs) {
    reinterpret_cast<cb_context *>(procs)->hwc->invalidate();
}

void HWComposer::invalidate() {
    mFlinger->repaintEverything();
}

void HWComposer::setFrameBuffer(EGLDisplay dpy, EGLSurface sur) {
    mDpy = (hwc_display_t)dpy;
    mSur = (hwc_surface_t)sur;
}

status_t HWComposer::createWorkList(size_t numLayers) {
    if (mHwc) {
        if (!mList || mCapacity < numLayers) 
        {
            //XLOGD("HWComposer::createWorkList");
#if 1
            freeList(mList);
            size_t size = sizeof(hwc_layer_list) + numLayers*sizeof(hwc_layer_t);
            mList = (hwc_layer_list_t*)malloc(size);
            // [mtk04189] memory reset for struct hwc_layer_list + struct hwc_layer_t
            memset(mList, 0x00, size);
#else
            // [mtk03712] use c++ style allocation to avoid sp<> leak
            //------------------------------------------------------------------------
            if (mList) {

                sp<GraphicBuffer> gb = mList->hwLayers->graphicBuffer;
                if (gb.get() != NULL)
                    XLOGI("free list+ handle=%p, cnt:%d", gb->handle, gb.get()->getStrongCount());


                delete [] mList->hwLayers;
                delete mList;

                if (gb.get() != NULL)
                    XLOGI("free list- handle=%p, cnt:%d", gb->handle, gb.get()->getStrongCount());
            }

            mList = new hwc_layer_list_t;
            memset(mList, 0x00, sizeof(hwc_layer_list_t));

            mList->hwLayers = new hwc_layer_t[numLayers];
            memset(mList->hwLayers, 0x00, numLayers * sizeof(hwc_layer_t));
            //------------------------------------------------------------------------

#endif
            mCapacity = numLayers;
        }
        mList->flags = HWC_GEOMETRY_CHANGED;
        mList->numHwLayers = numLayers;
    }
    return NO_ERROR;
}

status_t HWComposer::prepare() const {
    if (mList && mFlinger->isLayerScreenShotVisible()) {
        mList->flags |= HWC_LAYERSCREENSHOT_EXIST;
    }

    // [ mtk03712 ] pass SF info for S3D with hwc
    //-------------------------------------------------------------------------
    mList->composingOrientation = mFlinger->mDrawingState.composingOrientation;
    mList->composingPhase = mFlinger->mDrawingState.composingPhase;
    //-------------------------------------------------------------------------

    int err = mHwc->prepare(mHwc, mList);
    if (err == NO_ERROR) {
        size_t numOVLayers = 0;
        size_t numFBLayers = 0;
        size_t numEXLayers = 0;
        size_t count = mList->numHwLayers;
        for (size_t i=0 ; i<count ; i++) {
            hwc_layer& l(mList->hwLayers[i]);
            if (l.flags & HWC_SKIP_LAYER) {
                l.compositionType = HWC_FRAMEBUFFER;
            }
            switch (l.compositionType) {
                case HWC_OVERLAY:
                    numOVLayers++;
                    break;
                case HWC_FRAMEBUFFER:
                    numFBLayers++;
                    break;
                case HWC_OVERLAY_EX:
                    numEXLayers++;
                    break;
            }
        }
        mNumOVLayers = numOVLayers;
        mNumFBLayers = numFBLayers;
        mNumEXLayers = numEXLayers;
    }
    return (status_t)err;
}

size_t HWComposer::getLayerCount(int type) const {
    switch (type) {
        case HWC_OVERLAY:
            return mNumOVLayers;
        case HWC_FRAMEBUFFER:
            return mNumFBLayers;
        case HWC_OVERLAY_EX:
            return mNumEXLayers;
    }
    return 0;
}

status_t HWComposer::commit() const {
    // [mtk04189] get layer swap information and raise the hwc_layer_list_t flag if necessary
    //------------------------------------------------------------------------------------------
    if (mList && mFlinger->getAndClearLayersSwapRequired()) {
        mList->flags |= HWC_SWAP_REQUIRED;
    }

    //------------------------------------------------------------------------------------------
    int err = mHwc->set(mHwc, mDpy, mSur, mList);
    if (mList) {
        mList->flags &= ~(HWC_GEOMETRY_CHANGED | HWC_SWAP_REQUIRED | HWC_LAYERSCREENSHOT_EXIST);
    }
    return (status_t)err;
}

status_t HWComposer::release() const {
    if (mHwc) {
        int err = mHwc->set(mHwc, NULL, NULL, NULL);
        return (status_t)err;
    }
    return NO_ERROR;
}

status_t HWComposer::disable() {
    if (mHwc) {
        //free(mList);
        freeList(mList);
        mList = NULL;
        int err = mHwc->prepare(mHwc, NULL);
        return (status_t)err;
    }
    return NO_ERROR;
}

size_t HWComposer::getNumLayers() const {
    return mList ? mList->numHwLayers : 0;
}

hwc_layer_t* HWComposer::getLayers() const {
    return mList ? mList->hwLayers : 0;
}

void HWComposer::dump(String8& result, char* buffer, size_t SIZE,
        const Vector< sp<LayerBase> >& visibleLayersSortedByZ) const {
    if (mHwc && mList) {
        result.append("\n[ Hardware Composer state ]:\n");

        snprintf(buffer, SIZE, "  numHwLayers=%u, flags=%08x\n",
                mList->numHwLayers, mList->flags);
        result.append(buffer);
        result.append(
                " type |  handle  |   hints  |   flags  | tr | blend |  format  |       source crop         |           frame           | name\n"
                "------+----------+----------+----------+----+-------+----------+---------------------------+---------------------------+------\n");
        //      " ____ | ________ | ________ | ________ | __ | _____ | ________ | [_____,_____,_____,_____] | [_____,_____,_____,_____] | ....
        for (size_t i=0 ; i<mList->numHwLayers ; i++) {
            const hwc_layer_t& l(mList->hwLayers[i]);
            const sp<LayerBase> layer(visibleLayersSortedByZ[i]);
            int32_t format = -1;
            intptr_t handle = NULL;
            if (layer->getLayer() != NULL) {
                const sp<GraphicBuffer>& buffer(layer->getLayer()->getActiveBuffer());
                if (buffer != NULL) {
                    format = buffer->getPixelFormat();
                    handle = intptr_t(buffer->handle);
                }
            }
            snprintf(buffer, SIZE,
                    " %4s | %08x | %08x | %08x | %02x | %05x | %08x | [%5d,%5d,%5d,%5d] | [%5d,%5d,%5d,%5d] | %s\n",
                    compositionTypeName[l.compositionType],
                    handle, l.hints, l.flags, l.transform, l.blending, format,
                    l.sourceCrop.left, l.sourceCrop.top, l.sourceCrop.right, l.sourceCrop.bottom,
                    l.displayFrame.left, l.displayFrame.top, l.displayFrame.right, l.displayFrame.bottom,
                    layer->getName().string());
            result.append(buffer);
        }
    }
    if (mHwc && mHwc->common.version >= 1 && mHwc->dump) {
        mHwc->dump(mHwc, buffer, SIZE);
        result.append(buffer);
    }
}

// ---------------------------------------------------------------------------
}; // namespace android
