package com.androidsx.microrss.webservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.androidsx.microrss.cache.CacheImageManager;
import com.androidsx.microrss.cache.ThumbnailUtil;
import com.androidsx.microrss.configure.UpdateTaskStatus;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.MutableItem;

class DefaultRssSource implements RssSource {

  private static final String TAG = "DefaultRssSource";

  // RSS tags
  private static final String RSS_TAG_ITEM = "item";
  private static final String RSS_TAG_TITLE = "title";
  private static final String RSS_TAG_DESCRIPTION = "description";
  private static final String RSS_TAG_PUB_DATE = "pubDate";
  private static final String RSS_TAG_LINK = "link";

  // ATOM tags
  private static final String ATOM_TAG_ITEM = "entry";
  private static final String ATOM_TAG_TITLE = "title";
  private static final String ATOM_TAG_DESCRIPTION = "content";
  private static final String ATOM_TAG_PUB_DATE = "published";
  private static final String ATOM_TAG_LINK = "id";
  private static final String ATOM_TAG_SUMMARY = "summary";
  
  private static final String ATOM_RSS_TAG_LAST_BUILD_DATE = "lastBuildDate";
  
  // THUMBNAIL tags (from Media RSS specification)
  public final static String MEDIA_NAMESPACE = "http://search.yahoo.com/mrss/";
  
  private static final String THUMB_TAG_1 = "thumbnail";
  
  private static final String THUMB_TAG_2 = "content";
  private static final String THUMB_TAG_2_TYPE = "type";
  private static final String THUMB_TAG_2_TYPE_SHOULD_CONTAIN = "image/"; 
  private static final String THUMB_TAG_URL = "url";

  private static final int DEFAULT_ID_WHEN_EMPTY = -1;
  private static final String DEFAULT_DESCRIPTION_WHEN_EMPTY = "(no content)";
  private static final String DEFAULT_TITLE_WHEN_EMPTY = "(no title)";

  private static XmlPullParserFactory sFactory = null;

  private CacheImageManager cacheImageManager;
  
  private static final Pattern srcAttributePattern = Pattern.compile("src\\s*=\\s*([\\\"'])?([^ \\\"']*)");
  
  public DefaultRssSource(CacheImageManager cacheImageManager) {
    this.cacheImageManager = cacheImageManager;
  }
  

  /**
   * We try three times. Ugly as hell.
   */
    @Override
    public List<Item> getRssItems(String rssUrl, int maxNumberOfItems, long lastFeedUpdate) throws FeedProcessingException {
        try {
            return getRssItemsInternal(rssUrl, maxNumberOfItems, lastFeedUpdate);
        } catch (FeedProcessingException e) {
            try {
                return getRssItemsInternal(rssUrl, maxNumberOfItems, lastFeedUpdate);
            } catch (FeedProcessingException e1) {
                try {
                    return getRssItemsInternal(rssUrl, maxNumberOfItems, lastFeedUpdate);
                } catch (FeedProcessingException e2) {
                    throw e2;
                }
            }
        }
    }
  
    public List<Item> getRssItemsInternal(String rssUrl, int maxNumberOfItems, long lastFeedUpdate) throws FeedProcessingException {
        InputStream stream = null;
        List<Item> parseResponse;
        try {
            stream = WebserviceHelper.queryApi(rssUrl);
            parseResponse = parseResponse(stream, maxNumberOfItems, rssUrl, lastFeedUpdate);
            retrieveThumbnails(parseResponse);
        } catch (FeedProcessingException e) {
            throw e; // FIXME: Does this still happen at all?
        } catch (Exception e) {
            Log.e(TAG, "Error while grabbing and processing the feed: " + rssUrl, e);
            //parseResponse = new ArrayList<Item>();
            // why the line above? it gives an error and we continue like nothing:
            //   it updates the last update date every time there is an error, making
            //   imposible to retrieve any item if the first update was unsuccesful
            //   and there is no new items since then
            throw new FeedProcessingException("Error while grabbing and processing the feed: " + rssUrl, e, UpdateTaskStatus.DONT_KNOW);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // Ssshh
            }
        }
        return parseResponse;
    }

    private static ByteArrayOutputStream inputStreamAsByteArray(InputStream stream)
            throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = stream.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }
        buffer.flush();
        
        return buffer;
    }

    /**
     * Parse a webservice RSS response into {@link Item} objects.
     */
    private List<Item> parseRSSResponse(InputStream response, int maxNumberOfItems,
            long lastFeedUpdate) throws FeedProcessingException {
        List<Item> items = new LinkedList<Item>();

        try {
            if (sFactory == null) {
                sFactory = XmlPullParserFactory.newInstance();
            }
            sFactory.setNamespaceAware(true);

            XmlPullParser xpp = sFactory.newPullParser();
            boolean insideItem = false;
            String thisNamespace = null;
            String thisTag = null;
            int thisStoryDepth = 0; // for feeds with duplicated tags in sublevels
            xpp.setInput(response, null); // Setting null to the encoding auto-detects
            // encoding
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT && items.size() <= maxNumberOfItems) {
                if (eventType == XmlPullParser.START_TAG) {
                    thisTag = xpp.getName();
                    thisNamespace = xpp.getNamespace();
                    if (RSS_TAG_ITEM.equals(thisTag)) {
                        items.add(new MutableItem(DEFAULT_ID_WHEN_EMPTY, DEFAULT_DESCRIPTION_WHEN_EMPTY, new Date(),
                                DEFAULT_TITLE_WHEN_EMPTY, "", ""));
                        insideItem = true;
                        thisStoryDepth = xpp.getDepth();
                    }

                    if (insideItem) {
                        String thumbnail = parseThumbnailUrl(xpp, thisTag);
                        if (thumbnail != null) {
                            ((MutableItem) items.get(items.size() - 1)).setThumbnail(thumbnail);
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (RSS_TAG_ITEM.equals(thisTag)) {
                        thisTag = null;
                        insideItem = false;
                        thisNamespace = null;
                    }
                } else if (eventType == XmlPullParser.TEXT
                        && ((thisNamespace != null && !thisNamespace
                                .equalsIgnoreCase(MEDIA_NAMESPACE)) || thisNamespace == null)) {
                    // base or any namespace && on the same depth as the story (for feeds with
                    // duplicated tags in sublevels)
                    if (xpp.getDepth() == thisStoryDepth + 1) {
                        if (insideItem && RSS_TAG_TITLE.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            ((MutableItem) items.get(items.size() - 1)).setTitle(xpp.getText());
                        } else if (insideItem && RSS_TAG_DESCRIPTION.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            ((MutableItem) items.get(items.size() - 1)).setContent(xpp.getText());
                        } else if (insideItem && RSS_TAG_PUB_DATE.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            ((MutableItem) items.get(items.size() - 1)).setPubDate(DateParser
                                    .parseDateInRfc822(xpp.getText()));
                        } else if (insideItem && RSS_TAG_LINK.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            ((MutableItem) items.get(items.size() - 1)).setUrl(xpp.getText());
                        }
                    } else {
                        if (ATOM_RSS_TAG_LAST_BUILD_DATE.equals(thisTag) && xpp.getText().trim().length() > 0
                                && lastFeedUpdate > 0) {
                            if (lastFeedUpdate > DateParser.parseDateInRfc822(xpp.getText())
                                    .getTime()) {
                                Log.i(TAG, "Stop processing de feed, it has not been updated since last retrieval");
                                Log.i(TAG, "Last update feed: " + DateParser.parseDateInRfc822(xpp.getText()));
                                return items;
                            } else {
                                Log.i(TAG, "Process the feed, it has been updated since last retrieval");
                            }
                        }
                    }
                }
                try {
                    eventType = xpp.next();
                } catch (XmlPullParserException e) {
                    Log.w(TAG, "RSS error while parsing: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new FeedProcessingException(
                    "Can't parse the feed, got an IO error while accessing the memory", e,
                    UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
        } catch (XmlPullParserException e) {
            throw new FeedProcessingException(
                    "Can't parse the feed, it is probably not RSS 2.0 compliant", e,
                    UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
        }

        return items;
    }

    /**
     * Parse a webservice XML response into {@link Item} objects.
     */
    private List<Item> parseATOMResponse(InputStream response, int maxNumberOfItems,
            long lastFeedUpdate) throws FeedProcessingException {
        List<Item> items = new LinkedList<Item>();

        try {
            if (sFactory == null) {
                sFactory = XmlPullParserFactory.newInstance();
            }
            sFactory.setNamespaceAware(true);

            XmlPullParser xpp = sFactory.newPullParser();
            boolean insideItem = false;
            String thisNamespace = null;
            String thisTag = null;
            int thisStoryDepth = 0; // for feeds with duplicated tags in sublevels
            xpp.setInput(response, null); // Setting null to the encoding auto-detects
            // encoding
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT && items.size() <= maxNumberOfItems) {
                if (eventType == XmlPullParser.START_TAG) {
                    thisTag = xpp.getName();
                    thisNamespace = xpp.getNamespace();
                    if (ATOM_TAG_ITEM.equals(thisTag)) {
                        items.add(new MutableItem(DEFAULT_ID_WHEN_EMPTY, DEFAULT_DESCRIPTION_WHEN_EMPTY, new Date(),
                                DEFAULT_TITLE_WHEN_EMPTY, "", ""));
                        insideItem = true;
                        thisStoryDepth = xpp.getDepth();
                    }
                    if (insideItem) {
                        String thumbnail = parseThumbnailUrl(xpp, thisTag);
                        if (thumbnail != null) {
                            ((MutableItem) items.get(items.size() - 1)).setThumbnail(thumbnail);
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (ATOM_TAG_ITEM.equals(thisTag)) {
                        thisTag = null;
                        insideItem = false;
                        thisNamespace = null;
                    }
                } else if (eventType == XmlPullParser.TEXT
                        && ((thisNamespace != null && !thisNamespace
                                .equalsIgnoreCase(MEDIA_NAMESPACE)) || thisNamespace == null)) {
                    // base or any namespace && on the same depth as the story (for feeds with
                    // duplicated tags in sublevels)
                    if (xpp.getDepth() == thisStoryDepth + 1) {
                        if (insideItem && ATOM_TAG_TITLE.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            ((MutableItem) items.get(items.size() - 1)).setTitle(xpp.getText());
                        } else if (insideItem && ATOM_TAG_DESCRIPTION.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            ((MutableItem) items.get(items.size() - 1)).setContent(xpp.getText());
                        } else if (insideItem && ATOM_TAG_PUB_DATE.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            ((MutableItem) items.get(items.size() - 1)).setPubDate(DateParser
                                    .parseDateInRfc822(xpp.getText()));
                        } else if (insideItem && ATOM_TAG_LINK.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            ((MutableItem) items.get(items.size() - 1)).setUrl(xpp.getText());
                        } else if (insideItem && ATOM_TAG_SUMMARY.equals(thisTag)
                                && xpp.getText().trim().length() > 0) {
                            MutableItem item = ((MutableItem) items.get(items.size() - 1));
                            if (item.getContent().equals(DEFAULT_DESCRIPTION_WHEN_EMPTY)) {
                                item.setContent(xpp.getText());
                            }
                        }
                    } else {
                        if (ATOM_RSS_TAG_LAST_BUILD_DATE.equals(thisTag) && xpp.getText().trim().length() > 0
                                && lastFeedUpdate > 0) {
                            if (lastFeedUpdate > DateParser.parseDateInRfc822(xpp.getText())
                                    .getTime()) {
                                Log.i(TAG, "Stop processing de feed, it has not been updated since last retrieval");
                                return items;
                            } else {
                                Log.i(TAG, "Process the feed, it has been updated since last retrieval");
                            }
                        }
                    }
                }
                try {
                    eventType = xpp.next();
                } catch (XmlPullParserException e) {
                    Log.w(TAG, "ATOM error while parsing: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new FeedProcessingException(
                    "Can't parse the feed, got an IO error while accessing the memory", e,
                    UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
        } catch (XmlPullParserException e) {
            throw new FeedProcessingException(
                    "Can't parse the feed, it is probably not ATOM compliant", e,
                    UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
        }

        return items;
    }
  
  
  private String parseThumbnailUrl(XmlPullParser xpp, String thisTag) {
      if (THUMB_TAG_1.equals(thisTag) && xpp.getNamespace() != null
              && xpp.getNamespace().equalsIgnoreCase(MEDIA_NAMESPACE)) {
          int numAttrs = xpp.getAttributeCount();
          for (int i = 0; i < numAttrs; i++) {
              if (xpp.getAttributeName(i).equals(THUMB_TAG_URL)) {
                  return xpp.getAttributeValue(i).trim();
              }
          }
      }

      if (THUMB_TAG_2.equals(thisTag) && xpp.getNamespace() != null
              && xpp.getNamespace().equalsIgnoreCase(MEDIA_NAMESPACE)) {
          String thumbUrl = null;
          boolean isImage = false;
          int numAttrs = xpp.getAttributeCount();
          for (int i = 0; i < numAttrs; i++) {
              if (xpp.getAttributeName(i).equals(THUMB_TAG_2_TYPE)) {
                  if (xpp.getAttributeValue(i).contains(THUMB_TAG_2_TYPE_SHOULD_CONTAIN)) {
                      isImage = true;
                  }
              }
              if (xpp.getAttributeName(i).equals(THUMB_TAG_URL)) {
                  thumbUrl = xpp.getAttributeValue(i).trim();
              }
          }
          if (isImage == true && !thumbUrl.equals("")) {
              return thumbUrl;
          }
      }
      return null;
  }

  /**
   * Parse a webservice XML response into {@link Item} objects.
   */
  private List<Item> parseResponse(InputStream response, int maxNumberOfItems, String rssUrl, long lastFeedUpdate) throws FeedProcessingException {
    List<Item> items = null;
    ByteArrayOutputStream responseCopy = new ByteArrayOutputStream();
    try {
        responseCopy = inputStreamAsByteArray(response);
    } catch (IOException e1) {
      throw new FeedProcessingException(
          "Can't parse the feed, it cannot be converted from stream to byte array output stream",
          UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
    }

    try {
      items =
          parseRSSResponse(new ByteArrayInputStream(responseCopy.toByteArray()),
              maxNumberOfItems, lastFeedUpdate);
      if (items.size() == 0) {
        throw new FeedProcessingException(
            "Can't parse the feed, it is probably not RSS 2.0 compliant",
            UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
      }
    } catch (FeedProcessingException e) {
      Log.w(TAG, e.getMessage().toString());
      items =
          parseATOMResponse(
              new ByteArrayInputStream(responseCopy.toByteArray()),
              maxNumberOfItems, lastFeedUpdate);
    }

    // Remove the last one, because it is empty
    if (items.size() == maxNumberOfItems + 1) {
      items.remove(items.size() - 1);
    }

    // Let's make sure we don't have any null values
    for (Item item : items) {
      if (item.getTitle() == null) {
        ((MutableItem) item).setTitle("(Untitled)");
      }
      if (item.getContent() == null) {
        ((MutableItem) item).setContent("");
      }
      if (item.getPubDate() == null) {
        ((MutableItem) item).setPubDate(new Date());
      }
      if (item.getURL() == null) {
        ((MutableItem) item).setUrl(rssUrl);
      }
      if (item.getThumbnail() == null || item.getThumbnail().equals("")) {
          String firstImageURL = retrieveFirstImageURL(item.getContent());
          if (firstImageURL != null && !firstImageURL.equals("")) { 
              Log.v(TAG, "We have retrieved a thumbnail: " + firstImageURL + " for item: " + item.getTitle());
              ((MutableItem) item).setThumbnail(firstImageURL);
          } else {
              ((MutableItem) item).setThumbnail("");
          }
      }
    }
    Log.i(TAG, items.size() + " items have been downloaded and parsed");
    return items;
  }
  
    private void retrieveThumbnails(final List<Item> items) {
        int downloadedThumbs = 0;
        for (Item item : items) {
            String thumbnail = item.getThumbnail();
            if (thumbnail != "") {
                CacheImageManager.Options options = new CacheImageManager.Options();
                options.minTargetSizeToBeProcessed = ThumbnailUtil.MIN_SOURCE_SIZE_TO_BE_PROCESSED_MINI_THUMBNAIL;
                downloadedThumbs += (cacheImageManager.downloadAndSaveInCache(thumbnail, options)) ? 1 : 0;
            }
        }
        Log.i(TAG, downloadedThumbs + " thumbnails out of " + items.size() + " items has been downloaded or hitted in the cache");
    }

    /**
     * Retrives the first {@code <img>} tag, and get the attribute {@code src} with 
     * the URL
     * 
     * @param content usually a String with HTML tags
     * @return the URL of the image or null
     */
    private static String retrieveFirstImageURL(String content) {
        Pattern srcAttributePattern = Pattern.compile("src\\s*=\\s*([\\\"'])?([^ \\\"']*)");
        Matcher m = srcAttributePattern.matcher(content);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }
    
}
