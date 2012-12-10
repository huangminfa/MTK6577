/*
 *  Copyright Statement:
 *  --------------------
 *  This software is protected by Copyright and the information contained
 *  herein is confidential. The software may not be copied and the information
 *  contained herein may not be used or disclosed except with the written
 *  permission of MediaTek Inc. (C) 2005
 *
 *  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
 *  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 *  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 *  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 *  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
 *  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
 *  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
 *  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
 *
 *  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
 *  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 *  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 *  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
 *  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
 *  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
 *  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
 *  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
 *  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
 *
 */

/*
 * Filename:
 * ---------
 * PicturePicker.Java
 *
 * Project:
 * --------
 *   ALPS
 *
 * Description:
 * ------------
 *   Implement the picture selector activity
 *
 * Author:
 * -------
 *    Howard Yeh
 *
 */

package com.android.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.mediatek.xlog.Xlog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;

import java.io.File;


public class PicturePicker extends Activity implements View.OnClickListener{

    private static final String TAG = "PicturePicker";

    public final static String PICTURE_COUNT = "picCount";
    public final static String FILE_PATHS = "paths";
    public final static String PICTURES_TO_PICK = "pictures-to-pick";

    private final static String KEY_EV0 = "key_ev0";
    private final static String KEY_EVP = "key_evp";
    private final static String KEY_EVM = "key_evm";

    private final static int HEIGHT_PADDING = 40;
    private final static int WIDTH_PADDING = 200;

    private String [] mPaths = null;

    EVPickerItem mEv0;
    EVPickerItem mEvp;
    EVPickerItem mEvm;

    private boolean mHasStorage;
    int mPictures2Pick = 0;         // 0 : unlimited, 1: only 1

    private static final int MSG_FINISH = 1;

    private int mOrientation = 0;
    private final static boolean ROTATE_SUPPORTED = false;

    private final Handler mHandler = new MainHandler();

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FINISH:
                    finish();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Storage.updateDefaultDirectory(this, true);
        setContentView(R.layout.ev_select_view);

        mEv0 = (EVPickerItem) findViewById(R.id.checkBoxEv0);
        mEvp = (EVPickerItem) findViewById(R.id.checkBoxEvPlus);
        mEvm = (EVPickerItem) findViewById(R.id.checkBoxEvMinus);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mHasStorage = Storage.getAvailableSpace() > Storage.LOW_STORAGE_THRESHOLD;
            if (mHasStorage) {
                setParameters(extras, savedInstanceState);
            }
        } else {
            // TODO: error handling
        }
        setOnClicks();
    }

    @Override
    public void onResume() {
        super.onResume();
        Storage.updateDefaultDirectory(this, true);
        if (Storage.getAvailableSpace() < Storage.LOW_STORAGE_THRESHOLD) {
            View btnDone = (View) findViewById (R.id.btnEvSelDoneGroup);
            btnDone.setVisibility(View.INVISIBLE);
            mEv0.setVisibility(View.INVISIBLE);
            mEvp.setVisibility(View.INVISIBLE);
            mEvm.setVisibility(View.INVISIBLE);
            mHandler.sendEmptyMessage(MSG_FINISH);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save selected items, to restore them when goes into powersaving mode.
        outState.putBoolean(KEY_EV0, mEv0.isSelected());
        outState.putBoolean(KEY_EVP, mEvp.isSelected());
        outState.putBoolean(KEY_EVM, mEvm.isSelected());
        super.onSaveInstanceState(outState);
    }

    
    private void setParameters(Bundle params, Bundle history){

        boolean init = true;
        boolean isEv0Selected = false;
        boolean isEvpSelected = false;
        boolean isEvmSelected = false;

        if (history != null) {
            init = !history.containsKey(KEY_EV0);
            isEv0Selected = history.getBoolean(KEY_EV0);
            isEvpSelected = history.getBoolean(KEY_EVP);
            isEvmSelected = history.getBoolean(KEY_EVM);
        }

        mPaths = params.getStringArray(FILE_PATHS);
        int n = mPaths.length;

        WindowManager windowManager =
            (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        int thumbHeight =  windowManager.getDefaultDisplay().getHeight() / 2 - HEIGHT_PADDING;
        int thumbWidth = thumbHeight * 4 / 3;
        
        int maxWidth = windowManager.getDefaultDisplay().getWidth() - WIDTH_PADDING; //width of control
        if (thumbWidth > maxWidth) {
            
            thumbWidth = maxWidth;
            thumbHeight = thumbWidth * 3 / 4;
        }

        //create bitmaps
        int [] widgetId = {R.id.checkBoxEvMinus, R.id.checkBoxEv0, R.id.checkBoxEvPlus};

        EVPickerItem p;
        Bitmap bmp;
        Matrix matrix = null;
        for (int i = 0; i < n; i++) {
            p = (EVPickerItem) findViewById(widgetId[i]);
            bmp = null;
            Xlog.i(TAG, "thumb: " + thumbWidth + "x" + thumbHeight);
            bmp = Util.makeBitmap(mPaths[i], thumbWidth, thumbWidth * thumbHeight);
            if (bmp == null) {
                Xlog.e(TAG, "File is gone:" + mPaths[i]);
                View btnDone = (View) findViewById (R.id.btnEvSelDoneGroup);
                btnDone.setVisibility(View.INVISIBLE);
                mHandler.sendEmptyMessage(MSG_FINISH);
                return;
            }
            if (ROTATE_SUPPORTED) {
                matrix = new Matrix();
                mOrientation = Camera.sOrientationNow;
                if(mOrientation == 0){
                    matrix.postScale(((float)thumbHeight) / bmp.getWidth(), ((float)thumbWidth) / bmp.getHeight());
                    matrix.postRotate(270);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                }else if(mOrientation == 180){
                    matrix.postScale(((float)thumbHeight) / bmp.getWidth(), ((float)thumbWidth) / bmp.getHeight());
                    matrix.postRotate(90);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                }else if(mOrientation == 90){
                    matrix.postScale(((float) thumbHeight) / bmp.getHeight(),((float) thumbWidth) / bmp.getWidth());
                    matrix.postRotate(180);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),  bmp.getHeight(), matrix, true);
                }else {
                    bmp = Bitmap.createScaledBitmap(bmp, thumbWidth, thumbHeight, true);
                }
            } else {
                bmp = Bitmap.createScaledBitmap(bmp, thumbWidth, thumbHeight, true);
            }
            p.setImageBitmap(bmp);

            // set EV0 as chosen by default.
            if (widgetId[i] == R.id.checkBoxEv0 && (init || isEv0Selected)) {
                p.performClick();
            } else if (isEvmSelected && widgetId[i] == R.id.checkBoxEvMinus) {
                p.performClick();
            } else if (isEvpSelected && widgetId[i] == R.id.checkBoxEvPlus) {
                p.performClick();
            }
        }

        // hide done key if nothing being selected.
        if (!init && !(isEv0Selected || isEvmSelected || isEvpSelected)) {
            View btnDone = (View) findViewById (R.id.btnEvSelDoneGroup);
            btnDone.setVisibility(View.INVISIBLE);
        }
        mPictures2Pick = params.getInt(PICTURES_TO_PICK, 0);
    }


    private void setOnClicks(){
        View cancel = findViewById(R.id.btnEvSelCancel);
        View done = findViewById(R.id.btnEvSelDone);
        cancel.setOnClickListener(this);
        done.setOnClickListener(this);

        mEv0.setOnClickListener(this);
        mEvp.setOnClickListener(this);
        mEvm.setOnClickListener(this);
    }
    
    private boolean saveOrDelete(boolean checked, String filePath) {
        boolean ret = true;
        
        if (!checked) {
            if (mHasStorage) {
                new File(filePath).delete();
            }
            ret = false;
        }
        return ret;
    }


    private void evSelectDone() {

        int n = 0;
        String [] mPathSelected = new String[3];
        final EVPickerItem [] EvPickers = {mEvm, mEv0, mEvp};

        for (int i = 0; i < 3; i++) {
            if (saveOrDelete(EvPickers[i].isSelected(), mPaths[i])) {
                File f = new File(mPaths[i]);

                if (f.exists()) {
                    mPathSelected[n++] = mPaths[i];
                } else {
                    Xlog.e(TAG, "File is gone! :" + mPaths[i]);
                }
            }
        }

        Bundle results = new Bundle();
        results.putInt(PICTURE_COUNT, n);
        results.putStringArray(FILE_PATHS, mPathSelected);
        Intent i = new Intent();
        if (n > 0) {
            setResult(RESULT_OK, i.putExtras(results));
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }

    private void evSelectCancel() {
        // delete all pictures.
        // no extra, just return.
        if (mPaths != null) {
            for (String s : mPaths) {
                new File(s).delete();
            }
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private boolean isAnyImgSelected() {
        return (mEv0.isSelected() || mEvp.isSelected() || mEvm.isSelected());
    }

    public void onClick(View v) {
        int id = v.getId();
        
        switch (id) {
            case R.id.btnEvSelDone:
                evSelectDone();
                break;

            case R.id.checkBoxEv0:
            case R.id.checkBoxEvPlus:
            case R.id.checkBoxEvMinus: {
                // if any on the ev key is checked.enable done key.
                boolean enable = isAnyImgSelected();
                View btnDone = (View) findViewById (R.id.btnEvSelDoneGroup);
                if (mHasStorage) {
                    btnDone.setVisibility(enable ? View.VISIBLE: View.INVISIBLE);
                }
                
                if (mPictures2Pick == 1 && ((EVPickerItem) findViewById(id)).isSelected()){
                    /* disable the other selected item */
                    final int[] evIds = { R.id.checkBoxEv0,
                            R.id.checkBoxEvPlus, R.id.checkBoxEvMinus };
                    EVPickerItem p;
                    for (int i : evIds) {
                        p = (EVPickerItem) findViewById(i);
                        if (i != id && p.isSelected()) {
                            p.performClick();
                        }
                    }
                }
            }
                break;

            case R.id.btnEvSelCancel:
                // set the EV select view invisible.
                // delete pictures.
                evSelectCancel();
                break;
        }
    }

}
