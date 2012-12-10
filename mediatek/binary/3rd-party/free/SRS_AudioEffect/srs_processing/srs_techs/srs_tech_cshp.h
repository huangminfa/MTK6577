#ifndef ANDROID_SRS_TECH_CSHP
#define ANDROID_SRS_TECH_CSHP

namespace android {

struct SRS_Tech_CSHP {
	float IGain, OGain, BGain;
	bool DoTB, DoTBSplit, DoDef, DoDecode, DoDialog, DoLimit, Skip;
	float TBMin, TBWindow, TBSlide, TBCompress;
	float DefMin, DefWindow, DefSlide, Dialog, LimitMGain;
	int DecodeMode, TBFreq, TBAnalyze, TBMode;
	
	float TBSlideUDef, DefSlideUDef;
};

struct SRS_Source_CSHP;

// Instead of virtuals - or a 'public' class - we'll hide all of CSHP behind this...
extern SRS_Source_CSHP* SRS_Create_CSHP(SRS_Source_Out* pOut);
extern void SRS_Destroy_CSHP(SRS_Source_CSHP* pSrc, SRS_Source_Out* pOut);
extern void SRS_Config_CSHP(SRS_Source_CSHP* pSrc, SRS_Source_Out* pOut, SRS_Tech_CSHP* pCFG, bool bBypass);

extern void SRS_Process_CSHP_256(SRS_Source_CSHP* pSrc, SRSSamp* pData);

extern char* SRS_GetVersion_CSHP(char* pWork, size_t maxBytes);
extern SRS_Param* SRS_GetBank_CSHP(int& paramCount);
extern void SRS_SetParam_CSHP(SRS_Tech_CSHP* pCFG, SRS_Param* pParam, const char* pValue);
extern const char* SRS_GetParam_CSHP(SRS_Tech_CSHP* pCFG, SRS_Param* pParam);
extern void SRS_Default_CSHP(SRS_Tech_CSHP* pCFG);
extern void SRS_UserDefault_CSHP(SRS_Tech_CSHP* pCFG);

};

#endif	// ANDROID_SRS_TECH_CSHP

