package com.notedok.notedok;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class NoteDetailViewActivity extends AppCompatActivity {
    private ViewPager _viewPager;
    private ArrayList<String> _files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set context
        setContentView(R.layout.activity_note_view);

        // Display the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Unpack the parameters
        Intent intent = getIntent();
        _files = intent.getStringArrayListExtra(MasterActivity.FILES_INTENT_EXTRA_NAME);
        int position = intent.getIntExtra(MasterActivity.POSITION_INTENT_EXTRA_NAME, -1);

        if (_files != null && _files.size() > 0 && position >= 0) {
            // Create the pager view
            _viewPager = (ViewPager) findViewById(R.id.note_view_pager);

            // Set the view adapter
            NoteDetailViewPagerAdapter pagerAdapter = new NoteDetailViewPagerAdapter(getSupportFragmentManager(), _files);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_detail_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // back button in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.action_edit:
                int position = _viewPager.getCurrentItem();

                // TODO: disable the button completely?
                if (_files != null && _files.size() > 0 && position >= 0) {
                    Intent intent = new Intent(NoteDetailViewActivity.this, NoteEditorActivity.class);
                    // TODO: pass data somehow
                    // TODO: currently does not pass the current position correctly - the one passed is the initial one which never gets updated
                    intent.putStringArrayListExtra(NoteEditorActivity.FILES_INTENT_EXTRA_NAME, _files);
                    intent.putExtra(NoteEditorActivity.POSITION_INTENT_EXTRA_NAME, position);
                    startActivity(intent);
                }
                else {
                    // Do not react - nothing to edit.
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
