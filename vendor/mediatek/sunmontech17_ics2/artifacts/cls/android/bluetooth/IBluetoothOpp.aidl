package android.bluetooth;

import android.bluetooth.BluetoothDevice;

/** {@hide}
 */
interface IBluetoothOpp {

	int getState();
	BluetoothDevice getConnectedDevice();
	void disconnect(in BluetoothDevice device);
}
