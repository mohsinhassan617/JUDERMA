package com.app.juderma;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.chromium.base.Log;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private Context context;
    private List<HistoryItem> historyList;

    public HistoryAdapter(Context context, List<HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);

        if (item.getTimestamp() != null) {
            holder.timestampTextView.setText(item.getTimestamp());
        } else {
            holder.timestampTextView.setText("No timestamp");
        }

        if (item.getPrediction() != null) {
            holder.predictionTextView.setText(item.getPrediction());
        } else {
            holder.predictionTextView.setText("No prediction");
        }

        if (item.getDescription() != null) {
            holder.descriptionTextView.setText(item.getDescription());
        } else {
            holder.descriptionTextView.setText("No description");
        }

        // Load image using Glide
        Glide.with(context)
                .load(item.getImageUri())
                .placeholder(R.drawable.placeholder_image) // Optional: placeholder image while loading
                .error(R.drawable.ic_error_circle) // Optional: error image if loading fails
                .into(holder.imageView);
    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timestampTextView;
        TextView predictionTextView;
        TextView descriptionTextView;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            predictionTextView = itemView.findViewById(R.id.prediction_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            imageView = itemView.findViewById(R.id.image_view);

            if (timestampTextView == null) {
                Log.e("HistoryAdapter", "Timestamp TextView is null. Check layout file and ID.");
            }
            if (predictionTextView == null) {
                Log.e("HistoryAdapter", "Prediction TextView is null. Check layout file and ID.");
            }
            if (descriptionTextView == null) {
                Log.e("HistoryAdapter", "Description TextView is null. Check layout file and ID.");
            }
            if (imageView == null) {
                Log.e("HistoryAdapter", "ImageView is null. Check layout file and ID.");
            }
        }


    }
}
