package com.mediatek.weather3dwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RemoteViews.RemoteView;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Empty;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Text;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Object3D;
import com.mediatek.ngin3d.android.StageTextureView;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.android.Ngin3dLayoutInflater;
import com.mediatek.weather.WeatherUpdateResult;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.List;

@RemoteView
public class WeatherView extends StageTextureView{
    private static final String TAG = "W3D/WeatherView";

    private static final Color COLOR_BLACK = new Color(0x00, 0x00, 0x00);
    private static final Color COLOR_WHITE = new Color(0xff, 0xff, 0xff);

    private static final Color COLOR_TEMP = COLOR_BLACK;
    private static final Color COLOR_HIGH_LOW_TEMP = COLOR_BLACK;
    private static final Color COLOR_DATE = COLOR_BLACK;
    private static final Color COLOR_LAST_UPDATE = COLOR_WHITE;

    private static final float SIZE_TEXT_TEMP = 60;
    private static final float SIZE_TEXT_HIGH_LOW_TEMP = 21;
    private static final float SIZE_TEXT_DATE = 27;
    private static final float SIZE_TEXT_LAST_UPDATED = 18;
    private static final float SIZE_TEXT_CITY = 28;
    private static final float SIZE_TEXT_CITY_INDEX = 18;

    private static final Typeface FONT_SANS_BOLD = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

    private static final String[] SUNNY = {"tree_bend_gentle.glo", "leaves_blow_gentle.glo", "sheep_eat.glo"};
    private static final String[] WINDY = {"tree_bend_moderate.glo", "leaves_blow_moderate.glo", "sheep_sleep.glo"};
    private static final String[] BLUSTERY = {"clouds_show_hide_light.glo", "tree_bend_gail.glo", "leaves_blow_heavy.glo",
            "sheep_walk.glo"};
    private static final String[] CLOUDY = {"cloudy.glo", "tree_bend_gentle.glo", "leaves_blow_gentle.glo", "sheep_eat.glo"};
    private static final String[] RAINY = {"clouds_show_hide_heavy_bright.glo", "tree_bend_gail.glo", "leaves_blow_gail.glo",
            "sheep_sleep.glo", "rain_fall.glo", "rain_light.glo"};
    private static final String[] HEAVY_RAIN = {"clouds_show_hide_heavy_bright.glo", "tree_bend_gail.glo", "leaves_blow_gail.glo",
            "sheep_walk.glo", "rain_fall.glo", "drizzle.glo", "drizzle_long.glo", "rain_light.glo"};
    private static final String[] THUNDER = {"thunder.glo", "clouds_show_hide_heavy_dark.glo", "rain_fall.glo", "drizzle.glo",
            "tree_bend_gail.glo", "leaves_blow_gail.glo", "sheep_eat.glo", "thunder_light.glo"};
    private static final String[] SNOW = {"snow_small.glo", "tree_bend_gentle.glo", "leaves_blow_gentle_snow.glo", "sheep_sleep.glo"};
    private static final String[] HEAVY_SNOW = {"snow_heavy.glo", "tree_bend_gail.glo", "leaves_blow_gail_snow.glo", "sheep_walk.glo"};
    private static final String[] SNOW_RAIN = {"clouds_show_hide_heavy_bright.glo", "tree_bend_gail.glo", "leaves_blow_gail_snow.glo",
            "sheep_eat.glo", "snow_rain.glo", "snow_drizzle.glo", "snow_small.glo", "rain_light.glo"};
    private static final String[] FOG = {"fog1.glo", "tree_bend_gentle.glo", "leaves_blow_gentle.glo", "sheep_sleep.glo"};
    private static final String[] SAND = {"sand1.glo", "sandy_clouds.glo", "tree_bend_gentle.glo", "leaves_sandy.glo", "sheep_walk.glo"};

    private static final int FORECAST_PANEL_NUM = 3;
    private static int[] mForecastPanelX = new int[FORECAST_PANEL_NUM];
    private static int[] mForecastPanelY = new int[FORECAST_PANEL_NUM];

    private static Point[] mForecastUpperPoint = new Point[FORECAST_PANEL_NUM];
    private static Point[] mForecastCenterPoint = new Point[FORECAST_PANEL_NUM];
    private static Point[] mForecastLowerPoint = new Point[FORECAST_PANEL_NUM];

    private static PropertyAnimation[] mForecastPreviousDownAnimation = new PropertyAnimation[FORECAST_PANEL_NUM];
    private static PropertyAnimation[] mForecastCurrentDownAnimation = new PropertyAnimation[FORECAST_PANEL_NUM];
    private static PropertyAnimation[] mForecastPreviousUpAnimation = new PropertyAnimation[FORECAST_PANEL_NUM];
    private static PropertyAnimation[] mForecastCurrentUpAnimation = new PropertyAnimation[FORECAST_PANEL_NUM];

    // Replacement material types.
    private static final int EMT_WEATHER_MODEL_SOLID = 34;
    private static final int EMT_WEATHER_MODEL_BLEND = 35;
    private static final int EMT_UNLIT = 37;
    private static final int EMT_FOG = 40;

    private final HashMap<Integer, ArrayList<Animation>> mAnimationLists = new HashMap<Integer, ArrayList<Animation>>();
    private final HashMap<Integer, ArrayList<Glo3D>> mGloObjLists = new HashMap<Integer, ArrayList<Glo3D>>();

    private int mCurrentWeather;
    private int mCurrentDayNight = DayNight.DAY;
    private int mTotalCity;

    private final Stage mStage;
    private Container mRootContainer;
    private Container mCityContainer;
    private Container mWeatherContainer;
    private Container mForecastContainer;
    private Container mUpdateContainer;
    private Container mScenarioContainer;
    private Text mNoWeatherText;
    private Text mNoNetworkText;

    // glo model related
    private Glo3D mLandscapeGlo;
    private Glo3D mSunMoonGlo;
    private Glo3D mDayToNightGlo;
    private Glo3D mNightToDayGlo;
    private Glo3D mStarTwinkleGlo;
    private Glo3D mStarTwinkleSnowGlo;
    private BasicAnimation mSunMoonAni;
    private BasicAnimation mSunShowHideAni;
    private BasicAnimation mMoonShowHideAni;
    private BasicAnimation mDayToNightAni;
    private BasicAnimation mNightToDayAni;
    private BasicAnimation mStarTwinkleAni;
    private BasicAnimation mStarTwinkleSnowAni;

    private PropertyAnimation mChangeNextCityAnimation;
    private PropertyAnimation mChangePreviousCityAnimation;
    private static final String CHANGE_CITY_ANI_MARKER = "change";

    private PropertyAnimation mChangeSnowTextureAnimation;
    private static final String SNOW_PILE_000 = "000";
    private static final String SNOW_PILE_005 = "005";
    private static final String SNOW_PILE_010 = "010";
    private static final String SNOW_PILE_015 = "015";
    private static final String SNOW_PILE_020 = "020";
    private static final String SNOW_PILE_025 = "025";
    private static final String SNOW_PILE_030 = "030";
    private static final String SNOW_PILE_035 = "035";
    private static final String SNOW_PILE_040 = "040";
    private static final String SNOW_PILE_045 = "045";
    private static final String SNOW_PILE_050 = "050";
    private static final String SNOW_PILE_055 = "055";
    private static final String SNOW_PILE_060 = "060";
    private static final String SNOW_PILE_065 = "065";
    private static final String SNOW_PILE_070 = "070";
    private static final String SNOW_PILE_075 = "075";
    private static final String SNOW_PILE_080 = "080";
    private static final String SNOW_PILE_085 = "085";
    private static final String SNOW_PILE_090 = "090";
    private static final String SNOW_PILE_095 = "095";
    private static final String SNOW_PILE_100 = "100";
    private static final String SNOW_PILE_105 = "105";
    private static final String SNOW_PILE_110 = "110";
    private static final String SNOW_PILE_115 = "115";
    private static final String SNOW_PILE_120 = "120";
    private final HashMap<String, String> mSnowTextureList = new HashMap<String, String>();

    private static final int TAG_ANI_CHANGE_CITY_NEXT = 1;
    private static final int TAG_ANI_CHANGE_CITY_PREVIOUS = 2;
    private static final int TAG_ANI_CHANGE_SNOW_TEXTURE = 3;
    private static final int TAG_ANI_DAY_TO_NIGHT = 4;
    private static final int TAG_ANI_NIGHT_TO_DAY = 5;
    private static final int TAG_ANI_STAR_TWINKLE = 6;
    private static final int TAG_ANI_STAR_SNOW_TWINKLE = 7;
    private final HashMap<Integer, Glo3D> mGloList = new HashMap<Integer, Glo3D>();
    private final HashMap<Integer, BasicAnimation> mGloAniList = new HashMap<Integer, BasicAnimation>();
    private final LinkedList<Integer> mAniWaitingPlayQueue = new LinkedList<Integer>();

    private final WeatherInfo mWeatherInfo = new WeatherInfo();
    private Bundle mWeatherBundle;

    private final Context mContext;

    private GestureDetector mGestureDetector;

    private boolean mIsSnowing;

    private boolean mIsPaused;

    public enum STATE {
        INIT, NO_CITY, NO_NETWORK, NORMAL, UPDATING,
        SCROLLING, SCROLLED_WAIT_DATA, GOT_DATA_WAIT_SCROLL_END, SCROLLED_UPDATE_DATA
    }

    private STATE mState = STATE.INIT;

    public WeatherView(Context context) {
        this(context, null);
    }

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs, true);
        mContext = context;
        mStage = getStage();
        initialize();
        mGestureDetector = new GestureDetector(context, new WeatherGestureListener());
    }

    private void initialize() {
        LogUtil.v(TAG, "initialize(), width x height = (" + getWidth() + " x " + getHeight() + ")");

        mStage.setBackgroundColor(new Color(0x00, 0x00, 0x00, 0x00));
        mStage.setProjection(Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f);
        mRootContainer = new Container();
        mStage.add(mRootContainer);

        Image cityBarImage = (Image) Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.image_city_bar, mRootContainer);
        cityBarImage.setMaterialType(EMT_UNLIT);

        Image temperatureCloudImage = (Image) Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.image_temperature_cloud, mRootContainer);
        temperatureCloudImage.setMaterialType(EMT_UNLIT);

        initForecast();

        // No Weather Text
        mNoWeatherText = new Text(getResources().getString(R.string.no_weather));
        mNoWeatherText.setTextColor(COLOR_TEMP);
        mNoWeatherText.setBackgroundColor(new Color(0xff, 0xff, 0xff, 0xff));
        mNoWeatherText.setVisible(false);
        mRootContainer.add(mNoWeatherText);
        mNoWeatherText.setMaterialType(EMT_UNLIT);
        mNoWeatherText.setPosition(new Point(215, 470));

        // No Network Text
        mNoNetworkText = new Text(getResources().getString(R.string.no_network));
        mNoNetworkText.setTextColor(COLOR_TEMP);
        mNoNetworkText.setBackgroundColor(new Color(0xff, 0xff, 0xff, 0xff));
        mNoNetworkText.setVisible(false);
        mRootContainer.add(mNoNetworkText);
        mNoNetworkText.setMaterialType(EMT_UNLIT);
        mNoNetworkText.setPosition(new Point(215, 470, -1.0f));

        mScenarioContainer = (Container) Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.container_3d_model, mStage);
        int scenarioXRotation = 15;
        int scenarioYRotation = 15;
        int scenarioZRotation = 5;
        mScenarioContainer.setRotation(new Rotation(scenarioXRotation, scenarioYRotation, scenarioZRotation));
        int scale = 24;
        mScenarioContainer.setScale(new Scale(1.0f * scale, -1.0f * scale, -1f * scale));

        mLandscapeGlo = Glo3D.createFromAsset("landscape.glo");
        // Replace materials to use custom shader
        mLandscapeGlo.setMaterialType(EMT_WEATHER_MODEL_SOLID);
        // Make sure shadow uses alpha blending.
        mLandscapeGlo.setMaterialType("sheep_shadow", EMT_WEATHER_MODEL_BLEND);

        mLandscapeGlo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        mScenarioContainer.add(mLandscapeGlo);

        // Object 3D shadow
        Image shadowImage = (Image) Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.image_shadow, mRootContainer);
        shadowImage.setMaterialType(EMT_UNLIT);
        shadowImage.setRotation(new Rotation(0, 0, -3));

        initObject3D();

        mChangeNextCityAnimation = new PropertyAnimation(mScenarioContainer, "rotation",
                new Rotation(scenarioXRotation, scenarioYRotation, scenarioZRotation),
                new Rotation(scenarioXRotation + 360, scenarioYRotation, scenarioZRotation));
        mChangeNextCityAnimation.setDuration(960).setMode(Mode.LINEAR).setTag(TAG_ANI_CHANGE_CITY_NEXT);
        mChangeNextCityAnimation.addMarkerAtTime(CHANGE_CITY_ANI_MARKER, 720);
        mChangeNextCityAnimation.addListener(mAnimationCompletedHandler);

        mChangePreviousCityAnimation = new PropertyAnimation(mScenarioContainer, "rotation",
                new Rotation(scenarioXRotation, scenarioYRotation, scenarioZRotation),
                new Rotation(scenarioXRotation - 360, scenarioYRotation, scenarioZRotation));
        mChangePreviousCityAnimation.setDuration(960).setMode(Mode.LINEAR).setTag(TAG_ANI_CHANGE_CITY_PREVIOUS);
        mChangePreviousCityAnimation.addMarkerAtTime(CHANGE_CITY_ANI_MARKER, 240);
        mChangePreviousCityAnimation.addListener(mAnimationCompletedHandler);

        initSnowScene();
        mTotalCity = 0;
    }

    private void initObject3D() {
        initObject(SUNNY, WeatherType.ModelType.SUNNY, mScenarioContainer);
        initObject(WINDY, WeatherType.ModelType.WINDY, mScenarioContainer);
        initObject(BLUSTERY, WeatherType.ModelType.BLUSTERY, mScenarioContainer);
        initObject(CLOUDY, WeatherType.ModelType.CLOUDY, mScenarioContainer);
        initObject(RAINY, WeatherType.ModelType.RAIN, mScenarioContainer);
        initObject(HEAVY_RAIN, WeatherType.ModelType.HEAVY_RAIN, mScenarioContainer);
        initObject(THUNDER, WeatherType.ModelType.THUNDER, mScenarioContainer);
        initObject(SNOW, WeatherType.ModelType.SNOW, mScenarioContainer);
        initObject(HEAVY_SNOW, WeatherType.ModelType.HEAVY_SNOW, mScenarioContainer);
        initObject(SNOW_RAIN, WeatherType.ModelType.SNOW_RAIN, mScenarioContainer);
        initObject(FOG, WeatherType.ModelType.FOG, mScenarioContainer);
        initObject(SAND,WeatherType.ModelType.SAND, mScenarioContainer);

        mSunMoonGlo = Glo3D.createFromAsset("sunmoon.glo");
        mSunMoonGlo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        mScenarioContainer.add(mSunMoonGlo);
        mSunMoonGlo.setVisible(false);
        mSunMoonAni = mSunMoonGlo.getAnimation();

        Glo3D sunShowHideGlo = Glo3D.createFromAsset("sun_show_hide.glo");
        sunShowHideGlo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        mScenarioContainer.add(sunShowHideGlo);
        sunShowHideGlo.setVisible(false);
        mSunShowHideAni = sunShowHideGlo.getAnimation();

        Glo3D moonShowHideGlo = Glo3D.createFromAsset("moon_show_hide.glo");
        moonShowHideGlo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        mScenarioContainer.add(moonShowHideGlo);
        moonShowHideGlo.setVisible(false);
        mMoonShowHideAni = moonShowHideGlo.getAnimation();

        mDayToNightGlo = Glo3D.createFromAsset("sunmoon_day_to_night.glo");
        mDayToNightGlo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        mScenarioContainer.add(mDayToNightGlo);
        mDayToNightGlo.setVisible(false);
        mDayToNightAni = mDayToNightGlo.getAnimation();
        mDayToNightAni.setTag(TAG_ANI_DAY_TO_NIGHT);
        mDayToNightAni.addListener(mAnimationCompletedHandler);
        mGloList.put(TAG_ANI_DAY_TO_NIGHT, mDayToNightGlo);
        mGloAniList.put(TAG_ANI_DAY_TO_NIGHT, mDayToNightAni);

        mNightToDayGlo = Glo3D.createFromAsset("sunmoon_night_to_day.glo");
        mNightToDayGlo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        mScenarioContainer.add(mNightToDayGlo);
        mNightToDayGlo.setVisible(false);
        mNightToDayAni = mNightToDayGlo.getAnimation();
        mNightToDayAni.setTag(TAG_ANI_NIGHT_TO_DAY);
        mNightToDayAni.addListener(mAnimationCompletedHandler);
        mGloList.put(TAG_ANI_NIGHT_TO_DAY, mNightToDayGlo);
        mGloAniList.put(TAG_ANI_NIGHT_TO_DAY, mNightToDayAni);

        mStarTwinkleGlo = Glo3D.createFromAsset("stars_twinkle.glo");
        mStarTwinkleGlo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        mStarTwinkleGlo.setMaterialType(EMT_WEATHER_MODEL_SOLID);
        mStarTwinkleGlo.setVisible(false);
        mScenarioContainer.add(mStarTwinkleGlo);
        mStarTwinkleAni = mStarTwinkleGlo.getAnimation();
        mStarTwinkleAni.setTag(TAG_ANI_STAR_TWINKLE);
        mStarTwinkleAni.addListener(mAnimationCompletedHandler);
        mGloList.put(TAG_ANI_STAR_TWINKLE, mStarTwinkleGlo);
        mGloAniList.put(TAG_ANI_STAR_TWINKLE, mStarTwinkleAni);

        mStarTwinkleSnowGlo = Glo3D.createFromAsset("stars_twinkle_snow.glo");
        mStarTwinkleSnowGlo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
        mStarTwinkleSnowGlo.setMaterialType(EMT_WEATHER_MODEL_SOLID);
        mStarTwinkleSnowGlo.setVisible(false);
        mScenarioContainer.add(mStarTwinkleSnowGlo);
        mStarTwinkleSnowAni = mStarTwinkleSnowGlo.getAnimation();
        mStarTwinkleSnowAni.setTag(TAG_ANI_STAR_SNOW_TWINKLE);
        mStarTwinkleSnowAni.addListener(mAnimationCompletedHandler);
        mGloList.put(TAG_ANI_STAR_SNOW_TWINKLE, mStarTwinkleSnowGlo);
        mGloAniList.put(TAG_ANI_STAR_SNOW_TWINKLE, mStarTwinkleSnowAni);
    }

    private void initObject(String[] weather, int index, Container c) {
        LogUtil.v(TAG, "initObject, " + index);
        ArrayList<Animation> aniList = new ArrayList<Animation>();
        ArrayList<Glo3D> gloList = new ArrayList<Glo3D>();

        for (int i = 0; i < weather.length; i++) {
            Glo3D glo = Glo3D.createFromAsset(weather[i]);
            glo.setMaterialType(EMT_WEATHER_MODEL_SOLID);
            glo.setMaterialType("fog_cloud_01", EMT_FOG);
            glo.setAnchorPoint(new Point(0.f, 0.f, 0.f));
            LogUtil.v(TAG, "initObject, obj = " + weather[i]);
            gloList.add(glo);
            glo.setVisible(false);
            c.add(glo);
            Animation ani = glo.getAnimation();
            aniList.add(ani);
        }
        LogUtil.v(TAG,  "initObject, gloLists = " + gloList);
        mGloObjLists.put(index, gloList);
        mAnimationLists.put(index, aniList);
    }

    private void initForecast() {
        Resources res = getResources();
        mForecastPanelX[0] = res.getInteger(R.integer.x_left_panel);
        mForecastPanelX[1] = res.getInteger(R.integer.x_center_panel);
        mForecastPanelX[2] = res.getInteger(R.integer.x_right_panel);
        mForecastPanelY[0] = res.getInteger(R.integer.y_left_panel);
        mForecastPanelY[1] = res.getInteger(R.integer.y_center_panel);
        mForecastPanelY[2] = res.getInteger(R.integer.y_right_panel);

        int duration[] = {880, 1380, 1880};

        for (int i = 0; i < FORECAST_PANEL_NUM; i++) {
            // forecast panel image
            Point panelPoint = new Point(mForecastPanelX[i], mForecastPanelY[i], +0.01f);
            Image panelImage = Image.createFromResource(getResources(), R.raw.pnl_forecast);
            mRootContainer.add(panelImage);
            panelImage.setMaterialType(EMT_UNLIT);
            panelImage.setPosition(panelPoint);

            mForecastUpperPoint[i] = new Point(mForecastPanelX[i] - 5, mForecastPanelY[i] - 12 - 150);
            mForecastCenterPoint[i] = new Point(mForecastPanelX[i] - 5, mForecastPanelY[i] - 12);
            mForecastLowerPoint[i] = new Point(mForecastPanelX[i] - 5, mForecastPanelY[i] - 12 + 150);

            // initForecastAnimation
            mForecastCurrentDownAnimation[i] = new PropertyAnimation("position", mForecastUpperPoint[i], mForecastCenterPoint[i]);
            mForecastCurrentDownAnimation[i].setDuration(duration[i]).setMode(Mode.EASE_OUT_BOUNCE);

            mForecastPreviousDownAnimation[i] = new PropertyAnimation("position", mForecastCenterPoint[i], mForecastLowerPoint[i]);
            mForecastPreviousDownAnimation[i].setDuration(duration[i]).setMode(Mode.EASE_OUT_BOUNCE);

            mForecastPreviousUpAnimation[i] = new PropertyAnimation("position", mForecastCenterPoint[i], mForecastUpperPoint[i]);
            mForecastPreviousUpAnimation[i].setDuration(duration[i]).setMode(Mode.EASE_OUT_BOUNCE);

            mForecastCurrentUpAnimation[i] = new PropertyAnimation("position", mForecastLowerPoint[i], mForecastCenterPoint[i]);
            mForecastCurrentUpAnimation[i].setDuration(duration[i]).setMode(Mode.EASE_OUT_BOUNCE);
        }
    }

    private void startForecastDownAnimation() {
        // AnimationGroup doesn't support Property Animations with different duration, so start separately
        for (int i = 0; i < FORECAST_PANEL_NUM; i++) {
            mForecastCurrentDownAnimation[i].start();
            mForecastPreviousDownAnimation[i].start();
        }
    }

    private void startForecastUpAnimation() {
        for (int i = 0; i < FORECAST_PANEL_NUM; i++) {
            mForecastCurrentUpAnimation[i].start();
            mForecastPreviousUpAnimation[i].start();
        }
    }

    private void initSnowScene() {
        mChangeSnowTextureAnimation = new PropertyAnimation(new Empty(), "position",
                new Point(0, 0, 0), new Point(10, 0, 0));
        mChangeSnowTextureAnimation.setDuration(12000).setMode(Mode.LINEAR).setTag(TAG_ANI_CHANGE_SNOW_TEXTURE);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_005,   500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_010,  1000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_015,  1500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_020,  2000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_025,  2500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_030,  3000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_035,  3500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_040,  4000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_045,  4500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_050,  5000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_055,  5500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_060,  6000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_065,  6500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_070,  7000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_075,  7500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_080,  8000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_085,  8500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_090,  9000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_095,  9500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_100, 10000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_105, 10500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_110, 11000);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_115, 11500);
        mChangeSnowTextureAnimation.addMarkerAtTime(SNOW_PILE_120, 11800);
        mChangeSnowTextureAnimation.addListener(mAnimationCompletedHandler);

        mSnowTextureList.put(SNOW_PILE_000, "snow_dh_00000.jpg");
        mSnowTextureList.put(SNOW_PILE_005, "snow_dh_00005.jpg");
        mSnowTextureList.put(SNOW_PILE_010, "snow_dh_00010.jpg");
        mSnowTextureList.put(SNOW_PILE_015, "snow_dh_00015.jpg");
        mSnowTextureList.put(SNOW_PILE_020, "snow_dh_00020.jpg");
        mSnowTextureList.put(SNOW_PILE_025, "snow_dh_00025.jpg");
        mSnowTextureList.put(SNOW_PILE_030, "snow_dh_00030.jpg");
        mSnowTextureList.put(SNOW_PILE_035, "snow_dh_00035.jpg");
        mSnowTextureList.put(SNOW_PILE_040, "snow_dh_00040.jpg");
        mSnowTextureList.put(SNOW_PILE_045, "snow_dh_00045.jpg");
        mSnowTextureList.put(SNOW_PILE_050, "snow_dh_00050.jpg");
        mSnowTextureList.put(SNOW_PILE_055, "snow_dh_00055.jpg");
        mSnowTextureList.put(SNOW_PILE_060, "snow_dh_00060.jpg");
        mSnowTextureList.put(SNOW_PILE_065, "snow_dh_00065.jpg");
        mSnowTextureList.put(SNOW_PILE_070, "snow_dh_00070.jpg");
        mSnowTextureList.put(SNOW_PILE_075, "snow_dh_00075.jpg");
        mSnowTextureList.put(SNOW_PILE_080, "snow_dh_00080.jpg");
        mSnowTextureList.put(SNOW_PILE_085, "snow_dh_00085.jpg");
        mSnowTextureList.put(SNOW_PILE_090, "snow_dh_00090.jpg");
        mSnowTextureList.put(SNOW_PILE_095, "snow_dh_00095.jpg");
        mSnowTextureList.put(SNOW_PILE_100, "snow_dh_00100.jpg");
        mSnowTextureList.put(SNOW_PILE_105, "snow_dh_00105.jpg");
        mSnowTextureList.put(SNOW_PILE_110, "snow_dh_00110.jpg");
        mSnowTextureList.put(SNOW_PILE_115, "snow_dh_00115.jpg");
        mSnowTextureList.put(SNOW_PILE_120, "snow_dh_00120.jpg");
    }

    private void updateCityInfo(String cityName, int index, int total) {
        LogUtil.v(TAG, "updateCityInfo");

        int cityNameTag = 1;
        int indexTag = 2;

        if (mCityContainer == null) {
            mCityContainer = new Container();
            mRootContainer.add(mCityContainer);

            Text cityNameText = (Text)Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.text_city, mCityContainer);
            cityNameText.setMaterialType(EMT_UNLIT);
            cityNameText.setTypeface(FONT_SANS_BOLD);
            cityNameText.setTextSize(SIZE_TEXT_CITY);
            cityNameText.setTag(cityNameTag);
            cityNameText.setText(cityName);

            Text indexText = (Text)Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.text_city_index, mCityContainer);
            indexText.setMaterialType(EMT_UNLIT);
            indexText.setTypeface(FONT_SANS_BOLD);
            indexText.setTextSize(SIZE_TEXT_CITY_INDEX);
            indexText.setTag(indexTag);
            indexText.setText(getCityIndexString(index, total));
        } else {
            ((Text)(mCityContainer.findChildByTag(cityNameTag))).setText(cityName);
            ((Text)(mCityContainer.findChildByTag(indexTag))).setText(getCityIndexString(index, total));
        }
    }

    private void updateTemp(double temp, double tempHigh, double tempLow, int tempType) {
        LogUtil.v(TAG,"updateTemp");

        int tempTag = 1;
        int highLowTempTag = 2;

        if (mWeatherContainer == null) {
            mWeatherContainer = new Container();
            mRootContainer.add(mWeatherContainer);

            Text tempText = (Text)Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.text_temperature, mWeatherContainer);
            tempText.setMaterialType(EMT_UNLIT);
            tempText.setTypeface(FONT_SANS_BOLD);
            tempText.setTextColor(COLOR_TEMP);
            tempText.setTextSize(SIZE_TEXT_TEMP);
            tempText.setTag(tempTag);
            tempText.setText(getTempString(temp, tempType));

            Text highLowTempText = (Text)Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.text_high_low_temperature, mWeatherContainer);
            highLowTempText.setMaterialType(EMT_UNLIT);
            highLowTempText.setTypeface(FONT_SANS_BOLD);
            highLowTempText.setTextColor(COLOR_HIGH_LOW_TEMP);
            highLowTempText.setTextSize(SIZE_TEXT_HIGH_LOW_TEMP);
            highLowTempText.setTag(highLowTempTag);
            highLowTempText.setText(getHighLowTempString(tempHigh, tempLow));
        } else {
            ((Text)(mWeatherContainer.findChildByTag(tempTag))).setText(getTempString(temp, tempType));
            ((Text)(mWeatherContainer.findChildByTag(highLowTempTag))).setText(getHighLowTempString(tempHigh, tempLow));
        }
    }

    Container[] mPreviousContainer = new Container[3];
    Container[] mCurrentContainer = new Container[3];

    private void updateForecast(ForecastData[] data, int scroll_type) {
        LogUtil.v(TAG, "updateForecast");

        int totalForecast = 3;
        int containerTag[] = {1, 2, 3};

        int dayTag = 4;
        int iconTag = 5;
        int tempTag = 6;

        if (mForecastContainer == null) {
            mForecastContainer = new Container();
            mRootContainer.add(mForecastContainer);

            final int X_OFFSET_DAY_TEXT = -65;
            final int X_OFFSET_HIGH_LOW_TEMP = +60;
            final int Y_DELTA_DAY_TEXT = -62;
            final int Y_DELTA_HIGH_LOW_TEMP = +62;
            final Point dayPoint = new Point(X_OFFSET_DAY_TEXT, Y_DELTA_DAY_TEXT, -0.08f);
            final Point iconPoint = new Point(0, 0, -0.08f);
            final Point tempPoint = new Point(X_OFFSET_HIGH_LOW_TEMP, Y_DELTA_HIGH_LOW_TEMP, -0.08f);
            final Point dayAnchorPoint = new Point(0.0f, 0.0f);
            final Point tempAnchorPoint = new Point(1.0f, 1.0f);

            for (int i = 0; i < totalForecast; i++) {
                Container subContainer = new Container();
                subContainer.setTag(containerTag[i]);

                int displayAreaX1 = mForecastPanelX[i] - 70;
                int displayAreaY1 = mForecastPanelY[i] - 72;
                int displayAreaWidth = 140;
                int displayAreaHeight = 122;
                int displayAreaX2 = displayAreaX1 + displayAreaWidth;
                int displayAreaY2 = displayAreaY1 + displayAreaHeight;

                Box displayArea = new Box(displayAreaX1, displayAreaY1, displayAreaX2, displayAreaY2);
                LogUtil.v(TAG, "box = (" + displayAreaX1 + ", " + displayAreaY1 + ", " + displayAreaX2 + ", " + displayAreaY2 + ")");

                for (int j = 0; j < 2; j++) {
                    Text dayText = new Text(getDate(data[i].getDateOffset()));
                    dayText.setMaterialType(EMT_UNLIT);
                    dayText.setTypeface(FONT_SANS_BOLD);
                    dayText.setPosition(dayPoint);
                    dayText.setTextColor(COLOR_DATE);
                    dayText.setTextSize(SIZE_TEXT_DATE);
                    dayText.setTag(dayTag);
                    dayText.setDisplayArea(displayArea);
                    dayText.setAnchorPoint(dayAnchorPoint);

                    Image weatherImage = Image.createFromResource(getResources(), WeatherType.getWeatherIcon(data[i].getWeatherCondition()));
                    weatherImage.setMaterialType(EMT_UNLIT);
                    weatherImage.setPosition(iconPoint);
                    weatherImage.setTag(iconTag);
                    weatherImage.setDisplayArea(displayArea);

                    Text highLowTempText = new Text(getHighLowTempString(data[i].getHighTemp(), data[i].getLowTemp()));
                    highLowTempText.setMaterialType(EMT_UNLIT);
                    highLowTempText.setTypeface(FONT_SANS_BOLD);
                    highLowTempText.setPosition(tempPoint);
                    highLowTempText.setTextColor(COLOR_HIGH_LOW_TEMP);
                    highLowTempText.setTextSize(SIZE_TEXT_HIGH_LOW_TEMP);
                    highLowTempText.setTag(tempTag);
                    highLowTempText.setDisplayArea(displayArea);
                    highLowTempText.setAnchorPoint(tempAnchorPoint);

                    Container dataContainer = new Container();
                    if (j == 1) {
                        // currentContainer
                        dataContainer.setPosition(mForecastCenterPoint[i]);
                        mCurrentContainer[i] = dataContainer;
                    } else {
                        // previousContainer
                        dataContainer.setPosition(mForecastUpperPoint[i]);
                        mPreviousContainer[i] = dataContainer;
                    }
                    dataContainer.add(dayText);
                    dataContainer.add(weatherImage);
                    dataContainer.add(highLowTempText);

                    subContainer.add(dataContainer);
                }
                mForecastContainer.add(subContainer);
            }
        } else {
            Container mTempContainer;
            for (int i = 0; i < totalForecast; i++) {
                // if not scroll up or down, then just update the currentContainer context.
                // if scroll up or down, then change the currentContainer and previousContainer, update the currentContainer context, then play animation

                if (scroll_type == ScrollType.SCROLL_DOWN || scroll_type == ScrollType.SCROLL_UP) {
                    mTempContainer = mCurrentContainer[i];
                    mCurrentContainer[i] = mPreviousContainer[i];
                    mPreviousContainer[i] = mTempContainer;

                    if (scroll_type == ScrollType.SCROLL_DOWN) {
                        mForecastCurrentDownAnimation[i].setTarget(mCurrentContainer[i]);
                        mForecastPreviousDownAnimation[i].setTarget(mPreviousContainer[i]);
                    } else if (scroll_type == ScrollType.SCROLL_UP) {
                        mForecastCurrentUpAnimation[i].setTarget(mCurrentContainer[i]);
                        mForecastPreviousUpAnimation[i].setTarget(mPreviousContainer[i]);
                    }
                }

                ((Text)(mCurrentContainer[i].findChildByTag(dayTag))).setText(getDate(data[i].getDateOffset()));
                ((Image)(mCurrentContainer[i].findChildByTag(iconTag))).setImageFromResource(getResources(), WeatherType.getWeatherIcon(data[i].getWeatherCondition()));
                ((Text)(mCurrentContainer[i].findChildByTag(tempTag))).setText(getHighLowTempString(data[i].getHighTemp(), data[i].getLowTemp()));
            }
        }
    }

    private void updateLastUpdate(long lastUpdate, boolean updating) {
        LogUtil.v(TAG, "updateLastUpdate");

        int updateTag = 1;

        String updateString;
        if (updating) {
            updateString = getResources().getString(R.string.updating);
        } else {
            updateString = getLastUpdateString(lastUpdate);
        }

        if (mUpdateContainer == null) {
            mUpdateContainer = new Container();
            mRootContainer.add(mUpdateContainer);

            Text lastUpdatedText = (Text)Ngin3dLayoutInflater.inflateLayout(mContext, R.xml.text_last_update, mUpdateContainer);
            lastUpdatedText.setMaterialType(EMT_UNLIT);
            lastUpdatedText.setTag(updateTag);
            lastUpdatedText.setTextColor(COLOR_LAST_UPDATE);
            lastUpdatedText.setTextSize(SIZE_TEXT_LAST_UPDATED);
            lastUpdatedText.setTypeface(FONT_SANS_BOLD);
            lastUpdatedText.setShadowLayer(1, 2, 2, 0xff000000);
            lastUpdatedText.setText(updateString);
        } else {
            ((Text)(mUpdateContainer.findChildByTag(updateTag))).setText(updateString);
        }
    }

    private void updateWeatherObject(int condition, int dayNight) {
        int currentWeatherModelType = WeatherType.convertToModelType(mCurrentWeather);
        int conditionModelType = WeatherType.convertToModelType(condition);
        LogUtil.v(TAG, "updateWeatherObject - (current, new, currentDay, newDay) = (" +  mCurrentWeather + ", " +
                condition + ", " + mCurrentDayNight + ", " + dayNight + ")");

        if (mCurrentWeather != condition) {
            if (mCurrentWeather != 0) {
                ArrayList<Animation> aniList = mAnimationLists.get(currentWeatherModelType);
                for(int i = 0; i < aniList.size(); i++) {
                    aniList.get(i).stop();
                }
                ArrayList<Glo3D> gloList = mGloObjLists.get(currentWeatherModelType);
                for (int i = 0; i < gloList.size(); i++) {
                    gloList.get(i).setVisible(false);
                }
            }

            ArrayList<Glo3D> newGloList = mGloObjLists.get(conditionModelType);
            for (int i = 0; i < newGloList.size(); i++) {
                newGloList.get(i).setVisible(true);
            }
            ArrayList<Animation> newAniList = mAnimationLists.get(conditionModelType);
            for(int i = 0; i < newAniList.size(); i++) {
                ((BasicAnimation)(newAniList.get(i))).setLoop(true).start();
            }
        }

        if (mCurrentWeather != 0) {
            resetWeatherObject();
        }

        boolean isCurrentSnow = WeatherType.isSnowModelType(currentWeatherModelType);
        boolean isNewSnow = WeatherType.isSnowModelType(conditionModelType);

        if (WeatherType.isSnowModelType(conditionModelType)) {
            startSnow();
        } else if (WeatherType.isSandModelType(conditionModelType)) {
            startSandy();
        }

        // handle sun/moon show or not show start
        boolean isCurrentNeedSun = WeatherType.isSunMoonNeededModelType(currentWeatherModelType);
        boolean isNewNeedSun = WeatherType.isSunMoonNeededModelType(conditionModelType);

        // if new condition does not need sun/moon, then stop all the ani and hide glo
        if (!isNewNeedSun) {
            mSunMoonGlo.setVisible(false);
            mSunMoonAni.stop();
            mSunShowHideAni.stop();
            LogUtil.v(TAG, "sun_show_hide_stop");
            mMoonShowHideAni.stop();
            LogUtil.v(TAG, "moon_show_hide_stop");
            mDayToNightAni.stop();
            mNightToDayAni.stop();
            // sun_rise and moon_rise stop;
        }

        if (isNewNeedSun) {
            if (mCurrentWeather == 0 || !isCurrentNeedSun) {
                if (dayNight == DayNight.DAY) {
                    addAniToQueue(TAG_ANI_NIGHT_TO_DAY);
                    mSunShowHideAni.setLoop(true).start();
                    LogUtil.v(TAG, "sun_show_hide_start");
                } else {
                    addAniToQueue(TAG_ANI_DAY_TO_NIGHT);
                    mMoonShowHideAni.setLoop(true).start();
                    LogUtil.v(TAG, "moon_show_hide_start");
                }
            } else if (mCurrentDayNight != dayNight) {
                if (dayNight == DayNight.DAY) {
                    addAniToQueue(TAG_ANI_NIGHT_TO_DAY);
                } else {
                    addAniToQueue(TAG_ANI_DAY_TO_NIGHT);
                }
                if (!mSunShowHideAni.isStarted()) {
                    mSunShowHideAni.setLoop(true).start();
                    LogUtil.v(TAG, "sun_show_hide_start");
                }
                if (!mMoonShowHideAni.isStarted()) {
                    mMoonShowHideAni.setLoop(true).start();
                    LogUtil.v(TAG, "moon_show_hide_start");
                }
            }
        } else {
            removeAniFromQueue(TAG_ANI_DAY_TO_NIGHT);
            removeAniFromQueue(TAG_ANI_NIGHT_TO_DAY);
        }

        // if new condition need sun/moon, then set sun/moon glo visible and start animation
        if ((mCurrentWeather == 0  || !isCurrentNeedSun) && isNewNeedSun) {
            if (!mSunMoonGlo.getVisible()) {
                mSunMoonGlo.setVisible(true);
                mSunMoonAni.start();
            }
        }

        if (!(dayNight == DayNight.NIGHT && mCurrentDayNight == DayNight.NIGHT) || !isCurrentNeedSun || (isNewSnow != isCurrentSnow)) {
            // ! is CurrentNeedSun: from a rain to sunny, rainy has no sun or moon, so when changing to sunny,
            // need show sun/moon, after moon show, the star will show. so need to hide star first.

            // !(new day night = night and current day night = night)
            // if true, then originally, the star is existed, so no need to set invisible.
            // if false, then day to night, or night to day, in both cases, need to set invisible first
            setStarTwinkleInvisible();
        }
        // if dayNight == night, then show day to night first then when day to night end, the star will show

        if (dayNight == DayNight.NIGHT) {
            if (isNewSnow) {
                if (!mStarTwinkleSnowGlo.getVisible()) {
                    addAniToQueue(TAG_ANI_STAR_SNOW_TWINKLE);
                }
            } else {
                if (!mStarTwinkleGlo.getVisible()) {
                    addAniToQueue(TAG_ANI_STAR_TWINKLE);
                }
            }
        } else if (dayNight == DayNight.DAY) {
            // if new DayNight = DAY, then remove the star twinkle animation if the animation queue have such animation
            removeAniFromQueue(TAG_ANI_STAR_SNOW_TWINKLE);
            removeAniFromQueue(TAG_ANI_STAR_TWINKLE);
        }

        addAniToQueue(0);
        playQueuedAni();
        mCurrentWeather = condition;
        mCurrentDayNight = dayNight;
    }

    private void addAniToQueue(int aniTag) {
        LogUtil.v(TAG, "total = " + mAniWaitingPlayQueue.size() + ", tag = " + aniTag + ", queue: " + mAniWaitingPlayQueue);
        if (mAniWaitingPlayQueue.contains(0)) {
            // end of animation token existed, means a set of animation waited to play
            if (aniTag == TAG_ANI_NIGHT_TO_DAY) {
                mAniWaitingPlayQueue.clear();
                mAniWaitingPlayQueue.add(aniTag);
            } else if (aniTag == TAG_ANI_DAY_TO_NIGHT) {
                mAniWaitingPlayQueue.clear();
                mAniWaitingPlayQueue.add(aniTag);
            } else if (aniTag == TAG_ANI_STAR_SNOW_TWINKLE || aniTag == TAG_ANI_STAR_TWINKLE) {
                if (mAniWaitingPlayQueue.contains(TAG_ANI_STAR_SNOW_TWINKLE)) {
                    int indexSnowStar = mAniWaitingPlayQueue.indexOf(TAG_ANI_STAR_SNOW_TWINKLE);
                    mAniWaitingPlayQueue.set(indexSnowStar, aniTag);
                } else if (mAniWaitingPlayQueue.contains(TAG_ANI_STAR_TWINKLE)) {
                    int indexStar = mAniWaitingPlayQueue.indexOf(TAG_ANI_STAR_TWINKLE);
                    mAniWaitingPlayQueue.set(indexStar, aniTag);
                } else {
                    mAniWaitingPlayQueue.clear();
                    mAniWaitingPlayQueue.add(aniTag);
                }
            } else if (aniTag == 0) {
                if (mAniWaitingPlayQueue.getLast() != aniTag) {
                    mAniWaitingPlayQueue.add(aniTag);
                }
            }
        } else {
            // add new aniTag directly
            mAniWaitingPlayQueue.add(aniTag);
        }
        LogUtil.v(TAG, "afterAddedQueue: " + mAniWaitingPlayQueue);
    }

    private void removeAniFromQueue(int aniTag) {
        LogUtil.v(TAG, "total = " + mAniWaitingPlayQueue.size() + ", tag = " + aniTag + ", queue: " + mAniWaitingPlayQueue);
        int index = mAniWaitingPlayQueue.indexOf(aniTag);
        if (index != -1) {
            mAniWaitingPlayQueue.remove(index);
        }
        LogUtil.v(TAG, "afterRemovedQueue: " + mAniWaitingPlayQueue);
    }

    private void playQueuedAni() {
        if (mIsPaused) {
            return;
        }

        if (mAniWaitingPlayQueue.isEmpty()) {
            return;
        }

        int aniTag = mAniWaitingPlayQueue.remove();
        LogUtil.v(TAG, "playQueuedAni, tag = " + aniTag);

        if (aniTag == 0) {
            setState(STATE.NORMAL);
        } else {
            if (aniTag == TAG_ANI_STAR_TWINKLE) {
                mStarTwinkleSnowAni.stop();
                mStarTwinkleSnowGlo.setVisible(false);

                if (!mStarTwinkleGlo.getVisible()) {
                    mStarTwinkleGlo.setVisible(true);
                    mStarTwinkleAni.setLoop(true).start();
                }
            } else if (aniTag == TAG_ANI_STAR_SNOW_TWINKLE) {
                mStarTwinkleAni.stop();
                mStarTwinkleGlo.setVisible(false);

                if (!mStarTwinkleSnowGlo.getVisible()) {
                    mStarTwinkleSnowGlo.setVisible(true);
                    mStarTwinkleSnowAni.setLoop(true).start();
                }
            } else {
                mGloList.get(aniTag).setVisible(true);
                mGloAniList.get(aniTag).start();
            }
        }
    }

    private void setContainerVisible(boolean visible) {
        if (mCityContainer != null) {
            mCityContainer.setVisible(visible);
        }
        if (mWeatherContainer != null) {
            mWeatherContainer.setVisible(visible);
        }
        if (mForecastContainer != null) {
            mForecastContainer.setVisible(visible);
        }
        if (mUpdateContainer != null) {
            mUpdateContainer.setVisible(visible);
        }
    }

    private void setCityContainerVisible(boolean visible) {
        if (mCityContainer != null) {
            mCityContainer.setVisible(visible);
        }
    }

    private void setUpdateContainerVisible(boolean visible) {
        if (mUpdateContainer != null) {
            mUpdateContainer.setVisible(visible);
        }
    }

    private void setNoWeatherScenarioContainer() {
        setSpareWeatherGloInvisible();
        int currentWeatherModelType = WeatherType.convertToModelType(mCurrentWeather);

        if (WeatherType.isModelTypeInRange(currentWeatherModelType)) {
            ArrayList<Glo3D> gloList = mGloObjLists.get(currentWeatherModelType);
            for (int i = 0; i < gloList.size(); i++) {
                gloList.get(i).setVisible(false);
            }
            ArrayList<Animation> aniList = mAnimationLists.get(currentWeatherModelType);
            for(int i = 0; i < aniList.size(); i++) {
                ((BasicAnimation)(aniList.get(i))).stop();
            }
        }
    }

    private void setSpareWeatherGloInvisible() {
        mSunMoonGlo.setVisible(false);
        mSunShowHideAni.stop();
        LogUtil.v(TAG, "sun_show_hide_stop");
        mMoonShowHideAni.stop();
        LogUtil.v(TAG, "moon_show_hide_stop");
        mDayToNightGlo.setVisible(false);
        mNightToDayGlo.setVisible(false);
        setStarTwinkleInvisible();
    }

    private void changeGroundTexture(String filename) {
        mLandscapeGlo.setMaterialTexture("ground001", filename);
    }

    private void resetGroundSnowTexture() {
        changeGroundTexture(mSnowTextureList.get(SNOW_PILE_000));
    }

    private void changeGroundSandyTexture() {
        changeGroundTexture("landscape_sandy.jpg");
    }

    private void resetGroundTexture() {
        changeGroundTexture("new_weather.jpg");
    }

    private void changeTreeTexture(String filename) {
        mLandscapeGlo.setMaterialTexture("tree", filename);
    }

    private void changeTreeSnowTexture() {
        changeTreeTexture("tree_snow.jpg");
    }

    private void changeTreeSandyTexture() {
        changeTreeTexture("tree_sandy.jpg");
    }

    private void resetTreeTexture() {
        changeTreeTexture("tree.jpg");
    }

    private void changeWaterTexture(String filename) {
        mLandscapeGlo.setMaterialTexture("water", filename);
    }

    private void changeWaterSnowTexture() {
        changeWaterTexture("water_snow.png");
    }

    private void changeWaterSandyTexture() {
        changeWaterTexture("water_sandy.png");
    }

    private void resetWaterTexture() {
        changeWaterTexture("water.png");
    }

    private void startSnow() {
        if (mIsSnowing) {
            mChangeSnowTextureAnimation.stop();
        } else {
            changeTreeSnowTexture();
            changeWaterSnowTexture();
        }
        mChangeSnowTextureAnimation.setLoop(true).start();
        resetGroundSnowTexture();

        mIsSnowing = true;
    }

    private void startSandy() {
        changeTreeSandyTexture();
        changeWaterSandyTexture();
        changeGroundSandyTexture();
    }

    private void resetWeatherObject() {
        // use to stop snow, stop sand, reset weather object
        if (mIsSnowing) {
            mChangeSnowTextureAnimation.stop();
        }
        resetGroundTexture();
        resetTreeTexture();
        resetWaterTexture();
        mIsSnowing = false;
    }

    private String getTempString(double temp, int unit) {
        String unitString;
        if (unit == 0) {
            unitString = "C";
        } else {
            unitString = "F";
        }
        return (int)temp + getResources().getString(R.string.degree) + unitString;
    }

    private String getHighLowTempString(double tempHigh, double tempLow) {
        return (int)tempLow + getResources().getString(R.string.degree) + "/" + (int)tempHigh + getResources().getString(R.string.degree);
    }

    private String getCityIndexString(int index, int total) {
        return index + "/" + total;
    }

    private String getLastUpdateString(long lastUpdateTime) {
        String update;
        if (Util.isSameDay(lastUpdateTime, System.currentTimeMillis())) {
            update = Util.getTimeString(mContext, lastUpdateTime);
        } else {
            update = Util.getDateString(lastUpdateTime);
        }

        return getResources().getString(R.string.last_update) + " : " + update;
    }

    private String getDate(int dateOffset) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.getDefault());
        LogUtil.v(TAG, "getDate = " + sdf.format(calendar.getTime()));
        calendar.add(Calendar.DATE, dateOffset);
        return sdf.format(calendar.getTime());
    }

    private void updateWeather(WeatherInfo weather, int scroll_type) {
        LogUtil.v(TAG, "updateWeather, name = " + weather.getCityName());
        LogUtil.v(TAG, "updateWeather, temp = " + weather.getCurrentTemp());
        LogUtil.v(TAG, "updateWeather, low/high = " + weather.getTempLow() + "/" + weather.getTempHigh());
        updateCityInfo(weather.getCityName(), weather.getCityIndex() + 1, weather.getTotalCity());
        updateTemp(weather.getCurrentTemp(), weather.getTempHigh(), weather.getTempLow(), weather.getTempType());
        updateForecast(weather.getForecastData(), scroll_type);
        updateLastUpdate(weather.getLastUpdated(), false);
        updateWeatherObject(weather.getCondition(), Util.getDayNight(weather.getTimeZone()));
    }

    private void setState(STATE state) {
        LogUtil.v(TAG, "setState, (" + mState + " to " + state + ")");
        mState = state;
    }

    @android.view.RemotableViewMethod
    public void updateWeatherView(Bundle bundle) {
        LogUtil.v(TAG, "updateWeatherView - state = " + mState);
        mTotalCity = bundle.getInt("totalCity");

        if (mTotalCity == 0) {
            LogUtil.v(TAG, "updateWeatherView - totalCity = 0");
            setContainerVisible(false);
            setNoWeatherScenarioContainer();

            mNoNetworkText.setVisible(false);
            mNoWeatherText.setVisible(true);

            // reset currentWeather and currentDayNight
            mCurrentWeather = 0;
            mCurrentDayNight = DayNight.DAY;

            setState(STATE.NO_CITY);
        } else {
            LogUtil.v(TAG, "updateWeatherView - totalCity > 0");
            if (mState == STATE.SCROLLING) {
                // if needAnimation == false, then throw exception
                setState(STATE.GOT_DATA_WAIT_SCROLL_END);
                mWeatherBundle = bundle;
            } else {
                // STATE.INIT is happening when start a new demo widget
                // STATE.UPDATING is happening when start a new real data widget
                // STATE.SCROLLED_WAIT_DATA is happening when user swipe the widget
                updateScreen(bundle);
            }
        }
    }

    private void updateScreen(Bundle bundle) {

        mWeatherInfo.setCityIndex(bundle.getInt("cityIndex"));
        mWeatherInfo.setTotalCity(bundle.getInt("totalCity"));
        mWeatherInfo.setCityName(bundle.getString("cityName"));
        mWeatherInfo.setLastUpdated(bundle.getLong("lastUpdated"));
        String timeZoneString = bundle.getString("timeZone");

        int result = bundle.getInt("result");
        LogUtil.v(TAG, "result = " + result);
        mWeatherInfo.setResult(result);

        if (result == WeatherUpdateResult.SUCCESS) {
            setState(STATE.SCROLLED_UPDATE_DATA);

            mWeatherInfo.setCondition(bundle.getInt("condition"));
            mWeatherInfo.setTempType(bundle.getInt("tempType"));
            mWeatherInfo.setCurrentTemp(bundle.getDouble("temp"));
            mWeatherInfo.setTempHigh(bundle.getDouble("highTemp"));
            mWeatherInfo.setTempLow(bundle.getDouble("lowTemp"));
            mWeatherInfo.setTimeZone(timeZoneString);
            ForecastData[] data = new ForecastData[3];
            data[0] = new ForecastData(bundle.getInt("firstOffset"), bundle.getDouble("firstHighTemp"), bundle.getDouble("firstLowTemp"), bundle.getInt("firstForecast"));
            data[1] = new ForecastData(bundle.getInt("secondOffset"), bundle.getDouble("secondHighTemp"), bundle.getDouble("secondLowTemp"), bundle.getInt("secondForecast"));
            data[2] = new ForecastData(bundle.getInt("thirdOffset"), bundle.getDouble("thirdHighTemp"), bundle.getDouble("thirdLowTemp"), bundle.getInt("thirdForecast"));
            mWeatherInfo.setForecastData(data);
            int scroll_type = bundle.getInt("order");
            LogUtil.v(TAG, "scroll_type = " + scroll_type);

            mNoWeatherText.setVisible(false);
            mNoNetworkText.setVisible(false);
            setContainerVisible(true);

            updateWeather(mWeatherInfo, scroll_type);

            if (scroll_type == ScrollType.SCROLL_UP) {
                startForecastUpAnimation();
            } else if (scroll_type == ScrollType.SCROLL_DOWN) {
                startForecastDownAnimation();
            }
        } else {
            // no network case
            updateCityInfo(mWeatherInfo.getCityName(), mWeatherInfo.getCityIndex() + 1, mWeatherInfo.getTotalCity());
            updateLastUpdate(mWeatherInfo.getLastUpdated(), false);

            setContainerVisible(false);
            setNoWeatherScenarioContainer();
            mNoWeatherText.setVisible(false);

            setCityContainerVisible(true);
            setUpdateContainerVisible(true);

            if (result == WeatherUpdateResult.ERROR_NETWORK_NOT_AVAILABLE) {
                mNoNetworkText.setText(getResources().getString(R.string.no_network));
            } else {
                mNoNetworkText.setText(getResources().getString(R.string.update_fail));
            }
            mNoNetworkText.setVisible(true);

            mCurrentDayNight = Util.getDayNight(timeZoneString);
            mCurrentWeather = 0;

            setState(STATE.NO_NETWORK);
        }
    }

    @android.view.RemotableViewMethod
    public void showUpdating(int i) {
        LogUtil.v(TAG, "showUpdating");
        setContainerVisible(false);
        setNoWeatherScenarioContainer();
        mNoNetworkText.setVisible(false);
        mNoWeatherText.setVisible(false);

        setCityContainerVisible(true);
        setUpdateContainerVisible(true);
        updateLastUpdate(0, true);

        mCurrentWeather = 0;
        setState(STATE.UPDATING);
    }

    @android.view.RemotableViewMethod
    public void switchDayNight(int switchType) {
        if (!(mState == STATE.NORMAL || mState == STATE.SCROLLED_UPDATE_DATA)) {
            // when in init, no city, no network -> no weather condition shown -> don't need to update day night
            return;
        }
        int dayNight = Util.getDayNight(mWeatherInfo.getTimeZone());
        LogUtil.v(TAG, "switchDayNight, (original, current) = (" + mCurrentDayNight + ", " + dayNight + ")");

        if (dayNight == mCurrentDayNight) {
            // do nothing
            return;
        }

        setStarTwinkleInvisible();
        int currentWeatherModelType = WeatherType.convertToModelType(mCurrentWeather);
        boolean isSunMoonNeeded = WeatherType.isSunMoonNeededModelType(currentWeatherModelType);

        if (dayNight == DayNight.DAY) {
            // handle sun/moon
            if (mMoonShowHideAni.isStarted()) {
                mMoonShowHideAni.stop();
            }
            removeAniFromQueue(TAG_ANI_DAY_TO_NIGHT);

            if (isSunMoonNeeded) {
                mSunShowHideAni.setLoop(true).start();
                addAniToQueue(TAG_ANI_NIGHT_TO_DAY);
            }
            removeAniFromQueue(TAG_ANI_STAR_SNOW_TWINKLE);
            removeAniFromQueue(TAG_ANI_STAR_TWINKLE);
        } else {
            // handle sun/moon
            if (mSunShowHideAni.isStarted()) {
                mSunShowHideAni.stop();
            }
            removeAniFromQueue(TAG_ANI_NIGHT_TO_DAY);

            if (isSunMoonNeeded) {
                mMoonShowHideAni.setLoop(true).start();
                addAniToQueue(TAG_ANI_DAY_TO_NIGHT);
            }

            // handle star
            if (WeatherType.isSnowModelType(currentWeatherModelType)) {
                addAniToQueue(TAG_ANI_STAR_SNOW_TWINKLE);
            } else {
                addAniToQueue(TAG_ANI_STAR_TWINKLE);
            }
        }

        addAniToQueue(0);
        playQueuedAni();

        mCurrentDayNight = dayNight;
    }

    @Override
    public void onPause() {
        LogUtil.v(TAG);
        super.onPause();
        mIsPaused = true;
    }

    @Override
    public void onResume() {
        LogUtil.v(TAG);
        super.onResume();
        mIsPaused = false;
        playQueuedAni();
    }

    private void printWeatherBundle(Bundle bundle) {
        LogUtil.v(TAG,
                "cityIndex = " + bundle.getInt("cityIndex") +
                ", totalCity = " + bundle.getInt("totalCity") +
                ", cityName = " + bundle.getString("cityName") +
                ", condition = " + bundle.getInt("condition") +
                ", tempType = " + bundle.getInt("tempType") +
                ", temp = " + bundle.getDouble("temp") +
                ", tempHigh = " + bundle.getDouble("highTemp") +
                ", tempLow = " + bundle.getDouble("lowTemp") +
                ", lastUpdated = " + bundle.getLong("lastUpdated") +
                ", timeZone = " + bundle.getString("timeZone") +
                ", forecast-1 = " + bundle.getDouble("firstLowTemp") + "/" + bundle.getDouble("firstHighTemp") + "/" + bundle.getInt("firstForecast") +
                ", forecast-2 = " + bundle.getDouble("secondLowTemp") + "/" + bundle.getDouble("secondHighTemp") + "/" + bundle.getInt("secondForecast") +
                ", forecast-3 = " + bundle.getDouble("thirdLowTemp") + "/" + bundle.getDouble("thirdHighTemp") + "/" + bundle.getInt("thirdForecast"));
    }

    private void setStarTwinkleInvisible() {
        mStarTwinkleAni.stop();
        mStarTwinkleGlo.setVisible(false);

        mStarTwinkleSnowAni.stop();
        mStarTwinkleSnowGlo.setVisible(false);
    }

    private final Animation.Listener mAnimationCompletedHandler = new Animation.Listener() {
        public void onStarted(Animation animation) {
            final int tag = animation.getTag();
            //LogUtil.v(TAG, "ani - onStarted: " + animation.getName() + " ," + animation);

            if (tag == TAG_ANI_STAR_TWINKLE || tag == TAG_ANI_STAR_SNOW_TWINKLE) {
                playQueuedAni();
            }
        }

        public void onMarkerReached(Animation animation, int direction, String marker) {
            final int tag = animation.getTag();
            //LogUtil.v(TAG, "ani - onMarkerReached: " + animation.getName() + " ," + animation);

            if (tag == TAG_ANI_CHANGE_SNOW_TEXTURE) {
                changeGroundTexture(mSnowTextureList.get(marker));
            }
        }

        public void onCompleted(Animation animation) {
            final int tag = animation.getTag();
            //LogUtil.v(TAG, "ani - onCompleted: " + tag);

            if (tag == TAG_ANI_CHANGE_SNOW_TEXTURE) {
                resetGroundSnowTexture();
            } else if (tag == TAG_ANI_CHANGE_CITY_NEXT || tag == TAG_ANI_CHANGE_CITY_PREVIOUS) {
                LogUtil.v(TAG, "model_scroll_end");
                if (mState == STATE.SCROLLING) {
                    setState(STATE.SCROLLED_WAIT_DATA);
                    // do nothing
                } else if (mState == STATE.GOT_DATA_WAIT_SCROLL_END) {
                    // STATE.GOT_DATA_WAIT_SCROLL_END is happening when user swipe the widget
                    // update screen directly
                    updateScreen(mWeatherBundle);
                } else {
                    // ToDo:
                    // throw exception
                }
            } else if (tag == TAG_ANI_DAY_TO_NIGHT || tag == TAG_ANI_NIGHT_TO_DAY) {
                playQueuedAni();
                if (tag == TAG_ANI_DAY_TO_NIGHT) {
                    // from DAY to NIGHT, so stop SUN show hide
                    mSunShowHideAni.stop();
                    LogUtil.v(TAG, "sun_show_hide_stop");
                } else {
                    // from NIGHT to DAY, so stop MOON show hide
                    mMoonShowHideAni.stop();
                    LogUtil.v(TAG, "moon_show_hide_stop");
                }
            }
        }
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        LogUtil.v(TAG, "onSurfaceCreated(), width x height = (" + getWidth() + " x " + getHeight() + ")");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        LogUtil.v(TAG, "onSurfaceChanged(), width x height = (" + width + " x " + height + ")");
    }

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    @android.view.RemotableViewMethod
    public void setWidgetId(int widgetId) {
        mAppWidgetId = widgetId;
    }

    private float mDragYDist = 0f;

    private boolean isScrollAllowed() {
        return (mState == STATE.NO_NETWORK || mState == STATE.NORMAL);
    }

    private void sendScrollIntent(boolean direction) {
        LogUtil.v(TAG, "sendScrollIntent - isNextDirection = " + direction + ", widgetId = " + mAppWidgetId);
        Intent intent = new Intent(WeatherWidgetAction.ACTION_SCROLL);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        intent.putExtra(WeatherWidgetAction.DIRECTION, direction
                ? WeatherWidgetAction.DIRECTION_NEXT
                : WeatherWidgetAction.DIRECTION_PREVIOUS);
        getContext().sendBroadcast(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = super.onTouchEvent(event);
        boolean isNextDirection;

        if (!handled) {
            handled =  mGestureDetector.onTouchEvent(event);
        }

        if (!handled) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                LogUtil.v(TAG, "onTouchEvent - YDrag = " + mDragYDist);
                if (Math.abs(mDragYDist) > 0f) {
                    LogUtil.v(TAG, "onTouchEvent - YDragRatio = " + Math.abs(calcDragRatio()));
                    getChangeCityAnimation().stopDragging();

                    if ((Math.abs(calcDragRatio()) * 1000) > TO_GO_THRESHOLD) {
                        LogUtil.v(TAG, "onTouchEvent - YDragRatio > ");
                        if (mDragYDist > 0) {
                            isNextDirection = false;
                        } else {
                            isNextDirection = true;
                        }
                        sendScrollIntent(isNextDirection);
                        setState(STATE.SCROLLING);
                    } else {
                        LogUtil.v(TAG, "onTouchEvent - YDragRatio < ");
                        getChangeCityAnimation().reverse();
                    }
                }
                mDragDirection = DRAG_DIRECTION.IDLE;
                mDraggingDirection = DRAG_DIRECTION.IDLE;
                mDragState = DRAG_STATE.FINISH;
                LogUtil.v(TAG, "onUP - updateDragState = FINISH; Direction = IDLE");
                mDragYDist = 0;
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    enum DRAG_STATE {
        INITIAL, START, DRAGGING, FINISH;
    }

    enum DRAG_DIRECTION {
        IDLE, HOTZONE, UP, DOWN;
    }

    DRAG_STATE mDragState = DRAG_STATE.INITIAL;
    DRAG_DIRECTION mDragDirection = DRAG_DIRECTION.IDLE;    // to represent the current up or down animation
    DRAG_DIRECTION mDraggingDirection = DRAG_DIRECTION.IDLE; // to represent the current gesture is up or down

    private static final int ON_DRAG_THRESHOLD = 10;
    private static final float DRAG_STANDARD_DISTANCE = 900f;
    private static final float HOT_ZONE_THRESHOLD = 0.005f * DRAG_STANDARD_DISTANCE;
    private static final float TO_GO_THRESHOLD = 0.05f * DRAG_STANDARD_DISTANCE;

    private float calcDragRatio() {
        return mDragYDist/DRAG_STANDARD_DISTANCE;
    }

    private void updateDragState() {
        if (mDragState == DRAG_STATE.FINISH) {
            mDragState = DRAG_STATE.INITIAL;
            LogUtil.v(TAG, "updateDragState = INITIAL");
        } else if (mDragState == DRAG_STATE.INITIAL) {
            mDragState = DRAG_STATE.START;
            LogUtil.v(TAG, "updateDragState = START");
        } else {
            mDragState = DRAG_STATE.DRAGGING;
            LogUtil.v(TAG, "updateDragState = DRAGGING");
        }
    }

    private void updateAnimation() {
        if (mDragState == DRAG_STATE.START) {
            getChangeCityAnimation().startDragging();
            mDragState = DRAG_STATE.DRAGGING;
            LogUtil.v(TAG, "updateAnimation - startDragging");
        } else if (mDragState == DRAG_STATE.DRAGGING) {
            float ratio = Math.abs(calcDragRatio());
            if (isAnimationAndYDistNotConsistent()) {
                getChangeCityAnimation().setProgress(0.f);
                mDragDirection = DRAG_DIRECTION.HOTZONE;
                mDragState = DRAG_STATE.FINISH;
                LogUtil.v(TAG, "updateAnimation - discrete sampling point");
            } else if (isAnimationAndDragNotConsistent() && ratio * DRAG_STANDARD_DISTANCE <= HOT_ZONE_THRESHOLD) {
                getChangeCityAnimation().setProgress(0.f);
                mDragDirection = DRAG_DIRECTION.HOTZONE;
                mDragState = DRAG_STATE.FINISH;
                LogUtil.v(TAG, "updateAnimation - less than threshold");
            } else {
                getChangeCityAnimation().setProgress(ratio);
                LogUtil.v(TAG, "updateAnimation - setProgress = " + ratio);
            }
        }
    }

    private PropertyAnimation getChangeCityAnimation() {
        if (mDragDirection == DRAG_DIRECTION.DOWN) {
            LogUtil.v(TAG, "getChangeCityAnimation - down");
            return mChangeNextCityAnimation;
        } else {
            LogUtil.v(TAG, "getChangeCityAnimation - up");
            return mChangePreviousCityAnimation;
        }
    }

    private class WeatherGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean mDisallowIsRequested;

        @Override
        public boolean onDown(MotionEvent e) {
            // return true, otherwise we cannot get any other following events.
            mDisallowIsRequested = false;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                float distanceY) {
            if (!mDisallowIsRequested) {
                getParent().requestDisallowInterceptTouchEvent(true);
                mDisallowIsRequested = true;
            }

            if (!isScrollAllowed()) {
                return true;
            }

            mDragYDist += distanceY;
            if (distanceY >= 0) {
                mDraggingDirection = DRAG_DIRECTION.UP;
            } else {
                mDraggingDirection = DRAG_DIRECTION.DOWN;
            }

            LogUtil.v(TAG, "onScroll - DragY = " + mDragYDist + ", distanceY = " + distanceY + ", Direction = " + mDragDirection);
            if (mDragDirection == DRAG_DIRECTION.IDLE) {
                if (Math.abs(mDragYDist) > ON_DRAG_THRESHOLD) {
                    if (mDragYDist > 0) {
                        mDragDirection = DRAG_DIRECTION.UP;
                    } else {
                        mDragDirection = DRAG_DIRECTION.DOWN;
                    }
                    updateDragState();
                    updateAnimation();
                }
            } else if (mDragDirection == DRAG_DIRECTION.HOTZONE) {
                if (Math.abs(mDragYDist) > HOT_ZONE_THRESHOLD) {
                    LogUtil.v(TAG, "onScroll - DragY = " + mDragYDist + ", distanceY = " + distanceY + ", Direction = " + mDragDirection);
                    if (mDragYDist > 0) {
                        mDragDirection = DRAG_DIRECTION.UP;
                    } else {
                        mDragDirection = DRAG_DIRECTION.DOWN;
                    }
                    updateDragState();
                    updateAnimation();
                }
            } else {
                updateDragState();
                updateAnimation();
            }
            return true;
        }
    }

    private boolean isAnimationAndDragNotConsistent() {
        // Example: of return ! case.
        // swipe up first, then swipe down, the YDist is still positive, but the delta distance is negative, means this time,
        return !((mDragDirection == DRAG_DIRECTION.DOWN && mDraggingDirection == DRAG_DIRECTION.DOWN) ||
                (mDragDirection == DRAG_DIRECTION.UP && mDraggingDirection == DRAG_DIRECTION.UP));
    }

    private boolean isAnimationAndYDistNotConsistent() {
        // Example: of return ! case,
        // mDragDirection = UP, and mDragYDist < 0, it is because the position sampling is discrete,
        // when you swipe up, then down, you may think the mDragYDist will be from a +number and decrease to 0,
        // then to -number, and when decrease to 0, it will be handled by hot zone case.
        // but this will not always happen. the position sampling is discrete, so you may have a case to from a +number,
        // directly jump to -number, no procedure of decreasing to 0.
        return !((mDragDirection == DRAG_DIRECTION.DOWN && mDragYDist <= 0) ||
                (mDragDirection == DRAG_DIRECTION.UP && mDragYDist >= 0));
    }

    public void reapplyFog()
    {
         List<Actor> list = mScenarioContainer.getChildren();
         for (Actor actor : list) {
              Object3D obj = (Object3D)actor;
              if( obj != null) obj.setMaterialType("fog_cloud_01", EMT_FOG);
         }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);    //To change body of overridden methods use File | Settings | File Templates.
        if (visibility == VISIBLE) {
            reapplyFog();
            int currentWeatherModelType = WeatherType.convertToModelType(mCurrentWeather);
            if (WeatherType.isSnowModelType(currentWeatherModelType)) {
              changeTreeSnowTexture();
              changeWaterSnowTexture();
            } else if (WeatherType.isSandModelType(currentWeatherModelType)) {
                startSandy();
            }
        }
    }

}
