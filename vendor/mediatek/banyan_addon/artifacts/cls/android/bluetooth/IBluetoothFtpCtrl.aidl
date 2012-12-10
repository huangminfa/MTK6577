package android.bluetooth;

import android.bluetooth.BluetoothDevice;

/** {@hide}
 */
interface IBluetoothFtpCtrl {
    int getState();
    BluetoothDevice getCurrentDevice();
    void connect(in BluetoothDevice device);
    void disconnect(in BluetoothDevice device);
}
