package android.bluetooth;

import android.bluetooth.BluetoothDevice;


/** {@hide}
 */
interface IBluetoothPan {

    boolean isTetheringOn();
    void setBluetoothTethering(boolean value);
    int getState(in BluetoothDevice device);
    List<BluetoothDevice> getConnectedDevices();
    List<BluetoothDevice> getDevicesMatchingConnectionStates(in int[] states);
    void connect(in BluetoothDevice device);
    void disconnect(in BluetoothDevice device);
}

