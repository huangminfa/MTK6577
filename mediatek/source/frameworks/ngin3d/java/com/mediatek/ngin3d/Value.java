package com.mediatek.ngin3d;

/**
 * Represents property value that can be set by model tree and read by presentation tree.
 * @hide
 */
public abstract class Value<T> {
    protected boolean mDirty;

    public Value() {
        this(true);
    }

    public Value(boolean dirty) {
        mDirty = dirty;
    }

    public boolean isDirty() {
        synchronized (this) {
            return mDirty;
        }
    }

    public void setDirty() {
        synchronized (this) {
            mDirty = true;
        }
    }

    public abstract T get();

    public abstract T getAndClean();

    public abstract boolean set(T value);

    public abstract boolean setAndDirty(T value);
}
