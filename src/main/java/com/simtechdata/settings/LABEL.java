package com.simtechdata.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

enum LABEL {

    GRAAL,
    IGNORE_HISTORY,
    DOWNLOAD_FOLDER,
    LAST_UPDATE,
    BROWSER_HISTORY,
    BROWSER;

    public static final Preferences prefs = Preferences.userNodeForPackage(LABEL.class);
    /**
     * We include the app name pre-pended to the label name for the purposes of keeping these
     * settings unique for this program, to avoid label conflict between programs since we are
     * using Javas Preferences class.
     */
    private static final String APP = "Igram_";

    public static List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        for (LABEL l : values()) {
            labels.add(l.label());
        }
        return labels;
    }

    public String label() {
        return APP + this.name();
    }
}
