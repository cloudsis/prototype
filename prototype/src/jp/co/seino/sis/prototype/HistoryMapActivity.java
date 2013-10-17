package jp.co.seino.sis.prototype;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import jp.co.seino.sis.prototype.aspits.DrivingHistory;
import jp.co.seino.sis.prototype.common.ActivityUtil;
import jp.co.seino.sis.prototype.map.YahooMapActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class HistoryMapActivity extends YahooMapActivity {

	String TAG = this.getClass().getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title.setText(R.string.title_activity_history_map);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//�n�}�̏k�ڂ𒲐�
		setZoom(4);
		//���s�����t�@�C���̓ǂݍ���
		loadDriveHistory();  
	}

	protected void loadDriveHistory() {
		String fileName = getIntent().getExtras().getString("fileName");
		Log.i("onResume","fileName:"+fileName);
		DrivingHistory history = new DrivingHistory(this);
		try {
			//�t�@�C���̓ǂݍ���
			List<HashMap<String,String>> list = history.readHistory(fileName);
			double[] pre = null;
			//�n�}�ɐ}�`������			
			for (HashMap<String, String> data : list) {
				//�ʒu�����擾
				String gyomuCode = data.get("gyomuCode");
				double lat = toDouble(data.get("lat"));
				double lng = toDouble(data.get("lng"));
				//long time = Long.parseLong(data.get("time"));
				String time = data.get("time");
				Log.i("","gyomuCode:"+gyomuCode+" lat:"+lat+" lng:"+lng+" time:"+time);
				//�A�C�R����`��
				addAnnotation(history, gyomuCode, lat, lng,	time);
				//����`��
				double[] current = {lat,lng};//�ܓx�E�o�x��ێ�
				if (pre != null) {
					drawLine(pre, current);
				} else {
					setCenter(lat,lng); //START��n�}�̒��S�Ƃ���
				}
				pre = current;
			}
		} catch (IOException e) {
			Log.i("onResume","error:",e);
		}
	}
	
	protected double toDouble(String str) {
//		if (str.length() == 17) {
//			str = str.substring(0,str.length()-7);//���V�����
//		}
		return Double.parseDouble(str);
	}
	
	@Override
    public void onClick(View v){
        if(v == backButton){
        	ActivityUtil.toActivity(this,HistoryListActivity.class);//���ǂ�
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history_map, menu);
		return true;
	}


}
