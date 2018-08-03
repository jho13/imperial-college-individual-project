package richard.oh.android.app;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.display.DisplayManager;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Display;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import richard.oh.android.app.database.AppBaseHelper;
import richard.oh.android.app.database.AppDbSchema.AppTable;

/**
 * Created by Richard on 27/07/2017.
 */

public class AppMonitorService extends IntentService {

    private static final String TAG = "AppMonitorService";

    public static Intent newIntent(Context context) {
        return new Intent(context, AppMonitorService.class);
    }

    public AppMonitorService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context context, long monitor_interval_ms, boolean isOn) {
        Intent i = AppMonitorService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    monitor_interval_ms, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = AppMonitorService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SQLiteDatabase database = new AppBaseHelper(getApplicationContext()).getWritableDatabase();
        SharedPreferences settings = getSharedPreferences(SettingsFragment.PREFS_NAME, 0);

        Date date = new Date();

        // battery stats
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = Math.round((100 * level) / (float)scale);

        // monitorCycle
        int monitorCycle = settings.getInt(SettingsFragment.PREFS_MONITOR_CYCLE, 0);

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        PackageManager pm = getPackageManager();
        ApplicationInfo applicationInfo = null;
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo currentProcess = null;
        int i = 0;
        if (processes == null) {
            return;
        }

        // isForeground
        int isForeground = 0;
        if (Build.VERSION.SDK_INT >= 20) {
            DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    isForeground = 1;
                }
            }
        } else {
            isForeground = Math.random() > 0.9 ? 1 : 0;
        }

        // CPU
        double cpu;
        int maxCPU = 0, index = -1;
        if (isForeground == 1) {
            while (processes.get(i).importance !=
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                i++;
            }
            currentProcess = processes.get(i);

            int top, bottom = 0;
            String stats = null, totalStats = null;
            try {
                String fileName = "/proc/" + currentProcess.pid + "/stat";
                RandomAccessFile reader = new RandomAccessFile(fileName, "r");
                stats = reader.readLine();
                String fileName2 = "/proc/stat";
                reader = new RandomAccessFile(fileName2, "r");
                totalStats = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] statsList = stats.split(" ");
            top = - Integer.parseInt(statsList[13]) - Integer.parseInt(statsList[14]);
            String[] totalStatsList = totalStats.split(" ");
            Log.i(TAG, totalStats);
            for (String num : totalStatsList) {
                int temp2;
                try {
                    temp2 = Integer.parseInt(num);
                } catch (NumberFormatException e) {
                    temp2 = 0;
                }
                bottom -= temp2;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stats = null; totalStats = null;
            try {
                String fileName = "/proc/" + currentProcess.pid + "/stat";
                RandomAccessFile reader = new RandomAccessFile(fileName, "r");
                stats = reader.readLine();
                String fileName2 = "/proc/stat";
                reader = new RandomAccessFile(fileName2, "r");
                totalStats = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            statsList = stats.split(" ");
            top += Integer.parseInt(statsList[13]) + Integer.parseInt(statsList[14]);
            totalStatsList = totalStats.split(" ");
            for (String num : totalStatsList) {
                int temp2;
                try {
                    temp2 = Integer.parseInt(num);
                } catch (NumberFormatException e) {
                    temp2 = 0;
                }
                bottom += temp2;
            }

            cpu = (double) 100 * top / (double) bottom;

        } else {
            int bottom = 0;
            if (Math.random() > (1.0/Integer.parseInt(settings.getString(SettingsFragment.PREFS_APP_INTERVAL, "60")))) {
                int position = 0;
                while (position < processes.size() && processes.get(position).processName != "richard.oh.android.app") {
                    position++;
                }
                if (position < processes.size()) {
                    processes.remove(position);
                }
            }

            int[] usages = new int[processes.size()];
            i = 0;
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                String stats = null;
                try {
                    String fileName = "/proc/" + process.pid + "/stat";
                    RandomAccessFile reader = new RandomAccessFile(fileName, "r");
                    stats = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String[] statsList = stats.split(" ");
                usages[i] = - Integer.parseInt(statsList[13]) - Integer.parseInt(statsList[14]);
                i++;
            }

            String totalStats = null;
            try {
                String fileName2 = "/proc/stat";
                RandomAccessFile reader = new RandomAccessFile(fileName2, "r");
                totalStats = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] totalStatsList = totalStats.split(" ");
            for (String num : totalStatsList) {
                int temp2;
                try {
                    temp2 = Integer.parseInt(num);
                } catch (NumberFormatException e) {
                    temp2 = 0;
                }
                bottom -= temp2;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i = 0;
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                String stats = null;
                try {
                    String fileName2 = "/proc/" + process.pid + "/stat";
                    RandomAccessFile reader = new RandomAccessFile(fileName2, "r");
                    stats = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String[] statsList = stats.split(" ");
                usages[i] += Integer.parseInt(statsList[13]) + Integer.parseInt(statsList[14]);
                i++;
            }

            totalStats = null;
            try {
                String fileName2 = "/proc/stat";
                RandomAccessFile reader = new RandomAccessFile(fileName2, "r");
                totalStats = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            totalStatsList = totalStats.split(" ");
            for (String num : totalStatsList) {
                int temp2;
                try {
                    temp2 = Integer.parseInt(num);
                } catch (NumberFormatException e) {
                    temp2 = 0;
                }
                bottom += temp2;
            }

            for (int j = 0; j < processes.size(); j++) {
                if (usages[j] > maxCPU) {
                    maxCPU = usages[j];
                    index = j;
                }
            }
            if (index != -1) {
                currentProcess = processes.get(index);
            }

            cpu = (double) maxCPU * 100 / bottom;
        }

        // app name
        String applicationName;
        if (isForeground == 0 && index == -1) {
            applicationName = "Idle";
        } else {
            try {
                applicationInfo = pm.getApplicationInfo(currentProcess.processName, 0);
                if (applicationInfo != null) {
                    applicationName = (String) pm.getApplicationLabel(applicationInfo);
                } else {
                    return;
                }
            } catch (final PackageManager.NameNotFoundException e) {
                return;
            }
        }

        ContentValues values = new ContentValues();
        values.put(AppTable.Cols.DATE, date.getTime());
        values.put(AppTable.Cols.BATTERY, batteryPct);
        values.put(AppTable.Cols.APP_NAME, applicationName);
        values.put(AppTable.Cols.MONITOR_CYCLE, monitorCycle);
        values.put(AppTable.Cols.IS_FOREGROUND, isForeground);

        if (!applicationName.equals("Idle")) {

            // app memory
            Debug.MemoryInfo memoryInfo =
                    am.getProcessMemoryInfo(new int[]{processes.get(0).pid})[0];
            int applicationMemory = memoryInfo.getTotalPss();

            // app upload
            long applicationUpload = TrafficStats.getUidTxBytes(applicationInfo.uid);

            // app download
            long applicationDownload = TrafficStats.getUidRxBytes(applicationInfo.uid);

            // restartCycle
            int restartCycle = settings.getInt(SettingsFragment.PREFS_RESTART_CYCLE, 0);

            values.put(AppTable.Cols.APP_PACKAGE_NAME, currentProcess.processName);
            values.put(AppTable.Cols.APP_CPU, cpu);
            values.put(AppTable.Cols.APP_MEMORY, (long) applicationMemory);
            values.put(AppTable.Cols.APP_UPLOAD, applicationUpload);
            values.put(AppTable.Cols.APP_DOWNLOAD, applicationDownload);
            values.put(AppTable.Cols.RESTART_CYCLE, restartCycle);
            Log.i(TAG, Long.toString(date.getTime()));
            Log.i(TAG, "\nPROCESS");
            Log.i(TAG, currentProcess.processName);
            Log.i(TAG, applicationName);
            Log.i(TAG, Integer.toString(applicationMemory));
            Log.i(TAG, Integer.toString(monitorCycle));
            Log.i(TAG, "foreground" + isForeground);
        }
        database.insert(AppTable.NAME, null, values);

        /*
        .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("High battery temperature can drastically reduce battery" +
                        " life - try to avoid overheating your device."))
          */
    }
}