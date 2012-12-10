/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2012 by SRS Labs.
 *	All rights reserved.
 *
 *  Delta-removing patch for Ice-Cream Sandwich's AudioFlinger.cpp
 ********************************************************************************/

#ifndef ANDROID_POSTPRO_NULL_PATCH
#define ANDROID_POSTPRO_NULL_PATCH

#define POSTPRO_PATCH_ICS_PARAMS_SET(a) ((void)0)
#define POSTPRO_PATCH_ICS_PARAMS_GET(a, b) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_MIX_INIT(a, b) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_MIX_SAMPLES(a, fmt, buf, bsize, rate, count) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_MIX_EXIT(a, b) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_MIX_ROUTE(a, para, val) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_DIRECT_INIT(a, b) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_DIRECT_SAMPLES(a, fmt, buf, bsize, rate, count) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_DIRECT_EXIT(a, b) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_DUPE_INIT(a, b) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_DUPE_SAMPLES(a, fmt, buf, bsize, rate, count) ((void)0)
#define POSTPRO_PATCH_ICS_OUTPROC_DUPE_EXIT(a, b) ((void)0)
#define POSTPRO_PATCH_ICS_INPROC_INIT(a, b, fmt) ((void)0)
#define POSTPRO_PATCH_ICS_INPROC_SAMPLES(a, fmt, buf, bsize, rate, count) ((void)0)
#define POSTPRO_PATCH_ICS_INPROC_EXIT(a, b, fmt) ((void)0)
#define POSTPRO_PATCH_ICS_INPROC_ROUTE(a, para, val) ((void)0)

#endif // ANDROID_POSTPRO_NULL_PATCH
