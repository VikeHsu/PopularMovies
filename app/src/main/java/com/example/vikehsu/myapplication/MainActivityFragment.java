package com.example.vikehsu.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
    private JSONObject movieJson;


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
        //gridView.setOnClickListener();
        movieTask.execute();

        return rootView;
    }

    public void UpdatePoster(String results){
        String[] urls;
        urls=getMoviePostersFromJson(results);
        if (urls != null) {
            imageAdapter.updateAdapter(urls);
            gridView.setAdapter(imageAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView,View view, int position, long l){
                    //String s = imageAdapter.getItem(position).toString();

                    // In next phase id will be used.
                    try {
                        JSONArray movieResultArray = movieJson.getJSONArray("results");
                        JSONObject currentMovie = movieResultArray.getJSONObject(position);
                        String s = currentMovie.toString();
                        Intent intent = new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,s);
                        startActivity(intent);
                        //Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT).show();
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    private String[] getMoviePostersFromJson(String MovieJsonStr) {

        final String POSTER_BASE_URL="http://image.tmdb.org/t/p/w185";
        final String MOV_RESULTS="results";
        final String MOV_POSTER_PATH = "poster_path";
        final String MOV_ID = "id";
        // In next phase id will be used.
        try {
            movieJson = new JSONObject(MovieJsonStr);
            JSONArray movieResultArray = movieJson.getJSONArray(MOV_RESULTS);
            String[] posterUrls = new String[movieResultArray.length()];
            for (int i = 0; i < movieResultArray.length(); i++) {
                String posterUrl;
                JSONObject currentMovie = movieResultArray.getJSONObject(i);
                posterUrl = POSTER_BASE_URL + currentMovie.getString(MOV_POSTER_PATH);
                posterUrls[i] = posterUrl;
            }
            return posterUrls;
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();


        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;

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
                movieJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Forecast string: " + movieJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
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
            return movieJsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            UpdatePoster(result);
        }
    }
}
