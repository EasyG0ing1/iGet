package com.simtechdata.database;

import com.simtechdata.actions.Actions;
import com.simtechdata.actions.DownloadLink;
import com.simtechdata.actions.Result;
import com.simtechdata.enums.State;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.simtechdata.enums.State.*;

public class Link {

    private final String link;
    private final long timestamp;

    public Link(String link, long timestamp) {
        this.link = formatLink(link);
        this.timestamp = timestamp == -1 ? System.currentTimeMillis() : timestamp;
    }

    private static String formatIGLink(String link) {
        link = link.replace("instagram.com/reels", "instagram.com/reel");
        String[] items = link.replaceFirst("//", "/").split("/");
        StringBuilder sb = new StringBuilder(items[0]).append("/");
        for (int x = 1; x < items.length; x++) {
            sb.append("/").append(items[x]);
        }
        if (!sb.toString().contains("?utm_source=ig_embed"))
            sb.append("/").append("?utm_source=ig_embed");
        return sb.toString();
    }

    private String formatLink(String link) {
        if (link.toLowerCase().contains("youtube") || link.toLowerCase().contains("tu.be")) {
            return link;
        }
        String[] parts = link.replaceFirst("//", "/").split("/");
        int last = parts.length - 1;
        StringBuilder newURL = new StringBuilder(STR."\{parts[0]}//");
        if (parts[last].startsWith("?")) {
            for (int x = 1; x < last; x++) {
                newURL.append(parts[x]).append("/");
            }
        }
        else {
            newURL = new StringBuilder(link);
        }
        return newURL.toString().contains("instagram") ? formatIGLink(newURL.toString()) : newURL.toString();
    }

    public String getMonthDayYear() {
        return TimeUtil.toMonthDayYear(timestamp);
    }

    public void download(boolean retryFailed) {
        new Thread(() -> {
            State state = DB.downloadedState(getLink());
            boolean download = (state.equals(FAILED) && retryFailed) || state.equals(DOES_NOT_EXIST) || state.equals(NEW);
            if (!download) {
                System.out.println(STR."State: \{state.name()} for link: \{link} ");
                return;
            }
            Future<Result> future = Actions.executor.submit(new DownloadLink(this));
            try {
                Result result = future.get();
                if (result.isSuccess()) {
                    System.out.println(STR."Download successful: \{link} to \{result.getDownloadPath()}");
                }
                else {
                    if (this.link.contains(".com/reel")) {
                        Link link = new Link(this.link.replaceFirst("reel", "p"), timestamp);
                        System.out.println(STR."Download failed: \{result.getUrl()} retrying with \{link.getLink()}");
                        link.download(retryFailed);
                    }
                    else
                        System.out.println(STR."Download failed: \{result.getUrl()}");
                    DownloadResults.addFailedDownload(this);
                }
            }
            catch (InterruptedException | ExecutionException e) {
                System.out.println(STR."Download failed: \{getLink()}");
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }).start();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return STR."\{TimeUtil.toShortTime(timestamp)} - \{getLink()}";
    }
}
