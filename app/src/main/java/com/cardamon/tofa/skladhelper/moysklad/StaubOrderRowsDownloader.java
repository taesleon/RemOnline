package com.cardamon.tofa.skladhelper.moysklad;

import android.support.v7.app.AppCompatActivity;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.FragmentOrder;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StaubOrderRowsDownloader extends Downloader{

    public StaubOrderRowsDownloader(AppCompatActivity activity, int showDialogMsg, int insertMsg) {
        super(activity, showDialogMsg, insertMsg);
        if (this.SHOW_DIALOG_MODE) {
            this.mRequestParams.clear();
            this.mRequestParams.setExtraParam("route=myscript/rows&");
            this.mRequestParams.setAllIntervalForSites();
            this.mDialogTitle = activity.getResources().getString(R.string.title_orders);
        }
        this.mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.api_staub);
    }

    public StaubOrderRowsDownloader(AppCompatActivity appCompatActivity, int showDialogMsg, int insertMsg, FragmentOrder fragmentOrder) {
        this(appCompatActivity, showDialogMsg, insertMsg);
        mRequestParams.clear();
        mRequestParams.setExtraParam("route=myscript/rows&");
        mRequestParams.setAllIntervalForSites();
        SHOW_REFRESH_MODE = true;
        mRefreshCallBack = fragmentOrder;
    }
    @Override
    protected void parseJson(JSONObject json) {
        try {
            JSONArray rows = json.getJSONArray("rows");
            mCount = rows.length();

            for (int i = 0; i < rows.length(); i++) {
                String[] data = new String[10];
                data[0] = rows.getJSONObject(i).getString("order_id");
                data[1] = "STA";
                data[2] = rows.getJSONObject(i).getString("product_id");
                data[3] = rows.getJSONObject(i).getString("name");
                data[4] = rows.getJSONObject(i).getString("model");
                data[5] = rows.getJSONObject(i).getString("quantity");
                data[6] = rows.getJSONObject(i).getString("price");
                data[7] = rows.getJSONObject(i).getString("total");
                data[8] = rows.getJSONObject(i).getString("stock");
                data[9] = rows.getJSONObject(i).getString("domo_price");

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
        mDbHelper.updateOrderRows(allRows, "STA");
    }
}
