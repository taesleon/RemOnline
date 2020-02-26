package com.cardamon.tofa.skladhelper.moysklad;

import android.app.Activity;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dima on 06.12.17.
 * парсим возвраты
 */
public class ReturnDownloader extends Downloader {
    /** @see Downloader#Downloader(Activity, int, int) */
    public ReturnDownloader(Activity mActivity, int showDialogMsg, int insertMsg) {
        super(mActivity, showDialogMsg, insertMsg);
        mRequestParams.setExpandPositions();
        if(SHOW_DIALOG_MODE) {
            mRequestParams.setAllInterval();
            mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.retailsalesreturn);
        }

        mDialogTitle = mActivity.getResources().getString(R.string.title_retail_return);
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
                String[] data = new String[7];

                data[0] = rows.getJSONObject(i).getString("id");
                data[1] = rows.getJSONObject(i).getString("name");
                if (rows.getJSONObject(i).has("description"))
                    data[2] = rows.getJSONObject(i).getString("description");
                else data[2] = "";
                data[3] = DateHelper.convertMSdateToLong(rows.getJSONObject(i).getString("moment")) + "";
                data[4] = rows.getJSONObject(i).getDouble("sum") + "";

                String uuid = rows.getJSONObject(i).getJSONObject("retailStore").getJSONObject("meta").getString("href");
                uuid = uuid.replaceFirst("(.*)retailstore/", "");
                data[5] = uuid;


                if (rows.getJSONObject(i).has("demand")) {
                    String demand = rows.getJSONObject(i).getJSONObject("demand").getJSONObject("meta").getString("href");
                    demand = demand.replaceFirst("(.*)retaildemand/", "");
                    data[6] = demand;
                } else
                    data[6] = "";

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
        if(mCount==0)
            finishSuccess();
    }

    @Override
    protected void insertData() {
        mDbHelper.insertInTable(Model.Return.INSERT_ROW_STATEMENT, allRows);
        mDbHelper.insertInTable(Model.ReturnRows.INSERT_ROW_STATEMENT, allExpandedRows);
    }

    @Override
    protected void updateData() {

    }
}
