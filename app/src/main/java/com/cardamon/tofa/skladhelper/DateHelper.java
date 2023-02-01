package com.cardamon.tofa.skladhelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by dima on 13.12.17.
 */

public class DateHelper {
    public static String convertMillisToDateSimple(long l) {
        return new SimpleDateFormat("dd-MM-yyyy").format(l);
    }

    public static long convertMSdateToLong(String date) {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        parser.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        Date res = null;
        try {
            res = parser.parse(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res.getTime();
    }

    public static String convertDoubleToString(double d) {
        return String.format("%1$,.2f", d);
    }
    public static String convertDoubleToStringNullDigit(double d) {
        return String.format("%1$,.0f", d);
    }
    /**
     * готовим даты для запроса к базе
     * начальная дата ставится 00 00 00
     * конечная 23 59 59
     *
     * @param date дата в long
     * @param mode режим, 0 - начальная дата, 1 конечная
     * @return дата в long
     */
    public static long prepareDateForRequest(long date, int mode) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        switch (mode) {
            case 0:
                calendar.set(Calendar.HOUR, 00);
                calendar.set(Calendar.MINUTE, 00);
                calendar.set(Calendar.SECOND, 00);
                break;
            case 1:
                calendar.set(Calendar.HOUR, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
        }
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        calendar.set(Calendar.AM_PM, Calendar.AM);
        return calendar.getTimeInMillis();
    }

    public static String convertMillisToDate(long l) {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy   HH:mm");
        return dateFormatGmt.format(l);
    }

    public static long[] getDatesForGraph(boolean prevYear) {
        long[] dates = new long[8];
        Calendar cal = Calendar.getInstance();
        //cal.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        cal.set(Calendar.AM_PM, Calendar.AM);

        // день недели год назад, например ВТОРНИК !
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (prevYear) {
            cal.add(Calendar.YEAR, -1);
            cal.set(Calendar.WEEK_OF_YEAR, week);
            cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        }
        cal = setZeroInDate(cal);
        dates[0] = cal.getTimeInMillis();//начало дня недели

        cal = set59InDate(cal);
        dates[1] = cal.getTimeInMillis();//конец дня недели
        dates[3] = cal.getTimeInMillis();//конец для текущего дня в неделе


        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        cal = setZeroInDate(cal);
        dates[2] = cal.getTimeInMillis(); //начало недели

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        cal = setZeroInDate(cal);


        dates[4] = cal.getTimeInMillis();//начало месяца

        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal = setZeroInDate(cal);
        dates[6] = cal.getTimeInMillis();//начало года

        cal.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        if (prevYear)
            cal.add(Calendar.YEAR, -1);
        cal = set59InDate(cal);
        dates[5] = cal.getTimeInMillis();//конец для текущего дня в месяце
        dates[7] = cal.getTimeInMillis();//конец для текущего дня в году

        return dates;
    }

    private static Calendar setZeroInDate(Calendar c) {
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.AM_PM, Calendar.AM);
        return c;
    }

    private static Calendar set59InDate(Calendar c) {
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.AM_PM, Calendar.AM);
        return c;
    }

    public static long[] getDayOfWeekByYear(int year) {
        long[] dates = new long[2];
        Calendar cal = Calendar.getInstance();
        //cal.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        cal.set(Calendar.AM_PM, Calendar.AM);

        // день недели год назад, например ВТОРНИК !
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        cal = setZeroInDate(cal);
        dates[0] = cal.getTimeInMillis();
        cal = set59InDate(cal);
        dates[1] = cal.getTimeInMillis();
        return dates;
    }
}
