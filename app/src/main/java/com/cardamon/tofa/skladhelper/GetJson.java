package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cardamon.tofa.skladhelper.moysklad.JsonCatcher;
import com.cardamon.tofa.skladhelper.moysklad.RequestParams;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dima on 26.02.17.
 * поток для получения json от моего склада
 */

public class GetJson implements Runnable {
    private URL mUrl;
    private JsonCatcher mCatcher;

    /**
     * Конструктур запроса
     *
     * @param baseUrl базовая строка url
     * @param j       объект, который будет обрабатывать json (парсить), и учитывать countDown
     * @param params  доп. параметры http запроса
     */
    public GetJson(String baseUrl, JsonCatcher j, RequestParams params) {
        String strUrl = baseUrl;
        if (params != null)
            strUrl = baseUrl + params.getParams();
        try {
            mUrl = new URL(strUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mCatcher = j;
    }

    /**
     * собственно соединяемся по урл, получаем json, передаем его на парсинг
     * если ошибка, передаем ее код
     * уменьшаем countDown в вызывающем объекте
     */
    @Override
    public void run() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpURLConnection conn;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) MyApplication.ACTIVITY.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if(!(netInfo != null && netInfo.isConnectedOrConnecting())){
                mCatcher.catchJson(-2, null);
                return;
            }


            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setRequestMethod("GET");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.ACTIVITY);
            conn.setRequestProperty("Authorization", prefs.getString("password", null));
            conn.connect();
            switch (conn.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    try (InputStream is = conn.getInputStream()) {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        StringBuilder sb = new StringBuilder();
                        int cp;
                        while ((cp = rd.read()) != -1) {
                            sb.append((char) cp);
                        }
                        conn.disconnect();
                        JSONObject json = new JSONObject(sb.toString());
                        mCatcher.catchJson(json);
                    }
                    break;
                default:
                    mCatcher.catchJson(conn.getResponseCode(), mUrl);

            }

        } catch (Exception e) {
            mCatcher.catchJson(-1, null);
            e.printStackTrace();
        }
    }
}
