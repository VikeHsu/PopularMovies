package com.example.vikehsu.myapplication;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivityFragment extends Fragment {

    private ImageAdapter imageAdapter;
    private GridView gridView;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) rootView.findViewById(R.id.gird_view_poster);

        FetchMovieTask movieTask = new FetchMovieTask();
        imageAdapter = new ImageAdapter(getActivity());
        gridView.setAdapter(imageAdapter);
        movieTask.execute();

        return rootView;
    }

    public void UpdatePoster(String[] urls){
        if (urls != null) {
            imageAdapter.updateAdapter(urls);
            gridView.setAdapter(imageAdapter);
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private String[] getMovieDataFromJson(String MovieJsonStr)
                throws JSONException {

            final String POSTER_BASE_URL="http://image.tmdb.org/t/p/w185";
            final String MOV_RESULTS="results";
            final String MOV_POSTER_PATH = "poster_path";
            final String MOV_ID = "id";
            // In next phase id will be used.

            JSONObject movieJson = new JSONObject(MovieJsonStr);
            JSONArray movieResultArray = movieJson.getJSONArray(MOV_RESULTS);
            String [] posterUrls=new String[movieResultArray.length()];
            int [] movieIds=new int[movieResultArray.length()];
            for(int i = 0; i < movieResultArray.length(); i++) {
                String posterUrl;
                int id;
                JSONObject currentMovie = movieResultArray.getJSONObject(i);
                posterUrl = POSTER_BASE_URL + currentMovie.getString(MOV_POSTER_PATH);
                posterUrls[i] = posterUrl;
            }
            return posterUrls;
        }

        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            try {
                final String BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY = "sort_by";
                final String API_KEY = "api_key";
                final String POPULARITY_DESC="popularity.desc";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY, POPULARITY_DESC)
                        .appendQueryParameter(API_KEY,getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Forecast string: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            UpdatePoster(result);
        }
    }
}
