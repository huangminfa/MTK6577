
package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import com.mediatek.tvOut.*;
import com.mediatek.tvOut.TvOut;

public class TVOutReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        TvOut mTvOut = new TvOut();
        //System.out.println("TVOutReceiver");
        mTvOut.IPOPowerOff();

    }

}

