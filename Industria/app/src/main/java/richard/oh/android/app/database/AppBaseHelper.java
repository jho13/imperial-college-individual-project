package richard.oh.android.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import richard.oh.android.app.database.AppDbSchema.AppTable;

/**
 * Created by Richard on 25/07/2017.
 */

public class AppBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "appBase.db";

    public AppBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + AppTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                AppTable.Cols.DATE + ", " +
                AppTable.Cols.BATTERY + ", " +
                AppTable.Cols.ACTUAL_BATTERY + ", " +
                AppTable.Cols.APP_NAME + ", " +
                AppTable.Cols.APP_PACKAGE_NAME + ", " +
                AppTable.Cols.APP_CPU + ", " +
                AppTable.Cols.APP_MEMORY + ", " +
                AppTable.Cols.APP_UPLOAD + ", " +
                AppTable.Cols.APP_DOWNLOAD + ", " +
                AppTable.Cols.MONITOR_CYCLE + ", " +
                AppTable.Cols.RESTART_CYCLE + ", " +
                AppTable.Cols.IS_FOREGROUND +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
