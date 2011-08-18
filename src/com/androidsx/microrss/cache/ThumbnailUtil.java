/*
 * Copyright (C) 2009 The Android Open Source Project
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
 */

package com.androidsx.microrss.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

/**
 * Thumbnail generation routines
 * 
 * TODO: not to be density-dependent
 */
public class ThumbnailUtil {
    private static final String TAG = "ThumbnailUtils";

    /* Options used internally. */
    private static final int OPTIONS_NONE = 0x0;
    private static final int OPTIONS_SCALE_UP = 0x1;

    /**
     * Constant used to indicate we should recycle the input in
     * {@link #extractThumbnail(Bitmap, int, int, int)} unless the output is the input.
     */
    public static final int OPTIONS_RECYCLE_INPUT = 0x2;

    /**
     * Constant used to indicate the dimension of mini thumbnail, used by the story thumbs
     * 
     * TODO: Should be 240 in hdpi devices, in testing phase. WIMM device is 160 dip - 1px 1dip
     */
    public static final int TARGET_SIZE_MINI_THUMBNAIL = 160; 

    /**
     * Constant used to indicate the dimension of a favicon
     * 
     * TODO: Should be 24 in hdpi devices, in testing phase. WIMM device is 160 dip - 1px 1dip
     */
    public static final int TARGET_SIZE_FAVICON_THUMBNAIL = 16;

    public static final int MIN_SOURCE_SIZE_TO_BE_PROCESSED_MINI_THUMBNAIL = 75;

    /**
     * Decodes image and scales it in memory to reduce memory consumption
     * 
     * @param file the descriptor where the image is saved
     * @param desiredTargetSize the target size to try to reduce
     * @return the file or null if there is any error
     * @throws InvalidImageSizeException the width or height is less than {@link minTargetSize}
     * @throws FileNotFoundException
     */
    public static Bitmap decodeFile(File file, int desiredTargetSize)
            throws InvalidImageSizeException, FileNotFoundException {
        return decodeFile(file, desiredTargetSize, 0);
    }

    /**
     * Decodes image and scales it in memory to reduce memory consumption
     * 
     * @param file the descriptor where the image is saved
     * @param desiredTargetSize the target size to try to reduce
     * @param minTargetSize the minimum size of the image
     * @return the file or null if there is any error
     * @throws InvalidImageSizeException the width or height is less than {@link minTargetSize}
     * @throws FileNotFoundException
     */
    public static Bitmap decodeFile(File file, int desiredTargetSize, int minTargetSize)
            throws InvalidImageSizeException, FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();

        // We are trying to reduce the size in memory, but most likely it will always
        // bigger than the desired width/height
        options.inSampleSize = getSampleSize(new FileInputStream(file), desiredTargetSize,
                desiredTargetSize, minTargetSize);
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
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
     * @param minTargetSize minimum target size to deal with
     * @return the sample size
     * @throws FileNotFoundException
     * @throws InvalidImageSizeException the width or height is less than {@link minTargetSize}
     */
    private static int getSampleSize(InputStream is, final int sizeToStartResizing,
            final int estimatedTargetSize, int minTargetSize) throws FileNotFoundException,
            InvalidImageSizeException {
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
        } else if (bounds.outHeight < minTargetSize || bounds.outWidth < minTargetSize) {
            throw new InvalidImageSizeException("The image size is too small", bounds.outWidth,
                    bounds.outHeight);
        }

        return sampleSize;
    }

    /**
     * Creates a centered bitmap of the desired size.
     * 
     * @param source original bitmap source
     * @param width targeted width
     * @param height targeted height
     */
    public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
        return extractThumbnail(source, width, height, OPTIONS_NONE);
    }

    /**
     * Creates a centered bitmap of the desired size.
     * 
     * @param source original bitmap source
     * @param width targeted width
     * @param height targeted height
     * @param options options used during thumbnail extraction
     */
    public static Bitmap extractThumbnail(Bitmap source, int width, int height, int options) {
        if (source == null) {
            return null;
        }

        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = width / (float) source.getWidth();
        } else {
            scale = height / (float) source.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap thumbnail = transform(matrix, source, width, height, OPTIONS_SCALE_UP | options);
        return thumbnail;
    }

    /**
     * Transform source Bitmap to targeted width and height.
     */
    private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth,
            int targetHeight, int options) {
        boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
        boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;

        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
             * In this case the bitmap is smaller, at least in one dimension, than the target.
             * Transform it by placing as much of the image as possible into the target and leaving
             * the top/bottom or left/right (or both) black.
             */
            Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b2);
            
            Log.v(TAG, "[!SCALE_UP] Escaling bitmap of " + source.getWidth() + "*" + source.getHeight() + "to " + targetWidth
                    + "*" + targetHeight);

            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
                    + Math.min(targetWidth, source.getWidth()), deltaYHalf
                    + Math.min(targetHeight, source.getHeight()));
            int dstX = (targetWidth - src.width()) / 2;
            int dstY = (targetHeight - src.height()) / 2;
            Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
            c.drawBitmap(source, src, dst, null);
            if (recycle) {
                source.recycle();
            }
            return b2;
        }
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();

        Log.v(TAG, "Escaling bitmap of " + bitmapWidthF + "*" + bitmapHeightF + "to " + targetWidth
                + "*" + targetHeight);

        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect = (float) targetWidth / targetHeight;

        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        }

        Bitmap b1;
        if (scaler != null) {
            // this is used for minithumb and crop, so we want to filter here.
            b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler,
                    true);
        } else {
            b1 = source;
        }

        if (recycle && b1 != source) {
            source.recycle();
        }

        int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        int dy1 = Math.max(0, b1.getHeight() - targetHeight);

        Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);

        if (b2 != b1) {
            if (recycle || b1 != source) {
                b1.recycle();
            }
        }

        return b2;
    }

    private static class InvalidImageSizeException extends Exception {
        private static final long serialVersionUID = -8561312765074847978L;

        public InvalidImageSizeException(String detailMessage, int width, int height) {
            super(detailMessage + ", size: " + width + "*" + height);
        }
    }
}