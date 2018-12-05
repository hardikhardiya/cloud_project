package com.example.maikhar.tflite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_IMAGE = 1;
    private static final int GALLERY_IMAGE = 2;
    private ImageView imageView;
    Button cameraButton;
    Button galleryButton;

    private static final String TAG = "TfLiteCameraDemo";

    /** Name of the model file stored in Assets. */
    private static final String MODEL_PATH = "optimized_graph.lite";

    /** Name of the label file stored in Assets. */
    private static final String LABEL_PATH = "retrained_labels.txt";

    /** Number of results to show in the UI. */
    private static final int RESULTS_TO_SHOW = 3;

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    static final int DIM_IMG_SIZE_X = 224;
    static final int DIM_IMG_SIZE_Y = 224;

    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;


    /* Preallocated buffers for storing image data in. */
    private int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(myIntent, CAMERA_IMAGE);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent2 = new Intent();
                myIntent2.setType("image/*");
                myIntent2.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(myIntent2.createChooser(myIntent2, "Select from Gallery"), GALLERY_IMAGE);

            }
        });



    }
    protected void onActivityResult(int reqCode, int resCode, Intent imageData){
        if (reqCode == CAMERA_IMAGE && resCode == Activity.RESULT_OK) {
            Uri imageUrl = imageData.getData();
            imageView.setImageURI(imageUrl);

        } else if (reqCode == GALLERY_IMAGE && resCode == RESULT_OK && imageData != null) {
            Uri imageUrl = imageData.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUrl);
                convertBitmapToByteBuffer(bitmap);
                long startTime = SystemClock.uptimeMillis();
                tflite.run(imageData, labelProbArray);
                long endTime = SystemClock.uptimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageURI(imageUrl);

        }


    }
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        //imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                bitmap.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                bitmap.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                bitmap.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
        long endTime = SystemClock.uptimeMillis();
        //Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
    }
}
