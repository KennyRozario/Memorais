package com.example.kenny.memorais;

import android.graphics.Bitmap;
import android.util.Log;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.exception.ClarifaiException;

import java.io.ByteArrayOutputStream;

/**
 * Created by Kenny on 2016-03-01.
 */
public class ClarifaiService {

    private static final String TAG = ClarifaiService.class.getSimpleName();

    private static final ClarifaiClient mClient = new ClarifaiClient(com.example.kenny.memorais.Credentials.CLIENT_ID,
            com.example.kenny.memorais.Credentials.CLIENT_SECRET);

    // Pre: Requires a bitmap to be passed in
    // Post: Compresses the bitmap, sends it to Clarifai,
    // gets a recognition request (Tags and respective probabilities)
    public static RecognitionResult recognizeBitmap(Bitmap bitmap) {
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

}
