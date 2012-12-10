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

package android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetoothSimapCallback;

/**
 * System private API for Bluetooth simap service
 *
 * {@hide}
 */
interface IBluetoothSimap {
    boolean enableService();
    void disableService();
    int getState();
    BluetoothDevice getClient();
    boolean connect(in BluetoothDevice device);
    void disconnect();
    boolean isConnected(in BluetoothDevice device);
    boolean selectSIM(in int simIndex);
    int getSIMIndex();
    
    void registerCallback(IBluetoothSimapCallback cb);
    void unregisterCallback(IBluetoothSimapCallback cb);
    
		boolean isServiceStarted();
		void startSimapService();
}
