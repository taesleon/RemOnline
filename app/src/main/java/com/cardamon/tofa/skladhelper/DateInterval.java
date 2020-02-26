package com.cardamon.tofa.skladhelper;

import java.util.Calendar;
import java.util.Date;
/**
 * Created by dima on 31.10.17.
 */

public class DateInterval {

    /**
     * @return первый день текущей недели
     */
    public static Date getFirstDayOfWeek(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTime();
    }

    /**
     * @return последний день текущей недели
     */
    public static Date getLastDayOfWeek(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return cal.getTime();
    }
    /**
     * @param d дата
     * @return первый день месяца переданной даты
     */
    public static Date getFirstDayOfMonth(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    /**
     * @param d дата
     * @return последний день месяца переданной даты
     */
    public static Date getLastDayOfMonth(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }
    /**
     * сравнивает даты по полям год, месяц, дата
     *
     * @param date1 дата начала
     * @param date2 дата конца
     * @return true если даты равны
     */
    public static boolean simpleCompareDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        if (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            return true;
        }
        return false;
    }
    /**
     * @return сегодня
     */
    public static Date getToDay() {
        return new Date();
    }
}
