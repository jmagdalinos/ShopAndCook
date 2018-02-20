package com.johnmagdalinos.android.shopandcook.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.johnmagdalinos.android.shopandcook.ui.fragments.TutorialFragment;

/**
 * Adapter used in the Tutorial Activity's viewpager to display the tutorial slides
 */

public class TutorialPagerAdapter extends FragmentPagerAdapter {

    /** Class constructor */
    public TutorialPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return TutorialFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 6;
    }
}
