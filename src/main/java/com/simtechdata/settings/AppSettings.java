package com.simtechdata.settings;

import com.simtechdata.enums.Browser;

/**
 * This class is the gatekeeper for Getter, Setter and Clearer, which are package private classes that use
 * Javas Preferences class to maintain app settings that persist between program re-launches. The idea
 * is to have a class dedicated to each action for a Preference where setting, getting and clearing are
 * the main actions that would ever be used.
 * <p>
 * So the action classes being package private must be engaged through the AppSettings class.
 * <p>
 * LABEL class has the names of every different setting for the program.
 * <p>
 * Getter, Setter and Clearer will all have the same exact method names, one for each name in the LABEL class.
 * <p>
 * So for example, if we have an app setting LABEL called DATABASE_PATH, then we will have a method having the name
 * databasePath and that method will exist in Clear, Set and Get so that we merely need to engage the
 * AppSetting like this:
 * <p>
 * AppSettings.Set.databasePath(currentPath);
 * AppSettings.Get.databasePath();
 * AppSettings.Clear.databasePath();
 * <p>
 * The Clear class will remove the setting completely so that it no longer exists.
 **/

public class AppSettings {

    public static class Get {

        private static final Getter get = new Getter();

        public static boolean graal() {
            return get.graal();
        }

        public static boolean ignoreHistory() {
            return get.ignoreHistory();
        }

        public static String downloadFolder() {
            return get.downloadFolder();
        }

        public static long lastUpdate() {
            return get.lastUpdate();
        }

        public static String browserHistoryFile() {
            return get.browserHistoryFile();
        }

        public static String browser() {
            return get.browser();
        }
    }

    public static class Set {

        private static final Setter set = new Setter();

        public static void graal() {
            set.graal();
        }

        public static void ignoreHistory() {
            set.ignoreHistory();
        }

        public static void downloadFolder(String value) {
            set.downloadFolder(value);
        }

        public static void lastUpdate(long value) {
            set.lastUpdate(value);
        }

        public static void browserHistoryFile(String value) {
            set.browserHistoryFile(value);
        }

        public static void browser(Browser value) {
            set.browser(value);
        }
    }

    public static class Clear {

        private static final Clearer clear = new Clearer();

        public static void graal() {
            clear.graal();
        }

        public static void ignoreHistory() {
            clear.ignoreHistory();
        }

        public static void downloadFolder() {
            clear.downloadFolder();
        }

        public static void lastUpdate() {
            clear.lastUpdate();
        }

        public static void browserHistoryFile() {
            clear.browserHistoryFile();
        }

        public static void browser() {
            clear.browser();
        }

        public static void clearAll() {
            clear.clearAll();
        }
    }

}
