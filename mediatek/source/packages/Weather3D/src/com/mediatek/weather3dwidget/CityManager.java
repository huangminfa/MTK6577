package com.mediatek.weather3dwidget;

public final class CityManager {
    private static int sIndex = 0;
    private static int sTotal;

    private CityManager() {}

    public static int getNextCity() {
        sIndex = (sIndex + 1 == sTotal) ? 0: sIndex + 1;
        return sIndex;
    }

    public static int getPreviousCity() {
        sIndex = (sIndex == 0) ? sTotal - 1: sIndex - 1;
        return sIndex;
    }

    public static void setTotal(int total) {
        sTotal = total;
    }

    public static int getCurrentIndex() {
        return sIndex;
    }

    public static void setCurrentIndex(int index) {
        sIndex = index;
    }
}
