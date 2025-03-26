package com.example.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class passcode extends AppCompatActivity {

    private TextView tvTitle;
    private List<View> dots = new ArrayList<>();
    private StringBuilder enteredPasscode = new StringBuilder();
    private String firstEntry = "";
    private SharedPreferences preferences;
    private boolean isChangePasscode = false;
    private boolean oldPasscodeVerified = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        tvTitle = findViewById(R.id.tvTitle);
        preferences = getSharedPreferences("PasscodePrefs", MODE_PRIVATE);
        isChangePasscode = getIntent().getBooleanExtra("change_passcode", false);
        // Initialize dots
        dots.add(findViewById(R.id.dot1));
        dots.add(findViewById(R.id.dot2));
        dots.add(findViewById(R.id.dot3));
        dots.add(findViewById(R.id.dot4));

        // Check if passcode already exists
        if (preferences.contains("passcode")) {
            tvTitle.setText(R.string.EnterNewPasscode);
        } else if (isChangePasscode) {
            tvTitle.setText(R.string.EnterOldPasscode);
        } else {
            tvTitle.setText(R.string.EnterNewPasscode);
        }
    }

    public void onKeyPress(View view) {
        if (enteredPasscode.length() < 4) {
            enteredPasscode.append(((TextView) view).getText().toString());
            updateDots();
        }

        if (enteredPasscode.length() == 4) {
            handlePasscodeEntry();
        }
    }

    private void handlePasscodeEntry() {
        String savedPasscode = preferences.getString("passcode", "");

        if (savedPasscode.isEmpty()) {
            // Setting up a new passcode
            if (firstEntry.isEmpty()) {
                firstEntry = enteredPasscode.toString();
                tvTitle.setText(R.string.ReenterPasscode);
                enteredPasscode.setLength(0);
                updateDots();
            } else if (firstEntry.equals(enteredPasscode.toString())) {
                // Save passcode and restart app
                preferences.edit().putString("passcode", firstEntry).apply();
                finish();
            } else {
                // Passcodes do not match → Reset process
                tvTitle.setText(R.string.PasscodesdonotmatchEnterNewPasscode);
                firstEntry = "";
                enteredPasscode.setLength(0);
                updateDots();
            }
        } else if (isChangePasscode) {
            if (!oldPasscodeVerified) {
                if (enteredPasscode.toString().equals(savedPasscode)) {
                    oldPasscodeVerified = true;
                    tvTitle.setText(R.string.EnterNewPasscode);
                    enteredPasscode.setLength(0);
                    updateDots();
                } else {
                    tvTitle.setText(R.string.IncorrectPasscodeTryagain);
                    enteredPasscode.setLength(0);
                    updateDots();
                }
            }
            // Step 2: Enter and Confirm New Passcode
            else {
                if (firstEntry.isEmpty()) {
                    firstEntry = enteredPasscode.toString();
                    tvTitle.setText(R.string.ReenterPasscode);
                    enteredPasscode.setLength(0);
                    updateDots();
                } else if (firstEntry.equals(enteredPasscode.toString())) {
                    preferences.edit().putString("passcode", firstEntry).apply();
                    finish();
                } else {
                    tvTitle.setText(R.string.PasscodesdonotmatchEnterNewPasscode);
                    firstEntry = "";
                    enteredPasscode.setLength(0);
                    updateDots();
                }
            }

        } else {
            // Authentication Mode
            if (savedPasscode.equals(enteredPasscode.toString())) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                tvTitle.setText(R.string.IncorrectPasscodeTryagain);
                enteredPasscode.setLength(0);
                updateDots();
            }
        }
    }

    public void onDeletePress(View view) {
        if (enteredPasscode.length() > 0) {
            enteredPasscode.deleteCharAt(enteredPasscode.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < dots.size(); i++) {
            if (i < enteredPasscode.length()) {
                dots.get(i).setBackgroundResource(R.drawable.dot_filled);
            } else {
                dots.get(i).setBackgroundResource(R.drawable.dot_unfilled);
            }
        }
    }
    @Override
    public void onBackPressed() {
        SharedPreferences passcodePreferences = getSharedPreferences("PasscodePrefs", MODE_PRIVATE);
        String savedPasscode = passcodePreferences.getString("passcode", "");

        Intent intent = new Intent();
        if (savedPasscode.isEmpty()) {
            // No passcode set, return RESULT_CANCELED
            intent.putExtra("passcode_not_set", true);
            setResult(Activity.RESULT_CANCELED, intent);
        } else {
            setResult(Activity.RESULT_OK);
        }
        super.onBackPressed();
    }


}