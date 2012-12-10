package com.mediatek.ngin3d;

import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.PropertyAnimation;

/**
 * A transaction to start implicit animation.
 */
public class ImplicitAnimation extends Transaction {

    private boolean mDone;

    ///////////////////////////////////////////////////////////////////////////
    // Operations

    private class PropertyModification extends Modification {
        public Actor mTarget;
        public Property mProperty;
        public Object mFromValue;
        public Object mToValue;

        PropertyModification(Actor target, Property property, Object value) {
            this.mTarget = target;
            this.mProperty = property;
            this.mToValue = value;

            // Try getting 'from value' from presentation first.
            if (value instanceof Point && ((Point) value).isNormalized) {
                // Special handling for normalized position
                mFromValue = mTarget.getPresentationValue(mProperty, Actor.FLAG_NORMALIZED);
            } else {
                mFromValue = mTarget.getPresentationValue(mProperty);
            }

            // If presentation value is not available, use the one in model.
            if (mFromValue == null) {
                mFromValue = target.getValue(property);
            }
        }

        public BasicAnimation buildAnimation() {
            return new PropertyAnimation(mTarget, mProperty, mFromValue, mToValue).setMode(mAlphaMode).setDuration(mAnimationDuration);
        }

        @Override
        protected void apply() {
            BasicAnimation animation = buildAnimation();
            animation.addListener(new Animation.Listener() {
                @Override
                public void onPaused(Animation animation) {
                    if (mCompletion != null) {
                        mCompletion.run();
                    }

                    synchronized (ImplicitAnimation.this) {
                        mDone = true;
                        ImplicitAnimation.this.notifyAll();
                    }
                }
            });

            animation.start();
        }
    }

    @Override
    public void addPropertyModification(Actor target, Property property, Object value) {
        getModificationList().add(new PropertyModification(target, property, value));
    }

    public void waitForCompletion() throws InterruptedException {
        synchronized (this) {
            while (!mDone) {
                wait();
            }
        }
    }
}