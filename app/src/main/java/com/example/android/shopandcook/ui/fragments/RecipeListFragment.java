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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.data.RecipeContract.Recipes;
import com.example.android.shopandcook.model.Recipe;
import com.example.android.shopandcook.ui.DetailActivity;
import com.example.android.shopandcook.ui.DividerItemDecoration;
import com.example.android.shopandcook.ui.RecipeItemTouchHelper;
import com.example.android.shopandcook.ui.adapters.RecipeListAdapter;
import com.example.android.shopandcook.ui.adapters.RecipeListFromApiAdapter;
import com.example.android.shopandcook.utilities.Constants;
import com.example.android.shopandcook.utilities.RecipeIntentService;
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
 * Fragment displaying a list of recipes either from the database or the food2fork api
 */

public class RecipeListFragment extends android.support.v4.app.Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>,
        RecipeItemTouchHelper.RecyclerItemTouchHelperListener,
        RecipeListAdapter.RecipeListAdapterCallback,
        RecipeListFromApiAdapter.RecyclerViewCallback {

    /** Member Variables */
    private boolean mUseApi;
    private String mUId;
    private RecipeListFromApiAdapter mApiAdapter;
    private RecipeListAdapter mAdapter;
    private ConstraintLayout mConstraintLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoDataLinearLayout;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private LinearLayoutManager mLinearLayoutManager;
    private OnRecipeFragmentCallback mCallback;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRecipesRef, mIngredientsRef;
    private HashMap<String, Recipe> mDatabaseListItems;
    private ArrayList<Recipe> mTempListItems;
    private int mSortedBy = KEY_SORT_BY_NAME;
    private boolean mIsDeleted;
    private boolean mSortedByNameAsc = true;
    private boolean mSortedByCategoryAsc = false;
    private int mScrollPosition;

    /** Id of the cursor loader */
    private static final int LOADER_ID = 52;

    /** Keys for determining whether the list is sorted */
    private static final int KEY_SORT_BY_NAME = 0;
    private static final int KEY_SORT_BY_TAGS = 1;

    /** Key for the Fragment args */
    private static final String KEY_USE_API = "use_api";

    /**
     * Class constructor
     * @param useAPI:
     *              true if the fragment will search for recipes from food2fork API
     *              false if the fragment will search and display recipes from the database
     */
    public static RecipeListFragment newInstance(String userId, Boolean useAPI) {
        RecipeListFragment fragment = new RecipeListFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_USE_API, useAPI);
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    /** Interface used to implement on click functionality in an activity */
    public interface OnRecipeFragmentCallback {
        void onRecipeClick(boolean isNew, boolean isFromApi, @Nullable Recipe recipe, @Nullable
                String
                recipe_id);
    }

    /** Override onAttach to make sure that the container activity has implemented the callback */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnRecipeFragmentCallback) context;
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

        // Get the Arguments to see whether this fragment will use the API or the database
        mUseApi = getArguments().getBoolean(KEY_USE_API);
        mUId = getArguments().getString(Constants.KEY_USER_ID);

        // Get the Constraint Layout to be used in the SnackBar
        mConstraintLayout = getActivity().findViewById(R.id.cl_fragment_container);
        mTextView = viewRoot.findViewById(R.id.tv_recipe_list);
        mProgressBar = viewRoot.findViewById(R.id.pb_recipe_list);

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
            if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems
                    .size()) {
                mLinearLayoutManager.scrollToPosition(mScrollPosition);
            }
        }

        if (mUseApi) {
            // Show recipes from the foo2fork api
            DetailActivity.mCurrentFragment = Constants.EXTRAS_RECIPE_IDEAS;
            setupForApi();
        } else {
            // Set the divider on the recycler view
            Drawable dividerDrawable = getActivity().getResources().getDrawable(R.drawable
                    .recycler_view_divider);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration
                    (dividerDrawable);
            mRecyclerView.addItemDecoration(dividerItemDecoration);

            // Show recipes from the database
            DetailActivity.mCurrentFragment = Constants.EXTRAS_RECIPES;
            setupForFirebase();
        }
        return viewRoot;
    }

    /** Set the current fragment in the detail activity to null */
    @Override
    public void onDestroyView() {
        // Set the current fragment name in the detail activity to null so it does not save a
        // null fragment
        DetailActivity.mCurrentFragment = null;

        // Hide the food2fork banner
        TextView apiAttribTextView = getActivity().findViewById(R.id.tv_api_attribution);
        apiAttribTextView.setVisibility(View.GONE);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.KEY_USER_ID, mUId);
        mScrollPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        outState.putInt(Constants.KEY_SCROLL_POSITION, mScrollPosition);
    }

    /** Sets up the fragment for use with the food2fork API */
    private void setupForApi() {
        // Set the title
        getActivity().setTitle(getActivity().getString(R.string.main_recipe_ideas));

        // Display the food2fork attribution message
        TextView apiAttribTextView = getActivity().findViewById(R.id.tv_api_attribution);
        apiAttribTextView.setVisibility(View.VISIBLE);

        // Hide the "No Data" message
        toggleNoDataMessage(false);

        // Create an instance of the adapter and set it on the Recycler View
        mApiAdapter = new RecipeListFromApiAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mApiAdapter);

        // Set the enter animation
        AnimationSet set = new AnimationSet(true);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim
                .recycler_view_slide);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        mRecyclerView.setLayoutAnimation(controller);

        // Initiate the loader
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /** Sets up the fragment for use with the Firebase Realtime Database */
    private void setupForFirebase() {
        // Set the title
        getActivity().setTitle(getActivity().getString(R.string.main_recipes));

        // Retrieve an instance of the database
        mDatabase = FirebaseDatabase.getInstance();
        mRecipesRef = mDatabase.getReference()
                .child(Constants.NODE_RECIPES)
                .child(mUId);

        mIngredientsRef = mDatabase.getReference()
                .child(Constants.NODE_INGREDIENTS)
                .child(mUId);

        mDatabaseListItems = new HashMap<>();
        mTempListItems = new ArrayList<>();

        // Setup swipe actions on the Recycler View
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecipeItemTouchHelper(0,
                ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        if (mTempListItems != null) {
            // Create an instance of the adapter and set it on the Recycler View
            mAdapter = new RecipeListAdapter(getActivity(), mTempListItems, this, false);
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
        mRecipesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Recipe recipe = dataSnapshot.getValue(Recipe.class);
                String key = dataSnapshot.getKey();

                // Get the current position
                mScrollPosition= mLinearLayoutManager.findFirstVisibleItemPosition();

                // Add the recipe from the list and update the adapter
                mDatabaseListItems.put(key, recipe);
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
                    case KEY_SORT_BY_TAGS:
                        // Reverse the sort boolean to perform a correct sorting
                        mSortedByCategoryAsc = !mSortedByCategoryAsc;
                        sortByTags();
                        // Return to the previous position
                        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                            mLinearLayoutManager.scrollToPosition(mScrollPosition);
                        }
                        break;
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Recipe recipe = dataSnapshot.getValue(Recipe.class);
                String key = dataSnapshot.getKey();

                // Get the current position
                mScrollPosition = mLinearLayoutManager.findFirstVisibleItemPosition();

                // Add the recipe from the list and update the adapter
                mDatabaseListItems.put(key, recipe);
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
                    case KEY_SORT_BY_TAGS:
                        // Reverse the sort boolean to perform a correct sorting
                        mSortedByCategoryAsc = !mSortedByCategoryAsc;
                        sortByTags();
                        // Return to the previous pPosition
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
                        case KEY_SORT_BY_TAGS:
                            // Reverse the sort boolean to perform a correct sorting
                            mSortedByCategoryAsc = !mSortedByCategoryAsc;
                            sortByTags();
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

    /** Converts the HashMap of recipes to an ArrayList of recipes for the adapter */
    private ArrayList<Recipe> hashMapToArray(HashMap<String, Recipe> recipeHashMap) {
        ArrayList<Recipe> recipes= new ArrayList<>();
        // Convert the HashMap to an ArrayList
        for (String key : recipeHashMap.keySet()) {
            Recipe recipe = recipeHashMap.get(key);
            recipe.setRecipeId(key);
            recipes.add(recipe);
        }

        if (recipes.size() > 0) {
            toggleNoDataMessage(false);
        } else {
            toggleNoDataMessage(true);
        }
        return recipes;
    }

    /** Removes all recipes */
    private void deleteAllRecipes() {
        // Display a dialog to ensure that the user wants to clear the list
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_clear_recipes_title);
        builder.setMessage(R.string.dialog_clear_recipes);

        builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mRecipesRef.removeValue();
                mIngredientsRef.removeValue();
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
            Collections.sort(mTempListItems, Recipe.DescendingNameComparator);
            mSortedByNameAsc = false;
        } else {
            // Sort ascending
            Collections.sort(mTempListItems, Recipe.AscendingNameComparator);
            mSortedByNameAsc = true;
        }
        mAdapter.swapList(mTempListItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    /** Sorts the list by name */
    private void sortByTags() {
        mSortedBy = KEY_SORT_BY_TAGS;
        // Reset the sort by color toggle
        mAdapter.swapList(null);
        if (mSortedByCategoryAsc) {
            // Sort descending
            Collections.sort(mTempListItems, Recipe.DescendingTagComparator);
            mSortedByCategoryAsc = false;
        } else {
            // Sort ascending
            Collections.sort(mTempListItems, Recipe.AscendingTagComparator);
            mSortedByCategoryAsc = true;
        }
        mAdapter.swapList(mTempListItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    // Setup the loader for the food2fork recipes
    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        mProgressBar.setVisibility(View.VISIBLE);
        Uri uri = Recipes.CONTENT_URI;
        String[] projection = new String[] {
                Recipes.COLUMN_RECIPE_ID,
                Recipes.COLUMN_TITLE,
                Recipes.COLUMN_IMAGE_URL,
                Recipes.COLUMN_RANK};

        switch (loaderId) {
            case LOADER_ID:
                return new android.support.v4.content.CursorLoader(
                        getActivity(),
                        uri,
                        projection,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader: " + loaderId + " not implemented");
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        mProgressBar.setVisibility(View.GONE);
        if (cursor != null && cursor.getCount() != 0) {
            mApiAdapter.swapCursor(cursor);
        } else {
            mApiAdapter.swapCursor(null);
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(R.string.no_api_data);
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mApiAdapter.swapCursor(null);
    }

    /** Searches within the Name and Tags for a string and returns an ArrayList */
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

    /** Remove a recipe from the database */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecipeListAdapter.ViewHolder) {
            // Get the deleted position, recipe and recipe id
            final int deletedIndex = viewHolder.getAdapterPosition();
            final Recipe deletedRecipe = mAdapter.mRecipes.get(deletedIndex);
            final String deletedRecipeId = deletedRecipe.getRecipeId();

            // Remove the recipe from the adapter
            mTempListItems.remove(deletedIndex);
            mAdapter.swapList(mTempListItems);
            mIsDeleted = true;

            // Show SnackBar with undo option
            Snackbar snackbar = Snackbar.make(mConstraintLayout, getString(R.string.snackbar_delete_recipe),
                    Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color
                    .snackBarTextColor));
            snackbar.setAction(getString(R.string.snackbar_undo), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIsDeleted = false;
                    // Restore the recipe
                    mTempListItems.add(deletedIndex, deletedRecipe);
                    mAdapter.swapList(mTempListItems);
                }
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    // If the user did not press "Undo", delete the recipe
                    if (mDatabaseListItems.containsKey(deletedRecipeId) && mIsDeleted) {
                        // Remove the recipe from the recipes list
                        mRecipesRef.child(deletedRecipeId)
                                .removeValue();

                        // Remove all the recipe's ingredients
                        mIngredientsRef.child(deletedRecipeId)
                                .removeValue();

                        final DatabaseReference recipeMealsRef = mDatabase.getReference()
                                .child(Constants.NODE_RECIPE_MEALS)
                                .child(mUId);

                        final DatabaseReference mealRecipeRef = mDatabase.getReference()
                                .child(Constants.NODE_MEAL_RECIPES)
                                .child(mUId);

                        // Remove the associations of the meal within each recipe
                        recipeMealsRef
                                .child(deletedRecipeId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // Get all the meals associated with the recipe_id
                                        for (DataSnapshot shot : dataSnapshot.getChildren()) {
                                            String meal_id = shot.getKey();

                                            // Remove the association with the meal
                                            mealRecipeRef
                                                    .child(meal_id)
                                                    .child(deletedRecipeId)
                                                    .removeValue();
                                        }

                                        recipeMealsRef.child(deletedRecipeId).removeValue();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                }
            });
            snackbar.show();
        }
    }

    /** Called when a recipe from the database has been clicked */
    @Override
    public void onClicked(Recipe recipe) {
        mCallback.onRecipeClick(false, false, recipe, null);
    }

    /** Called when a recipe from the food2fork api has been clicked */
    @Override
    public void onItemClick(String recipe_id) {
        mCallback.onRecipeClick(false, true, null, recipe_id);
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
        inflater.inflate(R.menu.menu_recipe_list, menu);

        // Get the Search View
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Disable all but the SearchView if searching in food2fork
        if (mUseApi) {
            menu.findItem(R.id.action_recipe_list_add).setVisible(false);
            menu.findItem(R.id.action_recipe_list_clear).setVisible(false);
            menu.findItem(R.id.action_recipe_list_sort_by_name).setVisible(false);
            menu.findItem(R.id.action_recipe_list_sort_by_tags).setVisible(false);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (mUseApi) {
                    // Clear the Recycler View
                    mApiAdapter.swapCursor(null);

                    // Check for internet connection
                    ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null &&
                            activeNetwork.isConnectedOrConnecting();

                    if (isConnected) {
                        mTextView.setVisibility(View.GONE);

                        Intent intent = new Intent(getActivity(), RecipeIntentService.class);
                        intent.putExtra(RecipeIntentService.KEY_EXTRA, RecipeIntentService.KEY_SEARCH);
                        intent.putExtra(RecipeIntentService.KEY_SEARCH_QUERY, query);
                        getActivity().startService(intent);

                        // Set the adapter on the Recycler View
                        mRecyclerView.setAdapter(mApiAdapter);
                    } else {
                        mTextView.setVisibility(View.VISIBLE);
                        mTextView.setText(R.string.no_internet);
                    }
                } else {
                    ArrayList<Recipe> queryResults = searchForRecipe(query);
                    mAdapter.swapList(queryResults);
                }
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
                if (!mUseApi) {
                    mAdapter.swapList(mTempListItems);
                }
                return true;
            }
        });
    }

    /** Setup menu actions */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_recipe_list_add:
                // Add a new recipe
                mCallback.onRecipeClick(true, false, null, null);
                return true;
            case R.id.action_recipe_list_clear:
                deleteAllRecipes();
                return true;
            case R.id.action_recipe_list_sort_by_name:
                sortByName();
                return true;
            case R.id.action_recipe_list_sort_by_tags:
                sortByTags();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
