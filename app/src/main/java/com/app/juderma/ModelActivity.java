package com.app.juderma;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentResolver;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.DataType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ModelActivity extends AppCompatActivity {
    private static final String TAG = "ModelActivity";
    private static final int IMAGE_SIZE = 150;
    private static final String MODEL_FILENAME = "skin.tflite";

    private Interpreter interpreter;
    private ImageView resultImageView;
    private TextView resultTextView;
    private TextView classDescriptionTextView;
    private final String[] classLabels = {"Acne", "Carcinoma", "Eczema", "Keratosis", "Milia", "Rosacea", "Healthy Skin", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultImageView = findViewById(R.id.result_image_view);
        resultTextView = findViewById(R.id.result_text_view);
        classDescriptionTextView = findViewById(R.id.class_description_text_view);

        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            Log.e(TAG, "Failed to load model", e);
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            File imageFile = new File(Uri.parse(uriString).getPath());
            if (imageFile.exists()) {
                loadImageAndProcess(imageFile);
            } else {
                Log.e(TAG, "Image file does not exist");
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e(TAG, "No image URI provided");
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupBottomNavigation();
    }

    private void loadImageAndProcess(File imageFile) {
        try {
            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (imageBitmap != null) {
                imageBitmap = resizeBitmap(imageBitmap, IMAGE_SIZE, IMAGE_SIZE);
                resultImageView.setImageBitmap(imageBitmap);
                processImage(imageBitmap, Uri.fromFile(imageFile));
            } else {
                Log.e(TAG, "Failed to load image");
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void processImage(Bitmap bitmap, Uri imageUri) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(intValues, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

        float[] floatValues = new float[IMAGE_SIZE * IMAGE_SIZE * 3];
        for (int i = 0; i < intValues.length; ++i) {
            floatValues[i * 3 + 0] = ((intValues[i] >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((intValues[i] >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (intValues[i] & 0xFF) / 255.0f;
        }

        TensorBuffer inputTensor = TensorBuffer.createFixedSize(new int[]{1, IMAGE_SIZE, IMAGE_SIZE, 3}, DataType.FLOAT32);
        inputTensor.loadArray(floatValues);

        TensorBuffer outputTensor = TensorBuffer.createFixedSize(new int[]{1, classLabels.length}, DataType.FLOAT32);
        interpreter.run(inputTensor.getBuffer(), outputTensor.getBuffer());

        float[] output = outputTensor.getFloatArray();
        int maxIndex = 0;
        for (int i = 1; i < output.length; ++i) {
            if (output[i] > output[maxIndex]) {
                maxIndex = i;
            }
        }

        String resultLabel = classLabels[maxIndex];
        resultTextView.setText(resultLabel);
        classDescriptionTextView.setText(getDescriptionForClass(resultLabel));

        // Save the result to history
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        saveResult(timestamp, resultLabel, getDescriptionForClass(resultLabel), imageUri);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private String getDescriptionForClass(String classLabel) {
        // Implement descriptions for each class
        switch (classLabel) {
            case "Acne":
                return "Acne is a skin condition characterized by pimples, blackheads, and whiteheads.";
            case "Carcinoma":
                return "Carcinoma is a type of skin cancer that begins in the cells that make up the outer layer of the skin.";
            // Add descriptions for other classes
            default:
                return "No description available for this condition.";
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        File modelFile = new File(getFilesDir(), MODEL_FILENAME);
        if (!modelFile.exists()) {
            copyModelToFile(modelFile);
        }
        try (FileInputStream inputStream = new FileInputStream(modelFile);
             FileChannel fileChannel = inputStream.getChannel()) {
            long startOffset = 0;
            long declaredLength = fileChannel.size();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private void copyModelToFile(File modelFile) throws IOException {
        try (InputStream inputStream = getAssets().open(MODEL_FILENAME);
             OutputStream outputStream = Files.newOutputStream(modelFile.toPath())) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
    }

    private Bitmap loadImageFromUri(Uri uri) throws IOException {
        ContentResolver contentResolver = getContentResolver();
        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            } else {
                throw new IOException("Unable to open image stream for URI: " + uri.toString());
            }
        }
    }

    private void saveResult(String timestamp, String prediction, String description, Uri imageUri) {
        // Implement saving result to history (e.g., using SharedPreferences or a database)
        // This is a placeholder implementation
        Log.d(TAG, "Saving result: " + timestamp + " - " + prediction);
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
                intent = new Intent(ModelActivity.this, HistoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else if (itemId == R.id.navigation_settings) {
                // Navigate to SettingsActivity and clear the activity stack
                intent = new Intent(ModelActivity.this, SettingsActivity.class);
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



}