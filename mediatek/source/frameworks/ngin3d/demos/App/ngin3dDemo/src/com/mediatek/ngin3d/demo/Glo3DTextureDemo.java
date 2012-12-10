package com.mediatek.ngin3d.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.android.StageTextureView;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.demo.R;

/**
 * A demo for usage of Object3D.
 */
public class Glo3DTextureDemo extends Activity {

    Animation mBendGail;
    Animation mBendGentle;
    Animation mBendModerate;

    Animation mBlowGail;
    Animation mBlowGentle;
    Animation mBlowModerate;

    Animation mSheepEat;
    Animation mSheepWalk;
    Animation mSheepSleep;

    Animation mRainFall;
    Animation mStarTwinkle;
    Animation mHeavyCloud;
    Animation mLightCloud;

    Animation mDayToNight;
    Animation mNightToDay;
    Animation mShowHide;

    private StageTextureView mTextureView;
    private Stage mStage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTextureView = new StageTextureView(this);
        setContentView(mTextureView);

        mStage = mTextureView.getStage();
        final Glo3D landscape = Glo3D.createFromAsset("landscape.glo");
        // tree
        final Container tree = new Container();
        final Glo3D tree_bend_gail = Glo3D.createFromAsset("tree_bend_gail.glo");
        final Glo3D tree_bend_gentle = Glo3D.createFromAsset("tree_bend_gentle.glo");
        final Glo3D tree_bend_moderate = Glo3D.createFromAsset("tree_bend_moderate.glo");
        tree.add(tree_bend_gail, tree_bend_gentle, tree_bend_moderate);

        // sheep
        final Container sheep = new Container();
        final Glo3D sheep_walk = Glo3D.createFromAsset("sheep_walk.glo");
        final Glo3D sheep_eat = Glo3D.createFromAsset("sheep_eat.glo");
        final Glo3D sheep_sleep = Glo3D.createFromAsset("sheep_sleep.glo");
        sheep.add(sheep_eat, sheep_walk, sheep_sleep);

        // cloud
        final Container cloud = new Container();
        final Glo3D heavy_clouds_show_hide = Glo3D.createFromAsset("heavy_clouds_show_hide.glo");
        final Glo3D light_clouds_show_hide = Glo3D.createFromAsset("light_clouds_show_hide.glo");
        cloud.add(heavy_clouds_show_hide, light_clouds_show_hide);

        // sunmoon
        final Container sun_moon = new Container();
        final Glo3D sunmoon = Glo3D.createFromAsset("sunmoon.glo");
        final Glo3D sunmoon_day_to_night = Glo3D.createFromAsset("sunmoon_day_to_night.glo");

        final Glo3D sunmoon_night_to_day = Glo3D.createFromAsset("sunmoon_night_to_day.glo");
        final Glo3D sunmoon_show_hide = Glo3D.createFromAsset("sunmoon_show_hide.glo");
        sun_moon.add(sunmoon, sunmoon_day_to_night, sunmoon_night_to_day, sunmoon_show_hide);

        // leaves
        final Container leaves = new Container();
        final Glo3D leaves_blow_gail = Glo3D.createFromAsset("leaves_blow_gail.glo");
        final Glo3D leaves_blow_gentle = Glo3D.createFromAsset("leaves_blow_gentle.glo");
        final Glo3D leaves_blow_moderate = Glo3D.createFromAsset("leaves_blow_moderate.glo");
        leaves.add(leaves_blow_gail, leaves_blow_gentle, leaves_blow_moderate);

        final Glo3D stars_twinkle = Glo3D.createFromAsset("stars_twinkle.glo");
        final Glo3D rain_fall = Glo3D.createFromAsset("rain_fall.glo");


        Container scenario = new Container();
        scenario.add(landscape, tree, sheep, cloud, leaves, sun_moon, stars_twinkle, rain_fall);
        scenario.setPosition(new Point(240, 400, 1085));
        // default anchor of 0.5 shifts model inappropriately
        scenario.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        scenario.setRotation(new Rotation(-10, -30, 0));
        // Scale Y -1 as UI-Perspective is Y-down, but model is Y-up
        scenario.setScale(new Scale(1f, -1f, 1f));

        mStage.add(scenario);

        // Get animations
        mBendGail = tree_bend_gail.getAnimation();
        mBendGentle = tree_bend_gentle.getAnimation();
        mBendModerate = tree_bend_moderate.getAnimation();
        mSheepWalk = sheep_walk.getAnimation();
        mSheepEat = sheep_eat.getAnimation();
        mSheepSleep = sheep_sleep.getAnimation();
        mStarTwinkle = stars_twinkle.getAnimation();
        mHeavyCloud = heavy_clouds_show_hide.getAnimation();
        mLightCloud = light_clouds_show_hide.getAnimation();
        mRainFall = rain_fall.getAnimation();
        mBlowGail = leaves_blow_gail.getAnimation();
        mBlowGentle = leaves_blow_gentle.getAnimation();
        mBlowModerate = leaves_blow_moderate.getAnimation();
        mDayToNight = sunmoon_day_to_night.getAnimation();
        mNightToDay = sunmoon_night_to_day.getAnimation();
        mShowHide = sunmoon_show_hide.getAnimation();


    }

    private void toggleAnimation(Animation aniToStart, Animation... aniToStop) {
        if (aniToStart.isStarted()) {
            aniToStart.stop();
        } else {
            ((BasicAnimation)aniToStart).setLoop(true).start();
            for (Animation ani : aniToStop) {
                ani.stop();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.glo_demo_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId){
        case R.id.eat:
            toggleAnimation(mSheepEat, mSheepWalk, mSheepSleep);
            break;
        case R.id.walk:
            toggleAnimation(mSheepWalk, mSheepEat, mSheepSleep);
            break;
        case R.id.sleep:
            toggleAnimation(mSheepSleep, mSheepWalk, mSheepEat);
            break;
        case R.id.bend_gail:
            toggleAnimation(mBendGail, mBendGentle, mBendModerate);
            break;
        case R.id.bend_gentle:
            toggleAnimation(mBendGentle, mBendGail, mBendModerate);
            break;
        case R.id.bend_moderate:
            toggleAnimation(mBendModerate, mBendGentle, mBendGail);
            break;
        case R.id.blow_gail:
            toggleAnimation(mBendGail, mBendModerate, mBendGentle);
            break;
        case R.id.blow_gentle:
            toggleAnimation(mBendGentle, mBendGail, mBendModerate);
            break;
        case R.id.blow_moderate:
            toggleAnimation(mBendModerate, mBendGentle, mBendGail);
            break;
        case R.id.stars_twinkle:
            toggleAnimation(mStarTwinkle);
            break;
        case R.id.rain_fall:
            toggleAnimation(mRainFall);
            break;
        case R.id.heavy_clouds_show_hide:
            toggleAnimation(mHeavyCloud, mLightCloud);
            break;
        case R.id.light_clouds_show_hide:
            toggleAnimation(mLightCloud, mHeavyCloud);
            break;
        case R.id.day_to_night:
            mDayToNight.start();
            break;
        case R.id.night_to_day:
            mNightToDay.start();
            break;
        case R.id.show_hide:
            mShowHide.start();
            break;
        default:
            return false;
        }
        return true;
    }
}
