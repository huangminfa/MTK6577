package com.mediatek.thememanager;

import android.widget.ImageView;

public class ThemeData {

	/**
	 * The Name of theme package.
	 */
	String packageName;

	/**
	 * The path of theme package.
	 */
	String themePath;

	/**
	 * The name of theme.
	 */
	String themeName;

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getThemePath() {
		return themePath;
	}

	public void setThemePath(String themePath) {
		this.themePath = themePath;
	}

	public String getThemeName() {
		return themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}
}
