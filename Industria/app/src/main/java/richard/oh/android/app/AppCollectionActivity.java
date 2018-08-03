package richard.oh.android.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import richard.oh.android.app.database.AppBaseHelper;
import richard.oh.android.app.database.AppDbSchema;

/**
 * Created by Richard on 29/08/2017.
 */

public class AppCollectionActivity extends AppCompatActivity {

    public static final String TAG = "AppCollectionActivity";

    public static final String DIALOG_APP = "AppDialog";

    public static final String KEY_START = "start";
    public static final String KEY_END = "end";

    private FragmentManager mFragmentManager;
    private SharedPreferences mSettings;
    private SQLiteDatabase mDatabase;

    private List<App> mAppList;
    private double totalBatteryUsed;
    private Date mStart;
    private Date mEnd;

    public List<App> getAppList() {
        return mAppList;
    }

    public double getTotalBatteryUsed() {
        return totalBatteryUsed;
    }

    public Date getStart() {
        return mStart;
    }

    public void setStart(Date start) {
        mStart = start;
    }

    public Date getEnd() {
        return mEnd;
    }

    public void setEnd(Date end) {
        mEnd = end;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_collection);

        mFragmentManager = getSupportFragmentManager();
        mSettings = getSharedPreferences(SettingsFragment.PREFS_NAME, 0);
        mDatabase = new AppBaseHelper(this).getWritableDatabase();

        if (savedInstanceState != null) {
            mStart = new Date(savedInstanceState.getLong(KEY_START));
            mEnd = new Date(savedInstanceState.getLong(KEY_END));
        } else {
            mEnd = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(mEnd);
            cal.add(Calendar.DATE, -1 * Integer.parseInt(
                    mSettings.getString(SettingsFragment.PREFS_APP_DAYS, "0")));
            cal.add(Calendar.HOUR_OF_DAY, -1 * Integer.parseInt(
                    mSettings.getString(SettingsFragment.PREFS_APP_HOURS, "24")));
            mStart = cal.getTime();
        }

        Fragment intervalFragment =
                mFragmentManager.findFragmentById(R.id.interval_fragment_container);
        if (intervalFragment == null) {
            intervalFragment = new IntervalFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.interval_fragment_container, intervalFragment).commit();
        }

        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(KEY_START, mStart.getTime());
        savedInstanceState.putLong(KEY_END, mEnd.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.goto_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh:
                intent = new Intent(this, AppCollectionActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUI() {
        Helper.generateActualBattery(mDatabase);
        Helper.computeRate(mDatabase);
        mAppList = generateApps(mStart, mEnd);
        Log.i(TAG, mStart.toString() + " " + mEnd.toString());
        if (mAppList != null) {
            Log.i(TAG, " " + mAppList.size());
        }


        Fragment collectionFragment;
        if (mAppList == null) {
            totalBatteryUsed = 0;

            collectionFragment = new EmptyFragment();
        } else {
            totalBatteryUsed = 0;
            for (App app : mAppList) {
                totalBatteryUsed += app.getBBatteryUsed() + app.getFBatteryUsed();
            }

            if (mSettings.getInt(SettingsFragment.PREFS_APP_DISPLAY, 0) == 0) {
                collectionFragment = new AppListFragment();
            } else {
                collectionFragment = new AppPieChartFragment();
            }
        }
        mFragmentManager.beginTransaction()
                .replace(R.id.collection_fragment_container, collectionFragment).commit();
    }

    private List<App> generateApps(Date startDate, Date endDate) {
        Map<String, App> apps = new HashMap<>();

        // get all data within interval
        String whereClause = AppDbSchema.AppTable.Cols.DATE + " < " + Long.toString(endDate.getTime())
                + " AND " + AppDbSchema.AppTable.Cols.DATE + " > " + Long.toString(startDate.getTime());
        Cursor cursor = mDatabase.query(
                AppDbSchema.AppTable.NAME,
                null,
                whereClause,
                null,
                null,
                null,
                AppDbSchema.AppTable.Cols.DATE
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            // list of actual batteries
            int end = cursor.getCount();
            double[] actualBatteries = new double[end];

            int start = 0;
            cursor.moveToFirst();
            while (!cursor.isAfterLast() && !cursor.isNull(cursor.getColumnIndex(
                    AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))) {
                actualBatteries[start] = cursor.getDouble(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY));
                start++;
                cursor.moveToNext();
            }

            //Log.i(TAG, Integer.toString(start) + " " + Integer.toString(end));

            if (start < end) {
                int len = end - start;
                long[] dates = new long[len];
                double[] batteries = new double[len];
                int[] cycles = new int[len];
                double[] remainingActualBatteries;
                for (int i = 0; i < len; i++) {
                    cursor.moveToPosition(start + i);
                    dates[i] = cursor.getLong(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.DATE));
                    batteries[i] = cursor.getDouble(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.BATTERY));
                    cycles[i] = cursor.getInt(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.MONITOR_CYCLE));
                }
                remainingActualBatteries = Helper.computeActualBattery(dates, batteries, cycles);

                for (int i = 0; i < len; i++) {
                    actualBatteries[start + i] = remainingActualBatteries[i];
                }
            }

            long currentDate, previousDate = -1;
            double currentBattery, previousBattery = -1;
            int currentMonitorCycle, previousMonitorCycle = -1;
            int isForeground = -1;

            // work out information on apps
            App currentApp, previousApp = null;
            int j = 0;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String appName = cursor.getString(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.APP_NAME));
                currentDate = cursor.getLong(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.DATE));
                currentBattery = actualBatteries[j];
                currentMonitorCycle = cursor.getInt(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.MONITOR_CYCLE));
                isForeground = cursor.getInt(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.IS_FOREGROUND));

                // check if app is already in the list of apps
                if (apps.containsKey(appName)) {
                    currentApp = apps.get(appName);

                    if (!appName.equals("Idle")) {

                        if (isForeground == 1) {
                            int appearances = currentApp.getFAppearances() + 1;
                            double totalCpuUsage = currentApp.getFCPUUsage() *
                                    currentApp.getFAppearances() + cursor.getDouble(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_CPU));
                            long totalMemoryUsage = currentApp.getFMemoryUsage() *
                                    currentApp.getFAppearances() + cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_MEMORY));
                            currentApp.setFAppearances(appearances);
                            currentApp.setFCPUUsage(totalCpuUsage / appearances);
                            currentApp.setFMemoryUsage(totalMemoryUsage / appearances);

                            long currentFUpload = cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_UPLOAD));
                            long netUpload = currentApp.getFUpload();
                            int restartCycle = cursor.getInt(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.RESTART_CYCLE));
                            if (currentApp.getRestartCycle() != restartCycle) {
                                currentApp.setFUpload(netUpload + currentFUpload);
                                currentApp.setRestartCycle(restartCycle);
                            } else {
                                currentApp.setFUpload(netUpload +
                                        currentFUpload - currentApp.getFCurrentUpload());
                            }
                            currentApp.setFCurrentUpload(currentFUpload);

                            currentApp.setFCurrentDownload(cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_DOWNLOAD)));
                            currentApp.setFDownload(
                                    cursor.getLong(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_DOWNLOAD))
                                            - currentApp.getFCurrentDownload());
                        } else {
                            int appearances = currentApp.getBAppearances() + 1;
                            double totalCpuUsage = currentApp.getBCPUUsage() *
                                    currentApp.getBAppearances() + cursor.getDouble(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_CPU));
                            long totalMemoryUsage = currentApp.getBMemoryUsage() *
                                    currentApp.getBAppearances() + cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_MEMORY));
                            currentApp.setBAppearances(appearances);
                            currentApp.setBCPUUsage(totalCpuUsage / appearances);
                            currentApp.setBMemoryUsage(totalMemoryUsage / appearances);

                            long currentUpload = cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_UPLOAD));
                            long netUpload = currentApp.getFUpload();
                            int restartCycle = cursor.getInt(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.RESTART_CYCLE));
                            if (currentApp.getRestartCycle() != restartCycle) {
                                currentApp.setFUpload(netUpload + currentUpload);
                                currentApp.setRestartCycle(restartCycle);
                            } else {
                                currentApp.setFUpload(netUpload +
                                        currentUpload - currentApp.getFCurrentUpload());
                            }
                            currentApp.setFCurrentUpload(currentUpload);

                            currentApp.setFCurrentDownload(cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_DOWNLOAD)));
                            currentApp.setFDownload(
                                    cursor.getLong(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_DOWNLOAD))
                                            - currentApp.getFCurrentDownload());
                        }
                    }
                } else {
                    currentApp = new App();
                    currentApp.setName(appName);

                    currentApp.setFTimeUsed(0);
                    currentApp.setBTimeUsed(0);
                    currentApp.setFBatteryUsed(0);
                    currentApp.setBBatteryUsed(0);

                    if (!appName.equals("Idle")) {
                        currentApp.setPackageName(cursor.getString(
                                cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_PACKAGE_NAME)));

                        if (isForeground == 1) {
                            currentApp.setFCPUUsage(cursor.getDouble(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_CPU)));

                            currentApp.setFMemoryUsage(cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_MEMORY)));

                            currentApp.setRestartCycle(cursor.getInt(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.RESTART_CYCLE)));

                            currentApp.setFUpload(0);
                            currentApp.setFCurrentUpload(cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_UPLOAD)));

                            currentApp.setFDownload(0);
                            currentApp.setFCurrentDownload(cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_DOWNLOAD)));

                            currentApp.setFAppearances(1);
                        } else {
                            currentApp.setBCPUUsage(cursor.getDouble(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_CPU)));

                            currentApp.setBMemoryUsage(cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_MEMORY)));

                            currentApp.setRestartCycle(cursor.getInt(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.RESTART_CYCLE)));

                            currentApp.setBUpload(0);
                            currentApp.setBCurrentUpload(cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_UPLOAD)));

                            currentApp.setBDownload(0);
                            currentApp.setBCurrentDownload(cursor.getLong(
                                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.APP_DOWNLOAD)));

                            currentApp.setBAppearances(1);
                        }
                    }
                    apps.put(appName, currentApp);
                }

                if (previousApp != null && previousDate >= 0 && previousBattery >= 0
                        && currentMonitorCycle == previousMonitorCycle) {
                    if (isForeground == 1) {
                        previousApp.setFTimeUsed(previousApp.getFTimeUsed() +
                                currentDate - previousDate);
                        previousApp.setFBatteryUsed(previousApp.getFBatteryUsed() +
                                previousBattery - currentBattery);
                    } else {
                        previousApp.setBTimeUsed(previousApp.getBTimeUsed() +
                                currentDate - previousDate);
                        previousApp.setBBatteryUsed(previousApp.getBBatteryUsed() +
                                previousBattery - currentBattery);
                    }
                }

                previousApp = currentApp;
                previousDate = currentDate;
                previousBattery = currentBattery;
                previousMonitorCycle = currentMonitorCycle;

                j++;
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        List<App> appList = new ArrayList<>(apps.values());
        Collections.sort(appList, new Comparator<App>() {
            @Override
            public int compare(App app1, App app2) {
                return (int) Math.signum(app2.getBBatteryUsed() + app2.getFBatteryUsed()
                        - app1.getBBatteryUsed() - app1.getFBatteryUsed());
            }
        });
        return appList;
    }
}
