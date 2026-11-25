package ro.ase.ie.dma09.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import ro.ase.ie.dma09.R;
import ro.ase.ie.dma09.data.PrefsManager;
import ro.ase.ie.dma09.databinding.ActivitySettingsBinding;
import ro.ase.ie.dma09.util.Logs;

/** "A Settings screen for free": the UI comes from res/xml/preferences.xml. */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setSupportActionBar(b.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settingsContainer, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // By default this screen would use the app's DEFAULT SharedPreferences file.
            // We point it to our own file instead, so the Preferences tab and this
            // screen read and write the very same settings.xml.
            getPreferenceManager().setSharedPreferencesName(PrefsManager.FILE);
            setPreferencesFromResource(R.xml.preferences, rootKey);
            Logs.d(Logs.PREFS, "Settings screen opened, bound to file " + PrefsManager.FILE + ".xml");
        }
    }
}
