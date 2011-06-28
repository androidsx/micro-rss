package com.androidsx.microrss;

import android.appwidget.AppWidgetProvider;
import android.util.Log;

import com.androidsx.anyrss.AbstractUpdateService;
import com.androidsx.anyrss.AppWidgetUpdater;
import com.androidsx.anyrss.FlurryConstants;
import com.androidsx.microrss.R;
import com.androidsx.anyrss.configure.DefaultMaxNumItemsSaved;
import com.androidsx.microrss.db.ContentProviderAuthority;
import com.flurry.android.FlurryAgent;

public class UpdateService extends AbstractUpdateService {
    
    @SuppressWarnings("unchecked") //This is safe
    private static final Class<? extends AppWidgetProvider>[] WIDGET_PROVIDERS = new Class[] {
            MedAppWidget.class,
            TinyAppWidget.class,
            LargeAppWidget.class};
    
    @Override
    protected String getAuthority() {
        return ContentProviderAuthority.AUTHORITY;
    }
    
    @Override
    protected Class<? extends AppWidgetProvider>[] getWidgetProviders() {
        return WIDGET_PROVIDERS;
    }

    @Override
    protected int getMaxItemsToStoreInDb(int appWidgetId) {
        return new DefaultMaxNumItemsSaved(
                R.string.conf_default_num_items_saved,
                R.string.max_num_items_saved_prefs_name).getMaxNumItemsSaved(this, appWidgetId);
    }

    @Override
    protected AppWidgetUpdater getUpdaterForName(String providerName, int appWidgetId) {
      if (providerName.equals(MedAppWidget.class.getName())) {
        return new MedAppWidgetUpdater();
      } else if (providerName.equals(TinyAppWidget.class.getName())) {
        return new TinyAppWidgetUpdater();
      } else if (providerName.equals(LargeAppWidget.class.getName())) {
        return new LargeAppWidgetUpdater();
      } else {
          Log.e(TAG, "What? The provider " + providerName
                            + " does not correspond to any widget!");
          FlurryAgent.onError(FlurryConstants.ERROR_ID_INFO_PROVIDER, "Info provider is not correct", "");
          throw new IllegalArgumentException();
      }
    }

}
