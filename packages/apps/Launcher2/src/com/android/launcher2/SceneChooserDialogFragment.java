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
package com.android.launcher2;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.android.launcher.R;

import java.io.IOException;
import java.util.ArrayList;

public class SceneChooserDialogFragment extends DialogFragment implements
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener{

    private static final String TAG = "SceneChooserDialogFragment";
    private static final String EMBEDDED_KEY = "com.android.launcher2."
            + "SceneChooserDialogFragment.EMBEDDED_KEY";

    private boolean mEmbedded;
    private static final boolean DBG = true;
    public static final String SELECTED_SCENE = "selectedScene";
    
    /**
     * The position selected by user.
     */
    private int mSelectedPosition;
    
    /**
     * The position of current used Scene.
     */
    private int mCurrentPosition = 0;
    
    /**
     * The number of Scene in the system.
     */
    private int mSceneCount = 0;
    
    private Dialog mProgressDialog;
    
    private String[] sceneName;
    
    private TextView mSceneName;
        
    /**
     * ArrayList to store all of the Scene datas and bitmap images.
     */
    private ArrayList<SceneData> mSceneDatas = new ArrayList<SceneData>();    
    
    private Object mLock = new Object();

    public static SceneChooserDialogFragment newInstance() {
        SceneChooserDialogFragment fragment = new SceneChooserDialogFragment();
        fragment.setCancelable(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(EMBEDDED_KEY)) {
            mEmbedded = savedInstanceState.getBoolean(EMBEDDED_KEY);
        } else {
            mEmbedded = isInLayout();
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onCreate: savedInstanceState = " + savedInstanceState
                    + ",mEmbedded = " + mEmbedded + ",this = " + this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EMBEDDED_KEY, mEmbedded);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onSaveInstanceState: outState = " + outState + ",mEmbedded = " + mEmbedded);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDestroy: this = " + this);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        /* On orientation changes, the dialog is effectively "dismissed" so this is called
         * when the activity is no longer associated with this dying dialog fragment. We
         * should just safely ignore this case by checking if getActivity() returns null
         */
        Activity activity = getActivity();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDismiss: activity = " + activity + ",dialog = " + dialog);
        }
        if (activity != null) {
            activity.finish();
        }
    }

    /* This will only be called when in XLarge mode, since this Fragment is invoked like
     * a dialog in that mode
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onCreateDialog: savedInstanceState = " + savedInstanceState);
        }
        findScenes();
        
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onCreateView: mEmbedded = " + mEmbedded + ",container = "
                    + container);
        }
        
        sceneName = getResources().getStringArray(R.array.scene_name);
        
        findScenes();

        /* If this fragment is embedded in the layout of this activity, then we should
         * generate a view to display. Otherwise, a dialog will be created in
         * onCreateDialog()
         */
        if (mEmbedded) {
        	String scene = Launcher.getCurrentScene();
            for(int i=0; i<mSceneDatas.size(); i++) {
            	if (scene.equals(mSceneDatas.get(i).SceneName)) {
            		mCurrentPosition = i;
            		break;
            	}
            }
            mSelectedPosition = mCurrentPosition;
            
        	View view = inflater.inflate(R.layout.scene_chooser, container, false);
        	
        	mSceneName = (TextView)view.findViewById(R.id.scene_name);

            final Gallery gallery = (Gallery) view.findViewById(R.id.gallery);
            gallery.setAdapter(new ImageAdapter(getActivity()));
            gallery.setCallbackDuringFling(false);
            gallery.setSelection(mCurrentPosition);
            gallery.setOnItemClickListener(this);
            gallery.setOnItemSelectedListener(this);
            return view;
        }
        return null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Override this method for log tracking.
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onAttach: activity = " + activity + ",this = " + this);
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        // Override this method for log tracking.
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDetach: activity = " + this.getActivity() + ",this = " + this);
        }
    }
    
    private void findScenes() {
    	//mBookmarkItems = new ArrayList<BookmarkItem>(2);
    	mSceneDatas = new ArrayList<SceneData>(2);

        final Resources resources = getResources();
        // Context.getPackageName() may return the "original" package name,
        // com.android.launcher2; Resources needs the real package name,
        // com.android.launcher. So we ask Resources for what it thinks the
        // package name should be.
        final String packageName = resources.getResourcePackageName(R.array.scene_preview);

        addScenes(resources, packageName, R.array.scene_preview);
    }

    private void addScenes(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        int i = 0;
        
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
            	Drawable d = resources.getDrawable(res);
            	Bitmap previewImage = ((BitmapDrawable)d).getBitmap();
            	SceneData data = new SceneData();
            	data.SceneName = getSceneName(extra);
            	data.ScenePreviewImage = previewImage;
            	mSceneDatas.add(data);            	
            }
        }
    }
    
    static String getSceneName(String extra) {
    	//"scene_work_preview" name[1] = work, "work" is the scene name
        String name[] = extra.split("_");
        return name[1];
    }
    
    private String getSelectedSceneName() {
    	return mSceneDatas.get(mSelectedPosition).SceneName;
    }
    
    private void switchSceneAndBack() {
    	Bundle bundle = new Bundle();
        bundle.putString(SELECTED_SCENE, getSelectedSceneName());
        Intent intent = new Intent(LauncherModel.SWITCH_SCENE_ACTION);
        intent.putExtras(bundle);
        Activity activity = getActivity();
        activity.sendBroadcast(intent);
        activity.finish();
    }    

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "SceneChooserDialogFragment.onItemClick position = " + position);
		mSelectedPosition = position;
		switchSceneAndBack();
	}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected()" + " mSelectedPosition = " + position);
        mSelectedPosition = position;
        mSceneName.setText(sceneName[mSelectedPosition]);
    }    
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Extra saved information for displaying the Scene in the Scene.
     */
    private static class SceneData {
        /**
         * The name of Scene.
         */
        String SceneName;

        /**
         * The preview image of Scene.
         */
        Bitmap ScenePreviewImage;
    }
    
    private class ImageAdapter extends BaseAdapter implements ListAdapter, SpinnerAdapter {
        private LayoutInflater mLayoutInflater;

        ImageAdapter(Activity activity) {
            mLayoutInflater = activity.getLayoutInflater();
        }

        public int getCount() {
            return mSceneDatas.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.scene_item, parent, false);
            } else {
                view = convertView;
            }

            ImageView image = (ImageView) view.findViewById(R.id.scene_image);

            image.setImageBitmap(mSceneDatas.get(position).ScenePreviewImage);
            Drawable d = image.getDrawable();
            if (d != null) {
                d.setDither(true);
            } else {
                
            }

            return view;
        }
    }
}
