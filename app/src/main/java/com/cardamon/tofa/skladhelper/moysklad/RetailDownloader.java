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
    /** @see Downloader#Downloader(Activity, int, int) */
    public RetailDownloader(Activity mActivity, int showDialogMsg, int insertMsg) {
        super(mActivity, showDialogMsg, insertMsg);
        mRequestParams.setExpandPositions();
        if(SHOW_DIALOG_MODE) {
            mRequestParams.setAllInterval();
            mDialogTitle = mActivity.getResources().getString(R.string.title_retail_demand);
        }
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.retaildemand);

    }

    public RetailDownloader(Activity mActivity, int showDialogMsg, int insertMsg, FragmentRetail fragment) {
        this(mActivity, showDialogMsg, insertMsg);
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
            JSONArray rows = json.getJSONArray("rows");
            for (int i = 0; i < rows.length(); i++) {
                String[] data = new String[10];

                data[0] = rows.getJSONObject(i).getString("id");
                data[1] = rows.getJSONObject(i).getString("name");

                if (rows.getJSONObject(i).has("description"))
                    data[2] = rows.getJSONObject(i).getString("description");
                else data[2] = "";
                data[3] = DateHelper.convertMSdateToLong(rows.getJSONObject(i).getString("moment")) + "";
                data[4] = rows.getJSONObject(i).getDouble("sum") + "";
                data[5] = rows.getJSONObject(i).getLong("cashSum") + "";
                data[6] = rows.getJSONObject(i).getLong("noCashSum") + "";
                String uuid = rows.getJSONObject(i).getJSONObject("retailStore").getJSONObject("meta").getString("href");
                uuid = uuid.replaceFirst("(.*)retailstore/", "");
                data[7] = uuid;


                if (rows.getJSONObject(i).has("agent")) {
                    String s = rows.getJSONObject(i).getJSONObject("agent").getJSONObject("meta").getString("href");
                    s = s.replaceFirst("(.*)counterparty/", "");
                    data[8] = s;
                }


                data[9] = "";
                JSONArray nameArray = new JSONArray();
                if (rows.getJSONObject(i).has("attributes"))
                    nameArray = rows.getJSONObject(i).getJSONArray("attributes");
                for (int g = 0; g < nameArray.length(); g++) {
                    if (nameArray.getJSONObject(g).getString("type").equals("employee")) {
                        String ownerUuid;
                        ownerUuid = nameArray.getJSONObject(g).getJSONObject("value").getJSONObject("meta").getString("href");
                        data[9] = ownerUuid.replaceFirst("(.*)employee/", "");
                    }
                }
                if (data[9].equals("") && rows.getJSONObject(i).has("owner")) {
                    String ownerUuid;
                    ownerUuid = rows.getJSONObject(i).getJSONObject("owner").getJSONObject("meta").getString("href");
                    data[9] = ownerUuid.replaceFirst("(.*)employee/", "");
                }

                //а теперь парсим expanded rows
                JSONObject jsonExpanded = rows.getJSONObject(i).getJSONObject("positions");
                JSONArray rows1 = jsonExpanded.getJSONArray("rows");

                for (int j = 0; j < rows1.length(); j++) {
                    String[] data1 = new String[5];
                    data1[0] = data[0];
                    data1[1] = rows1.getJSONObject(j).getString("quantity");
                    data1[2] = rows1.getJSONObject(j).getLong("price") + "";
                    data1[3] = rows1.getJSONObject(j).getLong("discount") + "";
                    String ref = rows1.getJSONObject(j).getJSONObject("assortment").getJSONObject("meta").getString("href");
                    ref = ref.replaceFirst("(.*)product/", "");
                    data1[4] = ref;
                    allExpandedRows.add(data1);
                }
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

        if (mDbHelper.checkNotIssetPositions(Model.Retail.TABLE_NAME)) {
            if (SHOW_REFRESH_MODE) {
                MyApplication.ACTIVITY.Download(Model.Agent.TABLE_NAME);
                MyApplication.ACTIVITY.Download(Model.RetailStore.TABLE_NAME);
            }
            else{
                mDbHelper.deleteTable(Model.Agent.TABLE_NAME);
                new AgentDownloader(null, Downloader.STILL_MSG, Downloader.INSERT_MSG).run();

                mDbHelper.deleteTable(Model.RetailStore.TABLE_NAME);
                new RetailStoreDownloader(null, Downloader.STILL_MSG, Downloader.INSERT_MSG).run();
            }

        }
        if (mDbHelper.checkNotIssetPositions(Model.RetailRows.TABLE_NAME)) {
            if (SHOW_REFRESH_MODE) {
                MyApplication.ACTIVITY.Download(Model.Good.TABLE_NAME);
            }
            else{
                mDbHelper.deleteTable(Model.Good.TABLE_NAME);
                new GoodDownloader(null, Downloader.STILL_MSG, Downloader.INSERT_MSG).run();
            }
        }

    }
}
