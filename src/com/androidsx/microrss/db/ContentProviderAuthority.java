package com.androidsx.microrss.db;

/**
 * Provides the content provider authority, that is used to build Uris to access
 * the DB.
 * TODO (WIMM): use Activity.managedQuery instead of ContentResolver.query
 */
public final class ContentProviderAuthority {

    public static final String AUTHORITY = "com.androidsx.microrss";
    
}
