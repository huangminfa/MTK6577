/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.gallery3d.gadget;

import com.android.gallery3d.R;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.MpoHelper;
import com.android.gallery3d.util.MtkLog;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class WidgetClickHandler extends Activity {
    private static final String TAG = "PhotoAppWidgetClickHandler";

    private boolean isValidDataUri(Uri dataUri) {
        if (dataUri == null) return false;
        try {
            AssetFileDescriptor f = getContentResolver()
                    .openAssetFileDescriptor(dataUri, "r");
            f.close();
            return true;
        } catch (Throwable e) {
            Log.w(TAG, "cannot open uri: " + dataUri, e);
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Intent intent = getIntent();
        Uri dataUri = intent.getData();
        MtkLog.d(TAG, "WidgetClickHandler.onCreate: uri=" + dataUri);
        if (isValidDataUri(dataUri)) {
            String action = Intent.ACTION_VIEW;
            //as stereo mpo is added to data set, we cancel the following check
            //and send default intent, waiting Gallery to judge. 2012-04-15
            //if (MediatekFeature.isMpoSupported()) {
            //    String mimeType = intent.resolveType(this);
            //    if (MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            //        action = MpoHelper.MPO_VIEW_ACTION;
            //    }
            //}
            startActivity(new Intent(action, dataUri));
        } else {
            Toast.makeText(this,
                    R.string.no_such_item, Toast.LENGTH_LONG).show();
            MtkLog.e(TAG, "uri not valid, goto main page instead!");
            startActivity(new Intent(this, Gallery.class));
        }
        finish();
    }
}
