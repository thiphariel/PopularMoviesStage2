package com.thiphariel.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by Thomas on 28/03/2017.
 */

class MovieLoader extends Loader<JSONObject> {
    private static final String TAG = "movie_loader_tag";

    private Context mContext;
    private JSONObject mData;
    private ProgressBar mLoadingIndicator;
    private LinearLayout mNoInternet;
    private String mUrl;

    MovieLoader(Context context, ProgressBar loadingIndicator, LinearLayout noInternet, String url) {
        super(context);

        mContext = context;
        mLoadingIndicator = loadingIndicator;
        mNoInternet = noInternet;
        mUrl = url;
    }

    @Override
    public void deliverResult(JSONObject data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        // Show the loader
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mNoInternet.setVisibility(View.INVISIBLE);

        if (mData != null) {
            deliverResult(mData);
        } else {
            if (isOnline()) {
                forceLoad();
            } else {
                mNoInternet.setVisibility(View.VISIBLE);
                mLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onForceLoad() {
        VolleySingleton.getInstance(mContext).cancelAll(TAG);

        // Formulate the request and handle the response.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, mUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Do something with the response
                        deliverResult(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                        // Handle error
                        Log.e(TAG, "Error : " + error.getLocalizedMessage());
                        Toast.makeText(mContext, "Enable to retrieve data from the API... Did you check the readme and put your API key in /res/strings.xml ?", Toast.LENGTH_LONG).show();
                    }
                });

        // Add a tag to cancel the request as needed
        jsObjRequest.setTag(TAG);
        // Add the request to the RequestQueue.
        VolleySingleton.getInstance(mContext).addToRequestQueue(jsObjRequest);
    }

    @Override
    protected void onReset() {
        super.onReset();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
