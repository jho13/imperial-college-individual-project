package richard.oh.android.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Richard on 12/09/2017.
 */

public class TempDialogFragment extends DialogFragment {

    private static final String TAG = "TempDialogFragment";

    private int mWhichOption;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SharedPreferences settings = getActivity().getSharedPreferences(
                SettingsFragment.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();
        int defaultOption = settings.getInt(SettingsFragment.PREFS_BATTERY_TEMP, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warn me when battery temperature exceeds:")
                .setSingleChoiceItems(R.array.temp, defaultOption,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWhichOption = which;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putInt(SettingsFragment.PREFS_BATTERY_TEMP, mWhichOption);
                        editor.commit();
                    }
                });

        return builder.create();
    }
}