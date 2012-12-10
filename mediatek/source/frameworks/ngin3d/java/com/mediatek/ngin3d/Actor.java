package com.mediatek.ngin3d;

import android.util.Log;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.presentation.Presentation;
import com.mediatek.ngin3d.presentation.PresentationEngine;
import com.mediatek.ngin3d.utils.Ngin3dException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.util.JSON;

/**
 * Base abstract class for all visual stage actors.
 */
public class Actor<T extends Presentation> extends Base {
    /**
     * @hide
     */
    protected static final String TAG = "Ngin3d";

    private static int sSerial;
    /**
     * Unique id of this actor
     * @hide
     */
    protected int mId;
    /**
     * Tag of this actor
     * @hide
     */
    protected int mTag;
    /**
     * Reactive status of this actor
     * @hide
     */
    protected boolean mReactive;
    /**
     * Owner of this actor
     * @hide
     */
    protected Object mOwner;
    /**
     * Presentation of this actor
     * @hide
     */
    protected T mPresentation;

    /**
     * Store properties that was locked, any modification of these properties will cause exception.
     * @hide
     */
    protected ArrayList<Property> mLockedProperties;

    /**
     * Initialize this actor
     */
    protected Actor() {
        mId = sSerial++;
        mReactive = true;
    }

    /**
     * Get the actor id.
     * @return  actor id
     */
    public int getId() {
        return mId;
    }

    /**
     * Set the actor tag.
     * @param tag actor tag
     */
    public void setTag(int tag) {
        mTag = tag;
    }

    /**
     * Get the actor tag.
     * @return actor tag
     */
    public int getTag() {
        return mTag;
    }

    /**
     * Set the actor owner.
     * @param owner actor owner
     */
    public void setOwner(Object owner) {
        mOwner = owner;
    }

    /**
     * Get the actor owner.
     * @return actor owner
     */
    public Object getOwner() {
        return mOwner;
    }

    public void setRenderingHint(String effect, boolean enable) {
        // Do nothing by default
    }

    /**
     * @hide
     */
    protected void applyBatchValues() {
        // Do nothing by default
    }

    /**
     * @hide
     */
    protected void updateStreamingTexture() {
        // Do nothing by default
    }

    /**
     * @hide
     */
    protected T createPresentation(PresentationEngine engine) {
        throw new Ngin3dException("Should be overrided to create Presentation object");
    }

    /**
     * @hide
     */
    @Override
    protected void setPropertyChain(PropertyChain chain) {
        super.setPropertyChain(chain);
    }

    /**
     * Get the Presentation of this actor
     * @return  presentation
     * @hide
     */
    public T getPresentation() {
        return mPresentation;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Property handling

    /**
     * @hide
     */
    protected static final int MAX_OPACITY = 255;

    /**
     * Name property of this actor
     * @hide
     */
    public static final Property<String> PROP_NAME = new Property<String>("name", "noname");
    /**
     * Rotation property of this actor
     * @hide
     */
    public static final Property<Rotation> PROP_ROTATION = new Property<Rotation>("rotation", new Rotation(0.f, 0.f, 0.f), Property.FLAG_ANIMATABLE);
    /**
     * Scale property of this actor
     * @hide
     */
    public static final Property<Scale> PROP_SCALE = new Property<Scale>("scale", new Scale(1, 1, 1), Property.FLAG_ANIMATABLE);
    /**
     * Visible property of this actor
     * @hide
     */
    public static final Property<Boolean> PROP_VISIBLE = new Property<Boolean>("visible", true);
    /**
     * Anchor point property of this actor
     * @hide
     */
    public static final Property<Point> PROP_ANCHOR_POINT = new Property<Point>("anchor", new Point(0.5f, 0.5f), Property.FLAG_ANIMATABLE);
    /**
     * Position property of this actor
     * @hide
     */
    public static final Property<Point> PROP_POSITION = new Property<Point>("position", new Point(0.f, 0.f, 0.f), Property.FLAG_ANIMATABLE, PROP_ANCHOR_POINT);
    /**
     * Color property of this actor
     * @hide
     */
    public static final Property<Color> PROP_COLOR = new Property<Color>("color", Color.WHITE);
    /**
     * Opacity property of this actor
     * @hide
     */
    public static final Property<Integer> PROP_OPACITY = new Property<Integer>("opacity", MAX_OPACITY);
    /**
     * Z order property of this actor
     * @hide
     */
    public static final Property<Integer> PROP_ZORDER_ON_TOP = new Property<Integer>("zorder_on_top", -1);

    /**
     * The display area of this actor
     * @hide
     */
    public static final Property<Box> PROP_DISPLAY_AREA = new Property<Box>("display_area", null);

    /**
     * Apply the value of specified property to presentation tree.
     *
     * @param property property object
     * @param value    property value
     * @return true if the properties are applied successfully
     * @hide
     */
    protected boolean applyValue(Property property, Object value) {
        if (property.sameInstance(PROP_POSITION)) {
            Point pos = (Point) value;
            mPresentation.setPosition(pos);
            return true;
        } else if (property.sameInstance(PROP_ROTATION)) {
            Rotation rotation = (Rotation) value;
            mPresentation.setRotation(rotation);
            return true;
        } else if (property.sameInstance(PROP_SCALE)) {
            Scale scale = (Scale) value;
            mPresentation.setScale(scale);
            return true;
        } else if (property.sameInstance(PROP_VISIBLE)) {
            mPresentation.setVisible(asBoolean(value));
            return true;
        } else if (property.sameInstance(PROP_ANCHOR_POINT)) {
            Point point = (Point) value;
            mPresentation.setAnchorPoint(point);
            return true;
        } else if (property.sameInstance(PROP_NAME)) {
            String name = (String) value;
            mPresentation.setName(name);
            return true;
        } else if (property.sameInstance(PROP_COLOR)) {
            Color color = (Color) value;
            mPresentation.setColor(color);
            return true;
        } else if (property.sameInstance(PROP_OPACITY)) {
            Integer opacity = (Integer) value;
            mPresentation.setOpacity(opacity);
            return true;
        } else if (property.sameInstance(PROP_ZORDER_ON_TOP)) {
            Integer zOrder = (Integer) value;
            mPresentation.setRenderZOrder(zOrder);
            return true;
        } else if (property.sameInstance(PROP_DISPLAY_AREA)) {
            Box area = (Box) value;
            mPresentation.setDisplayArea(area);
            return true;
        }

        return false;
    }

    /**
     * Flag for {@link #getPresentationValue(Property)}: returned value should be normalized
     * @hide
     */
    public static final int FLAG_NORMALIZED = 0x01;

    /**
     * For the same property, there are two values: the logic one and the visual one. The visual
     * value can be retrieved from presentation tree using this method.
     *
     * @param property property object, such as Actor.PROP_POSITION, to retrieve
     * @return visual value. Will return null if the value does not exist.
     */
    public Object getPresentationValue(Property property) {
        return getPresentationValue(property, 0);
    }

    /**
     * The same as the one with single property parameter except that you can specify an additional format modifier.
     *
     * @param property property object
     * @param flags    modifiers for value format, e.g. {@link #FLAG_NORMALIZED}
     * @return visual value. Will return null if the value does not exist.
     */
    public Object getPresentationValue(Property property, int flags) {
        if (mPresentation == null) {
            return null;
        }

        if (property.sameInstance(PROP_POSITION)) {
            return mPresentation.getPosition((flags & FLAG_NORMALIZED) != 0);
        } else if (property.sameInstance(PROP_ROTATION)) {
            return mPresentation.getRotation();
        } else if (property.sameInstance(PROP_SCALE)) {
            return mPresentation.getScale();
        } else if (property.sameInstance(PROP_VISIBLE)) {
            return mPresentation.getVisible();
        } else if (property.sameInstance(PROP_ANCHOR_POINT)) {
            return mPresentation.getAnchorPoint();
        } else {
            Log.w(TAG, "Unknown property name: " + property.getName());
        }

        return null;
    }

    private void checkPropertyLocked(Property property) {
        if (isPropertyLocked(property)) {
            throw new Ngin3dException(this + ": Property" + property + "is locked, can not be modified");
        }
    }

    /**
     * Add the new value of property into the active transaction.
     * @param property  the property in the transaction to be set.
     * @param newValue  the value to be set to the property in the transaction.
     * @return true if the property is set successfully
     * @hide
     */
    protected final <T> boolean setValueInTransaction(Property<T> property, T newValue) {
        // Check the property is locked or not, if true it will throw an exception.
        checkPropertyLocked(property);

        // Add value modification to active transaction.
        Transaction transaction = Transaction.getActive();
        if (transaction == null) {
            // Set the logic value
            return setValue(property, newValue);
        } else {
            setValue(property, newValue, false);
            transaction.addPropertyModification(this, property, newValue);
            return true;
        }
    }

    /**
     * Set the new property value for the actor object.
     * @param property the property to be set
     * @param newValue the new value to be set to the property.
     * @return true if the property is set successfully
     * @hide
     */
    @Override
    public final <T> boolean setValue(Property<T> property, T newValue, boolean dirty) {
        // Check the property is locked or not, if true it will throw an exception.
        checkPropertyLocked(property);

        if (super.setValue(property, newValue, dirty)) {
            requestRender();
            return true;
        }
        return false;
    }

    /**
     * Set the new property value for the actor object.
     * @param property the property to be set
     * @param newValue the new value to be set to the property.
     * @return true if the property is set successfully
     * @hide
     */
    @Override
    public final <T> boolean setValue(Property<T> property, T newValue) {
        return setValue(property, newValue, true);
    }

    /**
     * Renew this actor using new property value.
     * @hide
     */
    public void requestRender() {
        if (mPresentation != null) {
            mPresentation.requestRender();
        }
    }

    /**
     * Return the value is dirty.
     * @return true is the dirty value is set
     * @hide
     */
    public boolean isDirty() {
        return dirtyValueExists();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Property accessors

    /**
     * Set the name property of this actor
     * @param name the name to be set for actor
     */
    public void setName(String name) {
        setValue(PROP_NAME, name);
    }

    /**
     * Get the property of name.
     * @return the name of actor
     */
    public CharSequence getName() {
        return getValue(PROP_NAME);
    }

    /**
     * Set the position of actor.
     * @param pos the property of position to be set
     */
    public void setPosition(Point pos) {
        setValueInTransaction(PROP_POSITION, pos);
    }

    /**
     * Gets the position of this actor.
     * @return  property of position
     */
    public Point getPosition() {
        return getValue(PROP_POSITION);
    }

    /**
     * Sets the property of visible of this actor.
     * @param visible  a boolean value indicate that this object is visible or not
     */
    public void setVisible(boolean visible) {
        setValueInTransaction(PROP_VISIBLE, visible);
    }

    /**
     * Gets the property of visible
     * @return  true if the object is visible
     */
    public boolean getVisible() {
        return getValue(PROP_VISIBLE);
    }

    /**
     * Gets the truly visible status of object
     * @return  true if the object is truly visible
     */
    public boolean getTrulyVisible() {
        if (mPresentation == null) {
            return false;
        }
        return mPresentation.getTrulyVisible();
    }

    /**
     * Sets the property of rotation.
     * @param rotation  the Rotation value to be used for the property
     */
    public void setRotation(Rotation rotation) {
        setValueInTransaction(PROP_ROTATION, rotation);
    }

    /**
     * Gets the Rotation property of this actor.
     * @return the property of rotation
     */
    public Rotation getRotation() {
        return getValue(PROP_ROTATION);
    }

    /**
     * Sets the scale property of this actor.
     * @param scale the Scale value to be used for actor
     */
    public void setScale(Scale scale) {
        setValueInTransaction(PROP_SCALE, scale);
    }

    /**
     * Gets the scale property of this actor
     * @return the property of scale
     */
    public Scale getScale() {
        return getValue(PROP_SCALE);
    }

    /**
     * Sets the anchor point property of this actor.
     * @param anchorPoint the Point value to be used for actor
     */
    public void setAnchorPoint(Point anchorPoint) {
        if (anchorPoint.x < 0.0f || anchorPoint.x > 1.0f) {
            throw new IllegalArgumentException("x must be >= 0 and <= 1");
        } else if (anchorPoint.y < 0.0f || anchorPoint.y > 1.0f) {
            throw new IllegalArgumentException("y must be >= 0 and <= 1");
        }
        setValueInTransaction(PROP_ANCHOR_POINT, anchorPoint);
    }

    /**
     * Gets the anchor point of this actor.
     * @return the property of anchor point
     */
    public Point getAnchorPoint() {
        return getValue(PROP_ANCHOR_POINT);
    }

    /**
     * Sets the reactive value of this actor
     * @param reactive  a boolean value indicate that the object is reactive or not
     */
    public void setReactive(boolean reactive) {
        mReactive = reactive;
    }

    /**
     * Gets the reactive status of this actor.
     * @return true if the object is reactive
     */
    public boolean getReactive() {
        return mReactive;
    }

    /**
     * This method is used for debug only.
     * Make properties in locked status. The locked properties can't be modified or it will cause exception.
     * @param properties the properties that will be locked.
     */
    public void lockProperty(Property... properties) {
        getLockProperties().addAll(Arrays.asList(properties));
    }

    /**
     * This method is used for debug only.
     * Make properties in unlocked status. The unlocked properties can't be modified or it will cause exception.
     * @param properties the properties that will be unlocked.
     */
    public void unlockProperty(Property... properties) {
        getLockProperties().removeAll(Arrays.asList(properties));
    }

    /**
     * This method is used for debug only.
     * Get the lock status of specific property.
     * @return the property is locked or not
     */
    public boolean isPropertyLocked(Property property) {
        return mLockedProperties != null && mLockedProperties.contains(property);
    }

    private ArrayList<Property> getLockProperties() {
        if (mLockedProperties == null) {
            mLockedProperties = new ArrayList<Property>();
        }
        return mLockedProperties;
    }

    /**
     * Sets the color property of this actor
     * @param color the color value to be used for actor
     */
    public void setColor(Color color) {
        setValueInTransaction(PROP_COLOR, color);
    }

    /**
     * Gets the color property of this actor
     * @return the property of color
     */
    public Color getColor() {
        return getValue(PROP_COLOR);
    }

    /**
     * Sets the opacity of this actor.
     * This function is just an easy way to modify the alpha channel of the
     * color of the node.
     * @param opacity Opacity to set
     */
    public void setOpacity(int opacity) {
        if (opacity < 0 || opacity > MAX_OPACITY) {
            throw new IllegalArgumentException("Invalid opacity value: " + opacity);
        }
        setValue(PROP_OPACITY, opacity);
    }

    /**
     * Returns the opacity of this actor.
     * @return Opacity of node
     */
    public int getOpacity() {
        return getValue(PROP_OPACITY);
    }

    /**
     * Set the clip rectangle of actor.
     * @param area the property of rectangle
     */
    public void setDisplayArea(Box area) {
        setValueInTransaction(PROP_DISPLAY_AREA, area);
    }

    /**
     * Get the clip rectangle of actor.
     * @return the property of rectangle
     */
    public Box getDisplayArea() {
        return getValue(PROP_DISPLAY_AREA);
    }

    ///////////////////////////////////////////////////////////////////////////
    // public methods

    /**
     * Realize this actor.
     * @param presentationEngine  an initialized PresentationEngine to be used for realizing actor
     * @hide
     */
    public void realize(PresentationEngine presentationEngine) {
        if (mPresentation == null) {
            mPresentation = createPresentation(presentationEngine);
            mPresentation.initialize(this);
            applyAllProperties();

            // continue because there maybe some attached properties...
        }

        applyDirtyValues();
    }

    /**
     * Check if this actor is realized.
     * @return  ture if the actor is realized
     */
    public boolean isRealized() {
        return (mPresentation != null);
    }

    /**
     * Un-realize this actor.
     * @hide
     */
    public void unrealize() {
        finishAnimations();
        if (mPresentation != null) {
            mPresentation.uninitialize();
            mPresentation = null;
        }
    }

    /**
     * Performs a raycast hit test on the tree under this actor, returning only
     * the actor which was hit by the test, if any.
     *
     * @param screenPoint Point on the screen to test
     * @return The actor that is at the point given, or null otherwise
     */
    public Actor hitTest(Point screenPoint) {
        HitTestResult result = hitTestFull(screenPoint);
        return result.getActor();
    }

    /**
     * Performs a hit test on the tree under this actor, returning full details
     * about any hit that may occur.
     *
     * @param screenPoint Point on the screen to test
     * @return Details about the hit test and its result
     */
    public HitTestResult hitTestFull(Point screenPoint) {
        HitTestResult result = new HitTestResult();

        if (mPresentation != null) {
            Presentation hit = mPresentation.hitTest(result, screenPoint);

            if (hit != null) {
                result.setActor(this);
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Animations

    private final Map<String, Animation> mAnimationMap = new HashMap<String, Animation>();

    /**
     * Register an animation for specified key.
     *
     * @param key       Typically property name.
     * @param animation the animation
     */
    @SuppressWarnings("PMD")
    public void onAnimationStarted(String key, Animation animation) {
        synchronized (mAnimationMap) {
            Animation existing = mAnimationMap.get(key);
            if (existing != animation) {
                if (existing != null) {
                    existing.stop();
                }
                mAnimationMap.put(key, animation);
            }
        }
    }

    public void onAnimationStopped(String key) {
        if (mAnimationMap != null) {
            synchronized (mAnimationMap) {
                mAnimationMap.remove(key);
            }
        }
    }

    /**
     * Stops all animations that are currently started on this actor.
     */
    public void stopAnimations() {
        if (mAnimationMap != null) {
            synchronized (mAnimationMap) {
                ArrayList<Animation> animations = new ArrayList<Animation>(mAnimationMap.values());
                for (Animation animation : animations) {
                    animation.stop();
                }
            }
        }
    }

    /**
     * Finishes all animations that are currently started on this actor.
     */
    public void finishAnimations() {
        if (mAnimationMap != null) {
            synchronized (mAnimationMap) {
                ArrayList<Animation> animations = new ArrayList<Animation>(mAnimationMap.values());
                for (Animation animation : animations) {
                    animation.complete();
                }
            }
        }
    }

    /**
     * Checks if the animation in this actor is started.
     * @return true if the animation in this actor is started.
     */
    public boolean isAnimationStarted() {
        if (mAnimationMap != null) {
            synchronized (mAnimationMap) {
                return !mAnimationMap.isEmpty();
            }
        }
        return false;
    }

    /**
     * Animate changes to one or more actors. Note that the default animation
     * duration <code>BasicAnimation.DEFAULT_DURATION</code> will be used.
     *
     * @param animations a runnable to change actor properties
     */
    public static void animate(Runnable animations) {
        animate(animations, null);
    }

    /**
     * Animate changes to one or more actors with specified completion handler.
     *
     * @param animations a runnable to change actor properties
     * @param completion completion a runnable that will be executed when the animation sequence ends.
     */
    public static void animate(Runnable animations, Runnable completion) {
        animate(BasicAnimation.DEFAULT_DURATION, animations, completion);
    }

    /**
     * Animate changes to one or more actors with specified duration and completion handler.
     *
     * @param duration duration of animations in milliseconds
     * @param animations a runnable to change actor properties
     * @param completion a runnable that will be executed when the animation sequence ends.
     */
    public static ImplicitAnimation animate(int duration, Runnable animations, Runnable completion) {
        ImplicitAnimation animation;

        try {
            animation = Transaction.beginImplicitAnimation();

            Transaction.setAnimationDuration(duration);
            Transaction.setCompletion(completion);

            animations.run();
        } finally {
            Transaction.commit();
        }

        return animation;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Debugging

    /**
     * Dump all of the property of this actor to log.
     */
    public String dump() {
        return dumpProperties(!mAnimationMap.isEmpty());
    }

    /**
     * Dump all of the property of the animation in this actor to log.
     */
    public String dumpAnimation() {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        if (!mAnimationMap.isEmpty()) {
            ArrayList<Animation> animations = new ArrayList<Animation>(mAnimationMap.values());
            for (Animation animation : animations) {
                builder.append(animation.getClass().getSimpleName() + index  + ":");
                String temp = "";
                if (animation instanceof AnimationGroup) {
                    temp = temp + wrapAnimationGroup(animation);
                } else  {
                    temp = temp + wrapSingleAnimation(animation);
                    temp = temp.substring(0, temp.length() - 1);
                    temp = JSON.wrap(temp);
                }
                builder.append(temp);
                builder.append(",");
                index++;
            }
        }

        if (builder.length() > 0) {
            // To compatible with JSON format
            builder.deleteCharAt(builder.length() - 1);
            return JSON.wrap(builder.toString());
        }
        return null;
    }

    private String wrapAnimationGroup(Animation animation) {
        StringBuilder builder = new StringBuilder();
        Log.w(TAG, "wrapAnimationGroup: " + ((AnimationGroup) animation).getAnimationCount());

        for (int index = 0; index < ((AnimationGroup) animation).getAnimationCount(); index++) {
            Animation ani = ((AnimationGroup) animation).getAnimation(index);

            builder.append(ani.getClass().getSimpleName() + index  + ":");
            Log.w(TAG, "wrapAnimationGroup  -- string: " +  builder.toString());

            String temp = "";
            if (ani instanceof AnimationGroup) {
                temp = temp + wrapAnimationGroup(ani);
            } else {
                temp = temp + wrapSingleAnimation(animation);
                temp = temp.substring(0, temp.length() - 1);
                temp = JSON.wrap(temp);
            }
            builder.append(temp);
            builder.append(",");
        }

        if (builder.length() > 0) {
            // To compatible with JSON format
            builder.deleteCharAt(builder.length() - 1);
            return JSON.wrap(builder.toString());
        }
        return null;
    }

    private String wrapSingleAnimation(Animation animation) {

        String property = "";

        if (animation instanceof PropertyAnimation) {
            Log.w(TAG, "wrapSingleAnimation  -- getPropertyName: " +  ((PropertyAnimation) animation).getPropertyName());
            property = property + ((PropertyAnimation) animation).getPropertyName();
        }

        return (wrapProperty("Property", property)
                + wrapProperty("AutoReverse", Boolean.toString(((BasicAnimation) animation).getAutoReverse()))
                + wrapProperty("Loop", Boolean.toString(((BasicAnimation) animation).getLoop()))
                + wrapProperty("Duration", Integer.toString(((BasicAnimation) animation).getDuration()))
                + wrapProperty("Direction", Integer.toString(((BasicAnimation) animation).getDirection()))
                + wrapProperty("TimeScale", Float.toString(((BasicAnimation) animation).getTimeScale())));
    }

    private String wrapProperty(String name, String value) {
        return name + ":" + value + ",";
    }


    /**
     * Convert the StringBuilder content to String
     * @return  output string
     */
    @Override
    public String toString() {
        return "Actor{" + "mId=" + mId + ", mTag=" + mTag + ", mReactive=" + mReactive
            + ", mOwner=" + mOwner + ", mPresentation=" + mPresentation + '}';
    }
}
