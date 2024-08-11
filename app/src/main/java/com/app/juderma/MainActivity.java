package com.app.juderma;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout; // Ensure this import is present
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PHOTO_POPUP = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private ImageView imageView;
    private TextView textSelect;
    private Button analyzeButton;
    private Uri selectedImageUri;
    private ProgressBar progressBar;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        imageView = findViewById(R.id.image_view);
        textSelect = findViewById(R.id.text_select);
        analyzeButton = findViewById(R.id.analyze_button);
        progressBar = findViewById(R.id.progress_bar);

        Button selectImg = findViewById(R.id.select_photo_button);
        selectImg.setOnClickListener(v -> launchAddPhotoPopup());

        analyzeButton.setOnClickListener(v -> analyzeImage());

        handleIntent(getIntent());

        setupBottomNavigation();

        requestPermissions();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                // Home activity is already open, no need to start it again
                return true;
            } else if (itemId == R.id.navigation_history) {
                // Navigate to HistoryActivity and clear the activity stack
                intent = new Intent(MainActivity.this, HistoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else if (itemId == R.id.navigation_settings) {
                // Navigate to SettingsActivity and clear the activity stack
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }

            if (intent != null) {
                startActivity(intent);
                finish(); // Optional: finish current activity
                return true;
            }

            return false;
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String imageUriString = intent.getStringExtra("imageUri");
            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                imageView.setImageURI(imageUri);
            }
        }
    }

    private void requestPermissions() {
        if (!hasCameraPermission() || hasStoragePermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            handleCameraPermissionResult(grantResults);
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            handleStoragePermissionResult(grantResults);
        }
    }

    private void handleCameraPermissionResult(@NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        } else {
            showToast("Camera permission denied. The app may not function properly.");
        }
    }

    private void handleStoragePermissionResult(@NonNull int[] grantResults) {
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            showToast("Storage permission denied. The app may not function properly.");
        }
    }

    private void requestStoragePermission() {
        if (hasStoragePermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    private void launchAddPhotoPopup() {
        Intent intent = new Intent(this, AddPhotoPopup.class);
        startActivityForResult(intent, REQUEST_PHOTO_POPUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_PHOTO_POPUP) {
                handlePhotoPopupResult(data);
            }
        } else {
            android.util.Log.e("MainActivity", "Result not OK or data is null");
        }
    }

    private void handlePhotoPopupResult(Intent data) {
        String uriString = data.getStringExtra("imageUri");
        if (uriString != null) {
            selectedImageUri = Uri.parse(uriString);
            if (selectedImageUri != null) {
                new LoadImageTask().execute(selectedImageUri);
            } else {
                android.util.Log.e("MainActivity", "Failed to parse Uri");
                showToast("Failed to parse image Uri");
            }
        } else {
            android.util.Log.e("MainActivity", "No imageUri received in intent");
            showToast("No image Uri received");
        }
    }

    private void analyzeImage() {
        if (selectedImageUri == null) {
            showToast("Please select an image first");
            return;
        }

        try {
            // Show the analyzing view
            findViewById(R.id.analyzing_view).setVisibility(View.VISIBLE);

            // Simulate image analysis with a delay
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(this, ModelActivity.class);
                intent.putExtra("imageUri", selectedImageUri.toString());
                startActivity(intent);
                findViewById(R.id.analyzing_view).setVisibility(View.GONE);
            }, 3000); // Simulated delay of 3 seconds

        } catch (Exception e) {
            showToast("Error accessing image");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Uri... uris) {
            try (InputStream inputStream = getContentResolver().openInputStream(uris[0])) {
                if (inputStream != null) {
                    return BitmapFactory.decodeStream(inputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                Glide.with(MainActivity.this)
                        .load(bitmap)
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
                textSelect.setVisibility(View.GONE);
                analyzeButton.setVisibility(View.VISIBLE);
            } else {
                showToast("Failed to load image");
            }
            progressBar.setVisibility(View.GONE);
        }
    }
}
