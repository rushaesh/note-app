package com.example.notes.Adptor;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notes.R;

import java.util.List;

public class VideoPagerAdapter extends RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder> {
    private Context context;
    private List<String> videoPaths;

    public VideoPagerAdapter(Context context, List<String> videoPaths) {
        this.context = context;
        this.videoPaths = videoPaths;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new VideoView for each item in the ViewPager2
        View view = LayoutInflater.from(context).inflate(R.layout.item_video_page, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        String videoPath = videoPaths.get(position);


        Glide.with(holder.imageView.getContext())
                .load(videoPaths.get(position))
                .into(holder.imageView);


    }


    @Override
    public int getItemCount() {
        return videoPaths.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;


        public VideoViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageviw1);

        }
    }
}
