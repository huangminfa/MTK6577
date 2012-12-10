package com.mediatek.ngin3d;

import com.mediatek.util.JSON;

/**
 * A class that content the size information.
 */
public class Dimension implements JSON.ToJson {

    /**
     * Width of dimension
     */
    public float width;
    /**
     * Height of dimension
     */
    public float height;

    public static final float NATURAL_SIZE = -1;

    /**
     *   Initialize the dimension class with natural size
     */
    public Dimension() {
        width = NATURAL_SIZE;
        height = NATURAL_SIZE;
    }

    /**
     * Initialize the dimension class with specific width and height
     * @param width  value of width
     * @param height  value of height
     */
    public Dimension(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Set the dimension with specific width and height
     * @param width  value of width
     * @param height  value of height
     */
    public final void set(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Check if the input size is a valid size
     * @param size  input size value
     * @return   true if the size is valid
     */
    public static boolean isValidSize(float size) {
        return (size >= 0 || size == Dimension.NATURAL_SIZE);
    }

    /**
     * Check if the input width and height is equal to this object's size.
     * @param width  specific width to be compared
     * @param height specific height to be compared
     * @return  true if the value of input is equal to this object's size
     */
    public final boolean equals(float width, float height) {
        return this.width == width && this.height == height;
    }

    /**
     * Check if the input object is equal to this dimension object.
     * @param o  input object
     * @return  true if two objects are the same or their properties are the same.
     */
    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof Dimension) {
            Dimension d = (Dimension) o;
            return this.width == d.width && this.height == d.height;
        }
        return false;
    }

    /**
     * Generate a new hash code
     * @return  new hash code
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + Float.floatToIntBits(this.width);
        result = 37 * result + Float.floatToIntBits(this.height);
        return result;
    }

    /**
     * Convert the dimension property to string for output
     * @return   output string
     */
    @Override
    public String toString() {
        return "Dimension: {width : " + width + ", height : " + height + "}";
    }

    /**
     * Convert the dimension property to JSON formatted String
     * @return   output JSON formatted String
     */
    public String toJson() {
        return "{Dimension: {width : " + width + ", height : " + height + "}}";
    }
}
