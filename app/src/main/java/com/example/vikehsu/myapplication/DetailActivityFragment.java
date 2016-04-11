package com.example.vikehsu.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private ListView trailers;
    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView title= (TextView) rootView.findViewById(R.id.detail_originalTitle);
        TextView releaseYear= (TextView) rootView.findViewById(R.id.detail_releaseYear);
        TextView overview= (TextView) rootView.findViewById(R.id.detail_overview);
        TextView rating= (TextView) rootView.findViewById(R.id.detail_rating);
        ImageView poster=(ImageView) rootView.findViewById(R.id.detail_imageThumbnail);
        trailers = (ListView) rootView.findViewById(R.id.detail_trailers);


        Intent intent = getActivity().getIntent();
        String MovieJsonStr = intent.getStringExtra(Intent.EXTRA_TEXT);
        String id;
        try {
            JSONObject currentMovie = new JSONObject(MovieJsonStr);
            title.setText(currentMovie.getString("title"));
            releaseYear.setText(currentMovie.getString("release_date").substring(0,4));
            overview.setText(currentMovie.getString("overview"));
            rating.setText(String.format("%.1f/10",currentMovie.getDouble("vote_average")));
            Picasso.with(this.getContext()).load("http://image.tmdb.org/t/p/w185"+currentMovie.getString("poster_path")).into(poster);

            id=currentMovie.getString("id");
            FetchTrailerTask tsk= new FetchTrailerTask();
            tsk.execute(id);

            //Toast.makeText(this.getActivity(),id,Toast.LENGTH_SHORT).show();
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return rootView;
    }
    public void UpdateTrailer(JSONObject result){
        try {
            JSONArray JSONVideos = result.getJSONArray("results");
            String [] name = new String[JSONVideos.length()];
            String [] link = new String[JSONVideos.length()];
            for(int i = 0; i < JSONVideos.length(); i++)
            {
                JSONObject currentVideo = JSONVideos.getJSONObject(i);
                name[i]= "Trailer "+(String.valueOf(i+1));
                link [i] ="https://www.youtube.com/watch?v="+currentVideo.getString("key");

                //Toast.makeText(this.getActivity(),name[i],Toast.LENGTH_SHORT).show();
                //Toast.makeText(this.getActivity(),link[i],Toast.LENGTH_SHORT).show();
                //trailers.addView(tv);
            }

            TrailerAdapter trailerAdapter = new TrailerAdapter(getActivity());
            trailerAdapter.updateAdapter(name,link);
            trailers.setAdapter(trailerAdapter);
        }catch (JSONException e) {
                e.printStackTrace();
        }
    }
    public class TrailerAdapter extends BaseAdapter {

        private  LayoutInflater inflater=null;
        private String [] name = null;
        private String [] link = null;

        public TrailerAdapter(Context context) {
            super();
            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        public void updateAdapter(String[] _name,String[] _link)
        {
            this.name= _name;
            this.link= _link;
        }
        public int getCount() {
            return name.length;
        }

        public Object getItem(int position) {
            return null;
        }
        public long getItemId(int position) {
            return 0;
        }
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                view = inflater.inflate(R.layout.trailer_list_item, null);
                TextView tv= (TextView) view.findViewById(R.id.trailer_title);
                tv.setText(name[position]);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link[position])));
                    }

                });
            } else {
                view = convertView;
            }
            return view;
        }
    }

    public class FetchTrailerTask extends AsyncTask<String, Void, JSONObject> {

        protected JSONObject doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            JSONObject movieJson=null;

            try {
                final String BASE_URL =
                        "http://api.themoviedb.org/3/movie/"+params[0]+"/videos?";
                final String API_KEY = "api_key";


                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
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
                movieJson = new JSONObject(movieJsonStr);

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }catch (IOException e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                    }
                }
            }

            return movieJson ;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            UpdateTrailer(result);
        }
    }
}
