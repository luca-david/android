package ro.ase.ie.g1098.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.NotImplementedError;
import ro.ase.ie.g1098.model.Movie;

/**
 * Everything about the network lives here: the background thread, the HTTP request
 * and the JSON parsing. The activity only calls {@link #loadMovies(MoviesCallback)}
 * and receives the result on the main thread.
 */
public class NetworkService {

    // The JSON file published in the course repository
    private static final String MOVIES_URL =
            "https://raw.githubusercontent.com/mihaidoinea/DMA2026/main/data/movies.json";
    private static final int TIMEOUT_MS = 10_000;

    /** Exactly one method is called, always on the main thread, so it can update the views. */
    public interface MoviesCallback {
        void onSuccess(List<Movie> movies);

        void onError(String message);
    }

    // Network work must not run on the main thread: one background thread is enough
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // Posts results back to the main thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ---------- Public API ----------

    public void loadMovies(MoviesCallback callback) {

    }

    /** Call from the activity's onDestroy: stops a running download and drops undelivered results. */
    public void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    // ---------- HTTP ----------

    /** Reads the whole response body as text. Runs on the background thread. */
    private String downloadText(String address) throws IOException {
        throw new NotImplementedError();
    }

    // ---------- JSON ----------

    /** Turns the JSON array into Movie objects. The keys match the Movie fields. */
    private List<Movie> parseMovies(String json) throws JSONException, ParseException {
       throw new NotImplementedError();
    }
}