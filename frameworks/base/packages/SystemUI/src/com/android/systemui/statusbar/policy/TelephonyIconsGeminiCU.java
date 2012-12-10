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

package com.android.systemui.statusbar.policy;

import com.android.internal.telephony.Phone;
import com.android.systemui.R;

// [SystemUI] Support "Dual SIM".
class TelephonyIconsGeminiCU {
    //***** Signal strength icons

    //GSM/UMTS
    static final int[][] TELEPHONY_SIGNAL_STRENGTH_SIM1 = {
        { R.drawable.zzz_stat_sys_signal_0_sim1,
          R.drawable.zzz_stat_sys_signal_1_sim1,
          R.drawable.zzz_stat_sys_signal_2_sim1,
          R.drawable.zzz_stat_sys_signal_3_sim1,
          R.drawable.zzz_stat_sys_signal_4_sim1 },
        { R.drawable.zzz_stat_sys_signal_0_fully_sim1,
          R.drawable.zzz_stat_sys_signal_1_fully_sim1,
          R.drawable.zzz_stat_sys_signal_2_fully_sim1,
          R.drawable.zzz_stat_sys_signal_3_fully_sim1,
          R.drawable.zzz_stat_sys_signal_4_fully_sim1 }
    };
    static final int[][] TELEPHONY_SIGNAL_STRENGTH_SIM2 = {
        { R.drawable.zzz_stat_sys_signal_0_sim2,
          R.drawable.zzz_stat_sys_signal_1_sim2,
          R.drawable.zzz_stat_sys_signal_2_sim2,
          R.drawable.zzz_stat_sys_signal_3_sim2,
          R.drawable.zzz_stat_sys_signal_4_sim2 },
        { R.drawable.zzz_stat_sys_signal_0_fully_sim2,
          R.drawable.zzz_stat_sys_signal_1_fully_sim2,
          R.drawable.zzz_stat_sys_signal_2_fully_sim2,
          R.drawable.zzz_stat_sys_signal_3_fully_sim2,
          R.drawable.zzz_stat_sys_signal_4_fully_sim2 }
    };

    static final int[][] TELEPHONY_SIGNAL_STRENGTH_ROAMING_SIM1 = {
        { R.drawable.zzz_stat_sys_r_signal_0_sim1,
          R.drawable.zzz_stat_sys_r_signal_1_sim1,
          R.drawable.zzz_stat_sys_r_signal_2_sim1,
          R.drawable.zzz_stat_sys_r_signal_3_sim1,
          R.drawable.zzz_stat_sys_r_signal_4_sim1 },
        { R.drawable.zzz_stat_sys_r_signal_0_fully_sim1,
          R.drawable.zzz_stat_sys_r_signal_1_fully_sim1,
          R.drawable.zzz_stat_sys_r_signal_2_fully_sim1,
          R.drawable.zzz_stat_sys_r_signal_3_fully_sim1,
          R.drawable.zzz_stat_sys_r_signal_4_fully_sim1 }
    };
    static final int[][] TELEPHONY_SIGNAL_STRENGTH_ROAMING_SIM2 = {
        { R.drawable.zzz_stat_sys_r_signal_0_sim2,
          R.drawable.zzz_stat_sys_r_signal_1_sim2,
          R.drawable.zzz_stat_sys_r_signal_2_sim2,
          R.drawable.zzz_stat_sys_r_signal_3_sim2,
          R.drawable.zzz_stat_sys_r_signal_4_sim2 },
        { R.drawable.zzz_stat_sys_r_signal_0_fully_sim2,
          R.drawable.zzz_stat_sys_r_signal_1_fully_sim2,
          R.drawable.zzz_stat_sys_r_signal_2_fully_sim2,
          R.drawable.zzz_stat_sys_r_signal_3_fully_sim2,
          R.drawable.zzz_stat_sys_r_signal_4_fully_sim2 }
    };

    static final int[][] DATA_SIGNAL_STRENGTH = TELEPHONY_SIGNAL_STRENGTH_SIM1;

    public static int[] getTelephonySignalStrengthIconList(int slotId, boolean isRoaming, int mInetCondition) {
        if (isRoaming) {
            if (slotId == Phone.GEMINI_SIM_1) {
                return TELEPHONY_SIGNAL_STRENGTH_ROAMING_SIM1[mInetCondition];
            } else {
                return TELEPHONY_SIGNAL_STRENGTH_ROAMING_SIM2[mInetCondition];
            }
        } else {
            if (slotId == Phone.GEMINI_SIM_1) {
                return TELEPHONY_SIGNAL_STRENGTH_SIM1[mInetCondition];
            } else {
                return TELEPHONY_SIGNAL_STRENGTH_SIM2[mInetCondition];
            }
        }
    }

    //***** Data connection icons

    //GSM/UMTS
    static final int[][] DATA_G = {
            { R.drawable.zzz_stat_sys_data_connected_g,
              R.drawable.zzz_stat_sys_data_connected_g,
              R.drawable.zzz_stat_sys_data_connected_g,
              R.drawable.zzz_stat_sys_data_connected_g },
            { R.drawable.zzz_stat_sys_data_fully_connected_g,
              R.drawable.zzz_stat_sys_data_fully_connected_g,
              R.drawable.zzz_stat_sys_data_fully_connected_g,
              R.drawable.zzz_stat_sys_data_fully_connected_g }
        };

    static final int[][] DATA_3G = {
            { R.drawable.zzz_stat_sys_data_connected_3g,
              R.drawable.zzz_stat_sys_data_connected_3g,
              R.drawable.zzz_stat_sys_data_connected_3g,
              R.drawable.zzz_stat_sys_data_connected_3g },
            { R.drawable.zzz_stat_sys_data_fully_connected_3g,
              R.drawable.zzz_stat_sys_data_fully_connected_3g,
              R.drawable.zzz_stat_sys_data_fully_connected_3g,
              R.drawable.zzz_stat_sys_data_fully_connected_3g }
        };

    static final int[][] DATA_E = {
            { R.drawable.zzz_stat_sys_data_connected_e,
              R.drawable.zzz_stat_sys_data_connected_e,
              R.drawable.zzz_stat_sys_data_connected_e,
              R.drawable.zzz_stat_sys_data_connected_e },
            { R.drawable.zzz_stat_sys_data_fully_connected_e,
              R.drawable.zzz_stat_sys_data_fully_connected_e,
              R.drawable.zzz_stat_sys_data_fully_connected_e,
              R.drawable.zzz_stat_sys_data_fully_connected_e }
        };

    //3.5G
    static final int[][] DATA_H = {
            { R.drawable.zzz_stat_sys_data_connected_h,
              R.drawable.zzz_stat_sys_data_connected_h,
              R.drawable.zzz_stat_sys_data_connected_h,
              R.drawable.zzz_stat_sys_data_connected_h },
            { R.drawable.zzz_stat_sys_data_fully_connected_h,
              R.drawable.zzz_stat_sys_data_fully_connected_h,
              R.drawable.zzz_stat_sys_data_fully_connected_h,
              R.drawable.zzz_stat_sys_data_fully_connected_h }
    };

    //CDMA
    // Use 3G icons for EVDO data and 1x icons for 1XRTT data
    static final int[][] DATA_1X = {
            { R.drawable.zzz_stat_sys_data_connected_1x,
              R.drawable.zzz_stat_sys_data_connected_1x,
              R.drawable.zzz_stat_sys_data_connected_1x,
              R.drawable.zzz_stat_sys_data_connected_1x },
            { R.drawable.zzz_stat_sys_data_fully_connected_1x,
              R.drawable.zzz_stat_sys_data_fully_connected_1x,
              R.drawable.zzz_stat_sys_data_fully_connected_1x,
              R.drawable.zzz_stat_sys_data_fully_connected_1x }
            };

    // LTE and eHRPD
    static final int[][] DATA_4G = {
            { R.drawable.zzz_stat_sys_data_connected_4g,
              R.drawable.zzz_stat_sys_data_connected_4g,
              R.drawable.zzz_stat_sys_data_connected_4g,
              R.drawable.zzz_stat_sys_data_connected_4g },
            { R.drawable.zzz_stat_sys_data_fully_connected_4g,
              R.drawable.zzz_stat_sys_data_fully_connected_4g,
              R.drawable.zzz_stat_sys_data_fully_connected_4g,
              R.drawable.zzz_stat_sys_data_fully_connected_4g }
        };


}

