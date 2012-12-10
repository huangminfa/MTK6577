package com.android.calendar;

import android.util.Log;

import com.mediatek.xlog.Xlog;

/**
 * This utility class is the main entrance to print log with Xlog/Log class.
 * Our application should always use this class to print logs.
 */
public final class LogUtil {

    private static final boolean XLOG_ENABLED = true;
    private static final String MTK_LOG_TAG = "Calendar";

    private LogUtil() {
    }

    public static void v(String tag, String msg) {
        if (XLOG_ENABLED) {
            Xlog.v(MTK_LOG_TAG, "<<" + tag + ">>: " + msg);
        } else {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable t) {
        if (XLOG_ENABLED) {
            Xlog.v(MTK_LOG_TAG, "<<" + tag + ">>: " + msg, t);
        } else {
            Log.v(tag, msg, t);
        }
    }

    public static void d(String tag, String msg) {
        if (XLOG_ENABLED) {
            Xlog.d(MTK_LOG_TAG, "<<" + tag + ">>: " + msg);
        } else {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable t) {
        if (XLOG_ENABLED) {
            Xlog.d(MTK_LOG_TAG, "<<" + tag + ">>: " + msg, t);
        } else {
            Log.d(tag, msg, t);
        }
    }

    public static void i(String tag, String msg) {
        if (XLOG_ENABLED) {
            Xlog.i(MTK_LOG_TAG, "<<" + tag + ">>: " + msg);
        } else {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable t) {
        if (XLOG_ENABLED) {
            Xlog.i(MTK_LOG_TAG, "<<" + tag + ">>: " + msg, t);
        } else {
            Log.i(tag, msg, t);
        }
    }

    public static void w(String tag, String msg) {
        if (XLOG_ENABLED) {
            Xlog.w(MTK_LOG_TAG, "<<" + tag + ">>: " + msg);
        } else {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable t) {
        if (XLOG_ENABLED) {
            Xlog.w(MTK_LOG_TAG, "<<" + tag + ">>: " + msg, t);
        } else {
            Log.w(tag, msg, t);
        }
    }

    public static void e(String tag, String msg) {
        if (XLOG_ENABLED) {
            Xlog.e(MTK_LOG_TAG, "<<" + tag + ">>: " + msg);
        } else {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable t) {
        if (XLOG_ENABLED) {
            Xlog.e(MTK_LOG_TAG, "<<" + tag + ">>: " + msg, t);
        } else {
            Log.e(tag, msg, t);
        }
    }

    /**
     * print log for performance test. this log records the starting time.
     * @param description the description of this check point.
     */
    public static void performanceStart(String description) {
        String msg = makePerformanceLogText("start", description);
        if (XLOG_ENABLED) {
            Xlog.i(MTK_LOG_TAG, msg);
        } else {
            Log.i(MTK_LOG_TAG, msg);
        }
    }

    /**
     * print log for performance test. this log records the ending time of procedure.
     * @param description the description of this check point.
     */
    public static void performanceEnd(String description) {
        String msg = makePerformanceLogText("end", description);
        if (XLOG_ENABLED) {
            Xlog.i(MTK_LOG_TAG, msg);
        } else {
            Log.i(MTK_LOG_TAG, msg);
        }
    }

    /**
     * the format of performance test about calendar app part.
     */
    private static final String PERFORMANCE_FORMAT_STRING = "[Performance test][Calendar][app] %s %s[%d]";

    /**
     * A tool to make the text following the {@link PERFORMANCE_FORMAT_STRING} format.
     * @param checkPoint can be "start" or "end" TODO: maybe an Enum is better.
     * @param description the description of the check point
     * @return text
     */
    private static String makePerformanceLogText(String checkPoint,
            String description) {
        String desc;
        if (description == null) {
            desc = "";
        } else {
            desc = description;
        }
        String msg = String.format(PERFORMANCE_FORMAT_STRING, 
                new Object[]{desc, checkPoint, System.currentTimeMillis()});
        return msg;
    }
}
