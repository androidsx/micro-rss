package com.androidsx.microrss.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.androidsx.microrss.provider.News.Categories;
import com.androidsx.microrss.provider.News.Feeds;
import com.androidsx.microrss.provider.News.Items;

/**
 * Helper to manage the database version upgrades. Works closely with the content provider,
 * {@link NewsProvider}.
 */
class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "microrss.db";
    private static final int DATABASE_VERSION = 3;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the DB tables. This is only done when the first feed is
     * added, usually after installing the application.
     * 
     * @param db reference to the SQLite database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Create a new database " + DATABASE_NAME
                        + ", version " + DATABASE_VERSION);
        
        db.execSQL("CREATE TABLE " + News.TABLE_CATEGORIES + " ("
                + Categories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Categories.NAME + " TEXT UNIQUE"
                + ");");
        
        db.execSQL("CREATE TABLE " + News.TABLE_FEEDS + " ("
                + Feeds._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Feeds.CATEGORY_ID + " INTEGER,"
                + Feeds.FEED_URL + " TEXT,"
                + Feeds.TITLE + " TEXT,"
                + Feeds.ACTIVE + " BOOLEAN,"
                + Feeds.LAST_UPDATE + " BIGINT, "
                + Feeds.G_READER + " BOOLEAN"
                + ");");

        db.execSQL("CREATE TABLE " + News.TABLE_ITEMS + " ("
                + Items._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Items.FEED_ID + " INTEGER,"
                + Items.POSITION + " INTEGER,"
                + Items.TITLE + " TEXT, "
                + Items.CONTENT + " TEXT,"
                + Items.ITEM_URL + " TEXT,"
                + Items.THUMBNAIL_URL + " TEXT,"
                + Items.DATE + " INTEGER);");
        
        Log.i(TAG, "Insert the initial set of feeds/categories into the DB");

        db.beginTransaction();
		try {
			String sql = "INSERT INTO " + News.TABLE_CATEGORIES + " (" + Categories.NAME + ")"
	        		+ " VALUES (?)";
			SQLiteStatement insert = db.compileStatement(sql);

			insert.bindString(1, "Technology");
			insert.executeInsert();
			
			insert.bindString(1, "News");
			insert.executeInsert();
			
			insert.bindString(1, "Sports");
			insert.executeInsert();
			
			insert.bindString(1, "Business");
			insert.executeInsert();
			
			insert.bindString(1, "Politics");
			insert.executeInsert();
			
			insert.bindString(1, "Autos");
			insert.executeInsert();
			
			insert.bindString(1, "Entertainment");
			insert.executeInsert();

			insert.bindString(1, "Science");
			insert.executeInsert();
			
			insert.bindString(1, "Living");
			insert.executeInsert();
			

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.beginTransaction();
		try {
			String sql = "INSERT INTO " + News.TABLE_FEEDS + " (" + Feeds.CATEGORY_ID + "," + Feeds.FEED_URL
	        		 + "," + Feeds.TITLE + "," + Feeds.ACTIVE + "," + Feeds.LAST_UPDATE + "," + Feeds.G_READER + ")"
	        		 + " VALUES (?, ?, ?, ?, -1, 0)";
			SQLiteStatement insert = db.compileStatement(sql);
			
			// 1 - TECH

			insert.bindLong(1, 1);
			insert.bindString(2, "http://feeds.feedburner.com/Techcrunch");
			insert.bindString(3, "TechCrunch");
			insert.bindLong(4, 1);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2, "http://www.engadget.com/rss.xml");
			insert.bindString(3, "Engadget");
			insert.bindLong(4, 1);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2,
					"http://googleblog.blogspot.com/feeds/posts/default?alt=rss");
			insert.bindString(3, "The Official Google Blog");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2, "http://www.bgr.com/feed/");
			insert.bindString(3, "BGR");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2, "http://feeds2.feedburner.com/thenextweb");
			insert.bindString(3, "The Next Web");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2, "http://feeds.feedburner.com/ommalik");
			insert.bindString(3, "GigaOM");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2, "http://rss.slashdot.org/slashdot/eqWf");
			insert.bindString(3, "Slashdot");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2, "http://lifehacker.com/index.xml");
			insert.bindString(3, "LifeHacker");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2, "http://www.readwriteweb.com/rss.xml");
			insert.bindString(3, "ReadWriteWeb");
			insert.bindLong(4, 1);
			insert.executeInsert();

			insert.bindLong(1, 1);
			insert.bindString(2, "http://www.gizmodo.net/index.xml");
			insert.bindString(3, "Gizmodo");
			insert.bindLong(4, 0);
			insert.executeInsert();

			
			// 2 - NEWS
			insert.bindLong(1, 2);
			insert.bindString(2,
					"http://news.google.com/news?ned=us&topic=h&output=rss");
			insert.bindString(3, "Google News");
			insert.bindLong(4, 1);
			insert.executeInsert();

			insert.bindLong(1, 2);
			insert.bindString(2, "http://feeds.bbci.co.uk/news/rss.xml");
			insert.bindString(3, "BBC Top Stories");
			insert.bindLong(4, 1);
			insert.executeInsert();

			insert.bindLong(1, 2);
			insert.bindString(2,
					"http://www.nytimes.com/services/xml/rss/nyt/GlobalHome.xml");
			insert.bindString(3, "NYTimes World");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 2);
			insert.bindString(2,
					"http://news.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml");
			insert.bindString(3, "BBC World");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 2);
			insert.bindString(2, "http://rss.cnn.com/rss/edition.rss");
			insert.bindString(3, "CNN Top Stories");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 2);
			insert.bindString(2, "http://rss.news.yahoo.com/rss/topstories");
			insert.bindString(3, "Yahoo News");
			insert.bindLong(4, 0);
			insert.executeInsert();

			// 3 - SPORTS
			insert.bindLong(1, 3);
			insert.bindString(2, "http://sports.yahoo.com/top/rss.xml");
			insert.bindString(3, "Yahoo Sports ");
			insert.bindLong(4, 1);
			insert.executeInsert();

			insert.bindLong(1, 3);
			insert.bindString(2, "http://sports.espn.go.com/espn/rss/news");
			insert.bindString(3, "ESPN Top");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 3);
			insert.bindString(2,
					"http://rivals.yahoo.com/ncaa/football/blog/dr_saturday/rss.xml");
			insert.bindString(3, "Dr. Saturday - NCAAF");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 3);
			insert.bindString(2, "http://www.sportsgrid.com/feed/");
			insert.bindString(3, "SportsGrid");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 3);
			insert.bindString(2, "http://sports.espn.go.com/espn/rss/nba/news");
			insert.bindString(3, "ESPN NBA");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 3);
			insert.bindString(2, "http://sports.espn.go.com/espn/rss/nhl/news");
			insert.bindString(3, "ESPN - NFL");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 3);
			insert.bindString(2, "http://soccernet.espn.go.com/rss/news");
			insert.bindString(3, "ESPN Soccer");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 3);
			insert.bindString(2,
					"http://www.nytimes.com/services/xml/rss/nyt/Sports.xml");
			insert.bindString(3, "NYTimes Sports");
			insert.bindLong(4, 0);
			insert.executeInsert();

			// 4 - Business
			insert.bindLong(4, 2);
			insert.bindString(2, "http://feeds.nytimes.com/nyt/rss/Business");
			insert.bindString(3, "NYTimes Business");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(4, 2);
			insert.bindString(2, "http://feeds.reuters.com/reuters/topNews");
			insert.bindString(3, "Reuters");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(4, 2);
			insert.bindString(2,
					"http://feeds2.feedburner.com/wsj/xml/rss/3_7481.xml");
			insert.bindString(3, "Wall Street Journal");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(4, 2);
			insert.bindString(2, "http://www.nakedcapitalism.com/feed");
			insert.bindString(3, "Naked Capitalism");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(4, 2);
			insert.bindString(2, "http://feeds2.feedburner.com/businessinsider");
			insert.bindString(3, "Business Insider");
			insert.bindLong(4, 1);
			insert.executeInsert();


			// 5 - Politics
			insert.bindLong(1, 5);
			insert.bindString(2,
					"http://feeds.huffingtonpost.com/huffingtonpost/raw_feed");
			insert.bindString(3, "The Huffington Post");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 5);
			insert.bindString(2, "http://www.mediaite.com/feed/");
			insert.bindString(3, "Mediaite");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 5);
			insert.bindString(2, "http://feeds.dailykos.com/dailykos/index.xml");
			insert.bindString(3, "Daily Kos");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 5);
			insert.bindString(2, "http://www.redstate.com/feed/rss/");
			insert.bindString(3, "RedState");
			insert.bindLong(4, 0);
			insert.executeInsert();

			// 6 - Autos

			insert.bindLong(1, 6);
			insert.bindString(2,
					"http://feeds.autoblog.com/weblogsinc/autoblog");
			insert.bindString(3, "Autoblog");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 6);
			insert.bindString(2, "http://www.topspeed.com/rss.xml");
			insert.bindString(3, "Top Speed");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 7);
			insert.bindString(2, "http://www.tmz.com/rss.xml");
			insert.bindString(3, "TMZ.com");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 7);
			insert.bindString(2, "http://www.deadline.com/feed/");
			insert.bindString(3, "Deadline");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 7);
			insert.bindString(2, "http://feeds.feedburner.com/nymag/vulture");
			insert.bindString(3, "Vulture");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 7);
			insert.bindString(2, "http://www.joystiq.com/rss.xml");
			insert.bindString(3, "Joystiq - games");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 7);
			insert.bindString(2, "http://feeds2.feedburner.com/justjared/TOvO");
			insert.bindString(3, "Just Jared");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 7);
			insert.bindString(2, "http://feeds.braingle.com/braingle/all");
			insert.bindString(3, "Brain Teasers");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 7);
			insert.bindString(2, "http://jokes4all.net/rss/040000111/jokes.xml");
			insert.bindString(3, "Random Jokes");
			insert.bindLong(4, 0);
			insert.executeInsert();

			// 8 - Science

			insert.bindLong(1, 8);
			insert.bindString(2, "http://www.wired.com/wiredscience/feed/");
			insert.bindString(3, "Wired Science");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 8);
			insert.bindString(2, "http://www.universetoday.com/feed/");
			insert.bindString(3, "Universe Today");
			insert.bindLong(4, 0);
			insert.executeInsert();

			// 9 - Living
			insert.bindLong(1, 9);
			insert.bindString(2, "http://feedproxy.google.com/nymag/fashion");
			insert.bindString(3, "The Cut");
			insert.bindLong(4, 0);
			insert.executeInsert();

			insert.bindLong(1, 9);
			insert.bindString(2, "http://feeds.feedburner.com/fashionistacom");
			insert.bindString(3, "Fashionista");
			insert.bindLong(4, 0);
			insert.executeInsert();

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
    }

    /**
     * Upgrade to a different DB version. Which means, the schema has
     * changed with this new version of the application, which the user just
     * installed.
     * <p>
     * Since the schema has changed, and we do not know how the old DB looks
     * like, we are going to have to remove all the old data, and let the
     * existing feeds crash miserably.
     * <p>
     * TODO(pablo): mechanism to let the update service, or the app feeds,
     * that this has happened, so proper error messages can be shown.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        
        if (version != DATABASE_VERSION) {
            Log.w(TAG, "The database version has changed from " + oldVersion + " to " + newVersion
                    + ". We will now destroy all existing tables and recreate them");
            db.execSQL("DROP TABLE IF EXISTS " + News.TABLE_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + News.TABLE_FEEDS);
            db.execSQL("DROP TABLE IF EXISTS " + News.TABLE_ITEMS);
            onCreate(db);
        }
    }
}
