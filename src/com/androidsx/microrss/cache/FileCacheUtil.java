package com.androidsx.microrss.cache;

/*
 * Copyright (C) 2011 Catch.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Harry Tormey <harry@catch.com> Modified by Omar Pera <omar@androidsx.com>
 */

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;

public class FileCacheUtil {
    private static final String NOMEDIA_FILENAME = ".nomedia";
    private static final long CACHE_FILE_EXPIRATION = DateUtils.DAY_IN_MILLIS * 7;
    private static final String TAG = "FileCacheUtil";

    /** We are supposed to check before this method if the sdcard is available */
    private static File getExternalStorageDir(Context context, String dir) {
        if (dir != null) {
            File extMediaDir = new File(Environment.getExternalStorageDirectory()
                    + "/Android/data/" + context.getPackageName() + dir);

            if (extMediaDir.exists()) {
                createNomediaDotFile(context, extMediaDir);
                return extMediaDir;
            }

            if (isSdCardWritable()) {
                extMediaDir.mkdirs();
                createNomediaDotFile(context, extMediaDir);
                return extMediaDir;
            } else {
                Log.e(TAG, "SD card not writeable, unable to create directory: "
                        + extMediaDir.getPath());
            }
        }
        return null;
    }

    private static void createNomediaDotFile(Context context, File directory) {
        if (directory != null && isSdCardWritable()) {
            File nomedia = new File(directory, NOMEDIA_FILENAME);

            if (nomedia != null && !nomedia.exists()) {
                try {
                    nomedia.mkdirs();
                    nomedia.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "unable to create .nomedia file in " + directory.getPath(), e);
                }
            }
        }
    }

    /**
     * Returns whether the SD card is available and writable
     */
    public static boolean isSdCardWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Returns whether the SD card is available
     */
    public static boolean isSdCardAvailable() {
        String externalStorage = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(externalStorage) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorage)) {
            return true;
        }
        return false;
    }

    public static File getExternalCacheDir(Context context) {
        return getExternalStorageDir(context, "/cache");
    }

    public static File addFileToExternalCache(Context context, String fileName) {
        if (isSdCardWritable()) {
            File extCacheDir = getExternalCacheDir(context);
            return addFileToCache(context, fileName, extCacheDir);
        } else {
            Log.w(TAG, "Sdcard not available while creating new file " + fileName);
            return null;
        }
    }

    private static File addFileToCache(Context context, String fileName, File cacheDir) {
        if (cacheDir != null) {
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File cachedFile = new File(cacheDir, fileName);
            if (cachedFile != null && !cachedFile.exists()) {
                try {
                    cachedFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w(TAG, "Unable to create file in " + cachedFile.getPath(), e);
                }
            }
            return cachedFile;
        }
        return null;
    }

    public static File getFileFromExternalCache(Context context, String fileName) {
        if (isSdCardAvailable()) {
            File extCacheDir = getExternalCacheDir(context);
            return getFileFromCache(context, fileName, extCacheDir);
        } else {
            Log.w(TAG, "Sdcard not available while retrieving " + fileName);
            return null;
        }
    }

    private static File getFileFromCache(Context context, String fileName, File cacheDir) {
        if (cacheDir != null) {
            File cachedFile = new File(cacheDir, fileName);
            if (cachedFile != null && cachedFile.exists()) {
                return cachedFile;
            }
        }
        return null;
    }

    public static void cleanExternalCache(Context context) {
        if (isSdCardWritable()) {
            Log.i(TAG, "Cleaning up cache from external storage");
            File externalDir = getExternalCacheDir(context);

            if (externalDir != null) {
                File[] externalFiles = externalDir.listFiles();

                if (externalFiles != null && externalFiles.length > 0) {
                    for (File file : externalFiles) {
                        if (System.currentTimeMillis() - file.lastModified() >= CACHE_FILE_EXPIRATION
                                && !NOMEDIA_FILENAME.equals(file.getName())) {
                            Log.d(TAG, "Deleting " + file.getPath());
                            file.delete();
                        }
                    }
                }
            }
        } else {
            Log.w(TAG, "Sdcard not writable while cleaning cache ");
        }
    }

    public static void deleteFileExternalCache(Context context, String fileName) {
        if (isSdCardWritable()) {
            File externalDir = getExternalCacheDir(context);

            if (externalDir != null) {
                File toBeDeletedFile = new File(externalDir, fileName);
                if (toBeDeletedFile.exists()) {
                    toBeDeletedFile.delete();
                    Log.d(TAG, "Deleting " + toBeDeletedFile.getPath());
                }
            }
        } else {
            Log.w(TAG, "Sdcard not writable while deleting " + fileName);
        }
    }
}