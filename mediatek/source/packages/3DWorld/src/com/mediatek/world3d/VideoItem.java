package com.mediatek.world3d;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.ngin3d.animation.AnimationLoader;

public class VideoItem extends Container implements RotateItem  {

    final private Activity mHost;
    private Text mTitle;
    private AnimationGroup mActionAnimation;
    private AnimationGroup mButterFlyAnimation;
    
    public VideoItem(Activity activity) {
        mHost = activity;
    }

    public VideoItem init() {
        Image mBackground = Image.createFromResource(mHost.getResources(), R.drawable.video_content);
        mBackground.setPosition(new Point(2, -10, 2));
        mBackground.setReactive(false);
        add(mBackground);
        
        Image mFrame = Image.createFromResource(mHost.getResources(), R.drawable.video_frame);
        mFrame.setPosition(new Point(0, 0, -5));
        mFrame.setReactive(false);
        add(mFrame);

        Image mClapper = Image.createFromResource(mHost.getResources(), R.drawable.video_upperclapper);
        mClapper.setAnchorPoint(new Point(0.14f, 0.75f, 0, true));
        mClapper.setPosition(new Point(-108, -123, -1));
        mClapper.setReactive(false);
        add(mClapper);

        Image mLight1 = Image.createFromResource(mHost.getResources(), R.drawable.video_purplelight);
        mLight1.setPosition(new Point(-108, -123, -10));
        mLight1.setReactive(false);
        mLight1.setScale(new Scale(0, 0, 0));
        add(mLight1);

        Image mLight2 = Image.createFromResource(mHost.getResources(), R.drawable.video_greenlight);
        mLight2.setPosition(new Point(105, 50, -10));
        mLight2.setReactive(false);
        mLight2.setScale(new Scale(0, 0, 0));
        add(mLight2);
        
        mActionAnimation = new AnimationGroup();
        mActionAnimation.add(AnimationLoader.loadAnimation(mHost, R.raw.video_purplelight).setTarget(mLight1));
        mActionAnimation.add(AnimationLoader.loadAnimation(mHost, R.raw.video_greenlight).setTarget(mLight2));
        mActionAnimation.add(AnimationLoader.loadAnimation(mHost, R.raw.video_action).setTarget(mClapper));
        mActionAnimation.setName("video_action");
        mActionAnimation.addListener(mAnimationListener);

        mTitle = new Text(mHost.getResources().getString(R.string.video_text));
        mTitle.setPosition(new Point(0, 130, 0));
        mTitle.setReactive(false);
        mTitle.setAlphaSource(Plane.FROM_TEXEL_VERTEX);
        mTitle.setOpacity(0);
        add(mTitle);

        setupButterFly();
        return this;
    }

    private void setupButterFly() {
        Container butterFly = new Container();
        butterFly.setReactive(false);
        butterFly.setVisible(false);

        Image rightWing = Image.createFromResource(mHost.getResources(), R.drawable.right);
        rightWing.setAnchorPoint(new Point(0f, 0.5f, 0f, true));
        rightWing.setReactive(false);
        butterFly.add(rightWing);

        Image leftWing = Image.createFromResource(mHost.getResources(), R.drawable.left);
        leftWing.setAnchorPoint(new Point(1f, 0.5f, 0f, true));
        leftWing.setReactive(false);
        butterFly.add(leftWing);

        add(butterFly);

        mButterFlyAnimation = new AnimationGroup();
        mButterFlyAnimation.add(AnimationLoader.loadAnimation(mHost, R.raw.butterfly_curve).setTarget(butterFly));
        mButterFlyAnimation.add(AnimationLoader.loadAnimation(mHost, R.raw.butterfly_rightwing).setTarget(rightWing));
        mButterFlyAnimation.add(AnimationLoader.loadAnimation(mHost, R.raw.butterfly_leftwing).setTarget(leftWing));
    }

    public void start() {
        mActionAnimation.start();
        mButterFlyAnimation.start();
    }

    private void stop() {
        if (mActionAnimation.isStarted()){
            mActionAnimation.complete();
        }

        if (mButterFlyAnimation.isStarted()) {
            mButterFlyAnimation.complete();
        }
    }

    public Actor getTitle() {
        return mTitle;
    }

    public void onIdle() {}
    public void onRotate() {}

    public void onFocus() {
        start();
    }
    
    public void onDefocus() {
        stop();
    }

    public void onClick(Point point) {
        Intent intent = new Intent("android.media.action.VIDEO_CAPTURE_3D");
        try  {
            Log.v("World3D", "Sending intent : " + intent);
            mHost.startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            Log.v("World3D", "exception :" + e);
        }
    }

    private final Animation.Listener mAnimationListener = new Animation.Listener() {
        public void onStarted(Animation animation) {}
        public void onPaused(Animation animation) {}
        public void onCompleted(final Animation animation) {
            if (animation.getName().equalsIgnoreCase("video_action")) {
                mButterFlyAnimation.start();
            }
        }
    };
}