package com.mediatek.ngin3d.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.Log;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.animation.SpriteAnimation;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Stack;

public final class Ngin3dAnimationInflater {

    public static final boolean DEBUG = true;
    private static final String TAG = "Ngin3dAnimationInflater";

    /**
     * The method to print log
     *
     * @param tag the tag of the class
     * @param msg the log message to print
     */
    private static void log(String tag, String msg) {
        if (Ngin3dLayoutInflater.DEBUG) {
            Log.d(tag, msg);
        }
    }

    /* xml Tags */
    private static final String XML_TAG_PROPERTY_ANIMATION = "PropertyAnimation";
    private static final String XML_TAG_KEYFRAME_ANIMATION = "KeyframeAnimation";
    private static final String XML_TAG_SPRITE_ANIMATION = "SpriteAnimation";
    private static final String XML_TAG_ANIMATION_GROUP = "AnimationGroup";

    /* Property of PropertyAnimation */
    private static final String POSITION_PROPERTY = "position";
    private static final String ROTATION_PROPERTY = "rotation";
    private static final String SCALE_PROPERTY = "scale";
    private static final String COLOR_PROPERTY = "color";
    private static final String OPACITY_PROPERTY = "opacity";

    /* SpriteAnimation Type */
    private static final int MULTI_SPRITE_ANIMATION = 0;
    private static final int SINGLE_SPRITE_ANIMATION = 1;
    private static final int SHEET_SPRITE_ANIMATION = 2;

    private static boolean sIsKeyframeAnimation;
    private static StyleableResolver sStyleable;

    private Ngin3dAnimationInflater() {
        // do nothing.
    }

    /**
     * inflate 3d animation from the given xml file
     *
     * @param context    : Application context used to access resources
     * @param xmlResId   : The resource id of the xml file
     * @param mContainer : the container contains the actor that caller wants to apply the animation on
     * @return The root animation which is described in the xml
     * @throws IllegalArgumentException or RuntimeException while error occurs
     */
    public static BasicAnimation inflateAnimation(Context context, int xmlResId, Container mContainer) {

        XmlResourceParser parser = context.getResources().getXml(xmlResId);
        BasicAnimation xmlRootAnimation = null;

        // Must create Styleable Resolver before inflating
        sStyleable = new StyleableResolver(context);

        /* use mAnimationGroupList to support/check nested animationGroup in xml */
        final Stack<AnimationGroup> animationGroupList = new Stack<AnimationGroup>();

        try {
            int xmlEventType;

            while ((xmlEventType = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                sIsKeyframeAnimation = false;

                switch (xmlEventType) {
                case XmlPullParser.START_DOCUMENT:
                    log(TAG, "Start document");
                    break;
                case XmlPullParser.START_TAG:
                    String tag = parser.getName();
                    log(TAG, "Start Tag: " + tag);

                    BasicAnimation animation = newAnimationByTag(tag, context, parser, mContainer);

                    if (xmlRootAnimation == null) {
                        xmlRootAnimation = animation;
                        log(TAG, "xmlRootAnimation = null, set to animation: " + animation);

                    } else {
                        /* handle nested AnimationGroup xml */
                        if (xmlRootAnimation instanceof AnimationGroup) {
                            /* find a proper AnimationGroup to add animation into */
                            if (animationGroupList.isEmpty()) {
                                ((AnimationGroup) xmlRootAnimation).add(animation);
                                log(TAG, "no proper sub AnimationGroup. add animation into xmlRootAnimation:  " + animation);
                            } else {
                                log(TAG, "animationGroupList size :  " + animationGroupList.size());
                                AnimationGroup properAnimationGroup = animationGroupList.peek();
                                properAnimationGroup.add(animation);
                                log(TAG, "add animation into proper sub animationGroup.  animation:  " + animation + ", properAnimationGroup: " + properAnimationGroup);
                            }

                            /* since keyframeAnimation return as an AnimationGroup, but it is not allowed to add animation into. */
                            if ((animation instanceof AnimationGroup) && (!sIsKeyframeAnimation)) {
                                /* this animation is sub AnimationGroup in xml file */
                                animationGroupList.push((AnimationGroup) animation);
                                log(TAG, "animation is a AnimationGroup, add into AnimationGroupList.");
                            }
                        } else {
                            throw new IllegalArgumentException("xmlRootAnimation is not an AnimationGroup, can't add animation into it.");
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    log(TAG, "End Tag: " + parser.getName());

                    if (parser.getName().equalsIgnoreCase(XML_TAG_ANIMATION_GROUP)) {
                        /* remove the latest AnimationGroup */
                        log(TAG, "End Tag -- mContainerList size :  " + animationGroupList.size());
                        if (!animationGroupList.isEmpty()) {
                            animationGroupList.pop();
                        }
                    }
                    break;
                case XmlPullParser.TEXT:
                    log(TAG, "Text: " + parser.getText());
                    break;
                default:
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Parser Error: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException occurs. Unable to read resource file.");
        }

        return xmlRootAnimation;
    }

    /**
     * new animation by tag
     *
     * @param tag        : the tag string in xml file
     * @param context    : the application environment
     * @param parser:    the xml resource parser created from the xml file
     * @param mContainer : the container contains the actor that caller wants to apply the animation on
     * @return the basicAnimation object
     */
    private static BasicAnimation newAnimationByTag(String tag, Context context, XmlResourceParser parser, Container mContainer) {

        BasicAnimation animation = null;
        if (tag.equalsIgnoreCase(XML_TAG_PROPERTY_ANIMATION)) {
            /* PropertyAnimation */
            animation = parsePropertyAnimation(context, parser, mContainer);

        } else if (tag.equalsIgnoreCase(XML_TAG_KEYFRAME_ANIMATION)) {
            /* KeyframeAnimation */
            animation = parseKeyframeAnimation(context, parser, mContainer);
            sIsKeyframeAnimation = true;

        } else if (tag.equalsIgnoreCase(XML_TAG_ANIMATION_GROUP)) {
            /* AnimationGroup */
            animation = parseAnimationGroup(context, parser, mContainer);

        } else if (tag.equalsIgnoreCase(XML_TAG_SPRITE_ANIMATION)) {
            /* SpriteAnimation */
            animation = parseSpriteAnimation(context, parser, mContainer);

        } else {
            // TODO : unknow tag , it might be a custom Animation. 
            // How to support custom Animation.
            throw new IllegalArgumentException("unknown Animation tag in xml file.");
        }
        return animation;
    }

    /**
     * parse attribute of PropertyAnimation in the xml parser
     *
     * @param context    : the application environment
     * @param parser:    the xml resource parser created from the xml file
     * @param mContainer : the container contains the actor that caller wants to apply the animation on
     * @return the PropertyAnimation object
     */
    private static PropertyAnimation parsePropertyAnimation(Context context, XmlResourceParser parser, Container mContainer) {
        /* get the android res id */
        TypedArray a = context.obtainStyledAttributes(parser, com.android.internal.R.styleable.View);
        int tagId = a.getResourceId(com.android.internal.R.styleable.View_id, -1);
        log(TAG, "PropertyAnimation  tagId : " + tagId);
        a.recycle();

        /* get the target actor */
        TypedArray ta = context.obtainStyledAttributes(parser, sStyleable.resolveIntArray("PropertyAnimation"));
        Actor target = null;
        int animationTargetId = ta.getResourceId(sStyleable.resolveInt("PropertyAnimation_target"), -1);
        if (animationTargetId != -1) {
            target = getAnimationTarget(animationTargetId, mContainer);
            log(TAG, "PropertyAnimation animationTargetId:" + animationTargetId + "  target: " + target);
        }

        /* get the animation property & new PropertyAnimation*/
        String fromString = ta.getString(sStyleable.resolveInt("PropertyAnimation_from"));
        String toString = ta.getString(sStyleable.resolveInt("PropertyAnimation_to"));
        if (fromString == null || toString == null) {
            ta.recycle();
            throw new IllegalArgumentException("invalid or null from or to attribute of the property aniamtion!");
        }

        /* get property & new PropertyAnimation via the property setting */
        String property = ta.getString(sStyleable.resolveInt("PropertyAnimation_property"));
        log(TAG, "PropertyAnimation property: " + property);

        PropertyAnimation animation = null;
        if (property.equals(POSITION_PROPERTY)) {
            /* property -- position */
            Point from = Point.newFromString(fromString);
            Point to = Point.newFromString(toString);
            log(TAG, "PropertyAnimation from: " + from + ", to: " + to);
            if (target == null) {
                animation = new PropertyAnimation(property, from, to);
            } else {
                animation = new PropertyAnimation(target, property, from, to);
            }
        } else if (property.equals(ROTATION_PROPERTY)) {
            /* property -- rotation */
            Rotation from = Rotation.newFromString(fromString);
            Rotation to = Rotation.newFromString(toString);
            log(TAG, "PropertyAnimation from: " + from + ", to: " + to);
            if (target == null) {
                animation = new PropertyAnimation(property, from, to);
            } else {
                animation = new PropertyAnimation(target, property, from, to);
            }
        } else if (property.equals(SCALE_PROPERTY)) {
            /* property -- scale */
            Scale from = Scale.newFromString(fromString);
            Scale to = Scale.newFromString(toString);
            log(TAG, "PropertyAnimation from: " + from + ", to: " + to);
            if (target == null) {
                animation = new PropertyAnimation(property, from, to);
            } else {
                animation = new PropertyAnimation(target, property, from, to);
            }
        } else if (property.equals(COLOR_PROPERTY)) {
            /* property -- color */
            Color from = newColorFromString(fromString);
            Color to = newColorFromString(toString);
            log(TAG, "PropertyAnimation from: " + from + ", to: " + to);
            if (target == null) {
                animation = new PropertyAnimation(property, from, to);
            } else {
                animation = new PropertyAnimation(target, property, from, to);
            }
        } else if (property.equals(OPACITY_PROPERTY)) {
            /* property -- opacity */
            Integer from = newOpacityFromString(fromString);
            Integer to = newOpacityFromString(toString);
            log(TAG, "PropertyAnimation from: " + from + ", to: " + to);
            if (target == null) {
                animation = new PropertyAnimation(property, from, to);
            } else {
                animation = new PropertyAnimation(target, property, from, to);
            }
        } else {
            ta.recycle();
            throw new IllegalArgumentException("invalid property attribute of PropertyAnimation in xml. ");
        }

        /* set animation Tag id */
        animation.setTag(tagId);

        /* set Animation alpha mode */
        Mode[] mode = Mode.values();
        int modeValue = ta.getInt(sStyleable.resolveInt("PropertyAnimation_alphaMode"), -1);
        if (modeValue != -1) {
            Mode alphaMode = mode[modeValue];
            animation.setMode(alphaMode);
            log(TAG, "PropertyAnimation alphaMode: " + alphaMode);
        }

        /* set Animation loop & autoReverse */
        animation.setLoop(ta.getBoolean(sStyleable.resolveInt("PropertyAnimation_loop"), false));
        animation.setAutoReverse(ta.getBoolean(sStyleable.resolveInt("PropertyAnimation_autoReverse"), false));
        log(TAG, "PropertyAnimation , loop: " + ta.getBoolean(sStyleable.resolveInt("PropertyAnimation_loop"), false)
            + ", autoReverse: " + ta.getBoolean(sStyleable.resolveInt("PropertyAnimation_autoReverse"), false));

        int duration = ta.getInteger(sStyleable.resolveInt("PropertyAnimation_duration"), -1);
        if (duration != -1) {
            animation.setDuration(duration);
        }
        log(TAG, "PropertyAnimation duration: " + duration);

        ta.recycle();
        return animation;
    }

    /**
     * parse attribute of KeyframeAnimation in the xml parser
     *
     * @param context    : the application environment
     * @param parser:    the xml resource parser created from the xml file
     * @param mContainer : the container contains the actor that caller wants to apply the animation on
     * @return the BasicAnimation object
     */
    private static AnimationGroup parseKeyframeAnimation(Context context, XmlResourceParser parser, Container mContainer) {
        /* get the android res id */
        TypedArray a = context.obtainStyledAttributes(parser, com.android.internal.R.styleable.View);
        int tagId = a.getResourceId(com.android.internal.R.styleable.View_id, -1);
        log(TAG, "KeyframeAnimation tagId : " + tagId);
        a.recycle();

        /* get the target actor */
        TypedArray ta = context.obtainStyledAttributes(parser, sStyleable.resolveIntArray("KeyframeAnimation"));
        Actor target = null;
        int animationTargetId = ta.getResourceId(sStyleable.resolveInt("KeyframeAnimation_target"), -1);
        log(TAG, "KeyframeAnimation animationTargetId:" + animationTargetId);
        if (animationTargetId != -1) {
            target = getAnimationTarget(animationTargetId, mContainer);
            log(TAG, "KeyframeAnimation animationTargetId:" + animationTargetId + "  target: " + target);
        }

        /* get JSON file which describes the animation info */
        int dataSrc = ta.getResourceId(sStyleable.resolveInt("KeyframeAnimation_dataSrc"), -1);
        if (dataSrc == -1) {
            ta.recycle();
            throw new IllegalArgumentException("invalid JSON file for keyframeAnimation. ");
        }
        log(TAG, "KeyframeAnimation dataSrc: " + dataSrc);

        /* create KeyframeAnimation & set Tag Id*/
        AnimationGroup keyframeAnimation = (AnimationGroup) AnimationLoader.loadAnimation(context, dataSrc);
        keyframeAnimation.setTag(tagId);

        /* set animation target */
        if (target != null) {
            log(TAG, "keyframeAnimation set target: target= " + target);
            keyframeAnimation.setTarget(target);
        }

        /* set Animation loop & autoReverse */
        keyframeAnimation.setLoop(ta.getBoolean(sStyleable.resolveInt("KeyframeAnimation_loop"), true));
        keyframeAnimation.setAutoReverse(ta.getBoolean(sStyleable.resolveInt("KeyframeAnimation_autoReverse"), true));
        log(TAG, "KeyframeAnimation loop: " + ta.getBoolean(sStyleable.resolveInt("KeyframeAnimation_loop"), true)
            + " , autoReverse: " + ta.getBoolean(sStyleable.resolveInt("KeyframeAnimation_autoReverse"), true));

        ta.recycle();
        return keyframeAnimation;
    }

    /**
     * parse attribute of SpriteAnimation in the xml parser
     *
     * @param context    : the application environment
     * @param parser:    the xml resource parser created from the xml file
     * @param mContainer : the container contains the actor that caller wants to apply the animation on
     * @return the SpriteAnimation object
     */
    private static SpriteAnimation parseSpriteAnimation(Context context, XmlResourceParser parser, Container mContainer) {
        /* get the android res id */
        TypedArray a = context.obtainStyledAttributes(parser, com.android.internal.R.styleable.View);
        int tagId = a.getResourceId(com.android.internal.R.styleable.View_id, -1);
        log(TAG, "SpriteAnimation tagId : " + tagId);
        a.recycle();

        /* get the target actor */
        TypedArray ta = context.obtainStyledAttributes(parser, sStyleable.resolveIntArray("SpriteAnimation"));
        Actor target = null;
        int animationTargetId = ta.getResourceId(sStyleable.resolveInt("SpriteAnimation_target"), -1);
        if (animationTargetId != -1) {
            target = getAnimationTarget(animationTargetId, mContainer);
            log(TAG, "SpriteAnimation animationTargetId:" + animationTargetId + "  target: " + target);
        }

        /* get SpriteAnimation type */
        int type = ta.getInt(sStyleable.resolveInt("SpriteAnimation_spriteType"), -1);
        if (type == -1) {
            ta.recycle();
            throw new IllegalArgumentException("invalid SpriteAnimation type.");
        }
        log(TAG, "SpriteAnimation type: " + type);

        /* get duration and check its validation */
        int duration = ta.getInteger(sStyleable.resolveInt("SpriteAnimation_duration"), -1);
        if (duration == -1) {
            ta.recycle();
            throw new IllegalArgumentException("invalid or null duration for SpriteAnimation, it's not allowed.");
        }
        log(TAG, "SpriteAnimation duration: " + duration);

        /* new SpriteAnimation by type & set Tag Id*/
        SpriteAnimation spriteAnimation = null;
        switch (type) {
        case MULTI_SPRITE_ANIMATION:
            /* new multi sprite animation */
            spriteAnimation = newMultiSpriteAnimation(target, duration);

            /* get and add the addSpriteFiles */
            int addSpriteResId = ta.getResourceId(sStyleable.resolveInt("SpriteAnimation_addSpriteFile"), -1);
            if (addSpriteResId != -1) {
                TypedArray addSpriteString = context.getResources().obtainTypedArray(addSpriteResId);
                log(TAG, "SpriteAnimation addSpriteResId : " + addSpriteResId + " , addSpriteString: " + addSpriteString);

                if (addSpriteString != null) {
                    for (int i = 0; i < addSpriteString.length(); i++) {
                        spriteAnimation.addSprite(context.getResources(), addSpriteString.getResourceId(i, 0));
                        log(TAG, "SpriteAnimation add sprite: " + addSpriteString.getResourceId(i, 0));
                    }
                    addSpriteString.recycle();
                }
            }
            break;
        case SINGLE_SPRITE_ANIMATION:
            /* get other related information */
            float singleSpriteWidth = ta.getDimension(sStyleable.resolveInt("SpriteAnimation_singleSpriteWidth"), -1);
            float singleSpriteHeight = ta.getDimension(sStyleable.resolveInt("SpriteAnimation_singleSpriteHeight"), -1);
            log(TAG, "SpriteAnimation singleSpriteWidth: " + singleSpriteWidth + ", singleSpriteHeight: " + singleSpriteHeight);
            if ((singleSpriteWidth == -1) || (singleSpriteHeight == -1)) {
                ta.recycle();
                throw new IllegalArgumentException("invalid sprite width or height for single SpriteAnimation type.");
            }

            /* get image res Id */
            int imageResId = ta.getResourceId(sStyleable.resolveInt("SpriteAnimation_spritImageFile"), -1);
            if ((imageResId == -1)) {
                ta.recycle();
                throw new IllegalArgumentException("invalid image resource for single spriteAnimation.");
            }

            /* new multi sprite animation */
            spriteAnimation = newSingleSpriteAnimation(context, target, imageResId, duration, singleSpriteWidth, singleSpriteHeight);

            break;
        case SHEET_SPRITE_ANIMATION:
            /* get image res Id */
            int imageFileResId = ta.getResourceId(sStyleable.resolveInt("SpriteAnimation_spritImageFile"), -1);
            if ((imageFileResId == -1)) {
                ta.recycle();
                throw new IllegalArgumentException("invalid image resource for single spriteAnimation.");
            }

            /* get image description file res Id */
            int sheetSpriteDescriptionFileId = ta.getResourceId(sStyleable.resolveInt("SpriteAnimation_sheetSpriteDescriptionFile"), -1);
            if (sheetSpriteDescriptionFileId == -1) {
                ta.recycle();
                throw new IllegalArgumentException("invalid sprite description file for sheet SpriteAnimation type.");
            }
            SpriteAnimation.SpriteSheet sheet = new SpriteAnimation.SpriteSheet(context.getResources(), imageFileResId, sheetSpriteDescriptionFileId);

            /* new multi sprite animation */
            spriteAnimation = newSheetSpriteAnimation(target, sheet, duration);

            break;
        default:
            ta.recycle();
            throw new RuntimeException("invalid SpriteAnimation type case.");
        }
        spriteAnimation.setTag(tagId);

        /* set Animation loop & autoReverse */
        spriteAnimation.setLoop(ta.getBoolean(sStyleable.resolveInt("SpriteAnimation_loop"), false));
        spriteAnimation.setAutoReverse(ta.getBoolean(sStyleable.resolveInt("SpriteAnimation_autoReverse"), false));
        log(TAG, "spriteAnimation loop: " + ta.getBoolean(sStyleable.resolveInt("SpriteAnimation_loop"), false)
            + " , autoReverse: " + ta.getBoolean(sStyleable.resolveInt("SpriteAnimation_autoReverse"), false));

        ta.recycle();
        return spriteAnimation;
    }

    /**
     * parse attribute of AnimationGroup in the xml parser
     *
     * @param context    : the application environment
     * @param parser:    the xml resource parser created from the xml file
     * @param mContainer : the container contains the actor that caller wants to apply the animation on
     * @return the AnimationGroup object
     */
    private static AnimationGroup parseAnimationGroup(Context context, XmlResourceParser parser, Container mContainer) {
        /* get the android res id */
        TypedArray a = context.obtainStyledAttributes(parser, com.android.internal.R.styleable.View);
        int tagId = a.getResourceId(com.android.internal.R.styleable.View_id, -1);
        log(TAG, "AnimationGroup tagId : " + tagId);
        a.recycle();

        /* get the target actor */
        TypedArray ta = context.obtainStyledAttributes(parser, sStyleable.resolveIntArray("AnimationGroup"));
        Actor target = null;
        int animationTargetId = ta.getResourceId(sStyleable.resolveInt("AnimationGroup_target"), -1);
        if (animationTargetId != -1) {
            target = getAnimationTarget(animationTargetId, mContainer);
            log(TAG, "AnimationGroup animationTargetId:" + animationTargetId + "  target: " + target);
        }

        /* create AnimationGroup */
        AnimationGroup animationGroup = new AnimationGroup();
        animationGroup.setTag(tagId);

        /* set animation target */
        if (target != null) {
            log(TAG, "animationGroup set target: target= " + target);
            animationGroup.setTarget(target);
        }

        /* set Animation loop & autoReverse */
        animationGroup.setLoop(ta.getBoolean(sStyleable.resolveInt("AnimationGroup_loop"), false));
        animationGroup.setAutoReverse(ta.getBoolean(sStyleable.resolveInt("AnimationGroup_autoReverse"), false));
        log(TAG, "parseAnimationGroup loop: " + ta.getBoolean(sStyleable.resolveInt("AnimationGroup_loop"), false)
            + " , autoReverse: " + ta.getBoolean(sStyleable.resolveInt("AnimationGroup_autoReverse"), false));

        ta.recycle();
        return animationGroup;
    }

    /**
     * new multiSprit Animation
     *
     * @param target    : the target of the animation
     * @param mDuration : the duration of the animation
     * @return the SpriteAnimation object
     */
    private static SpriteAnimation newMultiSpriteAnimation(Actor target, int mDuration) {
        SpriteAnimation animation;
        if (target == null) {
            animation = new SpriteAnimation(mDuration);
        } else {
            if (!(target instanceof Image)) {
                throw new IllegalArgumentException("the target of SpriteAnimation must be an Image actor.");
            }
            animation = new SpriteAnimation((Image) target, mDuration);
        }
        return animation;
    }

    /**
     * new singleSprite Animation
     *
     * @param context            : the context
     * @param target             : the target of the animation
     * @param imageResId         : the image resource id of the big sprite image
     * @param mDuration          : the duration of the sprite Animation
     * @param singleSpriteWidth  : the width of the single spriate
     * @param singleSpriteHeight : the hiehgt of the single sprite
     * @return the SpriteAnimation object
     */
    private static SpriteAnimation newSingleSpriteAnimation(Context context, Actor target, int imageResId, int mDuration, float singleSpriteWidth, float singleSpriteHeight) {
        SpriteAnimation animation;
        if (target == null) {
            animation = new SpriteAnimation(context.getResources(), imageResId, mDuration, (int) singleSpriteWidth, (int) singleSpriteHeight);
        } else {
            if (!(target instanceof Image)) {
                throw new IllegalArgumentException("the target of SpriteAnimation must be an Image actor.");
            }
            animation = new SpriteAnimation(context.getResources(), (Image) target, imageResId, mDuration, (int) singleSpriteWidth, (int) singleSpriteHeight);
        }
        return animation;
    }

    /**
     * new sheetSprite Animation
     *
     * @param target    : the target of the animation
     * @param sheet     : the image resource id of the big sprite image
     * @param mDuration : the duration of the sprite Animation
     * @return the SpriteAnimation object
     */
    private static SpriteAnimation newSheetSpriteAnimation(Actor target, SpriteAnimation.SpriteSheet sheet, int mDuration) {
        SpriteAnimation animation;
        if (target == null) {
            animation = new SpriteAnimation(sheet, mDuration);
        } else {
            if (!(target instanceof Image)) {
                throw new IllegalArgumentException("the target of SpriteAnimation must be an Image actor.");
            }
            animation = new SpriteAnimation((Image) target, sheet, mDuration);
        }
        return animation;
    }

    /**
     * parse the given string with rgb value and new a color
     *
     * @param string : the string contains rgb value
     * @return the color object
     */
    private static Color newColorFromString(String string) {

        String[] arrayString = string.split("#");
        int red = Integer.parseInt(arrayString[1].substring(2, 4), 16);
        int green = Integer.parseInt(arrayString[1].substring(4, 6), 16);
        int blue = Integer.parseInt(arrayString[1].substring(6, 8), 16);
        log(TAG, "newColorFromString -- red: " + red + ", green :" + green + " , blue:" + blue);
        Color color = new Color(red, green, blue);
        log(TAG, "newColorFromString -- color: " + color);
        return color;
    }

    /**
     * parse the given string with opacity value and new an integer
     *
     * @param string : the string contains opacity value
     * @return the integer object
     */
    private static Integer newOpacityFromString(String string) {
        int mIntValues = 0;
        mIntValues = Integer.valueOf(string);
        Integer opacity = Integer.valueOf(mIntValues); // new Integer(mIntValues);
        log(TAG, "newOpacityFromString -- opacity: " + opacity);
        return opacity;
    }

    /**
     * parse the given string with x/y/z info and new a cooresponding rotation
     *
     * @param targetResId targetResId : the resId of the target
     * @param container   container : the container contains the actor that caller wants to apply the animation on
     * @return the target
     */
    private static Actor getAnimationTarget(int targetResId, Container container) {
        Actor target = container;
        if (container != null) {
            target = container.findChildByTag(targetResId);
        }
        return target;
    }
}
