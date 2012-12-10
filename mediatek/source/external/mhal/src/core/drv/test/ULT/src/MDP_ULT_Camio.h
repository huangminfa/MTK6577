/********************************************************************************************
 *     LEGAL DISCLAIMER
 *
 *     (Header of MediaTek Software/Firmware Release or Documentation)
 *
 *     BY OPENING OR USING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *     THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE") RECEIVED
 *     FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON AN "AS-IS" BASIS
 *     ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES, EXPRESS OR IMPLIED,
 *     INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 *     A PARTICULAR PURPOSE OR NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY
 *     WHATSOEVER WITH RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK
 *     ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
 *     NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S SPECIFICATION
 *     OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
 *
 *     BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE LIABILITY WITH
 *     RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION,
TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE
 *     FEES OR SERVICE CHARGE PAID BY BUYER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE WITH THE LAWS
 *     OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF LAWS PRINCIPLES.
 ************************************************************************************************/
#ifndef _MDP_ULT_CAMIO_H_
#define _MDP_ULT_CAMIO_H_

#include "mdp_hal.h"

/*******************************************************************************
*
********************************************************************************/
class MdpUltCam : public MdpHal {
public:
    static MdpUltCam* getInstance();
    virtual void destroyInstance();
//
//protected:
private:
    MdpUltCam();
    virtual ~MdpUltCam();
//
public:
    MINT32 init();
    //
    MINT32 uninit();
    //
    MINT32 start();
    //
    MINT32 stop();
    //
    MINT32 setPrv(halIDPParam_t *phalIDPParam);
    //
    MINT32 setCapJpg(halIDPParam_t *phalIDPParam);
    //
    MINT32 setConf(halIDPParam_t *phalIDPParam);
    //
    MINT32 dequeueBuff(halMdpOutputPort_e e_Port , halMdpBufInfo_t * a_pstBuffInfo);
    //
    MINT32 enqueueBuff(halMdpOutputPort_e e_Port);
    //
    MINT32 calCropRect(rect_t rSrc, rect_t rDst, rect_t *prCrop, MUINT32 zoomRatio);
    //
    MINT32 waitDone(MINT32 mode);
    //
    MINT32 dumpReg();
    //
    MINT32 sendCommand(int cmd, int *parg1 = NULL, int *parg2 = NULL, int *parg3 = NULL);
//
private:
	
    //
    halIDPParam_t mMDPParam;
    //
    typedef struct {
        unsigned long u4Active; 
        unsigned long u4ReaderPosition;
        unsigned long u4BufferCnt;
        unsigned long u4PreStat;
    } stRingBuffer_PrivateData;
    //
    stRingBuffer_PrivateData mDISPRingBuffer;
    stRingBuffer_PrivateData mVDOENCRingBuffer;
    stRingBuffer_PrivateData mFDRingBuffer;
    //
    stRingBuffer_PrivateData mJPEGRingBuffer;
    stRingBuffer_PrivateData mQUICKVIEWRingBuffer;
    stRingBuffer_PrivateData mTHUMBNAILRingBuffer;
    //
    MINT32 lockResource(MUINT32 res);
    MINT32 unlockResource(MUINT32 res);
	
//
};

#endif // _MDP_HAL_IMP_H_

