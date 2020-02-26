package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dima on 14.01.18.
 */

public class AdapterGood extends SimpleExpandableListAdapter implements Filterable {
    private ArrayList<ArrayList<HashMap<String, String>>> originalData;
    private ArrayList<ArrayList<HashMap<String, String>>> filteredData;
    private ArrayList<HashMap<String, String>> originalGroupData;
    private ArrayList<HashMap<String, String>> filteredGroupData;

    public AdapterGood(Context context, ArrayList<HashMap<String, String>> groupData, int groupLayout, String[] groupFrom, int[] groupTo,
                       ArrayList<ArrayList<HashMap<String, String>>> childData, int childLayout, String[] childFrom, int[] childTo) {
        super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo);
        originalData = childData;
        filteredData = originalData;

        originalGroupData = groupData;
        filteredGroupData = originalGroupData;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) MyApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.good_lib_item, null);

        ((TextView) view.findViewById(R.id.name)).setText(filteredData.get(groupPosition).get(childPosition).get("name"));
        ((TextView) view.findViewById(R.id.code)).setText(filteredData.get(groupPosition).get(childPosition).get("code"));
        ((TextView) view.findViewById(R.id.ref)).setText(filteredData.get(groupPosition).get(childPosition).get("ref"));
        ((TextView) view.findViewById(R.id.sale)).setText(filteredData.get(groupPosition).get(childPosition).get("sale"));
        ((TextView) view.findViewById(R.id.buy)).setText(filteredData.get(groupPosition).get(childPosition).get("buy"));

        String stock = filteredData.get(groupPosition).get(childPosition).get("stock");
        TextView tvStock = view.findViewById(R.id.stock);
        if(stock.equals(""))
            tvStock.setVisibility(View.INVISIBLE);
        else tvStock.setText(stock);



        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return filteredData.get(groupPosition).size();
    }

    @Override
    public int getGroupCount() {
        return filteredData.size();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) MyApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.good_lib_item_group, null);

        ((TextView) view.findViewById(R.id.groupName)).setText(filteredGroupData.get(groupPosition).get("name"));
        return view;
    }

    //фильтрация данных
    @Override
    public android.widget.Filter getFilter() {

        {
            return new android.widget.Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults results = new FilterResults();

                // общая коллекция для коллекций элементов
                    ArrayList<ArrayList<HashMap<String, String>>> childData = null;

                    // коллекция для групп
                    ArrayList<HashMap<String, String>> groupData = null;

                    // коллекция для элементов одной группы
                    ArrayList<HashMap<String, String>> childDataItem = null;

                    HashMap<String, String> h = null;

                    if (charSequence == null || charSequence.length() == 0) {
                        results.values = originalData;
                        results.count = originalData.size();
                        filteredGroupData = originalGroupData;
                    } else {
                        ArrayList<ArrayList<HashMap<String, String>>> filterResultsData = new ArrayList<>();

                        filteredData = new ArrayList<>();
                        filteredGroupData = new ArrayList<>();

                        for (int j = 0; j < originalData.size(); j++) {
                            //берется отдельная группа и просматривается по элементам
                            ArrayList<HashMap<String, String>> fData = originalData.get(j);

                            childDataItem = new ArrayList<>();

                            for (int i = 0; i < fData.size(); i++) {
                                String constr = charSequence.toString().toLowerCase();
                                HashMap<String, String> hm = fData.get(i);
                                String name = hm.get("name").toLowerCase();
                                String ref = hm.get("ref").toLowerCase();
                                String code = hm.get("code").toLowerCase();
                                if (name.contains(constr) || ref.contains(constr) || code.contains(constr)) {
                                   childDataItem.add(hm);
                                }
                            }

                            if(childDataItem.size()>0) {
                                filterResultsData.add(childDataItem);
                                filteredGroupData.add(originalGroupData.get(j));
                            }


                        }

                        results.values = filterResultsData;
                        results.count = filterResultsData.size();

                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    filteredData = (ArrayList<ArrayList<HashMap<String, String>>>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

}
