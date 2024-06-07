package com.simtechdata.database;

import com.simtechdata.crypto.FileHash;
import com.simtechdata.enums.OS;
import com.simtechdata.settings.AppSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import static com.simtechdata.enums.OS.NL;

public class SQLite {

    private static final File sqliteFile = OS.getAppFolder().resolve("database.sqlite").toFile();
    private static final File browserHistoryCopy = OS.getAppFolder().resolve("browserHistoryCopy.sqlite").toFile();
    private static final String databaseName = "database.sqlite";
    private static final String connString = STR."jdbc:sqlite:\{sqliteFile.getAbsolutePath()}";
    private static final String browserConnString = STR."jdbc:sqlite:\{browserHistoryCopy.getAbsolutePath()}";
    private static final File browserHistoryFile = new File(AppSettings.Get.browserHistoryFile());
    private static boolean browserHistoryFileExists = browserHistoryFile.exists();
    private static boolean historyCopyExists = browserHistoryCopy.exists();


    public SQLite() {
        if (!sqliteFile.exists()) {
            try {
                checkDatabase();
            }
            catch (SQLException | IOException ignored) {
            }
        }
    }


    public static void wipeDatabase() throws SQLException, IOException {
        new SQLite().createNewDatabase();
    }

    public static Connection getBrowserConnection() {
        try {
            checkBrowserFiles();
            return DriverManager.getConnection(browserConnString);
        }
        catch (SQLException ignored) {
        }
        return null;
    }

    private static void checkBrowserFiles() {
        browserHistoryFileExists = browserHistoryFile.exists();
        historyCopyExists = browserHistoryCopy.exists();
        if (!browserHistoryFileExists) {
            System.out.println(STR."Could not find Browser history file. The assumed path is:\{NL}\{browserHistoryFile.getAbsolutePath()}\{NL}");
            System.out.println("Run iget with the checkBrowser option");
            System.exit(1);
        }
        System.out.println("*".repeat(40));
        System.out.println(browserHistoryCopy.getAbsolutePath());
        System.out.println(browserHistoryFile.getAbsolutePath());
        System.out.println("*".repeat(40));
        copyHistoryFile();
    }

    private static void copyHistoryFile() {
        /**
         * To help reduce writes on SSD / NVMe drives, we only make a backup copy of
         * the web browsers history file if it is not the same as the copy we already have.
         */
        try {
            if (browserHistoryFileExists) {
                if (historyCopyExists) {
                    boolean same = FileHash.filesEqual(browserHistoryCopy, browserHistoryFile);
                    if (!same) {
                        FileUtils.forceDelete(browserHistoryCopy);
                        FileUtils.copyFile(browserHistoryFile, browserHistoryCopy);
                    }
                }
                else {
                    FileUtils.copyFile(browserHistoryFile, browserHistoryCopy);
                }
            }
        }
        catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    public static void checkBrowser() {
        browserHistoryFileExists = browserHistoryFile.exists();
        String filename = FilenameUtils.getName(browserHistoryFile.getAbsolutePath());
        if (!browserHistoryFileExists) {
            String msg = STR."""
                        Could not find \{AppSettings.Get.browser()} history file.
                        The filename is '\{filename}' and this is the usual default path of where it can be located:

                            \{browserHistoryFile.getAbsolutePath()}

                        If the browser is installed, it might not have been installed into the default location.

                        You need to search your local drive for the filename shown above, and it should be found
                        in a path that is almost identical to the path shown above. The only variation would be
                        the folders on the left part of the path.

                        Once you find the file, you can tell iGet where that file is by invoking the 'setBrowserPath'
                        option.

                            iget setBrowserPath

                        """;
            System.out.println(msg);
        }
        else {
            System.out.println(STR."\{AppSettings.Get.browser()} history file was located. Browser history features should work as expected.");
        }
    }

    private void checkDatabase() throws SQLException, IOException {
        if (!sqliteFile.exists())
            createNewDatabase();
    }

    public Connection getConn() {
        try {
            Connection conn = DriverManager.getConnection(connString);
            conn.setAutoCommit(true);
            conn.setSchema(databaseName);
            conn.prepareStatement("PRAGMA foreign_keys = ON;").execute();
            System.setOut(System.out);
            return conn;
        }
        catch (SQLException ignored) {
        }
        return null;
    }

    public void createNewDatabase() throws SQLException, IOException {
        if (!sqliteFile.exists()) {
            FileUtils.createParentDirectories(sqliteFile);
        }
        else {
            FileUtils.forceDelete(sqliteFile);
        }
        Connection conn = getConn();
        String[] tables = getSchema().split("SPLIT");
        for (String table : tables) {
            conn.createStatement().executeUpdate(table);
        }
        System.out.println("Database created");
    }

    private String getSchema() {
        return """
                CREATE TABLE "Links" (
                  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                  "Timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  "Link" TEXT NOT NULL ON CONFLICT IGNORE DEFAULT '',
                  "State" TEXT NOT NULL ON CONFLICT IGNORE DEFAULT '',
                  "DownloadPath" TEXT NOT NULL ON CONFLICT IGNORE DEFAULT ''
                );
                SPLIT
                CREATE TABLE "AddHistory" (
                  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                  "Timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  "Link" TEXT NOT NULL ON CONFLICT IGNORE DEFAULT '',
                  "State" TEXT NOT NULL ON CONFLICT IGNORE DEFAULT '',
                  "DownloadPath" TEXT NOT NULL ON CONFLICT IGNORE DEFAULT ''
                );
                """;
    }
}
