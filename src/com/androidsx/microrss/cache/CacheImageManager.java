package com.androidsx.microrss.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Manages a cache for images dowloaded from the network. 
 */
public class CacheImageManager {

    private static final String TAG = "CacheImageManager";

    private static final int THUMB_WIDTH_PX = 250;

    /** If we the size is bigger or smaller than the desired size, we allow this number of pixels */
    private static final int THUMB_MARGIN_ERROR_PX = 30;

    private Context context;
    
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
     * Downloads the image from the url given, then it decodes, scale and compress the image
     * before saving it in the external cache.
     * 
     * The url will be the unique identifier of the cache image (if any), we can use {@link #getFilenameForUrl} to 
     * get the filename.
     * 
     * @return true if there was success saving to cache the image (or a hit in the cache), and false if not 
     */
    public boolean downloadAndSaveInCache(String url) {
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

            Log.v(TAG, "Generated thubnail for url: " + url);
            FileOutputStream cacheOutputStream = null;
            try {
                cacheOutputStream = new FileOutputStream(cacheFile);
                copyStream(imageInputStream, cacheOutputStream);
                cacheOutputStream.close();
                imageInputStream.close();

                Bitmap imageBitmap = decodeFile(cacheFile);
                Bitmap scaledBitmap = resizeBitmap(imageBitmap);

                FileOutputStream out = new FileOutputStream(cacheFile);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
                out.close();
                
                // advice the garbage collector?
                scaledBitmap = null;
                imageBitmap = null;
                
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Error compressing the image: " + url);
                e.printStackTrace();
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

    /**
     * Decodes image and scales it to reduce memory consumption
     * 
     * @param file
     * @return
     */
    private static Bitmap decodeFile(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();

            // We are trying to reduce the size in memory, but most likely it will always
            // bigger than the desired width/heigh
            options.inSampleSize = getSampleSize(new FileInputStream(file), THUMB_WIDTH_PX
                    - THUMB_MARGIN_ERROR_PX, THUMB_WIDTH_PX - THUMB_MARGIN_ERROR_PX);
            return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap resizeBitmap(Bitmap imageBitmap) {
        Bitmap scaledBitmap = imageBitmap;
        if (imageBitmap.getWidth() > (THUMB_WIDTH_PX + THUMB_MARGIN_ERROR_PX)
                && imageBitmap.getHeight() > (THUMB_WIDTH_PX + THUMB_MARGIN_ERROR_PX)) {
            boolean scaleByWidth = imageBitmap.getWidth() >= imageBitmap.getHeight();
            int widthInc = (scaleByWidth) ? imageBitmap.getWidth()
                    / imageBitmap.getHeight() : 1;
                    int heightInc = (scaleByWidth) ? 1 : imageBitmap.getHeight()
                            / imageBitmap.getWidth();
                    
                    scaledBitmap = Bitmap.createScaledBitmap(imageBitmap,
                            THUMB_WIDTH_PX * widthInc, THUMB_WIDTH_PX * heightInc, true);
                    Log.v(TAG, "Escaling bitmap of " + imageBitmap.getWidth() + "*"
                            + imageBitmap.getHeight() + "to " + THUMB_WIDTH_PX * widthInc + "*"
                            + THUMB_WIDTH_PX * heightInc);
        }
        return scaledBitmap;
    }

    /**
     * Get a good match for the sample size of the image (power of two).
     * <p>
     * The sample size is the number of pixels in either dimension that correspond to a single pixel
     * in the decoded bitmap. For example, inSampleSize == 4 returns an image that is 1/4 the
     * width/height of the original
     * 
     * @param is stream with the image
     * @param sizeToStartResizing largest size allowed for the picture before it will do any
     *            resizing
     * @param estimatedTargetSize it will try to resample the image to this size
     * @throws FileNotFoundException
     */
    private static int getSampleSize(InputStream is, final int sizeToStartResizing,
            final int estimatedTargetSize) throws FileNotFoundException {
        // The image won't be loaded into memory. But the outheight and
        // outwidth properties of BitmapFactory.Options will contain
        // the actual size params of the image specified
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, bounds);

        boolean scaleByHeight = Math.abs(bounds.outHeight - estimatedTargetSize) >= Math
                .abs(bounds.outWidth - estimatedTargetSize);

        int sampleSize = 1;

        // 200*200 is the largest size allowed for the picture before it will do any resizing
        if (bounds.outHeight * bounds.outWidth >= sizeToStartResizing * sizeToStartResizing) {
            // Load, scaling to smallest power of 2 if dimensions >= desired dimensions
            sampleSize = scaleByHeight ? bounds.outHeight / estimatedTargetSize : bounds.outWidth
                    / estimatedTargetSize;
            sampleSize = (int) Math.pow(2d, Math.floor(Math.log(sampleSize) / Math.log(2d)));
        }

        return sampleSize;
    }
    
    /** NOTE: We should close the stream outside this method */
    private static InputStream downloadBitmap(String url) {
        final HttpClient client = new DefaultHttpClient();
        final HttpGet getRequest = new HttpGet(url);

        try {
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
            getRequest.abort();
            Log.w(TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
            getRequest.abort();
            Log.w(TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
            getRequest.abort();
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
}