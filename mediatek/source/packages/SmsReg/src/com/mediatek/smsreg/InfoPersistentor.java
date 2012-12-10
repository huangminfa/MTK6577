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

package com.mediatek.smsreg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.Set;
import android.os.Bundle;
import android.util.Log;
import com.mediatek.xlog.Xlog;

interface IInfoPersistentor {
	
	String getSavedIMSI();
	void setSavedIMSI(String IMSI);
}

public class InfoPersistentor implements IInfoPersistentor {
	private String TAG = "SmsReg/InfoPersistentor";
	private DMAgent agent;

	InfoPersistentor() {
		
		if(agent == null){
			Xlog.i(TAG, "get the agent...");
			IBinder binder = ServiceManager.getService("DMAgent");
			if (binder == null) {
				Xlog.e(TAG, "get DMAgent fail! binder is null!");
				return;
			}
			agent = DMAgent.Stub.asInterface(binder);
		}		

	}

	public String getSavedIMSI() {

		if(agent == null){
			Xlog.e(TAG, "get IMSI failed, agent is null!");
			return null;
		}
//		String savedIMSI = agent.readIMSI();
		String savedIMSI=null;
		try{
			savedIMSI = agent.readIMSI();
		}catch (RemoteException e) {
			Xlog.e(TAG, "get IMSI failed, readIMSI failed!");
			e.printStackTrace();			
		}
		Xlog.i(TAG, "Get savedIMSI = [" + savedIMSI + "]");
		return savedIMSI;

	}

	public void setSavedIMSI(String IMSI) {
		if(agent == null){
			Xlog.e(TAG, "save IMSI failed, agent is null!");
			return;
		}
		try{
			agent.writeIMSI(IMSI);
		}catch (RemoteException e) {
			Xlog.e(TAG, "save IMSI failed, writeIMSI failed!");
			e.printStackTrace();			
		}
        
		Xlog.i(TAG, "save IMSI [" + IMSI + "]");		

	}

    public int getSavedCTA() {
        if(agent == null){
	            Xlog.e(TAG, "get CTA failed, agent is null!");
            return 0;
        }
        int savedCTA=0;
        try{
            savedCTA = Integer.parseInt(agent.readCTA());
        }catch (RemoteException e) {
	            Xlog.e(TAG, "get cta cmcc switch failed, readCTA failed!");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            Xlog.e(TAG, "number format exception. ", e);
        }
	        Xlog.i(TAG, "Get savedCTA = [" + savedCTA + "]");
        return savedCTA;
    }

    private void setSavedCTA(String cta) {
        if(agent == null){
	            Xlog.e(TAG, "save CTA switch value failed, agent is null!");
            return;
        }
        try{
            agent.writeCTA(cta);
        }catch (RemoteException e) {
	            Xlog.e(TAG, "save CTA switch failed, writeCTA failed!");
            e.printStackTrace();
        }
        
	        Xlog.i(TAG, "save CTA [" + cta + "]");

	    }
}
