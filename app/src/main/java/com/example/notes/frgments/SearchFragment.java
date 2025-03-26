package com.example.notes.frgments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.notes.Adptor.NotesAdapter;
import com.example.notes.R;
import com.example.notes.database.AppDatabase;
import com.example.notes.database.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SearchFragment extends Fragment implements NotesAdapter.NoteClickListener {
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private NotesAdapter adapter;
    private ImageView imageView;
    private List<Note> archivedNotes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();
    private AppDatabase database;
    private TextView textView;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.requestFocus(); // Request focus for the EditText

        // Show the keyboard automatically
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        recyclerView = view.findViewById(R.id.recyclerView);
        imageView = view.findViewById(R.id.back);
        textView = view.findViewById(R.id.nodata);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        database = AppDatabase.getInstance(getContext());
        adapter = new NotesAdapter(getContext(), filteredNotes, this, false);
        recyclerView.setAdapter(adapter);

        loadArchivedNotes();
        setupSearch();

        return view;
    }
    public void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    private void loadArchivedNotes() {
        Log.d("SearchFragment", "Loading archived notes...");

        database.noteDao().getAllNote().observe(getViewLifecycleOwner(), notes -> {
            archivedNotes.clear();
            archivedNotes.addAll(notes);
            filterNotes(""); // Show all initially
            Log.d("SearchFragment", "Archived notes loaded: " + archivedNotes.size());
            if (archivedNotes.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotes(String query) {
        filteredNotes.clear();
        for (Note note : archivedNotes) {
            if (note.getNoteTttale().toLowerCase().contains(query.toLowerCase())) {
                filteredNotes.add(note);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNoteLongPress(Note note) {
        showNoteOptionsDialog(note);
    }

    public void showNoteOptionsDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme);


        // Creating the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_note_options, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Finding views inside the custom dialog
        TextView pinText = dialogView.findViewById(R.id.textPin);
        ImageView pinIcon = dialogView.findViewById(R.id.iconPin);

        // Update Pin text/icon based on status
        if (note.isPinned()) {
            pinText.setText("Unpin");
            pinIcon.setImageResource(R.drawable.unpin_svgrepo_com);
        } else {
            pinText.setText("Pin");
            pinIcon.setImageResource(R.drawable.pin_svgrepo_com);
        }

        // Archive Click
        dialogView.findViewById(R.id.layoutArchive).setOnClickListener(v -> {
            archiveNote(note);
            dialog.dismiss();
        });

        // Delete Click
        dialogView.findViewById(R.id.layoutDelete).setOnClickListener(v -> {
            deleteNote(note);
            dialog.dismiss();
        });

        // Pin Click
        dialogView.findViewById(R.id.layoutPin).setOnClickListener(v -> {
            togglePinStatus(note);
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(dpToPx(370), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

    }
    public  int dpToPx(int dp) {
        return (int) (dp * this.getResources().getDisplayMetrics().density);
    }

    private void archiveNote(Note note) {
        note.setArchived(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            database.noteDao().update(note);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Note Archived", Toast.LENGTH_SHORT).show();
                refreshNotesList(); // Remove from current list
            });
        });
    }

    private void deleteNote(Note note) {
        note.setTrashed(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            database.noteDao().update(note);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Note Moved to Trash", Toast.LENGTH_SHORT).show();
                refreshNotesList(); // Remove from current list
            });
        });
    }

    private void togglePinStatus(Note note) {
        boolean newPinStatus = !note.isPinned();
        note.setPinned(newPinStatus);

        Executors.newSingleThreadExecutor().execute(() -> {
            database.noteDao().update(note);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), newPinStatus ? "Pinned" : "Unpinned", Toast.LENGTH_SHORT).show();
                refreshNotesList();
            });
        });
    }

    private void refreshNotesList() {
        loadArchivedNotes(); // Reload the archived notes list
    }
}
