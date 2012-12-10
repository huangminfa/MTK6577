/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.mediatek.camera.ui;

import com.android.camera.R;
import com.mediatek.xlog.Xlog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.ColorFilter;
import android.view.View;

class ProgressBarDrawable extends Drawable {
	
	private static final String TAG = "ProgressBarDrawable";		
		private int mBlockSizes[];		
		private int mParentHeight;
		private int mPadding;
		private View mAttachedView;
		private final Paint mPaint = new Paint();
		private Drawable mCleanBlock;
		private Drawable mDirtyBlock;
		
		public ProgressBarDrawable(Context context, View view,int[] blockSizes,int padding) {
			Resources res = context.getResources();
			mBlockSizes = blockSizes;
			mAttachedView = view;
			mPadding = padding;
			mCleanBlock = res.getDrawable(R.drawable.ic_panorama_block);
			mDirtyBlock = res.getDrawable(R.drawable.ic_panorama_block_highlight);
		}

		@Override
		protected boolean onLevelChange(int level) { 
			Xlog.i(TAG, "onLevelChange: " + level);
			invalidateSelf();
			return true; 
		}
		
		@Override
	    public int getIntrinsicWidth() {
	    	int width = 0;
	    	for(int i = 0,len = mBlockSizes.length; i < len-1; i++){
				width += mBlockSizes[i]+ mPadding;
			}
			width += mBlockSizes[mBlockSizes.length-1];
			Xlog.i(TAG, "getIntrinsicWidth: " + width);
			
	        return width;
	    }		
		
		public void setColorFilter(ColorFilter cf) {
			mPaint.setColorFilter(cf);
		}
		
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}
		
		public void setAlpha(int alpha) {
			mPaint.setAlpha(alpha);
		}

		
		public void draw(Canvas canvas) {
			int xoffset = 0;
			int level = getLevel();
			//draw dirty block according to the number captured.
			for (int i = 0; i < level; i++) {
				int yoffset = (mAttachedView.getHeight() - mBlockSizes[i]) / 2;
				mDirtyBlock.setBounds(xoffset, yoffset, xoffset + mBlockSizes[i], yoffset + mBlockSizes[i]);
				mDirtyBlock.draw(canvas);
				Xlog.i(TAG, "draw: i=" + i+" xoffset = "+xoffset+" yoffset = "+yoffset);
				xoffset += (mBlockSizes[i] + mPadding);
			}

			//draw the rest as clean block.
			for (int i = level,len = mBlockSizes.length; i < len; i++) {
				int yoffset = (mAttachedView.getHeight() - mBlockSizes[i]) / 2;
				mCleanBlock.setBounds(xoffset, yoffset, xoffset + mBlockSizes[i], yoffset + mBlockSizes[i]);				
				mCleanBlock.draw(canvas);
				Xlog.i(TAG, "draw: i=" + i+" xoffset = "+xoffset+" yoffset = "+yoffset);				
				xoffset += (mBlockSizes[i] + mPadding);
			}
		}							
}

