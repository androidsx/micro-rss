package org.jarx.android.reader;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;
import static org.jarx.android.reader.Utils.*; 

public class GoogleReaderClient extends ReaderClient {

    public static final String STATE_READING_LIST
        = "user/-/state/com.google/reading-list";
    public static final String STATE_READ = "user/-/state/com.google/read";

    public static final String URL_LOGIN
        = "https://www.google.com/accounts/ClientLogin";
    public static final String URL_BASE_
        = "http://www.google.com/reader";
    public static final String URL_API_
        = URL_BASE_ + "/api/0";
    public static final String URL_API_TOKEN
        = URL_API_ + "/token";
    public static final String URL_API_SUB_LIST
        = URL_API_ + "/subscription/list";
    public static final String URL_API_TAG_LIST
        = URL_API_ + "/tag/list";
    public static final String URL_API_UNREAD_COUNT
        = URL_API_ + "/unread-count";
    public static final String URL_API_STREAM_CONTENTS
        = URL_API_ + "/stream/contents";
    public static final String URL_API_EDIT_TAG
        = URL_API_ + "/edit-tag?client=scroll";
    public static final String URL_API_MARK_ALL_AS_READ
        = URL_API_ + "/mark-all-as-read?client=scroll";

    private static final String TAG = "GoogleReaderClient";
    private static final long TOKEN_TIME = 15 * 60 * 1000;

    private String loginId;
    private String password;
    private String auth;
    private BasicHeader authHeader;
    private String token;
    private long tokenExpiredTime;

    public GoogleReaderClient(Context c) {
        super(c);
    }

    @Override
    public boolean login() throws IOException, ReaderException {
        String loginId = Prefs.getGoogleId(super.context);
        String password = Prefs.getGooglePasswd(super.context);
        return login(loginId, password);
    }

    @Override
    public boolean login(String loginId, String password)
            throws IOException, ReaderException {
        logout();
        this.loginId = loginId;
        this.password = password;
        initAuth();
        return isLogined();
    }

    private String initAuth() throws IOException, ReaderException {
        if (this.loginId == null || this.password == null) {
            throw new IllegalStateException("no login info");
        }
        if (this.auth != null) {
            return this.auth;
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("accountType", "GOOGLE"));
        params.add(new BasicNameValuePair("Email", this.loginId));
        params.add(new BasicNameValuePair("Passwd", this.password));
        params.add(new BasicNameValuePair("service", "reader"));
        params.add(new BasicNameValuePair("source", "trycatchjp-fastreaderclient-1.0"));

        BufferedReader in = new BufferedReader(doPostReader(URL_LOGIN, params));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.indexOf("Auth=") == 0) {
                    this.auth = line.substring("Auth=".length());
                    break;
                }
            }
            if (this.auth == null) {
                throw new ReaderException("login failure");
            }
        } finally {
            in.close();
        }

        this.authHeader = new BasicHeader("Authorization", "GoogleLogin auth=" + this.auth);

        return this.auth;
    }

    private String initToken() throws IOException, ReaderException {
        initAuth();
        long now = System.currentTimeMillis();
        if (this.token != null && now < this.tokenExpiredTime) {
            return this.token;
        }
        Reader in = doGetReader(URL_API_TOKEN);
        try {
            char[] cbuf = new char[64];
            int len = in.read(cbuf);
            if (len != 57) {
                Log.w(TAG, "unknown token length " + len + ", "
                    + new String(cbuf, 0, len));
                //
                // throw new ReaderException("invalid token length " + len
                // + ", " + new String(cbuf, 0, len));
            }
            this.token = new String(cbuf, 0, len);
            this.tokenExpiredTime = now + TOKEN_TIME;
        } finally {
            in.close();
        }
        return this.token;
    }

    @Override
    protected HttpGet filterGet(HttpGet get) {
        get.addHeader(this.authHeader);
        return get;
    }

    @Override
    protected HttpPost filterPost(HttpPost post) {
        post.addHeader(this.authHeader);
        return post;
    }

    @Override
    public void logout() {
        this.auth = null;
        this.token = null;
        this.client.getCookieStore().clear();
    }

    @Override
    public boolean isLogined() {
        return (this.auth != null);
    }

    @Override
    public String getLoginId() {
        return this.loginId;
    }

    @Override
    public byte[] getFavicon(Subscription sub)
            throws IOException, ReaderException {
        URL url = new URL(sub.getHtmlUrl());
        StringBuilder buff = new StringBuilder(128);
        buff.append("http://s2.googleusercontent.com/s2/favicons");
        buff.append("?alt=feed&domain=");
        buff.append(url.getHost());
        Bitmap icon = null;
        InputStream in = doGetInputStream(new String(buff));
        try {
            icon = BitmapFactory.decodeStream(in);
        } finally {
            in.close();
        }
        int size = icon.getWidth() * icon.getHeight() * 2;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        icon.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();
        return out.toByteArray();
    }

    @Override
    public void handleSubList(SubListHandler handler, long syncTime)
            throws IOException, ReaderException {
        Reader in = readSubList(syncTime);
        try {
            new JSONParser().parse(in ,new JsonSubListHandler(handler));
        } catch (ParseException e) {
            Utils.logError(super.context, e);
            throw new ReaderException("json parse error", e);
        } finally {
            in.close();
        }
    }

    @Override
    public void handleTagList(TagListHandler handler, long syncTime)
            throws IOException, ReaderException {
        Reader in = readTagList(syncTime);
        try {
            new JSONParser().parse(in, new JsonTagListHandler(handler));
        } catch (ParseException e) {
            Utils.logError(super.context, e);
            throw new ReaderException("json parse error", e);
        } finally {
            in.close();
        }
    }

    @Override
    public void handleUnreadCount(UnreadCountHandler handler, long syncTime)
            throws IOException, ReaderException {
        Reader in = readUnreadCount(syncTime);
        try {
            new JSONParser().parse(in, new JsonUnreadCountHandler(handler));
        } catch (ParseException e) {
            Utils.logError(super.context, e);
            throw new ReaderException("json parse error", e);
        } finally {
            in.close();
        }
    }

    @Override
    public void handleItemList(ItemListHandler handler, long syncTime)
            throws IOException, ReaderException {
        Reader in = readStreamContents(syncTime, handler);
        try {
            new JSONParser().parse(in, new JsonItemListHandler(handler));
        } catch (ParseException e) {
            Utils.logError(super.context, e);
            throw new ReaderException("json parse error", e);
        } finally {
            in.close();
        }
    }

    @Override
    public boolean markAsRead(String itemUid)
            throws IOException, ReaderException {
        return editItemTag(itemUid, STATE_READ, true);
    }

    @Override
    public boolean markAllAsRead(String s, String t, long syncTime)
            throws IOException, ReaderException {
        if (s == null) {
            s = STATE_READING_LIST;
            t = "all";
        }
        String token = initToken();
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair("T", token));
        params.add(new BasicNameValuePair("s", s));
        params.add(new BasicNameValuePair("t", t));
        params.add(new BasicNameValuePair("ts", syncTime + "999"));

        Reader in = doPostReader(URL_API_MARK_ALL_AS_READ, params);
        try {
            char[] cbuf = new char[128];
            int len = in.read(cbuf);
            if (len != -1) {
                String res = new String(cbuf, 0, len);
                return res.equals("OK");
            }
        } finally {
            in.close();
        }
        return false;
    }

    @Override
    public boolean editItemTag(String itemUid, String tagUid, boolean add)
            throws IOException, ReaderException {
        String token = initToken();
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair("T", token));
        if (add) {
            params.add(new BasicNameValuePair("a", tagUid));
        } else {
            params.add(new BasicNameValuePair("r", tagUid));
        }
        params.add(new BasicNameValuePair("async", "true"));
        params.add(new BasicNameValuePair("i", itemUid));
        params.add(new BasicNameValuePair("pos", "0"));

        Reader in = doPostReader(URL_API_EDIT_TAG, params);
        try {
            char[] cbuf = new char[128];
            int len = in.read(cbuf);
            if (len != -1) {
                String res = new String(cbuf, 0, len);
                return res.equals("OK");
            }
        } finally {
            in.close();
        }
        return false;
    }

    // NOTE: /api/0/subscription/list
    public Reader readSubList(long syncTime)
            throws IOException, ReaderException {
        initAuth();

        StringBuilder buff = new StringBuilder(URL_API_SUB_LIST.length() + 32);
        buff.append(URL_API_SUB_LIST);
        buff.append("?client=scroll&output=json&ck=").append(syncTime);

        return doGetReader(new String(buff));
    }

    // NOTE: /api/0/tag/list
    public Reader readTagList(long syncTime)
            throws IOException, ReaderException {
        initAuth();

        StringBuilder buff = new StringBuilder(URL_API_TAG_LIST.length() + 32);
        buff.append(URL_API_TAG_LIST);
        buff.append("?client=scroll&output=json&ck=").append(syncTime);

        return doGetReader(new String(buff));
    }

    // NOTE: /api/0/unread-count
    public Reader readUnreadCount(long syncTime)
            throws IOException, ReaderException {
        initAuth();

        StringBuilder buff = new StringBuilder(
            URL_API_UNREAD_COUNT.length() + 32);
        buff.append(URL_API_UNREAD_COUNT);
        buff.append("?client=scroll&output=json&ck=").append(syncTime);

        return doGetReader(new String(buff));
    }

    // NOTE: /api/0/stream/contents
    public Reader readStreamContents(long syncTime, ItemListHandler handler)
            throws IOException, ReaderException {
        initAuth();

        StringBuilder buff = new StringBuilder(
            URL_API_STREAM_CONTENTS.length() + 64);
        buff.append(URL_API_STREAM_CONTENTS);
        String subUid = handler.getSubUid();
        if (subUid != null) {
            buff.append("/");
            buff.append(URLEncoder.encode(subUid, "UTF-8"));
        }
        if (handler.isStateRead()) {
            buff.append("/");
            buff.append(URLEncoder.encode(STATE_READ, "UTF-8"));
        }
        buff.append("?client=scroll&output=json&ck=").append(syncTime);
        if (handler.isExcludeRead()) {
            buff.append("&xt=").append(STATE_READ);
        }
        long startTime = handler.getStartTime();
        if (startTime > 0) {
            buff.append("&ot=").append(startTime);
        }
        int limit = handler.getLimit();
        if (limit > 0) {
            buff.append("&n=").append(limit);
        }
        buff.append("&r=").append(handler.isNewer() ? "n": "o");

        return doGetReader(new String(buff));
    }

    private static class JsonSubListHandler extends JsonHandlerAdapter {

        private final SubListHandler handler;
        private Subscription sub;

        public JsonSubListHandler(SubListHandler handler) {
            this.handler = handler;
        }

        public boolean startObject() throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null) {
                return true;
            }
            if (key.equals("subscriptions")) {
                this.sub = new Subscription();
            }
            return true;
        }

        public boolean endObject() throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null) {
                return true;
            }
            if (key.equals("subscriptions") && this.sub != null) {
                try {
                    if (!this.handler.subscription(this.sub)) {
                        return false;
                    }
                } catch (ReaderException e) {
                    e.printStackTrace();
                    return false;
                }
                this.sub = null;
            }
            return true;
        }

        public boolean primitive(Object value)
                throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null || this.sub == null) {
                return true;
            }
            if (key.equals("subscriptions/id")) {
                this.sub.setUid(asString(value));
            } else if (key.equals("subscriptions/title")) {
                this.sub.setTitle(asString(value));
            } else if (key.equals("subscriptions/sortid")) {
                this.sub.setSortid(asString(value));
            } else if (key.equals("subscriptions/htmlUrl")) {
                this.sub.setHtmlUrl(asString(value));
            } else if (key.equals("subscriptions/categories/id")) {
                this.sub.addCategory(asString(value));
            }
            return true;
        }
    }

    private static class JsonTagListHandler extends JsonHandlerAdapter {

        private final TagListHandler handler;
        private Tag tag;

        public JsonTagListHandler(TagListHandler handler) {
            this.handler = handler;
        }

        public boolean startObject() throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null) {
                return true;
            }
            if (key.equals("tags")) {
                this.tag = new Tag();
            }
            return true;
        }

        public boolean endObject() throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null) {
                return true;
            }
            if (key.equals("tags") && this.tag != null
                    && this.tag.getType() != Tag.TYPE_UNKNOWN) {
                try {
                    if (!this.handler.tag(this.tag)) {
                        return false;
                    }
                } catch (ReaderException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    this.tag = null;
                }
            }
            return true;
        }

        public boolean primitive(Object value)
                throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null || this.tag == null) {
                return true;
            }
            if (key.equals("tags/id")) {
                String uid = asString(value);
                this.tag.setUid(uid);
                int i = uid.indexOf("/label/");
                if (i != -1) {
                    this.tag.setLabel(uid.substring(i + "/label/".length()));
                    // NOTE: @see ReaderManager#updateTagTypes
                    this.tag.setType(Tag.TYPE_TAG_LABEL);
                } else if (uid.endsWith("state/com.google/starred")) {
                    this.tag.setLabel("state/com.google/starred");
                    this.tag.setType(Tag.TYPE_TAG_STARRED);
                }
            } else if (key.equals("tags/sortid")) {
                this.tag.setSortid(asString(value));
            }
            return true;
        }
    }

    private static class JsonUnreadCountHandler extends JsonHandlerAdapter {

        private final UnreadCountHandler handler;
        private String uid;
        private int count;
        private long newestItemTime;

        public JsonUnreadCountHandler(UnreadCountHandler handler) {
            this.handler = handler;
        }

        public boolean endObject() throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null) {
                return true;
            }
            if (key.equals("unreadcounts")) {
                try {
                    if (!this.handler.unreadCount(this.uid, this.count,
                            this.newestItemTime)) {
                        return false;
                    }
                } catch (ReaderException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        public boolean primitive(Object value)
                throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null) {
                return true;
            }
            if (key.equals("unreadcounts/id")) {
                this.uid = asString(value);
            } else if (key.equals("unreadcounts/count")) {
                this.count = asInt(value);
            } else if (key.equals("unreadcounts/newestItemTimestampUsec")) {
                this.newestItemTime = (long) (asLong(value) / 1000000);
            }
            return true;
        }
    }

    private static class JsonItemListHandler extends JsonHandlerAdapter {

        private final ItemListHandler handler;
        private Item item;

        public JsonItemListHandler(ItemListHandler handler) {
            this.handler = handler;
        }

        public boolean startObject() throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null) {
                return true;
            }
            if (key.equals("items")) {
                this.item = new Item();
            }
            return true;
        }

        public boolean endObject() throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null) {
                return true;
            }
            if (key.equals("items") && this.item != null) {
                try {
                    if (!this.handler.item(this.item)) {
                        return false;
                    }
                } catch (ReaderException e) {
                    e.printStackTrace();
                    return false;
                }
                this.item = null;
            }
            return true;
        }

        public boolean primitive(Object value)
                throws ParseException, IOException {
            String key = getCurrentKey();
            if (key == null || this.item == null) {
                return true;
            }
            if (key.equals("items/id")) {
                this.item.setUid(asString(value));
            } else if (key.equals("items/title")) {
                this.item.setTitle(asString(value));
            } else if (key.equals("items/published")) {
                this.item.setPublishedTime(asLong(value));
            } else if (key.equals("items/updated")) {
                this.item.setUpdatedTime(asLong(value));
            } else if (key.equals("items/alternate/href")) {
                this.item.setLink(asString(value));
            } else if (key.equals("items/alternate/type")) {
                this.item.setContentType(asString(value));
            } else if (key.equals("items/content/content")) {
                this.item.setContent(asString(value));
            } else if (key.equals("items/summary/content")) {
                this.item.setContent(asString(value));
            } else if (key.equals("items/author")) {
                this.item.setAuthor(asString(value));
            } else if (key.equals("items/categories")) {
                String category = asString(value);
                if (category.endsWith("state/com.google/read")) {
                    this.item.setRead(true);
                } else if (category.endsWith("state/com.google/starred")
                        || category.indexOf("/label/") != -1) {
                    this.item.addCategory(category);
                }
            }
            return true;
        }
    }
}
