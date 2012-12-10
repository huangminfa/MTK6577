#ifndef ANDROID_SRS_TECH_USERPEQ
#define ANDROID_SRS_TECH_USERPEQ

namespace android {
	
struct SRS_Tech_TruEQ;
	
struct SRS_Tech_UserPEQ_CFG {
	float IGain, OGain, BGain;
	
	bool Enabled;	// User-facing PEQ is enabled?	
	int Preset_Int;	// Internal Preset
	
	bool Skip;		// Skip processing entirely (instead of bypass)
};

struct SRS_Tech_UserPEQ_Preset {
	char* pName;
	float UserParams[4][3];	// 4 bands, 3 params (center freq, gain, q)
	float DefParams[4][3];	// 4 bands, 3 params (center freq, gain, q)
};

struct SRS_Source_UserPEQ;

// Instead of virtuals - or a 'public' class - we'll hide all of UserPEQ behind this...
extern SRS_Param* SRS_GetBank_UserPEQ_CFG(int& paramCount);
extern void SRS_SetParam_UserPEQ_CFG(SRS_Tech_UserPEQ_CFG* pCFG, SRS_Param* pParam, const char* pValue);
extern const char* SRS_GetParam_UserPEQ_CFG(SRS_Tech_UserPEQ_CFG* pCFG, SRS_Param* pParam);
extern void SRS_Default_UserPEQ_CFG(SRS_Tech_UserPEQ_CFG* pCFG);

extern SRS_Param* SRS_GetBank_UserPEQ_Preset(int& paramCount);
extern void SRS_SetParam_UserPEQ_Preset(SRS_Tech_UserPEQ_Preset* pCFG, SRS_Param* pParam, const char* pValue);
extern const char* SRS_GetParam_UserPEQ_Preset(SRS_Tech_UserPEQ_Preset* pCFG, SRS_Param* pParam);
extern void SRS_Default_UserPEQ_Preset(SRS_Tech_UserPEQ_Preset* pCFG);

extern void SRS_Apply_UserPEQ_CFG(SRS_Tech_TruEQ* pTruEQ, SRS_Tech_UserPEQ_CFG* pCFG);
extern void SRS_Apply_UserPEQ_Preset(SRS_Tech_TruEQ* pTruEQ, SRS_Tech_UserPEQ_Preset* pCFG);
};

#endif	// ANDROID_SRS_TECH_USERPEQ

