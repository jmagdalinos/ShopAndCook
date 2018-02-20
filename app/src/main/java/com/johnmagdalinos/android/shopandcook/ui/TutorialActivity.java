package com.johnmagdalinos.android.shopandcook.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.ui.adapters.TutorialPagerAdapter;

/**
 * Tutorial activity displayed only on first run
 */
public class TutorialActivity extends AppCompatActivity {

    /** Member variables */
    private SharedPreferences mSharedPrefs;
    private static final String KEY_IS_FIRST_RUN = "firstrun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Get the first run flag
        mSharedPrefs = getSharedPreferences("shopandcook", Context.MODE_PRIVATE);
        Boolean isFirstRun = mSharedPrefs.getBoolean(KEY_IS_FIRST_RUN, true);

        // If this is not the first run skip the tutorial
        if (!isFirstRun) {
            goToMainActivity(false);
        }

        ViewPager viewPager = findViewById(R.id.vp_tutorial);
        TabLayout tabLayout = findViewById(R.id.tl_tutorial);
        viewPager.setAdapter(new TutorialPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        // Setup the TextView so it skips to the main activity when pressed
        TextView textView = findViewById(R.id.tv_tutorial);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            goToMainActivity(true);
            }
        });
    }

    private void goToMainActivity(boolean saveFlag) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(KEY_IS_FIRST_RUN, false);
        editor.commit();

        Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
        startActivity(intent);

        // Save the first run flag in the preferences
        finish();
    }
}
