package com.simtechdata.database;

import com.simtechdata.actions.Result;
import com.simtechdata.enums.Browser;
import com.simtechdata.enums.OS;
import com.simtechdata.enums.Reason;
import com.simtechdata.enums.State;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static com.simtechdata.enums.Reason.*;
import static com.simtechdata.enums.State.*;


public class DB {

    private static final SQLite sqLite = new SQLite();

    private static Connection getConn() {
        return sqLite.getConn();
    }

    public static State downloadedState(String link) {
        String SQL = "SELECT State FROM Links WHERE Link = ?;";
        try (Connection conn = getConn();
             PreparedStatement pst = conn.prepareStatement(SQL)) {
            pst.setString(1, link);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return State.valueOf(rs.getString(1));
                }
            }
            return DOES_NOT_EXIST;
        }
        catch (SQLException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    public static Reason addLink(String link, boolean clearDownloadedFlag) {
        boolean linkExists = linkExists(link);
        if (clearDownloadedFlag && linkExists) {
            clearDownloadedFlag(link);
            return ADDED_SUCCESSFULLY;
        }
        if (!linkExists) {
            String SQL = "INSERT INTO Links (Link, State, Timestamp) VALUES (?, ?, ?);";
            try (Connection conn = getConn();
                 PreparedStatement pst = conn.prepareStatement(SQL)) {
                pst.setString(1, link);
                pst.setString(2, NEW.name());
                pst.setLong(3, TimeUtil.toWebKitTime(System.currentTimeMillis()));
                pst.executeUpdate();
                return ADDED_SUCCESSFULLY;
            }
            catch (SQLException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
        else {
            return DUPLICATE_ENTRY;
        }
        return FAILED_DATABASE;
    }

    public static LinkedList<Link> getLinks(State state) {
        LinkedList<Link> list = new LinkedList<>();
        String SQL = "SELECT Link, Timestamp FROM Links WHERE State = ?;";
        try (Connection conn = getConn();
             PreparedStatement pst = conn.prepareStatement(SQL)) {
            pst.setString(1, state.name());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String link = rs.getString("Link");
                    long timestamp = rs.getLong("Timestamp");
                    list.addLast(new Link(link, timestamp));
                }
            }
        }
        catch (SQLException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return list;
    }

    public static void setDownloadResult(Result result) {
        String state = result.isSuccess() ? State.DOWNLOADED.name() : FAILED.name();
        String folder = result.isSuccess() ? result.getDownloadPath() : "";
        String link = result.getUrl();
        String SQL = "UPDATE Links SET State = ?, DownloadPath = ? WHERE Link = ?;";
        try (Connection conn = getConn();
             PreparedStatement pst = conn.prepareStatement(SQL)) {
            pst.setString(1, state);
            pst.setString(2, folder);
            pst.setString(3, link);
            pst.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private static void clearDownloadedFlag(String link) {
        String SQL = "UPDATE Links SET State = ?, Timestamp = ? WHERE Link = ?;";
        try (Connection conn = getConn();
             PreparedStatement pst = conn.prepareStatement(SQL)) {
            pst.setString(1, NEW.name());
            pst.setLong(2, System.currentTimeMillis());
            pst.setString(3, link);
            pst.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public static void deleteDownloadHistory() {
        String SQL = "DELETE FROM Links WHERE State = ?;";
        try (Connection conn = getConn();
             PreparedStatement pst = conn.prepareStatement(SQL)) {
            pst.setString(1, State.DOWNLOADED.name());
            pst.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public static boolean linkExists(String link) {
        String SQL = "SELECT Count(*) FROM Links WHERE Link = ?;";
        try (Connection conn = getConn();
             PreparedStatement pst = conn.prepareStatement(SQL)) {
            pst.setString(1, link);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        catch (SQLException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    public static LinkedList<BrowserHistory> getBrowserHistoryDates(boolean includeFailed) {
        LinkedList<Link> linkList = getBrowserHistory(-1, includeFailed);
        LinkedList<BrowserHistory> historyList = new LinkedList<>();
        for (Link link : linkList) {
            String mdy = link.getMonthDayYear();
            boolean exists = false;
            for (BrowserHistory history : historyList) {
                if (history.getDate().equals(mdy)) {
                    history.addLink(link);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                historyList.addLast(new BrowserHistory(mdy, link));
            }
        }
        return historyList;
    }

    public static LinkedList<Link> searchBrowser(String search) {
        LinkedList<Link> list = new LinkedList<>();
        String SQL = STR."SELECT url, last_visit_time as TIME from urls;";
        try (Connection conn = SQLite.getBrowserConnection();
             ResultSet rs = conn.createStatement().executeQuery(SQL)) {
            while (rs.next()) {
                String url = rs.getString("url");
                long time = rs.getLong("TIME");
                Link link = new Link(url, time);
                if (url.toLowerCase().contains(search.toLowerCase()))
                    list.addLast(link);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static LinkedList<Link> getBrowserHistory(int hours, boolean includeFailed) {
        Browser browser = Browser.getBrowser();
        LinkedList<Link> list = new LinkedList<>();
        Set<String> dupeList = new HashSet<>();
        long time = convertTimestamp(browser, (hours == -1) ? 0 : hours);
        String SQL = getSql(browser);
        Connection conn;
        try {
            conn = SQLite.getBrowserConnection();
            PreparedStatement pst;
            pst = conn.prepareStatement(SQL);
            pst.setLong(1, time);
            pst.setString(2, "%time%"); //NOT
            pst.setString(3, "%//www.youtube.com%watch%");
            pst.setString(4, "%time%"); //NOT
            pst.setString(5, "%youtube.com%embed%");
            pst.setString(6, "%instagram.com%=="); //NOT
            pst.setString(7, "%instagram.com%/reel/%");
            pst.setString(8, "%instagram.com%=="); //NOT
            pst.setString(9, "%instagram.com%/p/%");
            pst.setString(10, "%youtu.be%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String url = rs.getString("URL");
                    State state = downloadedState(url);
                    if (state.equals(NEW) || state.equals(DOES_NOT_EXIST) || (state.equals(FAILED) && includeFailed)) {
                        String[] parts = url.split("/");
                        boolean add = true;
                        if (parts.length > 4) {
                            String interest = parts[4];
                            if (dupeList.contains(interest)) {
                                add = false;
                            }
                            dupeList.add(interest);
                        }
                        if (add) {
                            long timestamp = rs.getLong("TIME");
                            Link link = new Link(url, timestamp);
                            list.addLast(link);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                System.out.println(stackTraceElement.toString());
            }
        }
        return list;
    }

    private static String getSql(Browser browser) {
        String SQL;
        switch (browser) {
            case FIREFOX -> SQL = """
                    SELECT url as URL, last_visit_date as TIME from moz_places
                    WHERE last_visit_date < ?
                    AND (
                        (url NOT LIKE(?) AND url LIKE(?))
                        OR (url NOT LIKE(?) AND url LIKE(?))
                        OR (url NOT LIKE(?) AND url LIKE(?))
                        OR (url NOT LIKE(?) AND url LIKE(?))
                        OR url LIKE(?)
                    ) ORDER BY last_visit_date;
                    """;
            case SAFARI -> SQL = """
                    SELECT
                        hi.id,
                        hi.url as URL,
                        hv.visit_time as TIME
                    FROM
                        history_items hi
                            JOIN
                        history_visits hv ON hi.id = hv.history_item
                            JOIN
                        (
                            SELECT
                                history_item,
                                MAX(visit_time) as MaxVisitTime
                            FROM
                                history_visits
                            GROUP BY
                                history_item
                        ) latest_visits ON hv.history_item = latest_visits.history_item AND hv.visit_time = latest_visits.MaxVisitTime
                    WHERE hv.visit_time < ?
                    AND (
                       (hi.url NOT LIKE(?) AND hi.url LIKE(?))
                    OR (hi.url NOT LIKE(?) AND hi.url LIKE(?))
                    OR (hi.url NOT LIKE(?) AND hi.url LIKE(?))
                    OR (hi.url NOT LIKE(?) AND hi.url LIKE(?))
                    OR hi.url LIKE(?)
                    ) ORDER BY hv.visit_time;
                    """;
            default -> SQL = """
                    SELECT url as URL, last_visit_time as TIME from urls
                     WHERE last_visit_time < ?
                     AND (
                        (url NOT LIKE(?) AND url LIKE(?))
                     OR (url NOT LIKE(?) AND url LIKE(?))
                     OR (url NOT LIKE(?) AND url LIKE(?))
                     OR (url NOT LIKE(?) AND url LIKE(?))
                     OR url LIKE(?)
                     ) ORDER BY last_visit_time;
                    """;
        }
        return SQL;
    }

    private static long convertTimestamp(Browser browser, long hours) {
        long currentMillis = System.currentTimeMillis();
        long adjustedMillis = currentMillis - (hours * 60 * 60 * 1000); // Subtract 5 hours
        long secondsFrom1970To2001 = 978307200;
        long unixTimeInSeconds = adjustedMillis / 1000; // Convert from milliseconds to seconds
        long offsetBetweenEpochs = 11644473600000L; // Milliseconds from January 1, 1601 to January 1, 1970
        return switch (browser) {
            case FIREFOX -> adjustedMillis * 1000;
            case SAFARI -> unixTimeInSeconds - secondsFrom1970To2001;
            default -> (adjustedMillis + offsetBetweenEpochs) * 1000;
        };
    }

    public static boolean remove(Link link) {
        String SQL = "DELETE FROM Links WHERE Link = ?;";
        try (Connection conn = getConn();
             PreparedStatement pst = conn.prepareStatement(SQL)) {
            pst.setString(1, link.getLink());
            pst.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public static void graal() {
        String SQL = "INSERT INTO Links (Link, State, DownloadPath) VALUES (?, ?, ?);";
        try (Connection conn = getConn();
             PreparedStatement pst = conn.prepareStatement(SQL)) {
            pst.setString(1, "NoLink");
            pst.setString(2, NEW.name());
            pst.setString(3, OS.getAppFolder().toString());
            pst.executeUpdate();
            String SQL_DEL = "DELETE FROM Links WHERE Link = 'NoLink';";
            conn.createStatement().executeUpdate(SQL_DEL);
        }
        catch (SQLException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }

    }
}
