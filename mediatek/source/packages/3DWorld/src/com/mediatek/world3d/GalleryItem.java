package com.mediatek.world3d;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.animation.BasicAnimation;

import java.util.ArrayList;

public class GalleryItem extends Container implements RotateItem {

    final private Activity mHost;
    private Text mTitle;
    private Image mIcon;
    private Container mHitTestArea = new Container();

    public GalleryItem(Activity activity) {
        mHost = activity;
    }

    private ArrayList<Animation> mMarch;
    private ArrayList<Image> mMarchTargets;
    private Animation mLightAnimation;
    private static int[] res = {
            R.drawable.photo_01,
            R.drawable.photo_02,
            R.drawable.photo_03,
            R.drawable.photo_01,
            R.drawable.photo_02,
            R.drawable.photo_03,
            R.drawable.gallery_light };
    
    public GalleryItem init() {
        setupTargets();
        setupAnimation();
        
        mIcon = Image.createFromResource(mHost.getResources(), R.drawable.ic_gallery);
        mIcon.setReactive(false);
        mHitTestArea.add(mIcon);
        mHitTestArea.setReactive(false);
        add(mHitTestArea);
        
        mTitle = new Text(mHost.getResources().getString(R.string.gallery_text));
        mTitle.setPosition(new Point(0, 130, 0));
        mTitle.setReactive(false);
        mTitle.setAlphaSource(Plane.FROM_TEXEL_VERTEX);
        mTitle.setOpacity(0);
        add(mTitle);
        return this;
    }
    
    private void setupTargets() {
        mMarchTargets = new ArrayList<Image>();

        Point position = new Point(0.5f, 0.5f, 0, true);
        for (int i = 0; i < res.length; ++i) {
            Image target = Image.createFromResource(mHost.getResources(), res[i]);
            target.setPosition(position);
            target.setVisible(false);
            target.setDoubleSided(true);
            target.setReactive(false);
            mMarchTargets.add(target);
            add(target);
        }
    }
    
    private void setupAnimation() {
        mMarch = new ArrayList<Animation>();
        
        BasicAnimation flight = AnimationLoader.loadAnimation(mHost, R.raw.gallery_flightcurve);
        flight.enableOptions(Animation.SHOW_TARGET_DURING_ANIMATION);
        mMarch.add(flight.setTarget(mMarchTargets.get(0)));
        mMarch.add(flight.clone().setTarget(mMarchTargets.get(1)));
        mMarch.add(flight.clone().setTarget(mMarchTargets.get(2)));
        mMarch.add(flight.clone().setTarget(mMarchTargets.get(3)));
        mMarch.add(flight.clone().setTarget(mMarchTargets.get(4)));
        mMarch.add(flight.clone().setTarget(mMarchTargets.get(5)));
        mLightAnimation = AnimationLoader.loadAnimation(mHost, R.raw.gallery_light).setTarget(mMarchTargets.get(6));
    }

    public Actor getTitle() {
        return mTitle;
    }

    public void onRotate() {}
    public void onIdle() {}

    public void onFocus() {
        start(0);
    }
    
    public void onDefocus() {
        stop();
    }

    public void onClick(Point point) {
        Point hit = new Point(point);
        mHitTestArea.setReactive(true);
        if (mHitTestArea.hitTest(hit) == mHitTestArea) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName("com.android.gallery3d", "com.android.gallery3d.app.Gallery"));
            intent.putExtra("onlyStereoMedia", true);

            try  {
                Log.v("World3D", "Sending intent : " + intent);
                mHost.startActivityForResult(intent, 0);
            } catch (ActivityNotFoundException e) {
                Log.v("World3D", "exception :" + e);
            }
        }
        mHitTestArea.setReactive(false);
    }
    
    static private int START_ANIMATION = 1;
    private void start(int fromIndex) {
        if (fromIndex >= mMarch.size()) {
            return;
        }

        if (fromIndex == 0) {
            mLightAnimation.start();
        }

        mMarch.get(fromIndex).start();
        Message msg = Message.obtain();
        msg.what = START_ANIMATION;
        msg.arg1 = fromIndex + 1;
        mHandler.sendMessageDelayed(msg, 250);
    }

    private void stop() {
        for (int i = 0; i < mMarch.size(); ++i) {
            if (mMarch.get(i).isStarted()) {
                mMarch.get(i).complete();
            }
        }

        if (mLightAnimation.isStarted()) {
            mLightAnimation.complete();
        }
    }

    TinyHandler mHandler = new TinyHandler();
    private class TinyHandler extends Handler {
        public void handleMessage(Message msg) {
            if (msg.what == START_ANIMATION) {
                start(msg.arg1);
                return;
            }
            super.handleMessage(msg);
        }
    }
}