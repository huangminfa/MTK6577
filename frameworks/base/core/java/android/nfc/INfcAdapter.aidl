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

package android.nfc;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TechListParcel;
import android.nfc.INdefPushCallback;
import android.nfc.INfcAdapterExtras;
import android.nfc.INfcTag;

/**
 * @hide
 */
interface INfcAdapter
{
    INfcTag getNfcTagInterface();
    INfcAdapterExtras getNfcAdapterExtrasInterface(in String pkg);

    int getState();
    boolean disable();
    boolean enable();
    boolean enableNdefPush();
    boolean disableNdefPush();
    boolean isNdefPushEnabled();

    void setForegroundDispatch(in PendingIntent intent,
            in IntentFilter[] filters, in TechListParcel techLists);
    void setForegroundNdefPush(in NdefMessage msg, in INdefPushCallback callback);

	// ----- MTK begins ----- //
    int getCardEmulationConfig();
    boolean setCardEmulationConfig(in int config);
	boolean isP2pRecvEnabled();
	boolean enableP2pRecv();
	boolean disableP2pRecv();		
	boolean isTagRwEnabled();
	boolean enableTagRw();
	boolean disableTagRw();	
	boolean isAirplaneModeAutoSwitchEnabled();
	boolean enableAirplaneModeAutoSwitch();
	boolean disableAirplaneModeAutoSwitch();
	boolean isP2pBitrate106Enabled();
	boolean enableP2pBitrate106();
	boolean disableP2pBitrate106();
	boolean isVibrationEnabled();
	boolean enableVibration();
	boolean disableVibration();
	boolean isSoundEnabled();
	boolean enableSound();
	boolean disableSound();
	boolean isPromptEnabled();
	boolean enablePrompt();
	boolean disablePrompt();
	// ----- MTK ends ----- //
}
