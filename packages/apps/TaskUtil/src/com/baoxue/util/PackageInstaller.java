package com.baoxue.util;

import java.io.File;

import java.io.FileNotFoundException;

import java.io.FileOutputStream;

import java.io.IOException;

import android.content.Context;

import android.content.Intent;

import android.content.pm.PackageInfo;

import android.content.pm.PackageManager;

import android.content.pm.PackageManager.NameNotFoundException;

import android.content.pm.ApplicationInfo;

import android.content.pm.PackageParser;

import android.net.Uri;

import android.util.Log;

import android.util.DisplayMetrics;

import android.content.pm.IPackageInstallObserver;

import android.content.pm.IPackageDeleteObserver;

import android.os.FileUtils;

import android.os.Handler;

import android.os.Message;

public class PackageInstaller {

	private File mTmpFile;

	private final int INSTALL_COMPLETE = 1;

	final static int SUCCEEDED = 1;

	final static int FAILED = 0;

	private final static String TAG = "PackInstaller";

	private Context mContext;

	private ApplicationInfo mAppInfo;

	public PackageInstaller(Context context) {

		mContext = context;

	}

	public void install(String path) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(path)),

		"application/vnd.android.package-archive");

		mContext.startActivity(intent);

	}

	public void instatllBatch(String path) {

		Log.i(TAG, "path=" + path);

		int installFlags = 0;

		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		Uri mPackageURI = Uri.fromFile(file);

		PackageParser.Package mPkgInfo = getPackageInfo(mPackageURI);

		if (mPkgInfo == null) {
			Log.i(TAG, "getPackageInfo error");
			return;
		}
		mAppInfo = mPkgInfo.applicationInfo;

		String packageName = mAppInfo.packageName;

		Log.i(TAG, "====install packageName =" + packageName);

		PackageManager pm = mContext.getPackageManager();

		try {

			PackageInfo pi = pm.getPackageInfo(packageName,

			PackageManager.GET_UNINSTALLED_PACKAGES);

			if (pi != null) {

				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;

			}

		} catch (NameNotFoundException e) {

		}

		if ((installFlags & PackageManager.INSTALL_REPLACE_EXISTING) != 0) {

			Log.w(TAG, "Replacing package:" + packageName);

		}

		PackageInstallObserver observer = new PackageInstallObserver();

		pm.installPackage(mPackageURI, observer, installFlags,

		packageName);

	}

	private class PackageInstallObserver extends IPackageInstallObserver.Stub {

		public void packageInstalled(String packageName, int returnCode) {

			// Message msg = mHandler.obtainMessage(INSTALL_COMPLETE);

			// msg.arg1 = returnCode;

			// mHandler.sendMessage(msg);

			Log.i(TAG, "====INSTALL_COMPLETE");

		}

	}

	private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {

		public void packageDeleted(String packageName, int returnCode) {

			// Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);

			// msg.arg1 = succeeded?SUCCEEDED:FAILED;

			// mHandler.sendMessage(msg);

			Log.i(TAG, "====UNINSTALL_COMPLETE");

		}

	}

	public void uninstall(String packageName) {

		Uri packageURI = Uri.parse("package:" + packageName);

		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(uninstallIntent);

	}

	public void uninstallBatch(String packageName) {

		PackageDeleteObserver observer = new PackageDeleteObserver();

		mContext.getPackageManager().deletePackage(packageName, observer, 0);

	}

	/*
	 * 
	 * Utility method to get package information for a given packageURI
	 */

	public PackageParser.Package getPackageInfo(Uri packageURI) {

		final String archiveFilePath = packageURI.getPath();

		PackageParser packageParser = new PackageParser(archiveFilePath);

		File sourceFile = new File(archiveFilePath);

		DisplayMetrics metrics = new DisplayMetrics();

		metrics.setToDefaults();

		PackageParser.Package pkg = packageParser.parsePackage(sourceFile,

		archiveFilePath, metrics, 0);

		// Nuke the parser reference.

		packageParser = null;

		return pkg;

	}

	/*
	 * 
	 * Utility method to get application information for a given packageURI
	 */

	public ApplicationInfo getApplicationInfo(Uri packageURI) {

		final String archiveFilePath = packageURI.getPath();

		PackageParser packageParser = new PackageParser(archiveFilePath);

		File sourceFile = new File(archiveFilePath);

		DisplayMetrics metrics = new DisplayMetrics();

		metrics.setToDefaults();

		PackageParser.Package pkg = packageParser.parsePackage(sourceFile,
				archiveFilePath, metrics, 0);

		if (pkg == null) {

			return null;

		}

		return pkg.applicationInfo;

	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {

			case INSTALL_COMPLETE:

				if (msg.arg1 == SUCCEEDED) {

				} else {
				}

				break;

			default:

				break;

			}

		}

	};

}
