package com.notedok.notedok;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class NoteDetailViewPagerAdapter extends FragmentStatePagerAdapter {
    public NoteDetailViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        NoteDetailViewPageFragment fragment = new NoteDetailViewPageFragment();

        // Pass the note position to the fragment
        Bundle args = new Bundle();
        args.putInt(NoteDetailViewPageFragment.POSITION_ARGUMENT_NAME, position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return CurrentFileList.getInstance().getLength();
    }
}

