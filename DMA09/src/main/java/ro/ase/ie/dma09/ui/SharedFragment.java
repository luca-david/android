package ro.ase.ie.dma09.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import ro.ase.ie.dma09.data.FileStore;
import ro.ase.ie.dma09.databinding.FragmentSharedBinding;
import ro.ase.ie.dma09.util.Logs;

/**
 * Slides 27-30: the system file picker (SAF), res/raw + assets and FileProvider.
 * No storage permission is requested anywhere in this app.
 */
public class SharedFragment extends Fragment {

    private static final String SAMPLE = "Hello from DMA09";

    private FragmentSharedBinding b;
    private FileStore files;

    // Launchers must be created before the fragment is STARTED, so they are fields.
    private final ActivityResultLauncher<String> createDoc = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/plain"), this::onDocumentCreated);

    private final ActivityResultLauncher<String[]> openDoc = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), this::onDocumentOpened);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentSharedBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        files = new FileStore(requireContext());

        b.btnCreateDoc.setOnClickListener(v -> {
            Logs.d(Logs.SAF, "launching the system 'create document' screen");
            createDoc.launch("dma09_note.txt");
        });

        b.btnOpenDoc.setOnClickListener(v -> {
            Logs.d(Logs.SAF, "launching the system 'open document' screen");
            openDoc.launch(new String[]{"text/plain"});
        });

        b.btnRaw.setOnClickListener(v -> {
            try {
                show("res/raw (inside the APK, read-only, by id)", files.readRaw());
            } catch (IOException e) {
                fail("read raw", e);
            }
        });

        b.btnAssets.setOnClickListener(v -> {
            try {
                show("assets (inside the APK, read-only, by name)", files.readAsset());
            } catch (IOException e) {
                fail("read asset", e);
            }
        });

        b.btnShare.setOnClickListener(v -> shareNote());
    }

    // ---- SAF: the user chose a place, we receive a content:// Uri ----

    private void onDocumentCreated(@Nullable Uri uri) {
        if (uri == null) {
            Logs.i(Logs.SAF, "create document: user cancelled");
            return;
        }
        Logs.i(Logs.SAF, "create document -> " + uri);
        ContentResolver resolver = requireContext().getContentResolver();
        try (OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) {
                throw new IOException("openOutputStream returned null");
            }
            out.write(files.readOrDefault(SAMPLE).getBytes(StandardCharsets.UTF_8));
            show("Shared storage via the file picker (SAF)", "Saved to:\n" + uri);
        } catch (IOException e) {
            fail("write document", e);
        }
    }

    private void onDocumentOpened(@Nullable Uri uri) {
        if (uri == null) {
            Logs.i(Logs.SAF, "open document: user cancelled");
            return;
        }
        Logs.i(Logs.SAF, "open document -> " + uri);
        ContentResolver resolver = requireContext().getContentResolver();
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) {
                throw new IOException("openInputStream returned null");
            }
            String text = FileStore.readAll(in);
            Logs.d(Logs.SAF, "read " + text.length() + " chars from the chosen file");
            show("Shared storage via the file picker (SAF)", text);
        } catch (IOException e) {
            fail("read document", e);
        }
    }

    // ---- FileProvider: give another app ONE file ----

    private void shareNote() {
        try {
            File file = files.prepareShareFile(files.readOrDefault(SAMPLE));
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", file);
            Logs.i(Logs.SAF, "sharing " + file + " as " + uri);

            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_STREAM, uri);
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);   // temporary read access
            startActivity(Intent.createChooser(send, "Share note"));
            show("FileProvider (cache/shared/ shared as a content:// Uri)", "Sharing:\n" + uri);
        } catch (IOException e) {
            fail("share", e);
        }
    }

    private void show(String storage, String text) {
        b.tvStorage.setText("Storage used: " + storage);
        b.tvOutput.setText(text);
    }

    private void fail(String action, IOException e) {
        Logs.e(Logs.SAF, action + " failed", e);
        show("(error)", action + " failed: " + e.getMessage());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
