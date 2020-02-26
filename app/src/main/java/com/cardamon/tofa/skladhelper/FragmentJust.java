package com.cardamon.tofa.skladhelper;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dima on 08.01.18.
 */

public class FragmentJust extends Fragment implements DateSetObserver {
    protected TextView mSum;
    protected TextView mCashSum;
    protected TextView mNoneCashSum;
    private BarChart mChart;
    private BarChart mChart1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.ACTIVITY.fragmentJust = this;
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_just, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            RelativeLayout relativeLayout = view.findViewById(R.id.header1);
            relativeLayout.setBackgroundColor(getResources().getIntArray(R.array.tabs_colors)[MyApplication.ACTIVITY.getCurrentPage()]);
        }


        mSum = view.findViewById(R.id.sum);
        mCashSum = view.findViewById(R.id.cash);
        mNoneCashSum = view.findViewById(R.id.none_cash);
        view.findViewById(R.id.filter_add).setVisibility(View.INVISIBLE);

        MyApplication.ACTIVITY.getFabMenu().registerObserver(this);

        mChart = view.findViewById(R.id.chart);
        mChart1 = view.findViewById(R.id.chart1);

        setupChart1();
        setupChart2();
        update();


        DbHelper db = new DbHelper();


        TextView forecast = view.findViewById(R.id.forecast);
        forecast.setText("Прогноз на сегодня: "+DateHelper.convertDoubleToString(db.getForecast()*1.1));



    }


    public void update() {
        if(MyApplication.ACTIVITY==null || getView()==null)
            return;

        updateChart1();
        updateChart2();


        long date1 = MyApplication.ACTIVITY.getFabMenu().getLeftDateForServer();
        long date2 = MyApplication.ACTIVITY.getFabMenu().getRightDateServer();
        DbHelper db = new DbHelper();

        double sum = db.getDemandSum(date1, date2, "");
        double[] sums = db.getRetailSum(date1, date2, "");

        mSum.setText(DateHelper.convertDoubleToString(sum + sums[0]));
        mCashSum.setText(DateHelper.convertDoubleToString(sums[1]));
        mNoneCashSum.setText(DateHelper.convertDoubleToString(sums[2]));

    }


    private void setData(List<Data> dataList) {

        ArrayList<BarEntry> values = new ArrayList<BarEntry>();
        List<Integer> colors = new ArrayList<Integer>();

        int green = Color.rgb(76, 175, 80);
        int red = Color.rgb(255, 23, 68);

        for (int i = 0; i < dataList.size(); i++) {

            Data d = dataList.get(i);
            BarEntry entry = new BarEntry(d.xValue, d.yValue);
            values.add(entry);

            // specific colors
            if (d.yValue >= 0)
                colors.add(green);
            else
                colors.add(red);
        }

        BarDataSet set;

        set = new BarDataSet(values, "Values");
        set.setColors(colors);
        set.setValueTextColors(colors);

        BarData data = new BarData(set);
        data.setValueTextSize(13f);

        data.setBarWidth(0.8f);

        mChart.setData(data);
        mChart.invalidate();

    }


    /**
     * Demo class representing data.
     */
    private class Data {

        public String xAxisValue;
        public float yValue;
        public float xValue;

        public Data(float xValue, float yValue, String xAxisValue) {
            this.xAxisValue = xAxisValue;
            this.yValue = yValue;
            this.xValue = xValue;
        }
    }

    private void setupChart1() {
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.getDescription().setEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setTouchEnabled(false);
        mChart.getAxisLeft().setEnabled(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        mChart.setExtraBottomOffset(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setYOffset(20f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextSize(11f);
        xAxis.setLabelCount(4);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setSpaceTop(10f);
        yAxis.setSpaceBottom(10f);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawZeroLine(true); // draw a zero line
        yAxis.setZeroLineColor(Color.GRAY);
        yAxis.setZeroLineWidth(0.2f);

        TextView year_vs = getView().findViewById(R.id.year_vs_year);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        calendar.add(Calendar.YEAR, -1);
        int prev = calendar.get(Calendar.YEAR);
        year_vs.setText(prev + "   vs   " + year);


        ImageView help = getView().findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MyApplication.ACTIVITY)
                        .title("Памятка")
                        .content(R.string.total_help)
                        .titleColor(MyApplication.getAppContext().getResources().getColor(R.color.alert, null))
                        .positiveText("OK")
                        .positiveColor(ContextCompat.getColor(MyApplication.ACTIVITY, R.color.dialog_positive_btn))
                        .onPositive((dialog, which) -> {
                        })
                        .show();
            }
        });
    }

    private void setupChart2() {
        mChart1.setDrawBarShadow(false);
        mChart1.setDrawValueAboveBar(true);
        mChart1.getDescription().setEnabled(false);
        mChart1.setTouchEnabled(false);
        mChart1.setPinchZoom(false);
        mChart1.setDrawGridBackground(false);
        mChart1.setExtraBottomOffset(50f);
        mChart1.getLegend().setEnabled(false);
        mChart1.setExtraRightOffset(50f);


        XAxis xAxis = mChart1.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        YAxis yl = mChart1.getAxisLeft();
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setEnabled(false);
        yl.setAxisMinimum(0f);
        YAxis yr = mChart1.getAxisRight();
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setEnabled(false);

    }


    private void updateChart1() {
        DbHelper db = new DbHelper();
        long[] datesPrevYear = DateHelper.getDatesForGraph(true);
        long[] datesCurrentYear = DateHelper.getDatesForGraph(false);
        float mins[] = new float[4];

        for (int i = 0, j = 0; i < datesCurrentYear.length; i += 2, j++) {
            double d1 = db.getTotalSumForPeriod(datesCurrentYear[i], datesCurrentYear[i + 1]);
            double d2 = db.getTotalSumForPeriod(datesPrevYear[i], datesPrevYear[i + 1]);
            mins[j] = (float) (d1 - d2);
        }
        final List<Data> dataSet = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String[] dayNames = {"воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота"};
        String week = calendar.get(Calendar.WEEK_OF_YEAR) + "";
        int month = calendar.get(Calendar.MONTH);

        String[] monthNames = {"январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"};

        dataSet.add(new Data(0f, mins[0], dayNames[day-1]));
        dataSet.add(new Data(1f, mins[1], week + " неделя"));
        dataSet.add(new Data(2f, mins[2], monthNames[month]));
        dataSet.add(new Data(3f, mins[3], "год"));

        XAxis xAxis = mChart.getXAxis();
        setData(dataSet);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dataSet.get((int) value).xAxisValue;
            }
        });

        double max = 0;
        double min = 0;
        for (double d : mins) {
            if (max < d)
                max = d;
            if (min > d)
                min = d;
        }

        mChart.getAxisLeft().setAxisMaximum((float) max);
        mChart.getAxisLeft().setAxisMinimum((float) min);

        mChart.animateY(1000);







    }

    public void updateChart2() {

        new AsyncTask<Void, Void, LinkedHashMap<String, Double>>(){
            @Override
            protected void onPostExecute(LinkedHashMap<String, Double> agents) {
                super.onPostExecute(agents);
                ArrayList<BarEntry> yVals = new ArrayList<>();
                ArrayList<String> yTitles = new ArrayList<>();
                int i = 0;
                for (Map.Entry<String, Double> entry : agents.entrySet()) {
                    yVals.add(new BarEntry(i++, entry.getValue().floatValue()));
                    yTitles.add(entry.getKey());

                }
                HorizontalBarChart hbchart = getView().findViewById(R.id.chart1);
                HorizontalBarChart.LayoutParams params = hbchart.getLayoutParams();
                params.height = 120*yTitles.size();
                hbchart.setLayoutParams(params);

                XAxis xa = mChart1.getXAxis();

                xa.setLabelCount(yTitles.size());

                xa.mEntries=new float[]{};

                xa.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        if ((int) value <= yTitles.size() - 1)
                            return yTitles.get((int) value);
                        return "";
                    }
                });


                BarDataSet set = new BarDataSet(yVals, null);
                set.setColors(MyApplication.ACTIVITY.getResources().getIntArray(R.array.color_array));
                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                dataSets.add(set);
                BarData data = new BarData(dataSets);
                data.setDrawValues(true);
                data.setValueTextSize(11f);

                mChart1.setData(data);
                mChart1.invalidate();
                mChart1.animateY(1000);


            }

            @Override
            protected LinkedHashMap<String, Double> doInBackground(Void... voids) {
                long date1 = MyApplication.ACTIVITY.getFabMenu().getLeftDateForServer();
                long date2 = MyApplication.ACTIVITY.getFabMenu().getRightDateServer();


                DbHelper db = new DbHelper();
                LinkedHashMap<String, Double> groupes = db.getSalesByGroupe(date1, date2);
                LinkedHashMap<String, Double> agents = db.getSalesByAgent(date1, date2);
                LinkedHashMap<String, Double> goods = db.getSalesByGood(date1, date2);
                agents.putAll(goods);
                agents.putAll(groupes);

                return agents;

            }
        }.execute();






    }


}
