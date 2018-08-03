package richard.oh.android.app;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import richard.oh.android.app.database.AppBaseHelper;
import richard.oh.android.app.database.AppDbSchema.AppTable;

/**
 * Created by Richard on 13/08/2017.
 */

public abstract class AbstractAppCollectionFragment extends Fragment {

    public static final String TAG = "AbstractFragment";

    protected FragmentManager mFragmentManager;
    protected List<App> mAppList;
    protected double mTotalBatteryUsed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();
        AppCollectionActivity activity = (AppCollectionActivity) getActivity();
        mAppList = activity.getAppList();
        Log.i(TAG, "LIST GOT");
        if (mAppList != null) {
            Log.i(TAG, "NOT NULL" + mAppList.size());
        }
        mTotalBatteryUsed = activity.getTotalBatteryUsed();
    }
}
