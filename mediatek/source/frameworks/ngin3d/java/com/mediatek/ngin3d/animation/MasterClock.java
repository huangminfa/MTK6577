package com.mediatek.ngin3d.animation;

import java.util.ArrayList;

/**
 * The clock to drive timelines.
 * @hide
 */
public class MasterClock {

    private static MasterClock sMasterClock = new MasterClock();

    private final ArrayList<Timeline> mTimelines = new ArrayList<Timeline>();
    private final ArrayList<Timeline> mTimelinesToAdd = new ArrayList<Timeline>();
    private final ArrayList<Timeline> mTimelinesToRemove = new ArrayList<Timeline>();

    private double mTimeScale;

    public static MasterClock getDefault() {
        return sMasterClock;
    }

    public static void setDefault(MasterClock clock) {
        sMasterClock = clock;
    }

    public static void register(Timeline timeline) {
        getDefault().registerTimeline(timeline);
    }

    public static void unregister(Timeline timeline) {
        getDefault().unregisterTimeline(timeline);
    }

    public static void cleanup() {
        getDefault().removeAllTimelines();
    }

    public MasterClock() {
        mTimeScale = 1.0;
    }

    public void registerTimeline(Timeline timeline) {
        synchronized (this) {
            if (timeline == null) {
                throw new IllegalArgumentException("timeline cannot be null");
            }
            if (!mTimelinesToRemove.remove(timeline)) {
                mTimelinesToAdd.add(timeline);
            }
        }
    }

    public void unregisterTimeline(Timeline timeline) {
        synchronized (this) {
            if (!mTimelinesToAdd.remove(timeline)) {
                mTimelinesToRemove.add(timeline);
            }
        }
    }

    public boolean isTimelineRegistered(Timeline timeline) {
        return mTimelines.contains(timeline);
    }

    public double getTimeScale() {
        return mTimeScale;
    }

    /**
     * To modify the time scale of master clock for slower or faster animation.
     *
     * @param timeScale < 1.0 for slower and > 1.0 for faster
     */
    public void setTimeScale(double timeScale) {
        if (timeScale < 0.0) {
            throw new IllegalArgumentException("timeScale cannot be nagative");
        }
        mTimeScale = timeScale;
    }

    public long getTickTime() {
        long tickTime = System.currentTimeMillis();
        if (mTimeScale != 1.0) {
            tickTime *= mTimeScale;
        }
        return tickTime;
    }

    /**
     * Pause all timelines
     */
    public void pause() {
        for (Timeline timeline : mTimelines) {
            timeline.freeze();
        }
    }

    /**
     * In order to resume rendering and have continuous animation,
     * we need resume all timelines from current tick time.
     */
    public void resume() {
        for (Timeline timeline : mTimelines) {
            timeline.unfreeze();
        }
    }

    /**
     * Called typically in rendering thread to drive timelines.
     */
    public void tick() {
        tick(getTickTime());
    }

    public void tick(long tickTime) {
        applyTimelineRegistration();

        int size = mTimelines.size(); // Use indexing rather than iterator to prevent frequent GC
        for (int i = 0; i < size; ++i) {
            mTimelines.get(i).doTick(tickTime);
        }

        applyTimelineRegistration();
    }

    private void applyTimelineRegistration() {
        synchronized (this) {
            if (!mTimelinesToRemove.isEmpty()) {
                mTimelines.removeAll(mTimelinesToRemove);
                mTimelinesToRemove.clear();
            }

            if (!mTimelinesToAdd.isEmpty()) {
                mTimelines.addAll(mTimelinesToAdd);
                mTimelinesToAdd.clear();
            }
        }
    }

    private void removeAllTimelines() {
        synchronized (this) {
            mTimelines.clear();
            mTimelinesToAdd.clear();
            mTimelinesToRemove.clear();
        }
    }
}
