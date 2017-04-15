package com.thiphariel.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Thiphariel on 31/01/2017.
 */

public class Movie implements Parcelable {
    private String id;
    private String title;
    private String release_date;
    private String poster_path;
    private String backdrop_path;
    private float vote_average;
    private String overview;

    public Movie(String id, String title, String release_date, String poster_path, String backdrop_path, float vote_average, String overview) {
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.poster_path = poster_path;
        this.backdrop_path = backdrop_path;
        this.vote_average = vote_average;
        this.overview = overview;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public String getBackdropPath() {
        return backdrop_path;
    }

    public float getVoteAverage() {
        return vote_average;
    }

    public String getOverview() {
        return overview;
    }

    public String toString() {
        return "Movie : \nid : " + id + "\ntitle : " + title;
    }

    // Parcelable
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(title);
        out.writeString(release_date);
        out.writeString(poster_path);
        out.writeString(backdrop_path);
        out.writeFloat(vote_average);
        out.writeString(overview);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        release_date = in.readString();
        poster_path = in.readString();
        backdrop_path = in.readString();
        vote_average = in.readFloat();
        overview = in.readString();
    }
}
