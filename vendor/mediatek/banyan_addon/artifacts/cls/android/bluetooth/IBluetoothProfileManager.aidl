package android.bluetooth;
import android.bluetooth.BluetoothDevice;

interface IBluetoothProfileManager{
	boolean connect(in String profile, in BluetoothDevice device);
  	boolean disconnect(in String profile, in BluetoothDevice device);
	BluetoothDevice[] getConnectedDevices(in String profile);  // change to Set<> once AIDL supports
  	int getState(in String profile, in BluetoothDevice device);
	boolean isPreferred(in String profile, in BluetoothDevice device);
	boolean setPreferred(in String profile, in BluetoothDevice device, boolean preferred);
 	int getPreferred(in String profile, in BluetoothDevice device);
}
