package com.cardamon.tofa.skladhelper;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cardamon.tofa.skladhelper.moysklad.DemandDownloader;
import com.cardamon.tofa.skladhelper.moysklad.Downloader;
import com.cardamon.tofa.skladhelper.moysklad.RetailDownloader;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dima on 26.09.17.
 */

public class ServiceCheck extends Service {
    Timer myTimer;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DbHelper db = new DbHelper();
        Notification notification = SumNotification.getNotification(db.getToDaySum());
        startForeground(14, notification);
        myTimer = new Timer(); // Создаем таймер
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new RetailDownloader(null, Downloader.STILL_MSG, Downloader.UPDATE_MSG).run();
                new DemandDownloader(null, Downloader.STILL_MSG, Downloader.UPDATE_MSG).run();
            }
        }, 60000, 60000*5);//1 min * 5
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        myTimer.cancel();
    }

}
