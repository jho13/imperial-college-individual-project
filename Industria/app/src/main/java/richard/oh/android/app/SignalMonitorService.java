package richard.oh.android.app;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

import richard.oh.android.app.database.SignalDbSchema.SignalTable;

/**
 * Created by Richard on 20/07/2017.
 */

public class SignalMonitorService extends IntentService {

    private static final String TAG = "SignalMonitorService";

    private int mWifiSignalStrength;
    private int mCellularSignalStrength;

    public static Intent newIntent(Context context) {
        return new Intent(context, SignalMonitorService.class);
    }

    public SignalMonitorService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context context, long monitor_interval_ms, boolean isOn) {
        Intent i = SignalMonitorService.newIntent(context);
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
        Intent i = SignalMonitorService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Date date = new Date();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mWifiSignalStrength = wm.getConnectionInfo().getRssi();

        //Log.i(TAG, Integer.toString(mWifiSignalStrength));

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        PhoneStateListener psl = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String ssignal = signalStrength.toString();
                String[] parts = ssignal.split(" ");
                Log.i(TAG, "Start");
                Log.i(TAG, parts[8]);
                Log.i(TAG, parts[9]);
                Log.i(TAG, parts[10]);
                Log.i(TAG, parts[11]);
                Log.i(TAG, "End");
            }
        };
        tm.listen(psl, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        tm.listen(psl, PhoneStateListener.LISTEN_NONE);


        ContentValues values = new ContentValues();
        values.put(SignalTable.Cols.DATE, date.toString());
        /*
        String empty = null;
        if (info != null && info.isConnected() &&
                info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) getApplicationContext()
                    .getSystemService(WIFI_SERVICE);
            int signalStrength = wifiManager.getConnectionInfo().getRssi();
            values.put(SignalTable.Cols.WIFI_STRENGTH, Integer.toString(signalStrength));
            values.put(SignalTable.Cols.CELLULAR_TYPE, empty);
            values.put(SignalTable.Cols.CELLULAR_STRENGTH, empty);
        } else if (info != null && info.isConnected() &&
                info.getType() == ConnectivityManager.TYPE_MOBILE){
            values.put(SignalTable.Cols.WIFI_STRENGTH, empty);
            values.put(SignalTable.Cols.CELLULAR_TYPE, info.getSubtypeName());
            values.put(SignalTable.Cols.CELLULAR_STRENGTH, empty);
        } else {
            values.put(SignalTable.Cols.WIFI_STRENGTH, empty);
            values.put(SignalTable.Cols.CELLULAR_TYPE, empty);
            values.put(SignalTable.Cols.CELLULAR_STRENGTH, empty);
        }
        */
    }
}
