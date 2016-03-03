package com.example.kenny.memorais;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;


    private ArrayList<Bitmap> mImages;
    private static HashMap<Bitmap, ArrayList<Tag>> mPictures;
    public static HashMap<Bitmap, ArrayList<Tag>> mQueriedPictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImages = new ArrayList<Bitmap>();
        mPictures = new HashMap<>();
        mQueriedPictures = new HashMap<>();

        checkPermissions();

        final EditText editText = (EditText) findViewById(R.id.tag_search);
        Button button = (Button) findViewById(R.id.dank_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getPicturesFromStorage();

                for (int i = 0; i < mImages.size(); i++) {
                    Bitmap bitmap = mImages.get(i);
                    new AsyncTask<Bitmap, Void, RecognitionResult>() {
                        @Override
                        protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                            return ClarifaiService.recognizeBitmap(bitmaps[0]);
                        }

                        @Override
                        protected void onPostExecute(RecognitionResult result) {
                            findTags(result);
                        }
                    }.execute(bitmap);
                }

                String tagSearch = editText.getText().toString();
                compareTags(mPictures, tagSearch);

                Intent i = new Intent(MainActivity.this, TaggedPictures.class);
                startActivity(i);
            }
        });
    }

    //Checks if Marshmallow users have granted permission for reading external storage
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    //add images from the external storage to the mImages Array List
    private void getPicturesFromStorage() {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            int i = 0;
            Log.d(TAG, projection.length + "");
            //iterate through all the images and add them to the array list mImages

            do {
                String imageLocation = cursor.getString(1);
                File imageFile = new File(imageLocation);
                if (imageFile.exists()) {
                    Log.d(TAG, i + "");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inDither = true;
                    Bitmap bm = BitmapFactory.decodeFile(imageLocation, options);
                    mImages.add(i, bm);
                    i++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    //Goes through the recognition result of each image and creates a new array of tags for each image
    //Post: mPictures will be updated with all the resultant images and their appropriate tags
    private void findTags(RecognitionResult result) {
        if (result != null) {
            if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
                for (int i = 0; i < mImages.size(); i++) {
                    ArrayList<Tag> tags = new ArrayList<Tag>();
                    for (Tag tag : result.getTags()) {
                        tags.add(tag);
                        Log.d(TAG, tag.getName());
                    }
                    Picture picture = new Picture();
                    picture.setBitmap(mImages.get(i));
                    picture.setTags(tags);
                    mPictures.put(picture.getBitmap(), picture.getTags());
                }
                Log.d(TAG, "mPictures size is: " + mPictures.size());

            } else {
                Log.e(TAG, "Clarifai: " + result.getStatusMessage());
            }
        } else {
            Log.d(TAG, "There was an error recognizing the image.");
        }
    }

    //compares queried tags to the ones within mPictures, matching tags results in the respective bitmap
    //and tags to be put into the HashMap, mQueriedPictures, to only hold the images the user may want
    private void compareTags(HashMap<Bitmap, ArrayList<Tag>> hashMap, String searchTag) {
        Log.d(TAG, "compareTags called");

        if (mQueriedPictures != null) {
            mQueriedPictures.clear();
        }

        Iterator iterator = hashMap.keySet().iterator();
        Log.d(TAG, "iterator created");

        while (iterator.hasNext()) {
            Log.d(TAG, "while loop iterator begun");
            Bitmap key = (Bitmap) iterator.next();
//            Set<Bitmap> bitmapSet = hashMap.keySet();
//            bitmapSet.toArray()
            ArrayList<Tag> tags = hashMap.get(key);

            for (Tag tag : tags) {
                if (tag.getName().equalsIgnoreCase(searchTag.trim())) {
                    Log.d(TAG, "The matching tag is: " + tag);
                    mQueriedPictures.put(key, tags);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
