package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dima on 13.01.18.
 */

public class AdapterLibrary extends SimpleAdapter implements Filterable {
    private ArrayList<HashMap<String, String>> originalData;
    private ArrayList<HashMap<String, String>> filtredData;

    //конструктор адаптера
    public AdapterLibrary(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to, ActivityLibrary groupLib) {
        super(context, data, resource, from, to);
        //отфильтрованные данные
        filtredData = data;
        //оригинальные данные
        originalData = filtredData;

    }

    @Override
    public int getCount() {
        return filtredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filtredData.get(position);
    }

    public int getFilteredIdFromOriginal(int id) {
        String uuid = filtredData.get(id).get("id");
        for (int i = 0; i < originalData.size(); i++) {
            if(uuid.equals(originalData.get(i).get("id")))
                return i;
        }
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) MyApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = inflater.inflate(R.layout.lib_item, null);
        TextView uuid = newView.findViewById(R.id.uuid);
        uuid.setText(filtredData.get(position).get("id"));
        int color = Integer.parseInt(filtredData.get(position).get("color"));

        TextView prefix = newView.findViewById(R.id.prefix);
        String name = filtredData.get(position).get("name");
        TextView fullName = newView.findViewById(R.id.groupName);
        fullName.setText(name);
        prefix.setText(name.substring(0, 1));
        prefix.setBackgroundColor(MyApplication.getColorById((color)));


        return newView;
    }


    //фильтрация данных
    @Override
    public android.widget.Filter getFilter() {
        {
            return new android.widget.Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults results = new FilterResults();

                    if (charSequence == null || charSequence.length() == 0) {
                        results.values = originalData;
                        results.count = originalData.size();
                    } else {
                        ArrayList<HashMap<String, String>> filterResultsData = new ArrayList<HashMap<String, String>>();
                        filtredData = new ArrayList<>();
                        for (HashMap<String, String> data : originalData) {

                            String constr = charSequence.toString().toLowerCase();
                            String name = data.get("name").toLowerCase();
                            if (name.startsWith(constr)) {
                                filterResultsData.add(data);
                            }
                        }

                        results.values = filterResultsData;
                        results.count = filterResultsData.size();
                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    filtredData = (ArrayList<HashMap<String, String>>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }
}
