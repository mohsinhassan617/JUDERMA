package com.app.juderma;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final Context context;
    private final List<HistoryItem> historyList;

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

        holder.timestampTextView.setText(item.getTimestamp() != null ? item.getTimestamp() : "No timestamp");
        holder.predictionTextView.setText(item.getPrediction() != null ? item.getPrediction() : "No prediction");
        holder.descriptionTextView.setText(item.getDescription() != null ? item.getDescription() : "No description");

        Uri imageUri = item.getImageUri();
        if (imageUri != null) {
            Log.d("HistoryAdapter", "Image URI: " + imageUri.toString());
            Glide.with(context)
                    .load(imageUri)
                    .override(200, 200)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.ic_error_circle)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("Glide", "Load failed", e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_error_circle);
            Log.w("HistoryAdapter", "Image URI is null for position " + position);
        }
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

    @SuppressLint("Range")
    private String getRealPathFromURI(Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            cursor.close();
        }
        return path;
    }
}
