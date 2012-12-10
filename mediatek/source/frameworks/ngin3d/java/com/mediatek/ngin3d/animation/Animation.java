package com.mediatek.ngin3d.animation;

import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Ngin3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents animation that can be started or stopped.
 */
public abstract class Animation implements Cloneable {
    protected static final String TAG = "Animation";

    public static final int SHOW_TARGET_ON_STARTED = 1 << 0;
    public static final int HIDE_TARGET_ON_COMPLETED = 1 << 1;
    public static final int SHOW_TARGET_DURING_ANIMATION = SHOW_TARGET_ON_STARTED | HIDE_TARGET_ON_COMPLETED;
    public static final int DEACTIVATE_TARGET_ON_STARTED = 1 << 2;
    public static final int ACTIVATE_TARGET_ON_COMPLETED = 1 << 3;
    public static final int DEACTIVATE_TARGET_DURING_ANIMATION = DEACTIVATE_TARGET_ON_STARTED | ACTIVATE_TARGET_ON_COMPLETED;
    public static final int CAN_START_WITHOUT_TARGET = 1 << 4;
    public static final int BACK_TO_START_POINT_ON_COMPLETED = 1 << 5;
    public static final int LOCK_Z_ROTATION = 1 << 6;
    public static final int START_TARGET_WITH_INITIAL_VALUE = 1 << 7;
    public static final int DEBUG_ANIMATION_TIMING = 1 << 15;

    protected int mOptions = SHOW_TARGET_ON_STARTED | CAN_START_WITHOUT_TARGET | START_TARGET_WITH_INITIAL_VALUE;

    public static final int FORWARD = Timeline.FORWARD;
    public static final int BACKWARD = Timeline.BACKWARD;

    protected int mTag;
    protected String mName = "";

    protected Animation() {
        if (Ngin3d.DEBUG) {
            mOptions |= DEBUG_ANIMATION_TIMING;
        }
    }

    @Override
    public String toString() {
        if (mName.length() > 0) {
            return mName;
        } else {
            return super.toString();
        }
    }

    public void setTag(int tag) {
        mTag = tag;
    }

    public int getTag() {
        return mTag;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    /**
     * Start this animation.
     *
     * @return the animation itself
     */
    public abstract Animation start();

    public abstract Animation startDragging();

    /**
     * Pause this animation.
     *
     * @return the animation itself
     */
    public abstract Animation pause();

    /**
     * Stop this animation.
     *
     * @return the animation itself
     */
    public abstract Animation stop();

    public abstract Animation stopDragging();

    public abstract Animation reset();

    public abstract Animation complete();

    public abstract boolean isStarted();

    public abstract void setTimeScale(float scale);

    public abstract float getTimeScale();

    public abstract void setDirection(int direction);

    public abstract int getDirection();

    public abstract void reverse();

    public abstract Animation setTarget(Actor target);

    public abstract Animation setTargetVisible(boolean visible);

    public abstract Actor getTarget();

    public abstract int getDuration();

    public abstract void setProgress(float progress);

    public Animation enableOptions(int options) {
        mOptions |= options;
        return this;
    }

    public Animation disableOptions(int options) {
        mOptions &= ~options;
        return this;
    }

    public int getOptions() {
        return mOptions;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listener handling

    public static class Listener {
        /**
         * Notify the animation was started.
         *
         * @param animation the started animation
         */
        public void onStarted(Animation animation) {
            // Animation onStarted callback function
        }

        /**
         * Notify the marker was reached.
         *
         * @param animation the animation
         * @param direction direction of animation. FORWARD or BACKWARD.
         * @param marker marker name
         */
        public void onMarkerReached(Animation animation, int direction, String marker) {
            // Animation onMarkerReached callback function
        }

        /**
         * Notify the animation was paused or stopped.
         *
         * @param animation the paused animation
         */
        public void onPaused(Animation animation) {
            // Animation onPaused callback function
        }

        /**
         * Notify the animation was stopped.
         *
         * @param animation the completed animation
         */
        public void onCompleted(Animation animation) {
            // Animation onCompleted callback function
        }
    }

    protected List<Listener> mListeners = new ArrayList<Listener>();

    public void addListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Flags handling

    protected void applyOnStartedFlags() {
        final Actor target = getTarget();
        if ((mOptions & Animation.SHOW_TARGET_ON_STARTED) != 0 && target != null) {
            target.setVisible(true);
        }
        if ((mOptions & Animation.DEACTIVATE_TARGET_ON_STARTED) != 0 && target != null) {
            target.setReactive(false);
        }
    }

    protected void applyOnCompletedFlags() {
        final Actor target = getTarget();
        if ((mOptions & Animation.HIDE_TARGET_ON_COMPLETED) != 0 && target != null) {
            target.setVisible(false);
        }
        if ((mOptions & Animation.ACTIVATE_TARGET_ON_COMPLETED) != 0 && target != null) {
            target.setReactive(true);
        }
    }

    /**
     * Clone the animation, value in each member of cloned animation is same of original one, except animation name.
     * The name of cloned animation is empty in default.
     * @return the cloned animation
     */
    @Override
    public Animation clone() {
        try {
            Animation animation = (Animation) super.clone();
            animation.mListeners = new ArrayList<Listener>();
            animation.mName = "";
            return animation;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
