package ro.ase.ie.dma09.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ro.ase.ie.dma09.data.FileStore;
import ro.ase.ie.dma09.databinding.FragmentFilesBinding;
import ro.ase.ie.dma09.util.Logs;

/** Slides 18-26: internal files, cache and external app-specific storage. */
public class FilesFragment extends Fragment {

    private FragmentFilesBinding b;
    private FileStore files;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentFilesBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        files = new FileStore(requireContext());
        String internal = "Internal storage (getFilesDir)";
        String cache = "Cache (getCacheDir)";

        b.btnSave.setOnClickListener(v -> {
            try {
                files.save(note());
                show(internal, "Saved (the old content was replaced).");
            } catch (IOException e) {
                fail("save", e);
            }
        });

        b.btnAppend.setOnClickListener(v -> {
            try {
                files.append(note());
                show(internal, "Appended a line.");
            } catch (IOException e) {
                fail("append", e);
            }
        });

        b.btnRead.setOnClickListener(v -> {
            try {
                show(internal, files.read());
            } catch (FileNotFoundException e) {
                Logs.w(Logs.FILES, "read: " + FileStore.NOTE + " does not exist yet");
                show(internal, "No note yet. Press Save first.");
            } catch (IOException e) {
                fail("read", e);
            }
        });

        b.btnList.setOnClickListener(v -> show(internal, files.list()));

        b.btnDelete.setOnClickListener(v -> show(internal,
                files.delete() ? "Note deleted." : "Nothing to delete."));

        b.btnPaths.setOnClickListener(v -> show("All locations", files.paths()));

        b.btnCacheWrite.setOnClickListener(v -> {
            try {
                File f = files.writeCache("Temporary data, can be recreated.\n");
                show(cache, "Written: " + f);
            } catch (IOException e) {
                fail("cache write", e);
            }
        });

        b.btnCacheSize.setOnClickListener(v ->
                show(cache, "Cache size: " + files.cacheSize() + " bytes"));

        b.btnCacheClear.setOnClickListener(v ->
                show(cache, files.clearCache() + " file(s) removed from the cache."));

        b.btnExport.setOnClickListener(v -> {
            try {
                File f = files.exportExternal(files.readOrDefault("Hello from DMA09"));
                show("External app-specific (getExternalFilesDir)",
                        "Exported to:\n" + f + "\n\nNo permission needed. Visible over USB, "
                                + "removed on uninstall.");
            } catch (IOException e) {
                fail("export", e);
            }
        });
    }

    private String note() {
        return b.etNote.getText().toString();
    }

    private void show(String storage, String text) {
        b.tvStorage.setText("Storage used: " + storage);
        b.tvOutput.setText(text);
    }

    private void fail(String action, IOException e) {
        Logs.e(Logs.FILES, action + " failed", e);
        show("(error)", action + " failed: " + e.getMessage());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
