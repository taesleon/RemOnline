package com.cardamon.tofa.skladhelper;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import it.carlom.stikkyheader.core.StikkyHeaderBuilder;

/**
 * отдельная продажа
 * Created by dima on 22.12.17.
 */

public class ActivityDemand extends AppCompatActivity {
    private ListView mListView;
    private String mUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_retail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout linearLayout = findViewById(R.id.order_info_layout);
        linearLayout.setVisibility(View.INVISIBLE);

        mListView = findViewById(R.id.listview);
        mUuid = getIntent().getStringExtra("uuid");

        RelativeLayout relativeLayout = findViewById(R.id.header2);
        relativeLayout.setBackgroundColor(getColor(R.color.fragment1));
        toolbar.setBackgroundColor(getColor(R.color.fragment1));


        ((TextView) findViewById(R.id.description)).setText(getIntent().getStringExtra("description"));
        ((TextView) findViewById(R.id.date)).setText(getIntent().getStringExtra("date"));
        ((TextView) findViewById(R.id.groupName)).setText(getIntent().getStringExtra("name"));
        ((TextView) findViewById(R.id.sum)).setText(getIntent().getStringExtra("sum"));
        ((TextView) findViewById(R.id.cash)).setText(getIntent().getStringExtra("cash"));
        ((TextView) findViewById(R.id.none_cash)).setText(getIntent().getStringExtra("noncash"));

        TextView tvDiscount = findViewById(R.id.discount);
        tvDiscount.setVisibility(View.INVISIBLE);
        tvDiscount.setText("0");
        if (!getIntent().getStringExtra("discount").equals("0")) {
            tvDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText(getIntent().getStringExtra("discount"));
        }

        TextView tvPrefix = findViewById(R.id.prefix);
        tvPrefix.setBackgroundResource(R.drawable.rounded_coroner);
        GradientDrawable drawable = (GradientDrawable) tvPrefix.getBackground();
        drawable.setColor(MyApplication.getColorById(Integer.parseInt(getIntent().getStringExtra("store_color"))));
        tvPrefix.setText(getIntent().getStringExtra("prefix"));


        DbHelper db = new DbHelper();
        ArrayList<HashMap<String, String>> rows = db.getDemandRows(mUuid);
        int[] to = {R.id.line, R.id.discount, R.id.groupName, R.id.code, R.id.group_prefix, R.id.group_id};
        String[] from = {"line", "discount", "name", "code", "group_name", "group_color"};
        SimpleAdapter simpleAdapter = new SimpleAdapterForRetailDemand(getApplicationContext(), rows, R.layout.retail_demand_item, from, to);
        mListView.setAdapter(simpleAdapter);

        StikkyHeaderBuilder.stickTo(mListView)
                .setHeader(R.id.header2, (ViewGroup) findViewById(R.id.layout_container))
                .minHeightHeader(150)
                .build();
    }
}
