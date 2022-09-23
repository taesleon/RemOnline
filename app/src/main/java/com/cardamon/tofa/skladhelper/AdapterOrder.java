package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class AdapterOrder extends CursorAdapter {

    public AdapterOrder(Context context, Cursor cursor, boolean bl) {
        super(context, cursor, bl);
    }

    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.uuid)).setText(cursor.getString(cursor.getColumnIndex("order_id")));
        ((TextView) view.findViewById(R.id.date)).setText(DateHelper.convertMillisToDate(cursor.getLong(cursor.getColumnIndex("date_added"))));
        ((TextView) view.findViewById(R.id.sum)).setText(DateHelper.convertDoubleToString(cursor.getDouble(cursor.getColumnIndex("full_sum"))));
        TextView storeField = view.findViewById(R.id.groupName);
        String storePrefix = cursor.getString(cursor.getColumnIndex("store_name"));
        storeField.setText(storePrefix + " - " + cursor.getString(cursor.getColumnIndex("order_id")));

        TextView descrField = view.findViewById(R.id.description);
        String descrText = cursor.getString(cursor.getColumnIndex("comment"));

        if (!descrText.equals("null")) {
            descrField.setText(descrText);
        } else descrField.setText("");

        TextView agentName = view.findViewById(R.id.agent_name);
        agentName.setText(cursor.getString(cursor.getColumnIndex("firstname")) + " " + cursor.getString(cursor.getColumnIndex("lastname")) + " | " + cursor.getString(cursor.getColumnIndex("phone")));

        ((TextView) view.findViewById(R.id.cash)).setText(DateHelper.convertDoubleToString(cursor.getDouble(cursor.getColumnIndex("real_sum"))));
        ((TextView) view.findViewById(R.id.none_cash)).setText(DateHelper.convertDoubleToString(cursor.getDouble(cursor.getColumnIndex("real_sum")) - cursor.getDouble(cursor.getColumnIndex("full_sum"))));

        String shortPrefix = storePrefix.substring(0, 1);
        TextView bigPrefixRect = view.findViewById(R.id.prefix);
        bigPrefixRect.setBackgroundResource(R.drawable.rounded_coroner);
        GradientDrawable gradientDrawable = (GradientDrawable) bigPrefixRect.getBackground();

        int rectColor = 0;
        switch (shortPrefix){
            case "S":
                rectColor = 1;
                break;
            case "L":
                rectColor = 2;
        }

        gradientDrawable.setColor(MyApplication.getColorById(rectColor));

        bigPrefixRect.setText(shortPrefix);
        TextView discountField = view.findViewById(R.id.discount);
        discountField.setVisibility(View.INVISIBLE);
        discountField.setText( "0");
        if (cursor.getDouble(cursor.getColumnIndex("full_sum")) - cursor.getDouble(cursor.getColumnIndex("real_sum")) > 0.0) {
            discountField.setVisibility(View.VISIBLE);
            discountField.setText( "!");
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.retail_demand_list_item, viewGroup, false);
    }

    protected void onContentChanged() {
        super.onContentChanged();
    }

    public Cursor swapCursor(Cursor cursor) {
        return super.swapCursor(cursor);
    }
}
