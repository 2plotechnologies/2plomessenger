package com.twoploapps.a2plomessenger;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.twoploapps.a2plomessenger.FragmentsNew.MainChatsFragment;

public class AcesoTabsAdapter extends FragmentPagerAdapter {
    private Context context;

    public AcesoTabsAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                MainChatsFragment chatFragment = new MainChatsFragment();
                return chatFragment;
            case 1:
                PostsFragment postsFragment = new PostsFragment();
                return postsFragment;
            case 2:
                ContactosFragment contactosFragment = new ContactosFragment();
                return contactosFragment;
            case 3:
                SolicitudesFragment solicitudesFragment = new SolicitudesFragment();
                return solicitudesFragment;
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return 4;
    }
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:

                return "";
            case 1:

                return "";
            case 2:

                return "";
            case 3:

                return "";
            default:
                return null;
        }
    }

}
