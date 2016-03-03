package com.example.kenny.memorais;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.clarifai.api.Tag;

import java.util.ArrayList;
import java.util.HashMap;

public class TaggedPictures extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ResultsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagged_pictures);

        HashMap<Bitmap, ArrayList<Tag>> hashMap = MainActivity.mQueriedPictures;
//        ImageView imageView = (ImageView)findViewById(R.id.tagged_image);

        mRecyclerView =(RecyclerView)findViewById(R.id.recycler_view);
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ResultsAdapter(this, hashMap);
        mRecyclerView.setAdapter(mAdapter);
    }


}
