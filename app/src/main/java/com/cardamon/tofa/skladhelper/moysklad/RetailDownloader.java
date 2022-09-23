package com.cardamon.tofa.skladhelper.moysklad;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.FragmentRetail;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by dima on 06.12.17.
 * парсим розничные продажи
 */
public class RetailDownloader extends Downloader {
    /**
     * @see Downloader#Downloader(Activity, int, int)
     */
    public RetailDownloader(Activity mActivity, int showDialogMsg, int insertMsg) {
        super(mActivity, showDialogMsg, insertMsg);
        mRequestParams.setExpandPositions();
        mRequestParams.setExtraParam("warehouse_id=1808391&");
        if (SHOW_DIALOG_MODE) {
            mDialogTitle = mActivity.getResources().getString(R.string.title_retail_demand);
        }
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.retaildemand);

    }

    public RetailDownloader(Activity mActivity, int showDialogMsg, int insertMsg, FragmentRetail fragment) {
        this(mActivity, showDialogMsg, insertMsg);
        mRequestParams.setExtraParam("warehouse_id=1808391&");
        mRequestParams.setRefreshInterval();
        SHOW_REFRESH_MODE = true;
        mRefreshCallBack = fragment;
    }


    /**
     * парсим и добавляем в базу
     * если конец, гасим диалог
     *
     * @param json ответ HTTP сервера
     */
    @Override
    protected synchronized void parseJson(JSONObject json) {
        try {
            JSONArray rows = json.getJSONArray("data");
            for (int i = 0; i < rows.length(); i++) {

                String[] data = new String[10];

                data[0] = rows.getJSONObject(i).getString("id");
                data[1] = rows.getJSONObject(i).getString("id_label");
                data[2] = rows.getJSONObject(i).getString("description");
                data[3] = rows.getJSONObject(i).getString("created_at");
                data[4] = "0";
                data[5] = "0";
                data[6] = "0";
                data[7] = "114741";
                data[8] = rows.getJSONObject(i).has("client_id") != false ? rows.getJSONObject(i).getString("client_id") : "24766929";
                data[9] = "";

                JSONArray docPositions = rows.getJSONObject(i).getJSONArray("products");

                double docSum = 0;

                for (int j = 0; j < docPositions.length(); j++) {
                    String[] data1 = new String[5];
                    //id накладной
                    data1[0] = data[0];
                    //количество
                    data1[1] = docPositions.getJSONObject(j).getString("amount");


                    double discount = docPositions.getJSONObject(j).getDouble("discount_value");
                    double price = docPositions.getJSONObject(j).getDouble("price");

                    //цена
                    data1[2] = price * 100 + "";
                    //артикул
                    data1[4] = docPositions.getJSONObject(j).getString("article");

                    docSum += Double.parseDouble(data1[1]) * Double.parseDouble(data1[2]);

                    //скидка

                    data1[3] = 100 * discount/(discount+price) + "";
                    allExpandedRows.add(data1);
                }
                data[4] = docSum + "";

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
        mDbHelper.insertInTable(Model.Retail.INSERT_ROW_STATEMENT, allRows);
        mDbHelper.insertInTable(Model.RetailRows.INSERT_ROW_STATEMENT, allExpandedRows);
    }

    @Override
    protected void updateData() {

        mDbHelper.updateRetail(allRows, allExpandedRows, mRequestParams.getDateFrom(), mRequestParams.getDateTo());
/*
        if (mDbHelper.checkNotIssetPositions(Model.Retail.TABLE_NAME)) {
            if (SHOW_REFRESH_MODE) {
                MyApplication.ACTIVITY.Download(Model.Agent.TABLE_NAME);
                MyApplication.ACTIVITY.Download(Model.RetailStore.TABLE_NAME);
            } else {
                mDbHelper.deleteTable(Model.Agent.TABLE_NAME);
                new AgentDownloader(null, Downloader.STILL_MSG, Downloader.INSERT_MSG).run();

                mDbHelper.deleteTable(Model.RetailStore.TABLE_NAME);
                new RetailStoreDownloader(null, Downloader.STILL_MSG, Downloader.INSERT_MSG).run();
            }

        }
        if (mDbHelper.checkNotIssetPositions(Model.RetailRows.TABLE_NAME)) {
            if (SHOW_REFRESH_MODE) {
                MyApplication.ACTIVITY.Download(Model.Good.TABLE_NAME);
            } else {
                mDbHelper.deleteTable(Model.Good.TABLE_NAME);
                new GoodDownloader(null, Downloader.STILL_MSG, Downloader.INSERT_MSG).run();
            }
        }
*/
    }
}
