package jp.co.seino.sis.prototype;

import jp.co.seino.sis.prototype.common.ActivityUtil;
import jp.co.seino.sis.prototype.common.ApplSetting;
import jp.co.seino.sis.prototype.common.BaseActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SetupActivity extends BaseActivity {
	
	private ApplSetting settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		//ボタンの制御追加
		setBackButton(R.id.setup_back_button);
		//リスナーの追加
		ActivityUtil.setListener(this,R.id.setup_sb_gps_interval);
		ActivityUtil.setListener(this,R.id.setup_sb_gps_distance);
		ActivityUtil.setListener(this,R.id.setup_sb_history_count);
		ActivityUtil.setListener(this,R.id.setup_sb_alert_limit);
		ActivityUtil.setListener(this,R.id.setup_sb_sencer_interval);
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadPreference();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setup, menu);
		return true;
	}

	@Override
    public void onClick(View v){
        if(v == backButton){
        	editPreference();
        	ActivityUtil.toActivity(this,MainActivity.class);//メニューへもどる
        }
    }

    /**
     * 設定読み込み
     */
	public void loadPreference() {
    	//
    	settings = ApplSetting.initSetting(this);
    	//ApplSetting.Editor editor = settings.edit();
    	String tId = settings.getTerminalId();	//端末ID
    	EditText et = (EditText)findViewById(R.id.setup_et_terminal_id);
    	et.setText(tId);

    	int gpsAccuracy = settings.getGpsAccuracy();					//GPS精度
    	Spinner sp = (Spinner)findViewById(R.id.setup_sp_gps_accuracy);
    	sp.setSelection(gpsAccuracy);
    	
    	int gpsInterval = settings.getGpsInterval();				//GPS測位間隔
    	TextView tv = (TextView)findViewById(R.id.setup_txt_gps_interval);
    	tv.setText(Integer.toString(gpsInterval));
    	SeekBar sb = (SeekBar)findViewById(R.id.setup_sb_gps_interval);
    	sb.setProgress(gpsInterval);

    	int gpsDistance = settings.getGpsDistance(); 				//GPS測位距離
    	tv = (TextView)findViewById(R.id.setup_txt_gps_distance);
    	tv.setText(Integer.toString(gpsDistance));
    	sb = (SeekBar)findViewById(R.id.setup_sb_gps_distance);
    	sb.setProgress(gpsDistance);

    	int historyCount = settings.getHistoryCount();				//履歴保存件数
    	tv = (TextView)findViewById(R.id.setup_txt_history_count);
    	tv.setText(Integer.toString(historyCount));
    	sb = (SeekBar)findViewById(R.id.setup_sb_history_count);
    	sb.setProgress(historyCount);

    	float sencerInterval = settings.getSensorInterval();		//センサー間隔
    	tv = (TextView)findViewById(R.id.setup_txt_sencer_interval);
    	tv.setText(Float.toString(sencerInterval));
    	sb = (SeekBar)findViewById(R.id.setup_sb_sencer_interval);
    	int si = (int)(sencerInterval*10);
    	sb.setProgress(si);

    	float alertLimit = settings.getAlertLimit();				//危険運転閾値
    	tv = (TextView)findViewById(R.id.setup_txt_alert_limit);
    	tv.setText(Float.toString(alertLimit));
    	sb = (SeekBar)findViewById(R.id.setup_sb_alert_limit);
    	si = (int)(alertLimit*10);
    	sb.setProgress(si);
    	
    	boolean alertSound = settings.isAlertSound();				//危険警告音
    	ToggleButton tb = (ToggleButton)findViewById(R.id.setup_tb_alert_sound);
    	tb.setChecked(alertSound);
    	
    	boolean aspitsConn = settings.isAspitsConn();			//ASPITS連携
    	tb = (ToggleButton)findViewById(R.id.setup_tb_aspits_conn);
    	tb.setChecked(aspitsConn);
	}

	/**
     * 設定値の編集
     */
	public void editPreference() {
     	EditText et = (EditText)findViewById(R.id.setup_et_terminal_id);
    	settings.setTerminalId(et.getText().toString());	//端末ID

    	Spinner sp = (Spinner)findViewById(R.id.setup_sp_gps_accuracy);
    	settings.setGpsAccuracy(sp.getSelectedItemPosition());					//GPS精度

    	TextView tv = (TextView)findViewById(R.id.setup_txt_gps_interval);
    	//Log.i("", "setup_txt_gps_interval:"+Integer.parseInt(tv.getText().toString()));
    	settings.setGpsInterval(Integer.parseInt(tv.getText().toString()));				//GPS測位間隔

    	tv = (TextView)findViewById(R.id.setup_txt_gps_distance);
    	settings.setGpsDistance(Integer.parseInt(tv.getText().toString())); 				//GPS測位距離

    	tv = (TextView)findViewById(R.id.setup_txt_history_count);
    	settings.setHistoryCount(Integer.parseInt(tv.getText().toString()));				//履歴保存件数

    	tv = (TextView)findViewById(R.id.setup_txt_sencer_interval);
    	//Log.i("", "setup_txt_sencer_interval:"+Float.parseFloat(tv.getText().toString()));
    	settings.setSensorInterval(Float.parseFloat(tv.getText().toString()));			//センサー間隔

    	tv = (TextView)findViewById(R.id.setup_txt_alert_limit);
    	settings.setAlertLimit(Float.parseFloat(tv.getText().toString()));				//危険運転閾値

    	ToggleButton tb = (ToggleButton)findViewById(R.id.setup_tb_alert_sound);
    	settings.setAlertSound(tb.isChecked());			//危険警告音

    	tb = (ToggleButton)findViewById(R.id.setup_tb_aspits_conn);
    	settings.setAspitsConn(tb.isChecked());			//ASPITS連携

    	settings.edit();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar == findViewById(R.id.setup_sb_gps_interval)) {
	    	TextView tv = (TextView)findViewById(R.id.setup_txt_gps_interval);
	    	tv.setText(Integer.toString(progress));
		}
		if (seekBar == findViewById(R.id.setup_sb_gps_distance)) {
	    	TextView tv = (TextView)findViewById(R.id.setup_txt_gps_distance);
	    	tv.setText(Integer.toString(progress));
		}
		if (seekBar == findViewById(R.id.setup_sb_history_count)) {
	    	TextView tv = (TextView)findViewById(R.id.setup_txt_history_count);
	    	tv.setText(Integer.toString(progress));
		}
		if (seekBar == findViewById(R.id.setup_sb_sencer_interval)) {
	    	TextView tv = (TextView)findViewById(R.id.setup_txt_sencer_interval);
	    	float f = ((float)progress) / 10;
	    	tv.setText(Float.toString(f));
		}
		if (seekBar == findViewById(R.id.setup_sb_alert_limit)) {
	    	TextView tv = (TextView)findViewById(R.id.setup_txt_alert_limit);
	    	float f = ((float)progress) / 10;
	    	tv.setText(Float.toString(f));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
}
