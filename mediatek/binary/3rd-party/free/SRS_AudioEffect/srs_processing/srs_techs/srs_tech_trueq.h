#ifndef ANDROID_SRS_TECH_TruEQ
#define ANDROID_SRS_TECH_TruEQ

namespace android {

struct SRS_Tech_TruEQ {
	bool Skip;
	float IGain, OGain, BGain;
	
	bool LEnable;
	bool REnable;
	bool LBands[4];
	bool RBands[4];	
	
	float Params[8][3];	// 2 channels, 4 bands, 3 params (center freq, gain, q)
};

struct SRS_Source_TruEQ;

// Instead of virtuals - or a 'public' class - we'll hide all of TruEQ behind this...
extern SRS_Source_TruEQ* SRS_Create_TruEQ(SRS_Source_Out* pOut);
extern void SRS_Destroy_TruEQ(SRS_Source_TruEQ* pSrc, SRS_Source_Out* pOut);
extern void SRS_Config_TruEQ(SRS_Source_TruEQ* pSrc, SRS_Source_Out* pOut, SRS_Tech_TruEQ* pCFG, bool bBypass);

extern void SRS_Process_TruEQ_256(SRS_Source_TruEQ* pSrc, SRSSamp* pData);

extern char* SRS_GetVersion_TruEQ(char* pWork, size_t maxBytes);
extern SRS_Param* SRS_GetBank_TruEQ(int& paramCount);
extern void SRS_SetParam_TruEQ(SRS_Tech_TruEQ* pCFG, SRS_Param* pParam, const char* pValue);
extern const char* SRS_GetParam_TruEQ(SRS_Tech_TruEQ* pCFG, SRS_Param* pParam);
extern void SRS_Default_TruEQ(SRS_Tech_TruEQ* pCFG);

};

#endif	// ANDROID_SRS_TECH_TruEQ

