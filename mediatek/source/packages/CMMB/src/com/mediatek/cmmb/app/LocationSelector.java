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

package com.mediatek.cmmb.app;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.cmmb.app.Utils.SavedLocation;
import com.mediatek.mbbms.ServerStatus;
import com.mediatek.mbbms.MBBMSStore.DB;
import com.mediatek.mbbms.protocol.LocationInfo;
import com.mediatek.mbbms.protocol.LocationInfo.City;
import com.mediatek.mbbms.protocol.LocationInfo.County;
import com.mediatek.mbbms.protocol.LocationInfo.Province;

import android.app.ListActivity;
import android.app.NotificationManagerPlus;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LocationSelector extends ListActivity implements
        View.OnClickListener,
        OnItemClickListener {
    private static final String TAG = "LocationSelector";
    private static final boolean LOG = true;
       
    private static final int STEP_SUGGESTION = 0;
    private static final int STEP_PROVINCE = 1;
    private static final int STEP_CITY = 2;
    private static final int STEP_COUNTY = 3;
    
    private static final String HOLDER_START = "[";
    private static final String HOLDER_END = "]";
    private static final String SEPERATOR = "-";
    
    private int mMode;
    private boolean mIsPick;
    
    private TextView mTitle;
    private CheckBox mCheck;
    private Button mOk;
    private Button mCancel;
    private Button mPreStep;
    private ListView mList;
    private TextView mEmpty;
    private LocationInfo mLocation;
    private int mStep = STEP_SUGGESTION;
    private int mStepStart;
    private Province mSelectedProvince;
    private City mSelectedCity;
    private County mSelectedCounty;
    private City mSelectedSuggestion;
    private City mMockCity;
    private SavedLocation mSavedLocation;
    private ModeSwitchManager mModeSwitchManager;
    private NotificationManagerPlus mNMP;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_selector);
        mList = getListView();
        mEmpty = (TextView) findViewById(android.R.id.empty);
        mTitle = (TextView) findViewById(R.id.title);
        mCheck = (CheckBox) findViewById(R.id.save);
        mOk = (Button) findViewById(R.id.ok); 
        mCancel = (Button) findViewById(R.id.cancel);
        mPreStep = (Button) findViewById(R.id.previous_step);
        
        mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mList.setOnItemClickListener(this);
        mTitle.setOnClickListener(this);
        mOk.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mPreStep.setOnClickListener(this);

        mModeSwitchManager = new ModeSwitchManager(this,null,savedInstanceState);
        mNMP = new NotificationManagerPlus.ManagerBuilder(this).create();
        
        Intent intent = getIntent();
        if (Intent.ACTION_PICK.equals(intent.getAction())) {
            mIsPick = true;
            mCheck.setVisibility(View.VISIBLE);
        } else {
            mIsPick = false;
            mCheck.setVisibility(View.GONE);
        }
        mMode = intent.getIntExtra(Utils.EXTRA_LOCATION_MODE, Utils.LOCATION_MODE_NORMAL);
        if (mMode == Utils.LOCATION_MODE_NORMAL) {
            mStep = mStepStart = STEP_SUGGESTION;
        } else {
            mStep = mStepStart = STEP_PROVINCE;
        }
        new SyncLoacationTask().execute();
    }

    @Override
	protected void onPause() {
		super.onPause();
		mNMP.stopListening();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNMP.startListening();
	}

	@Override
	protected void onStart() {
		super.onStart();
        mModeSwitchManager.onActivityStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mModeSwitchManager.onActivityStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mModeSwitchManager.onSaveInstanceState(outState);
	}

    @Override
    public void onBackPressed() {
        onClick(mCancel);
    }
    
    private class ScrollPosition {
        int firstVisiblePosition;
        int firstTop;
    }
    
    private ScrollPosition[] mPosition = new ScrollPosition[STEP_COUNTY];
    private void previousStep() {
        mStep--;
        updateStep();
        
        ScrollPosition p = mPosition[mStep];
        if (p != null) {
            mList.setSelectionFromTop(p.firstVisiblePosition, p.firstTop);
        }
    }
    
    private void nextStep() {
        ScrollPosition p = mPosition[mStep];
        if (p == null) {
            p = new ScrollPosition();
        }
        p.firstVisiblePosition = mList.getFirstVisiblePosition();
        View cv = mList.getChildAt(0);
        if (cv != null) {
            p.firstTop = cv.getTop();
        }
        mPosition[mStep] = p;
        
        mStep++;
        updateStep();
    }
    
    private void updateStep() {
        if (LOG) Log.v(TAG, "updateStep() mStep=" + mStep + ", mMode=" + mMode + ", mIsPick=" + mIsPick);
        ListAdapter adapter = null;
        int candidateIndex = 0;
        switch(mStep) {
        case STEP_SUGGESTION:
            adapter = new ArrayAdapter<City>(LocationSelector.this,
                    R.layout.location_selector_item,
                    android.R.id.text1,
                    mLocation.candidateArea);
            mTitle.setText(getSuggestionTitle());
            if (mSelectedSuggestion != null) {
                candidateIndex = mLocation.candidateArea.indexOf(mSelectedSuggestion);
            } else if (mSavedLocation != null) {
                for(int i = 0, size = mLocation.candidateArea.size(); i < size; i++) {
                    City city = mLocation.candidateArea.get(i);
                    if (city.cityName.equalsIgnoreCase(mSavedLocation.city)) {
                        candidateIndex = i;
                        break;
                    }
                }
            }
            candidateIndex = candidateIndex >= 0 ? candidateIndex : 0;
            mSelectedSuggestion = mLocation.candidateArea.get(candidateIndex);
            break;
        case STEP_PROVINCE:
            adapter = new ArrayAdapter<Province>(LocationSelector.this,
                    R.layout.location_selector_item,
                    android.R.id.text1,
                    mLocation.provinces);
            mTitle.setText(getProvinceTitle());
            if (mSelectedProvince != null) {
                candidateIndex = mLocation.provinces.indexOf(mSelectedProvince);
            } else if (mSavedLocation != null) {
                for(int i = 0, size = mLocation.provinces.size(); i < size; i++) {
                    Province province = mLocation.provinces.get(i);
                    if (province.provinceName.equalsIgnoreCase(mSavedLocation.province)) {
                        candidateIndex = i;
                        break;
                    }
                }
            }
            candidateIndex = candidateIndex >= 0 ? candidateIndex : 0;
            mSelectedProvince = mLocation.provinces.get(candidateIndex);
            break;
        case STEP_CITY:
            adapter = new ArrayAdapter<City>(LocationSelector.this,
                    R.layout.location_selector_item,
                    android.R.id.text1,
                    mSelectedProvince.cities);
            mTitle.setText(getCityTitle());
            if (mSelectedCity != null) {
                candidateIndex = mSelectedProvince.cities.indexOf(mSelectedCity);
            } else if (mSavedLocation != null) {
                for(int i = 0, size = mSelectedProvince.cities.size(); i < size; i++) {
                    City city = mSelectedProvince.cities.get(i);
                    if (city.cityName.equalsIgnoreCase(mSavedLocation.city)) {
                        candidateIndex = i;
                        break;
                    }
                }
            }
            candidateIndex = candidateIndex >= 0 ? candidateIndex : 0;
            mSelectedCity = mSelectedProvince.cities.get(candidateIndex);
            break;
        case STEP_COUNTY:
            adapter = new ArrayAdapter<County>(LocationSelector.this,
                    R.layout.location_selector_item,
                    android.R.id.text1,
                    mSelectedCity.counties);
            mTitle.setText(getCountyTitle());
            if (mSelectedCounty != null) {
                candidateIndex = mSelectedCity.counties.indexOf(mSelectedCounty);
            } else if (mSavedLocation != null) {
                for(int i = 0, size = mSelectedCity.counties.size(); i < size; i++) {
                    County county = mSelectedCity.counties.get(i);
                    if (county.countyName.equalsIgnoreCase(mSavedLocation.county)) {
                        candidateIndex = i;
                        break;
                    }
                }
            }
            candidateIndex = candidateIndex >= 0 ? candidateIndex : 0;
            mSelectedCounty = mSelectedCity.counties.get(candidateIndex);
            break;
        default:
            //nothing
            break;
        }
        mList.setAdapter(adapter);
        mList.setItemChecked(candidateIndex, true);
        mList.setSelection(candidateIndex);
        if (mStep <= mStepStart) {
            mPreStep.setVisibility(View.GONE);
        } else {
            mPreStep.setVisibility(View.VISIBLE);
        }
    }
    
    private String getSuggestionTitle() {
        String title = null;
        if (Utils.LOCATION_MODE_NORMAL == mMode) {
            title = getString(R.string.select_suggestion, getString(R.string.check_all_cities));
        } else {
            title = getString(R.string.unknow_location)
                + getString(R.string.select_suggestion, getString(R.string.check_all_cities));
        }
        return title;
    }
    
    private String getProvinceTitle() {
        String title = null;
        if (Utils.LOCATION_MODE_NORMAL == mMode) {
            title = getString(R.string.select_province);
        } else {
            if (mStep <= mStepStart) {
                title = getString(R.string.unknow_location) + getString(R.string.select_province);
            } else {
                title = getString(R.string.select_province);    
            }
        }
        return title;
    }
    
    private SpannableStringBuilder getCityTitle() {
        String title = getString(R.string.select_city, mSelectedProvince.provinceName);
        int start = title.indexOf(HOLDER_START);
        int end = title.indexOf(HOLDER_END);
        SpannableStringBuilder style = new SpannableStringBuilder(title);
        style.setSpan(new ForegroundColorSpan(Color.RED),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.replace(end, end + 1, "");
        style.replace(start, start + 1, "");
        return style;
    }
    
    private SpannableStringBuilder getCountyTitle() {
        String title = getString(R.string.select_county,
                mSelectedProvince.provinceName, mSelectedCity.cityName);
        int start = title.indexOf(HOLDER_START);
        int end = title.indexOf(HOLDER_END);
        SpannableStringBuilder style = new SpannableStringBuilder(title);
        style.setSpan(new ForegroundColorSpan(Color.RED),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.replace(end, end + 1, "");
        style.replace(start, start + 1, "");
        return style;
    }
    
    public void onClick(View v) {
        onClick(v, true);
    }
    public void onClick(View v, boolean finishLast) {
        if (LOG) Log.v(TAG, "onClick(" + v + ", " + finishLast + ")");
        switch(v.getId()) {
        case R.id.ok:
            if (mLocation == null) {
                if (mIsPick) {
                    setResult(RESULT_CANCELED);
                }
                finish();
                return;
            }
            switch(mStep) {
            case STEP_SUGGESTION:
                if (mSelectedSuggestion == mMockCity) {
                    //check all, move next
                    //if we get suggestion, provinces will be got.
                    nextStep();
                } else {
                    String city = mSelectedSuggestion.cityName;
                    if (finishLast) {
                        if (!mIsPick || mCheck.isChecked()) {//save it
                            saveLocation(Utils.SavedLocation.SUGGESTION_HOLDER + SEPERATOR + city);
                        }
                        if (mIsPick) {
                            Intent data = new Intent();
                            data.putExtra(Utils.RESULT_EXTRA_LOCATION_PERSIST, mCheck.isChecked());
                            data.putExtra(Utils.RESULT_EXTRA_LOCATION_SUGGESTION, city);
                            setResult(RESULT_OK, data);
                        }
                        finish();
                    }
                }
                break;
            case STEP_PROVINCE:
                if (mSelectedProvince.cities != null && mSelectedProvince.cities.size() > 0) {
                    //have cities
                    nextStep();
                } else {
                    String province = mSelectedProvince.provinceName;
                    if (finishLast) {
                        if (!mIsPick || mCheck.isChecked()) {//save it
                            saveLocation(province);
                        }
                        if (mIsPick) {
                            Intent data = new Intent();
                            data.putExtra(Utils.RESULT_EXTRA_LOCATION_PERSIST, mCheck.isChecked());
                            data.putExtra(Utils.RESULT_EXTRA_LOCATION_PROVINCE, province);
                            setResult(RESULT_OK, data);
                        }
                        finish();
                    }
                }
                break;
            case STEP_CITY:
                if (mSelectedCity.counties != null && mSelectedCity.counties.size() > 0) {
                    //have counties
                    nextStep();
                } else {
                    String province = mSelectedProvince.provinceName;
                    String city = mSelectedCity.cityName;
                    if (finishLast) {
                        if (!mIsPick || mCheck.isChecked()) {//save it
                            saveLocation(province + SEPERATOR + city);
                        }
                        if (mIsPick) {
                            Intent data = new Intent();
                            data.putExtra(Utils.RESULT_EXTRA_LOCATION_PERSIST, mCheck.isChecked());
                            data.putExtra(Utils.RESULT_EXTRA_LOCATION_PROVINCE, province);
                            data.putExtra(Utils.RESULT_EXTRA_LOCATION_CITY, city);
                            setResult(RESULT_OK, data);
                        }
                        finish();
                    }
                }
                break;
            case STEP_COUNTY:
                String province = mSelectedProvince.provinceName;
                String city = mSelectedCity.cityName;
                String county = mSelectedCounty.countyName;
                if (finishLast) {
                    if (!mIsPick || mCheck.isChecked()) {//save it
                        saveLocation(province + SEPERATOR + city + SEPERATOR + county);
                    }
                    if (mIsPick) {
                        Intent data = new Intent();
                        data.putExtra(Utils.RESULT_EXTRA_LOCATION_PERSIST, mCheck.isChecked());
                        data.putExtra(Utils.RESULT_EXTRA_LOCATION_PROVINCE, province);
                        data.putExtra(Utils.RESULT_EXTRA_LOCATION_CITY, city);
                        data.putExtra(Utils.RESULT_EXTRA_LOCATION_COUNTY, county);
                        setResult(RESULT_OK, data);
                    }
                    finish();
                }
                break;
            default:
                break;
            }
            break;
        case R.id.cancel:
            if (mIsPick) {
                setResult(RESULT_CANCELED);
            }
            finish();
            break;
        case R.id.previous_step:
            previousStep();
            break;
        default:
            break;
        }
        
    }
    
    private void saveLocation(String location) {
        int countLocation = DB.Setting.UpdateSettings(getContentResolver(),
                DB.Setting.NAME_ENUM_LOCATION_SAVED,
                location);
        int countMode = DB.Setting.UpdateSettings(getContentResolver(),
                DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE,
                DB.Setting.LOCATION_CHECK_MANUAL);
        Toast.makeText(this, R.string.location_saved, Toast.LENGTH_SHORT).show();
        if (LOG) Log.v(TAG, "saveLocation(" + location + ") countLocation=" + countLocation
                + ", countMode=" + countMode);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (LOG) Log.v(TAG, "onItemClick(" + position + ", " + id + ")");
        int index = (int)id;
        switch(mStep) {
        case STEP_SUGGESTION:
            mSelectedSuggestion = mLocation.candidateArea.get(index);
            break;
        case STEP_PROVINCE:
            mSelectedProvince = mLocation.provinces.get(index);
            break;
        case STEP_CITY:
            mSelectedCity = mSelectedProvince.cities.get(index);
            break;
        case STEP_COUNTY:
            mSelectedCounty = mSelectedCity.counties.get(index);
            break;
        default:
            break;
        }
        onClick(mOk, false);
    }
    
    private class SyncLoacationTask extends AsyncTask<Void, Void, ServerStatus> implements OnCancelListener {
        private ProgressDialog mProgressDialog;
        
        private void showProgress() {
            if (LOG) Log.v(TAG, "showProgress() " + this);
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(LocationSelector.this);
                mProgressDialog.setCancelable(true);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setOnCancelListener(this);
            }
            mProgressDialog.setMessage(getString(R.string.update_user_location));
            mProgressDialog.show();
        }
        
        private void hideProgress() {
            if (LOG) Log.v(TAG, "hideProgress() ");
            if (mProgressDialog != null) {
                try {
                    mProgressDialog.dismiss();
                } catch (Exception e) {//to be fixed
                    e.printStackTrace();
                }
            }
        }
        
        public void onCancel(DialogInterface dialog) {
            if (LOG) Log.v(TAG, "hideProgress() ");
            cancel(true);
            onClick(mCancel);
        }
        
        @Override
        protected void onPreExecute() {
            showProgress();
        }
        
        @Override
        protected ServerStatus doInBackground(Void... params) {
            if (LOG) Log.v(TAG, "doInBackground() begin get location.");
            ServerStatus status = ServiceManager.getServiceManager(LocationSelector.this).doAreaSetRequest();
            if (LOG) Log.v(TAG, "doInBackground() end get location. " + status);
            if (Utils.isSuccess(status) && status.data instanceof LocationInfo) {
                mLocation = (LocationInfo)status.data;
                if (mLocation.candidateArea == null || mLocation.candidateArea.size() == 0) {
                    //move next
                    if (mStepStart == STEP_SUGGESTION) {
                        mStep = mStepStart = STEP_PROVINCE;
                    }
                } else {//need check
                    if (mMockCity == null) {
                        mMockCity = new City(getString(R.string.check_all_cities));
                    }
                    mLocation.candidateArea.add(mMockCity);
                }
                String locationCheckMode = DB.Setting.getSettingValue(getContentResolver(),
                        DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE);
                if (locationCheckMode == null || locationCheckMode.trim().equals("")) {
                    locationCheckMode = DB.Setting.LOCATION_CHECK_AUTO;
                }
                if (DB.Setting.LOCATION_CHECK_MANUAL.equals(locationCheckMode)) {
                    String saved = DB.Setting.getSettingValue(getContentResolver(),
                            DB.Setting.NAME_ENUM_LOCATION_SAVED);
                    mSavedLocation = Utils.parseSavedLocation(saved);
                }
            }
            return status;
        }
        
        @Override
        protected void onPostExecute(ServerStatus result) {
            if (LOG) Log.v(TAG, "onPostExecute(" + result + ") isCancelled()=" + isCancelled());
            if (isCancelled()) return;
            mEmpty.setText(R.string.no_location);
            hideProgress();
            String message = null;
            if (Utils.isSuccess(result)) {
                mCheck.setEnabled(true);//update check box
                updateStep();
                message = Utils.getErrorDescription(getResources(), result, null);
            } else {
                mCheck.setEnabled(false);//update check box
                message = Utils.getErrorDescription(getResources(), result, getString(R.string.error_sync_location));
            }
            if (message != null) {
                Toast.makeText(LocationSelector.this, message, Toast.LENGTH_SHORT);
            }
        }
        
    }
}
