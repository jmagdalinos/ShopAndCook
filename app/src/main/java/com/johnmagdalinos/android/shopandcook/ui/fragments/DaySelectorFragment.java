/*
* Copyright (C) 2017 John Magdalinos
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.johnmagdalinos.android.shopandcook.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.ui.DetailActivity;
import com.johnmagdalinos.android.shopandcook.ui.adapters.DayPagerAdapter;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;

/**
 * Fragment displaying a viewpager in order for the user to swipe to individual days
 */

public class DaySelectorFragment extends android.support.v4.app.Fragment {
    /** Member variables */
    private String mUId;
    private int mPosition = 0;

    /** Class constructor */
    public static DaySelectorFragment newInstance(String userId, int position) {
        DaySelectorFragment fragment = new DaySelectorFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.KEY_DAY_ID, position);
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_day_selector, container, false);

        DetailActivity.mCurrentFragment = Constants.EXTRAS_MEAL_PLANNER;

        if (getArguments() != null) {
            mPosition = getArguments().getInt(Constants.KEY_DAY_ID);
            mUId = getArguments().getString(Constants.KEY_USER_ID);
        }

        DayPagerAdapter adapter = new DayPagerAdapter(mUId, getChildFragmentManager(),
                getActivity());

        ViewPager viewpager = rootView.findViewById(R.id.pager_day_selector);
        viewpager.setAdapter(adapter);

        TabLayout tabLayout = rootView.findViewById(R.id.tl_day_selector);
        tabLayout.setupWithViewPager(viewpager);

        // Set the title
        getActivity().setTitle(getActivity().getResources().getString(R.string.main_meal_planner));

        viewpager.setCurrentItem(mPosition);

        return rootView;
    }

    /** Set the current fragment in the detail activity to null */
    @Override
    public void onDestroyView() {
        DetailActivity.mCurrentFragment = null;
        super.onDestroyView();
    }
}
