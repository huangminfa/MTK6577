package com.mediatek.ngin3d.debugtools.android.serveragent;

public interface IDebugee {

    /**
     * get a Actor dump information (JSON format) by tag
     *
     * @param tag the Actor's tag
     * @return String dump information. otherwise return null.
     */
    String dumpActorByTag(String tag);

    /**
     * get a Actor dump information (JSON format) by id
     *
     * @param id the Actor's ID
     * @return String dump information. otherwise return null.
     */
    String dumpActorByID(int id);

    /**
     * get a Stage dump information (JSON format)
     *
     * @return String dump information. otherwise return null.
     */
    String dumpStage();

    /**
     * update a property of Actor by tag
     *
     * @param tag      the Actor's tag
     * @param paramObj a class that contain parameters.
     * @return true if update succeed, otherwise return false.
     */
    boolean setParameterByTag(String tag, ParamObject paramObj);

    /**
     * update a property of Actor by ID
     *
     * @param id       the Actor's ID
     * @param paramObj a class that contain parameters.
     * @return true if update succeed, otherwise return false.
     */
    boolean setParameterByID(int id, ParamObject paramObj);

    /**
     * pause the ngin3d render
     */
    void pauseRender();

    /**
     * resume the ngin3d render
     */
    void resumeRender();

    /**
     * tick the ngin3d render by specific time
     *
     * @param time the specific time which wants to tick
     */
    void tickRender(int time);

    /**
     * get the frame interval
     *
     * @return int frame interval, otherwise return -1
     */
    double getFPS();

    /**
     * get the device memory info including uss, pss, rss
     *
     * @return the format is "uss pss rss", otherwise return null
     */
    String getMemoryInfo();

    /**
     * get the device total cpu info
     *
     * @return int cpu usage, otherwise return -1
     */
    int getCpuUsage();

    void removeActorByID(int id);

    String animationDumpToJSONByID(int id);

    int getFrameInterval();

}
