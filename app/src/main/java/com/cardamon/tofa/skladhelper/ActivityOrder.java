package com.cardamon.tofa.skladhelper;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import it.carlom.stikkyheader.core.StikkyHeaderBuilder;

public class ActivityOrder extends AppCompatActivity {

    private ListView mListView;
    private boolean mListVisibility = true;
    private String mUuid;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_retail);
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mListView = this.findViewById(R.id.listview);
        mUuid = this.getIntent().getStringExtra("uuid");

        //установка полей хедера
        RelativeLayout header = this.findViewById(R.id.header2);
        header.setBackgroundColor(this.getColor(R.color.fragment1));
        toolbar.setBackgroundColor(this.getColor(R.color.fragment1));

        LinearLayout linearLayout = findViewById(R.id.order_info_layout);
        linearLayout.setVisibility(View.INVISIBLE);

        ((TextView) this.findViewById(R.id.description)).setText(this.getIntent().getStringExtra("description"));
        ((TextView) this.findViewById(R.id.date)).setText(this.getIntent().getStringExtra("date"));
        ((TextView) this.findViewById(R.id.groupName)).setText(this.getIntent().getStringExtra("name"));
        ((TextView) this.findViewById(R.id.sum)).setText(this.getIntent().getStringExtra("sum"));
        ((TextView) this.findViewById(R.id.cash)).setText(this.getIntent().getStringExtra("cash"));
        ((TextView) this.findViewById(R.id.none_cash)).setText(this.getIntent().getStringExtra("noncash"));

        //префикс хедера, ! если какой то товар под заказ
        TextView fieldPrefix = findViewById(R.id.prefix);
        fieldPrefix.setBackgroundResource(R.drawable.rounded_coroner);
        ((GradientDrawable) fieldPrefix.getBackground()).setColor(MyApplication.getColorById(Integer.parseInt(this.getIntent().getStringExtra("store_color"))));
        fieldPrefix.setText(getIntent().getStringExtra("prefix"));
        DbHelper dbHelper = new DbHelper();
        String prefix = getIntent().getStringExtra("prefix");

        String prefixThreeSymb = !prefix.equals("L") ? (!prefix.equals((Object) "S") ? "" : "STA") : "LCT";
        ArrayList<HashMap<String, String>> orderRowsArray = dbHelper.getOrderRows(this.mUuid, prefixThreeSymb);

        int[] fieldsIdsArray = new int[]{R.id.line, R.id.discount, R.id.groupName, R.id.code, R.id.group_prefix, R.id.group_id};
        String[] fieldsNamesArray = new String[]{"line", "discount", "name", "model", "attention", "group_color"};

        SimpleAdapterForOrders simpleAdapterForOrders = new SimpleAdapterForOrders(this.getApplicationContext(), orderRowsArray, R.layout.retail_demand_item, fieldsNamesArray, fieldsIdsArray);
        this.mListView.setAdapter(simpleAdapterForOrders);

        StikkyHeaderBuilder.stickTo(this.mListView).setHeader(R.id.header2, this.findViewById(R.id.layout_container)).minHeightHeader(150).build();

        TextView fieldInfo = findViewById(R.id.discount);
        fieldInfo.setVisibility(View.VISIBLE);
        fieldInfo.setText("\uD835\uDC8A");//i

        HashMap<String, String> orderInfoData = dbHelper.getOrderInfo(this.mUuid, prefixThreeSymb);

        fieldInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListView.smoothScrollToPosition(0);
                int titleHeight = header.getHeight();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                layoutParams.setMargins(0, titleHeight, 0, 0);
                linearLayout.setLayoutParams(layoutParams);
                linearLayout.requestLayout();

                if (!mListVisibility) {
                    mListVisibility = true;
                    linearLayout.setVisibility(View.INVISIBLE);
                    mListView.setVisibility(View.VISIBLE);
                    fieldInfo.setText("\ud835\udf9d");
                    return;
                }
                mListVisibility = false;
                fieldInfo.setText("\ud835\udc8a");
                linearLayout.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.INVISIBLE);

            }
        });


        //!!!!! поправить !
        TextView bayerName = this.findViewById(R.id.bayer_name);
        bayerName.setText(orderInfoData.get("firstname") + " " + orderInfoData.get("lastname"));
        ((TextView) this.findViewById(R.id.bayer_phone)).setText(orderInfoData.get("phone"));
        ((TextView) this.findViewById(R.id.bayer_email)).setText(orderInfoData.get("email"));
        ((TextView) this.findViewById(R.id.bayer_city)).setText(orderInfoData.get("city"));
        ((TextView) this.findViewById(R.id.bayer_address)).setText(orderInfoData.get("address"));
        ((TextView) this.findViewById(R.id.bayer_payment)).setText(orderInfoData.get("payment"));
        ((TextView) this.findViewById(R.id.bayer_shipping)).setText(orderInfoData.get("shipping"));

    }

}
