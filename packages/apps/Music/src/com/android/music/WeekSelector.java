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

package com.android.music;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

public class WeekSelector extends Activity
{
    private static final int ALERT_DIALOG_KEY = 0;
    private static final int WEEK_START = 1;
    private static final int WEEK_END = 12;    
    private static final int UPDATE_INTERVAL = 200;
    private static final int EDITTEXT_POSITION = 1;
    
    private int mCurrentSelectedPos;
    
    private NumberPicker mNumberPicker;
    private View mView;
    private EditText mSpinnerInput;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.weekpicker);
        //getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
        //                            WindowManager.LayoutParams.WRAP_CONTENT);
        
        mView = getLayoutInflater().inflate(R.layout.weekpicker, null);         

        mNumberPicker = (NumberPicker)mView.findViewById(R.id.weeks);
        //mNumberPicker = new NumberPicker(this);
        
        mNumberPicker.setOnValueChangedListener(mChangeListener);
        mNumberPicker.setDisplayedValues(getResources().getStringArray(R.array.weeklist));
        
        int def = MusicUtils.getIntPref(this, "numweeks", WEEK_START); 
        int pos = icicle != null ? icicle.getInt("numweeks", def) : def;
        mCurrentSelectedPos = pos;
        mNumberPicker.setMinValue(WEEK_START);
        mNumberPicker.setMaxValue(WEEK_END);        
        mNumberPicker.setValue(pos);   
        mNumberPicker.setWrapSelectorWheel(false);
        mNumberPicker.setOnLongPressUpdateInterval(UPDATE_INTERVAL);
        mSpinnerInput = (EditText)mNumberPicker.getChildAt(EDITTEXT_POSITION);
        //mSpinnerInput = (EditText) mNumberPicker
        //        .findViewById(R.id.numberpicker_input);
        if (mSpinnerInput != null) {
            //mSpinnerInput.setEnale(false);
            mSpinnerInput.setFocusable(false);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {            
        case ALERT_DIALOG_KEY:
            MusicDialog dialog = new MusicDialog(this, mButtonClicked, mView);
            if (dialog != null) {
                dialog.setTitle(R.string.weekpicker_title);
                dialog.setPositiveButton(getResources().getString(R.string.weekpicker_set));            
                dialog.setNeutralButton(getResources().getString(R.string.cancel));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(true);
                return dialog;
            }
        default:
            return null;
        }
    }

    OnValueChangeListener mChangeListener = new OnValueChangeListener() {
        public void onValueChange(NumberPicker picker, int oldVal,
                int newVal) {
            if (picker == mNumberPicker) {
                mCurrentSelectedPos = newVal;
            }           
        }
    };
    
    private DialogInterface.OnClickListener  mButtonClicked = new DialogInterface.OnClickListener () {
        public void onClick(DialogInterface mDialogInterface, int button) {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                int numweeks = mCurrentSelectedPos;
                MusicUtils.setIntPref(WeekSelector.this, "numweeks", numweeks);
                setResult(RESULT_OK);
                finish();
            } else if (button == DialogInterface.BUTTON_NEUTRAL) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    };

    
    @Override
    public void onSaveInstanceState(Bundle outcicle) {
        outcicle.putInt("numweeks", mCurrentSelectedPos);
    }
    
    @Override
    public void onResume() {
        showDialog(ALERT_DIALOG_KEY);
        mNumberPicker.setVisibility(View.VISIBLE);   
        super.onResume();
    }    
}
