
package com.mediatek.ngin3d.debugtools.messagepackage;

public final class Utils {

    private Utils() {
        // Do nothing
    }

    public static void writeInt(byte[] buf, int offset, int value) {
        buf[offset] = (byte) (value >> 24);
        buf[offset + 1] = (byte) (value >> 16);
        buf[offset + 2] = (byte) (value >> 8);
        buf[offset + 3] = (byte) value;
    }

    public static byte[] writeInt(int value) {
        byte[] b = new byte[4];
        b[0] = (byte) (value >> 24);
        b[1] = (byte) (value >> 16);
        b[2] = (byte) (value >> 8);
        b[3] = (byte) value;
        return b;
    }

    public static int readInt(byte[] b, int begin) {

        return (b[begin] << 24)
                + ((b[begin + 1] & 0xFF) << 16)
                + ((b[begin + 2] & 0xFF) << 8)
                + (b[begin + 3] & 0xFF);
    }

}
