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

import com.android.systemui.R;

public class TelephonyIconsGemini {
    //***** Signal strength icons

    //GSM/UMTS
    private static final int[][] TELEPHONY_SIGNAL_STRENGTH = {
        { R.drawable.zzz_stat_sys_signal_0,
          R.drawable.zzz_stat_sys_signal_1_blue,
          R.drawable.zzz_stat_sys_signal_2_blue,
          R.drawable.zzz_stat_sys_signal_3_blue,
          R.drawable.zzz_stat_sys_signal_4_blue },
        { R.drawable.zzz_stat_sys_signal_0,
          R.drawable.zzz_stat_sys_signal_1_orange,
          R.drawable.zzz_stat_sys_signal_2_orange,
          R.drawable.zzz_stat_sys_signal_3_orange,
          R.drawable.zzz_stat_sys_signal_4_orange },
        { R.drawable.zzz_stat_sys_signal_0,
          R.drawable.zzz_stat_sys_signal_1_green,
          R.drawable.zzz_stat_sys_signal_2_green,
          R.drawable.zzz_stat_sys_signal_3_green,
          R.drawable.zzz_stat_sys_signal_4_green },
        { R.drawable.zzz_stat_sys_signal_0,
          R.drawable.zzz_stat_sys_signal_1_purple,
          R.drawable.zzz_stat_sys_signal_2_purple,
          R.drawable.zzz_stat_sys_signal_3_purple,
          R.drawable.zzz_stat_sys_signal_4_purple }
    };

    //GSM/UMTS for CMCC
    private static final int[][] TELEPHONY_CMCC_SIGNAL_STRENGTH = {
        { R.drawable.zzz_stat_sys_signal_0,
          R.drawable.zzz_stat_sys_signal_1_blue,
          R.drawable.zzz_stat_sys_signal_2_blue,
          R.drawable.zzz_stat_sys_signal_3_blue,
          R.drawable.zzz_stat_sys_signal_4_blue,
          R.drawable.zzz_stat_sys_signal_5_blue },
        { R.drawable.zzz_stat_sys_signal_0,
          R.drawable.zzz_stat_sys_signal_1_orange,
          R.drawable.zzz_stat_sys_signal_2_orange,
          R.drawable.zzz_stat_sys_signal_3_orange,
          R.drawable.zzz_stat_sys_signal_4_orange,
          R.drawable.zzz_stat_sys_signal_5_orange },
        { R.drawable.zzz_stat_sys_signal_0,
          R.drawable.zzz_stat_sys_signal_1_green,
          R.drawable.zzz_stat_sys_signal_2_green,
          R.drawable.zzz_stat_sys_signal_3_green,
          R.drawable.zzz_stat_sys_signal_4_green,
          R.drawable.zzz_stat_sys_signal_5_green },
        { R.drawable.zzz_stat_sys_signal_0,
          R.drawable.zzz_stat_sys_signal_1_purple,
          R.drawable.zzz_stat_sys_signal_2_purple,
          R.drawable.zzz_stat_sys_signal_3_purple,
          R.drawable.zzz_stat_sys_signal_4_purple,
          R.drawable.zzz_stat_sys_signal_5_purple }
    };

    public static int[] getTelephonySignalStrengthIconList(int simColorId) {
        return TELEPHONY_SIGNAL_STRENGTH[simColorId];
    }

    public static int[] getTelephonyCMCCSignalStrengthIconList(int simColorId) {
        return TELEPHONY_CMCC_SIGNAL_STRENGTH[simColorId];
    }
    public static int[] getTelephonySignalStrengthIconList(int simColorId,int num) {
    	if(num == 0)
    		return TELEPHONY_SIGNAL_STRENGTH_UP[simColorId];
    	else if(num == 1)
    		return TELEPHONY_SIGNAL_STRENGTH_DOWN[simColorId];
    	else
    		return null;
    }

    private static final int[][] TELEPHONY_SIGNAL_STRENGTH_UP = {
        { R.drawable.zzz_stat_sys_signal_up_0,
          R.drawable.zzz_stat_sys_signal_up_1_blue,
          R.drawable.zzz_stat_sys_signal_up_2_blue,
          R.drawable.zzz_stat_sys_signal_up_3_blue,
          R.drawable.zzz_stat_sys_signal_up_4_blue },
        { R.drawable.zzz_stat_sys_signal_up_0,
          R.drawable.zzz_stat_sys_signal_up_1_orange,
          R.drawable.zzz_stat_sys_signal_up_2_orange,
          R.drawable.zzz_stat_sys_signal_up_3_orange,
          R.drawable.zzz_stat_sys_signal_up_4_orange },
        { R.drawable.zzz_stat_sys_signal_up_0,
          R.drawable.zzz_stat_sys_signal_up_1_green,
          R.drawable.zzz_stat_sys_signal_up_2_green,
          R.drawable.zzz_stat_sys_signal_up_3_green,
          R.drawable.zzz_stat_sys_signal_up_4_green },
        { R.drawable.zzz_stat_sys_signal_up_0,
          R.drawable.zzz_stat_sys_signal_up_1_purple,
          R.drawable.zzz_stat_sys_signal_up_2_purple,
          R.drawable.zzz_stat_sys_signal_up_3_purple,
          R.drawable.zzz_stat_sys_signal_up_4_purple }
    };
    private static final int[][] TELEPHONY_SIGNAL_STRENGTH_DOWN = {
        { R.drawable.zzz_stat_sys_signal_down_0,
          R.drawable.zzz_stat_sys_signal_down_1_blue,
          R.drawable.zzz_stat_sys_signal_down_2_blue,
          R.drawable.zzz_stat_sys_signal_down_3_blue,
          R.drawable.zzz_stat_sys_signal_down_4_blue },
        { R.drawable.zzz_stat_sys_signal_down_0,
          R.drawable.zzz_stat_sys_signal_down_1_orange,
          R.drawable.zzz_stat_sys_signal_down_2_orange,
          R.drawable.zzz_stat_sys_signal_down_3_orange,
          R.drawable.zzz_stat_sys_signal_down_4_orange },
        { R.drawable.zzz_stat_sys_signal_down_0,
          R.drawable.zzz_stat_sys_signal_down_1_green,
          R.drawable.zzz_stat_sys_signal_down_2_green,
          R.drawable.zzz_stat_sys_signal_down_3_green,
          R.drawable.zzz_stat_sys_signal_down_4_green },
        { R.drawable.zzz_stat_sys_signal_down_0,
          R.drawable.zzz_stat_sys_signal_down_1_purple,
          R.drawable.zzz_stat_sys_signal_down_2_purple,
          R.drawable.zzz_stat_sys_signal_down_3_purple,
          R.drawable.zzz_stat_sys_signal_down_4_purple }
    };
    //***** Network type icons
    public static final int[] NETWORKTYE_G = {
        R.drawable.zzz_stat_sys_signal_g_blue,
        R.drawable.zzz_stat_sys_signal_g_orange,
        R.drawable.zzz_stat_sys_signal_g_green,
        R.drawable.zzz_stat_sys_signal_g_purple
    };
    
    public static final int[] NETWORKTYE_3G = {
        R.drawable.zzz_stat_sys_signal_3g_blue,
        R.drawable.zzz_stat_sys_signal_3g_orange,
        R.drawable.zzz_stat_sys_signal_3g_green,
        R.drawable.zzz_stat_sys_signal_3g_purple
    };
    public static final int[] NETWORKTYE_1X = {
        R.drawable.zzz_stat_sys_signal_1x_blue,
        R.drawable.zzz_stat_sys_signal_1x_orange,
        R.drawable.zzz_stat_sys_signal_1x_green,
        R.drawable.zzz_stat_sys_signal_1x_purple
    };
    
    public static final int[] NETWORKTYE_1X_3G = {
        R.drawable.zzz_stat_sys_signal_1x_3g_blue,
        R.drawable.zzz_stat_sys_signal_1x_3g_orange,
        R.drawable.zzz_stat_sys_signal_1x_3g_green,
        R.drawable.zzz_stat_sys_signal_1x_3g_purple
    };
    
    
    //***** Data connection icons

    //GSM/UMTS
    static final int[] DATA_G = {
        R.drawable.zzz_stat_sys_data_fully_connected_g_blue,
        R.drawable.zzz_stat_sys_data_fully_connected_g_orange,
        R.drawable.zzz_stat_sys_data_fully_connected_g_green,
        R.drawable.zzz_stat_sys_data_fully_connected_g_purple
    };

    static final int[] DATA_3G = {
        R.drawable.zzz_stat_sys_data_fully_connected_3g_blue,
        R.drawable.zzz_stat_sys_data_fully_connected_3g_orange,
        R.drawable.zzz_stat_sys_data_fully_connected_3g_green,
        R.drawable.zzz_stat_sys_data_fully_connected_3g_purple
    };

    static final int[] DATA_T = {
        R.drawable.zzz_stat_sys_data_fully_connected_t_blue,
        R.drawable.zzz_stat_sys_data_fully_connected_t_orange,
        R.drawable.zzz_stat_sys_data_fully_connected_t_green,
        R.drawable.zzz_stat_sys_data_fully_connected_t_purple
    };

    static final int[] DATA_E = {
        R.drawable.zzz_stat_sys_data_fully_connected_e_blue,
        R.drawable.zzz_stat_sys_data_fully_connected_e_orange,
        R.drawable.zzz_stat_sys_data_fully_connected_e_green,
        R.drawable.zzz_stat_sys_data_fully_connected_e_purple
    };

    //3.5G
    static final int[] DATA_H = {
        R.drawable.zzz_stat_sys_data_fully_connected_h_blue,
        R.drawable.zzz_stat_sys_data_fully_connected_h_orange,
        R.drawable.zzz_stat_sys_data_fully_connected_h_green,
        R.drawable.zzz_stat_sys_data_fully_connected_h_purple
    };

    //CDMA
    // Use 3G icons for EVDO data and 1x icons for 1XRTT data
    static final int[] DATA_1X = {
        R.drawable.zzz_stat_sys_data_fully_connected_1x_blue,
        R.drawable.zzz_stat_sys_data_fully_connected_1x_orange,
        R.drawable.zzz_stat_sys_data_fully_connected_1x_green,
        R.drawable.zzz_stat_sys_data_fully_connected_1x_purple
    };

    // LTE and eHRPD
    static final int[] DATA_4G = {
        R.drawable.zzz_stat_sys_data_fully_connected_4g_blue,
        R.drawable.zzz_stat_sys_data_fully_connected_4g_orange,
        R.drawable.zzz_stat_sys_data_fully_connected_4g_green,
        R.drawable.zzz_stat_sys_data_fully_connected_4g_purple
    };

    //***** Roaming icons

    static final int[] ROAMING = {
        R.drawable.zzz_stat_sys_data_connected_roam_blue,
        R.drawable.zzz_stat_sys_data_connected_roam_orange,
        R.drawable.zzz_stat_sys_data_connected_roam_green,
        R.drawable.zzz_stat_sys_data_connected_roam_purple
    };

    //Support CT Icon start
    

    //GSM/UMTS
    static final int[] DATA_G_CT = {
        R.drawable.zzz_stat_sys_data_fully_connected_g_blue_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_g_orange_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_g_green_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_g_purple_ct
    };

    static final int[] DATA_3G_CT = {
        R.drawable.zzz_stat_sys_data_fully_connected_3g_blue_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_3g_orange_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_3g_green_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_3g_purple_ct
    };

    static final int[] DATA_T_CT = {
        R.drawable.zzz_stat_sys_data_fully_connected_t_blue_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_t_orange_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_t_green_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_t_purple_ct
    };

    static final int[] DATA_E_CT = {
        R.drawable.zzz_stat_sys_data_fully_connected_e_blue_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_e_orange_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_e_green_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_e_purple_ct
    };

    //3.5G
    static final int[] DATA_H_CT = {
        R.drawable.zzz_stat_sys_data_fully_connected_h_blue_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_h_orange_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_h_green_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_h_purple_ct
    };

    //CDMA
    // Use 3G icons for EVDO data and 1x icons for 1XRTT data
    static final int[] DATA_1X_CT = {
        R.drawable.zzz_stat_sys_data_fully_connected_1x_blue_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_1x_orange_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_1x_green_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_1x_purple_ct
    };

    // LTE and eHRPD
    static final int[] DATA_4G_CT = {
        R.drawable.zzz_stat_sys_data_fully_connected_4g_blue_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_4g_orange_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_4g_green_ct,
        R.drawable.zzz_stat_sys_data_fully_connected_4g_purple_ct
    };

    //***** Roaming icons

    static final int[] ROAMING_CT = {
        R.drawable.zzz_stat_sys_data_connected_roam_blue_ct,
        R.drawable.zzz_stat_sys_data_connected_roam_orange_ct,
        R.drawable.zzz_stat_sys_data_connected_roam_green_ct,
        R.drawable.zzz_stat_sys_data_connected_roam_purple_ct
    };
    //Support CT Icon End
    //***** Flight Mode icons

    static final int[] FLIGHT_MODE = {
        R.drawable.zzz_stat_sys_signal_flightmode_blue,
        R.drawable.zzz_stat_sys_signal_flightmode_orange,
        R.drawable.zzz_stat_sys_signal_flightmode_green,
        R.drawable.zzz_stat_sys_signal_flightmode_purple
    };
}