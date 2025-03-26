package com.example.notes.Adptor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.notes.NotesEditorActivity;
import com.example.notes.R;
import com.example.notes.database.Note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final Context context;
    private List<Note> notesList;
    private final NoteClickListener noteClickListener;
    private boolean isGridView;

    public NotesAdapter(Context context, List<Note> notesList, NoteClickListener noteClickListener, boolean isGridView) {
        this.context = context;
        this.notesList = notesList != null ? notesList : new ArrayList<>();
        this.noteClickListener = noteClickListener;
        this.isGridView = isGridView;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = notesList.get(position);

        // Set card background color & pin visibility
        holder.cardView.setCardBackgroundColor(note.color);
        holder.pin.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

        // Hide title if empty
        if (note.getNoteTttale() != null && !note.getNoteTttale().isEmpty()) {
            holder.notet.setText(note.getNoteTttale());
            holder.notet.setVisibility(View.VISIBLE);
        } else {
            holder.notet.setVisibility(View.GONE);
        }

        // Hide date if empty
        if (note.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.datee.setText(dateFormat.format(note.getDate()));
            holder.datee.setVisibility(View.VISIBLE);
        } else {
            holder.datee.setVisibility(View.GONE);
        }

        // Set note text or checklist, hide if empty
        if (note.getChecklist() != null && !note.getChecklist().isEmpty()) {
            holder.noteText.setText(note.getChecklist());
            holder.noteText.setVisibility(View.VISIBLE);
        } else if (note.noteText != null && !note.noteText.isEmpty()) {
            holder.noteText.setText(note.noteText);
            holder.noteText.setVisibility(View.VISIBLE);
        } else {
            holder.noteText.setVisibility(View.GONE);
        }

        // Load image with error handling
        if (note.imagePaths != null && !note.imagePaths.isEmpty()) {
            Uri imageUri = getValidUri(note.imagePaths.get(0));

            if (imageUri != null) {
                Glide.with(context)
                        .load(imageUri)
                        .centerCrop()
                        .error(R.drawable.ic_launcher) // Add an error image if loading fails
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original and resized images
                        .into(holder.noteImage);

                holder.noteImage.setVisibility(View.VISIBLE);
                holder.cardview2.setVisibility(View.VISIBLE);
            } else {
                holder.noteImage.setVisibility(View.GONE);
                holder.cardview2.setVisibility(View.GONE);
            }
        } else {
            holder.noteImage.setVisibility(View.GONE);
            holder.cardview2.setVisibility(View.GONE);
        }


        // Click to open note editor
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NotesEditorActivity.class);
            intent.putExtra("note_id", note.id);
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        });

        // Long press listener
        holder.itemView.setOnLongClickListener(v -> {
            if (noteClickListener != null) {
                noteClickListener.onNoteLongPress(note);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notesList != null ? notesList.size() : 0;
    }

    // Convert file path to URI safely
    private Uri getValidUri(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return null;

        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            return Uri.parse(imagePath);
        } else {
            File file = new File(imagePath);
            if (file.exists()) {
                return Uri.fromFile(file);
            } else {
                Log.e("NotesAdapter", "File does not exist: " + imagePath);
                return null;
            }
        }
    }

    // Get image from URI safely
    public Bitmap getImageFromUri(Context context, Uri imageUri) {
        if (imageUri == null) {
            Log.e("NotesAdapter", "Invalid image URI: null");
            return null;
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            Log.e("NotesAdapter", "Error loading image from URI: " + imageUri, e);
            return null;
        }
    }

    // Update notes list
    public void updateNotes(List<Note> notes) {
        this.notesList = notes != null ? notes : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Update a specific note
    public void updateNote(List<Note> newNotes) {
        if (this.notesList == null) {
            this.notesList = new ArrayList<>();
        } else {
            this.notesList.clear();
        }

        if (newNotes != null) {
            this.notesList.addAll(newNotes);
        }

        notifyDataSetChanged();
    }

    // Toggle view mode between grid & list
    public void updateViewMode(boolean isGrid) {
        this.isGridView = isGrid;
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteText, notet, datee;
        ImageView noteImage, pin;
        CardView cardView,cardview2;

        NoteViewHolder(View itemView) {
            super(itemView);
            notet = itemView.findViewById(R.id.notetital);
            noteText = itemView.findViewById(R.id.noteText);
            noteImage = itemView.findViewById(R.id.noteImage);
            pin = itemView.findViewById(R.id.pinIcon);
            datee = itemView.findViewById(R.id.date);
            cardView = itemView.findViewById(R.id.cardview);
            cardview2 = itemView.findViewById(R.id.cardview2);
        }
    }

    // Interface for long press event
    public interface NoteClickListener {
        void onNoteLongPress(Note note);
    }
}
