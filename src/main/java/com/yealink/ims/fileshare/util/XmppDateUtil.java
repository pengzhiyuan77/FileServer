package com.yealink.ims.fileshare.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * xmpp时间转化 UTC
 * author:pengzhiyuan
 * Created on:2016/5/26.
 */
public class XmppDateUtil {
    private static final DateFormat dateFormat;
    private static final DateFormat dateFormatWithoutMillis;
    private static final DateFormat localFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormatWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormatWithoutMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
        localFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        localFormat.setTimeZone(TimeZone.getDefault());
    }

    private XmppDateUtil() {
    }

    /**
     * UTC时间 字符串转换为日期
     * @param dateString
     * @return
     */
    public static Date parseDate(String dateString) {
        Date date = null;
        if (dateString == null) {
            return null;
        }
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // ignore
        }
        if (date != null) {
            return date;
        }
        try {
            date = dateFormatWithoutMillis.parse(dateString);
        } catch (ParseException e) {
            // ignore
        }
        return date;
    }

    /**
     * UTC时间 日期转换为字符串
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }

    /**
     * UTC时间转换为本地时间
     * @param utcTime
     * @return
     */
    public static String utcToLocal(String utcTime) {
        Date utcDate = parseDate(utcTime);
        if (utcDate == null) {
            return "";
        }
        String localTime = localFormat.format(utcDate.getTime());
        return localTime;
    }

    /**
     * UTC时间转换为本地时间
     * @param utcDate
     * @return
     */
    public static String utcTimeToLocal(Date utcDate) {
        if (utcDate == null) {
            return "";
        }
        String localTime = localFormat.format(utcDate.getTime());
        return localTime;
    }

    /**
     * 本地时间转换
     * @param dateString
     * @return
     */
    public static Date parseToLocalDate(String dateString) {
        Date date = null;
        if (dateString == null || "".equals(dateString)) {
            return null;
        }
        try {
            date = localFormat.parse(dateString);
        } catch (ParseException e) {
            // ignore
        }
        return date;
    }

    /**
     * 取得当前的系统日期
     *
     * @return String
     */
    public static String getNowDate() {
        String thedate = "";
        String themonth = "";
        int thedate1 = 0;
        int themonth1 = 0;
        String nowday = "";
        Calendar calendar = Calendar.getInstance();

        thedate1 = calendar.get(Calendar.DATE);
        if ((thedate1 == 1) || (thedate1 == 2) || (thedate1 == 3) || (thedate1 == 4) || (thedate1 == 5) || (thedate1 == 6) || (thedate1 == 7) || (thedate1 == 8) || (thedate1 == 9)) {
            thedate = "0" + thedate1;
        }else {
            thedate = String.valueOf(thedate1);
        }
        themonth1 = calendar.get(Calendar.MONTH) + 1;
        if ((themonth1 == 1) || (themonth1 == 2) || (themonth1 == 3) || (themonth1 == 4) || (themonth1 == 5) || (themonth1 == 6) || (themonth1 == 7) || (themonth1 == 8) || (themonth1 == 9)) {
            themonth = "0" + themonth1;
        }else {
            themonth = String.valueOf(themonth1);
        }
        nowday = calendar.get(Calendar.YEAR) + "-" + (themonth) + "-" + (thedate); //当前日期
        return nowday;
    }

    /**
     * 取得当前的年
     * @return String
     */
    public static String getNowYear() {
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.get(Calendar.YEAR));
    }

    /**
     * 取得当前的月
     * @return String
     */
    public static String getNowMonth() {
        String themonth="";
        Calendar calendar = Calendar.getInstance();
        int themonth1 = calendar.get(Calendar.MONTH) + 1;
        if ((themonth1 == 1) || (themonth1 == 2) || (themonth1 == 3) || (themonth1 == 4) || (themonth1 == 5) || (themonth1 == 6) || (themonth1 == 7) || (themonth1 == 8) || (themonth1 == 9)) {
            themonth = "0" + themonth1;
        }else {
            themonth = String.valueOf(themonth1);
        }
        return themonth;
    }

    /**
     * 取得当前的日
     * @return String
     */
    public static String getNowDay() {
        String thedate = "";
        Calendar calendar = Calendar.getInstance();
        int thedate1 = calendar.get(Calendar.DATE);
        if ((thedate1 == 1) || (thedate1 == 2) || (thedate1 == 3) || (thedate1 == 4) || (thedate1 == 5) || (thedate1 == 6) || (thedate1 == 7) || (thedate1 == 8) || (thedate1 == 9)) {
            thedate = "0" + thedate1;
        }else {
            thedate = String.valueOf(thedate1);
        }
        return thedate;
    }

}
