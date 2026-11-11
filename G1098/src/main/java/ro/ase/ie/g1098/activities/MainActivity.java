package ro.ase.ie.g1098.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import ro.ase.ie.g1098.R;
import ro.ase.ie.g1098.model.Movie;

public class MainActivity extends AppCompatActivity {

    protected static final String POSITION_EXTRA = "position";
    protected static final String MOVIE_EXTRA = "movie";
    private FloatingActionButton fabInsert;
    private ListView listView;
    private ActivityResultLauncher<Intent> launcher;
    private final List<Movie> movies = new ArrayList<>();
    private ArrayAdapter<Movie> movieArrayAdapter; //first make a variable at class scope
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addSampleMovies();

        //an adapter should always have the context of the activity
        movieArrayAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1,
                movies);

        initControls();
        initListeners();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if(o.getResultCode() == RESULT_OK && o.getData() != null)
                        {
                            Intent intent = o.getData();
                            Movie movie = IntentCompat.getSerializableExtra(intent, MOVIE_EXTRA, Movie.class);
                            int position = intent.getIntExtra(POSITION_EXTRA, -1);

                            if (movie != null) {
                                if (position == -1) {
                                    // we have a new movie
                                    movies.add(movie);
                                }
                                else {
                                    // we edited an existing movie
                                    movies.set(position, movie);
                                }

                                //tell the array adapter to refresh the listview
                                movieArrayAdapter.notifyDataSetChanged();
                            }

                            Toast.makeText(MainActivity.this, "Successfully updated the movie list", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initListeners() {
        fabInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                launcher.launch(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                intent.putExtra(MOVIE_EXTRA, movies.get(position));
                intent.putExtra(POSITION_EXTRA, position);//pass position so edit knows which item to replace
                launcher.launch(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //intent is not needed since we don't need to pass data to ItemActivity
                //Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                AlertDialog.Builder alertBox = new AlertDialog.Builder(MainActivity.this);
                alertBox.setMessage("What operation to perform on this movie?");

                alertBox.setPositiveButton("Duplicate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //avoid shallow copy, instantiate a new movie object
                        Movie original = movies.get(position);
                        Movie duplicate = new Movie(
                                original.getTitle(),
                                original.getRelease(),
                                original.getGenre(),
                                original.getBudget(),
                                original.getDuration(),
                                original.getRating(),
                                original.isWatched(),
                                original.getApproval(),
                                original.isOnWatchlist(),
                                original.getPosterUrl()
                        );

                        //add the duplicate
                        movies.add(movies.size(), duplicate);

                        //never forget to update array adapter when making changes to the list
                        movieArrayAdapter.notifyDataSetChanged();

                        Toast.makeText(MainActivity.this, "Movie duplicated successfully", Toast.LENGTH_SHORT).show();
                    }
                });

                //new DialogInterface needs to be instantiated and then create anon function to handle the implementation and logic of the method
                alertBox.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //this is where we should implement the logic for deleting
                        movies.remove(position);
                        //ALWAYS call notifyDataSetChanged() to update the listview
                        movieArrayAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Movie deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                });

                alertBox.setNeutralButton("Cancel", null);

                alertBox.show();
                return true;
            }
        });
    }

    private void initControls() {
        fabInsert = findViewById(R.id.floatingActionButton);
        listView = findViewById(R.id.listView);
        //add setAdapter to init controls
        listView.setAdapter(movieArrayAdapter);
    }

    private void addSampleMovies() {
        movies.add(new Movie(
                "Inception",
                new GregorianCalendar(2010, Calendar.JULY, 16).getTime(),
                "Sci-Fi",
                160000000,
                148,
                4.5f,
                true,
                "PG-13",
                false,
                "https://example.com/posters/inception.jpg"));

        movies.add(new Movie(
                "Toy Story",
                new GregorianCalendar(1995, Calendar.NOVEMBER, 22).getTime(),
                "Animation",
                30000000,
                81,
                4f,
                true,
                "G",
                false,
                "https://example.com/posters/toy-story.jpg"));

        movies.add(new Movie(
                "The Matrix",
                new GregorianCalendar(1999, Calendar.MARCH, 31).getTime(),
                "Action",
                63000000,
                136,
                5f,
                false,
                "R",
                true,
                "https://example.com/posters/the-matrix.jpg"));
    }
}