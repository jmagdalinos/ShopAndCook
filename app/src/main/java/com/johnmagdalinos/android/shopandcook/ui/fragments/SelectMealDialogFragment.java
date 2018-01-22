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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.model.Meal;
import com.johnmagdalinos.android.shopandcook.ui.DividerItemDecoration;
import com.johnmagdalinos.android.shopandcook.ui.adapters.MealListAdapter;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Displays a dialog allowing the user to select a meal for a day
 */

public class SelectMealDialogFragment extends android.support.v4.app.DialogFragment implements
        MealListAdapter.MealListAdapterCallback,
        View.OnClickListener {
    
    /** Member variables */
    private String mUId, mCurrentDayId, mCurrentMealTitle, mCurrentMealTitleName;
    private int mCurrentDay;
    private boolean mIsNewMeal;
    private DatabaseReference mMealsRef, mDayMealsRef, mMealDaysRef;
    private HashMap<String, Meal> mDatabaseListItems;
    private ArrayList<Meal> mTempListItems;
    private RecyclerView mRecyclerView;
    private MealListAdapter mAdapter;

    /** Key for the Fragment args */
    private static final String KEY_DAY = "day";
    private static final String KEY_DAY_ID = "day_id";
    private static final String KEY_MEAL_TITLE = "meal_title";
    private static final String KEY_MEAL_TITLE_NAME = "meal_title_name";
    private static final String KEY_IS_NEW_MEAL = "is_new_meal";

    /** Class constructor */
    public static SelectMealDialogFragment newInstance(String userId, int day, String day_id,
                                                       String meal_title, String meal_title_name,
                                                       boolean
                                                               isNewMeal) {
        SelectMealDialogFragment fragment = new SelectMealDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_DAY, day);
        args.putString(KEY_DAY_ID, day_id);
        args.putString(KEY_MEAL_TITLE, meal_title);
        args.putString(KEY_MEAL_TITLE_NAME, meal_title_name);
        args.putString(Constants.KEY_USER_ID, userId);
        args.putBoolean(KEY_IS_NEW_MEAL, isNewMeal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the arguments
        if (getArguments() != null) {
            mCurrentDay = getArguments().getInt(KEY_DAY);
            mCurrentDayId = getArguments().getString(KEY_DAY_ID);
            mCurrentMealTitle = getArguments().getString(KEY_MEAL_TITLE);
            mCurrentMealTitleName = getArguments().getString(KEY_MEAL_TITLE_NAME);
            mUId = getArguments().getString(Constants.KEY_USER_ID);
            mIsNewMeal = getArguments().getBoolean(KEY_IS_NEW_MEAL);
        }

        // Get an instance of the Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mMealsRef = database.getReference()
                .child(Constants.NODE_MEALS)
                .child(mUId);

        mDayMealsRef = database.getReference()
                .child(Constants.NODE_DAYS)
                .child(mUId)
                .child(mCurrentDayId);

        mMealDaysRef = database.getReference()
                .child(Constants.NODE_MEAL_DAYS)
                .child(mUId);

        // Inflate the dialog view
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout
                .fragment_dialog_select, null);

        // Setup a dialog to select a meal
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);

        // Set the title
        TextView titleTextView = dialogView.findViewById(R.id.tv_dialog_title);
        titleTextView.setText(R.string.dialog_title_select_meal);

        // Setup the RecyclerView
        mRecyclerView = dialogView.findViewById(R.id.rv_dialog_select_recipe);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        // Set the divider on the recycler view
        Drawable dividerDrawable = getActivity().getResources().getDrawable(R.drawable
                .recycler_view_divider);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration
                (dividerDrawable);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        // Setup the "Add new Meal" button
        LinearLayout addMealLayout = dialogView.findViewById(R.id.ll_dialog_add);
        TextView addTextView = dialogView.findViewById(R.id.tv_dialog_add);
        addTextView.setText(R.string.meal_button_add_new_meal);
        addMealLayout.setOnClickListener(this);

        // Setup the "Remove current meal" button
        LinearLayout removeMealLayout = dialogView.findViewById(R.id
                .ll_dialog_remove);
        TextView removeTextView = dialogView.findViewById(R.id.tv_dialog_remove);
        removeTextView.setText(R.string.meal_button_remove_meal);
        removeMealLayout.setOnClickListener(this);
        if (!mIsNewMeal) removeMealLayout.setVisibility(View.VISIBLE);

        // Setup the cancel button
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked on "Cancel"
                if (dialogInterface != null) dialogInterface.dismiss();
            }
        });

        // Setup the search view
        SearchView searchView = dialogView.findViewById(R.id.sv_dialog_search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Meal> queryResults = searchForMeal(query);
                mAdapter.swapList(queryResults);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        // Set a listener to return to the default list on exit
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mAdapter.swapList(mTempListItems);

                return true;
            }
        });

        setupDatabaseRef();

        return builder.create();
    }

    /** Setup the database references and their listeners to get the data */
    private void setupDatabaseRef() {
        mDatabaseListItems = new HashMap<>();
        mTempListItems = new ArrayList<>();

        if (mTempListItems != null) {
            // Create an instance of the adapter and set it on the Recycler View
            mAdapter = new MealListAdapter(getActivity(), mTempListItems, this);
            mRecyclerView.setAdapter(mAdapter);
        }

        // Add a listener to update the Recycler View
        mMealsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Meal meal = dataSnapshot.getValue(Meal.class);
                String key = dataSnapshot.getKey();

                // Add the meal from the list and update the adapter
                mDatabaseListItems.put(key, meal);
                mTempListItems = hashMapToArray(mDatabaseListItems);

                // Sort the list
                Collections.sort(mTempListItems, Meal.AscendingNameComparator);
                mAdapter.swapList(mTempListItems);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Meal meal = dataSnapshot.getValue(Meal.class);
                String key = dataSnapshot.getKey();

                // Add the meal from the list and update the adapter
                mDatabaseListItems.put(key, meal);
                mTempListItems = hashMapToArray(mDatabaseListItems);

                // Sort the list
                Collections.sort(mTempListItems, Meal.AscendingNameComparator);
                mAdapter.swapList(mTempListItems);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                if (mDatabaseListItems.containsKey(key)) {

                    // Remove the ingredient from the list and update the adapter
                    mDatabaseListItems.remove(key);

                    // Update the adapter
                    mTempListItems = hashMapToArray(mDatabaseListItems);
                    mAdapter.swapList(mTempListItems);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Converts a HashMap to an ArrayList */
    private static ArrayList<Meal> hashMapToArray(HashMap<String, Meal> mealHashMap) {
        ArrayList<Meal> meals = new ArrayList<>();
        // Convert the HashMap to an ArrayList
        for (String key : mealHashMap.keySet()) {
            Meal meal = mealHashMap.get(key);
            meal.setMealId(key);
            meals.add(meal);
        }
        return meals;
    }

    /** Searches within the Name and Tags for a string and returns an ArrayList of meals */
    private ArrayList<Meal> searchForMeal(String query) {
        String queryUpper = query.toUpperCase().trim();
        ArrayList<Meal> queryResults = new ArrayList<>();
        for (Meal meal: mTempListItems) {
            String searchable;
            if (meal.getTags() != null && !TextUtils.isEmpty(meal.getTags())) {
                // Append the tags to the name
                searchable = meal.getName().toUpperCase() + meal.getTags().toUpperCase();
            } else {
                searchable = meal.getName().toUpperCase();
            }

            if (searchable.contains(queryUpper)) {
                queryResults.add(meal);
            }
        }
        return queryResults;
    }

    /** Remove a meal association from the database */
    private void removeMeal() {
        // Remove the meal's association with the day
        mDayMealsRef
                .child(mCurrentMealTitle)
                .removeValue();

        mDayMealsRef
                .child(mCurrentMealTitleName)
                .removeValue();
    }

    /** Called when a meal has been clicked */
    @Override
    public void onClicked(Meal meal) {
        mDayMealsRef
                .child(Constants.NODE_DAY_OF_THE_WEEK)
                .setValue(mCurrentDay);

        // Save the the meal_id in the database under the current day_id
        mDayMealsRef
                .child(mCurrentMealTitle)
                .setValue(meal.getMealId());

        // Save the meal name in the database under the current day_id
        mDayMealsRef
                .child(mCurrentMealTitleName)
                .setValue(meal.getName());
        dismiss();

        // Save the day_id  and meal_title in the database under the current meal_id
        String key = mCurrentMealTitle + "," + mCurrentDayId;
        mMealDaysRef
                .child(meal.getMealId())
                .child(key)
                .setValue(key);
    }

    /** Called when the "Add new Meal" button has been clicked */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_dialog_add:
                // Close the dialog box
                dismiss();

                // Open the RecipeFragment to create a new Recipe for this meal
                MealFragment fragment = MealFragment.newInstance(mUId, true, null, mCurrentDayId,
                        mCurrentMealTitle, mCurrentMealTitleName);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.cl_fragment_detail, fragment)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.ll_dialog_remove:
                // Remove the current meal from the day
                removeMeal();

                // Close the dialog box
                dismiss();
        }
    }
}