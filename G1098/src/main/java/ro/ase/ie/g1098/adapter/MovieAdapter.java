package ro.ase.ie.g1098.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ro.ase.ie.g1098.R;
import ro.ase.ie.g1098.model.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {


    private final ArrayList<Movie> movies;

    private final IMovieListener listener;

    public MovieAdapter(ArrayList<Movie> movies, IMovieListener listener) {
        this.movies = movies;
        this.listener = listener;

    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view); //calls the constructor for MovieViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        //take data from movie and put in the fields

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Movie movie = movies.get(position);
        holder.title.setText(movie.getTitle());
        holder.subtitle.setText(movie.getGenre().toString() + " . " + movie.getRelease().getYear());
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        //holds down all the views together
        final ImageView poster;
        final TextView title;
        final TextView subtitle;
        final RatingBar rating;
        final TextView ratingValue;
        final TextView facts;
        final Chip chipApproval;
        final Chip chipWatched;
        final Chip chipWatchList;
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.moviePoster);
            title = itemView.findViewById(R.id.movieTitle);
            subtitle = itemView.findViewById(R.id.movieSubtitle);
            rating = itemView.findViewById(R.id.movieRating);
            ratingValue = itemView.findViewById(R.id.movieRatingValue);
            facts = itemView.findViewById(R.id.movieFacts);
            chipApproval = itemView.findViewById(R.id.chipApproval);
            chipWatched = itemView.findViewById(R.id.chipWatched);
            chipWatchList = itemView.findViewById(R.id.chipWatchlist);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Movie movie = movies.get(pos);
                        listener.onMovieClick(movie, pos);
                    }
                }
            });

        }

    }
}
