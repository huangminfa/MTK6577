#ifndef ANDROID_SRS_SETUP
#define ANDROID_SRS_SETUP

// Path Stringizers

#define SRS_STR(s) DOSRS_STR(s)		// Trick to allow values of defines to become strings properly...
#define DOSRS_STR(s) #s

#ifdef _SRSCFG_ARCH_ARM
	#define SRSLIBINC(file) DOSRS_STR( srs_include_arm/file )
	#include SRSLIBINC(srs_fxp.h)
#endif	// _SRSCFG_ARCH_ARM

#ifdef _SRSCFG_ARCH_X86
	#define SRSLIBINC(file) DOSRS_STR( srs_include_x86/file )
	#define SRS_FXP16(val,iwl) val
	#define SRS_FXP32(val,iwl) val
#endif	// _SRSCFG_ARCH_X86

// Didn't allow Logging?
#ifndef SRS_VERBOSE

#ifdef SRS_LOG		// Mute Android's own LOG macro (add other macros to mute as needed)
	#undef SRS_LOG
#endif

#define SRS_LOG(...)   ((void)0)

#else	// SRS_VERBOSE

#ifdef SRS_LOG		// Mute Android's own LOG macro (add other macros to mute as needed)
	#undef SRS_LOG
#endif

//#if (SRS_AND_PLAT_INDEX < 8)
#if (SRS_AND_PLAT_INDEX < 10)
#define SRS_LOG(...)   ((void)LOG(LOG_VERBOSE, LOG_TAG, __VA_ARGS__))
#endif	// SRS_AND_PLAT_INDEX < 8

//#if (SRS_AND_PLAT_INDEX >= 8)
#if (SRS_AND_PLAT_INDEX >= 10)
#define SRS_LOG(...)   ((void)ALOG(LOG_VERBOSE, LOG_TAG, __VA_ARGS__))
#endif 	// SRS_AND_PLAT_INDEX >= 8

#endif	// SRS_VERBOSE


#endif	// ANDROID_SRS_SETUP

