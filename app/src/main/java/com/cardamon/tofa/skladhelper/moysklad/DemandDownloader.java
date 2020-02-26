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
        mRequestParams.setExpandPositions();
        if (SHOW_DIALOG_MODE) {
            mRequestParams.setAllInterval();
            mDialogTitle = mActivity.getResources().getString(R.string.title_demand);
        }
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.demand);
    }

    public DemandDownloader(Activity mActivity, int showDialogMsg, int insertMsg, FragmentDemand fragment) {
        this(mActivity, showDialogMsg, insertMsg);
        mRequestParams.setRefreshInterval();
        SHOW_REFRESH_MODE = true;
        mRefreshCallBack = fragment;
    }

    /**
     * парсим и добавляем в базу
     * если конец, гасим диалог
     *
     * @param json полученно от HTTP запроса
     */
    @Override
    protected synchronized void parseJson(JSONObject json) {
        try {
            JSONArray rows = json.getJSONArray("rows");
            for (int i = 0; i < rows.length(); i++) {
                String[] data = new String[7];

                data[0] = rows.getJSONObject(i).getString("id");
                data[1] = rows.getJSONObject(i).getString("name");
                if (rows.getJSONObject(i).has("description"))
                    data[2] = rows.getJSONObject(i).getString("description");
                else data[2] = "";
                data[3] = DateHelper.convertMSdateToLong(rows.getJSONObject(i).getString("moment")) + "";
                data[4] = rows.getJSONObject(i).getDouble("sum") + "";

                String uuid = rows.getJSONObject(i).getJSONObject("agent").getJSONObject("meta").getString("href");
                uuid = uuid.replaceFirst("(.*)counterparty/", "");
                data[5] = uuid;

                data[6] = "";

                data[6] = "";
                JSONArray nameArray = new JSONArray();
                if (rows.getJSONObject(i).has("attributes"))
                    nameArray = rows.getJSONObject(i).getJSONArray("attributes");
                for (int g = 0; g < nameArray.length(); g++) {
                    if (nameArray.getJSONObject(g).getString("type").equals("employee"))
                        data[6] = nameArray.getJSONObject(g).getJSONObject("value").getString("name");
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
        mDbHelper.insertInTable(Model.Demand.INSERT_ROW_STATEMENT, allRows);
        mDbHelper.insertInTable(Model.DemandRows.INSERT_ROW_STATEMENT, allExpandedRows);
    }

    @Override
    protected void updateData() {
        mDbHelper.updateDemand(allRows, allExpandedRows, mRequestParams.getDateFrom(), mRequestParams.getDateTo());

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
    }
}
