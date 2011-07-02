package com.androidsx.microrss.webservice;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.MutableItem;

class MockRssSource implements RssSource {

    private static int feedsGeneratedSoFar = 0;
    private static int itemsGeneratedSoFar = 0;
    
    @Override
    public List<Item> getRssItems(String rssUrl, int maxNumberOfItems) throws FeedProcessingException {
        return generateNewListOfItems(6);
    }
    
    private List<Item> generateNewListOfItems(int n) {
        feedsGeneratedSoFar++;
        List<Item> rssItems = new LinkedList<Item>();
        for (int i = 0; i < n; i++) {
            rssItems.add(generateNewItem());
        }
        return rssItems;
    }
    
    private Item generateNewItem() {
        itemsGeneratedSoFar++;
        final Date now = new Date();
        final String strNow = DateFormat.getDateTimeInstance(DateFormat.LONG,
                DateFormat.LONG, Locale.ENGLISH).format(now);

        MutableItem rssItem = new MutableItem();
        rssItem.setTitle("This is the item #" + itemsGeneratedSoFar + ", for the feed #" + feedsGeneratedSoFar);
        rssItem.setContent("[bla bla bla]\n"
                + " This is the item #" + itemsGeneratedSoFar + ", for the feed #" + feedsGeneratedSoFar + "\n"
                + "Generated on " + strNow + "\n"
                + "These are being generated in inverse order :(. The item #n is generated before #(n+1), "
                + "but it should appear AFTER it. This is just unfortunate, and the reason for ticket #100.");
        rssItem.setPubDate(now);
        rssItem.setUrl("bla");
        return rssItem;
    }
}
