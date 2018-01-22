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

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.johnmagdalinos.android.shopandcook.utilities.Constants;

/**
 * IntentService called to update each of the widgets
 */

public class WidgetUpdateService extends IntentService {
    /** Class constructor */
    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Get the extra from the intent
        String widgetName = intent.getStringExtra(Constants.WIDGET_NAME);

        // Get the user id from the preferences
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.KEY_USER_ID,
                MODE_PRIVATE);
        String uId = sharedPreferences.getString(Constants.KEY_USER_ID, null);

        // Get all the widget ids
        int[] shoppingWidgetIds = intent.getIntArrayExtra(Constants.WIDGET_SHOPPING_LIST);
        int[] dayWidgetIds = intent.getIntArrayExtra(Constants.WIDGET_SINGLE_DAY);
        int[] weekWidgetIds = intent.getIntArrayExtra(Constants.WIDGET_WEEK);

        if (Build.VERSION.SDK_INT >= 26) {
            showNotification();
        }

        // Identify which widgets have to be updated
        switch (widgetName) {
            case Constants.WIDGET_SHOPPING_LIST:
                // Update the shopping list widget
                WidgetUpdateTasks.updateShoppingListWidget(this, intent, uId);
                break;
            case Constants.WIDGET_SINGLE_DAY:
                // Update the day widget
                WidgetUpdateTasks.updateDayWidget(this, intent, uId);
                break;
            case Constants.WIDGET_WEEK:
                // Update the week widget
                WidgetUpdateTasks.updateWeekWidget(this, intent, uId);
                break;
            case Constants.WIDGET_ALL:
                // Update the shopping list widget
                Intent shoppingUpdateIntent = new Intent(this.getApplicationContext(),
                        WidgetUpdateService.class);
                shoppingUpdateIntent.putExtra(Constants.WIDGET_NAME, Constants.WIDGET_SHOPPING_LIST);
                shoppingUpdateIntent.putExtra(Constants.KEY_EXTRAS, shoppingWidgetIds);

                // Update the day widget
                Intent dayUpdateIntent = new Intent(this.getApplicationContext(),
                        WidgetUpdateService.class);
                dayUpdateIntent.putExtra(Constants.WIDGET_NAME, Constants.WIDGET_SINGLE_DAY);
                dayUpdateIntent.putExtra(Constants.KEY_EXTRAS, dayWidgetIds);

                // Update the week widget
                Intent weekUpdateIntent = new Intent(this.getApplicationContext(),
                        WidgetUpdateService.class);
                weekUpdateIntent.putExtra(Constants.WIDGET_NAME, Constants.WIDGET_WEEK);
                weekUpdateIntent.putExtra(Constants.KEY_EXTRAS, weekWidgetIds);

                WidgetUpdateTasks.updateShoppingListWidget(this, shoppingUpdateIntent, uId);
                WidgetUpdateTasks.updateDayWidget(this, dayUpdateIntent, uId);
                WidgetUpdateTasks.updateWeekWidget(this, weekUpdateIntent, uId);
        }
    }

    /** Shows notification as per API 26 */
    @TargetApi(26)
    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context
                .NOTIFICATION_SERVICE);
        // Sets an ID for the notification, so it can be updated.
        String id = "my_channel_01";
        CharSequence name = "channel_01";
        int importance = NotificationManager.IMPORTANCE_LOW;
        // The id of the channel.
        String CHANNEL_ID = "my_channel_01";

        NotificationChannel channel = new NotificationChannel(id, name,importance);

        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setTicker("channel_01")
                .setContentText("channel_01")
                .build();

        startForeground(111, notification);
    }
}
