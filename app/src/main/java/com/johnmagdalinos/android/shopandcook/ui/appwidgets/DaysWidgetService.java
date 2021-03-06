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

package com.johnmagdalinos.android.shopandcook.ui.appwidgets;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.model.Day;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;

import java.util.ArrayList;

/**
 * RemoteViewsService for the Day widget
 */

public class DaysWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DaysRemoteViewsFactory(this.getApplicationContext());
    }
}

/** RemoteViewsFactory used to populate the stack view of the Day widget */
class DaysRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    /** Member variables */
    private Context mContext;
    private ArrayList<Day> mDays;

    /** Class constructor */
    public DaysRemoteViewsFactory(Context context) {
        mContext = context;
        mDays = DayAppWidget.mDays;
    }

    @Override
    public void onCreate() {

    }

    /** Gets the days list from the widget */
    @Override
    public void onDataSetChanged() {
        mDays = DayAppWidget.mDays;
    }

    @Override
    public void onDestroy() {

    }

    /** Returns the size of the list */
    @Override
    public int getCount() {
        if (mDays != null) {

            return mDays.size();
        } else {
            return 0;
        }
    }

    /** Display all the day's data */
    @Override
    public RemoteViews getViewAt(int position) {
        if (mDays.size() == 0) return null;

        // Get the current Day
        Day currentDay = mDays.get(position);
        int day = currentDay.getDayOfTheWeek();

        // Get the values
        String dayName = Day.intToDay(mContext, day);
        String breakfast = currentDay.getBreakfastName();
        String morningSnack = currentDay.getMorningSnackName();
        String lunch = currentDay.getLunchName();
        String afternoonSnack = currentDay.getAfternoonSnackName();
        String dinner = currentDay.getDinnerName();
        String noMeal = mContext.getResources().getString(R.string.widget_day_no_meal);

        // Create a RemoteView using the app_widget_day_item as a template
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout
                .app_widget_day_item);

        // Display the values, checking for nulls
        views.setTextViewText(R.id.tv_widget_day_title, dayName);

        if (breakfast != null && !TextUtils.isEmpty(breakfast)) {
            views.setTextViewText(R.id.tv_widget_day_breakfast, breakfast);
        } else {
            views.setTextViewText(R.id.tv_widget_day_breakfast, noMeal);
        }


        if (morningSnack != null && !TextUtils.isEmpty(morningSnack)) {
            views.setTextViewText(R.id.tv_widget_day_morning_snack, morningSnack);
        } else {
            views.setTextViewText(R.id.tv_widget_day_morning_snack, noMeal);
        }

        if (lunch != null && !TextUtils.isEmpty(lunch)) {
            views.setTextViewText(R.id.tv_widget_day_lunch, lunch);
        } else {
            views.setTextViewText(R.id.tv_widget_day_lunch, noMeal);
        }

        if (afternoonSnack != null && !TextUtils.isEmpty(afternoonSnack)) {
            views.setTextViewText(R.id.tv_widget_day_afternoon_snack, afternoonSnack);
        } else {
            views.setTextViewText(R.id.tv_widget_day_afternoon_snack, noMeal);
        }

        if (dinner != null && !TextUtils.isEmpty(dinner)) {
            views.setTextViewText(R.id.tv_widget_day_dinner, dinner);
        } else {
            views.setTextViewText(R.id.tv_widget_day_dinner, noMeal);
        }

        // Set an empty fill in intent to enable click functionality for each item
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.EXTRAS_MEAL_PLANNER, day);
        views.setOnClickFillInIntent(R.id.ll_widget_day_title, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}