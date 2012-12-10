package com.android.settings.batterywarning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.mediatek.xlog.Xlog; 
import static com.android.settings.batterywarning.BatteryNotifyCodes.*;

public class ReadCodeTask extends TimerTask {
	private static final String XLOGTAG = "Settings/BW";
	private static final String TAG = "ReadCodeTask:";

    private String mFilePath;
    private Context mContext;
    private String mStringNotifyCode = null;
    private int mNotifyCode = 0;
    List<String> mActionList = new ArrayList<String>();
    Set<String> mActionSet = new HashSet<String>();
    private boolean DEBUG = false;

    
    public ReadCodeTask(Context context, String path){
        super();
        mContext = context;
        mFilePath = path;
    }
    
    @Override
    public void run() {
        //Xlog.i(XLOGTAG, TAG+"TimerTask: ReadCodeTask.run()");
        sendWarningMessage(mContext);

    }
    
    public void sendWarningMessage(Context context){
        
        //Xlog.i(XLOGTAG, TAG+"sendWarningMessage() called.");
        
        getNotifyCode(mFilePath);
        convertNotifyCode(mStringNotifyCode);
        getWarningMessage(mNotifyCode);
        
//        int actions = mActionSet.size();
        Intent mWarningMessageIntent = new Intent();
        Iterator it = mActionSet.iterator();
        while(it.hasNext()){
            String action = it.next().toString();
            mWarningMessageIntent.setAction(action);
            context.sendBroadcast(mWarningMessageIntent);
            Xlog.d(XLOGTAG, TAG+"sendBroadcast: " + action);
        }
    }
    
    
    public void getWarningMessage(int event){
		if(DEBUG) {
           Xlog.d(XLOGTAG, TAG+"getWarningMessage() called.Event: "+event);
		}
        Set<String> actionSet = new HashSet<String>();
        //over charger voltage
        if(CHARGER_OVER_VOLTAGE == (event & CHARGER_OVER_VOLTAGE)){
            actionSet.add(ACTION_CHARGER_OVER_VOLTAGE);
        }
        
        //over battery temperature
        if(BATTER_OVER_TEMPERATURE == (event & BATTER_OVER_TEMPERATURE)){
            actionSet.add(ACTION_BATTER_OVER_TEMPERATURE);
        }
        
        //over current-protection
        if(OVER_CURRENT_PROTECTION == (event & OVER_CURRENT_PROTECTION)){
            actionSet.add(ACTION_OVER_CURRENT_PROTECTION);
        }
        
        //over battery voltage
        if(BATTER_OVER_VOLTAGE == (event & BATTER_OVER_VOLTAGE)){
            actionSet.add(ACTION_BATTER_OVER_VOLTAGE);
        }
        
        //over 12hours, battery does not charge full
        if(SAFETY_TIMER_TIMEOUT == (event & SAFETY_TIMER_TIMEOUT)){
            actionSet.add(ACTION_SAFETY_TIMER_TIMEOUT);
        }
        
        /**
         * below branch just for test
         */
        if(DEBUG){

            if(0x1F == (event & 0xF)){
                actionSet.add("mtk.test.USB_CONNECTED0");
            }
            if(0x1F == (event & 0x1F)){
                actionSet.add("mtk.test.USB_CONNECTED1");
            }
            if(0x1B == (event & 0x1B)){
                actionSet.add("mtk.test.USB_CONNECTED2");
            }
            if(0x0A == (event & 0x0A)){
                actionSet.add("mtk.test.USB_DISCONNECTED");
            }
        }
        
        mActionSet = actionSet;
    }
    
    /**
     * 
     * @param stringNotifyCode A string which represent a integer.
     */
    public void convertNotifyCode(String stringNotifyCode){
        int notifyCode = 0;
        if(null == stringNotifyCode || "".equals(stringNotifyCode))
            return;
        if (!stringNotifyCode.equals("0")){
           Xlog.d(XLOGTAG, TAG+"notify code is " + stringNotifyCode);
        }
        try{
            notifyCode = Integer.parseInt(stringNotifyCode);
			mNotifyCode = notifyCode;
        } catch(NumberFormatException nfe){
            Xlog.d(XLOGTAG, TAG+"can't parse " + stringNotifyCode + "to integer!");
			nfe.printStackTrace();
        }
    }
    
    /**
     * 
     * @param path The complete path of file
     */
    public void getNotifyCode(String path) {
        File file = new File(path);
		FileReader fr = null;
		BufferedReader br = null;

        try {
	    if (file.exists()) {
	      fr = new FileReader(file);           
	    }
	    if (fr == null) {
	      return;
	    }	
            br = new BufferedReader(fr);
            StringBuffer relevantLines = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
				if (!line.equals("0")) {
                    Xlog.d(XLOGTAG, TAG+"line content: " + line);
				}
                relevantLines.append(line);
            }
            if (relevantLines.length() > 0) {
                mStringNotifyCode = relevantLines.toString();
            } 

        } catch (IOException ioe) {
            Xlog.d(XLOGTAG, TAG+"IOException happen when get NotifyCode");
			ioe.printStackTrace();
	    } finally{
			try{
			   if (br != null){
			   	   br.close();
			   	}
			   if (fr != null){
			      fr.close();
			   }
			}catch (IOException ioe){
				ioe.printStackTrace();
				}
	    }
	}

}
