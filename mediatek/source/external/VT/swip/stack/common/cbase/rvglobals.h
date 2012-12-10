/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#ifndef _rv_globals_h_
#define _rv_globals_h_


#if defined(__cplusplus)
extern "C" {
#endif

/********************************************************************************************
 * RvStartGlobalDataServices
 * 
 * Initializes global data services
 *
 * INPUT   : None
 * OUTPUT  : started - tells whether service was initialized currently or in a 
 *                     previous call to the function;
 * RETURN  : the data pointer of NULL if fails
 */
RvStatus RvStartGlobalDataServices(OUT RvBool* started);

/********************************************************************************************
 * RvStartGlobalDataServices
 * 
 * Stops global data services
 *
 * INPUT   : None
 * OUTPUT  : None
 * RETURN  : None
 */
void RvStopGlobalDataServices(void);

/********************************************************************************************
 * RvGetGlobalDataPtr
 * 
 * Fetches the pointer to the global data structure
 *
 * INPUT   :  
 *           indx - the index of the global data structure
 * OUTPUT  : None.
 * RETURN  : the data pointer of NULL if fails
 */
RVCOREAPI void* RvGetGlobalDataPtr(IN int indx);


/********************************************************************************************
 * RvGlobalDataCreateFunc
 * Function to be supplied by the module wishing to create the global data structure.
 * This function will be called by global data server upon request
 * INPUT   : 
 *              index - index within thread vars array for the newly created structure
 *              usrData - pointer to arbitrary data that may be needed when creating 
 *                        global data structure
 * OUTPUT  : None.
 * RETURN  : Pointer to the newly created global data structure or NULL if the function fails
 */
typedef void* (*RvGlobalDataCreateFunc)(IN int index, IN void* usrData);

/********************************************************************************************
 * RvGlobalDataDestroyFunc
 * Function to be supplied by the module wishing to create the global data structure.
 * This function will be called by global data server upon request
 * INPUT   : 
 *              index - index within thread vars array where global data structure was held
 *              glDataPtr - pointer to global data structure to be destroyed 
 * OUTPUT  : None.
 * RETURN  : None
 */
typedef void (*RvGlobalDataDestroyFunc)(IN int index, IN void* glDataPtr);


/********************************************************************************************
 * RvCreateGlobalData
 * Instructs global data server to create the global data structure for specific index
 * INPUT   : 
 *              index - index within thread vars array for the newly created structure
 *              crtF - function that creates and initializes the global data structure
 *              usrData - pointer to arbitrary data (will be passed to crtF)
 *              dstrF - function that destroys the global data structure
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RVCOREAPI RvStatus RvCreateGlobalData(IN int index, IN RvGlobalDataCreateFunc crtF, IN void *usrData, 
                            IN RvGlobalDataDestroyFunc dstrF);

/********************************************************************************************
 * RvDestroyGlobalData
 * Instructs global data server to destroy the global data structure for specific index
 * INPUT   : 
 *              index - index within thread vars array for the destroyed structure
 * OUTPUT  : None.
 * RETURN  : RV_OK if successful otherwise an error code.
 */
RVCOREAPI RvStatus RvDestroyGlobalData(IN int index);

#if defined(__cplusplus)
}
#endif

typedef struct 
{
    int                         iIndex;
    RvGlobalDataDestroyFunc     iDestroyFunc;
    void*                       iGlobalData;
} RvGlDataStruct;


#endif
