#ifndef ANDROID_SRS_TYPES
#define ANDROID_SRS_TYPES

namespace android {

#ifdef _SRSCFG_ARCH_ARM
	#define SRSSamp			int32_t
	#define SRSCoef16		srs_int16
	#define SRSCoef32		srs_int32
	#define SRSFadeScale		int
	#define SRSFadeScale_Default	256
#endif	// _SRSCFG_ARCH_ARM

#ifdef _SRSCFG_ARCH_X86
	#define SRSSamp			float
	#define SRSCoef16		float
	#define SRSCoef32		float
	#define SRSFadeScale		float
	#define SRSFadeScale_Default	1.0f
#endif	// _SRSCFG_ARCH_X86

struct SRS_Source_Out;
struct SRS_Source_In;

#include "srs_setup.h"

};

#include "srs_subs/srs_spools.h"

#endif	// ANDROID_SRS_TYPES

