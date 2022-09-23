package com.cardamon.tofa.skladhelper.moysklad;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.FragmentOrder;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StaubOrderDownloader extends Downloader {

    public StaubOrderDownloader(AppCompatActivity activity, int showDialogMsg, int insertMsg) {
        super(activity, showDialogMsg, insertMsg);
        if (this.SHOW_DIALOG_MODE) {
            this.mRequestParams.clear();
            this.mRequestParams.setExtraParam("route=myscript/orders&");
            this.mRequestParams.setAllIntervalForSites();
            this.mDialogTitle = activity.getResources().getString(R.string.title_orders);
        }
        this.mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.api_staub);
    }

    public StaubOrderDownloader(AppCompatActivity appCompatActivity, int showDialogMsg, int insertMsg, FragmentOrder fragmentOrder) {
        this(appCompatActivity, showDialogMsg, insertMsg);
        mRequestParams.clear();
        mRequestParams.setExtraParam("route=myscript/orders&");
        mRequestParams.setAllIntervalForSites();
        SHOW_REFRESH_MODE = true;
        mRefreshCallBack = fragmentOrder;
    }

    @Override
    protected void addTokenToRequest() {
    }

    @Override
    protected void parseJson(JSONObject json) {
        try {
            JSONArray rows = json.getJSONArray("rows");
            mCount = rows.length();

            for (int i = 0; i < rows.length(); i++) {
                String[] data = new String[13];
                data[0] = rows.getJSONObject(i).getString("order_id");
                data[1] = "STA";
                data[2] = rows.getJSONObject(i).getString("firstname");
                data[3] = rows.getJSONObject(i).getString("lastname");
                data[4] = rows.getJSONObject(i).getString("email");
                data[5] = rows.getJSONObject(i).getString("telephone");
                data[6] = rows.getJSONObject(i).getString("payment_method");
                data[7] = rows.getJSONObject(i).getString("shipping_address_1");
                data[8] = rows.getJSONObject(i).getString("shipping_city");
                data[9] = rows.getJSONObject(i).getString("shipping_method");
                data[10] = rows.getJSONObject(i).getString("comment");
                data[11] = rows.getJSONObject(i).getString("total");
                data[12] = DateHelper.convertMSdateToLong(rows.getJSONObject(i).getString("date_added")) + "";
                allRows.add(data);
                mCount--;
                publishProgress();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mCount == 0)
            finishSuccess();
    }

    @Override
    protected void insertData() {

    }

    @Override
    protected void updateData() {
        mDbHelper.updateOrders(allRows, "STA");
    }
}
