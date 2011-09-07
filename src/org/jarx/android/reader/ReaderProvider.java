package org.jarx.android.reader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class ReaderProvider extends ContentProvider {

    public static final String AUTHORITY = "org.jarx.android.reader";

    public static final String BEGIN_TXN_URI_NAME
        = "content://" + AUTHORITY + "/begin_txn";
    public static final String SUCCESS_TXN_URI_NAME
        = "content://" + AUTHORITY + "/success_txn";
    public static final String END_TXN_URI_NAME
        = "content://" + AUTHORITY + "/end_txn";
    public static final String SUB_CONTENT_URI_NAME
        = "content://" + AUTHORITY + "/" + Subscription.TABLE_NAME;
    public static final String TAG_CONTENT_URI_NAME
        = "content://" + AUTHORITY + "/" + Tag.TABLE_NAME;
    public static final String TAG2SUB_CONTENT_URI_NAME
        = "content://" + AUTHORITY + "/" + Tag2Sub.TABLE_NAME;
    public static final String ITEM_CONTENT_URI_NAME
        = "content://" + AUTHORITY + "/" + Item.TABLE_NAME;
    public static final String TAG2ITEM_CONTENT_URI_NAME
        = "content://" + AUTHORITY + "/" + Tag2Item.TABLE_NAME;
    public static final String UPDATE_UNREADS_CONTENT_URI_NAME
        = "content://" + AUTHORITY + "/update_unreads";

    public static final Uri URI_TXN_BEGIN = Uri.parse(BEGIN_TXN_URI_NAME);
    public static final Uri URI_TXN_SUCCESS = Uri.parse(SUCCESS_TXN_URI_NAME);
    public static final Uri URI_TXN_END = Uri.parse(END_TXN_URI_NAME);
    public static final Uri URI_UPDATE_UNREADS = Uri.parse(UPDATE_UNREADS_CONTENT_URI_NAME);

    private static final String TAG = "ReaderProvider";
    private static final String DATABASE_NAME = "reader.db";
    private static final int DATABASE_VERSION = 3;

    private static final String CONTENT_TYPE_ITEM
        = "vnd.android.cursor.item/vnd." + AUTHORITY;
    private static final String CONTENT_TYPE_DIR
        = "vnd.android.cursor.dir/vnd." + AUTHORITY;

    private static final String[] SQL_UPDATE_UNREADS = {
        "update subscription set unread_count = (select ifnull(count(item._id), 0) from item where item.sub_id = subscription._id and item.read = 0)",
        "update tag set unread_count = (select ifnull(sum(s.unread_count), 0) from subscription s, tag2sub t2s where s._id = t2s.sub_id and tag.uid = t2s.tag_uid) where tag.type = " + Tag.TYPE_FOLDER,
        "update tag set unread_count = (select ifnull(count(i._id), 0) from item i, tag2item t2i where i._id = t2i.item_id and tag.uid = t2i.tag_uid and i.read = 0) where tag.type <> " + Tag.TYPE_FOLDER
    };

    private static final UriMatcher uriMatcher;
    private static final int UM_BEGIN_TXN = 1;
    private static final int UM_SUCCESS_TXN = 2;
    private static final int UM_END_TXN = 3;
    private static final int UM_SUB_ID = 10;
    private static final int UM_SUBS = 11;
    private static final int UM_TAG_ID = 20;
    private static final int UM_TAGS = 21;
    private static final int UM_TAG2SUB_ID = 30;
    private static final int UM_TAG2SUBS = 31;
    private static final int UM_ITEM_ID = 40;
    private static final int UM_ITEMS = 41;
    private static final int UM_TAG2ITEM_ID = 50;
    private static final int UM_TAG2ITEMS = 51;
    private static final int UM_UPDATE_UNREADS = 80;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "begin_txn", UM_BEGIN_TXN);
        uriMatcher.addURI(AUTHORITY, "success_txn", UM_SUCCESS_TXN);
        uriMatcher.addURI(AUTHORITY, "end_txn", UM_END_TXN);
        uriMatcher.addURI(AUTHORITY, Subscription.TABLE_NAME + "/#", UM_SUB_ID);
        uriMatcher.addURI(AUTHORITY, Subscription.TABLE_NAME, UM_SUBS);
        uriMatcher.addURI(AUTHORITY, Tag.TABLE_NAME + "/#", UM_TAG_ID);
        uriMatcher.addURI(AUTHORITY, Tag.TABLE_NAME, UM_TAGS);
        uriMatcher.addURI(AUTHORITY, Tag2Sub.TABLE_NAME + "/#", UM_TAG2SUB_ID);
        uriMatcher.addURI(AUTHORITY, Tag2Sub.TABLE_NAME, UM_TAG2SUBS);
        uriMatcher.addURI(AUTHORITY, Item.TABLE_NAME + "/#", UM_ITEM_ID);
        uriMatcher.addURI(AUTHORITY, Item.TABLE_NAME, UM_ITEMS);
        uriMatcher.addURI(AUTHORITY, Tag2Item.TABLE_NAME + "/#", UM_TAG2ITEM_ID);
        uriMatcher.addURI(AUTHORITY, Tag2Item.TABLE_NAME, UM_TAG2ITEMS);
        uriMatcher.addURI(AUTHORITY, "update_unreads", UM_UPDATE_UNREADS);
    }

    static String sqlCreateIndex(String tableName, String[] columnNames) {
        StringBuilder buff = new StringBuilder(128);
        buff.append("create index idx_");
        buff.append(tableName);
        for (int i = 0; i < columnNames.length; i++) {
            buff.append("_");
            buff.append(columnNames[i]);
        }
        buff.append(" on ");
        buff.append(tableName);
        buff.append("(");
        for (int i = 0; i < columnNames.length; i++) {
            if (i > 0) {
                buff.append(", ");
            }
            buff.append(columnNames[i]);
        }
        buff.append(")");
        return new String(buff);
    }

    private static String sqlIdWhere(String id, String where) {
        StringBuilder buff = new StringBuilder(128);
        buff.append(BaseColumns._ID);
        buff.append(" = ");
        buff.append(id);
        if (!TextUtils.isEmpty(where)) {
            buff.append(" and ");
            buff.append(where);
        }
        return new String(buff);
    }

    private static class ReaderOpenHelper extends SQLiteOpenHelper {

        private ReaderOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Subscription.SQL_CREATE_TABLE);
            db.execSQL(Tag.SQL_CREATE_TABLE);
            db.execSQL(Tag2Sub.SQL_CREATE_TABLE);
            db.execSQL(Item.SQL_CREATE_TABLE);
            db.execSQL(Tag2Item.SQL_CREATE_TABLE);
            for (String[] columns: Subscription.INDEX_COLUMNS) {
                db.execSQL(sqlCreateIndex(Subscription.TABLE_NAME, columns));
            }
            for (String[] columns: Tag.INDEX_COLUMNS) {
                db.execSQL(sqlCreateIndex(Tag.TABLE_NAME, columns));
            }
            for (String[] columns: Tag2Sub.INDEX_COLUMNS) {
                db.execSQL(sqlCreateIndex(Tag2Sub.TABLE_NAME, columns));
            }
            for (String[] columns: Item.INDEX_COLUMNS) {
                db.execSQL(sqlCreateIndex(Item.TABLE_NAME, columns));
            }
            for (String[] columns: Tag2Item.INDEX_COLUMNS) {
                db.execSQL(sqlCreateIndex(Tag2Item.TABLE_NAME, columns));
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
            for (String sql: Subscription.sqlForUpgrade(oldVer, newVer)) {
                db.execSQL(sql);
            }
            for (String sql: Tag.sqlForUpgrade(oldVer, newVer)) {
                db.execSQL(sql);
            }
            for (String sql: Tag2Sub.sqlForUpgrade(oldVer, newVer)) {
                db.execSQL(sql);
            }
            for (String sql: Item.sqlForUpgrade(oldVer, newVer)) {
                db.execSQL(sql);
            }
            for (String sql: Tag2Item.sqlForUpgrade(oldVer, newVer)) {
                db.execSQL(sql);
            }
        }
    }

    private ReaderOpenHelper openHelper;

    @Override
    public boolean onCreate() {
        this.openHelper = new ReaderOpenHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case UM_SUB_ID:
        case UM_TAG_ID:
        case UM_TAG2SUB_ID:
        case UM_ITEM_ID:
        case UM_TAG2ITEM_ID:
            return CONTENT_TYPE_ITEM;
        case UM_SUBS:
        case UM_TAGS:
        case UM_TAG2SUBS:
        case UM_ITEMS:
        case UM_TAG2ITEMS:
            return CONTENT_TYPE_DIR;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = this.openHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String groupBy = null;
        String having = null;
        String limit = null;
        if (sortOrder != null) {
            int limitOff = sortOrder.indexOf(" limit ");
            if (limitOff != -1) {
                limit = sortOrder.substring(limitOff + " limit ".length());
                sortOrder = sortOrder.substring(0, limitOff);
            }
        }
        switch (uriMatcher.match(uri)) {
        case UM_BEGIN_TXN:
            db.beginTransaction();
            return null;
        case UM_SUCCESS_TXN:
            db.setTransactionSuccessful();
            return null;
        case UM_END_TXN:
            db.endTransaction();
            return null;
        case UM_SUB_ID:
            if (projection == null) {
                projection = Subscription.DEFAULT_SELECT;
            }
            qb.setTables(Subscription.TABLE_NAME);
            qb.appendWhere(Subscription._ID + " = "
                + uri.getPathSegments().get(1));
            break;
        case UM_SUBS:
            if (selection != null
                    && selection.indexOf(Tag2Sub.TABLE_NAME + ".") != -1) {
                if (projection == null) {
                    projection = Subscription.PREFIX_SELECT;
                }
                qb.setTables(Subscription.TABLE_NAME + ", " + Tag2Sub.TABLE_NAME);
            } else {
                if (projection == null) {
                    projection = Subscription.DEFAULT_SELECT;
                }
                qb.setTables(Subscription.TABLE_NAME);
            }
            break;
        case UM_TAG_ID:
            qb.setTables(Tag.TABLE_NAME);
            qb.appendWhere(Tag._ID + " = " + uri.getPathSegments().get(1));
            break;
        case UM_TAGS:
            qb.setTables(Tag.TABLE_NAME);
            break;
        case UM_TAG2SUB_ID:
            qb.setTables(Tag2Sub.TABLE_NAME);
            qb.appendWhere(Tag2Sub._ID + " = " + uri.getPathSegments().get(1));
            break;
        case UM_TAG2SUBS:
            qb.setTables(Tag2Sub.TABLE_NAME);
            break;
        case UM_ITEM_ID:
            qb.setTables(Item.TABLE_NAME);
            qb.appendWhere(Item._ID + " = " + uri.getPathSegments().get(1));
            break;
        case UM_ITEMS:
            if (selection != null
                    && selection.indexOf(Tag2Item.TABLE_NAME + ".") != -1) {
                if (projection == null) {
                    projection = Item.PREFIX_SELECT;
                }
                qb.setTables(Item.TABLE_NAME + ", " + Tag2Item.TABLE_NAME);
            } else {
                if (projection == null) {
                    projection = Item.DEFAULT_SELECT;
                }
                qb.setTables(Item.TABLE_NAME);
            }
            if (sortOrder == null) {
                sortOrder = Item.getDefaultOrderBy(getContext());
            }
            if (limit == null) {
                 limit = "1000";
            }
            break;
        case UM_TAG2ITEM_ID:
            qb.setTables(Tag2Item.TABLE_NAME);
            qb.appendWhere(Tag2Item._ID + " = " + uri.getPathSegments().get(1));
            break;
        case UM_TAG2ITEMS:
            if (selection != null
                    && selection.indexOf(Tag.TABLE_NAME + ".") != -1) {
                qb.setTables(Tag2Item.TABLE_NAME + ", " + Tag.TABLE_NAME);
            } else if (selection != null
                    && selection.indexOf(Item.TABLE_NAME + ".") != -1){
                qb.setTables(Tag2Item.TABLE_NAME + ", " + Item.TABLE_NAME);
            } else {
                qb.setTables(Tag2Item.TABLE_NAME);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs,
                groupBy, having, sortOrder, limit);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName;
        Uri contentUri;
        switch (uriMatcher.match(uri)) {
        case UM_SUBS:
            tableName = Subscription.TABLE_NAME;
            contentUri = Subscription.CONTENT_URI;
            break;
        case UM_TAGS:
            tableName = Tag.TABLE_NAME;
            contentUri = Tag.CONTENT_URI;
            break;
        case UM_TAG2SUBS:
            tableName = Tag2Sub.TABLE_NAME;
            contentUri = Tag2Sub.CONTENT_URI;
            break;
        case UM_ITEMS:
            tableName = Item.TABLE_NAME;
            contentUri = Item.CONTENT_URI;
            break;
        case UM_TAG2ITEMS:
            tableName = Tag2Item.TABLE_NAME;
            contentUri = Tag2Item.CONTENT_URI;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = openHelper.getWritableDatabase();
        long rowId = db.insert(tableName, tableName, values);
        if (rowId > 0) {
            Uri insertedUri = ContentUris.withAppendedId(contentUri, rowId);
            getContext().getContentResolver().notifyChange(insertedUri, null);
            return insertedUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        return update(uri, null, where, whereArgs, false);
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        return update(uri, values, where, whereArgs, true);
    }

    private int update(Uri uri, ContentValues values, String where,
            String[] whereArgs, boolean update) {
        SQLiteDatabase db = this.openHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
        case UM_SUB_ID:
            tableName = Subscription.TABLE_NAME;
            where = sqlIdWhere(uri.getPathSegments().get(1), where);
            break;
        case UM_SUBS:
            tableName = Subscription.TABLE_NAME;
            break;
        case UM_TAG_ID:
            tableName = Tag.TABLE_NAME;
            where = sqlIdWhere(uri.getPathSegments().get(1), where);
            break;
        case UM_TAGS:
            tableName = Tag.TABLE_NAME;
            break;
        case UM_TAG2SUB_ID:
            tableName = Tag2Sub.TABLE_NAME;
            where = sqlIdWhere(uri.getPathSegments().get(1), where);
            break;
        case UM_TAG2SUBS:
            tableName = Tag2Sub.TABLE_NAME;
            break;
        case UM_ITEM_ID:
            tableName = Item.TABLE_NAME;
            where = sqlIdWhere(uri.getPathSegments().get(1), where);
            break;
        case UM_ITEMS:
            tableName = Item.TABLE_NAME;
            break;
        case UM_TAG2ITEM_ID:
            tableName = Tag2Item.TABLE_NAME;
            where = sqlIdWhere(uri.getPathSegments().get(1), where);
            break;
        case UM_TAG2ITEMS:
            tableName = Tag2Item.TABLE_NAME;
            break;
        case UM_UPDATE_UNREADS:
            for (String sql: SQL_UPDATE_UNREADS) {
                db.execSQL(sql);
            }
            return 0;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        int count = update ? db.update(tableName, values, where, whereArgs):
            db.delete(tableName, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
