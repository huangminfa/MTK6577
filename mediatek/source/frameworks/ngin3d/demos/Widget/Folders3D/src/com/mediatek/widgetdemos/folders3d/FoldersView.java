
package com.mediatek.widgetdemos.folders3d;

import java.util.Collections;
import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPostDrawListener;
import android.widget.IMTKWidget;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews.RemoteView;

import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Transaction;
import com.mediatek.ngin3d.android.StageTextureView;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.Animation.Listener;
import com.mediatek.ngin3d.animation.BasicAnimation;

/**
 * FoldersView sets up scene for Folders3d application. It adds application
 * specific actors into scene container and manages animation.
 */
@RemoteView
public class FoldersView extends RelativeLayout implements IMTKWidget, OnPostDrawListener {
    
    public class Materials {
        public static final int FOLDERS = 27;
        public static final int FOLDERS_FLAT = 33;
        public static final int FOLDERS_PHOTO = 36;
    };

    private static final int TOTAL_FOLDERS = 7;

    private BasicAnimation mAnimForward;
    private BasicAnimation mAnimBackward;
    private float mAnimationSpeed = 1f;

    private Glo3D mLight;
    private Glo3D mFolder;

    /**
     * Linked list containing contacts information. Useful in updating contacts
     * while forward/backward spinning.
     */
    private LinkedList<FoldersContacts> mContactList;

    private MyStageTextureView mStageView;
    private ImageView mImageView;
    private RelativeLayout mGestureLayout;

    private float mScreenWidth;
    private float mScreenHeight;

    // We use a standard FOV as a baseline for calculating the camera
    private static final float CONFIGURED_CAMERA_FOV = 37f;

    // Default camera configuration. Do not change here for individual widget
    // configuration. Change using the set*() functions provided.
    private float mCameraAngleX = 20f;
    private float mCameraAngleY = 150f;
    private float mCameraDistance = 150f;
    private Point mCameraLookAt = new Point(0, 15, 0);
    private float mCameraFov = 40f;
    private float mCameraNearClipDistance = 70f;
    private float mCameraFarClipDistance = 230f;
    private float mScreenDistance = mCameraFarClipDistance - 1f;

    // Containers for framework setup
    private Container mSceneContainer;
    private Container mCameraContainer;
    private Container mCentralScreenContainer;
    private Container mTopLeftScreenContainer;

    public FoldersView(Context context) {
        super(context);
        initialise();
        createScene();
    }

    public FoldersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
        createScene();
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
        mTopLeftScreenContainer = new Container();

        stage.add(mSceneContainer);
        stage.add(mCameraContainer);
        mCameraContainer.add(mCentralScreenContainer);
        mCentralScreenContainer.add(mTopLeftScreenContainer);
    }

    /**
     * Creates Scene. Adds 3D objects and animation into scene container. Also
     * defines for animation listeners.
     */
    private void createScene() {

        setCameraAngleX(18.4f);
        setCameraAngleY(157f);
        // getStage().setBackgroundColor(Color.MAGENTA);

        // Scene light
        mLight = Glo3D.createFromAsset("point_light.glo");
        mSceneContainer.add(mLight);
        mLight.setPosition(new Point(18.39f, 87f, 41f));

        mFolder = Glo3D.createFromAsset("folders_geometry.glo");
        final Glo3D forwardAnim = Glo3D
                .createFromAsset("folders_forward_anim.glo");
        final Glo3D backwardAnim = Glo3D
                .createFromAsset("folders_backward_anim.glo");

        final Scale s = new Scale(1.12f, 1.12f, 1.12f);
        mFolder.setScale(s);
        mFolder.setPosition(new Point(5f, 8.3f, 0f));
        mFolder.setMaterialType(Materials.FOLDERS);
        mSceneContainer.add(mFolder);
        mSceneContainer.add(forwardAnim);
        mSceneContainer.add(backwardAnim);

        mContactList = new LinkedList<FoldersContacts>();
        mContactList.add(new FoldersContacts("Hanna Wilson",
                "hanna_wilson.jpg", "hanna_wilson_name.png"));
        mContactList.add(new FoldersContacts("Michael Chang",
                "michael_chang.jpg", "michael_chang_name.png"));
        mContactList.add(new FoldersContacts("Jonathan Jack",
                "jonathan_jack.jpg", "jonathan_jack_name.png"));
        mContactList.add(new FoldersContacts("Patricia Taylor",
                "patricia_taylor.jpg", "patricia_taylor_name.png"));
        mContactList.add(new FoldersContacts("Jimmy Kid", "jimmy_kid.jpg",
                "jimmy_kid_name.png"));
        mContactList.add(new FoldersContacts("Edward Yeh", "edward_yeh.jpg",
                "edward_yeh_name.png"));
        mContactList.add(new FoldersContacts("David Livingston",
                "david_livingston.jpg", "david_livingston_name.png"));
        Collections.sort(mContactList, new FoldersContacts());

        mAnimForward = (BasicAnimation) forwardAnim.getAnimation();
        mAnimBackward = (BasicAnimation) backwardAnim.getAnimation();
        mAnimForward.setTimeScale(2.5f);
        mAnimBackward.setTimeScale(2.5f);
        // mAnimForward.setDuration(400); // Can be used to play only
        // 400ms of animation and stop

        mAnimForward.addListener(new Listener() {
            public void onStarted(Animation animation) {
                Log.v("forward anim listener", " on started");
            }

            public void onCompleted(Animation animation) {
                forwardUpdate();
                // textUpdate();
            }
        });

        mAnimBackward.addListener(new Listener() {
            public void onStarted(Animation animation) {
                backwardUpdate();
            }

            public void onCompleted(Animation animation) {
                // textUpdate();
            }
        });

        GestureView gestureView = addGestureArea(0, 0, 400, 400);
        gestureView.setOnGestureListener(new SimpleOnGestureListener() {
            private boolean mDisallowIsRequested;

            public boolean onFling(MotionEvent downEvent, MotionEvent upEvent,
                    float velocityX, float velocityY) {

                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    return false;
                }
                if (!mAnimForward.isStarted() && !mAnimBackward.isStarted()) {
                    if (velocityY > 0) {
                        applyPhotos(mContactList);
                        mAnimForward.stop();
                        mAnimForward.start();
                    } else if (velocityY < 0) {
                        applyPhotos(mContactList);
                        mAnimBackward.stop();
                        mAnimBackward.start();
                    }
                }
                return true;
            }

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

        applyPhotos(mContactList);
        assignShaders();
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

    /*
     * Update linked list when forward finger fling occurs
     */
    private void forwardUpdate() {
        mContactList.addLast(mContactList.pollFirst());
    }

    /*
     * Update linked list when backward finger fling occurs
     */
    private void backwardUpdate() {
        mContactList.addFirst(mContactList.pollLast());
    }

    /**
     * Apply photos to corresponding scene Node from updated linked list of
     * contacts
     * 
     * @param mContactList
     */
    private void applyPhotos(LinkedList<FoldersContacts> mContactList) {
        for (int i = 0; i < TOTAL_FOLDERS; i++) {
            mFolder.setMaterialTexture(contactsPhotoSceneNodeName[i],
                    mContactList.get(i).contactPhotoFileName);
            mFolder.setMaterialTexture(contactsNameSceneNodeName[i],
                    mContactList.get(i).contactNameTexture);
        }
    }

    /**
     * Assign different shaders for contacts names and photo
     */
    private void assignShaders() {
        for (int i = 0; i < TOTAL_FOLDERS; i++) {
            mFolder.setMaterialType(contactsNameSceneNodeName[i],
                    Materials.FOLDERS_FLAT);
            mFolder.setMaterialType(contactsPhotoSceneNodeName[i],
                    Materials.FOLDERS_PHOTO);
        }
    }

    /**
     * Scene node names for contact photos as in .glo file
     */
    private String contactsPhotoSceneNodeName[] = {
            "Photo1", "Photo2",
            "Photo3", "Photo4", "Photo5", "Photo6", "Photo7"
    };

    /**
     * Scene node names for contact name planes as in .glo file
     */
    private String contactsNameSceneNodeName[] = {
            "NamePlane1", "NamePlane2",
            "NamePlane3", "NamePlane4", "NamePlane5", "NamePlane6",
            "NamePlane7"
    };

    //
    // For tweaking functions and getters/setters
    //
    public void setAnimationSpeed(float speed) {
        speed = Math.max(0.01f, speed);
        mAnimationSpeed = speed;

        Log.v("Animation Speed Inside setAnimationSpeed ",
                Float.toString(speed));

        mAnimForward.setTimeScale(speed);
        mAnimBackward.setTimeScale(speed);
    }

    public Glo3D _getLight() {
        return mLight;
    }

    public float _getAnimationSpeed() {
        return mAnimationSpeed;
    }

    public Glo3D _getFolders() {
        return mFolder;
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
                cameraLookAt.z - (float) (Math.cos(angleY) * Math.cos(angleX)) * distance);

        // We add 180 degrees because the camera is pointing in the opposite
        // direction from the billboards, which are what are actually attached
        // to the "camera container" (the camera cannot reside in a container
        // as it is separate from the scene graph for some reason)
        Rotation cameraRotation = new Rotation(
                mCameraAngleX, -mCameraAngleY, 0);

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
        float screenScale = 2 * mScreenDistance / mScreenWidth *
                (float) Math.tan(fov * 0.5f);

        // Now position and scale the screen containers. The top-left conatiner
        // positions the origin at the top-left corner of the screen, whereas
        // the central container positions it at the centre of the screen.
        // We must invert the y-axis because for some reason, text renders
        // upside down in ngin3d
        mCentralScreenContainer.setPosition(new Point(0, 0, mScreenDistance));
        mCentralScreenContainer.setScale(new Scale(-screenScale, -screenScale, screenScale));
        mTopLeftScreenContainer.setPosition(new Point(-mScreenWidth / 2, -mScreenHeight / 2, 0));

        // Standard perspective projection
        stage.setProjection(Stage.PERSPECTIVE,
                mCameraNearClipDistance, mCameraFarClipDistance, 0.0f);
        stage.setCameraFov((float)Math.toDegrees(fov));
    }

    // Camera setup functions

    public static float deg2rad(float degrees) {
        float radians = degrees / 180f * 3.14159f;
        return radians;
    }

    public void setCameraAngleX(float angle) {

        mCameraAngleX = angle;
        updateCamera();
    }

    public float getCameraAngleX() {
        return mCameraAngleX;
    }

    public void setCameraAngleY(float angle) {
        mCameraAngleY = angle;
        updateCamera();
    }

    public float getCameraAngleY() {
        return mCameraAngleY;
    }

    public void setCameraDistance(float distance) {
        mCameraDistance = distance;
        updateCamera();
    }

    public float getCameraDistance() {
        return mCameraDistance;
    }

    public void setCameraFov(float fov) {
        fov = Math.min(180, Math.max(fov, 10));
        mCameraFov = fov;
        updateCamera();
    }

    public float getCameraFov() {
        return mCameraFov;
    }

    public void setCameraLookAt(Point lookAt) {
        mCameraLookAt = lookAt;
        updateCamera();
    }

    public Point getCameraLookAt() {
        return mCameraLookAt;
    }

    public void setScreenDistance(float distance) {
        mScreenDistance = distance;
        updateCamera();
    }

    public float getScreenDistance() {
        return mScreenDistance;
    }

    public class MyStageTextureView extends StageTextureView {
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

    public MyStageTextureView getStageView() {
        return mStageView;
    }

    // --------------------------------------------------------------------------
    // Screen transition code
    // --------------------------------------------------------------------------

    // Invoked every time View tree is drawn
    // This is a callback exposed by Mediatek modifications to Android
    public boolean onPostDraw() {
        // Perform delayed hiding of the StageView from the freeze() function
        if (mFrozen) {
            mStageView.setVisibility(View.INVISIBLE);
        }

        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnPostDrawListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPostDrawListener(this);
    }

    // Indicates whether the widget is frozen while the home screen is
    // transitioning
    private boolean mFrozen = false;

    // Override freeze and unfreeze functions to make sure tasks and sound
    // are paused when the widget is frozen
    public boolean freeze(int currentScreen) {
        // boolean isFrozen = super.freeze(currentScreen);

        if (mFrozen) {

        }

        return mFrozen;
    }

    public void unfreeze(int currentScreen) {

        if (mFrozen) {
            mStageView.resumeRendering();
            mStageView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.INVISIBLE);

        }

    }

    // --------------------------------------------------------------------------
    // IMTKWidget methods
    // --------------------------------------------------------------------------

    // IMTKWidget parameters
    private static final int SCREEN_UNKNOWN = -10000;
    private int mAppWidgetScreen = SCREEN_UNKNOWN;
    private int mAppWidgetId = -1;

    public int getPermittedCount() {
        return 1;
    }

    public int getScreen() {
        return mAppWidgetScreen;
    }

    public int getWidgetId() {
        return mAppWidgetId;
    }

    // Called when the widget moves into a new screen
    public void moveIn(int currentScreen) {
        unfreeze(currentScreen);
    }

    // Called when the widget moves out of the screen
    // The return value determines whether the screen transition starts
    public boolean moveOut(int currentScreen) {
        freeze(currentScreen);
        return true;
    }

    // Called when the widget needs to be paused
    public void onPauseWhenShown(int currentScreen) {
        freeze(currentScreen);
    }

    // Called when the widget needs to be unpaused
    public void onResumeWhenShown(int currentScreen) {
        unfreeze(currentScreen);
    }

    public void setScreen(int screen) {
        mAppWidgetScreen = screen;
    }

    public void setWidgetId(int widgetId) {
        mAppWidgetId = widgetId;
    }

    // Called when the widget is about to be covered by something else
    public void startCovered(int currentScreen) {
        freeze(currentScreen);
    }

    // Called when the widget is about to be shown again
    public void stopCovered(int currentScreen) {
        unfreeze(currentScreen);
    }

    // Called when a drag operation on the widget starts
    public void startDrag() {
        freeze(mAppWidgetScreen);
    }

    // Called when a drag operation of the widget is ending
    public void stopDrag() {
        unfreeze(mAppWidgetScreen);
    }

    // Called after launcher's onSaveInstanceState is called.
    public void onSaveInstanceState(Bundle outState) {
    }

    // Called after launcher's onRestoreInstanceState is called.
    public void onRestoreInstanceState(Bundle state) {
    }

    public void enterAppwidgetScreen() {
        unfreeze(mAppWidgetScreen);
    }

    public void leaveAppwidgetScreen() {
        freeze(mAppWidgetScreen);
    }
}
