package com.thiphariel.popularmovies;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.thiphariel.popularmovies.data.FavoriteContract;
import com.thiphariel.popularmovies.data.Movie;

public class DetailMovieActivity extends AppCompatActivity {

    private static final String TAG = DetailMovieActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);

        TextView title = (TextView) findViewById(R.id.tv_detail_title);
        ImageView image = (ImageView) findViewById(R.id.iv_detail_image);
        TextView year = (TextView) findViewById(R.id.tv_detail_year);
        TextView rating = (TextView) findViewById(R.id.tv_detail_rating);
        TextView overview = (TextView) findViewById(R.id.tv_detail_overview);

        // Retrieve extra intent parcelable
        Movie movie = getIntent().getParcelableExtra("movie");

        //String movieTitle = getIntent().getStringExtra("title");
        //String movieImage = getIntent().getStringExtra("image");
        //String movieYear = getIntent().getStringExtra("year");
        //float movieRating = getIntent().getFloatExtra("rating", 0);
        //String movieOverview = getIntent().getStringExtra("overview");

        title.setText(movie.getTitle());
        year.setText(movie.getReleaseDate());
        rating.setText(getString(R.string.rating, movie.getVoteAverage()));
        overview.setText(getString(R.string.synopsis, movie.getOverview()));

        // Set the good width to the image.. Bad idea, but I wanted to avoid the moving layout effect on image load
        image.getLayoutParams().width = 342;
        image.getLayoutParams().height = 513;
        image.requestLayout();

        Log.d(TAG, movie.toString());
        Cursor cursor = getContentResolver().query(FavoriteContract.FavoriteEntry.CONTENT_URI, null, "movie_id=?", new String[]{movie.getId()}, null);
        if (cursor != null) {
            Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
            if (cursor.moveToNext()) {
                String movieId = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID));
                String movieTitle = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_TITLE));
                Log.d(TAG, "Movie id : " + movieId + " / title : " + movieTitle);
            }
            cursor.close();
        }

        // Load the movie image with Picasso
        Picasso.with(this).load("http://image.tmdb.org/t/p/w342/" + movie.getPosterPath()).into(image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Test which option is selected, and re-run the AsyncTask with the good param
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_favorite:
                item.setIcon(R.drawable.ic_star_white_24dp);
                Toast.makeText(getBaseContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
