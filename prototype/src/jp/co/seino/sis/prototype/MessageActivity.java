package jp.co.seino.sis.prototype;

import java.util.Timer;

import jp.co.seino.sis.prototype.aspits.AspitsManager;
import jp.co.seino.sis.prototype.aspits.AspitsManagerListener;
import jp.co.seino.sis.prototype.common.ActivityUtil;
import jp.co.seino.sis.prototype.common.ApplSetting;
import jp.co.seino.sis.prototype.common.ApplTimerTask;
import jp.co.seino.sis.prototype.common.BaseActivity;
import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MessageActivity extends BaseActivity implements AspitsManagerListener {

	String TAG = this.getClass().getSimpleName();
	
	private AspitsManager aspitsManager;
	private Location currentLocation;
	
	private ProgressDialog progressDialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		//���X�i�[�̒ǉ�
		ActivityUtil.setListener(this,R.id.message_back_button);
		ActivityUtil.setListener(this,R.id.message_ib_mochidashi);
		ActivityUtil.setListener(this,R.id.message_ib_fuzai);
		ActivityUtil.setListener(this,R.id.message_ib_haitatsu_kanryo);
		ActivityUtil.setListener(this,R.id.message_ib_barcode);
		//ASPITSManager�̐���
		aspitsManager = new AspitsManager();
		aspitsManager.setLocationManager((LocationManager)getSystemService(LOCATION_SERVICE));
	}
	
	@Override
	protected void onResume() {
        super.onResume();
		currentLocation = null;
        if (aspitsManager != null) {
    		//�ݒ�l�̎擾
        	ApplSetting settings = ApplSetting.initSetting(this);
    		aspitsManager.setTerminalId(settings.getTerminalId());		//�[��ID
    		aspitsManager.setAspitsConn(settings.isAspitsConn());//ASPITS���M�ݒ�
    		//���P�[�V�������̎擾�J�n
    		aspitsManager.requestLocationUpdates(this);
    		//aspitsManager.startManager(settings.getInt("gpsInterval",10)*1000);//�~���b�ɕϊ�
        }
        //�v���O���X�_�C�A���O�̕\��
        progressDialog = ActivityUtil.initProgressDialog(this,R.string.message_in_0001);
		//�{�^������
		buttonEnabled(false);

//		//TEST�p
//		Location loc = new Location(LocationManager.GPS_PROVIDER);
//		loc.setLatitude(35.367056);
//		loc.setLongitude(136.638919);
//		loc.setTime(System.currentTimeMillis());
//		aspitsManager.doPost("11", loc);
		
		setTimer(15000);//30�b��ɋN��
   }

	Timer timer;
	protected void setTimer(int msec) {
        ApplTimerTask timerTask = new ApplTimerTask(new Runnable() {
			@Override
			public void run() {
				dismissProgress();//
			}
		});
        timer = ActivityUtil.scheduleAfter(msec, timerTask);
    }

	protected void dismissProgress() {
		progressDialog.dismiss();//
		if (currentLocation == null) {
			Toast.makeText(this, R.string.message_er_0001, Toast.LENGTH_LONG).show();
			ActivityUtil.soundAlert(this);
		} else {
			buttonEnabled(true);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
        if (aspitsManager != null) {
    		//���P�[�V�������̎擾�I��
        	aspitsManager.removeUpdates(this);
        }
        if (timer != null) {
        	Log.i(TAG,"onPause:timer.cancel*****");
        	timer.cancel();
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message, menu);
		return true;
	}

	@Override
    public void onClick(View v){
		if (aspitsManager.isPosting()) {
	    	Toast.makeText(this, "�������ł�",Toast.LENGTH_LONG).show();
	    	return;
		}
    	//�{�^����
        ActivityUtil.soundButton(this);
        if(v == findViewById(R.id.message_back_button)){
        	ActivityUtil.toActivity(this,MainActivity.class);//���j���[�ւ��ǂ�
        	return;
        }
        if(v == findViewById(R.id.message_ib_barcode)){
        	ActivityUtil.toActivity(this,BarcodeActivity.class);//�o�[�R�[�h�ǎ��
        	return;
        }
		if (currentLocation == null) {
	    	Toast.makeText(this, "���݈ʒu���擾�ł��Ă��܂���",Toast.LENGTH_LONG).show();
	    	return;
		}
        if(v == findViewById(R.id.message_ib_mochidashi)){
        	aspitsManager.doPost("11",currentLocation);
        }
        if(v == findViewById(R.id.message_ib_fuzai)){
        	aspitsManager.doPost("12",currentLocation);
        }
        if(v == findViewById(R.id.message_ib_haitatsu_kanryo)){
        	aspitsManager.doPost("13",currentLocation);
        }
    }
	
	private void buttonEnabled(boolean enabled) {
		//((Button)findViewById(R.id.message_back_button)).setEnabled(enabled);
		((ImageButton)findViewById(R.id.message_ib_mochidashi)).setEnabled(enabled);
		((ImageButton)findViewById(R.id.message_ib_fuzai)).setEnabled(enabled);
		((ImageButton)findViewById(R.id.message_ib_haitatsu_kanryo)).setEnabled(enabled);
	}
	
	// AspitsManagerListener��override method ------------------------
	
	@Override
	public void onPostStart() {
		Log.i("onPostStart","");
	}
	@Override
	public void onPostFinish() {
		Log.i("onPostFinish","");
	}
	@Override
	public void onPostSuccess(int code, String res) {
		Log.i("onPostSuccess","code:"+code+" res:"+res);
    	Toast.makeText(this, "���M�������܂���",Toast.LENGTH_LONG).show();
	}
	@Override
	public void onPostFailure(Throwable arg0, String arg1) {
		Log.e("onPostFailure","res:"+arg1,arg0);
	}

	@Override
	public void onLocationChanged(Location location) {
		//Log.i("onLocationChanged","location:"+location);
		if (!aspitsManager.checkLocationAccuracy(location)) {
			return;
		}
		currentLocation = location;
		dismissProgress();//
	}

	@Override
	public void onProviderDisabled(String arg0) {
        Log.v("onProviderDisabled", arg0);
	}

	@Override
	public void onProviderEnabled(String arg0) {
        Log.v("onProviderEnabled", arg0);
	}

	/**
    * ���̃��\�b�h�́A�v���o�C�_�̏ꏊ���擾���邱�Ƃ��ł��Ȃ��ꍇ�A
    * �܂��͍ŋߎg�p�s�\�̊��Ԍ�ɗ��p�\�ƂȂ��Ă���ꍇ�ɌĂяo����܂��B
    */
	@Override
	public void onStatusChanged(String arg0, int status, Bundle arg2) {
        switch (status) {
        case LocationProvider.AVAILABLE:
            Log.v("Status", "AVAILABLE");
            break;
        case LocationProvider.OUT_OF_SERVICE:
            Log.v("Status", "OUT_OF_SERVICE");
            break;
        case LocationProvider.TEMPORARILY_UNAVAILABLE:
            Log.v("Status", "TEMPORARILY_UNAVAILABLE");
            break;
        }	
   }

	@Override
	public void didPost() {
		Log.i(TAG,"didPost***************************");
	}
}
