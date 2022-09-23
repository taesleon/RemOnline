package com.cardamon.tofa.skladhelper.moysklad;

import android.app.Activity;
import android.util.Log;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.FragmentDemand;
import com.cardamon.tofa.skladhelper.FragmentRetail;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dima on 06.12.17.
 * парсим отгрузки
 */
public class DemandDownloader extends Downloader {
    /**
     * @see Downloader#Downloader(Activity, int, int)
     */
    public DemandDownloader(Activity mActivity, int showDialogMsg, int insertMsg) {
        super(mActivity, showDialogMsg, insertMsg);
        mRequestParams.setExtraParam("warehouse_id=1836684&");
        mRequestParams.setExpandPositions();
        if (SHOW_DIALOG_MODE) {
            mDialogTitle = mActivity.getResources().getString(R.string.title_retail_demand);
        }
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.retaildemand);
    }

    public DemandDownloader(Activity mActivity, int showDialogMsg, int insertMsg, FragmentDemand fragment) {
        this(mActivity, showDialogMsg, insertMsg);
        mRequestParams.setExtraParam("warehouse_id=1836684&");
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

                String[] data = new String[7];

                data[0] = rows.getJSONObject(i).getString("id");
                data[1] = rows.getJSONObject(i).getString("id_label");
                data[2] = rows.getJSONObject(i).getString("description");
                data[3] = rows.getJSONObject(i).getString("created_at");
                data[4] = "0";

                data[5] = rows.getJSONObject(i).getString("client_id");
                data[6] = "";


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
        mDbHelper.insertInTable(Model.Demand.INSERT_ROW_STATEMENT, allRows);
        mDbHelper.insertInTable(Model.DemandRows.INSERT_ROW_STATEMENT, allExpandedRows);
    }

    @Override
    protected void updateData() {
        mDbHelper.updateDemand(allRows, allExpandedRows, mRequestParams.getDateFrom(), mRequestParams.getDateTo());
/*
        if (mDbHelper.checkNotIssetPositions(Model.Demand.TABLE_NAME)) {
            if (SHOW_REFRESH_MODE) {
                MyApplication.ACTIVITY.Download(Model.Agent.TABLE_NAME);
            }
            else{
                mDbHelper.deleteTable(Model.Agent.TABLE_NAME);
                new AgentDownloader(null, Downloader.STILL_MSG, Downloader.INSERT_MSG).run();
            }

        }
        if (mDbHelper.checkNotIssetPositions(Model.DemandRows.TABLE_NAME)) {
            if (SHOW_REFRESH_MODE) {
                MyApplication.ACTIVITY.Download(Model.Good.TABLE_NAME);
            }
            else{
                mDbHelper.deleteTable(Model.Good.TABLE_NAME);
                new GoodDownloader(null, Downloader.STILL_MSG, Downloader.INSERT_MSG).run();
            }
        }
        */
    }
}
