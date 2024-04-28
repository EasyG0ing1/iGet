package com.simtechdata.actions;

public class Result {
    private final String url;
    private final String downloadPath;
    private final boolean success;

    public Result(String url, String downloadPath, boolean success) {
        this.url = url;
        this.downloadPath = downloadPath;
        this.success = success;
    }

    public String getUrl() {
        return url;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public boolean isSuccess() {
        return success;
    }
}
