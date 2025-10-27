package org.tscd.model;

import java.util.List;

public class Movie {
    private String id;
    private String title;
    private int year;
    private List<Actor> cast;
    private List<Director> directors;
    private List<String> genre;
    private double rating;
    private int duration;

    public Movie(String id, String title, int year, List<Actor> cast, List<Director> directors, List<String> genre, double rating, int duration) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.cast = cast;
        this.directors = directors;
        this.genre = genre;
        this.rating = rating;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<Actor> getCast() {
        return cast;
    }

    public void setCast(List<Actor> cast) {
        this.cast = cast;
    }

    public List<Director> getDirectors() {
        return directors;
    }

    public void setDirectors(List<Director> directors) {
        this.directors = directors;
    }

    public List<String> getGenre() {
        return genre;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", cast=" + cast +
                ", directors=" + directors +
                ", genre=" + genre +
                ", rating=" + rating +
                ", duration=" + duration +
                '}';
    }
}
