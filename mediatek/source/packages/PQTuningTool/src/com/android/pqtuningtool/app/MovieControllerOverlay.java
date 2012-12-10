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
import android.graphics.Color;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * The playback controller for the Movie Player.
 */
public class MovieControllerOverlay extends FrameLayout implements
    ControllerOverlay,
    OnClickListener,
    AnimationListener,
    TimeBar.Listener {

  private enum State {
    PLAYING,
    PAUSED,
    ENDED,
    ERROR,
    LOADING,//mean connecting
    BUFFERING,
    RETRY_CONNECTING,
    RETRY_CONNECTING_ERROR
  }

  private static final float ERROR_MESSAGE_RELATIVE_PADDING = 1.0f / 6;

  private Listener listener;

  private final View background;
  private final TimeBar timeBar;

  private View mainView;
  private final LinearLayout loadingView;
  private final TextView errorView;
  private final ImageView playPauseReplayView;

  private final Handler handler;
  private final Runnable startHidingRunnable;
  private final Animation hideAnimation;

  private State state;

  private boolean hidden;

  private boolean canReplay = true;

  public MovieControllerOverlay(Context context) {
    super(context);

    state = State.LOADING;

    LayoutParams wrapContent =
        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    LayoutParams matchParent =
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    LayoutInflater inflater = LayoutInflater.from(context);
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    int padding = (int)(metrics.density * MARGIN);

    background = new View(context);
    background.setBackgroundColor(context.getResources().getColor(R.color.darker_transparent));
    addView(background, matchParent);

    timeBar = new TimeBar(context, this);
    timeBar.setPadding(padding, 0, padding, 0);
    addView(timeBar, wrapContent);

    loadingView = new LinearLayout(context);
    loadingView.setOrientation(LinearLayout.VERTICAL);
    loadingView.setGravity(Gravity.CENTER_HORIZONTAL);
    ProgressBar spinner = new ProgressBar(context);
    spinner.setIndeterminate(true);
    loadingView.addView(spinner, wrapContent);
    addView(loadingView, wrapContent);

    playPauseReplayView = new ImageView(context);
    playPauseReplayView.setImageResource(R.drawable.ic_vidcontrol_play);
    playPauseReplayView.setBackgroundResource(R.drawable.bg_vidcontrol);
    playPauseReplayView.setScaleType(ScaleType.CENTER);
    playPauseReplayView.setFocusable(true);
    playPauseReplayView.setClickable(true);
    playPauseReplayView.setOnClickListener(this);
    addView(playPauseReplayView, wrapContent);

    errorView = new TextView(context);
    errorView.setGravity(Gravity.CENTER);
    errorView.setBackgroundColor(0xCC000000);
    errorView.setTextColor(0xFFFFFFFF);
    addView(errorView, matchParent);
    
    //add screenView
    screenView = new ImageView(context);
    screenView.setImageResource(R.drawable.ic_media_fullscreen);//default next screen mode
    screenView.setScaleType(ScaleType.CENTER);
    screenView.setFocusable(true);
    screenView.setClickable(true);
    screenView.setOnClickListener(this);
    addView(screenView, wrapContent);
    
    //for screen layout
    Bitmap screenButton = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_bigscreen);
    screenWidth = screenButton.getWidth();
    screenPadding = (int)(metrics.density * MARGIN);
    screenButton.recycle();

    handler = new Handler();
    startHidingRunnable = new Runnable() {
      public void run() {
        startHiding();
      }
    };

    hideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
    hideAnimation.setAnimationListener(this);

    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    setLayoutParams(params);
    hide();
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void setCanReplay(boolean canReplay) {
    this.canReplay = canReplay;
  }

  public View getView() {
    return this;
  }

  public void showPlaying() {
    if (state == State.BUFFERING) {
        lastState = State.PLAYING;
    } else {
        state = State.PLAYING;
        showMainView(playPauseReplayView);
    }
    if (LOG) MtkLog.v(TAG, "showPlaying() state=" + state);
  }

  public void showPaused() {
    if (state == State.BUFFERING) {
        lastState = State.PAUSED;
    } else {
        state = State.PAUSED;
        showMainView(playPauseReplayView);
    }
    if (LOG) MtkLog.v(TAG, "showPaused() state=" + state);
  }

  public void showEnded() {
    if (LOG) MtkLog.v(TAG, "showEnded() state=" + state);
    clearBuffering();
    state = State.ENDED;
    timeBar.setInfo(null);
    showMainView(playPauseReplayView);
  }

  public void showLoading() {
    state = State.LOADING;
    int msgId = com.mediatek.R.string.media_controller_connecting;
    String text = this.getResources().getString(msgId);
    timeBar.setInfo(text);
    showMainView(loadingView);
  }

  public void showErrorMessage(String message) {
    clearBuffering();
    state = State.ERROR;
    int padding = (int) (getMeasuredWidth() * ERROR_MESSAGE_RELATIVE_PADDING);
    errorView.setPadding(padding, 10, padding, 10);
    errorView.setText(message);
    showMainView(errorView);
  }

  public void resetTime() {
    timeBar.resetTime();
  }

  public void setTimes(int currentTime, int totalTime) {
    timeBar.setTime(currentTime, totalTime);
  }

  public void hide() {
    boolean wasHidden = hidden;
    hidden = true;
    playPauseReplayView.setVisibility(View.INVISIBLE);
    loadingView.setVisibility(View.INVISIBLE);
    if (!alwaysShowBottom) {
        background.setVisibility(View.INVISIBLE);
        timeBar.setVisibility(View.INVISIBLE);
        screenView.setVisibility(View.INVISIBLE);
        setVisibility(View.INVISIBLE);
    } else {
        setAudioBackground(true);
    }
    setFocusable(true);
    requestFocus();
    if (listener != null && wasHidden != hidden) {
      listener.onHidden();
    }
    if (LOG) MtkLog.v(TAG, "hide() wasHidden=" + wasHidden + ", hidden=" + hidden);
  }

  private void showMainView(View view) {
    mainView = view;
    errorView.setVisibility(mainView == errorView ? View.VISIBLE : View.INVISIBLE);
    loadingView.setVisibility(mainView == loadingView ? View.VISIBLE : View.INVISIBLE);
    playPauseReplayView.setVisibility(
        mainView == playPauseReplayView ? View.VISIBLE : View.INVISIBLE);
    if (LOG) MtkLog.v(TAG, "showMainView(" + view + ") errorView=" + errorView + ", loadingView=" + loadingView
            + ", playPauseReplayView=" + playPauseReplayView);
    if (LOG) MtkLog.v(TAG, "showMainView() enableScrubbing=" + enableScrubbing + ", state=" + state);
    if (enableScrubbing && (state == State.PAUSED || state == State.PLAYING)) {
        timeBar.setScrubbing(true);
    } else {
        timeBar.setScrubbing(false);
    }
    show();
  }

  public void show() {
    boolean wasHidden = hidden;
    hidden = false;
    updateViews();
    setVisibility(View.VISIBLE);
    setFocusable(false);
    if (alwaysShowBottom) {
        setAudioBackground(false);
    }
    if (listener != null && wasHidden != hidden) {
      listener.onShown();
    }
    maybeStartHiding();
    if (LOG) MtkLog.v(TAG, "show() wasHidden=" + wasHidden + ", hidden=" + hidden + ", listener=" + listener);
  }

  private void maybeStartHiding() {
    cancelHiding();
    if (state == State.PLAYING) {
      handler.postDelayed(startHidingRunnable, 2500);
    }
    if (LOG) MtkLog.v(TAG, "maybeStartHiding() state=" + state);
  }

  private void startHiding() {
    if (!alwaysShowBottom) {
        startHideAnimation(timeBar);
        startHideAnimation(screenView);
    }
    startHideAnimation(playPauseReplayView);
  }

  private void startHideAnimation(View view) {
    if (view.getVisibility() == View.VISIBLE) {
      view.startAnimation(hideAnimation);
    }
  }

  private void cancelHiding() {
    handler.removeCallbacks(startHidingRunnable);
    playPauseReplayView.setAnimation(null);
    if (!alwaysShowBottom) {
        background.setAnimation(null);
        timeBar.setAnimation(null);
        screenView.setAnimation(null);
   }
  }

  public void onAnimationStart(Animation animation) {
    // Do nothing.
  }

  public void onAnimationRepeat(Animation animation) {
    // Do nothing.
  }

  public void onAnimationEnd(Animation animation) {
    hide();
  }

  public void onClick(View view) {
    if (LOG) MtkLog.v(TAG, "onClick(" + view + ") listener=" + listener + ", state=" + state + ", canReplay=" + canReplay);
    if (listener != null) {
      if (view == playPauseReplayView) {
        if (state == State.ENDED || state == State.RETRY_CONNECTING_ERROR) {
          //if (canReplay) {
            //replay it anyway.
              listener.onReplay();
          //}
        } else if (state == State.PAUSED || state == State.PLAYING) {
          listener.onPlayPause();
        }
      } else if (view == screenView) {
          setScreenMode(getNextScreenMode());
          listener.OnScreenModeChanged(getScreenMode());
          //show();//show it?
      }
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (hidden) {
      show();
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (super.onTouchEvent(event)) {
      return true;
    }

    if (hidden) {
      show();
      return true;
    }
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        cancelHiding();
        if (state == State.PLAYING || state == State.PAUSED) {
          listener.onPlayPause();
        }
        break;
      case MotionEvent.ACTION_UP:
        maybeStartHiding();
        break;
    }
    return true;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int bw;
    int bh;
    int y;
    int h = b - t;
    int w = r - l;
    boolean error = errorView.getVisibility() == View.VISIBLE;

    bw = timeBar.getBarHeight();
    bh = bw;
    y = b - bh;

    background.layout(l, y, r, b);
    
    //layout screen view position
    int sw = screenPadding * 2 + screenWidth;
    bh = timeBar.getPreferredHeight(); 
    screenView.layout(r - sw, b - bh, r, b);

    //left time bar screen view width
    timeBar.layout(l, b - timeBar.getPreferredHeight(), r - bw, b);
    // Needed, otherwise the framework will not re-layout in case only the padding is changed
    timeBar.requestLayout();

    // play pause / next / previous buttons
    int cx = l + w / 2; // center x
    int playbackButtonsCenterline = t + h / 2;
    bw = playPauseReplayView.getMeasuredWidth();
    bh = playPauseReplayView.getMeasuredHeight();
    playPauseReplayView.layout(
        cx - bw / 2, playbackButtonsCenterline - bh / 2, cx + bw / 2,
        playbackButtonsCenterline + bh / 2);

    // Space available on each side of the error message for the next and previous buttons
    int errorMessagePadding = (int) (w * ERROR_MESSAGE_RELATIVE_PADDING);

    if (mainView != null) {
      layoutCenteredView(mainView, l, t, r, b);
    }
  }

  private void layoutCenteredView(View view, int l, int t, int r, int b) {
    int cw = view.getMeasuredWidth();
    int ch = view.getMeasuredHeight();
    int cl = (r - l - cw) / 2;
    int ct = (b - t - ch) / 2;
    view.layout(cl, ct, cl + cw, ct + ch);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    measureChildren(widthMeasureSpec, heightMeasureSpec);
  }

  private void updateViews() {
    if (hidden) {
      return;
    }
    background.setVisibility(View.VISIBLE);
    timeBar.setVisibility(View.VISIBLE);
    playPauseReplayView.setImageResource(
        state == State.PAUSED ? R.drawable.ic_vidcontrol_play :
          state == State.PLAYING ? R.drawable.ic_vidcontrol_pause :
            R.drawable.ic_vidcontrol_reload);
    playPauseReplayView.setVisibility(
        (state != State.LOADING && state != State.ERROR &&
                !(state == State.ENDED && !canReplay) &&
                state != State.BUFFERING && state != State.RETRY_CONNECTING &&
                !(state != State.ENDED && !canPause))
        ? View.VISIBLE : View.GONE);
    screenView.setVisibility(View.VISIBLE);//always show it
    if (playingInfo != null && state == State.PLAYING) {
        timeBar.setInfo(playingInfo);
    }
    requestLayout();
    if (LOG) MtkLog.v(TAG, "updateViews() state=" + state + ", canReplay=" + canReplay + ", lastState=" + lastState);
  }

  // TimeBar listener

  public void onScrubbingStart() {
    cancelHiding();
    listener.onSeekStart();
  }

  public void onScrubbingMove(int time) {
    cancelHiding();
    listener.onSeekMove(time);
  }

  public void onScrubbingEnd(int time) {
    maybeStartHiding();
    listener.onSeekEnd(time);
  }

    private static final String TAG = "Gallery3D/MovieControllerOverlay";
    private static final boolean LOG = true;
    private static final int MARGIN = 10;//dip
    private State lastState;
    private String playingInfo;
    
    @Override
    public void showBuffering(boolean fullBuffer, int percent) {
        if (LOG) MtkLog.v(TAG, "showBuffering(" + fullBuffer + ", " + percent + ") lastState=" + lastState + ", state=" + state);
        if (fullBuffer) {
            //do not show text and loading
            timeBar.setBufferPercent(percent);
            return;
        }
        if (state == State.PAUSED || state == State.PLAYING) {
            lastState = state;
        }
        if (percent >=0 && percent < 100) {//valid value
            state = State.BUFFERING;
            int msgId = com.mediatek.R.string.media_controller_buffering;
            String text = String.format(getResources().getString(msgId), percent);
            timeBar.setInfo(text);
            showMainView(loadingView);
        } else if (percent == 100) {
            state = lastState;
            timeBar.setInfo(null);
            showMainView(playPauseReplayView);//restore play pause state
        } else {//here to restore old state
            state = lastState;
            timeBar.setInfo(null);
        }
    }
    
    //set buffer percent to unknown value
    @Override
    public void clearBuffering() {
        if (LOG) MtkLog.v(TAG, "clearBuffering()");
        timeBar.setBufferPercent(TimeBar.UNKNOWN);
        showBuffering(false, TimeBar.UNKNOWN);
    }
    
    @Override
    public void showReconnecting(int times) {
        clearBuffering();
        state = State.RETRY_CONNECTING;
        int msgId = R.string.VideoView_error_text_cannot_connect_retry;
        String text = getResources().getString(msgId, times);
        timeBar.setInfo(text);
        showMainView(loadingView);
        if (LOG) MtkLog.v(TAG, "showReconnecting(" + times + ")");
    }
    
    @Override
    public void showReconnectingError() {
        clearBuffering();
        state = State.RETRY_CONNECTING_ERROR;
        int msgId = com.mediatek.R.string.VideoView_error_text_cannot_connect_to_server;
        String text = getResources().getString(msgId);
        timeBar.setInfo(text);
        showMainView(playPauseReplayView);
        if (LOG) MtkLog.v(TAG, "showReconnectingError()");
    }

    @Override
    public void setPlayingInfo(boolean liveStreaming) {
        int msgId;
        if (liveStreaming) {
            msgId = com.mediatek.R.string.media_controller_live;
        } else {
            msgId = com.mediatek.R.string.media_controller_playing;
        }
        playingInfo = getResources().getString(msgId);
        if (LOG) MtkLog.v(TAG, "setPlayingInfo(" + liveStreaming + ") playingInfo=" + playingInfo);
    }
    
    //for screen mode feature
    private ImageView screenView;
    private int screenMode;
    //screen mode list
    private int mScreenModes = SCREENMODE_ALL;
    private static final int SCREENMODE_ALL = 7;
    private final int screenPadding;
    private final int screenWidth;
    
    private void updateScreenModeDrawable() {
        int screenMode = getNextScreenMode();
        if (screenMode == MTKVideoView.SCREENMODE_BIGSCREEN) {
            screenView.setImageResource(R.drawable.ic_media_bigscreen);
        } else if (screenMode == MTKVideoView.SCREENMODE_FULLSCREEN) {
            screenView.setImageResource(R.drawable.ic_media_fullscreen);
        } else {
            screenView.setImageResource(R.drawable.ic_media_cropscreen);
        }
    }
    
    private int getNextScreenMode() {
        int mode = getScreenMode();
        mode <<= 1;
        if ((mode & mScreenModes) == 0) {
            //not exist, find the right one
            if (mode > mScreenModes) {
                mode = 1;
            }
            while((mode & mScreenModes) == 0) {
                mode <<= 1;
                if (mode > mScreenModes) {
                    throw new RuntimeException("wrong screen mode = " + mScreenModes);
                }
            }
        }
        if (LOG) MtkLog.v(TAG, "getNextScreenMode() = " + mode);
        return mode;
    }
    
    public void setScreenMode(int curScreenMode) {
        if (LOG) MtkLog.v(TAG, "setScreenMode(" + curScreenMode + ")");
        screenMode = curScreenMode;
        updateScreenModeDrawable();
    }
    
    /**
     * Enable specified screen mode list. 
     * The screen mode's value determines the order of being shown. 
     * <br>you can enable three screen modes by setting screenModes = 
     * {@link MTKVideoView#SCREENMODE_BIGSCREEN} | 
     * {@link MTKVideoView#SCREENMODE_FULLSCREEN} |
     * {@link MTKVideoView#SCREENMODE_CROPSCREEN} or 
     * just enable two screen modes by setting screenModes = 
     * {@link MTKVideoView#SCREENMODE_BIGSCREEN} | 
     * {@link MTKVideoView#SCREENMODE_CROPSCREEN}.
     * <br>If current screen mode is the last one of the ordered list, 
     * then the next screen mode will be the first one of the ordered list.
     * @param screenModes enabled screen mode list.
     */
    public void setScreenModes(int screenModes) {
        mScreenModes = (MTKVideoView.SCREENMODE_BIGSCREEN & screenModes)
            | (MTKVideoView.SCREENMODE_FULLSCREEN & screenModes)
            | (MTKVideoView.SCREENMODE_CROPSCREEN & screenModes);
        if ((screenModes & SCREENMODE_ALL) == 0) {
            mScreenModes = SCREENMODE_ALL;
            MtkLog.w(TAG, "wrong screenModes=" + screenModes + ". use default value " + SCREENMODE_ALL);
        }
        if (LOG) MtkLog.v(TAG, "enableScreenMode(" + screenModes + ") mScreenModes=" + mScreenModes);
    }
    
    /**
     * Get the all screen modes of media controller.
     * <br><b>Note:</b> it is not the video's current screen mode.
     * @return the current screen modes.
     */
    public int getScreenModes() {
        return mScreenModes;
    }
    
    public int getScreenMode() {
        if (LOG) MtkLog.v(TAG, "getScreenMode() return " + screenMode);
        return screenMode;
    }
    
    //for pause feature
    private boolean canPause = true;
    private boolean enableScrubbing = false;
    @Override
    public void setCanPause(boolean canPause) {
        this.canPause = canPause;
        if (LOG) MtkLog.v(TAG, "setCanPause(" + canPause + ")");
    }
    
    public void setCanScrubbing(boolean enable) {
        enableScrubbing = enable;
        timeBar.setScrubbing(enable);
        if (LOG) MtkLog.v(TAG, "setCanScrubbing(" + enable + ")");
    }
    
    //for only audio feature
    private boolean alwaysShowBottom;
    public void setBottomPanel(boolean alwaysShow) {
        alwaysShowBottom = alwaysShow;
        if (LOG) MtkLog.v(TAG, "setBottomPanel(" + alwaysShow + ")");
    }
    
    //for only audio case
    private void setAudioBackground(boolean show) {
        if (show) {
            this.setBackgroundResource(R.drawable.media_default_bkg);
        } else {
            this.setBackgroundDrawable(null);
        }
        if (LOG) MtkLog.v(TAG, "setAudioBackground(" + show + ")");
    }
}
