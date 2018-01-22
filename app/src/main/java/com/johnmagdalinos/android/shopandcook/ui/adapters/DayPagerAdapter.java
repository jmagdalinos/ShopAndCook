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

package com.johnmagdalinos.android.shopandcook.ui.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.ui.fragments.DayFragment;

/**
 * Adapter used in the DaySelectorFragment's viewpager to display the days of the week
 */

public class DayPagerAdapter extends FragmentPagerAdapter {
    /** Member Variables */
    private String mUId;
    private String[] days;

    /** Class constructor */
    public DayPagerAdapter(String user_id, FragmentManager fm, Context context) {
        super(fm);
        days = context.getResources().getStringArray(R.array.days_of_the_week);
        mUId = user_id;
    }

   /** Returns a new DayFragment for each day */
    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        return DayFragment.newInstance(mUId, position);
    }

    /** Displays the name of the day */
    @Override
    public CharSequence getPageTitle(int position) {
        return days[position];
    }

    /** Always return 7 days */
    @Override
    public int getCount() {
        return 7;
    }
}
