package com.notedok.notedok;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class NoteDetailViewPagerAdapter extends FragmentStatePagerAdapter {
    private FileList _fileList;

    public NoteDetailViewPagerAdapter(FragmentManager fragmentManager, ArrayList<String> files) {
        super(fragmentManager);

        if (files == null)
            throw new IllegalArgumentException("files");

        _fileList = new FileList(files);
    }

    @Override
    public Fragment getItem(int position) {
        NoteDetailViewPageFragment fragment = new NoteDetailViewPageFragment();

        // Pass the note position to the fragment
        Bundle args = new Bundle();
        args.putStringArrayList(NoteDetailViewPageFragment.FILES_ARGUMENT_NAME, _fileList.getAsArrayList());
        args.putInt(NoteDetailViewPageFragment.POSITION_ARGUMENT_NAME, position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return _fileList.getLength();
    }
}

