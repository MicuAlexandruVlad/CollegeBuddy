package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static final String TAG = "TimeUtils";

    private String pattern;

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public String computeTimeDifference(String messageTime) throws ParseException {
        pattern = "MM/dd/yyyy HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date currentDate = format.parse(reformatTime(getCurrentTime()));
        Date messageDate = format.parse(reformatTime(messageTime));

        Log.d(TAG, "computeTimeDifference: current date -> " + currentDate.toString());
        Log.d(TAG, "computeTimeDifference:  message date -> " + messageDate.toString());

        long difference = currentDate.getTime() - messageDate.getTime();
        long hours, minutes, seconds;

        seconds = difference / 1000;
        minutes = seconds / 60;
        hours = minutes / 60;

        Log.d(TAG, "computeTimeDifference: seconds -> " + seconds);
        Log.d(TAG, "computeTimeDifference: minutes -> " + minutes);
        Log.d(TAG, "computeTimeDifference: hours -> " + hours);

        String text = getTimeText(seconds, minutes, hours, reformatTime(messageTime));

        if (text.equals(""))
            return "Error";
        else
            return text;
    }

    @SuppressLint("SimpleDateFormat")
    public String computeMessageDisplayTime(String messageTime) throws ParseException {
        pattern = "MM/dd/yyyy HH:mm:ss";

        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date currentDate = format.parse(reformatTime(getCurrentTime()));
        Date messageDate = format.parse(reformatTime(messageTime));

        long difference = currentDate.getTime() - messageDate.getTime();
        long hours, minutes, seconds;

        seconds = difference / 1000;
        minutes = seconds / 60;
        hours = minutes / 60;

        String text = getMessageTimeText(seconds, minutes, hours, reformatTime(messageTime));

        if (text.equals(""))
            return "Error";
        else
            return text;
    }

    @SuppressLint("SetTextI18n")
    private String getTimeText(long seconds, long minutes, long hours, String messageDate) {
        if (seconds < 30)
            return "Now";
        else if (seconds < 60 && seconds > 30)
            return "Less than a minute ago";
        else if (minutes == 1)
            return "1 minute ago";
        else if (minutes < 60 && minutes > 1)
            return minutes + " minutes ago";
        else if (hours == 1)
            return "1 hour ago";
        else if (hours < 24 && hours > 1)
            return hours + " hours ago";
        else if (hours >= 24 && hours < 48)
            return "Yesterday";
        else if (hours >= 48)
            return messageDate;

        return "";
    }

    @SuppressLint("SetTextI18n")
    private String getMessageTimeText(long seconds, long minutes, long hours, String messageDate) {

        Log.d(TAG, "getMessageTimeText: message date -> " + messageDate);

        String currentTime = getCurrentTime();
        String currentDay = currentTime.split(" ")[2];
        String currentYear = currentTime.split(" ")[5];
        String currentMonth = getMonthNumber(currentTime.split(" ")[1]);
        String messageDay = messageDate.split("/")[1];
        String messageMonth = messageDate.split("/")[0];
        String messageYear = messageDate.split(" ")[0].split("/")[2];

        Log.d(TAG, "getMessageTimeText: message year -> " + messageYear);
        Log.d(TAG, "getMessageTimeText: current year -> " + currentYear);
        Log.d(TAG, "getMessageTimeText: message month -> " + messageMonth);
        Log.d(TAG, "getMessageTimeText: current month -> " + currentMonth);
        Log.d(TAG, "getMessageTimeText: message day -> " + messageDay);
        Log.d(TAG, "getMessageTimeText: current day -> " + currentDay);

        String time = messageDate.split(" ")[1].split(":")[0] + ":" +
                messageDate.split(" ")[1].split(":")[1];
        if (messageYear.equalsIgnoreCase(currentYear)) {
            if (currentMonth.equalsIgnoreCase(messageMonth)) {
                if (messageDay.equalsIgnoreCase(currentDay))
                    return time;
                else {
                    return messageMonth + "/" + messageDay + "   " + time;
                }
            }
            else {
                return messageMonth + "/" + messageDay + "   " + time;
            }
        }

        return messageMonth + "/" + messageDay + "/" + messageYear + "   " + time;
    }

    private String reformatTime(String time) {
        String c[] = time.split(" ");
        return getMonthNumber(c[1]) + "/" + c[2] + "/" + c[5] + " " + c[3];
    }

    private String getMonthNumber(String month) {
        if (month.equalsIgnoreCase("Jan"))
            return "01";
        else if (month.equalsIgnoreCase("Feb"))
            return "02";
        else if (month.equalsIgnoreCase("Mar"))
            return "03";
        else if (month.equalsIgnoreCase("Apr"))
            return "04";
        else if (month.equalsIgnoreCase("May"))
            return "05";
        else if (month.equalsIgnoreCase("Jun"))
            return "06";
        else if (month.equalsIgnoreCase("Jul"))
            return "07";
        else if (month.equalsIgnoreCase("Aug"))
            return "08";
        else if (month.equalsIgnoreCase("Sep"))
            return "09";
        else if (month.equalsIgnoreCase("Oct"))
            return "10";
        else if (month.equalsIgnoreCase("Nov"))
            return "11";
        else if (month.equalsIgnoreCase("Dec"))
            return "12";

        return "";
    }

    private String getCurrentTime() {
        return Calendar.getInstance().getTime().toString();
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
