package jp.co.seino.sis.prototype.aspits;


public interface DriveManagerListener {
	public void onTimerEvent(double[] preEvent, double[] event);
	public void onAlertEvent(String eventType, double[] alertEvent);
}
