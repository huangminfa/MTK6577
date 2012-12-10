
package com.mediatek.widgetdemos.clock3d;

import java.lang.Math;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
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
import com.mediatek.ngin3d.android.GLTextureView;
import com.mediatek.ngin3d.android.StageTextureView;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.Animation.Listener;
import com.mediatek.ngin3d.animation.BasicAnimation;

@RemoteView
public class ClockView extends RelativeLayout implements IMTKWidget, OnPostDrawListener {
    private static final String TAG = "ClockView";

    private class Materials {
        public static final int CLOCK = 28;
        public static final int CLOCK_TRANSPARENT = 30;
        public static final int CLOCK_SPHERE_MAP = 31;
        public static final int CLOCK_EMISSIVE = 32;
    };

    private class ClockStates {
        public static final int ANALOG = 0;
        public static final int DIGITAL = 1;
        public static final int A2D = 2;
        public static final int D2A = 3;
        public static final int RINGING = 4;
        public static final int BEEPING = 5;
    }

    private class AlarmState {
        public static final int UNDEFINED = 0;
        public static final int OFF = 1;
        public static final int ON = 2;
    };

    private class AmPmState {
        public static final int UNDEFINED = 0;
        public static final int AM = 1;
        public static final int PM = 2;
    };

    private static final String[] NODES_TRANSPARENT = {
            "glass"
    };

    private static final String[] NODES_SPHERE_MAPPED = {
            "body",
            "handle",
            "hammer_head",
            "hammer_pole",
            "antenna_hole",
            "antenna_ball",
            "antenna_inner",
            "antenna_outer",
            "left_bell",
            "left_bell_knob",
            "left_bell_stem",
            "right_bell",
            "right_bell_knob",
            "right_bell_stem",
            "left_leg",
            "right_leg"
    };

    private static final String[] NODES_EMISSIVE = {
            "digital_face",
            "ten_hour_digit",
            "one_hour_digit",
            "ten_minute_digit",
            "one_minute_digit",
            "colon",
            "am_pm_indicator",
            "hour_hand",
            "minute_hand",
            "second_hand"
    };

    private static final int LIGHT_TRANSITION_PERIOD = 500;
    private static final int CAMERA_TRANSITION_PERIOD = 500;

    private int mClockState = ClockStates.ANALOG;

    private Map<String, Glo3D> mObjects = new HashMap<String, Glo3D>();
    private Map<String, BasicAnimation> mAnimations = new HashMap<String, BasicAnimation>();
    private Map<String, MediaPlayer> mSounds = new HashMap<String, MediaPlayer>();

    private Set<MediaPlayer> mPausedSounds = new HashSet<MediaPlayer>();

    private Point[] mLightPositions = {
            new Point(30, 70, 50),
            new Point(50, 30, 210)
    };

    private int mCurrentLightPosition = 0;
    private int mNextLightPosition = mCurrentLightPosition;

    // AngleX, AngleY, Zoom
    private Point[] mCameraPositions = {
            new Point(12, 158, 125),
            new Point(12, 178, 125)
    };

    private int mCurrentCameraPosition = 0;
    private int mNextCameraPosition = mCurrentCameraPosition;

    private float mAnimationSpeed = 1f;

    private int mCurrentAmPmState = AmPmState.UNDEFINED;
    private int mCurrentAlarmState = AlarmState.UNDEFINED;
    private int[] mCurrentDigitValues = {
            -1, -1, -1, -1
    };
    private boolean mDigitsOn = true;

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

    // --------------------------------------------------------------------------
    public ClockView(Context context) {
        super(context);
        initialise();
        createScene();
    }

    // --------------------------------------------------------------------------
    public ClockView(Context context, AttributeSet attrs) {
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

    // --------------------------------------------------------------------------
    private void createScene() {
        mStageView.setRenderMode(GLTextureView.RENDERMODE_CONTINUOUSLY);
        // Load sound effects
        mSounds.put("ring", MediaPlayer.create(getContext(), R.raw.ring));
        mSounds.put("beep", MediaPlayer.create(getContext(), R.raw.beep));

        // Custom camera setup
        Point cameraPosition = mCameraPositions[mCurrentCameraPosition];
        setCameraAngleX(cameraPosition.x);
        setCameraAngleY(cameraPosition.y);
        setCameraDistance(cameraPosition.z);
        setCameraFov(27.5f);

        // Load the scene objects
        Glo3D light = Glo3D.createFromAsset("light_point.glo");
        mObjects.put("light", light);
        light.setPosition(mLightPositions[mCurrentLightPosition]);
        mSceneContainer.add(light);

        Glo3D clock = Glo3D.createFromAsset("clock.glo");
        mObjects.put("clock", clock);
        clock.setPosition(new Point(-3, 0, 0));
        clock.setMaterialType(Materials.CLOCK);
        mSceneContainer.add(clock);

        // Set the various material types for the clock sub-nodes
        for (String node : NODES_TRANSPARENT) {
            clock.setMaterialType(node, Materials.CLOCK_TRANSPARENT);
        }

        for (String node : NODES_SPHERE_MAPPED) {
            clock.setMaterialType(node, Materials.CLOCK_SPHERE_MAP);
        }

        for (String node : NODES_EMISSIVE) {
            clock.setMaterialType(node, Materials.CLOCK_EMISSIVE);
        }

        // Manually set some of the textures
        clock.setMaterialTexture("digital_face", "digit_blank.png");
        clock.setMaterialTexture("am_pm_indicator", "am.png");

        // Get the clock animations
        Glo3D clockAnimA2dObject = Glo3D.createFromAsset("clock_anim_a2d.glo");
        mObjects.put("a2d", clockAnimA2dObject);
        mSceneContainer.add(clockAnimA2dObject);
        Glo3D clockAnimD2aObject = Glo3D.createFromAsset("clock_anim_d2a.glo");
        mObjects.put("a2d", clockAnimD2aObject);
        mSceneContainer.add(clockAnimD2aObject);
        Glo3D clockAnimRingObject = Glo3D.createFromAsset("clock_anim_ring.glo");
        mObjects.put("ring", clockAnimRingObject);
        mSceneContainer.add(clockAnimRingObject);

        BasicAnimation clockAnimA2d = clockAnimA2dObject.getAnimation();
        mAnimations.put("a2d", clockAnimA2d);
        BasicAnimation clockAnimD2a = clockAnimD2aObject.getAnimation();
        mAnimations.put("d2a", clockAnimD2a);
        BasicAnimation clockAnimRing = clockAnimRingObject.getAnimation();
        mAnimations.put("ring", clockAnimRing);

        // The widget state is set when animations finish
        clockAnimA2d.addListener(new Listener() {
            public void onCompleted(Animation animation) {
                mClockState = ClockStates.DIGITAL;
            }
        });
        clockAnimD2a.addListener(new Listener() {
            public void onCompleted(Animation animation) {
                mClockState = ClockStates.ANALOG;
            }
        });
        clockAnimRing.addListener(new Listener() {
            public void onCompleted(Animation animation) {
                mClockState = ClockStates.ANALOG;
            }
        });

        GestureView clockGestureView = addGestureArea(0, -24, 200, 300);

        clockGestureView.setOnGestureListener(new SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent upEvent) {
                switch (mClockState) {
                    case ClockStates.ANALOG:
                        MediaPlayer ringSound = mSounds.get("ring");
                        if (!ringSound.isPlaying()) {
                            mAnimations.get("ring").start();
                            ringSound.start();
                            mClockState = ClockStates.RINGING;
                        }
                        break;

                    case ClockStates.DIGITAL:
                        MediaPlayer beepSound = mSounds.get("beep");
                        if (!beepSound.isPlaying()) {
                            mAlarmFlashTask.startDelayed();
                            beepSound.start();
                            mClockState = ClockStates.BEEPING;

                            Log.v(TAG, "Beep sound is not playing");
                        } else {
                            Log.v(TAG, "Beep sound is playing");
                        }
                        break;
                }

                return true;
            }

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
                if (velocityY > 0) {
                    if (mCurrentCameraPosition == mNextCameraPosition) {
                        mNextCameraPosition = (mCurrentCameraPosition + 1)
                                % mCameraPositions.length;
                        mCameraTransitionTask.start();
                    }
                } else if (velocityY < 0) {
                    // if (mCurrentLightPosition == mNextLightPosition) {
                    // mNextLightPosition = (mCurrentLightPosition + 1) %
                    // mLightPositions.length;
                    // mLightTransitionTask.start();
                    // }
                    switch (mClockState) {
                        case ClockStates.ANALOG:
                            mAnimations.get("a2d").start();
                            mClockState = ClockStates.A2D;
                            break;
                        case ClockStates.DIGITAL:
                            mAnimations.get("d2a").start();
                            mClockState = ClockStates.D2A;
                            break;
                    }
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

        mUpdateTimeTask.start();
    }

    // Adjusts the camera configuration to match a new screen size
    public void setScreenSize(int screenWidth, int screenHeight) {
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        updateCamera();
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

    private PeriodicTask mLightTransitionTask = new PeriodicTask(1000 / 30) {
        public void run(int runCount, int timeElapsed) {
            float progress = Math.min(1.0f, timeElapsed / (float) LIGHT_TRANSITION_PERIOD);
            float interpolation = (float) -Math.abs(Math.pow(progress - 1.0f, 3.0f)) + 1.0f;

            Point currentPosition = new Point(mLightPositions[mCurrentLightPosition]);
            Point nextPosition = mLightPositions[mNextLightPosition];

            currentPosition.x = currentPosition.x * (1.0f - interpolation) + nextPosition.x
                    * interpolation;
            currentPosition.y = currentPosition.y * (1.0f - interpolation) + nextPosition.y
                    * interpolation;
            currentPosition.z = currentPosition.z * (1.0f - interpolation) + nextPosition.z
                    * interpolation;

            mObjects.get("light").setPosition(currentPosition);

            if (progress >= 1.0f) {
                mCurrentLightPosition = mNextLightPosition;
                stop();
            }
        }
    };

    private PeriodicTask mCameraTransitionTask = new PeriodicTask(1000 / 30) {
        public void run(int runCount, int timeElapsed) {
            float progress = Math.min(1.0f, timeElapsed / (float) CAMERA_TRANSITION_PERIOD);
            float interpolation = (float) -Math.abs(Math.pow(progress - 1.0f, 3.0f)) + 1.0f;
            ;

            Point currentPosition = new Point(mCameraPositions[mCurrentCameraPosition]);
            Point nextPosition = mCameraPositions[mNextCameraPosition];

            currentPosition.x = currentPosition.x * (1.0f - interpolation) + nextPosition.x
                    * interpolation;
            currentPosition.y = currentPosition.y * (1.0f - interpolation) + nextPosition.y
                    * interpolation;
            currentPosition.z = currentPosition.z * (1.0f - interpolation) + nextPosition.z
                    * interpolation;

            setCameraAngleX(currentPosition.x);
            setCameraAngleY(currentPosition.y);
            setCameraDistance(currentPosition.z);

            if (progress >= 1.0f) {
                mCurrentCameraPosition = mNextCameraPosition;
                stop();
            }
        }
    };

    private PeriodicTask mAlarmFlashTask = new PeriodicTask(1000, 0, true, 500) {
        public void run(int runCount, int timeElapsed) {
            // Stop flashing when the sound stops
            MediaPlayer beepSound = mSounds.get("beep");
            if (!(beepSound.isPlaying() || mPausedSounds.contains(beepSound))) {
                mClockState = ClockStates.DIGITAL;
                stop();
                return;
            }

            final String[] digits = {
                    "ten_hour_digit",
                    "one_hour_digit",
                    "ten_minute_digit",
                    "one_minute_digit",
            };

            Glo3D clock = mObjects.get("clock");
            for (int i = 0; i < digits.length; ++i) {
                String digitName = digits[i];
                clock.setMaterialTexture(digitName, "digit_off.png");
            }

            mDigitsOn = false;
        }
    };

    private PeriodicTask mUpdateTimeTask = new PeriodicTask(1000, true) {
        public void run(int runCount, int timeElapsed) {
            // Grab the next alarm time and set the text
            String nextAlarmString = Settings.System.getString(
                    getContext().getContentResolver(),
                    Settings.System.NEXT_ALARM_FORMATTED);

            // Grab the current time
            Calendar calendar = Calendar.getInstance();
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            int seconds = calendar.get(Calendar.SECOND);

            final String[] digits = {
                    "ten_hour_digit",
                    "one_hour_digit",
                    "ten_minute_digit",
                    "one_minute_digit",
            };

            // /////////////////////
            // Update analog time
            // /////////////////////

            Glo3D clock = mObjects.get("clock");
            clock.setRotation("hour_hand", new Rotation(180,
                    (hours + minutes / 60.0f) / 12.0f * 360.0f, 0));
            clock.setRotation("minute_hand", new Rotation(180, minutes / 60.0f * 360.0f, 0));
            clock.setRotation("second_hand", new Rotation(180, seconds / 60.0f * 360.0f, 0));

            // /////////////////////
            // Update digital time
            // /////////////////////

            int hours12 = (hours + 11) % 12 + 1;

            int[] digitValues = {
                    hours12 / 10,
                    hours12 % 10,
                    minutes / 10,
                    minutes % 10
            };

            // Set the numeric digits if they have changed since last time
            for (int i = 0; i < digits.length; ++i) {
                int value = digitValues[i];
                int oldValue = mCurrentDigitValues[i];

                if (!mDigitsOn || value != oldValue) {
                    String digitFilename;
                    String analogFilename;

                    // Special case for "zero" in the ten hours digit (turn off)
                    if (i == 0 && value == 0) {
                        digitFilename = "digit_off.png";
                        analogFilename = "alarm_clock_face_style_off.jpg";
                    } else {
                        digitFilename = "digit_" + Integer.toString(value) + ".png";
                        analogFilename = "alarm_clock_face_style.jpg";
                    }

                    String digitName = digits[i];
                    clock.setMaterialTexture(digitName, digitFilename);
                    mCurrentDigitValues[i] = value;
                }
            }

            // Make the colon flash once every second
            String colonFilename;

            if (seconds % 2 == 0) {
                colonFilename = "colon_off.png";
            } else {
                colonFilename = "colon_on.png";
            }

            clock.setMaterialTexture("colon", colonFilename);

            // Change the AM-PM indicator, if necessary
            int amPmState = (hours < 12) ? AmPmState.AM : AmPmState.PM;
            int alarmState = nextAlarmString.isEmpty() ? AlarmState.OFF : AlarmState.ON;

            if (amPmState != mCurrentAmPmState || alarmState != mCurrentAlarmState) {
                String amPmFilename = "indicator";

                switch (amPmState) {
                    case AmPmState.AM:
                        amPmFilename = amPmFilename.concat("_am");
                        break;
                    case AmPmState.PM:
                        amPmFilename = amPmFilename.concat("_pm");
                        break;
                }

                String analogFilename = "alarm_clock_face_style.jpg";

                switch (alarmState) {
                    case AlarmState.OFF:
                        amPmFilename = amPmFilename.concat("_off");
                        analogFilename = "alarm_clock_face_style_off.jpg";
                        break;

                    case AlarmState.ON:
                        amPmFilename = amPmFilename.concat("_on");
                        break;
                }

                amPmFilename = amPmFilename.concat(".png");

                Log.v(TAG, amPmFilename);

                clock.setMaterialTexture("am_pm_indicator", amPmFilename);
                clock.setMaterialTexture("analog_face", analogFilename);
                mCurrentAmPmState = amPmState;
                mCurrentAlarmState = alarmState;
            }
        }
    };

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
    // @Override
    public boolean freeze(int currentScreen) {
        // boolean isFrozen = super.freeze(currentScreen);

        if (mFrozen) {
            // This is necessary to ensure that the tasks do not cause the view
            // to be kept indefinitely alive in the background.
            mUpdateTimeTask.pause();
            mAlarmFlashTask.pause();
            mCameraTransitionTask.pause();
            mLightTransitionTask.pause();

            for (MediaPlayer sound : mSounds.values()) {
                if (sound.isPlaying()) {
                    sound.pause();
                    mPausedSounds.add(sound);
                }
            }
        }

        return mFrozen;
    }

    // @Override
    public void unfreeze(int currentScreen) {

        if (mFrozen) {
            mStageView.resumeRendering();
            mStageView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
            
            mUpdateTimeTask.resume();
            mAlarmFlashTask.resume();
            mCameraTransitionTask.resume();
            mLightTransitionTask.resume();

            for (MediaPlayer sound : mPausedSounds) {
                sound.start();
            }

            mPausedSounds.clear();
            mFrozen = false;
        }

    }

    // Tweak values
    public void setAnimationSpeed(float speed) {
        speed = Math.max(0.01f, speed);
        mAnimationSpeed = speed;

        mAnimations.get("a2d").setTimeScale(speed);
        mAnimations.get("d2a").setTimeScale(speed);
    }

    public float getAnimationSpeed() {
        return mAnimationSpeed;
    }

    public Glo3D _getLight() {
        return mObjects.get("light");
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
