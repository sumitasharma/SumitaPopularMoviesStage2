package com.example.android.popularmoviesstage1sumita;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.popularmoviesstage1sumita.utils.MovieDetails;
import com.example.android.popularmoviesstage1sumita.utils.MoviesAdapter;
import com.example.android.popularmoviesstage1sumita.utils.MoviesUtil;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesClickListener {
    private final String POPULARITY = "popular";
    private final String RATINGS = "top_rated";
    private final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mMoviesRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private MovieDetails[] mMovieDetails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Creating LayoutManager and setting it to the Recycler view */
        setContentView(R.layout.activity_main);
        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.movies_recyclerview);
        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mMoviesRecyclerView.setHasFixedSize(true);
        /* Calling the Asynchronous task FetchMovies */
        new FetchMovies(this).execute(POPULARITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sortby_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    /**
     * Function onOptionsItemSelected calls FetchMovies once option is selected based on Sort By
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Context context = this;
        switch (id) {
            case R.id.popularity:
                new FetchMovies(this).execute(POPULARITY);
                break;
            case R.id.rating:
                new FetchMovies(this).execute(RATINGS);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Function calls the MovieDetailsActivity to display the details of the clicked Movie by passing MovieId
     */


    @Override
    public void onClickMovie(int moviePosition) {
        Intent intent = new Intent(this, MoviesDetailActivity.class);
        intent.putExtra("MovieId", mMovieDetails[moviePosition].getId());
        startActivity(intent);
    }

    private class FetchMovies extends AsyncTask<String, Void, MovieDetails[]> {
        private Context mContext;

        FetchMovies(Context context) {
            mContext = context;
        }

        /**
         * Checks Internet Connectivity
         *
         * @return true if the Internet Connection is available, false otherwise.
         */
        boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        @Override
        protected MovieDetails[] doInBackground(String... params) {
            if (!isOnline()) {
                return null;
            }
            URL movieURL = MoviesUtil.buildUrl(params[0]);
            try {
                String movieResponse = MoviesUtil.getResponseFromHttpUrl(movieURL);
                mMovieDetails = MoviesUtil.convertJsonToMovieSortBy(movieResponse);
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            return mMovieDetails;
        }

        /**
         * Setting all the details in the XML file
         */
        @Override
        protected void onPostExecute(MovieDetails[] movieDetails) {

            if (movieDetails != null) {
                mMovieDetails = movieDetails;
                mMoviesAdapter = new MoviesAdapter(movieDetails, MainActivity.this, MainActivity.this);
                /* Setting the adapter in onPostExecute so the Movies Detail array isn't empty */
                mMoviesRecyclerView.setAdapter(mMoviesAdapter);
            } else {
                Log.i(TAG, "Post Execute Function. movie details null");
            }
        }
    }
}

