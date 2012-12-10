package com.baoxue.changelogo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class ChangeLogoActivity extends Activity {
	TextView text = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		text = (TextView) findViewById(R.id.text);
		changeLogo();
	}

	void runCmd(String arg) {
		String[] prog = new String[] { "/system/bin/boot_logo_updater", arg };

		try {
			Process pros = Runtime.getRuntime().exec(prog);

			String str = null;
			BufferedReader stdin = new BufferedReader(new InputStreamReader(
					pros.getInputStream()));
			while ((str = stdin.readLine()) != null) {
				Log.d("changelogo", "stdin:" + str);
			}
			BufferedReader stderr = new BufferedReader(new InputStreamReader(
					pros.getErrorStream()));
			while ((str = stderr.readLine()) != null) {
				Log.d("changelogo", "stderr:" + str);
			}
			pros.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void changeLogo() {
		runCmd("-1");
		boolean mIsSpecil = isSpecial();
		boolean setTo = !mIsSpecil;
		int i = 0;
		while (setTo != mIsSpecil && i < 10) {
			if (setTo) {
				runCmd("1");
			} else {
				runCmd("0");
			}

			mIsSpecil = isSpecial();
			i++;
		}

		if (setTo == mIsSpecil) {
			if (setTo) {
				text.setText(R.string.special);
			} else {
				text.setText(R.string.normal);
			}
		} else {
			text.setText(R.string.set_fail);
		}
	}

	private boolean isSpecial() {

		File logo_dev = new File("/dev/logo");
		byte[] buffer = new byte[1];
		RandomAccessFile file = null;
		boolean res = false;
		try {
			file = new RandomAccessFile(logo_dev, "r");
			file.seek(515);
			file.read(buffer);
			if (buffer[0] == 1) {
				res = true;
			} else {
				res = false;
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
				}
			}
		}
		return res;
	}

	@Override
	public void onBackPressed() {
		System.exit(0);
	}

}

