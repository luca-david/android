package ro.ase.csie.dma07;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Holds the result of a background computation. Because a ViewModel survives a
 * configuration change (rotation), the result is delivered to whichever Activity
 * instance is alive when it finishes — never to a dead one.
 */
public class ProblemsViewModel extends ViewModel {

    private final MutableLiveData<String> result = new MutableLiveData<>();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public LiveData<String> getResult() { return result; }

    public void compute() {
        io.execute(() -> {
            try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
            String now = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
            result.postValue("FIXED result arrived at " + now);   // safe after rotation
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        io.shutdownNow();
    }
}
