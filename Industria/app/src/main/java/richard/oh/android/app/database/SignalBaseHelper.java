package richard.oh.android.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import richard.oh.android.app.database.SignalDbSchema.SignalTable;

/**
 * Created by Richard on 20/07/2017.
 */

public class SignalBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "signalBase.db";

    public SignalBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + SignalTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                SignalTable.Cols.DATE + ", " +
                SignalTable.Cols.WIFI_STRENGTH + ", " +
                SignalTable.Cols.CELLULAR_TYPE + ", " +
                SignalTable.Cols.CELLULAR_STRENGTH +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
