package com.androidsx.microrss;

import com.androidsx.microrss.configure.DefaultMaxNumItemsSaved;

public class UpdateService extends AbstractUpdateService {
    
    @Override
    protected int getMaxItemsToStoreInDb(int appWidgetId) {
        return new DefaultMaxNumItemsSaved(
                R.string.conf_default_num_items_saved,
                R.string.max_num_items_saved_prefs_name).getMaxNumItemsSaved(this, appWidgetId);
    }
}
