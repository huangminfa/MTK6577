package com.mediatek.ngin3d.animation;

import android.util.Log;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.EulerOrder;
import com.mediatek.ngin3d.Plane;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;

/**
 * Keyframe animation that animate an actor by specified keyframe data.
 */
public class KeyframeAnimation extends BasicAnimation {
    private static final String TAG = "KeyframeAnimation";
    private final Samples mSamples;
    private KeyframeInterpolator mInterpolator;
    private Actor mTarget;
    private boolean mNormalized;

    public KeyframeAnimation(KeyframeData kfData) {
        this(null, kfData);
    }

    public KeyframeAnimation(Actor target, KeyframeData kfData) {
        int duration;
        mSamples = kfData.getSamples();
        mNormalized = kfData.isNormalized();

        setTarget(target);
        float[] time = mSamples.get(Samples.KEYFRAME_TIME);
        if (time == null) {
            duration = 0;
        } else {
            duration = (int) (time[time.length - 1] * 1000);
        }
        setDuration(duration);
        setupTimelineListener();
    }

    private void setupTimelineListener() {
        mTimeline.addListener(new Timeline.Listener() {
            public void onStarted(Timeline timeline) {
                if ((mOptions & DEBUG_ANIMATION_TIMING) != 0) {
                    Log.d(TAG, String.format("%d: Animation %s is started", System.currentTimeMillis(), KeyframeAnimation.this));
                }
                if (mTarget != null) {
                    mTarget.onAnimationStarted(getAnimationKey(mSamples), KeyframeAnimation.this);
                    // If this is an alpha animation, change alpha source of target automatically.
                    if (mSamples.getType() == Samples.ALPHA) {
                        if (mTarget instanceof Plane) {
                            ((Plane)mTarget).setAlphaSource(Plane.FROM_TEXEL_VERTEX);
                        }
                    }
                }

                if ((mOptions & START_TARGET_WITH_INITIAL_VALUE) != 0) {
                    if (getDirection() == FORWARD) {
                        onAnimate(0);
                    } else {
                        onAnimate(timeline.getProgressDuration());
                    }
                }
            }

            public void onNewFrame(Timeline timeline, int elapsedMsecs) {
                onAnimate((float) elapsedMsecs);
            }

            public void onMarkerReached(Timeline timeline, int elapsedMsecs, String marker, int direction) {
                // KeyframeAnimation onMarkerReached callback function
            }

            public void onPaused(Timeline timeline) {
                if (mTarget != null) {
                    mTarget.onAnimationStopped(getAnimationKey(mSamples));
                }
            }

            public void onCompleted(Timeline timeline) {
                if ((mOptions & DEBUG_ANIMATION_TIMING) != 0) {
                    Log.d(TAG, String.format("%d: Animation %s is completed, target is %s", System.currentTimeMillis(), KeyframeAnimation.this, mTarget));
                }
                if ((mOptions & Animation.BACK_TO_START_POINT_ON_COMPLETED) == 0) {
                    if (getDirection() == Timeline.FORWARD) {
                        onAnimate(timeline.getProgressDuration());
                    } else {
                        onAnimate(0);
                    }
                } else {
                    if (getDirection() == Timeline.FORWARD) {
                        onAnimate(0);
                    } else {
                        onAnimate(timeline.getProgressDuration());
                    }
                }

                if (mTarget != null) {
                    mTarget.onAnimationStopped(getAnimationKey(mSamples));
                }
            }

            public void onLooped(Timeline timeline) {
                // do nothing now
            }
        });
    }

    private void onAnimate(float timeMs) {
        if (mTarget == null) {
            return;
        }

        float currTime = timeMs / 1000;
        Object value = mInterpolator.getValue(currTime);
        if (value == null)
            return;

        switch (mSamples.getType()) {
        case Samples.ANCHOR_POINT:
            mTarget.setAnchorPoint((Point)value);
            break;
        
        case Samples.TRANSLATE:
            if (mNormalized) {
                ((Point)value).isNormalized = true;
            }
            mTarget.setPosition((Point)value);
            break;

        case Samples.ROTATE:
            Rotation rot = (Rotation)value;
            if ((mOptions & Animation.LOCK_Z_ROTATION) != 0) {
                float[] newValue = rot.getEulerAngles(EulerOrder.ZYX);
                float[] target = mTarget.getRotation().getEulerAngles(EulerOrder.ZYX);
                rot.set(EulerOrder.ZYX, newValue[0], newValue[1], target[2]);
            }
            mTarget.setRotation(rot);
            break;

        case Samples.X_ROTATE:
            mTarget.setRotation((Rotation)value);
            break;

        case Samples.Y_ROTATE:
            mTarget.setRotation((Rotation)value);
            break;

        case Samples.Z_ROTATE:
            if ((mOptions & Animation.LOCK_Z_ROTATION) == 0) {
                mTarget.setRotation((Rotation)value);
            }
            break;

        case Samples.SCALE:
            mTarget.setScale((Scale)value);
            break;

        case Samples.ALPHA:
            int opacity = (int) (2.55 * (Float) value);
            if (mTarget instanceof Container) {
                Container c = (Container) mTarget;
                c.setOpacity(opacity);
            } else if (mTarget instanceof Plane) {
                Plane plane = (Plane) mTarget;
                plane.setOpacity(opacity);
            }
            break;

        default:
            // do nothing.
            break;
        }
    }

    private static String getAnimationKey(Samples samples) {
        String value = "";
        switch (samples.getType()) {
        case Samples.ANCHOR_POINT:
            value = "anchor";
            break;

        case Samples.TRANSLATE:
            value = "position";
            break;

        case Samples.ROTATE:
            break;
        case Samples.X_ROTATE:
            break;
        case Samples.Y_ROTATE:
            break;
        case Samples.Z_ROTATE:
            value = "rotation";
            break;

        case Samples.SCALE:
            value = "scale";
            break;

        case Samples.ALPHA:
            value = "alpha";
            break;

        default:
            throw new IllegalArgumentException("Unknown samples type: " + samples.getType());
        }
        return value;
    }

    @Override
    public final Animation setTarget(Actor target) {
        mTarget = target;
        // Avoid two Actors reference the same mValue when animation switch target actor and restart in one tick.
        mInterpolator =  new KeyframeInterpolator(mSamples);
        return this;
    }

    @Override
    public Actor getTarget() {
        return mTarget;
    }

    @Override
    public Animation reset() {
        super.reset();
        if (getDirection() == FORWARD) {
            onAnimate(0);
        } else {
            onAnimate(getDuration());
        }

        return this;
    }

    @Override
    public Animation complete() {
        super.complete();
        if (getDirection() == FORWARD) {
            onAnimate(getDuration());
        } else {
            onAnimate(0);
        }
        return this;
    }

    /**
     * Clone the KeyframeAnimation, value in each member of cloned animation is same of original one, except target.
     * Mew instance of KeyframeAnimation has no target in default.
     * @return the cloned KeyframeAnimation
     */
    @Override
    public KeyframeAnimation clone() {
        KeyframeAnimation animation = (KeyframeAnimation) super.clone();
        animation.setupTimelineListener();
        animation.mTarget = null;
        return animation;
    }
}
