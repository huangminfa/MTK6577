package com.mediatek.ngin3d;

import com.mediatek.ngin3d.utils.Ngin3dException;

import java.util.ArrayList;

/**
 * This class combines plain text and bitmap font text. It can be used to create a normal text using system default font or a special text using customize font.
 * Must give a bitmap font setting reference to the class for initialization if bitmap font is used.
 */
public class BitmapText extends Group {

    private static BitmapFont sDefaultBmFont;
    private String mText;
    private BitmapFont mFont;
    private Point mAnchorPoint = new Point(0.5f, 0.5f);
    private final ArrayList<Image> mCharImages = new ArrayList<Image>();

    /**
     * Initialize a empty text.
     */
    public BitmapText() {
        this("");
    }

    /**
     * Construct a text with a specific string. Using setDefaultFont method to set up the default bitmap font setting, or it will be shown in default system font style.
     *
     * @param text a specific string to be used for initializing this Text object.
     */
    public BitmapText(String text) {
        this(text, sDefaultBmFont);
    }

    /**
     * Construct a text with a specific string and bitmap font setting. It will be shown in the specific font style.
     *
     * @param text a specific string to be used for initializing this Text object.
     * @param font a specific bitmap font setting to be used for initializing this Text object.
     */
    public BitmapText(String text, BitmapFont font) {
        mText = text;
        if (font == null) {
            throw new Ngin3dException("No bitmap font is specified.");
        }

        mFont = font;
        setupCharImages();
    }

    @Override
    protected void onChildAdded(Actor child) {
        // Do nothing
    }

    @Override
    protected void onChildRemoved(Actor child) {
        // Do nothing
    }

    /**
     * Sets up the default bitmap font style in the class.
     *
     * @param font a specific bitmap font used for default setting of text in the class.
     */
    public static void setDefaultFont(BitmapFont font) {
        sDefaultBmFont = font;
    }

    /**
     * Gets the default bitmap font setting.
     *
     * @return bitmap font setting
     */
    public static BitmapFont getDefaultFont() {
        return sDefaultBmFont;
    }

    /**
     * Gets the bitmap font.
     *
     * @return bitmap font
     */
    public BitmapFont getFont() {
        return mFont;
    }

    /**
     * Set a new string for the Text object.
     * The program will check if the local bitmap font style is set first, if not, then the program will check the default bitmap font style is set and use it.
     * Otherwise, the default system plain text will be used.
     *
     * @param text a new text to be used.
     */
    public void setText(String text) {
        if (!text.equals(mText)) {
            mText = text;
            setupCharImages();
        }
    }

    /**
     * Gets the text of this object.
     *
     * @return text of user input
     */
    public final String getText() {
        return mText;
    }

    /**
     * Set a new bitmap font style.
     *
     * @param font a new bitmap font style to be used.
     */
    public void setFont(BitmapFont font) {
        if (!font.equals(mFont)) {
            mFont = font;
            setupCharImages();
        }
    }

    private void setupCharImages() {
        int textWidth = 0;
        int textHeight = 0;

        BitmapFont.CharacterInfo charInfo;

        removeAllChildren();

        int xAdv = 0;
        for (int i = 0; i < mText.length(); i++) {
            // Each char image has its rect and dimension
            Box rect = new Box();
            Dimension dim = new Dimension();

            charInfo = mFont.getCharInfo(mText.charAt(i));

            if (charInfo.height > textHeight) {
                textHeight = charInfo.height;
            }
            textWidth = textWidth + charInfo.width + charInfo.xOffset;

            mFont.getCharRect(charInfo, rect);

            dim.height = charInfo.height;
            dim.width = charInfo.width;

            Image charImage;
            if (i >= mCharImages.size()) {
                charImage = mFont.createCharImage();

                mCharImages.add(charImage);
                addChild(charImage);
            } else {
                charImage = mCharImages.get(i);
                mFont.setupCharImage(charImage);
            }

            float y1 = rect.y1;
            rect.y1 = rect.y2;
            rect.y2 = y1;

            charImage.setSourceRect(rect);
            charImage.setSize(dim);
            charImage.setPosition(new Point(xAdv, charInfo.yOffset));

            xAdv += charInfo.xAdvance;
        }

        mCharImages.subList(mText.length(), mCharImages.size()).clear(); // Trim extra char images

        applyAnchorPoint(textWidth, textHeight);
    }

    private void applyAnchorPoint(int textWidth, int textHeight) {
        if (mAnchorPoint == null) {
            return;
        }

        float newOriginX = (float) textWidth * mAnchorPoint.x;
        float newOriginY = (float) textHeight * mAnchorPoint.y;

        for (int i = 0; i < mCharImages.size(); i++) {
            Image charImage = mCharImages.get(i);

            // A3M doesn't flip the characters on the y-axis, so we do not have
            // to shift the anchor point.
            charImage.setAnchorPoint(new Point(0.0f, 0.0f));

            Point oldPos = charImage.getPosition();
            charImage.setPosition(new Point(oldPos.x - newOriginX - 12, oldPos.y - newOriginY));
        }
    }

    /**
     * Set the anchor point.
     * If the local bitmap font style is set, the program will change the anchor point of local bitmap font.
     * Or it will change the anchor point of default font setting, then is the default system plain text.
     *
     * @param anchorPoint The point to be used for the anchor point.
     */
    @Override
    public void setAnchorPoint(Point anchorPoint) {
        if (anchorPoint.x < 0.0f || anchorPoint.x > 1.0f) {
            throw new IllegalArgumentException("x must be >= 0 and <= 1");
        } else if (anchorPoint.y < 0.0f || anchorPoint.y > 1.0f) {
            throw new IllegalArgumentException("y must be >= 0 and <= 1");
        }
        mAnchorPoint = anchorPoint;
    }

    /**
     * Gets the anchor point of this object.
     *
     * @return the anchor point
     */
    public Point getAnchorPoint() {
        return mAnchorPoint;
    }
}
