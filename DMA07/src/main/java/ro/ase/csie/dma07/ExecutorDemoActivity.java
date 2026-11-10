package ro.ase.csie.dma07;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Example 3 — ExecutorService (a reused thread pool) instead of "new Thread()"
 * per task. We submit 6 tasks to a pool of 3 threads: at most 3 run at once,
 * the rest queue and reuse the freed threads. Results are posted back to the UI
 * thread with a Handler.
 */
public class ExecutorDemoActivity extends AppCompatActivity {

    private final ExecutorService pool = Executors.newFixedThreadPool(3);
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_executor_demo);

        TextView tvLog = findViewById(R.id.tvLog);
        Button btnRun = findViewById(R.id.btnRun);

        btnRun.setOnClickListener(v -> {
            tvLog.setText("Submitting 6 tasks to a pool of 3 threads…\n");
            for (int i = 1; i <= 6; i++) {
                final int taskId = i;
                pool.execute(() -> {
                    String thread = Thread.currentThread().getName();
                    append(tvLog, "Task " + taskId + " start on " + thread);
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    append(tvLog, "Task " + taskId + " done");
                });
            }
        });
    }

    private void append(TextView tv, String line) {
        main.post(() -> tv.append(line + "\n"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pool.shutdownNow();   // stop the pool when the screen goes away
    }
}
