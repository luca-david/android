package ro.ase.ie.g1096.activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import ro.ase.ie.g1096.adapter.MovieAdapter;
import ro.ase.ie.g1096.model.Movie;
import ro.ase.ie.g1096.R;
import ro.ase.ie.g1096.network.NetworkService;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnMovieListener {

    protected static final String POSITION_EXTRA = "position";
    protected static final String MOVIE_EXTRA = "movie";
    private FloatingActionButton fabInsert;
    private RecyclerView recyclerView;
    private ActivityResultLauncher<Intent> launcher;
    private final List<Movie> movies = new ArrayList<>();
    //    private ArrayAdapter<Movie> movieAdapter;
    private MovieAdapter movieAdapter;
    private final NetworkService networkService = new NetworkService();

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
        movieAdapter = new MovieAdapter(movies, this);

//        movieAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, movies);
        initControls();
        initListeners();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if(o.getResultCode() == RESULT_OK)
                        {
                            Intent intent = o.getData();
                            Movie movie = (Movie) intent.getSerializableExtra(MOVIE_EXTRA);
                            int position = intent.getIntExtra(POSITION_EXTRA, -1);
                            if(position == -1)
                            {
                                movies.add(movie);
                                movieAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                movies.set(position, movie);
                                movieAdapter.notifyDataSetChanged();
                            }
                        }
                        else
                            Toast.makeText(MainActivity.this, "Action was cancelled by the user!", Toast.LENGTH_SHORT).show();
                    }
                });

        setupSwipeToDelete();
        loadRemoteMovies();
    }

    // ---------- Download movies from the server ----------

    private void loadRemoteMovies() {

    }

    private void initListeners() {
        fabInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                launcher.launch(intent);
            }
        });

       /* listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });*/
    }

    @Override
    protected void onDestroy() {
        networkService.shutdown();   // stop a running download
        super.onDestroy();
    }

    private void initControls() {
        fabInsert = findViewById(R.id.floatingActionButton);
        recyclerView = findViewById(R.id.rvMovies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(movieAdapter);
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
                "https://cdng.europosters.eu/pod_public/1300/263129.jpg"));

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
                "https://cdn.posteritati.com/posters/000/000/057/815/toy-story-4-md-web.jpg"));

        movies.add(new Movie(
                "The Matrix",
                new GregorianCalendar(1999, Calendar.MARCH, 31).getTime(),
                "Action",
                63000000,
                136,
                0f,          // not watched yet → not rated
                false,
                "R",
                true,
                "https://render.fineartamerica.com/images/rendered/default/poster/6/8/break/images/artworkimages/medium/3/the-matrix-bo-kev.jpg"));
    }

    @Override
    public void onMovieClick(Movie movie, int position) {
        Intent intent = new Intent(MainActivity.this, ItemActivity.class);
        intent.putExtra(MOVIE_EXTRA, movies.get(position));
        intent.putExtra(POSITION_EXTRA, position);
        launcher.launch(intent);
    }

    @Override
    public void onMovieDeleteRequest(int position) {
        confirmDelete(position);   // Delete button on the card
    }


    private void setupSwipeToDelete() {
        final ColorDrawable background = new ColorDrawable(
                MaterialColors.getColor(this, androidx.appcompat.R.attr.colorError, 0xFFD32F2F));
        final Drawable icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete);
        if (icon != null) {
            icon.mutate().setTint(
                    MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnError, 0xFFFFFFFF));
        }
        final int iconMargin = (int) (24 * getResources().getDisplayMetrics().density);

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;   // no drag & drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    confirmDelete(position);
                }
            }

            /** Paints a red background with a trash icon behind the card while it is swiped. */
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View item = viewHolder.itemView;
                if (dX < 0) {
                    background.setBounds(item.getRight() + (int) dX, item.getTop(),
                            item.getRight(), item.getBottom());
                    background.draw(c);

                    if (icon != null) {
                        int iconTop = item.getTop() + (item.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconRight = item.getRight() - iconMargin;
                        int iconLeft = iconRight - icon.getIntrinsicWidth();
                        if (iconLeft > item.getRight() + dX) {   // draw only once it's uncovered
                            icon.setBounds(iconLeft, iconTop, iconRight, iconTop + icon.getIntrinsicHeight());
                            icon.draw(c);
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    // ---------- Confirm → delete → undo ----------

    private void confirmDelete(int position) {
        Movie movie = movies.get(position);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete movie?")
                .setMessage("\"" + movie.getTitle() + "\" will be removed from your list.")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton("Delete", (dialog, which) -> deleteMovie(position))
                .setNegativeButton("Cancel", (dialog, which) -> restoreSwipedRow(position))
                .setOnCancelListener(dialog -> restoreSwipedRow(position))   // Back or tap outside
                .show();
    }

    /** A swiped row stays off-screen until the adapter redraws it. */
    private void restoreSwipedRow(int position) {
        movieAdapter.notifyItemChanged(position);
    }

    private void deleteMovie(int position) {
        final Movie removed = movies.remove(position);
        movieAdapter.notifyItemRemoved(position);

        Snackbar.make(recyclerView, "\"" + removed.getTitle() + "\" deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    int restoreAt = Math.min(position, movies.size());   // list may have shrunk meanwhile
                    movies.add(restoreAt, removed);
                    movieAdapter.notifyItemInserted(restoreAt);
                    recyclerView.scrollToPosition(restoreAt);
                })
                .show();
    }
}