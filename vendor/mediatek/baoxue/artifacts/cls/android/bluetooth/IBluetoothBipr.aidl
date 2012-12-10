package android.bluetooth;

import android.bluetooth.BluetoothDevice;

/** {@hide}
 */
interface IBluetoothBipr {
    int getState();
    BluetoothDevice getConnectedDevice();
    boolean disconnect(in BluetoothDevice device);
}
