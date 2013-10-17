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
	 * ���ʌ݊��Ɖ�ʃT�C�Y�̎擾
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
	 * Activity�֑J�ڂ���
	 * @param class1
	 */
	public static void toActivity(Context context, Class<?> class1) {
		ActivityUtil.toActivity(context, class1, null);
	}

	/**
	 * Activity�֑J�ڂ���
	 * @param class1
	 * @param �p�����[�^
	 */
	public static void toActivity(Context context, Class<?> class1, Map<String, String> param) {
		Intent it = new Intent(context, class1);
        if (it != null) {
            //�p�����[�^�̃Z�b�g
        	if (param != null && !param.isEmpty()){
	        	for(String key: param.keySet()) {
	        		it.putExtra(key, param.get(key));
	        	}
	        }
            //��ʑJ��
            try{
            	((Activity)context).startActivityForResult(it, 0);
            } catch(ActivityNotFoundException e){
            	throw new RuntimeException(e);
            }
    	}
	}

	/**
	 * ���X�i�[�̒ǉ�
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
	 * �_�C�A���O�̏�����
	 * @param context
	 * @param message
	 * @return
	 */
	public static ProgressDialog initProgressDialog(Context context,int resid) {
		String message = context.getResources().getString(resid);
		return initProgressDialog(context, message);
	}
	/**
	 * �_�C�A���O�̏�����
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
		timer.schedule(timerTask, msec);//�~���b��ɋN��
		return timer;
	}
	
	public static Timer scheduleInterval(int msec, TimerTask timerTask) {
		Timer timer = new Timer();
		timer.schedule(timerTask, 0, msec);//�~���b�Ԋu�ɋN��
		return timer;
	}
	
	public static void soundAlert(Context context) {
		ApplSetting settings = ApplSetting.initSetting(context);
		if (settings.isAlertSound()) {//�x������炷�ꍇ�̂�
			final ToneGenerator toneGenerator
			= new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
			toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR);//���̎��
			Handler hundler = new Handler();
			hundler.postDelayed(new Runnable() {
				@Override
				public void run() {
					toneGenerator.stopTone();
				}
			}, 1000);//1�b��ɒ�~
		}
	}
	
	public static void soundButton(Context context) {
		final ToneGenerator toneGenerator
		= new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
		toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);//���̎��
		Handler hundler = new Handler();
		hundler.postDelayed(new Runnable() {
			@Override
			public void run() {
				toneGenerator.stopTone();
			}
		}, 1000);//1�b��ɒ�~
		//�o�C�u���[�V��������
		vibrator(context, 100);
	}
	
	public static void vibrator(Context context, int msec) {
		Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		if (vib != null ) {
			vib.vibrate(msec);
		}
	}
	
}
