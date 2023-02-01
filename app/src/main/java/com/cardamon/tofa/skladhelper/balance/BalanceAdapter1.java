package com.cardamon.tofa.skladhelper.balance;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.DbHelper;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class BalanceAdapter1 extends BaseAdapter {
    private ArrayList<HashMap<String, String>> data;

    public BalanceAdapter1(ArrayList<HashMap<String, String>> arrayList) {
        this.data = arrayList;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) MyApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.balance_lv1_item, viewGroup, false);
        TextView prefix = view.findViewById(R.id.prefix);
        prefix.setText(data.get(i).get("group_name").substring(0, 2));
        GradientDrawable prefixDrawable = (GradientDrawable) prefix.getBackground();
        prefixDrawable.setColor(MyApplication.getColorById(Integer.parseInt(data.get(i).get("group_color"))));

        ((TextView) view.findViewById(R.id.line)).setText(data.get(i).get("line"));
        ((TextView) view.findViewById(R.id.ref)).setText(data.get(i).get("code"));
        ((TextView) view.findViewById(R.id.title)).setText(data.get(i).get("name"));
        ((TextView) view.findViewById(R.id.num)).setText(data.get(i).get("retail_name"));
        ((TextView) view.findViewById(R.id.discount)).setText(data.get(i).get("discount"));
        ((TextView) view.findViewById(R.id.date)).setText(DateHelper.convertMillisToDateSimple(Long.parseLong(data.get(i).get("date"))));

        String taxString = "";
        double retailSum = Double.parseDouble(data.get(i).get("cash_none_cash"));

        double bv_sum = Double.parseDouble(data.get(i).get("bv_sum"));


        if (retailSum < 0) {
            view.findViewById(R.id.bank_card).setVisibility(View.VISIBLE);
            taxString += "налог 7%: " + DateHelper.convertDoubleToStringNullDigit(bv_sum * 0.93);
        } else
            taxString += "налог 4%: " + DateHelper.convertDoubleToStringNullDigit(bv_sum * 0.96);

        if(data.get(i).get("group_name").equals("Woll"))
            taxString = "налог 30%: " + DateHelper.convertDoubleToStringNullDigit(bv_sum * 0.7);


        ((TextView)view.findViewById(R.id.tax)).setText(taxString);


        return view;
    }
}
