package jp.co.seino.sis.prototype.map;

import jp.co.yahoo.android.maps.GeoPoint;
import jp.co.yahoo.android.maps.MapView;
import jp.co.yahoo.android.maps.MyLocationOverlay;
import android.app.Activity;
import android.content.Context;


/**
 * ���ݒn �ǐ՗p�̃I�[�o�[���C
 * @author Tetsuji Ishii
 *
 */
public class SubMyLocationOverlay extends MyLocationOverlay {

	MapView _mapView = null;
	Activity _activity = null;

	public SubMyLocationOverlay(Context context, MapView mapView, Activity activity) {
		super(context, mapView);
		_mapView = mapView;
		_activity = activity;
	}

	//���ݒn�X�V�̃��X�i�[�C�x���g
	@Override
	public void onLocationChanged(android.location.Location location) {
		super.onLocationChanged(location);
		if (_mapView.getMapController() != null) {
			//�ʒu���X�V�����ƒn�}�̈ʒu���ς���B
			GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6),(int) (location.getLongitude() * 1E6));
			_mapView.getMapController().animateTo(p);
			_mapView.invalidate();
		}
	}
}
