package com.cardamon.tofa.skladhelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cardamon.tofa.skladhelper.moysklad.AgentDownloader;
import com.cardamon.tofa.skladhelper.moysklad.BrokenRequest;
import com.cardamon.tofa.skladhelper.moysklad.DemandDownloader;
import com.cardamon.tofa.skladhelper.moysklad.Downloader;
import com.cardamon.tofa.skladhelper.moysklad.GoodDownloader;
import com.cardamon.tofa.skladhelper.moysklad.GroupDownloader;
import com.cardamon.tofa.skladhelper.moysklad.RetailDownloader;
import com.cardamon.tofa.skladhelper.moysklad.RetailStoreDownloader;
import com.cardamon.tofa.skladhelper.moysklad.ReturnDownloader;
import com.cardamon.tofa.skladhelper.moysklad.StoreDownloader;
import com.cardamon.tofa.skladhelper.moysklad.UserDownloader;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import su.levenetc.android.textsurface.Text;
import su.levenetc.android.textsurface.TextBuilder;
import su.levenetc.android.textsurface.TextSurface;
import su.levenetc.android.textsurface.animations.Delay;
import su.levenetc.android.textsurface.animations.Sequential;
import su.levenetc.android.textsurface.animations.Slide;
import su.levenetc.android.textsurface.contants.Align;
import su.levenetc.android.textsurface.contants.Side;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by dima on 05.08.17.
 * если в системе нет текущего пользователя, попадаем сюда
 * пытаемся полчить доступ, если да в настройки вносим зашифрованный в басе64 пару
 * пароль:логин
 */

public class NewUser extends Activity implements BrokenRequest {
    private NewUser mActivity = this;
    private String mUser, mPassword;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    private ExecutorService mExecutorService;


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mActivity = this;
        setContentView(R.layout.new_user_backgroung);
        showGreeting();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.new_user_dialog_title)
                .customView(R.layout.new_user_dialog, false)
                .positiveText("OK")
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .positiveColor(ContextCompat.getColor(this, R.color.dialog_positive_btn))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        mUser = ((TextView) dialog.findViewById(R.id.user)).getText().toString();
                        mPassword = ((TextView) dialog.findViewById(R.id.password)).getText().toString();
                        mUser = "stas@shakh_bv";
                        mPassword = "tffik3";

                        String basicAuth = "";
                        try {
                            basicAuth = "Basic " + Base64.encodeToString((mUser + ":" + mPassword).getBytes("UTF-8"), Base64.NO_WRAP);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mEditor = mPrefs.edit();
                        mEditor.putString("password", basicAuth);
                        mEditor.commit();

                        mExecutorService = Executors.newFixedThreadPool(1);
                        mExecutorService.execute(new UserDownloader(mActivity, Downloader.SHOW_TOAST_MSG, Downloader.INSERT_MSG));
                        mExecutorService.execute(new GoodDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                        mExecutorService.execute(new RetailDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                        //mExecutorService.execute(new ReturnDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                        mExecutorService.execute(new DemandDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                        mExecutorService.execute(new AgentDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                        mExecutorService.execute(new StoreDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                        mExecutorService.execute(new RetailStoreDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                        mExecutorService.execute(new GroupDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));


                        Runnable startMain = new Runnable() {
                            @Override
                            public void run() {
                                mActivity.startActivity(new Intent(mActivity, ActivityMain.class));
                            }
                        };
                        mExecutorService.execute(startMain);

                    }
                })
                .show();


        final Intent intent = mActivity.getIntent();
        if (intent.hasExtra("user")) {
            ((TextView) dialog.findViewById(R.id.user)).setText(intent.getStringExtra("user"));
            ((TextView) dialog.findViewById(R.id.password)).setText(intent.getStringExtra("password"));
        }

    }

    //показ заставки
    private void showGreeting() {
        Text textDaai = TextBuilder
                .create("МОЙ СКЛАД")
                .setSize(24)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.SURFACE_CENTER)
                .build();
        Text textDaai1 = TextBuilder
                .create("OFFLINE")
                .setSize(24)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.CENTER_OF | Align.BOTTOM_OF, textDaai)
                .build();
        TextSurface ts = findViewById(R.id.text_surface);
        ts.play(
                new Sequential(
                        Delay.duration(500),
                        Slide.showFrom(Side.TOP, textDaai, 300),
                        Delay.duration(200),
                        new Sequential(
                                Slide.showFrom(Side.RIGHT, textDaai1, 300)))
        );
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void breakRequest() {
        mExecutorService.shutdown();
        DbHelper db = new DbHelper();
        db.deleteDocs();
        db.deleteLibs();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.finish();
                        Intent intent = new Intent(mActivity, NewUser.class);
                        intent.putExtra("user", mUser);
                        intent.putExtra("password", mPassword);
                        mActivity.startActivity(intent);
                    }
                }, 1000);
            }
        });

    }

}
