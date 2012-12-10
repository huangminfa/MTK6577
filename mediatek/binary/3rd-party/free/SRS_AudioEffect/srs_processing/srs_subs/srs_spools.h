#ifndef ANDROID_SRS_SPOOLS_API
#define ANDROID_SRS_SPOOLS_API

extern "C" {
	
void SRS_License_SetInfo(int ident, char* pPath);
int SRS_License_State();
void SRS_License_RandomLUT(unsigned char* pPairs, int pairCount);
	
void* SRS_Spool_Create(int maxSources);
void SRS_Spool_Destroy(void* pSpool);
void* SRS_Spool_SourceFind(void* pSpool, void* pIdent);				// Find a source with pIdent
bool SRS_Spool_SourceDel(void* pSpool, void* pIdent);				// Remove a source with pIdent
bool SRS_Spool_SourceAdd(void* pSpool, void* pSource, void* pIdent);	// Add a source with pIdent
bool SRS_Spool_SourceAvail(void* pSpool);							// How many sources are in use?

void* SRS_Spool_CreateCache();
void SRS_Spool_DestroyCache(void* pCache);
void SRS_Spool_ClearCache(void* pCache);
int SRS_Spool_GetCachePageSize(void* pCache);
void SRS_Spool_GetCachePtrs(void* pCache, void** pIn, void** pOut);
void SRS_Spool_UpdateCachePtrs(void* pCache, void* pIn, void* pOut);

} // extern "C"

#endif // ANDROID_SRS_SPOOLS_API
