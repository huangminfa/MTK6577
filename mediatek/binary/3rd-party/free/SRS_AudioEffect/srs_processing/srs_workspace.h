#ifndef ANDROID_SRS_WORKSPACE
#define ANDROID_SRS_WORKSPACE

namespace android {

struct SRS_Source_Out;			// Defined in srs_tech.h - derives from SRS_Base_Source
struct SRS_Source_In;			// Defined in srs_tech.h - derives from SRS_Base_Source
struct SRS_Workspace;			// Defined in srs_tech.h - derives from SRS_Base_Workspace

struct SRS_Base_Source {
	bool DidAPIInit;	// API, buffers, etc...
	int CFGSig;
	void* pSource;
	int Route;
	int SampleRate;
	int ChannelCount;
	SRS_Workspace* pOwner;
	
	SRS_Base_Source();
	~SRS_Base_Source();
};

#define SRS_WORKSOURCES		4

struct SRS_Base_Workspace {
	void* pOutSpool;
	void* pInSpool;
	int ActiveOut;
	int ActiveIn;
	
	int LicenseState;	
	int DidInit;

	int CFGSig;
	int Handle;
	
	SRS_Base_Workspace();
	~SRS_Base_Workspace();
	
	static int CreateWS();
	static void DeleteWS(int handle);
	static SRS_Workspace* GetWS(int handle, int autoId);
	
	static SRS_Workspace** pSW_Stack;
	static int pSW_StackSize;
};

};

#endif	// ANDROID_SRS_WORKSPACE

