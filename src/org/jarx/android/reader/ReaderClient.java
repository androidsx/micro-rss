package org.jarx.android.reader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;
import android.content.Context;
import android.util.Log;

public abstract class ReaderClient {

    private static final String TAG = "ReaderClient";

    protected final Context context;
    protected DefaultHttpClient client;

    public ReaderClient(Context context) {
        this.context = context;
        this.client = Utils.createHttpClient();

        cleanupOldVersionTemp();
    }

    private void cleanupOldVersionTemp() {
        String path = System.getProperty("java.io.tmpdir");
        File tmpdir = new File(path);
        String[] list = tmpdir.list(new FilenameFilter() {
            public  boolean accept(File dir, String name) {
                return (name.startsWith("reader") && name.endsWith(".txt"));
            }
        });
        if (list != null) {
            long expired = System.currentTimeMillis() - (30 * 60 * 1000);
            for (String l: list) {
                File f = new File(tmpdir, l);
                if (f.lastModified() < expired) {
                    f.delete();
                }
            }
        }
    }

    public abstract boolean login() throws IOException, ReaderException;

    public abstract boolean login(String loginId, String password)
        throws IOException, ReaderException;

    public abstract void logout();

    public abstract boolean isLogined();

    public abstract String getLoginId();

    public abstract byte[] getFavicon(Subscription sub)
        throws IOException, ReaderException;

    public abstract void handleSubList(SubListHandler handler, long syncTime)
        throws IOException, ReaderException;

    public abstract void handleTagList(TagListHandler handler, long syncTime)
        throws IOException, ReaderException;

    public abstract void handleUnreadCount(UnreadCountHandler handler,
        long syncTime) throws IOException, ReaderException;

    public abstract void handleItemList(ItemListHandler handler, long syncTime)
        throws IOException, ReaderException;

    public abstract boolean markAsRead(String itemUid)
        throws IOException, ReaderException;

    public abstract boolean markAllAsRead(String s, String t, long syncTime)
        throws IOException, ReaderException;

    public abstract boolean editItemTag(String itemUid, String tagUid,
        boolean add) throws IOException, ReaderException;

    protected HttpGet filterGet(HttpGet get) {
        return get;
    }

    public InputStream doGetInputStream(String url)
            throws IOException, ReaderException {
        Log.d(TAG, "[DEBUG] GET: " + url);
        HttpGet get = filterGet(new HttpGet(url));
        HttpResponse res = this.client.execute(get);
        int resStatus = res.getStatusLine().getStatusCode();
        if (resStatus != HttpStatus.SC_OK) {
            throw new ReaderException("invalid http status " + resStatus
                + ": " + url);
        }

        final HttpEntity entity = res.getEntity();
        if (entity == null) {
            throw new ReaderException("null response entity");
        }

        final File temp = File.createTempFile("get", ".txt",
            this.context.getCacheDir());
        temp.deleteOnExit();

        InputStream in = entity.getContent();
        try {
            FileOutputStream out = new FileOutputStream(temp);
            try {
                byte[] buff = new byte[1024];
                int len = in.read(buff);
                while (len != -1) {
                    out.write(buff, 0, len);
                    len = in.read(buff);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        entity.consumeContent();

        return new BufferedInputStream(new FileInputStream(temp) {
            public void close() throws IOException {
                super.close();
                if (temp.exists()) {
                    temp.delete();
                }
            }
        }, 1024);
    }

    protected HttpPost filterPost(HttpPost post) {
        return post;
    }

    public InputStream doPostInputStream(String url, List<NameValuePair> params)
            throws IOException, ReaderException {
        // Log.d(TAG, "[DEBUG] POST: " + url);
        // Log.d(TAG, "[DEBUG] PARAMS: " + params);
        HttpPost post = filterPost(new HttpPost(url));
        post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

        HttpResponse res = this.client.execute(post);
        int resStatus = res.getStatusLine().getStatusCode();
        if (resStatus != HttpStatus.SC_OK) {
            throw new ReaderException("invalid http status " + resStatus
                + ": " + url);
        }

        final HttpEntity entity = res.getEntity();
        if (entity == null) {
            throw new ReaderException("null response entity");
        }

        return new FilterInputStream(entity.getContent()) {
            public void close() throws IOException {
                super.close();
                // entity.consumeContent();
            }
        };
    }

    public java.io.Reader doGetReader(String url)
            throws IOException, ReaderException {
        InputStream in = doGetInputStream(url);
        if (in == null) {
            return null;
        }
        return new InputStreamReader(in, HTTP.UTF_8);
    }

    public java.io.Reader doPostReader(String url, List<NameValuePair> params)
            throws IOException, ReaderException {
        return new InputStreamReader(doPostInputStream(url, params), HTTP.UTF_8);
    }

    public static interface SubListHandler {
        boolean subscription(Subscription sub) throws ReaderException;
    }

    public static interface TagListHandler {
        boolean tag(Tag tag) throws ReaderException;
    }

    public static interface UnreadCountHandler {
        boolean unreadCount(String uid, int count, long newestItemTime)
            throws ReaderException;
    }

    public static interface ItemListHandler {
        String getSubUid();
        boolean isStateRead();
        int getLimit();
        boolean isNewer();
        long getStartTime();
        boolean isExcludeRead();
        boolean item(Item item) throws ReaderException;
    }

    public static class JsonHandlerAdapter implements ContentHandler {

        private Stack<String> keys;
        private Throwable error;

        protected String getCurrentKey() {
            if (this.keys == null || this.keys.size() == 0) {
                return null;
            }
            return this.keys.peek();
        }

        protected void setError(Throwable error) {
            this.error = error;
        }

        public Throwable getError() {
            return this.error;
        }

        public void startJSON() throws ParseException, IOException {
            this.keys = new Stack<String>();
        }

        public void endJSON() throws ParseException, IOException {
            this.keys = null;
        }

        public boolean startObject() throws ParseException, IOException {
            return true;
        }

        public boolean endObject() throws ParseException, IOException {
            return true;
        }

        public boolean startObjectEntry(String key)
                throws ParseException, IOException {
            String curKey = getCurrentKey();
            if (curKey != null) {
                key = curKey + "/" + key;
            }
            this.keys.push(key);
            return true;
        }

        public boolean endObjectEntry() throws ParseException, IOException {
            this.keys.pop();
            return true;
        }
 
        public boolean startArray() throws ParseException, IOException {
            return true;
        }

        public boolean endArray() throws ParseException, IOException {
            return true;
        }

        public boolean primitive(Object value)
                throws ParseException, IOException {
            return true;
        }
    }
}
