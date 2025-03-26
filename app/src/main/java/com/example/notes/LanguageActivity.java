package com.example.notes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.example.notes.databinding.ActivityLanguageBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity implements LanguageAdapter.OnLanguageChangeListener{
    ActivityLanguageBinding binding;
    private LanguageAdapter adapter;
    private List<LanguageItem> languages;
    private String selectedLanguageCode = "en";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        sharedPreferences = getSharedPreferences("LanguagePreferences", MODE_PRIVATE);
        SharedPreferences sharedPreferences1 = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstTimeLanguage = sharedPreferences1.getBoolean("isFirstTimeLanguage", true);

        if (isFirstTimeLanguage) {
            // Show "Setup for First Use" text if it's the first time
            binding.setupforfirstuse.setVisibility(View.VISIBLE);

            // Update SharedPreferences so next time it doesn't show the text
            SharedPreferences.Editor editor = sharedPreferences1.edit();
            editor.putBoolean("isFirstTimeLanguage", false);
            editor.apply();
        } else {
            // Hide text if not the first time
            binding.setupforfirstuse.setVisibility(View.GONE);
        }


        languages = new ArrayList<>();
        languages.add(new LanguageItem("English", "en", "english"));
        languages.add(new LanguageItem("Afrikaans", "af", "Afrikaans"));
        languages.add(new LanguageItem("Arabic", "ar", "اَلْعَرَبِيَّةُ"));
        languages.add(new LanguageItem("Bangla", "bn", "বাংলা"));
        languages.add(new LanguageItem("Czech", "cs", "Čeština"));
        languages.add(new LanguageItem("Danish", "da", "Dansk"));
        languages.add(new LanguageItem("Persian", "af", "فارسی"));
        languages.add(new LanguageItem("Finnish", "fi", "Suomi"));
        languages.add(new LanguageItem("French", "fr", "Français"));
        languages.add(new LanguageItem("German", "de", "Deutsch"));
        languages.add(new LanguageItem("Hindi", "hi", "हिन्दी"));
        languages.add(new LanguageItem("Indonesian", "in", "bahasa Indonesia"));
        languages.add(new LanguageItem("Italian", "it", "Italiano"));
        languages.add(new LanguageItem("Japanese", "ja", "日本語"));
        languages.add(new LanguageItem("Korean", "ko", "한국어"));
        languages.add(new LanguageItem("Marathi", "mr", "मराठी"));
        languages.add(new LanguageItem("Malay", "ms","Malaysia"));
        languages.add(new LanguageItem("Polish", "pl", "Polski"));
        languages.add(new LanguageItem("Portuguese", "pt", "Português"));
        languages.add(new LanguageItem("Russian", "ru", "Русский язык"));
        languages.add(new LanguageItem("Swedish", "swc", "Svenska"));
        languages.add(new LanguageItem("Thai", "th", "ภาษาไทย"));
        languages.add(new LanguageItem("Turkish", "tr", "Türkçe"));
        languages.add(new LanguageItem("Urdu", "ur", "اُردُو"));
        languages.add(new LanguageItem("Vietnamese", "vi", "tiếng Việt"));

        // Initialize adapter
        adapter = new LanguageAdapter(this, languages, this);
        binding.recyview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyview.setAdapter(adapter);

        // Button click listener to apply the selected language
        binding.changelanguage.setOnClickListener(v -> {
            if (selectedLanguageCode != null) {
                setAppLocale(selectedLanguageCode);  // Change language
            } else {
                Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLanguageChanged(String languageCode) {
        selectedLanguageCode = languageCode;
    }

    private void setAppLocale(String localeCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", localeCode);
        editor.apply();

        // Change app language
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);

        android.content.res.Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Intent intent = new Intent(LanguageActivity.this, SkipActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity (LanguageActivity)
    }
}
