package com.androidsx.microrss.webservice;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.androidsx.microrss.cache.CacheImageManager.CompressFormatImage;
import com.androidsx.microrss.configure.UpdateTaskStatus;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.MutableItem;
import com.androidsx.microrss.view.AnyRSSHelper;

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
  
  // THUMNAIL tags (from Media RSS specification)
  public final static String MEDIA_NAMESPACE = "http://search.yahoo.com/mrss/";
  
  private static final String THUMB_TAG_1 = "thumbnail";
  
  private static final String THUMB_TAG_2 = "content";
  private static final String THUMB_TAG_2_TYPE = "type";
  private static final String THUMB_TAG_2_TYPE_SHOULD_CONTAIN = "image/"; 
  private static final String THUMB_TAG_URL = "url";

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
    public List<Item> getRssItems(String rssUrl, int maxNumberOfItems) throws FeedProcessingException {
        try {
            return getRssItemsInternal(rssUrl, maxNumberOfItems);
        } catch (FeedProcessingException e) {
            try {
                return getRssItemsInternal(rssUrl, maxNumberOfItems);
            } catch (FeedProcessingException e1) {
                try {
                    return getRssItemsInternal(rssUrl, maxNumberOfItems);
                } catch (FeedProcessingException e2) {
                    throw e2;
                }
            }
        }
    }
  
    public List<Item> getRssItemsInternal(String rssUrl, int maxNumberOfItems) throws FeedProcessingException {
        InputStream stream = null;
        List<Item> parseResponse;
        try {
            stream = WebserviceHelper.queryApi(rssUrl);
            parseResponse = parseResponse(stream, maxNumberOfItems, rssUrl);
            retrieveThumbnails(parseResponse);
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

  private static String inputStreamAsString(InputStream stream)
      throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
    StringBuilder sb = new StringBuilder();
    String line = null;

    while ((line = br.readLine()) != null) {
      sb.append(line + "\n");
    }
    try {
        br.close();
    } catch (IOException e) {
        // Never mind
    }
    return sb.toString();
  }

  /**
   * Parse a webservice RSS response into {@link Item} objects.
   */
  private List<Item> parseRSSResponse(InputStream response, int maxNumberOfItems)
      throws FeedProcessingException {
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
      xpp.setInput(response, null); // Setting null to the encoding auto-detects
                                    // encoding
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT
          && items.size() <= maxNumberOfItems) {
        if (eventType == XmlPullParser.START_TAG) {
          thisTag = xpp.getName();
          thisNamespace = xpp.getNamespace();
          if (RSS_TAG_ITEM.equals(thisTag)) {
            items.add(new MutableItem("(no content)", new Date(), "(no title)", "", ""));
            insideItem = true;
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
        } else if (eventType == XmlPullParser.TEXT && ((thisNamespace != null && !thisNamespace.equalsIgnoreCase(
                MEDIA_NAMESPACE)) || thisNamespace == null)) { // any except mediaRSS
          if (insideItem && RSS_TAG_TITLE.equals(thisTag)
              && xpp.getText().trim().length() > 0) {
            ((MutableItem) items.get(items.size() - 1)).setTitle(xpp.getText());
          } else if (insideItem && RSS_TAG_DESCRIPTION.equals(thisTag)
              && xpp.getText().trim().length() > 0) {
            ((MutableItem) items.get(items.size() - 1)).setContent(xpp
                .getText());
          } else if (insideItem && RSS_TAG_PUB_DATE.equals(thisTag)
              && xpp.getText().trim().length() > 0) {
            ((MutableItem) items.get(items.size() - 1)).setPubDate(DateParser
                .parseDateInRfc822(xpp.getText()));
          } else if (insideItem && RSS_TAG_LINK.equals(thisTag)
              && xpp.getText().trim().length() > 0) {
            ((MutableItem) items.get(items.size() - 1)).setUrl(xpp.getText());
          }
        }
        try {
          eventType = xpp.next();
        } catch(XmlPullParserException e) {
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
  private List<Item> parseATOMResponse(InputStream response,
      int maxNumberOfItems) throws FeedProcessingException {
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
      xpp.setInput(response, null); // Setting null to the encoding auto-detects
      // encoding
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT
          && items.size() <= maxNumberOfItems) {
        if (eventType == XmlPullParser.START_TAG) {
          thisTag = xpp.getName();
          thisNamespace = xpp.getNamespace();
          if (ATOM_TAG_ITEM.equals(thisTag)) {
            items.add(new MutableItem("(no content)", new Date(), "(no title)", "", ""));
            insideItem = true;
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
        } else if (eventType == XmlPullParser.TEXT && ((thisNamespace != null && !thisNamespace.equalsIgnoreCase(
                MEDIA_NAMESPACE)) || thisNamespace == null)) { // base or any namespace
          if (insideItem && ATOM_TAG_TITLE.equals(thisTag)
              && xpp.getText().trim().length() > 0) {
            ((MutableItem) items.get(items.size() - 1)).setTitle(xpp.getText());
          } else if (insideItem && ATOM_TAG_DESCRIPTION.equals(thisTag)
              && xpp.getText().trim().length() > 0) {
            ((MutableItem) items.get(items.size() - 1)).setContent(xpp
                .getText());
          } else if (insideItem && ATOM_TAG_PUB_DATE.equals(thisTag)
              && xpp.getText().trim().length() > 0) {
            ((MutableItem) items.get(items.size() - 1)).setPubDate(DateParser
                .parseDateInRfc822(xpp.getText()));
          } else if (insideItem && ATOM_TAG_LINK.equals(thisTag)
              && xpp.getText().trim().length() > 0) {
            ((MutableItem) items.get(items.size() - 1)).setUrl(xpp.getText());
          }
        }
        try {
          eventType = xpp.next();
        } catch(XmlPullParserException e) {
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
  private List<Item> parseResponse(InputStream response, int maxNumberOfItems, String rssUrl) throws FeedProcessingException {
    List<Item> items = null;
    String responseString = "";
    try {
      responseString = inputStreamAsString(response);
    } catch (IOException e1) {
      throw new FeedProcessingException(
          "Can't parse the feed, it cannot be converted from stream to string",
          UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
    }

    try {
      items =
          parseRSSResponse(new ByteArrayInputStream(responseString.getBytes()),
              maxNumberOfItems);
      if (items.size() == 0) {
        throw new FeedProcessingException(
            "Can't parse the feed, it is probably not RSS 2.0 compliant",
            UpdateTaskStatus.FEED_PROCESSING_EXCEPTION);
      }
    } catch (FeedProcessingException e) {
      Log.w(TAG, e.getMessage().toString());
      items =
          parseATOMResponse(
              new ByteArrayInputStream(responseString.getBytes()),
              maxNumberOfItems);
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
