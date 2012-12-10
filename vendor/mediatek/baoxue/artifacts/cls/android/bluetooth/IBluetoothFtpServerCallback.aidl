package android.bluetooth;

import android.os.Bundle;

/** {@hide}
 */
oneway interface IBluetoothFtpServerCallback {
    /** {@hide}
     */
    void postEvent(int event, in Bundle data);
}
