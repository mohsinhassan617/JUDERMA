package com.app.juderma;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddPhotoPopup extends AppCompatActivity {
    private static final String TAG = "AddPhotoPopup";
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    returnResult(imageUri);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    Uri cachedUri = copyImageToCache(selectedImageUri);
                    if (cachedUri != null) {
                        returnResult(cachedUri);
                    } else {
                        Toast.makeText(this, "Failed to copy image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo_popup);

        ImageView captureButton = findViewById(R.id.imageView4);
        ImageView selectButton = findViewById(R.id.imageView5);

        captureButton.setOnClickListener(v -> captureImage());
        selectButton.setOnClickListener(v -> selectImage());
        setupBottomNavigation();
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile();
        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(this,
                    "com.app.juderma.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() {
        File cacheDir = getCacheDir();
        File imageFile = null;
        try {
            imageFile = File.createTempFile("JPEG_", ".jpg", cacheDir);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
        return imageFile;
    }

    private Uri copyImageToCache(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream != null) {
                File outputFile = createImageFile();
                OutputStream outputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                return Uri.fromFile(outputFile);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error copying image to cache", e);
        }
        return null;
    }

    private void returnResult(Uri uri) {
        if (uri != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("imageUri", uri.toString());
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                finish(); // Go back to MainActivity
                return true;
            } else if (itemId == R.id.navigation_history) {
                startActivity(new Intent(AddPhotoPopup.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.navigation_settings) {
                startActivity(new Intent(AddPhotoPopup.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}