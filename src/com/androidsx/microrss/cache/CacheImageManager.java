package com.androidsx.microrss.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.androidsx.microrss.cache.CacheImageManager.CompressFormatImage;

/**
 * Manages a cache for images downloaded from the network.
 */
public class CacheImageManager {

    private static final String TAG = "CacheImageManager";

    private Context context;

    public enum CompressFormatImage {
        PNG, JPEG
    }

    public CacheImageManager(Context context) {
        this.context = context;
    }

    /**
     * Retrieves the image file that is stored in the cache
     * 
     * @return the file where the downloaded image is located or null
     */
    public File retrieveImage(String fileName) {
        File cacheImage = FileCacheUtil.getFileFromExternalCache(context, fileName);
        return (cacheImage != null && cacheImage.exists()) ? cacheImage : null;
    }

    /**
     * Retrieves the image file that is stored in the cache
     * 
     * @return the file where the downloaded image is located or null
     */
    public void deleteImage(String fileName) {
        FileCacheUtil.deleteFileExternalCache(context, fileName);
    }

    /**
     * Retrieves the image file that is stored in the cache
     * 
     * @return the file where the downloaded image is located or null
     */
    public void cleanCache() {
        FileCacheUtil.cleanExternalCache(context);
    }

    /**
     * Downloads the image from the url given, then it decodes, scale and compress the image before
     * saving it in the external cache. The url will be the unique identifier of the cache image (if
     * any), we can use {@link #getFilenameForUrl} to get the filename.
     * 
     * @param options Options that control downsampling, scaling and compression.
     * @return true if there was success saving to cache the image (or a hit in the cache), and
     *         false if not
     */
    public boolean downloadAndSaveInCache(String url, Options options) {
        File cache = FileCacheUtil.getFileFromExternalCache(context, getFilenameForUrl(url));
        if (cache != null) {
            Log.d(TAG, "Trying to download an image that is already in cache, " + url);
            return true;
        }
        InputStream imageInputStream = downloadBitmap(url);
        if (imageInputStream != null) {
            File cacheFile = FileCacheUtil.addFileToExternalCache(context, getFilenameForUrl(url));
            if (cacheFile == null) {
                Log.w(TAG, "Error creating a file for the image: " + url);
                return false;
            }

            FileOutputStream cacheOutputStream = null;
            try {
                cacheOutputStream = new FileOutputStream(cacheFile);
                copyStream(imageInputStream, cacheOutputStream);
                cacheOutputStream.close();
                imageInputStream.close();

                Bitmap imageBitmap = ThumbnailUtil.decodeFile(cacheFile, options.targetSize,
                        options.minTargetSizeToBeProcessed);
                Bitmap resultBitmap = null;
                if (options.scaleImage) { 
                    resultBitmap = ThumbnailUtil.extractThumbnail(imageBitmap, options.targetSize, options.targetSize,
                                ThumbnailUtil.OPTIONS_RECYCLE_INPUT);
                } else {
                    resultBitmap = imageBitmap;
                }

                FileOutputStream out = new FileOutputStream(cacheFile);

                if (CompressFormatImage.PNG.equals(options.compressFormat)) {
                    resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } else {
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 42, out);
                }
                out.close();

                resultBitmap.recycle();

                // advice the garbage collector?
                resultBitmap = null;
                imageBitmap = null;

                Log.v(TAG, "Generated thumbnail for url: " + url);
                return true;
            } catch (Exception e) {
                FileCacheUtil.deleteFileExternalCache(context, getFilenameForUrl(url));

                Log.w(TAG, "Error compressing the image: " + e.getMessage()  + ", " + url);
            }
        }
        return false;
    }

    /**
     * Transforms a url into a <i>unique</i> file name.
     */
    public String getFilenameForUrl(String url) {
        return "" + url.hashCode() + ".urlimage";
    }

    /** NOTE: We should close the stream outside this method */
    private static InputStream downloadBitmap(String url) {
        HttpClient client = null;
        HttpGet getRequest = null;
        try {
            client = new DefaultHttpClient();
            getRequest = new HttpGet(url);
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w(TAG, "Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                inputStream = entity.getContent();
                // Bug on slow connections, fixed in future release.
                return new FlushedInputStream(inputStream);
            }
        } catch (IOException e) {
            if (getRequest != null) {
                getRequest.abort();
            }
            Log.w(TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
            if (getRequest != null) {
                getRequest.abort();
            }
            Log.w(TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
            if (getRequest != null) {
                getRequest.abort();
            }
            Log.w(TAG, "Error while retrieving bitmap from " + url, e);
        }
        return null;
    }

    private static int copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] stuff = new byte[1024];
        int read = 0;
        int total = 0;
        while ((read = input.read(stuff)) != -1) {
            output.write(stuff, 0, read);
            total += read;
        }
        return total;
    }

    /** An InputStream that skips the exact number of bytes provided, unless it reaches EOF. */
    private static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    public static class Options {
        
        public Options() { }

        /**
         * We will scale to this targetSize in pixels, scaling the image. Default is
         * {@link ThumbnailUtil.TARGET_SIZE_MINI_THUMBNAIL}. 
         * 
         * We will always use this value, either scaling the image or downsampling it in 
         * memory to reduce memory consumption.
         */
        public int targetSize = ThumbnailUtil.TARGET_SIZE_MINI_THUMBNAIL;

        /** Defines whether scale the image to {@link #targetSize} or not. Default is true. */
        public boolean scaleImage = true;

        /** Minimum size of the image to be processed. Default is 0 */
        public int minTargetSizeToBeProcessed = 0;

        /** Compression method use. Default is {@link CompressFormatImage.JPEG} */
        public CompressFormatImage compressFormat = CompressFormatImage.JPEG;
    }

}