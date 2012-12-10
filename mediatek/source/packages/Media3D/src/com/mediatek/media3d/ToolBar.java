package com.mediatek.media3d;

import android.content.res.Resources;
import android.util.Log;
import android.view.MotionEvent;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import java.util.HashMap;

public class ToolBar extends Bar {
    private static final String TAG = "ToolBar";

    private final Resources mResources;
    private final NavigationBarMenu mNaviMenu;
    private final HashMap<Integer, Button> mButtons = new HashMap<Integer, Button>();
    private int mHitActorTag;

    public static final int TAG_HOME = 1;
    public static final int TAG_MENU_ITEM_1 = 2;
    public static final int TAG_MENU_ITEM_2 = 3;
    public static final int TAG_HOME_PRESSED = 4;
    public static final int TAG_MENU_ITEM_1_PRESSED = 5;
    public static final int TAG_MENU_ITEM_2_PRESSED = 6;
    public static final int TAG_STEREO3D = 7;
    public static final int TAG_STEREO3D_PRESSED = 8;
    public static final int TAG_NORMAL2D = 9;
    public static final int TAG_NORMAL2D_PRESSED = 10;

    public interface Listener {
        void onButtonPressed(int buttonTag);
        void onMenuItemSelected(NavigationBarMenuItem item);
        void onTimerStopped();
        void onTimerStarted();
    }

    private final Listener mListener;

    static class Button {
        private final int mDefaultState;
        private final int mPressedTag;
        private final float mXPosition;
        private static final int PRESSED_ICON = R.drawable.tool_bar_button_pressed;

        Button(int defaultState, int pressedTag, float xPosition) {
            mDefaultState = defaultState;
            mPressedTag = pressedTag;
            mXPosition = xPosition;
        }

        int getDefaultIcon() {
            return mDefaultState;
        }

        int getPressedTag() {
            return mPressedTag;
        }

        int getPressedIcon() {
            return PRESSED_ICON;
        }

        float getXPosition() {
            return mXPosition;
        }
    }

    public ToolBar(Resources resources, Listener listener) {
        int zBase = -20;
        mResources = resources;
        mListener = listener;

        mNaviMenu = new NavigationBarMenu();

        Image mBackground = Image.createFromResource(resources, R.drawable.top_bar_background);
        mBackground.setPosition(new Point(0, 0, zBase));
        mBackground.setColor(new Color(128, 128, 128, 128));
        mBackground.setZOrderOnTop(true, 3);
        this.add(mBackground);

        mButtons.put(TAG_HOME,
                new Button(R.drawable.top_menu, TAG_HOME_PRESSED, -0.43875f));
        mButtons.put(TAG_STEREO3D,
                new Button(R.drawable.top_stereo3d, TAG_STEREO3D_PRESSED, -0.31625f));
        mButtons.put(TAG_NORMAL2D,
                new Button(R.drawable.top_normal2d, TAG_NORMAL2D_PRESSED, -0.31625f));
        mButtons.put(TAG_MENU_ITEM_1,
                new Button(R.drawable.top_menu, TAG_MENU_ITEM_1_PRESSED, 0.43875f));
        mButtons.put(TAG_MENU_ITEM_2,
                new Button(R.drawable.top_menu, TAG_MENU_ITEM_2_PRESSED, 0.31625f));

        for (int key : mButtons.keySet()) {
            if ((key == TAG_STEREO3D || key == TAG_NORMAL2D) && !Stereo3DWrapper.isStereo3DSupported()) {
                continue;
            }

            Button btnActor = mButtons.get(key);

            Image pressedActor = Image.createFromResource(resources, btnActor.getPressedIcon());
            pressedActor.setPosition(new Point(btnActor.getXPosition(), 0, -0.01f + zBase, true));
            pressedActor.setZOrderOnTop(true, 2);
            pressedActor.setReactive(false);
            pressedActor.setVisible(false);
            pressedActor.setTag(btnActor.getPressedTag());
            add(pressedActor);

            Image defaultActor = Image.createFromResource(resources, btnActor.getDefaultIcon());
            defaultActor.setPosition(new Point(btnActor.getXPosition(), 0, -0.02f + zBase, true));
            defaultActor.setZOrderOnTop(true, 1);
            defaultActor.setTag(key);
            add(defaultActor);

            if (key == TAG_NORMAL2D) {
                defaultActor.setVisible(false);
            }
        }
    }

    public NavigationBarMenu getNavigationMenu() {
        return mNaviMenu;
    }

    public void fillMenuIcon() {
        Image menu1 = (Image) (findChildByTag(TAG_MENU_ITEM_1));
        Image menu2 = (Image) (findChildByTag(TAG_MENU_ITEM_2));
        menu1.setReactive(false);
        menu1.setVisible(false);
        menu2.setReactive(false);
        menu2.setVisible(false);

        if (mNaviMenu.getItemCount() > 0 && mNaviMenu.getItem(0) != null) {
            menu1.setImageFromResource(mResources, mNaviMenu.getItem(0).getIconId());
            menu1.setReactive(true);
            menu1.setVisible(true);
        }
        if (mNaviMenu.getItemCount() > 1 && mNaviMenu.getItem(1) != null) {
            menu2.setImageFromResource(mResources, mNaviMenu.getItem(1).getIconId());
            menu2.setReactive(true);
            menu2.setVisible(true);
        }
    }

    private void onHitAction(int action, int hittingTag) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "action = " + action + " , hittingTag = " + hittingTag);
        }

        if (action == MotionEvent.ACTION_DOWN) {
            if (mHitActorTag == 0) {
                findChildByTag(mButtons.get(hittingTag).getPressedTag()).setVisible(true);
                mHitActorTag = hittingTag;
            }
            mListener.onTimerStopped();
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mHitActorTag != 0 && mHitActorTag != hittingTag) {
                findChildByTag(mButtons.get(mHitActorTag).getPressedTag()).setVisible(false);
            }
        } else {
            findChildByTag(mButtons.get(hittingTag).getPressedTag()).setVisible(false);
            if (mHitActorTag == hittingTag) {
                if (mHitActorTag == TAG_HOME ||
                    mHitActorTag == TAG_STEREO3D ||
                    mHitActorTag == TAG_NORMAL2D) {
                    mListener.onButtonPressed(mHitActorTag);
                } else if (mHitActorTag == TAG_MENU_ITEM_1) {
                    mListener.onMenuItemSelected(mNaviMenu.getItem(0));
                } else if (mHitActorTag == TAG_MENU_ITEM_2) {
                    mListener.onMenuItemSelected(mNaviMenu.getItem(1));
                }
                if (Media3D.DEBUG) {
                    Log.v(TAG, "onHitAction, " + hittingTag);
                }
            }
            mListener.onTimerStarted();
            mHitActorTag = 0;
        }
    }

    @Override
    protected boolean onHit(Actor hit, int action) {
        if (hit.getTag() == TAG_HOME ||
            hit.getTag() == TAG_STEREO3D || hit.getTag() == TAG_NORMAL2D ||
            hit.getTag() == TAG_MENU_ITEM_1 || hit.getTag() == TAG_MENU_ITEM_2) {
            onHitAction(action, hit.getTag());
            return true;
        } else {
            if (mHitActorTag != 0) {
                findChildByTag(mButtons.get(mHitActorTag).getPressedTag()).setVisible(false);
                mListener.onTimerStarted();
                mHitActorTag = 0;
            }
            return false;
        }
    }

    @Override
    protected boolean onHitNothing() {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onHitNothing");
        }

        if (mHitActorTag != 0) {
            findChildByTag(mButtons.get(mHitActorTag).getPressedTag()).setVisible(false);
            mListener.onTimerStarted();
            mHitActorTag = 0;
        }
        return false;
    }

    public void show3DIcon(boolean is3D) {
        findChildByTag(TAG_STEREO3D).setVisible(is3D);
        findChildByTag(TAG_NORMAL2D).setVisible(!is3D);
    }
}
