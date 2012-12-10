package com.mediatek.ngin3d;

import java.util.Arrays;

/**
 * Represents properties of class in model tree. A property can have name, default value, and dependency on another property.
 * If property B depends on property A, the value of property will be applied before value of property B.
 */
public class Property<T> {
    protected String mName;
    protected T mDefaultValue;
    protected Property[] mDependsOn;
    protected int mFlags;

    public static final int FLAG_ANIMATABLE = 0x0001;

    public Property(String name, T defaultValue, Property... dependsOn) {
        mName = name;
        mDefaultValue = defaultValue;
        mDependsOn = dependsOn;
    }

    public Property(String name, T defaultValue, int flags, Property... dependsOn) {
        mName = name;
        mDefaultValue = defaultValue;
        mFlags = flags;
        mDependsOn = dependsOn;
    }

    public boolean isAnimatable() {
        return (mFlags & FLAG_ANIMATABLE) != 0;
    }

    public String getName() {
        return mName;
    }

    public T defaultValue() {
        return mDefaultValue;
    }

    public boolean dependsOn(Property other) {
        if (this == other) {
            return false;
        }

        for (Property dep : mDependsOn) {
            if (dep.dependsOn(other)) {
                return true;
            }
        }

        return false;
    }

    public void addDependsOn(Property... dependsOn) {
        Property[] merged = new Property[mDependsOn.length + dependsOn.length];
        System.arraycopy(mDependsOn, 0, merged, 0, mDependsOn.length);
        System.arraycopy(dependsOn, 0, merged, mDependsOn.length, dependsOn.length);
        mDependsOn = merged;
    }

    Property[] getDependsOn() {
        return mDependsOn;
    }

    @Override
    public int hashCode() {
        int result = mName == null ? 0 : mName.hashCode();
        result = 31 * result + (mDefaultValue == null ? 0 : mDefaultValue.hashCode());
        result = 31 * result + (mDependsOn == null ? 0 : Arrays.hashCode(mDependsOn));
        result = 31 * result + mFlags;
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Property property = (Property) o;

        if (mFlags != property.mFlags) return false;
        if (mDefaultValue == null ? property.mDefaultValue != null : !mDefaultValue.equals(property.mDefaultValue)) return false;
        if (!Arrays.equals(mDependsOn, property.mDependsOn)) return false;
        if (mName == null ? property.mName != null : !mName.equals(property.mName)) return false;

        return true;
    }

    @Override
    public String toString() {
        return mName;
    }

    public final boolean sameInstance(Property another) {
        return this == another;
    }
}
