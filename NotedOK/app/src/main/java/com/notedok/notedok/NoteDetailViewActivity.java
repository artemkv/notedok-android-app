package com.notedok.notedok;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class NoteDetailViewActivity extends AppCompatActivity {
    private ViewPager _viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set context
        setContentView(R.layout.activity_note_view);

        // Unpack the parameters
        Intent intent = getIntent();
        ArrayList<String> files = intent.getStringArrayListExtra(MasterActivity.FILES_INTENT_EXTRA_NAME);
        int position = intent.getIntExtra(MasterActivity.POSITION_INTENT_EXTRA_NAME, -1);

        if (files != null && files.size() > 0 && position >= 0) {
            // Create the pager view
            _viewPager = (ViewPager) findViewById(R.id.note_view_pager);

            // Set the view adapter
            NoteDetailViewPagerAdapter pagerAdapter = new NoteDetailViewPagerAdapter(getSupportFragmentManager(), files);
            _viewPager.setAdapter(pagerAdapter);

            // Set the current page
            _viewPager.setCurrentItem(position);
        }
        else {
            // TODO: Show message "no notes to show"
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO: resume correctly
    }
}
