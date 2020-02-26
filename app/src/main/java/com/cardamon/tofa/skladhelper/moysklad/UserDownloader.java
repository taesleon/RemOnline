package com.cardamon.tofa.skladhelper.moysklad;

import android.app.Activity;

import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dima on 06.12.17.
 * парсим пользователей
 */
public class UserDownloader extends Downloader {
    /** @see Downloader#Downloader(Activity, int, int) */
    public UserDownloader(Activity activity, int showMode, int resultHandlerMode) {
        super(activity, showMode, resultHandlerMode);
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.employee);
    }

    /**
     * парсим и добавляем в базу
     *
     * @param json объект полученный с сервера
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
        if (mCount == 0)
            finishSuccess();
    }

    @Override
    protected void finishSuccess() {
        super.finishSuccess();
    }

    @Override
    protected void insertData() {
        mDbHelper.insertInTable(Model.User.INSERT_ROW_STATEMENT, allRows);
    }

    @Override
    protected void finishError(int code) {
        super.finishError(code);
        ((BrokenRequest) mActivity).breakRequest();
    }

    @Override
    protected void updateData() {
    }
}
