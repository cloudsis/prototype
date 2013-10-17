package jp.co.seino.sis.prototype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

import jp.co.seino.sis.prototype.aspits.AspitsManager;
import jp.co.seino.sis.prototype.aspits.AspitsManagerListener;
import jp.co.seino.sis.prototype.aspits.DriveManager;
import jp.co.seino.sis.prototype.aspits.DriveManagerListener;
import jp.co.seino.sis.prototype.aspits.DrivingHistory;
import jp.co.seino.sis.prototype.common.ActivityUtil;
import jp.co.seino.sis.prototype.common.ApplSetting;
import jp.co.seino.sis.prototype.common.ApplTimerTask;
import jp.co.seino.sis.prototype.map.YahooMapActivity;
import android.app.ProgressDialog;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class DriveActivity extends YahooMapActivity implements AspitsManagerListener, DriveManagerListener {

	String TAG = this.getClass().getSimpleName();
	
	private AspitsManager aspitsManager;
	private DrivingHistory drivingHistory;
	private Location currentLocation;
	private Location preLocation;
	
	private DriveManager driveManager;
	
	private boolean started = false;
	private boolean endPosting = false;
	
	private ArrayList<String> dataArray;
	private ArrayList<String> backupArray;//�񓯊��ʐM�̃o�b�N�A�b�v�p
	private ArrayList<String> historyArray; // ���s����p

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TITLE
		title.setText(R.string.title_activity_drive);
		//ASPITSManager�̐���
		aspitsManager = new AspitsManager();
		aspitsManager.setLocationManager((LocationManager)getSystemService(LOCATION_SERVICE));
		drivingHistory = new DrivingHistory(this);
		//DriveManager�̐���
		driveManager = new DriveManager();
		driveManager.setSensorManager((SensorManager)getSystemService(SENSOR_SERVICE));
	}

	@Override
	protected void onResume() {
		super.onResume();
		//�v���p�e�B�̏�����
		initProperties();
		//�n�}�̈ʒu�𒲐�
		setZoom(4);	  //�����\���̏k�ڂ��w��
		//���݈ʒu�̕\����ON
		setUsedMyLocation(true);
        //���X�i�[�̓o�^
        if (aspitsManager != null) {
    		//�ݒ�l�̎擾
        	ApplSetting settings = ApplSetting.initSetting(this);
    		aspitsManager.setTerminalId(settings.getTerminalId());	//�[��ID
    		aspitsManager.setAspitsConn(settings.isAspitsConn());	//ASPITS���M�ݒ�
    		//���P�[�V�������̎擾�J�n
    		aspitsManager.requestLocationUpdates(this);
    		//���M�Ԋu���J�n
    		aspitsManager.startManager(settings.getGpsInterval()*1000);//�~���b�ɕϊ�
        }
        //���X�i�[�̓o�^
        if (driveManager != null) {
        	driveManager.registerListener(this);
        }
        //�v���O���X�_�C�A���O�̕\��
        progressDialog = ActivityUtil.initProgressDialog(this,R.string.message_in_0001);
		setTimer(15000);//�߂�{�^���������Ȃ��Ȃ�̂ŁA�Ƃ肠����30�b��ɔ�\���ɂ���
        // Keep screen on        
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        
	}

	Timer timer;
	protected void setTimer(int msec) {
        ApplTimerTask timerTask = new ApplTimerTask(new Runnable() {
			@Override
			public void run() {
				dismissProgress();//�_�C�A���O���\���ɂ���
			}
		});
        timer = ActivityUtil.scheduleAfter(msec, timerTask);
    }
	
	protected void dismissProgress() {
		progressDialog.dismiss();//
		if (currentLocation == null) {
			Toast.makeText(this, R.string.message_er_0001, Toast.LENGTH_LONG).show();
			ActivityUtil.soundAlert(this);
		}
	}

	protected void initProperties() {
		started = false;
		endPosting = false;
		currentLocation = null;
		preLocation = null;
		dataArray = new ArrayList<String>();
		backupArray = null;
		historyArray = new ArrayList<String>();
		drivingHistory.init();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
        if (aspitsManager != null) {
    		//���P�[�V�������̎擾�I��
        	aspitsManager.removeUpdates(this);
        	aspitsManager.endManager();
        }
        if (driveManager != null) {
        	driveManager.unregisterListener();
        }
        if (timer != null) {
        	Log.i(TAG,"onPause:timer.cancel*****");
        	timer.cancel();
        }
        // Keep screen off
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        
    }
	
	@Override
    public void onClick(View v){
        if(v == backButton){
        	if (started) {//�J�n����Ă����
            	//�I������
        		end();
        		//���s�����t�@�C���o��
        		writeHistory();
        		//POST����
        		doPost();
        		endPosting = true;
        		//���M��������������܂őҋ@���邽�߁A�_�C�A���O�\��
                progressDialog = ActivityUtil.initProgressDialog(this,"�T�[�o�ɑ��M���Ă��܂�");
                return;//�����I���@onPostFinish�Ń��j���[�J�ڂ���
        	}
        	//onPause();//pause�����𖾎��I�ɃR�[������
        	ActivityUtil.toActivity(this,MainActivity.class);//���ǂ�
        }
    }
	
	/**
	 * �����ɏo�͂���
	 */
	public void writeHistory() {
		try {
			drivingHistory.writeHistory(historyArray);
		} catch (IOException e) {
			Log.e(TAG, "writeHistory:error",e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.drive, menu);
		return true;
	}

	//STR AspitsManagerListener��override method ------------------------

	@Override
	public void onLocationChanged(Location location) {
		if (!aspitsManager.checkLocationAccuracy(location)) {
			return;//���x�ɂ��͂���
		}
		if (currentLocation == null) {
			currentLocation = location;
			start();
		} else {
			currentLocation = location;
		}
	}
	
	/**
	 * �J�n����
	 * @param location
	 */
	protected void start() {
		started = true;
		//�_�C�A���O���\��
		dismissProgress();
		//�s����`��
		addAnnotation(drivingHistory, DrivingHistory.GYOMU_START, currentLocation);
		setCenter(currentLocation);
		//���s������ǉ�
		addHistory(DrivingHistory.GYOMU_START, currentLocation);
		//�w�b�_�[����ݒ�
		drivingHistory.setHeaderLocation(currentLocation);
	}
	
	/**
	 * �������
	 * @param location
	 */
	protected void point() {
		//���s������ǉ�
		addHistory(DrivingHistory.GYOMU_POINT, currentLocation);
		//�s����`��
		addAnnotation(drivingHistory, DrivingHistory.GYOMU_POINT, currentLocation);
		setCenter(currentLocation);
		//��������
		drawLine(preLocation, currentLocation);
		preLocation = currentLocation;
	}
	
	/**
	 * �I������
	 * @param location
	 */
	protected void end() {
		started = false;
		//���s������ǉ�
		addHistory(DrivingHistory.GYOMU_END, currentLocation);
		//�s����`��
		addAnnotation(drivingHistory, DrivingHistory.GYOMU_END, currentLocation);
		//��������
		drawLine(preLocation, currentLocation);
		preLocation = currentLocation;
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	@Override
	public void didPost() {
		if (!started) {//�J�n�����܂Ŏ��s���Ȃ�
			return;
		}
		//������ʂ�ǉ�
		point();
		//POST����
		doPost();
	}

	protected void doPost() {
		aspitsManager.setDataArray(dataArray);
		aspitsManager.doPost(); //�񓯊��ʐM�@���ʂ�onPostSuccess/onPostFailure�ŏ�������
		backupArray = dataArray;//�񓯊��ʐM����������܂ŕێ�
		dataArray = new ArrayList<String>();//������
	}

	@Override
	public void onPostStart() {
	}

	@Override
	public void onPostFinish() {
		if (endPosting) {
			progressDialog.dismiss();
        	ActivityUtil.toActivity(this,MainActivity.class);//���ǂ�
		}
	}

	@Override
	public void onPostSuccess(int code, String res) {
		//���������珉��������
		backupArray = null;
	}

	@Override
	public void onPostFailure(Throwable arg0, String arg1) {
		Log.e("onPostFailure","res:"+arg1,arg0);
		//�G���[�Ȃ�ޔ������f�[�^���ēx�Z�b�g����
		if (backupArray != null) {
			dataArray.addAll(backupArray);
		}
	}

	protected void addHistory(String gyomuCode, Location location) {
		String data = DrivingHistory.toData(gyomuCode, location);
		dataArray.add(data);
		historyArray.add(data);
	}

	//END AspitsManagerListener��override method ------------------------
	

	//STR DriveManagerListener��override method ------------------------
	@Override
	public void onTimerEvent(double[] preEvent, double[] event) {
//		Log.i("onTimerEvent", "**********************");
	}

	@Override
	public void onAlertEvent(String eventType, double[] alertEvent) {
	    if (!started) {
	        return;//�J�n����Ă��Ȃ���Ζ���
	    }
        alertEvent(eventType);
        Log.i("onAlertEvent"," eventType:"+eventType+ " alert x:"+alertEvent[0]+" y:"+alertEvent[1]+" z"+alertEvent[2]);
	}

	protected void alertEvent(String eventType) {
		// �x�����b�Z�[�W��\�����A�x������炷
		String message = drivingHistory.toTitle(eventType)+"�����m���܂���";
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		ActivityUtil.soundAlert(this);
		//���s������ǉ�
		addHistory(eventType, currentLocation);
		drivingHistory.setAlert(true);
		//�s����`��
		addAnnotation(drivingHistory, eventType, currentLocation);
		//��������
		drawLine(preLocation, currentLocation);
		preLocation = currentLocation;
	}
	//END DriveManagerListener��override method ------------------------

}