package ro.ase.csie.dma07;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Example 1 — a raw Thread for heavy work, and Activity.runOnUiThread(...)
 * to hop results back to the UI thread.
 *
 * The "Tap me" counter proves the UI stays responsive while the worker runs:
 * if you ran the loop on the main thread instead, the whole screen would freeze.
 */
public class ThreadDemoActivity extends AppCompatActivity {

    private int taps = 0;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_thread_demo);

        TextView tvProgress = findViewById(R.id.tvProgress);
        TextView tvTaps = findViewById(R.id.tvTaps);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnTap = findViewById(R.id.btnTap);

        btnTap.setOnClickListener(v -> tvTaps.setText("Taps: " + (++taps)));

        btnStart.setOnClickListener(v -> {
            btnStart.setEnabled(false);
            tvProgress.setText("Working…");

            // Heavy work on a background thread.
            new Thread(() -> {
                long sum = 0;
                for (int i = 1; i <= 100; i++) {
                    for (int j = 0; j < 2_000_000; j++) sum += j;   // fake heavy work
                    final int percent = i;
                    // Can't touch views here — hop to the UI thread:
                    runOnUiThread(() -> tvProgress.setText("Working… " + percent + "%"));
                }
                final long result = sum;
                runOnUiThread(() -> {
                    tvProgress.setText("Done. sum=" + result);
                    btnStart.setEnabled(true);
                });
            }).start();
        });
    }
}
