package ro.ase.ie.g1097.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import ro.ase.ie.g1097.R;
import ro.ase.ie.g1097.model.Movie;

public class ItemActivity extends AppCompatActivity {

    private EditText etRelease;
    private int position = -1;   // -1 = insert mode
    private Calendar releaseCal = Calendar.getInstance();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initControls();
        initListeners();

        Intent intent = getIntent();
        Movie existing = IntentCompat.getSerializableExtra(intent, MainActivity.MOVIE_EXTRA, Movie.class);
        if (existing != null) {
            position = intent.getIntExtra(MainActivity.POSITION_EXTRA, -1);
            //fillForm(existing);
            setTitle("Edit movie");
        }
    }

    private void initControls() {

        etRelease = findViewById(R.id.etRelease);
    }

    private void initListeners() {
        etRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReleaseDatePicker();
            }
        });
    }

    private void showReleaseDatePicker() {
        String current = etRelease.getText().toString();
        if (!current.isEmpty()) {
            try {
                Date parsed = dateFormat.parse(current);
                if (parsed != null)
                    releaseCal.setTime(parsed);
            } catch (ParseException ignored) { }
        }

        new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        releaseCal.set(year, month, day);
                        etRelease.setError(null);
                        etRelease.setText(dateFormat.format(releaseCal.getTime()));
                    }
                },
                releaseCal.get(Calendar.YEAR),
                releaseCal.get(Calendar.MONTH),
                releaseCal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}