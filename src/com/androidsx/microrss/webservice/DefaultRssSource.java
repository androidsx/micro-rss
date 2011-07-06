package com.androidsx.microrss.webservice;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

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

  private static XmlPullParserFactory sFactory = null;

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

      XmlPullParser xpp = sFactory.newPullParser();
      boolean insideItem = false;
      String thisTag = null;
      xpp.setInput(response, null); // Setting null to the encoding auto-detects
                                    // encoding
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT
          && items.size() <= maxNumberOfItems) {
        if (eventType == XmlPullParser.START_TAG) {
          thisTag = xpp.getName();
          if (RSS_TAG_ITEM.equals(thisTag)) {
            items.add(new MutableItem("(no content)", new Date(), "(no title)", ""));
            insideItem = true;
          }
        } else if (eventType == XmlPullParser.END_TAG) {
          if (RSS_TAG_ITEM.equals(thisTag)) {
            thisTag = null;
            insideItem = false;
          }
        } else if (eventType == XmlPullParser.TEXT) {
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

      XmlPullParser xpp = sFactory.newPullParser();
      boolean insideItem = false;
      String thisTag = null;
      xpp.setInput(response, null); // Setting null to the encoding auto-detects
      // encoding
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT
          && items.size() <= maxNumberOfItems) {
        if (eventType == XmlPullParser.START_TAG) {
          thisTag = xpp.getName();
          if (ATOM_TAG_ITEM.equals(thisTag)) {
            items.add(new MutableItem("(no content)", new Date(), "(no title)", ""));
            insideItem = true;
          }
        } else if (eventType == XmlPullParser.END_TAG) {
          if (ATOM_TAG_ITEM.equals(thisTag)) {
            thisTag = null;
            insideItem = false;
          }
        } else if (eventType == XmlPullParser.TEXT) {
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

  /**
   * Parse a webservice XML response into {@link Item} objects.
   */
  private List<Item> parseResponse(InputStream response, int maxNumberOfItems,
      String rssUrl) throws FeedProcessingException {
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
    }

    Log.i(TAG, items.size() + " items have been downloaded and parsed");

    return items;
  }

}
