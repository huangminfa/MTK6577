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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mediatek.engineermode.emsvr;

import java.io.IOException;
import android.util.Log;
/**
 *
 * @author MTK80905
 */
public class AFMFunctionCallEx {

     //==============Feature id======================================
     
     public static final int FUNCTION_EM_BASEBAND= 10001;
     public static final int FUNCTION_EM_CPU_FREQ_TEST_START = 20001;
     public static final int FUNCTION_EM_CPU_FREQ_TEST_STOP = 20002;
     public static final int FUNCTION_EM_CPU_FREQ_TEST_CURRENCT = 20003;
     public static final int FUNCTION_EM_FB0_IOCTL = 30001;
     public static final int FUNCTION_EM_CPU_STRESS_TEST_APMCU = 40001;
     public static final int FUNCTION_EM_CPU_STRESS_TEST_SWCODEC = 40002;
     public static final int FUNCTION_EM_CPU_STRESS_TEST_BACKUP = 40003;
     public static final int FUNCTION_EM_CPU_STRESS_TEST_THERMAL = 40004;
     
    private Client socket = null;

    public boolean StartCallFunctionStringReturn(int function_id) {
        boolean ret = false;
        socket = new Client();
        socket.StartClient();
        try {
            socket.WriteFunctionNo(String.format("%d", function_id));            
            ret = true;

        } catch (IOException e) {
        	Log.e("EMX", "StartCallFunctionStringReturnEXP " + e.getMessage());
            ret = false;
        }
        return ret;
    }
    public boolean WriteParamNo(int n) {
        boolean ret = false;        
        try {
            socket.WriteParamNo(n);            
            ret = true;

        } catch (IOException e) {
            ret = false;
        }
        return ret;
    }
    public boolean WriteParamInt(int n) {
        boolean ret = false;        
        try {
            socket.WriteParamInt(n);            
            ret = true;

        } catch (IOException e) {
            ret = false;
        }
        return ret;
    }
    public boolean WriteParamString(String s) {
        boolean ret = false;        
        try {
            socket.WriteParamString(s);            
            ret = true;

        } catch (IOException e) {
            ret = false;
        }
        return ret;
    }
/**
 *
 * @return returnCode=0 if sockt close normally
 *   returnCode=1  has more data to be read.
 *   returnCode = -1  close abnormally.
 */
     public static final int RESULT_FIN = 0;
     public static final int RESULT_CONTINUE = 1;
     public static final int RESULT_IO_ERR = -1;
     
    public FunctionReturn GetNextResult() {
        FunctionReturn ret = new FunctionReturn();
        try {
            ret.returnString = socket.Read();
            if( ret.returnString.equals(""))
            {
                ret.returnCode = RESULT_FIN;
                EndCallFunction();
            }
            else
            {
                ret.returnCode = RESULT_CONTINUE;  // maybe has another response.
            }
            
        } catch (IOException e) {
            ret.returnCode = RESULT_IO_ERR;
            EndCallFunction();
            ret.returnString = "ERROR " + e.toString();
            if(e.toString().equals("java.io.EOFException"))
            {//such as read a socket that already be  write shutdown and no more data avilable.
                ret.returnCode = RESULT_FIN;
                ret.returnString = "";
            }
        }
        return ret;
    }
   
    private void EndCallFunction()
    {        
        socket.StopClient();
    }
    
    
    
}
