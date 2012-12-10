package com.android.nfc.extension.nfc.log;

/**
 * @hide
 */
public abstract class Logger {
	
	public interface ILog {

		// print debug message
		public void d(String TAG, String ...msg);
		// print information message
		public void i(String TAG, String ...msg);
		// print error message
		public void e(String TAG, String ...msg);
	}

	private static ILog log  = new AndroidLog();
	
	public static ILog getInstance(){
		return log;
	}
}
