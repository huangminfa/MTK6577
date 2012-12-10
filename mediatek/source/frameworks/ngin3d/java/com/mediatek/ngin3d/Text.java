package com.mediatek.ngin3d;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.mediatek.util.JSON;

/**
 * To display text using system fonts.
 */
public class Text extends Canvas2d {

    private Paint.FontMetrics mFontMetrics = mPaint.getFontMetrics();
    private int mTextBackgroundColor;

    /**
     * Initialize a plain text with empty content.
     */
    public Text() {
        this("");
    }

    /**
     * Initialize a plain text with an input string.
     * @param text  a string to be set in this text class
     */
    public Text(String text) {
        setText(text);
        mPaint.setAntiAlias(true);
    }

    /**
     * @hide
     */
    @Override
    protected void applyBatchValues() {
        String text = getText();
        final float width = Math.max(mPaint.measureText(text), 1);
        Canvas canvas = mPresentation.beginDraw((int) width, (int) (-mFontMetrics.top + mFontMetrics.bottom) + 1, mTextBackgroundColor);
        canvas.drawText(text, 0, (int) -mFontMetrics.top, mPaint);
        mPresentation.endDraw();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Property

    /**
     * The property of size of this text.
     * @hide
     */
    static final Property<Float> PROP_TEXT_SIZE = new Property<Float>("text_size", 32.f);
    /**
     * The property of color of this text.
     * @hide
     */
    static final Property<Color> PROP_TEXT_COLOR = new Property<Color>("text_color", Color.WHITE);
    /**
     * The property of background color of this text.
     * @hide
     */
    static final Property<Color> PROP_TEXT_BACKGROUND_COLOR = new Property<Color>("text_background_color", new Color(0, 0, 0, 0));
    /**
     * The property of typeface of this text.
     * @hide
     */
    static final Property<Typeface> PROP_TEXT_TYPEFACE = new Property<Typeface>("text_typeface", Typeface.DEFAULT);
    /**
     * The property of shadow layer of this text.
     * @hide
     */
    static final Property<ShadowLayer> PROP_TEXT_SHADOW_LAYER = new Property<ShadowLayer>("text_shadow_layer", null);
    /**
     * The property of text body of this text.
     * @hide
     */
    static final Property<String> PROP_TEXT = new Property<String>("text", "", PROP_TEXT_SIZE, PROP_TEXT_COLOR, PROP_TEXT_TYPEFACE, PROP_TEXT_SHADOW_LAYER);

    /**
     * Apply the properties to this text object.
     * @param property  input property type
     * @param value  input property value
     * @return  true if the properties is successfully set.
     * @hide
     */
    protected boolean applyValue(Property property, Object value) {
        if (super.applyValue(property, value)) {
            return true;
        }

        if (property.sameInstance(PROP_TEXT)) {
            enableApplyFlags(FLAG_APPLY_LATER_IN_BATCH);
            return true;
        } else if (property.sameInstance(PROP_TEXT_SIZE)) {
            final Float textSize = (Float) value;
            mPaint.setTextSize(textSize);
            enableApplyFlags(FLAG_APPLY_LATER_IN_BATCH);
            return true;
        } else if (property.sameInstance(PROP_TEXT_COLOR)) {
            final Color textColor = (Color) value;
            mPaint.setColor(textColor.getRgb());
            enableApplyFlags(FLAG_APPLY_LATER_IN_BATCH);
            return true;
        } else if (property.sameInstance(PROP_TEXT_BACKGROUND_COLOR)) {
            final Color textBackgroundColor = (Color) value;
            mTextBackgroundColor = textBackgroundColor.getRgb();
            enableApplyFlags(FLAG_APPLY_LATER_IN_BATCH);
            return true;
        } else if (property.sameInstance(PROP_TEXT_TYPEFACE)) {
            final Typeface tf = (Typeface) value;
            mPaint.setTypeface(tf);
            mFontMetrics = mPaint.getFontMetrics();
            enableApplyFlags(FLAG_APPLY_LATER_IN_BATCH);
            return true;
        } else if (property.sameInstance(PROP_TEXT_SHADOW_LAYER)) {
            ShadowLayer shadowLayer = (ShadowLayer) value;
            if (shadowLayer == null) {
                mPaint.setShadowLayer(0, 0, 0, 0);
            } else {
                mPaint.setShadowLayer(shadowLayer.radius, shadowLayer.dx, shadowLayer.dy, shadowLayer.color);
            }
            enableApplyFlags(FLAG_APPLY_LATER_IN_BATCH);
            return true;
        }

        return false;
    }

    /**
     * Get the text body of this text object.
     * @return  text body
     */
    public final String getText() {
        return getValue(PROP_TEXT);
    }

    /**
     * Set the text body of this text object.
     * @param text  the text to be set
     */
    public final void setText(String text) {
        setValue(PROP_TEXT, text);
    }

    /**
     * Get the text size property of this text object.
     * @return  text size
     */
    public float getTextSize() {
        return getValue(PROP_TEXT_SIZE);
    }

    /**
     * Set the text size property of this text object.
     * @param textSize  the size of text value to be set
     */
    public void setTextSize(float textSize) {
        setValue(PROP_TEXT_SIZE, textSize);
    }

    /**
     * Set the property of color of this text object.
     * @param color  the color value to be set.
     */
    public void setTextColor(Color color) {
        setValue(PROP_TEXT_COLOR, color);
    }

    /**
     * Get the text color property of this text object.
     * @return  color property of this text object
     */
    public Color getTextColor() {
        return getValue(PROP_TEXT_COLOR);
    }

    /**
     * Set the background color of this text object.
     * @param color  color value to be set.
     */
    public void setBackgroundColor(Color color) {
        setValue(PROP_TEXT_BACKGROUND_COLOR, color);
    }

    /**
     * Get the background color of this text object.
     * @return  the color of background color
     */
    public Color getBackgroundColor() {
        return getValue(PROP_TEXT_BACKGROUND_COLOR);
    }

    /**
     * Set the typeface of this text object.
     * @param tf  the typeface to be set.
     */
    public void setTypeface(Typeface tf) {
        setValue(PROP_TEXT_TYPEFACE, tf);
    }

    /**
     * Get the typeface of this text object.
     * @return  the typeface of this object
     */
    public Typeface getTypeface() {
        return getValue(PROP_TEXT_TYPEFACE);
    }

    /**
     * Class for shadow layer setting
     */
    public static class ShadowLayer implements JSON.ToJson {
        /**
         * The radius variable of shadow.
         */
        public float radius;
        /**
         * The x displacement variable of shadow.
         */
        public float dx;
         /**
         * The y displacement variable of shadow.
         */
        public float dy;
         /**
         * The color variable of shadow.
         */
        public int color;

        ShadowLayer(float radius, float dx, float dy, int color) {
            this.radius = radius;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
        }

        /**
         * Convert the ShadowLayer property to string for output
         * @return   output string
         */
        @Override
        public String toString() {
            return "ShadowLayer: radius : " + radius + ", x : " + dx + ", y : " + dy + ", color : " + color;
        }

        public String toJson() {
            return "{ShadowLayer: {radius : " + radius + ", x : " + dx + ", y : " + dy + ", color : " + color + "}}";
        }        
    }

    /**
     * Set the shadow layer of this text object.
     * @param radius  the radius setting for shadow
     * @param dx  the x displacement of the shadow
     * @param dy  the y displacement of the shadow
     * @param color  the color displacement of the shadow
     */
    public void setShadowLayer(float radius, float dx, float dy, int color) {
        setValue(PROP_TEXT_SHADOW_LAYER, new ShadowLayer(radius, dx, dy, color));
    }

    /**
     * Get the shadow later of this text object.
     * @return  shadow layer property
     */
    public ShadowLayer getShadowLayer() {
        return getValue(PROP_TEXT_SHADOW_LAYER);
    }

}
