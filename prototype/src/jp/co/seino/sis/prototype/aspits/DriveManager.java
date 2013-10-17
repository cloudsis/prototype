package jp.co.seino.sis.prototype.aspits;

import java.util.List;
import java.util.Timer;

import jp.co.seino.sis.prototype.common.ActivityUtil;
import jp.co.seino.sis.prototype.common.ApplDateFormat;
import jp.co.seino.sis.prototype.common.ApplSetting;
import jp.co.seino.sis.prototype.common.ApplTimerTask;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class DriveManager implements SensorEventListener {
	
	private final String TAG = this.getClass().getSimpleName();
	
	private SensorManager sensorManager;
	private DriveManagerListener listener;
	
	private int sensorInterval; //ミリ秒
	private float alertLimit;
	private String alertEventType;
	
	private SensorEvent event;
	//private SensorEvent preEvent;
	private double[] eventValues;
	private double[] preValues;
	private double[] alertEvent;//警告時の変化値
	
	private long startTime;
	
	public DriveManager() {
	}

	/**
	 * リスナーの設定
	 * @param listener
	 */
	public void registerListener(DriveManagerListener listener) {
		if (sensorManager != null) {
			//初期化
			eventValues = null;
			preValues = null;
			//設定値を取得
			ApplSetting setting = ApplSetting.initSetting((Context)listener);
			sensorInterval = (int)(setting.getSensorInterval() * 1000);//ミリ秒に換算
			alertLimit = setting.getAlertLimit();
			// センサーのオブジェクトリストを取得する
			List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);//加速度センサーを利用
			Log.i("registerListener", "sensors:"+sensors.size());
			// イベントリスナーを登録する
			if (sensors.size() > 0) {
			    Sensor sensor = sensors.get(0);
			    //registerListenerの最後の引数は精度
				// SensorManager.SENSOR_DELAY_FASTEST	変化があり次第、ただちに取得する(遅延時間0ms)
				// SensorManager.SENSOR_DELAY_GAME		ゲーム利用に適している(遅延時間20ms程度)
				// SensorManager.SENSOR_DELAY_NORMAL	スクリーンの向き変更に適している(遅延時間60ms程度)
				// SensorManager.SENSOR_DELAY_UI		ユーザインタフェースへの利用に適している(遅延時間200ms程度)
				sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
				startTime = System.currentTimeMillis(); //開始時刻を設定
				Log.d(TAG,"計測開始:"+ApplDateFormat.formatUpdateDatet(startTime, "yyyy/MM/dd HH:mm:ss SSS"));
				//タイマーの設定
				setTimer(sensorInterval);
			}
		}
		setListener(listener);
	}
	
	Timer timer;
	protected void setTimer(int msec) {
        ApplTimerTask timerTask = new ApplTimerTask(new Runnable() {
			@Override
			public void run() {
				if (event != null) {
					eventValues = new double[]{event.values[0],event.values[1],event.values[2]};
					//log(event);
					preValues = (preValues != null) ? preValues : eventValues;
					//計測間隔でのイベント通知
					listener.onTimerEvent(preValues, eventValues);
					if (isDriveAlert()) {
						//アラートが発生した場合でのイベント通知
						listener.onAlertEvent(alertEventType, alertEvent);
					}
					preValues = eventValues;
				}
			}
		});
        timer = ActivityUtil.scheduleInterval(msec, timerTask);
    }
	
	protected boolean isDriveAlert() {
		alertEventType = null;
		alertEvent = null;
		//センサー現在
		double acx = eventValues[0];
		double acy = eventValues[1];
		double acz = eventValues[2];
		//センサー前回
		double apx = preValues[0];
		double apy = preValues[1];
		double apz = preValues[2];
	    // 上下の加速度（車体の上下動）検知（横向き想定でのX軸方向の加速度センサー情報利用）
		double aax = Math.abs(acx - apx);
	    if(aax > alertLimit) {
	    	alertEventType = DrivingHistory.GYOMU_ALERT_JOGE;
	    }
	    // 左右の加速度（急ハンドル）検知（横向き想定でのY軸方向の加速度センサー情報利用）
		double aay = Math.abs(acy - apy);
	    if(aay > alertLimit) {
	    	alertEventType = DrivingHistory.GYOMU_ALERT_KYU_HANDLE;
	    }
	    // 前後の急加速検知（Z軸方向の加速度センサー情報利用）
		double aaz = acz - apz;
	    if(aaz > alertLimit) {
	    	alertEventType = DrivingHistory.GYOMU_ALERT_KYU_KASOKU;
	    }
	    // 前後の急ブレーキ検知（Z軸方向の加速度センサー情報利用）
	    if(aaz < (alertLimit*-1)) {
	    	alertEventType = DrivingHistory.GYOMU_ALERT_KYU_BRAKE;
	    }
	    //アラートの判断
	    if (alertEventType == null) {
	        return false;//警告なし
	    }
    	alertEvent = new double[]{aax,aay,aaz};
//		Log.i(TAG, " alertLimit:"+alertLimit
//				+ ",x," + alertEvent[0]
//				+ ",y," + alertEvent[1] 
//			    + ",z," + alertEvent[2]
//			   );
		return true;//警告あり
	}

	/**
	 * リスナーの削除
	 * @param listener
	 */
	public void unregisterListener() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(this);
			Log.d(TAG,"計測終了:"+ApplDateFormat.getCurrentTime("yyyy/MM/dd HH:mm:ss SSS"));
		}
		if (timer != null) {
			timer.cancel();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			//log(event);
			setEvent(event);
		}
	}

	protected void log(SensorEvent event) {
		long time = System.currentTimeMillis() - startTime;//経過時間
		String str = time
				+ "," + event.values[0]
				+ "," + event.values[1] 
			    + "," + event.values[2]
			   ;
		Log.d(TAG,str);
	}

	public SensorManager getSensorManager() {
		return sensorManager;
	}

	public void setSensorManager(SensorManager sensorManager) {
		this.sensorManager = sensorManager;
	}

	public DriveManagerListener getListener() {
		return listener;
	}

	public void setListener(DriveManagerListener listener) {
		this.listener = listener;
	}

	public int getSensorInterval() {
		return sensorInterval;
	}

	public void setSensorInterval(int sensorInterval) {
		this.sensorInterval = sensorInterval;
	}

	public float getAlertLimit() {
		return alertLimit;
	}

	public void setAlertLimit(float alertLimit) {
		this.alertLimit = alertLimit;
	}

	public SensorEvent getEvent() {
		return event;
	}

	public void setEvent(SensorEvent event) {
		this.event = event;
	}
}
