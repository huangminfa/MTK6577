package com.mediatek.util;

/**
 * Add JSON utilities that doesn't implement in JSON library of Android
 */
public final class JSON {

    private JSON() {
        // Do nothing
    }

    public static String wrap(String object) {
        return "{" + object + "}";
    }

    public static StringBuffer wrap(StringBuffer buffer) {
        buffer.insert(0, "{");
        buffer.append("}");
        return buffer;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof ToJson) {
            return ((ToJson) obj).toJson();
        }
        return obj.toString();
    }

    public interface ToJson {
        String toJson();
    }
}
