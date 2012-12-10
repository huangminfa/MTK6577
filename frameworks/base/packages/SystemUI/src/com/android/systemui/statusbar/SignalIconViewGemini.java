package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.systemui.R;

// [SystemUI] Support "Dual SIM".
public class SignalIconViewGemini extends ImageView {
    private int mPadding;

    public SignalIconViewGemini(Context context) {
        super(context);
        init(context);
    }

    public SignalIconViewGemini(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SignalIconViewGemini(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        final Resources res = context.getResources();
        mPadding = res.getDimensionPixelSize(R.dimen.status_bar_signal_bg_padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(-mPadding/2, -mPadding/2);
        super.onDraw(canvas);
        canvas.restore();
    }
}
