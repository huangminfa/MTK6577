package com.mediatek.ngin3d.presentation;

import com.mediatek.util.JSON;

/**
 * A class that responsible for storing object source reference and information.
 */
public class ObjectSource implements JSON.ToJson {
    public static final int FILE = 1;
    public static final int RES_ID = 2;
    public static final int ASSET = 3;

    /**
     * Initialize this class with object source type and its data
     * @param srcType  object source type
     * @param srcInfo  object data
     */
    public ObjectSource(int srcType, Object srcInfo) {
        this.srcType = srcType;
        this.srcInfo = srcInfo;
    }

    public int srcType;
    public Object srcInfo;

    @Override
    public String toString() {
        return String.format("ObjectSource: type=%d, info=%s", srcType, srcInfo);
    }

    public String toJson() {
        return String.format("ObjectSource{type=%d, info=%s}", srcType, srcInfo);
    }    
}
