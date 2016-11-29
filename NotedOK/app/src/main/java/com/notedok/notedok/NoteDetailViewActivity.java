package com.notedok.notedok;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class NoteDetailViewActivity extends AppCompatActivity {
    private static final int EDIT_NOTE_REQUEST_CODE = 1;

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
        } else {
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
                onHomeAction();
                return true;
            case R.id.action_edit:
                onEditAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onHomeAction() {
        // back button in action bar clicked; goto parent activity.
        finish();
    }

    private void onEditAction() {
        int position = _viewPager.getCurrentItem();

        // TODO: disable the button completely?
        if (_files != null && _files.size() > 0 && position >= 0) {
            Intent intent = new Intent(NoteDetailViewActivity.this, NoteEditorActivity.class);
            intent.putStringArrayListExtra(NoteEditorActivity.FILES_INTENT_EXTRA_NAME, _files);
            intent.putExtra(NoteEditorActivity.POSITION_INTENT_EXTRA_NAME, position);
            startActivityForResult(intent, EDIT_NOTE_REQUEST_CODE);
        } else {
            // Do not react - nothing to edit.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == EDIT_NOTE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // TODO: somehow refresh fragment
            }
        }
    }
}
