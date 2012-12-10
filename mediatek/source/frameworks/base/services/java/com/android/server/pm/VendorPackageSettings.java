package com.android.server.pm;

final class VendorPackageSetting {
      
    final String mPackageName;
    boolean mIntallStatus = true;
    
    VendorPackageSetting(String packageName) {
        this.mPackageName = packageName;
    }
    
    VendorPackageSetting(String packageName, boolean intallStatus) {
        this.mPackageName = packageName;
        this.mIntallStatus = intallStatus;
    }

    boolean getIntallStatus() {
        return mIntallStatus;
    }

    void setIntallStatus(boolean intallStatus) {
        this.mIntallStatus = intallStatus;
    }

    String getPackageName() {
        return mPackageName;
    }
}
