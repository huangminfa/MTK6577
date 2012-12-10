package com.mediatek.ngin3d;

/**
 * Interface used by rendering thread to perform callback in UI thread.
 */
public interface UiHandler {
    /**
     * To run specified runnable in UI thread
     *
     * @param runnable to run
     */
    void post(Runnable runnable);
}
