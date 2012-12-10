package com.mediatek.ngin3d;

import com.mediatek.util.JSON;

/**
 * A rectangular box setting for image drawing.
 */
public class Box implements JSON.ToJson {

    /**
     * Two point variable
     */
    public float x1, y1, x2, y2;

    /**
     * Initialize a box without any argument.
     */
    public Box() {
        // Do nothing by default
    }

    /**
     * Construct by copying the contents of another box.
     */
    public Box(Box other) {
        x1 = other.x1;
        y1 = other.y1;
        x2 = other.x2;
        y2 = other.y2;
    }

    /**
     * Initialize a box with start and end points value.
     */
    public Box(float x1, float y1, float x2, float y2) {
        set(x1, y1, x2, y2);
    }

    /**
     * Set the box with start and end points value.
     * @param x1  start value of x
     * @param y1  start value of y
     * @param x2  end value of x
     * @param y2  end value of y
     */
    public final void set(float x1, float y1, float x2, float y2) {
        if (x2 < x1 || y2 < y1) {
            throw new IllegalArgumentException("x1 should be less than x2; y1 should be less than y2");
        }
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Get the size of this box.
     * @return  the size of this box
     */
    public Dimension getSize() {
        return new Dimension(x2 - x1, y2 - y1);
    }

    /**
     * Get the width value of this box.
     * @return  the value of width of this box
     */
    public float getWidth() {
        return x2 - x1;
    }
    /**
     * Get the height value of this box.
     * @return  the value of height of this box
     */
    public float getHeight() {
        return y2 - y1;
    }

    /**
     * Convert the box property to string for output
     * @return   output string
     */
    @Override
    public String toString() {
        return "Box:[" + this.x1 + ", " + this.y1 + ", " + this.x2 + ", " + this.y2 + "]";
    }

    /**
     * Convert the box property to JSON formatted String
     * @return   output JSON formatted String
     */
    public String toJson() {
        return "{Box:[" + this.x1 + ", " + this.y1 + ", " + this.x2 + ", " + this.y2 + "]" + "}";
    }

    /**
     * Compare the object is same as this box object or property
     * @param o  the object to be compared
     * @return  true if two objects are the same
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        if (Float.compare(box.x1, x1) != 0) return false;
        if (Float.compare(box.x2, x2) != 0) return false;
        if (Float.compare(box.y1, y1) != 0) return false;
        if (Float.compare(box.y2, y2) != 0) return false;

        return true;
    }

    /**
     * Create a new hash code.
     * @return  a new hash code
     */
    @Override
    public int hashCode() {
        int result = (x1 == +0.0f ? 0 : Float.floatToIntBits(x1));
        result = 31 * result + (y1 == +0.0f ? 0 : Float.floatToIntBits(y1));
        result = 31 * result + (x2 == +0.0f ? 0 : Float.floatToIntBits(x2));
        result = 31 * result + (y2 == +0.0f ? 0 : Float.floatToIntBits(y2));
        return result;
    }
}
