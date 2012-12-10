#ifndef ANDROID_SRS_TECH_HLimit
#define ANDROID_SRS_TECH_HLimit

namespace android {

struct SRS_Tech_HLimit {
	bool Skip;
	float IGain, OGain, BGain;
	int DelayLen;
	float Boost, Limit;
	bool DecaySmooth;
	
	// Not sure how to use these?
	//void		SRS_SetHardLimiterLCoef(SRSHardLimiterObj hlObj, srs_int32 lmtcoef);
	//void		SRS_SetHardLimiterHLThresh(SRSHardLimiterObj hlObj, srs_int32 hlthresh);
};

struct SRS_Source_HLimit;

// Instead of virtuals - or a 'public' class - we'll hide all of HLimit behind this...
extern SRS_Source_HLimit* SRS_Create_HLimit(SRS_Source_Out* pOut);
extern void SRS_Destroy_HLimit(SRS_Source_HLimit* pSrc, SRS_Source_Out* pOut);
extern void SRS_Config_HLimit(SRS_Source_HLimit* pSrc, SRS_Source_Out* pOut, SRS_Tech_HLimit* pCFG, bool bBypass);

extern void SRS_Process_HLimit_256(SRS_Source_HLimit* pSrc, SRSSamp* pIn, SRSSamp* pOut);

extern char* SRS_GetVersion_HLimit(char* pWork, size_t maxBytes);
extern SRS_Param* SRS_GetBank_HLimit(int& paramCount);
extern void SRS_SetParam_HLimit(SRS_Tech_HLimit* pCFG, SRS_Param* pParam, const char* pValue);
extern const char* SRS_GetParam_HLimit(SRS_Tech_HLimit* pCFG, SRS_Param* pParam);
extern void SRS_Default_HLimit(SRS_Tech_HLimit* pCFG, bool Boosted);

};

#endif	// ANDROID_SRS_TECH_HLimit

