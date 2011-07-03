package com.androidsx.microrss.webservice;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

import com.androidsx.microrss.FlurryConstants;
import com.androidsx.microrss.configure.UpdateTaskStatus;
import com.androidsx.microrss.db.FeedColumns;
import com.androidsx.microrss.db.MicroRssContentProvider;
import com.androidsx.microrss.db.RssItemsDao;
import com.androidsx.microrss.db.SqLiteRssItemsDao;
import com.androidsx.microrss.domain.DefaultItemList;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.ItemList;
import com.flurry.android.FlurryAgent;

/**
 * Helper class to handle querying a webservice for forecast details and parsing
 * results into {@code com.androidsx.anyrss.db.AnyRssAbstractContentProvider}.
 */
public class WebserviceHelper {
    private static final String TAG = "WebserviceHelper";

    private static final String USER_AGENT_TEMPLATE = "Mozilla/5.0 (X11; U; Linux i686; es-ES; rv:1.9.1.8) Gecko/20100202 Firefox/3.5.8";
    
    private static final String[] PROJECTION_APPWIDGET = {
        FeedColumns.FEED_URL
    };

    private static final int COL_RSS_URL = 0;
    
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
                FlurryAgent.onError(FlurryConstants.ERROR_ID_PACKAGE_MANAGER, "Package manager is corrupt", e.getClass().toString());
            }
        } else {
            // It is done already
        }
    }
    
    /**
     * Open a request to the given URL, returning a {@link InputStream} across the
     * response from that API.
     */
    static InputStream queryApi(String url) throws FeedProcessingException {
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

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            stream = entity.getContent();
        } catch (Exception e) {
            throw new FeedProcessingException(
                "Can't download the feed! Either the network connection or the server is down",
              e,
              UpdateTaskStatus.FEED_PROCESSING_EXCEPTION_NO_EMAIL);
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
     * @param appWidgetId the widget ID
     * @param maxItemsToRetrieve number of items to retrieve from the feed
     * @param maxItemsToStore number of items to store in the DB
     */
    public static void updateForecastsAndFeeds(Context context,
                    int appWidgetId,
                    int maxItemsToRetrieve,
                    int maxItemsToStore)
            throws FeedProcessingException {
        Log.d(TAG, "Start to update feeds");
        prepareUserAgent(context);
        final ContentResolver resolver = context.getContentResolver();
        final String rssUrl = extractRssUrl(appWidgetId, resolver);
        Log.v(TAG, "Ask the RSS source to retrieve the items from " + rssUrl);

        final ContentValues values = new ContentValues();
            
        insertNewItemsIntoDb(resolver, appWidgetId, rssUrl, maxItemsToStore, new SqLiteRssItemsDao());
        
        final int numberOfItemsInTheDB = new SqLiteRssItemsDao().getItemList(resolver, appWidgetId).getNumberOfItems();
        final int itemsToDelete = Math.max(0, numberOfItemsInTheDB - maxItemsToStore);
        int deletedItems = new SqLiteRssItemsDao().deleteOldestItems(resolver, appWidgetId, itemsToDelete);
        if (itemsToDelete == deletedItems) {
            // OK
        } else {
            Log.e(TAG, "Tried to delete the oldest " + itemsToDelete + ", but "
                    + deletedItems + " were deleted instead");
        }
        
        // Mark widget cache as being updated
        values.clear();
        
        ContentValues values2 = new ContentValues();
        long lastUpdate = System.currentTimeMillis();
        // This Uri has the WIDGET_ID, so we only update ONE widget
        Uri appWidgetUriWithId = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI,
                        appWidgetId);
        values2.put(FeedColumns.LAST_UPDATE, lastUpdate);
        int updateRows = context.getContentResolver().update(appWidgetUriWithId, values2, null, null);
        if (updateRows != 1) {
            Log.w(TAG, "Updated [" + updateRows + " != " + "1] rows for LAST_UPDATED and CURRENT_ITEM_POSITION");
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
        
        List<Item> newRssItems = new DefaultRssSource().getRssItems(rssUrl, maxNumberOfItems);
        DefaultItemList itemList = new DefaultItemList();
        itemList.setTitle(rssName);
        for (int i = 0; i < newRssItems.size(); i++) {
          itemList.addItem(newRssItems.get(i));
        }
        
        Log.d(TAG, "Retrieved " + newRssItems.size() + " items from " + rssUrl);
        return itemList;
    }
    
    private static void insertNewItemsIntoDb(ContentResolver resolver, int appWidgetId, String rssUrl, int maxNumberOfItems, RssItemsDao dao) throws FeedProcessingException {
        final List<Item> newRssItems = new DefaultRssSource().getRssItems(rssUrl, maxNumberOfItems);
        final ItemList oldRssItemsList = dao.getItemList(resolver, appWidgetId);
        
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
        new SqLiteRssItemsDao().insertItems(resolver, appWidgetId, itemsToInsert);
        Log.i(TAG, "Just inserted " + itemsToInsert.getNumberOfItems() + " new items into the DB");
    }
    
    private static String extractRssUrl(int feedId,
            ContentResolver resolver) throws FeedProcessingException {
        Cursor cursor = null;
        try {
            Uri uri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId); 
            
            cursor = resolver.query(uri, PROJECTION_APPWIDGET, null,
                    null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(COL_RSS_URL);
            } else {
                Log.e(TAG, "Fatal error: RSS URL not found");
                throw new FeedProcessingException(
                        "Can't find the URL for this feed. Please re-add the widget",
                        UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}