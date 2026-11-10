package ro.ase.csie.dma07;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Runs on a background thread, scheduled by WorkManager. Logs a "sync". */
public class SyncWorker extends Worker {
    public static final String TAG = "DMA07";

    public SyncWorker(@NonNull Context c, @NonNull WorkerParameters p) { super(c, p); }

    @NonNull
    @Override
    public Result doWork() {
        String now = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        Log.d(TAG, "sync @ " + now);
        return Result.success();
    }
}
