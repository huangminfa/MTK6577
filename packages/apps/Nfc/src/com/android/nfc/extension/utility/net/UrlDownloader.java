package com.android.nfc.extension.utility.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @hide
 */
public class UrlDownloader
{
	private UrlDownloader()
	{
		// do nothing
	}
	
	/**
	 * create instance of UrlDownloader
	 * @return
	 */
	public static IUrlDownloader createUrlDownloader()
	{
		return new AndroidUrlDownloader();
	}
	
	/**
	 * Downloader interface
	 * @author Ray
	 *
	 */
	public interface IUrlDownloader {
		
		InputStream getInputStream(URL url) throws IOException;
		void setDisable(boolean disable);
		
		int getReadTimeout();
		int getConnectTimeout();
		void setReadTimeout(int timeout);
		void setConnectTimeout(int timeout);
		
		void copyTo(URL url, OutputStream out) throws IOException;
	}
	
	/**
	 * Android UrlDownloader
	 * @author Ray
	 *
	 */
	public static class AndroidUrlDownloader implements IUrlDownloader
	{
		AndroidUrlDownloader() 
		{
			// do nothing
		}
		
		/**
		 * Max Buffer size
		 */
		private static final int MAX_BUFFER = 1024;
		
		/**
		 * Disabled
		 */
		private boolean mDisable = false;
		
		/**
		 * Connect Timeout
		 */
		private int mConnectTimeout;
		
		/**
		 * Read Timeout
		 */
		private int mReadTimeout;

		/**
		 * Set disable
		 */
		@Override
		public void setDisable(boolean disable) 
		{
			mDisable = disable;
		}

		@Override
		public int getReadTimeout() {
			return mReadTimeout;
		}

		@Override
		public int getConnectTimeout() {
			return mConnectTimeout;
		}

		@Override
		public InputStream getInputStream(URL url) throws IOException 
		{
			if(url == null)
			{
				throw new IllegalArgumentException("Url field MUST NOT null");
			}
			
			mDisable = false;
			URLConnection connection = url.openConnection();
			
			if(mConnectTimeout == 0)
			{
				mConnectTimeout = connection.getConnectTimeout();
			}
			else
			{			
				connection.setConnectTimeout(mConnectTimeout);
			}
			
			if(mReadTimeout == 0)
			{
				mReadTimeout = connection.getReadTimeout();
			}
			else
			{
				connection.setReadTimeout(mReadTimeout);
			}
			
			if(mDisable)
			{
				throw new IOException("User disabled");
			}
			
			return connection.getInputStream();
		}
		
		/**
		 * Copy data from inputstream to outputstream
		 * @param in
		 * @param out
		 * @throws IOException
		 */
		private void copyTo(InputStream in, OutputStream out) throws IOException
		{
			byte[] buffer = new byte[MAX_BUFFER];
			int readLen = 0;
			
			while((readLen = in.read(buffer)) > 0)
			{
				if(mDisable)
				{
					throw new IOException("User disable");
				}
				
				out.write(buffer, 0, readLen);
			}
			
			out.flush();
		}

		@Override
		public void copyTo(URL url, OutputStream out) throws IOException 
		{
			InputStream in = getInputStream(url);
			try
			{
				if(mDisable)
				{
					throw new IOException("User disable");
				}
				
				copyTo(in, out);
			}
			finally
			{
				in.close();
			}
		}

		@Override
		public void setReadTimeout(int timeout) {
			mReadTimeout = timeout;
		}

		@Override
		public void setConnectTimeout(int timeout) {
			mConnectTimeout = timeout;
		}
	}
}
