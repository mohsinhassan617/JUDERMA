package com.app.juderma;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.tensorflow.lite.Interpreter;

public class ModelDownloadManager {

    private static final String MODEL_FILENAME = "skin.tflite";
    private static final String TAG = "ModelDownloadManager";
    private Context context;

    public ModelDownloadManager(Context context) {
        this.context = context;
    }

    public Interpreter getInterpreter() throws IOException {
        File modelFile = new File(context.getFilesDir(), MODEL_FILENAME);
        if (!modelFile.exists()) {
            copyModelToFile(modelFile);
        }
        return new Interpreter(loadModelFile(modelFile));
    }

    private void copyModelToFile(File modelFile) throws IOException {
        try (InputStream inputStream = context.getAssets().open(MODEL_FILENAME);
             FileOutputStream outputStream = new FileOutputStream(modelFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            Log.i(TAG, "Model file copied to internal storage.");
        } catch (IOException e) {
            Log.e(TAG, "Error copying model file", e);
            throw e;
        }
    }

    private MappedByteBuffer loadModelFile(File modelFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(modelFile);
             FileChannel fileChannel = inputStream.getChannel()) {
            long startOffset = 0;
            long declaredLength = fileChannel.size();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            Log.e(TAG, "Error loading model file", e);
            throw e;
        }
    }
}
