package com.mediatek.vt;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;


public class PeerAndLocalSurfaceMng {
	SurfaceHolder mBigOne;
	SurfaceHolder mSmallOne;
	public final static int QCIF_WIDTH = 176;
	public final static int QCIF_HEIGHT = 144;
	
	
	public PeerAndLocalSurfaceMng(SurfaceHolder big, SurfaceHolder small) {
		mBigOne = big;
		mSmallOne = small;
		openCamera();
	}
	
	
	
	
	
	//test function	
	public Camera camera;
	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			camera = Camera.open();
			camera.getParameters().flatten();
			try {
				camera.setPreviewDisplay(holder);
			} catch (Throwable t) {
				Log.e("PictureDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Camera.Parameters parameters = camera.getParameters();

			//parameters.setPreviewSize(width, height);
			parameters.setPreviewSize(QCIF_WIDTH, QCIF_HEIGHT);
			parameters.setPictureFormat(PixelFormat.JPEG);
			camera.setParameters(parameters);
			camera.startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	};
	
	
	void openCamera() {
	
	    mBigOne.addCallback(surfaceCallback);
	    mBigOne.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
}
