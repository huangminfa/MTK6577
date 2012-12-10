#ifndef ANDROID_SRS_TECH_WOWHD
#define ANDROID_SRS_TECH_WOWHD

namespace android {

struct SRS_Tech_WOWHD {
	float IGain, OGain, BGain;
	bool DoLimit, DoSRS3D, DoTB, DoTBSplit, DoFocus, DoDef, Skip;
	float TBMin, TBWindow, TBSlide, TBCompress;
	float Focus, DefMin, DefWindow, DefSlide, Center, Space, LimitGain;
	int TBFreq, TBAnalyze, TBMode, SRSType, SRSMode;
	
	float TBSlideUDef, DefSlideUDef;
};

struct SRS_Source_WOWHD;

// Instead of virtuals - or a 'public' class - we'll hide all of WOWHD behind this...
extern SRS_Source_WOWHD* SRS_Create_WOWHD(SRS_Source_Out* pOut);
extern void SRS_Destroy_WOWHD(SRS_Source_WOWHD* pSrc, SRS_Source_Out* pOut);
extern void SRS_Config_WOWHD(SRS_Source_WOWHD* pSrc, SRS_Source_Out* pOut, SRS_Tech_WOWHD* pCFG, bool bBypass);

extern void SRS_Process_WOWHD_256(SRS_Source_WOWHD* pSrc, SRSSamp* pIn, SRSSamp* pOut);

extern char* SRS_GetVersion_WOWHD(char* pWork, size_t maxBytes);
extern SRS_Param* SRS_GetBank_WOWHD(int& paramCount);
extern void SRS_SetParam_WOWHD(SRS_Tech_WOWHD* pCFG, SRS_Param* pParam, const char* pValue);
extern const char* SRS_GetParam_WOWHD(SRS_Tech_WOWHD* pCFG, SRS_Param* pParam);
extern void SRS_Default_WOWHD(SRS_Tech_WOWHD* pCFG, const char* pBankName);
extern void SRS_UserDefault_WOWHD(SRS_Tech_WOWHD* pCFG);

};

#endif	// ANDROID_SRS_TECH_WOWHD

