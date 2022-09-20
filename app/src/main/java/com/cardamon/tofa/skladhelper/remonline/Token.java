/*
 * Decompiled with CFR 0.0.
 * 
 * Could not load the following classes:
 *  android.content.res.Resources
 *  android.util.Log
 *  java.io.BufferedReader
 *  java.io.DataOutputStream
 *  java.io.InputStream
 *  java.io.InputStreamReader
 *  java.io.OutputStream
 *  java.io.Reader
 *  java.lang.Exception
 *  java.lang.Integer
 *  java.lang.Object
 *  java.lang.Runnable
 *  java.lang.String
 *  java.lang.StringBuilder
 *  java.lang.Throwable
 *  java.net.HttpURLConnection
 *  java.net.URL
 *  java.net.URLConnection
 *  java.nio.charset.Charset
 *  java.nio.charset.StandardCharsets
 *  org.json.JSONObject
 */
package com.cardamon.tofa.skladhelper.remonline;


import android.os.StrictMode;
import android.util.Log;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class Token
implements Runnable {
    public static String token;

    public void run() {
        this.updateToken();
    }

    public void updateToken() {
        int n;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Log.d("mimi", "token attempt");
            URL uRL = new URL(MyApplication.getAppContext().getResources().getString(R.string.token_rem));
            byte[] arrby = "api_key=4fc8d2b839304f9c89dace192473bc51".getBytes(StandardCharsets.UTF_8);
            int n2 = arrby.length;
            HttpURLConnection httpURLConnection = (HttpURLConnection) uRL.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Length", Integer.toString(n2));
            httpURLConnection.setUseCaches(false);
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(arrby);
            dataOutputStream.close();
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader((Reader) new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            while ((n = bufferedReader.read()) != -1) {
                stringBuilder.append((char) n);
            }
            Log.d("mimi", stringBuilder.toString());
            httpURLConnection.disconnect();
            token = new JSONObject(stringBuilder.toString()).getString("token");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("new token ");
            stringBuilder2.append(token);
            Log.d("mimi",  stringBuilder2.toString());
            if (inputStream == null) return;
            inputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
}

