package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.UpdateService;
import com.androidsx.microrss.db.FeedColumns;
import com.androidsx.microrss.db.MicroRssContentProvider;
import com.androidsx.microrss.db.dao.MicroRssDao;

/**
 * Main activity: starts the service, waits for the configuration thread to do the first update, and
 * then gets the items from the DB, and passes them to the view activity.
 */
public class InitActivity extends Activity {
    public static final String TAG = "RetrieveRssItemsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate init activity");

        Log.i(TAG, "Start the update service");
        startService(new Intent(this, UpdateService.class)); // if already started, does nothing

        MicroRssDao dao = new MicroRssDao(getContentResolver());
        int[] currentIds = dao.findAllFeedIds();
        if (currentIds.length == 0) {
            // FIXME (WIMM): do in an a-sync task? or is this really necessary to build the first view when there are no items?
            Log.i("WIMM", "This is temporary: put some feeds into the DB");
            
            final String MESSAGE_ES = " (ES)";
            
            // wimm tests
            writeConfigToBackend(this, "Tech Crunch", "http://feeds.feedburner.com/Techcrunch", true);    
            writeConfigToBackend(this, "BBC Top Stories", "http://feeds.bbci.co.uk/news/rss.xml");
            writeConfigToBackend(this, "and.roid.es", "http://feeds.feedburner.com/AndroidEnEspanol", true);
            writeConfigToBackend(this, "Geek And Poke", "http://geekandpoke.typepad.com/geekandpoke/rss.xml");
            
            // news 
//            writeConfigToBackend(this, "El mundo" + MESSAGE_ES, "http://rss.elmundo.es/rss/descarga.htm?data2=4");
//            writeConfigToBackend(this, "El país" + MESSAGE_ES, "http://www.elpais.com/rss/feed.html?feedId=1022");
//            writeConfigToBackend(this, "ABC" + MESSAGE_ES, "http://www.abc.es/rss/feeds/abcPortada.xml");
            writeConfigToBackend(this, "NYTimes World", "http://www.nytimes.com/services/xml/rss/nyt/GlobalHome.xml", true);
            writeConfigToBackend(this, "BBC World", "http://news.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml", true);
            writeConfigToBackend(this, "CNN Top Stories", "http://rss.cnn.com/rss/edition.rss");
            writeConfigToBackend(this, "CNN Most recent", "http://rss.cnn.com/rss/cnn_latest.rss");
            writeConfigToBackend(this, "Wired top stories", "http://www.wired.com/news_drop/netcenter/netcenter.rdf");
            writeConfigToBackend(this, "FoxNews", "http://www.foxnews.com/xmlfeed/rss/0,4313,0,00.rss");
            writeConfigToBackend(this, "Google News", "http://news.google.com/news?ned=us&topic=h&output=rss", true);
            writeConfigToBackend(this, "Yahoo News", "http://rss.news.yahoo.com/rss/topstories");
            writeConfigToBackend(this, "Reuters", "http://feeds.reuters.com/reuters/topNews");
            
            // tech
            writeConfigToBackend(this, "Barrapunto" + MESSAGE_ES, "http://barrapunto.com/index.rss");
            writeConfigToBackend(this, "Genbeta" + MESSAGE_ES, "http://feeds.weblogssl.com/genbeta");
            writeConfigToBackend(this, "An.droid.es" + MESSAGE_ES, "http://feeds.feedburner.com/AndroidEnEspanol?format=xml");
            writeConfigToBackend(this, "ALT1040" + MESSAGE_ES, "http://feeds.hipertextual.com/alt1040?format=xml");
            writeConfigToBackend(this, "Slashdot", "http://rss.slashdot.org/slashdot/eqWf");
            writeConfigToBackend(this, "LifeHacker", "http://lifehacker.com/index.xml", true);
            writeConfigToBackend(this, "Engadget", "http://www.engadget.com/rss.xml");
            writeConfigToBackend(this, "ReadWriteWeb", "http://www.readwriteweb.com/rss.xml");
//            writeConfigToBackend(this, "TechCrunch", "http://www.techcrunch.com/feed/");
            writeConfigToBackend(this, "Gizmodo", "http://www.gizmodo.net/index.xml");
            writeConfigToBackend(this, "Joel on Software", "http://www.joelonsoftware.com/rss.xml");
            writeConfigToBackend(this, "Good Coders", "http://feeds.feedburner.com/catonmat?format=xml");
            writeConfigToBackend(this, "Android Blog", "http://feeds2.feedburner.com/androinica");
            
            // sports
            writeConfigToBackend(this, "Marca" + MESSAGE_ES, "http://rss.marca.com/rss/descarga.htm?data2=425");
            writeConfigToBackend(this, "Marca, fútbol" + MESSAGE_ES, "http://rss.marca.com/rss/descarga.htm?data2=372");
            writeConfigToBackend(this, "As.com" + MESSAGE_ES, "http://www.as.com/rss.html");
            writeConfigToBackend(this, "ESPN Top", "http://sports.espn.go.com/espn/rss/newsf");
            writeConfigToBackend(this, "ESPN NBA", "http://sports.espn.go.com/espn/rss/nba/news");
            writeConfigToBackend(this, "ESPN Soccer", "http://soccernet.espn.go.com/rss/news");
            writeConfigToBackend(this, "NYTimes Sports", "http://www.nytimes.com/services/xml/rss/nyt/Sports.xml");
            writeConfigToBackend(this, "Yahoo Sports", "http://sports.yahoo.com/top/rss.xml");
            
            // finance
            writeConfigToBackend(this, "Loogic" + MESSAGE_ES, "http://feeds.feedburner.com/Loogiccom?format=xml");
            writeConfigToBackend(this, "NYTimes Business", "http://feeds.nytimes.com/nyt/rss/Business");
            writeConfigToBackend(this, "Google News Business", "http://news.google.com/news?ned=us&topic=w&output=rss");
            writeConfigToBackend(this, "Wall Street Journal", "http://feeds2.feedburner.com/wsj/xml/rss/3_7481.xml");
            writeConfigToBackend(this, "The Economist", "http://www.economist.com/rss/daily_news_and_views_rss.xml");
            
            // entertainment
            writeConfigToBackend(this, "Meneame" + MESSAGE_ES, "http://meneame.net/rss2.php");
            writeConfigToBackend(this, "Dilbert Daily Strip", "http://feeds.feedburner.com/DilbertDailyStrip");
            writeConfigToBackend(this, "CNN entertainment", "http://rss.cnn.com/rss/edition_entertainment.rss");
            writeConfigToBackend(this, "Brain Teasers", "http://feeds.braingle.com/braingle/all");
            writeConfigToBackend(this, "Random Jokes", "http://jokes4all.net/rss/040000111/jokes.xml");
            
            // others
            writeConfigToBackend(this, "Asco De Vida" + MESSAGE_ES, "http://feeds2.feedburner.com/AscoDeVida");
            writeConfigToBackend(this, "F**k My Life", "http://feeds2.feedburner.com/fmylife");
            writeConfigToBackend(this, "Quote of the day", "http://www.quotationspage.com/data/qotd.rss");
            writeConfigToBackend(this, "Flickr abstract art", "http://api.flickr.com/services/feeds/groups_pool.gne?id=61057342@N00&lang=en-us&format=rss_200");
            writeConfigToBackend(this, "NYT Idea of the day", "http://ideas.blogs.nytimes.com/feed/");
            writeConfigToBackend(this, "LifeHacker", "http://feeds.gawker.com/lifehacker/excerpts.xml");
            writeConfigToBackend(this, "Today in history", "http://feeds2.feedburner.com/historyorb/todayinhistory");
            writeConfigToBackend(this, "How stuff works","http://feeds.howstuffworks.com/DailyStuff");
            
            // parser tests taken from the email errors of AnyRSS
//            writeConfigToBackend(this, "test 0", "http://news.baidu.com/n?cmd=7&loc=5495&name=%B9%E3%B6%AB&tn=rss");
//            writeConfigToBackend(this, "test 1", "http://feeds.feedburner.com/Destructoid");
//            writeConfigToBackend(this, "test 2", "http://www.foxnews.com/xmlfeed/rss/0,4313,0,00.rss");
//            writeConfigToBackend(this, "test 3", "http://tabnak.ir/en/rss/allnews");
//            writeConfigToBackend(this, "test 4", "http://news.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml");
//            writeConfigToBackend(this, "test 5", "http://www.wp.pl/rss.xml?id=1");
//            writeConfigToBackend(this, "test 6", "http://www.google.com/reader/public/atom/user/01578920782114465644/label/Periodicos");
//            writeConfigToBackend(this, "test 7", "http://feeds.gawker.com/lifehacker/excerpts.xml");
//            writeConfigToBackend(this, "test 8", "http://www.quotationspage.com/data/qotd.rss");
//            writeConfigToBackend(this, "test 9", "http://news.google.co.in/news?pz=1&cf=all&ned=in&hl=en&output=rss");
//            writeConfigToBackend(this, "test 10", "http://bossip.com/feed/");
//            writeConfigToBackend(this, "test 11" + MESSAGE_ES, "http://rss.elmundo.es/rss/descarga.htm?data2=4");
//            writeConfigToBackend(this, "test 12", "http://skattershooting.blogspot.com/feeds/posts/default");
//            writeConfigToBackend(this, "test 13", "http://www.nytimes.com/services/xml/rss/nyt/Sports.xml");
//            writeConfigToBackend(this, "test 14", "http://www.ctv.ca/generic/generated/freeheadlines/rdf/MontrealHome.xml");
//            writeConfigToBackend(this, "test 15", "http://feeds.feedburner.com/blabbermouth");
//            writeConfigToBackend(this, "test 16", "http://www.Motorsport-Total.com/rss_f1.xml");
//            writeConfigToBackend(this, "test 17", "http://www.gizmodo.net/index.xml");
//            writeConfigToBackend(this, "test 18", "http://rss.feedsportal.com/c/32314/f/440274/index.rss");
          
        }
        
        dispatchToViewActivities();
    }

    private static void writeConfigToBackend(Context context, String title,
            String feedUrl) {
        writeConfigToBackend(context, title, feedUrl, false);
    }
    
    private static void writeConfigToBackend(Context context, String title,
            String feedUrl, boolean active) {
        Log.i(TAG, "Save initial config to the DB");

        ContentValues values = new ContentValues();
        values.put(FeedColumns.LAST_UPDATE, -1);
        values.put(FeedColumns.TITLE, title);
        values.put(FeedColumns.FEED_URL, feedUrl);
        values.put(FeedColumns.ACTIVE, active);
        values.put(FeedColumns.G_READER, false);

        // TODO: update instead of insert if editing an existing feed
        ContentResolver resolver = context.getContentResolver();
        resolver.insert(MicroRssContentProvider.FEEDS_CONTENT_URI, values);
    }
    
    private void dispatchToViewActivities() {
        Log.i(TAG, "Dispatch to the view activities");

        Intent intent = IntentHelper
                .createIntent(this, getIntent().getExtras(), FeedActivity.class);
        startActivity(intent);
        Log.i(TAG, "End of the initialization activity");
        finish();
    }
}
