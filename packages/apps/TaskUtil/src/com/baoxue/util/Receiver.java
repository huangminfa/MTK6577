package com.baoxue.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

	private final static String ACTION_INSTALL = "baoxue.action.INSTALL_PACKAGES";
	private final static String ACTION_DELETE = "baoxue.action.DELETE_PACKAGES";
	private final static String EXTRA_SHOW_DIALOG = "show_dialog"; // boolean
	private final static String EXTRA_PATH = "path"; // stirng
	private final static String EXTRA_PACKAGE_NAME = "packagename"; // stirng

	@Override
	public void onReceive(Context content, Intent internt) {

		PackageInstaller installer = new PackageInstaller(content);
		boolean showui = internt.getBooleanExtra(EXTRA_SHOW_DIALOG, true);
		String path = internt.getStringExtra(EXTRA_PATH);
		String package_name = internt.getStringExtra(EXTRA_PACKAGE_NAME);
		if (ACTION_INSTALL.equals(internt.getAction())) {
			if (path != null) {
				if (showui) {
					installer.install(path);
				} else {
					installer.instatllBatch(path);
				}
			}
		} else if (ACTION_DELETE.equals(internt.getAction())) {
			if (package_name != null) {
				if (showui) {
					installer.uninstall(package_name);
				} else {
					installer.uninstallBatch(package_name);
				}
			}
		}

	}

}
