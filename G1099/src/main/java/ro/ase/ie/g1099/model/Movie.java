package ro.ase.ie.g1099.model;

import java.io.Serializable;
import java.util.Date;

public class Movie implements Serializable {
    private String title; //EditText
    private Date release; //EditText date
    private String genre; //Spinner
    private double budget; //EditText number
    private int duration; //SeekBar
    private float rating; //RatingBar
    private boolean watched; //Switch
    private String approval; //RadioGroup | RadioButtons
    private boolean onWatchlist; //Checkbox
    private String posterUrl;

    public Movie(String title, Date release, String genre, double budget, int duration, float rating, boolean watched, String approval, boolean onWatchlist, String posterUrl) {
        this.title = title;
        this.release = release;
        this.genre = genre;
        this.budget = budget;
        this.duration = duration;
        this.rating = rating;
        this.watched = watched;
        this.approval = approval;
        this.onWatchlist = onWatchlist;
        this.posterUrl = posterUrl;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", release=" + release +
                ", genre='" + genre + '\'' +
                ", budget=" + budget +
                ", duration=" + duration +
                ", rating=" + rating +
                ", watched=" + watched +
                ", approval='" + approval + '\'' +
                ", onWatchlist=" + onWatchlist +
                '}';
    }
}
