#ifndef VIDEO_SNAPSHOT_H
#define VIDEO_SNAPSHOT_H
//-----------------------------------------------------------------------------
#include <mhal/inc/camera/types.h>
#include <cam_types.h>
#include <aaa_hal_base.h>
//-----------------------------------------------------------------------------
using namespace NSCamera;
//-----------------------------------------------------------------------------
#define VIDEO_SNAPSHOT_SKIA_WIDTH       (1280)
//-----------------------------------------------------------------------------
typedef enum
{
    VIDEO_SNAPSHOT_BUF_TYPE_EXIF,
    VIDEO_SNAPSHOT_BUF_TYPE_JPG,
    VIDEO_SNAPSHOT_BUF_TYPE_VSS
}VIDEO_SNAPSHOT_BUF_TYPE_ENUM;

typedef struct
{
    MUINT32         Width;
    MUINT32         Height;
    ESensorType     SensorType;
    EDeviceId       DeviceId;
    Hal3ABase*      pHal3AObj;
    mhalCamParam_s* pCamParam;
    mHalCamObserver CB;
}VIDEO_SNAPSHOT_CONFIG_STRUCT;
//----------------------------------------------------------------------------
class VideoSnapshot
{
    protected:
        virtual ~VideoSnapshot() {};
    //
    public:
        static VideoSnapshot* CreateInstance(void);
        virtual void    DestroyInstance(void) = 0;
        virtual MINT32  Init(void) = 0;
        virtual MINT32  Uninit(void) = 0;
        virtual MINT32  Config(VIDEO_SNAPSHOT_CONFIG_STRUCT* pConfig) = 0;
        virtual MINT32  Release(void) = 0;
        virtual MINT32  TakePic(MUINT32 Rotation) = 0;
        virtual MINT32  SetFrame(MUINT32 BufAddr) = 0;
        virtual MUINT32 GetAddr(VIDEO_SNAPSHOT_BUF_TYPE_ENUM BufType) = 0;
};
//-----------------------------------------------------------------------------
#endif

