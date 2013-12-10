package jp.spidernet.myphone;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

class BatteryInfoReceiver extends BroadcastReceiver {
	private static final String TAG = "BatteryInfoReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "onReceive was called");
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
			// 充電状態の取得
			String statusStr = "";
			int status = intent.getIntExtra("status", 0);
			if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
				statusStr = "充電中";
			} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
				statusStr = "充電切断";
			} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
				statusStr = "充電満タン";
			} else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
				statusStr = "充電切断中";
			} else if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
				statusStr = "不明";
			}

			// プラグ種別の取得
			String pluggedStr = "";
			int plugged = intent.getIntExtra("plugged", 0);
			if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
				pluggedStr = "ACアダプタ";
			} else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
				pluggedStr = "USB";
			}

			// バッテリー量の取得
			int level = intent.getIntExtra("level", 0);
			int scale = intent.getIntExtra("scale", 0);
			String batLevel = level + "%";
			// 温度の取得
			int temperature = intent.getIntExtra("temperature", 0);

			String str = "";
			str += "充電状態:" + statusStr + "\n";
			str += "プラグ種別:" + pluggedStr + "\n";
			str += "バッテリー量:" + level + "/" + scale + "\n";
			str += "温度:" + (temperature / 10) + "度\n";
			notifi(String.valueOf(batLevel), str, context);
		}
	}

	public void notifi(String level, String content, Context context) {
		NotificationManager mNotificationManager;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(level)
				.setContentText("Battery level:" + level);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());
	}
}
