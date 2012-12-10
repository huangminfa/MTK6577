#ifndef ANDROID_SRS_ROUTING_API
#define ANDROID_SRS_ROUTING_API

namespace android {
	
#define SRS_ROUTEMAP_MAXROUTES	32

struct SRS_RouteMap {
	int RouteTable[SRS_ROUTEMAP_MAXROUTES];
	int ForceRoute;
	
	SRS_RouteMap();
	
	static SRS_Param* RouteParams();
	static int RouteParamCount();
	
	void RouteMapSet(int index, const char* pValue);
	const char* RouteMapGet(int index);
	
	int ResolveRoute(int routeFlags, int* pFoundFlags);
};

};	// namespace android

#endif // ANDROID_SRS_ROUTING_API
