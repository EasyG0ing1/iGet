package com.simtechdata.database;


import com.simtechdata.actions.Actions;
import com.simtechdata.actions.Watch;
import com.simtechdata.crypto.Algo;
import com.simtechdata.crypto.FileHash;
import com.simtechdata.enums.Folder;
import com.simtechdata.enums.OS;
import com.simtechdata.settings.AppSettings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static com.simtechdata.enums.OS.NL;

/**
 * This class makes it easy to auto generate graal json config files by engaging every possible java feature that the program does.
 */
public class Graal {

    private static final File sqliteFile = OS.getAppFolder().resolve("tempDatabase.sqlite").toFile();
    private static final String databaseName = "Temp";
    private static final String connString = STR."jdbc:sqlite:\{sqliteFile.getAbsolutePath()}";


    public void run() {
        try {
            OS.setOSName();
            createFile();
            addRecord();
            System.out.println("Record Added");
            getRecord();
            System.out.println(" - Record Retrieved");
            clear();
            System.out.println("Table Wiped");
            String hash = FileHash.getFileHash(sqliteFile, Algo.SHA_256);
            System.out.println(STR."File hash: \{hash}");
            deleteFile();
            System.out.println("Database file deleted");
            AppSettings.Set.graal();
            AppSettings.Get.graal();
            AppSettings.Clear.graal();
            System.out.print(STR."\{NL}Program Version: ");
            Actions.showVersion();
            AppSettings.Clear.lastUpdate();
            Folder.copyResourceToFile();
            System.out.println(STR."\{NL}Copy an instagram link to the clipboard and wait for the download to finish then manually stop the program");
            Watch.startGraal();
        }
        catch (SQLException | IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    private Connection getConn() throws SQLException {
        Connection conn = DriverManager.getConnection(connString);
        if (conn == null) {
            System.out.println("Connection failed");
            System.exit(0);
        }
        return conn;
    }

    private void createFile() throws SQLException, IOException {
        if (!sqliteFile.exists()) {
            Files.createDirectories(sqliteFile.getParentFile().toPath());
        }
        else {
            deleteFile();
        }
        if (checkFile()) {
            final Connection conn = getConn();
            conn.createStatement().executeUpdate(getSchema());
            System.setOut(System.out);
            System.out.println("Doing GraalVM Tasks");
            System.out.println("Database created");
        }
        else {
            System.out.println(STR."Database could not be created, check folder permissions and try again\{NL}\{NL}\tFolder: \{sqliteFile.getParent()}");
            throw new RuntimeException(STR."Database could not be created, check folder permissions and try again\{NL}\{NL}\tFolder: \{sqliteFile.getParent()}");
        }
    }

    private boolean checkFile() throws SQLException {
        Connection conn = getConn();
        conn.setAutoCommit(true);
        conn.setSchema(databaseName);
        return sqliteFile.exists();
    }

    private void deleteFile() {
        try {
            FileUtils.forceDeleteOnExit(sqliteFile);
            if (sqliteFile.getParentFile().listFiles().length == 0) {
                FileUtils.forceDeleteOnExit(sqliteFile.getParentFile());
            }
        }
        catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    private String getSchema() {
        return """
                CREATE TABLE "Test" (
                  "id" INTEGER NOT NULL PRIMARY KEY,
                  "Item1" TEXT NOT NULL ON CONFLICT IGNORE DEFAULT ''
                );
                """;
    }

    private void addRecord() throws SQLException {
        String SQL = "INSERT INTO Test (id, Item1) VALUES (1, 'Test Data')";
        Connection conn = getConn();
        conn.createStatement().executeUpdate(SQL);
    }

    private void getRecord() throws SQLException {
        String SQL = "SELECT * FROM Test;";
        Connection conn = getConn();
        ResultSet rs = conn.createStatement().executeQuery(SQL);
        while (rs.next()) {
            System.out.print(rs.getString("Item1"));
        }
    }

    private void clear() throws SQLException {
        String SQL = "DELETE FROM Test WHERE id > 0;";
        Connection conn = getConn();
        conn.createStatement().executeUpdate(SQL);
    }
}
