/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2012 by SRS Labs.
 *	All rights reserved.
 *
 *  Delta-removing patch for Jellybean's AudioFlinger.cpp
 ********************************************************************************/

#ifndef ANDROID_POSTPRO_NULL_PATCH
#define ANDROID_POSTPRO_NULL_PATCH

#define POSTPRO_PATCH_JB_PARAMS_SET(a) ((void)0)
#define POSTPRO_PATCH_JB_PARAMS_GET(a, b) ((void)0)
#define POSTPRO_PATCH_JB_OUTPROC_PLAY_INIT(a, b) ((void)0)
#define POSTPRO_PATCH_JB_OUTPROC_PLAY_SAMPLES(a, fmt, buf, bsize, rate, count) ((void)0)
#define POSTPRO_PATCH_JB_OUTPROC_PLAY_EXIT(a, b) ((void)0)
#define POSTPRO_PATCH_JB_OUTPROC_PLAY_ROUTE(a, para, val) ((void)0)
#define POSTPRO_PATCH_JB_OUTPROC_DUPE_SAMPLES(a, fmt, buf, bsize, rate, count) ((void)0)
#define POSTPRO_PATCH_JB_INPROC_INIT(a, b, fmt) ((void)0)
#define POSTPRO_PATCH_JB_INPROC_SAMPLES(a, fmt, buf, bsize, rate, count) ((void)0)
#define POSTPRO_PATCH_JB_INPROC_EXIT(a, b, fmt) ((void)0)
#define POSTPRO_PATCH_JB_INPROC_ROUTE(a, para, val) ((void)0)

#endif // ANDROID_POSTPRO_NULL_PATCH
