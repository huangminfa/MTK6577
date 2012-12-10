package com.mediatek.ngin3d;

import com.mediatek.util.JSON;

/**
 * A special class that is responsible for scale operation
 */
public class Scale implements JSON.ToJson {
    public float x;
    public float y;
    public float z;

    /**
     * Initialize the object with empty setting.
     */
    public Scale() {
        // Do nothing by default
    }

    /**
     * Initialize the object with specific x and y amount.
     */
    public Scale(float x, float y) {
        set(x, y, 0.0f);
    }

    /**
     * Initialize the object with specific x, y, and z amount.
     */
    public Scale(float x, float y, float z) {
        set(x, y, z);
    }

    /**
     *  Set the specific value to this scale object
     * @param x  x value
     * @param y  y value
     * @param z  z value
     */
    public final void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Compare the input object with this scale object.
     * @param o   the object to be compared
     * @return  true if two objects are the same or their properties are the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Scale scale = (Scale) o;

        if (Float.compare(scale.x, x) != 0) return false;
        if (Float.compare(scale.y, y) != 0) return false;
        if (Float.compare(scale.z, z) != 0) return false;

        return true;
    }

     /**
     * Create a new hash code.
     * @return  hash code
     */
    @Override
    public int hashCode() {
        int result = (x == +0.0f ? 0 : Float.floatToIntBits(x));
        result = 31 * result + (y == +0.0f ? 0 : Float.floatToIntBits(y));
        result = 31 * result + (z == +0.0f ? 0 : Float.floatToIntBits(z));
        return result;
    }

    /**
     * Convert the scale property to string for output
     * @return   output string
     */
    @Override
    public String toString() {
        return "Point:[" + this.x + ", " + this.y + ", " + this.z + "]";
    }

    /**
     * Convert the scale property to JSON formatted String
     * @return   output JSON formatted String
     */
    public String toJson() {
        return "{Point:[" + this.x + ", " + this.y + ", " + this.z + "]" + "}";
    }

    public static Scale newFromString(String positionString) {
        float[] xyz = Utils.parseStringToFloat(positionString);
        return new Scale(xyz[0], xyz[1], xyz[2]);
    }

}
