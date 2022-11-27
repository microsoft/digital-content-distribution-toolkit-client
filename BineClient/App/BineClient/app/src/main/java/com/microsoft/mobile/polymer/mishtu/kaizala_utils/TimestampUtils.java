package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.content.Context;
import androidx.annotation.Keep;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Keep
public class TimestampUtils {
    private static final long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long MINUTES_PER_DAY = 1440;
    private static final TimeZone sTimeZone = TimeZone.getDefault();

    /**
     * Gets the current system time
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }


    public static String convertTimestampToString(Context context, long timestamp) {
        if (timestamp == 0) {
            return "";
        }

        String strTimestamp = null;
        Calendar calendar = Calendar.getInstance(sTimeZone);

        // Get todays date and year
        int todayYear = calendar.get(Calendar.YEAR);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        // Set calender time based on message timestamp
        calendar.setTimeInMillis(timestamp);

        // Check if user messge is received on same day then show time otherwise return date based on device settings
        // Note : If user device time is in past, still we show time only
        if ((calendar.get(Calendar.YEAR)== todayYear) && (calendar.get(Calendar.DAY_OF_YEAR) >= dayOfYear) ) {
            strTimestamp = DateUtils.formatDateTime(context, timestamp, DateUtils
                    .FORMAT_SHOW_TIME);
        } else {
            strTimestamp = DateUtils.formatDateTime(context, timestamp, DateUtils
                    .FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
        }

        return strTimestamp;
    }

    public static boolean isToday(long timeStamp) {
        if (timeStamp == 0) {
            return false;
        }
        Calendar calendar = Calendar.getInstance(sTimeZone);
        int todayYear = calendar.get(Calendar.YEAR);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        // Set calender time based on message timestamp
        calendar.setTimeInMillis(timeStamp);

        // Check if user messge is received on same day then return true
        return (calendar.get(Calendar.YEAR) == todayYear) && (calendar.get(Calendar.DAY_OF_YEAR) >= dayOfYear);
    }

    /**
     * Calculates the time after the given no. of days from the current time
     */
    public static long calculateUpcomingTime(long currentTime, int days) {
        return currentTime + (days * MILLISECS_PER_DAY);
    }

    /**
     * Calculates difference between given time values in terms of years
     * @param afterTime time with higher value
     * @param beforeTime time with lower value than afterTime, ie., which comes before afterTime
     * @return no. of years between the two given times
     */
    public static long getYearsDiff(long afterTime, long beforeTime) {
        Calendar after = Calendar.getInstance(sTimeZone);
        after.setTimeInMillis(afterTime);
        Calendar before = Calendar.getInstance(sTimeZone);
        before.setTimeInMillis(beforeTime);
        int diff = after.get(Calendar.YEAR) - before.get(Calendar.YEAR);
        if (before.get(Calendar.MONTH) > after.get(Calendar.MONTH) ||
                (after.get(Calendar.MONTH) == before.get(Calendar.MONTH) && before.get(Calendar.DATE) > after.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    /**
     * Calculates difference between given time values in terms of days
     * @param afterTime time with higher value
     * @param beforeTime time with lower value than afterTime, ie., which comes before afterTime
     * @return no. of days between the two given times
     */
    public static long getDaysDiff(long afterTime, long beforeTime) {
        return TimeUnit.DAYS.convert(afterTime - beforeTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Calculates difference between given time values in terms of hours
     * @param afterTime time with higher value
     * @param beforeTime time with lower value than afterTime, ie., which comes before afterTime
     * @return no. of hours between the two given times
     */
    public static long getHoursDiff(long afterTime, long beforeTime) {
        return TimeUnit.HOURS.convert(afterTime - beforeTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Calculates difference of dates between the given date and the current date(Example: if difference == 1, then the given date is tomorrow wrt the current date)
     * @param time the given time
     * @param maxDiff the max sought difference of dates
     * @return difference between dates
     */
    public static int getDateDiff(long time, int maxDiff) {
        Calendar dueTime = Calendar.getInstance(sTimeZone);
        dueTime.setTimeInMillis(time);
        Calendar now = Calendar.getInstance(sTimeZone);
        int diff = 0;

        while (diff <= maxDiff) {
            if (dueTime.get(Calendar.DATE) == now.get(Calendar.DATE)) {
                return diff;
            }
            now.add(Calendar.DATE, 1);
            diff++;
        }
        return -1;
    }

    public static String getISO8601StringForDate(Date date) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(date);

    }

    public static boolean isMessageDayDifferent(long newerMessageTimestamp, long olderMessageTimestamp){
        Calendar calNewerMessage = Calendar.getInstance();
        calNewerMessage.setTimeInMillis(newerMessageTimestamp);

        Calendar calOlderMessage = Calendar.getInstance();
        calOlderMessage.setTimeInMillis(olderMessageTimestamp);

        return calNewerMessage.get(Calendar.YEAR) != calOlderMessage.get(Calendar.YEAR) ||
                calNewerMessage.get(Calendar.MONTH) != calOlderMessage.get(Calendar.MONTH) ||
                calNewerMessage.get(Calendar.DATE) > calOlderMessage.get(Calendar.DATE);
    }

    /**
     * This method converts the given UTC Date to device locale specific date time zone
     * @param dateString : dateTimeString in UTC timezone in "yyyy-MM-dd'T'HH:mm:ss" format
     * @return : time (the number of milliseconds since January 1, 1970) off setted to local timezone
     *
     * @exception ParseException in case the the dateString is not recognized/parse-able DateFormat string .
     */
    public static long ConvertServerUTCDateStringToLocaleDate(String dateString) throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        long time = formatter.parse(dateString).getTime();
        return time + TimeZone.getDefault().getDSTSavings();
    }


    public static String getLocalizedDateString(Context context, String dateString, String format) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = dateFormat.parse(dateString);

        int flags = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_ABBREV_MONTH
                | DateUtils.FORMAT_ABBREV_WEEKDAY
                | DateUtils.FORMAT_SHOW_WEEKDAY;

        return DateUtils.formatDateTime(context, date.getTime(), flags);
    }


    /**
     * Returns date in the format specified by format.
     * For e.g: If format looks like "MMM dd, yyyy 'at' HH:mm" time will look like this: Feb 12, 2019 at 15:13
     * @param timeStamp Timestamp(in long) which needs to be converted to desired string format.
     * @param pattern The format to which timestamp needs to be converted.
     * @return formatted string.
     */
    public static String getFormattedDate(long timeStamp, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(new Date(timeStamp));
    }


    public static Date getDateFromUTCString(String dateString){
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(dateString);

        } catch (ParseException e) { return new Date();}
    }

    public static Date getDateFromUTC(String dateString){
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(dateString);

        } catch (ParseException e) { return new Date();}
    }

    public static boolean isDateBeforeToday(Date date) {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        if (calendar1.get(Calendar.YEAR) < today.get(Calendar.YEAR)) return true; //Past year
        if (calendar1.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return false; // Future year
        if (calendar1.get(Calendar.MONTH) < today.get(Calendar.MONTH)) return true; // Past month
        if (calendar1.get(Calendar.MONTH) > today.get(Calendar.MONTH)) return false; //Future month
        return calendar1.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR); // same year & month
    }

    public static boolean isDatePastOrToday(Date date) {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        if (calendar1.get(Calendar.YEAR) < today.get(Calendar.YEAR)) return true; //Past year
        if (calendar1.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return false; // Future year
        if (calendar1.get(Calendar.MONTH) < today.get(Calendar.MONTH)) return true; // Past month
        if (calendar1.get(Calendar.MONTH) > today.get(Calendar.MONTH)) return false; //Future month
        return calendar1.get(Calendar.DAY_OF_YEAR) <= today.get(Calendar.DAY_OF_YEAR); // same year & month
    }
}
