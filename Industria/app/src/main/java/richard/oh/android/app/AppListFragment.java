package richard.oh.android.app;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Richard on 25/07/2017.
 */

public class AppListFragment extends AbstractAppCollectionFragment {

    public static String TAG = "AppListFragment";

    private RecyclerView mAppRecyclerView;
    private AppAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "LIST FRAG CREATED");
        View view = inflater.inflate(R.layout.fragment_app_list, container, false);

        mAppRecyclerView = (RecyclerView) view.findViewById(R.id.app_recycler_view);
        mAppRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (mAdapter == null) {
            mAdapter = new AppAdapter(mAppList);
            mAppRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setApps(mAppList);
            mAdapter.notifyDataSetChanged();
        }

        return view;
    }

    private class AppHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected App mApp;
        protected int position;
        protected ImageView mIconImageView;
        protected TextView mNameTextView;
        protected TextView mBatteryTextView;

        public AppHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_app_list_item, parent, false));
            itemView.setOnClickListener(this);

            mIconImageView = (ImageView) itemView.findViewById(R.id.app_icon);
            mNameTextView = (TextView) itemView.findViewById(R.id.app_title);
            mBatteryTextView = (TextView) itemView.findViewById(R.id.battery_used);
        }

        public void setPosition(int p) {
            position = p;
        }

        public void bind(App app) {
            mApp = app;
            Drawable icon;
            try {
                icon = getContext().getPackageManager()
                        .getApplicationIcon(app.getPackageName());
            } catch (final PackageManager.NameNotFoundException e) {
                icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_launcher);
            }
            mIconImageView.setImageDrawable(icon);
            mNameTextView.setText(mApp.getName());
            mBatteryTextView.setText(Double.toString(
                    Helper.round(mApp.getFBatteryUsed() + mApp.getBBatteryUsed(), 1)) + "%");
        }

        @Override
        public void onClick(View v) {
            AppDialogFragment dialog = AppDialogFragment.newInstance(position);
            dialog.show(mFragmentManager, AppCollectionActivity.DIALOG_APP);
        }
    }

    private class AppAdapter extends RecyclerView.Adapter<AppHolder> {

        private List<App> mApps;

        public AppAdapter(List<App> apps) {
            mApps = apps;
        }

        @Override
        public AppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new AppHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(AppHolder holder, int position) {
            App app = mApps.get(position);
            holder.setPosition(position);
            holder.bind(app);
        }

        @Override
        public int getItemCount() {
            return mApps.size();
        }

        public void setApps(List<App> apps) {
            mApps = apps;
        }
    }
}
