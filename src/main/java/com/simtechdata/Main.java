package com.simtechdata;

import com.simtechdata.actions.Actions;
import com.simtechdata.actions.Watch;
import com.simtechdata.database.Graal;
import com.simtechdata.database.Link;
import com.simtechdata.database.SQLite;
import com.simtechdata.database.TimeUtil;
import com.simtechdata.enums.Folder;
import com.simtechdata.enums.State;
import com.simtechdata.settings.AppSettings;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import static com.simtechdata.enums.OS.NL;

public class Main {


    private static final boolean isNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    public static void main(String[] args) throws SQLException, IOException {

        if (args.length == 0) {
            showHelp();
            System.exit(0);
        }

        if (AppSettings.Get.downloadFolder().equalsIgnoreCase("Start")) {
            Actions.initDownloadFolder();
        }

        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
                //do nothing
            }
        }));


        System.setProperty("prism.order", "sw");

        Folder.getYTDLP();


        for (String a : args) {
            String arg = a.toLowerCase();
            switch (arg) {
                case "watch" -> Watch.start();
                case "remove" -> Actions.remove();
                case "checkbrowser" -> SQLite.checkBrowser();
                case "setbrowserpath" -> Actions.setBrowserPath();
                case "setbrowser" -> Actions.setBrowser();
                case "get" -> {
                    if (args.length > 1) {
                        Actions.downloadLink(args[1]);
                    }
                    else
                        Actions.downloadLinks();
                }
                case "setfolder" -> Actions.setDownloadFolder();
                case "addbrowserhistory" -> {
                    if (args.length > 1) {
                        String hoursString = args[1];
                        if (!hoursString.matches("\\d+")) {
                            System.out.println("Must specify how much history you wish to add in hours");
                        }
                        else {
                            int hours = Integer.parseInt(hoursString);
                            Actions.addBrowserHistory(hours, false);
                        }
                    }
                }
                case "browserhistory" -> Actions.showHistory();
                case "list", "show" -> Actions.showLinks(State.NEW);
                case "?", "--help" -> showHelp();
                case "cmds" -> Actions.showCommands();
                case "history" -> {
                    System.out.println(STR."\{NL}Showing Downloaded Links:\{NL}");
                    Actions.showLinks(State.DOWNLOADED);
                }
                case "clear" -> {
                    System.out.println(STR."\{NL}Clearing history:\{NL}");
                    Actions.deleteHistory();
                    System.out.println(STR."  Done!\{NL}");
                }
                case "failed" -> {
                    System.out.println(STR."\{NL}Showing Failed Downloads:\{NL}");
                    Actions.showLinks(State.FAILED);
                }
                case "reset" -> Actions.reset();
                case "graal" -> {
                    if (!isNativeImage) {
                        new Graal().run();
                    }
                }
                case "--version", "version" -> Actions.showVersion();
                case "th" -> {
                    AppSettings.Set.ignoreHistory();
                    System.out.println(STR."Ignore History: \{AppSettings.Get.ignoreHistory()}");
                }
            }
        }

        if (args[0].toLowerCase().startsWith("http")) {
            long webKitTime = TimeUtil.toWebKitTime(System.currentTimeMillis());
            boolean override = args.length > 1 || AppSettings.Get.ignoreHistory();
            Actions.addLink(new Link(args[0], webKitTime), override);
        }
        System.exit(0);
    }

    private static void showHelp() {
        String help = STR."""
                Add URLs to the list simply by typing:

                    iget http://www.someserver.com/some/link     - Add a link to the que
                    iget get http://www.someserver.com/some/link - Download link immediately

                    (http must begin the link to be recognized as a link)

                    Options:
                      get        - Download links in que
                      get <url>  - download one URL right now
                      watch      - Watch mode looks for links to show up in clipboard then downloads them
                      setFolder  - Set the folder where downloads get stored
                      setBrowser - Set which browser you use for Instagram (this helps prevent download failures)
                      list       - show links in que ready to download
                      cmds       - Show download commands as they will be executed in the get option
                      history    - Show links that have been downloaded
                      reset      - Reset database and all settings to "factory defaults"
                      clear      - Delete download history
                      remove     - Remove one link from the download list (A menu will be provided)
                      failed     - Show links that failed to download
                      version    - Show iGet version number
                      browserHistory - show ALL of the links that are in your browsers history
                      checkBrowser   - Lets you verify that iGet can find the History file for your web browser
                      setBrowserPath - Lets you set the path to the browser History file if the above option does not find it.
                      addBrowserHistory hours - add the history from your browser into the que for the previous number of hours
                      th (\{AppSettings.Get.ignoreHistory()})\{AppSettings.Get.ignoreHistory() ? "  " : " "}- Toggle Ignore History - can add links that have been downloaded if true

                      Download folder: \{AppSettings.Get.downloadFolder()}

                      When downloading from Instagram, downloads can fail if the video you're downloading cannot be accessed
                      without your user credentials. Use the setBrowser option to help the program know which browser you use
                      to log into Instagram and make sure you are actively logged in with that browser. The program will then
                      use the cookie from your browser to access the video file.

                """;
        System.out.println(help);
    }

}
