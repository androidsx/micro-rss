package com.androidsx.microrss;

import com.androidsx.anyrss.AbstractUpdateService;
import com.androidsx.anyrss.configure.DefaultMaxNumItemsSaved;
import com.androidsx.microrss.db.ContentProviderAuthority;

public class UpdateService extends AbstractUpdateService {
    
    @Override
    protected String getAuthority() {
        return ContentProviderAuthority.AUTHORITY;
    }

    @Override
    protected int getMaxItemsToStoreInDb(int appWidgetId) {
        return new DefaultMaxNumItemsSaved(
                R.string.conf_default_num_items_saved,
                R.string.max_num_items_saved_prefs_name).getMaxNumItemsSaved(this, appWidgetId);
    }
}
