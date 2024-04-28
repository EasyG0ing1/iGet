package com.simtechdata.enums;

import com.simtechdata.process.ProcBuilder;
import com.simtechdata.process.ProcResult;
import com.simtechdata.settings.AppSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.simtechdata.enums.OS.NL;

public class Folder {

    private static final String FILE_MAC = "yt-dlp_macos";
    private static final String FILE_LIN = "yt-dlp_linux";
    private static final String FILE_WIN = "yt-dlp.exe";
    private static final String FILE = getFilename();
    private static final Path appFolder = OS.getAppFolder();
    private static File ytDLPFile;
    private static boolean updatingFile = false;

    private static String getFilename() {
        return switch (OS.getOS()) {
            case OS.MAC -> FILE_MAC;
            case OS.WIN -> FILE_WIN;
            default -> FILE_LIN;
        };
    }

    public static File getYtDLPFile() {
        if (ytDLPFile == null) {
            ytDLPFile = appFolder.resolve(FILE).toFile();
        }
        return ytDLPFile;
    }

    public static void copyResourceToFile() {
        AppSettings.Clear.lastUpdate();
        try (InputStream is = Folder.class.getClassLoader().getResourceAsStream(FILE)) {
            if (is == null) {
                System.out.println(STR."Could not find resource: \{FILE}");
                System.exit(1);
            }
            Files.copy(is, appFolder.resolve(FILE), StandardCopyOption.REPLACE_EXISTING);
            if (!OS.getOS().equals(OS.WIN)) {
                setExecutableFlag(appFolder.resolve(FILE));
            }
            updateDLP();
        }
        catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    private static void setExecutableFlag(Path target) {
        String command = "chmod";
        String[] args = new String[]{"+x", target.toAbsolutePath().toString()};
        ProcBuilder pb = new ProcBuilder(command).withArgs(args).withTimeoutMillis(5000).ignoreExitStatus();
        ProcResult pr = pb.run();
        if (pr.getExitValue() != 0) {
            System.out.println(STR."Could not set \{target.toAbsolutePath().toString()} executable flag, You might need to set it like this:\{NL}\tsudo chmod +x \{target.toAbsolutePath().toString()}\{NL}");
            System.exit(1);
        }
        else {
            System.out.println(STR."\{FILE} executable flag set");
        }
    }

    public static void getYTDLP() {
        if (!appFolder.toFile().exists()) {
            appFolder.toFile().mkdirs();
        }
        if (!getYtDLPFile().exists()) {
            copyResourceToFile();
            System.out.println(STR."\{FILE} copied to app folder: \{appFolder}");
        }
        updateDLP();
    }

    private static void updateDLP() {
        long lastUpdate = AppSettings.Get.lastUpdate();
        long now = System.currentTimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(now - lastUpdate);
        if (days >= 7) {
            updatingFile = true;
            updateAni();
            System.out.println(STR."Updating \{FILE}");
            String command = getYtDLPFile().getAbsolutePath();
            String argument = "-U";
            ProcResult pr = new ProcBuilder(command).withArg(argument).ignoreExitStatus().withTimeoutMillis(1000 * 120).run();
            int exitValue = pr.getExitValue();
            updatingFile = false;
            if (exitValue != 0) {
                System.out.println(STR."\{NL}Problem updating yt-dlp, update threw error \{pr.getExitValue()}");
                System.out.println(pr.getOutputString());
            }
            else {
                System.out.println(STR."\{NL}\{FILE} updated successfully");
                System.out.println(pr.getOutputString());
                AppSettings.Set.lastUpdate(now);
            }
        }
    }

    private static void updateAni() {
        new Thread(() -> {
            String ud1 = STR."Updating \{FILE} .          ";
            String ud2 = STR."Updating \{FILE} ..         ";
            String ud3 = STR."Updating \{FILE} ...        ";
            String ud4 = STR."Updating \{FILE} ....       ";
            String[] uds = new String[]{ud1, ud2, ud3, ud4};
            System.out.println(" ");
            int idx = 0;
            int max = uds.length - 1;
            boolean up = false;
            while (updatingFile) {
                System.out.print("\r");
                System.out.print(uds[idx]);
                sleep(500L);
                if (idx == max || idx == 0) {
                    up = !up;
                }
                idx = up ? idx + 1 : idx - 1;
            }
        }).start();
    }

    private static void sleep(long milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        }
        catch (InterruptedException ignored) {
        }
    }
}
