package ro.ase.ie.g1098.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ro.ase.ie.g1098.R;
import ro.ase.ie.g1098.model.Movie;

public class ItemActivity extends AppCompatActivity {

    private EditText etRelease;
    private int position = -1;   // -1 = insert mode
    private Calendar releaseCal = Calendar.getInstance();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // we need to create variables of all fields
    private EditText etTitle;
    private Spinner spGenre;
    private EditText etBudget;
    private SeekBar sbDuration;
    private RatingBar rbRating;
    private MaterialSwitch swWatched;
    private CheckBox cbWatchlist;
    private RadioGroup rgApproval;
    private EditText etPoster;
    private Button btnMovieAction;


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
        }
    }

    private void fillForm(Movie existing) {
        //1. get data from existing object
        //2. put its attributes inside the form fields
        etTitle.setText(existing.getTitle());
        etBudget.setText(String.valueOf(existing.getBudget()));
        //etRelease.setText(dateFormat.format(existing.getRelease()));
        sbDuration.setProgress(existing.getDuration());
        rbRating.setRating(existing.getRating());
        swWatched.setChecked(existing.isWatched());
        cbWatchlist.setChecked(existing.isOnWatchlist());
        if (existing.getRelease() != null) {
            releaseCal.setTime(existing.getRelease());
            etRelease.setText(dateFormat.format(existing.getRelease()));
        }

        //setting the spinner
        for (int i = 0; i < spGenre.getCount(); i++) {
            //we can get via child if we use adapter
            String genre = spGenre.getItemAtPosition(i).toString();
            if (genre.equalsIgnoreCase(existing.getGenre())) {
                spGenre.setSelection(i);
            }
        }

        //setting the RadioGroup
        for (int i = 0; i < rgApproval.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgApproval.getChildAt(i);
            if (rb.getText().toString().equalsIgnoreCase(existing.getApproval())) {
                rb.setChecked(true);
            }
        }
    }

    private void initControls() {
        // we need to initialize the variables

        etTitle = findViewById(R.id.etTitle);
        etRelease = findViewById(R.id.etRelease);
        spGenre = findViewById(R.id.spGenre);
        etBudget = findViewById(R.id.etBudget);
        sbDuration = findViewById(R.id.sbDuration);
        rbRating = findViewById(R.id.rbRating);
        swWatched = findViewById(R.id.swWatched);
        rgApproval = findViewById(R.id.rgApproval);
        cbWatchlist = findViewById(R.id.cbOnWatchList);
        etPoster = findViewById(R.id.etPoster);
        btnMovieAction = findViewById(R.id.btnMovieAction);
    }

    private void initListeners() {
        etRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReleaseDatePicker();
            }
        });

        btnMovieAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMovie();
            }
        });
    }

    private void saveMovie() {
        //this is where we define the method

        //1. extract values from form controls
        String title = etTitle.getText().toString().trim();
        Date releaseDate = releaseCal.getTime();
        String genre = (String) spGenre.getSelectedItem();
        long budget=  0;
        try {
            budget = Long.parseLong(etBudget.getText().toString().trim());
        } catch (NumberFormatException ignored) {

        }
        int duration = sbDuration.getProgress();
        float rating = rbRating.getRating();
        boolean isWatched = swWatched.isChecked();

        //get the selected radio button text for approval rating
        String approval = "";
        int selectedRadioId = rgApproval.getCheckedRadioButtonId(); //method returns -1 if there is an empty selection
        if (selectedRadioId != -1) {
            RadioButton selectedRadio = findViewById(selectedRadioId);
            approval = selectedRadio.getText().toString();
        }

        boolean isOnWatchlist = cbWatchlist.isChecked();
        String poster = etPoster.getText().toString().trim();

        //2. now we create the movie object using the extracted values
        Movie movie = new Movie(title, releaseDate, genre, budget, duration, rating, isWatched, approval, isOnWatchlist, poster);

        //3. send result back to main activity
        Intent intent = new Intent();
        intent.putExtra(MainActivity.MOVIE_EXTRA, movie);
        intent.putExtra(MainActivity.POSITION_EXTRA, position);//pass position back: -1 for new, >= 0 for edit

        setResult(RESULT_OK, intent);
        finish();
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