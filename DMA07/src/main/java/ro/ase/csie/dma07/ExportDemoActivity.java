package ro.ase.csie.dma07;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Example 4 — write a file in the background with an ExecutorService, then
 * report the result (file path) back on the UI thread via a Handler.
 */
public class ExportDemoActivity extends AppCompatActivity {

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_export_demo);

        TextView tvStatus = findViewById(R.id.tvStatus);
        Button btnExport = findViewById(R.id.btnExport);

        btnExport.setOnClickListener(v -> {
            btnExport.setEnabled(false);
            tvStatus.setText("Exporting…");

            io.execute(() -> {
                try {
                    String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                            .format(new Date());
                    File out = new File(getExternalFilesDir(null), "export_" + stamp + ".txt");
                    try (FileWriter w = new FileWriter(out)) {
                        w.write("DMA07 export demo\n");
                        for (int i = 1; i <= 20; i++) w.write("Line " + i + "\n");
                    }
                    final String path = out.getAbsolutePath();
                    main.post(() -> {
                        tvStatus.setText("Saved to:\n" + path);
                        Toast.makeText(this, "Export done", Toast.LENGTH_SHORT).show();
                        btnExport.setEnabled(true);
                    });
                } catch (IOException e) {
                    main.post(() -> {
                        tvStatus.setText("Failed: " + e.getMessage());
                        btnExport.setEnabled(true);
                    });
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }
}
