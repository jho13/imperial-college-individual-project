package richard.oh.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;

import java.util.Date;

/**
 * Created by Richard on 29/08/2017.
 */

public class IntervalFragment extends Fragment {

    public static final String TAG = "IntervalFragment";

    private AppCollectionActivity mActivity;
    private Button mStartButton;
    private Button mEndButton;

    private SlideDateTimeListener startDateListener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            mActivity.setStart(date);
            mStartButton.setText(mActivity.getStart().toString());
            mActivity.updateUI();
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
        }
    };

    private SlideDateTimeListener endDateListener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            mActivity.setEnd(date);
            mEndButton.setText(mActivity.getEnd().toString());
            mActivity.updateUI();
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interval, container, false);

        mActivity = (AppCollectionActivity) getActivity();

        Log.i(TAG, "date fragment created");
        Log.i(TAG, mActivity.getStart().toString());

        mStartButton = (Button) view.findViewById(R.id.start_button);
        mStartButton.setText(mActivity.getStart().toString());
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getFragmentManager())
                        .setListener(startDateListener)
                        .setInitialDate(mActivity.getStart())
                        .build()
                        .show();
            }
        });

        mEndButton = (Button) view.findViewById(R.id.end_button);
        mEndButton.setText(mActivity.getEnd().toString());
        mEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getFragmentManager())
                        .setListener(endDateListener)
                        .setInitialDate(mActivity.getEnd())
                        .build()
                        .show();
            }
        });
        return view;
    }
}
