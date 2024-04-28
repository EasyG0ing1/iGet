package com.simtechdata.settings;

import com.simtechdata.enums.Browser;

import static com.simtechdata.settings.LABEL.*;

class Getter {
    public boolean graal() {
        return prefs.getBoolean(GRAAL.label(), false);
    }

    public boolean ignoreHistory() {
        return prefs.getBoolean(IGNORE_HISTORY.label(), false);
    }

    public String downloadFolder() {
        return prefs.get(DOWNLOAD_FOLDER.label(), "Start");
    }

    public long lastUpdate() {
        return prefs.getLong(LAST_UPDATE.label(), 0L);
    }

    public String browserHistoryFile() {
        return prefs.get(BROWSER_HISTORY.label(), Browser.getBrowser().getBrowserHistoryPath());
    }

    public String browser() {
        return prefs.get(BROWSER.label(), Browser.CHROME.label());
    }
}
