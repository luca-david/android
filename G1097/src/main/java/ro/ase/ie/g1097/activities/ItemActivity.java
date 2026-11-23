package ro.ase.ie.g1097.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import ro.ase.ie.g1097.R;
import ro.ase.ie.g1097.model.Movie;

public class ItemActivity extends AppCompatActivity {

    // ---------- Validation limits ----------
    private static final int TITLE_MIN_LENGTH = 2;
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int MIN_RELEASE_YEAR = 1888;        // oldest surviving film
    private static final int MAX_YEARS_AHEAD = 10;           // announced future releases
    private static final double BUDGET_MAX = 1_000_000_000d;
    private static final int DURATION_MIN = 1;               // minutes
    // Set to true if the first entry of @array/movie_genre is a hint such as "Select genre"
    private static final boolean GENRE_HAS_PLACEHOLDER = true;

    private EditText etTitle;
    private EditText etRelease;
    private Spinner spGenre;
    private EditText etBudget;
    private SeekBar sbDuration;
    private RatingBar rbRating;
    private RadioGroup rgApproval;
    private CheckBox cbonWatchList;
    private MaterialSwitch swWatched;
    private EditText etPoster;
    private Button btMovieAction;
    private TextView tvDurationValue;
    private TextView tvWatchedValue;

    private int position = -1;   // -1 = insert mode
    private final Calendar releaseCal = Calendar.getInstance();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // First invalid control found during validation (focused + reported to the user)
    private View firstInvalidView;
    private String firstErrorMessage;

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

        dateFormat.setLenient(false);   // reject dates like 31/02/2024

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
        if (existing.getRelease() != null) {
            releaseCal.setTime(existing.getRelease());
            etRelease.setText(dateFormat.format(existing.getRelease()));
        }
        etBudget.setText(String.valueOf(existing.getBudget()));
        sbDuration.setProgress(existing.getDuration());
        cbonWatchList.setChecked(existing.isOnWatchlist());
        swWatched.setChecked(existing.isWatched());
        rbRating.setRating(existing.getRating());
        etPoster.setText(existing.getPoster() != null ? existing.getPoster() : "");
        for (int index = 0; index < spGenre.getCount(); index++) {
            if (spGenre.getItemAtPosition(index).equals(existing.getGenre())) {
                spGenre.setSelection(index);
                break;
            }
        }
        for (int index = 0; index < rgApproval.getChildCount(); index++) {
            RadioButton rb = (RadioButton) rgApproval.getChildAt(index);
            if (rb.getText().toString().equals(existing.getApproval())) {
                rgApproval.check(rb.getId());
                break;
            }
        }
    }

    private void initControls() {
        etTitle = findViewById(R.id.etTitle);
        etRelease = findViewById(R.id.etRelease);
        spGenre = findViewById(R.id.spGenre);
        etBudget = findViewById(R.id.etBudget);
        sbDuration = findViewById(R.id.sbDuration);
        rbRating = findViewById(R.id.rbRating);
        rgApproval = findViewById(R.id.rgApproval);
        cbonWatchList = findViewById(R.id.cbOnWatchList);
        swWatched = findViewById(R.id.swWatched);
        etPoster = findViewById(R.id.etPoster);
        btMovieAction = findViewById(R.id.btnMovieAction);
        tvDurationValue = findViewById(R.id.tvDurationValue);
        tvWatchedValue = findViewById(R.id.tvWatchedValue);
    }

    private void initListeners() {
        etRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReleaseDatePicker();
            }
        });
        btMovieAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMovie();
            }
        });
        swWatched.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                tvWatchedValue.setText(isChecked ? "Yes" : "No");
                swWatched.setError(null);
                cbonWatchList.setError(null);
            }
        });
        cbonWatchList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                cbonWatchList.setError(null);
            }
        });
        sbDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDurationValue.setText(progress + " min.");
                tvDurationValue.setError(null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        rgApproval.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < group.getChildCount(); i++) {
                    ((RadioButton) group.getChildAt(i)).setError(null);
                }
            }
        });

        // Remove the error icon as soon as the user corrects a text field
        clearErrorWhileTyping(etTitle);
        clearErrorWhileTyping(etBudget);
        clearErrorWhileTyping(etPoster);
    }

    private void clearErrorWhileTyping(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void saveMovie() {
        Movie movie = readAndValidateForm();
        if (movie == null) {
            return;   // errors are already shown on the form
        }
        Intent intent = new Intent();
        intent.putExtra(MainActivity.MOVIE_EXTRA, movie);
        if (position != -1)
            intent.putExtra(MainActivity.POSITION_EXTRA, position);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Validates every control. Returns the filled Movie when all values are valid,
     * or null after marking the invalid controls and focusing the first one.
     */
    private Movie readAndValidateForm() {
        firstInvalidView = null;
        firstErrorMessage = null;

        // ----- Title: required, 2..100 characters -----
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            markInvalid(etTitle, "Title is required");
        } else if (title.length() < TITLE_MIN_LENGTH) {
            markInvalid(etTitle, "Title must have at least " + TITLE_MIN_LENGTH + " characters");
        } else if (title.length() > TITLE_MAX_LENGTH) {
            markInvalid(etTitle, "Title can have at most " + TITLE_MAX_LENGTH + " characters");
        }

        // ----- Release date: required, valid dd/MM/yyyy, realistic year -----
        Date release = null;
        String releaseText = etRelease.getText().toString().trim();
        if (releaseText.isEmpty()) {
            markInvalid(etRelease, "Release date is required");
        } else {
            try {
                release = dateFormat.parse(releaseText);
            } catch (ParseException ignored) { }

            if (release == null) {
                markInvalid(etRelease, "Release date must have the format dd/MM/yyyy");
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(release);
                Calendar latest = Calendar.getInstance();
                latest.add(Calendar.YEAR, MAX_YEARS_AHEAD);

                if (cal.get(Calendar.YEAR) < MIN_RELEASE_YEAR) {
                    markInvalid(etRelease, "Release year can't be before " + MIN_RELEASE_YEAR);
                } else if (release.after(latest.getTime())) {
                    markInvalid(etRelease, "Release date can't be more than "
                            + MAX_YEARS_AHEAD + " years in the future");
                }
            }
        }

        // ----- Genre: something must be selected -----
        int genrePosition = spGenre.getSelectedItemPosition();
        if (genrePosition == AdapterView.INVALID_POSITION
                || spGenre.getSelectedItem() == null
                || (GENRE_HAS_PLACEHOLDER && genrePosition == 0)) {
            View selected = spGenre.getSelectedView();
            if (selected instanceof TextView) {
                ((TextView) selected).setError("Select a genre");
            }
            markInvalid(spGenre, "Select a genre");
        }

        // ----- Budget: required, numeric, > 0, below a sane maximum -----
        double budget = 0;
        String budgetText = etBudget.getText().toString().trim().replace(',', '.');
        if (budgetText.isEmpty()) {
            markInvalid(etBudget, "Budget is required");
        } else {
            try {
                budget = Double.parseDouble(budgetText);
                if (Double.isNaN(budget) || Double.isInfinite(budget)) {
                    markInvalid(etBudget, "Budget must be a number");
                } else if (budget <= 0) {
                    markInvalid(etBudget, "Budget must be greater than 0");
                } else if (budget > BUDGET_MAX) {
                    markInvalid(etBudget, "Budget can't exceed " + String.format(Locale.getDefault(), "%,.0f", BUDGET_MAX));
                }
            } catch (NumberFormatException e) {
                markInvalid(etBudget, "Budget must be a number");
            }
        }

        // ----- Duration: at least 1 minute -----
        int duration = sbDuration.getProgress();
        if (duration < DURATION_MIN) {
            tvDurationValue.setError("Too short");
            markInvalid(sbDuration, "Duration must be at least " + DURATION_MIN + " minute");
        }

        // ----- Approval: one option must be checked -----
        String approval = null;
        int checkedId = rgApproval.getCheckedRadioButtonId();
        if (checkedId == -1) {
            int last = rgApproval.getChildCount() - 1;
            if (last >= 0) {
                ((RadioButton) rgApproval.getChildAt(last)).setError("Select an approval rating");
            }
            markInvalid(rgApproval, "Select an approval rating");
        } else {
            RadioButton checked = findViewById(checkedId);
            approval = checked.getText().toString();
        }

        // ----- Watched / watchlist / rating must be consistent -----
        boolean watched = swWatched.isChecked();
        boolean onWatchlist = cbonWatchList.isChecked();
        float rating = rbRating.getRating();

        if (watched && onWatchlist) {
            markInvalid(cbonWatchList, "A watched movie can't also be on the watchlist");
        }
        if (watched && rating == 0f) {
            markInvalid(rbRating, "Rate the movie, since you marked it as watched");
        } else if (!watched && rating > 0f) {
            markInvalid(rbRating, "You can only rate a movie you have watched");
        }

        // ----- Poster: required, valid http(s) URL -----
        String poster = etPoster.getText().toString().trim();
        String posterLower = poster.toLowerCase(Locale.ROOT);
        if (poster.isEmpty()) {
            markInvalid(etPoster, "Poster URL is required");
        } else if (!(posterLower.startsWith("http://") || posterLower.startsWith("https://"))
                || !Patterns.WEB_URL.matcher(poster).matches()) {
            markInvalid(etPoster, "Enter a valid URL starting with http:// or https://");
        }

        // ----- Any errors? Show the first one and stop -----
        if (firstInvalidView != null) {
            showFirstError();
            return null;
        }

        // ----- All valid: build the movie -----
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setRelease(release);
        movie.setGenre(spGenre.getSelectedItem().toString());
        movie.setBudget(budget);
        movie.setDuration(duration);
        movie.setRating(rating);
        movie.setWatched(watched);
        movie.setOnWatchlist(onWatchlist);
        movie.setPoster(poster);
        movie.setApproval(approval);
        return movie;
    }

    /** Shows the error on the control (when it can display one) and remembers the first invalid control. */
    private void markInvalid(View view, String message) {
        if (view instanceof TextView) {   // EditText, CheckBox, RadioButton, Switch
            ((TextView) view).setError(message);
        }
        if (firstInvalidView == null) {
            firstInvalidView = view;
            firstErrorMessage = message;
        }
    }

    /** Scrolls to and focuses the first invalid control, and repeats its message in a Snackbar. */
    private void showFirstError() {
        final View target = firstInvalidView;
        target.requestFocus();   // opens the error popup for focusable EditTexts
        Snackbar.make(findViewById(R.id.main), firstErrorMessage, Snackbar.LENGTH_LONG).show();
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

        DatePickerDialog dialog = new DatePickerDialog(
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
        );

        // Limit the picker to the same range the validation accepts
        Calendar min = Calendar.getInstance();
        min.set(MIN_RELEASE_YEAR, Calendar.JANUARY, 1);
        Calendar max = Calendar.getInstance();
        max.add(Calendar.YEAR, MAX_YEARS_AHEAD);
        dialog.getDatePicker().setMinDate(min.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(max.getTimeInMillis());

        dialog.show();
    }
}