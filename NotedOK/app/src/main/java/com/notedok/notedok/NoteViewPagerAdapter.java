package com.notedok.notedok;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class NoteViewPagerAdapter extends FragmentStatePagerAdapter {
    public NoteViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        NoteViewPageFragment fragment = new NoteViewPageFragment();

        // Pass the note position to the fragment
        Bundle args = new Bundle();
        args.putInt(NoteViewPageFragment.POSITION_ARGUMENT_NAME, position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return CurrentFileList.getInstance().length();
    }
}

