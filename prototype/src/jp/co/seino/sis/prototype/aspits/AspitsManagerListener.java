package jp.co.seino.sis.prototype.aspits;

import android.location.LocationListener;

public interface AspitsManagerListener extends LocationListener {
	public void onPostStart();
	public void onPostFinish();
	public void onPostSuccess(int code,String res);
	public void onPostFailure(Throwable arg0, String arg1);
	public void didPost();
}
