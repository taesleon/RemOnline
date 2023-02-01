package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by dima on 27.12.17.
 */

public class AdapterDemand extends CursorAdapter {
    public AdapterDemand(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = lInflater.inflate(R.layout.retail_demand_list_item, viewGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.groupName)).setText(cursor.getString(cursor.getColumnIndex("name")));
        ((TextView) view.findViewById(R.id.sum)).setText(DateHelper.convertDoubleToString(cursor.getDouble(cursor.getColumnIndex("sum")) / 100 ));
        ((TextView) view.findViewById(R.id.uuid)).setText(cursor.getString(cursor.getColumnIndex("uuid")));
        ((TextView) view.findViewById(R.id.cash)).setText("");
        ((TextView) view.findViewById(R.id.none_cash)).setText("");
        ((TextView) view.findViewById(R.id.colorid)).setText(cursor.getString(cursor.getColumnIndex("color")));

        ((TextView) view.findViewById(R.id.date1)).setText(DateHelper.convertMillisToDate(cursor.getLong(cursor.getColumnIndex("date"))));
        ((TextView) view.findViewById(R.id.description)).setText(cursor.getString(cursor.getColumnIndex("description")));
        ((TextView) view.findViewById(R.id.agent_name)).setText(cursor.getString(cursor.getColumnIndex("agent")));

        String prefix = cursor.getString(cursor.getColumnIndex("agent"));
        prefix = prefix.substring(0, 1);
        TextView tvPrefix = view.findViewById(R.id.prefix);

        tvPrefix.setBackgroundResource(R.drawable.rounded_coroner);
        GradientDrawable drawable = (GradientDrawable) tvPrefix.getBackground();


        drawable.setColor(MyApplication.getColorById(cursor.getInt(cursor.getColumnIndex("color"))));


        tvPrefix.setText(prefix);

        TextView tvDiscount = view.findViewById(R.id.discount);
        tvDiscount.setVisibility(View.INVISIBLE);
        tvDiscount.setText("0");
        int discount = cursor.getInt(cursor.getColumnIndex("discount"));
        if (discount > 0) {
            tvDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText(discount+"");
        }

    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(newCursor);
    }

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
    }
}
