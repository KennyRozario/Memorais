package com.example.kenny.memorais;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.clarifai.api.Tag;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kenny on 2016-03-03.
 */
public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder{

        private final ImageView mPhoto;

        public ViewHolder (View v){
            super(v);
            mPhoto = (ImageView)v.findViewById(R.id.tagged_image);
        }
    }

    private HashMap<Bitmap, ArrayList<Tag>> mQueriedPictures;
    private Context mContext;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int i = position;
        Bitmap picture = null;
        while (i == position){
            for (Bitmap bitmap : mQueriedPictures.keySet()){
                picture = bitmap;
                Log.d("Adapter", mQueriedPictures.get(bitmap).toString());
            }
            position ++;
        }
        holder.mPhoto.setImageBitmap(picture);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.grid_image, parent, false);

        return  new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        if (mQueriedPictures != null){
            return mQueriedPictures.size();
        }else {
            return 0;
        }
    }

    public ResultsAdapter (Context context, HashMap<Bitmap, ArrayList<Tag>> queriedPictures){
        mContext = context;
        mQueriedPictures = queriedPictures;
    }
}
