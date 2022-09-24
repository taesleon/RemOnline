package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class SimpleAdapterForOrders extends SimpleAdapter {

    public SimpleAdapterForOrders(Context context, List<? extends Map<String, ?>> list, int n, String[] arrstring, int[] arrn) {
        super(context, list, n, arrstring, arrn);
    }

    public View getView(int n, View v, ViewGroup viewGroup) {
        View view = super.getView(n, v, viewGroup);
        TextView prefixField = view.findViewById(R.id.group_prefix);
        String perfixText = (String)prefixField.getText();

        /*
        TextView discountField = view.findViewById(R.id.discount);
        if (discountField.getText().equals("")) {
            view.findViewById(R.id.discount).setVisibility(View.INVISIBLE);
        }

        if (!(string2 = (String)((TextView)view.findViewById(R.id.group_id)).getText()).equals((Object)"")) {
            Integer.parseInt((String)string2);
        }
        */

        GradientDrawable gradientDrawable = (GradientDrawable)prefixField.getBackground();
        prefixField.setTextSize(20.0f);
        gradientDrawable.setColor(Color.argb((int)0, (int)0, (int)0, (int)0));
        if (perfixText.equals("notstock")) {
            gradientDrawable.setColor(MyApplication.getColorById(49));
            prefixField.setText((CharSequence)"-");
            return view;
        }
        gradientDrawable.setColor(MyApplication.getColorById(19));
        prefixField.setText("+");
        return view;
    }
}
