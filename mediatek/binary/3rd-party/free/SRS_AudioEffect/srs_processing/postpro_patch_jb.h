/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2012 by SRS Labs.
 *	All rights reserved.
 *
 *  Delta-minimizing patch for Jellybean's AudioFlinger.cpp
 ********************************************************************************/

#ifndef ANDROID_SRS_AUDIOFLINGER_PATCH
#define ANDROID_SRS_AUDIOFLINGER_PATCH

// DEFINE if detailed SRS-related logs are required...
//#define SRS_VERBOSE

#include "srs_processing.h"

#define SRS_PROCESSING_ACTIVE

namespace android {
	
// MACROS to help create very minimal deltas at the audioflinger level

#ifdef SRS_VERBOSE
	#define SRS_LOG(...) ((void)ALOG(LOG_VERBOSE, LOG_TAG, __VA_ARGS__))
#else
	#define SRS_LOG(...) ((void)0)
#endif

#define POSTPRO_PATCH_JB_PARAMS_SET(a) \
	if (SRS_DoAny()){ \
		SRS_Processing::ParamsSet(SRS_Processing::AUTO, a); \
	}

#define POSTPRO_PATCH_JB_PARAMS_GET(a, b) \
	if (SRS_DoAny()){ \
		String8 srs_params = SRS_Processing::ParamsGet(SRS_Processing::AUTO, a); \
    	if (srs_params != "") b += srs_params+";"; \
	}
	
#define POSTPRO_PATCH_JB_OUTPROC_PLAY_INIT(a, b) \
	if (SRS_DoOutput()){ \
		SRS_LOG("SRS_Processing - OutNotify_Init: %s\n", b.string()); \
		SRS_Processing::ProcessOutNotify(SRS_Processing::AUTO, a, true); \
	}
	
#define POSTPRO_PATCH_JB_OUTPROC_PLAY_SAMPLES(a, fmt, buf, bsize, rate, count) \
	if ((fmt == AUDIO_FORMAT_PCM_16_BIT) && SRS_DoOutput()){ \
		SRS_Processing::ProcessOut(SRS_Processing::AUTO, a, buf, bsize, rate, count); \
	}
	
#define POSTPRO_PATCH_JB_OUTPROC_PLAY_EXIT(a, b) \
	if (SRS_DoOutput()){ \
		SRS_LOG("SRS_Processing - OutNotify_Exit: %s\n", b.string()); \
		SRS_Processing::ProcessOutNotify(SRS_Processing::AUTO, a, false); \
	}
	
#define POSTPRO_PATCH_JB_OUTPROC_PLAY_ROUTE(a, para, val) \
	if (SRS_DoOutput()){ \
		if (para.getInt(String8(AudioParameter::keyRouting), val) == NO_ERROR){ \
        	SRS_Processing::ProcessOutRoute(SRS_Processing::AUTO, a, val); \
        } \
	}
	
#define POSTPRO_PATCH_JB_OUTPROC_DUPE_SAMPLES(a, fmt, buf, bsize, rate, count) \
	if ((fmt == AUDIO_FORMAT_PCM_16_BIT) && SRS_DoOutput()){ \
		SRS_Processing::ProcessOut(SRS_Processing::AUTO, a, buf, bsize, rate, count); \
	}
	
#define POSTPRO_PATCH_JB_INPROC_INIT(a, b, fmt) \
	if ((fmt == AUDIO_FORMAT_PCM_16_BIT) && SRS_DoInput()){ \
		SRS_LOG("SRS_Processing - RecordThread - InNotify_Init: %p TID %d\n", a, b); \
		SRS_Processing::ProcessInNotify(SRS_Processing::AUTO, a, true); \
	}
	
#define POSTPRO_PATCH_JB_INPROC_SAMPLES(a, fmt, buf, bsize, rate, count) \
	if ((bsize > 0) && (fmt == AUDIO_FORMAT_PCM_16_BIT) && SRS_DoInput()){ \
		SRS_Processing::ProcessIn(SRS_Processing::AUTO, a, buf, bsize, rate, count); \
	}
	
#define POSTPRO_PATCH_JB_INPROC_EXIT(a, b, fmt) \
	if ((fmt == AUDIO_FORMAT_PCM_16_BIT) && SRS_DoInput()){ \
		SRS_LOG("SRS_Processing - RecordThread - InNotify_Exit: %p TID %d\n", a, b); \
		SRS_Processing::ProcessInNotify(SRS_Processing::AUTO, a, false); \
	}
	
#define POSTPRO_PATCH_JB_INPROC_ROUTE(a, para, val) \
	if (SRS_DoInput()){ \
		if (para.getInt(String8(AudioParameter::keyRouting), val) == NO_ERROR){ \
        	SRS_Processing::ProcessInRoute(SRS_Processing::AUTO, a, val); \
        } \
	}
	    
	      	
// FUNCTIONS to help direct execution based on build.prop settings

#ifdef POSTPRO_PROPGATE
bool SRS_DoOutput();
bool SRS_DoInput();
bool SRS_DoAny();
#else
bool SRS_DoOutput(){ return true; }
bool SRS_DoInput(){ return true; }
bool SRS_DoAny(){ return true; }
#endif

};	// namespace android

#endif // ANDROID_SRS_AUDIOFLINGER_PATCH
