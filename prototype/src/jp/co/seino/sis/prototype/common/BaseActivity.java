package jp.co.seino.sis.prototype.common;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BaseActivity extends Activity 
implements OnClickListener
, OnItemClickListener
, OnSeekBarChangeListener
{
	public static final int SHOW_EDITOR = 0;
	
	protected Button backButton;
	
	@Override
    public void onClick(View v){
    }
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}
	
	public void setBackButton(int id) {
		//戻るボタン制御
		backButton = (Button)findViewById(id);
		ActivityUtil.setListener(this,id);
	}
	
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

}
