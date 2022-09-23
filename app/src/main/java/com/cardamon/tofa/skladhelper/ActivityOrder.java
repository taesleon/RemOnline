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
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        this.mListView = (ListView) this.findViewById(R.id.listview);
        this.mUuid = this.getIntent().getStringExtra("uuid");
        RelativeLayout relativeLayout = (RelativeLayout) this.findViewById(R.id.header2);
        relativeLayout.setBackgroundColor(this.getColor(R.color.fragment1));
        toolbar.setBackgroundColor(this.getColor(R.color.fragment1));
        ((TextView) this.findViewById(R.id.description)).setText((CharSequence) this.getIntent().getStringExtra("description"));
        ((TextView) this.findViewById(R.id.date)).setText((CharSequence) this.getIntent().getStringExtra("date"));
        ((TextView) this.findViewById(R.id.groupName)).setText((CharSequence) this.getIntent().getStringExtra("name"));
        ((TextView) this.findViewById(R.id.sum)).setText((CharSequence) this.getIntent().getStringExtra("sum"));
        ((TextView) this.findViewById(R.id.cash)).setText((CharSequence) this.getIntent().getStringExtra("cash"));
        ((TextView) this.findViewById(R.id.none_cash)).setText((CharSequence) this.getIntent().getStringExtra("noncash"));
        TextView textView = (TextView) this.findViewById(R.id.prefix);
        textView.setBackgroundResource(R.drawable.rounded_coroner);
        ((GradientDrawable) textView.getBackground()).setColor(MyApplication.getColorById(Integer.parseInt((String) this.getIntent().getStringExtra("store_color"))));
        textView.setText((CharSequence) this.getIntent().getStringExtra("prefix"));
        DbHelper dbHelper = new DbHelper();
        String string2 = this.getIntent().getStringExtra("prefix");
        string2.hashCode();
        String string3 = !string2.equals((Object) "L") ? (!string2.equals((Object) "S") ? "" : "STA") : "LCT";
        ArrayList<HashMap<String, String>> arrayList = dbHelper.getOrderRows(this.mUuid, string3);
        int[] arrn = new int[]{R.id.line, R.id.discount, R.id.groupName, R.id.code, R.id.group_prefix, R.id.group_id};
        String[] arrstring = new String[]{"line", "discount", "name", "model", "attention", "group_color"};
        SimpleAdapterForOrders simpleAdapterForOrders = new SimpleAdapterForOrders(this.getApplicationContext(), (List<? extends Map<String, ?>>) arrayList, R.layout.retail_demand_item, arrstring, arrn);
        this.mListView.setAdapter((ListAdapter) simpleAdapterForOrders);
        StikkyHeaderBuilder.stickTo(this.mListView).setHeader(R.id.header2, (ViewGroup) this.findViewById(R.id.layout_container)).minHeightHeader(150).build();
        TextView textView2 = (TextView) this.findViewById(R.id.discount);
        textView2.setVisibility(View.VISIBLE);
        textView2.setText((CharSequence) "\ud835\udc8a");
        HashMap<String, String> hashMap = dbHelper.getOrderInfo(this.mUuid, string3);


        //!!!!! поправить !
        // TextView textView3 = (TextView)this.findViewById(2131296321);
        //textView3.setText(hashMap.get((Object)"lastname") +"  "+ (String)hashMap.get((Object)"firstname"));
/*

        ((TextView) this.findViewById(R.id.bayer_phone)).setText((CharSequence) hashMap.get((Object) "phone"));
        ((TextView) this.findViewById(R.id.bayer_email)).setText((CharSequence) hashMap.get((Object) "email"));
        ((TextView) this.findViewById(R.id.bayer_city)).setText((CharSequence) hashMap.get((Object) "city"));
        ((TextView) this.findViewById(R.id.bayer_address)).setText((CharSequence) hashMap.get((Object) "address"));
        ((TextView) this.findViewById(R.id.bayer_payment)).setText((CharSequence) hashMap.get((Object) "payment"));
        ((TextView) this.findViewById(R.id.bayer_shipping)).setText((CharSequence) hashMap.get((Object) "shipping"));

        textView2.setOnClickListener(new View.OnClickListener(this, relativeLayout, textView2) {


            public void onClick(View view) {

            }
        });
        */
    }

}
