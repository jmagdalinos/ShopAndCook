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

package com.example.android.shopandcook.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.model.Meal;
import com.example.android.shopandcook.ui.DetailActivity;
import com.example.android.shopandcook.ui.DividerItemDecoration;
import com.example.android.shopandcook.ui.MealItemTouchHelper;
import com.example.android.shopandcook.ui.adapters.MealListAdapter;
import com.example.android.shopandcook.utilities.Constants;
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
 * Fragment displaying a list of meals
 */

public class MealListFragment extends android.support.v4.app.Fragment implements
        MealItemTouchHelper.RecyclerItemTouchHelperListener,
        MealListAdapter.MealListAdapterCallback {

    /** Member Variables */
    private String mUId;
    private MealListAdapter mAdapter;
    private ConstraintLayout mConstraintLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoDataLinearLayout;
    private LinearLayoutManager mLinearLayoutManager;
    private OnMealFragmentCallback mCallback;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMealsRef, mMealRecipesRef;
    private HashMap<String, Meal> mDatabaseListItems;
    private ArrayList<Meal> mTempListItems;
    private boolean mIsDeleted;
    private int mSortedBy = KEY_SORT_BY_NAME;
    private boolean mSortedByNameAsc = true;
    private boolean mSortedByCategoryAsc = false;
    private int mScrollPosition;

    /** Keys for determining whether the list is sorted */
    private static final int KEY_SORT_BY_NAME = 0;
    private static final int KEY_SORT_BY_CATEGORY = 1;

    /** Class constructor */
    public static MealListFragment newInstance(String userId) {
        MealListFragment fragment = new MealListFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    /** Interface used to implement on click functionality in an activity */
    public interface OnMealFragmentCallback {
        void onMealClick(boolean isNew, @Nullable Meal meal, @Nullable String day_id, @Nullable
                         String dayTitle, @Nullable String dayTitleName);
    }

    /** Override onAttach to make sure that the container activity has implemented the callback */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnMealFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnMealFragmentCallback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View viewRoot = inflater.inflate(R.layout.fragment_recipe_meal_list, container, false);
        // Enable the menu
        setHasOptionsMenu(true);

        // Set the title
        getActivity().setTitle(getActivity().getString(R.string.main_meals));
        DetailActivity.mCurrentFragment = Constants.EXTRAS_MEALS;

        mUId = getArguments().getString(Constants.KEY_USER_ID);

        // Get the Constraint Layout to be used in the SnackBar
        mConstraintLayout = getActivity().findViewById(R.id.cl_fragment_container);

        // Get the LinearLayout for the "No Data" message
        mNoDataLinearLayout = viewRoot.findViewById(R.id.ll_recipe_meal_list_no_data);

        // Setup the Recycler View
        mRecyclerView = viewRoot.findViewById(R.id.rv_recipe_list);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        if (savedInstanceState != null) {
            mUId = savedInstanceState.getString(Constants.KEY_USER_ID);
            mScrollPosition = savedInstanceState.getInt(Constants.KEY_SCROLL_POSITION);
            if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                mLinearLayoutManager.scrollToPosition(mScrollPosition);
            }
        }

        // Set the divider on the recycler view
        Drawable dividerDrawable = getActivity().getResources().getDrawable(R.drawable
                .recycler_view_divider);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration
                (dividerDrawable);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        setupUI();

        return viewRoot;
    }

    /** Set the current fragment in the detail activity to null */
    @Override
    public void onDestroyView() {
        // Set the current fragment name in the detail activity to null so it does not save a
        // null fragment
        DetailActivity.mCurrentFragment = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.KEY_USER_ID, mUId);
        mScrollPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        outState.putInt(Constants.KEY_SCROLL_POSITION, mScrollPosition);
    }

    /** Sets up the fragment's UI */
    private void setupUI() {
        // Retrieve an instance of the database
        mDatabase = FirebaseDatabase.getInstance();
        mMealsRef = mDatabase.getReference()
                .child(Constants.NODE_MEALS)
                .child(mUId);

        mMealRecipesRef = mDatabase.getReference()
                .child(Constants.NODE_MEAL_RECIPES)
                .child(mUId);

        mDatabaseListItems = new HashMap<>();
        mTempListItems = new ArrayList<>();

        // Setup swipe actions on the Recycler View
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new MealItemTouchHelper(0,
                ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);


        if (mTempListItems != null) {
            // Create an instance of the adapter and set it on the Recycler View

            mAdapter = new MealListAdapter(getActivity(), mTempListItems, this);
            mRecyclerView.setAdapter(mAdapter);

            // Set the enter animation
            AnimationSet set = new AnimationSet(true);
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim
                    .recycler_view_slide);
            set.addAnimation(animation);
            LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
            mRecyclerView.setLayoutAnimation(controller);
        }

        // Add a listener to update the Recycler View
        mMealsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Meal meal = dataSnapshot.getValue(Meal.class);
                String key = dataSnapshot.getKey();

                // Get the current position
                mScrollPosition = mLinearLayoutManager.findFirstVisibleItemPosition();

                // Add the recipe from the list and update the adapter
                mDatabaseListItems.put(key, meal);
                mTempListItems = hashMapToArray(mDatabaseListItems);
                mAdapter.swapList(mTempListItems);

                // Check if the list was sorted prior to the insertion
                switch (mSortedBy) {
                    case KEY_SORT_BY_NAME:
                        // Reverse the sort boolean to perform a correct sorting
                        mSortedByNameAsc = !mSortedByNameAsc;
                        sortByName();
                        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                            mLinearLayoutManager.scrollToPosition(mScrollPosition);
                        }
                        break;
                    case KEY_SORT_BY_CATEGORY:
                        // Reverse the sort boolean to perform a correct sorting
                        mSortedByCategoryAsc = !mSortedByCategoryAsc;
                        sortByCategory();
                        // Return to the previous position
                        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                            mLinearLayoutManager.scrollToPosition(mScrollPosition);
                        }
                        break;
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Meal meal = dataSnapshot.getValue(Meal.class);
                String key = dataSnapshot.getKey();

                // Get the current position
                mScrollPosition = mLinearLayoutManager.findFirstVisibleItemPosition();

                // Add the recipe from the list and update the adapter
                mDatabaseListItems.put(key, meal);
                mTempListItems = hashMapToArray(mDatabaseListItems);
                mAdapter.swapList(mTempListItems);

                // Check if the list was sorted prior to the insertion
                switch (mSortedBy) {
                    case KEY_SORT_BY_NAME:
                        // Reverse the sort boolean to perform a correct sorting
                        mSortedByNameAsc = !mSortedByNameAsc;
                        sortByName();
                        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                            mLinearLayoutManager.scrollToPosition(mScrollPosition);
                        }
                        break;
                    case KEY_SORT_BY_CATEGORY:
                        // Reverse the sort boolean to perform a correct sorting
                        mSortedByCategoryAsc = !mSortedByCategoryAsc;
                        sortByCategory();
                        // Return to the previous position
                        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                            mLinearLayoutManager.scrollToPosition(mScrollPosition);
                        }
                        break;
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                if (mDatabaseListItems.containsKey(key)) {

                    removeMealAssociations(mDatabaseListItems.get(key));

                    // Remove the recipe from the list and update the adapter
                    mDatabaseListItems.remove(key);

                    // Get the current position
                    mScrollPosition = mLinearLayoutManager.findFirstVisibleItemPosition();

                    // Update the adapter
                    mTempListItems = hashMapToArray(mDatabaseListItems);
                    mAdapter.swapList(mTempListItems);

                    switch (mSortedBy) {
                        case KEY_SORT_BY_NAME:
                            // Reverse the sort boolean to perform a correct sorting
                            mSortedByNameAsc = !mSortedByNameAsc;
                            sortByName();
                            if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                                mLinearLayoutManager.scrollToPosition(mScrollPosition);
                            }
                            break;
                        case KEY_SORT_BY_CATEGORY:
                            // Reverse the sort boolean to perform a correct sorting
                            mSortedByCategoryAsc = !mSortedByCategoryAsc;
                            sortByCategory();
                            // Return to the previous position
                            if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                                mLinearLayoutManager.scrollToPosition(mScrollPosition);
                            }
                            break;
                    }
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

    /** Converts the HashMap of meals to an ArrayList of meals for the adapter */
    private ArrayList<Meal> hashMapToArray(HashMap<String, Meal> mealHashMap) {
        ArrayList<Meal> meals= new ArrayList<>();
        // Convert the HashMap to an ArrayList
        for (String key : mealHashMap.keySet()) {
            Meal meal = mealHashMap.get(key);
            meal.setMealId(key);
            meals.add(meal);
        }

        if (meals.size() > 0) {
            toggleNoDataMessage(false);
        } else {
            toggleNoDataMessage(true);
        }
        return meals;
    }

    /** Removes all meals */
    private void deleteAllMeals() {
        // Display a dialog to ensure that the user wants to clear the list
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_clear_meals_title);
        builder.setMessage(R.string.dialog_clear_meals);

        builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mMealsRef.removeValue();
                mMealRecipesRef.removeValue();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** Sorts the list by name */
    private void sortByName() {
        mSortedBy = KEY_SORT_BY_NAME;
        // Reset the sort by color toggle
        mAdapter.swapList(null);
        if (mSortedByNameAsc) {
            // Sort descending
            Collections.sort(mTempListItems, Meal.DescendingNameComparator);
            mSortedByNameAsc = false;
        } else {
            // Sort ascending
            Collections.sort(mTempListItems, Meal.AscendingNameComparator);
            mSortedByNameAsc = true;
        }
        mAdapter.swapList(mTempListItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    /** Sorts the list by name */
    private void sortByCategory() {
        mSortedBy = KEY_SORT_BY_CATEGORY;
        // Reset the sort by color toggle
        mAdapter.swapList(null);
        if (mSortedByCategoryAsc) {
            // Sort descending
            Collections.sort(mTempListItems, Meal.DescendingTagComparator);
            mSortedByCategoryAsc = false;
        } else {
            // Sort ascending
            Collections.sort(mTempListItems, Meal.AscendingTagComparator);
            mSortedByCategoryAsc = true;
        }
        mAdapter.swapList(mTempListItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    /** Searches within the Name and Tags for a string and returns an ArrayList */
    private ArrayList<Meal> searchForMeal(String query) {
        String queryUpper = query.toUpperCase().trim();
        ArrayList<Meal> queryResults = new ArrayList<>();
        for (Meal meal: mTempListItems) {
            String searchable;
            if (meal.getTags() != null && !TextUtils.isEmpty(meal.getTags())) {
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

    /** Remove a meal from the database */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof MealListAdapter.ViewHolder) {
            // Get the deleted position, meal and meal id
            final int deletedIndex = viewHolder.getAdapterPosition();
            final Meal deletedMeal = mAdapter.mMeals.get(deletedIndex);
            final String deletedMealId = deletedMeal.getMealId();

            // Remove the meal from the adapter
            mTempListItems.remove(deletedIndex);
            mAdapter.swapList(mTempListItems);
            mIsDeleted = true;

            // Show SnackBar with undo option
            Snackbar snackbar = Snackbar.make(mConstraintLayout, getString(R.string
                            .snackbar_delete_meal),Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color
                    .snackBarTextColor));
            snackbar.setAction(getString(R.string.snackbar_undo), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIsDeleted = false;
                    // Restore the meal
                    mTempListItems.add(deletedIndex, deletedMeal);
                    mAdapter.swapList(mTempListItems);
                }
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (mIsDeleted) {
                        // If the user did not press "Undo", delete the meal
                        if (mDatabaseListItems.containsKey(deletedMealId) &&mIsDeleted) {
                            // Remove the meal from the meal list
                            mMealsRef.child(deletedMealId)
                                    .removeValue();
                        }
                    }
                }
            });
            snackbar.show();
        }
    }

    /** Removes all associations of the meal with recipes and days */
    private void removeMealAssociations(final Meal meal) {
        final String deletedMealId = meal.getMealId();

        // Remove the association with recipes
        final DatabaseReference recipeMealsRef = mDatabase.getReference()
                .child(Constants.NODE_RECIPE_MEALS)
                .child(mUId);

        mMealRecipesRef
                .child(deletedMealId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {

                    String recipe_id = dataSnapshotChild.getKey();
                    recipeMealsRef
                            .child(recipe_id)
                            .child(deletedMealId)
                            .removeValue();
                }
                mMealRecipesRef
                        .child(deletedMealId)
                        .removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Remove the association with days
        final DatabaseReference dayMealsRef = mDatabase.getReference()
                .child(Constants.NODE_DAYS)
                .child(mUId);

        final DatabaseReference mealDaysRef = mDatabase.getReference()
                .child(Constants.NODE_MEAL_DAYS)
                .child(mUId);

        mealDaysRef
                .child(deletedMealId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                    String title_day_id = dataSnapshotChild.getKey();

                    String[] splitKey = title_day_id.split(",");
                    dayMealsRef
                            .child(splitKey[1])
                            .child(splitKey[0])
                            .removeValue();

                }
                mealDaysRef
                        .child(deletedMealId)
                        .removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Called when a meal has been clicked */
    @Override
    public void onClicked(Meal meal) {
        mCallback.onMealClick(false,  meal, null, null, null);
    }

    /** Toggles the visibility of the "No data" message */
    private void toggleNoDataMessage(boolean showMessage) {
        if (showMessage) {
            mNoDataLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mNoDataLinearLayout.setVisibility(View.GONE);
        }
    }

    /** Setup the menu */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_meal_list, menu);

        // Get the Search View
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Meal> queryResults = searchForMeal(query);
                mAdapter.swapList(queryResults);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });

        // Set a listener to return to the default list on exit
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                mAdapter.swapList(mTempListItems);
                return true;
            }
        });
    }

    /** Setup menu actions */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_meal_list_add:
                // Add a new meal
                mCallback.onMealClick(true, null, null, null, null);
                return true;
            case R.id.action_meal_list_clear:
                deleteAllMeals();
                return true;
            case R.id.action_meal_list_sort_by_name:
                sortByName();
                return true;
            case R.id.action_meal_list_sort_by_tags:
                sortByCategory();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
