package jp.co.seino.sis.prototype.common;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ActivityUtil {

	/**
	 * 下位互換と画面サイズの取得
	 * @param display
	 * @param outSize
	 */
	public static void displaySize(Display display, Point outSize) {
		try {
			// test for new method to trigger exception
			Class<?> pointClass;
			pointClass = Class.forName("android.graphics.Point");
			Method newGetSize = Display.class.getMethod("getSize", new Class[]{ pointClass });
			// no exception, so new method is available, just use it
			newGetSize.invoke(display, outSize);
		} catch(Exception ex) {
			// new method is not available, use the old ones
			outSize.x = display.getWidth();
			outSize.y = display.getHeight();
		}
	}
	
	/**
	 * Activityへ遷移する
	 * @param class1
	 */
	public static void toActivity(Context context, Class<?> class1) {
		ActivityUtil.toActivity(context, class1, null);
	}

	/**
	 * Activityへ遷移する
	 * @param class1
	 * @param パラメータ
	 */
	public static void toActivity(Context context, Class<?> class1, Map<String, String> param) {
		Intent it = new Intent(context, class1);
        if (it != null) {
            //パラメータのセット
        	if (param != null && !param.isEmpty()){
	        	for(String key: param.keySet()) {
	        		it.putExtra(key, param.get(key));
	        	}
	        }
            //画面遷移
            try{
            	((Activity)context).startActivityForResult(it, 0);
            } catch(ActivityNotFoundException e){
            	throw new RuntimeException(e);
            }
    	}
	}

	/**
	 * リスナーの追加
	 * @param id
	 */
	public static void setListener(Activity activity, int id) {
		View vw = activity.findViewById(id);
		if (vw instanceof Button) {
			((Button)activity.findViewById(id)).setOnClickListener((OnClickListener)activity);
		}
		if (vw instanceof ImageButton) {
			((ImageButton)activity.findViewById(id)).setOnClickListener((OnClickListener)activity);
		}
		if (vw instanceof SeekBar) {
			((SeekBar)activity.findViewById(id)).setOnSeekBarChangeListener((OnSeekBarChangeListener)activity);
		}
	}
	
	/**
	 * ダイアログの初期化
	 * @param context
	 * @param message
	 * @return
	 */
	public static ProgressDialog initProgressDialog(Context context,int resid) {
		String message = context.getResources().getString(resid);
		return initProgressDialog(context, message);
	}
	/**
	 * ダイアログの初期化
	 * @param context
	 * @param message
	 * @return
	 */
	public static ProgressDialog initProgressDialog(Context context,String message) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(message);
		progressDialog.setCancelable(true);
		progressDialog.show();
		return progressDialog;
	}
	
	public static Timer scheduleAfter(int msec, TimerTask timerTask) {
		Timer timer = new Timer();
		timer.schedule(timerTask, msec);//ミリ秒後に起動
		return timer;
	}
	
	public static Timer scheduleInterval(int msec, TimerTask timerTask) {
		Timer timer = new Timer();
		timer.schedule(timerTask, 0, msec);//ミリ秒間隔に起動
		return timer;
	}
	
	public static void soundAlert(Context context) {
		ApplSetting settings = ApplSetting.initSetting(context);
		if (settings.isAlertSound()) {//警告音を鳴らす場合のみ
			final ToneGenerator toneGenerator
			= new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
			toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR);//音の種類
			Handler hundler = new Handler();
			hundler.postDelayed(new Runnable() {
				@Override
				public void run() {
					toneGenerator.stopTone();
				}
			}, 1000);//1秒後に停止
		}
	}
	
	public static void soundButton(Context context) {
		final ToneGenerator toneGenerator
		= new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
		toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);//音の種類
		Handler hundler = new Handler();
		hundler.postDelayed(new Runnable() {
			@Override
			public void run() {
				toneGenerator.stopTone();
			}
		}, 1000);//1秒後に停止
		//バイブレーションする
		vibrator(context, 100);
	}
	
	public static void vibrator(Context context, int msec) {
		Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		if (vib != null ) {
			vib.vibrate(msec);
		}
	}
	
}
