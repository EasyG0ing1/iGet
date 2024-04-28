package com.simtechdata.settings;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Search {

    public static boolean stopWalk = false;
    private final Path rootPath;
    private final String searchString;
    private FolderWalker folderWalker;

    public Search(Path rootPath, String searchString) {
        this.rootPath = rootPath;
        this.searchString = searchString;
    }

    public LinkedList<Path> getFinalList() {
        return folderWalker.pathList;
    }

    public void run() {
        try {
            folderWalker = new FolderWalker(searchString);
            Files.walkFileTree(rootPath, folderWalker);
            folderWalker.stop();
        }
        catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public static class FolderWalker extends SimpleFileVisitor<Path> {

        private final String PS = FileSystems.getDefault().getSeparator();
        private final String searchString;
        private final Timer timer = new Timer();
        LinkedList<Path> pathList = new LinkedList<>();
        private String currentFolder = "no folder";
        private int lastLength = 10;
        private String firefoxParent = "";

        public FolderWalker(String searchString) {
            this.searchString = searchString;
            timer.scheduleAtFixedRate(timerTask(), 250, 100);
        }

        public void stop() {
            timer.cancel();
        }

        private TimerTask timerTask() {
            return new TimerTask() {
                @Override
                public void run() {
                    String line = currentFolder;
                    System.out.print("\r");
                    System.out.print(" ".repeat(lastLength));
                    System.out.print("\r");
                    System.out.print(line);
                    lastLength = line.length();
                }
            };
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (stopWalk) return FileVisitResult.TERMINATE;
            if (attrs.isDirectory()) {
                currentFolder = dir.toString();
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (stopWalk) return FileVisitResult.TERMINATE;
            if (attrs.isRegularFile()) {
                String filePathString = file.toAbsolutePath().toString();

                if (!firefoxParent.isEmpty()) {
                    if (!filePathString.contains(firefoxParent)) {
                        stopWalk = true;
                    }
                }

                String filename = FilenameUtils.getName(filePathString);
                if (filename.equalsIgnoreCase(searchString)) {
                    if (firefoxParent.isEmpty()) {
                        String[] parts = filePathString.split(PS);
                        int len = parts.length;
                        int max = len - 3;
                        while (max <= 0) {
                            max += 1;
                        }
                        Path parent = FileSystems.getDefault().getRootDirectories().iterator().next();
                        for (int x = 0; x <= max; x++) {
                            parent = parent.resolve(parts[x]);
                        }
                        firefoxParent = parent.toString();
                    }
                    pathList.addLast(file);
                    System.out.println(STR."\{file.toString()}");
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (stopWalk) return FileVisitResult.TERMINATE;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) {
            if (stopWalk) return FileVisitResult.TERMINATE;
            return FileVisitResult.CONTINUE;
        }
    }
}
