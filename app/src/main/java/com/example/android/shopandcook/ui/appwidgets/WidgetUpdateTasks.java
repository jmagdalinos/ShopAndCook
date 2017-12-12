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

package com.example.android.shopandcook.ui.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.model.Day;
import com.example.android.shopandcook.model.Ingredient;
import com.example.android.shopandcook.ui.DetailActivity;
import com.example.android.shopandcook.ui.MainActivity;
import com.example.android.shopandcook.utilities.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Helper class with methods for updating the widgets. It is called from the WidgetUpdateService.
 */

class WidgetUpdateTasks {

    /** Called to update the ShoppingList widget */
    public static void updateShoppingListWidget(Context context, Intent intent, String uId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Create the remote views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout
                .app_widget_shopping_list);

        // Get the ids for all the widgets
        int[] appWidgetIds = intent.getIntArrayExtra(Constants.KEY_EXTRAS);

        if (appWidgetIds == null || appWidgetIds.length == 0) return;

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent activityIntent;
            boolean isTablet = context.getResources().getBoolean(R.bool.tablet_mode);

            // Create the appropriate pending intent for tablet/phone
            if (isTablet) {
                activityIntent = new Intent(context, MainActivity.class);
            } else {
                activityIntent = new Intent(context, DetailActivity.class);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }

            // Set the data to differentiate this intent
            Uri uri = Uri.parse(Constants.EXTRAS_SHOPPING_LIST);
            activityIntent.setData(uri);
            activityIntent.putExtra(Constants.KEY_EXTRAS, Constants.EXTRAS_SHOPPING_LIST);
            activityIntent.putExtra(Constants.KEY_USER_ID, uId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

            // Setup the empty view
            if (uId == null || TextUtils.isEmpty(uId)) {
                views.setViewVisibility(R.id.tv_app_widget_shopping_empty_view, View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.tv_app_widget_shopping_empty_view, View.GONE);

                views.setOnClickPendingIntent(R.id.tv_app_widget_shopping, pendingIntent);
                views.setPendingIntentTemplate(R.id.lv_app_widget_shopping, pendingIntent);
                populateShoppingList(context, appWidgetManager, appWidgetId, views, uId);
            }
        }
    }

    /** Called to update the Day widget */
    public static void updateDayWidget(Context context, Intent intent, String uId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Create the remote views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout
                .app_widget_day_flipper);

        // Get the ids for all the widgets
        int[] appWidgetIds = intent.getIntArrayExtra(Constants.KEY_EXTRAS);

        if (appWidgetIds == null || appWidgetIds.length == 0) return;

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent activityIntent;
            boolean isTablet = context.getResources().getBoolean(R.bool.tablet_mode);

            // Create the appropriate pending intent for tablet/phone
            if (isTablet) {
                activityIntent = new Intent(context, MainActivity.class);
            } else {
                activityIntent = new Intent(context, DetailActivity.class);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            // Set the data to differentiate this intent
            Uri uri = Uri.parse(Constants.EXTRAS_MEAL_PLANNER);
            activityIntent.setData(uri);
            activityIntent.putExtra(Constants.KEY_EXTRAS, Constants.EXTRAS_MEAL_PLANNER);
            activityIntent.putExtra(Constants.KEY_USER_ID, uId);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

            // Setup the empty view
            if (uId == null || TextUtils.isEmpty(uId)) {
                views.setViewVisibility(R.id.tv_app_widget_day_empty_view, View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.tv_app_widget_day_empty_view, View.GONE);

                views.setPendingIntentTemplate(R.id.flipper_app_widget_day, pendingIntent);
                populateDays(context, appWidgetManager, appWidgetId, views, uId);
            }
        }
    }

    /** Called to update the Week widget */
    public static void updateWeekWidget(Context context, Intent intent, String uId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Create the remote views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout
                .app_widget_week);

        // Get the ids for all the widgets
        int[] appWidgetIds = intent.getIntArrayExtra(Constants.KEY_EXTRAS);

        if (appWidgetIds == null || appWidgetIds.length == 0) return;

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent activityIntent;
            boolean isTablet = context.getResources().getBoolean(R.bool.tablet_mode);

            // Create the appropriate pending intent for tablet/phone
            if (isTablet) {
                activityIntent = new Intent(context, MainActivity.class);
            } else {
                activityIntent = new Intent(context, DetailActivity.class);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            // Set the data to differentiate this intent
            Uri uri = Uri.parse(Constants.EXTRAS_MEAL_PLANNER);
            activityIntent.setData(uri);
            activityIntent.putExtra(Constants.KEY_EXTRAS, Constants.EXTRAS_MEAL_PLANNER);
            activityIntent.putExtra(Constants.KEY_USER_ID, uId);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

            // Setup the empty view
            if (uId == null || TextUtils.isEmpty(uId)) {
                views.setViewVisibility(R.id.tv_app_widget_week_empty_view, View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.tv_app_widget_week_empty_view, View.GONE);

                views.setPendingIntentTemplate(R.id.app_widget_week, pendingIntent);
                populateWeek(context, appWidgetManager, appWidgetId, views, uId);
            }
        }

    }

    /** Gets all the shopping list items from the Firebase Database */
    private static void populateShoppingList(final Context context, final AppWidgetManager appWidgetManager, final
    int appWidgetId, final RemoteViews views, String uId) {

        // Get an instance of the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ingredientsRef = database.getReference()
                .child(Constants.NODE_SHOPPING_LIST)
                .child(uId);

        // Add a listener to get all the database items
        ingredientsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Ingredient> shoppingList = new ArrayList<>();

                // Get each shopping list item
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Ingredient ingredient = child.getValue(Ingredient.class);
                    shoppingList.add(ingredient);
                }
                shoppingList = sumIngredients(shoppingList);

                // Sort the list
                Collections.sort(shoppingList, Ingredient.AscendingNameComparator);

                // Pass the complete list to the widget
                ShoppingListAppWidget.mShoppingList = shoppingList;

                // Set the remote adapter on the view
                Intent listIntent = new Intent(context, ShoppingListWidgetService.class);
                views.setRemoteAdapter(R.id.lv_app_widget_shopping, listIntent);

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_app_widget_shopping);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Gets the meals for every day from the Firebase Database */
    private static void populateDays(final Context context, final AppWidgetManager appWidgetManager, final int
            appWidgetId, final RemoteViews views, String uId) {

        // Get an instance of the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference daysRef = database.getReference()
                .child(Constants.NODE_DAYS)
                .child(uId);

        // Add a listener to get all the database items
        daysRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Day> days = new ArrayList<>();

                // Get each day
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Day day = child.getValue(Day.class);
                    days.add(day);
                }

                // Sort the list
                Collections.sort(days, Day.AscendingComparator);

                // Pass the complete list to the widget
                DayAppWidget.mDays = days;

                // Set the remote adapter on the view
                Intent listIntent = new Intent(context, DaysWidgetService.class);
                views.setRemoteAdapter(R.id.flipper_app_widget_day, listIntent);

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.flipper_app_widget_day);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Gets the meals for every day from the Firebase Database */
    private static void populateWeek(final Context context, final AppWidgetManager appWidgetManager,
                                  final int appWidgetId, final RemoteViews views, String uId) {

        // Get an instance of the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference daysRef = database.getReference()
                .child(Constants.NODE_DAYS)
                .child(uId);

        // Add a listener to get all the database items
        daysRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Day> week = new ArrayList<>();

                // Get each day
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Day day = child.getValue(Day.class);
                    week.add(day);
                }

                // Sort the list
                Collections.sort(week, Day.AscendingComparator);

                // Pass the complete list to the widget
                WeekAppWidget.mWeek = week;

                // Set the remote adapter on the view
                Intent listIntent = new Intent(context, WeekWidgetService.class);
                views.setRemoteAdapter(R.id.app_widget_week, listIntent);

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.app_widget_week);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Sums the shopping items with the same name and measure */
    private static ArrayList<Ingredient> sumIngredients(ArrayList<Ingredient> ingredientArray) {

        ArrayList<Ingredient> ingredients = new ArrayList<>();

        // Iterate through all of the ingredients
        for (Ingredient ingredient : ingredientArray) {

            if (ingredients.size() == 0) {
                // This is the first item, add it
                ingredients.add(ingredient);
            } else {
                // Check if the item already exists
                if (checkIfNewIngredient(ingredients, ingredient)) {
                    ingredients.add(ingredient);
                }
            }
        }
        return ingredients;
    }

    /** Checks if an shopping item exists. If so, it just updates its quantity */
    private static boolean checkIfNewIngredient(ArrayList<Ingredient> ingredients, Ingredient
            currentIngredient) {
        boolean check = true;

        // Get the current shopping item
        String name = currentIngredient.getName();
        int measure = currentIngredient.getMeasure();
        double quantity = currentIngredient.getQuantity();
        String ingredientId = currentIngredient.getIngredientId();

        // Iterate through the current list of shopping items
        for (Ingredient ingredient : ingredients) {
            String name1 = ingredient.getName();
            int measure1 = ingredient.getMeasure();
            double quantity1 = ingredient.getQuantity();
            String ingredientId1 = ingredient.getIngredientId();

            if (name1.toUpperCase().trim().equals(name.toUpperCase().trim()) && measure1 ==
                    measure) {
                // Increment the quantity
                ingredient.setQuantity(quantity + quantity1);
                // Set a new id in order to be able to delete all items with one action
                String newId = ingredientId + "," + ingredientId1;
                ingredient.setIngredientId(newId);
                check = false;
                break;
            } else {
                check = true;
            }
        }
        return check;
    }
}
