/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.wifi;

import com.android.settings.R;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiManager;


public class MtkCTIATestDialog extends AlertDialog implements DialogInterface.OnClickListener, AdapterView.OnItemSelectedListener {

	private static final String TAG = "CTIATestDialog";
	
	private static final int POSITIVE_BUTTON = BUTTON1;
	private static final int NEGATIVE_BUTTON = BUTTON2;	
	
	// Preferences 
	private static final String CTIA_PREF = "CTIA_PREF";
	private static final String PREF_CTIA_ENABLE = "CTIA_ENABLE";
	private static final String PREF_CTIA_RATE = "CTIA_RATE";
	private static final String PREF_CTIA_POWER = "CTIA_POWER_MODE";
	
    // add by mtk03034
	private static final String PREF_CTIA_DUMP_BEACON = "CTIA_DUMP_1";
	private static final String PREF_CTIA_DUMP_COUNTER = "CTIA_DUMP_2";
	private static final String PREF_CTIA_DUMP_INTERVAL = "CTIA_DUMP_3";


	private View mView = null;
	private WifiManager mWm = null;
	private Context mContext = null;
	
	// Supported rate 
	private String[] mRate = {
			"Automatic",
			"1M",
			"2M",
			"5_5M",
			"11M",
			"6M",
			"9M",
			"12M",
			"18M",
			"24M",
			"36M",
			"48M",
			"54M",
			"20MCS0800",
			"20MCS01800",
			"20MCS2800",
			"20MCS3800",
			"20MCS4800",
			"20MCS5800",
			"20MCS6800",
			"20MCS7800",
			"20MCS0400",
			"20MCS1400",
			"20MCS2400",
			"20MCS3400",
			"20MCS4400",
			"20MCS5400",
			"20MCS6400",
			"20MCS7400",
			"40MCS0800",
			"40MCS1800",
			"40MCS2800",
			"40MCS3800",
			"40MCS4800",
			"40MCS5800",
			"40MCS6800",
			"40MCS7800",
			"40MCS32800",
			"40MCS0400",
			"40MCS1400",
			"40MCS2400",
			"40MCS3400",
			"40MCS4400",
			"40MCS5400",
			"40MCS6400",
			"40MCS7400",
			"40MCS32400",
	};
	
	// Supported power saving mode
	/*
	private String[] mPsMode = {
			"CAM",
			"Maximum PS",				
			"Fast PS",
	};
    */

	private CheckBox mCheckbox = null;
	private Spinner mRateSpinner = null;
	//private Spinner mPsSpinner = null;
	private int mRateVal = 0;
	private int mPowerMode = 0;
	private Button mGetBtn = null;
	private Button mSetBtn = null;
	private EditText mIdEditText = null;
	private EditText mValEditText = null;
	
    // add 20111107
	private EditText mIntervalEditText = null;

    // add 20111107
	private CheckBox mDumpBeaconCheckbox = null;
	private CheckBox mDumpCounterCheckbox = null;


	protected MtkCTIATestDialog(Context context) {
		super(context);
		mContext = context;
		mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	/** Called after flags are set, the dialog's layout/etc should be set up here */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		onLayout();
		restorePrefs();
		super.onCreate(savedInstanceState);		
	}

    private void setLayout(int layoutResId) {
        setView(mView = getLayoutInflater().inflate(layoutResId, null));
        onReferenceViews(mView);
    }
	private void onLayout() {
		setLayout(R.layout.ctiasetting);
	    int positiveButtonResId = R.string.ok;
	    int negativeButtonResId = R.string.cancel;
	    int neutralButtonResId = 0;		
		setButtons(positiveButtonResId, negativeButtonResId, neutralButtonResId);
	}

    /** Called when the widgets are in-place waiting to be filled with data */
	private void onFill() {
		//Todo	
	}
	
	private Button.OnClickListener mBtnClickListener = new Button.OnClickListener() {	
		public void onClick(View v) {
            int val = 0;
            int id = 0;

			if (v == mSetBtn) {
                try {
                    // Parse and format the ID to hexadecimal
                    id = (int) Long.parseLong(mIdEditText.getText().toString(), 16);
                    // Parse and format the Value to hexadecimal
                    val = (int) Long.parseLong(mValEditText.getText().toString(), 16);
                    // SW Flag set
                    mWm.doCTIATestSet(id, val);
                } catch (NumberFormatException e) {
				    Log.v(TAG, "number format error");
                    mValEditText.setText("0");  
                }
			} else if (v == mGetBtn) {
                try {
                    // Parse and format the ID to hexadecimal
                    id = (int) Long.parseLong(mIdEditText.getText().toString(), 16);
                    String sval = mWm.doCTIATestGet(id);
                    //mValEditText.setText(Long.toString(val, 16));  
                    mValEditText.setText(sval);  
                } catch (NumberFormatException e) {
				    Log.v(TAG, "number format error");
                    mValEditText.setText("0");  
                }
			}
		}
	};
	
	@Override
	protected void onStop() {
		super.onStop();
		// Save user preferences.
		SharedPreferences settings = mContext.getSharedPreferences(CTIA_PREF, 0);
		settings.edit().putBoolean(PREF_CTIA_ENABLE, mCheckbox.isChecked()).commit();
		settings.edit().putInt(PREF_CTIA_RATE, getRateFromSpinner()).commit();
		//settings.edit().putInt(PREF_CTIA_POWER, getPsModeFromSpinner()).commit();
	
		settings.edit().putBoolean(PREF_CTIA_DUMP_BEACON, mDumpBeaconCheckbox.isChecked()).commit();
		settings.edit().putBoolean(PREF_CTIA_DUMP_COUNTER, mDumpCounterCheckbox.isChecked()).commit();

        int tmp_interval = 0;
        try {
            tmp_interval = Integer.parseInt(((TextView)mIntervalEditText).getText().toString());
        } catch (Exception e) { 
            tmp_interval = 1;
        }
            
        if (tmp_interval > 255) {
            tmp_interval = 255;
        } else if (tmp_interval < 1) {
            tmp_interval = 1;
        }

        settings.edit().putInt(PREF_CTIA_DUMP_INTERVAL, tmp_interval).commit();

    }
	
	private void restorePrefs() {
		SharedPreferences settings = mContext.getSharedPreferences(CTIA_PREF, 0);
		boolean pref_enable_ctia = settings.getBoolean(PREF_CTIA_ENABLE, false);
		int pref_rate = settings.getInt(PREF_CTIA_RATE, 0);
        
        boolean pref_dump_beacon = settings.getBoolean(PREF_CTIA_DUMP_BEACON, false);
        boolean pref_dump_counter = settings.getBoolean(PREF_CTIA_DUMP_COUNTER, false);
        
        int interval = settings.getInt(PREF_CTIA_DUMP_INTERVAL, 1);



		//int pref_power = settings.getInt(PREF_CTIA_POWER, 0);
	
		mCheckbox.setChecked(pref_enable_ctia);
		mRateSpinner.setSelection(pref_rate);
		//mPsSpinner.setSelection(pref_power);
	    
        mDumpBeaconCheckbox.setChecked(pref_dump_beacon);
        mDumpCounterCheckbox.setChecked(pref_dump_counter);
        mIntervalEditText.setText(interval + "");

	}
	
	/** Called when we need to set our member variables to point to the views. */
	private void onReferenceViews(View view) {
		mRateSpinner = (Spinner) view.findViewById(R.id.rate_spinner);
		//mPsSpinner = (Spinner) view.findViewById(R.id.ps_spinner);
		setSpinnerAdapter(mRateSpinner, mRate);
		//setSpinnerAdapter(mPsSpinner, mPsMode);
		
		mGetBtn = (Button) view.findViewById(R.id.get_btn);
		mSetBtn = (Button) view.findViewById(R.id.set_btn);
		
		mSetBtn.setOnClickListener(mBtnClickListener);
		mGetBtn.setOnClickListener(mBtnClickListener);
		
		mRateSpinner.setOnItemSelectedListener(this);		
		//mPsSpinner.setOnItemSelectedListener(this);
		mCheckbox = (CheckBox) view.findViewById(R.id.enable_checkbox); 
		
		mIdEditText = (EditText) view.findViewById(R.id.idedittext);
		mValEditText = (EditText) view.findViewById(R.id.valedittext);
        mIntervalEditText = (EditText) view.findViewById(R.id.interval_edittext);

        // Add dump beacon/probe response
        mDumpBeaconCheckbox = (CheckBox) view.findViewById(R.id.enable_dump_checkbox);
        mDumpCounterCheckbox = (CheckBox) view.findViewById(R.id.enable_dump_counter_checkbox);


	}

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (items != null) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    getContext(), android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }
    
    private void setButtons(int positiveResId, int negativeResId, int neutralResId) {
        final Context context = getContext();

        if (positiveResId > 0) {
            setButton(context.getString(positiveResId), this);
        }

        if (negativeResId > 0) {
            setButton2(context.getString(negativeResId), this);
        }

        if (neutralResId > 0) {
            setButton3(context.getString(neutralResId), this);
        }
    }
 
    private void handleRateChange(int rate) {
    	//Log.v(TAG, "rate = " + rate);
    	mRateVal = rate;
    }   
    
    private void handlePsChange(int psMode) {
    	//Log.v(TAG, "ps_mode = " + psMode);
        mPowerMode = psMode;
    }   

    private int getRateFromSpinner() {
        int position = mRateSpinner.getSelectedItemPosition();
        return position;
    }
 
    /*
    private int getPsModeFromSpinner() {
        int position = mPsSpinner.getSelectedItemPosition();
        return position;
    }
    */

    public void onItemSelected (AdapterView parent, View view, int position, long id) {
    	if (parent == mRateSpinner) {
    		handleRateChange(getRateFromSpinner());
    	} 
        /*  else if (parent == mPsSpinner) {
    		handlePsChange(getPsModeFromSpinner());
    	}*/
    }
    
	public void onClick(DialogInterface arg0, int arg1) {
        if (arg1 == POSITIVE_BUTTON) {
            if (mCheckbox.isChecked()) {
                mWm.doCTIATestOn();
                mWm.doCTIATestRate(mRateVal);
                //mWm.doCTIATestPower(mPowerMode);
                //mWm.doCTIATestSet(, val);
            } else {
                mWm.doCTIATestOff();
                mWm.doCTIATestRate(mRateVal);
                //mWm.doCTIATestPower(mPowerMode);
            }

            int id = 0; 
            int val = 0;

            if (mDumpBeaconCheckbox.isChecked()) {
                id = (int) Long.parseLong("10020000", 16);
                mWm.doCTIATestSet(id, 1);
                //Log.v(TAG, "id is " + id + " val is " + 1);
            } else {
                id = (int) Long.parseLong("10020000", 16);
                mWm.doCTIATestSet(id, 0);
                //Log.v(TAG, "id is " + id + " val is " + 0);
            }

            // add dump case
            int tmp_interval = 0;
            try {
                tmp_interval = Integer.parseInt(((TextView)mIntervalEditText).getText().toString());
            } catch (Exception e) { 
                tmp_interval = 1;
            }

            if (tmp_interval > 255) {
                mIntervalEditText.setText("255");
            } else if (tmp_interval < 1) {
                mIntervalEditText.setText("1");
            }

            // handle the dump counter case 
            if (mDumpCounterCheckbox.isChecked()) {
                id = (int) Long.parseLong("10020001", 16);
                val = (int) Long.parseLong("0000" + Integer.toHexString(tmp_interval) + "01", 16);
                mWm.doCTIATestSet(id, val);

                //Log.v(TAG, "id is " + id + " val is " + val);
            } else {
                id = (int) Long.parseLong("10020001", 16);
                val = (int) Long.parseLong("0000" + Integer.toHexString(tmp_interval) + "00", 16);
                mWm.doCTIATestSet(id, val);

                //Log.v(TAG, "id is " + id + " val is " + val);
            }

            this.dismiss();
        } else if (arg1 == NEGATIVE_BUTTON) {
			//Log.d(TAG, "cancel");
			this.dismiss();	
		} 
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
}
