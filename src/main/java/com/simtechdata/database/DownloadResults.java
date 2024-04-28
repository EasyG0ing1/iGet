package com.simtechdata.database;

import java.util.LinkedList;

public class DownloadResults {

    private static final LinkedList<Link> failedList = new LinkedList<>();

    public static void addFailedDownload(Link link) {
        failedList.addLast(link);
    }

    public static LinkedList<Link> getFailedList() {
        LinkedList<Link> list = new LinkedList<>(failedList);
        failedList.clear();
        return list;
    }

    public static boolean haveFailures() {
        return !failedList.isEmpty();
    }
}
