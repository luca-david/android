package ro.ase.ie.dma09.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Date;

import ro.ase.ie.dma09.DmaApp;
import ro.ase.ie.dma09.data.PrefsManager;
import ro.ase.ie.dma09.databinding.FragmentPrefsBinding;
import ro.ase.ie.dma09.util.Logs;

/** Slides 7-17: SharedPreferences. */
public class PrefsFragment extends Fragment {

    private FragmentPrefsBinding b;
    private PrefsManager prefs;

    // Kept in a field: SharedPreferences holds listeners weakly.
    // Registered in onStart and removed in onStop, so it never outlives the screen.
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sp, key) -> {
        if (b == null) {
            return;
        }
        String text = key == null ? "everything was cleared" : key + " = " + sp.getAll().get(key);
        b.tvListener.setText("Listener: " + text);
        Logs.d(Logs.PREFS, "screen listener got: " + text);
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentPrefsBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefs = new PrefsManager(requireContext());

        b.btnApply.setOnClickListener(v -> {
            prefs.setUserName(b.etUserName.getText().toString());
            Toast.makeText(requireContext(), "apply(): memory updated, disk later",
                    Toast.LENGTH_SHORT).show();
        });

        b.btnCommit.setOnClickListener(v -> {
            boolean ok = prefs.setUserNameNow(b.etUserName.getText().toString());
            Toast.makeText(requireContext(), "commit() returned " + ok, Toast.LENGTH_SHORT).show();
        });

        b.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        b.btnXml.setOnClickListener(v -> b.tvXml.setText(prefs.readRawXml()));

        b.btnBadPassword.setOnClickListener(v -> {
            prefs.saveInsecurePassword("hunter2");
            b.tvXml.setText(prefs.readRawXml());   // the password is right there, in plain text
        });

        b.btnReset.setOnClickListener(v -> {
            prefs.clear();
            bindValues();
            b.tvXml.setText("");
        });

        bindValues();
    }

    /** Copies the saved values into the views. */
    private void bindValues() {
        long previous = ((DmaApp) requireActivity().getApplication()).getPreviousOpened();
        b.tvLaunch.setText("This app was started " + prefs.getLaunchCount() + " time(s)");
        b.tvPrevious.setText("Previous start: " + (previous == 0L
                ? "never"
                : DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date(previous))));

        b.etUserName.setText(prefs.getUserName());

        // Listener removed while we set the initial value, so it does not save it again
        b.switchDark.setOnCheckedChangeListener(null);
        b.switchDark.setChecked(prefs.isDarkMode());
        b.switchDark.setOnCheckedChangeListener((btn, checked) -> {
            Logs.i(Logs.PREFS, "dark mode switch -> " + checked);
            prefs.setDarkMode(checked);   // DmaApp's listener applies the theme
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        prefs.raw().registerOnSharedPreferenceChangeListener(listener);
        Logs.d(Logs.PREFS, "screen listener registered");
        bindValues();   // values may have changed in the Settings screen
    }

    @Override
    public void onStop() {
        super.onStop();
        prefs.raw().unregisterOnSharedPreferenceChangeListener(listener);
        Logs.d(Logs.PREFS, "screen listener unregistered");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
