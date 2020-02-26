package com.cardamon.tofa.skladhelper.moysklad;

import android.app.Activity;

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
    /** @see Downloader#Downloader(Activity, int, int) */
    public GoodDownloader(Activity mActivity, int showDialogMsg, int insertMsg) {
        super(mActivity, showDialogMsg, insertMsg);
        if(SHOW_DIALOG_MODE)
            mDialogTitle = mActivity.getResources().getString(R.string.title_good);
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.assortment);
        mRequestParams.clear();
        mRequestParams.setLimit(100);

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

                if (rows.getJSONObject(i).has("name"))
                    data[1] = rows.getJSONObject(i).getString("name");
                else
                    data[1] = "";
                if (rows.getJSONObject(i).has("code"))
                    data[2] = rows.getJSONObject(i).getString("code");
                else
                    data[2] = "";
                if (rows.getJSONObject(i).has("article"))
                    data[3] = rows.getJSONObject(i).getString("article");
                else
                    data[3] = "";

                if (rows.getJSONObject(i).has("productFolder")) {
                    String group = rows.getJSONObject(i).getJSONObject("productFolder").getJSONObject("meta").getString("href");
                    group = group.replaceFirst("(.*)productfolder/", "");
                    data[4] = group;
                } else
                    data[4] = "";//without group

                String by_price = "0";
                String sell_price = "0";
                if (rows.getJSONObject(i).has("buyPrice"))
                    by_price = rows.getJSONObject(i).getJSONObject("buyPrice").getString("value");
                if (rows.getJSONObject(i).has("salePrices"))
                    sell_price = rows.getJSONObject(i).getJSONArray("salePrices").getJSONObject(0).getString("value");
                data[5] = sell_price;
                data[6] = by_price;

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
