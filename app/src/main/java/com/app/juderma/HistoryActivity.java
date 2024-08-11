package com.app.juderma;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.preference.PreferenceManager;


public class HistoryActivity extends AppCompatActivity {
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView historyRecyclerView = findViewById(R.id.history_recycler_view);

        // Initialize the list and adapter
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(this, historyList);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);

        // Fetch history data and update the list
        fetchHistoryData();

        setupBottomNavigation();
    }

    private void fetchHistoryData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String historyData = sharedPreferences.getString("history", "");
        String[] entries = historyData.split("\n");
        historyList.clear();

        for (String entry : entries) {
            // Assuming entries are in the format: TIMESTAMP | PREDICTION | DESCRIPTION | IMAGE_URI
            String[] parts = entry.split(" \\| ");
            if (parts.length == 4) {
                String timestamp = parts[0];
                String prediction = parts[1];
                String description = parts[2];
                Uri imageUri = Uri.parse(parts[3]);
                historyList.add(new HistoryItem(timestamp, prediction, description, imageUri));
            }
        }
        historyAdapter.notifyDataSetChanged();
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
