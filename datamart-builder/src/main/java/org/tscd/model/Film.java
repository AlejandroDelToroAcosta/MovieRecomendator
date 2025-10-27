package org.tscd.model;

import java.util.List;

public class Film {
    private String id;
    private String title;
    private int year;
    private List<Actor> cast;
    private List<Director> directors;
    private List<String> genre;
    private float rating;

    public Film(String id, String title, int year, List<Actor> cast, List<Director> directors, List<String> genre, float rating) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.cast = cast;
        this.directors = directors;
        this.genre = genre;
        this.rating = rating;
    }

    public String getId() {
        return id;
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Film{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", cast=" + cast +
                ", directors=" + directors +
                ", genre=" + genre +
                ", rating=" + rating +
                '}';
    }
}
