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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.johnmagdalinos.android.shopandcook.model.Day;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;

import java.util.ArrayList;

/**
 * AppWidget displaying all the days of the week
 */
public class WeekAppWidget extends AppWidgetProvider {
    /** Member variables */
    public static ArrayList<Day> mWeek;

    /** Launch an IntentService to update the widget in the background */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Launch the update service
        Intent updateIntent = new Intent(context.getApplicationContext(),
                WidgetUpdateService.class);
        updateIntent.putExtra(Constants.WIDGET_NAME, Constants.WIDGET_WEEK);
        updateIntent.putExtra(Constants.KEY_EXTRAS, appWidgetIds);

        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(updateIntent);
        } else {
            context.startService(updateIntent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

