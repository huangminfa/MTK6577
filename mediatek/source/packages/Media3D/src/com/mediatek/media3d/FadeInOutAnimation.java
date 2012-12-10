package com.mediatek.media3d;

import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Property;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.PropertyAnimation;

/**
 * Pack Fade in/ out PropertyAnimation for easy usage.
 */
public class FadeInOutAnimation extends PropertyAnimation {

    private static final String TAG = "FadeInOutAnimation";

    public enum FadeType {
        IN, OUT;
    }

    private static final Color INVISIBLE_DARK = new Color(0, 0, 0, 255);
    private static final Color TRANSPARENT = new Color(255, 255, 255, 255);
    private static final int FADE_DURATION = 240;

    private static Object getStartColor(FadeType type) {
        if (type == FadeType.IN) {
            return INVISIBLE_DARK;
        }
        return TRANSPARENT;
    }

    private static Object getEndColor(FadeType type) {
        if (type == FadeType.IN) {
            return TRANSPARENT;
        }
        return INVISIBLE_DARK;
    }

    public FadeInOutAnimation(Actor actor, FadeType type) {
        super(actor, "color", getStartColor(type), getEndColor(type));

        setDuration(FADE_DURATION);
        setMode(Mode.LINEAR);
        enableOptions((type == FadeType.IN) ? Animation.SHOW_TARGET_ON_STARTED : Animation.HIDE_TARGET_ON_COMPLETED);
    }
}

