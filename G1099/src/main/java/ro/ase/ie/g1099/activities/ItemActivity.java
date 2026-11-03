package ro.ase.ie.g1099.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ro.ase.ie.g1099.R;
import ro.ase.ie.g1099.model.Movie;

public class ItemActivity extends AppCompatActivity {

    private EditText etRelease;
    private EditText etTitle;
    private EditText etBudget;
    private EditText etPoster;
    private Spinner spGenre;
    private SeekBar sbDuration;
    private RatingBar rbRating;
    private MaterialSwitch swWatched;
    private CheckBox cbWatchlist;
    private RadioGroup rgApproval;
    private TextView tvDurationValue;
    private Button btMovieAction;

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
            fillForm(existing);
            setTitle("Edit movie");
            btMovieAction.setText("Update Movie");
        }
    }

    private void fillForm(Movie existing) {
        etTitle.setText(existing.getTitle());
        etBudget.setText(String.valueOf(existing.getBudget()));
        etRelease.setText(dateFormat.format(existing.getRelease()));
        etPoster.setText(existing.getPosterUrl());
        sbDuration.setProgress(existing.getDuration());
        rbRating.setRating(existing.getRating());
        swWatched.setChecked(existing.isWatched());
        cbWatchlist.setChecked(existing.isOnWatchlist());
        for(int i = 0; i < spGenre.getChildCount(); i++){
            String genre = spGenre.getItemAtPosition(i).toString();
            if (existing.getGenre().equals(genre))
                spGenre.setSelection(i);
        }
        for(int i=0; i < rgApproval.getChildCount();i++){
            RadioButton radioButton = (RadioButton) rgApproval.getChildAt(i);
            if(radioButton.getText().equals(existing.getApproval())){
                radioButton.setChecked(true);
            }
        }
    }

    private void initControls() {
        etRelease = findViewById(R.id.etRelease);
        etTitle = findViewById(R.id.etTitle);
        etBudget = findViewById(R.id.etBudget);
        etPoster = findViewById(R.id.etPoster);
        spGenre = findViewById(R.id.spGenre);
        sbDuration = findViewById(R.id.sbDuration);
        rbRating = findViewById(R.id.rbRating);
        swWatched = findViewById(R.id.swWatched);
        cbWatchlist = findViewById(R.id.cbOnWatchList);
        rgApproval = findViewById(R.id.rgApproval);
        tvDurationValue = findViewById(R.id.tvDurationValue);
        btMovieAction = findViewById(R.id.btnMovieAction);
    }

    private void initListeners() {
        etRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReleaseDatePicker();
            }
        });
        sbDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDurationValue.setText(progress + " min");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btMovieAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMovie();
            }
        });
    }

    private void saveMovie() {

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
