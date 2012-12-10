package android.bluetooth;

import android.bluetooth.BluetoothDevice;


/** {@hide}
 */
interface IBluetoothHid {
    int getState(in BluetoothDevice device);
    BluetoothDevice[] getCurrentDevices();
    void connect(in BluetoothDevice device);
    void disconnect(in BluetoothDevice device);
}
