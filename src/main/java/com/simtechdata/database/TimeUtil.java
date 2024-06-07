package com.simtechdata.database;

import com.simtechdata.enums.Browser;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final long WEBKIT_EPOCH_START = 11644473600000000L;

    public static String toMonthDayYear(long webkitTimestamp) {
        Browser browser = Browser.getBrowser();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yy");
        switch (browser) {
            case FIREFOX -> {
                Instant firefoxInstant = Instant.ofEpochMilli(webkitTimestamp / 1000);
                LocalDate firefoxDate = firefoxInstant.atZone(ZoneId.systemDefault()).toLocalDate();
                return formatter.format(firefoxDate);
            }

            case SAFARI -> {
                long secondsFrom1970To2001 = 978307200; // Seconds from Jan 1, 1970 to Jan 1, 2001
                Instant safariInstant = Instant.ofEpochSecond(webkitTimestamp + secondsFrom1970To2001);
                LocalDate safariDate = safariInstant.atZone(ZoneId.systemDefault()).toLocalDate();
                return formatter.format(safariDate);
            }

            default -> {
                long offsetBetweenEpochs = 11644473600000L; // milliseconds from Jan 1, 1601 to Jan 1, 1970
                Instant chromeInstant = Instant.ofEpochMilli((webkitTimestamp / 1000) - offsetBetweenEpochs);
                LocalDate chromeDate = chromeInstant.atZone(ZoneId.systemDefault()).toLocalDate();
                return formatter.format(chromeDate);
            }
        }
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
}
