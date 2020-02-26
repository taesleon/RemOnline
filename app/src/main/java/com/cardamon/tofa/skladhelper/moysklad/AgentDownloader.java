package com.cardamon.tofa.skladhelper.moysklad;

import android.app.Activity;
import android.util.Log;

import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dima on 06.12.17.
 * парсим контрагентов
 */
public class AgentDownloader extends Downloader {
    /** @see Downloader#Downloader(Activity, int, int) */
    public AgentDownloader(Activity mActivity, int showDialogMsg, int insertMsg) {
        super(mActivity, showDialogMsg, insertMsg);
        if(SHOW_DIALOG_MODE) {
            mDialogTitle = mActivity.getResources().getString(R.string.title_agent);
        }
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.counterparty);
        mRequestParams.clear();
        mRequestParams.setLimit(100);
    }

    /**
     * парсим и добавляем в базу
     * если конец, гасим диалог
     * @param json данные из http запроса
     */
    @Override
    protected synchronized void parseJson(JSONObject json) {
        try {
            JSONArray rows = json.getJSONArray("rows");
            for (int i = 0; i < rows.length(); i++) {
                String[] data = new String[2];
                data[0] = rows.getJSONObject(i).getString("id");
                String name = rows.getJSONObject(i).getString("name");
                data[1] = name;
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
        mDbHelper.insertInTable(Model.Agent.INSERT_ROW_STATEMENT, allRows);
        mDbHelper.createColors(allRows, "agent");
    }

    @Override
    protected void updateData() {

    }

}
