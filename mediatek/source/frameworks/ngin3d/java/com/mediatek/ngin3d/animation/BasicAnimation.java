package com.mediatek.ngin3d.animation;

import android.util.Log;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.UiHandler;

/**
 * Basic animation that has its own timeline and alpha.
 */
public abstract class BasicAnimation extends Animation {
    private static final String TAG = "BasicAnimation";

    public static final int DEFAULT_DURATION = 2000;

    protected Timeline mTimeline;
    protected Alpha mAlpha;

    public BasicAnimation() {
        this(DEFAULT_DURATION, Mode.LINEAR);
    }

    public BasicAnimation(int duration, Mode mode) {
        this(new Timeline(duration), mode);
    }

    public BasicAnimation(Timeline timeline, Mode mode) {
        setTimeline(timeline);
        setAlphaMode(mode);
    }

    private void setTimeline(Timeline timeline) {
        mTimeline = timeline;
        mTimeline.addListener(new Timeline.Listener() {
            public void onStarted(Timeline timeline) {
                if ((mOptions & DEBUG_ANIMATION_TIMING) != 0) {
                    Log.d(TAG, String.format("%d: Animation %s is started", System.currentTimeMillis(), BasicAnimation.this));
                }

                applyOnStartedFlags();
                synchronized (mListeners) {
                    int size = mListeners.size();
                    for (int i = 0; i < size; i++) {
                        final Listener l = mListeners.get(i);
                        Runnable runnable = new Runnable() {
                            public void run() {
                                l.onStarted(BasicAnimation.this);
                            }
                        };
                        runCallback(runnable);
                    }
                }
            }

            public void onNewFrame(Timeline timeline, int elapsedMsecs) {
                // do nothing now
            }

            public void onMarkerReached(Timeline timeline, int elapsedMsecs, String marker, int direction) {
                if ((mOptions & DEBUG_ANIMATION_TIMING) != 0) {
                    Log.d(TAG, String.format("%d: Marker [%s] of Animation %s is reached", System.currentTimeMillis(), marker, BasicAnimation.this));
                }
                final String m = marker;
                final int d = direction;
                synchronized (mListeners) {
                    int size = mListeners.size();
                    for (int i = 0; i < size; i++) {
                        final Listener l = mListeners.get(i);
                        Runnable runnable = new Runnable() {
                            public void run() {
                                l.onMarkerReached(BasicAnimation.this, d, m);
                            }
                        };
                        runCallback(runnable);
                    }
                }
            }

            public void onPaused(Timeline timeline) {
                if ((mOptions & DEBUG_ANIMATION_TIMING) != 0) {
                    Log.d(TAG, String.format("%d: Animation %s is paused", System.currentTimeMillis(), BasicAnimation.this));
                }

                synchronized (mListeners) {
                    int size = mListeners.size();
                    for (int i = 0; i < size; i++) {
                        final Listener l = mListeners.get(i);
                        Runnable runnable = new Runnable() {
                            public void run() {
                                l.onPaused(BasicAnimation.this);
                            }
                        };
                        runCallback(runnable);
                    }
                }
            }

            public void onCompleted(Timeline timeline) {
                if ((mOptions & DEBUG_ANIMATION_TIMING) != 0) {
                    Log.d(TAG, String.format("%d: Animation %s is completed", System.currentTimeMillis(), BasicAnimation.this));
                }

                applyOnCompletedFlags();
                synchronized (mListeners) {
                    int size = mListeners.size();
                    for (int i = 0; i < size; i++) {
                        final Listener l = mListeners.get(i);
                        Runnable runnable = new Runnable() {
                            public void run() {
                                l.onCompleted(BasicAnimation.this);
                            }
                        };
                        runCallback(runnable);
                    }
                }
            }

            public void onLooped(Timeline timeline) {
                // do nothing now
            }
        });

    }

    private void setAlphaMode(Mode mode) {
        mAlpha = new Alpha(mTimeline, mode);
    }

    private void runCallback(Runnable runnable) {
        UiHandler uiHandler = Stage.getUiHandler();
        if (uiHandler == null) {
            runnable.run();
        } else {
            uiHandler.post(runnable);
        }
    }

    public void addMarkerAtTime(String name, int time) {
        mTimeline.addMarkerAtTime(name, time);
    }

    public Mode getMode() {
        return mAlpha.getMode();
    }

    public BasicAnimation setMode(Mode mode) {
        mAlpha.setMode(mode);
        return this;
    }

    public BasicAnimation setAutoReverse(boolean autoReverse) {
        mTimeline.setAutoReverse(autoReverse);
        return this;
    }

    public boolean getAutoReverse() {
        return mTimeline.getAutoReverse();
    }

    /**
     * Set duration of animation.
     *
     * @param duration in milliseconds
     * @return the animation itself
     */
    public BasicAnimation setDuration(int duration) {
        mTimeline.setDuration(duration, duration);
        return this;
    }

    public int getDuration() {
        return mTimeline.getProgressDuration();
    }

    public void setProgress(float progress) {
        mTimeline.setProgress(progress);
    }

    public float getProgress() {
        return mTimeline.getProgress();
    }

    public BasicAnimation setLoop(boolean loop) {
        mTimeline.setLoop(loop);
        return this;
    }

    public boolean getLoop() {
        return mTimeline.getLoop();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Animation

    @Override
    public Animation start() {
        Actor target = getTarget();
        if (target != null || (mOptions & CAN_START_WITHOUT_TARGET) != 0) {
            mTimeline.start();
        }

        return this;
    }

    @Override
    public Animation startDragging() {
        disableOptions(Animation.START_TARGET_WITH_INITIAL_VALUE);
        setDirection(Animation.FORWARD);
        setProgress(0);
        setTargetVisible(true);
        return this;
    }

    @Override
    public Animation pause() {
        mTimeline.pause();
        return this;
    }

    @Override
    public Animation stop() {
        mTimeline.stop();
        return this;
    }

    @Override
    public Animation stopDragging() {
        start();
        return this;
    }

    @Override
    public Animation reset() {
        mTimeline.rewind();
        return this;
    }

    @Override
    public Animation complete() {
        mTimeline.complete();
        return this;
    }

    @Override
    public boolean isStarted() {
        return mTimeline.isStarted();
    }

    @Override
    public void setTimeScale(float scale) {
        mTimeline.setTimeScale(scale);
    }

    @Override
    public float getTimeScale() {
        return mTimeline.getTimeScale();
    }

    @Override
    public void setDirection(int direction) {
        mTimeline.setDirection(direction);
    }

    @Override
    public int getDirection() {
        return mTimeline.getDirection();
    }

    @Override
    public void reverse() {
        mTimeline.reverse();
    }

    @Override
    public Animation setTargetVisible(boolean visible) {
        Actor target = getTarget();
        if (target != null) {
            target.setVisible(visible);
        }
        return this;
    }

    /**
     * Wait for an animation to complete. A stopped animation is treated as a completed one.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    public void waitForCompletion() throws InterruptedException {
        mTimeline.waitForCompletion();
    }

    /**
     * Clone the BasicAnimation, value in each member of cloned BasicAnimation is same of original one, except Timeline and Alpha.
     * Mew instance of Timeline and Alpha will be created for cloned BasicAnimation.
     * @return the cloned animation
     */
    @Override
    public BasicAnimation clone() {
        BasicAnimation animation = (BasicAnimation) super.clone();
        animation.setTimeline(mTimeline.clone());
        animation.setAlphaMode(mAlpha.getMode());
        return animation;
    }
}
