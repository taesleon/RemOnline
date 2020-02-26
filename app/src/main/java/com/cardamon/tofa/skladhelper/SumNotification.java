package com.cardamon.tofa.skladhelper;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by dima on 28.09.17.
 */

public class SumNotification  {
    public static Notification getNotification(String sum) {
        Intent notificationIntent = new Intent(MyApplication.getAppContext(), ActivityMain.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getAppContext(), 0,
                notificationIntent, 0);
        Context context = MyApplication.getAppContext();
        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);


        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_small_white)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setAutoCancel(false)
                .setContentTitle("продажи сегодня: "+sum)
                .setContentText("нажмите для подробной информации")
                .setPriority(Notification.PRIORITY_HIGH)
                .setShowWhen(false)
                //.setVibrate(new long[]{100,100  })
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setOngoing(true);

        Notification notification = builder.build();

        /*only for XIAOMI !*/

        try {
            Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
            Object miuiNotification = miuiNotificationClass.newInstance();
            Field field = miuiNotification.getClass().getDeclaredField("customizedIcon");
            field.setAccessible(true);
            field.set(miuiNotification, true);
            field = notification.getClass().getField("extraNotification");
            field.setAccessible(true);
            field.set(notification, miuiNotification);
        } catch (Exception e) {}
        /*only for XIAOMI !*/
        return notification;
    }
}
