package com.mediatek.media3d;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.OrientationEventListener;
import android.widget.FrameLayout;
import com.mediatek.media3d.photo.PhotoPage;
import com.mediatek.media3d.portal.PortalPage;
import com.mediatek.media3d.video.VideoPage;
import com.mediatek.media3d.weather.WeatherPage;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.animation.AnimationLoader;

import java.util.ArrayList;

public class Main extends Activity implements PortalPage.PageQueryCallback{
    private static final String TAG = "Media3D.Main";
    public static final boolean ON_DRAG_MODE = true;

    private Stage mStage;

    private Media3DView mMedia3DView;
    private PortalPage mPortalPage;
    private WeatherPage mWeatherPage;
    private PhotoPage mPhotoPage;
    private VideoPage mVideoPage;
    private Page mCurrentPage;
    private GestureDetector mGestureDetector;
    private boolean mIsAutoRotateLaunched;
    private final ArrayList<MediaSourceListener> mMediaSourceListeners = new ArrayList<MediaSourceListener>();

    public Media3DView getMedia3DView() {
        return mMedia3DView;
    }

    public Main() {
        Media3D.setDemoMode(false);
    }

    public PhotoPage getPhotoPage() {
        return mPhotoPage;
    }

    public WeatherPage getWeatherPage() {
        return mWeatherPage;
    }

    public VideoPage getVideoPage() {
        return mVideoPage;
    }

    public PortalPage getPortalPage() {
        return mPortalPage;
    }

    private void loadPage(Page page, Bundle savedInstanceState) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "Load page " + page);
        }

        page.onAttach(this);
        page.onCreate(savedInstanceState);
        mMedia3DView.addPage(page);
        mMedia3DView.loadPage(page);
    }

    private void loadPageAsync(Bundle savedInstanceState, Page... pages) {
        final int count = pages.length;
        for (int i = 0; i < count; i++) {
            pages[i].onAttach(this);
            pages[i].onCreate(savedInstanceState);
            mMedia3DView.addPage(pages[i]);
        }
        mMedia3DView.loadPageAsync(pages);
    }

    private void unloadPage(Page page) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "Unload page " + page);
        }

        mMedia3DView.removePage(page);
        page.onDestroy();
        page.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Media3D.DEBUG) {
            Log.v(TAG, "onCreate()");
        }
        setContentView(R.layout.media3d);

        AnimationLoader.setCacheDir(getCacheDir());

        SimpleVideoView backgroundVideo = (SimpleVideoView) findViewById(R.id.bg_video);

        mGestureDetector = new GestureDetector(this, new MyGestureListener());

        mStage = new Stage();
        mStage.setBackgroundColor(new com.mediatek.ngin3d.Color(0x00, 0x00, 0x00, 0x00));
        mStage.addTextureAtlas(getResources(), R.raw.media3d_atlas, R.raw.media3d);
        mStage.setMaxFPS(60);

        // Media3D was designed with a system that used left-handed coordinates.
        // The graphics engine now uses a conventional right-handed system so we
        // use a 'special' projection to compensate for this.
        mStage.setProjection(Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f);
 
        mMedia3DView = new Media3DView(this, mStage, backgroundVideo);

        mPortalPage = new PortalPage(mStage);
        loadPage(mPortalPage, savedInstanceState);
        mPortalPage.setPageQueryCallback(this);

        mWeatherPage = new WeatherPage(mStage);
        mPhotoPage = new PhotoPage(mStage);
        mVideoPage = new VideoPage(mStage);

        Setting setting = Setting.realize(this, R.xml.configuration);
        WeatherPage.loadConfiguration(setting);
        PhotoPage.loadConfiguration(setting);
        VideoPage.loadConfiguration(setting);

        loadPageAsync(savedInstanceState, mWeatherPage, mPhotoPage, mVideoPage);
        
        backgroundVideo.setZOrderMediaOverlay(false);

        mMedia3DView.setZOrderMediaOverlay(true);
        FrameLayout frame = (FrameLayout) findViewById(R.id.stage_root);
        frame.addView(mMedia3DView);

        mOrientationListener =
            new OrientationListener(this, SensorManager.SENSOR_DELAY_NORMAL);

        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }

        Intent intent = getIntent();
        if (intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase("android.intent.action.ROTATED_MAIN")) {
                mIsAutoRotateLaunched = true;
            }
        }

        if (mCurrentPage == null) {
            mCurrentPage = mPortalPage;
        }
        mMedia3DView.enterPage(mCurrentPage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mPortalPage.onSaveInstanceState(outState);
        mWeatherPage.onSaveInstanceState(outState);
        mPhotoPage.onSaveInstanceState(outState);
        mVideoPage.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (Media3D.DEBUG) {
            Log.d(TAG, "onDestroy");
        }
        unloadPage(mPortalPage);
        mPortalPage = null;

        unloadPage(mWeatherPage);
        mWeatherPage = null;

        unloadPage(mPhotoPage);
        mPhotoPage = null;

        unloadPage(mVideoPage);
        mVideoPage = null;

        mOrientationListener.disable();
        mOrientationListener = null;

        // Remove references of all actors, especially for Text bitmap
        mStage.removeAll();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Media3D.DEBUG) {
            Log.d(TAG, "onResume");
        }
        startMonitorExternalStorage();
        mMedia3DView.onResume();
    }

    @Override
    protected void onPause() {
        if (Media3D.DEBUG) {
            Log.d(TAG, "onPause");
        }
        mMedia3DView.onPause();
        stopMonitorExternalStorage();
        super.onPause();
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private int getFlingDirection(float velocityX, float velocityY) {
            int direction = FlingEvent.NONE;

            if (Math.abs(velocityX) >= Math.abs(velocityY)) {
                if (velocityX < -FlingEvent.THRESHOLD) {
                    direction = FlingEvent.LEFT;
                } else if (velocityX > FlingEvent.THRESHOLD) {
                    direction = FlingEvent.RIGHT;
                }
            } else {
                if (velocityY > FlingEvent.THRESHOLD) {
                    direction = FlingEvent.DOWN;
                } else if (velocityY < -FlingEvent.THRESHOLD) {
                    direction = FlingEvent.UP;
                }
            }

            if (Media3D.DEBUG) {
                Log.v(TAG, "getFling(): " + direction);
            }
            return direction;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (Media3D.DEBUG) {
                Log.v(TAG, "onSingleTapConfirmed()");
            }
            return mMedia3DView.onSingleTapConfirmed(event);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (ON_DRAG_MODE) {
                return false;
            } else {
                int direction = getFlingDirection(velocityX, velocityY);
                mMedia3DView.onFling(direction);
                return direction != FlingEvent.NONE;
            }
        }

        // TODO: Try to use onTouchEvent instead of onScroll
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (ON_DRAG_MODE) {
                return mMedia3DView.onScroll(distanceX, distanceY);
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        boolean handled = super.dispatchTouchEvent(m);
        if (!handled) {
            handled = mGestureDetector.onTouchEvent(m);
        }
        return handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent m) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onTouchEvent, action = " + m.getAction() + ", (" + m.getX() + ", " + m.getY() + ")");
        }

        mMedia3DView.onTouchEvent(m);
        return false;
    }

    /*
    private static final int CMD_DUMP_ACTOR = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, CMD_DUMP_ACTOR, 0, R.string.dump_actor).setIcon(R.drawable.simple);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CMD_DUMP_ACTOR:
            mStage.dump();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onKeyUp - " + keyCode);
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Page backPage = mMedia3DView.getBackPage();
            if (backPage == null) {
                if (mPortalPage.isShowLoading()) {
                    mPortalPage.cancelLoading();
                    Log.v(TAG, "cancel loading");
                } else {
                    onBackPressed();
                    Log.v(TAG, "back key");
                }
            } else {
                mMedia3DView.enterPage(backPage);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            return mMedia3DView.onBarShowHide();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mMedia3DView.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (Media3D.DEBUG) {
            Log.v(TAG, "onConfigurationChanged");
        }

        // handle configuration change to avoid drawing on lock screen
        // and the surface memory which makes gpu oom.
        if (mMedia3DView == null) {
            return;
        }
        mMedia3DView.onConfigurationChanged(config);
        mMedia3DView.setVisibility(View.VISIBLE);
    }

    public Page queryWeatherPage() {
        return getWeatherPage();
    }
    
    public Page queryPhotoPage() {
        return getPhotoPage();
    }
    
    public Page queryVideoPage() {
        return getVideoPage();
    }

    private OrientationListener mOrientationListener;
    private final class OrientationListener extends OrientationEventListener {
        public OrientationListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int degree) {
            if (mIsAutoRotateLaunched && isPortraitScope(degree)) {
                finish();
            }
        }

        private boolean isBetween(int x, int lower, int upper) {
            return (x >= lower && x <= upper);
        }

        private boolean isPortraitScope(int degree) {
            return isBetween(degree, 0, 10) || isBetween(degree, 350, 360);
        }
    }

    BroadcastReceiver mBroadcastReceiver;
    private void startMonitorExternalStorage() {
        if (mBroadcastReceiver != null) {
            return;
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                onBroadcastReceive(intent);
            }
        };

        IntentFilter mediaFilter = new IntentFilter();
        mediaFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mediaFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        mediaFilter.addDataScheme("file");
        registerReceiver(mBroadcastReceiver, mediaFilter);
    }

    private void stopMonitorExternalStorage() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void onBroadcastReceive(Intent intent) {
        String action = intent.getAction();
        String path = intent.getDataString();
        LogUtil.v(TAG, "action :" + action + ", path :" + path);

        int event;
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            event = MediaSourceListener.MEDIA_MOUNTED_EVENT;
        } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
            event = MediaSourceListener.MEDIA_UNMOUNTED_EVENT;
        } else {
            return;
        }

        for (MediaSourceListener listener : mMediaSourceListeners) {
            listener.onChanged(event);
        }
    }

    public void addMediaSourceListener(MediaSourceListener listener) {
        if (!mMediaSourceListeners.contains(listener)) {
            mMediaSourceListeners.add(listener);
        }
    }

    public void removeMediaSourceListener(MediaSourceListener listener) {
        if (mMediaSourceListeners.contains(listener)) {
            mMediaSourceListeners.remove(listener);
        }
    }
}