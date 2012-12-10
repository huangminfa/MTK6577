package com.mediatek.media3d.portal;

import android.content.res.TypedArray;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import com.mediatek.media3d.FadeInOutAnimation;
import com.mediatek.media3d.Media3D;
import com.mediatek.media3d.Page;
import com.mediatek.media3d.PageHost;
import com.mediatek.media3d.R;
import com.mediatek.media3d.photo.PhotoPage;
import com.mediatek.media3d.video.VideoPage;
import com.mediatek.media3d.weather.WeatherPage;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.weather.Weather;

import java.util.HashMap;

public class PortalPage extends Page {
    private static final String TAG = "PortalPage";
    private final Stage mStage;
    private Container mContainer;
    private final HashMap<Integer, Animation> mAnimations = new HashMap<Integer, Animation>();

    private Text mLoadingText;
    private final Handler mTimeHandler = new Handler();

    private String mNextPageName;
    private static final boolean ENABLE_LOADING_PAGE = true;
    private static final boolean ENABLE_PORTAL_THUMBNAIL = true;

    private static final int FRONT_ACTOR_TAG = 1;
    private Animation mEnteredAni;
    private boolean[] mPageInitialized = new boolean[3];

    private static final int[] DEFAULT_IMAGES = new int[] {
        R.drawable.portal_weather_demo,
        R.drawable.portal_photo_demo,
        R.drawable.portal_video_demo
    };

    private static final String[] CONTAINER_STRING = {
        "weather_container",
        "photo_container",
        "video_container"
    };

    public PortalPage(Stage stage) {
        super(0);
        mStage = stage;
    }

    @Override
    public int getPageType() {
        return PageType.PORTAL;
    }

    private void loadDefaultThumbnailContainer() {
        for (int i = 0; i < DEFAULT_IMAGES.length; i++) {
            Container c = new Container();
            c.setTag(DEFAULT_IMAGES[i]);
           
            if (ENABLE_PORTAL_THUMBNAIL) {
                Image frontActor = Image.createFromResource(getActivity().getResources(), DEFAULT_IMAGES[i]);
                frontActor.setReactive(false);
                frontActor.setPosition(new Point(0, 0, -0.02f));
                frontActor.setName("front");
                frontActor.setAlphaSource(Plane.OPAQUE);
                frontActor.setTag(FRONT_ACTOR_TAG);
                c.add(frontActor);                
            }


            Image backActor = Image.createFromResource(getActivity().getResources(), R.drawable.mainmenu_blank2);
            backActor.setReactive(false);
            backActor.setPosition(new Point(0, 0, +2f));
            backActor.setRotation(new Rotation(0, 180, 0));
            backActor.setAlphaSource(Plane.OPAQUE);
            backActor.setName("back");
            c.add(backActor);
            c.setName(CONTAINER_STRING[i]);
            mContainer.add(c);
        }
    }

    @Override
    public void onAdded(PageHost host) {
        super.onAdded(host);

        mContainer = new Container();

        loadDefaultThumbnailContainer();

        Image background = Image.createFromResource(getActivity().getResources(), R.drawable.portal_background);
        background.setTag(R.drawable.portal_background);
        background.setAlphaSource(Plane.OPAQUE);
        background.setPosition(new Point(400, 240, 100));
        background.setScale(new Scale(1.6f, 1.6f, 1.0f));
        mContainer.add(background);

        Text appTitleText = new Text(getActivity().getResources().getString(R.string.app_name));
        appTitleText.setAnchorPoint(new Point(0, 0));
        appTitleText.setPosition(new Point(-10, -10, 90));
        appTitleText.setScale(new Scale(1.5f, 1.2f));
        mContainer.add(appTitleText);

        final Image surface = Image.createFromResource(getActivity().getResources(), R.drawable.perlin_noise);
        surface.setRenderingHint("reflection", true);
        mContainer.add(surface);
        surface.setPosition(new Point(341, 370, 86));
        surface.setScale(new Scale(11.15f, 10, 10));
        surface.setRotation(new Rotation(273, 0, 0));

        Text[] labels = new Text[] {
            new Text(getActivity().getResources().getString(R.string.enter_weather)),
            new Text(getActivity().getResources().getString(R.string.enter_photo)),
            new Text(getActivity().getResources().getString(R.string.enter_video))
        };

        for (int i = 0; i < labels.length; i++) {
            labels[i].setPosition(new Point((150 + 250 * i) / 800f, 120 / 480f, true));
            mContainer.add(labels[i]);
        }

        mLoadingText = new Text(getActivity().getResources().getString(R.string.loading));
        mLoadingText.setPosition(new Point(0.55f, 0.5f, -600, true));
        mLoadingText.setVisible(false);
        mLoadingText.setTextSize(12);
        mContainer.add(mLoadingText);

        Image loadingImage3 = Image.createFromResource(getActivity().getResources(), R.drawable.spinner_black_48);
        loadingImage3.setTag(R.drawable.spinner_black_48);
        loadingImage3.setPosition(new Point(0.5f, 0.5f, -600, true));
        loadingImage3.setVisible(false);
        loadingImage3.setScale(new Scale(0.3f,0.3f));
        mContainer.add(loadingImage3);

        mStage.add(mContainer);

        /* enter */
        AnimationGroup group = addAnimationList(R.string.enter,
            R.array.portal_enter, R.array.portal_enter_strings);
        group.getAnimationByTag(R.raw.mainmenu_in_top_weather).setTarget(mContainer.findChildByTag(R.drawable.portal_weather_demo));
        group.getAnimationByTag(R.raw.mainmenu_in_top_photo).setTarget(mContainer.findChildByTag(R.drawable.portal_photo_demo));
        group.getAnimationByTag(R.raw.mainmenu_in_top_video).setTarget(mContainer.findChildByTag(R.drawable.portal_video_demo));

        FadeInOutAnimation fadeIn = new FadeInOutAnimation(mContainer, FadeInOutAnimation.FadeType.IN);
        fadeIn.setTag(R.string.fade_in);
        group.add(fadeIn);          // also add fade-in into animation group

        /* subpages popping animations */
        int outAnimationOptions = Animation.DEACTIVATE_TARGET_DURING_ANIMATION;
        AnimationGroup weather_out = addAnimationList(R.string.weather_out,
            R.array.weather_out, R.array.weather_out_strings);
        weather_out.getAnimationByTag(R.raw.mainmenu_out_top_weather).setTarget(mContainer.findChildByTag(R.drawable.portal_weather_demo));
        weather_out.getAnimationByTag(R.raw.mainmenu_out_top_weather).enableOptions(outAnimationOptions);

        AnimationGroup photo_out = addAnimationList(R.string.photo_out,
            R.array.photo_out, R.array.photo_out_strings);
        photo_out.getAnimationByTag(R.raw.mainmenu_out_top_photo).setTarget(mContainer.findChildByTag(R.drawable.portal_photo_demo));
        photo_out.getAnimationByTag(R.raw.mainmenu_out_top_photo).enableOptions(outAnimationOptions);

        AnimationGroup video_out = addAnimationList(R.string.video_out,
            R.array.video_out, R.array.video_out_strings);
        video_out.getAnimationByTag(R.raw.mainmenu_out_top_video).setTarget(mContainer.findChildByTag(R.drawable.portal_video_demo));
        video_out.getAnimationByTag(R.raw.mainmenu_out_top_video).enableOptions(outAnimationOptions);

    }

    private final Animation.Listener mAnimationListener = new Animation.Listener() {
        public void onCompleted(final Animation animation) {
            if (animation == mAnimations.get(R.string.enter)) {
                if (Media3D.DEBUG) {
                    Log.v(TAG, "onCompleted: page = " + PortalPage.this + " , ani = enter");
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setState(IDLE);
                    }
                });
            } else if (animation == mAnimations.get(R.string.weather_out)) {
                if (Media3D.DEBUG) {
                    Log.v(TAG, "onCompleted: page = " + PortalPage.this + " , ani = weather_out");
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (animation.getDirection() == Animation.FORWARD) {
                            if (ENABLE_LOADING_PAGE && !mPageInitialized[0]) {
                                mNextPageName = PageHost.WEATHER;
                                showLoading();
                                Page nextPage = getHost().getPage(mNextPageName);
                                if (nextPage != null) {
                                    ((WeatherPage)nextPage).initialize();
                                }
                                mPageInitialized[0] = true;
                            } else {
                                getHost().enterPage(PageHost.WEATHER);
                            }
                        } else {
                            setState(IDLE);
                        }
                    }
                });
            } else if (animation == mAnimations.get(R.string.photo_out)) {
                if (Media3D.DEBUG) {
                    Log.v(TAG, "onCompleted: page = " + PortalPage.this + " , ani = photo_out");
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (animation.getDirection() == Animation.FORWARD) {
                            if (ENABLE_LOADING_PAGE && !mPageInitialized[1]) {
                                mNextPageName = PageHost.PHOTO;
                                showLoading();
                                Page nextPage = getHost().getPage(mNextPageName);
                                if (nextPage != null) {
                                    ((PhotoPage)nextPage).initialize();
                                }
                                mPageInitialized[1] = true;
                            } else {
                                getHost().enterPage(PageHost.PHOTO);
                            }
                        } else {
                            setState(IDLE);
                        }
                    }
                });
            } else if (animation == mAnimations.get(R.string.video_out)) {
                if (Media3D.DEBUG) {
                    Log.v(TAG, "onCompleted: page = " + PortalPage.this + " , ani = video_out");
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (animation.getDirection() == Animation.FORWARD) {
                            if (ENABLE_LOADING_PAGE && !mPageInitialized[2]) {
                                mNextPageName = PageHost.VIDEO;
                                showLoading();
                                Page nextPage = getHost().getPage(mNextPageName);
                                if (nextPage != null) {
                                    ((VideoPage)nextPage).initialize();
                                }
                                mPageInitialized[2] = true;
                            } else {
                                getHost().enterPage(PageHost.VIDEO);
                            }
                        } else {
                            setState(IDLE);
                        }
                    }
                });
            }
        }
    };

    private final Runnable mTimerRun = new Runnable() {
        public void run() {
            mLoadingText.setVisible(false);

            mContainer.findChildByTag(R.drawable.spinner_black_48).stopAnimations();
            mContainer.findChildByTag(R.drawable.spinner_black_48).setVisible(false);

            String pageName = mNextPageName;
            mNextPageName = null;
            if (getHost() != null) {
                getHost().enterPage(pageName);
            }
        }
    };

    private void showLoading() {
        Rotation start = new Rotation(0, 0, 0);
        Rotation end = new Rotation(0, 0, 360);
        mLoadingText.setVisible(true);
        Actor spinner = mContainer.findChildByTag(R.drawable.spinner_black_48);
        spinner.setVisible(true);
        PropertyAnimation ani = new PropertyAnimation(spinner, "rotation", start, end);
        ani.setDuration(800);
        ani.setLoop(true);
        ani.start();

        mTimeHandler.removeCallbacks(mTimerRun);
        mTimeHandler.postDelayed(mTimerRun, 1200);
    }

    public void cancelLoading() {
        mTimeHandler.removeCallbacks(mTimerRun);
        Animation anim = null;
        if (mNextPageName.equalsIgnoreCase(PageHost.WEATHER)) {
            anim = mAnimations.get(R.string.weather_out);
        } else if (mNextPageName.equalsIgnoreCase(PageHost.PHOTO)) {
            anim = mAnimations.get(R.string.photo_out);
        } else if (mNextPageName.equalsIgnoreCase(PageHost.VIDEO)) {
            anim = mAnimations.get(R.string.video_out);
        }
        if (anim != null) {
            anim.setDirection(Animation.BACKWARD);
            anim.start();
        }
        mLoadingText.setVisible(false);
        mContainer.findChildByTag(R.drawable.spinner_black_48).stopAnimations();
        mContainer.findChildByTag(R.drawable.spinner_black_48).setVisible(false);

        mNextPageName = null;
    }

    public boolean isShowLoading() {
        return (mNextPageName != null);
    }

    private AnimationGroup addAnimationList(int nameResId, int animArrayResId, int cacheNameArrayResId) {
        AnimationGroup group = new AnimationGroup();
        group.setName(getString(nameResId));
        TypedArray animResIds = getActivity().getResources().obtainTypedArray(animArrayResId);
        String[] cacheNameResIds =
            getActivity().getResources().getStringArray(cacheNameArrayResId);

        final int length = animResIds.length();
        for (int i = 0; i < length; i++) {
            int id = animResIds.getResourceId(i, 0);
            Animation animation =
                AnimationLoader.loadAnimation(getActivity(), id, cacheNameResIds[i]);
            animation.setTag(id);
            group.add(animation);
        }
        mAnimations.put(nameResId, group);
        group.addListener(mAnimationListener);

        return group;
    }

    private void stopAllAnimations() {
        for (Animation ani : mAnimations.values()) {
            ani.stop();
        }
    }

    @Override
    protected void onAbortEntering() {
        getContainer().setVisible(false);
        if (mEnteredAni != null) {
            mEnteredAni.reset();
        }
        super.onAbortEntering();
    }

    @Override
    protected void onPageEntering(int transitionType) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onPageEntering");
        }

        if (ENABLE_PORTAL_THUMBNAIL) {
            Container thumbContainer;
            Actor thumbActor;
            Long t1, t2;
            if (Media3D.DEBUG) {
                t1 = System.currentTimeMillis();
            }
            for (int i = 0; i < DEFAULT_IMAGES.length; i++) {
                Long t_start, t_end;
                if (i == 0) {
                    if (Media3D.DEBUG) {
                        t_start = System.currentTimeMillis();
                    }
                    thumbActor = getHost().getThumbnailActor(PageHost.WEATHER);
                    if (Media3D.DEBUG) {
                        t_end = System.currentTimeMillis();
                        Log.v(TAG, "onPageEntering - get WEATHER thumbnail duration = " + (t_end - t_start));
                    }
                    thumbContainer = ((Container) mContainer.findChildByTag(R.drawable.portal_weather_demo));
                    thumbActor.setName("weather_actor");
                } else if (i == 1) {
                    if (Media3D.DEBUG) {
                        t_start = System.currentTimeMillis();
                    }
                    thumbActor = getHost().getThumbnailActor(PageHost.PHOTO);
                    if (Media3D.DEBUG) {
                        t_end = System.currentTimeMillis();
                        Log.v(TAG, "onPageEntering - get PHOTO thumbnail duration = " + (t_end - t_start));
                    }
                    thumbContainer = ((Container) mContainer.findChildByTag(R.drawable.portal_photo_demo));
                    thumbActor.setName("photo_actor");
                } else {
                    if (Media3D.DEBUG) {
                        t_start = System.currentTimeMillis();
                    }
                    thumbActor = getHost().getThumbnailActor(PageHost.VIDEO);
                    if (Media3D.DEBUG) {
                        t_end = System.currentTimeMillis();
                        Log.v(TAG, "onPageEntering - get VIDEO thumbnail duration = " + (t_end - t_start));
                    }
                    thumbContainer = ((Container) mContainer.findChildByTag(R.drawable.portal_video_demo));
                    thumbActor.setName("video_actor");
                }
                
                Actor oldThumb = thumbContainer.findChildByTag(FRONT_ACTOR_TAG);
                if (oldThumb != null) {
                    thumbContainer.remove(oldThumb);
                }
                thumbActor.setReactive(false);
                thumbActor.setPosition(new Point(0, 0, -0.02f));
                thumbActor.setTag(FRONT_ACTOR_TAG);
                thumbContainer.add(thumbActor);
            }
            if (Media3D.DEBUG) {
                t2 = System.currentTimeMillis();
                Log.v(TAG, "onPageEntering - get thumbnail duration = " + (t2 - t1));
            }
        }

        if (transitionType == Page.TransitionType.LAUNCH_PORTAL) {
            mAnimations.get(R.string.enter).start();
        } else {
            Page oldPage = getHost().getOldPage();
            int resId = 0;
            if (getHost().isPageEqual(oldPage, PageHost.WEATHER) == 0) {
                resId = R.string.weather_out;
            } else if (getHost().isPageEqual(oldPage, PageHost.PHOTO) == 0) {
                resId = R.string.photo_out;
            } else if (getHost().isPageEqual(oldPage, PageHost.VIDEO) == 0) {
                resId = R.string.video_out;
            }

            if (resId != 0) {
                Animation ani = mAnimations.get(resId);
                ani.setDirection(Animation.BACKWARD);
                ani.start();
            }
        }
        super.onPageEntering(transitionType);
    }

    @Override
    protected void onPageLeaving(int transitionType) {
        setState(IDLE);
        if (mEnteredAni != null) {
            mEnteredAni.reset();
        }
    }

    @Override
    public void onRemoved() {
        Log.v(TAG, "onRemoved (" + this + ")");
        stopAllAnimations();
        super.onRemoved();
    }

    @Override
    protected Container getContainer() {
        return mContainer;
    }

    @Override
    protected boolean onTouchEvent(MotionEvent event) {
        return false;
    }
    

    public interface PageQueryCallback{
        Page queryWeatherPage();
        Page queryPhotoPage();
        Page queryVideoPage();
    }
    
    private PageQueryCallback mPageQueryCallback = null;

    public void setPageQueryCallback(PageQueryCallback pqc) {
        mPageQueryCallback = pqc;
    }

    @Override
    protected boolean onSingleTapConfirmed(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Point pos = new Point(event.getX(), event.getY());
            if (Media3D.DEBUG) {
                Log.v(TAG, "onSingleTapConfirmed - down, Point = " + pos);
            }

            Actor hitActor = mContainer.hitTest(pos);
            if (Media3D.DEBUG) {
                Log.v(TAG, "hitActor = " + hitActor);
            }

            if (hitActor == null) {
                return false;
            }

            mEnteredAni = null;

            switch (hitActor.getTag()) {
            case R.drawable.portal_weather_demo: {
                if (Media3D.DEBUG) {
                    Log.v(TAG, "Weather page touched");
                }
                if (mPageQueryCallback.queryWeatherPage().isLoaded()) {
                    mEnteredAni = mAnimations.get(R.string.weather_out);
                }
                break;
            }

            case R.drawable.portal_photo_demo: {
                if (Media3D.DEBUG) {
                    Log.v(TAG, "Photo page touched");
                }
                if (mPageQueryCallback.queryPhotoPage().isLoaded()) {
                    mEnteredAni = mAnimations.get(R.string.photo_out);
                }
                break;
            }

            case R.drawable.portal_video_demo: {
                if (Media3D.DEBUG) {
                    Log.v(TAG, "Video page touched");
                }
                if (mPageQueryCallback.queryVideoPage().isLoaded()) {
                    mEnteredAni = mAnimations.get(R.string.video_out);
                }   
                break;
            }

            default:
                if (Media3D.DEBUG) {
                    Log.v(TAG, "No page touched");
                }
                break;
            }

            if (mEnteredAni != null) {
                mEnteredAni.setDirection(Animation.FORWARD);
                mEnteredAni.start();
                return true;
            }
        }

        return false;
    }

    private String getString(int stringId) {
        return getActivity().getString(stringId);
    }

}
