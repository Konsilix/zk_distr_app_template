package com.konsilix.services;

import com.konsilix.chatbot.models.File;

import java.util.ArrayList;
import java.util.List;

/** @author "Rob Marano" 2024-07-21 */
public final class DataStorage {

    private static List<File> fileList = new ArrayList<>();

    public static List<File> getFileListFromStorage() {
        return fileList;
    }

    public static void setFile(File file) {
        fileList.add(file);
    }

    private DataStorage() {}
}