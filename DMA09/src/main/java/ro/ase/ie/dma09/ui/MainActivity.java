package ro.ase.ie.dma09.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import ro.ase.ie.dma09.R;
import ro.ase.ie.dma09.databinding.ActivityMainBinding;
import ro.ase.ie.dma09.util.Logs;

/** Three tabs: Preferences (slides 7-17), Files (18-26), Shared storage and resources (27-30). */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Logs.d(Logs.APP, "MainActivity.onCreate, savedInstanceState="
                + (savedInstanceState == null ? "null (first start)" : "restored"));

        b.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_prefs) {
                f = new PrefsFragment();
            } else if (id == R.id.nav_files) {
                f = new FilesFragment();
            } else {
                f = new SharedFragment();
            }
            Logs.d(Logs.APP, "tab selected: " + item.getTitle());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            b.bottomNav.setSelectedItemId(R.id.nav_prefs);
        }
    }
}
