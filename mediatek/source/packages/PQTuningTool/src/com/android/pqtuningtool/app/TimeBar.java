/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2011 The Android Open Source Project
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

package  com.android.pqtuningtool.app;

import  com.android.pqtuningtool.common.Utils;
import  com.android.pqtuningtool.util.MtkLog;
import  com.android.pqtuningtool.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * The time bar view, which includes the current and total time, the progress bar,
 * and the scrubber.
 */
public class TimeBar extends View {

  public interface Listener {
    void onScrubbingStart();
    void onScrubbingMove(int time);
    void onScrubbingEnd(int time);
  }

  // Padding around the scrubber to increase its touch target
  private static final int SCRUBBER_PADDING_IN_DP = 10;

  // The total padding, top plus bottom
  private static final int V_PADDING_IN_DP = 30;

  private static final int TEXT_SIZE_IN_DP = 14;

  private final Listener listener;

  // the bars we use for displaying the progress
  private final Rect progressBar;
  private final Rect playedBar;

  private final Paint progressPaint;
  private final Paint playedPaint;
  private final Paint timeTextPaint;

  private final Bitmap scrubber;
  private final int scrubberPadding; // adds some touch tolerance around the scrubber

  private int scrubberLeft;
  private int scrubberTop;
  private int scrubberCorrection;
  private boolean scrubbing;
  private boolean showTimes;
  private boolean showScrubber;

  private int totalTime;
  private int currentTime;

  private final Rect timeBounds;

  private int vPaddingInPx;

  public TimeBar(Context context, Listener listener) {
    super(context);
    this.listener = Utils.checkNotNull(listener);

    showTimes = true;
    showScrubber = true;

    progressBar = new Rect();
    playedBar = new Rect();
    secondaryBar = new Rect();

    progressPaint = new Paint();
    progressPaint.setColor(0xFF808080);
    playedPaint = new Paint();
    playedPaint.setColor(0xFFFFFFFF);
    secondaryPaint = new Paint();
    secondaryPaint.setColor(0xFF5CA0C5);

    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    float textSizeInPx = metrics.density * TEXT_SIZE_IN_DP;
    timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    timeTextPaint.setColor(0xFFCECECE);
    timeTextPaint.setTextSize(textSizeInPx);
    timeTextPaint.setTextAlign(Paint.Align.CENTER);

    timeBounds = new Rect();
//    timeTextPaint.getTextBounds("0:00:00", 0, 7, timeBounds);
    
    infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    infoPaint.setColor(0xFFCECECE);
    infoPaint.setTextSize(textSizeInPx);
    infoPaint.setTextAlign(Paint.Align.CENTER);

    ellipseLength = (int)Math.ceil(infoPaint.measureText(ELLIPSE));

    scrubber = BitmapFactory.decodeResource(getResources(), R.drawable.scrubber_knob);
    scrubberPadding = (int) (metrics.density * SCRUBBER_PADDING_IN_DP);

    vPaddingInPx = (int) (metrics.density * V_PADDING_IN_DP);
    textPadding = scrubberPadding / 2;
  }

  private void update() {
    playedBar.set(progressBar);

    if (totalTime > 0) {
      playedBar.right =
          playedBar.left + (int) ((progressBar.width() * (long) currentTime) / totalTime);
      //if duration is not accurate, here just adjust playedBar
      //we also show the accurate position text to final user.
      if (playedBar.right > progressBar.right) {
          playedBar.right = progressBar.right;
      }
    } else {
      playedBar.right = progressBar.left;
    }

    if (!scrubbing) {
      scrubberLeft = playedBar.right - scrubber.getWidth() / 2;
    }
    //update text bounds when layout changed or time changed
    updateBounds();
    updateVisibleText();
    invalidate();
  }

  /**
   * @return the preferred height of this view, including invisible padding
   */
  public int getPreferredHeight() {
    //double height for time bar
    return timeBounds.height()* 2 + vPaddingInPx + scrubberPadding + textPadding;
  }

  /**
   * @return the height of the time bar, excluding invisible padding
   */
  public int getBarHeight() {
    //double height for time bar
    return timeBounds.height() * 2 + vPaddingInPx + textPadding;
  }

  public void setTime(int currentTime, int totalTime) {
    if (LOG) MtkLog.v(TAG, "setTime(" + currentTime + ", " + totalTime + ")");
    mOriginalTotalTime = totalTime;
    if (this.currentTime == currentTime && this.totalTime == totalTime) {
        return;
    }
    this.currentTime = currentTime;
    this.totalTime = Math.abs(totalTime);
    update();
  }

  public void setShowTimes(boolean showTimes) {
    this.showTimes = showTimes;
    requestLayout();
  }

  public void resetTime() {
    setTime(0, 0);
  }

  public void setShowScrubber(boolean showScrubber) {
    if (LOG) MtkLog.v(TAG, "setShowScrubber(" + showScrubber + ") showScrubber=" + showScrubber);
    this.showScrubber = showScrubber;
    if (!showScrubber && scrubbing) {
      listener.onScrubbingEnd(getScrubberTime());
      scrubbing = false;
    }
    requestLayout();
  }

  private boolean inScrubber(float x, float y) {
    int scrubberRight = scrubberLeft + scrubber.getWidth();
    int scrubberBottom = scrubberTop + scrubber.getHeight();
    return scrubberLeft - scrubberPadding < x && x < scrubberRight + scrubberPadding
        && scrubberTop - scrubberPadding < y && y < scrubberBottom + scrubberPadding;
  }

  private void clampScrubber() {
    int half = scrubber.getWidth() / 2;
    int max = progressBar.right - half;
    int min = progressBar.left - half;
    scrubberLeft = Math.min(max, Math.max(min, scrubberLeft));
  }

  private int getScrubberTime() {
    return (int) ((long) (scrubberLeft + scrubber.getWidth() / 2 - progressBar.left)
        * totalTime / progressBar.width());
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int textH = timeBounds.height() + textPadding;
    int w = r - l;
    int h = b - t;
    
    if (!showTimes && !showScrubber) {
      progressBar.set(0, 0, w, h);
    } else {
      int margin = scrubber.getWidth() / 3;
      /** mark for show time upon the progress bar
      if (showTimes) {
        margin += timeBounds.width();
      }
      */
      int progressY = textH + (h + scrubberPadding - textH) / 2;
      scrubberTop = progressY - scrubber.getHeight() / 2 + 1;
      progressBar.set(
          getPaddingLeft() + margin, progressY,
          w - getPaddingRight() - margin, progressY + 4);
    }
    update();
  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);

    // draw progress bars
    canvas.drawRect(progressBar, progressPaint);
    canvas.drawRect(playedBar, playedPaint);
    if (bufferPercent >= 0) {
      canvas.drawRect(secondaryBar, secondaryPaint);
      if (LOG) Log.v(TAG, "draw() bufferPercent=" + bufferPercent + ", secondaryBar=" + secondaryBar);
    }

    // draw scrubber and timers
    if (showScrubber) {
      canvas.drawBitmap(scrubber, scrubberLeft, scrubberTop, null);
    }
    if (showTimes) {
      canvas.drawText(
          stringForTime(currentTime),
          timeBounds.width() / 2 + getPaddingLeft(),
          timeBounds.height() + scrubberPadding + 1 + textPadding,
          timeTextPaint);
      canvas.drawText(
          stringForTime(totalTime),
          getWidth() - getPaddingRight() - timeBounds.width() / 2,
          timeBounds.height() + scrubberPadding + 1 + textPadding,
          timeTextPaint);
    }
    if (infoText != null && visibleText != null) {
       canvas.drawText(visibleText,
           getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight()) / 2,
           timeBounds.height() + scrubberPadding + 1 + textPadding,
           infoPaint);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (LOG) MtkLog.v(TAG, "onTouchEvent() showScrubber=" + showScrubber + ", enableScrubbing=" + enableScrubbing
            + ", totalTime=" + totalTime);
    if (showScrubber && enableScrubbing) {
      int x = (int) event.getX();
      int y = (int) event.getY();

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          if (inScrubber(x, y)) {
            if (mOriginalTotalTime <= 0) {
                //consume it
            } else {
                scrubbing = true;
                scrubberCorrection = x - scrubberLeft;
                listener.onScrubbingStart();
            }
            return true;
          }
          break;
        case MotionEvent.ACTION_MOVE:
          if (scrubbing) {
            scrubberLeft = x - scrubberCorrection;
            clampScrubber();
            currentTime = getScrubberTime();
            listener.onScrubbingMove(currentTime);
            invalidate();
            return true;
          }
          break;
        case MotionEvent.ACTION_UP:
          if (scrubbing) {
            listener.onScrubbingEnd(getScrubberTime());
            scrubbing = false;
            return true;
          }
          break;
      }
    }
    return false;
  }

  private String stringForTime(long millis) {
    int totalSeconds = (int) millis / 1000;
    int seconds = totalSeconds % 60;
    int minutes = (totalSeconds / 60) % 60;
    int hours = totalSeconds / 3600;
    if (hours > 0) {
      return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
    } else {
      return String.format("%02d:%02d", minutes, seconds).toString();
    }
  }
  
  private static final String TAG = "Gallery3D/TimeBar";
  private static final boolean LOG = true;
  
  public static final int UNKNOWN = -1;
  private static final String ELLIPSE = "...";
  
  private final Rect secondaryBar;
  private final Paint secondaryPaint;
  private final Paint infoPaint;
  private Rect infoBounds;
  
  private int bufferPercent = UNKNOWN;
  private int lastShowTime = UNKNOWN;
  private String infoText;
  private String visibleText;
  private boolean enableScrubbing;
  private final int ellipseLength;
  private final int textPadding;
  
  public void setScrubbing(boolean enable) {
      if (LOG) MtkLog.v(TAG, "setScrubbing(" + enable + ")");
      enableScrubbing = enable;
  }
  
  public void setInfo(String info) {
      if (LOG) MtkLog.v(TAG, "setInfo(" + info + ")");
      infoText = info;
      invalidate();
  }
  
  public void setBufferPercent(int percent) {
      if (LOG) MtkLog.v(TAG, "setBufferPercent(" + percent + ")");
      //enable buffer progress bar
      bufferPercent = percent;
      if (bufferPercent >= 0) {
          secondaryBar.set(progressBar);
          secondaryBar.right = secondaryBar.left + (int)(bufferPercent * progressBar.width() / 100);
      } else {
          secondaryBar.right = secondaryBar.left;
      }
      invalidate();
  }

  private void updateBounds() {
      int showTime = totalTime > currentTime ? totalTime : currentTime;
      if (lastShowTime == showTime) {
          //do not need to recompute the bounds.
          return;
      }
      String durationText = stringForTime(showTime);
      int length = durationText.length();
      timeTextPaint.getTextBounds(durationText, 0, length, timeBounds);
      lastShowTime = showTime;
      if (LOG) MtkLog.v(TAG, "updateBounds() durationText=" + durationText + ", timeBounds=" + timeBounds);
  }

  private void updateVisibleText() {
      if (infoText == null) {
          visibleText = null;
          return;
      }
      float tw = infoPaint.measureText(infoText);
      float space = progressBar.width() - timeBounds.width() * 2 - getPaddingLeft() - getPaddingRight(); 
      if (tw > 0 && space > 0 && tw > space) {
          //we need to cut the info text for visible
          float originalNum = infoText.length();
          int realNum = (int)((space - ellipseLength) * originalNum / tw);
          if (LOG) MtkLog.v(TAG, "updateVisibleText() infoText=" + infoText + " text width=" + tw
                  + ", space=" + space + ", originalNum=" + originalNum + ", realNum=" + realNum
                  + ", getPaddingLeft()=" + getPaddingLeft() + ", getPaddingRight()=" + getPaddingRight()
                  + ", progressBar=" + progressBar + ", timeBounds=" + timeBounds);
          visibleText = infoText.substring(0, realNum) + ELLIPSE;
      } else {
          visibleText = infoText;
      }
      if (LOG) MtkLog.v(TAG, "updateVisibleText() infoText=" + infoText + ", visibleText=" + visibleText
              + ", text width=" + tw + ", space=" + space);
  }
  
  //for duration displayed
  private int mOriginalTotalTime;
}
