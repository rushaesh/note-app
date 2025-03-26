package com.example.notes;


import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.notes.Adptor.NotesAdapter;
import com.example.notes.database.AppDatabase;
import com.example.notes.database.Note;
import com.example.notes.frgments.ArchiveFragment;
import com.example.notes.frgments.SearchFragment;
import com.example.notes.frgments.SettingsFragment;
import com.example.notes.frgments.TrashFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NotesAdapter.NoteClickListener {
    private static final int REQUEST_EDIT_NOTE = 101;
    RecyclerView recyclerView;
    LinearLayout addNoteButton, srechlinerlayout, fistliner, secondli;
    CardView titalsorting, datesorting, grid;
    NotesAdapter adapter;
    List<Note> notesList;
    AppDatabase database;
    ImageView gridimage, tsort, dsort, actionbutoon, serched;
    TextView gridtext;
    private static final int NOTIFICATION_ID = 1;
    private static final String PREFS_NAME = "AppPref";
    private static final String KEY_RATED = "hasRated";
    private boolean isExpanded = false;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean isAscending = true;
    private boolean isAscendingdate = true;
    private boolean isGridView = false;
    private DrawerLayout drawerLayout;
    private ImageView drawerButton;
    private List<Note> serchedNotes = new ArrayList<>();
    private static final String TAG = "MainActivity";
    LinearLayout addliner, addnote, addchecklist;
    SharedPreferences sharedPreferences;
    private boolean isBackPressedOnce = false;
    private BannerAdHelper bannerAdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences1 = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isLightMode = sharedPreferences1.getBoolean("LightMode", false);

        if (isLightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        sharedPreferences = getSharedPreferences("LanguagePreferences", MODE_PRIVATE);

        // Load language preference and set it
        String languageCode = sharedPreferences.getString("language", "en"); // Default is "en"
        changeLanguage(languageCode);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        boolean hasRated = preferences.getBoolean(KEY_RATED, false);
//
//        boolean showRateDialog = getIntent().getBooleanExtra("show_rate_dialog", false);
//        Log.d(TAG, "show_rate_dialog value: " + showRateDialog);
//        Log.d(TAG, "Has user already rated? " + hasRated);
//
//        if (showRateDialog && !hasRated) {
//            Log.d(TAG, "Showing rate bottom sheet.");
//            showrateDialog();
//        } else {
//            Log.d(TAG, "No rate dialog needed.");
//        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        drawerLayout = findViewById(R.id.drawer);
        LinearLayout adContainer = findViewById(R.id.adContainer);
        bannerAdHelper = new BannerAdHelper(this);
        bannerAdHelper.loadBannerAd(adContainer);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        drawerButton = findViewById(R.id.manu);
        recyclerView = findViewById(R.id.recyclerView);
        addNoteButton = findViewById(R.id.note);
        gridtext = findViewById(R.id.grid);
        gridimage = findViewById(R.id.gridicon);
        titalsorting = findViewById(R.id.titalsorting);
        datesorting = findViewById(R.id.datesorting);
        grid = findViewById(R.id.gridlayout);
        dsort = findViewById(R.id.diconsorting);
        tsort = findViewById(R.id.ticonsorting);
        actionbutoon = findViewById(R.id.action);
        secondli = findViewById(R.id.checklist);
        addliner = findViewById(R.id.firstlayout);
        addnote = findViewById(R.id.addnote);
        addchecklist = findViewById(R.id.addchecklist);
        serched = findViewById(R.id.search_bar);
        database = AppDatabase.getInstance(this);

        adapter = new NotesAdapter(this, null, this, isGridView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        // Observe LiveData from Room so that queries run off the main thread.
        database.noteDao().getAllNote().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                adapter.updateNotes(notes);
                if (notes.isEmpty()) {
                    addliner.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    actionbutoon.setVisibility(View.GONE);
                    titalsorting.setVisibility(View.GONE);
                    datesorting.setVisibility(View.GONE);
                    grid.setVisibility(View.GONE);
                    serched.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    actionbutoon.setVisibility(View.VISIBLE);
                    serched.setVisibility(View.VISIBLE);
                    titalsorting.setVisibility(View.VISIBLE);
                    datesorting.setVisibility(View.VISIBLE);
                    grid.setVisibility(View.VISIBLE);
                    addliner.setVisibility(View.GONE);

                }
            }
        });
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });
        addnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotesEditorActivity.class);
                intent.putExtra("isChecked", false);
                startActivity(intent);
            }
        });
        addNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotesEditorActivity.class);
            intent.putExtra("isChecked", false);
            startActivity(intent);
            addNoteButton.setVisibility(View.GONE);
            secondli.setVisibility(View.GONE);
        });
        addchecklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotesEditorActivity.class);
                intent.putExtra("isChecked", true);
                startActivity(intent);
            }
        });
        secondli.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotesEditorActivity.class);
            intent.putExtra("isChecked", true);
            startActivity(intent);
            actionbutoon.setImageResource(R.drawable.add_svgrepo_com); // Replace with your default icon
            addNoteButton.setVisibility(View.GONE);
            secondli.setVisibility(View.GONE);
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;
                Context context = MainActivity.this;
                if (id == R.id.Archivei) {
                    selectedFragment = new ArchiveFragment();
                } else if (id == R.id.Trash) {
                    selectedFragment = new TrashFragment();
                } else if (id == R.id.Settings) {
                    selectedFragment = new SettingsFragment();
                } else if (id == R.id.ShareApp) {
                    String appPackageName = context.getPackageName();
                    String appLink = "https://play.google.com/store/apps/details?id=com.applus.notepad.note" + appPackageName;

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this awesome app!");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Download this app: " + appLink);

                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                } else if (id == R.id.RateApp) {
                    String packageName = "com.applus.notepad.note"; // Your app's package name
                    try {
                        Uri uri = Uri.parse("market://details?id=" + packageName);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).addToBackStack(null).commit();
                }

                // Close Drawer after selection
                drawerLayout.closeDrawers();
                return true;
            }
        });
        actionbutoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    // Set default icon & hide buttons
                    actionbutoon.setImageResource(R.drawable.add_svgrepo_com); // Replace with your default icon
                    addNoteButton.setVisibility(View.GONE);
                    secondli.setVisibility(View.GONE);
                } else {
                    // Set new icon & show buttons
                    actionbutoon.setImageResource(R.drawable.close_svgrepo_com); // Replace with new icon
                    addNoteButton.setVisibility(View.VISIBLE);
                    secondli.setVisibility(View.VISIBLE);
                }
                isExpanded = !isExpanded; // Toggle state
            }
        });
        requestNotificationPermission();
    }


    private void changeLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        android.content.res.Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // ✅ Android 13+ check
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    // ✅ User denied before but didn't select "Don't ask again"
                    Toast.makeText(this, "Notification permission is required for reminders!", Toast.LENGTH_LONG).show();
                }
                // ✅ Request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshNotesList(); // ✅ Refresh when coming back to MainActivity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("MainActivity", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);
//
//        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        boolean hasRated = preferences.getBoolean("hasRated", false); // Check if user already rated
//
//        if (data != null && data.getBooleanExtra("show_rate_dialog", false) && !hasRated) {
//            showrateDialog();
//        }

        if (requestCode == REQUEST_EDIT_NOTE && resultCode == RESULT_OK && data != null) {
            int trashedNoteId = data.getIntExtra("trashed_note_id", -1);
            int noteId = data.getIntExtra("note_id", -1);
            boolean isPinned = data.getBooleanExtra("is_pinned", false);

            Log.d("MainActivity", "onActivityResult: Note ID " + noteId + " updated. Pinned: " + isPinned);

            if (noteId != -1) {
                for (Note note : notesList) {
                    if (note.getId() == noteId) {
                        note.setPinned(isPinned);
                        adapter.notifyDataSetChanged();  // Refresh UI
                        break;
                    }
                }
            }

            if (trashedNoteId > 0 && notesList != null) {
                for (int i = 0; i < notesList.size(); i++) {
                    if (notesList.get(i).getId() == trashedNoteId) {
                        Log.d("MainActivity", "Removing trashed note at position: " + i);
                        notesList.remove(i);
                        adapter.notifyItemRemoved(i);
                        refreshNotesList();
                        return;
                    }
                }
                Log.w("MainActivity", "Trashed note ID not found in notesList.");
            } else if (noteId > 0) {
                Log.d("MainActivity", "Refreshing notes list due to pin/unpin change.");
                refreshNotesList();
            } else {
                Log.e("MainActivity", "Invalid note ID received in onActivityResult.");
            }
        }
    }


//    private void showrateDialog() {
//        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.Bottom_Sheet_Style);
//        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_rate, null);
//        bottomSheetDialog.setContentView(view);
//        LinearLayout linearLayout = view.findViewById(R.id.linner);
//        linearLayout.setVisibility(View.VISIBLE);
//        ImageView ratingBar = view.findViewById(R.id.star);
//        TextView maybelater = view.findViewById(R.id.maybelater);
//        TextView backagin = view.findViewById(R.id.backagin);
//        ratingBar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openPlayStore();
//                bottomSheetDialog.dismiss();
//
//                // 🛑 Prevent dialog from showing again in future
//                SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
//                editor.putBoolean("hasRated", true);
//                editor.apply();
//            }
//
//        });
//        maybelater.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetDialog.dismiss();
//            }
//        });
//        backagin.setVisibility(View.GONE);
//        bottomSheetDialog.show();
//
//    }


    private void openPlayStore() {
        try {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.applus.notepad.note" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.applus.notepad.note" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    // Refresh Notes List from Database
    private void refreshNotesList() {
        Log.d("MainActivity", "Refreshing notes list from database...");

        database.noteDao().getAllNote().observe(this, notes -> {
            if (notes != null) {
                List<Note> filteredNote = new ArrayList<>();
                for (Note note : notes) {
                    if (!note.isArchived()) { // ✅ Ignore archived notes
                        filteredNote.add(note);
                    }
                }
                notesList = filteredNote; // ✅ Update notes list
                adapter.updateNotes(notesList);
                Log.d("MainActivity", "Notes list updated, size: " + notesList.size());
            }
        });
    }

    public void search_bar(View view) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchFragment()) // Replace with your Fragment
                .addToBackStack(null) // Allows going back
                .commit();
    }


    @Override
    public void onNoteLongPress(Note note) {
        showNoteOptionsDialog(note);
    }

    public void showNoteOptionsDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);

        // Creating the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_note_options, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // Finding views inside the custom dialog
        TextView pinText = dialogView.findViewById(R.id.textPin);
        ImageView pinIcon = dialogView.findViewById(R.id.iconPin);
//        TextView archiveText = dialogView.findViewById(R.id.textArchive);
//        ImageView archiveIcon = dialogView.findViewById(R.id.iconArchive);
//        TextView deleteText = dialogView.findViewById(R.id.textDelete);
//        ImageView deleteIcon = dialogView.findViewById(R.id.iconDelete);

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

    private int dpToPx(int dp) {
        return (int) (dp * this.getResources().getDisplayMetrics().density);
    }

    private void archiveNote(Note note) {
        note.setArchived(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            database.noteDao().update(note);
            runOnUiThread(() -> {
                Toast.makeText(this, "Note Archived", Toast.LENGTH_SHORT).show();
                refreshNotesList();  // Remove from current list
            });
        });
    }

    private void deleteNote(Note note) {
        note.setTrashed(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            database.noteDao().update(note);
            runOnUiThread(() -> {
                Toast.makeText(this, "Note Moved to Trash", Toast.LENGTH_SHORT).show();
                refreshNotesList();  // Remove from current list
            });
        });
    }

    private void togglePinStatus(Note note) {
        boolean newPinStatus = !note.isPinned();
        note.setPinned(newPinStatus);

        Executors.newSingleThreadExecutor().execute(() -> {
            database.noteDao().update(note);
            runOnUiThread(() -> {
                Toast.makeText(this, newPinStatus ? "Pinned" : "Unpinned", Toast.LENGTH_SHORT).show();
                refreshNotesList();
            });
        });
    }

    public void titalesorting(View view) {
        dsort.setImageResource(R.drawable.unfold_more_svgrepo_com);
        titalsorting.setCardBackgroundColor(getResources().getColor(R.color.main));
        datesorting.setCardBackgroundColor(getResources().getColor(R.color.dargray));
        isAscending = !isAscending;
        loadNotes(isAscending);

    }

    private void loadNotes(boolean ascending) {
        if (ascending) {
            tsort.setImageResource(R.drawable.down_svgrepo_com);
            database.noteDao().getNotesAscending().observe(this, notes -> adapter.updateNotes(notes));
        } else {
            tsort.setImageResource(R.drawable.up_svgrepo_com);
            database.noteDao().getNotesDescending().observe(this, notes -> adapter.updateNotes(notes));
        }
    }


    public void datesorting(View view) {
        tsort.setImageResource(R.drawable.up_and_down_arrows_svgrepo_com);
        datesorting.setCardBackgroundColor(getResources().getColor(R.color.main));
        titalsorting.setCardBackgroundColor(getResources().getColor(R.color.dargray));
        isAscendingdate = !isAscendingdate;
        loadNotesdate(isAscendingdate);

    }

    private void loadNotesdate(boolean ascending) {
        if (ascending) {
            dsort.setImageResource(R.drawable.down_svgrepo_com);
            database.noteDao().getNotesByDateAsc().observe(this, notes -> adapter.updateNotes(notes));
        } else {
            dsort.setImageResource(R.drawable.up_svgrepo_com);
            database.noteDao().getNotesByDateDesc().observe(this, notes -> adapter.updateNote(notes));
        }
    }

    public void changelayout(View view) {
        toggleLayout();
    }

    public void toggleLayout() {
        isGridView = !isGridView; // Toggle state

        if (isGridView) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            gridimage.setImageResource(R.drawable.menu_svgrepo_com);
            gridtext.setText(R.string.list);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            gridimage.setImageResource(R.drawable.grid_svgrepo_com);
            gridtext.setText(R.string.Grid);
        }

        adapter.updateViewMode(isGridView);
    }


    @Override
    public void onBackPressed() {
        // If there's a fragment in the back stack, pop it
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            // Handle double back press to exit
            if (isBackPressedOnce) {
                finishAffinity(); // Close the app
                finish();
            } else {
                isBackPressedOnce = true;
                showExitDialog();

            }
        }
    }

    private void showExitDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.Bottom_Sheet_Style);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_rate, null);
        bottomSheetDialog.setContentView(view);
        LinearLayout linearLayout = view.findViewById(R.id.linner);
        linearLayout.setVisibility(View.GONE);
        TextView back = view.findViewById(R.id.backagin);
        back.setVisibility(View.VISIBLE);
        bottomSheetDialog.show();
    }

}