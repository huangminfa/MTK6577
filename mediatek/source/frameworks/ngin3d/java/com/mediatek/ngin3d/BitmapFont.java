package com.mediatek.ngin3d;

import android.content.res.Resources;
import com.mediatek.ngin3d.utils.Ngin3dException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Bitmap font setting, must initialize before using.
 */
public class BitmapFont {

    private final int mResId;
    private final Resources mResources;
    private final HashMap<Character, CharacterInfo> mCharMap = new HashMap<Character, CharacterInfo>();
    private int mScaleH;
    private int mWordWidth;

    /**
     * Initialize a bitmap font style with FNT and image file.
     *
     * @param resources android resource
     * @param settingId setting file resource id
     * @param resId     image resource id.
     */
    public BitmapFont(Resources resources, int settingId, int resId) {
        mResId = resId;
        mResources = resources;
        initialize(resources.openRawResource(settingId));
    }

    public static class CharacterInfo {
        public int srcX;
        public int srcY;
        public int width;
        public int height;
        public int xOffset;
        public int yOffset;
        public int xAdvance;
    }

    private void initialize(InputStream settingFile) {
        parseFntFile(settingFile);
    }

    public void getCharRect(CharacterInfo charInfo, Box rect) {
        rect.set(charInfo.srcX, mScaleH - charInfo.srcY - charInfo.height, charInfo.width + charInfo.srcX, mScaleH - charInfo.srcY);
    }

    private static final char CHAR_SPACE = ' ';

    public CharacterInfo getCharInfo(char ch) {
        CharacterInfo charInfo;
        charInfo = mCharMap.get(ch);
        if (charInfo == null) {
            charInfo = mCharMap.get(CHAR_SPACE);    // replace as space
            if (charInfo == null) {
                throw new Ngin3dException("Cannot find replacement character");
            }
        }
        return charInfo;
    }

    public Image createCharImage() {
        return Image.createFromResource(mResources, mResId);
    }

    public void setupCharImage(Image image) {
        image.setImageFromResource(mResources, mResId);
    }

    private void parseFntFile(InputStream settingFile) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(settingFile));
        try {
            reader.readLine();  // info

            String line = reader.readLine();
            if (line == null) {
                throw new Ngin3dException("Invalid font file");
            }
            String[] common = line.split(" ");
            if (common.length < 4) {
                throw new Ngin3dException("Invalid font file");
            }
            if (!common[1].startsWith("lineHeight=")) {
                throw new Ngin3dException("Invalid font file");
            }

            if (!common[2].startsWith("base=")) {
                throw new Ngin3dException("Invalid font file");
            }

            mScaleH = Integer.parseInt(common[4].substring(7));
            line = reader.readLine();
            if (line == null) {
                throw new Ngin3dException("Invalid font file");
            }
            String[] pages = line.split(" ", 4);
            if (!pages[2].startsWith("file=")) {
                throw new Ngin3dException("Invalid font file");
            }

            line = reader.readLine();  // char number

            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("kernings ")) {
                    break;
                }
                if (!line.startsWith("char ")) {
                    continue;
                }

                CharacterInfo font = new CharacterInfo();
                StringTokenizer tokens = new StringTokenizer(line, " ");
                tokens.nextToken();
                String[] tmpChar = tokens.nextToken().toString().split("=", 2);
                char ch = (char) Integer.parseInt(tmpChar[1]);

                tmpChar = tokens.nextToken().toString().split("=", 2);
                font.srcX = Integer.parseInt(tmpChar[1]);
                tmpChar = tokens.nextToken().toString().split("=", 2);
                font.srcY = Integer.parseInt(tmpChar[1]);
                tmpChar = tokens.nextToken().toString().split("=", 2);
                font.width = Integer.parseInt(tmpChar[1]);
                if (font.width > mWordWidth) {
                    mWordWidth = font.width;
                }
                tmpChar = tokens.nextToken().toString().split("=", 2);
                font.height = Integer.parseInt(tmpChar[1]);
                tmpChar = tokens.nextToken().toString().split("=", 2);
                font.xOffset = Integer.parseInt(tmpChar[1]);
                tmpChar = tokens.nextToken().toString().split("=", 2);
                font.yOffset = Integer.parseInt(tmpChar[1]);
                tmpChar = tokens.nextToken().toString().split("=", 2);
                font.xAdvance = Integer.parseInt(tmpChar[1]);

                mCharMap.put(ch, font);
            }
        } catch (IOException ex) {
            throw new Ngin3dException("Error loading font file", ex);
        } finally {
            Utils.closeQuietly(reader);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitmapFont)) return false;

        BitmapFont that = (BitmapFont) o;

        if (mResId != that.mResId) return false;
        if (mResources == null ? that.mResources != null : !mResources.equals(that.mResources)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mResId;
        result = 31 * result + (mResources == null ? 0 : mResources.hashCode());
        return result;
    }
}
