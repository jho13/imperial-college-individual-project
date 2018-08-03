package richard.oh.android.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Richard on 09/08/2017.
 */

public class DisplayDialogFragment extends DialogFragment {

    private static final String TAG = "DisplayDialogFragment";

    private int mWhichOption;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SharedPreferences settings = getActivity().getSharedPreferences(
                SettingsFragment.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();
        int defaultOption = settings.getInt(SettingsFragment.PREFS_APP_DISPLAY, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(R.array.display, defaultOption,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWhichOption = which;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putInt(SettingsFragment.PREFS_APP_DISPLAY, mWhichOption);
                        editor.commit();
                    }
                });

        return builder.create();
    }
}
