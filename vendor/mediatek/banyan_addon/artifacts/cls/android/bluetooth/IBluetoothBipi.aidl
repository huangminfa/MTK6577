package android.bluetooth;

import android.bluetooth.BluetoothDevice;

/** {@hide}
 */
interface IBluetoothBipi {
    int getState();
    BluetoothDevice getConnectedDevice();
    boolean disconnect(in BluetoothDevice device);
}
