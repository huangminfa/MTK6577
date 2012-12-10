package com.mediatek.media3d;

import android.util.Pair;
import java.util.HashMap;

public interface VideoBackground {

    /**
     * setup seek table.
     *
     * @param seekTable : segmentId, start time in ms, end time in ms.
     */
    void setupSeekTable(HashMap<Integer,Pair<Integer, Integer>> seekTable);

    /**
     * play the specific segment.
     *
     * @param segmentId :
     */
    void play(int segmentId);

    /**
     * Starts or resumes playback.
     *
     * @param startMsec start time in milliseconds
     * @param endMsec end time in milliseconds
     */
    void play(int startMsec, int endMsec);

    /**
     * Pauses playback.
     */
    void pause();

    /**
     * Stops playback.
     */
    void stop();
}
