package com.example.kenny.spartahack2016;

import android.graphics.Bitmap;

import com.clarifai.api.Tag;

import java.util.ArrayList;

/**
 * Created by Kenny on 2016-02-28.
 */
public class Picture {
    private Bitmap mBitmap;
    private ArrayList<Tag> mTags;

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public void setTags(ArrayList<Tag> tags) {
        mTags = tags;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public ArrayList<Tag> getTags() {
        return mTags;
    }
}