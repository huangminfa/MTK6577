#ifndef _MDP_SERVICE_H_
#define _MDP_SERVICE_H_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <MediaHal.h>

#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>
#include <binder/ProcessState.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>
#include <binder/BinderService.h>

#include <cutils/xlog.h>

#undef LOG_TAG
#define LOG_TAG "MdpService" 

namespace android
{
//
//  Holder service for pass objects between processes.
//
class IMdpService : public IInterface 
{
protected:
    enum {
        JPEG_DECODE = IBinder::FIRST_CALL_TRANSACTION,
        JPEG_DECODE_PARSE,
        JPEG_DECODE_INFO,
        MDP_BITBLT
    };
public:
    DECLARE_META_INTERFACE(MdpService);

    virtual int decodeJpg(MHAL_JPEG_DEC_START_IN* inParams, void* procHandler) = 0;
    virtual int parseJpg(unsigned char* addr, unsigned int size, int fd) = 0;
    virtual int getJpgInfo(MHAL_JPEG_DEC_INFO_OUT* outParams) = 0;
    virtual int BitBlt( mHalBltParam_t* bltParam ) = 0;
};

class BnMdpService : public BnInterface<IMdpService> 
{
    virtual status_t onTransact(uint32_t code,
                                const Parcel& data,
                                Parcel* reply,
                                uint32_t flags = 0);
};


class MdpService : 
            public BinderService<MdpService>, 
            public BnMdpService 
{
    friend class BinderService<MdpService>;
public:
    static char const* getServiceName() { return "media.mdp_service"; }
    
    int decodeJpg(MHAL_JPEG_DEC_START_IN* inParams, void* procHandler);
    int parseJpg(unsigned char* addr, unsigned int size, int fd);
    int getJpgInfo(MHAL_JPEG_DEC_INFO_OUT* outParams);
    int BitBlt( mHalBltParam_t* bltParam );
};

};
#endif
