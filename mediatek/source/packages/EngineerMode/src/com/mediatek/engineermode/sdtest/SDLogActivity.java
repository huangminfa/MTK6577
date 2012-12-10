package com.mediatek.engineermode.sdtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.ToggleButton;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

public class SDLogActivity extends Activity {
    
    private boolean mState;
    private boolean mThreadState = true;
    private int mFileCount=0;
    private Vector<String> mFileList;
    private Random random;
    private final String FODERNAME = "EM_SDLog";
    private final String FILENAME = "EM_SDLOG_TESTFILE" ;
    private final int AVAILABLESPACE = 4193304;
    private final int COUNT = 10;
    private ToggleButton mToggleButton;
    private ActionThread mThread;   
    
    private static final String TAG = "SD Log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desense_sdlog_activity);
        mToggleButton=(ToggleButton) findViewById(R.id.desense_sdlog_toggle_btn);
        mToggleButton.setOnClickListener(new ButtonClickListener());
        mFileList=new Vector<String>();
        random=new Random();
        checkSDCard();
        createFileForder();
        mThread = new ActionThread();
    }  
    
    
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Elog.i(TAG, "DesenceSDLogActivity onStop()");
        mState = false;
        mThreadState = false; 
        emptyForder(false);
    }
    
    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        init();
    }
   
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        emptyForder(true);
        Elog.i(TAG, "DesenceSDLogActivity onDestroy()"); 
    }  


    private void init(){
        mState = false;
        mThreadState = true;
        mFileCount=0;
        mFileList.clear();  
        mFileList=new Vector<String>();
        random=new Random();
        mThread = new ActionThread();
    }
    
    public class ButtonClickListener implements View.OnClickListener {

        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v.getId() == mToggleButton.getId()) {
                mToggleButton.setEnabled(false);
                if (mToggleButton.isChecked()) {
                    mState = true;
                    if (!mThread.isAlive()) {
                        mThread.start();
                    }
                    Elog.i(TAG, "mSDLogToggleButton is checked");
                } else {
                    mState = false;
                    Elog.i(TAG,
                            "mSDLogToggleButton is unchecked");
                }
                mToggleButton.setEnabled(true);
            }
        }
    }

    private void checkSDCard() {
        if (!isSdMounted()) {
            new AlertDialog.Builder(this).setTitle("Warning!")
            .setMessage("Please insert SD card!")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    finish();
                }
            }).create().show();
        } else if(!isSdWriteable()){
            new AlertDialog.Builder(this).setTitle("Warning!")
            .setMessage("SD card isn't writeable!")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    finish();
                }
            }).create().show();
        } else if(getSdAvailableSpace() < AVAILABLESPACE){
            new AlertDialog.Builder(this).setTitle("Warning!")
            .setMessage("SD card space < 4M!")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    finish();
                }
            }).create().show();            
        }
    }

    private void emptyForder(boolean isDeleteForder) {
        File testForder = new File(getSdPath() + File.separator + FODERNAME);
        if (testForder.exists() && testForder.isDirectory()) {
            File[] fileList = testForder.listFiles();
            if (null != fileList) {
                for (File file : fileList) {
                    if (file.exists()) {
                        file.delete();
                        Elog.v(TAG, "Delete File :" + file.getPath());
                    }
                }
            }
            if (isDeleteForder) {
                testForder.delete();
            }
        }
    }
    
    private void createFileForder() {
        if (isSdMounted()) {
            File testForder = new File(getSdPath() + File.separator+ FODERNAME);
            if (!testForder.exists()) {
                testForder.mkdir();
                Elog.i(TAG, "createFileForder: " + testForder.getPath());
            }
        }
    }
    
     public class ActionThread extends Thread{

        @Override
        public void run() {
            // TODO Auto-generated method stub
            createAndWriteFile();
            while (true) {
                if(!mThreadState){
                    break;
                }
                
                if(mFileCount >= 200){
                    emptyForder(false);
                    mFileList.clear();
                    mFileCount = 0;
                    createAndWriteFile();
                    Elog.w(TAG, "mFileCount > 200 , empty forder.");
                }
                
                if (mState) {
                    switch (getRandom(3)) {
                    case 0:
                        createAndWriteFile();
                        break;
                    case 1:
                        readFile();
                        break;
                    case 2:
                        deleteFile();
                        break;
                    default:
                        break;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    Elog.i(TAG, "mThread : mState == false");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    } 
     
     private void createAndWriteFile() {   
         if(getSdAvailableSpace() < AVAILABLESPACE){
             emptyForder(false);
         }
        if (isSdWriteable()) {
            File testFile = new File(getSdPath() + File.separator + FODERNAME
                    + File.separator + FILENAME + mFileCount);
            if (!testFile.exists()) {
                 try {
                     testFile.createNewFile();
                     Elog.i(TAG, "CreateAndWriteFile :" + testFile.getPath());
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }             
            mFileList.add(FILENAME + mFileCount);
            mFileCount++;
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(testFile);
                try {
                    for (int i = 0; i < COUNT; i++) {
                        outputStream.write(SDLog_Text.getBytes());
                    }
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
     }
     
    private void deleteFile() {
        if (mFileList.size() > 0) {
            File deleteFile = new File(getSdPath() + File.separator
                    + FODERNAME + File.separator
                    + mFileList.get(getRandom(mFileList.size())));
            Elog.i(TAG, "deleteFile: " + deleteFile.getPath());
            if (deleteFile.exists()) {
                deleteFile.delete();
                mFileList.remove(deleteFile.getName());
            } else {
                Elog.w(TAG, "deleteFile doesn't exist!");
            }
        } else {
            createAndWriteFile();
        }
    }
     
    private void readFile() {
        if (mFileList.size() > 0) {
            File readFile = new File(getSdPath() + File.separator + FODERNAME
                    + File.separator
                    + mFileList.get(getRandom(mFileList.size())));
            Elog.i(TAG, "readFile: " + readFile.getPath());
            if (readFile.exists()) {
                FileInputStream inputStream;
                try {
                    inputStream = new FileInputStream(readFile);
                    byte[] buffer = new byte[256];
                    try {
                        int len = inputStream.read(buffer);
                        while (len != -1) {
                            len = inputStream.read(buffer);
                        }
                        inputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Elog.w(TAG, "readFile doesn't exist!");
            }
        } else {
            createAndWriteFile();
        }
    }
     
     private int getRandom(int count){
         if(count <= 0){
             return 0;
         }
         
         return random.nextInt(count);
     }
     
     public static boolean isSdMounted(){
         if (Environment.MEDIA_MOUNTED.equals(
                 Environment.getExternalStorageState()) || 
             Environment.MEDIA_MOUNTED_READ_ONLY.equals(
                 Environment.getExternalStorageState())){
             return true;
         } else {
             return false;
         }
     }
     
     public static boolean isSdReadable(){
         return isSdMounted();
     }
     
     public static boolean isSdWriteable(){
         return Environment.getExternalStorageState().equals(
                 Environment.MEDIA_MOUNTED);
     }
     
     public static String getSdPath(){
         return Environment.getExternalStorageDirectory().getPath();
     }    
     
     public static long getSdAvailableSpace() {
         if (isSdMounted()){
             String sdcard = Environment.getExternalStorageDirectory().getPath();
             StatFs statFs = new StatFs(sdcard);
             long availableSpace = (long)statFs.getBlockSize() * 
             statFs.getAvailableBlocks(); 
             
             return availableSpace;
         } else {
             return -1;
         }
     }
     
     

    private static final String SDLog_Text = "Copyright Statement:This software/firmware"
            + " and related documentation MediaTek Softwareare* protected under relevant "
            + "copyright laws. The information contained herein* is confidential and proprietary"
            + " to MediaTek Inc. and/or its licensors.* Without the prior written permission of "
            + "MediaTek inc. and/or its licensors,* any reproduction, modification, use or "
            + "disclosure of MediaTek Software,* and information contained herein, in whole "
            + "or in part, shall be strictly prohibited. MediaTek Inc. (C) 2010. All rights "
            + "reserved** BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES "
            + "AND AGREES* THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS (MEDIATEK SOFTWARE)"
            + "* RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON* "
            + "AN AS-IS BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,* EXPRESS"
            + " OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF* MERCHANTABILITY,"
            + " FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.* NEITHER DOES MEDIATEK PROVIDE"
            + " ANY WARRANTY WHATSOEVER WITH RESPECT TO THE* SOFTWARE OF ANY THIRD PARTY WHICH MAY "
            + "BE USED BY, INCORPORATED IN, OR* SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER "
            + "AGREES TO LOOK ONLY TO SUCH* THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. "
            + "RECEIVER EXPRESSLY ACKNOWLEDGES* THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN"
            + " FROM ANY THIRD PARTY ALL PROPER LICENSES* CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK "
            + "SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEKSOFTWARE RELEASES MADE TO RECEIVER'S "
            + "SPECIFICATION OR TO CONFORM TO A PARTICULARSTANDARD OR OPEN FORUM. RECEIVER'S SOLE "
            + "AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE ANCUMULATIVE LIABILITY WITH RESPECT TO "
            + "THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,AT MEDIATEK'S OPTION, TO REVISE OR "
            + "REPLACE THE MEDIATEK SOFTWARE AT ISSUE,OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE"
            + " CHARGE PAID BY RECEIVER TOMEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.The following"
            + " software/firmware and/or related documentation (MediaTek Software)have been modified"
            + " by MediaTek Inc. All revisions are subject to any receiver'sapplicable license "
            + "agreements with MediaTek Inc.";
}
