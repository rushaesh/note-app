package com.example.notes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_SCREEN_TIME_OUT = 1000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Check if the app is opened for the first time
        SharedPreferences sharedPreferences = getSharedPreferences("AppPre", MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);
        SharedPreferences preferences = getSharedPreferences("PasscodePrefs", MODE_PRIVATE);
        String savedPasscode = preferences.getString("passcode", "");
        SharedPreferences sharedPreferences1 = getSharedPreferences("Pref", MODE_PRIVATE);
        boolean isSkipped = sharedPreferences1.getBoolean("isSkipped", false);

        TextView tvVersion = findViewById(R.id.version);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            int versionCode = pInfo.versionCode;

            tvVersion.setText("Version: " + versionName  + versionCode + " ");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (isFirstTime) {
                    // Open LanguageActivity for first-time users
                    intent = new Intent(SplashActivity.this, LanguageActivity.class);

                    // Update SharedPreferences to mark that the app has been opened before
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isFirstTime", false);
                    editor.apply();
                } else if (!isSkipped) {
                    intent = new Intent(SplashActivity.this, LanguageActivity.class);
                } else if (savedPasscode.isEmpty()) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // Open MainActivity for returning users
                    intent = new Intent(SplashActivity.this, passcode.class);
                }

                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN_TIME_OUT);
    }
}
