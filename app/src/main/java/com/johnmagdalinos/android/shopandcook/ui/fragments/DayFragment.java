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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.model.Day;
import com.johnmagdalinos.android.shopandcook.model.Ingredient;
import com.johnmagdalinos.android.shopandcook.model.Meal;
import com.johnmagdalinos.android.shopandcook.model.Recipe;
import com.johnmagdalinos.android.shopandcook.ui.appwidgets.DayAppWidget;
import com.johnmagdalinos.android.shopandcook.ui.appwidgets.WeekAppWidget;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


/**
 * Fragment displaying a single day and its meals
 */
public class DayFragment extends Fragment implements
        View.OnClickListener, View.OnLongClickListener {

    /** Member variables */
    private String mUId, mCurrentDayId;
    private DayCallback mCallback;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDayMealsRef, mMealsRef;
    private HashMap<String, Meal> mDatabaseListItems;
    private TextView mBreakfastTextView, mMorningSnackTextView, mLunchTextView,
            mAfternoonSnackTextView, mDinnerTextView;
    private int mCurrentDay;
    private Context mContext;

    /** Key for the Fragment args */
    private static final String KEY_DAY = "day";

    public interface DayCallback {
        void onDayMealClick(Meal meal, String day_id, String meal_title, String meal_title_name);
    }

    /** Override onAttach to make sure that the container activity has implemented the callback */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (DayCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DayCallback");
        }
    }

    /** Class Constructor for use with the database */
    public static DayFragment newInstance(String userId, int day) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_DAY, day);
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_day, container, false);

        mContext = getActivity();

        setHasOptionsMenu(true);

        // Get the arguments
        if (getArguments() != null) {
            mCurrentDay = getArguments().getInt(KEY_DAY);
            mCurrentDayId = Day.intToDay(mContext, getArguments().getInt(KEY_DAY));
            mUId = getArguments().getString(Constants.KEY_USER_ID);
        }

        // Initialize the HashMap
        mDatabaseListItems = new HashMap<>();

        mBreakfastTextView = rootView.findViewById(R.id.tv_day_breakfast);
        TextView breakfastTitleTextView = rootView.findViewById(R.id.tv_day_breakfast_title);
        mMorningSnackTextView = rootView.findViewById(R.id.tv_day_morning_snack);
        TextView morningSnackTitleTextView = rootView.findViewById(R.id.tv_day_morning_snack_title);
        mLunchTextView = rootView.findViewById(R.id.tv_day_lunch);
        TextView lunchTitleTextView = rootView.findViewById(R.id.tv_day_lunch_title);
        mAfternoonSnackTextView = rootView.findViewById(R.id.tv_day_afternoon_snack);
        TextView afternoonSnackTitleTextView = rootView.findViewById(R.id
                .tv_day_afternoon_snack_title);
        mDinnerTextView = rootView.findViewById(R.id.tv_day_dinner);
        TextView dinnerTitleTextView = rootView.findViewById(R.id.tv_day_dinner_title);

        // Set the click listeners on the texts and titles
        mBreakfastTextView.setOnClickListener(this);
        mBreakfastTextView.setOnLongClickListener(this);
        mMorningSnackTextView.setOnClickListener(this);
        mMorningSnackTextView.setOnLongClickListener(this);
        mLunchTextView.setOnClickListener(this);
        mLunchTextView.setOnLongClickListener(this);
        mAfternoonSnackTextView.setOnClickListener(this);
        mAfternoonSnackTextView.setOnLongClickListener(this);
        mDinnerTextView.setOnClickListener(this);
        mDinnerTextView.setOnLongClickListener(this);

        breakfastTitleTextView.setOnClickListener(this);
        breakfastTitleTextView.setOnLongClickListener(this);
        morningSnackTitleTextView.setOnClickListener(this);
        morningSnackTitleTextView.setOnLongClickListener(this);
        lunchTitleTextView.setOnClickListener(this);
        lunchTitleTextView.setOnLongClickListener(this);
        afternoonSnackTitleTextView.setOnClickListener(this);
        afternoonSnackTitleTextView.setOnLongClickListener(this);
        dinnerTitleTextView.setOnClickListener(this);
        dinnerTitleTextView.setOnLongClickListener(this);

        // Get the custom font and set it on the titles
        Typeface mFont = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/courgette_regular.ttf");

        breakfastTitleTextView.setTypeface(mFont);
        morningSnackTitleTextView.setTypeface(mFont);
        lunchTitleTextView.setTypeface(mFont);
        afternoonSnackTitleTextView.setTypeface(mFont);
        dinnerTitleTextView.setTypeface(mFont);

        setupForDatabase();

        return rootView;
    }

    /** Loads the Meal data from the database into the views */
    private void setupForDatabase() {
        // Get an instance of the database
        mDatabase = FirebaseDatabase.getInstance();

        // Setup the reference for the meals
        mDayMealsRef = mDatabase.getReference()
                .child(Constants.NODE_DAYS)
                .child(mUId);

        mDayMealsRef
                .child(mCurrentDayId)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                if(!key.equals(Constants.NODE_DAY_OF_THE_WEEK)) {
                    String  mealId = dataSnapshot.getValue(String.class);
                    addMealToList(key, mealId);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                if(!key.equals(Constants.NODE_DAY_OF_THE_WEEK)) {
                    String  mealId = dataSnapshot.getValue(String.class);
                    addMealToList(key, mealId);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String mealTitle = dataSnapshot.getKey();
                if (mDatabaseListItems.containsKey(mealTitle)) {

                    Meal deletedMeal = mDatabaseListItems.get(mealTitle);

                    removeMealAssociations(deletedMeal, mealTitle);

                    // Remove the meal from the list and update the adapter
                    mDatabaseListItems.remove(mealTitle);

                    updateTextViews(mealTitle, null);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Setup the reference for all meals
        mMealsRef = mDatabase.getReference()
                .child(Constants.NODE_MEALS)
                .child(mUId);
    }

    /** Finds a Meal given a meal_id and adds it to the adapter list */
    private void addMealToList(final String meal_title, final String meal_id) {
        mMealsRef.child(meal_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Meal meal = dataSnapshot.getValue(Meal.class);
                if (meal != null) {
                    meal.setMealId(meal_id);
                }

                // Add the meal to the list and update the adapter
                mDatabaseListItems.put(meal_title, meal);
                updateTextViews(meal_title, meal);
                updateWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateTextViews(String meal_title, Meal meal) {
        String mealName;
        if (meal != null) {
            mealName = meal.getName();
        } else {
            mealName =mContext.getResources().getString(R.string.day_add_meal);
        }
        switch (meal_title) {
            case Constants.NODE_BREAKFAST:
                mBreakfastTextView.setText(mealName);
                break;
            case Constants.NODE_MORNING_SNACK:
                mMorningSnackTextView.setText(mealName);
                break;
            case Constants.NODE_LUNCH:
                mLunchTextView.setText(mealName);
                break;
            case Constants.NODE_AFTERNOON_SNACK:
                mAfternoonSnackTextView.setText(mealName);
                break;
            case Constants.NODE_DINNER:
                mDinnerTextView.setText(mealName);
                break;
        }
    }

    /** Updates the widgets with the new day */
    private void updateWidgets() {

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        final int[] dayWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext,
                DayAppWidget.class));
        final int[] weekWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext,
                WeekAppWidget.class));

        mDayMealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Day> days = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Day day = child.getValue(Day.class);
                    days.add(day);
                }
                // Sort the list
                Collections.sort(days, Day.AscendingComparator);

                // Pass the complete list to the widget
                DayAppWidget.mDays = days;
                WeekAppWidget.mWeek = days;

                // Instruct the widget manager to update the widget
                appWidgetManager.notifyAppWidgetViewDataChanged(dayWidgetIds, R.id.flipper_app_widget_day);
                appWidgetManager.notifyAppWidgetViewDataChanged(weekWidgetIds, R.id.app_widget_week);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        appWidgetManager.notifyAppWidgetViewDataChanged(dayWidgetIds, R.id.flipper_app_widget_day);
        appWidgetManager.notifyAppWidgetViewDataChanged(weekWidgetIds, R.id.app_widget_week);
    }

    /** Removes all associations of the meal with the day */
    private void removeMealAssociations(Meal meal, String mealTitle) {
        // Remove the association of the meal with the day
        if (meal != null) {
            DatabaseReference mealDaysRef = mDatabase.getReference()
                    .child(Constants.NODE_MEAL_DAYS)
                    .child(mUId)
                    .child(meal.getMealId());

            String finalKey = mealTitle + "," + mCurrentDayId;
            mealDaysRef
                    .child(finalKey)
                    .removeValue();
        }
    }

    /** Adds the day's ingredients to the shopping list */
    private void addDayToShoppingList(final String dayId) {
        mDayMealsRef
                .child(dayId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Iterate through all the Meal Titles
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            // Check if the child is a meal
                            String key = child.getKey();
                            boolean isMeal = false;
                            switch (key) {
                                case Constants.NODE_BREAKFAST:
                                    isMeal = true;
                                    break;
                                case Constants.NODE_MORNING_SNACK:
                                    isMeal = true;
                                    break;
                                case Constants.NODE_LUNCH:
                                    isMeal = true;
                                    break;
                                case Constants.NODE_AFTERNOON_SNACK:
                                    isMeal = true;
                                    break;
                                case Constants.NODE_DINNER:
                                    isMeal = true;
                                    break;
                                default:
                                    isMeal = false;
                                    break;
                            }

                            // The child is a meal; add its ingredients to the shopping list
                            if (isMeal) {
                                String mealTitleId = child.getKey() + "," + dayId;
                                String mealId = child.getValue(String.class);

                                // Get the meal
                                getMealServings(mealId, mealTitleId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /** Adds the week's ingredients to the shopping list */
    private void addWeekToShoppingList(){
        for (int i = 0; i < 7; i++) {
            addDayToShoppingList(Day.intToDay(mContext, i));
        }
    }

    /** Finds a meal' servings and searches for the meal's recipes */
    private void getMealServings(final String mealId, final String mealTitle) {
        mMealsRef
                .child(mealId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get the meal servings and find its recipes
                        int mealServings =  dataSnapshot.getValue(Meal.class).getServings();
                        getMealRecipes(mealId, mealServings, mealTitle);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /** Find's a meal's recipes and searches for the recipes' ingredients */
    private void getMealRecipes(final String mealId, final int mealServings, final String mealTitle) {
        DatabaseReference mealRecipesRef = mDatabase.getReference()
                .child(Constants.NODE_MEAL_RECIPES)
                .child(mUId)
                .child(mealId);

        mealRecipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterate through all recipes and get their recipe_id
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String recipeId = child.getKey();
                    getRecipeServings(recipeId, mealServings, mealTitle);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Finds a recipe and searches for its ingredients */
    private void getRecipeServings(final String recipeId, final int mealServings,
                                   final String mealTitle) {
        DatabaseReference recipesRef = mDatabase.getReference()
                .child(Constants.NODE_RECIPES)
                .child(mUId)
                .child(recipeId);

        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the meal servings and find its recipes
                int recipeServings =  dataSnapshot.getValue(Recipe.class).getServings();
                // Get the multiplier to adjust the recipe servings to the meal servings
                double servingsMultiplier = mealServings / (double) recipeServings;

                addIngredientToShoppingList(recipeId, servingsMultiplier, mealTitle);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Adds an ingredient to the shopping list */
    private void addIngredientToShoppingList(String recipeId, final double servingsMultiplier, final String
            mealTitle) {
        DatabaseReference ingredientsRef = mDatabase.getReference()
                .child(Constants.NODE_INGREDIENTS)
                .child(mUId)
                .child(recipeId);

        ingredientsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Ingredient ingredient = child.getValue(Ingredient.class);

                    // Adjust the quantity based on the meal servings
                    double finalQuantity = ingredient.getQuantity() * servingsMultiplier;
                    ingredient.setQuantity(finalQuantity);
                    ingredient.setDayMealId(mealTitle);

                    DatabaseReference shoppingRef = mDatabase.getReference()
                            .child(Constants.NODE_SHOPPING_LIST)
                            .child(mUId);

                    shoppingRef.push()
                            .setValue(ingredient);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        String meal_title = null;
        String meal_title_name = null;
        // Check which view was clicked
        switch (view.getId()) {
            case R.id.tv_day_breakfast:
                meal_title = Constants.NODE_BREAKFAST;
                meal_title_name = Constants.NODE_BREAKFAST_NAME;
                break;
            case R.id.tv_day_breakfast_title:
                meal_title = Constants.NODE_BREAKFAST;
                meal_title_name = Constants.NODE_BREAKFAST_NAME;
                break;
            case R.id.tv_day_morning_snack:
                meal_title = Constants.NODE_MORNING_SNACK;
                meal_title_name = Constants.NODE_MORNING_SNACK_NAME;
                break;
                case R.id.tv_day_morning_snack_title:
                meal_title = Constants.NODE_MORNING_SNACK;
                meal_title_name = Constants.NODE_MORNING_SNACK_NAME;
                break;
            case R.id.tv_day_lunch:
                meal_title = Constants.NODE_LUNCH;
                meal_title_name = Constants.NODE_LUNCH_NAME;
                break;
            case R.id.tv_day_lunch_title:
                meal_title = Constants.NODE_LUNCH;
                meal_title_name = Constants.NODE_LUNCH_NAME;
                break;
            case R.id.tv_day_afternoon_snack:
                meal_title = Constants.NODE_AFTERNOON_SNACK;
                meal_title_name = Constants.NODE_AFTERNOON_SNACK_NAME;
                break;
            case R.id.tv_day_afternoon_snack_title:
                meal_title = Constants.NODE_AFTERNOON_SNACK;
                meal_title_name = Constants.NODE_AFTERNOON_SNACK_NAME;
                break;
            case R.id.tv_day_dinner:
                meal_title = Constants.NODE_DINNER;
                meal_title_name = Constants.NODE_DINNER_NAME;
                break;
                case R.id.tv_day_dinner_title:
                meal_title = Constants.NODE_DINNER;
                meal_title_name = Constants.NODE_DINNER_NAME;
                break;
        }

        if (mDatabaseListItems.containsKey(meal_title)) {
            // A meal was clicked, display it
            mCallback.onDayMealClick(mDatabaseListItems.get(meal_title), mCurrentDayId,
                    meal_title, meal_title_name);
        } else {
            // An empty view was clicked, select a view
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            SelectMealDialogFragment fragment = SelectMealDialogFragment.newInstance
                    (mUId, mCurrentDay, mCurrentDayId, meal_title, meal_title_name, true);
            fragment.show(fragmentManager, null);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        String meal_title = null;
        String meal_title_name = null;
        // Check which view was clicked
        switch (view.getId()) {
            case R.id.tv_day_breakfast :
                meal_title = Constants.NODE_BREAKFAST;
                meal_title_name = Constants.NODE_BREAKFAST_NAME;
                break;
            case R.id.tv_day_breakfast_title :
                meal_title = Constants.NODE_BREAKFAST;
                meal_title_name = Constants.NODE_BREAKFAST_NAME;
                break;
            case R.id.tv_day_morning_snack:
                meal_title = Constants.NODE_MORNING_SNACK;
                meal_title_name = Constants.NODE_MORNING_SNACK_NAME;
                break;
            case R.id.tv_day_morning_snack_title:
                meal_title = Constants.NODE_MORNING_SNACK;
                meal_title_name = Constants.NODE_MORNING_SNACK_NAME;
                break;
            case R.id.tv_day_lunch:
                meal_title = Constants.NODE_LUNCH;
                meal_title_name = Constants.NODE_LUNCH_NAME;
                break;
            case R.id.tv_day_lunch_title:
                meal_title = Constants.NODE_LUNCH;
                meal_title_name = Constants.NODE_LUNCH_NAME;
                break;
            case R.id.tv_day_afternoon_snack:
                meal_title = Constants.NODE_AFTERNOON_SNACK;
                meal_title_name = Constants.NODE_AFTERNOON_SNACK_NAME;
                break;
            case R.id.tv_day_afternoon_snack_title:
                meal_title = Constants.NODE_AFTERNOON_SNACK;
                meal_title_name = Constants.NODE_AFTERNOON_SNACK_NAME;
                break;
            case R.id.tv_day_dinner:
                meal_title = Constants.NODE_DINNER;
                meal_title_name = Constants.NODE_DINNER_NAME;
                break;
            case R.id.tv_day_dinner_title:
                meal_title = Constants.NODE_DINNER;
                meal_title_name = Constants.NODE_DINNER_NAME;
                break;
        }

        if (mDatabaseListItems.containsKey(meal_title)) {
            // A meal was clicked, replace it
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            SelectMealDialogFragment fragment = SelectMealDialogFragment.newInstance
                    (mUId, mCurrentDay, mCurrentDayId, meal_title, meal_title_name, false);
            fragment.show(fragmentManager, null);
        }
        return true;
    }

    /** Setup the menu */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_day, menu);
    }

    /** Setup menu actions */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_day_add_shopping:
                addDayToShoppingList(mCurrentDayId);
                return true;
            case R.id.action_week_add_shopping:
                addWeekToShoppingList();
                return true;
                case R.id.action_day_clear:
                mDayMealsRef
                        .child(mCurrentDayId)
                        .removeValue();
                return true;
            case R.id.action_week_clear:
                mDayMealsRef
                        .removeValue();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}