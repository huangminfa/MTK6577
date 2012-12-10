
package com.mediatek.ngin3d.demo;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation.Listener;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.PropertyAnimation;

/**
 * A demo for usage of Object3D.
 */
public class CameraPositionDemo extends StageActivity {

    private static final float DEFAULT_Z_NEAR = 2f;
    private static final float DEFAULT_Z_FAR = 3000f;
    private static final int HEIGHT = 800;
    private static final int WIDTH = 480;
    private static final float CAMERA_Z_POS = -1111;
    private BasicAnimation mBendGail;
    private BasicAnimation mBlowGail;
    private BasicAnimation mSheepWalk;
    private BasicAnimation mShowHide;
    private BasicAnimation mCameraMove;
    private Point mCameraPos;
    private Point mCameraLookAt;
    private Stage.Camera mCameraFrom;
    private Stage.Camera mCameraTo;
    private float mFOV;
    private final int MOVIE_LIFT_START = 0;
    private final int MOVIE_RIGHT_START = 2;
    private final int MOVIE_UP_START = 4;
    private final int MOVIE_DOWN_START = 6;
    private final int MOVIE_CENTER_START = 8;
    private final int MOVIE_SHALLOW_START = 10;
    private final int MOVIE_DEEP_START = 12;
    private int mAnimationStat = -1;
    private BasicAnimation mCameraMoveRigth;
    private BasicAnimation mCameraMoveLift;
    private BasicAnimation mCameraMoveUP;
    private BasicAnimation mCameraMoveDOWN;
    private BasicAnimation mCameraMoveSHALLOW;
    private BasicAnimation mCameraMoveDEEP;
    private Stage.Camera mCameraToRight;
    private Stage.Camera mCameraToLift;
    private Stage.Camera mCameraToUP;
    private Stage.Camera mCameraToDOWN;
    private Stage.Camera mCameraToSHALLOW;
    private Stage.Camera mCameraToDEEP;
    private Stage.Camera mCurrentCameraPosition;
    private Container mScenario;
    private SensorManager oriSensorManager;

    @Override
    protected void onPause() {
        Log.e("CameraPositionDemo", "onPause");
        oriSensorManager.unregisterListener(MtkSensorListenser);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("CameraPositionDemo", "onResume");

        if (!oriSensorManager.registerListener(MtkSensorListenser
                , oriSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                , SensorManager.SENSOR_DELAY_UI) &&

                oriSensorManager.registerListener(MtkSensorListenser
                        , oriSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                        , SensorManager.SENSOR_DELAY_UI)) {
            Log.e("cameraposition", "sensor not found");
            oriSensorManager.unregisterListener(MtkSensorListenser);
        }

    }

    float[] accelermeter_value;
    float[] magntiude_value;

    private SensorEventListener MtkSensorListenser = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {

            switch (arg0.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelermeter_value = (float[]) arg0.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magntiude_value = (float[]) arg0.values.clone();
                    break;
                default:
                    break;

            }
            if (accelermeter_value != null && magntiude_value != null) {
                float[] R = new float[9];
                float[] values = new float[3];
                SensorManager.getRotationMatrix(R, null, accelermeter_value, magntiude_value);
                SensorManager.getOrientation(R, values);

                Log.e("sensorInfo", "values[0]=" + values[0] + " values[1]=" + values[1]
                        + "values[2]=" + values[2]);

            }

            Log.e("SnowFall", "mOriYaw = " + arg0.values[0] + ", mOriPitch = " + arg0.values[1]
                    + ", mOriRoll = " + arg0.values[2]);

            if (arg0.values[2] > 20) {

            } else if (arg0.values[2] < -20) {

            } else if (arg0.values[1] > 4) {
                Log.e("up", "up");
                if (mAnimationStat != MOVIE_UP_START) {
                    Log.e("1111", "mAnimationStat=" + mAnimationStat);
                    mCameraMoveUP.start();
                }
            } else if (arg0.values[1] < -4) {
                Log.e("down", "down");
                if (mAnimationStat != MOVIE_DOWN_START) {
                    mCameraMoveDOWN.start();
                }
            } else if (arg0.values[0] > 4) {
                Log.e("r", "r");
                if (mAnimationStat != MOVIE_LIFT_START) {
                    mCameraMoveLift.start();
                }
            } else if (arg0.values[0] < -4) {
                Log.e("l", "l");
                if (mAnimationStat != MOVIE_RIGHT_START) {
                    mCameraMoveRigth.start();
                }
            } else if (mAnimationStat != MOVIE_CENTER_START) {

                mCameraMove.start();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oriSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        final Image upbutton = Image.createFromResource(getResources(), R.drawable.up_button);
        final Image downbutton = Image.createFromResource(getResources(), R.drawable.down_button);
        final Container button = new Container();
        button.add(upbutton, downbutton);
        upbutton.setPosition(new Point(100, 700, 0));
        downbutton.setPosition(new Point(400, 700, 0));

        final Glo3D landscape = Glo3D.createFromAsset("landscape.glo");
        // tree
        final Container tree = new Container();
        final Glo3D tree_bend_gail = Glo3D.createFromAsset("tree_bend_gail.glo");
        tree.add(tree_bend_gail);

        // sheep
        final Container sheep = new Container();
        final Glo3D sheep_walk = Glo3D.createFromAsset("sheep_walk.glo");
        sheep.add(sheep_walk);

        // sunmoon
        final Container sun_moon = new Container();
        final Glo3D sunmoon = Glo3D.createFromAsset("sunmoon.glo");
        final Glo3D sunmoon_show_hide = Glo3D.createFromAsset("sunmoon_show_hide.glo");
        sun_moon.add(sunmoon, sunmoon_show_hide);

        // leaves
        final Container leaves = new Container();
        final Glo3D leaves_blow_gail = Glo3D.createFromAsset("leaves_blow_gail.glo");
        leaves.add(leaves_blow_gail);

        final Glo3D stars_twinkle = Glo3D.createFromAsset("stars_twinkle.glo");
        final Glo3D rain_fall = Glo3D.createFromAsset("rain_fall.glo");

        mScenario = new Container();
        mScenario.add(landscape, tree, sheep, leaves, sun_moon, stars_twinkle, rain_fall);
        mScenario.setPosition(new Point(240, 400, -780)); // (new Point(240,
                                                          // 400,
                                                          // -800));
        mScenario.setRotation(new Rotation(10, 30, 0));
        mScenario.setScale(new Scale(1f, -1f, 1f));
        mStage.add(mScenario, button);

        // Get animations
        mBendGail = tree_bend_gail.getAnimation();
        mSheepWalk = sheep_walk.getAnimation();
        mBlowGail = leaves_blow_gail.getAnimation();
        mShowHide = sunmoon_show_hide.getAnimation();

        mBendGail.setLoop(true).start();
        mSheepWalk.setLoop(true).start();
        mBlowGail.setLoop(true).start();
        mShowHide.setLoop(true).start();

        // Calculate field of view with radians
        mFOV = (float) Math.atan((HEIGHT / 2f) / Math.abs(CAMERA_Z_POS)) * 2;
        // mStage.setPerspectiveProjection(mFOV, WIDTH / HEIGHT, DEFAULT_Z_NEAR,
        // DEFAULT_Z_FAR);

        mCameraPos = new Point(WIDTH / 2, HEIGHT / 2, -920);// -2000
        mCameraLookAt = new Point(WIDTH / 2, HEIGHT / 2, 0);
        mStage.setCamera(mCameraPos, mCameraLookAt);

        mCameraFrom = new Stage.Camera(mCameraPos, mCameraLookAt);
        mCameraTo = new Stage.Camera(new Point(WIDTH / 2, HEIGHT / 2, -820),
                mCameraLookAt);

        mCameraMove = new PropertyAnimation(mStage, "camera", mCameraTo, mCameraTo) // mCameraTo
                .setDuration(1000)
                .setLoop(true)
                .setAutoReverse(true);
        mCameraMove.start();

        mCurrentCameraPosition = mStage.getCamera();

        mCameraToSHALLOW = new Stage.Camera(new Point(WIDTH / 2,
                HEIGHT / 2, -820 + 15),
                new Point(WIDTH / 2, HEIGHT / 2, 0 - 15));

        mCameraMoveSHALLOW = new PropertyAnimation(mStage, "camera", mCurrentCameraPosition,
                mCameraToSHALLOW) // mCameraTo
                .setDuration(500);

        mCameraToDEEP = new Stage.Camera(new Point(WIDTH / 2,
                HEIGHT / 2, -820 - 15),
                new Point(WIDTH / 2, HEIGHT / 2, 0 + 15));

        mCameraMoveDEEP = new PropertyAnimation(mStage, "camera", mCurrentCameraPosition,
                mCameraToDEEP) // mCameraTo
                .setDuration(500);

        mCameraToRight = new Stage.Camera(new Point(WIDTH / 2 + 15,
                HEIGHT / 2, -820),
                new Point(WIDTH / 2 - 15, HEIGHT / 2, 0));

        mCameraMoveRigth = new PropertyAnimation(mStage, "camera", mCurrentCameraPosition,
                mCameraToRight) // mCameraTo
                .setDuration(500);

        mCameraToLift = new Stage.Camera(new Point(WIDTH / 2 - 15, HEIGHT / 2,
                -820),
                new Point(WIDTH / 2 + 15, HEIGHT / 2, 0));

        mCameraMoveLift = new PropertyAnimation(mStage, "camera", mCurrentCameraPosition,
                mCameraToLift) // mCameraTo
                .setDuration(500);

        mCameraToUP = new Stage.Camera(new Point(WIDTH / 2, HEIGHT / 2 + 15,
                -820),
                new Point(WIDTH / 2, HEIGHT / 2, 0));

        mCameraMoveUP = new PropertyAnimation(mStage, "camera", mCurrentCameraPosition,
                mCameraToUP) // mCameraTo
                .setDuration(500);

        mCameraToDOWN = new Stage.Camera(new Point(WIDTH / 2, HEIGHT / 2 - 15,
                -820),
                new Point(WIDTH / 2, HEIGHT / 2 - 15, 0));

        mCameraMoveDOWN = new PropertyAnimation(mStage, "camera", mCurrentCameraPosition,
                mCameraToDOWN) // mCameraTo
                .setDuration(500);

        mCameraMove.addListener(new Listener() {
            public void onStarted(Animation animation) {
                mAnimationStat = MOVIE_CENTER_START;
            }

            public void onCompleted(Animation animation) {
                mCameraMove.stop();

            }
        });

        mCameraMoveRigth.addListener(new Listener() {
            public void onStarted(Animation animation) {
                mCameraMoveLift.stop();
                mAnimationStat = MOVIE_RIGHT_START;
            }

            public void onCompleted(Animation animation) {
                mCameraMoveRigth.stop();

            }
        });

        mCameraMoveLift.addListener(new Listener() {
            public void onStarted(Animation animation) {
                mCameraMoveRigth.stop();
                mAnimationStat = MOVIE_LIFT_START;
            }

            public void onCompleted(Animation animation) {
                mCameraMoveLift.stop();

            }
        });

        mCameraMoveUP.addListener(new Listener() {
            public void onStarted(Animation animation) {
                mCameraMoveDOWN.stop();
                mAnimationStat = MOVIE_UP_START;
            }

            public void onCompleted(Animation animation) {
                mCameraMoveUP.stop();

            }
        });

        mCameraMoveDOWN.addListener(new Listener() {
            public void onStarted(Animation animation) {
                mCameraMoveUP.stop();
                mAnimationStat = MOVIE_DOWN_START;
            }

            public void onCompleted(Animation animation) {
                mCameraMoveDOWN.stop();

            }
        });

        mCameraMoveSHALLOW.addListener(new Listener() {
            public void onStarted(Animation animation) {
                mCameraMoveDEEP.stop();
                mAnimationStat = MOVIE_SHALLOW_START;
            }

            public void onCompleted(Animation animation) {
                mCameraMoveSHALLOW.stop();

            }
        });

        mCameraMoveDEEP.addListener(new Listener() {
            public void onStarted(Animation animation) {
                mCameraMoveSHALLOW.stop();
                mAnimationStat = MOVIE_DEEP_START;
            }

            public void onCompleted(Animation animation) {
                mCameraMoveDEEP.stop();

            }
        });

    }

}
