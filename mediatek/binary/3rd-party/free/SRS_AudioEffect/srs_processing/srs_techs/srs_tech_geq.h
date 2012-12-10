#ifndef ANDROID_SRS_TECH_GEQ
#define ANDROID_SRS_TECH_GEQ

namespace android {

struct SRS_Tech_GEQ {
	char* pName;
	int16_t Defs[10];
	int16_t Users[10];
};

struct SRS_Source_GEQ;

// Instead of virtuals - or a 'public' class - we'll hide all of GEQ behind this...
extern SRS_Source_GEQ* SRS_Create_GEQ(SRS_Source_Out* pOut);
extern void SRS_Destroy_GEQ(SRS_Source_GEQ* pSrc, SRS_Source_Out* pOut);
extern void SRS_Config_GEQ(SRS_Source_GEQ* pSrc, SRS_Source_Out* pOut, SRS_Tech_GEQ* pCFG, bool bLimit);

extern void SRS_Process_GEQ_256(SRS_Source_GEQ* pSrc, SRSSamp* pData);

extern char* SRS_GetVersion_GEQ(char* pWork, size_t maxBytes);
extern SRS_Param* SRS_GetBank_GEQ(int& paramCount);
extern void SRS_SetParam_GEQ(SRS_Tech_GEQ* pCFG, SRS_Param* pParam, const char* pValue);
extern const char* SRS_GetParam_GEQ(SRS_Tech_GEQ* pCFG, SRS_Param* pParam);
extern void SRS_Default_GEQ(SRS_Tech_GEQ* pCFG);

};

#endif	// ANDROID_SRS_TECH_GEQ

