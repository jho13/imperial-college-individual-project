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

/**
 * Created by Richard on 08/09/2017.
 */

public class TempService extends IntentService {

    public static final String TAG = "IntentService";
    public static final int NOTIFICATION_TEMP = 0;

    public TempService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, TempService.class );
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    300 * 1000, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, TempService.class );
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = getSharedPreferences(SettingsFragment.PREFS_NAME, 0);
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);

        int threshold = settings.getInt(SettingsFragment.PREFS_BATTERY_TEMP, 0);
        if (temp >= 10 * (threshold + 36)) {
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_warning)
                    .setContentTitle("Battery temperature - " + temp / 10.0 + "ºC")
                    .build();

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_TEMP, notification);
        }
    }
}
