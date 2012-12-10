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

package  com.android.pqtuningtool.photoeditor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.photoeditor.actions.EffectAction;
import  com.android.pqtuningtool.photoeditor.actions.EffectToolFactory;

/**
 * Effects bar that contains all effects and shows them in categorized views.
 */
public class EffectsBar extends LinearLayout {

    private final LayoutInflater inflater;
    private FilterStack filterStack;
    private EffectsMenu effectsMenu;
    private View effectsGallery;
    private ViewGroup effectToolPanel;
    private EffectAction activeEffect;

    public EffectsBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void initialize(FilterStack filterStack) {
        this.filterStack = filterStack;

        effectsMenu = (EffectsMenu) findViewById(R.id.effects_menu);
        effectsMenu.setOnToggleListener(new EffectsMenu.OnToggleListener() {

            @Override
            public boolean onToggle(boolean isSelected, final int effectsId) {
                // Create and show effects-gallery only if the clicked toggle isn't selected or it's
                // selected but showing an active effect instead of effects-gallery. Set the clicked
                // toggle selected only when its effects-gallery will be created and shown.
                boolean select = !isSelected || (effectsGallery == null);
                exit(select ? new Runnable() {

                    @Override
                    public void run() {
                        createEffectsGallery(effectsId);
                    }
                } : null);
                return select;
            }
        });
    }

    private void createEffectsGallery(int effectsId) {
        // Inflate scrollable effects-gallery and desired effects into effects-bar.
        effectsGallery = inflater.inflate(R.layout.photoeditor_effects_gallery, this, false);
        ViewGroup scrollView = (ViewGroup) effectsGallery.findViewById(R.id.scroll_view);
        ViewGroup effects = (ViewGroup) inflater.inflate(effectsId, scrollView, false);
        for (int i = 0; i < effects.getChildCount(); i++) {
            setupEffectListener((EffectAction) effects.getChildAt(i));
        }
        scrollView.addView(effects);
        scrollView.scrollTo(0, 0);
        addView(effectsGallery, 0);
    }

    private void setupEffectListener(final EffectAction effect) {
        effect.setListener(new EffectAction.Listener() {

            @Override
            public void onClick() {
                if (isEnabled()) {
                    // Set the clicked effect active before exiting effects-gallery.
                    activeEffect = effect;
                    exitEffectsGallery();
                    // Create effect tool panel first before the factory could create tools within.
                    createEffectToolPanel();
                    activeEffect.begin(filterStack,
                            new EffectToolFactory(effectToolPanel, inflater));
                }
            }

            @Override
            public void onDone() {
                exit(null);
            }
        });
    }

    private void createEffectToolPanel() {
        effectToolPanel = (ViewGroup) inflater.inflate(
                R.layout.photoeditor_effect_tool_panel, this, false);
        ((TextView) effectToolPanel.findViewById(R.id.effect_label)).setText(activeEffect.name());
        addView(effectToolPanel, 0);
    }

    private boolean exitEffectsGallery() {
        if (effectsGallery != null) {
            if (activeEffect != null) {
                // Detach the active effect to prevent it stopping effects-gallery from gc.
                ViewGroup scrollView = (ViewGroup) effectsGallery.findViewById(R.id.scroll_view);
                ((ViewGroup) scrollView.getChildAt(0)).removeView(activeEffect);
            }
            removeView(effectsGallery);
            effectsGallery = null;
            return true;
        }
        return false;
    }

    private boolean exitActiveEffect(final Runnable runnableOnDone) {
        if (activeEffect != null) {
            SpinnerProgressDialog.showDialog();
            activeEffect.end(new Runnable() {

                @Override
                public void run() {
                    SpinnerProgressDialog.dismissDialog();
                    View fullscreenTool = getRootView().findViewById(R.id.fullscreen_effect_tool);
                    if (fullscreenTool != null) {
                        ((ViewGroup) fullscreenTool.getParent()).removeView(fullscreenTool);
                    }
                    removeView(effectToolPanel);
                    effectToolPanel = null;
                    activeEffect = null;
                    if (runnableOnDone != null) {
                        runnableOnDone.run();
                    }
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Exits from effects gallery or the active effect; then executes the runnable if applicable.
     *
     * @return true if exiting from effects gallery or the active effect; otherwise, false.
     */
    public boolean exit(final Runnable runnableOnDone) {
        // Exit effects-menu selected states.
        effectsMenu.clearSelected();

        if (exitActiveEffect(runnableOnDone)) {
            return true;
        }

        boolean exited = exitEffectsGallery();
        if (runnableOnDone != null) {
            runnableOnDone.run();
        }
        return exited;
    }
}
