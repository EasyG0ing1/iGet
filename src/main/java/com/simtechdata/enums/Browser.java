package com.simtechdata.enums;

import com.simtechdata.settings.AppSettings;

import java.util.ArrayList;
import java.util.List;

public enum Browser {

    BRAVE,
    CHROME,
    CHROMIUM,
    EDGE,
    FIREFOX,
    OPERA,
    SAFARI,
    VIVALDI;

    public static Browser getBrowser() {
        String b = AppSettings.Get.browser();
        return switch (b) {
            case "brave" -> BRAVE;
            case "chromium" -> CHROMIUM;
            case "edge" -> EDGE;
            case "firefox" -> FIREFOX;
            case "opera" -> OPERA;
            case "safari" -> SAFARI;
            case "vivaldi" -> VIVALDI;
            default -> CHROME;
        };
    }

    public static List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        for (Browser browser : Browser.values()) {
            labels.add(browser.label());
        }
        return labels;
    }

    public String label() {
        return this.name().toLowerCase();
    }

    public String getBrowserHistoryPath() {
        return switch (this) {
            case BRAVE -> OS.getOS().getBraveHistoryFile().getAbsolutePath();
            case CHROME -> OS.getOS().getChromeHistoryFile().getAbsolutePath();
            case CHROMIUM -> OS.getOS().getChromiumHistoryFile().getAbsolutePath();
            case EDGE -> OS.getOS().getEdgeHistoryFile().getAbsolutePath();
            case FIREFOX -> "";
            case OPERA -> OS.getOS().getOperaHistoryFile().getAbsolutePath();
            case SAFARI -> OS.getOS().getSafariHistoryFile().getAbsolutePath();
            case VIVALDI -> OS.getOS().getVivaldiHistoryFile().getAbsolutePath();
        };
    }
}
