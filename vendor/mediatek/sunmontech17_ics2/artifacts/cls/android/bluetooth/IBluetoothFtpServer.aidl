package android.bluetooth;

import android.bluetooth.IBluetoothFtpServerCallback;

/** {@hide} 
 * Interface for binding to BluetoothFtpService.
 */
interface IBluetoothFtpServer {

    /** {@hide} 
     */
    String getName();

    /** {@hide} 
     * register the callback interface
     */
    void registerCallback(IBluetoothFtpServerCallback cb);

    /** {@hide} 
     *  unregister the callback interface
     */
    void unregisterCallback(IBluetoothFtpServerCallback cb);

    /** {@hide} 
     * enable FTP server
     */
    boolean enable();

    /** {@hide} 
     * disable FTP server
     */
    void disable();

    /** {@hide} 
     * get status of FTP server
     */
    int getStatus();

    /** {@hide} 
     * set access-permission of FTP server
     */
    boolean setPermission(int permission);

    /** {@hide} 
     * get access-permission of FTP server
     */
    int getPermission();

    /** {@hide} 
     * set root directory of FTP server
     */
    boolean setRootDir(String rootDir);

}

