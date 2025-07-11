package com.konsilix.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.springframework.web.multipart.MultipartFile;

import org.apache.tika.Tika;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.nio.charset.Charset;
import java.util.Random;

public class Utils {
    public static long fetchPid() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }

    public static void writePidToLocalFile(String fileName, final long pid) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(String.format("%d", pid));
        writer.close();
    }

    public static String generateRandomString(int length) {
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String randomString = "";
        for (int i = 0; i < length; i++) {
            randomString += characters.charAt(random.nextInt(characters.length()));
        }
        return randomString;
    }

    public static boolean checkAndCreateDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);

            // Java NIO (Recommended)
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                return true; // Directory created
            } else {
                return false; // Directory already exists
            }

            // Alternatively, using File class (Java IO)
            // File directory = new File(directoryPath);
            // if (!directory.exists()) {
            //     directory.mkdirs(); // Creates all parent directories as needed
            //     return true; // Directory created
            // } else {
            //     return false; // Directory already exists
            // }
        } catch (IOException e) {
            // Handle exceptions gracefully (log, rethrow, etc.)
            // For example:
            // log.error("Error creating directory: {}", e.getMessage());
            return false; // Indicate failure
        }
    }

    public static boolean isTextFile(File file) {
        if (!file.exists() || file.isDirectory()) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            int charCode;
            while ((charCode = fis.read()) != -1) {
                // Check if the character is a control character (other than whitespace)
                if (Character.isISOControl(charCode) && !Character.isWhitespace(charCode)) {
                    return false;
                }
            }
        } catch (IOException e) {
            // Handle the exception appropriately (e.g., log it or rethrow)
            // TODO: log the exception
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean isTextFile(FileInputStream file) {
//        if (!file.exists() || file.isDirectory()) {
//            return false;
//        }

        try (FileInputStream fis = file) {
            int charCode;
            while ((charCode = fis.read()) != -1) {
                // Check if the character is a control character (other than whitespace)
                if (Character.isISOControl(charCode) && !Character.isWhitespace(charCode)) {
                    return false;
                }
            }
        } catch (IOException e) {
            // Handle the exception appropriately (e.g., log it or rethrow)
            // TODO: log the exception
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean isTextFile(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream());
        return mimeType.startsWith("text/");
    }

    // see https://www.baeldung.com/how-to-use-resttemplate-with-basic-authentication-in-spring#manual_auth
//    public static HttpHeaders createHeaders(String username, String password){
//        return new HttpHeaders() {{
//            String auth = username + ":" + password;
//            byte[] encodedAuth = Base64.encodeBase64(
//                    auth.getBytes(Charset.forName("US-ASCII")) );;
//            String authHeader = "Basic " + new String( encodedAuth );
//            set( "Authorization", authHeader );
//        }};
//    }
}