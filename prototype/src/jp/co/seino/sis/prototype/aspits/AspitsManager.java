package jp.co.seino.sis.prototype.aspits;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import jp.co.seino.sis.prototype.common.ActivityUtil;
import jp.co.seino.sis.prototype.common.ApplDateFormat;
import jp.co.seino.sis.prototype.common.ApplSetting;
import jp.co.seino.sis.prototype.common.ApplTimerTask;
import jp.co.seino.sis.prototype.common.BeanUtil;
import jp.co.seino.sis.prototype.common.Encryption;
import jp.co.seino.sis.prototype.common.FileUtil;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class AspitsManager  {
	
	public AspitsManager() {
	}
	
	public final String TAG = this.getClass().getSimpleName();
	
	public static final String ASPITS_URL = "http://aspitst.aspits.jp/aspits/MobileDataReceiver";
//	public static final String ASPITS_URL = "http://10.2.34.16/aspits/MobileDataReceiver";
//	public static final String ASPITS_URL = "http://10.2.32.145:8080/muratec/Upload.do";
//	public final String ASPITS_URL = "http://10.2.32.104:8080/test/sample.html";
	public static final String ASPITS_KEY = "MobilePhoneKey23";
	public static final String ASPITS_GZIP_FILE_NAME = "aspitsdata.gz";
	
	private static AsyncHttpClient client = new AsyncHttpClient();
	private LocationManager locationManager;

	//private Context context;
	private String terminalId;
	private ArrayList<String> dataArray;
	private boolean aspitsConn = false;
	private boolean posting	= false;
	
	private AspitsManagerListener listener;
	
	private float[] accuracyLevel = {50, 10, 100};
	private float accuracy = 0;

	public void requestLocationUpdates(AspitsManagerListener listener){
    	ApplSetting settings = ApplSetting.initSetting((Context)listener);
		int gpsInterval = settings.getGpsInterval();
		int gpsDistance = settings.getGpsDistance();
		int gpsAccuracy = settings.getGpsAccuracy();
		accuracy = accuracyLevel[gpsAccuracy];
		//gpsInterval = 0; gpsDistance = 0;
		Log.i(TAG,"gpsInterval:"+gpsInterval+" gpsDistance:"+gpsDistance);
		if (locationManager != null) {
		    // プロバイダの状態を表示
		    //List<String> providers = locationManager.getAllProviders();
		    List<String> providers = locationManager.getProviders(true);
		    for (String provider : providers) {
				Log.i(TAG,"provider name:"+provider);
			    String e = locationManager.isProviderEnabled(provider) ? "ENABLED" : "DISABLED";
			    Log.i(TAG,"provider enable:"+e);
				locationManager.requestLocationUpdates(
						provider
						, (gpsInterval * 1000)  //測位間隔(ミリ秒)
						, gpsDistance			//測位距離(m)
						, listener);
		    }
		}
		setListener(listener);
	}

	public void removeUpdates(AspitsManagerListener listener) {
		if (locationManager != null) {
			locationManager.removeUpdates(listener);
		}
	}
	
	/**
	 * ロケーションの精度情報をチェックする
	 * @param location
	 * @return
	 */
	public boolean checkLocationAccuracy(Location location) {
		if (location == null) return false;
		logLocation(location);
		boolean isOK = true;
		//TODO ここのロジックを修正
		if (!LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
			Log.d(TAG,"GPS情報でない。location.provider:"+location.getProvider());
			isOK = false;
		}
		if (location.getAccuracy() > accuracy) {
			Log.d(TAG,"精度が悪い。比較するaccuracy:"+accuracy);
			isOK = false;
		}
		return isOK;
	}
	
	
	
	
	
	protected void logLocation(Location location) {
		Log.d(TAG,
		"logLocation"
        + " Time:" + ApplDateFormat.formatUpdateDatet(location.getTime(), "MM/dd HH:mm:ss SSS")
        + " Latitude:" + String.valueOf(location.getLatitude())
        + " Longitude:" + String.valueOf(location.getLongitude())
        + " Accuracy:" + String.valueOf(location.getAccuracy())
        + " Speed:" + String.valueOf(location.getSpeed())
        + " Bearing:" + String.valueOf(location.getBearing())
        + " Altitude:" + String.valueOf(location.getAltitude())
        );
	}

	Timer timer;
	public void startManager(int msec) {
		Log.i(TAG, "startManager");
        ApplTimerTask timerTask = new ApplTimerTask(new Runnable() {
			@Override
			public void run() {
				listener.didPost();
			}
		});
        timer = ActivityUtil.scheduleInterval(msec, timerTask);
    }

	public void endManager() {
		if (timer != null) {
			timer.cancel();
		}
		Log.i(TAG, "endManager");
	}
	
	/**
	 * 業務コードとロケーション情報からデータを作成し、送信する
	 * @param gyomuCode
	 * @param loc
	 * @return
	 */
	public boolean doPost(String gyomuCode, Location loc) {
		ArrayList<String> dataArray = new ArrayList<String>();
		dataArray.add(DrivingHistory.toData(gyomuCode, loc));
		setDataArray(dataArray);
		return doPost();
	}
	
	/**
	 * 非同期通信でPOST処理を行う
	 * @return
	 * @throws IOException
	 */
	public boolean doPost() {
		if (!isAspitsConn()) {
			Log.w(TAG, "ASPITS送信設定がOFFです");
			return true;
		}
		try {
			//端末IDの暗号化
    		Log.i("doPost","getTerminalId:"+getTerminalId());
			String encTerminalId = Encryption.encryptAES(getTerminalId(),ASPITS_KEY);
    		Log.i("doPost","encTerminalId:["+encTerminalId+"]");
			//ファイルの圧縮
    		File dir = ((Context)getListener()).getFilesDir();
			File gzipFile = new File(dir,ASPITS_GZIP_FILE_NAME);
        	Log.i("doPost","gzipFile:"+gzipFile.getAbsolutePath());
			FileUtil.gzip(gzipFile, toDataString(getDataArray()));//gzipで圧縮
			//パラメータの設定
			RequestParams params = new RequestParams();
			params.put("ID",encTerminalId);
		    params.put("file", new FileInputStream(gzipFile), gzipFile.getName(),"application/x_gzip");
		    //client.addHeader("", arg1)
	   		Log.i("doPost","ASPITS_URL:"+ASPITS_URL);
	   		//送信処理
		    post(params);
			return true;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	/**
	 * 送信処理
	 * @param params
	 */
	protected void post(RequestParams params) {
		client.post(ASPITS_URL, params, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				//Log.i("doPost","onStart");
				posting = true;
				listener.onPostStart();
			}
			@Override
			public void onFinish() {
				//Log.i("doPost","onFinish");
				posting = false;
				listener.onPostFinish();
			}
			@Override
			public void onSuccess(int arg0, String arg1) {
				//Log.i("doPost","onSuccess");
				listener.onPostSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				//Log.i("doPost","onFailure");
				listener.onPostFailure(arg0,arg1);
			}
		});
	}
	
	public String toFixedRow(String[] cols) {
	    //固定長に変換してセットする
	    StringBuffer buf = new StringBuffer();
	    //作成日時
	    buf.append(cols[0]);//yyyyMMddHHmmss
	    //端末識別ID
	    buf.append(BeanUtil.rpad(getTerminalId(), 15));
	    //データ種別
	    buf.append(toDataShubetsu(cols[6]));
	    //測位日時
	    buf.append(cols[1]);
	    //測位（緯度）
	    buf.append(toNum(cols[2]));
	    //測位（経度）
	    buf.append(toNum(cols[3]));
	    //速度
	    buf.append(toNum(cols[4]));
	    //方角
	    buf.append(toNum(cols[5]));
	    //測位モード
	    buf.append("2");//1:XyzD 2:2D(固定)
	    //測位精度劣化度
	    buf.append("0225");		//取得できないため、固定
	    //受信衛星数
	    buf.append("07");		//取得できないため、固定
	    //業務種別
	    buf.append(BeanUtil.lpad(cols[6], 2, "0"));
	    //拡張データ
	    buf.append("0000");
	    //改行
	    buf.append("\r\n");
	    return buf.toString();
	}

	public String toDataShubetsu(String val) {
		if (val == null) return val;
		if ("0".equals(val)||"00".equals(val)) {
			return "1";
		}
		return "3";
	}

	public String toNum(String num) {
		return BeanUtil.replace(num, ".", "");
	}
	
	public String toDataString(ArrayList<String> arr) {
		if (arr == null || arr.isEmpty()) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		for(int i=0; i< arr.size(); i++) {
			String row = arr.get(i);
			buf.append(toFixedRow(row.split(",")));//カンマ区切り
		}
		return buf.toString();
	}
	

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public ArrayList<String> getDataArray() {
		return dataArray;
	}

	public void setDataArray(ArrayList<String> dataArray) {
		this.dataArray = dataArray;
	}

	public AspitsManagerListener getListener() {
		return listener;
	}

	public void setListener(AspitsManagerListener listener) {
		this.listener = listener;
	}

	public boolean isAspitsConn() {
		return aspitsConn;
	}
	public void setAspitsConn(boolean aspitsConn) {
		this.aspitsConn = aspitsConn;
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	public void setLocationManager(LocationManager locationManager) {
		this.locationManager = locationManager;
	}

	public boolean isPosting() {
		return posting;
	}

	public void setPosting(boolean posting) {
		this.posting = posting;
	}

}
