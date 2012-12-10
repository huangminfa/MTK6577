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

/* //hardware/ril/reference-ril/ril_nw.h
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/
#ifndef RIL_NW_H 
#define RIL_NW_H 1
            
extern void requestSignalStrength(void * data, size_t datalen, RIL_Token t);
extern void requestRegistrationState(void * data, size_t datalen, RIL_Token t);
extern void requestGprsRegistrationState(void * data, size_t datalen, RIL_Token t);
extern void requestOperator(void * data, size_t datalen, RIL_Token t);
extern void requestRadioPower(void * data, size_t datalen, RIL_Token t);
extern void requestGetImei(void * data, size_t datalen, RIL_Token t);
extern void requestGetImeisv(void * data, size_t datalen, RIL_Token t);
extern void requestQueryNetworkSelectionMode(void * data, size_t datalen, RIL_Token t);
extern void requestSetNetworkSelectionAutomatic(void * data, size_t datalen, RIL_Token t);
extern void requestSetNetworkSelectionManual(void * data, size_t datalen, RIL_Token t);
extern void requestQueryAvailableNetworks(void * data, size_t datalen, RIL_Token t);
extern void requestBasebandVersion(void * data, size_t datalen, RIL_Token t);
extern void requestSetBandMode(void * data, size_t datalen, RIL_Token t);
extern void requestQueryAvailableBandMode(void * data, size_t datalen, RIL_Token t);
extern void requestSetPreferredNetworkType(void * data, size_t datalen, RIL_Token t);
extern void requestGetPreferredNetworkType(void * data, size_t datalen, RIL_Token t);
extern void requestGetNeighboringCellIds(void * data, size_t datalen, RIL_Token t);
extern void requestSetLocationUpdates(void * data, size_t datalen, RIL_Token t);
extern void requestGetPacketSwitchBearer(RILId rid);

// GCG switcher feature
extern void requestSetGCFSwitch(RILId rid);
// GCG switcher feature
extern void requestSN(RILId rid);

extern void onRadioState(char* urc, RILId rid);
extern void onNetworkStateChanged(char *urc, RILId rid);
extern void onNitzTimeReceived(char *urc, RILId rid);
extern void onSignalStrenth(char* urc, RILId rid); 
extern void onRestrictedStateChanged(RILId rid);
extern void onSimInsertChanged(const char * s,RILId rid);
extern void onInvalidSimInfo(char *urc,RILId rid); 

/* Add-BY-JUNGO-20101008-CTZV SUPPORT */
extern void onNitzTzReceived(char *urc, RILId rild);
extern void updateNitzOperInfo(RILId rid);


extern int isRadioOn(RILId rid);
extern int queryRadioState(RILId rid);

extern int rilNwMain(int request, void *data, size_t datalen, RIL_Token t);
extern int rilNwUnsolicited(const char *s, const char *sms_pdu, RILChannelCtx* p_channel);

extern int getSingnalStrength(char *line, int *rssi, int *ber, int *rssi_qdbm, int *rscp_qdbm, int *ecn0_qdbm);

#ifdef MTK_GEMINI
extern int sim_inserted_status;

extern void requestSimReset(RILId rid);
extern void requestSimInsertStatus(RILId rid);

#endif

#ifdef EVDO_DT_SUPPORT
void requestContinueMdInit(void *data, size_t datalen, RIL_Token t);
void requestOperatorLimitService(void * data, size_t datalen, RIL_Token t);
#endif

#endif /* RIL_NW_H */

