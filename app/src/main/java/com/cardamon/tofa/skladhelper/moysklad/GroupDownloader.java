package com.cardamon.tofa.skladhelper.moysklad;

import android.app.Activity;

import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dima on 06.12.17.
 * парсим группы
 */
public class GroupDownloader extends Downloader {
    /** @see Downloader#Downloader(Activity, int, int) */
    public GroupDownloader(Activity mActivity, int showDialogMsg, int insertMsg) {
        super(mActivity, showDialogMsg, insertMsg);
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.groupes);
        mDialogTitle = mActivity.getResources().getString(R.string.title_group);
    }

    /**
     * парсим и добавляем в базу
     * если конец, гасим диалог
     * @param json ответ HTTP сервера
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
        String[] withoutGroup = {"", "wg"};
        allRows.add(withoutGroup);
        mDbHelper.insertInTable(Model.Group.INSERT_ROW_STATEMENT, allRows);
        mDbHelper.createColors(allRows, "groupe");
    }

    @Override
    protected void updateData() {
    }
}
