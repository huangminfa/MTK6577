package com.mediatek.ngin3d.android;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.animation.AnimationLoader;

/**
 * Represents an activity that shows a Stage as its content.
 */
public class StageActivity extends Activity {

    private static final String TAG = "StageActivity";

    /**
     * The stage of this activity.
     */
    protected Stage mStage;

    /**
     * A new stage view object for this activity.
     */
    protected StageView mStageView;

    /**
     * Gets the stage object of this object.
     *
     * @return the stage object.
     */
    public Stage getStage() {
        return mStage;
    }

    /**
     * Gets the stage view object of this object.
     *
     * @return the stage view object.
     */
    public StageView getStageView() {
        return mStageView;
    }

    /**
     * Create an android activity view.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");

        mStageView = new StageView(this);
        mStage = mStageView.getStage();
        setContentView(mStageView);
        AnimationLoader.setCacheDir(getCacheDir());
    }

    /**
     * When pause this activity.
     */
    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        mStageView.onPause();
        super.onPause();
    }

    /**
     * When resume this activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        mStageView.onResume();
    }

    /**
     * When the setting of this activity changed.
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mStageView.requestRender();
        Log.v(TAG, "onConfigurationChanged");
    }

}
