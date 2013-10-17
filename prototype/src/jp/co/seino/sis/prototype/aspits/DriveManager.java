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
	
	private int sensorInterval; //�~���b
	private float alertLimit;
	private String alertEventType;
	
	private SensorEvent event;
	//private SensorEvent preEvent;
	private double[] eventValues;
	private double[] preValues;
	private double[] alertEvent;//�x�����̕ω��l
	
	private long startTime;
	
	public DriveManager() {
	}

	/**
	 * ���X�i�[�̐ݒ�
	 * @param listener
	 */
	public void registerListener(DriveManagerListener listener) {
		if (sensorManager != null) {
			//������
			eventValues = null;
			preValues = null;
			//�ݒ�l���擾
			ApplSetting setting = ApplSetting.initSetting((Context)listener);
			sensorInterval = (int)(setting.getSensorInterval() * 1000);//�~���b�Ɋ��Z
			alertLimit = setting.getAlertLimit();
			// �Z���T�[�̃I�u�W�F�N�g���X�g���擾����
			List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);//�����x�Z���T�[�𗘗p
			Log.i("registerListener", "sensors:"+sensors.size());
			// �C�x���g���X�i�[��o�^����
			if (sensors.size() > 0) {
			    Sensor sensor = sensors.get(0);
			    //registerListener�̍Ō�̈����͐��x
				// SensorManager.SENSOR_DELAY_FASTEST	�ω������莟��A�������Ɏ擾����(�x������0ms)
				// SensorManager.SENSOR_DELAY_GAME		�Q�[�����p�ɓK���Ă���(�x������20ms���x)
				// SensorManager.SENSOR_DELAY_NORMAL	�X�N���[���̌����ύX�ɓK���Ă���(�x������60ms���x)
				// SensorManager.SENSOR_DELAY_UI		���[�U�C���^�t�F�[�X�ւ̗��p�ɓK���Ă���(�x������200ms���x)
				sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
				startTime = System.currentTimeMillis(); //�J�n������ݒ�
				Log.d(TAG,"�v���J�n:"+ApplDateFormat.formatUpdateDatet(startTime, "yyyy/MM/dd HH:mm:ss SSS"));
				//�^�C�}�[�̐ݒ�
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
					//�v���Ԋu�ł̃C�x���g�ʒm
					listener.onTimerEvent(preValues, eventValues);
					if (isDriveAlert()) {
						//�A���[�g�����������ꍇ�ł̃C�x���g�ʒm
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
		//�Z���T�[����
		double acx = eventValues[0];
		double acy = eventValues[1];
		double acz = eventValues[2];
		//�Z���T�[�O��
		double apx = preValues[0];
		double apy = preValues[1];
		double apz = preValues[2];
	    // �㉺�̉����x�i�ԑ̂̏㉺���j���m�i�������z��ł�X�������̉����x�Z���T�[��񗘗p�j
		double aax = Math.abs(acx - apx);
	    if(aax > alertLimit) {
	    	alertEventType = DrivingHistory.GYOMU_ALERT_JOGE;
	    }
	    // ���E�̉����x�i�}�n���h���j���m�i�������z��ł�Y�������̉����x�Z���T�[��񗘗p�j
		double aay = Math.abs(acy - apy);
	    if(aay > alertLimit) {
	    	alertEventType = DrivingHistory.GYOMU_ALERT_KYU_HANDLE;
	    }
	    // �O��̋}�������m�iZ�������̉����x�Z���T�[��񗘗p�j
		double aaz = acz - apz;
	    if(aaz > alertLimit) {
	    	alertEventType = DrivingHistory.GYOMU_ALERT_KYU_KASOKU;
	    }
	    // �O��̋}�u���[�L���m�iZ�������̉����x�Z���T�[��񗘗p�j
	    if(aaz < (alertLimit*-1)) {
	    	alertEventType = DrivingHistory.GYOMU_ALERT_KYU_BRAKE;
	    }
	    //�A���[�g�̔��f
	    if (alertEventType == null) {
	        return false;//�x���Ȃ�
	    }
    	alertEvent = new double[]{aax,aay,aaz};
//		Log.i(TAG, " alertLimit:"+alertLimit
//				+ ",x," + alertEvent[0]
//				+ ",y," + alertEvent[1] 
//			    + ",z," + alertEvent[2]
//			   );
		return true;//�x������
	}

	/**
	 * ���X�i�[�̍폜
	 * @param listener
	 */
	public void unregisterListener() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(this);
			Log.d(TAG,"�v���I��:"+ApplDateFormat.getCurrentTime("yyyy/MM/dd HH:mm:ss SSS"));
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
		long time = System.currentTimeMillis() - startTime;//�o�ߎ���
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
