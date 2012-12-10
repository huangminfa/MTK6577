package com.mediatek.ngin3d.animation;

import java.util.ArrayList;

/**
 * The group of timeline.
 * @hide
 */
public class TimelineGroup extends Timeline implements Timeline.Owner {
    /**
     * Construct a timeline with specified duration.
     *
     * @param duration in milliseconds
     */
    public TimelineGroup(int duration) {
        super(duration);
    }

    @Override
    protected boolean isComplete() {
        return super.isComplete() && mTimelines.isEmpty();
    }

    private void stopAndComplete() {
        stop();
        onComplete(mCurrentTickTime);
    }

    @Override
    public void doTick(long tickTime) {
        applyTimelineRegistration();

        int size = mTimelines.size();
        for (int i = 0; i < size; ++i) {
            Timeline t = mTimelines.get(i);
            t.doTick(tickTime);
        }

        applyTimelineRegistration();

        if (mWaitingFirstTick) {
            mStartedTickTime = tickTime;
            mLastFrameTickTime = tickTime;
            mWaitingFirstTick = false;
        } else {
            long delta = tickTime - mLastFrameTickTime;
            if (updateDeltaTime(delta)) {
                doFrame(tickTime);
            }
        }

        // Automatically stop the group when all children are stopped.
        if (isStarted() && mTimelines.isEmpty()) {
            stopAndComplete();
        }
    }

    @Override
    protected boolean updateDeltaTime(long delta) {
        if (delta < 0) {
            return false; // skip one frame
        } else if (delta != 0) {
            mLastFrameTickTime += delta;
            /**
             * In the case that TimelineGroup is completed but the children in it doesn't,
             * we need to accumulate mDeltaTime to make Marker callback work correctly.
             */
            if (super.isComplete()) {
                mDeltaTime += (int) (delta * mTimeScale);
            } else {
                mDeltaTime = (int) (delta * mTimeScale);
            }
        }
        return true;
    }

    @Override
    protected boolean onComplete(long tickTime) {
        if (mDirection == FORWARD) {
            mElapsedTime = mDuration;
        } else {
            mElapsedTime = 0;
        }
        return super.onComplete(tickTime);

    }

    private final ArrayList<Timeline> mTimelines = new ArrayList<Timeline>();
    private final ArrayList<Timeline> mTimelinesToAdd = new ArrayList<Timeline>();
    private final ArrayList<Timeline> mTimelinesToRemove = new ArrayList<Timeline>();

    public void register(Timeline timeline) {
        synchronized (this) {
            if (timeline == null) {
                throw new IllegalArgumentException("timeline cannot be null");
            }
            if (!mTimelinesToRemove.remove(timeline)) {
                mTimelinesToAdd.add(timeline);
            }
        }
    }

    public void unregister(Timeline timeline) {
        synchronized (this) {
            if (!mTimelinesToAdd.remove(timeline)) {
                mTimelinesToRemove.add(timeline);
            }
        }
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

    public void attach(Timeline timeline) {
        timeline.setOwner(this);
    }

    public void detach(Timeline timeline) {
        timeline.setOwner(null);
    }

    public boolean isEmpty() {
        return mTimelines.isEmpty();
    }

    @Override
    public void freeze() {
        super.freeze();
        for (Timeline timeline : mTimelines) {
            timeline.freeze();
        }
    }

    @Override
    public void unfreeze() {
        super.unfreeze();
        for (Timeline timeline : mTimelines) {
            timeline.unfreeze();
        }
    }
}
