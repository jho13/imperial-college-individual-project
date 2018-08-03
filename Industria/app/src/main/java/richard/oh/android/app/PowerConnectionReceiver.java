package richard.oh.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import richard.oh.android.app.database.AppBaseHelper;
import richard.oh.android.app.database.AppDbSchema.AppTable;

/**
 * Created by Richard on 31/07/2017.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {

    public static final String TAG = "PCReceiver";

    @Override
    public void onReceive(Context context, Intent falseIntent) {
        SQLiteDatabase database = new AppBaseHelper(context).getWritableDatabase();
        SharedPreferences settings = context.getSharedPreferences(SettingsFragment.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        boolean charging = false;
        final Intent intent = context
                .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharge = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (batteryCharge) charging = true;
        if (usbCharge) charging = true;
        if (acCharge) charging = true;


        int duration = Integer
                .parseInt(settings.getString(SettingsFragment.PREFS_APP_INTERVAL, "60"));
        boolean isMonitoring = settings
                .getBoolean(SettingsFragment.PREFS_APP_MONITORING, false);

        if (isMonitoring) {
            System.out.println(charging);
            System.out.println(AppMonitorService.isServiceAlarmOn(context));
            if (AppMonitorService.isServiceAlarmOn(context) == charging) {
                AppMonitorService.setServiceAlarm(context, duration * 1000, /*!charging*/ true);
                if (!charging) {
                    Log.i(TAG, "not charging");
                    int cycle = (settings.getInt(SettingsFragment.PREFS_MONITOR_CYCLE, 0) + 1) % 1000;
                    editor.putInt(SettingsFragment.PREFS_MONITOR_CYCLE, cycle);
                    editor.commit();
                } else {
                    Log.i(TAG, "charging");
                    // delete data older than 1 month
                    Date date = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.DATE, -30);
                    Date limit = cal.getTime();
                    database.delete(AppTable.NAME,
                            AppTable.Cols.DATE + " < " + Long.toString(limit.getTime()) , null);

                    // generate actual battery levels and record battery drain rate
                    Helper.generateActualBattery(database);
                    float rate = Helper.computeRate(database);
                    Log.i(TAG, Float.toString(rate));
                    if (rate > 0) {
                        editor.putFloat(SettingsFragment.PREFS_DRAIN_RATE, rate);
                        editor.commit();
                    }
                }
            }
        }
    }
}