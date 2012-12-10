package android.bluetooth;

import android.os.Bundle;

/** {@hide}
 */
oneway interface IBluetoothSimapCallback {
    /** {@hide}
     */
    void postEvent(int event, in Bundle data);
}
