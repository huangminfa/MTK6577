/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.contacts.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View.MeasureSpec;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.android.contacts.R;

public class ButtonGridLayout extends FrameLayout {
    private final int COLUMNS = 3;
    private final int ROWS = 4;

    // Width and height of a button
    private int mButtonWidth;
    private int mButtonHeight;

    // Width and height of a button + padding.
    private int mWidthInc;
    private int mHeightInc;

    // Height of the dialpad. Used to align it at the bottom of the
    // view.
    private int mHeight;

    private boolean mLayouted = false;

    public ButtonGridLayout(Context context) {
        super(context);
    }

    public ButtonGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected ImageButton createGridButton(int id, int imageResId, Drawable drawable) {
        ImageButton imageButton = new ImageButton(getContext());
        imageButton.setId(id);
        imageButton.setImageResource(imageResId);
        imageButton.setBackgroundDrawable(drawable);
        imageButton.setSoundEffectsEnabled(false);

        FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(mButtonWidth, mButtonHeight);
        imageButton.setLayoutParams(lParams);

        return imageButton;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mButtonWidth = getContext().getResources().getDimensionPixelSize(R.dimen.button_grid_layout_button_width);
        mButtonHeight = getContext().getResources().getDimensionPixelSize(R.dimen.button_grid_layout_button_height);
        mWidthInc = mButtonWidth;
        mHeightInc = mButtonHeight;

        ImageButton button;
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(new int[] {android.R.attr.selectableItemBackground});
        Drawable drawable = typedArray.getDrawable(0);

        button = createGridButton(R.id.one, R.drawable.dial_num_1, drawable);
        addView(button);

        button = createGridButton(R.id.two, R.drawable.dial_num_2, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.three, R.drawable.dial_num_3, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.four, R.drawable.dial_num_4, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.five, R.drawable.dial_num_5, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.six, R.drawable.dial_num_6, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.seven, R.drawable.dial_num_7, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.eight, R.drawable.dial_num_8, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.nine, R.drawable.dial_num_9, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.star, R.drawable.dial_num_star, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.zero, R.drawable.dial_num_0, drawable.getConstantState().newDrawable());
        addView(button);

        button = createGridButton(R.id.pound, R.drawable.dial_num_pound, drawable.getConstantState().newDrawable());
        addView(button);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(mLayouted)
            return;

        mLayouted = true;

        int i = 0;
        // The last row is bottom aligned.
        int y = mPaddingTop;
        for (int row = 0; row < ROWS; row++) {
            int x = mPaddingLeft;
            for (int col = 0; col < COLUMNS; col++) {
                View child = getChildAt(i);

                child.layout(x, y, x + mButtonWidth, y + mButtonHeight);

                x += mWidthInc;
                i++;
            }
            y += mHeightInc;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Measure the first child and get it's size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        final int width = displayMetrics.widthPixels;
        final int height = mButtonHeight * ROWS;

        setMeasuredDimension(width, height);
    }
}
