package com.mediatek.media3d;

import android.content.res.Resources;
import android.util.Log;
import android.view.MotionEvent;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;

import java.util.HashMap;

public class NavigationBar extends Bar {
    private static final String TAG = "NavigationBar";

    private final Listener mListener;
    private int mHitActorTag;
    private final HashMap<Integer, Button> mButtons = new HashMap<Integer, Button>();

    public interface Listener {
        void onPageSelected(String page);

        void onTimerStopped();

        void onTimerStarted();
    }

    static class Button {
        private final int mDefaultState;
        private final int mPressedState;
        private final String mPageName;

        Button(int defaultState, int pressedState, String pageName) {
            mDefaultState = defaultState;
            mPressedState = pressedState;
            mPageName = pageName;
        }

        int getDefaultIcon() {
            return mDefaultState;
        }

        int getPressedIcon() {
            return mPressedState;
        }

        String getPageName() {
            return mPageName;
        }
    }

    private static final int TAG_WEATHER = 1;
    private static final int TAG_PHOTO = 2;
    private static final int TAG_VIDEO = 3;

    public NavigationBar(Resources resources, Listener listener) {
        int zBase = -20;
        mListener = listener;

        mButtons.put(TAG_WEATHER,
            new Button(R.raw.bottom_tab_weather, R.drawable.bottom_tab_weather_pressed, PageHost.WEATHER));
        mButtons.put(TAG_PHOTO,
            new Button(R.raw.bottom_tab_photo, R.drawable.bottom_tab_photo_pressed, PageHost.PHOTO));
        mButtons.put(TAG_VIDEO,
            new Button(R.raw.bottom_tab_video, R.drawable.bottom_tab_video_pressed, PageHost.VIDEO));

        Image mBackground = Image.createFromResource(resources, R.drawable.bottom_bar_background);
        mBackground.setPosition(new Point(0, 0, zBase));
        mBackground.setColor(new Color(64, 64, 64, 128));
        mBackground.setZOrderOnTop(true, 3);
        add(mBackground);

        int xStartPos = -98;
        int xPosInterval = 98;
        int index = 0;
        for (int key : mButtons.keySet()) {
            Button btnActor = mButtons.get(key);

            Image pressedActor = Image.createFromResource(resources, btnActor.getPressedIcon());
            pressedActor.setPosition(new Point((xStartPos + xPosInterval * index), 0, -0.01f + zBase));
            pressedActor.setTag(btnActor.getPressedIcon());
            pressedActor.setZOrderOnTop(true, 2);
            pressedActor.setReactive(false);
            pressedActor.setVisible(false);
            add(pressedActor);

            Image defaultActor = Image.createFromResource(resources, btnActor.getDefaultIcon());
            defaultActor.setPosition(new Point((xStartPos + xPosInterval * index++), 0, -0.02f + zBase));
            defaultActor.setTag(key);
            defaultActor.setZOrderOnTop(true, 1);
            add(defaultActor);
        }
    }

    private void onHitAction(int action, int hittingTag) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "action = " + action + " , hittingTag = " + hittingTag);
        }

        if (action == MotionEvent.ACTION_DOWN) {
            if (mHitActorTag == 0) {
                findChildByTag(mButtons.get(hittingTag).getPressedIcon()).setVisible(true);
                mHitActorTag = hittingTag;
            }
            mListener.onTimerStopped();
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mHitActorTag != 0 && mHitActorTag != hittingTag) {
                findChildByTag(mButtons.get(mHitActorTag).getPressedIcon()).setVisible(false);
            }
        } else {
            findChildByTag(mButtons.get(hittingTag).getPressedIcon()).setVisible(false);
            if (mHitActorTag == hittingTag) {
                String pageName = mButtons.get(hittingTag).getPageName();
                mListener.onPageSelected(pageName);
                if (Media3D.DEBUG) {
                    Log.v(TAG, "onTouchEvent, " + pageName);
                }
            }
            mListener.onTimerStarted();
            mHitActorTag = 0;
        }
    }

    @Override
    protected boolean onHit(Actor hit, int action) {
        if (hit.getTag() == TAG_WEATHER || hit.getTag() == TAG_PHOTO || hit.getTag() == TAG_VIDEO) {
            onHitAction(action, hit.getTag());
            return true;
        } else {
            if (mHitActorTag != 0) {
                findChildByTag(mButtons.get(mHitActorTag).getPressedIcon()).setVisible(false);
                mListener.onTimerStarted();
                mHitActorTag = 0;
            }
            return false;
        }
    }

    @Override
    protected boolean onHitNothing() {
        Log.v(TAG, "onHitNothing");
        return false;
    }
}

