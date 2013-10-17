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
	private ArrayList<String> backupArray;//非同期通信のバックアップ用
	private ArrayList<String> historyArray; // 走行履歴用

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TITLE
		title.setText(R.string.title_activity_drive);
		//ASPITSManagerの生成
		aspitsManager = new AspitsManager();
		aspitsManager.setLocationManager((LocationManager)getSystemService(LOCATION_SERVICE));
		drivingHistory = new DrivingHistory(this);
		//DriveManagerの生成
		driveManager = new DriveManager();
		driveManager.setSensorManager((SensorManager)getSystemService(SENSOR_SERVICE));
	}

	@Override
	protected void onResume() {
		super.onResume();
		//プロパティの初期化
		initProperties();
		//地図の位置を調整
		setZoom(4);	  //初期表示の縮尺を指定
		//現在位置の表示をON
		setUsedMyLocation(true);
        //リスナーの登録
        if (aspitsManager != null) {
    		//設定値の取得
        	ApplSetting settings = ApplSetting.initSetting(this);
    		aspitsManager.setTerminalId(settings.getTerminalId());	//端末ID
    		aspitsManager.setAspitsConn(settings.isAspitsConn());	//ASPITS送信設定
    		//ロケーション情報の取得開始
    		aspitsManager.requestLocationUpdates(this);
    		//送信間隔を開始
    		aspitsManager.startManager(settings.getGpsInterval()*1000);//ミリ秒に変換
        }
        //リスナーの登録
        if (driveManager != null) {
        	driveManager.registerListener(this);
        }
        //プログレスダイアログの表示
        progressDialog = ActivityUtil.initProgressDialog(this,R.string.message_in_0001);
		setTimer(15000);//戻るボタンも押せなくなるので、とりあえず30秒後に非表示にする
        // Keep screen on        
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        
	}

	Timer timer;
	protected void setTimer(int msec) {
        ApplTimerTask timerTask = new ApplTimerTask(new Runnable() {
			@Override
			public void run() {
				dismissProgress();//ダイアログを非表示にする
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
    		//ロケーション情報の取得終了
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
        	if (started) {//開始されていれば
            	//終了処理
        		end();
        		//走行履歴ファイル出力
        		writeHistory();
        		//POST処理
        		doPost();
        		endPosting = true;
        		//送信処理が完了するまで待機するため、ダイアログ表示
                progressDialog = ActivityUtil.initProgressDialog(this,"サーバに送信しています");
                return;//処理終了　onPostFinishでメニュー遷移する
        	}
        	//onPause();//pause処理を明示的にコールする
        	ActivityUtil.toActivity(this,MainActivity.class);//もどる
        }
    }
	
	/**
	 * 履歴に出力する
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

	//STR AspitsManagerListenerのoverride method ------------------------

	@Override
	public void onLocationChanged(Location location) {
		if (!aspitsManager.checkLocationAccuracy(location)) {
			return;//精度によりはじく
		}
		if (currentLocation == null) {
			currentLocation = location;
			start();
		} else {
			currentLocation = location;
		}
	}
	
	/**
	 * 開始処理
	 * @param location
	 */
	protected void start() {
		started = true;
		//ダイアログを非表示
		dismissProgress();
		//ピンを描画
		addAnnotation(drivingHistory, DrivingHistory.GYOMU_START, currentLocation);
		setCenter(currentLocation);
		//走行履歴を追加
		addHistory(DrivingHistory.GYOMU_START, currentLocation);
		//ヘッダー情報を設定
		drivingHistory.setHeaderLocation(currentLocation);
	}
	
	/**
	 * 定期測位
	 * @param location
	 */
	protected void point() {
		//走行履歴を追加
		addHistory(DrivingHistory.GYOMU_POINT, currentLocation);
		//ピンを描画
		addAnnotation(drivingHistory, DrivingHistory.GYOMU_POINT, currentLocation);
		setCenter(currentLocation);
		//線を引く
		drawLine(preLocation, currentLocation);
		preLocation = currentLocation;
	}
	
	/**
	 * 終了処理
	 * @param location
	 */
	protected void end() {
		started = false;
		//走行履歴を追加
		addHistory(DrivingHistory.GYOMU_END, currentLocation);
		//ピンを描画
		addAnnotation(drivingHistory, DrivingHistory.GYOMU_END, currentLocation);
		//線を引く
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
		if (!started) {//開始されるまで実行しない
			return;
		}
		//定期測位を追加
		point();
		//POST処理
		doPost();
	}

	protected void doPost() {
		aspitsManager.setDataArray(dataArray);
		aspitsManager.doPost(); //非同期通信　結果はonPostSuccess/onPostFailureで処理する
		backupArray = dataArray;//非同期通信が完了するまで保持
		dataArray = new ArrayList<String>();//初期化
	}

	@Override
	public void onPostStart() {
	}

	@Override
	public void onPostFinish() {
		if (endPosting) {
			progressDialog.dismiss();
        	ActivityUtil.toActivity(this,MainActivity.class);//もどる
		}
	}

	@Override
	public void onPostSuccess(int code, String res) {
		//成功したら初期化する
		backupArray = null;
	}

	@Override
	public void onPostFailure(Throwable arg0, String arg1) {
		Log.e("onPostFailure","res:"+arg1,arg0);
		//エラーなら退避したデータを再度セットする
		if (backupArray != null) {
			dataArray.addAll(backupArray);
		}
	}

	protected void addHistory(String gyomuCode, Location location) {
		String data = DrivingHistory.toData(gyomuCode, location);
		dataArray.add(data);
		historyArray.add(data);
	}

	//END AspitsManagerListenerのoverride method ------------------------
	

	//STR DriveManagerListenerのoverride method ------------------------
	@Override
	public void onTimerEvent(double[] preEvent, double[] event) {
//		Log.i("onTimerEvent", "**********************");
	}

	@Override
	public void onAlertEvent(String eventType, double[] alertEvent) {
	    if (!started) {
	        return;//開始されていなければ無視
	    }
        alertEvent(eventType);
        Log.i("onAlertEvent"," eventType:"+eventType+ " alert x:"+alertEvent[0]+" y:"+alertEvent[1]+" z"+alertEvent[2]);
	}

	protected void alertEvent(String eventType) {
		// 警告メッセージを表示し、警告音を鳴らす
		String message = drivingHistory.toTitle(eventType)+"を検知しました";
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		ActivityUtil.soundAlert(this);
		//走行履歴を追加
		addHistory(eventType, currentLocation);
		drivingHistory.setAlert(true);
		//ピンを描画
		addAnnotation(drivingHistory, eventType, currentLocation);
		//線を引く
		drawLine(preLocation, currentLocation);
		preLocation = currentLocation;
	}
	//END DriveManagerListenerのoverride method ------------------------

}