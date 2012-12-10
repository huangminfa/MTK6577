package com.mediatek.media3d;

import android.graphics.Bitmap;
import android.util.Pair;
import com.mediatek.ngin3d.Actor;

import java.util.HashMap;

/**
 * Represents the host that will contain a list of pages.
 */
public interface PageHost {
    /**
     * Callback for page when left.
     */
    void onPageLeft(Page page);

    /**
     * Callback for page when entered.
     */
    void onPageEntered(Page page);

    /*
     *  VideoBackground interface implementation
     */
    VideoBackground setVideoBackground(int resId, HashMap<Integer, Pair<Integer,Integer>> seekTable);

    String PORTAL = "portal";
    String WEATHER = "weather";
    String PHOTO = "photo";
    String VIDEO = "video";
        
    void enterPage(String pageName);
    Actor getThumbnailActor(String pageName);
    public Page getOldPage();
    public Page getPage(String PageName);
    int isPageEqual(Page page, String pageName);
}
