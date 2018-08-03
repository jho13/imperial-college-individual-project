package richard.oh.android.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Richard on 09/08/2017.
 */

public class AppPieChartFragment extends AbstractAppCollectionFragment {

    public static final String TAG = "AppPieChartFragment";
    public static final double EPSILON = 1E-6;

    private PieChart mPieChart;
    private String[] xData;
    private double[] yData;

    public class Formatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                        ViewPortHandler viewPortHandler) {
            return Helper.round(value, 1) + "%";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAppList = ((AppCollectionActivity) getActivity()).getAppList();
        View view = inflater.inflate(R.layout.fragment_app_pie_chart, container, false);
        mPieChart = (PieChart) view.findViewById(R.id.pie_chart);
        mPieChart.getDescription().setText("Click on a segment to view details of the app");
        mPieChart.getDescription().setTextSize(16);
        mPieChart.getLegend().setEnabled(false);

        mPieChart.setCenterText("Total Battery Used: " + Helper.round(mTotalBatteryUsed,1) + "%");
        mPieChart.setCenterTextSize(16);
        mPieChart.setRotationAngle(180f);
        mPieChart.setRotationEnabled(true);
        mPieChart.setHoleRadius(30);
        mPieChart.setTransparentCircleRadius(35);
        mPieChart.setTransparentCircleAlpha(100);
        mPieChart.setEntryLabelColor(Color.WHITE);

        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                App tempApp = new App();
                tempApp.setBBatteryUsed(e.getY());
                Comparator<App> comp = new Comparator<App>() {
                    @Override
                    public int compare(App app1, App app2) {
                        double diff = app2.getBBatteryUsed() + app2.getFBatteryUsed()
                                - app1.getBBatteryUsed() - app1.getFBatteryUsed();
                        if (diff < EPSILON && diff > -1 * EPSILON) {
                            return 0;
                        } else {
                            if (diff < 0) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    }
                };
                int position = Collections.binarySearch(mAppList, tempApp, comp);

                AppDialogFragment dialog = AppDialogFragment.newInstance(position);
                dialog.show(mFragmentManager, AppCollectionActivity.DIALOG_APP);
            }

            @Override
            public void onNothingSelected() {}
        });
        addDataSet();
        mPieChart.notifyDataSetChanged();
        mPieChart.invalidate();

        return view;
    }

    private void addDataSet() {
        xData = new String[mAppList.size()];
        yData = new double[mAppList.size()];
        for (int i = 0; i < mAppList.size(); i++) {
            App app = mAppList.get(i);
            xData[i] = app.getName();
            yData[i] = app.getFBatteryUsed() + app.getBBatteryUsed();
        }

        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < mAppList.size(); i++) {
            entries.add(new PieEntry((float) yData[i], xData[i]));
        }

        PieDataSet pieDataSet = new PieDataSet(entries, "Battery Usage");

        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(14);

        List<Integer> colours = new ArrayList<>();
        colours.add(Color.argb(220,52,221,33));
        colours.add(Color.argb(220,221,62,4));
        colours.add(Color.argb(220,228,232,23));
        colours.add(Color.argb(220,10,109,214));
        colours.add(Color.argb(220,187,10,214));
        colours.add(Color.argb(220,4,230,242));
        colours.add(Color.argb(220,255,102,0));
        pieDataSet.setColors(colours);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new Formatter());
        pieData.setValueTextColor(Color.WHITE);
        mPieChart.setData(pieData);
    }
}
