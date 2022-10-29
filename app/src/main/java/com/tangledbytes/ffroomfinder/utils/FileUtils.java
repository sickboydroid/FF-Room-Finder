package com.tangledbytes.ffroomfinder.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Deletes given directory or file recursively
     */
    public static boolean delete(File fileToDelete) {
        if (fileToDelete.isFile()) {
            if (!fileToDelete.delete()) {
                Log.v(TAG, "delete(File): Could not delete, " + fileToDelete);
                return true;
            }
            return false;
        }
        for (File file : fileToDelete.listFiles()) {
            if (file.isDirectory()) {
                if (delete(file))
                    return true;
            } else if (!file.delete()) {
                Log.d(TAG, "Could not delete file, " + file);
                return true;
            }
        }

        // Delete root folder
        if (!fileToDelete.delete()) {
            Log.d(TAG, "Could not delete dir, " + fileToDelete);
            return true;
        }
        return false;
    }

    public static boolean write(String destFile, String data) throws IOException {
        return write(new File(destFile), data);
    }

    public static boolean write(File destFile, String data) throws IOException {
        return write(destFile, data, false);
    }

    public static boolean write(String destFile, String data, boolean append) throws IOException {
        return write(new File(destFile), data, append);
    }

    public static boolean write(File destFile, String data, boolean append) throws IOException {
        FileWriter fw = new FileWriter(destFile, append);
        fw.write(data);
        fw.close();
        return true;
    }

    public static String read(String filePath) throws IOException {
        return read(new File(filePath));
    }

    public static String read(File file) throws IOException {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder data = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            data.append("\n").append(line);
        br.close();
        fr.close();

        // Remove the extra line that was added in first iteration of loop
        if (data.length() > 0)
            data = new StringBuilder(data.substring(1));
        return data.toString();
    }

}
