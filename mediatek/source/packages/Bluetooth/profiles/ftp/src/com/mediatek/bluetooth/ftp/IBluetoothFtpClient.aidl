package com.mediatek.bluetooth.ftp;

import com.mediatek.bluetooth.ftp.IBluetoothFtpClientCallback;

interface IBluetoothFtpClient {
    void registerCallback(in IBluetoothFtpClientCallback cb);
    void unregisterCallback(in IBluetoothFtpClientCallback cb);

    /* Get current status of FTP client from Service (BluetoothFtpService) */
    int getState();

    /* Get current browsing path of FTP client */
    String getCurrentPath();

    /* Get the result of last transferring, used when client was recycled */
    int getLastTransferResult();

    /* Connect to FTP servcer */
    void connect();

    /* Abort current pulling or pushing operation */
    boolean abort();

    /* Disconnect from FTP servcer */
    void disconnect();

    /* Request Service to pull folder-listing-object from server */
    void refresh();

    /* Request to change path forward, backward, or to root folder */
    void goForward(in String path);
    void goBackward();
    void goToRoot();

    /* Request to create a new folder */
    void createFolder(in String name);

    /* Request Service to start pulling the file(s) in Provider (BluetoothFtpProvider) */
    void startPull();

    /* Request Service to start pushing the file(s) in Provider (BluetoothFtpProvider) */
    void startPush();

    /* Request to delete a file */
    void delete(String name);
}
