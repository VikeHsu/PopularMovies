package com.example.vikehsu.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

/**
 * Created by VikeHsu on 1/1/2016.
 */
public class ImageAdapter extends BaseAdapter {
    private final Context mContext;
    private static LayoutInflater inflater=null;

    private GridView.LayoutParams mImageViewLayoutParams;
    private String[] PosterUrls={};

    public ImageAdapter(Context context) {
        super();
        mContext = context;
        mImageViewLayoutParams = new GridView.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateAdapter(String[] urls)
    {
        this.PosterUrls= urls;
    }
    public int getCount() {
        return PosterUrls.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = (ImageView)inflater.inflate(R.layout.grid_movie_poster, null);
        } else {
            imageView = (ImageView) convertView;
        }
        Picasso.with(mContext).load(PosterUrls[position]).into(imageView);
        return imageView;
    }

}
