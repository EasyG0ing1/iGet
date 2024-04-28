package com.simtechdata.database;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final long WEBKIT_EPOCH_START = 11644473600000000L;

    public static String toMonthDayYear(long webkitTimestamp) {
        long epochMilli = (webkitTimestamp - WEBKIT_EPOCH_START) / 1000;
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yy");
        return dateTime.format(formatter);
    }

    public static long toWebKitTime(long epochTime) {
        return (epochTime * 1000) + WEBKIT_EPOCH_START;
    }

    public static String toShortTime(long webkitTimestamp) {
        long epochMilli = (webkitTimestamp - WEBKIT_EPOCH_START) / 1000;
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm:ss a");
        return dateTime.format(formatter);
    }

    public static long convertToWebKitTimestamp(LocalDateTime dateTime) {
        long epochMilli = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        return epochMilli * 1000 + WEBKIT_EPOCH_START;
    }

    public static long getTimestampWithOffset(long hours, boolean all) {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime pastTime = all ? localDateTime.minusYears(30) : localDateTime.minusHours(hours);
        return convertToWebKitTimestamp(pastTime);
    }
}
