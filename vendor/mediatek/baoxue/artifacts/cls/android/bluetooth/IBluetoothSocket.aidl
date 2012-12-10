/*
 * Mediatek Inc.
 * Bluetooth Socket Interface AIDL file
 */

package android.bluetooth;

//import android.bluetooth.BluetoothSocket;

/**
 * System private API for Bluetooth Socket service
 *
 * {@hide}
 */
interface IBluetoothSocket {
    int initSocket(in int type, in boolean auth, in boolean encrypt, in int port);
    int connect(in int fdHandle, in String sAddr, in int channelNumber);
    int bindListen(in int fdHandle);
    int accept(in int timeout, in int fdHandle);
    int available(in int fdHandle);
    int read(inout byte[] b, in int offset, in int length, in int fdHandle);
    int write(in byte[] b, in int offset, in int length, in int fdHandle);
    int abort(in int fdHandle);
    int destroy(in int fdHandle);
    void throwErrno(int errno, in int fdHandle);
    String getAddr(in int fdHandle);
    int getRealServerChannel(in int channelOriginal);
}

