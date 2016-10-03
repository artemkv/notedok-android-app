package com.notedok.notedok;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NoteDetailViewActivity extends AppCompatActivity {
    private ViewPager _viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set context
        setContentView(R.layout.activity_note_view);

        // Which note is it?
        Intent intent = getIntent();
        int position = intent.getIntExtra("pos", -1);

        if (position >= 0) {
            // Create the pager view
            _viewPager = (ViewPager) findViewById(R.id.note_view_pager);

            // Set the view adapter
            NoteDetailViewPagerAdapter pagerAdapter = new NoteDetailViewPagerAdapter(getSupportFragmentManager());
            _viewPager.setAdapter(pagerAdapter);

            // Set the current page
            _viewPager.setCurrentItem(position);
        }
    }
}
