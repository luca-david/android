package ro.ase.csie.dma07;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

/**
 * Example 5 — WorkManager. The periodic 15-minute "sync" is scheduled in
 * DMA07App at startup (15 min is the OS minimum for periodic work). Since you
 * can't wait 15 minutes in class, this screen also fires a ONE-TIME copy of the
 * same worker on demand, and observes its state so you can watch it run.
 * Check Logcat (tag "DMA07") for the "sync @ ..." line.
 */
public class WorkManagerDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_workmanager_demo);

        TextView tvStatus = findViewById(R.id.tvStatus);
        Button btnRunNow = findViewById(R.id.btnRunNow);

        tvStatus.setText("Periodic sync is scheduled (every 15 min).\n"
                + "Tap below to run it once now and watch Logcat (tag DMA07).");

        btnRunNow.setOnClickListener(v -> {
            OneTimeWorkRequest once = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
            WorkManager wm = WorkManager.getInstance(this);
            wm.enqueue(once);

            // Observe this job's state and show it live.
            wm.getWorkInfoByIdLiveData(once.getId()).observe(this, info -> {
                if (info != null) {
                    WorkInfo.State state = info.getState();
                    tvStatus.setText("One-time sync state: " + state
                            + (state.isFinished() ? "\n(check Logcat for 'sync @ ...')" : ""));
                }
            });
        });
    }
}
