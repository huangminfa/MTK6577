package com.mediatek.world3d;

import android.app.Activity;
import android.util.Log;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.animation.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

public class RotationManager {
    final private Activity mHost;
    final private Stage mStage;
    private Container mRoot;
    private ArrayList<Actor> mActors;
    final private Hashtable<String, AnimationGroup>  mAnimationMap = new Hashtable<String, AnimationGroup>();
    final private LinkedList<Actor>  mFocuses = new LinkedList<Actor>();
    final private LinkedList<AnimationGroup>  mRotationAnimation = new LinkedList<AnimationGroup>();

    private static double RADIUS = 550;
    private static double FLOAT_RADIUS = 575;
    private static double ANGLE_RADIAN = 1.0472;
    private static Point[] POS = {
            new Point(0.0f, 0.0f, (float)-RADIUS, false),
            new Point(-(float)(Math.sin(ANGLE_RADIAN) * RADIUS), 0.0f, (float)(Math.cos(ANGLE_RADIAN) * RADIUS), false),
            new Point((float)(Math.sin(ANGLE_RADIAN) * RADIUS), 0.0f, (float)(Math.cos(ANGLE_RADIAN) * RADIUS), false) };

    private static Point[] FLOAT_POS = {
            new Point(0.0f, 0.0f, -(float) FLOAT_RADIUS, false),
            new Point(-(float)(Math.sin(ANGLE_RADIAN) * FLOAT_RADIUS), 0.0f, (float)(Math.cos(ANGLE_RADIAN) * FLOAT_RADIUS), false),
            new Point((float)(Math.sin(ANGLE_RADIAN) * FLOAT_RADIUS), 0.0f, (float)(Math.cos(ANGLE_RADIAN) * FLOAT_RADIUS), false) };

    private static Scale[] SCALE = {
        new Scale(1.0f, 1.0f, 1.0f),    // Front
        new Scale(0.7f, 0.7f, 1.0f),    // Back
        new Scale(0.7f, 0.7f, 1.0f) };  // Back

    public RotationManager(Activity activity, Stage stage) {
        mHost = activity;
        mStage = stage;
    }

    public void init() {
        mActors = new ArrayList<Actor>();
        mActors.add(new CameraItem(mHost).init());
        mActors.add(new GalleryItem(mHost).init());
        mActors.add(new VideoItem(mHost).init());

        mRoot = new Container();
        mRoot.setPosition(new Point(0.5f, 0.5f, 0, true));
        for (int i = 0; i < mActors.size(); ++i) {
            mRoot.add(mActors.get(i));
            mFocuses.add(mActors.get(i));
        }
        mStage.add(mRoot);

        setupPositionAndScale();

        AnimationGroup animation = generateYRotationAnimation("rotate120", 0, 120, 500, Mode.LINEAR);
        animation.add(new PropertyAnimation(((RotateItem)mActors.get(0)).getTitle(), "opacity", 255, 0).setDuration(500));
        animation.add(new PropertyAnimation(((RotateItem)mActors.get(2)).getTitle(), "opacity", 0, 255).setDuration(500));
        animation.add(new PropertyAnimation(mActors.get(0), "scale", SCALE[0], SCALE[2]).setDuration(500));
        animation.add(new PropertyAnimation(mActors.get(2), "scale", SCALE[2], SCALE[0]).setDuration(500));

        mRotationAnimation.add(animation);

        animation = generateYRotationAnimation("rotate240", 120, 240, 500, Mode.LINEAR);
        animation.add(new PropertyAnimation(((RotateItem)mActors.get(2)).getTitle(), "opacity", 255, 0).setDuration(500));
        animation.add(new PropertyAnimation(((RotateItem)mActors.get(1)).getTitle(), "opacity", 0, 255).setDuration(500));
        animation.add(new PropertyAnimation(mActors.get(2), "scale", SCALE[0], SCALE[1]).setDuration(500));
        animation.add(new PropertyAnimation(mActors.get(1), "scale", SCALE[1], SCALE[0]).setDuration(500));
        mRotationAnimation.add(animation);

        animation = generateYRotationAnimation("rotate360", 240, 360, 500, Mode.LINEAR);
        animation.add(new PropertyAnimation(((RotateItem)mActors.get(1)).getTitle(), "opacity", 255, 0).setDuration(500));
        animation.add(new PropertyAnimation(((RotateItem)mActors.get(0)).getTitle(), "opacity", 0, 255).setDuration(500));
        animation.add(new PropertyAnimation(mActors.get(1), "scale", SCALE[0], SCALE[1]).setDuration(500));
        animation.add(new PropertyAnimation(mActors.get(0), "scale", SCALE[1], SCALE[0]).setDuration(500));
        mRotationAnimation.add(animation);

        setupTitleAnimation();
        setupFloatingAnimation();
    }

    private void setupPositionAndScale() {
        for (int i = 0; i < mActors.size(); ++i) {
            mActors.get(i).setPosition(POS[i]);
            mActors.get(i).setScale(SCALE[i]);
        }
    }

    public boolean hit(Actor hit, Point point) {
        if (hit == null) {
            return false;
        }
        int index = mActors.indexOf(hit);
        if (index != -1) {
            if (mActors.get(index) == mFocuses.peek()) {
                ((RotateItem)mActors.get(index)).onClick(point);
            }
        }
        return true;
    }

    public BasicAnimation getAnim(String name) {
        Log.v("World3D", "getAnim : " + name);
        return mAnimationMap.get(name);
    }

    public void startTitle() {
        getAnim("title").start();
    }

    public RotateItem getFocus() {
        return (RotateItem)mFocuses.peek();
    }

    public Animation updateFocusAndGetRotationAnimation(boolean isLeft) {
        AnimationGroup play = isLeft ? mRotationAnimation.poll() : mRotationAnimation.pollLast();
        if (isLeft) {
            mFocuses.addFirst(mFocuses.pollLast());
            mRotationAnimation.add(play);
            play.setDirection(Animation.FORWARD);
        } else {
            mFocuses.add(mFocuses.poll());
            mRotationAnimation.addFirst(play);
            play.setDirection(Animation.BACKWARD);
        }
        return play;
    }

    public boolean rotate(boolean isLeft) {
        getFocus().onDefocus();
        updateFocusAndGetRotationAnimation(isLeft).start();
        return true;
    }

    private AnimationGroup generateYRotationAnimation(String name, int fromAngle, int toAngle, int duration, Mode mode) {
        AnimationGroup rotate = new AnimationGroup();
        Rotation fromRot = new Rotation(0, fromAngle, 0);
        Rotation fromReverseRot = new Rotation(0, -fromAngle, 0);
        Rotation toRot = new Rotation(0, toAngle, 0);
        Rotation toReverseRot = new Rotation(0, -toAngle, 0);

        for (int i = 0; i < mActors.size(); ++i) {
            Log.v("World3D", "Object :" + mActors.get(i) + " from : " + fromReverseRot + ", to :" + toReverseRot);
            rotate.add(new PropertyAnimation(mActors.get(i), "rotation", fromReverseRot, toReverseRot).setDuration(duration).setMode(mode));
        }

        Log.v("World3D", "root from : " + fromRot + ", to :" + toRot);
        rotate.add(new PropertyAnimation(mRoot, "rotation", fromRot, toRot).setDuration(duration));
        rotate.setName(name);
        rotate.addListener(mAnimationListener);
        return rotate;
    }

    private void setupFloatingAnimation() {
        AnimationGroup floating = new AnimationGroup();
        for (int i = 0; i < mActors.size(); ++i) {
            floating.add(new PropertyAnimation(mActors.get(i), "position", POS[i], FLOAT_POS[i]));
        }

        floating.setLoop(true).setAutoReverse(true).setName("floating");
        floating.addListener(mAnimationListener);
        mAnimationMap.put("floating", floating);
    }

    private void setupTitleAnimation() {
        int duration = 1000;
        int rotation = 720;
        AnimationGroup title = new AnimationGroup();
        title.add(new PropertyAnimation(mRoot, "position", new Point(0.5f, 0, 0, true), new Point(0.5f,0.5f,0,true)).setDuration(duration).setMode(Mode.EASE_OUT_EXPO));
        title.add(new PropertyAnimation(mActors.get(0), "rotation", new Rotation(0, 0, 0), new Rotation(0, -rotation, 0)).setDuration(duration).setMode(Mode.EASE_OUT_EXPO));
        title.add(new PropertyAnimation(mActors.get(1), "rotation", new Rotation(0, 0, 0), new Rotation(0, -rotation, 0)).setDuration(duration).setMode(Mode.EASE_OUT_EXPO));
        title.add(new PropertyAnimation(mActors.get(2), "rotation", new Rotation(0, 0, 0), new Rotation(0, -rotation, 0)).setDuration(duration).setMode(Mode.EASE_OUT_EXPO));
        title.add(new PropertyAnimation(mRoot, "rotation", new Rotation(0, 0, 0), new Rotation(0, rotation, 0)).setDuration(duration).setMode(Mode.EASE_OUT_EXPO));
        title.setName("title");
        title.addListener(mAnimationListener);
        mAnimationMap.put("title", title);

    }

    private void handleAnimationComplete(final Animation animation) {
        String name = animation.getName();
        if (name.equalsIgnoreCase("title")) {
            getAnim("floating").start();
        }

        if (name.startsWith("rotate")) {
            Log.v("World3D", "rotate : " + getFocus());
            getFocus().onFocus();
        }
    }

    private final Animation.Listener mAnimationListener = new Animation.Listener() {
        public void onCompleted(final Animation animation) {
            handleAnimationComplete(animation);
        }
    };
}