package com.androidsx.microrss.db;

import com.androidsx.anyrss.db.AnyRssAbstractContentProvider;

public class AnyRssContentProvider extends AnyRssAbstractContentProvider {

    @Override
    public String getAuthority() {
        return ContentProviderAuthority.AUTHORITY;
    }
    
}
