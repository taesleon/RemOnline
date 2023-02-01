package com.cardamon.tofa.skladhelper.balance;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;

public class BalanceAdapter extends BaseAdapter {
    private JSONArray dataArr;

    public JSONArray getDataArr() {
        return dataArr;
    }

    public BalanceAdapter(JSONArray jSONArray) {
        this.dataArr = jSONArray;
    }

    @Override
    public int getCount() {
        return this.dataArr.length();
    }

    @Override
    public Object getItem(int i) {
        try {
            JSONArray jSONArray = this.dataArr.getJSONArray(i);
            return jSONArray;
        } catch (JSONException jSONException) {
            jSONException.printStackTrace();
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) MyApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.balance_lv_item, viewGroup, false);
        TextView prefixTv = view.findViewById(R.id.balance_litera);
        prefixTv.setBackgroundResource(R.drawable.rounded_coroner);
        GradientDrawable drawable = (GradientDrawable) prefixTv.getBackground();
        TextView sumTv = view.findViewById(R.id.balance_lv_item_sum);
        TextView lastDateSverTv = view.findViewById(R.id.balance_lv_item_last_date);
        TextView dateTv = view.findViewById(R.id.balance_lv_item_date);
        TextView paymentTv = view.findViewById(R.id.balance_lv_item_payments);
        TextView sellTv = view.findViewById(R.id.balance_lv_item_sells);
        TextView notateTv = view.findViewById(R.id.balance_lv_item_descr);
        ConstraintLayout crrLayout = view.findViewById(R.id.crr_container);
        try {
            JSONArray arr = dataArr.getJSONArray(i);

            if (arr.getString(6).equals("crr")) {
                lastDateSverTv.setText(DateHelper.convertMillisToDate(arr.getLong(7)));
                paymentTv.setText(DateHelper.convertDoubleToStringNullDigit(arr.getDouble(4)));
                sellTv.setText(DateHelper.convertDoubleToStringNullDigit(arr.getDouble(5)));
                prefixTv.setText("S");
                drawable.setColor(MyApplication.getColorById(0));

            } else {
                prefixTv.setText("O");
                drawable.setColor(MyApplication.getColorById(1));
                ((ViewManager)crrLayout.getParent()).removeView(crrLayout);

            }
            sumTv.setText(DateHelper.convertDoubleToStringNullDigit(Double.parseDouble(arr.getString(1))));
            dateTv.setText(DateHelper.convertMillisToDate(Long.parseLong(arr.getString(2))));
            notateTv.setText(arr.getString(3));
            return view;
        } catch (JSONException jSONException) {
            jSONException.printStackTrace();
            return view;
        }
    }

    public void updateData(JSONArray jsonArray) {
        dataArr = jsonArray;
        long prevDate = 0l;

        try {
            for (int i = dataArr.length() - 1; i >= 0; i--) {
                if (dataArr.getJSONArray(i).getString(6).equals("crr")) {
                    if (prevDate > 0)
                        dataArr.getJSONArray(i).put(prevDate);
                    prevDate = dataArr.getJSONArray(i).getLong(2);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BalanceBv.setData(dataArr);
        this.notifyDataSetChanged();

    }
}
