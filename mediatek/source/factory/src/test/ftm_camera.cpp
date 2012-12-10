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
/*******************************************************************************
 *
 * $Log: ftm_camera.cpp $
 *
 * 10 21 2010 sean.cheng
 * [ALPS00130683] [Factory Mode] The cursor will first at "Test Pass" then at "Capture\strobe\preview" when enter camera test item in this case
 * Do do the redraw item.
 *
 * 08 27 2010 sean.cheng
 * [ALPS00003622] [Critical Patch][Factory Mode][Camera] Optimize the camera factory item, merge preview/capture/strobe in one test item
 * .Optimize the camera test items
 *
 * 08 23 2010 sean.cheng
 * [ALPS00003542] [Critical Patch][Factory Camera] Fix strobe function to decrease it test time
 * .add a thread to handle flash test.
 *
 * 07 05 2010 sean.cheng
 * [ALPS00121433][Factory Mode] Please remove "Return" from Camera 
 * .remove RETURN items
 *
 * 06 29 2010 sean.cheng
 * [ALPS00002778][Need Patch] [Volunteer Patch]Camera Factory mode feature change 
 * .change factory camera feature, if there is no strobe HW, no strobe test items
 *
 * 06 22 2010 sean.cheng
 * [ALPS00120788][Factory Mode] Both "Preview" and "Strobe" in Camera have test result, this will effect camera test result in test report 
 * .separate the 3 test results
 *
 * 06 14 2010 sean.cheng
 * [ALPS00002514][Need Patch] [Volunteer Patch] ALPS.10X.W10.11 Volunteer patch for E1k Camera 
 * .
 * Add strobe test for factory mode
 *
 * 06 13 2010 sean.cheng
 * [ALPS00002514][Need Patch] [Volunteer Patch] ALPS.10X.W10.11 Volunteer patch for E1k Camera 
 * .
 * 1. Add set zoom factor and capdelay frame for YUV sensor 
 * 2. Modify e1k sensor setting
 *
 * 05 07 2010 sean.cheng
 * [ALPS00006039][FactoryMode] It will exit automatically in Camera app. 
 * .
 *
 * 04 27 2010 sean.cheng
 * [ALPS00005457][FactoryMode] Screen winks in Camera->Preview 
 * .Fix the bug that screen always winks
 *
 *******************************************************************************/
#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <pthread.h>
#include <sys/mount.h>
#include <sys/statfs.h>

extern "C" {
#include "common.h"
#include "miniui.h"
#include "ftm.h"
} 

#ifdef FEATURE_FTM_MAIN_CAMERA

#include "AcdkIF.h"
//#include "msdk_flash_light_exp.h"
#include "camera_custom_flashlight.h"

#define TAG                  "[CAMERA] "

#define DEBUG_FTM_CAMERA
#ifdef DEBUG_FTM_CAMERA
#define FTM_CAMERA_DBG(fmt, arg...) LOGD(fmt, ##arg)
#define FTM_CAMERA_ERR(fmt, arg...)  LOGE("Err: %5d:, "fmt, __LINE__, ##arg)
#else
#define FTM_CAMERA_DBG(a,...)
#define FTM_CAMERA_ERR(a,...)
#endif

/*******************************************************************************
*
********************************************************************************/
enum {
    ITEM_PASS,
    ITEM_FAIL,
    ITEM_RETURN,
    ITEM_CAMERA_TEST, 
//    ITEM_CAPTURE,
//    ITEM_STROBE, 
};

//
/*******************************************************************************
*
********************************************************************************/
static item_t camera_items[] = {
    {ITEM_CAMERA_TEST, "Preview/Capture",   0}, 
    {ITEM_PASS, "Test Pass", 0}, 
    {ITEM_FAIL, "Test Fail", 0}, 
    {-1, NULL,   0},       
};

/*******************************************************************************
*
********************************************************************************/
struct camera {
    char  info[1024];
    bool  exit_thd;
    
    text_t    title;
    text_t    text;
    text_t    left_btn;
    text_t    center_btn;
    text_t    right_btn;

    text_t    cap_left_btn; 
    text_t    cap_center_btn; 
    text_t    cap_right_btn; 
        
    pthread_t update_button_thd;
    pthread_t update_cap_thd; 
    struct ftm_module *mod;
    struct textview tv;
    struct itemview *iv;
//    struct imageview imv;
//    struct paintview pv; 
};

#define mod_to_camera(p)     (struct camera*)((char*)(p) + sizeof(struct ftm_module))

#define FREEIF(p)   do { if(p) free(p); (p) = NULL; } while(0)

pthread_t camera_flash_thread_handle = 0;
FLASHLIGHT_TYPE_ENUM eFlashSupport = FLASHLIGHT_NONE;
UINT8 srcDev = 0;

/*******************************************************************************
*
********************************************************************************/
static bool bSendDataToACDK(ACDK_CCT_FEATURE_ENUM	FeatureID,
                                                              UINT8*        pInAddr,
                                                              UINT32  nInBufferSize,
                                                              UINT8*  pOutAddr,
                                                              UINT32  nOutBufferSize,
                                                              UINT32* pRealOutByeCnt)
{
    ACDK_FEATURE_INFO_STRUCT rAcdkFeatureInfo; 

    rAcdkFeatureInfo.puParaIn = pInAddr; 
    rAcdkFeatureInfo.u4ParaInLen = nInBufferSize; 
    rAcdkFeatureInfo.puParaOut = pOutAddr; 
    rAcdkFeatureInfo.u4ParaOutLen = nOutBufferSize; 
    rAcdkFeatureInfo.pu4RealParaOutLen = pRealOutByeCnt; 
    

    return (MDK_IOControl(FeatureID, &rAcdkFeatureInfo));
}

/*******************************************************************************
*
********************************************************************************/
enum 
{
    CAMERA_STATE_NONE, 
    CAMERA_STATE_IDLE, 
    CAMERA_STATE_PREVIEW,
};

#define MEDIA_PATH "/data/"
static unsigned int g_u4ImgCnt = 0; 
static bool bCapDone = FALSE; 
static int camera_state = CAMERA_STATE_NONE; 
static char szFileName[256]; 

/////////////////////////////////////////////////////////////////////////
//
//   camera_preview_test () -
//!
//!  brief for camera preview test 
//
/////////////////////////////////////////////////////////////////////////
static int camera_preview_test()
{
    FTM_CAMERA_DBG("ACDK_CCT_OP_PREVIEW_LCD_START\n"); 

    ACDK_CCT_CAMERA_PREVIEW_STRUCT rCCTPreviewConfig; 
    rCCTPreviewConfig.fpPrvCB = NULL; 
    rCCTPreviewConfig.u2PreviewWidth = 320; 
    rCCTPreviewConfig.u2PreviewHeight = 240; 
  
    unsigned int u4RetLen = 0; 
   
    bool bRet = bSendDataToACDK (ACDK_CCT_OP_PREVIEW_LCD_START, (unsigned char *)&rCCTPreviewConfig, 
                                                                                                                sizeof(ACDK_CCT_CAMERA_PREVIEW_STRUCT),
                                                                                                                NULL,
                                                                                                                0, 
                                                                                                                &u4RetLen);     
    if (!bRet) {
        FTM_CAMERA_ERR("ACDK_CCT_OP_PREVIEW_LCD_START Fail\n"); 
        return 1; 
    }

    camera_state = CAMERA_STATE_PREVIEW; 
    
    return 0;
}

/////////////////////////////////////////////////////////////////////////
//
//   camera_flash_test_thread () -
//!
//!  brief for camera flash test thread 
//
/////////////////////////////////////////////////////////////////////////
static void *camera_flash_test_thread(void *priv)
{
    unsigned int u4RetLen = 0; 
    ACDK_FLASH_CONTROL flashCtrl; 

    memset (&flashCtrl, 0, sizeof(ACDK_FLASH_CONTROL)); 
    flashCtrl.level = 32;       //max is 32, min is 0 
    flashCtrl.duration = 50000; 

    struct camera *cam = (struct camera *)priv;
    
    FTM_CAMERA_DBG("ACDK_CCT_OP_FLASH_CONTROL ON \n"); 

    while (!cam->exit_thd) {
        bSendDataToACDK(ACDK_CCT_OP_FLASH_CONTROL, (UINT8*)&flashCtrl, (UINT32)sizeof(int),NULL, 0, &u4RetLen); 
        usleep(100000); 
    }
    return NULL;
}

/////////////////////////////////////////////////////////////////////////
//
//   mrSaveJPEGImg () -
//!
//!  brief for geneirc function to save image file 
//
/////////////////////////////////////////////////////////////////////////
bool bSaveJPEGImg(char *a_pBuf,  unsigned int a_u4Size)
{
    sprintf(szFileName, "%s//%04d.jpg" , MEDIA_PATH, g_u4ImgCnt);

    FILE *pFp = fopen(szFileName, "wb");
 
    if (NULL == pFp ) {
        FTM_CAMERA_ERR("Can't open file to save Image\n"); 
        return FALSE; 
    }

    
    int i4WriteCnt = fwrite(a_pBuf, 1, a_u4Size , pFp);		

    FTM_CAMERA_DBG("Save image file name:%s\n", szFileName); 

    fclose(pFp);
    sync();
    return TRUE; 
}


/////////////////////////////////////////////////////////////////////////
//
//   vCapCb () -
//!
//!  brief for capture callback 
//
/////////////////////////////////////////////////////////////////////////
static void vCapCb(void *a_pParam)
{
    FTM_CAMERA_DBG("Capture Callback \n"); 

    ImageBufInfo *pImgBufInfo = (ImageBufInfo *)a_pParam; 

   if (pImgBufInfo->eImgType == JPEG_TYPE) {
        FTM_CAMERA_DBG("Size:%d\n", pImgBufInfo->rCapBufInfo.u4ImgSize); 
        FTM_CAMERA_DBG("Width:%d\n", pImgBufInfo->rCapBufInfo.u2ImgXRes); 
        FTM_CAMERA_DBG("Height:%d\n", pImgBufInfo->rCapBufInfo.u2ImgYRes);
            
        bSaveJPEGImg((char*)pImgBufInfo->rCapBufInfo.pucImgBuf, 
                                             pImgBufInfo->rCapBufInfo.u4ImgSize); 
   }
   else {
        FTM_CAMERA_DBG("UnKnow Format \n"); 
        bCapDone = FALSE; 
        return; 
   }

    bCapDone = TRUE;     
    g_u4ImgCnt ++; 
}

/////////////////////////////////////////////////////////////////////////
//
//   camera_capture_test () -
//!
//!  brief for camera capture test 
//
/////////////////////////////////////////////////////////////////////////
static int camera_capture_test()
{

    FTM_CAMERA_DBG("Camera_Capture_Test \n"); 


    FTM_CAMERA_DBG("Get Sensor Resolution Info\n"); 
    ACDK_CCT_SENSOR_RESOLUTION_STRUCT  SensorResolution;   
    memset(&SensorResolution,0,sizeof(ACDK_CCT_SENSOR_RESOLUTION_STRUCT));

    unsigned int u4RetLen = 0; 

    bool bRet = bSendDataToACDK(ACDK_CCT_V2_OP_GET_SENSOR_RESOLUTION, 
                                                        NULL, 
                                                        0, 
                                                        (UINT8 *)&SensorResolution, 
                                                        sizeof(ACDK_CCT_SENSOR_RESOLUTION_STRUCT),
                                                        &u4RetLen); 
    
    
    if (!bRet) {
        FTM_CAMERA_DBG("Get Sensor Resolution Fail \n"); 
        return 1; 
    }
    
    ACDK_CCT_STILL_CAPTURE_STRUCT rCCTStillCapConfig; 

    rCCTStillCapConfig.eCameraMode = FACTORY_MODE; 
    rCCTStillCapConfig.eOutputFormat = OUTPUT_JPEG; 
    
    //align to 16x 
    rCCTStillCapConfig.u2JPEGEncWidth =  SensorResolution.SensorFullWidht & (~0xF); 
    rCCTStillCapConfig.u2JPEGEncHeight =  SensorResolution.SensorFullHeight & (~0xF); 
    rCCTStillCapConfig.fpCapCB = vCapCb; 

    bCapDone = FALSE; 
    bRet = bSendDataToACDK(ACDK_CCT_OP_SINGLE_SHOT_CAPTURE_EX, (unsigned char *)&rCCTStillCapConfig, 
                                                                                                                     sizeof(ACDK_CCT_STILL_CAPTURE_STRUCT), 
                                                                                                                     NULL, 
                                                                                                                     0,
                                                                                                                     &u4RetLen); 

    //wait JPEG Done; 
    while (!bCapDone) {
        usleep(1000); 
    }

    if (!bRet) {
        return 1; 
    }
    
    return 0; 
}


/////////////////////////////////////////////////////////////////////////
//
//   camera_preview_stop () -
//!
//!  brief for camera preview stop
//
/////////////////////////////////////////////////////////////////////////
static int camera_preview_stop(void)
{
    if (camera_state != CAMERA_STATE_PREVIEW) {
        return 0; 
    }

    FTM_CAMERA_DBG("Stop Camera Preview \n"); 

    unsigned int u4RetLen = 0; 
    bool bRet = bSendDataToACDK(ACDK_CCT_OP_PREVIEW_LCD_STOP, NULL, 0, NULL, 0, &u4RetLen);

    if (!bRet) {
        return 1;
    }

    camera_state = CAMERA_STATE_IDLE; 
    return 0; 
}


/////////////////////////////////////////////////////////////////////////
//
//   camera_reset_layer_buffer () -
//!
//!  brief for camera reset camera preview layer buffer 
//
/////////////////////////////////////////////////////////////////////////
static int camera_reset_layer_buffer(void)
{
    unsigned int u4RetLen = 0; 
    bool bRet = 0; 
    ACDK_FEATURE_INFO_STRUCT rAcdkFeatureInfo; 

    rAcdkFeatureInfo.puParaIn = NULL; 
    rAcdkFeatureInfo.u4ParaInLen = 0; 
    rAcdkFeatureInfo.puParaOut = NULL; 
    rAcdkFeatureInfo.u4ParaOutLen = 0; 
    rAcdkFeatureInfo.pu4RealParaOutLen = &u4RetLen; 
    
    bRet = MDK_IOControl(ACDK_CCT_FEATURE_RESET_LAYER_BUFFER, &rAcdkFeatureInfo);
    if (!bRet) {
        return 1;
    }
    return 0;      
}

/////////////////////////////////////////////////////////////////////////
//
//   vQuickViewCallback () -
//!
//!  brief for camera quick view callback 
//
/////////////////////////////////////////////////////////////////////////

static volatile int quickViewDone = FALSE; 
VOID vQuickViewCallback(VOID *a_pArg)
{
    FTM_CAMERA_DBG("Quick View the image callback \n");
    
    quickViewDone = TRUE; 
}

/////////////////////////////////////////////////////////////////////////
//
//   camera_show_image () -
//!
//!  brief for camera show image on the screen 
//
/////////////////////////////////////////////////////////////////////////
static int camera_show_image(void)
{
    if (camera_state != CAMERA_STATE_IDLE) {
        return 0; 
    }

    FTM_CAMERA_DBG("QuickView the Image  \n"); 

    unsigned int u4RetLen = 0; 
    ACDK_FEATURE_INFO_STRUCT rAcdkFeatureInfo; 
    rAcdkFeatureInfo.puParaIn =  (UINT8*)szFileName; 
    rAcdkFeatureInfo.u4ParaInLen = 256; 
    rAcdkFeatureInfo.puParaOut = (UINT8*)vQuickViewCallback; 
    rAcdkFeatureInfo.u4ParaOutLen = 0; 
    rAcdkFeatureInfo.pu4RealParaOutLen =&u4RetLen; 

    bool bRet = MDK_IOControl(ACDK_CCT_FEATURE_QUICK_VIEW_IMAGE, &rAcdkFeatureInfo);

    if (!bRet) {
        return 1;
    }
    return 0; 
}



/*******************************************************************************
*
********************************************************************************/
static volatile int capture_done = FALSE; 
static int camera_cap_key_handler(int key, void *priv) 
{
    int handled = 0, exit = 0;
    struct camera *cam = (struct camera *)priv;
    struct textview *tv = &cam->tv;
    struct ftm_module *fm = cam->mod;
    
    switch (key) {
    case UI_KEY_CONFIRM:
        FTM_CAMERA_DBG("Back Button Click - Capture \n"); 
        if (camera_capture_test() == 0) {
            capture_done = TRUE; 
        } 
        exit = 1;
        break;
    case UI_KEY_BACK:
        FTM_CAMERA_DBG("Key_Confirm Click - Back\n");                 
        exit = 1;
        break;        
    default:
        handled = -1;
        break;
    }
    if (exit) {
        FTM_CAMERA_DBG( "%s: Exit thead\n", __FUNCTION__);
        cam->exit_thd = true;
        tv->exit(tv);        
    }
    return handled;
}

/*******************************************************************************
*
********************************************************************************/
static int camera_preview_key_handler(int key, void *priv) 
{
    int handled = 0, exit = 0;
    struct camera *cam = (struct camera *)priv;
    struct textview *tv = &cam->tv;
    struct ftm_module *fm = cam->mod;
    
    switch (key) {
    case UI_KEY_CONFIRM:
        FTM_CAMERA_DBG("Key Confirm press \n"); 
        cam->mod->test_result = FTM_TEST_PASS;         
        exit = 1; 
        break;
    case UI_KEY_BACK:
        FTM_CAMERA_DBG("Key back press \n");                 
        cam->mod->test_result = FTM_TEST_FAIL;         
        exit = 1; 
        break;        
    default:
        handled = -1;
        break;
    }
    if (exit) {
        FTM_CAMERA_DBG( "%s: Exit thead\n", __FUNCTION__);
        cam->exit_thd = true;
        tv->exit(tv);        
    }
    return handled;
}


/*******************************************************************************
*
********************************************************************************/
static void *camera_update_capture_tv_thread(void *priv)
{
    struct camera *cam = (struct camera *)priv;
    struct textview *tv = &cam->tv;
    struct statfs stat;
    int count = 1, chkcnt = 5;
    int key; 

    //tv = &cam ->tv;
    ui_init_textview(tv, camera_cap_key_handler, (void*)cam );
    tv->set_title(tv, &cam->title);
    tv->set_text(tv, &cam->text);    
    tv->set_btn(tv, &cam->left_btn, &cam->center_btn, &cam->right_btn);
    tv->redraw(tv); 
    //tv->redraw(tv); 
    FTM_CAMERA_DBG("%s: Start\n", __FUNCTION__);
    camera_preview_test();     
    FTM_CAMERA_DBG("PREVIEW_Start \n");

    cam->exit_thd = false;    

    if (eFlashSupport != FLASHLIGHT_NONE)
    {

        // run Flash test thread
        
        if (pthread_create(&camera_flash_thread_handle, NULL, camera_flash_test_thread, cam)) 
        {
            FTM_CAMERA_DBG("Create flash test thread fail \n");
            return (0);
        }
    }
    
    while (1) {
        key = ui_wait_key(); 
        usleep(200000);
        chkcnt--;
        tv->m_khandler(key, tv->m_priv); 
        if (cam ->exit_thd)
            break;

        if (chkcnt > 0)
            continue;

        chkcnt = 5;
    }
    FTM_CAMERA_DBG("%s: Exit\n", __FUNCTION__);

    if (eFlashSupport != FLASHLIGHT_NONE)
    {
        pthread_join(camera_flash_thread_handle, NULL);
        camera_flash_thread_handle = 0; 
    }
    //pthread_exit(NULL);    
    return NULL;
}

/*******************************************************************************
*
********************************************************************************/
static int camera_cap_result_key_handler(int key, void *priv) 
{
    int handled = 0, exit = 0;
    struct camera *cam = (struct camera *)priv;
    struct textview *tv = &cam->tv;
    struct ftm_module *fm = cam->mod;
    
    switch (key) {
    case UI_KEY_BACK:
        FTM_CAMERA_DBG("Back Button Click\n"); 
        exit = 1;
        break;
    default:
        handled = -1;
        break;
    }
    if (exit) {
        FTM_CAMERA_DBG("%s: Exit thead\n", __FUNCTION__);
        cam->exit_thd = true;
        tv->exit(tv);        
    }
    return handled;
}


/*******************************************************************************
*
********************************************************************************/
static void *camera_update_showImg_tv_thread(void *priv)
{
    struct camera *cam = (struct camera *)priv;
    struct textview *tv =  &cam->tv;
    struct statfs stat;
    int count = 1, chkcnt = 5, key;

    FTM_CAMERA_DBG("%s: Start\n", __FUNCTION__);

    /* Initial the title info. */
    init_text(&cam ->left_btn, "", COLOR_YELLOW);
    init_text(&cam ->center_btn, "", COLOR_YELLOW);
    init_text(&cam ->right_btn, "Back", COLOR_YELLOW);                ;

    /* Initial the paintview function pointers */
    ui_init_textview(tv, camera_cap_result_key_handler,  (void*)cam);
    tv->set_title(tv, &cam->title);
    tv->set_text(tv, &cam->text);    
    tv->set_btn(tv, &cam ->left_btn, &cam ->center_btn, &cam ->right_btn);
    camera_show_image(); 
    while (!quickViewDone)
    {
        usleep(200); 
    }  
    tv->redraw(tv);       
    cam->exit_thd = false;    
    while (1) {
        key = ui_wait_key(); 
        usleep(200000);
        chkcnt--;
        tv->m_khandler(key, tv->m_priv); 
        if (cam ->exit_thd)
            break;

        if (chkcnt > 0)
            continue;

        chkcnt = 5;
    }
    
    FTM_CAMERA_DBG("%s: Exit\n", __FUNCTION__);
    return NULL;
}


/*******************************************************************************
*
********************************************************************************/
int camera_entry(struct ftm_param *param, void *priv)
{
    char *ptr;
    int chosen;
    bool exit = false;
    struct camera *cam = (struct camera *)priv;
    struct textview *tv = NULL ;
    struct itemview *iv = NULL ;
    struct statfs stat;
    PFLASH_LIGHT_FUNCTION_STRUCT pstFlash;
    int isTestDone = 0; 
    int initDone = 1; 

    FTM_CAMERA_DBG("%s\n", __FUNCTION__);

    init_text(&cam ->title, param->name, COLOR_YELLOW);
    init_text(&cam ->text, &cam->info[0], COLOR_YELLOW);
      
    cam->exit_thd = false;  

    if (!cam->iv) {
        iv = ui_new_itemview();
        if (!iv) {
            FTM_CAMERA_DBG("No memory");
            return -1;
        }
        cam->iv = iv;
    }
    
    //
    iv = cam->iv;
    iv->set_title(iv, &cam->title);
    
    int index = 0; 
    
   // iv->redraw(iv); 

    //! *************************************************
    //! Create the related object and init/enable it 
    //! *************************************************
    FTM_CAMERA_DBG("Open ACDK \n"); 
    if (MDK_Open() == FALSE)
    {
        FTM_CAMERA_ERR("MDK_Open() Fail \n"); 
        goto Exit; 
    }

#if 0 
    FTM_CAMERA_DBG("Init ACDK \n"); 
    if (MDK_Init() == FALSE) 
    {
        FTM_CAMERA_ERR("MDK_Init() Fail \n"); 
        goto Exit; 
    }   
#endif  
    
    //select camera sensor
    FTM_CAMERA_DBG("Set main/sub sensor for isp object in AppCamCtrl::mrInitCamCtrl():%d \n",srcDev); 
    ACDK_FEATURE_INFO_STRUCT rAcdkFeatureInfo; 
    bool bRet;
    unsigned int u4RetLen;
    rAcdkFeatureInfo.puParaIn = (UINT8*)&srcDev; 
    rAcdkFeatureInfo.u4ParaInLen = sizeof(int); 
    rAcdkFeatureInfo.puParaOut = NULL; 
    rAcdkFeatureInfo.u4ParaOutLen = 0; 
    rAcdkFeatureInfo.pu4RealParaOutLen = &u4RetLen; 

    FTM_CAMERA_DBG("Set main/sub sensor for sensorInit() in IspHal::init():%d \n",srcDev); 
    bRet = MDK_IOControl(ACDK_CCT_FEATURE_SET_SRC_DEV, &rAcdkFeatureInfo);
    if (!bRet) {
        FTM_CAMERA_DBG("ACDK_FEATURE_SET_SRC_DEV Fail: %d \n",srcDev); 
    }

    FTM_CAMERA_DBG("Init ACDK \n"); 
    if (MDK_Init() == FALSE) 
    {
        FTM_CAMERA_ERR("MDK_Init() Fail \n"); 
        memset(cam->info, 0, 1024); 
        sprintf(cam->info, "Init Sensor Fail  \n"); 
        iv->set_text(iv, &cam->text);
        iv->redraw(iv);         
        initDone = 0; 
        isTestDone = 1; 
    }   

    //init camera items     
    eFlashSupport = FlashLightInit(&pstFlash); 
    if (initDone) {
        if (eFlashSupport != FLASHLIGHT_NONE ) 
        {
            //init camera items with support flash 
            camera_items[index].id = ITEM_CAMERA_TEST; 
            camera_items[index].name = "Preview/Capture/Strobe"; 
        }
        else 
        {
            //init camera items without support flash 
            camera_items[index].id = ITEM_CAMERA_TEST; 
            camera_items[index].name = "Preview/Capture";
        }
        index++;     
        //ITEM PASS 
        camera_items[index].id = ITEM_PASS; 
        camera_items[index].name = "Test Pass"; 
        index++; 
    }   
    //ITEM FAIL 
    camera_items[index].id = ITEM_FAIL; 
    camera_items[index].name = "Test Fail"; 
    index++; 

    camera_items[index].id = -1; 
    camera_items[index].name = NULL; 
   
    iv->set_items(iv, camera_items, 0);
    iv->set_text(iv, &cam->text);
    
    //iv->redraw(iv); 
    camera_state = CAMERA_STATE_IDLE; 
    do {
        chosen = iv->run(iv, &exit);
        switch (chosen) {
        case ITEM_CAMERA_TEST:
            if (camera_state == CAMERA_STATE_IDLE )
            {
                capture_done = FALSE; 
                init_text(&cam ->left_btn, "", COLOR_YELLOW);
                init_text(&cam ->center_btn, "Capture", COLOR_YELLOW);
                init_text(&cam ->right_btn, "Back", COLOR_YELLOW);                      
                camera_update_capture_tv_thread(priv); 
                camera_preview_stop();
                if (capture_done == TRUE)
                {
                    camera_update_showImg_tv_thread(priv); 
                }
                camera_reset_layer_buffer();
            }
            isTestDone = 1; 
            memset(cam->info, 0, 1024); 
            iv->set_text(iv, &cam->text);
            iv->redraw(iv); 
            //iv->redraw(iv); 
            break; 
        case ITEM_PASS:
            if (isTestDone) { 
                cam->mod->test_result = FTM_TEST_PASS; 
                exit = true; 
            }
            else {
                memset(cam->info, 0, 1024); 
                sprintf(cam->info, "Not test done !! \n"); 
                iv->set_text(iv, &cam->text);
                iv->redraw(iv); 
            }
            break;
        case ITEM_FAIL:
            if (isTestDone) {
                cam->mod->test_result = FTM_TEST_FAIL; 
                exit  = true; 
            }
            else {
                memset(cam->info, 0, 1024); 
                sprintf(cam->info, "Not test done !! \n"); 
                iv->set_text(iv, &cam->text);
                iv->redraw(iv); 
            }
            break;
        }
        
        if (exit) {
            cam->exit_thd = true;
            break;
        }        
    } while (1);

Exit:
    if (initDone) {
        MDK_DeInit(); 
    }
    MDK_Close();        
    camera_state = CAMERA_STATE_NONE; 

    return 0;
}


/*******************************************************************************
*
********************************************************************************/
int camera_main_preview_entry(struct ftm_param *param, void *priv)
{
    srcDev = 0; //main sensor
    return camera_entry(param,priv);
}

/*******************************************************************************
*
********************************************************************************/
int camera_sub_preview_entry(struct ftm_param *param, void *priv)
{
    srcDev = 1; //sub sensor
    return camera_entry(param,priv);
}

/*******************************************************************************
*
********************************************************************************/
extern "C" int camera_main_init(void)
{
    int ret = 0;
    struct ftm_module *mod;
    struct camera *cam;

    FTM_CAMERA_DBG( "%s\n", __FUNCTION__);
    
    mod = ftm_alloc(ITEM_MAIN_CAMERA, sizeof(struct camera));
    cam  = mod_to_camera(mod);

    memset(cam, 0x0, sizeof(struct camera));

    /*NOTE: the assignment MUST be done, or exception happens when tester press Test Pass/Test Fail*/    
    cam->mod = mod; 

    if (!mod)
        return -ENOMEM;

    ret = ftm_register(mod, camera_main_preview_entry, (void*)cam);

    return ret;
}

/*******************************************************************************
*
********************************************************************************/
extern "C" int camera_sub_init(void)
{
    int ret = 0;
    struct ftm_module *mod;
    struct camera *cam;

    FTM_CAMERA_DBG("%s\n", __FUNCTION__);
    
    mod = ftm_alloc(ITEM_SUB_CAMERA, sizeof(struct camera));
    cam  = mod_to_camera(mod);

    memset(cam, 0x0, sizeof(struct camera));

    /*NOTE: the assignment MUST be done, or exception happens when tester press Test Pass/Test Fail*/    
    cam->mod = mod; 

    if (!mod)
        return -ENOMEM;

    ret = ftm_register(mod, camera_sub_preview_entry, (void*)cam);
    return ret;
}


#endif
