package com.mediatek.ngin3d;

import com.mediatek.util.JSON;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A base class for actor properties setting.
 * @hide
 */
public abstract class Base {

    /**
     * The property chain to dispatch attached properties and query inherited properties.
     */
    interface PropertyChain {
        /**
         * To handle attached properties.
         *
         * @param obj      the object that the property is attached to
         * @param property the property key
         * @param value    the value
         * @return true if the property value change is handled. false otherwise.
         */
        boolean applyAttachedProperty(Base obj, Property property, Object value);

        /**
         * Query properties that can be inherited from the chain.
         */
        Object getInheritedProperty(Property property);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Property system

    private PropertyChain mPropertyChain;

    /**
     * @hide
     */
    protected void setPropertyChain(PropertyChain chain) {
        mPropertyChain = chain;
    }

    /**
     * HashMap to store all non-default property values. In addition to properties set by the class itself, the
     * container may also 'attach' properties to it.
     */
    private final ConcurrentHashMap<Property, Value> mValues = new ConcurrentHashMap<Property, Value>();

    /**
     * Touch property and make it dirty.
     * @param property  selected property
     * @hide
     */
    public <T> void touchProperty(Property<T> property) {
        if (property != null) {
            Value value = mValues.get(property);
            if (value != null) {
                value.setDirty();
            }
        }
    }

    /**
     * Touch property and make it dirty.
     * @param propertyName  selected property name
     * @hide
     */
    public <T> void touchProperty(String propertyName) {
        touchProperty(getProperty(propertyName));
    }

    /**
     * Sets the value of selected property.
     * @param property  selected property
     * @param newValue  new value of this property
     * @param dirty  make value dirty or not
     * @return  true if the value is updated successfully.
     * @hide
     */
    public <T> boolean setValue(Property<T> property, T newValue, boolean dirty) {
        return updateValue(property, newValue, dirty);
    }

    /**
     * Sets the value of selected property.
     *
     * @param property selected property
     * @param newValue new value of this property
     * @return true if the value is updated successfully.
     * @hide
     */
    public <T> boolean setValue(Property<T> property, T newValue) {
        return updateValue(property, newValue, true);
    }

    /**
     * Updates the value of selected property.
     *
     * @param property selected property
     * @param newValue new value of this property
     * @return true if the value is updated successfully.
     * @hide
     */
    public final <T> boolean updateValue(Property<T> property, T newValue, boolean dirty) {
        Value existing = mValues.get(property);
        if (existing == null) {
            Value value = new FixedValue<T>(newValue, dirty);
            mValues.put(property, value);

        } else {
            boolean success;
            if (dirty) {
                success = existing.setAndDirty(newValue);
            } else {
                success = existing.set(newValue);
            }
            if (!success) {
                Value value = new FixedValue<T>(newValue, dirty);
                mValues.put(property, value);
            }
        }


        return true;
    }

    /**
     * Gets the value of specified property
     *
     * @param property specified property
     * @return value
     * @hide
     */
    public <T> T getValue(Property<T> property) {
        Value value = mValues.get(property);
        return value == null ? property.defaultValue() : (T) value.get();
    }

    /**
     * Sets property value of specified key path.
     *
     * @param keyPath a string to identify the dynamic property uniquely
     * @param value new value of this property
     * @return true if the value is updated successfully.
     */
    public <T> boolean setKeyPathValue(String keyPath, Object value) {
        Property property = new KeyPathProperty(keyPath);
        return updateValue(property, value, true);
    }

    /**
     * Gets property value of specified of key path.
     * @param keyPath key path
     * @return value
     */
    public Object getKeyPathValue(String keyPath) {
        Property property = new KeyPathProperty(keyPath);
        Value value = mValues.get(property);
        return value == null ? null : value.get();
    }

    /**
     * @hide
     */
    protected boolean applyLocalValueWithDependency(Property property, Object value) {
        boolean applied;

        // Apply all dependent properties first.
        for (Property dep : property.getDependsOn()) {
            Value val = mValues.get(dep);
            if (val != null && val.isDirty()) {
                applied = applyLocalValueWithDependency(dep, val.getAndClean()); // recursive call
                if (!applied) {
                    val.setDirty();
                    return false; // won't continue if the dependency cannot be applied
                }
            }
        }

        // Then apply target value directly.
        applied = applyValue(property, value);

        // It may be an attached property if it cannot be applied.
        if (!applied && mPropertyChain != null) {
            // Dispatch all unhandled properties to next one in property chain
            applied = mPropertyChain.applyAttachedProperty(this, property, value);
        }

        return applied;
    }

    protected abstract boolean applyValue(Property property, Object value);

    private static final String PROPERTY_NAME_PREFIX = "PROP_";
    private static final String PROPERTY_MID_PREFIX = "mId";
    private static final String PROPERTY_MTAG_PREFIX = "mTag";

    /**
     * Use refection to apply all properties inherited from super classes. It should be done
     * only once when presentation is created because of its performance penalty.
     * @hide
     */
    protected void applyAllProperties() {
        mApplyFlags = 0;
        List<Property> properties = new ArrayList<Property>();

        // Get property list first
        Class clazz = this.getClass();
        while (clazz != Base.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().startsWith(PROPERTY_NAME_PREFIX)) {
                    try {
                        properties.add((Property) field.get(null));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        // Sort for dependencies
        Collections.sort(properties, new Comparator<Property>() {

            public int compare(Property prop1, Property prop2) {
                if (prop1.dependsOn(prop2)) {
                    return 1;   // prop1 depends on prop2
                }
                if (prop2.dependsOn(prop1)) {
                    return -1;  // prop2 depends on prop1
                }

                // no dependencies, sort by name.
                return prop1.getName().compareTo(prop2.getName());
            }
        });

        for (Property prop : properties) {
            applyProperty(prop);
        }

        if ((mApplyFlags & FLAG_APPLY_LATER_IN_BATCH) != 0) {
            applyBatchValues();
        }
    }

    private void applyProperty(Property prop) {
        Value value = mValues.get(prop);

        boolean applied;
        if (value == null) { // apply default value
            final Object defaultValue = prop.defaultValue();
            applied = applyValue(prop, defaultValue);
            if (!applied) {
                // something wrong with the property
                throw new RuntimeException("Failed to apply " + prop.getName() 
                        + " with default value " + (defaultValue == null ? "null" : defaultValue.toString()));
            }
        } else { // apply local value
            applied = applyLocalValueWithDependency(prop, value.getAndClean());
            if (!applied) {
                value.setDirty(); // make it dirty again so that it can be applied later.
            }
        }
    }

    /**
     * Apply all dirty property changes. Can only be called when presentation is initialized.
     * @hide
     */
    protected void applyDirtyValues() {
        mApplyFlags = 0;

        for (Map.Entry entry : mValues.entrySet()) {
            Value value = (Value) entry.getValue();
            Property property = (Property) entry.getKey();
            if (!value.isDirty()) {
                continue;
            }
            Object val = value.getAndClean();
            applyLocalValueWithDependency(property, val);
        }

        if ((mApplyFlags & FLAG_APPLY_LATER_IN_BATCH) != 0) {
            applyBatchValues();
        }
        updateStreamingTexture();
    }

    /**
     * @hide
     */
    protected static final int FLAG_APPLY_LATER_IN_BATCH = 0x0001;
    /**
     * @hide
     */
    protected int mApplyFlags;

    /**
     * Called in applyValue to trigger batch apply.
     * @hide
     */
    protected void enableApplyFlags(int flags) {
        mApplyFlags |= flags;
    }

    /**
     * Override to apply multiple value changes in a batch.
     * @hide
     */
    protected abstract void applyBatchValues();

    /**
     * Override to update streaming texture.
     * @hide
     */
    protected abstract void updateStreamingTexture();

    /**
     * Check whether there is any dirty value that are not applied yet.
     *
     * @return true if any dirty value exists
     * @hide
     */
    protected boolean dirtyValueExists() {
        for (Map.Entry entry : mValues.entrySet()) {
            Value value = (Value) entry.getValue();
            if (value.isDirty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find property by name.
     *
     * @param name property name
     * @return property object if found. null otherwise.
     */
    public Property getProperty(String name) {
        Class clazz = this.getClass();
        while (clazz != Base.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().startsWith(PROPERTY_NAME_PREFIX)) {
                    try {
                        Property prop = (Property) field.get(null);
                        if (prop.getName().equals(name)) {
                            return prop;
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

    /**
     * Dump properties for debugging.
     */
    public String dumpProperties() {
        return dumpProperties(false);
    }

    public String dumpProperties(boolean hasAnimation) {
        StringBuffer buffer = new StringBuffer();

        Class clazz = this.getClass();
        while (clazz != Base.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().startsWith(PROPERTY_NAME_PREFIX)) {
                    try {
                        Property prop = (Property) field.get(null);
                        Object value = mValues.get(prop);

                        if (value == null) {
                            if (prop.defaultValue() == null) {
                                buffer.append(wrapProperty(prop.getName(), "null"));
                            } else {
                                buffer.append(wrapProperty(prop.getName(), JSON.toJson(prop.defaultValue())));
                            }
                        } else {
                            buffer.append(wrapProperty(prop.getName(), JSON.toJson(value)));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else if (field.getName().equals(PROPERTY_MID_PREFIX)) {
                    try {
                        String s1 = wrapProperty("mId", Integer.toString(field.getInt(this)));
                        buffer.append(s1);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else if (field.getName().equals(PROPERTY_MTAG_PREFIX)) {
                    try {
                        String s2 = wrapProperty("mTag", Integer.toString(field.getInt(this)));
                        buffer.append(s2);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        String s3;
        if (hasAnimation) {
            s3 = wrapProperty("mAnimation", "1");
        } else {
            s3 = wrapProperty("mAnimation", "0");
        }

        buffer.append(s3);

        // To compatible with JSON format
        buffer.deleteCharAt(buffer.length() - 1);
        return buffer.toString();
    }

    private String wrapProperty(String name, String value) {
        if ("name".equals(name) || "text_typeface".equals(name) || "text".equals(name)) {
            return name + ":" + JSONObject.quote(value) + ",";
        }
        return name + ":" + value + ",";
    }

    ///////////////////////////////////////////////////////////////////////////
    // Conversion helper

    static float asFloat(Object obj) {
        return ((Number) obj).floatValue();
    }

    static double asDouble(Object obj) {
        return ((Number) obj).doubleValue();
    }

    static int asInt(Object obj) {
        return ((Number) obj).intValue();
    }

    @SuppressWarnings("unused")
    static byte asByte(Object obj) {
        return ((Number) obj).byteValue();
    }

    static long asLong(Object obj) {
        return ((Number) obj).longValue();
    }

    static boolean asBoolean(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue() == 1;
        } else {
            return (Boolean) obj;
        }
    }

    static char asChar(Object obj) {
        return (Character) obj;
    }
}
