package com.mediatek.videoplayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

public class CachedThumbnail {
    private static final String TAG = "CachedThumbnail";
    private static final boolean LOG = false;//for performance
    
    private final HashMap<Long, MyDrawable> sCachedPreview = new HashMap<Long, MyDrawable>();
    private Context mContext;
    private ContentResolver mCr;
    
    private final int mDefaultIconWidth;
    private final int mDefaultIconHeight;
    private final Bitmap mDefaultDrawable;
    private final ArrayList<DrawableStateListener> mListeners = new ArrayList<DrawableStateListener>();
    
    //asyc request media for fast the calling thread
    private Handler mTaskHandler;
    private static Looper sLooper;
    //priority queue for async request
    private static final PriorityQueue<TaskParams> mTaskQueue =
        new PriorityQueue<TaskParams>(10, TaskParams.getComparator());
    private static final int TASK_REQUEST_DONE = 1;
    private static final int TASK_REQUEST_NEW = 2;
    private static final int TASK_GROUP_ID = 1999;//just a number
    
    private Bitmap mDefaultDrawable3D;
    private final Bitmap mDefaultOverlay3D;
    
    private CachedThumbnail(Context context, Bitmap defaultDrawable, Bitmap icon) {
        mContext = context;
        mCr = mContext.getContentResolver();
        
        mDefaultDrawable = defaultDrawable;
        mDefaultIconWidth = defaultDrawable.getWidth();
        mDefaultIconHeight = defaultDrawable.getHeight();
        
        mDefaultOverlay3D = icon;
        if (LOG) MtkLog.v(TAG, "CachedPreview() mDefaultIconWidth=" + mDefaultIconWidth + ", mDefaultIconHeight=" + mDefaultIconHeight);
    }
    
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (LOG) MtkLog.v(TAG, "mCallingHandler.handleMessage(" + msg + ")"); 
            if (msg.what == TASK_REQUEST_DONE && (msg.obj instanceof TaskParams)) {
                TaskParams task = (TaskParams)msg.obj;
                MyDrawable drawable = task.drawable;
                for(DrawableStateListener listener : mListeners) {
                    listener.onChanged(task.rowId, drawable.type, drawable.drawable);
                }
            }
        }
    };
    
    private boolean mInitedTask;
    private long prioritySeed;
    private TaskParams currentRequest;
    private void initTask() {
        if (mInitedTask) return;
        if (LOG) MtkLog.v(TAG, "initTask() prioritySeed=" + prioritySeed);
        prioritySeed = 0;
        synchronized(CachedThumbnail.class) {
            if (sLooper == null) {
                HandlerThread t = new HandlerThread("cached-thumbnail-thread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
                t.start();
                sLooper = t.getLooper();
            }
        }
        mTaskHandler = new Handler(sLooper) {
            @Override
            public void handleMessage(Message msg) {
                if (LOG) MtkLog.v(TAG, "mTaskHandler.handleMessage(" + msg + ") this=" + this); 
                if (msg.what == TASK_REQUEST_NEW) {
                    synchronized(mTaskQueue) {
                        currentRequest = mTaskQueue.poll();
                    }
                    if (currentRequest == null) {
                        MtkLog.w(TAG, "wrong request, has request but no task params.");
                        return;
                    }
                    TaskParams task = currentRequest;//currentRequest may be cleared by other thread.
                    //recheck the drawable is exists or not.
                    long _id = task.rowId;
                    MyDrawable cachedDrawable = null;
                    synchronized(sCachedPreview) {
                        cachedDrawable = sCachedPreview.get(_id);
                    }
                    if (cachedDrawable == null) {
                        MtkLog.w(TAG, "cached drawable was delete. may for clear.");
                        return;
                    }
                    //load or reload the preview
                    if (cachedDrawable.type == TYPE_NEED_LOAD) {
                        Bitmap tempBitmap = getThumbnail(_id);
                        if (tempBitmap != null) {
                            tempBitmap = Bitmap.createScaledBitmap(tempBitmap, mDefaultIconWidth, mDefaultIconHeight, true);
                            if (cachedDrawable.support3D) {
                                tempBitmap = overlay3DImpl(tempBitmap);
                            }
                            cachedDrawable.set(tempBitmap, TYPE_LOADED_HAS_PREVIEW);
                        } else {
                            cachedDrawable.set(null, TYPE_LOADED_NO_PREVIEW);
                        }
                    }
                    if (task != currentRequest) {
                        MtkLog.w(TAG, "current request was changed by other thread. task=" + task + ", currentRequest=" + currentRequest);
                        return;
                    }
                    task.drawable = cachedDrawable;
                    Message done = mUiHandler.obtainMessage(TASK_REQUEST_DONE);
                    done.obj = task;
                    done.sendToTarget();
                    if (LOG) MtkLog.v(TAG, "mTaskHandler.handleMessage() send done. " + currentRequest + " this=" + this);
                    currentRequest = null;
                }
            }
        };
        mInitedTask = true;
    }
    
    private Bitmap getDefaultBitmap(boolean support3D) {
        if (MtkUtils.isSupport3d() && support3D) {
            if (mDefaultDrawable3D == null) {
                mDefaultDrawable3D = mDefaultDrawable.copy(Bitmap.Config.ARGB_8888, true);
                mDefaultDrawable3D = overlay3DImpl(mDefaultDrawable3D);
            }
            return mDefaultDrawable3D;
        } else {
            return mDefaultDrawable;
        }
    }
    
    private Bitmap overlay3DImpl(Bitmap bitmap) {
        Canvas overlayCanvas = new Canvas(bitmap);
        int overlayWidth = mDefaultOverlay3D.getWidth();
        int overlayHeight = mDefaultOverlay3D.getHeight();
        int left = 0;//bitmap.getWidth() - overlayWidth;
        int top = bitmap.getHeight() - overlayHeight;
        Rect newBounds = new Rect(left, top, left + overlayWidth, top + overlayHeight);
        overlayCanvas.drawBitmap(mDefaultOverlay3D, null, newBounds, null);
        return bitmap;
    }
    
    private void clearTask() {
        if (LOG) MtkLog.v(TAG, "clearTask() initTask=" + mInitedTask);
        if (mInitedTask) {
            prioritySeed = 0;
            mUiHandler.removeMessages(TASK_REQUEST_DONE);
            synchronized(mTaskQueue) {
                mTaskQueue.clear();
            }
            mTaskHandler.removeMessages(TASK_REQUEST_NEW);
            mTaskHandler = null;
            mInitedTask = false;
            cancelThumbnail();
        }
        if (sLooper != null) {
            sLooper.quit();
            sLooper = null;
        }
    }
    
    //will be loaded if sdcard is in phone
    public Bitmap getCachedPreview(long _id, long dateModified, boolean support3D, boolean request) {
        if (LOG) MtkLog.v(TAG, "getCachedPreview(" + _id + ", " + dateModified + ", " + request + ")");
        initTask();
        MyDrawable cachedDrawable = null;
        synchronized(sCachedPreview) {
            cachedDrawable = sCachedPreview.get(_id);
        }
        if (request) {
            if (cachedDrawable != null && cachedDrawable.dateModified != dateModified) {
                //video was updated, reload its thumbnail
                cachedDrawable.type = TYPE_NEED_LOAD;
            }
            if (cachedDrawable == null || cachedDrawable.type == TYPE_NEED_LOAD) {
                prioritySeed++;
                synchronized(mTaskQueue) {
                    //check is processing or not
                    //current request is not in queue.
                    boolean isProcessing = false;
                    if (currentRequest != null) {
                        synchronized(currentRequest) {
                            if (currentRequest.rowId == _id) {
                                if (currentRequest.drawable.dateModified == dateModified) {
                                    isProcessing = true;
                                } else {//need to reload it
                                    isProcessing = false;
                                }
                            }
                        }
                    }
                    if (LOG) MtkLog.v(TAG, "getCachedPreview() isProcessing=" + isProcessing);
                    if (!isProcessing) {
                        //check it is in request queue or not.
                        TaskParams oldRequest = null;
                        for(TaskParams one : mTaskQueue) {
                            if (one.rowId == _id) {
                                oldRequest = one;
                                break;
                            }
                        }
                        if (LOG) MtkLog.v(TAG, "getCachedPreview() oldRequest=" + oldRequest);
                        if (oldRequest == null) {//not in cache and not in request
                            synchronized(sCachedPreview) {//double check it
                                cachedDrawable = sCachedPreview.get(_id);
                                if (cachedDrawable == null) {
                                    MyDrawable temp = new MyDrawable(getDefaultBitmap(support3D), TYPE_NEED_LOAD, dateModified, support3D);
                                    sCachedPreview.put(_id, temp);
                                    cachedDrawable = temp;
                                } else if (cachedDrawable.dateModified != dateModified) {
                                    //reload it too
                                    cachedDrawable.dateModified = dateModified;
                                    cachedDrawable.type = TYPE_NEED_LOAD;
                                }
                            }
                            //check whether to refresh it or not
                            if (cachedDrawable.type != TYPE_LOADED_HAS_PREVIEW) {
                                TaskParams task = new TaskParams(_id, -prioritySeed, cachedDrawable);
                                mTaskQueue.add(task);
                                mTaskHandler.sendEmptyMessage(TASK_REQUEST_NEW);
                                if (LOG) MtkLog.v(TAG, "getCachedPreview() mTaskQueue.size()=" + mTaskQueue.size());
                            }
                        } else {//not in cache, but in request queue
                            //just update priority and dateModified
                            oldRequest.priority = -prioritySeed;
                            oldRequest.drawable.dateModified = dateModified;
                            if (mTaskQueue.remove(oldRequest)) {
                                mTaskQueue.add(oldRequest);//re-order the queue
                            }
                        }
                    } else {
                        //do nothing
                    }
                }
                if (LOG) MtkLog.v(TAG, "getCachedPreview() async load the drawable for " + _id);
            }
        } else {
            //do not request any thing
        }
        Bitmap result = null;
        if (cachedDrawable == null || cachedDrawable.drawable == null) {
            result = getDefaultBitmap(support3D);
        } else {
            result = cachedDrawable.drawable;
        }
        if (LOG) MtkLog.v(TAG, "getCachedPreview() cachedDrawable=" + cachedDrawable + ", return " + result);
        return result;
    }
    
    private Bitmap getThumbnail(final long _id) {
//        mUiHandler.postDelayed(new Runnable() {
//            
//            @Override
//            public void run() {
//                MediaStore.Video.Thumbnails.cancelThumbnailRequest(mCr, _id, TASK_GROUP_ID);
//            }
//        }, 5000);
        Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(mCr,
                _id,
                TASK_GROUP_ID,
                MediaStore.Video.Thumbnails.MICRO_KIND,
                null);
        if (LOG) MtkLog.v(TAG, "getPreview() bitmap=" + bitmap);
        return bitmap;
    }
    
    private void cancelThumbnail() {
        MediaStore.Video.Thumbnails.cancelThumbnailRequest(mCr, -1, TASK_GROUP_ID);
    }
    
    public void clearCachedPreview() {
        if (LOG) MtkLog.v(TAG, "clearCachedPreview()");
        clearTask();
        mListeners.clear();
        synchronized(sCachedPreview) {
            Set<Long> keys = sCachedPreview.keySet();
            for(Long key : keys) {
                MyDrawable drawable = sCachedPreview.get(key);
                if (drawable != null && drawable.drawable != null && drawable.drawable != mDefaultDrawable
                        && drawable.drawable != mDefaultDrawable3D) {
                    drawable.drawable.recycle();
                }
            }
            sCachedPreview.clear();
        }
        if (LOG) MtkLog.v(TAG, "clearCachedPreview() finished");
    }
    
    public boolean addListener(DrawableStateListener listener) {
        return mListeners.add(listener);
    }
    
    public boolean removeListener(DrawableStateListener listener) {
        return mListeners.remove(listener);
    }
    
    private static CachedThumbnail mCachedManager;
    public static CachedThumbnail getCachedManager(Context context, Bitmap defaultBitmap, Bitmap icon3d) {
        if (mCachedManager == null) {
            mCachedManager = new CachedThumbnail(context, defaultBitmap, icon3d);
        }
        return mCachedManager;
    }
    
    public static void releaseCachedManager() {
        if (mCachedManager != null) {
            mCachedManager.clearCachedPreview();
        }
        mCachedManager = null;
    }
    
    public static final int TYPE_NEED_LOAD = 0;
    public static final int TYPE_LOADED_NO_PREVIEW = 1;
    public static final int TYPE_LOADED_HAS_PREVIEW = 2;

    public class MyDrawable {
        long dateModified;
        int type;
        Bitmap drawable;
        boolean support3D;
        
        public MyDrawable(Bitmap idrawable, int itype, long idateModified, boolean isupport3D) {
            type = itype;
            drawable = idrawable;
            dateModified = idateModified;
            support3D = isupport3D;
        }
        
        public void set(Bitmap idrawable, int itype) {
            type = itype;
            drawable = idrawable;
        }

        @Override
        public String toString() {
            return new StringBuilder()
            .append("MyDrawable(type=")
            .append(type)
            .append(", drawable=")
            .append(drawable)
            .append(")")
            .toString();
        }
        
    }
    
    public interface DrawableStateListener {
        //will be called if requesgted drawable state is changed.
        void onChanged(long rowId, int type, Bitmap drawable);
    }
    
    public static class TaskParams {
        long rowId;//thumbnail _id
        long priority;
        MyDrawable drawable;//final drawable
        
        public TaskParams(long rowId, long priority, MyDrawable drawable) {
            this.rowId = rowId;
            this.priority = priority;
            this.drawable = drawable;
        }

        @Override
        public String toString() {
            return new StringBuilder()
            .append("TaskInput(rowId=")
            .append(rowId)
            .append(", drawable=")
            .append(drawable)
            .append(")")
            .toString();
        }
        
        static Comparator<TaskParams> getComparator() {
            return new Comparator<TaskParams>() {

                public int compare(TaskParams r1, TaskParams r2) {
                    if (r1.priority != r2.priority) {
                        return (r1.priority < r2.priority) ? -1 : 1;
                    }
                    return 0;
                }
                
            };
        }
    }
}
