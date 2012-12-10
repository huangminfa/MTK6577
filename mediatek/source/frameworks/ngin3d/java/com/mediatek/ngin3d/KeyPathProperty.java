package com.mediatek.ngin3d;

import java.security.InvalidParameterException;

/**
 * KeyPathProperty represents a property with name in the format of dot-separated key, e.g. a.b.c.
 */
public class KeyPathProperty<T> extends Property<T> {
    
    protected String[] mKeys;
    protected String mParentKey;

    public KeyPathProperty(String keyPath) {
        this(keyPath, 0);
    }

    public KeyPathProperty(String key, int flags) {
        super(key, null, flags);

        mKeys = key.split("\\.");
        if (getFirstKey().length() == 0) {
            throw new InvalidParameterException("Empty keyPath was passed.");
        }
    }

    public int getKeyPathLength() {
        return mKeys.length;
    }

    public String getKey(int index) {
        return mKeys[index];
    }

    public String getFirstKey() {
        if (mKeys.length < 1) {
            return "";
        }

        return mKeys[0];
    }

    public String getLastKey() {
        if (mKeys.length < 1) {
            return "";
        }

        return mKeys[mKeys.length - 1];
    }

    public String getParentKeyPath() {
        if (mKeys.length < 2) {
            return "";
        }

        if (mParentKey == null) {
            StringBuilder sb = new StringBuilder(mKeys[0]);

            int last = mKeys.length - 1;
            for (int i = 1; i < last; i++) {
                sb.append(".");
                sb.append(mKeys[i]);
            }

            mParentKey = sb.toString(); // Cache it!
        }
        return mParentKey;
    }
    

    @Override
    public boolean equals(Object o) {
        // If the key is the same, we treat them as the same.
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
