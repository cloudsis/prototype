package jp.co.seino.sis.prototype.common;

import java.util.TimerTask;

import android.os.Handler;

public class ApplTimerTask extends TimerTask {
	private Handler handler = new Handler();
	//private Context context;
	private Runnable runnable;

	public ApplTimerTask(Runnable runnable) {
		//this.context = context;
		this.runnable = runnable;
	}
	
	@Override
	public void run() {
		handler.post(runnable);
	}

}
