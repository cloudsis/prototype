package jp.co.seino.sis.prototype.map;

import jp.co.seino.sis.prototype.MainActivity;
import jp.co.seino.sis.prototype.R;
import jp.co.seino.sis.prototype.aspits.DrivingHistory;
import jp.co.seino.sis.prototype.common.ActivityUtil;
import jp.co.seino.sis.prototype.common.ApplDateFormat;
import jp.co.yahoo.android.maps.GeoPoint;
import jp.co.yahoo.android.maps.MapActivity;
import jp.co.yahoo.android.maps.MapController;
import jp.co.yahoo.android.maps.MapView;
import jp.co.yahoo.android.maps.MyLocationOverlay;
import jp.co.yahoo.android.maps.OverlayItem;
import jp.co.yahoo.android.maps.PinOverlay;
import jp.co.yahoo.android.maps.PolylineOverlay;
import jp.co.yahoo.android.maps.PopupOverlay;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class YahooMapActivity extends MapActivity implements OnClickListener {

	private static final int GEO_TO_INT = 1000000;
	public static final String API_KEY = "dj0zaiZpPTJwWXF3RnB5S3owQSZkPVlXazlhV2hMZEVJd056SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9ZWI-"; 
	protected final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	
	protected Button backButton;
	protected TextView title;
	protected MapView mapView;

	protected MyLocationOverlay myLocationOverlay;
	protected SubMyLocationOverlay subMyLocationOverlay;
	protected boolean usedMyLocation = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//地図表示の初期化
		initMapView();
	}

	protected void initMapView() {
        // タイトルバーのカスタマイズを設定可能にする
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_yahoo_map);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_back);
		//Title
        title = (TextView) findViewById(R.id.titlebar_map_title);
		//back.
		backButton = (Button) getWindow().findViewById(R.id.titlebar_map_bt_back);
		backButton.setOnClickListener(this);
        // 地図の初期化
		mapView = new MapView(this, API_KEY);
		LinearLayout parent = new LinearLayout(this);
		parent.setOrientation(LinearLayout.VERTICAL);
		parent.addView(mapView);
//        // 画面サイズを取得する
//        Display display = getWindowManager().getDefaultDisplay();
//        Point outSize = new Point();
//        ActivityUtil.displaySize(display, outSize);
//        //画面サイズにあわせて地図を表示する
//        int height = (int)(outSize.y*0.8);
//		Log.i("onCreate","window x:"+outSize.x+" y:"+outSize.y+ " height:"+height);
//		parent.addView(mapView, outSize.x, height);
//		//backButton = new Button(this);
//		//backButton.setText(R.string.button_back);
		setContentView(parent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (usedMyLocation) {
		    useMyLocation();
		}
	}

	@Override
	protected void onPause() {
		if (usedMyLocation) {
			pauseMyLocation();
		}
		super.onPause();
	}

	@Override
    public void onClick(View v){
		Log.i("", "onClick:"+v);
		if (v == backButton) {
	        ActivityUtil.toActivity(this,MainActivity.class);//メニューへもどる
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.yahoo_map, menu);
		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	protected void useMyLocation() {
//		//MyLocationOverlayインスタンス作成
//		myLocationOverlay = new MyLocationOverlay(getApplicationContext(), mapView);
//		//現在位置取得開始
//		myLocationOverlay.enableMyLocation();
//		//位置が更新されると、地図の位置も変わるよう設定
//		myLocationOverlay.runOnFirstFix(new Runnable(){
//			public void run() {
//				if (mapView.getMapController() != null) {
//					//現在位置を取得
//					GeoPoint p = myLocationOverlay.getMyLocation();
//					//地図移動
//					mapView.getMapController().animateTo(p);
//				}
//			}
//		});
//		//MapViewにMyLocationOverlayを追加。
//		mapView.getOverlays().add(myLocationOverlay);
		//追跡用のオーバレイを追加
		subMyLocationOverlay = new SubMyLocationOverlay(getApplicationContext(), mapView, this);
		//現在地を取得開始
		subMyLocationOverlay.enableMyLocation();
		//MapViewに追加
	    mapView.getOverlays().add(subMyLocationOverlay);
	    mapView.invalidate();
	}


	private void pauseMyLocation() {
//		if (myLocationOverlay != null) {
//			myLocationOverlay.disableMyLocation();
//		}
		if (subMyLocationOverlay != null) {
			subMyLocationOverlay.disableMyLocation();
		}
	}

	/**
	 * 縮尺の設定
	 * @param zoom
	 */
	protected void setZoom(int zoom) {
		MapController c = mapView.getMapController();
		c.setZoom(zoom); 				  //初期表示の縮尺を指定
	}

	/**
	 * 中心位置の設定
	 * @param location
	 */
	protected void setCenter(Location location) {
		setCenter(location.getLatitude(), location.getLongitude());
	}
	/**
	 * 中心位置の設定
	 * @param lat
	 * @param lng
	 */
	protected void setCenter(double lat, double lng) {
		MapController c = mapView.getMapController();
		GeoPoint point = new GeoPoint(toGeoInt(lat),toGeoInt(lng));
		c.setCenter(point); 				  //初期表示の縮尺を指定
	}
	
	/**
	 * 線を引く
	 * @param preLocation
	 * @param currentLocation
	 */
	protected void drawLine(Location preLocation, Location currentLocation) {
		preLocation = (preLocation == null) ? currentLocation : preLocation;
		double[] pre = {preLocation.getLatitude(), preLocation.getLongitude()};
		double[] current = {currentLocation.getLatitude(),currentLocation.getLongitude()};
		drawLine(pre, current);
	}
	
	/**
	 * 線を引く
	 * @param prePoint
	 * @param point
	 */
	protected void drawLine(double[] pre, double[] current) {
		GeoPoint prePoint = new GeoPoint(toGeoInt(pre[0]),toGeoInt(pre[1]));
		GeoPoint currentPoint = new GeoPoint(toGeoInt(current[0]),toGeoInt(current[1]));
		PolylineOverlay polylineOverlay = new PolylineOverlay(new GeoPoint[]{prePoint, currentPoint});
		mapView.getOverlays().add(polylineOverlay);
	}

	/**
	 * アノテーションを追加
	 * @param history
	 * @param gyomuCode
	 * @param location
	 */
	protected void addAnnotation(DrivingHistory history, String gyomuCode, Location location) {
		String time = ApplDateFormat.formatUpdateDatet(location.getTime(), "yyyyMMddHHmmss"); 
		addAnnotation(history, gyomuCode, location.getLatitude(),location.getLongitude(),time);
	}

	/**
	 * アノテーションを追加
	 * @param history
	 * @param gyomuCode
	 * @param lat
	 * @param lng
	 * @param time
	 */
	protected void addAnnotation(DrivingHistory history, String gyomuCode,
			double latitude, double longitude, String time) {
		int lat = toGeoInt(latitude);
		int lng = toGeoInt(longitude);
		String nichiji = ApplDateFormat.convertIgnore(time, "yyyyMMddHHmmss" ,"MM/dd HH:mm:ss");
		//アイコンの追加
		GeoPoint point = new GeoPoint(lat,lng);
		Drawable icon = history.toIcon(gyomuCode);
		String title = history.toTitle(gyomuCode);
		String subtitle = (title != null && !title.equals("")) ? null : nichiji;//日時
		//アイコンの追加
		PopupOverlay popupOverlay = new PopupOverlay(){
			@Override
			public void onTap(OverlayItem item){
				//ポップアップをタッチした際の処理
			}
		};
		mapView.getOverlays().add(popupOverlay);
		PinOverlay pinOverlay = new PinOverlay(icon);
		pinOverlay.setOnFocusChangeListener(popupOverlay);
		pinOverlay.addPoint(point, title, subtitle);
		mapView.getOverlays().add(pinOverlay);
	}
	
	public int toGeoInt(double d) {
		return (int)(d * GEO_TO_INT);//int型に変換;
	}
	
	public boolean isUsedMyLocation() {
		return usedMyLocation;
	}

	public void setUsedMyLocation(boolean usedMyLocation) {
		this.usedMyLocation = usedMyLocation;
	}
	
}
