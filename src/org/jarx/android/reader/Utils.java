package org.jarx.android.reader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

public class Utils {

    private static final String TAG = "Utils";
    private static final String ERR_REPORT_URL
        = "http://android.jarx.org/report/fastreader";

    private static final byte[] logErrorLock = new byte[0];

    public static int asInt(Object value) {
        return asInt(value, 0);
    }

    public static int asInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long asLong(Object value) {
        return asLong(value, 0);
    }

    public static long asLong(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String asString(Object value) {
        return asString(value, null);
    }

    public static String asString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return String.valueOf(value).trim();
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String stripWhitespaces(String value) {
        if (value == null || value.length() == 0) {
            return value;
        }
        return value.replaceAll("[\\s\u3000]+", " ").trim();
    }

    public static String htmlAsPlainText(String value) {
        if (value == null || value.length() == 0) {
            return value;
        }
        value = value.replaceAll("\\s+", " ");
        value = value.replaceAll("<br\\s?/?>", "\n");
        value = value.replaceAll("<.*?>", " ");

        // NOTE: some html entities
        value = value.replaceAll("&lt;", "<");
        value = value.replaceAll("&gt;", ">");
        value = value.replaceAll("&quot;", "\"");
        value = value.replaceAll("&apos;", "\'");
        value = value.replaceAll("&nbsp;", " ");
        value = value.replaceAll("&amp;", "&");

        value = value.replaceAll("  +", " ");
        return value;
    }

    public static String htmlEscape(String value) {
        if (value == null || value.length() == 0) {
            return value;
        }
        value = value.replaceAll("&", "&amp;");
        value = value.replaceAll("<", "&lt;");
        value = value.replaceAll(">", "&gt;");
        value = value.replaceAll("\"", "&quot;");
        return value;
    }

    public static String formatTimeAgo(long time) {
        long diff = (System.currentTimeMillis() / 1000) - time;
        if (diff < (7 * 24 * 60 * 60)) {
            if (diff < (60 * 60)) {
                return (diff / 60) + " min ago";
            } else if (diff < (24 * 60 * 60)) {
                return (diff / 60 / 60) + " hours ago";
            } else {
                return (diff / 24 / 60 / 60) + " days ago";
            }
        }
        return DateFormat.getDateInstance().format(new Date(time * 1000));
    }

    public static String getVersionName(Context c) {
        try {
            PackageInfo info = c.getPackageManager().getPackageInfo(
                c.getPackageName(), 0);
            return info.versionName;
        } catch (NameNotFoundException e) {
            return "version error: " + e;
        }
    }

    public static File getErrorReportFile(Context c) {
        return new File(c.getCacheDir(), "err-report.txt");
    }

    public static File getErrorFile(Context c) {
        return new File(c.getCacheDir(), "err.txt");
    }

    public static void logError(Context c, String report) {
        if (!Prefs.isEnableErrReporting(c)) {
            return;
        }
        File f = getErrorFile(c);
        synchronized (logErrorLock) {
            PrintWriter out = null;
            try {
                out = new PrintWriter(new FileWriter(f, true));
                out.println(report);
                out.flush();
            } catch (IOException ie) {
                ie.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    public static void logError(Context c, Throwable e) {
        if (!Prefs.isEnableErrReporting(c)) {
            return;
        }
        File f = getErrorFile(c);
        String v = getVersionName(c);
        logError(f, v, e);
    }

    private static void logError(File f, String v, Throwable e) {
        synchronized (logErrorLock) {
            PrintWriter out = null;
            try {
                out = new PrintWriter(new FileWriter(f, true));
                out.println("[ERROR]");
                out.print("DATE: ");
                    out.println(new java.util.Date());
                out.print("DEVICE: ");
                    out.println(Build.DEVICE);
                out.print("MODEL: ");
                    out.println(Build.MODEL);
                out.print("SDK: ");
                    out.println(Build.VERSION.SDK);
                out.print("VERSION: ");
                    out.println(v);
                out.flush();
                if (e != null) {
                    e.printStackTrace(out);
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    public static void handleUncatchException(Context c) {
        File f = getErrorFile(c);
        String v = getVersionName(c);
        Thread.setDefaultUncaughtExceptionHandler(new ErrorReportHandler(f, v));
    }

    private static class ErrorReportHandler
            implements Thread.UncaughtExceptionHandler {

        private final File file;
        private final String versionName;

        public ErrorReportHandler(File file, String versionName) {
            this.file = file;
            this.versionName = versionName;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logError(this.file, this.versionName, e);
        }
    }

    public static DefaultHttpClient createHttpClient() {
        HttpParams config = new BasicHttpParams();
        HttpProtocolParams.setVersion(config, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(config, HTTP.UTF_8);
        HttpProtocolParams.setUserAgent(config, Utils.class.getName());

        final SchemeRegistry reg = new SchemeRegistry();
        reg.register(new Scheme("http",
            PlainSocketFactory.getSocketFactory(), 80));
        reg.register(new Scheme("https",
            SSLSocketFactory.getSocketFactory(), 443));

        final ThreadSafeClientConnManager manager
            = new ThreadSafeClientConnManager(config, reg);

        DefaultHttpClient client = new DefaultHttpClient(manager, config);
        client.getParams().setParameter("http.socket.timeout", 30 * 1000);
        return client;
    }

    public static String readFile(File file) throws IOException {
        FileReader in = new FileReader(file);
        try {
            StringBuilder buff = new StringBuilder(1024);
            char[] c = new char[1024];
            int len = in.read(c);
            while (len != -1) {
                buff.append(c, 0, len);
                len = in.read(c);
            }
            return new String(buff);
        } finally {
            in.close();
        }
    }

    public static boolean sendErrorReport(File file) throws IOException {
        DefaultHttpClient client = createHttpClient();

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(1);
        params.add(new BasicNameValuePair("report", readFile(file)));
        HttpPost post = new HttpPost(ERR_REPORT_URL);
        post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

        HttpResponse res = client.execute(post);
        int status = res.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            Log.d(TAG, "[ERROR] sendErrorReport: status " + status);
            return false;
        }
        return true;
    }

    private Utils() {
    }
}
