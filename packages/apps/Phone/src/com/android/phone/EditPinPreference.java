/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.phone;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Map;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.featureoption.FeatureOption;
import android.text.*;
import com.mediatek.xlog.Xlog;


/**
 * Class similar to the com.android.settings.EditPinPreference
 * class, with a couple of modifications, including a different layout 
 * for the dialog.
 */
public class EditPinPreference extends EditTextPreference {

    private static final String LOG_TAG = "Settings/EditPinPreference";
	public static final int FDN_MODE_FLAG=10;
	private Phone mPhone;
	private int mMode;
    private int mSimId;
    private boolean shouldHideButtons;
    
    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference preference, boolean positiveResult);
    }
    
    private OnPinEnteredListener mPinListener;

    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        mPinListener = listener;
    }
    
    public EditPinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditPinPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    /**
     * Overridden to setup the correct dialog layout, as well as setting up 
     * other properties for the pin / puk entry field.
     */
    @Override
    protected View onCreateDialogView() {
        // set the dialog layout
        setDialogLayoutResource(R.layout.pref_dialog_editpin);
        
        View dialog = super.onCreateDialogView();
        
        // set the transformation method and the key listener to ensure
        // correct input and presentation of the pin / puk.
        final EditText textfield = getEditText();
        textfield.setTransformationMethod(PasswordTransformationMethod.getInstance());
        textfield.setKeyListener(DigitsKeyListener.getInstance());
        
        if (this instanceof CallBarringChangePassword)
        {
            InputFilter filters[] = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(4);
            textfield.setFilters(filters);
        }
        
        return dialog;
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        // If the layout does not contain an edittext, hide the buttons.
        shouldHideButtons = (view.findViewById(android.R.id.edit) == null);
    }
    
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        
        // hide the buttons if we need to.
        if (shouldHideButtons) {
            builder.setPositiveButton(null, this);
            builder.setNegativeButton(null, this);
        }
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (mPinListener != null) {
            mPinListener.onPinEntered(this, positiveResult);
        }
    }
    
    /**
     * Externally visible method to bring up the dialog to 
     * for multi-step / multi-dialog requests (like changing 
     * the SIM pin). 
     */
    public void showPinDialog() {
        showDialog(null);
    }
    public void showTipDialog(String title,String msg ) {
    	Toast.makeText(this.getContext(), msg, Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onClick() {
    if(this.getDialog() != null)
		{
			return ;	
		}	
		switch (mMode) {
		//FDN_MODE_FLAG :added for judge if Phone book is ready
		case FDN_MODE_FLAG:
            
                        Xlog.i(LOG_TAG, "onClick, FDN_MODE_FLAG");
			handleFdnModeClick();
			break;
		default:
			showDialog(null);
			break;
		}
    }
    
    public void initFdnModeData(Phone phone,int mode, int simId){
    	mPhone=phone;
    	mMode=mode;    	
        mSimId = simId;
    }
    
    private void handleFdnModeClick(){
    	Xlog.i(LOG_TAG, "Enable or Disable the FDN state button is clicked");
    	boolean isPhoneBookReady=false;
    	//isPhoneBookReady=mPhone.getIccCard().isPhbReady();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
        	isPhoneBookReady = ((GeminiPhone)mPhone).getIccCardGemini(mSimId).isPhbReady();
        } else {
    	isPhoneBookReady=mPhone.getIccCard().isPhbReady();
        }
    	
    	Xlog.i(LOG_TAG, "Phone book state from system is :"+isPhoneBookReady);
		if (!isPhoneBookReady) {
			Context context=this.getContext();
			showTipDialog(context.getString(R.string.error_title),context.getString(R.string.fdn_phone_book_busy));
		}else{
			showDialog(null);
		}
    }
}
