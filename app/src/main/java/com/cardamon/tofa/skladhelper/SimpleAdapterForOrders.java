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

    public View getView(int n, View view, ViewGroup viewGroup) {
        String string2;
        View view2 = super.getView(n, view, viewGroup);
        TextView textView = (TextView)view2.findViewById(R.id.group_prefix);
        if (!((TextView)view2.findViewById(R.id.discount)).getText().equals((Object)"")) {
            view2.findViewById(R.id.discount).setVisibility(View.INVISIBLE);
        }
        if (!(string2 = (String)((TextView)view2.findViewById(R.id.group_id)).getText()).equals((Object)"")) {
            Integer.parseInt((String)string2);
        }
        String string3 = (String)textView.getText();
        GradientDrawable gradientDrawable = (GradientDrawable)textView.getBackground();
        textView.setTextSize(20.0f);
        gradientDrawable.setColor(Color.argb((int)0, (int)0, (int)0, (int)0));
        if (string3.equals((Object)"notstock")) {
            gradientDrawable.setColor(MyApplication.getColorById(49));
            textView.setText((CharSequence)"-");
            return view2;
        }
        gradientDrawable.setColor(MyApplication.getColorById(19));
        textView.setText((CharSequence)"+");
        return view2;
    }
}
