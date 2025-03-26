package com.example.notes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notes.Adptor.ImageAdapter;
import com.example.notes.database.AppDatabase;
import com.example.notes.database.Note;
import com.example.notes.database.NoteDao;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NotesEditorActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private static final String BACK_COUNT_KEY = "back_press_count";
    private static final String TAG = "NotesEditorActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    Note note = new Note();
    TextView doneButton, undo;
    ImageView galleryButton, cameraButton, closebutton, manui, backbutton, sharebutton, manubutton;
    RecyclerView imageRecyclerView;
    ImageAdapter imageAdapter;
    List<String> imagePaths = new ArrayList<>();
    AppDatabase database;
    LinearLayout linearLayout, undoli, main, manuli;
    EditText noteText, notetitale1;
    List<ChecklistItem> checklistItems = new ArrayList<>();
    boolean isChecklistVisible=false;
    boolean checklist;
    private ChecklistItem lastDeletedItem = null;
    private int lastDeletedIndex = -1;
    int noteId = -1;
    private boolean isReminderSet = false;
    private OneTimeWorkRequest reminderRequest;
    private Calendar selectedDateTime;
    private static final String TAGG = "NotesEditorActivity";
    private ImageView reminderButton, color1;
    private int selectedColor;
    private int selectedColor2;
    private EditText currentlyFocusedEditText = null;
    private boolean isPinned;
    private Uri cameraImageUri;
    private boolean moveCheckedToBottom;
    private static final int NOTIFICATION_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(selectedColor); // Set the status bar color dynamically
        }
        Window window = getWindow();
        window.setStatusBarColor(selectedColor);

        selectedColor = ContextCompat.getColor(this, R.color.bydefult);
        selectedColor2 = ContextCompat.getColor(this, R.color.bydefult2);
        doneButton = findViewById(R.id.doneButton);
        cameraButton = findViewById(R.id.camera);
        galleryButton = findViewById(R.id.image);
        imageRecyclerView = findViewById(R.id.recyview);
        noteText = findViewById(R.id.notettext);
        linearLayout = findViewById(R.id.checklistContainer);
        undoli = findViewById(R.id.undolinner);
        undo = findViewById(R.id.undoa);
        reminderButton = findViewById(R.id.remider);
        color1 = findViewById(R.id.color);
        main = findViewById(R.id.noteBackground);
        manui = findViewById(R.id.menu);
        backbutton = findViewById(R.id.back);
        manuli = findViewById(R.id.manulayout);
        sharebutton = findViewById(R.id.share);
        manubutton = findViewById(R.id.manu);
        closebutton = findViewById(R.id.close);
        notetitale1 = findViewById(R.id.notetitale);


        preferences = getSharedPreferences("AppPref", MODE_PRIVATE);
        isChecklistVisible = getIntent().getBooleanExtra("isChecked", false);

        Log.d(TAG, "onCreate: " + isChecklistVisible);

        // Set initial state based on isChecklistVisible
        if (isChecklistVisible) {
            showChecklist();
        } else {
            showNoteText();
        }
        reminderButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ requires POST_NOTIFICATIONS permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
                } else {
                    handleReminder();
                }
            } else {
                handleReminder();
            }
        });


        SharedPreferences sharedPreferences = getSharedPreferences("checklistchecked", MODE_PRIVATE);
        moveCheckedToBottom = sharedPreferences.getBoolean("moveCheckedBottom", false);

        database = AppDatabase.getInstance(this);

        imageAdapter = new ImageAdapter(this, imagePaths, database);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imageAdapter);

        galleryButton.setOnClickListener(v -> openGallery());
        cameraButton.setOnClickListener(v -> openCamera());
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            loadNoteData(noteId);
        }
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoDeleteChecklistItem();
            }
        });
        color1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorBottomSheet();
            }
        });
        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                manui.setVisibility(View.VISIBLE);
                manuli.setVisibility(View.GONE);
            }
        });
        manui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manuli.setVisibility(View.VISIBLE);
                manui.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDateTimePicker(); // Now we can show the picker
            } else {
                Toast.makeText(this, "Notification permission is required to set reminders.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleReminder() {
        if (isReminderSet) {
            cancelReminder();
        } else {
            showDateTimePicker();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadNoteData(int noteId) {
        Log.d(TAG, "loadNoteData() called with noteId: " + noteId);

        if (noteId <= 0) {
            Log.e(TAG, "Invalid note ID: " + noteId);
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Note loadedNote = database.noteDao().getNoteById(noteId);

            if (loadedNote != null) {
                Log.d(TAG, "Note retrieved from database: " + loadedNote.toString());
                note = loadedNote;  // Assign to global variable

                runOnUiThread(() -> {
                    Log.d(TAG, "Updating UI with retrieved note data.");
                    notetitale1.setText(note.getNoteTttale());
                    if (note.getNoteText() != null) {
                        noteText.setText(note.getNoteText());
                        Log.d(TAG, "Note text set: " + note.getNoteText());
                    }

                    selectedColor = note.getColor();
                    selectedColor2 = note.getColor2();
                    main.setBackgroundColor(selectedColor);
                    backbutton.setBackgroundColor(selectedColor2);
                    manubutton.setBackgroundColor(selectedColor2);
                    doneButton.setBackgroundColor(selectedColor2);
                    sharebutton.setBackgroundColor(selectedColor2);
                    manuli.setBackgroundColor(selectedColor2);
                    Log.d(TAG, "Note color set: " + selectedColor);

                    // Load checklist
                    if (note.getChecklist() != null && !note.getChecklist().isEmpty()) {
                        linearLayout.setVisibility(View.VISIBLE);
                        noteText.setVisibility(View.GONE);
                        loadChecklistItems(note.getChecklist());
                        Log.d(TAG, "Checklist loaded: " + note.getChecklist());
                    }

                    // Load images
                    // Load images
                    if (note.imagePaths != null) {
                        if (!note.imagePaths.isEmpty()) {
                            Log.d(TAG, "Images found in note: " + note.imagePaths);

                            for (String path : note.imagePaths) {
                                if (path == null || path.trim().isEmpty()) {
                                    Log.e(TAG, "Skipping invalid image path: " + path);
                                } else {
                                    imagePaths.add(path);
                                }
                            }

                            imageAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Images added to adapter: " + imagePaths);
                        } else {
                            Log.d(TAG, "No images found for this note.");
                        }
                    } else {
                        Log.e(TAG, "note.imagePaths is NULL");
                    }


                    isPinned = note.isPinned();
                });

            } else {
                Log.e(TAG, "loadNoteData() failed - Note not found in database for ID: " + noteId);
            }
        });
    }


    private void saveNote() {
        Log.d(TAG, "Saving Note...");

        if (note == null) {
            note = new Note(); // Ensure note is initialized
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {

            // Save basic note details
            String noteTextValue = noteText.getText().toString().trim();
            String noteTitleValue = notetitale1.getText().toString().trim();

            note.noteText = noteTextValue;
            note.noteTttale = noteTitleValue; // Fix typo
            note.color = selectedColor;
            note.color2 = selectedColor2;
            note.setPinned(isPinned);

            Log.d(TAG, "Note text: " + note.noteText);
            Log.d(TAG, "Note title: " + note.noteTttale);

            // Ensure only non-empty image paths are saved
            if (imagePaths != null && !imagePaths.isEmpty()) {
                note.imagePaths = imagePaths;
            } else {
                note.imagePaths = null;
            }
            Log.d(TAG, "Note images: " + (note.imagePaths != null ? note.imagePaths.size() : "No images"));

            // Save checklist properly
            StringBuilder checklistText = new StringBuilder();
            boolean allChecked = true;
            boolean hasValidItems = false; // Track if any valid checklist item exists

            if (checklistItems != null) {
                for (ChecklistItem item : checklistItems) {
                    String text = item.editText.getText().toString().trim();
                    boolean isChecked = item.checkBox.isChecked();

                    if (!text.isEmpty()) {
                        hasValidItems = true; // At least one valid item exists
                        checklistText.append(isChecked ? "✔ " : "☐ ").append(text).append("\n");
                        if (!isChecked) {
                            allChecked = false;
                        }
                    }
                }
            }

            // Only save checklist if it contains valid items
            if (hasValidItems) {
                note.setChecklist(checklistText.toString().trim());
                note.setAllChecked(allChecked);
            } else {
                note.setChecklist(null);
                note.setAllChecked(false);
            }

            Log.d(TAG, "Checklist: " + (note.getChecklist() != null ? note.getChecklist() : "No checklist"));

            // **Prevent Saving Empty Notes**: If no text, title, images, or checklist, do NOT save
            if (noteTextValue.isEmpty() && noteTitleValue.isEmpty() &&
                    (note.imagePaths == null || note.imagePaths.isEmpty()) &&
                    (note.getChecklist() == null || note.getChecklist().isEmpty())) {
                Log.d(TAG, "Note is empty, not saving.");
                runOnUiThread(() -> finish()); // Close activity without saving
                return;
            }

            // Insert or Update Note
            if (noteId != -1) {
                note.id = noteId;

                // Ensure removed items are deleted in the database
                if (note.imagePaths == null) {
                    note.imagePaths = new ArrayList<>(); // Clear images if deleted
                }
                if (note.getChecklist() == null) {
                    note.setChecklist(""); // Ensure empty checklist is not stored
                }

                database.noteDao().update(note);
                Log.d(TAG, "Note updated successfully");
            } else {
                database.noteDao().insert(note);
                Log.d(TAG, "Note inserted successfully");
            }

            runOnUiThread(() -> {
                finish();
            });
        });
    }


    private void showColorBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.Bottom_Sheet_Style);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_colors, null);
        bottomSheetDialog.setContentView(view);

        FrameLayout whiteColor = view.findViewById(R.id.color_white_container);
        FrameLayout twocolor = view.findViewById(R.id.darkgay_container);
        FrameLayout thcolor = view.findViewById(R.id.tcolor_container);
        FrameLayout focolor = view.findViewById(R.id.fcolor_container);
        FrameLayout fivcolor = view.findViewById(R.id.ficolor_container);
        FrameLayout sicolor = view.findViewById(R.id.scolor_container);
        FrameLayout sevecolor = view.findViewById(R.id.secolor_container);
        FrameLayout eigholor = view.findViewById(R.id.eicolor_container);
        FrameLayout nieolor = view.findViewById(R.id.nicolor_container);
        FrameLayout tenolor = view.findViewById(R.id.tecolor_container);


        ImageView cone = view.findViewById(R.id.check_white);
        ImageView ctwo = view.findViewById(R.id.check_darkgary);
        ImageView cthree = view.findViewById(R.id.check_tcolor);
        ImageView cfour = view.findViewById(R.id.check_fcolor);
        ImageView cfive = view.findViewById(R.id.check_ficolor);
        ImageView csix = view.findViewById(R.id.check_scolor);
        ImageView cseven = view.findViewById(R.id.check_secolor);
        ImageView ceight = view.findViewById(R.id.check_eicolor);
        ImageView cnine = view.findViewById(R.id.check_nicolor);
        ImageView ctan = view.findViewById(R.id.check_tecolor);

        View.OnClickListener colorClickListener = v -> {
            resetCheckmarks(cone, ctwo, cthree, cfour, cfive, csix, cseven, ceight, cnine, ctan);

            if (v == whiteColor) {
                selectedColor = ContextCompat.getColor(this, R.color.bydefult);
                selectedColor2 = ContextCompat.getColor(this, R.color.bydefult2);
                cone.setVisibility(View.VISIBLE);
            } else if (v == twocolor) {
                selectedColor = ContextCompat.getColor(this, R.color.darkgray);
                selectedColor2 = ContextCompat.getColor(this, R.color.darkgray2);
                ctwo.setVisibility(View.VISIBLE);
            } else if (v == thcolor) {
                selectedColor = ContextCompat.getColor(this, R.color.tcolor);
                selectedColor2 = ContextCompat.getColor(this, R.color.tcolor2);
                cthree.setVisibility(View.VISIBLE);
            } else if (v == focolor) {
                selectedColor = ContextCompat.getColor(this, R.color.fcolor);
                selectedColor2 = ContextCompat.getColor(this, R.color.fcolor2);
                cfour.setVisibility(View.VISIBLE);
            } else if (v == fivcolor) {
                selectedColor = ContextCompat.getColor(this, R.color.ficolor);
                selectedColor2 = ContextCompat.getColor(this, R.color.ficolor2);
                cfive.setVisibility(View.VISIBLE);
            } else if (v == sicolor) {
                selectedColor = ContextCompat.getColor(this, R.color.scolor);
                selectedColor2 = ContextCompat.getColor(this, R.color.scolor2);
                csix.setVisibility(View.VISIBLE);
            } else if (v == sevecolor) {
                selectedColor = ContextCompat.getColor(this, R.color.secolor);
                selectedColor2 = ContextCompat.getColor(this, R.color.secolor2);
                cseven.setVisibility(View.VISIBLE);
            } else if (v == eigholor) {
                selectedColor = ContextCompat.getColor(this, R.color.eicolor);
                selectedColor2 = ContextCompat.getColor(this, R.color.eicolor2);
                ceight.setVisibility(View.VISIBLE);
            } else if (v == nieolor) {
                selectedColor = ContextCompat.getColor(this, R.color.nicolor);
                selectedColor2 = ContextCompat.getColor(this, R.color.nicolor2);
                cnine.setVisibility(View.VISIBLE);
            } else if (v == tenolor) {
                selectedColor = ContextCompat.getColor(this, R.color.tecolor);
                selectedColor2 = ContextCompat.getColor(this, R.color.tecolor2);
                ctan.setVisibility(View.VISIBLE);
            }

            main.setBackgroundColor(selectedColor);
            backbutton.setBackgroundColor(selectedColor2);
            manubutton.setBackgroundColor(selectedColor2);
            doneButton.setBackgroundColor(selectedColor2);
            sharebutton.setBackgroundColor(selectedColor2);
            manuli.setBackgroundColor(selectedColor2);


        };

        whiteColor.setOnClickListener(colorClickListener);
        twocolor.setOnClickListener(colorClickListener);
        thcolor.setOnClickListener(colorClickListener);
        focolor.setOnClickListener(colorClickListener);
        fivcolor.setOnClickListener(colorClickListener);
        sicolor.setOnClickListener(colorClickListener);
        sevecolor.setOnClickListener(colorClickListener);
        eigholor.setOnClickListener(colorClickListener);
        nieolor.setOnClickListener(colorClickListener);
        tenolor.setOnClickListener(colorClickListener);


        bottomSheetDialog.show();
    }

    private void resetCheckmarks(ImageView... checkmarks) {
        for (ImageView checkmark : checkmarks) {
            checkmark.setVisibility(View.GONE);
        }
    }

    private void loadChecklistItems(String checklistData) {
        linearLayout.removeAllViews();
        checklistItems.clear();

        String[] items = checklistData.split("\n");
        for (String item : items) {
            boolean isChecked = item.startsWith("✔");
            String text = item.replace("✔", "").replace("☐", "").trim();
            addChecklistItem(text, isChecked);
        }
        addChecklistItem("", false);
    }

    private void openGallery() {
        Log.d(TAG, "Opening Gallery");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                requestGalleryPermission();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                requestGalleryPermission();
            }
        }
    }

    private void requestGalleryPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
            Toast.makeText(this, "Gallery permission is required to pick an image.", Toast.LENGTH_SHORT).show();
            // User denied once but not permanently, don't ask again
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);


        } else {
            // First time asking, request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        }
    }

    private void openCamera() {
        Log.d(TAG, "Opening Camera");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            captureImage();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission is required to take a photo.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }


    private void pickImageFromGallery() {
        Log.d(TAG, "Launching Gallery Picker Intent");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryActivityResultLauncher.launch(intent);
    }

    private void captureImage() {
        Log.d(TAG, "Launching Camera Intent");

        File imageFile = createImageFile();
        if (imageFile != null) {
            Uri imageUri = FileProvider.getUriForFile(this, "com.example.notes.fileprovider", imageFile);
            cameraImageUri = imageUri; // Store URI globally

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cameraActivityResultLauncher.launch(intent);
        }
    }


    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Log.d(TAG, "Image selected from gallery: " + imageUri.toString());

                        // Take persistable URI permission
                        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(imageUri, takeFlags);

                        imageAdapter.addImage(imageUri.toString());
                    } else {
                        Log.e(TAG, "Gallery returned null URI");
                    }
                } else {
                    Log.e(TAG, "Gallery selection failed or canceled");
                }
            });

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.d(TAG, "Image captured from camera: " + cameraImageUri.toString());
                    imageAdapter.addImage(cameraImageUri.toString());
                } else {
                    Log.e(TAG, "Camera capture failed or canceled");
                }
            });


    // Create a file to store captured image
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile("IMG_" + timeStamp, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            return null;
        }
    }

    public void checklist(View view) {
        Log.d(TAG, "checklist: "+isChecklistVisible);
        toggleChecklist();
    }

    private void toggleChecklist() {
        if (!isChecklistVisible) {
            showChecklist();
        } else {
            hideChecklist();
        }
        isChecklistVisible = !isChecklistVisible; // Toggle state
    }
    private void showNoteText() {
        linearLayout.setVisibility(View.GONE); // Hide checklist
        noteText.setVisibility(View.VISIBLE); // Show note field

        StringBuilder checklistText = new StringBuilder();
        for (ChecklistItem item : checklistItems) {
            String text = item.editText.getText().toString().trim();
            if (!text.isEmpty()) {
                checklistText.append(text).append("\n"); // Append all checklist items
            }
        }

        noteText.setText(checklistText.toString().trim()); // Set converted checklist items as noteText
        checklistItems.clear(); // Clear checklist items list
        linearLayout.removeAllViews(); // Remove all checklist views
    }

    // **Show Checklist & Convert Note to Checklist Items**
    private void showChecklist() {
        linearLayout.setVisibility(View.VISIBLE);
        noteText.setVisibility(View.GONE);

        if (checklistItems.isEmpty()) { // Convert text only once
            String savedText = noteText.getText().toString().trim();
            noteText.setText(""); // Clear noteText

            if (!savedText.isEmpty()) {
                // Convert each line of text into a checklist item
                String[] items = savedText.split("\n");
                for (String item : items) {
                    addChecklistItem(item.trim(), false);
                }
            }
            addChecklistItem("", false); // Always add a blank item for new entry
        }
    }

    // **Hide Checklist & Convert Checklist Items Back to Text**
    private void hideChecklist() {
        linearLayout.setVisibility(View.GONE);
        noteText.setVisibility(View.VISIBLE);

        StringBuilder checklistText = new StringBuilder();
        for (ChecklistItem item : checklistItems) {
            String text = item.editText.getText().toString().trim();
            if (!text.isEmpty()) {
                checklistText.append(text).append("\n"); // Append all checklist items
            }
        }

        noteText.setText(checklistText.toString().trim()); // Set back in noteText
        checklistItems.clear(); // Clear checklist items
        linearLayout.removeAllViews(); // Remove views from UI
    }


    private void addChecklistItem(String text, boolean isCheck) {
        Log.d(TAG, "Adding new checklist item");

        View checklistView = getLayoutInflater().inflate(R.layout.item_checklist, linearLayout, false);
        linearLayout.addView(checklistView);

        EditText editText = checklistView.findViewById(R.id.checklistEditText);
        CheckBox checkBox = checklistView.findViewById(R.id.checklistCheckBox);
        ImageView deleteb = checklistView.findViewById(R.id.delete);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editText.setSingleLine(false);
        editText.setMaxLines(5);

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                View nextFocus = editText.focusSearch(View.FOCUS_DOWN);
                if (nextFocus != null) {
                    nextFocus.requestFocus();
                } else {
                    // Optionally, add a new checklist item if at the last one
                    addChecklistItem("", false);
                }
                return true; // Consume the event
            }
            return false;
        });



        if (text != null && !text.isEmpty()) {
            editText.setText(text);
        }
        checkBox.setChecked(isCheck);

        updateTextStyle(editText, checkBox.isChecked());
        deleteb.setVisibility(View.GONE);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTextStyle(editText, checkBox.isChecked());
            sortChecklist();
        });
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (currentlyFocusedEditText != null && currentlyFocusedEditText != editText) {
                    // Hide delete button of the previously focused EditText
                    View previousChecklistView = (View) currentlyFocusedEditText.getParent();
                    ImageView previousDeleteButton = previousChecklistView.findViewById(R.id.delete);
                    previousDeleteButton.setVisibility(View.GONE);
                }

                deleteb.setVisibility(View.VISIBLE);
                currentlyFocusedEditText = editText;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                deleteb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty() && isLastChecklistItem(editText)) {

                    addChecklistItem("", isCheck); // Add a new blank checklist item
                }

            }
        });
        deleteb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteChecklistItem(getChecklistItem(editText));
            }
        });
        checklistItems.add(new ChecklistItem(editText, checkBox, checklistView));
    }

    private boolean isLastChecklistItem(EditText editText) {
        return !checklistItems.isEmpty() && checklistItems.get(checklistItems.size() - 1).editText == editText;
    }

    private ChecklistItem getChecklistItem(EditText editText) {
        for (ChecklistItem item : checklistItems) {
            if (item.editText == editText) {
                return item;
            }
        }
        return null; // Should never happen
    }

    private void deleteChecklistItem(ChecklistItem item) {
        if (checklistItems.size() > 1) { // Allow deleting if more than one item exists
            int index = checklistItems.indexOf(item);
            if (index >= 0) {
                lastDeletedIndex = index;
                lastDeletedItem = item;

                checklistItems.remove(index);
                linearLayout.removeView(item.view);

                undoli.setVisibility(View.VISIBLE);
                new Handler(Looper.getMainLooper()).postDelayed(() -> undoli.setVisibility(View.GONE), 1000);
            }
        } else {
            // Toast.makeText(this, "At least one checklist item must remain", Toast.LENGTH_SHORT).show();
        }
    }

    private void undoDeleteChecklistItem() {
        if (lastDeletedItem != null && lastDeletedIndex >= 0) {
            int index = Math.min(lastDeletedIndex, checklistItems.size());
            checklistItems.add(index, lastDeletedItem);
            linearLayout.addView(lastDeletedItem.view, index);

            lastDeletedItem = null;
            lastDeletedIndex = -1;
            undoli.setVisibility(View.GONE);
        }
    }

    private void updateTextStyle(EditText editText, boolean isChecked) {
        if (isChecked) {
            editText.setPaintFlags(editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            editText.setTextColor(ContextCompat.getColor(this, R.color.graytwo));
        } else {
            editText.setPaintFlags(editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            editText.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void sortChecklist() {
        if (!moveCheckedToBottom) return; // Skip sorting if the setting is OFF

        Collections.sort(checklistItems, (item1, item2) -> {
            boolean isChecked1 = item1.checkBox.isChecked();
            boolean isChecked2 = item2.checkBox.isChecked();
            boolean isEmpty1 = item1.editText.getText().toString().trim().isEmpty();
            boolean isEmpty2 = item2.editText.getText().toString().trim().isEmpty();

            // Empty items always go to the bottom
            if (isEmpty1 != isEmpty2) {
                return Boolean.compare(isEmpty1, isEmpty2);
            }

            // Among non-empty items, unchecked should be above checked
            return Boolean.compare(isChecked1, isChecked2);
        });

        runOnUiThread(() -> {
            linearLayout.removeAllViews();
            for (ChecklistItem item : checklistItems) {
                linearLayout.addView(item.view);
            }
        });
    }


    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        selectedDateTime = Calendar.getInstance();

        // DatePicker
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // TimePicker
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0);

                setReminder(selectedDateTime.getTimeInMillis());
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setReminder(long triggerTime) {
        String noteContent;
        if (isChecklistVisible) {
            // Collect checklist items
            StringBuilder checklistText = new StringBuilder();
            for (ChecklistItem item : checklistItems) {
                String text = item.editText.getText().toString().trim();
                boolean isChecked = item.checkBox.isChecked();
                if (!text.isEmpty()) {
                    checklistText.append(isChecked ? "[✔] " : "[ ] ").append(text).append("\n");
                }
            }
            noteContent = checklistText.toString().trim();
        } else {
            // Get plain note text
            noteContent = noteText.getText().toString().trim();
        }
        String noteTitle = noteText.getText().toString().trim(); // Update with your title EditText ID// Update with your note content EditText ID

        if (noteContent.isEmpty()) {
            Toast.makeText(this, "Cannot set a reminder for an empty note!", Toast.LENGTH_SHORT).show();
            return; // Exit the method early
        }

        long delay = triggerTime - System.currentTimeMillis();
        if (delay <= 0) {
            Toast.makeText(this, "Selected time is in the past!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass the note title to the Worker
        Data data = new Data.Builder()
                .putString("reminder_title", noteTitle.isEmpty() ? "Reminder" : noteTitle) // Use title or default
                .build();

        reminderRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this).enqueue(reminderRequest);

        isReminderSet = true;
        Toast.makeText(this, "Reminder Set!", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Reminder set for: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(selectedDateTime.getTime()));
    }


    private void cancelReminder() {
        if (reminderRequest != null) {
            WorkManager.getInstance(this).cancelWorkById(reminderRequest.getId());
            Toast.makeText(this, "Reminder Canceled!", Toast.LENGTH_SHORT).show();
        }
        isReminderSet = false;
        // reminderButton.setText("Set Reminder");
    }

    public void share(View view) {
        StringBuilder noteContent = new StringBuilder();

        // Get the note text
        String noteText1 = noteText.getText().toString().trim();
        if (!noteText1.isEmpty()) {
            noteContent.append("📝 Note: ").append(noteText1).append("\n\n");
        }

        // Get checklist items
        if (!checklistItems.isEmpty()) {
            noteContent.append("📝 Note: \n");
            for (ChecklistItem item : checklistItems) {
                String itemText = item.editText.getText().toString().trim();
                if (!itemText.isEmpty()) {
                    String checkMark = item.checkBox.isChecked() ? "✔ " : "☐   ";
                    noteContent.append(checkMark).append(itemText).append("\n");
                }
            }
        }

        if (noteContent.length() == 0) {
            Toast.makeText(this, "Nothing to share!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, noteContent.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Note"));


    }

    public void manu(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_custom_menu, null);
        int widthInDp = 210;
        float scale = getResources().getDisplayMetrics().density;
        int widthInPx = (int) (widthInDp * scale + 0.5f);
        PopupWindow popupWindow = new PopupWindow(dialogView,
                widthInPx,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(8f);

        // Get menu options
        LinearLayout deleteOption = dialogView.findViewById(R.id.delete);
        LinearLayout discardOption = dialogView.findViewById(R.id.decardchanges);
        LinearLayout pinOption = dialogView.findViewById(R.id.pin);
        ImageView pinIcon = dialogView.findViewById(R.id.pinicon);
        TextView pintext = dialogView.findViewById(R.id.pintext);

        if (isPinned) {
            pinIcon.setImageResource(R.drawable.unpin_svgrepo_com);
            pintext.setText(R.string.unpin);
        } else {
            pinIcon.setImageResource(R.drawable.pin_svgrepo_com);
            pintext.setText(R.string.pin);
        }

        // Delete Note
        deleteOption.setOnClickListener(v -> {
            showDeleteDialog(v);
            popupWindow.dismiss();
        });

        // Discard Changes
        discardOption.setOnClickListener(v -> {
            discardChanges(v);
            popupWindow.dismiss();
        });

        // Toggle Pin
        pinOption.setOnClickListener(v -> {
            isPinned = !isPinned; // Toggle pin state
            if (isPinned) {
                pinIcon.setImageResource(R.drawable.unpin_svgrepo_com);
                pintext.setText(R.string.unpin);
            } else {
                pinIcon.setImageResource(R.drawable.pin_svgrepo_com);
                pintext.setText(R.string.pin);
            }

            // Update in database in a background thread
            Executors.newSingleThreadExecutor().execute(() -> {
                database.noteDao().updatePinStatus(noteId, isPinned);
                Log.d("NotesEditorActivity", "Updated Note ID: " + noteId + " | New Pin Status: " + isPinned);

                runOnUiThread(() -> {
                    Toast.makeText(this, isPinned ? "Pinned!" : "Unpinned!", Toast.LENGTH_SHORT).show();
                });
            });

            // Send result back to MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("note_id", noteId);
            resultIntent.putExtra("is_pinned", isPinned);
            setResult(RESULT_OK, resultIntent);

            popupWindow.dismiss();
        });

        // Convert 15dp to pixels
        int marginInDp = 15;
        float scale1 = getResources().getDisplayMetrics().density;
        int marginInPx = (int) (marginInDp * scale1 + 0.5f);

        // Get location of the anchor view (button where menu is clicked)
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int y = location[1] + view.getHeight() + marginInPx;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;


        int x1 = screenWidth - popupWindow.getWidth() - marginInPx;

        popupWindow.showAtLocation(view, Gravity.TOP | Gravity.START, x1, y);

    }


    private void discardChanges(View v) {

        android.app.AlertDialog.Builder dialog1 = new android.app.AlertDialog.Builder(v.getRootView().getContext());
        AlertDialog OptionDialog = dialog1.create();
        View dedialog = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.delete_dilog, null);
        TextView textView = dedialog.findViewById(R.id.titale);
        Button ybutton = dedialog.findViewById(R.id.yesbutton);
        Button nbutton = dedialog.findViewById(R.id.nobytton);
        textView.setText(R.string.Areyousureyouwanttodiscardallchanges);
        ybutton.setText(R.string.DiscardChanges);
        OptionDialog.setCancelable(false);
        OptionDialog.setView(dedialog);
        ybutton.setOnClickListener(v1 -> {
            Log.d("NotesEditorActivity", "Changes discarded, closing activity.");
            if (isChecklistVisible == true) {
                loadata();

            } else {
                loadNoteData(noteId);
            }
            isChecklistVisible = !isChecklistVisible; // Toggle state
            OptionDialog.dismiss();
        });
        nbutton.setOnClickListener(v1 -> OptionDialog.dismiss());
        OptionDialog.show();

    }

    private void loadata() {
        linearLayout.setVisibility(View.GONE);
        noteText.setVisibility(View.VISIBLE);

        StringBuilder checklistText = new StringBuilder();
        for (ChecklistItem item : checklistItems) {
            String text = item.editText.getText().toString().trim();
            if (!text.isEmpty()) {
                checklistText.append(text).append("\n"); // Append all checklist items
                loadNoteData(noteId);
            }
        }

        if (checklistText.length() >= 0) {
            checklistText.setLength(checklistText.length()); // Remove last comma
        }
        noteText.setText(checklistText.toString()); // Set all checklist items in noteText

        checklistItems.clear(); // Clear checklist items
        linearLayout.removeAllViews();
    }

    private void showDeleteDialog(View v) {
        android.app.AlertDialog.Builder dialog1 = new android.app.AlertDialog.Builder(v.getRootView().getContext());
        AlertDialog OptionDialog = dialog1.create();
        View dedialog = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.delete_dilog, null);

        Button ybutton = dedialog.findViewById(R.id.yesbutton);
        Button nbutton = dedialog.findViewById(R.id.nobytton);
        OptionDialog.setCancelable(false);
        OptionDialog.setView(dedialog);
        ybutton.setOnClickListener(v1 -> {
            deleteNote();
            OptionDialog.dismiss();
        });
        nbutton.setOnClickListener(v1 -> OptionDialog.dismiss());
        OptionDialog.show();
    }


    private void deleteNote() {
        if (note != null && note.getId() > 0) {  // Ensure note exists
            Log.d(TAG, "Deleting note with ID: " + note.getId());

            note.setTrashed(true);  // Mark note as trashed

            Executors.newSingleThreadExecutor().execute(() -> {
                database.noteDao().update(note);  // Update in database

                // Check if the update was successful
                Note updatedNote = database.noteDao().getNoteById(note.getId());
                if (updatedNote != null && updatedNote.isTrashed()) {
                    Log.d(TAG, "Note successfully marked as trashed in database.");
                } else {
                    Log.e(TAG, "Failed to mark note as trashed!");
                }

                runOnUiThread(() -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("trashed_note_id", note.getId());  // Send note ID
                    setResult(RESULT_OK, resultIntent);
                    Log.d(TAG, "Returning trashed note ID: " + note.getId());

                    finish();  // Close activity
                });
            });
        } else {
            Log.e(TAG, "deleteNote() failed - Note is null or ID is invalid");
        }
    }

    public void backtomainactivity(View view) {
        onBackPressed();
    }


    private static class ChecklistItem {
        EditText editText;
        CheckBox checkBox;
        View view;

        ChecklistItem(EditText editText, CheckBox checkBox, View view) {
            this.editText = editText;
            this.checkBox = checkBox;
            this.view = view;
        }
    }

    @Override
    public void onBackPressed() {
//        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        int backPressCount = preferences.getInt(BACK_COUNT_KEY, 0);
//        boolean hasRated = preferences.getBoolean("hasRated", false); // Check if user has already rated
//
//        backPressCount++;
//        Log.d(TAG, "Back pressed count: " + backPressCount);
//
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt(BACK_COUNT_KEY, backPressCount);
//        editor.apply();

        Intent intent = new Intent(NotesEditorActivity.this, MainActivity.class);

//        if (backPressCount >= 3 && !hasRated) {  // Only show dialog if user has not rated yet
//            Log.d(TAG, "Third back press detected. Showing rate dialog in MainActivity.");
//            intent.putExtra("show_rate_dialog", true);
//            editor.putInt(BACK_COUNT_KEY, 0); // Reset counter after showing dialog
//            editor.apply();
//        } else {
//            Log.d(TAG, "Moving to MainActivity without showing rate dialog.");
//        }

        saveNote();
        startActivity(intent);
        finish();
    }
}