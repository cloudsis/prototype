package jp.co.seino.sis.prototype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jp.co.seino.sis.prototype.aspits.DrivingHistory;
import jp.co.seino.sis.prototype.common.ActivityUtil;
import jp.co.seino.sis.prototype.common.BaseActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class HistoryListActivity extends BaseActivity {

	private ArrayList<HashMap<String, String>> rows;
	private DrivingHistory drivingHistory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_list);
		//�{�^���̐���ǉ�
		setBackButton(R.id.history_list_back_button);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//���X�g�̏�����
		initListView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history_list, menu);
		return true;
	}

	@Override
    public void onClick(View v){
		Log.i("TEST","onClick:");
        if(v == backButton){
        	ActivityUtil.toActivity(this,MainActivity.class);//���j���[�ւ��ǂ�
        }
    }
	
	@Override
	public void onItemClick(android.widget.AdapterView<?> arg0, View view, int position, long arg3) {
//		Log.i("TEST","position:"+position);
//		Log.i(this.getClass().getName(),"date:"+rows.get(position).get("date"));
//		Log.i(this.getClass().getName(),"addr:"+rows.get(position).get("addr"));
		//�p�����[�^�̃Z�b�g
		HashMap<String,String> param = new HashMap<String,String>();
		param.put("fileName",rows.get(position).get("fileName"));
		param.put("date",rows.get(position).get("date"));
		param.put("addr",rows.get(position).get("addr"));
		//��ʑJ��
		ActivityUtil.toActivity(this,HistoryMapActivity.class, param);
	};
	
	private void initListView() {
		//���s�����̓ǂݍ���
		try {
			drivingHistory = new DrivingHistory(this);
			rows = drivingHistory.readHistoryListHeader();
		} catch (IOException e) {
			Log.e("initListView","readHistoryList:error",e);
		}
		if (rows != null && !rows.isEmpty() ) {
			//���X�g�̏�����
			SimpleAdapter sa = new SimpleAdapter(this, rows, R.layout.activity_history_list_row
					, new String[]{"date","addr","alert"}
					, new int[]{R.id.history_list_row_date,R.id.history_list_row_addr,R.id.history_list_row_alert_flg});
			ListView listView = (ListView)findViewById(R.id.listView1);
			listView.setAdapter(sa);
			listView.setOnItemClickListener(this);
		}
	}

}
