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
 * парсим товары
 */
public class GoodDownloader extends Downloader {
    /**
     * @see Downloader#Downloader(Activity, int, int)
     */
    public GoodDownloader(Activity mActivity, int showDialogMsg, int insertMsg) {
        super(mActivity, showDialogMsg, insertMsg);
        if (SHOW_DIALOG_MODE)
            mDialogTitle = mActivity.getResources().getString(R.string.title_good);
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.assortment);
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
                data[1] = rows.getJSONObject(i).getString("title");
                data[2] = rows.getJSONObject(i).getString("article");
                data[3] = rows.getJSONObject(i).getString("code");
                data[4] = rows.getJSONObject(i).getJSONObject("category").getString("id");
                data[5] = rows.getJSONObject(i).getJSONObject("price").getString("270851");
                data[6] = rows.getJSONObject(i).getJSONObject("price").getString("270852");
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
        mDbHelper.insertInTable(Model.Good.INSERT_ROW_STATEMENT, allRows);
    }

    @Override
    protected void updateData() {

    }

}
