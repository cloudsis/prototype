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
		//{^Ì§äÇÁ
		setBackButton(R.id.setup_back_button);
		//Xi[ÌÇÁ
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
        	ActivityUtil.toActivity(this,MainActivity.class);//j[ÖàÇé
        }
    }

    /**
     * ÝèÇÝÝ
     */
	public void loadPreference() {
    	//
    	settings = ApplSetting.initSetting(this);
    	//ApplSetting.Editor editor = settings.edit();
    	String tId = settings.getTerminalId();	//[ID
    	EditText et = (EditText)findViewById(R.id.setup_et_terminal_id);
    	et.setText(tId);

    	int gpsAccuracy = settings.getGpsAccuracy();					//GPS¸x
    	Spinner sp = (Spinner)findViewById(R.id.setup_sp_gps_accuracy);
    	sp.setSelection(gpsAccuracy);
    	
    	int gpsInterval = settings.getGpsInterval();				//GPSªÊÔu
    	TextView tv = (TextView)findViewById(R.id.setup_txt_gps_interval);
    	tv.setText(Integer.toString(gpsInterval));
    	SeekBar sb = (SeekBar)findViewById(R.id.setup_sb_gps_interval);
    	sb.setProgress(gpsInterval);

    	int gpsDistance = settings.getGpsDistance(); 				//GPSªÊ£
    	tv = (TextView)findViewById(R.id.setup_txt_gps_distance);
    	tv.setText(Integer.toString(gpsDistance));
    	sb = (SeekBar)findViewById(R.id.setup_sb_gps_distance);
    	sb.setProgress(gpsDistance);

    	int historyCount = settings.getHistoryCount();				//ðÛ¶
    	tv = (TextView)findViewById(R.id.setup_txt_history_count);
    	tv.setText(Integer.toString(historyCount));
    	sb = (SeekBar)findViewById(R.id.setup_sb_history_count);
    	sb.setProgress(historyCount);

    	float sencerInterval = settings.getSensorInterval();		//ZT[Ôu
    	tv = (TextView)findViewById(R.id.setup_txt_sencer_interval);
    	tv.setText(Float.toString(sencerInterval));
    	sb = (SeekBar)findViewById(R.id.setup_sb_sencer_interval);
    	int si = (int)(sencerInterval*10);
    	sb.setProgress(si);

    	float alertLimit = settings.getAlertLimit();				//ë¯^]èl
    	tv = (TextView)findViewById(R.id.setup_txt_alert_limit);
    	tv.setText(Float.toString(alertLimit));
    	sb = (SeekBar)findViewById(R.id.setup_sb_alert_limit);
    	si = (int)(alertLimit*10);
    	sb.setProgress(si);
    	
    	boolean alertSound = settings.isAlertSound();				//ë¯x¹
    	ToggleButton tb = (ToggleButton)findViewById(R.id.setup_tb_alert_sound);
    	tb.setChecked(alertSound);
    	
    	boolean aspitsConn = settings.isAspitsConn();			//ASPITSAg
    	tb = (ToggleButton)findViewById(R.id.setup_tb_aspits_conn);
    	tb.setChecked(aspitsConn);
	}

	/**
     * ÝèlÌÒW
     */
	public void editPreference() {
     	EditText et = (EditText)findViewById(R.id.setup_et_terminal_id);
    	settings.setTerminalId(et.getText().toString());	//[ID

    	Spinner sp = (Spinner)findViewById(R.id.setup_sp_gps_accuracy);
    	settings.setGpsAccuracy(sp.getSelectedItemPosition());					//GPS¸x

    	TextView tv = (TextView)findViewById(R.id.setup_txt_gps_interval);
    	//Log.i("", "setup_txt_gps_interval:"+Integer.parseInt(tv.getText().toString()));
    	settings.setGpsInterval(Integer.parseInt(tv.getText().toString()));				//GPSªÊÔu

    	tv = (TextView)findViewById(R.id.setup_txt_gps_distance);
    	settings.setGpsDistance(Integer.parseInt(tv.getText().toString())); 				//GPSªÊ£

    	tv = (TextView)findViewById(R.id.setup_txt_history_count);
    	settings.setHistoryCount(Integer.parseInt(tv.getText().toString()));				//ðÛ¶

    	tv = (TextView)findViewById(R.id.setup_txt_sencer_interval);
    	//Log.i("", "setup_txt_sencer_interval:"+Float.parseFloat(tv.getText().toString()));
    	settings.setSensorInterval(Float.parseFloat(tv.getText().toString()));			//ZT[Ôu

    	tv = (TextView)findViewById(R.id.setup_txt_alert_limit);
    	settings.setAlertLimit(Float.parseFloat(tv.getText().toString()));				//ë¯^]èl

    	ToggleButton tb = (ToggleButton)findViewById(R.id.setup_tb_alert_sound);
    	settings.setAlertSound(tb.isChecked());			//ë¯x¹

    	tb = (ToggleButton)findViewById(R.id.setup_tb_aspits_conn);
    	settings.setAspitsConn(tb.isChecked());			//ASPITSAg

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
