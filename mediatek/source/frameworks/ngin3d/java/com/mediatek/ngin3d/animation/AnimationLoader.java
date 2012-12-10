package com.mediatek.ngin3d.animation;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Utils;
import com.mediatek.ngin3d.utils.Ngin3dException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A helper class to load animations from resource.
 */
public final class AnimationLoader {

    private static final String TAG = "AnimationLoader";
    private static File sCacheDir;

    private AnimationLoader() {
        // Do nothing
    }

    /**
     * Load animation from specified resource ID.
     *
     * @param ctx context
     * @param scriptResId resource id of animation script
     * @param saveCache whether to cache animation in local storage
     * @param cacheFileName the cache file name to use
     * @return the loaded animation
     */
    public static BasicAnimation loadAnimation(Context ctx, int scriptResId, boolean saveCache, String cacheFileName) {
        if (scriptResId == 0)
            return null;

        KeyframeAnimation animToMark = null;
        KeyframeDataSet dataSet = getKeyframeDataSet(ctx, scriptResId, saveCache, cacheFileName);

        AnimationGroup animationGroup = new AnimationGroup();
        for (KeyframeData kfData : dataSet.getList()) {
            KeyframeAnimation anim = new KeyframeAnimation(kfData);

            if (animToMark == null) {
                animToMark = anim;
            } else {
                if (anim.getDuration() > animToMark.getDuration())
                    animToMark = anim;
            }

            animationGroup.add(anim);
        }

        animationGroup.setName(ctx.getResources().getResourceName(scriptResId));
        animationGroup.setProposedWidth(dataSet.getTargetWidth());
        animationGroup.setProposedHeight(dataSet.getTargetHeight());
        dataSet.applyMarker(animToMark);

        return animationGroup;
    }

    public static BasicAnimation loadAnimation(Context ctx, int scriptResId) {
        return loadAnimation(ctx, scriptResId, true, null);
    }

    public static BasicAnimation loadAnimation(Context ctx, int scriptResId, boolean saveCache) {
        return loadAnimation(ctx, scriptResId, saveCache, null);
    }

    public static BasicAnimation loadAnimation(Context ctx, int scriptResId, String cacheFileName) {
        return loadAnimation(ctx, scriptResId, true, cacheFileName);
    }

    private static final int READ_BUFF_SIZE = 8192;

    private static <T> T loadCachedObject(File filePath) {
        if (filePath == null) {
            return null;
        }
        if (Ngin3d.DEBUG) Log.d(TAG, "loadCachedObject: " + filePath);

        Object cachedObj = null;

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(filePath);
            bis = new BufferedInputStream(fis, READ_BUFF_SIZE);
            ois = new ObjectInputStream(bis);
            cachedObj = ois.readObject();
        } catch (IOException e) {
            Log.e(TAG, "Load cache failed: " + filePath + ", " + e);
        } catch (ClassNotFoundException cE) {
            Log.e(TAG, "Class not found in: " + filePath + " !");
        } finally {
            Utils.closeQuietly(ois);
            Utils.closeQuietly(bis);
            Utils.closeQuietly(fis);
        }

        return (T) cachedObj;
    }

    private static boolean saveObjectToCache(Object obj, File filePath) {
        // TODO: thread version.

        if (obj == null) {
            return false;
        }

        boolean succeeded = true;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(filePath);
            bos = new BufferedOutputStream(fos, READ_BUFF_SIZE);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
        } catch (IOException e) {
            Log.e(TAG, "Save cache failed: " + filePath + ", " + e);
            succeeded = false;
        } finally {
            Utils.closeQuietly(oos);
            Utils.closeQuietly(bos);
            Utils.closeQuietly(fos);
        }
        if (Ngin3d.DEBUG) {
            Log.v(TAG, "Cache saved: " + filePath);
        }
        return succeeded;
    }

    /**
     * Load KeyframeDataSet by specified resource ID.
     *
     * @param context   the context to load data
     * @param dataResId resource ID of keyframe data set
     * @param saveCache whether to cached the data in cache directory
     * @return loaded keyframe data set
     */
    private static KeyframeDataSet getKeyframeDataSet(Context context, int dataResId,
                                                      boolean saveCache, String cacheFileName) {
        synchronized (AnimationLoader.class) {
            if (sCacheDir == null) {
                sCacheDir = new File(Environment.getExternalStorageDirectory().getPath() + "/.ngin3d");
                mkdirs(sCacheDir);
            }
        }

        // try to load cache first, if all null,  we check script.
        File cacheFile = new File(sCacheDir, (cacheFileName == null) ? Integer.toString(dataResId) : cacheFileName);
        KeyframeDataSet kf = loadCachedObject(cacheFile);
        if (kf == null) {
            // cache does not exist, load from JSON script.
            KeyframeDataLoader script = new KeyframeDataLoader(context, dataResId);
            if (Ngin3d.DEBUG) Log.d(TAG, "done new script, load anime and save cache");

            kf = script.getKeyframeDataSet();
            if (kf != null && saveCache) {
                saveObjectToCache(kf, cacheFile);
            }
        }
        return kf;
    }

    /**
     * Specify the path to store animation cache.
     *
     * @param dir the directory to store cache files
     */
    public static void setCacheDir(File dir) {
        sCacheDir = dir;
        mkdirs(dir);
    }

    private static void mkdirs(File dir) {
        if (dir != null && !dir.exists()) {
            if (!dir.mkdirs()) {
                throw new Ngin3dException("Failed to create cache directory " + dir);
            }
        }
    }
}
