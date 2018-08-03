package richard.oh.android.app;

import android.support.v4.app.Fragment;

/**
 * Created by Richard on 26/07/2017.
 */

public class SettingsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new SettingsFragment();
    }
}
