package com.cardmonix.cardmonix.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {
    public static String getDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(formatter);
        return formatter.format(date);
    }
    public static Date getDateInString(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(formatter);
        return formatter.parse(date);
    }
    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    public static boolean isExpired(Date expiresAt) {
        if (expiresAt == null) {
            return false;
        }
        Date currentDate = new Date();
        return currentDate.after(expiresAt);
    }
}
