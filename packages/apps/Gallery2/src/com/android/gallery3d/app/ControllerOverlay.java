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

package com.android.gallery3d.app;

import com.android.gallery3d.app.MovieControllerOverlay.State;

import android.view.View;

public interface ControllerOverlay {

  interface Listener {
    void onPlayPause();
    void onSeekStart();
    void onSeekMove(int time);
    void onSeekEnd(int time);
    void onShown();
    void onHidden();
    void onReplay();
    //for screen mode event
    void OnScreenModeChanged(int newMode);
  }

  void setListener(Listener listener);

  void setCanReplay(boolean canReplay);

  /**
   * @return The overlay view that should be added to the player.
   */
  View getView();

  void show();

  void showPlaying();

  void showPaused();

  void showEnded();

  void showLoading();

  void showErrorMessage(String message);

  void hide();

  void setTimes(int currentTime, int totalTime);

  void resetTime();

  //show buffering
  void showBuffering(boolean fullBuffer, int percent);
  void clearBuffering();
  //show re-connecting
  void showReconnecting(int times);
  //show re-connecting error
  void showReconnectingError();
  //init or update screen mode
  void setScreenMode(int curScreenMode);
  //for common text
  void setPlayingInfo(boolean liveStreaming);
  //for can pause feature
  void setCanPause(boolean canPause);
  //for scrubbing feature
  void setCanScrubbing(boolean enable);
  //for only audio feature
  void setBottomPanel(boolean alwaysShow);
  //for stop feature
  State getState();
  //for stereo feature
  void displayTimeBar(boolean display);
}
