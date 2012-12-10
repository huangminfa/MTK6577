//----------------------------------------------------------------------------
#ifndef VIDEO_SNAPSHOT_IMP_H
#define VIDEO_SNAPSHOT_IMP_H
//----------------------------------------------------------------------------
#include <cutils/xlog.h>
#include <utils/threads.h>
#include "VideoSnapshot.h"
#include "mdp_hal.h"
#include "ICameraIO.h"
#include <mhal/inc/MediaHal.h>
//----------------------------------------------------------------------------
using namespace android;
//----------------------------------------------------------------------------
#define LOG_MSG(fmt, arg...)    XLOGD(""          fmt,           ##arg)
#define LOG_WRN(fmt, arg...)    XLOGW("WRN(%5d):" fmt, __LINE__, ##arg)
#define LOG_ERR(fmt, arg...)    XLOGE("ERR(%5d):" fmt, __LINE__, ##arg)
#define LOG_DMP(fmt, arg...)    XLOGE(""          fmt,           ##arg)
//----------------------------------------------------------------------------
#define VIDEO_SNAPSHOT_BUF_OFST_EXIF    (0)
#define VIDEO_SNAPSHOT_BUF_OFST_JPG     (VIDEO_SNAPSHOT_BUF_OFST_EXIF+2*1024)
#define VIDEO_SNAPSHOT_BUF_OFST_VSS     (VIDEO_SNAPSHOT_BUF_OFST_JPG+512*1024)
#define VIDEO_SNAPSHOT_JPG_SOI_LEN      (2)
#define VIDEO_SNAPSHOT_HW_JPG_SUPPORT   (0)
//----------------------------------------------------------------------------
typedef enum
{
    VIDEO_SNAPSHOT_THREAD_STATE_NONE,
    VIDEO_SNAPSHOT_THREAD_STATE_CREATE,
    VIDEO_SNAPSHOT_THREAD_STATE_END
}VIDEO_SNAPSHOT_THREAD_STATE_ENUM;
//----------------------------------------------------------------------------
class VideoSnapshotImp : public VideoSnapshot
{
    protected:
        VideoSnapshotImp();
        ~VideoSnapshotImp();
    //
    public:
        static VideoSnapshot* GetInstance(void);
        virtual void    DestroyInstance(void);
        virtual MINT32  Init(void);
        virtual MINT32  Uninit(void);
        virtual MINT32  Config(VIDEO_SNAPSHOT_CONFIG_STRUCT* pConfig);
        virtual MINT32  Release(void);
        virtual MINT32  TakePic(MUINT32 Rotation);
        virtual MINT32  SetFrame(MUINT32 BufAddr);
        virtual MINT32  ProcessImg(void);
        virtual MUINT32 GetAddr(VIDEO_SNAPSHOT_BUF_TYPE_ENUM BufType);
        virtual MINT32  AddExif(void);
    //
    private:
        volatile int    mUsers;
        mutable Mutex   mLock;
        MBOOL           mIsFirstFrame;
        MBOOL           mIsConfig;
        MBOOL           mIsTakePic;
        MUINT32         mBufVirAddr;
        MUINT32         mFrameVirAddr;
        MUINT32         mJpgSize;
        MUINT32         mRotation;
        ICameraIO*      mpCameraIOObj;
        ICameraIO::BuffInfo_t           mBufInfo;
        VIDEO_SNAPSHOT_CONFIG_STRUCT    mConfig;
        mHalRegisterLoopMemoryObj_t     mRegMemObj;
};
//----------------------------------------------------------------------------
#endif

