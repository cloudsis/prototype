package jp.co.seino.sis.prototype.aspits;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jp.co.seino.sis.prototype.R;
import jp.co.seino.sis.prototype.common.ApplDateFormat;
import jp.co.seino.sis.prototype.common.ApplDecimalFormat;
import jp.co.seino.sis.prototype.common.ApplSetting;
import jp.co.seino.sis.prototype.common.BeanUtil;
import jp.co.seino.sis.prototype.common.FileUtil;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class DrivingHistory {
	
	private final String TAG = this.getClass().getSimpleName();

	public static final String GYOMU_POINT = "00";
	public static final String GYOMU_ALERT_JOGE = "01";
	public static final String GYOMU_ALERT_KYU_HANDLE = "02";
	public static final String GYOMU_ALERT_KYU_KASOKU = "03";
	public static final String GYOMU_ALERT_KYU_BRAKE = "04";
	public static final String GYOMU_START = "08";
	public static final String GYOMU_END = "09";
	public static final String GYOMU_MOCHIDASHI = "11";
	public static final String GYOMU_FUZAI = "12";
	public static final String GYOMU_HAITATSU_KANRYO = "13";
	
	public static final String FILE_DIR = "history";
	private Context context;
	
	private Location headerLocation;
	private boolean alert = false;
	
	public DrivingHistory(Context context) {
		setContext(context);
	}
	
	public void init() {
		headerLocation = null;
		alert = false;
	}
	
	public ArrayList<HashMap<String, String>> readHistoryListHeader() throws IOException {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		//履歴情報を格納するディレクトリを作成
		File historyDir = new File(getContext().getFilesDir(),FILE_DIR);
		String[] files = historyDir.list();
		if (files != null) {
			Arrays.sort(files);//日時順で並べ替え
			//逆順にリストへセットする
			//for (int i = 0; i<files.length; i++) {	
			for (int i = (files.length-1); i >= 0 ; i--) {
				//if (files[i] == null) continue;
				File file = new File(historyDir,files[i]);
				List<String> list = FileUtil.read(file);
				//1行目のヘッダーを取得
				if (list != null && list.size() >= 1) {
					String row = list.get(0);
					String[] cols = row.split(",");
					HashMap<String, String> data = new HashMap<String, String>();
					data.put("fileName", files[i]);
					data.put("date", cols[0]);
					data.put("addr", cols[1]);
					data.put("alertFlg", cols[2]);
					data.put("alert", ("0".equals(cols[2])) ? "" : " アラート有");
					dataList.add(data);
				}
			}
		}
		return dataList;
	}
	
	public ArrayList<HashMap<String, String>> readHistory(String fileName) throws IOException {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		//履歴情報を格納するディレクトリ
		File historyDir = new File(getContext().getFilesDir(),FILE_DIR);
		File file = new File(historyDir,fileName);
		List<String> list = FileUtil.read(file);
		Log.i("readHistory", "list:size"+list.size());
		int cnt = 0;
		for (String row : list) {
			cnt++;
			Log.i("readHistory", "row("+cnt+")"+row);
			if (cnt == 1) continue; //ヘッダーは無視
			String[] cols = row.split(",");
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("current", cols[0]);
			data.put("time", cols[1]);
			data.put("lat", cols[2]);
			data.put("lng", cols[3]);
			data.put("speed", cols[4]);
			data.put("course", cols[5]);
			data.put("gyomuCode", cols[6]);
			dataList.add(data);
		}
		return dataList;
	}
	
	public void writeHistory(ArrayList<String> data) throws IOException {
		//履歴情報を格納するディレクトリを作成
		File historyDir = new File(getContext().getFilesDir(),FILE_DIR);
		if (!historyDir.canWrite()) {
			historyDir.mkdir();
		}
		//ファイル作成
		String current = ApplDateFormat.getCurrentTime("yyyyMMddHHmmss");
		File file = new File(historyDir,current+".csv");
		Log.i("writeFile","file:"+file.getAbsolutePath());
		//データ作成
		ArrayList<String> rows = new ArrayList<String>();
		//1行目（ヘッダー） 日時、住所、アラート有無
		rows.add(toHeader(current));
		//走行履歴
		rows.addAll(data);
		//ファイル出力
		FileUtil.write(file, rows);
		//ファイル作成保存数以上なら過去を削除
		deleteOldHistory(historyDir);
	}
	
	/**
	 * 古い履歴ファイルを削除する
	 */
	protected void deleteOldHistory(File historyDir) {
		if (historyDir == null) return;
		String[] files = historyDir.list();
		if (files != null) {
			//保存件数を取得
	    	ApplSetting settings = ApplSetting.initSetting(getContext());
	    	int historyCount = settings.getHistoryCount();				//履歴保存件数
	    	if (files.length > historyCount) {
				//ファイル名が日時となっているため、昇順に並べ替え
	    		Arrays.sort(files);
	    		int deleteCount = files.length - historyCount;
	    		for (int i=0;i<deleteCount;i++) {
	    			File file = new File(historyDir,files[i]);
	    			file.delete();
	    		}
	    	}
			
		}
	}
	
	protected String toHeader(String current) {
		//ロケーションから住所を取得
		String addr = point2address(getHeaderLocation().getLatitude(), getHeaderLocation().getLongitude());
		addr = BeanUtil.replace(addr, "," , "，");//カンマを除去
		//ヘッダー情報を作成
		StringBuffer header = new StringBuffer();
		header.append(ApplDateFormat.convertIgnore(current, "yyyyMMddHHmmss", "yyyy/MM/dd HH:mm"));
		header.append(",").append(addr);
		header.append(",").append((alert)?"1":"0");
		return header.toString();
	}
	
	public void writeSampleFile() throws IOException {
		//ファイル作成
		Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(35.367056);
		location.setLongitude(136.638919);
		//location.setTime(System.currentTimeMillis());
		//データ作成
		ArrayList<String> rows = new ArrayList<String>();
		//走行履歴
		String current = ApplDateFormat.getCurrentTime("yyyyMMddHHmmss");
		//long time = new Date().getTime();
		rows.add(toData("08",current,current,35.367056,136.638919,60.0,0.1));//START
		rows.add(toData("00",current,current,(35.367056+0.01),(136.638919+0.01),60.0,0.1));//
		rows.add(toData("00",current,current,(35.367056+0.02),(136.638919+0.015),60.0,0.1));//
		rows.add(toData("01",current,current,(35.367056-0.003),(136.638919-0.004),60.0,0.1));//
		rows.add(toData("09",current,current,(35.367056+0.001),(136.638919+0.001),60.0,0.1));//END
		//ファイル出力
		setHeaderLocation(location);
		setAlert(true);
		writeHistory(rows);
	}

	/**
	 * データ文字列に変更
	 * @param gyomuCode
	 * @param location
	 * @return
	 */
	public static String toData(String gyomuCode,Location location) {
		String current = ApplDateFormat.getCurrentTime("yyyyMMddHHmmss");
        String time = ApplDateFormat.formatUpdateDatet(location.getTime(), "yyyyMMddHHmmss");
        double speed = (location.getSpeed() < 0) ? 0 : (location.getSpeed() * 60 * 60) / 1000;//時速に変換
        double course = (location.getBearing() < 0) ? 0 : location.getBearing();
        return toData(gyomuCode, current, time, location.getLatitude(), location.getLongitude(), speed, course);
	}

	public static String toData(String gyomuCode, String current, String time, double lat, double lng, double speed, double course) {
		//String current = ApplDateFormat.getCurrentTime("yyyyMMddHHmmss");
		String str = current
				+ "," + time
				+ "," + toNumL(lat)//"035.3670560000000"
				+ "," + toNumL(lng)//"136.6389190000000"
				+ "," + toNum(speed)//"000.00"
				+ "," + toNum(course)//"000.00"
				+ "," + gyomuCode
				;
		return str;
	}
	
	public Drawable toIcon(String gyomuCode) {
		if (DrivingHistory.GYOMU_START.equals(gyomuCode)) {
			return getContext().getResources().getDrawable(R.drawable.pin_start);
		} else if (DrivingHistory.GYOMU_END.equals(gyomuCode)) {
			return getContext().getResources().getDrawable(R.drawable.pin_goal);
		} else if (DrivingHistory.GYOMU_POINT.equals(gyomuCode)) {
			return getContext().getResources().getDrawable(R.drawable.pin_head_s);//定点
		} else {
			return getContext().getResources().getDrawable(R.drawable.alert);//警告
		}
	}
	
	public String toTitle(String gyomuCode) {
		if (DrivingHistory.GYOMU_START.equals(gyomuCode)) {
			return "スタート";
		} else if (DrivingHistory.GYOMU_END.equals(gyomuCode)) {
			return "ゴール";
		} else if (DrivingHistory.GYOMU_ALERT_JOGE.equals(gyomuCode)) {
			return "上下振動";
		} else if (DrivingHistory.GYOMU_ALERT_KYU_HANDLE.equals(gyomuCode)) {
			return "急ハンドル";
		} else if (DrivingHistory.GYOMU_ALERT_KYU_KASOKU.equals(gyomuCode)) {
			return "急加速";
		} else if (DrivingHistory.GYOMU_ALERT_KYU_BRAKE.equals(gyomuCode)) {
			return "急ブレーキ";
		} else if (DrivingHistory.GYOMU_POINT.equals(gyomuCode)) {
			return "";
		} else {
			return null;
		}
	}

	//座標を住所のStringへ変換
	public String point2address(double latitude, double longitude) {
		StringBuffer strbuf = new StringBuffer();
		//geocoedrの実体化
		Geocoder geocoder = new Geocoder(getContext(), Locale.JAPAN);
		List<Address> listAddress;
		try {
			listAddress = geocoder.getFromLocation(latitude, longitude, 5);
			//ジオコーディングに成功したらStringへ
			if (!listAddress.isEmpty()){
				Address address = listAddress.get(0);//
				//adressをStringへ
				String buf;
				for (int i = 0; (buf = address.getAddressLine(i)) != null; i++){
					if (i== 0) continue; //１つめ「国名：日本」となるので、読み飛ばし
					strbuf.append(buf);
				}
			}
			//失敗（Listが空だったら）
			else {
				return "不明";
			}
			return strbuf.toString();
		} catch (IOException e) {
			Log.w(TAG,"point2address",e);
		}	//引数末尾は返す検索結果数
		return "不明";
	}

	protected static String toNumL(double f) {
		return ApplDecimalFormat.formatNumber(f,"000.0000000000000");
	}

	protected static String toNum(double f) {
		return ApplDecimalFormat.formatNumber(f,"000.00");
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Location getHeaderLocation() {
		return headerLocation;
	}

	public void setHeaderLocation(Location headerLocation) {
		this.headerLocation = headerLocation;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}
}
