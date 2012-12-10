package com.mediatek.ngin3d;

import android.text.TextPaint;
import com.mediatek.ngin3d.presentation.Graphics2d;
import com.mediatek.ngin3d.presentation.PresentationEngine;

/**
 * A canvas that can be draw by Android graphics API and then be rendered in 3D. Typically
 * it can be used for UI object that can be draw only once or for a few times.
 * <p/>
 * Applications can create their own Canvas2d object by deriving from Canvas2d class and
 * override the drawRect method.
 */
public class Canvas2d extends Plane<Graphics2d> {

    protected final TextPaint mPaint = new TextPaint();

    private static class LastProperty<T> extends Property<T> {
        public LastProperty(String name, T defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public boolean dependsOn(Property other) {
            if (this == other) {
                return false;
            }
            return true;    // always depends on other properties
        }
    }

    /**
     * @hide
     */
    static final LastProperty<Box> PROP_DIRTY_RECT = new LastProperty<Box>("dirty_rect", null);

    /**
     * Apply new rectangular data to this object
     *
     * @param property input property type
     * @param value    input property value
     * @return true if the property is applied successfully
     * @hide
     */
    @Override
    protected boolean applyValue(Property property, Object value) {
        if (property.sameInstance(PROP_DIRTY_RECT)) {
            Box rect = (Box) value;
            drawRect(rect, getPresentation());
            return true;
        }

        return super.applyValue(property, value);
    }

    /**
     * Override to provide custom drawing.
     *
     * @param rect the direct rectangle
     * @param g2d  the Graphics2d presentation object
     */
    protected void drawRect(Box rect, Graphics2d g2d) {
        // Do nothing
    }

    /**
     * Set up new rectangular data
     *
     * @param rect box variable to be set
     */
    public void setDirtyRect(Box rect) {
        setValue(PROP_DIRTY_RECT, rect);
    }

    /**
     * Get the rectangular data of this object
     *
     * @return rectangular data
     */
    public Box getDirtyRect() {
        return getValue(PROP_DIRTY_RECT);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Presentation
    /**
     * @hide
     */
    @Override
    protected Graphics2d createPresentation(PresentationEngine engine) {
        return engine.createGraphics2d();
    }

}
