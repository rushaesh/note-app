package com.example.notes.frgments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.notes.LanguageActivity;
import com.example.notes.R;
import com.example.notes.passcode;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.json.gson.GsonFactory;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

public class SettingsFragment extends Fragment {
    private Switch themeSwitch, switchPasscode;
    private SharedPreferences sharedPreferences, passcodePreferences, sharedPreferences1;
    private SharedPreferences.Editor editor;
    private TextView languageNameTextView, languageCodeTextView;
    private Switch switchMoveCheckedBottom;
    private ImageView imageView;
    private LinearLayout rateliner, sharelinner, changepassword, changelanguage,backup;
    private static final int REQUEST_CODE_SIGN_IN = 100;
    private static final int REQUEST_CODE_PICK_FILE = 101;

    private GoogleSignInClient googleSignInClient;
    private Drive googleDriveService;
    private static final int REQUEST_CODE_PASSCODE = 102;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        // Initialize Views
        themeSwitch = view.findViewById(R.id.themeSwitch);
        switchPasscode = view.findViewById(R.id.switchPasscode);
        languageNameTextView = view.findViewById(R.id.languageNameTextView);
        languageCodeTextView = view.findViewById(R.id.languageCodeTextView);
        rateliner = view.findViewById(R.id.Ratethisapp);
        sharelinner = view.findViewById(R.id.Sharewithfriend);
        changepassword = view.findViewById(R.id.security2);
        imageView = view.findViewById(R.id.back);
        switchMoveCheckedBottom = view.findViewById(R.id.switchMoveCheckedBottom);
        changelanguage = view.findViewById(R.id.changelanguage);
        backup = view.findViewById(R.id.data);

        // Get SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        passcodePreferences = requireActivity().getSharedPreferences("PasscodePrefs", Context.MODE_PRIVATE);
        sharedPreferences1 = requireActivity().getSharedPreferences("checklistchecked", Context.MODE_PRIVATE);

        // 🌙 Apply saved theme preference
        if (sharedPreferences.getBoolean("LightMode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);  // Light Mode
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // Dark Mode
        }

        // 🎚️ Theme switch setup
        themeSwitch.setChecked(sharedPreferences.getBoolean("LightMode", false));
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor = sharedPreferences.edit();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
            editor.putBoolean("LightMode", isChecked);
            editor.apply();
            requireActivity().recreate();
        });

        // 🔒 Passcode switch setup
        String savedPasscode = passcodePreferences.getString("passcode", ""); // Get stored passcode
        boolean isPasscodeEnabled = passcodePreferences.getBoolean("passcode_enabled", false);

// If no passcode is set, disable the switch and update preferences
        if (savedPasscode.isEmpty()) {
            isPasscodeEnabled = false;
            SharedPreferences.Editor passcodeEditor = passcodePreferences.edit();
            passcodeEditor.putBoolean("passcode_enabled", false);
            passcodeEditor.apply();
            changepassword.setEnabled(false);
        }

// Update the switch state
        switchPasscode.setChecked(isPasscodeEnabled);
        changepassword.setEnabled(isPasscodeEnabled);
        changepassword.setAlpha(isPasscodeEnabled ? 1.0f : 0.5f);

        switchPasscode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor passcodeEditor = passcodePreferences.edit();

            if (isChecked) {
                // If passcode is empty, force user to set a new one
                if (savedPasscode.isEmpty()) {
                    Intent intent = new Intent(requireContext(), passcode.class);
                    intent.putExtra("set_passcode", true);
                    startActivityForResult(intent, REQUEST_CODE_PASSCODE);
                }
                passcodeEditor.putBoolean("passcode_enabled", true);
                changepassword.setEnabled(true);
                changepassword.setAlpha(1.0f);
            } else {
                passcodeEditor.putBoolean("passcode_enabled", false);
                changepassword.setEnabled(false);
                changepassword.setAlpha(0.5f);
            }
            passcodeEditor.apply();
        });


        changepassword.setOnClickListener(v -> {
            if (switchPasscode.isChecked()) { // Only open if switch is ON
                Intent intent = new Intent(getContext(), passcode.class);
                intent.putExtra("change_passcode", true);
                startActivity(intent);
            }
        });
        changelanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LanguageActivity.class);
                startActivity(intent);
            }
        });
        // Load saved language preference
        SharedPreferences langPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        languageNameTextView.setText(langPreferences.getString("SelectedLanguageName", "English"));
        languageCodeTextView.setText(langPreferences.getString("SelectedLanguage", "en"));

        // 🔙 Back button action
        imageView.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // ⭐ Rate App
        rateliner.setOnClickListener(v -> {
            String packageName = requireContext().getPackageName();
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
        });

        // 📤 Share App
        sharelinner.setOnClickListener(v -> {
            String appLink = "https://play.google.com/store/apps/details?id=" + requireContext().getPackageName();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this awesome app!");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Download this app: " + appLink);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // ✅ Move checked items to bottom
        boolean isChecked = sharedPreferences1.getBoolean("moveCheckedBottom", false);
        switchMoveCheckedBottom.setChecked(isChecked);
        switchMoveCheckedBottom.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            sharedPreferences1.edit().putBoolean("moveCheckedBottom", isChecked1).apply();
        });
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInToGoogle();
            }
        });
        return view;
    }
    private void signInToGoogle() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), signInOptions);
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
            if (account != null) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        requireContext(), Collections.singleton(DriveScopes.DRIVE_FILE)
                );
                credential.setSelectedAccount(account.getAccount());

                googleDriveService = new Drive.Builder(
                        new com.google.api.client.http.javanet.NetHttpTransport(),
                        new com.google.api.client.json.gson.GsonFactory(),
                        credential)
                        .setApplicationName("BackupRestoreApp")
                        .build();

                backupData();
            }
        } else if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            restoreFromDrive(uri);
        } else if (requestCode == REQUEST_CODE_PASSCODE) {
            if (resultCode == Activity.RESULT_CANCELED && data != null && data.getBooleanExtra("passcode_not_set", false)) {
                // 🔴 User exited Passcode activity without setting a passcode
                switchPasscode.setChecked(false);
                changepassword.setEnabled(false);
                changepassword.setAlpha(0.5f);

                SharedPreferences.Editor passcodeEditor = passcodePreferences.edit();
                passcodeEditor.putBoolean("passcode_enabled", false);
                passcodeEditor.apply();
            } else if (resultCode == Activity.RESULT_OK) {
                // ✅ User set a passcode successfully
                switchPasscode.setChecked(true);
                changepassword.setEnabled(true);
                changepassword.setAlpha(1.0f);

                SharedPreferences.Editor passcodeEditor = passcodePreferences.edit();
                passcodeEditor.putBoolean("passcode_enabled", true);
                passcodeEditor.apply();
            }
        }
    }


    private void backupData() {
        if (googleDriveService == null) {
            Toast.makeText(requireContext(), "Sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a file for backup
        java.io.File filePath = new java.io.File(requireContext().getFilesDir(), "backup.txt");

        try (OutputStream os = new FileOutputStream(filePath)) {
            os.write("This is sample backup data".getBytes());
        } catch (Exception e) {
            Log.e("Backup", "Error writing file", e);
        }

        // Define metadata for Google Drive
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName("backup.txt");

        // Create media content
        FileContent mediaContent = new FileContent("text/plain", filePath);

        new Thread(() -> {
            try {
                googleDriveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Backup successful!", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                Log.e("Backup", "Upload failed", e);
            }
        }).start();
    }


    private void restoreData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    private void restoreFromDrive(Uri uri) {
        try {
            java.io.File file = new java.io.File(requireContext().getFilesDir(), "restored.txt");

            try (InputStream is = requireContext().getContentResolver().openInputStream(uri);
                 OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            Toast.makeText(requireContext(), "Restore complete!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Restore", "Error restoring file", e);
        }
    }
}