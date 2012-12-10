package com.mediatek.media3d;

import android.graphics.Typeface;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Text;

public final class FormatUtil {
    private FormatUtil() {}

    public static Text applyTextAttributes(final Text target, final int textSize, final boolean isBold) {
        target.setTextSize(textSize);
        target.setTextColor(new Color(0xFFFFFFFF));
        target.setShadowLayer(2, 0, 0, 0);
        target.setTypeface(isBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return target;
    }
}
