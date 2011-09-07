package org.jarx.android.reader;

import java.io.Serializable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.provider.BaseColumns;

public class Tag implements Serializable, BaseColumns {

    public static final String TABLE_NAME = "tag";

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_TAG_STARRED = 1;
    public static final int TYPE_TAG_LABEL = 2;
    public static final int TYPE_FOLDER = 3;

    public static final Uri CONTENT_URI
        = Uri.parse(ReaderProvider.TAG_CONTENT_URI_NAME);

    public static final String _UID = "uid";
    public static final String _TYPE = "type";
    public static final String _SORTID = "sortid";
    public static final String _LABEL = "label";
    public static final String _UNREAD_COUNT = "unread_count";
    public static final String _SYNC_TIME = "sync_time";

    public static final String[] DEFAULT_SELECT = {
        _ID, _UID, _SORTID, _LABEL, _SYNC_TIME
    };

    public static final String[] SELECT_ID = {_ID};

    public static final String SQL_CREATE_TABLE
        = "create table if not exists " + TABLE_NAME + " ("
        + _ID + " integer primary key, "
        + _UID + " text not null,"
        + _TYPE + " integer not null,"
        + _SORTID + " text not null,"
        + _LABEL + " text not null,"
        + _UNREAD_COUNT + " integer not null default 0,"
        + _SYNC_TIME + " integer not null default 0"
        + ")";

    public static final String[][] INDEX_COLUMNS = {
        {_UID},
        {_TYPE},
        {_SORTID},
        {_SYNC_TIME}
    };

    public static String[] sqlForUpgrade(int oldVersion, int newVersion) {
        return new String[0];
    }

    private long id;
    private String uid;
    private int type;
    private String sortid;
    private String label;
    private int unreadCount;
    private long syncTime;

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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSortid() {
        return this.sortid;
    }

    public void setSortid(String sortid) {
        this.sortid = sortid;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getUnreadCount() {
        return this.unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getSyncTime() {
        return this.syncTime;
    }

    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (this.id > 0) {
            values.put(_ID, this.id);
        }
        values.put(_UID, this.uid);
        if (this.type != 0) {
            values.put(_TYPE, this.type);
        }
        values.put(_SORTID, this.sortid);
        values.put(_LABEL, this.label);
        /*
        values.put(_UNREAD_COUNT, this.unreadCount);
        values.put(_SYNC_TIME, this.syncTime);
        */
        return values;
    }

    public static class FilterCursor extends CursorWrapper {

        private final Tag tag;
        private final int posId;
        private final int posUid;
        private final int posType;
        private final int posSortid;
        private final int posLabel;
        private final int posUnreadCount;
        private final int posSyncTime;

        public FilterCursor(Cursor csr) {
            this(csr, null);
        }

        public FilterCursor(Cursor csr, Tag tag) {
            super(csr);
            this.tag = tag;
            this.posId = getColumnIndex(Tag._ID);
            this.posUid = getColumnIndex(Tag._UID);
            this.posType = getColumnIndex(Tag._TYPE);
            this.posSortid = getColumnIndex(Tag._SORTID);
            this.posLabel = getColumnIndex(Tag._LABEL);
            this.posUnreadCount = getColumnIndex(Tag._UNREAD_COUNT);
            this.posSyncTime = getColumnIndex(Tag._SYNC_TIME);
        }

        public Tag getTag() {
            Tag tag = (this.tag == null) ? new Tag(): this.tag;
            tag.setId(getLong(this.posId));
            tag.setUid(getString(this.posUid));
            tag.setType(getInt(this.posType));
            tag.setSortid(getString(this.posSortid));
            tag.setLabel(getString(this.posLabel));
            tag.setUnreadCount(getInt(this.posUnreadCount));
            tag.setSyncTime(getLong(this.posSyncTime));
            return tag;
        }

        public String getUid() {
            return getString(this.posUid);
        }

        public int getType() {
            return getInt(this.posType);
        }
    }
}
