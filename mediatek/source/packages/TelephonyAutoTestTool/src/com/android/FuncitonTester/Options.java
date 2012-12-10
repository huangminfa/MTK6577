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

package com.android.FuncitonTester;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

public class Options extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(createPreferenceHierarchy());    
	}
	
	private PreferenceScreen createPreferenceHierarchy() {        
        // FunctionTesterActivity.mTestRunner
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);    
     
		PreferenceCategory inlinePrefCat = new PreferenceCategory(this);        
		inlinePrefCat.setTitle("Test Cases");        
		root.addPreference(inlinePrefCat);     
		
        for (String testCaseName : FunctionTesterActivity.TestCaseNames) {
            Log.v("@@@", testCaseName);
			Class<?> clz = null;
			Class<?>[] nullClass = null;

			try {
                clz = Class.forName(testCaseName);
                Log.v("@@@", "clz=" + clz);
				Method getDesc = clz.getMethod("getDescription", nullClass);
				String strDesc = (String) getDesc.invoke(clz.newInstance(), (Object[]) null );
                Log.v("@@@", "strDesc=" + strDesc);
				inlinePrefCat.addPreference(getCheckBoxPreference(testCaseName, 
                        testCaseName.substring(testCaseName.lastIndexOf(".") + 1,
                                testCaseName.length()),
                        strDesc));

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		PreferenceCategory miscPrefCat = new PreferenceCategory(this);        
		miscPrefCat.setTitle("Misc");        
		root.addPreference(miscPrefCat);  
		
		miscPrefCat.addPreference(getCheckBoxPreference("Infinite", 
				"Infinite", 
				"Run test cases infinitely"));
		
		/*miscPrefCat.addPreference(getCheckBoxPreference("Random", 
				"Random", 
				"Run test cases randomly"));*/
		return root;
	}
	
	private CheckBoxPreference getCheckBoxPreference(String key, String title, String summary) {
		CheckBoxPreference checkboxPref = new CheckBoxPreference(this);        
		checkboxPref.setKey(key);        
		checkboxPref.setTitle(title);        
		checkboxPref.setSummary(summary);
		return checkboxPref;
	}
}
