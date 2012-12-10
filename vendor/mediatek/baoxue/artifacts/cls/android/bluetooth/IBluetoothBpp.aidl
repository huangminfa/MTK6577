package android.bluetooth;

import android.bluetooth.BluetoothDevice;

/** {@hide}
 */
interface IBluetoothBpp {
    int getState();
    BluetoothDevice getConnectedDevice();
    boolean disconnect(in BluetoothDevice device);
}
