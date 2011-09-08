package com.androidsx.microrss.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

import com.androidsx.microrss.cache.CacheImageManager;
import com.androidsx.microrss.cache.ThumbnailUtil;
import com.androidsx.microrss.configure.UpdateTaskStatus;
import com.androidsx.microrss.db.FeedColumns;
import com.androidsx.microrss.db.MicroRssContentProvider;
import com.androidsx.microrss.db.RssItemsDao;
import com.androidsx.microrss.db.SqLiteRssItemsDao;
import com.androidsx.microrss.domain.DefaultItemList;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.ItemList;
import com.androidsx.microrss.view.AnyRSSHelper;

/**
 * Helper class to handle querying a webservice for forecast details and parsing
 * results into {@code com.androidsx.anyrss.db.AnyRssAbstractContentProvider}.
 */
public class WebserviceHelper {
    private static final String TAG = "WebserviceHelper";

    private static final String USER_AGENT_TEMPLATE = "Mozilla/5.0 (X11; U; Linux i686; es-ES; rv:1.9.1.8) Gecko/20100202 Firefox/3.5.8";
    
    private static final String[] PROJECTION_APPFEED = {
        FeedColumns.FEED_URL, 
        FeedColumns.LAST_UPDATE
    };

    private static final int COL_RSS_URL = 0;
    private static final int COL_RSS_LAST_UPDATE = 1;
    
    /**
     * Timeout to wait for webservice to connect. Because we're in the
     * background, we don't mind waiting for good data.
     */
    private static final int WEBSERVICE_TIMEOUT_CONNECTION = (int) (30 * DateUtils.SECOND_IN_MILLIS);
    
    /**
     * Timeout to wait for webservice to respond. Because we're in the
     * background, we don't mind waiting for good data.
     */
    private static final int WEBSERVICE_TIMEOUT_SO = (int) (30 * DateUtils.SECOND_IN_MILLIS);
    
    /**
     * User-agent string to use when making requests. Should be filled using
     * {@link #prepareUserAgent(Context)} before making any other calls.
     */
    private static String sUserAgent = null;

    /**
     * Prepare the internal User-Agent string for use. This requires a
     * {@link Context} to pull the package name and version number for this
     * application.
     */
    private static void prepareUserAgent(Context context) {
        if (sUserAgent == null) {
            try {
                // Read package name and version number from manifest
                PackageManager manager = context.getPackageManager();
                PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                sUserAgent = String.format(USER_AGENT_TEMPLATE,
                        info.packageName, info.versionName);
                
            } catch(NameNotFoundException e) {
                Log.e(TAG, "Couldn't find package information in PackageManager", e);
                //FlurryAgent.onError(FlurryConstants.ERROR_ID_PACKAGE_MANAGER, "Package manager is corrupt", e.getClass().toString());
            }
        } else {
            // It is done already
        }
    }
    
    /**
     * Open a request to the given URL, returning a {@link InputStream} across the
     * response from that API.
     */
    static InputStream queryApi(String url) throws IOException {
        if (sUserAgent == null) {
            // This should never happen in production 
            throw new RuntimeException("Must prepare user agent string");
        }
        
        // Append the protocol if missing (otherwise it will fail later)
        try {
          new URL(url);
        } catch (MalformedURLException e) {
          url = "http://" + url;
        }
        
        InputStream stream = null;
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", sUserAgent);
        
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 
          WEBSERVICE_TIMEOUT_CONNECTION);
        HttpConnectionParams.setSoTimeout(httpParams, 
          WEBSERVICE_TIMEOUT_SO);
        
        HttpClient client = new DefaultHttpClient(httpParams);

        HttpResponse response;
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            stream = entity.getContent();
        } catch (ClientProtocolException e) {
            Log.w(TAG, "Network exception. Do we have connection to the internet?");
        }
        
        return stream;
    }

    /**
     * Perform a webservice query to retrieve the items for this feed from the
     * corresponding webserver, and store a selection of them in the database.
     * This call blocks until the request is finished and the feeds have been
     * updated.
     * <p>
     * TODO: Move to a non-static method
     *
     * @param context the activity context. TODO: This violates Demeter's law
     * @param feedId the feed ID
     * @param maxItemsToRetrieve number of items to retrieve from the feed
     * @param maxItemsToStore number of items to store in the DB
     */
    public static void updateForecastsAndFeeds(Context context,
                    int feedId,
                    int maxItemsToRetrieve,
                    int maxItemsToStore)
            throws FeedProcessingException {
        Log.d(TAG, "Start to update feeds");
        prepareUserAgent(context);
        final ContentResolver resolver = context.getContentResolver();
        final Cursor feedCursor = extractFeedInfo(feedId, resolver);
        final String rssUrl = feedCursor.getString(COL_RSS_URL);
        final long lastFeedUpdate = feedCursor.getLong(COL_RSS_LAST_UPDATE);
        feedCursor.close();
        
        Log.v(TAG, "Ask the RSS source to retrieve the items from " + rssUrl);

        final ContentValues values = new ContentValues();
            
        insertNewItemsIntoDb(context, resolver, feedId, rssUrl, maxItemsToStore, lastFeedUpdate, new SqLiteRssItemsDao());
        
        final int numberOfItemsInTheDB = new SqLiteRssItemsDao().getItemList(resolver, feedId).getNumberOfItems();
        final int itemsToDelete = Math.max(0, numberOfItemsInTheDB - maxItemsToStore);
        int deletedItems = new SqLiteRssItemsDao().deleteOldestItems(resolver, feedId, itemsToDelete, new CacheImageManager(context));
        if (itemsToDelete == deletedItems) {
            // OK
        } else {
            Log.e(TAG, "Tried to delete the oldest " + itemsToDelete + ", but "
                    + deletedItems + " were deleted instead");
        }
        
        // Mark feed cache as being updated
        values.clear();
        
        ContentValues values2 = new ContentValues();
        long lastUpdate = System.currentTimeMillis();
        // This Uri has the FEED_ID, so we only update ONE feed
        Uri feedUriWithId = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI,
                        feedId);
        values2.put(FeedColumns.LAST_UPDATE, lastUpdate);
        int updateRows = resolver.update(feedUriWithId, values2, null, null);
        if (updateRows != 1) {
            Log.w(TAG, "Updated [" + updateRows + " != " + "1] rows for LAST_UPDATED and CURRENT_ITEM_POSITION");
        }
    }
    
    /**
     * Perform a query to retrieve the favicon for this feed from the
     * corresponding domain, and store the image in the cache.
     * This call blocks until the request is finished.
     * <p>
     * TODO: Move to a non-static method
     *
     * @param context the activity context. TODO: This violates Demeter's law
     * @param feedId the feed ID
     */
    public static void retrieveFaviconFromFeed(Context context, int feedId) {
        Log.d(TAG, "Start to retrieve the favicon for [" + feedId + "]");
        try {
            final ContentResolver resolver = context.getContentResolver();
            final Cursor feedCursor = extractFeedInfo(feedId, resolver);
            final String rssUrl = feedCursor.getString(COL_RSS_URL);
            feedCursor.close();
    
            prepareUserAgent(context);
            
            CacheImageManager.Options options = new CacheImageManager.Options();
            options.targetSize = ThumbnailUtil.TARGET_SIZE_FAVICON_THUMBNAIL;
            options.compressFormat = CacheImageManager.CompressFormatImage.PNG;
    
            CacheImageManager cacheManager = new CacheImageManager(context);
            boolean result = cacheManager.downloadAndSaveInCache(AnyRSSHelper.retrieveFaviconUrl(rssUrl), options);
            Log.d(TAG, "Result of retrieval of favicon for feed [" + feedId + "]: " + result);
        } catch (Exception e) {
            // do not go up in the stack, this is not a killing error
            Log.w(TAG, "Error while retrieving favicon for feed [" + feedId + "]: " + e.getMessage());
        }
    }
    
    /**
     * Perform a webservice query to retrieve the items for this feed from the
     * corresponding webserver.
     * <p>
     * This call blocks until the request is finished
     * <p>
     * TODO: Move to a non-static method
     *
     * @param context the activity context. TODO: This violates Demeter's law
     * @param rssUrl Url to retrieve the list of items
     * @param maxNumberOfItems number of items to retrieve from the feed
     * 
     * @return the list of items as a {@link ItemList}
     */
    public static ItemList getRssItems(Context context, String rssUrl, String rssName, int maxNumberOfItems)
            throws FeedProcessingException {
        Log.d(TAG, "Ask the RSS source to retrieve the items from " + rssUrl);
   
        prepareUserAgent(context);
        
        List<Item> newRssItems = new DefaultRssSource(new CacheImageManager(context)).getRssItems(rssUrl, maxNumberOfItems, -1);
        DefaultItemList itemList = new DefaultItemList();
        itemList.setTitle(rssName);
        for (int i = 0; i < newRssItems.size(); i++) {
          itemList.addItem(newRssItems.get(i));
        }
        
        Log.d(TAG, "Retrieved " + newRssItems.size() + " items from " + rssUrl);
        return itemList;
    }
    
    private static void insertNewItemsIntoDb(Context context, ContentResolver resolver, int feedId, String rssUrl, int maxNumberOfItems, long lastFeedUpdate, RssItemsDao dao) throws FeedProcessingException {
        final List<Item> newRssItems = new DefaultRssSource(new CacheImageManager(context)).getRssItems(rssUrl, maxNumberOfItems, lastFeedUpdate);
        final ItemList oldRssItemsList = dao.getItemList(resolver, feedId);
        
        final DefaultItemList itemsToInsert = new DefaultItemList();
        final DuplicateDetector duplicateDetector = new HashItemBasedDuplicateDetector();
        for (int i = 0; i < newRssItems.size(); i++) {
            final Item feedItem = newRssItems.get(i);
            if (duplicateDetector.isDuplicated(feedItem, oldRssItemsList)) {
                // Don't insert it
                Log.v(TAG, "The item " + feedItem + " is duplicated");
            } else {
                itemsToInsert.addItem(feedItem);
            }
        }
        new SqLiteRssItemsDao().insertItems(resolver, feedId, itemsToInsert);
        Log.i(TAG, "Just inserted " + itemsToInsert.getNumberOfItems() + " new items into the DB");
    }
    
    /**
     * The client should close the cursor after using it.
     */
    private static Cursor extractFeedInfo(int feedId,
            ContentResolver resolver) throws FeedProcessingException {
        Cursor cursor = null;
        Uri uri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId); 
        
        cursor = resolver.query(uri, PROJECTION_APPFEED, null,
                null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor;
        } else {
            Log.e(TAG, "Fatal error: RSS URL not found");
            throw new FeedProcessingException(
                    "Can't find the URL for this feed. Please re-add the feed",
                    UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
        }
    }
}
