package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.WIMMCompatibleHelper;
import com.androidsx.microrss.db.dao.MicroRssDao;

/**
 * Init activity: if the DB is empty, it populates it with the initial list of feeds. It then
 * requests a synchronization of the data, and starts the feed view activity.
 */
public class InitActivity extends Activity {
    public static final String TAG = "InitActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MicroRssDao dao = new MicroRssDao(getContentResolver());
        if (dao.findAllFeedIds().length == 0) {
            Log.i(TAG, "Insert the initial set of feeds into the DB");
            insertInitialFeeds(dao);
            WIMMCompatibleHelper.requestSync(this);
        }
        
        Intent intent = IntentHelper
                .createIntent(this, getIntent().getExtras(), FeedActivity.class);
        startActivity(intent);
        finish();
    }

    /** 
     * TODO: use a persistFeeds that takes them all in one chunk
     */
    private void insertInitialFeeds(MicroRssDao dao) {
        final String MESSAGE_ES = " (ES)";
        
        // TODO: use a persistFeeds that takes them all in one chunk
        // wimm tests
        dao.persistFeed(this, "Technology", "Tech Crunch", "http://feeds.feedburner.com/Techcrunch", true, false);    
        dao.persistFeed(this, "News", "BBC Top Stories", "http://feeds.bbci.co.uk/news/rss.xml", false, false);
        dao.persistFeed(this, "Technology", "and.roid.es", "http://feeds.feedburner.com/AndroidEnEspanol", true, false);
        dao.persistFeed(this, "News", "港澳台新闻-新浪新闻", "http://rss.sina.com.cn/news/china/hktaiwan15.xml", true, false);
        dao.persistFeed(this, "Entertainment", "Geek And Poke", "http://geekandpoke.typepad.com/geekandpoke/rss.xml", false, false);
        
        // news 
//            dao.persistFeed(this, "El mundo" + MESSAGE_ES, "http://rss.elmundo.es/rss/descarga.htm?data2=4", false);
//            dao.persistFeed(this, "El país" + MESSAGE_ES, "http://www.elpais.com/rss/feed.html?feedId=1022", false);
//            dao.persistFeed(this, "ABC" + MESSAGE_ES, "http://www.abc.es/rss/feeds/abcPortada.xml", false);
        dao.persistFeed(this, "News", "NYTimes World", "http://www.nytimes.com/services/xml/rss/nyt/GlobalHome.xml", true, false);
        dao.persistFeed(this, "News", "BBC World", "http://news.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml", true, false);
        dao.persistFeed(this, "News", "CNN Top Stories", "http://rss.cnn.com/rss/edition.rss", false, false);
        dao.persistFeed(this, "News", "CNN Most recent", "http://rss.cnn.com/rss/cnn_latest.rss", false, false);
        dao.persistFeed(this, "News", "Wired top stories", "http://www.wired.com/news_drop/netcenter/netcenter.rdf", false, false);
        dao.persistFeed(this, "News", "FoxNews", "http://www.foxnews.com/xmlfeed/rss/0,4313,0,00.rss", false, false);
        dao.persistFeed(this, "News", "Google News", "http://news.google.com/news?ned=us&topic=h&output=rss", true, false);
        dao.persistFeed(this, "News", "Yahoo News", "http://rss.news.yahoo.com/rss/topstories", false, false);
        dao.persistFeed(this, "Finance", "Reuters", "http://feeds.reuters.com/reuters/topNews", false, false);
        
        // tech
        dao.persistFeed(this, "Technology", "Barrapunto" + MESSAGE_ES, "http://barrapunto.com/index.rss", false, false);
        dao.persistFeed(this, "Technology", "Genbeta" + MESSAGE_ES, "http://feeds.weblogssl.com/genbeta", false, false);
        dao.persistFeed(this, "Technology", "An.droid.es" + MESSAGE_ES, "http://feeds.feedburner.com/AndroidEnEspanol?format=xml", false, false);
        dao.persistFeed(this, "Technology", "ALT1040" + MESSAGE_ES, "http://feeds.hipertextual.com/alt1040?format=xml", false, false);
        dao.persistFeed(this, "Technology", "Slashdot", "http://rss.slashdot.org/slashdot/eqWf", false, false);
        dao.persistFeed(this, "Technology", "LifeHacker", "http://lifehacker.com/index.xml", true, false);
        dao.persistFeed(this, "Technology", "Engadget", "http://www.engadget.com/rss.xml", false, false);
        dao.persistFeed(this, "Technology", "ReadWriteWeb", "http://www.readwriteweb.com/rss.xml", false, false);
//            dao.persistFeed(this, "TechCrunch", "http://www.techcrunch.com/feed/", false, false);
        dao.persistFeed(this, "Technology", "Gizmodo", "http://www.gizmodo.net/index.xml", false, false);
        dao.persistFeed(this, "Technology", "Joel on Software", "http://www.joelonsoftware.com/rss.xml", false, false);
        dao.persistFeed(this, "Technology", "Good Coders", "http://feeds.feedburner.com/catonmat?format=xml", false, false);
        dao.persistFeed(this, "Technology", "Android Blog", "http://feeds2.feedburner.com/androinica", false, false);
        
        // sports
        dao.persistFeed(this, "Sports", "Marca" + MESSAGE_ES, "http://rss.marca.com/rss/descarga.htm?data2=425", false, false);
        dao.persistFeed(this, "Sports", "Marca, fútbol" + MESSAGE_ES, "http://rss.marca.com/rss/descarga.htm?data2=372", false, false);
        dao.persistFeed(this, "Sports", "As.com" + MESSAGE_ES, "http://www.as.com/rss.html", false, false);
        dao.persistFeed(this, "Sports", "ESPN Top", "http://sports.espn.go.com/espn/rss/newsf", false, false);
        dao.persistFeed(this, "Sports", "ESPN NBA", "http://sports.espn.go.com/espn/rss/nba/news", false, false);
        dao.persistFeed(this, "Sports", "ESPN Soccer", "http://soccernet.espn.go.com/rss/news", false, false);
        dao.persistFeed(this, "Sports", "NYTimes Sports", "http://www.nytimes.com/services/xml/rss/nyt/Sports.xml", false, false);
        dao.persistFeed(this, "Sports", "Yahoo Sports", "http://sports.yahoo.com/top/rss.xml", false, false);
        
        // finance
        dao.persistFeed(this, "Finance", "Loogic" + MESSAGE_ES, "http://feeds.feedburner.com/Loogiccom?format=xml", false, false);
        dao.persistFeed(this, "Finance", "NYTimes Business", "http://feeds.nytimes.com/nyt/rss/Business", false, false);
        dao.persistFeed(this, "Finance", "Google News Business", "http://news.google.com/news?ned=us&topic=w&output=rss", false, false);
        dao.persistFeed(this, "Finance", "Wall Street Journal", "http://feeds2.feedburner.com/wsj/xml/rss/3_7481.xml", false, false);
        dao.persistFeed(this, "Finance", "The Economist", "http://www.economist.com/rss/daily_news_and_views_rss.xml", false, false);
        
        // entertainment
        dao.persistFeed(this, "Entertainment", "Meneame" + MESSAGE_ES, "http://meneame.net/rss2.php", false, false);
        dao.persistFeed(this, "Entertainment", "Dilbert Daily Strip", "http://feeds.feedburner.com/DilbertDailyStrip", false, false);
        dao.persistFeed(this, "Entertainment", "CNN entertainment", "http://rss.cnn.com/rss/edition_entertainment.rss", false, false);
        dao.persistFeed(this, "Entertainment", "Brain Teasers", "http://feeds.braingle.com/braingle/all", false, false);
        dao.persistFeed(this, "Entertainment", "Random Jokes", "http://jokes4all.net/rss/040000111/jokes.xml", false, false);
        
        // others
        dao.persistFeed(this, "Misc", "Asco De Vida" + MESSAGE_ES, "http://feeds2.feedburner.com/AscoDeVida", false, false);
        dao.persistFeed(this, "Misc", "F**k My Life", "http://feeds2.feedburner.com/fmylife", false, false);
        dao.persistFeed(this, "Misc", "Quote of the day", "http://www.quotationspage.com/data/qotd.rss", false, false);
        dao.persistFeed(this, "Misc", "Flickr abstract art", "http://api.flickr.com/services/feeds/groups_pool.gne?id=61057342@N00&lang=en-us&format=rss_200", false, false);
        dao.persistFeed(this, "Misc", "NYT Idea of the day", "http://ideas.blogs.nytimes.com/feed/", false, false);
        dao.persistFeed(this, "Misc", "LifeHacker", "http://feeds.gawker.com/lifehacker/excerpts.xml", false, false);
        dao.persistFeed(this, "Misc", "Today in history", "http://feeds2.feedburner.com/historyorb/todayinhistory", false, false);
        dao.persistFeed(this, "Misc", "How stuff works","http://feeds.howstuffworks.com/DailyStuff", false, false);
        
        // parser tests taken from the email errors of AnyRSS
//          dao.persistFeed(this, "test 0", "http://news.baidu.com/n?cmd=7&loc=5495&name=%B9%E3%B6%AB&tn=rss", false, false);
//          dao.persistFeed(this, "test 1", "http://feeds.feedburner.com/Destructoid", false, false);
//          dao.persistFeed(this, "test 2", "http://www.foxnews.com/xmlfeed/rss/0,4313,0,00.rss", false, false);
//          dao.persistFeed(this, "test 3", "http://tabnak.ir/en/rss/allnews", false, false);
//          dao.persistFeed(this, "test 4", "http://news.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml", false, false);
//          dao.persistFeed(this, "test 5", "http://www.wp.pl/rss.xml?id=1", false, false);
//          dao.persistFeed(this, "test 6", "http://www.google.com/reader/public/atom/user/01578920782114465644/label/Periodicos", false, false);
//          dao.persistFeed(this, "test 7", "http://feeds.gawker.com/lifehacker/excerpts.xml", false, false);
//          dao.persistFeed(this, "test 8", "http://www.quotationspage.com/data/qotd.rss", false, false);
//          dao.persistFeed(this, "test 9", "http://news.google.co.in/news?pz=1&cf=all&ned=in&hl=en&output=rss", false, false);
//          dao.persistFeed(this, "test 10", "http://bossip.com/feed/", false, false);
//          dao.persistFeed(this, "test 11" + MESSAGE_ES, "http://rss.elmundo.es/rss/descarga.htm?data2=4", false, false);
//          dao.persistFeed(this, "test 12", "http://skattershooting.blogspot.com/feeds/posts/default", false, false);
//          dao.persistFeed(this, "test 13", "http://www.nytimes.com/services/xml/rss/nyt/Sports.xml", false, false);
//          dao.persistFeed(this, "test 14", "http://www.ctv.ca/generic/generated/freeheadlines/rdf/MontrealHome.xml", false, false);
//          dao.persistFeed(this, "test 15", "http://feeds.feedburner.com/blabbermouth", false, false);
//          dao.persistFeed(this, "test 16", "http://www.Motorsport-Total.com/rss_f1.xml", false, false);
//          dao.persistFeed(this, "test 17", "http://www.gizmodo.net/index.xml", false, false);
//          dao.persistFeed(this, "test 18", "http://rss.feedsportal.com/c/32314/f/440274/index.rss", false, false);
    }
}
