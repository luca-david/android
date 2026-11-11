package ro.ase.ie.g1098.adapter;

import ro.ase.ie.g1098.model.Movie;

public interface IMovieListener {
    void onMovieClick(Movie movie, int position);
}
