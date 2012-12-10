/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.mtp;

//ALPS00120037, add log for support MTP debugging
import android.util.Log;
//ALPS00120037, add log for support MTP debugging
/**
 * Java wrapper for MTP/PTP support as USB responder.
 * {@hide}
 */
public class MtpServer implements Runnable {

    private int mNativeContext; // accessed by native methods
    //ALPS00120037, Check Thread run status
    private static final String TAG = "MtpServer";
	
    private boolean 	mServerEndup = false;
    //ALPS00120037, Check Thread run status

    static {
        System.loadLibrary("media_jni");
    }

    public MtpServer(MtpDatabase database, boolean usePtp) {

		//ALPS00120037, Check Thread run status
		Log.w(TAG, "MtpServer constructor: native_setup!!");

		mServerEndup = false;
		//ALPS00120037, Check Thread run status

        native_setup(database, usePtp);
    }

    public void start() {
		
		//ALPS00120037, Check Thread run status
		Log.w(TAG, "MtpServer start!!");
		mServerEndup = false;
		//ALPS00120037, Check Thread run status

        Thread thread = new Thread(this, "MtpServer");
        thread.start();
    }

    @Override
    public void run() {

		//ALPS00120037, Check Thread run status
		Log.w(TAG, "MtpServer run!!");
		mServerEndup = false;
		//ALPS00120037, Check Thread run status
        native_run();
        native_cleanup();
		//ALPS00120037, Check Thread run status
		mServerEndup = true;
		Log.w(TAG, "MtpServer run-end!!");
		//ALPS00120037, Check Thread run status
    }

	//ALPS00120037, Check Thread run status
    public boolean getStatus() {
		Log.w(TAG, "MtpServer getStatus!!");

		return mServerEndup;		
    }
    //ALPS00120037, Check Thread run status

    //Added Modification for ALPS00255822, bug from WHQL test
    public void endSession() {
		Log.w(TAG, "MtpServer endSession!!");
		native_end_session();
		//return mServerEndup;
    }
    //Added Modification for ALPS00255822, bug from WHQL test

    public void sendObjectAdded(int handle) {
        native_send_object_added(handle);
    }

    public void sendObjectRemoved(int handle) {
        native_send_object_removed(handle);
    }

    //ALPS00289309, update Object
    public void sendObjectInfoChanged(int handle) {
        native_send_object_infoChanged(handle);
    }
    //ALPS00289309, update Object
	
    public void addStorage(MtpStorage storage) {
        native_add_storage(storage);
    }

    public void removeStorage(MtpStorage storage) {
        native_remove_storage(storage.getStorageId());
    }
	//Added for Storage Update
    public void updateStorage(MtpStorage storage) {
        native_update_storage(storage);
    }
	//Added for Storage Update
	//Added for Storage Update
    public void sendStorageInfoChanged(MtpStorage storage) {
        native_send_storage_infoChanged(storage.getStorageId());
    }
	//Added for Storage Update

    private native final void native_setup(MtpDatabase database, boolean usePtp);
    private native final void native_run();
    private native final void native_cleanup();
    private native final void native_send_object_added(int handle);
    private native final void native_send_object_removed(int handle);
    //ALPS00289309, update Object
    private native final void native_send_object_infoChanged(int handle);
    //ALPS00289309, update Object
    private native final void native_add_storage(MtpStorage storage);
    private native final void native_remove_storage(int storageId);
    //Added Modification for ALPS00255822, bug from WHQL test
    private native final void native_end_session();
    //Added Modification for ALPS00255822, bug from WHQL test
	//Added for Storage Update
    private native final void native_update_storage(MtpStorage storage);
    private native final void native_send_storage_infoChanged(int storageId);
	//Added for Storage Update
}
