package com.mediatek.agps;

import com.mediatek.agps.MtkAgpsProfile;
import com.mediatek.agps.MtkAgpsCdmaProfile;
import com.mediatek.agps.MtkAgpsConfig;
import android.os.Bundle;

interface IMtkAgpsManager {

    //=============version 2 API ==================
    void enable();
    void disable();
    boolean getStatus();
    
    void setConfig(in MtkAgpsConfig c);
    MtkAgpsConfig getConfig();
    
    void setProfile(in MtkAgpsProfile p);
    void setCdmaProfile(in MtkAgpsCdmaProfile p);
    MtkAgpsProfile getProfile();
    MtkAgpsCdmaProfile getCdmaProfile();
    
    void setMode(int mode);
    int getMode();
    
    void setNiEnable(boolean enable);
    void setUpEnable(boolean enable);
    void setCpEnable(boolean enable);
    void setRoamingEnable(boolean enable);
    
    boolean getNiStatus();
    boolean getUpStatus();
    boolean getCpStatus();
    boolean getRoamingStatus();
    
    int extraCommand(String command, in Bundle extra);

    void log2file(boolean enable);
    void log2uart(boolean enable);
    void supl2file(boolean enable);
    
    void niUserResponse(int response);
    
}
