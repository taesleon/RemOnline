package com.cardamon.tofa.skladhelper;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextSwitcher;
import android.widget.TextView;

/**
 * Created by dima on 09.10.17.
 */

public class MonthFactory implements TextSwitcher.ViewFactory
{
    @Override
    public View makeView() {
        TextView textView = new TextView(MyApplication.ACTIVITY);
        textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        textView.setTextColor(MyApplication.ACTIVITY.getResources().getColor(R.color.black, MyApplication.ACTIVITY.getTheme()));
        return textView;
    }

}
