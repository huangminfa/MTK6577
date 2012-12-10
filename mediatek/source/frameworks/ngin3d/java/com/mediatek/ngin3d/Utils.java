package com.mediatek.ngin3d;

import java.io.Closeable;
import java.io.IOException;

public final class Utils {
    private Utils() {
        // Do nothing
    }

    public static void closeQuietly(Closeable i) {
        if (i != null) {
            try {
                i.close();
            } catch (IOException e) {
                // ignore 'cause this function will only be called in finally block.
                e.printStackTrace();
            }
        }
    }

    public static float[] parseStringToFloat(String string) {
        float[] xyz = {0, 0, 0};
        String[] arrayString = string.split(",");
        for (int i = 0; i < arrayString.length; i++) {
            xyz[i] = Float.parseFloat(arrayString[i]);
        }
        return xyz;
    }

}
