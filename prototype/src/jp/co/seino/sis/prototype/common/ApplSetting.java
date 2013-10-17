package jp.co.seino.sis.prototype.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ApplSetting {

	private static ApplSetting setting;
	private SharedPreferences preferences;
	
	private String terminalId;
	private int gpsAccuracy;
	private int gpsInterval;				//GPS���ʊԊu
	private int gpsDistance; 				//GPS���ʋ���
	private int historyCount;				//����ۑ�����
	private float sensorInterval;		//�Z���T�[�Ԋu
	private float alertLimit;				//�댯�^�]臒l
	private boolean alertSound;			//�댯�x����
	private boolean aspitsConn;			//ASPITS�A�g
	
	private ApplSetting () {
	}
	
	@SuppressLint("CommitPrefEdits")
	public static ApplSetting initSetting(Context context) {
		if (setting == null) {
			ApplSetting setting = new ApplSetting();
			setting.setPreferences(PreferenceManager.getDefaultSharedPreferences(context));
			setting.load();
			// �[��ID��null�Ȃ�
			if (setting.getTerminalId() == null) {
				//�����ݒ肷��
				setting.setTerminalId("08099995001");
				setting.edit();
			}
			return setting;
		}
		return setting;
	}
	
	public void load() {
		terminalId = preferences.getString("terminalId",null);
		gpsAccuracy = preferences.getInt("gpsAccuracy",0);
		gpsInterval = preferences.getInt("gpsInterval",10);
		gpsDistance = preferences.getInt("gpsDistance",1);
		historyCount = preferences.getInt("historyCount",10);
		sensorInterval = preferences.getFloat("sensorInterval",0.5f);
		alertLimit = preferences.getFloat("alertLimit",5.0f);
		alertSound = preferences.getBoolean("alertSound",true);
		aspitsConn = preferences.getBoolean("aspitsConn",true);
	}
	
	public void edit() {
		SharedPreferences.Editor editor = preferences.edit();
       	editor.putString("terminalId", terminalId);	//�[��ID
    	editor.putInt("gpsAccuracy",gpsAccuracy);					//GPS���x
    	editor.putInt("gpsInterval",gpsInterval);				//GPS���ʊԊu
    	editor.putInt("gpsDistance",gpsDistance); 				//GPS���ʋ���
    	editor.putInt("historyCount",historyCount);				//����ۑ�����
    	editor.putFloat("sensorInterval",sensorInterval);			//�Z���T�[�Ԋu
    	editor.putFloat("alertLimit",alertLimit);				//�댯�^�]臒l
    	editor.putBoolean("alertSound",alertSound);			//�댯�x����
    	editor.putBoolean("aspitsConn",aspitsConn);			//ASPITS�A�g
    	editor.commit();
	}

	public SharedPreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public int getGpsAccuracy() {
		return gpsAccuracy;
	}

	public void setGpsAccuracy(int gpsAccuracy) {
		this.gpsAccuracy = gpsAccuracy;
	}

	public int getGpsInterval() {
		return gpsInterval;
	}

	public void setGpsInterval(int gpsInterval) {
		this.gpsInterval = gpsInterval;
	}

	public int getGpsDistance() {
		return gpsDistance;
	}

	public void setGpsDistance(int gpsDistance) {
		this.gpsDistance = gpsDistance;
	}

	public int getHistoryCount() {
		return historyCount;
	}

	public void setHistoryCount(int historyCount) {
		this.historyCount = historyCount;
	}

	public float getSensorInterval() {
		return sensorInterval;
	}

	public void setSensorInterval(float sensorInterval) {
		this.sensorInterval = sensorInterval;
	}

	public float getAlertLimit() {
		return alertLimit;
	}

	public void setAlertLimit(float alertLimit) {
		this.alertLimit = alertLimit;
	}

	public boolean isAlertSound() {
		return alertSound;
	}

	public void setAlertSound(boolean alertSound) {
		this.alertSound = alertSound;
	}

	public boolean isAspitsConn() {
		return aspitsConn;
	}

	public void setAspitsConn(boolean aspitsConn) {
		this.aspitsConn = aspitsConn;
	}

}
