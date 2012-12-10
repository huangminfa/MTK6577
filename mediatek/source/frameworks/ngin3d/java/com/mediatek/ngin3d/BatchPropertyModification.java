package com.mediatek.ngin3d;

/**
 * A transaction to do multiple property modifications at the same time. Note that there
 * is no animation and these properties are modified instantly.
 */
public class BatchPropertyModification extends Transaction {

    private boolean mDone;

    ///////////////////////////////////////////////////////////////////////////
    // Operations

    private class PropertyModification extends Modification {
        public Actor mTarget;
        public Property mProperty;

        PropertyModification(Actor target, Property property) {
            this.mTarget = target;
            this.mProperty = property;
        }

        public void apply() {
            // The value of property is saved and one thing we need to do is make it dirty
            mTarget.touchProperty(mProperty);

            synchronized (BatchPropertyModification.this) {
                mDone = true;
                BatchPropertyModification.this.notifyAll();
            }
        }
    }

    @Override
    public void addPropertyModification(Actor target, Property property, Object value) {
        getModificationList().add(new PropertyModification(target, property));
    }

    public void waitForCompletion() throws InterruptedException {
        synchronized (this) {
            while (!mDone) {
                wait();
            }
        }
    }
}