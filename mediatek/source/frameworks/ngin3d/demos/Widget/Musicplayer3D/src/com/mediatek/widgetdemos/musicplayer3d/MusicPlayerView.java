
package com.mediatek.widgetdemos.musicplayer3d;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.IMTKWidget;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews.RemoteView;

import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Text;
import com.mediatek.ngin3d.Transaction;
import com.mediatek.ngin3d.android.StageTextureView;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.Animation.Listener;
import com.mediatek.ngin3d.animation.BasicAnimation;

@RemoteView
public class MusicPlayerView extends RelativeLayout implements IMTKWidget {
    private static final String TAG = "MusicPlayer3d";

    public class Materials {
        public static final int MUSIC_PLAYER = 26;
    };

    // private Container mContainer; // for scene container
    // private Container mScreen; // hold screen centre position
    // private Container mBillBoard;
    private StatePattern mState;

    private static final Typeface FONT_SANS_BOLD = Typeface.create(Typeface.SANS_SERIF,
            Typeface.BOLD);
    private static final Typeface FONT_SANS_NORMAL = Typeface.create(Typeface.SANS_SERIF,
            Typeface.NORMAL);
    private static final Color COLOR_ALBUM = new Color(0x3C, 0x3C, 0x3C);
    private static final float SIZE_TEXT_SINGER = 30;
    private static final float SIZE_TEXT_TRACK = 40;

    private static final int ANIMATION_NONE = 0;
    private static final int ANIMATION_FROM_TOP = 1;
    private static final int ANIMATION_FROM_BOTTOM = 2;

    private boolean mIsAnimationCompleted = true;
    private int mScheduledAnimation = ANIMATION_NONE;

    private Glo3D mSpinAlbumGeo;
    private Glo3D mSpinAlbumTop;
    private Glo3D mSpinAlbumBom;
    private Glo3D mMusicPlayer;

    private BasicAnimation mSpinTopAnimation;
    private BasicAnimation mSpinBomAnimation;

    private Resources mRes = getResources();

    private Text mSingerText;
    private Text mTrackText;

    private Random mMyRandom;

    // Indicates whether the widget is frozen while the home screen is
    // transitioning
    private boolean mFrozen = false;

    // We use a standard FOV as a baseline for calculating the camera
    private static final float CONFIGURED_CAMERA_FOV = 50f;

    // Default camera configuration. Do not change here for individual widget
    // configuration. Change using the set*() functions provided.
    private float mCameraAngleX = 20f;
    private float mCameraAngleY = 150f;
    private float mCameraDistance = 150f;
    private Point mCameraLookAt = new Point(0, 15, 0);
    private float mCameraFov = 50f;
    private float mCameraNearClipDistance = 1f;
    private float mCameraFarClipDistance = 300f;
    private float mScreenDistance = mCameraFarClipDistance - 1f;

    private float mScreenWidth;
    private float mScreenHeight;

    private MyStageTextureView mStageView;
    private ImageView mImageView;
    private RelativeLayout mGestureLayout;

    // Containers for framework setup
    private Container mSceneContainer;
    private Container mCameraContainer;
    private Container mCentralScreenContainer;

    public MusicPlayerView(Context context) {
        super(context);
        initialise();
        createStage();
    }

    public MusicPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
        createStage();
    }

    private void initialise() {
        RelativeLayout.LayoutParams gestureParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        gestureParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        gestureParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        mGestureLayout = new RelativeLayout(getContext());
        addView(mGestureLayout, gestureParams);

        // Programmatically create child StageView and GestureView
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        mStageView = new MyStageTextureView(getContext());
        addView(mStageView, params);

        // Create snapshop ImageView
        mImageView = new ImageView(getContext());
        addView(mImageView, params);

        // Retrieve the root ngine3d stage object
        Stage stage = mStageView.getStage();

        // Set transparent background
        stage.setBackgroundColor(new Color(0, 0, 0, 0));

        // Create framework container hierarchy
        mSceneContainer = new Container();
        mCameraContainer = new Container();
        mCentralScreenContainer = new Container();

        stage.add(mSceneContainer);
        stage.add(mCameraContainer);
        mCameraContainer.add(mCentralScreenContainer);
    }

    private void createStage() {
        mSpinAlbumGeo = Glo3D.createFromAsset("music_player_geo.glo");
        mSpinAlbumBom = Glo3D.createFromAsset("music_player_anim_from_bottom.glo");
        mSpinAlbumTop = Glo3D.createFromAsset("music_player_anim_from_top.glo");
        mMusicPlayer = mSpinAlbumGeo;

        mSpinAlbumGeo.setMaterialType("album25", Materials.MUSIC_PLAYER);
        mSpinAlbumGeo.setMaterialType("album033", Materials.MUSIC_PLAYER);

        mSpinAlbumGeo.setMaterialTexture("album25", "monybrother.jpg");
        mSpinAlbumGeo.setMaterialTexture("album026", "coldplay.jpg");
        mSpinAlbumGeo.setMaterialTexture("album027", "daft_punk.jpg");
        mSpinAlbumGeo.setMaterialTexture("album028", "deportees.jpg");
        mSpinAlbumGeo.setMaterialTexture("album029", "james_blake.jpg");
        mSpinAlbumGeo.setMaterialTexture("album030", "radio_head.jpg");
        mSpinAlbumGeo.setMaterialTexture("album031", "britney_spears.jpg");
        mSpinAlbumGeo.setMaterialTexture("album032", "robyn.jpg");
        mSpinAlbumGeo.setMaterialTexture("album033", "pink_floyd.jpg");

        // Scale media player
        float scale = 2.8f;

        float albumContentPosition = mRes.getInteger(R.integer.album_content_position);

        int yMusicPlayer = mRes.getInteger(R.integer.y_3D_model);

        mMusicPlayer.setPosition(new Point(0, yMusicPlayer, 0));
        mMusicPlayer.setScale(new Scale(scale, scale, scale));
        mMusicPlayer.setRotation(new Rotation(5, -15, 0));

        mSceneContainer.add(mSpinAlbumGeo);
        mSceneContainer.add(mSpinAlbumTop);
        mSceneContainer.add(mSpinAlbumBom);

        mSpinTopAnimation = (BasicAnimation) mSpinAlbumTop.getAnimation();
        mSpinBomAnimation = (BasicAnimation) mSpinAlbumBom.getAnimation();

        // increase speed to 150%
        mSpinTopAnimation.setTimeScale(1.5f);
        mSpinBomAnimation.setTimeScale(1.5f);

        mState = new StatePattern();

        mSingerText = new Text();
        mSingerText.setText("Moneybrother");
        mSingerText.setTypeface(FONT_SANS_NORMAL);
        mSingerText.setTextColor(COLOR_ALBUM);
        mSingerText.setTextSize(SIZE_TEXT_SINGER);
        mCentralScreenContainer.add(mSingerText);
        mSingerText.setPosition(new Point(0, albumContentPosition, -0.08f));

        mTrackText = new Text();
        mTrackText.setText("We Die Only Once");
        mTrackText.setTypeface(FONT_SANS_BOLD);
        mTrackText.setTextColor(COLOR_ALBUM);
        mTrackText.setTextSize(SIZE_TEXT_TRACK);
        mCentralScreenContainer.add(mTrackText);
        mTrackText.setPosition(new Point(0, albumContentPosition + 40, -0.08f));

        mSpinTopAnimation.addListener(new Listener() {
            public void onCompleted(Animation animation) {
                mState.updateState();
                setAnimationCompleted(true);
                processScheduledAnimation();
            }

            public void onStarted(Animation animation) {
                setAnimationCompleted(false);
            }
        });

        mSpinBomAnimation.addListener(new Listener() {
            public void onCompleted(Animation animation) {
                mState.updateState();
                setAnimationCompleted(true);
                processScheduledAnimation();
            }

            public void onStarted(Animation animation) {
                setAnimationCompleted(false);
            }

        });

        mMyRandom = new Random();

        GestureView gestureView = addGestureArea(0, 0, 300, 450);
        gestureView.setOnGestureListener(new SimpleOnGestureListener() {
            private boolean mDisallowIsRequested;

            @Override
            public boolean onDown(MotionEvent e) {
                mDisallowIsRequested = false;
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                    float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    return false;
                }

                // Ignore swipe if last animation does not complete
                if (isAnimationCompleted()) {
                    if (velocityY > 0) {
                        startAnimationFromTop();
                    } else {
                        startAnimationFromBottom();
                    }
                } else {
                    scheduleAnimation(velocityY > 0 ? ANIMATION_FROM_TOP : ANIMATION_FROM_BOTTOM);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
                if (mDisallowIsRequested == false) {
                    float x = Math.abs(distanceX);
                    float y = Math.abs(distanceY);
                    if (y > x) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        mDisallowIsRequested = true;
                    }
                }
                return true;
            }
        });

        placePlayerControlButton();

    }

    // Adds a GestureView covering a certain area of the screen
    public GestureView addGestureArea(int x, int y, int width, int height) {
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        GestureView gestureView = new GestureView(getContext());
        mGestureLayout.addView(gestureView, params);
        gestureView.setPosition(x, y);

        return gestureView;
    }

    private void placePlayerControlButton() {

        int bottomLineY = mRes.getInteger(R.integer.y_bottom_line);

        final Image shuffleButton = Image.createFromResource(getResources(),
                R.drawable.shuffle_button);
        mCentralScreenContainer.add(shuffleButton);
        shuffleButton.setPosition(new Point(-177, bottomLineY));

        final Image rewButton = Image.createFromResource(getResources(), R.drawable.rew_button);
        mCentralScreenContainer.add(rewButton);
        rewButton.setPosition(new Point(-99, bottomLineY));

        final Image playButton = Image.createFromResource(getResources(), R.drawable.play_button);
        mCentralScreenContainer.add(playButton);
        playButton.setPosition(new Point(0, bottomLineY));

        final Image ffButton = Image.createFromResource(getResources(), R.drawable.ff_button);
        mCentralScreenContainer.add(ffButton);
        ffButton.setPosition(new Point(99, bottomLineY, -0.1f));

        final Image loopButton = Image.createFromResource(getResources(), R.drawable.loop_button);
        mCentralScreenContainer.add(loopButton);
        loopButton.setPosition(new Point(177, bottomLineY, -0.1f));

        final Image playListButton = Image.createFromResource(getResources(),
                R.drawable.go_to_playlist_button);
        mCentralScreenContainer.add(playListButton);
        playListButton
                .setPosition(new Point(-180, 0 - mRes.getInteger(R.integer.playlist_position)));
        playListButton.setScale(new Scale(1.4f, 1.4f, 1.4f));

        final Image background = Image.createFromResource(getResources(),
                R.drawable.music_player_background);
        mCentralScreenContainer.add(background);
        background.setPosition(new Point(0, mRes.getInteger(R.integer.y_background), 0.08f));

    }

    public void setAnimationCompleted(boolean isAnimationCompleted) {
        mIsAnimationCompleted = isAnimationCompleted;
    }

    public boolean isAnimationCompleted() {
        return mIsAnimationCompleted;
    }

    private void startAnimationFromBottom() {
        mState.setSwipeState(true);

        Transaction.beginPropertiesModification();

        mSpinBomAnimation.stop();
        mSpinBomAnimation.start();

        // chop off the bouncing album animation after frame 42. (1667 * 42/50 =
        // 1400)
        mSpinBomAnimation.setDuration(1400);

        String billBoard[] = mState.getContext(mState.getCurrent());
        updateText(billBoard);

        // Log.v(TAG, "(onFling) Swipe UP");
        // for BomAnimation, i.e. swipe up, album25 in front, start from
        // album033
        mSpinAlbumGeo.setMaterialTexture("album25", billBoard[2]);
        mSpinAlbumGeo.setMaterialTexture("album033",
                mState.getContext(mState.getPrevious())[2]);

        mSpinAlbumGeo.setMaterialTexture("album026",
                mState.getContext(mMyRandom.nextInt(mState.getContextLength() - 1))[2]);
        Transaction.commit();
    }

    private void startAnimationFromTop() {
        mState.setSwipeState(false);

        Transaction.beginPropertiesModification();

        mSpinTopAnimation.stop();
        mSpinTopAnimation.start();

        // chop off the bouncing album animation after frame 42. (1667 * 42/50 =
        // 1400)
        mSpinTopAnimation.setDuration(1400);

        String billBoard[] = mState.getContext(mState.getCurrent());
        updateText(billBoard);

        // Log.v(TAG, "(onFling) Swipe DOWN");
        // for TopAnimation, i.e. swipe down, album033 in front, start from
        // album25
        mSpinAlbumGeo.setMaterialTexture("album033", billBoard[2]);
        mSpinAlbumGeo.setMaterialTexture("album25",
                mState.getContext(mState.getPrevious())[2]);

        mSpinAlbumGeo.setMaterialTexture("album026",
                mState.getContext(mMyRandom.nextInt(mState.getContextLength() - 1))[2]);
        Transaction.commit();
    }

    void updateText(String[] billBoard) {
        mSingerText.setText(billBoard[0]);
        mTrackText.setText(billBoard[1]);
    }

    // Adjusts the camera configuration to match a new screen size
    public void setScreenSize(int screenWidth, int screenHeight) {
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        updateCamera();
    }

    // Schedule and animation and set the expiry task running
    private void scheduleAnimation(int animation) {
        mScheduledAnimation = animation;
        mScheduledAnimationTimerTask.startDelayed();
    }

    // Plays an animation if it is scheduled
    private void processScheduledAnimation() {
        mScheduledAnimationTimerTask.stop();

        switch (mScheduledAnimation) {
            case ANIMATION_FROM_BOTTOM:
                startAnimationFromBottom();
                break;

            case ANIMATION_FROM_TOP:
                startAnimationFromTop();
                break;
        }

        mScheduledAnimation = ANIMATION_NONE;
    }

    // Updates the camera using the current camera configuration
    // The camera position is defined by a point at which it is looking, its
    // distance away from that point, and its angle of rotation around the X and
    // Y axes.
    //
    // Because ngine3d currently only supports a single mode of projection
    // at a time (orthographic or perspective), we have to handle screen-space
    // rendering by attaching text and billboards to the camera.
    private void updateCamera() {
        Stage stage = mStageView.getStage();

        // Math library uses radians
        float angleX = deg2rad(mCameraAngleX);
        float angleY = deg2rad(mCameraAngleY);
        float fov = deg2rad(mCameraFov);

        // The distance from the camera is modulated by the FOV, so that the FOV
        // can be adjusted without affecting the perceived distance of the
        // camera from the scene. Thus, the camera distance is only the value
        // it is set as if the FOV equals the configured FOV.
        float distance = mCameraDistance *
                (float) (Math.tan(deg2rad(CONFIGURED_CAMERA_FOV * 0.5f)) /
                Math.tan(fov * 0.5f));

        Point cameraLookAt = new Point(mCameraLookAt);

        // Calculate the camera position by moving it way from the "look at"
        // point by the calculated distance, at the specified X and Y angles
        Point cameraPosition = new Point(
                cameraLookAt.x + (float) (Math.sin(angleY) * Math.cos(angleX)) * distance,
                cameraLookAt.y + (float) Math.sin(angleX) * distance,
                cameraLookAt.z + (float) (Math.cos(angleY) * Math.cos(angleX)) * distance);

        // We add 180 degrees because the camera is pointing in the opposite
        // direction from the billboards, which are what are actually attached
        // to the "camera container" (the camera cannot reside in a container
        // as it is separate from the scene graph for some reason)
        Rotation cameraRotation = new Rotation(
                mCameraAngleX, mCameraAngleY + 180f, 0);

        // Now point the camera and rotate the "camera container" at the same
        // time
        Transaction.beginPropertiesModification();

        stage.setCamera(cameraPosition, cameraLookAt);
        mCameraContainer.setPosition(cameraPosition);
        mCameraContainer.setRotation(cameraRotation);

        Transaction.commit();

        // stage.setCamera(new Point(0, 200, 0), new Point(0, 0, 0));

        // Here we do some funky trigonometry to determine by how much to scale
        // the "billboard/screen" container to ensure objects within it render
        // in screen-space no matter the camera setup
        float screenScale = 2 * mScreenDistance / mScreenHeight *
                (float) Math.tan(fov * 0.5f);

        // Now position and scale the screen containers. The top-left conatiner
        // positions the origin at the top-left corner of the screen, whereas
        // the central container positions it at the centre of the screen.
        // We must invert the y-axis because for some reason, text renders
        // upside down in ngin3d
        mCentralScreenContainer.setPosition(new Point(0, 0, mScreenDistance));
        mCentralScreenContainer.setScale(new Scale(screenScale, -screenScale, screenScale));

        // Standard perspective projection
        float aspectRatio = (float) mScreenWidth / mScreenHeight;
        aspectRatio = (aspectRatio > 0) ? aspectRatio : 1;
        stage.setPerspectiveProjection(fov, aspectRatio, mCameraNearClipDistance,
                mCameraFarClipDistance);
    }

    // Causes a scheduled animation to expire after a period of time
    private PeriodicTask mScheduledAnimationTimerTask = new PeriodicTask(700) {
        public void run(int runCount, int timeElapsed) {
            mScheduledAnimation = ANIMATION_NONE;
        }
    };

    private class MyStageTextureView extends StageTextureView {
        public MyStageTextureView(Context context) {
            super(context);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            super.onSurfaceChanged(gl, width, height);
            mScreenWidth = width;
            mScreenHeight = height;
            updateCamera();
        }
    }

    public static float deg2rad(float degrees) {
        float radians = degrees / 180f * 3.14159f;
        return radians;
    }

    // Captures a snapshot of the StageView, displays it on an ImageView, and
    // pauses the StageView
    protected boolean freeze(int currentScreen) {
        if (!mFrozen) {
            // StageView is not hidden just yet, otherwise the widget will
            // flicker when freezing. Instead the hiding is deferred to the
            // onPostDraw() callback to ensure the ImageView is visible before
            // hiding the StageView
            mStageView.pauseRendering();
            Bitmap bitmap = mStageView.getScreenShot();

            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);

            mFrozen = true;
        }

        return true;
    }

    // Unpauses and shows the StageView, and hides the ImageView
    protected void unfreeze(int currentScreen) {
        if (mFrozen) {
            mStageView.resumeRendering();
            mStageView.setVisibility(View.VISIBLE);

            mImageView.setVisibility(View.INVISIBLE);

            mFrozen = false;
        }
    }

    // --------------------------------------------------------------------------
    // IMTKWidget methods
    // --------------------------------------------------------------------------

    // IMTKWidget parameters
    private static final int SCREEN_UNKNOWN = -10000;
    private int mAppWidgetScreen = SCREEN_UNKNOWN;
    private int mAppWidgetId = -1;

    @Override
    public int getPermittedCount() {
        return 1;
    }

    @Override
    public int getScreen() {
        return mAppWidgetScreen;
    }

    @Override
    public int getWidgetId() {
        return mAppWidgetId;
    }

    // Called when the widget moves into a new screen
    @Override
    public void moveIn(int currentScreen) {
        // unfreeze(currentScreen);
    }

    // Called when the widget moves out of the screen
    // The return value determines whether the screen transition starts
    @Override
    public boolean moveOut(int currentScreen) {
        // freeze(currentScreen);
        return true;
    }

    // Called when the widget needs to be paused
    @Override
    public void onPauseWhenShown(int currentScreen) {
        // freeze(currentScreen);
    }

    // Called when the widget needs to be unpaused
    @Override
    public void onResumeWhenShown(int currentScreen) {
        // unfreeze(currentScreen);
    }

    @Override
    public void setScreen(int screen) {
        mAppWidgetScreen = screen;
    }

    @Override
    public void setWidgetId(int widgetId) {
        mAppWidgetId = widgetId;
    }

    // Called when the widget is about to be covered by something else
    @Override
    public void startCovered(int currentScreen) {
        // freeze(currentScreen);
    }

    // Called when the widget is about to be shown again
    @Override
    public void stopCovered(int currentScreen) {
        // unfreeze(currentScreen);
    }

    // Called when a drag operation on the widget starts
    @Override
    public void startDrag() {
        freeze(mAppWidgetScreen);
    }

    // Called when a drag operation of the widget is ending
    @Override
    public void stopDrag() {
        unfreeze(mAppWidgetScreen);
    }

    // Called after launcher's onSaveInstanceState is called.
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    // Called after launcher's onRestoreInstanceState is called.
    @Override
    public void onRestoreInstanceState(Bundle state) {
    }

    public void leaveAppwidgetScreen() {
    }

    public void enterAppwidgetScreen() {
    }

}
