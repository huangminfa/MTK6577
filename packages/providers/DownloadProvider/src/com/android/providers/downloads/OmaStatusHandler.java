//package com.mediatek.omadownload;
package com.android.providers.downloads;

class OmaStatusHandler {
    
    //Installation status code
    static final int ATTRIBUTE_MISMATCH = 905;
    static final int DEVICE_ABORTED = 952;
    static final int INSUFFICIENT_MEMORY = 901;
    static final int INVALID_DDVERSION = 951;
    static final int INVALID_DESCRIPTOR = 906;
    static final int LOADER_ERROR = 954;
    static final int LOSS_OF_SERVICE = 903;
    static final int NON_ACCEPTABLE_CONTENT = 953;
    static final int SUCCESS = 900;
    static final int USER_CANCELLED = 902;
    
    //InstallNotify status code
    static final int DISCARD = 0;
    static final int READY = 1;
    
    //Maximum number of times to retry a request
    static final int MAXIMUM_RETRY = 3;
    
    protected static String statusCodeToString(int code) {
        String s = null;
        if (code == ATTRIBUTE_MISMATCH) {
            s = "905 Attribute mismatch";
        } else if (code == DEVICE_ABORTED) {
            s = "952 Device aborted";
        } else if (code == INSUFFICIENT_MEMORY) {
            s = "901 Insufficient memory";
        } else if (code == INVALID_DDVERSION) {
            s = "951 Invalid DDVersion";
        } else if (code == INVALID_DESCRIPTOR) {
            s = "906 Invalid descriptor";
        } else if (code == LOADER_ERROR) {
            s = "954 Loader Error";
        } else if (code == LOSS_OF_SERVICE) {
            s = "903 Loss of Service";
        } else if (code == NON_ACCEPTABLE_CONTENT) {
            s = "953 Non-Acceptable Content";
        } else if (code == SUCCESS) {
            s = "900 Success";
        } else if (code == USER_CANCELLED) {
            s = "902 User Cancelled";
        } else {}       
        return s;
    }
}