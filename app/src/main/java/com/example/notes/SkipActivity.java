package com.example.notes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.notes.Adptor.ViewPagerAdapter;


public class SkipActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ImageView dot1, dot2, dot3;
    TextView  btnSkip, btnNext;
    private int[] images = {R.drawable.skip1, R.drawable.skip2, R.drawable.skip3};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skip);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        viewPager = findViewById(R.id.viewPager);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        btnNext = findViewById(R.id.next);
        btnSkip = findViewById(R.id.skip);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, images);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                if (position == images.length - 1) {
                    btnNext.setText(R.string.GetStarted);
                } else {
                    btnNext.setText(R.string.Next);
                }
            }
        });
        btnSkip.setOnClickListener(v -> openMainActivity());
        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < images.length - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                openMainActivity(); // Last page, open MainActivity
            }
        });


        updateDots(0); // Set first dot as active
    }
    private void openMainActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences("Pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isSkipped", true);
        editor.apply();

        Intent intent = new Intent(SkipActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void updateDots(int position) {
        dot1.setBackgroundResource(R.drawable.dot_default);
        dot2.setBackgroundResource(R.drawable.dot_default);
        dot3.setBackgroundResource(R.drawable.dot_default);

        switch (position) {
            case 0:
                dot1.setBackgroundResource(R.drawable.dot_selected);
                break;
            case 1:
                dot2.setBackgroundResource(R.drawable.dot_selected);
                break;
            case 2:
                dot3.setBackgroundResource(R.drawable.dot_selected);
                break;
        }
    }
}