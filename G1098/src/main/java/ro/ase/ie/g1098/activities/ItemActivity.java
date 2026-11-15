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
import ro.ase.ie.g1098.databinding.ActivityItemBinding;
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
    private CheckBox cbFavorited;

    private ActivityItemBinding binding; //this needs to be a class level variable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_item); -> this is for findViewById which is prone to errors

        // Inflate the binding
        binding = ActivityItemBinding.inflate(getLayoutInflater()); // this is for view binding, this means that we get a single object that contains references to all views that have an ID
        //by inflating we are reading the XML IDs and returning them to the binding Obj
        //thus we do not need to have initControls() anymore because that method is for initializing variables

        // Set the content view to the root of the binding
        setContentView(binding.getRoot());

        // (optional) update insets to use binding rather than getViewById
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initControls(); -> no longer needed since inflating automatically handles gathering the data from the ids
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

        //REPLACE ALL VARIABLES DECLARED IN THE CLASS WITH BINDING OBJ
        binding.etTitle.setText(existing.getTitle());
        binding.etBudget.setText(String.valueOf(existing.getBudget()));
        //etRelease.setText(dateFormat.format(existing.getRelease()));
        binding.sbDuration.setProgress(existing.getDuration());
        binding.rbRating.setRating(existing.getRating());
        binding.swWatched.setChecked(existing.isWatched());
        binding.cbOnWatchList.setChecked(existing.isOnWatchlist()); //again, for cbOnWatchList we get an error because the class variable is cbWatchlist, while the XML id is cbOnWatchList(we read from XML id)
        if (existing.getRelease() != null) {
            releaseCal.setTime(existing.getRelease());
            binding.etRelease.setText(dateFormat.format(existing.getRelease()));
        }

        //setting the spinner
        for (int i = 0; i < binding.spGenre.getCount(); i++) {
            //we can get via child if we use adapter
            String genre = binding.spGenre.getItemAtPosition(i).toString();
            if (genre.equalsIgnoreCase(existing.getGenre())) {
                binding.spGenre.setSelection(i);
            }
        }

        //setting the RadioGroup
        for (int i = 0; i < binding.rgApproval.getChildCount(); i++) {
            RadioButton rb = (RadioButton) binding.rgApproval.getChildAt(i);
            if (rb.getText().toString().equalsIgnoreCase(existing.getApproval())) {
                rb.setChecked(true);
            }
        }
        binding.cbFavorited.setChecked(existing.isFavorited());
    }

    private void initControls() {
        // we need to initialize the variables

//        etTitle = findViewById(R.id.etTitle);
//        etRelease = findViewById(R.id.etRelease);
//        spGenre = findViewById(R.id.spGenre);
//        etBudget = findViewById(R.id.etBudget);
//        sbDuration = findViewById(R.id.sbDuration);
//        rbRating = findViewById(R.id.rbRating);
//        swWatched = findViewById(R.id.swWatched);
//        rgApproval = findViewById(R.id.rgApproval);
//        cbWatchlist = findViewById(R.id.cbOnWatchList);
//        etPoster = findViewById(R.id.etPoster);
//        btnMovieAction = findViewById(R.id.btnMovieAction);
//        cbFavorited = findViewById(R.id.cbFavorited);

        //this initialization for variables is no longer needed since we are using viewBinding, which
        // means that we can access the views directly by binding.spGenre.getSelectedItem()


        binding.etTitle.getText();
        binding.etRelease.getText();
        binding.spGenre.getSelectedItem(); //since sp means Spinner we use getSelectedItem(spinner contains multiple items, it's like a dropdown list)
        binding.etBudget.getText(); // since et means editText we use getText()
        binding.sbDuration.getProgress(); // seekbar, think of volume bar
        binding.rbRating.getRating(); // ratingbar, gets the float value
        binding.swWatched.isChecked(); //Material Switch, similar to cb(checkbox)
        //Material Switch is used usually for toggles(dark mode on or off)
        //checkbox is used for setting a bool(watched movie? yes or no)
        binding.rgApproval.getCheckedRadioButtonId(); // rg means radio group, think of choosing an alarm in android, only one of them can be selected
        binding.cbOnWatchList.isChecked(); //cb means checkbox, is either true or false

        //IMPORTANT!
        //via binding we get the id directly from the XML rather than setting a separate variable declared in the class,
        // and then getting data from getViewById, and storing it in the variable

        //in binding we store all the data in one single variable.


    }

    private void initListeners() {
        binding.etRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReleaseDatePicker();
            }
        });

        binding.btnMovieAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMovie();
            }
        });
    }

    private void saveMovie() {
        //this is where we define the method

        //1. extract values from form controls
        String title = binding.etTitle.getText().toString().trim();
        Date releaseDate = releaseCal.getTime();
        String genre = (String) binding.spGenre.getSelectedItem();
        long budget=  0;
        try {
            budget = Long.parseLong(binding.etBudget.getText().toString().trim());
        } catch (NumberFormatException ignored) {

        }
        int duration = binding.sbDuration.getProgress();
        float rating = binding.rbRating.getRating();
        boolean isWatched = binding.swWatched.isChecked();

        //get the selected radio button text for approval rating
        String approval = "";
        int selectedRadioId = binding.rgApproval.getCheckedRadioButtonId(); //method returns -1 if there is an empty selection
        if (selectedRadioId != -1) {
            RadioButton selectedRadio = findViewById(selectedRadioId);
            approval = selectedRadio.getText().toString();
        }

        boolean isOnWatchlist = binding.cbOnWatchList.isChecked();
        String poster = binding.etPoster.getText().toString().trim();

        boolean favorited = binding.cbFavorited.isChecked();

        //2. now we create the movie object using the extracted values
        Movie movie = new Movie(title, releaseDate, genre, budget, duration, rating, isWatched, approval, isOnWatchlist, poster, favorited);

        //3. send result back to main activity
        Intent intent = new Intent();
        intent.putExtra(MainActivity.MOVIE_EXTRA, movie);
        intent.putExtra(MainActivity.POSITION_EXTRA, position);//pass position back: -1 for new, >= 0 for edit

        setResult(RESULT_OK, intent);
        finish();
    }

    private void showReleaseDatePicker() {
        String current = binding.etRelease.getText().toString();
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
                        binding.etRelease.setError(null);
                        binding.etRelease.setText(dateFormat.format(releaseCal.getTime()));
                    }
                },
                releaseCal.get(Calendar.YEAR),
                releaseCal.get(Calendar.MONTH),
                releaseCal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}