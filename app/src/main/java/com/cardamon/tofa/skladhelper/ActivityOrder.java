package com.cardamon.tofa.skladhelper;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        HashMap<String, String> orderInfo = dbHelper.getOrderInfo(this.mUuid, prefixThreeSymb);


        //!!!!! поправить !
         TextView bayerName = findViewById(R.id.ba);
        //bayerName.setText(orderInfo.get((Object)"lastname") +"  "+ (String)orderInfo.get((Object)"firstname"));
/*

        ((TextView) this.findViewById(R.id.bayer_phone)).setText((CharSequence) orderInfo.get((Object) "phone"));
        ((TextView) this.findViewById(R.id.bayer_email)).setText((CharSequence) orderInfo.get((Object) "email"));
        ((TextView) this.findViewById(R.id.bayer_city)).setText((CharSequence) orderInfo.get((Object) "city"));
        ((TextView) this.findViewById(R.id.bayer_address)).setText((CharSequence) orderInfo.get((Object) "address"));
        ((TextView) this.findViewById(R.id.bayer_payment)).setText((CharSequence) orderInfo.get((Object) "payment"));
        ((TextView) this.findViewById(R.id.bayer_shipping)).setText((CharSequence) orderInfo.get((Object) "shipping"));

        fieldInfo.setOnClickListener(new View.OnClickListener(this, header, fieldInfo) {


            public void onClick(View view) {
                ActivityOrder.access$000(this.this$0).smoothScrollToPosition(0);
                int n = this.val$relativeLayout.getHeight();
                android.widget.LinearLayout linearLayout = (android.widget.LinearLayout)this.this$0.findViewById(2131296514);
                android.widget.FrameLayout$LayoutParams layoutParams = new android.widget.FrameLayout$LayoutParams((android.view.ViewGroup$MarginLayoutParams)new android.widget.LinearLayout$LayoutParams(-1, -2));
                layoutParams.setMargins(0, n, 0, 0);
                linearLayout.setLayoutParams((android.view.ViewGroup$LayoutParams)layoutParams);
                linearLayout.requestLayout();
                ActivityOrder activityOrder = this.this$0;
                ActivityOrder.access$102(activityOrder, true ^ ActivityOrder.access$100(activityOrder));
                if (ActivityOrder.access$100(this.this$0)) {
                    this.val$tvInfo.setText((CharSequence)"\ud835\udc8a");
                    ActivityOrder.access$000(this.this$0).setVisibility(0);
                    linearLayout.setVisibility(4);
                    return;
                }
                this.val$tvInfo.setText((CharSequence)"\ud835\udf9d");
                ActivityOrder.access$000(this.this$0).setVisibility(4);
                linearLayout.setVisibility(0);
            }
        });
        */
    }

}
