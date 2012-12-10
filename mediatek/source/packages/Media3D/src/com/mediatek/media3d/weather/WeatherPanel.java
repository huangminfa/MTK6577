package com.mediatek.media3d.weather;

import com.mediatek.media3d.FormatUtil;
import com.mediatek.media3d.LogUtil;
import com.mediatek.media3d.R;

import android.content.Context;
import com.mediatek.media3d.Setting;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationGroup;

public final class WeatherPanel {
    private static final String TAG = "Media3D.WeatherPanel";

    private static final int[] WEATHER_ICONS = new int[] {
            R.drawable.icon_moon, R.drawable.icon_sun,R.drawable.icon_raincloud};

    private final Context mContext;
    private final Text mName;
    private final Image mWeather;
    private final DigitClock mClock;
    private final Text mTemperature;
    private final Actor[] mActorsMap; // index :index of animation script, value :weather sprite actor id.
    private LocationWeather mCity;
    private Animation mCurrentAnimation;

    private static int sLargeFontSize;
    private static int sSmallFontSize;

    public static void loadConfiguration(Setting setting) {
        sLargeFontSize = setting.getInt(Setting.WEATHER_TITLE_SIZE);
        sSmallFontSize = setting.getInt(Setting.WEATHER_TITLE_WOUPDATE_SIZE);
    }

    public WeatherPanel(final Context context, final LocationWeather city) {
        mCity = city;
        mContext = context;

        mName = mCity.getNameText();
        FormatUtil.applyTextAttributes(mName, 30, false);
        float namescalesize = getNameScale();
        mName.setScale(new Scale(namescalesize, namescalesize));
        mName.setPosition(new Point(0.625f, 0.917f, true));

        mTemperature = mCity.getTemperatureText(context);
        FormatUtil.applyTextAttributes(mTemperature, 30, false);
        float scalesize = mCity.isUpdated() ? 2f : 1f;
        mTemperature.setScale(new Scale(scalesize, scalesize));
        mTemperature.setPosition(new Point(0.819f, 0.7f, true));

        mClock = new DigitClock("weather_panel", context, mCity);
        updateClock();

        mWeather = Image.createFromResource(context.getResources(), getWeatherIcon(mCity));
        mWeather.setAlphaSource(Plane.FROM_TEXEL_VERTEX);

        mActorsMap = new Actor[] {mName, mTemperature, mClock, mWeather};
        setVisible(false);
    }

    public void addToContainer(final Container container) {
        LogUtil.v(TAG, "container :" + container);
        container.add(mWeather);
        container.add(mClock);
        container.add(mName);
        container.add(mTemperature);
    }

    public void setCity(final LocationWeather city, final boolean visibility) {
        mCity = city;

        mName.setText(mCity.getLocationName());
        float namescalesize = getNameScale();
        mName.setScale(new Scale(namescalesize, namescalesize));

        LogUtil.v(TAG, "city id:" + city.getCityId() + ", update :" + city.getLastUpdated());
        float scalesize = mCity.isUpdated() ? 2f : 1f;
        mTemperature.setScale(new Scale(scalesize, scalesize));
        mTemperature.setText(mCity.getTemperatureString(mContext));
        mWeather.setImageFromResource(mContext.getResources(), getWeatherIcon(mCity));
        setVisible(visibility);
        updateClock();
    }

    public void setVisible(final boolean visibility) {
        for (Actor actor : mActorsMap) {
            actor.setVisible(visibility);
        }
    }

    public void hideNameAndTemperature() {
        mName.setVisible(false);
        mTemperature.setVisible(false);
    }

    public final void updateClock() {
        mClock.update(mCity, mContext);
    }

    private float getNameScale() {
        return (mCity.getLocationName().length() > 12) ? 1.0f : 2.0f;
    }

    public void updateWeather() {
        mWeather.setImageFromResource(mContext.getResources(), getWeatherIcon(mCity));
        final float scalesize = mCity.isUpdated() ? 2f : 1f;
        mTemperature.setScale(new Scale(scalesize, scalesize));
        mTemperature.setText(mCity.getTemperatureString(mContext));
        updateClock();
    }

    public AnimationGroup bindAnimations(final AnimationGroup group) {
        for (int i = 0; i < group.getAnimationCount(); ++i) {
            final Animation animation = group.getAnimation(i);
            final int tag = animation.getTag();
            if (tag < mActorsMap.length && tag >= 0) {
                final Actor actor = mActorsMap[tag];
                if (actor != null) {
                    animation.setTarget(actor);
                }
            }
        }
        mCurrentAnimation = group;
        return group;
    }

    public Animation getBoundAnimation(final boolean isRelease) {
        final Animation boundAnimation = mCurrentAnimation;
        if (isRelease) {
            mCurrentAnimation = null;
        }
        return boundAnimation;
    }

    public static int getWeatherIcon(final LocationWeather city) {
        return WEATHER_ICONS[city.getWeather()];
    }
}
