package com.cardamon.tofa.skladhelper.moysklad;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cardamon.tofa.skladhelper.FragmentRetail;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dima on 06.12.17.
 * парсим контрагентов
 */
public class CashBoxRowsDownloader extends Downloader {
    private String boxUuid;

    /** @see Downloader#Downloader(Activity, int, int) */
    public CashBoxRowsDownloader(Activity mActivity, int showDialogMsg, int insertMsg, FragmentRetail fragmentRetail, String uuid) {
        super(mActivity, showDialogMsg, insertMsg);
        mRefreshCallBack = fragmentRetail;

        this.boxUuid = uuid;
        if(SHOW_DIALOG_MODE) {
            mDialogTitle = mActivity.getResources().getString(R.string.title_cashbox);
        }
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.cashboxrows)+uuid;
    }

    public CashBoxRowsDownloader(AppCompatActivity activity, int n, int n2, String uuid) {
        super(activity, n, n2);
        if (this.SHOW_DIALOG_MODE) {
            this.mDialogTitle = "кассы";
            this.mRequestParams.setAllInterval();
        }
        boxUuid = uuid;
        mBaseUrl = MyApplication.ACTIVITY.getResources().getString(R.string.cashboxrows)+uuid;
    }


    /**
     * парсим и добавляем в базу
     * если конец, гасим диалог
     * @param json данные из http запроса
     */
    @Override
    protected synchronized void parseJson(JSONObject json) {
        try {
            JSONArray rows = json.getJSONArray("data");
            for (int i = 0; i < rows.length(); i++) {
                String[] data = new String[2];
                data[0] = rows.getJSONObject(i).getString("created_at");
                data[1] = (long)(100.0 * rows.getJSONObject(i).getDouble("value"))+"";
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

    }

    @Override
    protected void updateData() {
        mDbHelper.updateRetailSellType(allRows, boxUuid);
    }

}
