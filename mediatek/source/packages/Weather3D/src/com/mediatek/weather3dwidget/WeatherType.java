package com.mediatek.weather3dwidget;

public final class WeatherType {
    static class Type {
        // sunny
        public static final int SUNNY = 1;
        // windy
        public static final int WINDY = 2;
        // strong wind
        public static final int BLUSTERY = 3;
        public static final int TORNADO = 4;
        // cloudy
        public static final int CLOUDY = 5;
        public static final int OVERCAST = 6;
        // rain
        public static final int SHOWER = 7;
        public static final int RAIN = 8;
        // heavy rain
        public static final int DOWNPOUR = 9;
        public static final int HAIL = 10;
        // thunder shower
        public static final int THUNDER_SHOWER = 11;
        public static final int THUNDER_STORM = 12;
        public static final int THUNDER_STORM_HAIL = 13;
        // snow
        public static final int SNOW_SHOWER = 14;
        public static final int SNOW_LIGHT = 15;
        public static final int SNOW = 16;
        //heavy snow
        public static final int HEAVY_SNOW = 17;
        // snow rain
        public static final int SLEET = 18;
        // fog
        public static final int FOG = 19;
        // sand
        public static final int DUST = 20;
        public static final int SAND = 21;      
    }
    
    private static int[] mWeatherIcon =
    //       0,                   1,                       2,                       3,                           4,
            {R.raw.ic_sunny,      R.raw.ic_sunny,          R.raw.ic_windy,          R.raw.ic_blustery,           R.raw.ic_tornado,
             R.raw.ic_cloudy,     R.raw.ic_overcast,       R.raw.ic_showers,        R.raw.ic_rain,               R.raw.ic_downpour,
             R.raw.ic_hail,       R.raw.ic_thunder_shower, R.raw.ic_thunder_storms, R.raw.ic_thunder_storm_hail, R.raw.ic_snow_showers,
             R.raw.ic_snow_light, R.raw.ic_snow,           R.raw.ic_snow_heavy,     R.raw.ic_sleet,              R.raw.ic_fog,
             R.raw.ic_sandy,      R.raw.ic_sandy};
    //       20,                  21

    static class ModelType {
        public static final int SUNNY = 1;
        public static final int WINDY = 2;
        public static final int BLUSTERY = 3;
        public static final int CLOUDY = 4;
        public static final int RAIN = 5;
        public static final int HEAVY_RAIN = 6;
        public static final int THUNDER = 7;
        public static final int SNOW = 8;
        public static final int HEAVY_SNOW = 9;
        public static final int SNOW_RAIN = 10;
        public static final int FOG = 11;
        public static final int SAND = 12;

        public static final int INDEX_MIN = SUNNY;
        public static final int INDEX_MAX = SAND;
    }

    private static int[] mModelType =
    //       0,                    1,                 2,                    3,                   4,
            {ModelType.SUNNY,      ModelType.SUNNY,   ModelType.WINDY,      ModelType.BLUSTERY,  ModelType.BLUSTERY,
             ModelType.CLOUDY,     ModelType.CLOUDY,  ModelType.RAIN,       ModelType.RAIN,      ModelType.HEAVY_RAIN,
             ModelType.HEAVY_RAIN, ModelType.THUNDER, ModelType.THUNDER,    ModelType.THUNDER,   ModelType.SNOW,
             ModelType.SNOW,       ModelType.SNOW,    ModelType.HEAVY_SNOW, ModelType.SNOW_RAIN, ModelType.FOG,
             ModelType.SAND,       ModelType.SAND};
    //       20,                   21

    private WeatherType() {}

    public static int convertToModelType(int type) {
        return mModelType[type];
    }

    public static int getWeatherIcon(int type) {
        return mWeatherIcon[type];
    }

    public static boolean isSunMoonNeededModelType(int modelType) {
        return !(modelType == ModelType.RAIN || modelType == ModelType.HEAVY_RAIN || modelType == ModelType.THUNDER || modelType == ModelType.SNOW_RAIN);
    }

    public static boolean isSnowModelType(int modelType) {
        return (modelType == ModelType.SNOW || modelType == ModelType.HEAVY_SNOW || modelType == ModelType.SNOW_RAIN);
    }

    public static boolean isSandModelType(int modelType) {
        return (modelType == ModelType.SAND);
    }

    public static boolean isModelTypeInRange(int modelType) {
        return (modelType >= ModelType.INDEX_MIN && modelType <= ModelType.INDEX_MAX);
    }
}