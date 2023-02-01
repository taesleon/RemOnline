package com.cardamon.tofa.skladhelper.balance;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;

import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BalanceRequest extends AsyncTask<String, Void, String> {
    BalanceAdapter adapter;

    public BalanceRequest(BalanceAdapter balanceBvAdapter) {
        this.adapter = balanceBvAdapter;
    }
    private void errorToast(int n) {
        String string2;
        SuperActivityToast superActivityToast = new SuperActivityToast(MyApplication.ACTIVITY, Style.red());
        superActivityToast.setFrame(Style.FRAME_KITKAT);
        superActivityToast.setGravity(Gravity.CENTER);
        superActivityToast.setDuration(Style.DURATION_VERY_SHORT);
        if (n != 202) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(MyApplication.ACTIVITY.getResources().getString(R.string.unknown_error));
            stringBuilder.append(" code ");
            stringBuilder.append(n);
            string2 = stringBuilder.toString();
        } else {
            string2 = "\u043e\u0439, \u043e\u0448\u0438\u0431\u043a\u0430";
        }
        superActivityToast.setText(string2);
        superActivityToast.show();
    }

    @Override
    protected String doInBackground(String... arrstring) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("https://staub.com.ua/index.php?route=myscript/bvbalance&type=getdata").openConnection();
            httpURLConnection.setRequestProperty("Authorization", PreferenceManager.getDefaultSharedPreferences((Context) MyApplication.ACTIVITY).getString("password", null));
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 202) {
                this.errorToast(202);
            }
            BufferedReader bufferedReader = new BufferedReader((Reader) new InputStreamReader(httpURLConnection.getInputStream()));
            do {
                int n;
                if ((n = bufferedReader.read()) == -1) {
                    httpURLConnection.disconnect();
                    return stringBuilder.toString();
                }
                stringBuilder.append((char) n);
            } while (true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return stringBuilder.toString();
    }

    protected void onPostExecute(String result) {
        try {
            adapter.updateData(new JSONArray(result));
        } catch (JSONException jSONException) {
            jSONException.printStackTrace();
        }
        //BalanceBv.progressBar.setVisibility(4);
    }
}
