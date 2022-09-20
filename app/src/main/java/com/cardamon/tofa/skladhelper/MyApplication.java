package com.cardamon.tofa.skladhelper;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

public class MyApplication extends Application {
    public final static int MAX_THREAD = 5;
    public static ActivityMain ACTIVITY;
    private static Context mContext;
    private static SQLiteDatabase sDataBase;
    public static int SHORT_VIBRATE = 100;
    public static String TOKEN;
    public static int UPDATE_PERIOD_DAYS = 7;

    public void onCreate() {
        super.onCreate();
        MyApplication.mContext = getApplicationContext();
    }
    public static Context getAppContext(){
        return mContext;
    }
    public synchronized static SQLiteDatabase getSqlDataBase(){
        if(sDataBase!=null)
            return sDataBase;
        else
            return new DbHelper().getReadableDatabase();
    }
    public static void vibrate(int length){
        Vibrator v = (Vibrator) ACTIVITY.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(length);
    }

    public static void sound(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(MyApplication.getAppContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int getColorById(int id){

        return getAppContext().getResources().getIntArray(R.array.color_array)[id];
    }
}