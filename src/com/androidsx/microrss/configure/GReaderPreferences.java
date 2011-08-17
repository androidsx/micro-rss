package com.androidsx.microrss.configure;

import java.io.IOException;

import org.jarx.android.reader.GoogleReaderClient;
import org.jarx.android.reader.ReaderClient;
import org.jarx.android.reader.ReaderException;
import org.jarx.android.reader.Subscription;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.FeedColumns;
import com.androidsx.microrss.db.MicroRssContentProvider;
import com.wimm.framework.app.TextInputDialog;

public class GReaderPreferences extends PreferenceActivity {
    private static final String TAG = "GReaderPreferences";

    /** FIXME: workaround: it will be true if the TextDialog has been cancelled */
    private boolean dialogHasBeenCancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_greader);
        setContentView(R.layout.wrapper_list_goback);

        ((Preference) findPreference("syncGoogleReaderFeeds"))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new SyncGoogleReaderTask().execute((Void) null);

                        return true;
                    }
                });

        // FIXME: workaround to get the event of an input text
        ((Preference) findPreference(getResources().getString(R.string.pref_google_user_name)))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final SharedPreferences sharedPrefs = PreferenceManager
                                .getDefaultSharedPreferences(GReaderPreferences.this);
                        final String storedUsername = sharedPrefs.getString(getResources()
                                .getString(R.string.pref_google_user_name), "");

                        final TextInputDialog textInput = new TextInputDialog(
                                GReaderPreferences.this);
                        textInput.setTitle("Username");
                        textInput.setText(storedUsername);
                        textInput.setOnDismissListener(new OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (dialogHasBeenCancelled == false) {
                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString(getResources().getString(
                                            R.string.pref_google_user_name), textInput.getText());
                                    editor.commit();
                                } else {
                                    dialogHasBeenCancelled = false;
                                }
                            }
                        });
                        textInput.setOnCancelListener(new OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialogHasBeenCancelled = true;
                            }
                        });

                        textInput.show();

                        return true;
                    }
                });

        // FIXME: workaround to get the event of an input text
        ((Preference) findPreference(getResources().getString(R.string.pref_google_password)))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final TextInputDialog textInput = new TextInputDialog(
                                GReaderPreferences.this);
                        textInput.setTitle("Password");
                        textInput.setOnDismissListener(new OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (dialogHasBeenCancelled == false) {
                                    final SharedPreferences sharedPrefs = PreferenceManager
                                            .getDefaultSharedPreferences(GReaderPreferences.this);
                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString(getResources().getString(
                                            R.string.pref_google_user_name), textInput.getText());
                                    editor.commit();
                                } else {
                                    dialogHasBeenCancelled = false;
                                }
                            }
                        });
                        textInput.setOnCancelListener(new OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialogHasBeenCancelled = true;
                            }
                        });

                        textInput.show();

                        return true;
                    }
                });
        // FIXME: Missing: when we change the update interval, do some kind of refresh. For
        // instance,
        // if it is set to 24 hours and you change it down to 3 minutes, you have to wait another 17
        // hours
        // to get your 3 minutes! Maybe with WIMM this will radically change

    }

    public void onGoBackClick(View target) {
        Intent intent = IntentHelper.createIntent(GReaderPreferences.this, null, Preferences.class);
        startActivity(intent);
    }

    // FIXME: copy-pasted from InitActivity.And, besides, broken (should update instead of insert
    // sometimes)
    private static void writeConfigToBackend(Context context, String title, String feedUrl,
            boolean active) {

        ContentValues values = new ContentValues();
        values.put(FeedColumns.LAST_UPDATE, -1);
        values.put(FeedColumns.TITLE, title);
        values.put(FeedColumns.FEED_URL, feedUrl);
        values.put(FeedColumns.ACTIVE, active);
        values.put(FeedColumns.G_READER, true);

        // TODO: update instead of insert if editing an existing feed
        ContentResolver resolver = context.getContentResolver();
        resolver.insert(MicroRssContentProvider.FEEDS_CONTENT_URI, values);
    }

    public class SyncGoogleReaderTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(GReaderPreferences.this, "Starting syncronization", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            try {
                GoogleReaderClient gReader = new GoogleReaderClient(getApplicationContext());

                SharedPreferences sharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(GReaderPreferences.this);
                String user = sharedPrefs.getString(getResources().getString(
                        R.string.pref_google_user_name), "");
                String password = sharedPrefs.getString(getResources().getString(
                        R.string.pref_google_password), "");

                if (user.equals("") || password.equals("")) {
                    return "Input your credentials";
                }

                gReader.login(user, password);

                gReader.handleSubList(new ReaderClient.SubListHandler() {
                    @Override
                    public boolean subscription(Subscription sub) throws ReaderException {
                        if (sub.getUid().startsWith("feed/")) {
                            String url = sub.getUid().replaceFirst("feed/", "");
                            String title = sub.getTitle();
                            writeConfigToBackend(getApplication(), title, url, false);
                        }
                        return true;
                    }
                }, System.currentTimeMillis());

                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean(
                        getResources().getString(R.string.pref_synced_with_google_reader), true);
                editor.commit();
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e);
                result = "Can't connect: login error";
            } catch (ReaderException e) {
                /*
                 * // TODO: This gets us a very nasty inflater exception. A bug in wimm? Dialog
                 * dialog = new Dialog(Preferences.this);
                 * dialog.setTitle("Can't connect: log-in error"); dialog.show();
                 */
                Log.e(TAG, "ReaderException: " + e);
                result = "Can't connect: login error";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("")) {
                result = "Completed feed syncronization";
            }
            Toast.makeText(GReaderPreferences.this, result, Toast.LENGTH_SHORT).show();
        }
    }

}
