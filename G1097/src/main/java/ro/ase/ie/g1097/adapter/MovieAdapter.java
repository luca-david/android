package ro.ase.ie.g1097.adapter;   // <-- change to your package

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ro.ase.ie.g1097.R;
import ro.ase.ie.g1097.model.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    /** Optional callbacks so the activity can react to taps. */
    public interface OnMovieListener {
        void onMovieClick(Movie movie, int position);
        void onMovieDeleteRequest(int position);
    }

    private final List<Movie> movies;
    private final OnMovieListener listener;   // may be null

    private static final SimpleDateFormat YEAR = new SimpleDateFormat("yyyy", Locale.US);

    public MovieAdapter(List<Movie> movies, OnMovieListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder h, int position) {
        Movie m = movies.get(position);

        // --- text ---
        h.title.setText(m.getTitle());

        String year = (m.getRelease() != null) ? YEAR.format(m.getRelease()) : "—";
        h.subtitle.setText(m.getGenre() + " · " + year);
        bindRating(h, m.getRating());
        h.facts.setText(m.getDuration() + " min  · " + formatBudget(m.getBudget()));

        // --- chips ---
        bindApprovalChip(h.chipApproval, m.getApproval());
        h.chipWatched.setVisibility(m.isWatched() ? View.VISIBLE : View.GONE);
        h.chipWatchlist.setVisibility(m.isOnWatchlist() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    // ---- helpers ----

    // Rating colors
    private static final int RATING_GREEN = Color.parseColor("#2E7D32");
    private static final int RATING_RED   = Color.parseColor("#C62828");
    private static final int RATING_GREY  = Color.parseColor("#757575");

    /**
     * Stars and label share one color:
     *   0          → "Not rated" (grey)
     *   above 4    → "Excellent" (green)
     *   below 2    → "Poor"      (red)
     *   otherwise  → "Average"   (grey)
     */
    private void bindRating(MovieViewHolder h, float rating) {
        String label;
        int color;
        if (rating == 0f) {
            label = "Not rated";
            color = RATING_GREY;
        } else if (rating > 4f) {
            label = "Excellent";
            color = RATING_GREEN;
        } else if (rating < 2f) {
            label = "Poor";
            color = RATING_RED;
        } else {
            label = "Average";
            color = RATING_GREY;
        }

        h.rating.setRating(rating);
        h.rating.setProgressTintList(ColorStateList.valueOf(color));            // filled stars
        h.rating.setSecondaryProgressTintList(ColorStateList.valueOf(color));   // half stars

        h.ratingValue.setText(rating == 0f
                ? label
                : String.format(Locale.US, "%.1f · %s", rating, label));     // e.g. "4.5 · Excellent"
        h.ratingValue.setTextColor(color);
    }

    // Approval colors: light background + dark text of the same hue, so the label stays readable
    private static final int GREEN_BG  = Color.parseColor("#C8E6C9"), GREEN_FG  = Color.parseColor("#1B5E20");
    private static final int ORANGE_BG = Color.parseColor("#FFE0B2"), ORANGE_FG = Color.parseColor("#E65100");
    private static final int RED_BG    = Color.parseColor("#FFCDD2"), RED_FG    = Color.parseColor("#B71C1C");

    /** G / PG → green, PG-13 → orange, R / NC-17 → red; hidden when the movie has no approval. */
    private void bindApprovalChip(Chip chip, String approval) {
        if (approval == null || approval.trim().isEmpty()) {
            chip.setVisibility(View.GONE);
            return;
        }
        chip.setVisibility(View.VISIBLE);
        chip.setText(approval);

        int bg, fg;
        switch (approval.trim().toUpperCase(Locale.ROOT)) {
            case "G":
            case "PG":
                bg = GREEN_BG;  fg = GREEN_FG;  break;
            case "PG-13":
                bg = ORANGE_BG; fg = ORANGE_FG; break;
            case "R":
            case "NC-17":
                bg = RED_BG;    fg = RED_FG;    break;
            default:            // unknown value: neutral grey
                bg = Color.parseColor("#EEEEEE"); fg = Color.parseColor("#424242");
        }
        chip.setChipBackgroundColor(ColorStateList.valueOf(bg));
        chip.setChipStrokeColor(ColorStateList.valueOf(fg));
        chip.setTextColor(fg);
    }

    private String formatBudget(double budget) {
        if (budget >= 1_000_000)   return String.format(Locale.US, "$%.0fM", budget / 1_000_000);
        if (budget >= 1_000)       return String.format(Locale.US, "$%.0fK", budget / 1_000);
        return String.format(Locale.US, "$%.0f", budget);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        final ImageView poster;
        final TextView title;
        final TextView subtitle;
        final RatingBar rating;
        final TextView ratingValue;
        final TextView facts;
        final Chip chipApproval;
        final Chip chipWatched;
        final Chip chipWatchlist;
        final Button btnDelete;

        MovieViewHolder(@NonNull View v) {
            super(v);
            poster       = v.findViewById(R.id.moviePoster);
            title        = v.findViewById(R.id.movieTitle);
            subtitle     = v.findViewById(R.id.movieSubtitle);
            rating       = v.findViewById(R.id.movieRating);
            ratingValue  = v.findViewById(R.id.movieRatingValue);
            facts        = v.findViewById(R.id.movieFacts);
            chipApproval = v.findViewById(R.id.chipApproval);
            chipWatched  = v.findViewById(R.id.chipWatched);
            chipWatchlist= v.findViewById(R.id.chipWatchlist);
            btnDelete    = v.findViewById(R.id.movieDelete);

            v.setOnClickListener(click -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMovieClick(movies.get(pos), pos);
                }
            });

            btnDelete.setOnClickListener(click -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null)
                    listener.onMovieDeleteRequest(pos);
            });
        }
    }
}