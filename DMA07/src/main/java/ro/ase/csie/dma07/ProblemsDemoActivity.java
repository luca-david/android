package ro.ase.csie.dma07;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The four "problems with raw threads", each as a BROKEN vs FIXED pair.
 * Watch Logcat (tag "DMA07") alongside the on-screen log.
 */
public class ProblemsDemoActivity extends AppCompatActivity {

    private static final String TAG = "DMA07";

    // Gives each Activity instance a visible id, so rotation (= new instance) is obvious.
    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);
    private final int instanceId = INSTANCE_COUNT.incrementAndGet();

    private TextView tvLog;
    private TextView tvRotationResult;

    // --- shared handles for the demos ---
    private final ExecutorService pool = Executors.newFixedThreadPool(4);   // P1 fixed
    private volatile boolean fixedWorkerRunning = false;                    // P2 fixed flag
    private boolean brokenWorkerRunning = false;                           // P2 broken (uncancellable)
    private static final Handler LEAK_HANDLER = new Handler(Looper.getMainLooper()); // P3
    private ProblemsViewModel viewModel;                                   // P4 fixed

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_problems_demo);

        tvLog = findViewById(R.id.tvLog);
        tvRotationResult = findViewById(R.id.tvRotationResult);
        log("Activity instance #" + instanceId + " created");

        viewModel = new ViewModelProvider(this).get(ProblemsViewModel.class);
        // FIXED rotation: observe the ViewModel; result shows even after rotating.
        viewModel.getResult().observe(this, r ->
                tvRotationResult.setText(r + "  (seen by instance #" + instanceId + ")"));

        // ---------- P1: Unbounded creation ----------
        b(R.id.btnP1Broken, () -> {
            log("P1 BROKEN: spawning 200 raw threads…");
            for (int i = 0; i < 200; i++) {
                new Thread(() -> {
                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                }).start();
            }
            log("  active threads now ≈ " + Thread.activeCount() + " (expensive!)");
        });
        b(R.id.btnP1Fixed, () -> {
            log("P1 FIXED: 200 tasks on a pool of 4 threads…");
            for (int i = 0; i < 200; i++) {
                pool.execute(() -> {
                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                });
            }
            log("  active threads now ≈ " + Thread.activeCount() + " (bounded)");
        });

        // ---------- P2: Hard to cancel ----------
        b(R.id.btnP2StartBroken, () -> {
            brokenWorkerRunning = true;
            new Thread(() -> {
                int n = 0;
                while (brokenWorkerRunning) {   // no interrupt check; see "stop broken"
                    Log.d(TAG, "P2 broken worker tick " + (++n));
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }).start();
            log("P2 BROKEN worker started (watch Logcat). 'Stop broken' barely helps.");
        });
        b(R.id.btnP2StopBroken, () -> {
            // We DO have a flag here, but imagine a thread with no such hook:
            // Thread.stop() is deprecated/unsafe and there'd be no clean way out.
            brokenWorkerRunning = false;
            log("P2 tried to stop broken worker (only worked because we left a flag).");
        });
        b(R.id.btnP2StartFixed, () -> {
            fixedWorkerRunning = true;
            new Thread(() -> {
                int n = 0;
                while (fixedWorkerRunning && !Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "P2 fixed worker tick " + (++n));
                    try { Thread.sleep(1000); }
                    catch (InterruptedException e) { break; }   // cooperative cancellation
                }
                Log.d(TAG, "P2 fixed worker stopped cleanly");
            }).start();
            log("P2 FIXED worker started (cooperatively cancellable).");
        });
        b(R.id.btnP2StopFixed, () -> {
            fixedWorkerRunning = false;
            log("P2 FIXED worker asked to stop — it exits its loop cleanly.");
        });

        // ---------- P3: Memory leak (lambda holds the Activity) ----------
        b(R.id.btnP3Broken, () -> {
            // Anonymous Runnable captures THIS Activity; a static Handler keeps it
            // for 60s. Rotate now and the OLD Activity can't be GC'd -> leak.
            LEAK_HANDLER.postDelayed(new Runnable() {
                @Override public void run() {
                    // Implicit reference to ProblemsDemoActivity.this
                    log("P3 broken callback fired on instance #" + instanceId);
                }
            }, 60_000);
            log("P3 BROKEN: pending 60s callback holds this Activity. "
                    + "Rotate, then check the Profiler for a leaked instance.");
        });
        b(R.id.btnP3Fixed, () -> {
            // Static Runnable + WeakReference: no strong hold on the Activity.
            LEAK_HANDLER.postDelayed(new SafeCallback(this), 60_000);
            log("P3 FIXED: WeakReference callback does not pin the Activity.");
        });

        // ---------- P4: Result for a dead Activity (rotation) ----------
        b(R.id.btnP4Broken, () -> {
            log("P4 BROKEN: raw thread will update THIS instance (#" + instanceId
                    + ") in 4s. Rotate now — result is lost to the old instance.");
            new Thread(() -> {
                try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
                String now = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
                // Captures the OLD Activity's view; after rotation this is the dead one.
                runOnUiThread(() -> tvRotationResult.setText(
                        "BROKEN result for instance #" + instanceId + " at " + now));
            }).start();
        });
        b(R.id.btnP4Fixed, () -> {
            log("P4 FIXED: ViewModel computes; rotate during the 4s — result still appears.");
            viewModel.compute();
        });
    }

    /** Static class + WeakReference = the fix for P3. */
    private static class SafeCallback implements Runnable {
        private final WeakReference<ProblemsDemoActivity> ref;
        SafeCallback(ProblemsDemoActivity a) { ref = new WeakReference<>(a); }
        @Override public void run() {
            ProblemsDemoActivity a = ref.get();
            if (a != null) a.log("P3 fixed callback fired (Activity still alive).");
            else Log.d(TAG, "P3 fixed callback fired; Activity already gone — no leak.");
        }
    }

    private void b(int id, Runnable action) {
        ((Button) findViewById(id)).setOnClickListener(v -> action.run());
    }

    private void log(String line) {
        Log.d(TAG, line);
        tvLog.append(line + "\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pool.shutdownNow();
        // Removing pending callbacks here is also part of the P3 fix.
        LEAK_HANDLER.removeCallbacksAndMessages(null);
        log("instance #" + instanceId + " destroyed");
    }
}
