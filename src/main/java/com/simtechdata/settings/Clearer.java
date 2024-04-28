package com.simtechdata.settings;

import static com.simtechdata.settings.LABEL.*;

class Clearer {

    public void ignoreHistory() {
        prefs.remove(IGNORE_HISTORY.label());
    }

    public void graal() {
        prefs.remove(GRAAL.label());
    }

    public void downloadFolder() {
        prefs.remove(DOWNLOAD_FOLDER.label());
    }

    public void lastUpdate() {
        prefs.remove(LAST_UPDATE.label());
    }

    public void browserHistoryFile() {
        prefs.remove(BROWSER_HISTORY.label());
    }

    public void browser() {
        prefs.remove(BROWSER.label());
    }

    public void clearAll() {
        /**
         * Iterating the LABELs and removing them is preferential to just issuing a general .clear()
         * because it is possible that there might be other programs that have preferences using the
         * same package namespace and we don't want to wipe those out unintentionally.
         */
        for (String label : LABEL.getLabels()) {
            prefs.remove(label);
        }
    }
}
