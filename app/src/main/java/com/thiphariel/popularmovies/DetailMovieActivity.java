package com.thiphariel.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
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
    private Movie mMovie;
    private Menu mMenu;

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
        mMovie = getIntent().getParcelableExtra("movie");

        //String movieTitle = getIntent().getStringExtra("title");
        //String movieImage = getIntent().getStringExtra("image");
        //String movieYear = getIntent().getStringExtra("year");
        //float movieRating = getIntent().getFloatExtra("rating", 0);
        //String movieOverview = getIntent().getStringExtra("overview");

        title.setText(mMovie.getTitle());
        year.setText(mMovie.getReleaseDate());
        rating.setText(getString(R.string.rating, mMovie.getVoteAverage()));
        overview.setText(getString(R.string.synopsis, mMovie.getOverview()));

        // Set the good width to the image.. Bad idea, but I wanted to avoid the moving layout effect on image load
        image.getLayoutParams().width = 342;
        image.getLayoutParams().height = 513;
        image.requestLayout();

        // Read the content provider data if the current movie is in favorites
        Cursor cursor = getContentResolver().query(FavoriteContract.FavoriteEntry.CONTENT_URI, null, "movie_id=?", new String[]{mMovie.getId()}, null);
        if (cursor != null) {
            Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
            if (cursor.moveToNext()) {
                String movieId = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID));
                String movieTitle = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_TITLE));
                Log.d(TAG, "Movie id : " + movieId + " / title : " + movieTitle);

                // Set as favorite
                mMovie.setFavorite(true);
            }
            cursor.close();
        }

        // Load the movie image with Picasso
        Picasso.with(this).load("http://image.tmdb.org/t/p/w342/" + mMovie.getPosterPath()).into(image);
    }

    /**
     * Add the current movie to the favorites
     */
    private void addToFavorite() {
        ContentValues values = new ContentValues();
        values.put(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID, mMovie.getId());
        values.put(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_TITLE, mMovie.getTitle());

        Uri uri = getContentResolver().insert(FavoriteContract.FavoriteEntry.CONTENT_URI, values);

        if (uri != null) {
            Log.d(TAG, "Successfully added to favorite : " + uri);
            mMovie.setFavorite(true);
        } else {
            throw new UnsupportedOperationException("Unknown error while trying to add movie to favorite");
        }
    }

    /**
     * Remove the current movie from the favorites
     */
    private void removeFavorite() {
        int deleted = getContentResolver().delete(FavoriteContract.FavoriteEntry.CONTENT_URI.buildUpon().appendPath(mMovie.getId()).build(), null, null);

        if (deleted > 0) {
            Log.d(TAG, "Successfully removed from favorite");
            mMovie.setFavorite(false);
        } else {
            throw new UnsupportedOperationException("Unknown error while trying to remove movie to favorite");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);

        // if the current movie is favorite, update the menu item icon
        if (mMovie.isFavorite()) {
            menu.getItem(0).setIcon(R.drawable.ic_star_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Test which option is selected, and re-run the AsyncTask with the good param
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_favorite:
                // Add / remove favorite
                if (mMovie.isFavorite()) {
                    item.setIcon(R.drawable.ic_star_border_white_24dp);

                    removeFavorite();
                    Toast.makeText(getBaseContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    item.setIcon(R.drawable.ic_star_white_24dp);

                    addToFavorite();
                    Toast.makeText(getBaseContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
