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
import android.text.util.Linkify;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

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
        Log.d(TAG, "Received URI string: " + uriString);
        if (uriString != null) {
            Uri uri = Uri.parse(uriString);
            Log.d(TAG, "Parsed URI: " + uri);

            loadImageAndProcess(uri);
        } else {
            Log.e(TAG, "No image URI provided");
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupBottomNavigation();
    }

    private void loadImageAndProcess(Uri imageUri) {
        try {
            Bitmap imageBitmap = loadImageFromUri(imageUri);
            if (imageBitmap != null) {
                imageBitmap = resizeBitmap(imageBitmap, IMAGE_SIZE, IMAGE_SIZE);
                resultImageView.setImageBitmap(imageBitmap);
                processImage(imageBitmap, imageUri);
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
        String description = getDescriptionForClass(resultLabel);
        classDescriptionTextView.setText(description);

        // Apply Linkify to make links clickable
        Linkify.addLinks(classDescriptionTextView, Linkify.WEB_URLS);

        // Save the result to history
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        saveResult(timestamp, resultLabel, description, imageUri);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private String getDescriptionForClass(String classLabel) {
        switch (classLabel) {
            case "Acne":
                return "Acne is a common skin condition that occurs when hair follicles become clogged with oil and dead skin cells. It typically appears on the face, forehead, chest, upper back, and shoulders. Acne can present itself in various forms such as whiteheads, blackheads, pimples, cysts, and nodules. This condition is most common among teenagers, though it affects people of all ages. Treatment options range from topical creams to oral medications, depending on the severity.  \n For more information, visit the [Wikipedia page] \n https://en.wikipedia.org/wiki/Acne";

            case "Carcinoma":
                return "Carcinoma is a type of cancer that starts in the skin cells or the tissue lining internal organs. The most common forms of skin carcinoma include basal cell carcinoma and squamous cell carcinoma. Both types can spread to other parts of the body if not treated early. Carcinomas typically present as new growths or sores that do not heal, often in sun-exposed areas of the skin. Early detection and treatment are crucial for a good prognosis. \n For more information, visit the [Wikipedia page] \n https://en.wikipedia.org/wiki/Carcinoma";

            case "Eczema":
                return "Eczema, also known as atopic dermatitis, is a chronic skin condition that causes the skin to become inflamed, itchy, and red. It often appears in childhood but can persist into adulthood. Eczema can affect any part of the body, but it is most common on the face, hands, and inside the elbows and knees. The exact cause of eczema is not known, but it is believed to be linked to an overactive immune response. Treatment usually involves moisturizing creams, topical steroids, and avoiding triggers. \n For more information, visit the [Wikipedia page] \n https://en.wikipedia.org/wiki/Eczema";

            case "Keratosis":
                return "Keratosis is a growth of keratin on the skin or on mucous membranes stemming from keratinocytes, the prominent cell type in the epidermis. The most common types are seborrheic keratosis and actinic keratosis. While seborrheic keratosis is generally benign and non-cancerous, actinic keratosis can develop into squamous cell carcinoma if left untreated. These growths often appear as rough, scaly patches on sun-exposed areas of the skin. Treatment may involve cryotherapy, laser therapy, or topical medications. \n For more information, visit the [Wikipedia page]  \n https://en.wikipedia.org/wiki/Keratosis";

            case "Milia":
                return "Milia are small, white cysts that typically appear on the face, particularly around the eyes and cheeks. They occur when keratin becomes trapped beneath the surface of the skin. Milia are common in newborns but can also appear in people of all ages. Unlike acne, milia are not related to blocked pores or inflammation, and they typically resolve on their own. In some cases, they may be removed by a dermatologist if they persist. \n For more information, visit the [Wikipedia page]  \n https://en.wikipedia.org/wiki/Milia";

            case "Rosacea":
                return "Rosacea is a chronic skin condition characterized by facial redness, swelling, and sometimes acne-like bumps. It typically affects the central part of the face, including the nose, cheeks, and forehead. The exact cause of rosacea is unknown, but it is believed to involve a combination of genetic and environmental factors. Common triggers include sun exposure, stress, spicy foods, and alcohol. While there is no cure, treatments like topical creams, oral medications, and laser therapy can help manage symptoms. \n For more information, visit the [Wikipedia page]  \n https://en.wikipedia.org/wiki/Rosacea";

            case "Healthy Skin":
                return "Healthy skin is typically smooth, hydrated, and free of visible blemishes or abnormalities. It functions as a protective barrier against environmental factors such as bacteria, UV rays, and pollution. Maintaining healthy skin involves a balanced diet, regular cleansing, moisturizing, and the use of sunscreen to protect against sun damage. Regular skin check-ups can also help detect any potential skin issues early on. \n For more information, visit the [Wikipedia page]  \n https://en.wikipedia.org/wiki/Skin";

            case "Other":
                return "The skin condition identified does not match any of the specific categories in the current dataset. It may represent a less common skin issue or one that requires further medical evaluation for accurate diagnosis and treatment. In such cases, consulting a healthcare professional is recommended. \n For more information, visit the [Wikipedia page]  \n https://en.wikipedia.org/wiki/Skin_condition";

            default:
                return "No description available for this skin condition.";
        }
    }

    private void saveResult(String timestamp, String prediction, String description, Uri imageUri) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String baseKey = "result_" + timestamp.replace(" ", "_").replace(":", "_");
        editor.putString(baseKey + "_timestamp", timestamp);
        editor.putString(baseKey + "_prediction", prediction);
        editor.putString(baseKey + "_description", description);
        editor.putString(baseKey + "_imageUri", imageUri.toString());
        editor.apply();
        Log.d(TAG, "Saved item - Timestamp: " + timestamp + ", Prediction: " + prediction);
    }



    private MappedByteBuffer loadModelFile() throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(getAssets().openFd(MODEL_FILENAME).getFileDescriptor());
             FileChannel fileChannel = fileInputStream.getChannel()) {
            long startOffset = getAssets().openFd(MODEL_FILENAME).getStartOffset();
            long declaredLength = getAssets().openFd(MODEL_FILENAME).getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_history) {
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
