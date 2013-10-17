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
		    // �v���o�C�_�̏�Ԃ�\��
		    //List<String> providers = locationManager.getAllProviders();
		    List<String> providers = locationManager.getProviders(true);
		    for (String provider : providers) {
				Log.i(TAG,"provider name:"+provider);
			    String e = locationManager.isProviderEnabled(provider) ? "ENABLED" : "DISABLED";
			    Log.i(TAG,"provider enable:"+e);
				locationManager.requestLocationUpdates(
						provider
						, (gpsInterval * 1000)  //���ʊԊu(�~���b)
						, gpsDistance			//���ʋ���(m)
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
	 * ���P�[�V�����̐��x�����`�F�b�N����
	 * @param location
	 * @return
	 */
	public boolean checkLocationAccuracy(Location location) {
		if (location == null) return false;
		logLocation(location);
		boolean isOK = true;
		//TODO �����̃��W�b�N���C��
		if (!LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
			Log.d(TAG,"GPS���łȂ��Blocation.provider:"+location.getProvider());
			isOK = false;
		}
		if (location.getAccuracy() > accuracy) {
			Log.d(TAG,"���x�������B��r����accuracy:"+accuracy);
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
	 * �Ɩ��R�[�h�ƃ��P�[�V������񂩂�f�[�^���쐬���A���M����
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
	 * �񓯊��ʐM��POST�������s��
	 * @return
	 * @throws IOException
	 */
	public boolean doPost() {
		if (!isAspitsConn()) {
			Log.w(TAG, "ASPITS���M�ݒ肪OFF�ł�");
			return true;
		}
		try {
			//�[��ID�̈Í���
    		Log.i("doPost","getTerminalId:"+getTerminalId());
			String encTerminalId = Encryption.encryptAES(getTerminalId(),ASPITS_KEY);
    		Log.i("doPost","encTerminalId:["+encTerminalId+"]");
			//�t�@�C���̈��k
    		File dir = ((Context)getListener()).getFilesDir();
			File gzipFile = new File(dir,ASPITS_GZIP_FILE_NAME);
        	Log.i("doPost","gzipFile:"+gzipFile.getAbsolutePath());
			FileUtil.gzip(gzipFile, toDataString(getDataArray()));//gzip�ň��k
			//�p�����[�^�̐ݒ�
			RequestParams params = new RequestParams();
			params.put("ID",encTerminalId);
		    params.put("file", new FileInputStream(gzipFile), gzipFile.getName(),"application/x_gzip");
		    //client.addHeader("", arg1)
	   		Log.i("doPost","ASPITS_URL:"+ASPITS_URL);
	   		//���M����
		    post(params);
			return true;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	/**
	 * ���M����
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
	    //�Œ蒷�ɕϊ����ăZ�b�g����
	    StringBuffer buf = new StringBuffer();
	    //�쐬����
	    buf.append(cols[0]);//yyyyMMddHHmmss
	    //�[������ID
	    buf.append(BeanUtil.rpad(getTerminalId(), 15));
	    //�f�[�^���
	    buf.append(toDataShubetsu(cols[6]));
	    //���ʓ���
	    buf.append(cols[1]);
	    //���ʁi�ܓx�j
	    buf.append(toNum(cols[2]));
	    //���ʁi�o�x�j
	    buf.append(toNum(cols[3]));
	    //���x
	    buf.append(toNum(cols[4]));
	    //���p
	    buf.append(toNum(cols[5]));
	    //���ʃ��[�h
	    buf.append("2");//1:XyzD 2:2D(�Œ�)
	    //���ʐ��x�򉻓x
	    buf.append("0225");		//�擾�ł��Ȃ����߁A�Œ�
	    //��M�q����
	    buf.append("07");		//�擾�ł��Ȃ����߁A�Œ�
	    //�Ɩ����
	    buf.append(BeanUtil.lpad(cols[6], 2, "0"));
	    //�g���f�[�^
	    buf.append("0000");
	    //���s
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
			buf.append(toFixedRow(row.split(",")));//�J���}��؂�
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
