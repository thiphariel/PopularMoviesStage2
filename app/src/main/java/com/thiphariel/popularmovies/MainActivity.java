package com.thiphariel.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thiphariel.popularmovies.data.FavoriteContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.ListItemClickListener {

    private final String TAG = MainActivity.class.getSimpleName();

    private static final int MOVIES_LOADER_ID = 0;
    private static final int FAVORITES_LOADER_ID = 1;

    private static final String SORT_BY = "sort_by";
    private String mCurrentSort = "popular";

    private static final String RECYCLER_VIEW_STATE = "recycler_view_state";
    private static final String TMDB_CURRENT_RESPONSE = "tmdbçcurrent_response";
    private JSONObject mResponse;

    private static final String TMDB_BASE_PATH = "https://api.themoviedb.org/3";
    private static final String TMDB_POPULAR_PATH = TMDB_BASE_PATH + "/movie/popular";
    private static final String TMDB_TOP_PATH = TMDB_BASE_PATH + "/movie/top_rated";

    private RecyclerView mRecyclerView;
    private Parcelable mRecyclerViewState;
    private MovieAdapter mMovieAdapter;
    private LinearLayout mNoInternet;
    private Button mRetry;
    private ProgressBar mLoadingIndicator;
    private List<Movie> mMoviesList;

    private LoaderManager.LoaderCallbacks<JSONObject> mMoviesLoader;
    private LoaderManager.LoaderCallbacks<Cursor> mFavoritesLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies_list);
        mNoInternet = (LinearLayout) findViewById(R.id.no_internet);
        mRetry = (Button) findViewById(R.id.btn_retry);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mMoviesList = new ArrayList<>();

        GridLayoutManager layout = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layout);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(mMoviesList, MainActivity.this, this);
        mRecyclerView.setAdapter(mMovieAdapter);

        // Init Loaders
        mMoviesLoader = initMoviesLoader();
        mFavoritesLoader = initFavoritesLoader();

        // Retry button
        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMovies();
            }
        });

        // Update the view
        if (savedInstanceState != null) {
            try {
                String response = savedInstanceState.getString(TMDB_CURRENT_RESPONSE);
                JSONObject object = new JSONObject(response);

                // Rebuild the view with the cached data
                buildAdapterWithJSONData(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            loadMovies();
        }

        // Init favorites loader
        getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, mFavoritesLoader);
    }

    /**
     * Movies loader
     * @return
     */
    private LoaderManager.LoaderCallbacks<JSONObject> initMoviesLoader() {
        return new LoaderManager.LoaderCallbacks<JSONObject>() {
            @Override
            public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
                String url = String.format((mCurrentSort.equals("popular") ? TMDB_POPULAR_PATH : TMDB_TOP_PATH) + "?api_key=%1$s", BuildConfig.TMDB_API_KEY);

                return new MovieLoader(getBaseContext(), mLoadingIndicator, mNoInternet, url);
            }

            @Override
            public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                if (data != null) {
                    buildAdapterWithJSONData(data);
                }

                if (mRecyclerViewState != null) {
                    mRecyclerView.getLayoutManager().onRestoreInstanceState(mRecyclerViewState);
                }
            }

            @Override
            public void onLoaderReset(Loader<JSONObject> loader) {

            }
        };
    }

    /**
     * Favorites loader
     * @return
     */
    private LoaderManager.LoaderCallbacks<Cursor> initFavoritesLoader() {
        return new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<Cursor>(getBaseContext()) {

                    // Initialize a Cursor, this will hold all the task data
                    Cursor mTaskData = null;

                    // onStartLoading() is called when a loader first starts loading data
                    @Override
                    protected void onStartLoading() {
                        if (mTaskData != null) {
                            // Delivers any previously loaded data immediately
                            deliverResult(mTaskData);
                        } else {
                            // Force a new load
                            forceLoad();
                        }
                    }

                    // loadInBackground() performs asynchronous loading of data
                    @Override
                    public Cursor loadInBackground() {
                        // Will implement to load data

                        // Query and load all task data in the background; sort by priority
                        // [Hint] use a try/catch block to catch any errors in loading data

                        try {
                            return getContentResolver().query(FavoriteContract.FavoriteEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    FavoriteContract.FavoriteEntry.COLUMN_MOVIE_TITLE);

                        } catch (Exception e) {
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    }

                    // deliverResult sends the result of the load, a Cursor, to the registered listener
                    public void deliverResult(Cursor data) {
                        mTaskData = data;
                        super.deliverResult(data);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                Log.d(TAG, DatabaseUtils.dumpCursorToString(data));
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    private void loadMovies() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader moviesLoader = loaderManager.getLoader(MOVIES_LOADER_ID);

        if (moviesLoader == null) {
            loaderManager.initLoader(MOVIES_LOADER_ID, null, mMoviesLoader);
        } else {
            loaderManager.restartLoader(MOVIES_LOADER_ID, null, mMoviesLoader);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save list state
        mRecyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(RECYCLER_VIEW_STATE, mRecyclerViewState);

        // Save the current result
        outState.putString(TMDB_CURRENT_RESPONSE, mResponse.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRecyclerViewState != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mRecyclerViewState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Test which option is selected, and re-run the AsyncTask with the good param
        int id = item.getItemId();

        // Delete the current saving state to avoid a messy behaviour if we already navigate on a detail activity :D
        mRecyclerViewState = null;

        switch (id) {
            case R.id.menu_sort_popular:
                mCurrentSort = "popular";
                break;
            case R.id.menu_sort_top_rated:
                mCurrentSort = "top_rated";
                break;
        }

        loadMovies();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Movie movie = mMoviesList.get(clickedItemIndex);

        if (movie != null) {
            // Create the new intent to go on the detail screen, and pass all the needed data
            Intent intent = new Intent(this, DetailMovieActivity.class);
            //intent.putExtra("title", movie.getTitle());
            //intent.putExtra("image", movie.getPosterPath());
            //intent.putExtra("year", movie.getReleaseDate().substring(0, 4));
            //intent.putExtra("rating", movie.getVoteAverage());
            //intent.putExtra("overview", movie.getOverview());
            intent.putExtra("movie", movie);
            // Launch the Detail Activity
            startActivity(intent);
        }
    }

    // Rebuild the ArrayList<Movie> with the JSON data retrieved
    // via the API. Empty the list then refull it
    private void buildAdapterWithJSONData(JSONObject object) {
        // Save the current data
        mResponse = object;

        try {
            JSONArray array = object.getJSONArray("results");

            Type type = new TypeToken<ArrayList<Movie>>(){}.getType();
            List<Movie> list = new Gson().fromJson(String.valueOf(array), type);

            // Remove all movies
            mMoviesList.clear();
            // Add all the new retrieved movies
            mMoviesList.addAll(list);
            // Notify our adapter that the data has changed
            mMovieAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
