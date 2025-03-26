package com.example.notes.frgments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.database.AppDatabase;
import com.example.notes.database.Note;
import com.example.notes.Adptor.NotesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class TrashFragment extends Fragment implements NotesAdapter.NoteClickListener {

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private NotesAdapter adapter;
    private List<Note> trashedNotes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();
    private AppDatabase database;
    ImageView imageView;
    TextView textView;

    public TrashFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        searchEditText = view.findViewById(R.id.searchEditText);
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

        loadTrashedNotes();
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

    private void loadTrashedNotes() {
        database.noteDao().getTrashedNotes().observe(getViewLifecycleOwner(), notes -> {
            trashedNotes.clear();
            trashedNotes.addAll(notes);
            filterNotes(""); // Show all initially
            if (trashedNotes.isEmpty()) {
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterNotes(String query) {
        filteredNotes.clear();
        for (Note note : trashedNotes) {
            if (note.getNoteTttale().toLowerCase().contains(query.toLowerCase())) {
                filteredNotes.add(note);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void restoreNote(Note note) {
        Executors.newSingleThreadExecutor().execute(() -> {
            note.setTrashed(false);
            database.noteDao().update(note);
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Note restored!", Toast.LENGTH_SHORT).show();
                loadTrashedNotes();
            });
        });
    }

    private void permanentlyDeleteNote(Note note) {
        Executors.newSingleThreadExecutor().execute(() -> {
            database.noteDao().delete(note);
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Note deleted permanently!", Toast.LENGTH_SHORT).show();
                loadTrashedNotes();
            });
        });
    }

    @Override
    public void onNoteLongPress(Note note) {
        showNoteOptionsDialog(note);
    }

    public void showNoteOptionsDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);

        // Creating the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_note_options, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // Finding views inside the custom dialog
        TextView pinText = dialogView.findViewById(R.id.textPin);
        ImageView pinIcon = dialogView.findViewById(R.id.iconPin);
        TextView archiveText = dialogView.findViewById(R.id.textArchive);
//        ImageView archiveIcon = dialogView.findViewById(R.id.iconArchive);
//        TextView deleteText = dialogView.findViewById(R.id.textDelete);
//        ImageView deleteIcon = dialogView.findViewById(R.id.iconDelete);
        dialogView.findViewById(R.id.layoutArchive).setVisibility(View.GONE);
        pinText.setText(R.string.Restore);
        pinIcon.setImageResource(R.drawable.backup_cloud_svgrepo_com);
        // Restore Click
        dialogView.findViewById(R.id.layoutPin).setOnClickListener(v -> {
            restoreNote(note);
            dialog.dismiss();
        });

        // Permanent Delete Click
        dialogView.findViewById(R.id.layoutDelete).setOnClickListener(v -> {
            android.app.AlertDialog.Builder dialog1 = new android.app.AlertDialog.Builder(v.getRootView().getContext());
            AlertDialog OptionDialog = dialog1.create();
            View dedialog = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.delete_dilog, null);

            Button ybutton = dedialog.findViewById(R.id.yesbutton);
            Button nbutton = dedialog.findViewById(R.id.nobytton);
            OptionDialog.setCancelable(false);
            OptionDialog.setView(dedialog);
            ybutton.setOnClickListener(v1 -> {
                permanentlyDeleteNote(note);
                OptionDialog.dismiss();
                dialog.dismiss();
            });
            nbutton.setOnClickListener(v1 -> OptionDialog.dismiss());
            OptionDialog.show();


        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(dpToPx(370), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

    }

    public int dpToPx(int dp) {
        return (int) (dp * this.getResources().getDisplayMetrics().density);
    }
}
