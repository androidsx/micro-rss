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
 * Harry Tormey <harry@catch.com>
 * Modified by Omar Pera <omar@androidsx.com>
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

    private static File getExternalStorageDir(Context context, String dir) {
        if (dir != null) {
            File extMediaDir = new File(Environment.getExternalStorageDirectory()
                    + "/Android/data/" + context.getPackageName() + dir);

            if (extMediaDir.exists()) {
                createNomediaDotFile(context, extMediaDir);
                return extMediaDir;
            }

            if (isSdCardAvailable()) {
                File sdcard = Environment.getExternalStorageDirectory();

                if (sdcard.canWrite()) {
                    extMediaDir.mkdirs();
                    createNomediaDotFile(context, extMediaDir);
                    return extMediaDir;
                } else {
                    Log.e(TAG, "SD card not writeable, unable to create directory: "
                            + extMediaDir.getPath());
                }
            } else {
                return extMediaDir;
            }
        }
        return null;
    }

    private static void createNomediaDotFile(Context context, File directory) {
        if (directory != null) {
            File nomedia = new File(directory, NOMEDIA_FILENAME);

            if (!nomedia.exists()) {
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
     * Returns whether the SD card is available.
     */
    public static boolean isSdCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getExternalCacheDir(Context context) {
        return getExternalStorageDir(context, "/cache");
    }

    public static File addFileToExternalCache(Context context, String fileName) {
        File extCacheDir = getExternalCacheDir(context);
        return addFileToCache(context, fileName, extCacheDir);
    }

    public static File addFileToCache(Context context, String fileName, File cacheDir) {
        if (cacheDir != null) {

            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File cachedFile = new File(cacheDir, fileName);

            if (!cachedFile.exists()) {
                try {
                    cachedFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Unable to create file in " + cachedFile.getPath(), e);
                }
            }
            return cachedFile;
        }
        return null;
    }

    public static File getFileFromExternalCache(Context context, String fileName) {
        File extCacheDir = getExternalCacheDir(context);
        return getFileFromCache(context, fileName, extCacheDir);
    }

    public static File getFileFromCache(Context context, String fileName, File cacheDir) {
        if (cacheDir != null) {
            File cachedFile = new File(cacheDir, fileName);
            if (cachedFile.exists()) {
                return cachedFile;
            }
        }
        return null;
    }

    public static void cleanExternalCache(Context context) {
        Log.i(TAG, "Cleaning up cache from external storage");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
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
        }
    }

    public static void deleteFileExternalCache(Context context, String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalDir = getExternalCacheDir(context);

            if (externalDir != null) {
                File toBeDeletedFile = new File(externalDir, fileName);
                if (toBeDeletedFile.exists()) {
                    toBeDeletedFile.delete();
                    Log.d(TAG, "Deleting " + toBeDeletedFile.getPath());
                }
            }
        }
    }
}