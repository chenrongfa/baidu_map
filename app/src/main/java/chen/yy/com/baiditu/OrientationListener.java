package chen.yy.com.baiditu;/**
 * Baiditu
 * Created by chenrongfa on 2017/3/28
 * email:18720979339@163.com
 * qq:952786280
 * company:yy
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 *
 *Baiditu
 * Created by chenrongfa on 2017/3/28
 * email:18720979339@163.com
 * qq:952786280
 * company:yy
 */
public class OrientationListener implements SensorEventListener{
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private Context mContext;
	private float lastx;

	public  OrientationListener(Context context){
		this.mContext=context;
		mSensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

	}
	public void start(){
		mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);

	}
	public void stop(){
		mSensorManager.unregisterListener(this);
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.e("jinlai", "onSensorChanged: " );
		float x=event.values[0];
		if (Math.abs(x-lastx)>0.5){
			if (mOtl!=null){
				mOtl.onOrientationChangeListener(x);
				Log.e("jinlai", "onSensorChanged: " );
			}
		}
		lastx=x;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void setmOtl(OrientationChangeListener mOtl) {
		this.mOtl = mOtl;
	}

	private OrientationChangeListener mOtl;
	public interface OrientationChangeListener{
		void onOrientationChangeListener(float x);

	}
}
