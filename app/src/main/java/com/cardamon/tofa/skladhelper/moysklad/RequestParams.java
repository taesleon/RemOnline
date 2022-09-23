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
     * @param offset отступ, офсет
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @param dateFrom установка начальной даты
     */
    public void setDateFrom(long dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
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
        calendar.setTimeInMillis(prefs.getLong("lastUpdate", 1546293600000L));
        calendar.add(Calendar.DATE, -MyApplication.UPDATE_PERIOD_DAYS);
        setDateFrom(calendar.getTimeInMillis());
        setDateTo(new Date().getTime());
    }

    /**
     * установка интервала для загрузки всех документов
     */
    public void setAllInterval() {
        this.setDateFrom(1655413201000L);
        this.setDateTo(new Date().getTime());
    }

    /**
     * @return возврат готовой строки с параметрами
     */
    public String getParams() {
        int n = this.limit;
        String result = "?";
        if (n != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(result);
            stringBuilder.append("limit=");
            stringBuilder.append(this.limit);
            stringBuilder.append("&");
            result = stringBuilder.toString();
        }
        if (this.offset != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(result);
            stringBuilder.append("page=");
            stringBuilder.append(this.offset);
            stringBuilder.append("&");
            result = stringBuilder.toString();
        }
        if (this.dateFrom != 0L && this.dateTo != 0L) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(result);
            stringBuilder.append("created_at[]=");
            stringBuilder.append(this.dateFrom);
            stringBuilder.append("&created_at[]=");
            stringBuilder.append(this.dateTo);
            stringBuilder.append("&");
            result = stringBuilder.toString();
        }
        if (this.extraParam != "") {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(result);
            stringBuilder.append(this.extraParam);
            result = stringBuilder.toString();
        }
        return result;
    }

    /**
     * ковертация даты long в формат для http запроса
     *
     * @param l дата в миллисекундах
     * @return отформатированная дата
     */
    public static String convertLong(long l) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone((String) "Europe/Kiev"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(l);
        return simpleDateFormat.format(calendar.getTime());
    }

    public void clear() {
        extraParam = "";
        limit = 0;
        offset = 0;
        dateFrom = 0;
        dateTo = 0;
    }

    public void setExtraParam(String str) {
        extraParam += str;
    }

    public void clearExtraParam() {
        this.extraParam = "";
    }
    public void setAllIntervalForSites() {
        setAllInterval();
        setExtraParam("filter=moment%3E"+RequestParams.convertLong(961913449L)+";moment%3C"+RequestParams.convertLong(this.dateTo)+"&");
    }

}
