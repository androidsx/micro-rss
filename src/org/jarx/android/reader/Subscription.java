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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public class Subscription implements Serializable, BaseColumns, Parcelable {

    public static final String TABLE_NAME = "subscription";

    public static final Uri CONTENT_URI
        = Uri.parse(ReaderProvider.SUB_CONTENT_URI_NAME);

    public static final String _UID = "uid";
    public static final String _SORTID = "sortid";
    public static final String _TITLE = "title";
    public static final String _HTML_URL = "html_url";
    public static final String _ICON = "icon";
    public static final String _UNREAD_COUNT = "unread_count";
    public static final String _NEWEST_ITEM_TIME = "newest_item_time";
    public static final String _READ_ITEM_ID = "read_item_id";
    public static final String _SYNC_TIME = "sync_time";
    public static final String _ITEM_SYNC_TIME = "item_sync_time";

    public static final String[] DEFAULT_SELECT = {
        _ID, _UID, _SORTID, _TITLE, _HTML_URL, _ICON,
        _UNREAD_COUNT, _NEWEST_ITEM_TIME,
        _READ_ITEM_ID, _SYNC_TIME, _ITEM_SYNC_TIME
    };

    public static final String[] PREFIX_SELECT = new String[DEFAULT_SELECT.length];

    public static final String[] SELECT_ID = {_ID};
    public static final String[] SELECT_ICON = {_ICON};
    public static final String[] SELECT_COUNT = {"count(" + _ID + ")"};
    public static final String[] SELECT_SUM_UNREAD_COUNT
        = {"sum(" + _UNREAD_COUNT + ")"};

    public static final String SQL_CREATE_TABLE
        = "create table if not exists " + TABLE_NAME + " ("
        + _ID + " integer primary key, "
        + _UID + " text not null,"
        + _SORTID + " text not null,"
        + _TITLE + " text not null,"
        + _HTML_URL + " text,"
        + _ICON + " blob,"
        + _UNREAD_COUNT + " integer not null default 0,"
        + _NEWEST_ITEM_TIME + " integer not null default 0,"
        + _READ_ITEM_ID + " integer not null default 0,"
        + _SYNC_TIME + " integer not null default 0,"
        + _ITEM_SYNC_TIME + " integer not null default 0"
        + ")";

    public static final String[] []INDEX_COLUMNS = {
        {_UID},
        {_UNREAD_COUNT},
        {_NEWEST_ITEM_TIME},
        {_ITEM_SYNC_TIME},
        {_NEWEST_ITEM_TIME, _ITEM_SYNC_TIME}
    };

    static {
        for (int i = 0; i < PREFIX_SELECT.length; i++) {
            PREFIX_SELECT[i] = TABLE_NAME + "." + DEFAULT_SELECT[i];
        }
    }

    public static String[] sqlForUpgrade(int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            return new String[]{"update " + TABLE_NAME + " set "
                + _ITEM_SYNC_TIME + " = (" + _ITEM_SYNC_TIME + " / 1000)"};
        }
        return new String[0];
    }

    private long id;
    private String uid;
    private String sortid;
    private String title;
    private String htmlUrl;
    private List<String> categories;
    private int unreadCount;
    private long newestItemTime;
    private long readItemId;
    private long syncTime;
    private long itemSyncTime;

    public Subscription() {
    }

    public Subscription(Parcel in) {
        this.id = in.readLong();
        this.uid = in.readString();
        this.sortid = in.readString();
        this.title = in.readString();
        this.htmlUrl = in.readString();
        int categorySize = in.readInt();
        if (categorySize > 0) {
            String[] cs = new String[categorySize];
            in.readStringArray(cs);
            this.categories = new ArrayList<String>(cs.length);
            for (String c: cs) {
                this.categories.add(c);
            }
        }
        this.unreadCount = in.readInt();
        this.newestItemTime = in.readLong();
        this.readItemId = in.readLong();
        this.syncTime = in.readLong();
        this.itemSyncTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.id);
        out.writeString(this.uid);
        out.writeString(this.sortid);
        out.writeString(this.title);
        out.writeString(this.htmlUrl);
        int size = (this.categories == null) ? 0: this.categories.size();
        out.writeInt(size);
        if (size > 0) {
            out.writeStringArray(this.categories.toArray(new String[size]));
        }
        out.writeInt(this.unreadCount);
        out.writeLong(this.newestItemTime);
        out.writeLong(this.readItemId);
        out.writeLong(this.syncTime);
        out.writeLong(this.itemSyncTime);
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSortid() {
        return this.sortid;
    }

    public void setSortid(String sortid) {
        this.sortid = sortid;
    }

    public String getTitle() {
        return this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtmlUrl() {
        return this.htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public int getUnreadCount() {
        return this.unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getNewestItemTime() {
        return this.newestItemTime;
    }

    public void setNewestItemTime(long newestItemTime) {
        this.newestItemTime = newestItemTime;
    }

    public long getReadItemId() {
        return this.readItemId;
    }

    public void setReadItemId(long readItemId) {
        this.readItemId = readItemId;
    }

    public long getSyncTime() {
        return this.syncTime;
    }

    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

    public long getItemSyncTime() {
        return this.itemSyncTime;
    }

    public void setItemSyncTime(long itemSyncTime) {
        this.itemSyncTime = itemSyncTime;
    }

    public Bitmap getIcon(Context context) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, this.getId());
        Cursor csr = cr.query(uri, SELECT_ICON, null, null, null);
        try {
            csr.moveToNext();
            byte[] data = csr.getBlob(0);
            if (data != null) {
                return BitmapFactory.decodeByteArray(
                    data, 0, data.length);
            }
        } catch (OutOfMemoryError e) {
            // NOTE: ignore, display no icon
        } finally {
            csr.close();
        }
        return null;
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
        values.put(_UID, this.uid);
        values.put(_SORTID, this.sortid);
        values.put(_TITLE, this.title);
        values.put(_HTML_URL, this.htmlUrl);
        /*
        values.put(_UNREAD_COUNT, this.unreadCount);
        values.put(_NEWEST_ITEM_TIME, this.newestItemTime);
        values.put(_READ_ITEM_ID, this.readItemId);
        values.put(_SYNC_TIME, this.syncTime);
        values.put(_ITEM_SYNC_TIME, this.itemSyncTime);
        */
        return values;
    }

    public Uri getContentUri() {
        return ContentUris.withAppendedId(Subscription.CONTENT_URI, getId());
    }

    public Subscription clear() {
        this.id = 0;
        this.uid = null;
        this.sortid = null;
        this.title = null;
        this.htmlUrl = null;
        if (this.categories != null) {
            this.categories.clear();
        }
        this.unreadCount = 0;
        this.newestItemTime = 0;
        this.readItemId = 0;
        this.syncTime = 0;
        this.itemSyncTime = 0;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Subscription) {
            Subscription s = (Subscription) o;
            return s.getId() == this.getId();
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder(64);
        buff.append("Subscription{id=").append(this.id);
        buff.append(",uid=").append(this.uid);
        buff.append(",title=").append(this.title).append("}");
        return new String(buff);
    }

    public static class FilterCursor extends CursorWrapper {

        private final Subscription sub;
        private final int posId;
        private final int posUid;
        private final int posSortid;
        private final int posTitle;
        private final int posHtmlUrl;
        private final int posUnreadCount;
        private final int posNewestItemTime;
        private final int posReadItemId;
        private final int posSyncTime;
        private final int posItemSyncTime;

        public FilterCursor(Cursor csr) {
            this(csr, null);
        }

        public FilterCursor(Cursor csr, Subscription sub) {
            super(csr);
            this.sub = sub;
            this.posId = getColumnIndex(Subscription._ID);
            this.posUid = getColumnIndex(Subscription._UID);
            this.posSortid = getColumnIndex(Subscription._SORTID);
            this.posTitle = getColumnIndex(Subscription._TITLE);
            this.posHtmlUrl = getColumnIndex(Subscription._HTML_URL);
            this.posUnreadCount = getColumnIndex(Subscription._UNREAD_COUNT);
            this.posNewestItemTime = getColumnIndex(Subscription._NEWEST_ITEM_TIME);
            this.posReadItemId = getColumnIndex(Subscription._READ_ITEM_ID);
            this.posSyncTime = getColumnIndex(Subscription._SYNC_TIME);
            this.posItemSyncTime = getColumnIndex(Subscription._ITEM_SYNC_TIME);
        }

        public Subscription getSubscription() {
            Subscription sub = (this.sub == null)? new Subscription():
                this.sub.clear();
            sub.setId(getLong(this.posId));
            sub.setUid(getString(this.posUid));
            sub.setSortid(getString(this.posSortid));
            sub.setTitle(getString(this.posTitle));
            sub.setHtmlUrl(getString(this.posHtmlUrl));
            sub.setUnreadCount(getInt(this.posUnreadCount));
            sub.setNewestItemTime(getInt(this.posNewestItemTime));
            sub.setReadItemId(getLong(this.posReadItemId));
            sub.setSyncTime(getLong(this.posSyncTime));
            sub.setItemSyncTime(getLong(this.posItemSyncTime));
            return sub;
        }

        public long getId() {
            return getLong(this.posId);
        }

        public int getUnreadCount() {
            return getInt(this.posUnreadCount);
        }

        public long getNewestItemTime() {
            return getLong(this.posNewestItemTime);
        }
    }
}
