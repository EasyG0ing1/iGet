package com.simtechdata.enums;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


public enum OS {
    WIN, MAC, LINUX, SOLARIS, FREEBSD;

    public static final String NL = System.lineSeparator();
    private static final File braveHistoryFileMac = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "BraveSoftware", "Brave-Browser", "Default", "History").toFile();
    private static final File braveHistoryFileWindows = Paths.get(System.getProperty("user.home"), "AppData", "Local", "BraveSoftware", "Brave-Browser", "User Data", "Default", "History").toFile();
    private static final File braveHistoryFileLinux = Paths.get(System.getProperty("user.home"), ".config", "BraveSoftware", "Brave-Browser", "Default", "History").toFile();
    private static final File chromeHistoryFileMac = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Google", "Chrome", "Default", "History").toFile();
    private static final File chromeHistoryFileWindows = Paths.get(System.getProperty("user.home"), "AppData", "Local", "Google", "Chrome", "User Data", "Default", "History").toFile();
    private static final File chromeHistoryFileLinux = Paths.get(System.getProperty("user.home"), ".config", "google-chrome", "Default", "History").toFile();
    private static final File chromiumHistoryFileMac = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Chromium", "Default", "History").toFile();
    private static final File chromiumHistoryFileWindows = Paths.get(System.getProperty("user.home"), "AppData", "Local", "Chromium", "User Data", "Default", "History").toFile();
    private static final File chromiumHistoryFileLinux = Paths.get(System.getProperty("user.home"), ".config", "chromium", "Default", "History").toFile();
    private static final File edgeHistoryFileMac = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Microsoft Edge", "Default", "History").toFile();
    private static final File edgeHistoryFileWindows = Paths.get(System.getProperty("user.home"), "AppData", "Local", "Microsoft", "Edge", "User Data", "Default", "History").toFile();
    private static final File edgeHistoryFileLinux = Paths.get(System.getProperty("user.home"), ".config", "microsoft-edge", "Default", "History").toFile();
    private static final File operaHistoryFileMac = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "com.operasoftware.Opera", "History").toFile();
    private static final File operaHistoryFileWindows = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", "Opera Software", "Opera Stable", "History").toFile();
    private static final File operaHistoryFileLinux = Paths.get(System.getProperty("user.home"), ".config", "opera", "History").toFile();
    private static final File safariHistoryFileMac = Paths.get(System.getProperty("user.home"), "Library", "Safari", "History.db").toFile();
    private static final File vivaldiHistoryFileMac = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Vivaldi", "Default", "History").toFile();
    private static final File vivaldiHistoryFileWindows = Paths.get(System.getProperty("user.home"), "AppData", "Local", "Vivaldi", "User Data", "Default", "History").toFile();
    private static final File vivaldiHistoryFileLinux = Paths.get(System.getProperty("user.home"), ".config", "vivaldi", "Default", "History").toFile();
    private static OS osType;
    private static Path dataFolder;

    public static Path getAppFolder() {
        if (dataFolder == null) {
            if (isWindows()) {
                dataFolder = Paths.get(System.getenv("APPDATA"), "iGet");
            }
            else {
                dataFolder = Paths.get(System.getProperty("user.home"), ".iget");
            }
        }
        return dataFolder;
    }

    public static OS getOS() {
        if (isWindows())
            return OS.WIN;
        if (isMac())
            return OS.MAC;
        return OS.LINUX;
    }

    private static void setOSType() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            osType = OS.WIN;
        }
        else if (osName.contains("mac")) {
            osType = OS.MAC;
        }
        else if (osName.contains("linux")) {
            osType = OS.LINUX;
        }
        else if (osName.contains("sun")) {
            osType = OS.SOLARIS;
        }
        else if (osName.contains("free")) {
            osType = OS.FREEBSD;
        }
        else {
            osType = OS.LINUX;
        }
    }

    public static void setOSName() {
        if (osType == null) {
            setOSType();
        }
    }

    public static boolean isWindows() {
        if (osType == null) {
            setOSType();
        }
        return osType.equals(OS.WIN);
    }

    public static boolean isMac() {
        if (osType == null) {
            setOSType();
        }
        return osType.equals(OS.MAC);
    }

    public File getBraveHistoryFile() {
        return switch (this) {
            case WIN -> braveHistoryFileWindows;
            case MAC -> braveHistoryFileMac;
            default -> braveHistoryFileLinux;
        };
    }

    public File getChromeHistoryFile() {
        return switch (this) {
            case WIN -> chromeHistoryFileWindows;
            case MAC -> chromeHistoryFileMac;
            default -> chromeHistoryFileLinux;
        };
    }

    public File getChromiumHistoryFile() {
        return switch (this) {
            case WIN -> chromiumHistoryFileWindows;
            case MAC -> chromiumHistoryFileMac;
            default -> chromiumHistoryFileLinux;
        };
    }

    public File getEdgeHistoryFile() {
        return switch (this) {
            case WIN -> edgeHistoryFileWindows;
            case MAC -> edgeHistoryFileMac;
            default -> edgeHistoryFileLinux;
        };
    }

    public File getOperaHistoryFile() {
        return switch (this) {
            case WIN -> operaHistoryFileWindows;
            case MAC -> operaHistoryFileMac;
            default -> operaHistoryFileLinux;
        };
    }

    public File getSafariHistoryFile() {
        return switch (this) {
            case MAC -> safariHistoryFileMac;
            default -> null;
        };
    }

    public File getVivaldiHistoryFile() {
        return switch (this) {
            case WIN -> vivaldiHistoryFileWindows;
            case MAC -> vivaldiHistoryFileMac;
            default -> vivaldiHistoryFileLinux;
        };
    }
}
