package ro.ase.ie.dma09.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ro.ase.ie.dma09.util.Logs;

/**
 * Wrapper around SharedPreferences: the rest of the app never sees a key string.
 * (Slides: "A wrapper class", "Good and bad uses".)
 */
public class PrefsManager {

    /** File name: /data/data/ro.ase.ie.dma09/shared_prefs/settings.xml */
    public static final String FILE = "settings";

    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_SORT_ORDER = "sort_order";
    public static final String KEY_LAUNCH_COUNT = "launch_count";
    public static final String KEY_LAST_OPENED = "last_opened";
    public static final String KEY_PASSWORD = "password_BAD_IDEA";

    private final Context appContext;
    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        appContext = context.getApplicationContext();
        // Always MODE_PRIVATE: the world-readable modes are no longer allowed.
        prefs = appContext.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Needed to register / unregister change listeners. */
    public SharedPreferences raw() {
        return prefs;
    }

    // ---- user name ----

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    /** apply(): memory is updated at once, the disk write happens later in the background. */
    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
        Logs.d(Logs.PREFS, "apply() user_name=\"" + name + "\" (disk write is asynchronous)");
    }

    /** commit(): writes to disk now, on the calling thread, and tells us if it worked. */
    public boolean setUserNameNow(String name) {
        boolean ok = prefs.edit().putString(KEY_USER_NAME, name).commit();
        Logs.d(Logs.PREFS, "commit() user_name=\"" + name + "\" returned " + ok);
        return ok;
    }

    // ---- display ----

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean dark) {
        prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply();
    }

    public String getSortOrder() {
        // ListPreference stores a String, so we read a String with the same default
        return prefs.getString(KEY_SORT_ORDER, "date");
    }

    // ---- launch statistics ----

    public int incrementLaunchCount() {
        int n = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_LAUNCH_COUNT, n).apply();
        return n;
    }

    public int getLaunchCount() {
        return prefs.getInt(KEY_LAUNCH_COUNT, 0);
    }

    /** Returns the previous value (0 = never) and stores "now". */
    public long touchLastOpened() {
        long previous = prefs.getLong(KEY_LAST_OPENED, 0L);
        prefs.edit().putLong(KEY_LAST_OPENED, System.currentTimeMillis()).apply();
        return previous;
    }

    // ---- bad example ----

    /** DO NOT do this in a real app: SharedPreferences are plain text on disk. */
    public void saveInsecurePassword(String password) {
        prefs.edit().putString(KEY_PASSWORD, password).commit();
        Logs.w(Logs.PREFS, "SECURITY: a password was written in plain text to " + FILE + ".xml");
    }

    // ---- maintenance ----

    public void clear() {
        prefs.edit().clear().apply();
        Logs.i(Logs.PREFS, "all preferences cleared");
    }

    /**
     * Reads the real XML file from disk. Right after apply() the file may still be
     * the old version (the write is asynchronous); after commit() it is up to date.
     */
    public String readRawXml() {
        File file = new File(appContext.getDataDir(), "shared_prefs/" + FILE + ".xml");
        if (!file.exists()) {
            Logs.d(Logs.PREFS, "XML file does not exist yet: " + file);
            return "(file not written yet)\n" + file;
        }
        try (InputStream in = new FileInputStream(file)) {
            String xml = FileStore.readAll(in);
            Logs.d(Logs.PREFS, "read " + file + " (" + file.length() + " bytes)");
            return xml;
        } catch (IOException e) {
            Logs.e(Logs.PREFS, "cannot read " + file, e);
            return "Error: " + e.getMessage();
        }
    }
}
