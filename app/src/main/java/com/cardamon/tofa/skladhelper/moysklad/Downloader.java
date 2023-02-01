package com.cardamon.tofa.skladhelper.moysklad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cardamon.tofa.skladhelper.DbHelper;
import com.cardamon.tofa.skladhelper.GetJson;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;
import com.cardamon.tofa.skladhelper.remonline.Token;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by dima on 06.12.17.
 * класс реализующий отдельный поток, который запрашивает у сервера
 * данные, парсит их и управляет UI
 */

public abstract class Downloader extends Thread implements JsonCatcher {
    //для управления UI
    protected Activity mActivity;
    //оставшееся кол-во записей для парсинга
    protected int mCount = -1;
    //количество всех записей, которые нужно парсить
    private int mAllRows = 0;
    //базовый URL
    protected String mBaseUrl;
    //готовые данные для вставки в базу данных
    protected ArrayList<String[]> allRows = new ArrayList<>();
    //позиции, например накладных, дополнительные
    ArrayList<String[]> allExpandedRows = new ArrayList<>();
    //заголовок диалога
    String mDialogTitle;
    //сервис для http запросов
    private ExecutorService mExecutorService;
    //база данных

    protected DbHelper mDbHelper = new DbHelper();
    //http дополнительные параметры запроса
    RequestParams mRequestParams;
    //диалог с изменяющимся прогрессом
    private MaterialDialog mDialog;
    //усовершенствованный toast, успех, ошибка, действие и т.д.
    private SuperActivityToast mToast;
    //обратная связь с фрагментом или другим объектом реализующим интерфейс StopRefreshing
    stopRefreshing mRefreshCallBack;

    //показать toast
    public static final int SHOW_TOAST_MSG = 0;
    //показать toast успех
    private static final int SUCCESS_TOAST_MSG = 1;
    //показать диалог
    public static final int SHOW_DIALOG_MSG = 2;
    //скрыть диалог
    private static final int DISMISS_DIALOG_MSG = 3;
    //показать кольцо обновления
    public static final int SHOW_REFRESH_MSG = 4;
    //скрыть кольцо обновления
    private static final int DISMISS_REFRESH_MSG = 5;
    //тихий запрос из сервиса
    public static final int STILL_MSG = 6;

    //вставить данные в базу
    public static final int INSERT_MSG = 1;
    //обновить данные в базе (вставить, удалить, все проверить)
    public static final int UPDATE_MSG = 2;
    //флаг вставки данных
    private boolean INSERT_MODE = false;
    //флаг обновления данных
    private boolean UPDATE_MODE = false;

    //флаг кольца обновления
    boolean SHOW_REFRESH_MODE = false;
    //флаг toast
    private boolean SHOW_TOAST_MODE = false;
    //флаг диалога
    boolean SHOW_DIALOG_MODE = false;
    //флаг запроса из сервиса
    private boolean STILL_MODE = false;

    private CallBackHandler mHandler;

    /**
     * конструктор объекта,
     * выставляет флаги,
     * режим визуального отображения
     * режим вставки/обновления данных
     * здесь же, создаем параметры для запроса http
     *
     * @param activity         активность где создается диалог/toast
     * @param showMode         режим отображения, диалог/toast
     * @param resultHandleMode как обработать результат, вставить все данные / обновить
     */
    Downloader(Activity activity, int showMode, int resultHandleMode) {
        mActivity = activity;
        mRequestParams = new RequestParams();


        switch (showMode) {
            case SHOW_TOAST_MSG:
                SHOW_TOAST_MODE = true;
                break;
            case SHOW_DIALOG_MSG:
                SHOW_DIALOG_MODE = true;
                break;
            case STILL_MSG:
                STILL_MODE = true;
                mRequestParams.setRefreshInterval();

                break;
        }

        if (!STILL_MODE)
            mHandler = new CallBackHandler();

        switch (resultHandleMode) {
            case INSERT_MSG:
                INSERT_MODE = true;
                break;
            case UPDATE_MSG:
                UPDATE_MODE = true;
                break;

        }
    }

    /**
     * запускаем новый поток
     * показываем диалог прогресса / toast
     * блокируем поток, до завершения всех дочерних потоков
     */
    @Override
    public void run() {
        if (SHOW_TOAST_MODE)
            mHandler.sendEmptyMessage(SHOW_TOAST_MSG);
        if (SHOW_DIALOG_MODE) {
            mHandler.sendEmptyMessage(SHOW_DIALOG_MSG);
        }
        addTokenToRequest();
        new GetJson(mBaseUrl, this, mRequestParams).run();

        if (!STILL_MODE)
            freezeThread();
    }

    /**
     * поток отработал успешно
     * завершаем его
     * в настройках сохраняем дату последнего обновления
     * в зависимости от флагов
     * вставляем/обновляем данные в базе
     * если на экране что то отображается, завершаем
     * завершаем заблокированный при старте поток
     */
    protected void finishSuccess() {
        Log.d("mimi", "finish sucess");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.ACTIVITY);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("lastUpdate", new Date().getTime());
        editor.apply();

         if (INSERT_MODE) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    insertData();
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (UPDATE_MODE) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    updateData();
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if (SHOW_TOAST_MODE)
            mHandler.sendEmptyMessage(SUCCESS_TOAST_MSG);
        if (SHOW_DIALOG_MODE)
            mHandler.sendEmptyMessage(DISMISS_DIALOG_MSG);
        if (SHOW_REFRESH_MODE)
            mHandler.sendEmptyMessage(DISMISS_REFRESH_MSG);
        if (!STILL_MODE)
            freezeThread();

        if (MyApplication.ACTIVITY.fragmentRetail == null)
            return;



        MyApplication.ACTIVITY.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                MyApplication.ACTIVITY.fragmentRetail.update();


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.ACTIVITY.fragmentDemand.update();
                    }
                }, 300);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.ACTIVITY.fragmentJust.update();
                    }
                }, 500);



            }
        });


    }

    /**
     * парсим результат http запроса
     *
     * @param json объект для парсинга
     */

    abstract protected void parseJson(JSONObject json);

    /**
     * запускаем все дочерние потоки
     */

    private void startAllThreads() {
         mExecutorService = Executors.newFixedThreadPool(MyApplication.MAX_THREAD);
        int pageCount = (mCount - mCount%50)/50;
        if(mCount % 50 > 0)
            pageCount++;

        for (int i = 2; i <= pageCount; ++i) {
            mRequestParams.setOffset(i);
            mExecutorService.execute(new GetJson(mBaseUrl, this, mRequestParams));
        }
    }

    /**
     * блокировщик потока, чтобы работала очередь задач
     * дожидается, пока все дочерние потоки отработают
     */
    private synchronized void freezeThread() {
        if (mCount != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else
            notify();
    }

    /**
     * здесь получаем json объект от GetJson
     * если mCount -1, записываем сколько объектов нужно получить и запускаем все потоки
     * если нет, просто парсим объект
     *
     * @param json объект для парсинга
     */

    public void catchJson(JSONObject json) {
        //если не определено кол-во данных в запросе, записываем его
        if (mCount == -1) {
            try {
                mCount = json.getInt("count");
                mAllRows = mCount;
                startAllThreads();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        parseJson(json);
    }

    /**
     * если GetJson получает любой код ответа от сервера кроме 200
     * если получили код 429 (too many requests) - этот запрос отправляем еще раз
     * прерываем метод
     * если передается другой код ошибки HTTP запроса или json непонятный,
     * прерываем выполнение потока
     * завершаем поток с ошибкой
     *
     * @param code HTTP код ответа сервера
     * @param url  запрашиваемый ресурс
     */

    public void catchJson(int code, URL url) {
        if (code == 429) {
            mExecutorService.execute(new GetJson(url.toString(), this, null));
            return;
        }
        finishError(code);
    }

    /**
     * аварийное завершение потока
     * убираем все с экрана,
     * показываем toast ошибка
     * сообщаем основной активности о прерванном запросе
     *
     * @param code HTTP код ответа сервера
     */
    protected void finishError(final int code) {
        if (STILL_MODE)
            return;
        if (SHOW_DIALOG_MODE)
            mHandler.sendEmptyMessage(DISMISS_DIALOG_MSG);
        if (SHOW_REFRESH_MODE)
            mHandler.sendEmptyMessage(DISMISS_REFRESH_MSG);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                errorToast(code);
            }
        });
        ((BrokenRequest) mActivity).breakRequest();

    }

    /**
     * если на экране диалог, обновляем прогресс 0-100 %
     */
    protected void publishProgress() {
        if (SHOW_DIALOG_MODE) {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt("progress", (100 - 100 * mCount / mAllRows));
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }

    /**
     * вставка данных в базу
     */
    abstract protected void insertData();

    /**
     * обновление данных в базе
     */
    abstract protected void updateData();

    /**
     * показать toast
     */
    @SuppressLint("WrongConstant")
    private void showToast() {
        mToast = new SuperActivityToast(mActivity, Style.TYPE_PROGRESS_CIRCLE);
        mToast.setText(mActivity.getResources().getString(R.string.try_connect));
        mToast.setIndeterminate(true);
        mToast.setProgressIndeterminate(true);
        mToast.setFrame(Style.FRAME_KITKAT);
        mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        mToast.show();
    }

    /**
     * скрыть toast
     */
    private void dismissToast() {
        mToast.dismiss();
    }

    /**
     * показать toast успех
     */
    @SuppressLint("WrongConstant")
    private void successToast() {
        dismissToast();
        mToast = new SuperActivityToast(mActivity, Style.green());
        mToast.setFrame(Style.FRAME_KITKAT);
        mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        mToast.setDuration(Style.DURATION_VERY_SHORT);
        mToast.setText(mActivity.getResources().getString(R.string.success));
        mToast.show();
    }

    /**
     * показать toast ошибка, + код ответа сервера
     * или варианты
     *
     * @param code код ответа сервера
     */
    @SuppressLint("WrongConstant")
    private void errorToast(int code) {
        Log.d("mimi", "finish filed");
        if (STILL_MODE)
            return;

        if (mToast != null)
            dismissToast();
        mToast = new SuperActivityToast(mActivity, Style.red());
        mToast.setFrame(Style.FRAME_KITKAT);
        mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        mToast.setDuration(Style.DURATION_VERY_SHORT);
        String title;
        switch (code) {
            case HttpsURLConnection.HTTP_UNAUTHORIZED:
                title = mActivity.getResources().getString(R.string.unauthorized);
                break;
            case HttpsURLConnection.HTTP_BAD_GATEWAY:
                title = mActivity.getResources().getString(R.string.service_unaviable);
                break;
            case HttpsURLConnection.HTTP_NOT_IMPLEMENTED:
                title = mActivity.getResources().getString(R.string.service_closed);
                break;
            case -2:
                title = mActivity.getResources().getString(R.string.check_connection);
                break;
            default:
                title = mActivity.getResources().getString(R.string.unknown_error) + " code " + code;
        }
        mToast.setText(title);
        mToast.show();
    }


    /**
     * показываем диалог прогресса
     */
    private void showDialog() {
        mDialog = new MaterialDialog.Builder(mActivity)
                .content(mActivity.getResources().getString(R.string.please_wait))
                .title(mDialogTitle)
                .progress(false, 100, true)
                .show();
        mDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * обновляем диалог
     *
     * @param progress значение прогресса 0-100%
     */
    private void updateDialog(int progress) {
        mDialog.setProgress(progress);
    }

    /**
     * скрыть диалог
     */
    private void dismissDialog() {
        mDialog.dismiss();
    }

    /**
     * скрыть кольцо обновления
     */
    private void dismissRefresh() {
        mRefreshCallBack.stopAnimation();
    }

    /**
     * класс для управления UI
     * диалогом, toast, кольцом рефреша
     */
    class CallBackHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.getData().getInt("progress") > 0) {
                updateDialog(msg.getData().getInt("progress"));
                return;
            }
            switch (msg.what) {
                case SHOW_TOAST_MSG:
                    showToast();
                    break;
                case SUCCESS_TOAST_MSG:
                    successToast();
                    break;
                case SHOW_DIALOG_MSG:
                    showDialog();
                    break;
                case DISMISS_DIALOG_MSG:
                    dismissDialog();
                    break;
                case DISMISS_REFRESH_MSG:
                    dismissRefresh();
                    break;
                default:
                    errorToast(msg.what);
            }
        }
    }
    protected void addTokenToRequest() {
        mRequestParams.setExtraParam("token="+Token.token+"&");
    }
}
