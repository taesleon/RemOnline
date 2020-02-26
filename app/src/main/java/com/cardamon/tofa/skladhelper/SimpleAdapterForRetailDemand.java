package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import java.util.List;
import java.util.Map;

/**
 * Created by dima on 07.08.17.
 */

public class SimpleAdapterForRetailDemand extends SimpleAdapter {
    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    public SimpleAdapterForRetailDemand(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView tv = view.findViewById(R.id.group_prefix);//значек группы

        if ((!((TextView) view.findViewById(R.id.discount)).getText().equals("")))
            (view.findViewById(R.id.discount)).setVisibility(View.VISIBLE);

        int colorId = 0;
        String n = (String) ((TextView) view.findViewById(R.id.group_id)).getText();
        if (!n.equals("")) {
            colorId = Integer.parseInt(n);
        }


        String text = (String) tv.getText();

        if (!text.equals(""))
            tv.setText(text.substring(0, 2));
        tv.setBackgroundResource(R.drawable.cirle_group);
        GradientDrawable drawable = (GradientDrawable) tv.getBackground();
        drawable.setColor(MyApplication.getColorById(colorId));

        return view;
    }

}
