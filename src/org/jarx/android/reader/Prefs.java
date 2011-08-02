package org.jarx.android.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    public static final String KEY_GOOGLE_ID = "google_login_id";
    public static final String KEY_GOOGLE_PASSWD = "google_passwd";
    public static final String KEY_SYNC_INTERVAL_HOURS = "sync_interval_hours";
    public static final String KEY_SYNC_UNREAD_ONLY = "sync_unread_only";
    public static final String KEY_SYNC_NOTIFIABLE = "sync_notifiable";
    public static final String KEY_SYNC_ITEM_LIMIT = "sync_item_limit";
    public static final String KEY_VIEW_UNREAD_ONLY = "view_unread_only";
    public static final String KEY_ITEM_SORT_TYPE = "item_sort_type";
    public static final String KEY_DISABLE_ITEM_LINKS = "disable_item_links";
    public static final String KEY_SHOW_ITEM_CONTROLLS = "show_item_controlls";
    public static final String KEY_ITEM_BODY_FONT_SIZE = "item_body_font_size";
    public static final String KEY_OMIT_ITEM_LIST = "omit_item_list";
    public static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    public static final String KEY_CACHE_KEEP_TAGGED = "cache_keep_tagged";
    public static final String KEY_CACHE_AUTO_CLEANUP_DAYS = "cache_auto_cleanup_days";
    public static final String KEY_UNREAD_COUNT = "unread_count";
    public static final String KEY_ENABLE_ERR_REPORTING = "enable_err_reporting";

    public static SharedPreferences getPrefs(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static String getString(Context c, String name) {
        return getPrefs(c).getString(name, null);
    }

    public static int getInt(Context c, String name, int def) {
        try {
            return getPrefs(c).getInt(name, def);
        } catch (RuntimeException e) {
            return def;
        }
    }

    public static long getLong(Context c, String name, long def) {
        try {
            return getPrefs(c).getLong(name, def);
        } catch (RuntimeException e) {
            return def;
        }
    }

    public static boolean getBoolean(Context c, String name, boolean def) {
        return getPrefs(c).getBoolean(name, def);
    }

    public static void putString(Context c, String name, String value) {
        SharedPreferences sp = getPrefs(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(name, value);
        editor.commit();
    }

    public static void putLong(Context c, String name, long value) {
        SharedPreferences sp = getPrefs(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(name, value);
        editor.commit();
    }

    public static void putInt(Context c, String name, int value) {
        SharedPreferences sp = getPrefs(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public static void putBoolean(Context c, String name, boolean value) {
        SharedPreferences sp = getPrefs(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    public static String getGoogleId(Context c) {
        return getString(c, KEY_GOOGLE_ID);
    }

    public static String getGooglePasswd(Context c) {
        return getString(c, KEY_GOOGLE_PASSWD);
    }

    public static void setGoogleIdPasswd(Context c, String googleId,
            String googlePasswd) {
        SharedPreferences sp = getPrefs(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_GOOGLE_ID, googleId);
        editor.putString(KEY_GOOGLE_PASSWD, googlePasswd);
        editor.commit();
    }

    public static long getSyncInterval(Context c) {
        String h = getString(c, KEY_SYNC_INTERVAL_HOURS);
        int hour = 2;
        if (h != null && h.length() != 0) {
            hour = Integer.parseInt(h);
        }
        return (hour * 60 * 60 * 1000);
    }

    public static boolean isSyncUnreadOnly(Context c) {
        return getBoolean(c, KEY_SYNC_UNREAD_ONLY, false);
    }

    public static boolean isSyncNotifiable(Context c) {
        return getBoolean(c, KEY_SYNC_NOTIFIABLE, true);
    }

    public static int getSyncItemLimit(Context c) {
        return getInt(c, KEY_SYNC_ITEM_LIMIT, 100);
    }

    public static boolean isViewUnreadOnly(Context c) {
        return getBoolean(c, KEY_VIEW_UNREAD_ONLY, false);
    }

    public static int getItemSortType(Context c) {
        return getInt(c, KEY_ITEM_SORT_TYPE, Item.ORDER_BY_NEWER);
    }

    public static void setItemSortType(Context c, int type) {
        putInt(c, KEY_ITEM_SORT_TYPE, type);
    }

    public static boolean isDisableItemLinks(Context c) {
        return getBoolean(c, KEY_DISABLE_ITEM_LINKS, false);
    }

    public static boolean isShowItemControlls(Context c) {
        return getBoolean(c, KEY_SHOW_ITEM_CONTROLLS, true);
    }

    public static int getItemBodyFontSize(Context c) {
        String fontSize = getString(c, KEY_ITEM_BODY_FONT_SIZE);
        if (fontSize != null && fontSize.length() != 0) {
            return Integer.parseInt(fontSize);
        } else {
            return 13;
        }
    }

    public static void setItemBodyFontSize(Context c, int value) {
        putString(c, KEY_ITEM_BODY_FONT_SIZE, Integer.toString(value));
    }

    public static boolean isOmitItemList(Context c) {
        return getBoolean(c, KEY_OMIT_ITEM_LIST, false);
    }

    public static long getLastSyncTime(Context c) {
        return getLong(c, KEY_LAST_SYNC_TIME, 0);
    }

    public static void setLastSyncTime(Context c, long value) {
        putLong(c, KEY_LAST_SYNC_TIME, value);
    }

    public static boolean isCacheKeepTagged(Context c) {
        return getBoolean(c, KEY_CACHE_KEEP_TAGGED, true);
    }

    public static int getCacheAutoCleanupDays(Context c) {
        return getInt(c, KEY_CACHE_AUTO_CLEANUP_DAYS, 7);
    }

    public static int getUnreadCount(Context c) {
        return getInt(c, KEY_UNREAD_COUNT, 0);
    }

    public static void setUnreadCount(Context c, int value) {
        putInt(c, KEY_UNREAD_COUNT, value);
    }

    public static boolean isEnableErrReporting(Context c) {
        return getBoolean(c, KEY_ENABLE_ERR_REPORTING, true);
    }

    public static void setEnableErrReporting(Context c, boolean value) {
        putBoolean(c, KEY_ENABLE_ERR_REPORTING, value);
    }
}
