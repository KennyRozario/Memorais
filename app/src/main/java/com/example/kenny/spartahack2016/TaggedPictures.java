package com.example.kenny.spartahack2016;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.clarifai.api.Tag;

import java.util.ArrayList;
import java.util.HashMap;

public class TaggedPictures extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagged_pictures);

        mRecyclerView =(RecyclerView)findViewById(R.id.recycler_view);
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);

        HashMap<Bitmap, ArrayList<Tag>> hashMap = MainActivity.mQueriedPictures;
        ImageView imageView = (ImageView)findViewById(R.id.tagged_image);
    }
}
