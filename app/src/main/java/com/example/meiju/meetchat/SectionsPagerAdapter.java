package com.example.meiju.meetchat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter{

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 1:
                PlansFragment plansFragment = new PlansFragment();
                return plansFragment;
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "FRIENDS";
            case 1:
                return "PLANS";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }
}
