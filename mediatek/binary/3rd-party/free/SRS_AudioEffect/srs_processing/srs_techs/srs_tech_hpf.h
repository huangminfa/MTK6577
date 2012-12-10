#ifndef ANDROID_SRS_TECH_HIPASS
#define ANDROID_SRS_TECH_HIPASS

namespace android {

struct SRS_Tech_HiPass {
	bool Is32Bit;
	
	int Order;
	int Frequency;
	
	bool Skip;
};

struct SRS_Source_HiPass;

// Instead of virtuals - or a 'public' class - we'll hide all of HiPass behind this...
extern SRS_Source_HiPass* SRS_Create_HiPass(SRS_Source_Out* pOut);
extern void SRS_Destroy_HiPass(SRS_Source_HiPass* pSrc, SRS_Source_Out* pOut);
extern void SRS_Config_HiPass(SRS_Source_HiPass* pSrc, SRS_Source_Out* pOut, SRS_Tech_HiPass* pCFG, bool bBypass);

extern void SRS_Process_HiPass_256(SRS_Source_HiPass* pSrc, SRSSamp* pData);

extern char* SRS_GetVersion_HiPass(char* pWork, size_t maxBytes);
extern SRS_Param* SRS_GetBank_HiPass(int& paramCount);
extern void SRS_SetParam_HiPass(SRS_Tech_HiPass* pCFG, SRS_Param* pParam, const char* pValue);
extern const char* SRS_GetParam_HiPass(SRS_Tech_HiPass* pCFG, SRS_Param* pParam);
extern void SRS_Default_HiPass(SRS_Tech_HiPass* pCFG);

};

#endif	// ANDROID_SRS_TECH_HIPASS

