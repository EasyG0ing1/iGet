package com.simtechdata.actions;

import com.simtechdata.database.DB;
import com.simtechdata.database.Link;
import com.simtechdata.enums.Folder;
import com.simtechdata.process.ProcBuilder;
import com.simtechdata.process.ProcResult;
import com.simtechdata.settings.AppSettings;

import java.util.concurrent.Callable;

import static com.simtechdata.enums.OS.NL;

public class DownloadLink implements Callable<Result> {

    private static final String command = Folder.getYtDLPFile().getAbsolutePath();
    private static final String downloadPath = AppSettings.Get.downloadFolder();
    private final Link link;

    public DownloadLink(Link link) {
        this.link = link;
    }

    private ProcBuilder getPB(String link) {
        String[] args = {"--quiet", "--cookies-from-browser", AppSettings.Get.browser(), "-P", downloadPath, link, "-f", "bestvideo*+bestaudio/best", "-f", "mp4"};
        return new ProcBuilder(command).ignoreExitStatus().withNoTimeout().withArgs(args);
    }

    public void showCommand() {
        System.out.println(STR."\{getPB(formatLink(link.getLink())).getCommandLine()}\{NL}");
    }

    private String formatLink(String link) {
        if (link.toLowerCase().contains("instagram")) {
            link = link.replace("instagram.com/reels", "instagram.com/reel");
            String[] items = link.replaceFirst("//", "/").split("/");
            StringBuilder sb = new StringBuilder(items[0]).append("/");
            for (int x = 1; x < items.length; x++) {
                sb.append("/").append(items[x]);
            }
            if (!sb.toString().contains("?utm_source=ig_embed"))
                sb.append("/").append("?utm_source=ig_embed");
            return sb.toString();
        }
        return link;
    }

    @Override
    public Result call() {
        String link = formatLink(this.link.getLink());
        System.out.println(STR."Downloading: \{link}");
        ProcBuilder procBuilder = getPB(link);
        ProcResult pr = procBuilder.run();
        boolean success = pr.getExitValue() == 0;
        if (!success) {
            System.out.println(pr.getErrorString());
        }
        Result result = new Result(this.link.getLink(), downloadPath, success);
        DB.setDownloadResult(result);
        return result;
    }
}
