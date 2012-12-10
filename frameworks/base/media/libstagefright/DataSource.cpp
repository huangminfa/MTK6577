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

#include "include/AMRExtractor.h"
#include "include/MP3Extractor.h"
#include "include/MPEG4Extractor.h"
#include "include/WAVExtractor.h"
#include "include/OggExtractor.h"
#include "include/MPEG2PSExtractor.h"
#include "include/MPEG2TSExtractor.h"
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_AUDIO_APE_SUPPORT
#include "include/APEExtractor.h"
#endif
#endif  //#ifndef ANDROID_DEFAULT_CODE
#include "include/NuCachedSource2.h"
#include "include/HTTPBase.h"
#include "include/DRMExtractor.h"
#include "include/FLACExtractor.h"
#include "include/AACExtractor.h"
#include "include/WVMExtractor.h"
#ifndef ANDROID_DEFAULT_CODE
#include "MtkAACExtractor.h"

#ifdef MTK_MTKPS_PLAYBACK_SUPPORT
#include "MtkMPEGPSExtractor.h"
#endif //#ifdef MTK_MTKPS_PLAYBACK_SUPPORT

#include "include/ARTSPController.h"
#endif //#ifndef ANDROID_DEFAULT_CODE


#include "matroska/MatroskaExtractor.h"

#include <media/stagefright/foundation/AMessage.h>
#include <media/stagefright/DataSource.h>
#include <media/stagefright/FileSource.h>
#include <media/stagefright/MediaErrors.h>
#include <utils/String8.h>

#include <cutils/properties.h>


#ifndef ANDROID_DEFAULT_CODE
#include <media/stagefright/MediaDefs.h>   
#include <dlfcn.h>					   
#endif  //#ifndef ANDROID_DEFAULT_CODE

#ifdef MTK_AVI_PLAYBACK_SUPPORT
#include <MtkAVIExtractor.h>
#endif // #ifndef ANDROID_DEFAULT_CODE

#ifdef MTK_OGM_PLAYBACK_SUPPORT
#include <OgmExtractor.h>
#endif

namespace android {

#ifndef ANDROID_DEFAULT_CODE  

#ifdef MTK_ASF_PLAYBACK_SUPPORT
#define MTK_ASF_EXTRACTOR_LIB_NAME			"libasfextractor.so"
#define MTK_ASF_EXTRACTOR_RECOGNIZER_NAME	"mtk_asf_extractor_recognize"
#define MTK_ASF_EXTRACTOR_FACTORY_NAME		"mtk_asf_extractor_create_instance"
typedef sp<MediaExtractor> AsfFactory_Ptr(const sp<DataSource> &source);
typedef bool AsfRecognizer_Ptr(const sp<DataSource> &source);
bool SniffASF(const sp<DataSource> &source, String8 *mimeType, float *confidence, sp<AMessage> *) {
	bool ret = false;
	void* pAsfLib = NULL;

	pAsfLib = dlopen(MTK_ASF_EXTRACTOR_LIB_NAME, RTLD_NOW);
	if (NULL == pAsfLib) {
		LOGE ("%s", dlerror());
		return NULL;
	}

	AsfRecognizer_Ptr* asf_extractor_recognize = (AsfRecognizer_Ptr*) dlsym(pAsfLib, MTK_ASF_EXTRACTOR_RECOGNIZER_NAME);
	if (NULL == asf_extractor_recognize) {
		LOGE ("%s", dlerror());
		ret = false;
	}

	if (asf_extractor_recognize(source)) {
		*mimeType = MEDIA_MIMETYPE_CONTAINER_ASF;
		*confidence = 0.8;
		ret = true;
	}
	else {
		ret = false;
	}

	dlclose(pAsfLib);

	LOGE ("SniffASF return %d", ret);
	return ret;
}
#endif

#ifdef MTK_FLV_PLAYBACK_SUPPORT

#define MTK_FLV_EXTRACTOR_LIB_NAME			"libflvextractor.so"
#define MTK_FLV_EXTRACTOR_RECOGNIZER_NAME	"mtk_flv_extractor_recognize"
#define MTK_FLV_EXTRACTOR_FACTORY_NAME		"mtk_flv_extractor_create_instance"
typedef sp<MediaExtractor> FlvFactory_Ptr(const sp<DataSource> &source);
typedef bool FlvRecognizer_Ptr(const sp<DataSource> &source);

bool SniffFLV(const sp<DataSource> &source, String8 *mimeType, float *confidence, sp<AMessage> *) {
	bool ret = false;
	void* pFlvLib = NULL;

	pFlvLib = dlopen(MTK_FLV_EXTRACTOR_LIB_NAME, RTLD_NOW);
	if (NULL == pFlvLib) {
		LOGE ("%s", dlerror());
		return NULL;
	}

	FlvRecognizer_Ptr* flv_extractor_recognize = (FlvRecognizer_Ptr*) dlsym(pFlvLib, MTK_FLV_EXTRACTOR_RECOGNIZER_NAME);
	if (NULL == flv_extractor_recognize) {
		LOGE ("%s", dlerror());
		ret = false;
	}

	if (flv_extractor_recognize(source)) {
		*mimeType = MEDIA_MIMETYPE_CONTAINER_FLV;
		*confidence = 0.8;
		ret = true;
	}
	else {
		ret = false;
	}

	dlclose(pFlvLib);

	LOGE ("SniffFLV return %d", ret);
	return ret;
}

#endif //#ifdef MTK_FLV_PLAYBACK_SUPPORT
#endif  //#ifndef ANDROID_DEFAULT_CODE


bool DataSource::getUInt16(off64_t offset, uint16_t *x) {
    *x = 0;

    uint8_t byte[2];
    if (readAt(offset, byte, 2) != 2) {
        return false;
    }

    *x = (byte[0] << 8) | byte[1];

    return true;
}

status_t DataSource::getSize(off64_t *size) {
    *size = 0;

    return ERROR_UNSUPPORTED;
}

////////////////////////////////////////////////////////////////////////////////

Mutex DataSource::gSnifferMutex;
List<DataSource::SnifferFunc> DataSource::gSniffers;

bool DataSource::sniff(
        String8 *mimeType, float *confidence, sp<AMessage> *meta) {
    *mimeType = "";
    *confidence = 0.0f;
/*#ifndef ANDROID_DEFAULT_CODE
	int32_t isMustNotDRM = 0;
	if ((*meta) != NULL)
	{
		(*meta)->findInt32("must-not-drm", &isMustNotDRM);
	}
#endif*/
    meta->clear();

    Mutex::Autolock autoLock(gSnifferMutex);
    for (List<SnifferFunc>::iterator it = gSniffers.begin();
         it != gSniffers.end(); ++it) {
        String8 newMimeType;
        float newConfidence;
        sp<AMessage> newMeta;
/*#ifndef ANDROID_DEFAULT_CODE
		if (((*it) == SniffDRM) && (1 == isMustNotDRM))
		{
			LOGD("Must not drm");
			continue;
		}
#endif*/
        if ((*it)(this, &newMimeType, &newConfidence, &newMeta)) {
            if (newConfidence > *confidence) {
                *mimeType = newMimeType;
                *confidence = newConfidence;
                *meta = newMeta;
            }
        }
    }

    return *confidence > 0.0;
}

// static
void DataSource::RegisterSniffer(SnifferFunc func) {
    Mutex::Autolock autoLock(gSnifferMutex);

    for (List<SnifferFunc>::iterator it = gSniffers.begin();
         it != gSniffers.end(); ++it) {
        if (*it == func) {
            return;
        }
    }

    gSniffers.push_back(func);
}

// static
void DataSource::RegisterDefaultSniffers() {
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_DRM_APP
    // OMA DRM v1 implementation: this need to be registered always, and as the first one.
    RegisterSniffer(SniffDRM);
#else
    // when OMA DRM v1 is disabled
    char value[PROPERTY_VALUE_MAX];
    if (property_get("drm.service.enabled", value, NULL)
            && (!strcmp(value, "1") || !strcasecmp(value, "true"))) {
        RegisterSniffer(SniffDRM);
    }
#endif
#endif

    RegisterSniffer(SniffMPEG4);
    RegisterSniffer(SniffMatroska);
    RegisterSniffer(SniffOgg);
    RegisterSniffer(SniffWAV);
    RegisterSniffer(SniffFLAC);
    RegisterSniffer(SniffAMR);
    RegisterSniffer(SniffMPEG2TS);
    RegisterSniffer(SniffMPEG2PS);
    RegisterSniffer(SniffMP3);
#ifndef ANDROID_DEFAULT_CODE
#ifdef MTK_AUDIO_APE_SUPPORT
    RegisterSniffer(SniffAPE);
#endif
	RegisterSniffer(SniffMtkAAC);
#endif
    RegisterSniffer(SniffAAC);
    
    
#ifndef ANDROID_DEFAULT_CODE 

#ifdef MTK_ASF_PLAYBACK_SUPPORT
    RegisterSniffer(SniffASF);    
#endif

#ifdef MTK_FLV_PLAYBACK_SUPPORT
  RegisterSniffer(SniffFLV);
#endif  

#ifdef MTK_AVI_PLAYBACK_SUPPORT
    RegisterSniffer(MtkSniffAVI);
#endif
    RegisterSniffer(SniffSDP);

#ifdef MTK_OGM_PLAYBACK_SUPPORT 
    RegisterSniffer(SniffOgm);
#endif

#endif // #ifndef ANDROID_DEFAULT_CODE


#ifdef ANDROID_DEFAULT_CODE
    // for android default code, the DRM sniffer should be registed for WV DRM here.
    char value[PROPERTY_VALUE_MAX];
    if (property_get("drm.service.enabled", value, NULL)
            && (!strcmp(value, "1") || !strcasecmp(value, "true"))) {
        RegisterSniffer(SniffDRM);
    }
#endif
}

// static
sp<DataSource> DataSource::CreateFromURI(
        const char *uri, const KeyedVector<String8, String8> *headers) {
    sp<DataSource> source;
    if (!strncasecmp("file://", uri, 7)) {
        source = new FileSource(uri + 7);
    } else if (!strncasecmp("http://", uri, 7)
            || !strncasecmp("https://", uri, 8)) {
        sp<HTTPBase> httpSource = HTTPBase::Create();
        if (httpSource->connect(uri, headers) != OK) {
            return NULL;
        }
        source = new NuCachedSource2(httpSource);
    } else {
        // Assume it's a filename.
        source = new FileSource(uri);
    }

    if (source == NULL || source->initCheck() != OK) {
        return NULL;
    }

    return source;
}

String8 DataSource::getMIMEType() const {
    return String8("application/octet-stream");
}

}  // namespace android
