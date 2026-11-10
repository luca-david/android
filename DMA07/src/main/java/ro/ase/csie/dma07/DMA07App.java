package ro.ase.csie.dma07;

import android.app.Application;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/** Schedules the periodic "sync" worker once at app start. */
public class DMA07App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PeriodicWorkRequest sync =
                new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "periodic-sync", ExistingPeriodicWorkPolicy.KEEP, sync);
    }
}
