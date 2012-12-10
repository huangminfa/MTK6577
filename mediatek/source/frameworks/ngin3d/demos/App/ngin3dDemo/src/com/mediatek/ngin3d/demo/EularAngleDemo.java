
package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Text;
import com.mediatek.ngin3d.android.StageActivity;

public class EularAngleDemo extends StageActivity {

    private static final float SURFZ = 0.f;
    private Container mScenario;
    private GestureDetector mGestureDetector;
    private float mYaw;
    private float mRoll;
    private float mPitch;
    private Text mYawText;
    private Text mRollText;
    private Text mPitchText;

    public class MyGestureDetector extends SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                float distanceY) {
            mPitch = mScenario.getRotation().getEulerAngles()[0];
            mYaw = mScenario.getRotation().getEulerAngles()[1];
            mRoll = mScenario.getRotation().getEulerAngles()[2];

            if (e1.getAction() != MotionEvent.ACTION_POINTER_DOWN
                    || e1.getAction() != MotionEvent.ACTION_POINTER_UP) {
                if (distanceX < 10 || distanceX > 10) {
                    mRoll = mRoll + distanceX / 10;
                    mRollText.setText("roll angle: " + mRoll);
                }

                if (distanceY > 10 || distanceY < 10) {
                    mPitch = mPitch + distanceY / 10;
                    mPitchText.setText("pitch angle: " + mPitch);
                }
            }

            mScenario.setRotation(new Rotation(mPitch, mYaw, mRoll));
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent upEvent) {

            return true;
        }

    }

    private void zoom(float f) {
        mYaw = (mYaw + 0.1f) * f;
        mYawText.setText("yaw angle: " + mYaw);
        mScenario.setRotation(new Rotation(mPitch, mYaw, mRoll));
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    int mode = 0;
    float oldDist;

    public boolean onTouchEvent(MotionEvent event) {

        mYaw = mScenario.getRotation().getEulerAngles()[1];

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = 1;
                break;
            case MotionEvent.ACTION_UP:
                mode = 0;

                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode -= 1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                mode += 1;
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode >= 2) {
                    float newDist = spacing(event);
                    if (newDist > oldDist + 1) {
                        Log.e("zoom", ">>");
                        zoom(newDist / oldDist);
                        oldDist = newDist;
                    }
                    if (newDist < oldDist - 1) {
                        Log.e("zoom", "<<");
                        zoom(newDist / oldDist);
                        oldDist = newDist;
                    }
                    return true;
                }

        }

        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        return true;
    }

    private int mWidth;
    private int mHight;

    private void getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth(); // deprecated
        mHight = display.getHeight(); // deprecated

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDisplaySize();

        mGestureDetector = new GestureDetector(new MyGestureDetector());
        mScenario = new Container();

        final Glo3D landscape = Glo3D.createFromAsset("landscape.glo");
        mScenario.add(landscape);
        mScenario.setPosition(new Point(mWidth / 2, mHight / 2 + 50, SURFZ));
        mScenario.setScale(new Scale(30, -30, 30));

        mYawText = new Text("yaw angle: " + mScenario.getRotation().getEulerAngles()[0]);
        mYawText.setBackgroundColor(new Color(255, 0, 0, 128));
        mYawText.setPosition(new Point(mWidth / 2, 100));

        mRollText = new Text("roll angle: " + mScenario.getRotation().getEulerAngles()[1]);
        mRollText.setBackgroundColor(new Color(255, 0, 0, 128));
        mRollText.setPosition(new Point(mWidth / 2, 150));

        mPitchText = new Text("pitch angle: " + mScenario.getRotation().getEulerAngles()[2]);
        mPitchText.setBackgroundColor(new Color(255, 0, 0, 128));
        mPitchText.setPosition(new Point(mWidth / 2, 200));

        mStage.add(mScenario, mYawText, mRollText, mPitchText);
    }
}
