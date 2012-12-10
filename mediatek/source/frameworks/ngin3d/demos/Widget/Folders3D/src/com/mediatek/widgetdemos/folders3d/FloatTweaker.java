package com.mediatek.widgetdemos.folders3d;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Button;
import android.widget.RemoteViews.RemoteView;

// A widget that allows you to change a floating point value by swiping left and
// right.
@RemoteView
public class FloatTweaker extends Button {
    private String mName = "";
    private float mValue;
    private VelocityTracker mVelocityTracker;
    private OnValueChangeCallback mOnValueChangeCallback;

    public FloatTweaker(Context context) {
        super(context);
        initialise();
    }

    public FloatTweaker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public void initialise() {
        mVelocityTracker = VelocityTracker.obtain();
    }

    @Override
    public void onFinishInflate() {
        updateText();
    }

    // Override the onTouch callback to allow the value to be changed
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v("FloatTweaker", "onTouchEvent");
        int action = event.getAction();

        if (action == MotionEvent.ACTION_MOVE) {
            mVelocityTracker.addMovement(event);
            mVelocityTracker.computeCurrentVelocity(10); 

            float velocityX = mVelocityTracker.getXVelocity();
            mValue += velocityX;

            // Called when the value changes
            if (mOnValueChangeCallback != null) {
                mValue = mOnValueChangeCallback.onValueChange(mValue);
            }
            updateText();
        }

        return true;
    }

    public void updateText() {
        if (mName.equals("")) {
            setText("");
        } else {
            setText(mName + ": " + Float.toString(mValue));
        }
    }

    public void setValue(float value) {
        mValue = value;
        updateText();
    }

    public float getValue() {
        return mValue;
    }

    public void setName(String name) {
        mName = name;
        updateText();
    }

    public String getName() {
        return mName;
    }

    public interface OnValueChangeCallback {
        float onValueChange(float value);
    }

    public void setOnValueChangeCallback(OnValueChangeCallback callback) {
        mOnValueChangeCallback = callback;
    }

}
