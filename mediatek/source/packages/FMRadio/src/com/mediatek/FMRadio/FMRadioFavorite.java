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

package com.mediatek.FMRadio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
import android.content.res.Configuration;
import com.mediatek.featureoption.FeatureOption;

public class FMRadioFavorite extends Activity {
    public static final String TAG = "FMRadioFavorite";
    // Add for cmcc
    public static final String OP = android.os.SystemProperties.get("ro.operator.optr");
    public static final boolean IS_CMCC = ("OP01").equals(OP);
    
    public static final String ACTIVITY_RESULT = "ACTIVITY_RESULT";
    
    private static final String LV_COLUMN_STATION_TYPE = "STATION_TYPE";
    private static final String LV_COLUMN_STATION_NAME = "STATION_NAME";
    private static final String LV_COLUMN_STATION_FREQ = "STATION_FREQ";
    private static final String LV_COLUMN_VALUE_FREQ = "STATION_FREQ_VALUE"; // Save the frequency value into hash map.
    private static final String FM_SAVE_INSTANCE_STATE_FAVORITE_NAME = "FM_SAVE_INSTANCE_STATE_FAVORITE_NAME";
    private static final String FM_SAVE_INSTANCE_STATE_FAVORITE_FREQ = "FM_SAVE_INSTANCE_STATE_FAVORITE_FREQ";
    private static final int LV_CAPACITY = 1024;
    private static final int CONTMENU_ID_EDIT = 1;
    private static final int CONTMENU_ID_DELETE = 2;
    private static final int CONTMENU_ID_ADD = 3;
    
    // The max count of characters limited by edit text in add_edit_dlg.
    private static final int MAX_STATION_NAME_LENGTH = 15;
    private static final int MAX_STATION_FREQUENCY_LENGTH = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 6 : 5;
    
    private ListView mLvFavorites = null;
    private SimpleAdapter mSimpleAdapter = null;
    private ArrayList<HashMap<String, Object>> mListStations = null;
    private int mStationCount = 0; // Record how many stations displayed in list view.
    private static final int DLGID_ADD_EDIT_STATION = 1;
    private static final int DLGID_DELETE_CONFIRM = 2;
    
    // Dialog to edit or add station.
    private int mPosition = 0; // Record the long clicked station position in the list view.
    private String mDlgStationName = null; // Record the long clicked station name.
    private int mDlgStationFreq = 0; // Record the long clicked station frequency.
    private int mStationType = 0; // Record the long clicked station type.
    private int mStationOperation = 0; // Record the long clicked station operation.
    private AlertDialog mDialogAdd = null;
    private AlertDialog mDialogEdit = null;
    private String mCurrentStationName = null;
    private EditText mEditTextFrequency = null;
    private static final int CONVERT_RATE = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 100 : 10;
    
    public void onCreate(Bundle savedInstanceState) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioFavorite.onCreate");
        super.onCreate(savedInstanceState);
        // Bind the activity to FM audio stream.
        setVolumeControlStream(AudioManager.STREAM_FM);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.favorite);
        
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.favorite_manager);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //Get saved instance state
        if(savedInstanceState != null)
        {
            mDlgStationName = savedInstanceState.getString(FM_SAVE_INSTANCE_STATE_FAVORITE_NAME);
            mDlgStationFreq = savedInstanceState.getInt(FM_SAVE_INSTANCE_STATE_FAVORITE_FREQ);
        }
        
        mListStations = new ArrayList<HashMap<String, Object>>(LV_CAPACITY);
        mSimpleAdapter = new SimpleAdapter(
            this,
            mListStations,
            R.layout.simpleadapter,
            new String[]{LV_COLUMN_STATION_TYPE, LV_COLUMN_STATION_FREQ, LV_COLUMN_STATION_NAME},
            new int[]{R.id.lv_station_type, R.id.lv_station_freq, R.id.lv_station_name}
        );
        mSimpleAdapter.setViewBinder(
            new SimpleAdapter.ViewBinder() {
                
                public boolean setViewValue(View view, Object data,
                        String textRepresentation) {
                    if (view instanceof ImageView) {
                        ((ImageView)view).setBackgroundResource(0);
                        // If the station is a favorite station, set its icon;
                        // otherwise, it does not have an icon.
                        if (FMRadioStation.STATION_TYPE_FAVORITE == (Integer)data) {
                            ((ImageView)view).setImageResource(R.drawable.btn_fm_favorite_on);
                        }
                        else {
                            ((ImageView)view).setImageResource(0);
                        }
                        
                        return true;
                    }
                    return false;
                }
            }
        );
        mLvFavorites = (ListView)findViewById(R.id.station_list);
        mLvFavorites.setAdapter(mSimpleAdapter);
        initListView();
        mLvFavorites.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Set the selected frequency to main UI and finish the favorite manager.
                    HashMap<String, Object> hashmap = mListStations.get(position);
                    int iStation = (Integer)hashmap.get(LV_COLUMN_VALUE_FREQ);
                    Intent intentResult = new Intent();
                    intentResult.putExtra(ACTIVITY_RESULT, iStation);
                    setResult(RESULT_OK, intentResult);
                    finish();
                }
            }
        );
        mLvFavorites.setOnCreateContextMenuListener(
            new View.OnCreateContextMenuListener() {
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.setHeaderTitle(R.string.contmenu_title);
                    HashMap<String, Object> hashmap = mListStations.get(((AdapterView.AdapterContextMenuInfo)menuInfo).position);
                    int iType = (Integer)hashmap.get(LV_COLUMN_STATION_TYPE);
                    if (FMRadioStation.STATION_TYPE_SEARCHED == iType) {
                        // Searched station.
                        menu.add(0, CONTMENU_ID_ADD, 0, getProjectString(R.string.add_to_favorite1, R.string.add_to_favorite));
                    }
                    else {
                        // Favorite station.
                        menu.add(0, CONTMENU_ID_EDIT, 0, R.string.contmenu_item_edit);
                        menu.add(0, CONTMENU_ID_DELETE, 0, getProjectString(R.string.contmenu_item_delete1, R.string.contmenu_item_delete));
                    }
                }
            }
        );
        
        FMRadioLogUtils.d(TAG, "<<< FMRadioFavorite.onCreate");
    }
    
    public boolean onContextItemSelected(MenuItem item) {
        mPosition = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
        HashMap<String, Object> hashmap = mListStations.get(mPosition);
        mDlgStationName = (String)hashmap.get(LV_COLUMN_STATION_NAME);
        mDlgStationFreq = (Integer)hashmap.get(LV_COLUMN_VALUE_FREQ);
        mStationType = (Integer)hashmap.get(LV_COLUMN_STATION_TYPE);
        mStationOperation = item.getItemId();
        switch (mStationOperation) {
            case CONTMENU_ID_ADD: {
                if (FMRadioStation.getStationCount(this, FMRadioStation.STATION_TYPE_FAVORITE) >= FMRadioStation.MAX_FAVORITE_STATION_COUNT) {
                    // Favorite list is full. Toast it.
                    Toast.makeText(this, getProjectString(R.string.toast_favorite_full1, R.string.toast_favorite_full), Toast.LENGTH_SHORT).show();
                    break;
                } else if (FMRadioStation.isStationExist(this, mDlgStationFreq, FMRadioStation.STATION_TYPE_FAVORITE)) {
                // The station is already in favorite list.
                    Toast.makeText(this, getProjectString(R.string.toast_already_favorite1, R.string.toast_already_favorite), Toast.LENGTH_SHORT).show();
                    break;
                }
                
                
                // Pop up a dialog to edit the new station.
                // Do not call showDialog because the dialog shown by showDialog will be restored after config changed.
                // showDialog(DLGID_ADD_EDIT_STATION);
                View v = View.inflate(this, R.layout.addstation, null);
                EditText editTextStationName = (EditText)v.findViewById(R.id.dlg_add_station_name_text);
                editTextStationName.setFilters(
                    new InputFilter[]{new InputFilter.LengthFilter(MAX_STATION_NAME_LENGTH)}
                );
                mDialogAdd = new AlertDialog.Builder(this)
                    // Must call setTitle here or the title will not be displayed.
                    .setTitle(R.string.app_name)
                    .setView(v)
                    .setPositiveButton(R.string.btn_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mDlgStationName = ((EditText)mDialogAdd.findViewById(R.id.dlg_add_station_name_text)).getText().toString().trim();
                                if (0 == mDlgStationName.length()) {
                                    HashMap map = mListStations.get(mPosition);
                                    mCurrentStationName = (String)map.get(LV_COLUMN_STATION_NAME);
                                    //msDlgStationName = getString(R.string.default_station_name);
                                    if (null == mCurrentStationName || "" == mCurrentStationName) {
                                        mDlgStationName = getString(R.string.default_station_name);
                                    } else {
                                        mDlgStationName = mCurrentStationName;
                                    }
                                }                               
                               // Insert the new station to data base and display it in list view.
                               // can't use upateStationInDB,because this function is find channel by frequency and station type, 
                               // so it just use to update  station name
                               FMRadioStation.insertStationToDB(
                                       FMRadioFavorite.this, mDlgStationName,
                                       mDlgStationFreq, FMRadioStation.STATION_TYPE_FAVORITE
                                       );
                               FMRadioStation.deleteStationInDB(FMRadioFavorite.this, mDlgStationFreq, FMRadioStation.STATION_TYPE_SEARCHED);
                               HashMap<String, Object> hashmap = new HashMap<String, Object>();
                               hashmap.put(LV_COLUMN_STATION_TYPE, FMRadioStation.STATION_TYPE_FAVORITE);
                               hashmap.put(LV_COLUMN_STATION_FREQ, formatStation(mDlgStationFreq));
                               hashmap.put(LV_COLUMN_STATION_NAME, mDlgStationName);
                               hashmap.put(LV_COLUMN_VALUE_FREQ, mDlgStationFreq);
                               // Sort the favorite stations.
                               mListStations.remove(mPosition);
                               mListStations.add(getInsertIndex(mDlgStationFreq), hashmap);
                               mSimpleAdapter.notifyDataSetChanged();
                                
                                mDialogAdd.cancel();
                            }
                        }
                    )
                    .setNegativeButton(R.string.btn_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mDialogAdd.cancel();
                            }
                        }
                    )
                    .create();
                mDialogAdd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                
                ((TextView)v.findViewById(R.id.dlg_add_station_freq_text))
                    .setText(formatStation(mDlgStationFreq));
                HashMap map = mListStations.get(mPosition);
                mCurrentStationName = (String)map.get(LV_COLUMN_STATION_NAME);
                if (null == mCurrentStationName || "" == mCurrentStationName) {
                    ((TextView)v.findViewById(R.id.dlg_add_station_name_text))
                        .setHint(R.string.default_station_name);
                } else {
                    ((TextView)v.findViewById(R.id.dlg_add_station_name_text))
                        .setHint(mCurrentStationName);
                }
                editTextStationName.requestFocus();
                editTextStationName.requestFocusFromTouch();
                mDialogAdd.setTitle(getProjectString(R.string.add_to_favorite1, R.string.add_to_favorite));
                ((TextView)v.findViewById(R.id.dlg_add_station_name_text)).setText("");
                mDialogAdd.show();
                break;
            }
            case CONTMENU_ID_EDIT:{
                
                // Pop up a dialog to edit the new station.
                // Do not call showDialog because the dialog shown by showDialog will be restored after config changed.
                View v = View.inflate(this, R.layout.editstation, null);
                EditText editTextStationName = (EditText)v.findViewById(R.id.dlg_edit_station_name_text);
                mEditTextFrequency = (EditText)v.findViewById(R.id.dlg_edit_station_freq_text);
                mEditTextFrequency.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(MAX_STATION_FREQUENCY_LENGTH)});
                if (FeatureOption.MTK_FM_50KHZ_SUPPORT) {
                    mEditTextFrequency.addTextChangedListener(watcher);
                }
                
                editTextStationName.setFilters(
                    new InputFilter[]{new InputFilter.LengthFilter(MAX_STATION_NAME_LENGTH)}
                );
                
                mDialogEdit = new AlertDialog.Builder(this)
                    // Must call setTitle here or the title will not be displayed.
                    .setTitle(R.string.app_name)   
                    .setView(v)
                    .setPositiveButton(R.string.btn_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mDlgStationName = ((EditText)mDialogEdit.findViewById(R.id.dlg_edit_station_name_text)).getText().toString().trim();
                                String newStationFreqStr = ((EditText)mDialogEdit.findViewById(R.id.dlg_edit_station_freq_text)).getText().toString().trim();
                                if (0 == mDlgStationName.length()) {
                                    HashMap map = mListStations.get(mPosition);
                                    mCurrentStationName = (String)map.get(LV_COLUMN_STATION_NAME);
                                    if (null== mCurrentStationName || "" == mCurrentStationName) {
                                        mDlgStationName = getString(R.string.default_station_name);
                                    } else {
                                        mDlgStationName=mCurrentStationName;
                                    }
                                }
                                float newStationFreq = 0;
                                try {
                                    newStationFreq = Float.parseFloat(newStationFreqStr);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                
                                int newStation = (int)(newStationFreq * CONVERT_RATE);
                                if (isValidFrequency(newStation)) {
                                    // if station is exist in channel list delete it
                                    // ignore current station, because current station not display in listview
                                    if (FMRadioStation.isStationExist(FMRadioFavorite.this, newStation, FMRadioStation.STATION_TYPE_FAVORITE) && (newStation != mDlgStationFreq)) {
                                        FMRadioStation.deleteStationInDB(FMRadioFavorite.this, newStation, FMRadioStation.STATION_TYPE_FAVORITE);
                                        mListStations.remove(getDeleteFavoriteIndex(newStation));
                                        if (newStation < mDlgStationFreq) {
                                            mPosition = mPosition - 1;
                                        }
                                    } else  if (FMRadioStation.isStationExist(FMRadioFavorite.this, newStation, FMRadioStation.STATION_TYPE_SEARCHED)) {
                                        FMRadioStation.deleteStationInDB(FMRadioFavorite.this, newStation, FMRadioStation.STATION_TYPE_SEARCHED);
                                        mListStations.remove(getDeleteSearchIndex(newStation));
                                    }
                                    FMRadioStation.updateStationToDB(FMRadioFavorite.this, mDlgStationName, mDlgStationFreq, newStation, FMRadioStation.STATION_TYPE_FAVORITE);
                                    // update the station displayed in listview
                                    HashMap<String, Object> hashmap = new HashMap<String, Object>();
                                    hashmap.put(LV_COLUMN_STATION_TYPE, FMRadioStation.STATION_TYPE_FAVORITE);
                                    if (!newStationFreqStr.contains(".")){
                                        newStationFreqStr += ".0";
                                    } else if (newStationFreqStr.endsWith(".")) {
                                        newStationFreqStr += "0";
                                    }
                                    hashmap.put(LV_COLUMN_STATION_FREQ, newStationFreqStr);
                                    hashmap.put(LV_COLUMN_STATION_NAME, mDlgStationName);
                                    hashmap.put(LV_COLUMN_VALUE_FREQ, newStation);
                                    // sort favorite stations
                                    mListStations.remove(mPosition);
                                    mListStations.add(getInsertIndex(newStation), hashmap);
                                    mSimpleAdapter.notifyDataSetChanged();
                                    
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.toast_invalid_frequency, Toast.LENGTH_SHORT).show();
                                }
                                mDialogEdit.cancel();
                            }
                        }
                    )
                    .setNegativeButton(R.string.btn_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mDialogEdit.cancel();
                            }
                        }
                    )
                    .create();              
                mDialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                
                ((TextView)v.findViewById(R.id.dlg_edit_station_freq_text))
                    .setText(formatStation(mDlgStationFreq));
                HashMap map = mListStations.get(mPosition);
                mCurrentStationName = (String)map.get(LV_COLUMN_STATION_NAME);
                if (null == mCurrentStationName || "" == mCurrentStationName) {
                    ((TextView)v.findViewById(R.id.dlg_edit_station_name_text))
                        .setHint(R.string.default_station_name);
                }else{
                    ((TextView)v.findViewById(R.id.dlg_edit_station_name_text))
                        .setHint(mCurrentStationName);
                }
                editTextStationName.requestFocus();
                editTextStationName.requestFocusFromTouch();
                // Edit
                mDialogEdit.setTitle(R.string.dlg_addedit_title_edit);
                ((TextView)v.findViewById(R.id.dlg_edit_station_name_text))
                            .setText(mDlgStationName);
                Editable text = editTextStationName.getText();
                int index = text.length();
                Selection.setSelection(text, index);
                
                mDialogEdit.show();
                break;
            }
            case CONTMENU_ID_DELETE: {
                //Do not call showDialog because the dialog shown by showDialog will be restored after config changed.
                AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(getProjectString(R.string.dlg_delete_confirm_title1, R.string.dlg_delete_confirm_title))
                    .setMessage(getProjectString(R.string.dlg_delete_confirm_text1, R.string.dlg_delete_confirm_text))
                    .setPositiveButton(R.string.btn_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete the station in data base.
                                HashMap<String, Object> hashmap = mListStations.get(mPosition);
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                String stationName = (String)hashmap.get(LV_COLUMN_STATION_NAME);
                                if (null != hashmap && null != hashmap.get(LV_COLUMN_VALUE_FREQ)) {
                                    int stationFreq = ((Integer)hashmap.get(LV_COLUMN_VALUE_FREQ)).intValue();
                                    map.put(LV_COLUMN_STATION_TYPE, FMRadioStation.STATION_TYPE_SEARCHED);
                                    map.put(LV_COLUMN_STATION_FREQ, formatStation(mDlgStationFreq));
                                    map.put(LV_COLUMN_STATION_NAME, stationName);
                                    map.put(LV_COLUMN_VALUE_FREQ, stationFreq);
                                    //can't use upateStationInDB,because this function is find channel by frequency and station type, 
                                    //so it just use to update  station name
                                    FMRadioStation.deleteStationInDB(
                                        FMRadioFavorite.this,
                                        (Integer)hashmap.get(LV_COLUMN_VALUE_FREQ),
                                        FMRadioStation.STATION_TYPE_FAVORITE
                                    );
                                    FMRadioStation.insertStationToDB(FMRadioFavorite.this, stationName, stationFreq, FMRadioStation.STATION_TYPE_SEARCHED);
                                    
                                    // Delete the station in list view.
                                    mListStations.remove(mPosition);
                                    mListStations.add(getInsertSearchIndex(stationFreq),map);
                                    //miStationCount--;
                                    mSimpleAdapter.notifyDataSetChanged();
                                } else {
                                    FMRadioLogUtils.e(TAG, "Error: can't find key in hashmap");
                                }
                                
                            }
                        }
                    )
                    .setNegativeButton(R.string.btn_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                    )
                    .create();
                dialog.show();
                break;
            }
            default: {
                FMRadioLogUtils.e(TAG, "Error: Invalid menu item.");
                break;
            }
        }
        return false;
    }
    
    InputFilter filter = new InputFilter() {

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                int dstart, int dend) {
            final int accuracy = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 2 : 1;
            
            if ("".equals(source.toString())) {
                return null;
            }
            
            // according the point divide string 
            String[] splitArray = dest.toString().split("\\.");
            // input have point, should delete the redundant
            if(splitArray.length > 1) {
                String fraction = splitArray[1];
                int deleteIndex = fraction.length() + 1 - accuracy;
                if (deleteIndex > 0) {
                    int dotIndex = dest.toString().indexOf(".") + 1;
                    if (dstart > dotIndex) {
                        return source.subSequence(start, end - deleteIndex);
                    } else {
                        return dest.subSequence(dstart, dend) + source.toString();
                    }
                }
            }
            return null;

        }
    };
    // add for 50khz
    private TextWatcher watcher = new TextWatcher() {
        
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (null == mDialogEdit) {
                return;
            }
            CharSequence cs = mEditTextFrequency.getText();
            FMRadioLogUtils.e(TAG, "edit frequency string = " + cs.toString());
            float frequency = 0;
            try {
                frequency = Float.parseFloat(cs.toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            int station = Math.round(frequency * 100); 
            Button positiveButton = mDialogEdit.getButton(DialogInterface.BUTTON_POSITIVE);
            // just the last of digital is 5 or 0
            if ((station % 5) == 0) {
                if (null != positiveButton) {
                    positiveButton.setEnabled(true);
                }
                
            } else {
                if (null != positiveButton) {
                    positiveButton.setEnabled(false);
                }
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_invalid_input), Toast.LENGTH_SHORT).show();
            }
        }
        
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            
        }
        
        public void afterTextChanged(Editable s) {
            
            
        }
    };
    
    private void initListView() {
        HashMap<String, Object> hashmap = null;
        // Display all the stations in the data base.
        Uri uri = FMRadioStation.Station.CONTENT_URI;
        Cursor cur = managedQuery(uri, FMRadioStation.columns, null, null, null);
        if (null != cur) {
            // Add favorite stations into list.
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                int iType = cur.getInt(cur.getColumnIndex(FMRadioStation.Station.COLUMN_STATION_TYPE));
                if (FMRadioStation.STATION_TYPE_FAVORITE == iType) {
                    String sStationName = cur.getString(cur.getColumnIndex(FMRadioStation.Station.COLUMN_STATION_NAME));
                    int iStation = cur.getInt(cur.getColumnIndex(FMRadioStation.Station.COLUMN_STATION_FREQ));
                    hashmap = new HashMap<String, Object>();
                    hashmap.put(LV_COLUMN_STATION_TYPE, iType);
                    hashmap.put(LV_COLUMN_STATION_FREQ, formatStation(iStation));
                    hashmap.put(LV_COLUMN_STATION_NAME, sStationName);
                    hashmap.put(LV_COLUMN_VALUE_FREQ, iStation);
                    mListStations.add(getInsertIndex(iStation), hashmap);
                    mStationCount++;
                }
                
                cur.moveToNext();
            }
            
            // Add searched stations into list.
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                int iType = cur.getInt(cur.getColumnIndex(FMRadioStation.Station.COLUMN_STATION_TYPE));
                if (FMRadioStation.STATION_TYPE_SEARCHED == iType) {
                    String sStationName = cur.getString(cur.getColumnIndex(FMRadioStation.Station.COLUMN_STATION_NAME));
                    int iStation = cur.getInt(cur.getColumnIndex(FMRadioStation.Station.COLUMN_STATION_FREQ));
                    hashmap = new HashMap<String, Object>();
                    hashmap.put(LV_COLUMN_STATION_TYPE, iType);
                    hashmap.put(LV_COLUMN_STATION_FREQ, formatStation(iStation));
                    hashmap.put(LV_COLUMN_STATION_NAME, sStationName);
                    hashmap.put(LV_COLUMN_VALUE_FREQ, iStation);
                    mListStations.add(getInsertSearchIndex(iStation),hashmap);
                    mStationCount++;
                }
                
                cur.moveToNext();
            }
        }
        
        mSimpleAdapter.notifyDataSetChanged();
    }
    
    private int getInsertIndex(int stationFreq) {
        int iRet = 0;
        int iFavoriteCount = mListStations.size();
        HashMap<String, Object> hashmap = null;
        for (; iRet < iFavoriteCount; iRet++) {
            hashmap = mListStations.get(iRet);
            if (null == hashmap) {
                break;
            }
            if (FMRadioStation.STATION_TYPE_FAVORITE != (Integer)hashmap.get(LV_COLUMN_STATION_TYPE)
                || (Integer)hashmap.get(LV_COLUMN_VALUE_FREQ) >= stationFreq) {
                break;
            }
        }
        return iRet;
    }
    private int getInsertSearchIndex(int stationFreq) {
        int iRet = 0;
        int iFavoriteCount = mListStations.size();
        HashMap<String, Object> hashmap = null;
        for (; iRet < iFavoriteCount; iRet++) {
            hashmap = mListStations.get(iRet);
            if (null == hashmap) {
                break;
            }
            if (FMRadioStation.STATION_TYPE_FAVORITE != (Integer)hashmap.get(LV_COLUMN_STATION_TYPE)
                && (Integer)hashmap.get(LV_COLUMN_VALUE_FREQ) >= stationFreq) {
                break;
            }
        }
        return iRet;
    }
    private int getDeleteFavoriteIndex(int stationFreq) {
        int iRet = 0;
        int iFavoriteCount = mListStations.size();
        HashMap<String, Object> hashmap = null;
        for (; iRet < iFavoriteCount; iRet++) {
            hashmap = mListStations.get(iRet);
            if (null == hashmap) {
                break;
            }
            if (FMRadioStation.STATION_TYPE_FAVORITE == (Integer)hashmap.get(LV_COLUMN_STATION_TYPE)
                && (Integer)hashmap.get(LV_COLUMN_VALUE_FREQ) == stationFreq) {
                break;
            }
        }
        return iRet;
    }
    private int getDeleteSearchIndex(int stationFreq) {
        int iRet = 0;
        int iFavoriteCount = mListStations.size();
        HashMap<String, Object> hashmap = null;
        for (; iRet < iFavoriteCount; iRet++) {
            hashmap = mListStations.get(iRet);
            if (null == hashmap) {
                break;
            }
            if (FMRadioStation.STATION_TYPE_SEARCHED == (Integer)hashmap.get(LV_COLUMN_STATION_TYPE)
                && (Integer)hashmap.get(LV_COLUMN_VALUE_FREQ) == stationFreq) {
                break;
            }
        }
        return iRet;
    }
    //Save instance state when being destroyed
    protected void onSaveInstanceState(Bundle outState) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioFavorite.onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString(FM_SAVE_INSTANCE_STATE_FAVORITE_NAME, mDlgStationName);
        outState.putInt(FM_SAVE_INSTANCE_STATE_FAVORITE_FREQ, mDlgStationFreq);
        FMRadioLogUtils.d(TAG, "<<< FMRadioFavorite.onSaveInstanceState");
    }

    public void onStart() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioFavorite.onStart");
        super.onStart();
        FMRadioLogUtils.d(TAG, "<<< FMRadioFavorite.onStart");
    }

    public void onResume() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioFavorite.onResume");
        super.onResume();
        FMRadioLogUtils.d(TAG, "<<< FMRadioFavorite.onResume");
    }

    public void onPause() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioFavorite.onPause");
        super.onPause();
        FMRadioLogUtils.d(TAG, "<<< FMRadioFavorite.onPause");
    }

    public void onStop() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioFavorite.onStop");
        super.onStop();
        FMRadioLogUtils.d(TAG, "<<< FMRadioFavorite.onStop");
    }

    public void onDestroy() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioFavorite.onDestroy");
        super.onDestroy();
        FMRadioLogUtils.d(TAG, "<<< FMRadioFavorite.onDestroy");
    }

    public void onConfigurationChanged(Configuration newConfig) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioFavorite.onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        FMRadioLogUtils.d(TAG, "<<< FMRadioFavorite.onConfigurationChanged");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String getProjectString(int resId1, int resId2) {
        if (IS_CMCC) {
            return getString(resId1);
        } else {
            return getString(resId2);
        }
    }
    
    private boolean isValidFrequency (int station) {
        if (FeatureOption.MTK_FM_50KHZ_SUPPORT) {
            final int checkNumber = 5;
            return (station >= FMRadioStation.LOWEST_STATION && station <= FMRadioStation.HIGHEST_STATION) && (station % checkNumber == 0);
        } else {
            return (station >= FMRadioStation.LOWEST_STATION && station <= FMRadioStation.HIGHEST_STATION);
        }
    }
    
    private String formatStation(int station) {
        float frequency = (float)station / CONVERT_RATE;
        String result = null;
        if (FeatureOption.MTK_FM_50KHZ_SUPPORT) {
            result = String.format(Locale.ENGLISH, "%.2f", Float.valueOf(frequency));
        } else {
            result = String.format(Locale.ENGLISH, "%.1f", Float.valueOf(frequency));
        }
        return result;
    }
}
