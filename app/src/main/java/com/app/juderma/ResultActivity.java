package com.app.juderma;

import static android.content.Intent.getIntent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    ImageView resultImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Initialize views
        resultImageView = findViewById(R.id.result_image_view);

        // Get the URI or Bitmap of the selected image from the intent or wherever you stored it
        Bitmap selectedImageBitmap = getIntent().getParcelableExtra("selected_image_bitmap");

        // Set the selected image as the placeholder
        if (selectedImageBitmap != null) {
            resultImageView.setImageBitmap(selectedImageBitmap);
        } else {
            // If no selected image, you can set a default placeholder image
            resultImageView.setImageResource(R.drawable.placeholder_image);
        }
    }
}
