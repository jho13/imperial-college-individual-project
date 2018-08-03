package richard.oh.android.app;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Richard on 01/08/2017.
 */

public class AppDialogFragment extends DialogFragment {

    private static final String TAG = "AppDialogFragment";

    private static final String KEY_APP_POSITION = "AppPosition";

    private List<App> mAppList;
    private double mTotalBatteryUsed;
    private App mApp;

    private ImageView mAppIcon;
    private TextView mAppName;
    private TextView mAppBatteryUsage;
    private TextView mAppBatteryPercentage;
    private TextView mAppBatteryBreakdown;
    private TextView mAppTimeUsage;
    private TextView mAppBatteryTime;
    private TextView mAppCPUUsage;
    private TextView mAppMemoryUsage;
    private TextView mAppUpload;
    private TextView mAppDownload;

    public static AppDialogFragment newInstance(int position) {
        AppDialogFragment fragment = new AppDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_APP_POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCollectionActivity activity = (AppCollectionActivity) getActivity();
        mAppList = activity.getAppList();
        mTotalBatteryUsed = activity.getTotalBatteryUsed();
        int position = getArguments().getInt(KEY_APP_POSITION);
        mApp = mAppList.get(position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_app, container, false);

        getDialog().setTitle("Additional Info:");

        mAppIcon = (ImageView) view.findViewById(R.id.app_image);
        Drawable icon;
        if (!mApp.getName().equals("Idle")) {
            try {
                icon = getContext().getPackageManager()
                        .getApplicationIcon(mApp.getPackageName());
            } catch (final PackageManager.NameNotFoundException e) {
                icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_launcher);
            }
        } else {
            icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_launcher);
        }
        mAppIcon.setImageDrawable(icon);

        mAppName = (TextView) view.findViewById(R.id.app_name);
        mAppName.setText(mApp.getName());

        mAppBatteryUsage = (TextView) view.findViewById(R.id.app_battery_usage);
        mAppBatteryUsage.setText(Double.toString(
                Helper.round(mApp.getFBatteryUsed() + mApp.getBBatteryUsed(), 1)) + "% used");

        mAppBatteryPercentage = (TextView) view.findViewById(R.id.app_battery_percentage);
        mAppBatteryPercentage.setText(
                Math.round(100 * (mApp.getFBatteryUsed() + mApp.getBBatteryUsed()) /
                        mTotalBatteryUsed) + "% of total ("
                        + Helper.round(mTotalBatteryUsed, 1) + "%)");

        mAppBatteryBreakdown = (TextView) view.findViewById(R.id.app_battery_breakdown);
        mAppBatteryBreakdown.setText("Battery usage breakdown: "
                + Double.toString(Helper.round(mApp.getFBatteryUsed(), 1)) + "% / "
                + Double.toString(Helper.round(mApp.getBBatteryUsed(), 1)) + "%");

        mAppTimeUsage = (TextView) view.findViewById(R.id.app_time_used);
        mAppTimeUsage.setText("Time used: " + Helper.getDurationBreakdownDHMS(mApp.getFTimeUsed())
                + " / " + Helper.getDurationBreakdownDHMS(mApp.getBTimeUsed()));

        double fAverage = mApp.getFTimeUsed() != 0 ? Helper.round(
                mApp.getFBatteryUsed() * 1000 * 60 * 60 / mApp.getFTimeUsed(), 1) : 0;
        double bAverage = mApp.getBTimeUsed() != 0 ? Helper.round(
                mApp.getBBatteryUsed() * 1000 * 60 * 60 / mApp.getBTimeUsed(), 1) : 0;
        mAppBatteryTime = (TextView) view.findViewById(R.id.app_battery_time);
        mAppBatteryTime.setText("Battery usage per hour: "
                + fAverage + "% / " + bAverage + "%");

        mAppCPUUsage = (TextView) view.findViewById(R.id.app_cpu_usage);
        mAppMemoryUsage = (TextView) view.findViewById(R.id.app_memory_usage);
        mAppUpload = (TextView) view.findViewById(R.id.app_upload);
        mAppDownload = (TextView) view.findViewById(R.id.app_download);

        if (!mApp.getName().equals("Idle")) {
            mAppCPUUsage.setText("Average CPU usage: " +
                    Double.toString(Helper.round(mApp.getFCPUUsage(), 1)) + "% / " +
                    Double.toString(Helper.round(mApp.getBCPUUsage(), 1)) + "%");

            String[] temp = Helper.round(mApp.getFMemoryUsage());
            String[] temp2 = Helper.round(mApp.getBMemoryUsage());
            mAppMemoryUsage.setText("Average memory used: " + temp[1] + temp[0]
                    + " / " + temp2[1] + temp2[0]);

            temp = Helper.round(mApp.getFUpload());
            temp2 = Helper.round(mApp.getBUpload());
            mAppUpload.setText("Data uploaded: " + temp[1] + temp[0]
                    + " / " + temp2[1] + temp2[0]);

            temp = Helper.round(mApp.getFDownload());
            temp2 = Helper.round(mApp.getBDownload());
            mAppDownload.setText("Data downloaded: " + temp[1] + temp[0]
                    + " / " + temp2[1] + temp2[0]);
        } else {
            mAppCPUUsage.setText("CPU time: N/A");

            mAppMemoryUsage.setText("Average memory used: N/A");

            mAppUpload.setText("Data uploaded: N/A");

            mAppDownload.setText("Data downloaded: N/A");
        }

        return view;
    }
}