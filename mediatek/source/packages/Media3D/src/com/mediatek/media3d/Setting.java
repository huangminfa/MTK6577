package com.mediatek.media3d;

import android.content.Context;
import android.content.res.XmlResourceParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

public class Setting {
    private final static String TAG = "Setting";

    public final static String PHOTO_NAME = "photo";
    public final static String VIDEO_NAME = "video";
    public final static String WEATHER_NAME = "weather";

    public final static String DRAGGING = "dragging";
    public final static String BACKGROUND = "background";
    public final static String TEMPORARY = "temporary";
    public final static String TITLE = "title";
    public final static String TITLE_WO_UPDATE = "titleWoUpdated";
    public final static String THUMBNAIL = "thumbnail";
    public final static String PORTAL_THUMBNAIL = "portalThumbnail";
    public final static String PACKAGE = "package";

    public final static String MAX = "max";
    public final static String THRESHOLD = "threshold";
    public final static String WIDTH = "width";
    public final static String HEIGHT = "height";
    public final static String SIZE = "size";
    public final static String NAME = "name";

    public final static String PHOTO_BACKGROUND_WIDTH = PHOTO_NAME + "." + BACKGROUND + "." + WIDTH;
    public final static String PHOTO_BACKGROUND_HEIGHT = PHOTO_NAME + "." + BACKGROUND + "." + HEIGHT;
    public final static String PHOTO_THUMBNAIL_WIDTH = PHOTO_NAME + "." + THUMBNAIL + "." + WIDTH;
    public final static String PHOTO_THUMBNAIL_HEIGHT = PHOTO_NAME + "." + THUMBNAIL + "." + HEIGHT;
    public final static String PHOTO_TEMP_BACKGROUND_WIDTH = PHOTO_NAME + "." + TEMPORARY + "." + WIDTH;
    public final static String PHOTO_TEMP_BACKGROUND_HEIGHT = PHOTO_NAME + "." + TEMPORARY + "." + HEIGHT;
    public final static String PHOTO_PORTAL_THUMBNAIL_WIDTH = PHOTO_NAME + "." + PORTAL_THUMBNAIL + "." + WIDTH;
    public final static String PHOTO_PORTAL_THUMBNAIL_HEIGHT = PHOTO_NAME + "." + PORTAL_THUMBNAIL + "." + HEIGHT;
    public final static String PHOTO_DRAGGING_MAX = PHOTO_NAME + "." + DRAGGING + "." + MAX;
    public final static String PHOTO_DRAGGING_THRESHOLD = PHOTO_NAME + "." + DRAGGING + "." + THRESHOLD;
    public final static String PHOTO_GALLERY_PACKAGE = PHOTO_NAME + "." + PACKAGE + "." + NAME;

    public final static String VIDEO_THUMBNAIL_WIDTH = VIDEO_NAME + "." + THUMBNAIL + "." + WIDTH;
    public final static String VIDEO_THUMBNAIL_HEIGHT = VIDEO_NAME + "." + THUMBNAIL + "." + HEIGHT;
    public final static String VIDEO_PORTAL_THUMBNAIL_WIDTH = VIDEO_NAME + "." + PORTAL_THUMBNAIL + "." + WIDTH;
    public final static String VIDEO_PORTAL_THUMBNAIL_HEIGHT = VIDEO_NAME + "." + PORTAL_THUMBNAIL + "." + HEIGHT;
    public final static String VIDEO_DRAGGING_MAX = VIDEO_NAME + "." + DRAGGING + "." + MAX;
    public final static String VIDEO_DRAGGING_THRESHOLD = VIDEO_NAME + "." + DRAGGING + "." + THRESHOLD;

    public final static String WEATHER_DRAGGING_MAX = WEATHER_NAME + "." + DRAGGING + "." + MAX;
    public final static String WEATHER_DRAGGING_THRESHOLD = WEATHER_NAME + "." + DRAGGING + "." + THRESHOLD;
    public final static String WEATHER_TITLE_SIZE = WEATHER_NAME + "." + TITLE + "." + SIZE;
    public final static String WEATHER_TITLE_WOUPDATE_SIZE = WEATHER_NAME + "." + TITLE_WO_UPDATE + "." + SIZE;

    public static Setting realize(Context context, int resId) {
        XmlResourceParser parser = context.getResources().getXml(resId);
        Setting setting = new Setting();
        int xmlEventType;
        try {
            while ((xmlEventType = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                 switch (xmlEventType) {
                     case XmlPullParser.START_DOCUMENT:
                         LogUtil.v(TAG, "Start Document.");
                         break;
                     case XmlPullParser.START_TAG:
                         setting.processStartElement(parser);
                         break;
                     case XmlPullParser.END_TAG:
                         setting.processEndElement(parser);
                         break;
                     case XmlPullParser.TEXT:
                         break;
                     default:
                         break;
                 }
            }
        } catch (XmlPullParserException e) {
            LogUtil.v(TAG, "Exception :" + e);
        } catch (IOException e) {
            LogUtil.v(TAG, "Exception :" + e);
        }

        return setting;
    }

    private final HashMap<String, Object> mMap = new HashMap<String, Object>();
    private final Stack<String> mPrefix = new Stack<String>();

    public String getString(String key) {
        return (String)mMap.get(key);
    }

    public int getInt(String key) {
        return (Integer)mMap.get(key);
    }

    public float getFloat(String key) {
        return (Float)mMap.get(key);
    }

    private void processStartElement(XmlPullParser parser) {
        String tag = parser.getName();
        if (tag.equalsIgnoreCase("page")) {
            processPageElement(parser);
        } else if (tag.equalsIgnoreCase("image")) {
            processImageElement(parser);
        } else if (tag.equalsIgnoreCase("dragging")) {
            processDraggingElement(parser);
        } else if (tag.equalsIgnoreCase("font")) {
            processFontElement(parser);
        } else if (tag.equalsIgnoreCase("application")) {
        } else {
            LogUtil.v(TAG, "Start Element : unknown");
        }
    }

    private void processPageElement(XmlPullParser parser) {
        String key = parser.getAttributeValue(null, "id");
        mPrefix.push(key);
        LogUtil.v(TAG, "prefix :" + mPrefix.peek());
    }

    private void processImageElement(XmlPullParser parser) {
        String prefix = mPrefix.peek();
        String key = prefix.concat("." + parser.getAttributeValue(null, "id"));
        put(key.concat(".width"), Integer.valueOf(parser.getAttributeValue(null, "width")));
        put(key.concat(".height"), Integer.valueOf(parser.getAttributeValue(null, "height")));
        mPrefix.push(key);
        LogUtil.v(TAG, "prefix :" + mPrefix.peek());
    }

    private void processDraggingElement(XmlPullParser parser) {
        String prefix = mPrefix.peek();
        LogUtil.v(TAG, "prefix :" + prefix);
        String key = prefix.concat("." + parser.getAttributeValue(null, "id"));
        LogUtil.v(TAG, "key1 :" + key);
        put(key.concat(".max"), Float.valueOf(parser.getAttributeValue(null, "max")));
        LogUtil.v(TAG, "key2 max :" + Float.valueOf(parser.getAttributeValue(null, "max")));
        put(key.concat(".threshold"), Float.valueOf(parser.getAttributeValue(null, "threshold")));
        LogUtil.v(TAG, "key3 threshold :" + Float.valueOf(parser.getAttributeValue(null, "threshold")));
        mPrefix.push(key);
        LogUtil.v(TAG, "prefix :" + mPrefix.peek());
    }

    private void processFontElement(XmlPullParser parser) {
        String prefix = mPrefix.peek();
        String key = prefix.concat("." + parser.getAttributeValue(null, "id"));
        put(key.concat(".size"), Integer.valueOf(parser.getAttributeValue(null, "size")));
        mPrefix.push(key);
        LogUtil.v(TAG, "prefix :" + mPrefix.peek());
    }

    public void processEndElement(XmlPullParser parser){
        String name = parser.getName();
        String uri = parser.getNamespace();
        if ("".equals (uri)) {
            LogUtil.v(TAG, "End element : " + name);
        } else {
            LogUtil.v(TAG, "End element:   {" + uri + "}" + name);
        }
        if (!mPrefix.empty()) {
            mPrefix.pop();
        }
    }

    private void put(String key, Object value) {
        mMap.put(key, value);
    }
}