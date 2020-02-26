package com.cardamon.tofa.skladhelper;

import android.app.Activity;
import android.widget.ImageView;
import java.util.ArrayList;

/**
 * Created by dima on 31.12.17.
 */

public class SaleFilter {
    private String mFilterString;
    private ArrayList<String> mFilterNames;
    private ArrayList<String> mFilterUuids;
    private Integer[] mSelectedItems;

    SaleFilter() {
        clear();
    }

    public void clear() {
        mFilterString = "";
        mFilterNames = new ArrayList<>();
        mFilterUuids = new ArrayList<>();
        mSelectedItems = new Integer[0];
    }

    public String getString() {
        return mFilterString;
    }

    public void setDialog(ArrayList<ArrayList<String>> values) {

        ArrayList<String> prevValues = new ArrayList<>(mFilterUuids);

        mFilterNames = values.get(1);
        mFilterUuids = values.get(0);


        ArrayList<Integer> newSelectedItems = new ArrayList<>();

        for (int i = 0; i < mSelectedItems.length; i++) {
            if (mFilterUuids.contains(prevValues.get(mSelectedItems[i]))) {
                int j = mFilterUuids.indexOf(prevValues.get(mSelectedItems[i]));
                newSelectedItems.add(j);
            }
        }
        mSelectedItems = new Integer[newSelectedItems.size()];
        for (int i = 0; i < newSelectedItems.size(); i++) {
            mSelectedItems[i] = newSelectedItems.get(i);
        }

        updateString();
        setSelectedItems(mSelectedItems);
    }

    public ArrayList<String> getFilterNames() {
        return mFilterNames;
    }

    public Integer[] getSelectedItems() {
        return mSelectedItems;
    }

    public void setSelectedItems(Integer[] which) {
        mSelectedItems = which;
        updateString();
    }

    public void updateString() {
        mFilterString = "AND (S._id = '";
        for (int i = 0; i < mSelectedItems.length; i++) {
            mFilterString += mFilterUuids.get(mSelectedItems[i]) + "'";
            if (i < mSelectedItems.length - 1)
                mFilterString += " OR S._id = '";
        }
        mFilterString+=") ";
        if (mSelectedItems.length == 0)
            mFilterString = "";
    }
    public boolean isNull(){
        if(mSelectedItems.length>0)
            return false;
        return true;
    }

}
