package com.mediatek.ngin3d;

import com.mediatek.util.JSON;

/**
 * Fixed value in contrast to dynamic expression.
 * @hide
 */
public class FixedValue<T> extends Value<T> implements JSON.ToJson {
    protected T mValue;

    public FixedValue(T value) {
        mValue = value;
    }

    public FixedValue(T value, boolean dirty) {
        super(dirty);
        mValue = value;
    }

    public T get() {
        synchronized (this) {
            return mValue;
        }
    }

    public T getAndClean() {
        synchronized (this) {
            mDirty = false;
            return mValue;
        }
    }

    public boolean set(T value) {
        synchronized (this) {
            mValue = value;
            return true;
        }
    }

    public boolean setAndDirty(T value) {
        synchronized (this) {
            mValue = value;
            mDirty = true;
            return true;
        }
    }

    @Override
    public String toString() {
        if (mDirty) {
            return "*" + mValue.toString();
        }
        if (mValue == null) {
            return "null";
        } else {
            return mValue.toString();
        }
    }

    public String toJson() {
        synchronized (this) {
            return JSON.toJson(mValue);
        }
    }
}
