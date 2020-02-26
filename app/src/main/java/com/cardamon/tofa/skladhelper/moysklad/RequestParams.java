package com.cardamon.tofa.skladhelper.moysklad;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cardamon.tofa.skladhelper.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by dima on 20.12.17.
 * объект описывающий дополнительные параметры HTTP запроса к серверу моего склада
 */

public class RequestParams {
    private int limit = 0;
    private int offset = 0;
    private long dateFrom = 0;
    private long dateTo = 0;
    private String extraParam = "";

    /**
     * @return начальная дата диапазона запроса
     */
    public long getDateFrom() {
        return dateFrom;
    }

    /**
     * @return конечная дата диапазона запроса
     */
    public long getDateTo() {
        return dateTo;
    }

    /**
     * @param limit кол-во записей в запросе
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     *
     * @param offset отступ, офсет
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     *
     * @param dateFrom установка начальной даты
     */
    public void setDateFrom(long dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     *
     * @param dateTo установка конечной даты
     */
    public void setDateTo(long dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * параметр показывающий в запросе расширенные позиции, розница, продажа, возврат
     */
    public void setExpandPositions() {
        extraParam += "expand=positions&";
    }

    /**
     * установка интервала обновления
     */
    public void setRefreshInterval() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.ACTIVITY);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(prefs.getLong("lastUpdate", 946684740000l));
        calendar.add(Calendar.DATE, - 3);
        setDateFrom(calendar.getTimeInMillis());
        setDateTo(new Date().getTime());
    }

    /**
     * установка интервала для загрузки всех документов
     */
    public void setAllInterval() {
        setDateFrom(946684740000l);//31.12.1999
        setDateTo(new Date().getTime());
    }

    /**
     *
     * @return возврат готовой строки с параметрами
     */
    public String getParams() {
        String params = "?";
        if (limit != 0)
            params += "limit=" + limit + "&";
        if (offset != 0)
            params += "offset=" + offset + "&";
        if (dateFrom != 0 && dateTo != 0)
            params += "filter=moment%3E" + convertLong(dateFrom) + ";moment%3C" + convertLong(dateTo) + "&";
        if (extraParam != "")
            params += extraParam;
        return params;
    }

    /**
     * ковертация даты long в формат для http запроса
     * @param l дата в миллисекундах
     * @return отформатированная дата
     */
    public static String convertLong(long l) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(l);
        return formatter.format(cal.getTime());
    }

    public void clear(){
        extraParam = "";
        limit = 0;
        offset = 0;
        dateFrom = 0;
        dateTo = 0;
    }
}
