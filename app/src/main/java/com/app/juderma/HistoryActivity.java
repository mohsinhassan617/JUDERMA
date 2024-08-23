package com.app.juderma;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Collections;
import org.chromium.base.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";
    private HistoryAdapter historyAdapter;
    private final List<HistoryItem> historyList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerView = findViewById(R.id.history_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.empty_state_text_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(this, historyList);
        recyclerView.setAdapter(historyAdapter);

        loadHistoryItems();
        setupBottomNavigation();
    }

    private void loadHistoryItems() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        historyList.clear(); // Clear existing items

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (String key : allEntries.keySet()) {
            if (key.endsWith("_timestamp")) {
                String baseKey = key.replace("_timestamp", "");
                String timestamp = sharedPreferences.getString(baseKey + "_timestamp", "");
                String prediction = sharedPreferences.getString(baseKey + "_prediction", "");
                String description = sharedPreferences.getString(baseKey + "_description", "");
                String imageUriString = sharedPreferences.getString(baseKey + "_imageUri", "");

                Uri imageUri = imageUriString.isEmpty() ? null : Uri.parse(imageUriString);

                HistoryItem item = new HistoryItem(timestamp, prediction, description, imageUri);
                historyList.add(item);

                Log.d(TAG, "Loaded item - Timestamp: " + timestamp + ", Prediction: " + prediction);
            }
        }

        // Sort the list by timestamp (newest first)
        Collections.sort(historyList, (item1, item2) -> item2.getTimestamp().compareTo(item1.getTimestamp()));

        historyAdapter.notifyDataSetChanged();
        updateUIState();
    }

    private void updateUIState() {
        progressBar.setVisibility(View.GONE);
        if (historyList.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText("No history items found");
        } else {
            emptyStateTextView.setVisibility(View.GONE);
        }
    }


    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                // Navigate to MainActivity and clear the activity stack
                intent = new Intent(HistoryActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else if (itemId == R.id.navigation_history) {
                // History activity is already open, no need to start it again
                return true;
            } else if (itemId == R.id.navigation_settings) {
                // Navigate to SettingsActivity and clear the activity stack
                intent = new Intent(HistoryActivity.this, SettingsActivity.class);
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
