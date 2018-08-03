package richard.oh.android.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import richard.oh.android.app.database.AppBaseHelper;
import richard.oh.android.app.database.AppDbSchema;

/**
 * Created by Richard on 30/08/2017.
 */

public class Helper {

    public static final String TAG = "Helper";

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static String[] round(long value) {
        if (value == 0) {
            return new String[] { " B", "0.0" };
        }

        String unit, newValue;
        if (value < 1000) {
            //B
            unit = " B";
            newValue = Long.toString(value);
            return new String[] { unit, newValue };
        }

        double temp = value / 1000.0;
        if (temp < 1000) {
            //KB
            unit = " kB";
            newValue = Double.toString(round(temp, 1));
            return new String[] { unit, newValue };
        }

        temp = temp / 1000.0;
        if (temp < 1000) {
            //MB
            unit = " MB";
            newValue = Double.toString(round(temp, 1));
            return new String[] { unit, newValue };
        }

        temp = temp / 1000.0;
        //GB
        unit = " GB";
        newValue = Double.toString(round(temp, 1));
        return new String[] { unit, newValue };
    }

    public static String getDurationBreakdownDHMS(long millis) {
        if (millis < 1000) {
            return "0 s";
        }

        long[] duration = new long[4];
        duration[0] = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(duration[0]);
        duration[1] = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(duration[1]);
        duration[2] = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(duration[2]);
        duration[3] = TimeUnit.MILLISECONDS.toSeconds(millis);

        String[] unit = new String[] { " days ", " h ", " m ", " s " };

        StringBuilder sb = new StringBuilder(64);
        int i = 0;
        while (duration[i] == 0) {
            i++;
        }
        while (i < 4) {
            sb.append(duration[i]);
            sb.append(unit[i]);
            i++;
        }

        return(sb.toString());
    }

    public static String getDurationBreakdownDHM(long millis) {
        if (millis < 60000) {
            return "0 minutes";
        }

        long[] duration = new long[3];
        duration[0] = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(duration[0]);
        duration[1] = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(duration[1]);
        duration[2] = TimeUnit.MILLISECONDS.toMinutes(millis);

        String[] unit = new String[] { " days ", " hours ", " minutes " };

        StringBuilder sb = new StringBuilder(64);
        int i = 0;
        while (duration[i] == 0) {
            i++;
        }
        while (i < 3) {
            sb.append(duration[i]);
            sb.append(unit[i]);
            i++;
        }

        return(sb.toString());
    }

    public static String getDurationBreakdownHMSmS(long millis) {
        if (millis < 1) {
           return "0 ms";
        }

        long[] duration = new long[4];
        duration[0] = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(duration[1]);
        duration[1] = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.HOURS.toMillis(duration[1]);
        duration[2] = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.HOURS.toMillis(duration[1]);
        duration[3] = millis;

        String[] unit = new String[] { " h ", " m ", " s ", " ms " };

        StringBuilder sb = new StringBuilder(64);
        int i = 0;
        while (duration[i] == 0) {
            i++;
        }
        while (i < 4) {
            sb.append(duration[i]);
            sb.append(unit[i]);
            i++;
        }

        return(sb.toString());
    }

    public static double[] computeActualBattery(long[] dates, double[] batteries, int[] cycles) {
        int len = dates.length;
        double[] actualBatteries = new double[len];
        Arrays.fill(actualBatteries, -1);

        // base cases
        actualBatteries[0] = batteries[0];
        for (int i = 1; i < len - 1; i++) {
            if (cycles[i] != cycles[i - 1]) {
                actualBatteries[i] = batteries[i];

                if (actualBatteries[i - 1] == -1) {
                    actualBatteries[i - 1] = batteries[i - 1] - 1;
                }

            } else if (batteries[i] < batteries[i - 1]) {
                actualBatteries[i] = batteries[i];
            }
        }
        if (actualBatteries[len - 1] == -1) {
            actualBatteries[len - 1] = batteries[len - 1] - 1;
        }

        // interpolation case
        long leftDate = -1, rightDate = -1;
        double leftBattery = -1, rightBattery = -1;

        for (int i = 0; i < len - 1; i++) {
            if (actualBatteries[i] != -1) {
                // left end
                leftDate = dates[i];
                leftBattery = actualBatteries[i];

                // right end
                int j = i + 1;
                while (actualBatteries[j] == -1) {
                    j++;
                }
                rightDate = dates[j];
                rightBattery = actualBatteries[j];

            } else {
                double battery = leftBattery + (rightBattery - leftBattery) *
                        (dates[i] - leftDate) / (rightDate - leftDate);
                actualBatteries[i] = battery;
            }
        }

        return actualBatteries;
    }

    public static void generateActualBattery(SQLiteDatabase database) {
        Cursor cursor = database.query(
                AppDbSchema.AppTable.NAME,
                null,
                null,
                null,
                null,
                null,
                AppDbSchema.AppTable.Cols.DATE);

        try {
            if (cursor.getCount() == 0) {
                return;
            }

            // determine where to start
            cursor.moveToFirst();
            while (!cursor.isAfterLast() && !cursor.isNull(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))) {
                cursor.moveToNext();
            }
            int start = cursor.getPosition();

            // determine where to end
            cursor.moveToLast();
            int monitorCycle = cursor.getInt(cursor.getColumnIndex(
                    AppDbSchema.AppTable.Cols.MONITOR_CYCLE));
            while (!cursor.isBeforeFirst() && monitorCycle == cursor.getInt(cursor.getColumnIndex(
                    AppDbSchema.AppTable.Cols.MONITOR_CYCLE))) {
                cursor.moveToPrevious();
            }
            int end = cursor.getPosition();

            //Log.i(TAG, Integer.toString(start) + " " + Integer.toString(end));
            if (end < start) {
                return;
            }

            int len = end - start + 1;
            long[] dates = new long[len];
            double[] batteries = new double[len];
            int[] cycles = new int[len];
            double[] actualBatteries;
            for (int i = 0; i < len; i++) {
                cursor.moveToPosition(start + i);
                dates[i] = cursor.getLong(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.DATE));
                batteries[i] = cursor.getDouble(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.BATTERY));
                cycles[i] = cursor.getInt(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.MONITOR_CYCLE));
            }
            actualBatteries = computeActualBattery(dates, batteries, cycles);

            ContentValues values;
            for (int i = 0; i < len; i++) {
                cursor.moveToPosition(start + i);
                values = new ContentValues();
                values.put(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY, actualBatteries[i]);
                database.update(AppDbSchema.AppTable.NAME, values,
                        AppDbSchema.AppTable.Cols.DATE + " = " + Long.toString(dates[i]), null);
            }
        } finally {
            cursor.close();
        }
    }

    public static float computeRate(SQLiteDatabase database) {
        Cursor cursor = database.query(
                AppDbSchema.AppTable.NAME,
                null,
                null,
                null,
                null,
                null,
                AppDbSchema.AppTable.Cols.DATE);

        try {
            if (cursor.getCount() == 0) {
                return 0;
            }

            // work out rate
            long totalTime = 0;
            double totalBatteryUsed = 0;
            long currentDate, previousDate = -1;
            double currentActualBattery, previousActualBattery = -1;
            int currentMonitorCycle, previousMonitorCycle = -1;

            cursor.moveToFirst();
            while (!cursor.isBeforeFirst() && !cursor.isNull(cursor.getColumnIndex(
                    AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))) {
                currentDate = cursor.getLong(
                        cursor.getColumnIndex(AppDbSchema.AppTable.Cols.DATE));
                currentActualBattery = cursor.getDouble(
                        cursor.getColumnIndex(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY));
                Log.i(TAG, "from rate" + Double.toString(currentActualBattery));
                currentMonitorCycle = cursor.getInt(
                        cursor.getColumnIndex(AppDbSchema.AppTable.Cols.MONITOR_CYCLE));

                if (previousDate >= 0 && previousActualBattery >= 0
                        && currentMonitorCycle == previousMonitorCycle) {
                    totalTime += currentDate - previousDate;
                    totalBatteryUsed += previousActualBattery - currentActualBattery;
                }
                previousDate = currentDate;
                previousActualBattery = currentActualBattery;
                previousMonitorCycle = currentMonitorCycle;
                cursor.moveToNext();
            }
            if (previousDate == -1) {
                return 0;
            } else {
                return (float) totalTime / (float) totalBatteryUsed;
            }
        } finally {
            cursor.close();
        }
    }
}


/*
  // work out base cases
            double currentBattery, previousBattery = -1;
            int currentMonitorCycle, previousMonitorCycle = -1;

            for (int position = start; position < end; position++) {
                cursor.moveToPosition(position);
                currentBattery = cursor.getDouble(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.BATTERY));
                currentMonitorCycle = cursor.getInt(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.MONITOR_CYCLE));


                Log.i(TAG, Double.toString(currentBattery) + " " + Integer.toString(currentMonitorCycle)
                + " " + Double.toString(cursor.getDouble(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))));

                if (currentMonitorCycle != previousMonitorCycle) {
                        date = cursor.getLong(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.DATE));
                        values = new ContentValues();
                        values.put(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY, currentBattery);
                        database.update(AppDbSchema.AppTable.NAME, values,
                        AppDbSchema.AppTable.Cols.DATE + " = " + Long.toString(date), null);

                        Log.i(TAG, Double.toString(currentBattery) + " " + Double.toString(cursor.getDouble(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))));

                        cursor.moveToPrevious();
                        if (!cursor.isBeforeFirst()) {
                        if (cursor.isNull(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))) {
                        date = cursor.getLong(cursor.getColumnIndex(
                        AppDbSchema.AppTable.Cols.DATE));
                        values = new ContentValues();
                        values.put(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY, previousBattery - 1);
                        database.update(AppDbSchema.AppTable.NAME, values,
                        AppDbSchema.AppTable.Cols.DATE + " = " + Long.toString(date), null);
                        }
                        }

                        } else if (currentBattery < previousBattery) {
        date = cursor.getLong(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.DATE));
        values = new ContentValues();
        values.put(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY, currentBattery);
        database.update(AppDbSchema.AppTable.NAME, values,
        AppDbSchema.AppTable.Cols.DATE + " = " + Long.toString(date), null);
        }

        previousBattery = currentBattery;
        previousMonitorCycle = currentMonitorCycle;
        }
        cursor.moveToPosition(end);
        if (cursor.isNull(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))) {
        date = cursor.getLong(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.DATE));
        values = new ContentValues();
        values.put(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY,
        cursor.getDouble(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.BATTERY)) - 1);
        database.update(AppDbSchema.AppTable.NAME, values,
        AppDbSchema.AppTable.Cols.DATE + " = " + Long.toString(date), null);
        }

        // work out interpolation cases
        long currentDate, leftDate = -1, rightDate = -1;
        double leftBattery = -1, rightBattery = -1;

        for (int position = start; position < end; position++) {
        cursor.moveToPosition(position);
        if (!cursor.isNull(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))) {
        // left end
        leftDate = cursor.getLong(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.DATE));
        leftBattery = cursor.getDouble(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY));

        // right end
        cursor.moveToNext();
        while (cursor.isNull(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY))) {
        cursor.moveToNext();
        }
        rightDate = cursor.getLong(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.DATE));
        rightBattery = cursor.getDouble(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.ACTUAL_BATTERY));

        } else {
        currentDate = cursor.getLong(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.DATE));
        double battery = leftBattery + (rightBattery - leftBattery) *
        (currentDate - leftDate) / (rightDate - leftDate);
        date = cursor.getLong(cursor.getColumnIndex(
        AppDbSchema.AppTable.Cols.DATE));
        values = new ContentValues();
        values.put(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY, battery);
        database.update(AppDbSchema.AppTable.NAME, values,
        AppDbSchema.AppTable.Cols.DATE + " = " + Long.toString(date), null);
        }
        }
                    cursor.moveToPrevious();
                    if (cursor.isBeforeFirst()
                    || (!cursor.isBeforeFirst() && cursor.getDouble(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.BATTERY)) > currentBatteryStamp)
                    || currentMonitorCycle != cursor.getInt(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.MONITOR_CYCLE))) {
                    currentBattery = currentBatteryStamp;
                    cursor.moveToNext();
                    } else {
                    // compute date_pre
                    int offset = 0;
                    long date_pre;
                    while (!cursor.isBeforeFirst() && cursor.getDouble(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.BATTERY)) == currentBatteryStamp
                    && cursor.getInt(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.MONITOR_CYCLE)) == currentMonitorCycle) {
                    cursor.moveToPrevious();
                    offset++;
                    }
                    cursor.moveToNext();
                    offset--;
                    date_pre = cursor.getLong(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.DATE));
                    cursor.move(offset);

                    // compute date_post
                    offset = 0;
                    long date_post;
                    double postBatteryStamp;
                    while (!cursor.isAfterLast() && cursor.getDouble(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.BATTERY)) == currentBatteryStamp
                    && cursor.getInt(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.MONITOR_CYCLE)) == currentMonitorCycle) {
                    cursor.moveToNext();
                    offset--;
                    }
                    if (cursor.isAfterLast() || cursor.getInt(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.MONITOR_CYCLE)) != currentMonitorCycle) {
                    cursor.moveToPrevious();
                    postBatteryStamp = currentBatteryStamp - 1;
                    offset++;
                    } else {
                    postBatteryStamp = cursor.getDouble(
                    cursor.getColumnIndex(AppDbSchema.AppTable.Cols.BATTERY));
                    }
                    date_post = cursor.getLong(cursor.getColumnIndex(AppDbSchema.AppTable.Cols.DATE));
                    cursor.move(offset);
                    cursor.moveToNext();
                    currentBattery = currentBatteryStamp + (currentDate - date_pre) *
                    (postBatteryStamp - currentBatteryStamp) / (date_post - date_pre);
                    ContentValues values = new ContentValues();
                    values.put(AppDbSchema.AppTable.Cols.ACTUAL_BATTERY, currentBattery);
                    database.update(AppDbSchema.AppTable.NAME, values,
                    AppDbSchema.AppTable.Cols.DATE + " = " + Long.toString(currentDate), null);
                    }
                    */