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

package com.android.music;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

class MusicDialog extends AlertDialog {
    private final DialogInterface.OnClickListener mListener;
    private View mView;
    private Activity mActivity;
    private final DialogInterface.OnCancelListener mCancelListener = new DialogInterface.OnCancelListener () {
        public void onCancel(DialogInterface dialog) {
            mActivity.finish();
        }
    };

    public MusicDialog(Context context, DialogInterface.OnClickListener listener, View view) {
        super(context);
        mActivity = (Activity)context;
        mListener = listener;
        mView = view;
    }    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mView != null) {
            setView(mView);
        }
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void setCancelable(boolean flag) {
        if (flag) {
            setOnCancelListener(mCancelListener);
        }
        super.setCancelable(flag);
    }
    
    public void setPositiveButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_POSITIVE, text, mListener);
    }

    public void setNeutralButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_NEUTRAL, text, mListener);
    }

    public Button getPositiveButton() {
        return getButton(DialogInterface.BUTTON_POSITIVE);
    }

    public Button getNeutralButton() {
        return getButton(DialogInterface.BUTTON_NEUTRAL);
    }
}
