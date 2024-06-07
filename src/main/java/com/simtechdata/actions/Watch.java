package com.simtechdata.actions;

import com.simtechdata.database.Link;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Watch {

    private static final List<String> downloadedLinks = new ArrayList<>();
    private static boolean graal = false;

    public static void startGraal() {
        graal = true;
        start();
    }

    public static void start() {

        if (GraphicsEnvironment.isHeadless()) {
            String msg = """
                                        \s
                     \tYou can only use the watch option when your terminal has been opened
                     \tfrom within a GUI environment. Currently you are running iget in a
                     \t"headless" environment. Please open a Terminal window from within your
                     \tGUI, then run iGet with the watch option.
                                        \s
                    \s""";
            System.out.println(msg);
            return;
        }

        System.out.println("Watching Clipboard - Ctrl+C to exit");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        clearClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
        while (true) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable contents = clipboard.getContents(null);
                if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
                    if (isIGLink(text)) {
                        clearClipboard(clipboard);
                        if (!downloadedLinks.contains(text)) {
                            downloadedLinks.add(text);
                            new Thread(() -> {
                                Link link = null;
                                try {
                                    link = new Link(text, System.currentTimeMillis());
                                    DownloadLink downloadLink = new DownloadLink(link);
                                    Future<Result> future = executor.submit(downloadLink);
                                    Result result = future.get();
                                    if (result.isSuccess()) {
                                        System.out.println(STR."Success: \{link.getLink()}");
                                    }
                                    else {
                                        System.out.println(STR."Failed: \{link.getLink()}");
                                    }
                                }
                                catch (ExecutionException | InterruptedException ignored) {
                                    System.out.println(STR."Failed: \{link.getLink()}");
                                }
                                if (graal)
                                    System.exit(0);
                            }).start();
                        }
                    }
                }
            }
            catch (IOException | UnsupportedFlavorException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
            sleep(1);
        }
    }

    private static void clearClipboard(Clipboard clipboard) {
        StringSelection stringSelection = new StringSelection("");
        clipboard.setContents(stringSelection, null);
    }

    private static boolean isIGLink(String text) {
        boolean hasHttp = text.toLowerCase().startsWith("http");
        boolean hasInstagram = text.toLowerCase().contains("instagram");
        boolean hasYoutube = text.toLowerCase().contains("youtube");
        boolean hasPartYoutube = text.toLowerCase().contains("youtu.be");
        return hasHttp && (hasInstagram || hasYoutube || hasPartYoutube);
    }

    private static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        }
        catch (InterruptedException ignored) {
        }
    }

}
