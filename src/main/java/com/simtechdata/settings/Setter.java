package com.simtechdata.settings;

import com.simtechdata.enums.Browser;

import static com.simtechdata.settings.LABEL.*;

class Setter {

    public void graal() {
        boolean value = true;
        AppSettings.Clear.graal();
        prefs.putBoolean(GRAAL.label(), value);
    }

    public void ignoreHistory() {
        boolean value = !AppSettings.Get.ignoreHistory();
        AppSettings.Clear.ignoreHistory();
        prefs.putBoolean(IGNORE_HISTORY.label(), value);
    }

    public void downloadFolder(String path) {
        AppSettings.Clear.downloadFolder();
        prefs.put(DOWNLOAD_FOLDER.label(), path);
    }

    public void lastUpdate(long value) {
        AppSettings.Clear.lastUpdate();
        prefs.putLong(LAST_UPDATE.label(), value);
    }

    public void browserHistoryFile(String value) {
        AppSettings.Clear.browserHistoryFile();
        prefs.put(BROWSER_HISTORY.label(), value);
    }

    public void browser(Browser value) {
        AppSettings.Clear.browser();
        prefs.put(BROWSER.label(), value.label());
        browserHistoryFile(value.getBrowserHistoryPath());
    }

}
