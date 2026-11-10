package ro.ase.csie.dma07;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/** Home menu — one button per async example. */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        open(R.id.btnThread,   ThreadDemoActivity.class);
        open(R.id.btnHandler,  HandlerDemoActivity.class);
        open(R.id.btnExecutor, ExecutorDemoActivity.class);
        open(R.id.btnExport,   ExportDemoActivity.class);
        open(R.id.btnWork,     WorkManagerDemoActivity.class);
        open(R.id.btnImage,    ImageLoadDemoActivity.class);
        open(R.id.btnProblems, ProblemsDemoActivity.class);
    }

    private void open(int buttonId, Class<?> target) {
        Button b = findViewById(buttonId);
        b.setOnClickListener(v -> startActivity(new Intent(this, target)));
    }
}
