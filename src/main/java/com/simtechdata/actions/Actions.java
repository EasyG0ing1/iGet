package com.simtechdata.actions;

import com.simtechdata.database.*;
import com.simtechdata.enums.Browser;
import com.simtechdata.enums.OS;
import com.simtechdata.enums.Reason;
import com.simtechdata.enums.State;
import com.simtechdata.settings.AppSettings;
import com.simtechdata.settings.Search;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

import static com.simtechdata.enums.OS.NL;
import static com.simtechdata.enums.Reason.DUPLICATE_ENTRY;

public class Actions {

    public static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);


    public static void deleteHistory() {
        DB.deleteDownloadHistory();
        System.out.println(STR."\{NL}  Successful download history has been removed from the database\{NL}");
    }

    public static void remove() {
        LinkedList<Link> list = DB.getLinks(State.NEW);
        if (list.isEmpty()) {
            showNothing();
        }
        else {
            Map<Integer, Link> map = new HashMap<>();
            int x = 1;
            for (Link link : list) {
                map.put(x, link);
                x++;
            }
            int choice = -1;
            while (choice < 0) {
                for (int idx : map.keySet()) {
                    String line = STR."\{idx}) \{map.get(idx)}";
                    System.out.println(line);
                }
                System.out.print("Which one (0 to quit): ");
                choice = new Scanner(System.in).nextInt();
            }
            if (choice > 0) {
                if (DB.remove(map.get(choice))) {
                    System.out.println("Success");
                }
                else {
                    System.out.println("Failed, check your selection");
                }
            }
        }
    }

    public static void addBrowserHistory(int hours, boolean includeFailed) {
        LinkedList<Link> list = DB.getBrowserHistory(hours, includeFailed);
        if (list.isEmpty()) {
            showNothing();
        }
        else {
            LinkedList<Link> newList = new LinkedList<>();
            for (Link link : list) {
                if (!DB.linkExists(link.getLink()))
                    newList.addLast(link);
            }
            String answer = "";
            while (answer.isEmpty()) {
                for (Link link : newList) {
                    System.out.println(link);
                }
                System.out.print("Add these links? (Y/N): ");
                answer = new Scanner(System.in).nextLine();
            }
            if (answer.toLowerCase().contains("y")) {
                boolean override = false;
                for (Link link : newList) {
                    if (link.getLink().length() > 1) {
                        override = true;
                    }
                    addLink(link, override);
                }
            }
        }
    }

    public static void downloadLink(String lnk) {
        Link link = new Link(lnk, System.currentTimeMillis());
        try {
            Future<Result> future = executor.submit(new DownloadLink(link));
            Result result = future.get();
            if (!result.isSuccess()) {
                System.out.println(STR."FAILED: \{result.getUrl()}");
            }
            else {
                System.out.println(STR."SUCCESS: \{result.getUrl()}\{NL}TO: \{result.getDownloadPath()}\{NL}");
            }
        }
        catch (ExecutionException | InterruptedException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    public static void downloadLinks() {
        LinkedList<Link> list = DB.getLinks(State.NEW);
        if (list.isEmpty()) {
            showNothing();
        }
        else {
            LinkedList<Future<Result>> futureList = new LinkedList<>();
            for (Link link : list) {
                futureList.addLast(executor.submit(new DownloadLink(link)));
            }
            boolean done = false;
            while (!done) {
                done = true;
                for (Future<Result> future : futureList) {
                    if (!future.isDone()) {
                        done = false;
                        break;
                    }
                }
                sleep(500);
            }
            System.out.println(STR."\{NL}Download Failures:");
            boolean failures = false;
            for (Future<Result> future : futureList) {
                try {
                    Result result = future.get();
                    if (!result.isSuccess()) {
                        System.out.println(STR."    \{result.getUrl()}\{NL}TO: \{result.getDownloadPath()}\{NL}");
                        failures = true;
                    }
                }
                catch (InterruptedException | ExecutionException e) {
                    System.out.println(Arrays.toString(e.getStackTrace()));
                }
            }
            if (!failures) {
                System.out.println(STR."    None!\{NL}");
            }
            else {
                System.out.println(NL);
            }
        }
    }

    public static void addLink(Link link, boolean override) {
        Reason reason = DB.addLink(link.getLink(), override);
        System.out.println(switch (reason) {
            case ADDED_SUCCESSFULLY -> STR."Added \{link}";
            case FAILED_DATABASE -> "Database Failure, link not added";
            case DUPLICATE_ENTRY -> STR."Link already exists: \{link}";
            default -> "";
        });
    }

    public static void showHistory() {
        System.out.print("Include failed downloads in list? (Y/N): ");
        String option = new Scanner(System.in).nextLine();
        boolean includeFailed = option.toLowerCase().contains("y");
        LinkedList<BrowserHistory> historyList = DB.getBrowserHistoryDates(includeFailed);
        boolean retryFailed = false;
        while (true) {
            StringBuilder sb = new StringBuilder(STR."Which date would you like to download?\{NL + NL}");
            int c = 0;
            for (BrowserHistory history : historyList) {
                String date = history.getDate();
                int count = history.getCount();
                sb.append(c).append(") ").append(date).append(STR." (\{count})").append(NL);
                c++;
            }
            int toggleFailed = c;
            sb.append(toggleFailed).append(") Retry Failed Downloads: ").append(retryFailed).append(NL);
            int exit = c + 10;
            sb.append(STR."\{exit}) QUIT\{NL}: ");
            System.out.print(sb);
            int choice = new Scanner(System.in).nextInt();
            System.out.println(" ");
            if (choice == exit) {
                break;
            }
            else if (choice == toggleFailed)
                retryFailed = !retryFailed;
            else {
                BrowserHistory history = historyList.get(choice);
                for (Link link : history.getLinkList()) {
                    if (DB.addLink(link).equals(DUPLICATE_ENTRY)) {
                        System.out.println(STR."Link already downloaded: \{link.getLink()}");
                    }
                    else {
                        link.download(retryFailed);
                    }
                }
                while (executor.getActiveCount() > 0) {
                    sleep(200);
                }
                if (DownloadResults.haveFailures()) {
                    System.out.println("Failures:");
                    for (Link link : DownloadResults.getFailedList()) {
                        System.out.println(STR."\t\{link.getLink()}");
                    }
                }
            }
            System.out.println("Hit Enter To Continue");
            new Scanner(System.in).nextLine();
        }
    }

    public static void showLinks(State state) {
        LinkedList<Link> list = DB.getLinks(state);
        if (list.isEmpty()) {
            showNothing();
        }
        else {
            for (Link link : list) {
                System.out.println(link);
            }
        }
    }

    public static void reset() throws SQLException, IOException {
        String message = """

                ** THIS WILL COMPLETELY RESET THE DATABASE AND ALL SETTINGS TO FACTORY DEFAULTS! **

                This includes deleting all link history,  current download list and failed downloads.

                ARE YOU SURE THIS IS WHAT YOU WANT TO DO (Y/N)?:\s""";
        String choice = "";
        while (choice.isEmpty()) {
            System.out.print(message);
            choice = getChoice().toLowerCase();
        }
        if (choice.contains("y")) {
            SQLite.wipeDatabase();
            AppSettings.Clear.clearAll();
            System.out.println(STR."\{NL}  IT IS DONE!");
        }
        else {
            System.out.println(STR."\{NL}  NOTHING WAS CHANGED");
        }
    }

    private static String getChoice() {
        return new Scanner(System.in).nextLine().toLowerCase();
    }

    public static void showCommands() {
        LinkedList<Link> list = DB.getLinks(State.NEW);
        if (list.isEmpty()) {
            showNothing();
        }
        else {
            for (Link link : list) {
                new DownloadLink(link).showCommand();
            }
        }
    }

    private static void sleep(long milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        }
        catch (InterruptedException ignored) {
        }
    }

    private static void showNothing() {
        System.out.println(STR."\{NL}  Nothing to see here\{NL}");
    }

    public static void initDownloadFolder() {
        String home = Paths.get(System.getProperty("user.home"), "Downloads").toString();
        AppSettings.Set.downloadFolder(home);
        String response = "";
        while (!response.equalsIgnoreCase("Y") && !response.equalsIgnoreCase("N")) {
            System.out.print(STR."\{NL}Download folder will be set to\{NL}\t\{home}\{NL}Accept? (Y/N): ");
            response = new Scanner(System.in).nextLine();
        }
        if (response.equalsIgnoreCase("N")) {
            setDownloadFolder();
        }
        else {
            System.out.println(STR."\{NL}\tDefault path accepted, change it by running `iget setFolder`\{NL + NL}");
        }
        setBrowser();
    }

    public static void setDownloadFolder() {
        String prompt = STR."Current: \{AppSettings.Get.downloadFolder()}\{NL}New: ";
        System.out.print(prompt);
        String folderString = new Scanner(System.in).nextLine();
        File folder = Paths.get(folderString).toFile();
        if (!folder.exists()) {
            System.out.print(STR."\{NL + NL}This folder does not exist. Shall I create it? (Y/N): ");
            String response = new Scanner(System.in).nextLine();
            if (response.equalsIgnoreCase("Y")) {
                if (folder.mkdirs()) {
                    AppSettings.Set.downloadFolder(folderString);
                    System.out.println(STR."\{NL + NL}Folder created successfully!");
                }
                else {
                    System.out.println(STR."\{NL + NL}Failed to create folder!");
                }
            }
            else {
                System.out.println(STR."\{NL + NL}Nothing changed");
            }
        }
        else {
            AppSettings.Set.downloadFolder(folderString);
        }
        System.out.println(STR."Download folder set to: \{AppSettings.Get.downloadFolder()}");
    }

    public static void setBrowserPath() {
        Browser browser = Browser.getBrowser();
        System.out.print(STR."\{NL}Your selected browser is:                \{browser.label()}\{NL}");
        System.out.print(STR."\{NL}The current path to the history file is: \{AppSettings.Get.browserHistoryFile()}\{NL}");
        System.out.print(STR."\{NL}Set new path (type default to reset it to default or leave blank for no change)\{NL}: ");
        String newPath = new Scanner(System.in).nextLine();
        if (newPath.equalsIgnoreCase("default")) {
            AppSettings.Clear.browserHistoryFile();
            File file = new File(AppSettings.Get.browserHistoryFile());
            if (file.exists()) {
                System.out.println(STR."\{NL}Default Path Set");
            }
            else {
                System.out.println(STR."\{NL}Default Path Set, however, I could not locate the History file at path\{NL}\t\{file.getAbsolutePath()}");
            }
        }
        else if (!newPath.isEmpty()) {
            File file = new File(newPath);
            if (file.exists() && file.isFile()) {
                AppSettings.Set.browserHistoryFile(newPath);
            }
            else {
                System.out.println(STR."\{NL}The path you provided does not exist or it is not pointing to a file");
            }
        }
        System.out.println(STR."\{NL}Browser history path is: \{AppSettings.Get.browserHistoryFile()}");
    }

    public static void showVersion() {
        Properties prop = new Properties();
        try (InputStream input = Actions.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                System.out.println("Could not determine current version");
            }
            else {
                prop.load(input);
                System.out.println(prop.getProperty("version"));
            }
        }
        catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    public static void setBrowser() {
        List<String> list = Browser.getLabels();
        int idx = 0;
        System.out.println("Which browser do you use:");
        for (String browser : list) {
            System.out.print(STR."\{idx}) \{browser}\{NL}");
            idx++;
        }
        System.out.print(STR."\{NL} If your browser is not listed, just chose Chrome: ");
        int option = new Scanner(System.in).nextInt();
        while (option < 0 || option >= list.size()) {
            System.out.print(STR."\{NL}Invalid choice - If your browser is not listed, just chose Chrome: ");
            option = new Scanner(System.in).nextInt();
        }
        String browserString = list.get(option);
        Browser browser = Browser.valueOf(browserString.toUpperCase());
        AppSettings.Set.browser(browser);

        if (browser.equals(Browser.FIREFOX)) {
            String filename = "places.sqlite";
            String msg = STR."""
                        Firefox does not keep its browser History file in a static location, where instead it
                        stores it in a path that contains the name of the profile for which it is assigned to.

                        The file named '\{filename}' needs to be located and the path to that file needs to
                        be set using
                       \s
                            iget setBrowserPath
                       \s
                        You can do this manually, or I can search for the file now.
                       \s
                        Would you like me to search your user home folder for the file?
                       \s
                        (Y/N):\s""";
            System.out.print(msg);
            String response = new Scanner(System.in).nextLine();
            while (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("n")) {
                System.out.print(STR."\{NL}(Y/N): ");
                response = new Scanner(System.in).nextLine();
            }
            if (response.equalsIgnoreCase("y")) {
                Path root = Paths.get(System.getProperty("user.home"));
                Search search = new Search(root, filename);
                search.run();
                LinkedList<Path> pathList = search.getFinalList();
                if (pathList.size() > 1) {
                    System.out.print(STR."\{NL}It seems there was more than one history file found.\{NL}Please chose which one to use:\{NL}");
                    idx = 0;
                    for (Path path : pathList) {
                        System.out.print(STR."\{idx}) \{path.toAbsolutePath().toString()}\{NL}");
                        idx++;
                    }
                    System.out.print(": ");
                    int choice = new Scanner(System.in).nextInt();
                    while (choice < 0 || choice >= pathList.size()) {
                        System.out.print(STR."Invalid option\{NL}: ");
                        choice = new Scanner(System.in).nextInt();
                    }
                    Path chosenPath = pathList.get(choice);
                    AppSettings.Set.browserHistoryFile(chosenPath.toAbsolutePath().toString());
                    System.out.print(STR."\{NL}Path set: \{chosenPath.toAbsolutePath().toString()}\{NL}");
                }
                else if (!pathList.isEmpty()) {
                    String fullPath = pathList.getFirst().toAbsolutePath().toString();
                    AppSettings.Set.browserHistoryFile(fullPath);
                    System.out.print(STR."\{NL}File found at: \{fullPath}\{NL}Browser path was set successfully!\{NL}");
                }
                else {
                    msg = STR."""
                            It seems that the Firefox history file could not be located in your user folder.

                            You can locate the file yourself using this commend:

                                find / -name '\{filename}'

                            This might take some time since it will be searching your entire hard drive for the file.
                            The command will return the full path to the file. Copy the entire String to your clipboard
                            then run:

                                iget setBrowserPath

                            And when prompted, paste the path and press enter.

                            """;
                    System.out.println(msg);
                }
            }
            else {
                msg = STR."""

                        You can locate the file yourself using this commend:

                            find ~/ -name '\{filename}'

                        That command will return the full path to the file. Copy the entire String to your clipboard
                        then run:

                            iget setBrowserPath

                        And when prompted, paste the path and press enter.

                        """;
                System.out.println(msg);
            }
        }
        else {
            File file = new File(AppSettings.Get.browserHistoryFile());
            if (file.exists()) {
                System.out.println(STR."Browser set to: \{browserString}");
            }
            else {
                System.out.println(STR."Browser set to: \{browserString}, however, I could not locate this browsers History file");
            }
        }

        if (browser.equals(Browser.CHROME) && OS.getOS().equals(OS.WIN)) {
            showWindowsChromeInfo();
        }

    }

    private static void showWindowsChromeInfo() {
        String msg = """
                                \s
                 Using Chrome on Windows requires a slight change to the shortcut that you use to launch Chrome
                 so that the cookie database can be used to authenticate you to Instagram while the browser is
                 open.
                                \s
                 Here is what you need to do:
                                \s
                 - Locate the shortcut that you use to launch Chrome.
                 - Right click on the shortcut and select Properties at the bottom.
                 - In the `Target` field, place the cursor after the last quotation mark at the end of the text
                 - Hit the space bar then paste this text into the field:
                                \s
                     --disable-features=LockProfileCookieDatabase
                                \s
                 Click OK
                                \s
                 Use that shortcut to launch Chrome and you should not have any problems.
                \s""";
        System.out.println(msg);
    }
}
