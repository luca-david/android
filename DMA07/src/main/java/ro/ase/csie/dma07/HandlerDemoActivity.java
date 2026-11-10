package ro.ase.csie.dma07;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Example 2 — Handler + Looper. A background thread sends messages to a Handler
 * bound to the MAIN Looper, so handleMessage() runs on the UI thread.
 *
 * Shows BOTH message styles:
 *   - post(Runnable): a block of code to run on the main thread
 *   - sendMessage(what): a tagged Message interpreted by handleMessage()
 */
public class HandlerDemoActivity extends AppCompatActivity {

    private static final int MSG_PROGRESS = 1;
    private static final int MSG_DONE = 2;

    private TextView tvStatus;

    // Handler on the MAIN looper; handleMessage runs on the UI thread.
    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS:
                    tvStatus.setText("Progress: " + msg.arg1 + "%");
                    break;
                case MSG_DONE:
                    tvStatus.setText("Finished: " + msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_handler_demo);

        tvStatus = findViewById(R.id.tvStatus);
        Button btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> new Thread(() -> {
            for (int i = 10; i <= 100; i += 10) {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}

                // Style A: a tagged Message handled by handleMessage()
                Message m = mainHandler.obtainMessage(MSG_PROGRESS);
                m.arg1 = i;
                mainHandler.sendMessage(m);
            }

            // Style B: post a Runnable to run on the main thread
            mainHandler.post(() -> tvStatus.setText("Almost there…"));

            Message done = mainHandler.obtainMessage(MSG_DONE, "all data loaded");
            mainHandler.sendMessage(done);
        }).start());
    }
}
