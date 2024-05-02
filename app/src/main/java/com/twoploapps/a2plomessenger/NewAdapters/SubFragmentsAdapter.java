package com.twoploapps.a2plomessenger.NewAdapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.twoploapps.a2plomessenger.ChatFragment;
import com.twoploapps.a2plomessenger.FragmentsNew.CanalesFragment;
import com.twoploapps.a2plomessenger.GruposFragment;
import com.twoploapps.a2plomessenger.R;

public class SubFragmentsAdapter extends FragmentPagerAdapter {
    private static final int NUM_PAGES = 3;
    private Context context;

    public SubFragmentsAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ChatFragment();
            case 1:
                return new GruposFragment();
            case 2:
                return new CanalesFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.chats);
            case 1:
                return context.getString(R.string.groups);
            case 2:
                return context.getString(R.string.channels);
            default:
                return null;
        }
    }
}
