package com.mediatek.world3d;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.animation.*;

public class CameraItem extends Container implements RotateItem {

    final private Activity mHost;
    private Text mTitle;
    private AnimationGroup mPictureTaker;

    public CameraItem(Activity activity) {
        mHost = activity;
    }

    public CameraItem init() {
        Image body = Image.createFromResource(mHost.getResources(), R.drawable.camera_body);
        body.setPosition(new Point(0, 0, -1));
        body.setReactive(false);
        add(body);

        Image aperture = Image.createFromResource(mHost.getResources(), R.drawable.camera_aperture);
        aperture.setPosition(new Point(27, -12, -5));
        aperture.setReactive(false);
        add(aperture);

        Image lens = Image.createFromResource(mHost.getResources(), R.drawable.camera_lens);
        lens.setPosition(new Point(27, -12, -2));
        lens.setReactive(false);
        add(lens);

        Image lenszoom = Image.createFromResource(mHost.getResources(), R.drawable.camera_lenszoom);
        lenszoom.setPosition(new Point(27, -12, -2));
        lenszoom.setReactive(false);
        lenszoom.setVisible(false);
        add(lenszoom);

        Image strobe = Image.createFromResource(mHost.getResources(), R.drawable.camera_strobe);
        strobe.setPosition(new Point(0, 20, 2));
        strobe.setReactive(false);
        add(strobe);

        Image radiance = Image.createFromResource(mHost.getResources(), R.drawable.camera_radiance);
        radiance.setPosition(new Point(25, -13, -10));
        radiance.setReactive(false);
        radiance.setScale(new Scale(0, 0, 0));
        add(radiance);

        Image shine = Image.createFromResource(mHost.getResources(), R.drawable.camera_shine);
        shine.setPosition(new Point(25, -13, -20));
        shine.setReactive(false);
        shine.setScale(new Scale(0, 0, 0));
        add(shine);

        Image light = Image.createFromResource(mHost.getResources(), R.drawable.camera_light);
        light.setPosition(new Point(83, -85, -30));
        light.setReactive(false);
        light.setScale(new Scale(0, 0, 0));
        add(light);

        mTitle = new Text(mHost.getResources().getString(R.string.camera_text));
        mTitle.setPosition(new Point(0, 130, 0));
        mTitle.setReactive(false);
        mTitle.setAlphaSource(Plane.FROM_TEXEL_VERTEX);
        mTitle.setOpacity(255);
        add(mTitle);
        setName("camera");

        mPictureTaker = new AnimationGroup();
        mPictureTaker.add(AnimationLoader.loadAnimation(mHost, R.raw.camera_aperture).setTarget(aperture));
        mPictureTaker.add(AnimationLoader.loadAnimation(mHost, R.raw.camera_strobe).setTarget(strobe));
        mPictureTaker.add(AnimationLoader.loadAnimation(mHost, R.raw.camera_radiance).setTarget(radiance));
        mPictureTaker.add(AnimationLoader.loadAnimation(mHost, R.raw.camera_shine).setTarget(shine));
        mPictureTaker.add(AnimationLoader.loadAnimation(mHost, R.raw.camera_light).setTarget(light));
        mPictureTaker.add(AnimationLoader.loadAnimation(mHost, R.raw.camera_lenszoom).
            setTarget(lenszoom).enableOptions(Animation.SHOW_TARGET_DURING_ANIMATION));
        mPictureTaker.setName("PictureTaker");
        return this;
    }

    private void start() {
        mPictureTaker.start();
    }

    private void stop() {
        if (mPictureTaker.isStarted()) {
            mPictureTaker.complete();
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
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE_3D");

        try  {
            Log.v("World3D", "Sending intent : " + intent);
            mHost.startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            Log.v("World3D", "exception :" + e);
        }
    }
}