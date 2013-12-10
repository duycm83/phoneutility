package jp.spidernet.myphone.adlantis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import jp.Adlantis.Android.AdlantisView;
import jp.spidernet.myphone.AlarmReceiver;

public class AdlantisUtil {
	private static final int REPEATING_TIME = 15*60*1000; //10分
	private static final String TAG = "StartUpService";
	private static Intent intent = null;
	public static void startAtlantis(Context context) {
		if (intent == null) {
			intent = new Intent(context,
					AlarmReceiver.class);
		} else {
			return;
		}
		PendingIntent mAlarmSender = PendingIntent.getBroadcast(context, 0, intent, 0);
		// Schedule the alarm!
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		// repeat
		am.cancel(mAlarmSender);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,
				REPEATING_TIME, mAlarmSender);
	}
}
