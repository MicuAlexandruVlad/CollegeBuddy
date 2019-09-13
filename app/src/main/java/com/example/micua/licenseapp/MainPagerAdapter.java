package com.example.micua.licenseapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class MainPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    public static final int NUM_ITEMS = 3;
    private String tabTitles[] = new String[] { "Groups", "Faculties", "Colleges" };
    private Bundle bundleGroups, bundleColleges, bundleFaculties;

    public MainPagerAdapter(FragmentManager fragmentManager, Bundle bundleGroups, Bundle bundleColleges, Bundle bundleFaculties) {
        super(fragmentManager);
        this.bundleGroups = bundleGroups;
        this.bundleColleges = bundleColleges;
        this.bundleFaculties = bundleFaculties;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0: {
                return GroupsFragment.newInstance(0, "Groups", bundleGroups);
            }
            case 1: {
                return FacultiesFragment.newInstance(1, "Faculties", bundleFaculties);
            }
            case 2: {
                return CollegesFragment.newInstance(2, "Colleges", bundleColleges);
            }
            default: return null;

        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
