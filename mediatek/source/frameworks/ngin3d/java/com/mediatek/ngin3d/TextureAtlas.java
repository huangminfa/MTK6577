package com.mediatek.ngin3d;

import android.content.res.Resources;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.utils.Ngin3dException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Represents animation that can be started or stopped.
 */
public class TextureAtlas {

    private static final String FRAMES = "frames";
    private static final String FRAME = "frame";
    private static final String ROTATED = "rotated";
    private static final String TRIMMED = "trimmed";
    private static final String SPRITE_SOURCE_SIZE = "spriteSourceSize";
    private static final String SOURCE_SIZE = "sourceSize";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String WIDTH = "w";
    private static final String HEIGHT = "h";

    private static TextureAtlas sDefault;

    /**
     * Gets the default atlas object.
     * @return  texture atlas object.
     */
    public static TextureAtlas getDefault() {
        synchronized (TextureAtlas.class) {
            if (sDefault == null) {
                sDefault = new TextureAtlas();
            }
        }
        return sDefault;
    }

    private final HashMap<Integer, Atlas> mAtlasSet = new HashMap<Integer, Atlas>();

    /**
     * The Atlas that contains one image and one JSONObject.
     */
    private static class Atlas {
        public final int imageId;
        public final JSONObject frames;

        public Atlas(int resId, JSONObject frames) {
            this.imageId = resId;
            this.frames = frames;
        }
    }

    /**
     * Add a new image into this atlas object.
     * @param resources  gets the android resource manager
     * @param atlasId  resource id
     * @param scriptId  the JSON script file id.
     */
    public void add(Resources resources, int atlasId, int scriptId) {
        if (mAtlasSet.containsKey(atlasId)) {
            return;
        }
        JSONObject frames;
        InputStream is = resources.openRawResource(scriptId);
        try {
            int length = is.available();
            byte[] b = new byte[length];
            if (is.read(b, 0, length) == length) {
                final String s = new String(b);
                JSONObject atlas = new JSONObject(s);
                frames = atlas.optJSONObject(FRAMES);
            } else {
                throw new Ngin3dException("JSON of Packer List doesn't read completely");
            }
        } catch (IOException e) {
            throw new Ngin3dException(e);
        } catch (JSONException e) {
            throw new Ngin3dException(e);
        } finally {
            Utils.closeQuietly(is);
        }

        if (frames != null) {
            mAtlasSet.put(atlasId, new Atlas(atlasId, frames));
        }
    }

    private String getResourceFilename(ImageDisplay.Resource res) {
        String resName = res.resources.getString(res.resId);
        return resName.substring(resName.lastIndexOf("/") + 1);
    }

    /**
     * Gets the specific image information from the atlas by image resource , box , and dimension information.
     * @param res  image resource information.
     * @param rect  box information.
     * @param size  dimension information.
     * @return  true if get the information is successful.
     */
    public boolean getFrame(ImageDisplay.Resource res, Box rect, Dimension size) {
        String fileName = getResourceFilename(res);

        for (Atlas atlas : mAtlasSet.values()) {
            JSONObject resObject = atlas.frames.optJSONObject(fileName);
            if (resObject != null) {
                res.resId = atlas.imageId;

                JSONObject frame = resObject.optJSONObject(FRAME);
                int x = frame.optInt(X);
                int y = frame.optInt(Y);
                int w = frame.optInt(WIDTH);
                int h = frame.optInt(HEIGHT);
                rect.set(x, y, w + x, h + y);

                JSONObject sourceSize = resObject.optJSONObject(SOURCE_SIZE);
                size.width = sourceSize.optInt(WIDTH);
                size.height = sourceSize.optInt(HEIGHT);
                return true;
            }
        }
        return false;
    }

    /**
     * Clears all of the setting of this atlas.
     */
    public void cleanup() {
        mAtlasSet.clear();
    }

    /**
     * For test ONLY. Check if the atlas set is empty.
     * @return  boolean indicate atlas set is empty.
     */
    public boolean isEmpty() {
        return mAtlasSet.isEmpty();
    }

}
