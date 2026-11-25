package ro.ase.ie.dma09;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import ro.ase.ie.dma09.data.FileStore;
import ro.ase.ie.dma09.data.PrefsManager;
import ro.ase.ie.dma09.util.Logs;

/**
 * Runs once per process. Kill the app from the Recents screen and reopen it:
 * the launch counter goes up, which proves the data survived.
 */
public class DmaApp extends Application {

    private long previousOpened;

    // A field on purpose: SharedPreferences keeps listeners in a weak map,
    // so a listener held only by a local variable could be garbage collected.
    private SharedPreferences.OnSharedPreferenceChangeListener appListener;

    @Override
    public void onCreate() {
        super.onCreate();

        PrefsManager prefs = new PrefsManager(this);
        previousOpened = prefs.touchLastOpened();
        int launches = prefs.incrementLaunchCount();
        Logs.i(Logs.APP, "process started, launch #" + launches
                + ", previous launch at " + previousOpened);

        applyNightMode(prefs.isDarkMode());

        appListener = (sp, key) -> {
            // key is null when the whole file was cleared (API 30+)
            Object value = key == null ? "<cleared>" : sp.getAll().get(key);
            Logs.d(Logs.PREFS, "changed: " + key + " = " + value);
            if (PrefsManager.KEY_DARK_MODE.equals(key)) {
                applyNightMode(sp.getBoolean(key, false));
            }
        };
        prefs.raw().registerOnSharedPreferenceChangeListener(appListener);

        Logs.d(Logs.APP, new FileStore(this).paths());
    }

    private void applyNightMode(boolean dark) {
        Logs.i(Logs.APP, "night mode -> " + (dark ? "YES" : "NO"));
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public long getPreviousOpened() {
        return previousOpened;
    }
}
