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
import com.johnmagdalinos.android.shopandcook.model.Recipe;
import com.johnmagdalinos.android.shopandcook.ui.DividerItemDecoration;
import com.johnmagdalinos.android.shopandcook.ui.adapters.RecipeListAdapter;
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
 * Displays a dialog allowing the user to select a recipe for a meal
 */

public class SelectRecipeDialogFragment extends android.support.v4.app.DialogFragment implements
        RecipeListAdapter.RecipeListAdapterCallback,
        View.OnClickListener {
    
    /** Member variables */
    private String mUId;
    private String mCurrentMealId;
    private DatabaseReference mRecipesRef, mMealRecipesRef, mRecipeMealsRef;
    private HashMap<String, Recipe> mDatabaseListItems;
    private ArrayList<Recipe> mTempListItems;
    private RecyclerView mRecyclerView;
    private RecipeListAdapter mAdapter;

    /** Key for the Fragment args */
    private static final String KEY_MEAL_ID = "meal_id";

    /** Class constructor */
    public static SelectRecipeDialogFragment newInstance(String userId, String meal_id) {
        SelectRecipeDialogFragment fragment = new SelectRecipeDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_MEAL_ID, meal_id);
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the arguments
        if (getArguments() != null) {
            mCurrentMealId = getArguments().getString(KEY_MEAL_ID);
            mUId = getArguments().getString(Constants.KEY_USER_ID);
        }

        // Get an instance of the Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRecipesRef = database.getReference()
                .child(Constants.NODE_RECIPES)
                .child(mUId);

        mMealRecipesRef = database.getReference()
                .child(Constants.NODE_MEAL_RECIPES)
                .child(mUId)
                .child(mCurrentMealId);

        mRecipeMealsRef = database.getReference()
                .child(Constants.NODE_RECIPE_MEALS)
                .child(mUId);

        // Inflate the dialog view
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout
                .fragment_dialog_select, null);

        // Setup a dialog to select a recipe
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);

        // Set the title
        TextView titleTextView = dialogView.findViewById(R.id.tv_dialog_title);
        titleTextView.setText(R.string.dialog_title_select_recipes);

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

        // Setup the "Add new Recipe" button
        LinearLayout addMealLayout = dialogView.findViewById(R.id.ll_dialog_add);
        TextView addTextView = dialogView.findViewById(R.id.tv_dialog_add);
        addTextView.setText(R.string.meal_button_add_new_recipe);
        addMealLayout.setOnClickListener(this);

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
                ArrayList<Recipe> queryResults = searchForRecipe(query);
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
            mAdapter = new RecipeListAdapter(getActivity(), mTempListItems, this, false);
            mRecyclerView.setAdapter(mAdapter);
        }

        // Add a listener to update the Recycler View
        mRecipesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Recipe recipe = dataSnapshot.getValue(Recipe.class);
                String key = dataSnapshot.getKey();

                // Add the recipe from the list and update the adapter
                mDatabaseListItems.put(key, recipe);
                mTempListItems = hashMapToArray(mDatabaseListItems);

                // Sort the list
                Collections.sort(mTempListItems, Recipe.AscendingNameComparator);
                mAdapter.swapList(mTempListItems);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Recipe recipe = dataSnapshot.getValue(Recipe.class);
                String key = dataSnapshot.getKey();

                // Add the ingredient from the list and update the adapter
                mDatabaseListItems.put(key, recipe);
                mTempListItems = hashMapToArray(mDatabaseListItems);

                // Sort the list
                Collections.sort(mTempListItems, Recipe.AscendingNameComparator);
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

    /** Converts the HashMap of recipes to an ArrayList of recipes for the adapter */
    private static ArrayList<Recipe> hashMapToArray(HashMap<String, Recipe> recipeHashMap) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        // Convert the HashMap to an ArrayList
        for (String key : recipeHashMap.keySet()) {
            Recipe recipe = recipeHashMap.get(key);
            recipe.setRecipeId(key);
            recipes.add(recipe);
        }
        return recipes;
    }

    /** Searches within the Name and Tags for a string and returns an ArrayList of recipes */
    private ArrayList<Recipe> searchForRecipe(String query) {
        String queryUpper = query.toUpperCase().trim();
        ArrayList<Recipe> queryResults = new ArrayList<>();
        for (Recipe recipe : mTempListItems) {
            String searchable;
            if (recipe.getTags() != null && !TextUtils.isEmpty(recipe.getTags())) {
                // Append the tags to the name
                searchable = recipe.getName().toUpperCase() + recipe.getTags().toUpperCase();
            } else {
                searchable = recipe.getName().toUpperCase();
            }

            if (searchable.contains(queryUpper)) {
                queryResults.add(recipe);
            }
        }
        return queryResults;
    }

    /** Called when a recipe has been clicked */
    @Override
    public void onClicked(Recipe recipe) {
        // Save the the recipe_id in the database under the current meal_id
        mMealRecipesRef
                .child(recipe.getRecipeId())
                .setValue(recipe.getRecipeId());
        dismiss();

        // Save the meal_id in the database under the current recipe_id
        mRecipeMealsRef
                .child(recipe.getRecipeId())
                .child(mCurrentMealId)
                .setValue(mCurrentMealId);
    }

    /** Called when the "Add new Recipe has been clicked */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ll_dialog_add) {
            // Close the dialog box
            dismiss();

            // Open the RecipeFragment to create a new Recipe for this meal
            RecipeFragment fragment = RecipeFragment.newInstance(mUId, true, null, mCurrentMealId);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.cl_fragment_detail, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
