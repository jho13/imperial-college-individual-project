package richard.oh.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by Richard on 26/08/2017.
 */

public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent falseIntent) {
        SharedPreferences settings = context.getSharedPreferences(SettingsFragment.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        int restartCycle = (settings.getInt(SettingsFragment.PREFS_RESTART_CYCLE, 0) + 1) % 1000;
        editor.putInt(SettingsFragment.PREFS_RESTART_CYCLE, restartCycle);
        editor.commit();

        int monitorCycle = (settings.getInt(SettingsFragment.PREFS_MONITOR_CYCLE, 0) + 1) % 1000;
        editor.putInt(SettingsFragment.PREFS_MONITOR_CYCLE, monitorCycle);
        editor.commit();
    }
}
