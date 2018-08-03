package richard.oh.android.app;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

/**
 * Created by Richard on 26/07/2017.
 */

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private static final String DIALOG_REP = "DialogRep";
    private static final String DIALOG_TEMP = "DialogTemp";

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREFS_APP_MONITORING = "AppMonitoring";
    public static final String PREFS_APP_INTERVAL = "AppInterval";
    public static final String PREFS_APP_DISPLAY = "AppDisplay"; // 0 = list
    public static final String PREFS_APP_DAYS = "AppDays";
    public static final String PREFS_APP_HOURS = "AppHours";
    public static final String PREFS_REMAINING_BATTERY = "RemainingBattery";
    public static final String PREFS_BATTERY_TEMP = "BatteryTemp";
    public static final String PREFS_MONITOR_CYCLE = "ChargeCycle";
    public static final String PREFS_RESTART_CYCLE = "RestartCycle";
    public static final String PREFS_DRAIN_RATE = "DrainRate";

    private ToggleButton mAppMonitorButton;
    private EditText mAppIntervalEditText;
    private ConstraintLayout mAppDisplay;
    private EditText mAppDays;
    private EditText mAppHours;

    private ToggleButton mRemainingBatteryButton;
    private ConstraintLayout mBatteryTempButton;

    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mSettings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        mEditor = mSettings.edit();

        mAppMonitorButton = (ToggleButton) view.findViewById(R.id.app_monitoring_button);
        mAppMonitorButton.setChecked(mSettings.getBoolean(PREFS_APP_MONITORING, false));
        mAppMonitorButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !isCharging()) {
                    if (!AppMonitorService.isServiceAlarmOn(getActivity())) {
                        int duration = Integer.parseInt(mSettings.getString(PREFS_APP_INTERVAL, "60"));
                        AppMonitorService.setServiceAlarm(getActivity(), duration * 1000, true);
                        int cycle = (mSettings.getInt(SettingsFragment.PREFS_MONITOR_CYCLE, 0) + 1) % 1000;
                        mEditor.putInt(SettingsFragment.PREFS_MONITOR_CYCLE, cycle);
                        mEditor.commit();
                    }
                } else {
                    if (AppMonitorService.isServiceAlarmOn(getActivity())) {
                        AppMonitorService.setServiceAlarm(getActivity(), 60 * 1000, false);
                    }
                }
                mEditor.putBoolean(PREFS_APP_MONITORING, isChecked);
                mEditor.commit();
            }
        });



        mAppIntervalEditText = (EditText) view.findViewById(R.id.app_interval_editText);
        mAppIntervalEditText.setText(mSettings.getString(PREFS_APP_INTERVAL, "60"));
        mAppIntervalEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            String textBefore = "";
            String textAfter = "";
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    textBefore = mAppIntervalEditText.getText().toString();
                } else {
                    String interval;
                    textAfter = mAppIntervalEditText.getText().toString();
                    if (isPositiveInteger(textAfter)) {
                        interval = textAfter;
                    } else {
                        interval = textBefore;
                    }
                    mEditor.putString(PREFS_APP_INTERVAL, interval);
                    mEditor.commit();
                    mAppIntervalEditText.setText(interval);
                }
            }
        });

        mAppDisplay = (ConstraintLayout) view.findViewById(R.id.app_rep);
        mAppDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DisplayDialogFragment dialog = new DisplayDialogFragment();
                dialog.show(manager, DIALOG_REP);
            }
        });

        mAppDays = (EditText) view.findViewById(R.id.app_interval_days);
        mAppDays.setText(mSettings.getString(PREFS_APP_DAYS, "0"));
        mAppDays.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            String textBefore = "";
            String textAfter = "";
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    textBefore = mAppDays.getText().toString();
                } else {
                    String days;
                    textAfter = mAppDays.getText().toString();
                    if (isPositiveInteger(textAfter) || textAfter == "0") {
                        days = textAfter;
                    } else {
                        days = textBefore;
                    }
                    mEditor.putString(PREFS_APP_DAYS, days);
                    mEditor.commit();
                    mAppDays.setText(days);
                }
            }
        });

        mAppHours = (EditText) view.findViewById(R.id.app_interval_hours);
        mAppHours.setText(mSettings.getString(PREFS_APP_HOURS, "24"));
        mAppHours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            String textBefore = "";
            String textAfter = "";
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    textBefore = mAppHours.getText().toString();
                } else {
                    String hours;
                    textAfter = mAppHours.getText().toString();
                    if (isPositiveInteger(textAfter) || textAfter == "0") {
                        hours = textAfter;
                    } else {
                        hours = textBefore;
                    }
                    mEditor.putString(PREFS_APP_HOURS, hours);
                    mEditor.commit();
                    mAppHours.setText(hours);
                }
            }
        });

        mRemainingBatteryButton = (ToggleButton) view.findViewById(R.id.remaining_battery_button);
        mRemainingBatteryButton.setChecked(BatteryService.isServiceAlarmOn(getActivity()));
        mRemainingBatteryButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BatteryService.setServiceAlarm(getActivity(), isChecked);
            }
        });

        mBatteryTempButton = (ConstraintLayout) view.findViewById(R.id.temp);
        mBatteryTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TempDialogFragment dialog = new TempDialogFragment();
                dialog.show(manager, DIALOG_TEMP);
                boolean on = mSettings.getInt(PREFS_BATTERY_TEMP, 5) != 5;
                TempService.setServiceAlarm(getActivity(), on);
            }
        });

        return view;
    }

    private boolean isPositiveInteger(String str) {
        if (str.length() == 0) {
            return false;
        }
        if (str.charAt(0) < '1' || str.charAt(0) > '9') {
            return false;
        }

        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9') {
                return false;
            }
        }

        return true;
    }

    private boolean isCharging() {
        boolean charging = false;
        final Intent batteryIntent = getActivity()
                .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharge = (status == BatteryManager.BATTERY_STATUS_CHARGING);

        int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (batteryCharge) charging = true;
        if (usbCharge) charging = true;
        if (acCharge) charging = true;

        return charging;
    }
}
