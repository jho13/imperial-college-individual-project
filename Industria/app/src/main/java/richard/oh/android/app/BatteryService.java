package richard.oh.android.app;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by Richard on 08/09/2017.
 */

public class BatteryService extends IntentService {

    public static final String TAG = "BatteryService";
    public static final String PREFS_BATTERY = "battery";
    public static final int NOTIFICATION_REMAINING = 0;

    public BatteryService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, BatteryService.class );
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    120 * 1000, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, BatteryService.class );
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = getSharedPreferences(SettingsFragment.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = Math.round((100 * level) / (float)scale);

        if (batteryPct != settings.getInt(PREFS_BATTERY, 0)) {
            editor.putInt(PREFS_BATTERY, batteryPct);
            editor.commit();

            long duration = (long) (batteryPct * 1398324);
            Log.i(TAG, Long.toString(duration) + " " );
            String msg = Helper.getDurationBreakdownDHM(duration);

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_battery_100)
                    .setContentTitle("Battery - " + batteryPct + "%")
                    .setContentText(msg + " remaining")
                    .build();

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_REMAINING, notification);
        }
    }
}
