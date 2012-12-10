/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.server;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

import com.android.server.location.ComprehensiveCountryDetector;

import android.content.Context;
import android.location.Country;
import android.location.CountryListener;
import android.location.ICountryDetector;
import android.location.ICountryListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import java.util.Iterator;
import java.util.List;
import android.util.Log;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Binder;

/**
 * This class detects the country that the user is in through
 * {@link ComprehensiveCountryDetector}.
 *
 * @hide
 */
public class CountryDetectorService extends ICountryDetector.Stub implements Runnable
{
    /**
     * The class represents the remote listener, it will also removes itself
     * from listener list when the remote process was died.
     */
	private final class Receiver implements IBinder.DeathRecipient
	{
        private final ICountryListener mListener;
        private final IBinder mKey;
		public String mApplicationName = "default";

		public Receiver(ICountryListener listener)
		{
            mListener = listener;
            mKey = listener.asBinder();
        }

		public void binderDied()
		{
            removeListener(mKey);
        }

        @Override
		public boolean equals(Object otherObj)
		{
			if (otherObj instanceof Receiver)
			{
                return mKey.equals(((Receiver) otherObj).mKey);
            }
            return false;
        }

        @Override
		public int hashCode()
		{
            return mKey.hashCode();
        }

		public ICountryListener getListener()
		{
            return mListener;
        }

		public String toString()
		{
			String result;
			if (mListener != null)
			{
				result = "Receiver{" + Integer.toHexString(System.identityHashCode(this)) + " Listener " + mKey + "mAplicationName: " + mApplicationName + "}";
			}
			else
			{
				result = "Receiver{" + Integer.toHexString(System.identityHashCode(this)) + " Intent " + mKey + "mAplicationName: " + mApplicationName + "}";
    }
			return result;
		}
	}

    private final static String TAG = "CountryDetector";

    /** Whether to dump the state of the country detector service to bugreports */
    private static final boolean DEBUG = false;

    private final HashMap<IBinder, Receiver> mReceivers;
    private final Context mContext;
    private ComprehensiveCountryDetector mCountryDetector;
    private boolean mSystemReady;
    private Handler mHandler;
    private CountryListener mLocationBasedDetectorListener;

	public CountryDetectorService(Context context)
	{
        super();
        mReceivers = new HashMap<IBinder, Receiver>();
        mContext = context;
    }

    @Override
	public Country detectCountry() throws RemoteException
	{
		if (!mSystemReady)
		{
			Log.e(TAG, "detectCountry is called before the system is ready ap name:" + getAppName(Binder.getCallingPid()));
            throw new RemoteException();
        }
		Country country = mCountryDetector.detectCountry();

		if (country != null)
		{
			Log.d(TAG, "detectCountry is called callingPid: " + Binder.getCallingPid() + "ap name:" + getAppName(Binder.getCallingPid()) + "country:" + country.getCountryIso());
		}
		else
		{
			Log.d(TAG, "detectCountry is called callingPid: " + Binder.getCallingPid() + "ap name:" + getAppName(Binder.getCallingPid()) + "country is null");
		}
		return country;
	}

	private String getAppName(int pid)
	{
		String appName = null;
		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procList = null;
		procList = am.getRunningAppProcesses();
		if (procList != null)
		{
			for (Iterator<RunningAppProcessInfo> iterator = procList.iterator(); iterator.hasNext();)
			{
				RunningAppProcessInfo procInfo = iterator.next();
				if (procInfo.pid == pid)
				{
					appName = procInfo.processName;
					break;
				}
			}
		}
		if (appName == null)
		{
			Log.d(TAG, "can not get the app name of the pid:" + pid);
		}
		return appName;
    }

    /**
     * Add the ICountryListener into the listener list.
     */
    @Override
	public void addCountryListener(ICountryListener listener) throws RemoteException
	{
		if (!mSystemReady)
		{
			Log.e(TAG, "calling addCountryListener when system is not ready ap name:" + getAppName(Binder.getCallingPid()));
            throw new RemoteException();
        }
        addListener(listener);
    }

    /**
     * Remove the ICountryListener from the listener list.
     */
    @Override
	public void removeCountryListener(ICountryListener listener) throws RemoteException
	{
		if (!mSystemReady)
		{
			Log.e(TAG, "calling removeCountryListener when system is not ready ap name:" + getAppName(Binder.getCallingPid()));
            throw new RemoteException();
        }
        removeListener(listener.asBinder());
    }

	private void addListener(ICountryListener listener)
	{
		synchronized (mReceivers)
		{
            Receiver r = new Receiver(listener);
			r.mApplicationName = getAppName(Binder.getCallingPid());
			try
			{
                listener.asBinder().linkToDeath(r, 0);
                mReceivers.put(listener.asBinder(), r);
				if (mReceivers.size() == 1)
				{
                    Slog.d(TAG, "The first listener is added");
                    setCountryListener(mLocationBasedDetectorListener);
                }
			}
			catch (RemoteException e)
			{
                Slog.e(TAG, "linkToDeath failed:", e);
            }
        }
    }

	private void removeListener(IBinder key)
	{
		synchronized (mReceivers)
		{
            mReceivers.remove(key);
			if (mReceivers.isEmpty())
			{
                setCountryListener(null);
                Slog.d(TAG, "No listener is left");
            }
        }
    }

	protected void notifyReceivers(Country country)
	{
		synchronized (mReceivers)
		{
			for (Receiver receiver : mReceivers.values())
			{
				try
				{
					Log.d(TAG, "notifyReceivers is called receiver" + receiver.toString() + "country:" + country.getCountryIso());
                    receiver.getListener().onCountryDetected(country);
				}
				catch (RemoteException e)
				{
                    // TODO: Shall we remove the receiver?
                    Slog.e(TAG, "notifyReceivers failed:", e);
                }
            }
        }
    }

	void systemReady()
	{
        // Shall we wait for the initialization finish.
        Thread thread = new Thread(this, "CountryDetectorService");
        thread.start();
    }

	private void initialize()
	{
        mCountryDetector = new ComprehensiveCountryDetector(mContext);
		mLocationBasedDetectorListener = new CountryListener()
		{
			public void onCountryDetected(final Country country)
			{
				mHandler.post(new Runnable()
				{
					public void run()
					{
                        notifyReceivers(country);
                    }
                });
            }
        };
    }

	public void run()
	{
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Looper.prepare();
        mHandler = new Handler();
        initialize();
        mSystemReady = true;
        Looper.loop();
    }

	protected void setCountryListener(final CountryListener listener)
	{
		mHandler.post(new Runnable()
		{
            @Override
			public void run()
			{
                mCountryDetector.setCountryListener(listener);
            }
        });
    }

    // For testing
	boolean isSystemReady()
	{
        return mSystemReady;
    }

    @SuppressWarnings("unused")
    @Override
    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (!DEBUG) return;
        try {
            final Printer p = new PrintWriterPrinter(fout);
            p.println("CountryDetectorService state:");
            p.println("  Number of listeners=" + mReceivers.keySet().size());
            if (mCountryDetector == null) {
                p.println("  ComprehensiveCountryDetector not initialized");
            } else {
                p.println("  " + mCountryDetector.toString());
            }
        } catch (Exception e) {
            Slog.e(TAG, "Failed to dump CountryDetectorService: ", e);
        }
    }
}
