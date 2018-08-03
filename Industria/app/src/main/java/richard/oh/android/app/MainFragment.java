package richard.oh.android.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Richard on 25/07/2017.
 */

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    private ConstraintLayout mAppUsageButton;
    private ConstraintLayout mSettingsButton;
    private Button mTestButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getActivity().getSharedPreferences(
                SettingsFragment.PREFS_NAME, 0);
        mEditor = mSettings.edit();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mTestButton = (Button) view.findViewById(R.id.test_button);
        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = AppMonitorService.isServiceAlarmOn(getContext())
                        ? "TRUE" : "FALSE";
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            }
        });

        mAppUsageButton = (ConstraintLayout) view.findViewById(R.id.main_app_usage);
        mAppUsageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AppCollectionActivity.class));
            }
        });

        mSettingsButton = (ConstraintLayout) view.findViewById(R.id.main_settings);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.goto_settings:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
