package com.simtechdata.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.simtechdata.enums.OS.NL;

public class FileHash {

    public static String getFileHash(File file, Algo algo) {
        String filePath = file.getAbsolutePath();
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance(algo.getName());
            try (FileInputStream fis = new FileInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            catch (IOException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                throw new RuntimeException(e);
            }
            byte[] hashBytes = md.digest();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static String getFileHash(Path path, Algo algo) {
        return getFileHash(path.toFile(), algo);
    }

    public static boolean filesEqual(File sourceFile, File checkFile) {
        return filesEqual(sourceFile.toPath(), checkFile.toPath());
    }

    public static boolean filesEqual(Path sourcePath, Path checkPath) {
        try {
            if (filesExist(sourcePath, checkPath)) {
                String hash1 = getFileHash(sourcePath, Algo.SHA_256);
                String hash2 = getFileHash(checkPath, Algo.SHA_256);
                return hash1.equals(hash2);
            }
        }
        catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
        return false;
    }

    private static boolean filesExist(Path path1, Path path2) {
        boolean oneExists = path1.toFile().exists();
        boolean twoExists = path2.toFile().exists();
        boolean filesExist = oneExists && twoExists;
        final String msg = STR."\{NL}The file %S does not seem to exist\{NL}";

        if (!filesExist) {
            if (!oneExists) {
                System.out.printf(STR."\{msg}%n", path1);
            }
            if (!twoExists) {
                System.out.printf(STR."\{msg}%n", path2);
            }
        }
        return filesExist;
    }
}
