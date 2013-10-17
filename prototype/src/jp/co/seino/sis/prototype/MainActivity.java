package jp.co.seino.sis.prototype;

import jp.co.seino.sis.prototype.common.ActivityUtil;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity implements OnClickListener {

	public static final int SHOW_EDITOR = 0;
	final String TAG = "MainActivity";
	
	private ImageButton driveButton;
	private ImageButton historyButton;
	private ImageButton messageButton;
	private Button setupButton;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("メインメニュー");
        Log.i(TAG, "メインメニュー");
        //ボタンの動作の設定
        driveButton = (ImageButton)findViewById(R.id.main_drive);
        driveButton.setOnClickListener(this);
        historyButton = (ImageButton)findViewById(R.id.main_history);
        historyButton.setOnClickListener(this);
        messageButton = (ImageButton)findViewById(R.id.main_message);
        messageButton.setOnClickListener(this);
        setupButton = (Button)findViewById(R.id.main_setupButton);
        setupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        Intent it = null;
        if(v == setupButton){
            it = new Intent(this, SetupActivity.class);
        } else if (v == driveButton) {
            Log.i(TAG, "ドライブボタンが押されました");
            it = new Intent(this, DriveActivity.class);
        } else if (v == historyButton) {
        	Log.i(TAG, "historyButtonが押されました");
            it = new Intent(this, HistoryListActivity.class);
            //it = new Intent(this, YahooMapActivity.class);
        } else if (v == messageButton) {
        	Log.i(TAG, "messageButtonが押されました");
            it = new Intent(this, MessageActivity.class);
            //it = new Intent(this, BarcodeActivity.class);
        }
        if (it != null) {
        	//ボタン音
            ActivityUtil.soundButton(this);
            //画面遷移
            try{
                startActivityForResult(it, SHOW_EDITOR);
            } catch(ActivityNotFoundException e){
                Log.e(TAG, "ActivityNotFoundException");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
}
