    package com.example.notes.Adptor;

    import android.annotation.SuppressLint;
    import android.app.AlertDialog;
    import android.app.Dialog;
    import android.content.Context;
    import android.net.Uri;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.view.Window;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;
    import androidx.viewpager2.widget.ViewPager2;

    import com.bumptech.glide.Glide;
    import com.example.notes.R;
    import com.example.notes.database.AppDatabase;
    import com.example.notes.database.Note;
    import com.example.notes.database.NoteDao;

    import java.io.File;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private static final String TAG = "ImageAdapter";
        private Context context;
        private List<String> imagePaths;
        AppDatabase database;

        public ImageAdapter(Context context, List<String> imagePaths, AppDatabase database) {
            this.context = context;
            this.imagePaths = imagePaths;
            this.database = database;
        }



        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            String imagePath = imagePaths.get(position);

            if (imagePath == null || imagePath.isEmpty()) {
                Log.e(TAG, "Image path is null or empty at position: " + position);
                return;
            }

            if (imagePath.startsWith("content://")) {
                // Gallery Image
                Log.d(TAG, "Loading gallery image: " + imagePath);
                Glide.with(context)
                        .load(Uri.parse(imagePath))
                        .into(holder.imageView);
            } else {
                // Camera Image (Stored in internal storage)
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Log.d(TAG, "Loading camera image: " + imagePath);
                    Glide.with(context)
                            .load(imageFile)
                            .into(holder.imageView);
                } else {
                    Log.e(TAG, "File does not exist: " + imagePath);
                }
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVideoDialog(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "Total image count: " + imagePaths.size());
            return imagePaths.size();
        }

        public void addImage(String imagePath) {
            imagePaths.add(imagePath);
            notifyItemInserted(imagePaths.size() - 1);  // Update only the new item
        }

        private void showVideoDialog(int position) {

            Dialog dialog = new Dialog(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.zoom_out_dilog);
            dialog.setCancelable(false);
            ImageView close = dialog.findViewById(R.id.close);
            TextView deitem = dialog.findViewById(R.id.delectitem);
            ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);


            VideoPagerAdapter videoPagerAdapter = new VideoPagerAdapter(context, imagePaths);
            viewPager.setAdapter(videoPagerAdapter);


            if (position >= 0 && position < imagePaths.size()) {
                viewPager.setCurrentItem(position, false);
            } else {
                Log.e("ViewPager", "Position out of bounds: " + position);
            }

            close.setOnClickListener(v -> dialog.dismiss());
            deitem.setOnClickListener(v -> {
                android.app.AlertDialog.Builder dialog1 = new android.app.AlertDialog.Builder(v.getRootView().getContext());
                AlertDialog OptionDialog = dialog1.create();
                View dedialog = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.delete_dilog, null);

                Button ybutton = dedialog.findViewById(R.id.yesbutton);
                Button nbutton = dedialog.findViewById(R.id.nobytton);
                OptionDialog.setCancelable(false);
                OptionDialog.setView(dedialog);
                ybutton.setOnClickListener(v1 -> {
                    removeVideo(position);
                    OptionDialog.dismiss();
                    dialog.dismiss();
                });
                nbutton.setOnClickListener(v1 -> OptionDialog.dismiss());
                OptionDialog.show();
            });

            dialog.show();

        }
        private void removeVideo(int position) {
            if (position >= 0 && position < imagePaths.size()) {
                String imagePath = imagePaths.get(position);

                // Fetch Note from Database
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    NoteDao noteDao = database.noteDao();
                    Note note = noteDao.getNoteByImagePath(imagePath);

                    if (note != null) {
                        // Convert Immutable List to Mutable List
                        List<String> mutableImagePaths = new ArrayList<>(note.imagePaths);
                        mutableImagePaths.remove(imagePath); // Now safe to remove

                        // Update the Note
                        note.imagePaths = mutableImagePaths; // Assign updated list
                        if (mutableImagePaths.isEmpty() && (note.noteText == null || note.noteText.isEmpty()) &&
                                (note.getChecklist() == null || note.getChecklist().isEmpty())) {
                            noteDao.delete(note); // Delete note if empty
                            Log.d(TAG, "Note deleted because it became empty: " + note.id);
                        } else {
                            noteDao.update(note); // Update only if some data remains
                            Log.d(TAG, "Image removed from note in database: " + imagePath);
                        }
                    }
                });

                // Delete Image from Storage (if needed)
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    boolean deleted = imageFile.delete();
                    Log.d(TAG, deleted ? "File deleted: " + imagePath : "Failed to delete file: " + imagePath);
                }

                // Remove Image from Adapter & Refresh UI
                imagePaths.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, imagePaths.size());
            } else {
                Log.e(TAG, "Invalid position for deletion: " + position);
            }
        }






        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
