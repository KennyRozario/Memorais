package com.example.kenny.spartahack2016;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Credentials;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;

    private final ClarifaiClient mClient = new ClarifaiClient(com.example.kenny.spartahack2016.Credentials.CLIENT_ID,
            com.example.kenny.spartahack2016.Credentials.CLIENT_SECRET);


    private ArrayList<Bitmap> mImages;
    public HashMap<Bitmap, ArrayList<Tag>> mPictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        mImages = new ArrayList<Bitmap>();
        mPictures = new HashMap<>();

        Button button = (Button)findViewById(R.id.dank_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPictures();
                for (int i = 0; i < mImages.size(); i++) {
                    Bitmap bitmap = mImages.get(i);
                    new AsyncTask<Bitmap, Void, RecognitionResult>() {
                        @Override
                        protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                            return recognizeBitmap(bitmaps[0]);
                        }

                        @Override
                        protected void onPostExecute(RecognitionResult result) {
                            updateUIForResult(result);
                        }
                    }.execute(bitmap);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    //add images from the external storage to the mImages Array
    private void addPictures(){
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

        if (cursor.moveToFirst()) {
            int i = 0;
            Log.d(TAG, projection.length + "");
                while (cursor.getPosition() < projection.length) {
                String imageLocation = cursor.getString(1);
                File imageFile = new File(imageLocation);
                if (imageFile.exists()) {
                    Log.d(TAG, i +"");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inDither = true;
                    Bitmap bm = BitmapFactory.decodeFile(imageLocation, options);
                    //TODO:add stuff here
                    mImages.add(i, bm);
                    i++;
                    cursor.moveToNext();
                }
            }

        }
    }

    //    private Bitmap loadBitmapFromUri(Uri uri) {
//        try {
//            // The image may be large. Load an image that is sized for display. This follows best
//            // practices from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
//            BitmapFactory.Options opts = new BitmapFactory.Options();
//            opts.inJustDecodeBounds = true;
//            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
//            int sampleSize = 1;
//            while (opts.outWidth / (2 * sampleSize) >= imageView.getWidth() &&
//                    opts.outHeight / (2 * sampleSize) >= imageView.getHeight()) {
//                sampleSize *= 2;
//            }
//
//            opts = new BitmapFactory.Options();
//            opts.inSampleSize = sampleSize;
//            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
//        } catch (IOException e) {
//            Log.e(TAG, "Error loading image: " + uri, e);
//        }
//        return null;
//    }

    private RecognitionResult recognizeBitmap(Bitmap bitmap) {
        try {
            // Scale down the image.
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
                    320 * bitmap.getHeight() / bitmap.getWidth(), true);

            // Compress the image as a JPEG.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            byte[] jpeg = out.toByteArray();

            // Send the JPEG to Clarifai and return the result.
            return mClient.recognize(new RecognitionRequest(jpeg)).get(0);
        } catch (ClarifaiException e) {
            Log.e(TAG, "Clarifai error", e);
            return null;
        }
    }

    private void updateUIForResult(RecognitionResult result) {
        if (result != null) {
            if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {

                for (int i = 0; i < mImages.size(); i++){
                    ArrayList<Tag> tags = new ArrayList<Tag>();
                    for (Tag tag : result.getTags()){
                        tags.add(tag);
                        Log.d(TAG, tag.getName());
                    }
                    Picture picture = new Picture();
                    picture.setBitmap(mImages.get(i));
                    picture.setTags(tags);
                    if (picture != null) {
                        mPictures.put(picture.getBitmap(), picture.getTags());
                    }
                }
            } else {
                Log.e(TAG, "Clarifai: " + result.getStatusMessage());
            }
        } else {
            Log.d(TAG, "There was an error recognizing the image.");
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
