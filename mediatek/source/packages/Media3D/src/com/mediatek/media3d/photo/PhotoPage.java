package com.mediatek.media3d.photo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import com.mediatek.media3d.FadeInOutAnimation;
import com.mediatek.media3d.LogUtil;
import com.mediatek.media3d.Main;
import com.mediatek.media3d.Media3D;
import com.mediatek.media3d.MediaDbItemSet;
import com.mediatek.media3d.MediaItem;
import com.mediatek.media3d.MediaItemSet;
import com.mediatek.media3d.MediaSourceListener;
import com.mediatek.media3d.MediaUtils;
import com.mediatek.media3d.NavigationBarMenu;
import com.mediatek.media3d.NavigationBarMenuItem;
import com.mediatek.media3d.Page;
import com.mediatek.media3d.PageDragHelper;
import com.mediatek.media3d.PageHost;
import com.mediatek.media3d.PageTransitionDetector;
import com.mediatek.media3d.R;
import com.mediatek.media3d.ResourceItemSet;
import com.mediatek.media3d.Setting;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Plane;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Text;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.animation.Timeline;
import com.mediatek.ngin3d.presentation.BitmapGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PhotoPage extends Page implements PageTransitionDetector.ActionListener, MediaSourceListener {
    private static final String TAG = "PhotoPage";
    private static final int PHOTO_PER_PAGE = 4;
    private final Stage mStage;
    private Container mContainer;

    private Image mBackground;
    private Text mNoPhotoText;
    private Text mPageIndexText;

    private int mPhotoPageIndex;
    private int mPhotoOffsetIndex;

    private String mBucketId;
    private MediaItemSet mMediaItemSet;

    private final PageTransitionDetector mScrollDetector = new PageTransitionDetector(this);
    private final HashMap<Integer, AnimationGroup> mAnimationLists = new HashMap<Integer, AnimationGroup>();
    private AnimationGroup mRunningAnimation;
    private int mRunningAniId;

    private static final float ANIM_SPEED_NORMAL = 1.0f;
    private static final float ANIM_SPEED_FASTER = 1.4f;

    private Actor[] mActorBefore = new Container[PHOTO_PER_PAGE];
    private Actor[] mActorNow = new Container[PHOTO_PER_PAGE];
    private Actor[] mActorNext = new Container[PHOTO_PER_PAGE];


    private Actor mIconLast;
    private Actor mIconNext;

    private boolean mIsMediaItemSetModified;

    private static Bitmap mBlankBitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.RGB_565);

    private static final int PHOTO_TAG = 1;
    private static final String KEY_PHOTO_BUCKET_ID = "photo_bucket_id";

    public PhotoPage(Stage stage) {
        super(Page.SUPPORT_FLING);
        mStage = stage;
    }

    @Override
    public void onAdded(PageHost host) {
        super.onAdded(host);
    }

    @Override
    public void onLoad() {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onLoad");
        }
        initPhoto();
        mBucketId = getBucketId();
        prepareMediaItemSet(mBucketId);

        mPhotoPageIndex = 0;
        mPhotoOffsetIndex = 0;
        ((Main)getActivity()).addMediaSourceListener(this);
        super.onLoad();
    }

    private boolean mInitialized;

    public void initialize() {
        if (mInitialized) {
            return;
        }
        initPhotoActorContent();
        mInitialized = true;
    }

    private int getTransition(final int transitionType, final String action) {
        final boolean isEnter = action.equalsIgnoreCase("enter");
        switch (transitionType) {
            case TransitionType.INNER_TO_LEFT:
                return isEnter ? R.string.right_enter : R.string.left_exit;
            case TransitionType.INNER_TO_RIGHT:
                return isEnter ? R.string.left_enter : R.string.right_exit;
            case TransitionType.PORTAL_INNER:
                return isEnter ? R.string.next_enter : R.string.next_exit;
            default:
                return R.string.next_enter;
        }
    }

    private static Dimension sBackgroundDimension;
    private static int sThumbnailWidth;
    private static int sThumbnailHeight;
    private static int sTempBackgroundWidth;
    private static int sTempBackgroundHeight;
    private static int sPortalThumbnailWidth;
    private static int sPortalThumbnailHeight;
    private static float sMaxDragRange;
    private static float sDragThreshold;

    public static void loadConfiguration(Setting setting) {
        sBackgroundDimension = new Dimension(
                setting.getInt(Setting.PHOTO_BACKGROUND_WIDTH),
                setting.getInt(Setting.PHOTO_BACKGROUND_HEIGHT));

        sTempBackgroundWidth = setting.getInt(Setting.PHOTO_TEMP_BACKGROUND_WIDTH);
        sTempBackgroundHeight = setting.getInt(Setting.PHOTO_TEMP_BACKGROUND_HEIGHT);

        sPortalThumbnailWidth = setting.getInt(Setting.PHOTO_PORTAL_THUMBNAIL_WIDTH);
        sPortalThumbnailHeight = setting.getInt(Setting.PHOTO_PORTAL_THUMBNAIL_HEIGHT);

        sThumbnailWidth = setting.getInt(Setting.PHOTO_THUMBNAIL_WIDTH);
        sThumbnailHeight = setting.getInt(Setting.PHOTO_THUMBNAIL_HEIGHT);

        sMaxDragRange = setting.getFloat(Setting.PHOTO_DRAGGING_MAX);
        sDragThreshold = setting.getFloat(Setting.PHOTO_DRAGGING_THRESHOLD);
    }

    @Override
    protected void onPageEntering(int transitionType) {
        initialize();
        if (isNoPhoto()) {
            mNoPhotoText.setVisible(true);
            setState(IDLE);
        } else {
            showPhoto(getTransition(transitionType, "enter"));
        }
        super.onPageEntering(transitionType);
        mScrollDetector.reset();
    }

    protected void prepareDragLeaving() {
        setActorVisibility(mActorBefore, false);
        setActorVisibility(mActorNext, false);
        stopAllAnimations();
        updateBackgroundText();
        mPageIndexText.setVisible(false);
        getIconLast().setVisible(false);
        getIconNext().setVisible(false);
        releaseLoaders(false);
        super.prepareDragLeaving();
    }

    protected Animation prepareDragLeavingAnimation(int transitionType) {
        if (transitionType == TransitionType.PORTAL_INNER) {
            return mAnimationLists.get(R.string.fade_out);
        }

        AnimationGroup photoLeaving = preparePhotoAnimation(getTransition(transitionType, "leave"));
        AnimationGroup backgroundLeaving = mAnimationLists.get(R.string.swap_out);
        backgroundLeaving.getAnimation(0).setTarget(mBackground);
        return photoLeaving.add(backgroundLeaving);
    }

    protected void revertDragLeaving() {
        startPhotoAnimation(R.string.floating);
        showBackground(R.string.swap_in);
        updateBackgroundText();
        updateIndicators();
        updatePageIndexText();
        super.revertDragLeaving();
    }

    @Override
    protected void onPageLeaving(int transitionType) {
        if (isDragLeaving()) {
            return;
        }
        setActorVisibility(mActorBefore, false);
        setActorVisibility(mActorNext, false);
        stopAllAnimations();

        if (isNoPhoto()) {
            mNoPhotoText.setVisible(false);
            setState(IDLE);
        } else {
            if (transitionType == TransitionType.PORTAL_INNER) {
                mAnimationLists.get(R.string.fade_out).start();
            } else {
                hidePhoto(getTransition(transitionType, "leave"));
            }
        }
        releaseLoaders(false);
        mScrollDetector.reset();
    }

    @Override
    public void onRemoved() {
        Log.v(TAG, "onRemoved (" + this + ")");
        ((Main)getActivity()).removeMediaSourceListener(this);
        destroyMediaItemSet();
        super.onRemoved();
    }

    @Override
    protected Container getContainer() {
        return mContainer;
    }

    @Override
    public void onFling(int direction) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onFling: " + direction);
        }
        if (getTotalPhotoPage() <= 1) {
            return;     // ignore fling event if no item on screen, or only 1 page.
        }

        mScrollDetector.onFling(direction);
    }

    private void preparePhotoAnimations() {
        FadeInOutAnimation fadeInAni = new FadeInOutAnimation(getContainer(), FadeInOutAnimation.FadeType.IN);
        FadeInOutAnimation fadeOutAni = new FadeInOutAnimation(getContainer(), FadeInOutAnimation.FadeType.OUT);

        final int enterMarkerDuration = 240;
        // left enter
        addAnimationList(R.string.left_enter,
            R.array.photo_left_enter_id, R.array.photo_left_enter_strings)
            .add(fadeInAni).addMarkerAtTime("enter", enterMarkerDuration);

        // left exit
        addAnimationList(R.string.left_exit,
            R.array.photo_left_exit_id, R.array.photo_left_exit_strings)
            .enableOptions(Animation.HIDE_TARGET_ON_COMPLETED);

        // right enter
        addAnimationList(R.string.right_enter,
            R.array.photo_right_enter_id, R.array.photo_right_enter_strings)
            .add(fadeInAni).addMarkerAtTime("enter", enterMarkerDuration);

        // right exit
        addAnimationList(R.string.right_exit,
            R.array.photo_right_exit_id, R.array.photo_right_exit_strings)
            .enableOptions(Animation.HIDE_TARGET_ON_COMPLETED);

        // next enter
        addAnimationList(R.string.next_enter,
            R.array.photo_next_enter_id, R.array.photo_next_enter_strings)
            .add(fadeInAni);

        // next exit
        addAnimationList(R.string.next_exit,
            R.array.photo_next_exit_id, R.array.photo_next_exit_strings)
            .add(fadeOutAni)
            .enableOptions(Animation.HIDE_TARGET_ON_COMPLETED);

        // last enter
        addAnimationList(R.string.last_enter,
            R.array.photo_last_enter_id, R.array.photo_last_enter_strings)
            .add(fadeInAni);

        // last exit
        addAnimationList(R.string.last_exit,
            R.array.photo_last_exit_id, R.array.photo_last_exit_strings)
            .add(fadeOutAni)
            .enableOptions(Animation.HIDE_TARGET_ON_COMPLETED);

        // swap in
        int start_value = (int)(255* 0.4f);
        Color c1 = new Color(start_value, start_value, start_value, 255);
        Color c2 = new Color(255, 255, 255, 255);
        PropertyAnimation bgFadeInAni = new PropertyAnimation(mBackground, "color", c1, c2);
        bgFadeInAni.setDuration(100).setMode(Mode.LINEAR).enableOptions(Animation.SHOW_TARGET_ON_STARTED);

        addAnimationList(R.string.swap_in,
            R.array.photo_swap_enter_id, R.array.photo_swap_enter_strings)
            .add(bgFadeInAni);

        // swap out
        addAnimationList(R.string.swap_out,
            R.array.photo_swap_exit_id, R.array.photo_swap_exit_strings);

        // floating
        addAnimationList(R.string.floating,
            R.array.photo_floating_id, R.array.photo_floating_strings)
            .setLoop(true).setAutoReverse(true);

        // next exit - enter
        addAnimationList(R.string.next_exit_enter,
            R.array.photo_next_exit_enter_id, R.array.photo_next_exit_enter_strings);

        // last exit - enter
        addAnimationList(R.string.last_exit_enter,
            R.array.photo_last_exit_enter_id, R.array.photo_last_exit_enter_strings);

        AnimationGroup group = new AnimationGroup();
        group.setName(getString(R.string.fade_out));
        group.add(fadeOutAni);
        group.addListener(mAnimationCompletedHandler);
        mAnimationLists.put(R.string.fade_out, group);
    }

    final public int getPageIndex() {
        return mPhotoPageIndex;
    }

    final public int getNextPageIndex() {
        if (isLessOrEqualOnePage()) {
            return 0;
        }
        return (getPageIndex() + 1) < getTotalPhotoPage() ? (getPageIndex() + 1) : 0;
    }

    final public int getPrevPageIndex() {
        if (isLessOrEqualOnePage()) {
            return 0;
        }
        return (getPageIndex() > 0) ? (getPageIndex() - 1) : (getTotalPhotoPage() - 1);
    }

    private void initPhoto() {
        mContainer = new Container();
        mContainer.setVisible(false);

        int index = 1;
        initPhotoActor(mActorBefore, index);
        index += mActorBefore.length;
        initPhotoActor(mActorNow, index);
        index += mActorNow.length;
        initPhotoActor(mActorNext, index);

        mBackground = Image.createEmptyImage();
        mBackground.setVisible(false);
        mBackground.setAlphaSource(Plane.OPAQUE);

        mContainer.add(mBackground);

        mNoPhotoText = new Text(getActivity().getResources().getString(R.string.no_photo_text));
        mNoPhotoText.setPosition(new Point(0.5f, 0.5f, true));
        mNoPhotoText.setBackgroundColor(Color.BLACK);
        mNoPhotoText.setVisible(false);
        mContainer.add(mNoPhotoText);

        mPageIndexText = new Text();
        mPageIndexText.setPosition(new Point(0.875f, 0.8958f, true));
        mPageIndexText.setVisible(false);
        mContainer.add(mPageIndexText);

        mStage.add(mContainer);
        preparePhotoAnimations();
    }

    private void initPhotoActor(Actor[] actors, int index) {
        int idx = index;
        for (int i = 0; i < actors.length; i++) {
            actors[i] = new Container();

            Image frame = Image.createFromResource(getActivity().getResources(), R.drawable.photo_frame);
            frame.setReactive(false);
            frame.setPosition(new Point(0, 0, -0.02f));
            ((Container) actors[i]).add(frame);

            Image defaultThumbnail = Image.createFromResource(getActivity().getResources(), R.drawable.photo_frame_background);
            defaultThumbnail.setReactive(false);
            defaultThumbnail.setPosition(new Point(0, 0, +0.1f));
            ((Container) actors[i]).add(defaultThumbnail);

            actors[i].setVisible(false);
            actors[i].setTag(idx++);
            getContainer().add(actors[i]);
        }
    }

    private void removeChildByTag(Container container, int tag) {
        if (container != null) {
            final Actor child = container.findChildByTag(tag);
            if (child != null) {
                container.remove(child);
            }
        }
    }

    private void resetActor(int pageIndex, Actor[] actors) {
        int totalCount = mMediaItemSet.getItemCount();
        for (int i = 0; i < actors.length; i++) {
            Container c = (Container)actors[i];
            removeChildByTag(c, PHOTO_TAG);
            if (pageIndex * PHOTO_PER_PAGE + i < totalCount) {
                Container image = new Container();
                image.setReactive(false);
                image.setPosition(new Point(0, 0, +0.08f));
                image.setTag(PHOTO_TAG);
                c.add(image);
            }
        }
    }

    private void removeActorContent(Actor[] actors) {
        for (Actor actor : actors) {
            removeChildByTag((Container)actor, PHOTO_TAG);
        }
    }

    private void initPhotoActorContent() {
        if (getTotalPhotoPage() > 0) {
            resetActor(getPageIndex(), mActorNow);
            setPhotoActorContent(getPageIndex(), mActorNow);

            if (isLessOrEqualOnePage()) {
                removeActorContent(mActorBefore);
                removeActorContent(mActorNext);
            } else {
                resetActor(getPrevPageIndex(), mActorBefore);
                setPhotoActorContent(getPrevPageIndex(), mActorBefore);
                resetActor(getNextPageIndex(), mActorNext);
                setPhotoActorContent(getNextPageIndex(), mActorNext);
            }
        } else {
            removeActorContent(mActorNow);
        }
    }

    private AnimationGroup preparePhotoAnimation(int animationName) {
        AnimationGroup group = mAnimationLists.get(animationName);
        for (int i = 0; i < mActorNow.length; i++) {
            if (((Container) mActorNow[i]).findChildByTag(PHOTO_TAG) == null) {
                group.getAnimation(i).setTarget(null);
            } else {
                group.getAnimation(i).setTarget(mActorNow[i]);
            }
        }
        return group;
    }

    private void startPhotoAnimation(int name) {
        preparePhotoAnimation(name).start();
    }

    private void startPhotoBackgroundAnimation(int animation) {
        AnimationGroup group = mAnimationLists.get(animation);
        if (animation == R.string.swap_in || animation == R.string.swap_out) {
            group.getAnimation(0).setTarget(mBackground);
            group.start();
        }
    }

    private void stopPhotoAnimation(int name) {
        mAnimationLists.get(name).stop();
    }

    private void stopAllAnimations() {
        for (Animation ani : mAnimationLists.values()) {
             ani.stop();
        }
    }

    private void showPhoto(int transitionName) {
        LogUtil.v(TAG, "trans : " + transitionName);
        startPhotoAnimation(transitionName);
        updateIndicators();
        updatePageIndexText();
    }

    private void hidePhoto(int transitionName) {
        leavePage(transitionName);
        leaveBackground(R.string.swap_out);
    }

    private AnimationGroup addAnimationList(int nameResId, int aniArrayResId, int cacheNameArrayResId) {
        AnimationGroup group = new AnimationGroup();
        group.setName(getString(nameResId));
        TypedArray aniResIds = getActivity().getResources().obtainTypedArray(aniArrayResId);
        String[] cacheNameResIds =
            getActivity().getResources().getStringArray(cacheNameArrayResId);

        final int length = aniResIds.length();
        for (int i = 0; i < length; i++) {
            group.add(AnimationLoader.loadAnimation(
                getActivity(), aniResIds.getResourceId(i, 0), cacheNameResIds[i]).
                disableOptions(Animation.CAN_START_WITHOUT_TARGET));
        }
        mAnimationLists.put(nameResId, group);
        group.addListener(mAnimationCompletedHandler);
        return group;
    }

    private void setActorVisibility(Actor[] actors, boolean visible) {
        for (Actor actor : actors) {
            actor.setVisible(visible);
        }
    }

    public int getTotalPhotoPage() {
        return (mMediaItemSet.getItemCount() + PHOTO_PER_PAGE - 1) / PHOTO_PER_PAGE;
    }

    private boolean isEnterAnimation(String name) {
        return name.equals(getString(R.string.left_enter))
            || name.equals(getString(R.string.right_enter))
            || name.equals(getString(R.string.next_enter));
    }

    private boolean isExitAnimation(String name) {
        return name.equals(getString(R.string.left_exit))
            || name.equals(getString(R.string.right_exit))
            || name.equals(getString(R.string.fade_out));
    }

    private boolean isEnterExitAnimation(String name) {
        return isEnterAnimation(name) || isExitAnimation(name);
    }

    private final Animation.Listener mAnimationCompletedHandler = new Animation.Listener() {
        public void onMarkerReached(Animation animation, int direction, String marker) {
            if ("enter".equals(marker)) {
                setState(IDLE);
            }
        }

        private void prepareIdleScene(final String name) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (name.equals(getString(R.string.next_exit_enter))) {
                        setActorVisibility(mActorNext, false);
                    } else {
                        setActorVisibility(mActorBefore, false);
                    }
                    updateIndicators();
                    updatePageIndexText();
                }
            });
        }

        public void onPaused(Animation animation) {
            if (getActivity() == null) {
                return;
            }
            final String name = animation.getName();
            if (Media3D.DEBUG) {
                Log.v(TAG, "ani - onPaused: " + animation.getName() + " ," + animation + ", state = " + getState());
            }

            if (Main.ON_DRAG_MODE) {
                if (mRunningAnimation != null && mRunningAnimation.getName().equals(name)) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (Media3D.DEBUG) {
                                Log.v(TAG, "removing: " + name);
                            }
                            mRunningAnimation = null;
                        }
                    });
                }
                if (name.equals(getString(R.string.next_exit_enter)) || name.equals(getString(R.string.last_exit_enter))) {
                    prepareIdleScene(name);
                }
            }
        }

        public void onCompleted(Animation animation) {
            if (getActivity() == null) {
                return;
            }
            final String name = animation.getName();
            if (Media3D.DEBUG) {
                Log.v(TAG, "ani - onCompleted: " + animation.getName() + " ," + animation);
            }

            if (isEnterExitAnimation(name) && getState() != IDLE) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setState(IDLE);
                    }
                });
            }

            if (isEnterAnimation(name)) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mAnimationLists.get(R.string.floating).setDirection(Timeline.FORWARD);
                        startPhotoAnimation(R.string.floating);
                        showBackground(R.string.swap_in);
                    }
                });
            } else if (name.equals(getString(R.string.swap_in)) && getState() == IDLE) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mPhotoOffsetIndex = getNextOffsetIndex();
                        showBackground(R.string.swap_in);
                    }
                });
            } else if (name.equals(getString(R.string.next_exit_enter)) || name.equals(getString(R.string.last_exit_enter))) {
                if (!Main.ON_DRAG_MODE) {
                    prepareIdleScene(name);
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (!Main.ON_DRAG_MODE) {
                            mScrollDetector.onAnimationFinish();
                        }
                        mAnimationLists.get(R.string.floating).setDirection(Timeline.FORWARD);
                        startPhotoAnimation(R.string.floating);
                        mPhotoOffsetIndex = 0;
                        showBackground(R.string.swap_in);
                    }
                });
            }

        }
    };

    private static final int REQUEST_GET_PHOTO = 2;

    private void choosePhotoFolder() {
        stopAllAnimations();
        setActorVisibility(mActorNow, false);
        mBackground.setEmptyImage();
        mBackground.setVisible(false);

        try {
            Intent intent = new Intent("com.mediatek.action.PICK_IMAGE_FOLDER");
            intent.setType("image/*");
            getActivity().startActivityForResult(intent, REQUEST_GET_PHOTO);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.toString());
            Intent intent = new Intent("android.appwidget.action.GET_PARAMS");
            intent.setType("image/*");
            intent.putExtra("fromAppWidgetConfigure", true);
            startActivityForResult(intent, REQUEST_GET_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onActivityResult(): " + data);
        }
        if (requestCode == REQUEST_GET_PHOTO && resultCode == Activity.RESULT_OK) {
            String bid = data.getStringExtra("bucketId");
            int mediaTypes = data.getIntExtra("mediaTypes", 0);
            if (Media3D.DEBUG) {
                Log.v(TAG, String.format("onActivityResult(), result=%d, bucketId=%s, mediaTypes=%d", resultCode, bid, mediaTypes));
            }

            if (bid != null && bid.length() > 0) {
                mBucketId = bid;
                setBucketId(mBucketId);
                prepareMediaItemSet(mBucketId);
                adjustPageIndex(true);
                initPhotoActorContent();
                updateBackgroundText();
            }
        }
        if(!isNoPhoto()) {
            showPhoto(R.string.next_enter);
        }

    }

    private ImageDbItemSet createImageDbItemSet(String bucketId) {
        return new ImageDbItemSet(getActivity().getContentResolver(), bucketId);
    }

    private ImageDbItemSet createNullImageDbItemSet() {
        return new ImageDbItemSet(getActivity().getContentResolver(), null);
    }

    private void prepareMediaItemSet(String bucketId) {
        if (mMediaItemSet != null) {
            mMediaItemSet.close();
        }

        if (Media3D.isDemoMode()) {
            final int [] demoImage = new int[] {
                    R.drawable.gg_hyoyeon, R.drawable.gg_jessica,
                    R.drawable.gg_seohyun, R.drawable.gg_sunny,
                    R.drawable.gg_taeyeon, R.drawable.gg_tiffany,
                    R.drawable.gg_yoona, R.drawable.gg_yuri };
            mMediaItemSet = new ResourceItemSet(
                getActivity().getResources(), demoImage, demoImage);
        } else {
            mMediaItemSet = createImageDbItemSet(bucketId);
            ((MediaDbItemSet)mMediaItemSet).registerObserver(mImageContentObserver);
        }
    }

    private void destroyMediaItemSet() {
        if (mMediaItemSet != null) {
            mMediaItemSet.close();
            mMediaItemSet = null;
        }
    }

    private void onSlideShowClicked() {
        if (mMediaItemSet.getItemCount() == 0) {
            return;
        }

        Uri targetUri = getCurrentUri();
        if (Media3D.DEBUG) {
            Log.v(TAG, "onSlideShowClicked, targetUri = " + targetUri);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, targetUri);
        intent.putExtra("slideshow", true);
        intent.putExtra("repeat", false);
        if (mBucketId != null) {
            intent.putExtra("media-set-path", "/local/all/" + mBucketId);
        }
        startActivity(intent);
    }

    private Uri getCurrentUri() {
        int index = getPageIndex() * PHOTO_PER_PAGE + mPhotoOffsetIndex;
        return mMediaItemSet.getItem(index).getUri();
    }

    private void onViewClicked(int offsetIndex) {
        int index = getPageIndex() * PHOTO_PER_PAGE + offsetIndex;
        Uri targetUri = mMediaItemSet.getItem(index).getUri();
        Intent intent = new Intent(Intent.ACTION_VIEW, targetUri);
        startActivity(intent);
    }

    private static Bitmap getThumbnail(MediaItem mi, int desiredWidth, int desiredHeight) {
        return mi.getThumbnail(desiredWidth, desiredHeight);
    }

    private static class ThumbLoader implements Runnable {
        private final ArrayList<Actor> mActors = new ArrayList<Actor>();
        private final ArrayList<MediaItem> mMediaItems = new ArrayList<MediaItem>();
        private Actor[] mOwner;
        private volatile boolean mIsCancelled;

        public void setOwner(Actor[] owner) {
            mOwner = owner;
        }

        public Actor[] getOwner() {
            return mOwner;
        }

        public boolean hasOwner() {
            return (mOwner != null);
        }

        public void add(Actor a, MediaItem mi) {
            if (a == null || mi == null) {
                throw new IllegalArgumentException();
            }
            mActors.add(a);
            mMediaItems.add(mi);
        }

        public int size() {
            return mActors.size();
        }

        public void clear() {
            mActors.clear();
            mMediaItems.clear();
            mOwner = null;
            mIsCancelled = false;
        }

        public void cancel() {
            mIsCancelled = true;
        }

        public void run() {
            final int count = mActors.size();
            LogUtil.v(TAG, "Loader start, job count: " + count);

            for (int i = 0; i < count; i++) {
                if (mIsCancelled) {
                    LogUtil.v(TAG, "Cancelled, at count: " + i);
                    break;
                }
                final MediaItem mi = mMediaItems.get(i);
                BitmapGenerator generator = new BitmapGenerator() {
                    public Bitmap generate() {
                        return getThumbnail(mi, sThumbnailWidth, sThumbnailHeight);
                    }
                };
                /**
                 * Set default bitmap for generator and if it generates null thumbnail,
                 * it will return default one we provided.
                 */
                generator.setDefaultBitmap(mBlankBitmap);
                generator.cacheBitmap();
                Image imageActor = Image.createFromBitmapGenerator(generator);
                ((Container) mActors.get(i)).add(imageActor);
                Thread.yield();
            }
            clear();
        }
    }

    private ExecutorService mExecutorService;
    private final ArrayList<ThumbLoader> mThumbLoaders = new ArrayList<ThumbLoader>();
    private final ArrayList<ThumbLoader> mRunningLoaders = new ArrayList<ThumbLoader>();
    private void releaseLoaders(boolean isForce) {
        for (ThumbLoader loader : mThumbLoaders) {
            if (loader.hasOwner()) {
                if (isForce) {
                    loader.cancel();
                } else {
                    mRunningLoaders.add(loader);
                }
            }
        }
        LogUtil.v(TAG, "Running loader count :" + mRunningLoaders.size());
        mThumbLoaders.clear();
        mThumbLoaders.addAll(mRunningLoaders);
        mRunningLoaders.clear();
    }

    private ThumbLoader getFreeLoader(Actor[] actors) {
        for (int i = 0 ; i < mThumbLoaders.size(); ++i) {
            ThumbLoader loader = mThumbLoaders.get(i);
            if (loader.getOwner() == actors) {
                loader.cancel();
                continue;
            }
            if (!loader.hasOwner()) {
                loader.setOwner(actors);
                return loader;
            }
        }

        ThumbLoader newLoader = new ThumbLoader();
        newLoader.setOwner(actors);
        mThumbLoaders.add(newLoader);
        LogUtil.v(TAG, "Total Loader :" + mThumbLoaders.size());
        return newLoader;
    }

    private static final int MAX_THREADS_NUM = 8;
    private void setPhotoActorContent(int pageIndex, Actor[] photoActor) {
        if (photoActor.length != PHOTO_PER_PAGE) {
            return;
        }

        ThumbLoader newLoader = null;
        int totalCount = mMediaItemSet.getItemCount();
        for (int i = 0; i < photoActor.length; i++) {
            int index = pageIndex * PHOTO_PER_PAGE + i;
            if (index >= totalCount) {
                break;
            }
            Container imageContainer = (Container) (((Container) (photoActor[i])).findChildByTag(PHOTO_TAG));
            MediaItem mi = mMediaItemSet.getItem(index);
            if (imageContainer != null && mi != null) {
                if (newLoader == null) {
                    newLoader = getFreeLoader(photoActor);
                }
                newLoader.add(imageContainer, mi);
            }
        }

        if (newLoader != null && newLoader.size() > 0) {
            if (mExecutorService == null) {
                mExecutorService = Executors.newFixedThreadPool(MAX_THREADS_NUM);
            }
            mExecutorService.submit(newLoader);
        }
    }

    private ExecutorService mBackgroundExecutorService;
    private Future<?> mBackgroundFuture;

    private static class BackgroundLoader implements Runnable {
        private final Image mActor;
        private final MediaItem mMediaItem;
        private final AnimationGroup mAniGrp;

        public BackgroundLoader(Image a, MediaItem mi, AnimationGroup aniGrp) {
            if (a == null || mi == null || aniGrp == null) {
                throw new IllegalArgumentException();
            }
            mActor = a;
            mMediaItem = mi;
            mAniGrp = aniGrp;
        }

        public void run() {
            BitmapGenerator generator = new BitmapGenerator() {
                public Bitmap generate() {
                    return getThumbnail(mMediaItem, sTempBackgroundWidth, sTempBackgroundHeight);
                }
            };
            /**
             * Set default bitmap for generator and if it generates null thumbnail,
             * it will return default one we provided.
             */
            generator.setDefaultBitmap(mBlankBitmap);
            generator.cacheBitmap();
            mActor.setImageFromBitmapGenerator(generator);

            // If aspect ratio of image is 1.333, vertical line artifact occurred when applying simple blur effect.
            // Change aspect ratio and fit the image size with screen resolution.
            mActor.setSize(sBackgroundDimension);
            mAniGrp.stop();
            mAniGrp.getAnimation(0).setTarget(mActor);
            mAniGrp.start();
        }
    }

    private void setBackgroundPhotoActorContent(int animation) {
        int index = getPageIndex() * PHOTO_PER_PAGE + mPhotoOffsetIndex;
        if (index >= mMediaItemSet.getItemCount()) {
            throw new IllegalArgumentException("index > totalCount");
        } else {
            AnimationGroup aniGrp;
            if (animation == R.string.swap_in || animation == R.string.swap_out) {
                aniGrp = mAnimationLists.get(animation);

                BackgroundLoader bl = new BackgroundLoader(mBackground, mMediaItemSet.getItem(index), aniGrp);
                if (mBackgroundExecutorService == null) {
                    mBackgroundExecutorService = Executors.newCachedThreadPool();
                }

                if (mBackgroundFuture != null) {
                    mBackgroundFuture.cancel(false);
                }
                mBackgroundFuture = mBackgroundExecutorService.submit(bl);
            }
        }
    }

    private void leavePage(int name) {
        startPhotoAnimation(name);
    }

    private int getNextOffsetIndex() {
        if (mPhotoOffsetIndex == PHOTO_PER_PAGE - 1 || getPageIndex() * PHOTO_PER_PAGE + mPhotoOffsetIndex == mMediaItemSet.getItemCount() - 1) {
            return 0;
        } else {
            return mPhotoOffsetIndex + 1;
        }
    }

    private void leaveBackground(int name) {
        if (mMediaItemSet.getItemCount() != 0) {
            startPhotoBackgroundAnimation(name);
        }
    }

    private void showBackground(int name) {
        if (mMediaItemSet.getItemCount() != 0) {
            setBackgroundPhotoActorContent(name);
        }
    }

    @Override
    protected boolean onSingleTapConfirmed(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Point pos = new Point(event.getX(), event.getY());
            if (Media3D.DEBUG) {
                Log.v(TAG, "onSingleTapConfirmed - down, Point = " + pos);
            }
            return hitTest(pos);
        }
        return false;
    }

    @Override
    protected boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    private boolean hitTest(Point pos) {
        Actor hitActor = mStage.hitTest(pos);
        if (hitActor == null) {
            return false;
        }

        for (int i = 0; i < PHOTO_PER_PAGE; i++) {
            if (hitActor == mActorNow[i]) {
                onViewClicked(i);
                return true;
            }
        }

        return false;
    }

    private static final int CMD_CHOOSE_FOLDER = 1;
    private static final int CMD_ENTER_SLIDESHOW = 2;

    @Override
    public boolean onCreateBarMenu(NavigationBarMenu menu) {
        super.onCreateBarMenu(menu);
        menu.add(CMD_CHOOSE_FOLDER).setIcon(R.drawable.top_photo_folder);
        menu.add(CMD_ENTER_SLIDESHOW).setIcon(R.drawable.top_photo_slideshow);
        return true;
    }

    @Override
    public boolean onBarMenuItemSelected(NavigationBarMenuItem item) {
        int itemId = item.getItemId();
        if (itemId == CMD_CHOOSE_FOLDER) {
            choosePhotoFolder();
        } else if (itemId == CMD_ENTER_SLIDESHOW) {
            onSlideShowClicked();
        } else {
            return super.onBarMenuItemSelected(item);
        }
        return true;
    }

    @Override
    public Actor getThumbnailActor() {
        Container thumbContainer = new Container();
        Image thumbImage;

        // get mediaitemset first
        if (mMediaItemSet == null) {
            prepareMediaItemSet(getBucketId());
        }

        if (mMediaItemSet.getItemCount() == 0) {
            thumbImage = Image.createFromResource(getActivity().getResources(), R.drawable.mainmenu_photo_empty);
        } else {
            final MediaItem mi = mMediaItemSet.getItem(0);
            BitmapGenerator generator = new BitmapGenerator() {
                public Bitmap generate() {
                    return getThumbnail(mi, sPortalThumbnailWidth, sPortalThumbnailHeight);
                }
            };
            /**
             * Set default bitmap for generator and if it generates null thumbnail,
             * it will return default one we provided.
             */
            generator.setDefaultBitmap(mBlankBitmap);
            generator.cacheBitmap();
            thumbImage = Image.createFromBitmapGenerator(generator);
        }
        thumbImage.setPosition(new Point(2, 0, +0.08f));
        thumbImage.setRotation(new Rotation(0, 0, -12));
        thumbImage.setReactive(false);
        thumbContainer.add(thumbImage);

        Image emptyImage = Image.createFromResource(getActivity().getResources(), R.drawable.portal_photo_none);
        emptyImage.setPosition(new Point(0, 0, -0.02f));
        emptyImage.setReactive(false);
        thumbContainer.add(emptyImage);
        return thumbContainer;
    }

    private String getString(int ResId) {
        return getActivity().getString(ResId);
    }

    private boolean switchPageIndex(boolean is_next) {
        if (isNoPhoto()) {
            return false;
        }

        if (is_next) {
            mPhotoPageIndex = getNextPageIndex();
            return true;

        } else {
            mPhotoPageIndex = getPrevPageIndex();
            return true;
        }
    }

    private void shiftActors(boolean is_next) {
        Actor[] actorTmp = is_next ? mActorBefore : mActorNext;
        if (is_next) {
            mActorBefore = mActorNow;
            mActorNow = mActorNext;
            mActorNext = actorTmp;
        } else {
            mActorNext = mActorNow;
            mActorNow = mActorBefore;
            mActorBefore = actorTmp;
        }
    }

    void prepareNewActors(boolean is_next) {
        if (is_next) {
            // prepare new ActorNext
            if (!isLessOrEqualOnePage()) {
                resetActor(getNextPageIndex(), mActorNext);
                mHandler.post(new Runnable() {
                    public void run() {
                        setPhotoActorContent(getNextPageIndex(), mActorNext);
                    }
                });
            }
        } else {
            // prepare new ActorBefore
            if (!isLessOrEqualOnePage()) {
                resetActor(getPrevPageIndex(), mActorBefore);
                mHandler.post(new Runnable() {
                    public void run() {
                        setPhotoActorContent(getPrevPageIndex(), mActorBefore);
                    }
                });
            }
        }
    }

    private static final float ARROW_X = 0.5f;

    private Actor getIconLast() {
        if (mIconLast == null) {
            mIconLast = Image.createFromResource(getActivity().getResources(), R.drawable.ic_arrow_up);
            mIconLast.setPosition(new Point(ARROW_X, 0.95f, true));  // TODO: how can we know the width before realize?
            mIconLast.setRotation(new Rotation(0, 0, 180));
            mIconLast.setVisible(false);
            getContainer().add(mIconLast);
        }
        return mIconLast;
    }

    private Actor getIconNext() {
        if (mIconNext == null) {
            mIconNext = Image.createFromResource(getActivity().getResources(), R.drawable.ic_arrow_up);
            mIconNext.setPosition(new Point(ARROW_X, 0.05f, true));
            mIconNext.setVisible(false);
            getContainer().add(mIconNext);
        }
        return mIconNext;
    }

    private boolean isNoPhoto() {
        return (mMediaItemSet.getItemCount() == 0);
    }

    private boolean isLessOrEqualOnePage() {
        return (getTotalPhotoPage() <= 1);
    }

    private void updateBackgroundText() {
        mNoPhotoText.setVisible(isNoPhoto());
    }

    private void updateIndicators() {
        final boolean isArrowVisible = !isLessOrEqualOnePage();
        getIconLast().setVisible(isArrowVisible);
        getIconNext().setVisible(isArrowVisible);
    }

    private void updatePageIndexText() {
        if (isNoPhoto()) {
            mPageIndexText.setVisible(false);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getPageIndex() + 1);
        sb.append("/");
        sb.append(getTotalPhotoPage());
        mPageIndexText.setText(sb.toString());
        mPageIndexText.setVisible(true);
    }

    public void movingActor(boolean is_next) {
        switchPageIndex(is_next);
        shiftActors(is_next);
        prepareNewActors(is_next);
        updateIndicators();
        updatePageIndexText();
    }

    void startTransitionAnimation(int name, Actor[] actor1, Actor[] actor2, boolean accelerated) {
        AnimationGroup group = mAnimationLists.get(name);
        int index = 0;

        for (int i = 0; i < actor1.length; ++i) {
            Actor actor = actor1[i];
            if (((Container) actor).findChildByTag(PHOTO_TAG) == null) {
                group.getAnimation(index).setTarget(null);
            } else {
                group.getAnimation(index).setTarget(actor);
            }
            index++;
        }

        for (int i = 0; i < actor2.length; ++i) {
            Actor actor = actor2[i];
            if (((Container) actor).findChildByTag(PHOTO_TAG) == null) {
                group.getAnimation(index).setTarget(null);
            } else {
                group.getAnimation(index).setTarget(actor);
            }
            index++;
        }

        mRunningAniId = name;
        group.setDirection(Timeline.FORWARD);
        group.setTimeScale(accelerated ? ANIM_SPEED_FASTER : ANIM_SPEED_NORMAL);
        if (Main.ON_DRAG_MODE) {
            mRunningAnimation = group;
        } else {
            group.start();
        }
    }

    private void preparePreviousScene(boolean accelerated) {
        // next exit and next enter
        stopAllAnimations();
        shiftActors(false);
        prepareNewActors(false);
        startTransitionAnimation(R.string.next_exit_enter, mActorNext, mActorNow, accelerated);
    }

    private void prepareNextScene(boolean accelerated) {
        // last exit and last enter
        stopAllAnimations();
        shiftActors(true);
        prepareNewActors(true);
        startTransitionAnimation(R.string.last_exit_enter, mActorBefore, mActorNow, accelerated);
    }

    public boolean onDrag(PageDragHelper.State state, float disY) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onDrag(): " + state);
        }

        switch (state) {
            case INITIAL:
                if (mRunningAnimation != null && mRunningAnimation.isStarted()) {
                    mRunningAnimation.complete();
                    mRunningAnimation.stop();
                }
                break;
            case START:
                if (disY < 0) { // drag up
                    if (switchPageIndex(false)) {
                        preparePreviousScene(false);
                        mRunningAnimation.startDragging();
                    }
                } else {  // drag down
                    if (switchPageIndex(true)) {
                        prepareNextScene(false);
                        mRunningAnimation.startDragging();
                    }
                }
                break;
            case DRAGGING:
                if (mRunningAnimation != null) {
                    mRunningAnimation.setProgress(Math.abs(disY) / sMaxDragRange);
                }
                break;
            case FINISH:
                if (mRunningAnimation != null) {
                    mRunningAnimation.stopDragging();
                    if (mRunningAnimation.getProgress() < sDragThreshold ||
                        isLessOrEqualOnePage()) {
                        mRunningAnimation.reverse();
                        if (mRunningAnimation.getName().equals(getString(R.string.last_exit_enter))) {
                            movingActor(mRunningAnimation.getDirection() == Timeline.FORWARD);
                        } else {
                            movingActor(mRunningAnimation.getDirection() != Timeline.FORWARD);
                        }
                    } else {
                        updatePageIndexText();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    public boolean onAction(PageTransitionDetector.Action action, boolean accelerated) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onAction(): " + action);
        }

        switch (action) {
            case DOWN:
                    if (switchPageIndex(false)) {
                        preparePreviousScene(accelerated);
                        return true;
                    } else {                    // TODO: bounding animation
                        if (Media3D.DEBUG) {
                            Log.v(TAG, "1st page");
                        }
                    }
                break;

            case UP:
                    if (switchPageIndex(true)) {
                        prepareNextScene(accelerated);
                        return true;
                    } else {                    // TODO: bounding animation
                        if (Media3D.DEBUG) {
                            Log.v(TAG, "1st page");
                        }
                    }
                break;

            case REVERSE:
                AnimationGroup group = mAnimationLists.get(mRunningAniId);
                group.reverse();

                    if (mRunningAniId == R.string.last_exit_enter) {
                        movingActor(group.getDirection() == Timeline.FORWARD);
                    } else {
                        movingActor(group.getDirection() != Timeline.FORWARD);
                    }
                return true;

            case ACCELERATE:
                mAnimationLists.get(mRunningAniId).setTimeScale(ANIM_SPEED_FASTER);
                return true;

            default:
                break;
        }
        return false;
    }

    private String getBucketId() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String bid = pref.getString(KEY_PHOTO_BUCKET_ID, MediaUtils.CAMERA_IMAGE_BUCKET_ID);

        if (Media3D.DEBUG) {
            Log.v(TAG, "getBucketId - id = " + bid);
        }
        return bid;
    }

    private void setBucketId(String newBucketId) {
        if (newBucketId != null && newBucketId.length() > 0) {
            mBucketId = newBucketId;
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(KEY_PHOTO_BUCKET_ID, newBucketId);
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        if (mIsMediaItemSetModified) {
            stopPhotoAnimation(R.string.floating);
            prepareMediaItemSet(mBucketId);
            setActorVisibility(mActorNow, false);
            adjustPageIndex(false);
            initPhotoActorContent();
            updateBackgroundText();
            showPhoto(R.string.next_enter);
            mIsMediaItemSetModified = false;
        }
        super.onResume();
    }

    private void adjustPageIndex(boolean reset) {
        if (reset) {
            mPhotoPageIndex = 0;
        } else {
            int lastPageIndex = (getTotalPhotoPage() > 0) ? (getTotalPhotoPage() - 1) : 0;
            if (lastPageIndex < mPhotoPageIndex) {
                mPhotoPageIndex = lastPageIndex;
            }
        }
        mPhotoOffsetIndex = 0;
    }

    private boolean peekChangeAndRequery(boolean isNull) {
        ImageDbItemSet dbItemSet = (isNull) ? createNullImageDbItemSet() : createImageDbItemSet(mBucketId);
        if (dbItemSet.getItemCount() != mMediaItemSet.getItemCount()) {
            mMediaItemSet.close();
            dbItemSet.registerObserver(mImageContentObserver);
            mMediaItemSet = dbItemSet;
            return true;
        }
        dbItemSet.close();
        return false;
    }

    public void onChanged(int event) {
        if (event != MediaSourceListener.MEDIA_MOUNTED_EVENT &&
            event != MediaSourceListener.MEDIA_UNMOUNTED_EVENT ) {
            return;
        }

        releaseLoaders(true);
        stopAllAnimations();
        setActorVisibility(mActorNow, false);
        mBackground.setEmptyImage();
        mBackground.setVisible(false);

        boolean isNullDataSet = (event == MediaSourceListener.MEDIA_UNMOUNTED_EVENT);
        if (peekChangeAndRequery(isNullDataSet)) {
            adjustPageIndex(true);
            initPhotoActorContent();
            if (getContainer().getTrulyVisible()) {
                updateBackgroundText();
                showPhoto(R.string.next_enter);
            }
        }
    }

    private final ImageContentObserver mImageContentObserver =
            new ImageContentObserver(new Handler());

    private final class ImageContentObserver extends ContentObserver {
        public ImageContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            if (!mIsMediaItemSetModified && mMediaItemSet != null &&
                    ((MediaDbItemSet)mMediaItemSet).peekChange()) {
                mIsMediaItemSetModified = true;
            }
        }
    }

    private final Handler mHandler = new Handler();
}
