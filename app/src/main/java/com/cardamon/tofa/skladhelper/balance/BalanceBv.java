package com.cardamon.tofa.skladhelper.balance;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.DbHelper;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class BalanceBv extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    //данные с сервера
    public static JSONArray mListData = new JSONArray();
    private static ArrayList<HashMap<String, String>> mSellsData = new ArrayList<>();
    private BalanceBv mBalanceBv;

    private static TextView title_balance;
    private static TextView title_payments;
    private static TextView title_sales;
    private static TextView last_date;
    private static ListView mListView;
    private static ListView mListView1;
    private static BalanceAdapter mAdapter;
    private static BalanceAdapter1 mAdapter1;
    private SwipeRefreshLayout mSwipeRefresh;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mBalanceBv = this;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_balance);

        title_balance = findViewById(R.id.bv_header_sum);
        title_payments = findViewById(R.id.bv_header_payment);
        title_sales = findViewById(R.id.bv_header_sell);
        last_date = findViewById(R.id.bv_header_last_check);
        mSwipeRefresh = findViewById(R.id.swipe_container);
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeResources(new int[]{17170454, 17170452, 17170459, 17170456});

//установка toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.getSupportActionBar().setTitle("Баланс БВ");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBalanceBv.finish();
            }
        });
        mListView = findViewById(R.id.listview);
        mListView1 = findViewById(R.id.listview1);


        AbsListView.OnScrollListener listener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (mListView == null || mListView.getChildCount() == 0)
                                ? 0
                                : mListView.getChildAt(0).getTop();
                mSwipeRefresh.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        };

        mListView.setOnScrollListener(listener);
        mListView1.setOnScrollListener(listener);




        registerForContextMenu(mListView);

        mAdapter = new BalanceAdapter(mListData);
        new BalanceRequest(mAdapter).execute(new String[0]);
        mListView.setAdapter(mAdapter);

        mAdapter1 = new BalanceAdapter1(mSellsData);
        mListView1.setAdapter(mAdapter1);

        FloatingActionButton floatButton = findViewById(R.id.add_new_row_balance_btn);
        BalanceBv bv = this;
        floatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(bv);
                View dialogView = MyApplication.ACTIVITY.getLayoutInflater().inflate(R.layout.balance_dialog, null);
                builder.setView(dialogView);
                builder.setNegativeButton("отмена", null);
                AlertDialog alertDialog = builder.create();

                TextView newPmt = dialogView.findViewById(R.id.new_pmt_textview);
                TextView newCrr = dialogView.findViewById(R.id.new_crr_textview);
                newPmt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        new DialogAddPayment(mBalanceBv);
                    }
                });

                newCrr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        new DialogAddCorrection(mBalanceBv);
                    }
                });

                alertDialog.show();
            }
        });
        SwitchCompat switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mListView.setVisibility(View.INVISIBLE);
                    mListView1.setVisibility(View.VISIBLE);
                }
                else{
                    mListView1.setVisibility(View.INVISIBLE);
                    mListView.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        if (view.getId() == R.id.listview) {
            contextMenu.add("удалить");
        }
    }

    public static void update() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(BalanceBv.findLastCorrectionDate(0));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);

        last_date.setText(DateHelper.convertMillisToDateSimple(BalanceBv.findLastCorrectionDate(0)));
        double sell_sum = findSalesByGroupes(calendar.getTimeInMillis(), calendar2.getTimeInMillis());
        title_sales.setText(DateHelper.convertDoubleToStringNullDigit(sell_sum));
        double payment_sum = findPaymentsFromDate(calendar.getTimeInMillis(), calendar2.getTimeInMillis());
        title_payments.setText(DateHelper.convertDoubleToStringNullDigit(payment_sum));
        title_balance.setText(DateHelper.convertDoubleToStringNullDigit(sell_sum - payment_sum));
        DbHelper dbHelper = new DbHelper();
        mSellsData.addAll(dbHelper.getRetailRowsFiltredByGroupes(calendar.getTimeInMillis(), calendar2.getTimeInMillis(), "bv"));

    }

    public static void setData(JSONArray jSONArray) {
        mListData = jSONArray;
        BalanceBv.update();
    }

    @Override
    public void onRefresh() {
        mSwipeRefresh.setRefreshing(false);
        new BalanceRequest(this.mAdapter).execute(new String[0]);
        BalanceBv.update();
    }

    public static long findLastCorrectionDate(int mode) {
        long res = 1604181600000L;
        try {
            for (int i = 0; i < mListData.length(); i++) {
                JSONArray arr = mListData.getJSONArray(i);
                long val = Long.parseLong((String) arr.get(2));
                if (arr.get(6).equals("crr") && val > res) {
                    res = val;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static double findLastCorrectionDebt() {
        long date = 1604181600000L;
        double sum = 0.0;
        try {
            for (int i = 0; i < mListData.length(); i++) {
                JSONArray arr = mListData.getJSONArray(i);
                long val = Long.parseLong((String) arr.get(2));
                if (arr.get(6).equals("crr") && val > date) {
                    date = val;
                    sum = arr.getDouble(1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sum;
    }

    public static double findPaymentsFromDate(long dateFrom, long dateTo) {
        double res = 0;
        try {
            for (int i = 0; i < mListData.length(); i++) {
                JSONArray arr = mListData.getJSONArray(i);
                long date = Long.parseLong((String) arr.get(2));
                if (arr.get(6).equals("pmt") && date > dateFrom) {
                    res += Double.parseDouble((String) arr.get(1));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static double findSalesByGroupes(long startDate, long endDate) {
        double res = 0;
        ArrayList<HashMap<String, String>> list = new DbHelper().getOwnerSums(startDate, endDate).get(1);
        for (HashMap<String, String> hm : list) {
            float tax_cash = Float.parseFloat(hm.get("tax_cash"));
            float tax_bank = Float.parseFloat(hm.get("tax_bank"));
            res += Double.parseDouble(hm.get("cash")) * tax_cash + Double.parseDouble(hm.get("none_cash")) * tax_bank;
        }
        return res;
    }

    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        try {
            if (Long.parseLong(mListData.getJSONArray(adapterContextMenuInfo.position).getString(2)) < BalanceBv.findLastCorrectionDate(0)) {
                SuperActivityToast superActivityToast = new SuperActivityToast(this, Style.red());
                superActivityToast.setFrame(Style.FRAME_KITKAT);
                superActivityToast.setGravity(Gravity.CENTER);
                superActivityToast.setDuration(Style.DURATION_VERY_SHORT);
                superActivityToast.setText("Удалить можно только последнюю запись !");
                superActivityToast.show();
                return false;
            }
        } catch (JSONException jSONException) {
            jSONException.printStackTrace();
        }

        if (!menuItem.getTitle().toString().equals("удалить")) {
            return super.onContextItemSelected(menuItem);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Удалить запись ?");
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            int pos = adapterContextMenuInfo.position;
            try {
                String request = "https://staub.com.ua/index.php?route=myscript/bvbalance&type=del&id=";
                request += mAdapter.getDataArr().getJSONArray(pos).getInt(0);
                request += "&doc=";
                request += mAdapter.getDataArr().getJSONArray(pos).getString(6);
                sendRequest(request);
                onRefresh();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //
        });
        builder.setNegativeButton("Отмена", (dialogInterface, n) -> dialogInterface.dismiss());
        builder.show();

        return true;
    }

    public void sendRequest(String request) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(request).openConnection();
            httpURLConnection.setRequestProperty("Authorization", PreferenceManager.getDefaultSharedPreferences(MyApplication.ACTIVITY).getString("password", null));
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() != 200) {
                this.errorToast();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    private void errorToast() {
        SuperActivityToast superActivityToast = new SuperActivityToast((Context) MyApplication.ACTIVITY, Style.red());
        superActivityToast.setFrame(Style.FRAME_KITKAT);
        superActivityToast.setGravity(Gravity.CENTER);
        superActivityToast.setDuration(Style.DURATION_VERY_SHORT);
        superActivityToast.setText("ой, ошибка");
        superActivityToast.show();
    }
}
