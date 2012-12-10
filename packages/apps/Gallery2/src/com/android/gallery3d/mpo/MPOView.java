package com.android.gallery3d.mpo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.AttributeSet;
import android.widget.ImageView;

public abstract class MPOView extends ImageView implements SensorEventListener{
	
	public MPOView(Context context) {
	    super(context);
	    init();
	}
	
	public MPOView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	}
	
	private void init() {
	    setScaleType(ImageView.ScaleType.MATRIX);
	}
	
	public void onSensorChanged(SensorEvent event) {
	    
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	    
	}

}
