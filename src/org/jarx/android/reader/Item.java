package org.jarx.android.reader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Parcelable;
import android.os.Parcel;
import android.provider.BaseColumns;
import static org.jarx.android.reader.Utils.*; 

public class Item implements Serializable, BaseColumns {

    public static final String TABLE_NAME = "item";

    public static final Uri CONTENT_URI
        = Uri.parse(ReaderProvider.ITEM_CONTENT_URI_NAME);

    public static final String _SUB_ID = "sub_id";
    public static final String _UID = "uid";
    public static final String _TITLE = "title";
    public static final String _CONTENT = "content";
    public static final String _CONTENT_TYPE = "content_type";
    public static final String _AUTHOR = "author";
    public static final String _LINK = "link";
    public static final String _PUBLISHED_TIME = "published_time";
    public static final String _UPDATED_TIME = "updated_time";
    public static final String _READ = "read";
    public static final String _READ_TIME = "read_time";
    public static final String _SYNC_TIME = "sync_time";

    public static final String[] DEFAULT_SELECT = {
        _ID, _SUB_ID, _UID, _TITLE, _CONTENT, _CONTENT_TYPE, _AUTHOR, _LINK,
        _PUBLISHED_TIME, _UPDATED_TIME, _READ, _READ_TIME, _SYNC_TIME
    };
    public static final String[] PREFIX_SELECT
        = new String[DEFAULT_SELECT.length];
    public static final String[] PREFIX_SELECT_NO_CONTENT
        = new String[DEFAULT_SELECT.length];

    public static final String[] SELECT_ID = {TABLE_NAME + "." + _ID};
    public static final String[] SELECT_COUNT = {"count(" + _ID + ")"};
    public static final String[] SELECT_MAX_ID
        = {"max(" + _ID + ")", "count(" + _ID + ")"};
    public static final String[] SELECT_MIN_ID
        = {"min(" + _ID + ")", "count(" + _ID + ")"};

    public static final int ORDER_BY_NEWER = 1;
    public static final int ORDER_BY_OLDER = 2;

    public static final String SQL_CREATE_TABLE
        = "create table if not exists " + TABLE_NAME + " ("
        + _ID + " integer primary key, "
        + _SUB_ID + " integer not null,"
        + _UID + " text not null,"
        + _TITLE + " text not null,"
        + _CONTENT + " text,"
        + _CONTENT_TYPE + " text,"
        + _AUTHOR + " text,"
        + _LINK + " text,"
        + _PUBLISHED_TIME + " integer,"
        + _UPDATED_TIME + " integer,"
        + _READ + " integer not null default 0,"
        + _READ_TIME + " integer not null default 0,"
        + _SYNC_TIME + " integer not null default 0"
        + ")";

    public static final String[][] INDEX_COLUMNS = {
        {_SUB_ID},
        {_UID},
        {_TITLE},
        {_PUBLISHED_TIME},
        {_READ},
        {_READ_TIME},
        {_SYNC_TIME},
        {_ID, _READ},
        {_SUB_ID, _READ},
        {_READ, _READ_TIME} // since database version 2
    };

    static {
        for (int i = 0; i < PREFIX_SELECT.length; i++) {
            String col = DEFAULT_SELECT[i];
            PREFIX_SELECT[i] = TABLE_NAME + "." + col;
            if (col.equals(_CONTENT)) {
                PREFIX_SELECT_NO_CONTENT[i] = "NULL";
            } else {
                PREFIX_SELECT_NO_CONTENT[i] = PREFIX_SELECT[i];
            }
        }
    }

    public static String[] sqlForUpgrade(int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            return new String[] {
                ReaderProvider.sqlCreateIndex(TABLE_NAME,
                    new String[]{_READ, _READ_TIME})
            };
        }
        return new String[0];
    }

    public static String getDefaultOrderBy(Context c) {
        int type = Prefs.getItemSortType(c);
        if (type == ORDER_BY_NEWER) {
            return TABLE_NAME + "." + _PUBLISHED_TIME + " desc, "
                + TABLE_NAME + "." + _ID + " desc";
        } else {
            return TABLE_NAME + "." + _PUBLISHED_TIME + " asc, "
                + TABLE_NAME + "." + _ID + " asc";
        }
    }

    private long id;
    private long subId;
    private String uid;
    private String title;
    private String content;
    private String contentType;
    private String author;
    private String link;
    private long publishedTime;
    private long updatedTime;
    private boolean read;
    private List<String> categories;
    private long readTime;
    private long syncTime;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSubId() {
        return this.subId;
    }

    public void setSubId(long subId) {
        this.subId = subId;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getPublishedTime() {
        return this.publishedTime;
    }

    public void setPublishedTime(long publishedTime) {
        this.publishedTime = publishedTime;
    }

    public long getUpdateedTime() {
        return this.updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public boolean isRead() {
        return this.read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public long getReadTime() {
        return this.readTime;
    }

    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }

    public long getSyncTime() {
        return this.syncTime;
    }

    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void addCategory(String category) {
        if (this.categories == null) {
            this.categories = new ArrayList<String>(8);
        }
        this.categories.add(category);
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (this.id > 0) {
            values.put(_ID, this.id);
        }
        values.put(_SUB_ID, this.subId);
        values.put(_UID, this.uid);
        values.put(_TITLE, (this.title == null) ? "(No title)": this.title);
        values.put(_CONTENT, this.content);
        values.put(_CONTENT_TYPE, this.contentType);
        values.put(_AUTHOR, this.author);
        values.put(_LINK, this.link);
        values.put(_PUBLISHED_TIME, this.publishedTime);
        values.put(_UPDATED_TIME, this.updatedTime);
        values.put(_READ, this.read ? 1: 0);
        /*
        values.put(_READ_TIME, this.readTime);
        values.put(_SYNC_TIME, this.syncTime);
        */
        return values;
    }

    public Item clear() {
        this.id = 0;
        this.subId = 0;
        this.uid = null;
        this.title = null;
        this.content = null;
        this.contentType = null;
        this.author = null;
        this.link = null;
        this.publishedTime = 0;
        this.updatedTime = 0;
        this.read = false;
        if (this.categories != null) {
            this.categories.clear();
        }
        this.readTime = 0;
        this.syncTime = 0;

        return this;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder(256);
        buff.append("Item{id=").append(this.id);
        buff.append(",subId=").append(this.subId);
        buff.append(",uid=").append(this.uid);
        buff.append(",title=").append(this.title);
        buff.append(",content.length=")
            .append((this.content == null) ? 0: this.content.length());
        buff.append(",contentType=").append(this.contentType);
        buff.append(",author=").append(this.author);
        buff.append(",link=").append(this.link);
        buff.append(",publishedTime=").append(this.publishedTime);
        buff.append(",updatedTime=").append(this.updatedTime);
        buff.append(",read=").append(this.read);
        buff.append(",categories=").append(this.categories);
        buff.append(",readTime=").append(this.readTime);
        buff.append(",syncTime=").append(this.syncTime);
        return new String(buff);
    }

    public static class FilterCursor extends CursorWrapper {

        private final Item item;
        private final int posId;
        private final int posSubId;
        private final int posUid;
        private final int posTitle;
        private final int posContent;
        private final int posContentType;
        private final int posAuthor;
        private final int posLink;
        private final int posPublishedTime;
        private final int posUpdatedTime;
        private final int posRead;
        private final int posReadTime;
        private final int posSyncTime;

        public FilterCursor(Cursor csr) {
            this(csr, null);
        }

        public FilterCursor(Cursor csr, Item item) {
            super(csr);
            this.item = item;
            this.posId = getColumnIndex(Item._ID);
            this.posSubId = getColumnIndex(Item._SUB_ID);
            this.posUid = getColumnIndex(Item._UID);
            this.posTitle = getColumnIndex(Item._TITLE);
            this.posContent = getColumnIndex(Item._CONTENT);
            this.posContentType = getColumnIndex(Item._CONTENT_TYPE);
            this.posAuthor = getColumnIndex(Item._AUTHOR);
            this.posLink = getColumnIndex(Item._LINK);
            this.posPublishedTime = getColumnIndex(Item._PUBLISHED_TIME);
            this.posUpdatedTime = getColumnIndex(Item._UPDATED_TIME);
            this.posRead = getColumnIndex(Item._READ);
            this.posReadTime = getColumnIndex(Item._READ_TIME);
            this.posSyncTime = getColumnIndex(Item._SYNC_TIME);
        }

        public Item getItem() {
            Item item = (this.item == null) ? new Item(): this.item.clear();
            item.setId(getLong(this.posId));
            item.setSubId(getLong(this.posSubId));
            item.setUid(getString(this.posUid));
            item.setTitle(getString(this.posTitle));
            if (this.posContent != -1) {
               item.setContent(getString(this.posContent));
            }
            item.setContentType(getString(this.posContentType));
            item.setAuthor(getString(this.posAuthor));
            item.setLink(getString(this.posLink));
            item.setPublishedTime(getLong(this.posPublishedTime));
            item.setUpdatedTime(getLong(this.posUpdatedTime));
            item.setRead(isRead());
            item.setReadTime(getLong(this.posReadTime));
            item.setSyncTime(getLong(this.posSyncTime));
            return item;
        }

        public long getId() {
            return getLong(this.posId);
        }

        public long getSubId() {
            return getLong(this.posSubId);
        }

        public boolean isRead() {
            return (getInt(this.posRead) == 1);
        }

        public String getUid() {
            return getString(this.posUid);
        }

        public long getReadTime() {
            return getLong(this.posReadTime);
        }

        public long getPublishedTime() {
            return getLong(this.posPublishedTime);
        }
    }
}
