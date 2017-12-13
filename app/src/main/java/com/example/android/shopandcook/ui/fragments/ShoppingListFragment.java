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
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import android.widget.TextView;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.model.Ingredient;
import com.example.android.shopandcook.ui.DetailActivity;
import com.example.android.shopandcook.ui.IngredientItemTouchHelper;
import com.example.android.shopandcook.ui.adapters.ShoppingListAdapter;
import com.example.android.shopandcook.ui.appwidgets.ShoppingListAppWidget;
import com.example.android.shopandcook.utilities.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Fragment displaying a list of shopping list items
 */

public class ShoppingListFragment extends android.support.v4.app.Fragment implements
        IngredientItemTouchHelper.RecyclerItemTouchHelperListener,
        ShoppingListAdapter.ShoppingListAdapterCallback {

    /** Member Variables */
    private ConstraintLayout mConstraintLayout;
    private String mUId;
    private RecyclerView mShoppingListRecyclerView;
    private HashMap<String, Ingredient> mDatabaseListItems;
    private ArrayList<Ingredient> mTempListItems;
    private ShoppingListAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private DatabaseReference mShoppingListRef;
    private int mSortedBy = KEY_SORT_BY_NAME;
    private boolean mSortedByNameAsc = true;
    private boolean mSortedByColorAsc = false;
    private boolean mShowColors = true;
    private Context mContext;
    private boolean mIsDeleted;
    private int mScrollPosition;

    /** Keys for determining whether the list is sorted */
    private static final int KEY_SORT_BY_NAME = 0;
    private static final int KEY_SORT_BY_COLOR = 1;

    /** Class constructor */
    public static ShoppingListFragment newInstance(String userId) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View viewRoot = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        setHasOptionsMenu(true);

        // Get the data from the arguments
        mUId = getArguments().getString(Constants.KEY_USER_ID);

        DetailActivity.mCurrentFragment = Constants.EXTRAS_SHOPPING_LIST;
        mContext = getActivity();

        // Set the title
        getActivity().setTitle(getActivity().getString(R.string.main_shopping_list));

        // Initialize the HashMap and the ArrayList
        mDatabaseListItems = new HashMap<>();
        mTempListItems = new ArrayList<>();

        // Get the Constraint Layout to be used in the snackbar
        mConstraintLayout = getActivity().findViewById(R.id.cl_fragment_container);
        TextView nameTextView = viewRoot.findViewById(R.id.tv_shopping_list_name);
        TextView quantityTextView = viewRoot.findViewById(R.id
                .tv_shopping_list_quantity);

        // Get the custom font and set it on the titles
        Typeface mFont = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/courgette_regular.ttf");
        nameTextView.setTypeface(mFont);
        quantityTextView.setTypeface(mFont);

        // Setup the Recycler View
        mShoppingListRecyclerView = viewRoot.findViewById(R.id.rv_shopping_list);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mShoppingListRecyclerView.setLayoutManager(mLinearLayoutManager);
        mShoppingListRecyclerView.setHasFixedSize(true);

        if (mTempListItems != null) {
            mAdapter = new ShoppingListAdapter(getActivity(), mTempListItems, mShowColors, true,
                    this);
            mShoppingListRecyclerView.setAdapter(mAdapter);

            // Set the enter animation
            AnimationSet set = new AnimationSet(true);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim
                    .recycler_view_slide);
            set.addAnimation(animation);
            LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
            mShoppingListRecyclerView.setLayoutAnimation(controller);
        }

        if (savedInstanceState != null) {
            mUId = savedInstanceState.getString(Constants.KEY_USER_ID);
            mScrollPosition = savedInstanceState.getInt(Constants.KEY_SCROLL_POSITION);
            if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                mLinearLayoutManager.scrollToPosition(mScrollPosition);
            }
        }

        // Setup swipe actions on the Recycler View
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new IngredientItemTouchHelper(0,
                ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mShoppingListRecyclerView);

        // Get an instance of the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mShoppingListRef = database.getReference()
                .child(Constants.NODE_SHOPPING_LIST)
                .child(mUId);

        // Add a listener to update the Recycler View
        mShoppingListRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);
                String key = dataSnapshot.getKey();

                // Get the current position
                int pos = mLinearLayoutManager.findFirstVisibleItemPosition();

                // Add the ingredient from the list and update the adapter
                mDatabaseListItems.put(key, ingredient);
                mTempListItems = hashMapToArray(mDatabaseListItems);
                mAdapter.swapList(mTempListItems);

                // Update the widgets
                updateWidgets();

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
                    case KEY_SORT_BY_COLOR:
                        // Reverse the sort boolean to perform a correct sorting
                        mSortedByColorAsc = !mSortedByColorAsc;
                        sortByColor();
                        // Return to the previous position
                        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                            mLinearLayoutManager.scrollToPosition(mScrollPosition);
                        }
                        break;
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);
                String key = dataSnapshot.getKey();

                // Get the current position
                int pos = mLinearLayoutManager.findFirstVisibleItemPosition();

                // Add the ingredient from the list and update the adapter
                mDatabaseListItems.put(key, ingredient);
                mTempListItems = hashMapToArray(mDatabaseListItems);
                mAdapter.swapList(mTempListItems);

                // Update the widgets
                updateWidgets();

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
                    case KEY_SORT_BY_COLOR:
                        // Reverse the sort boolean to perform a correct sorting
                        mSortedByColorAsc = !mSortedByColorAsc;
                        sortByColor();
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

                    // Remove the ingredient from the list and update the adapter
                    mDatabaseListItems.remove(key);

                    // Get the current position
                    int pos = mLinearLayoutManager.findFirstVisibleItemPosition();

                    // Update the adapter
                    mTempListItems = hashMapToArray(mDatabaseListItems);
                    mAdapter.swapList(mTempListItems);

                    // Update the widgets
                    updateWidgets();

                    switch (mSortedBy) {
                        case KEY_SORT_BY_NAME:
                            // Reverse the sort boolean to perform a correct sorting
                            mSortedByNameAsc = !mSortedByNameAsc;
                            sortByName();
                            if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
                                mLinearLayoutManager.scrollToPosition(mScrollPosition);
                            }
                            break;
                        case KEY_SORT_BY_COLOR:
                            // Reverse the sort boolean to perform a correct sorting
                            mSortedByColorAsc = !mSortedByColorAsc;
                            sortByColor();
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

        return viewRoot;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.KEY_USER_ID, mUId);
        mScrollPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        outState.putInt(Constants.KEY_SCROLL_POSITION, mScrollPosition);
    }

    /** Set the current fragment in the detail activity to null */
    @Override
    public void onDestroyView() {
        // Set the current fragment name in the detail activity to null so it does not save a
        // null fragment
        DetailActivity.mCurrentFragment = null;
        super.onDestroyView();
    }

    /** Converts the HashMap of ingredients to an ArrayList of ingredients for the adapter */
    private ArrayList<Ingredient> hashMapToArray(HashMap<String, Ingredient> ingredientHashMap) {

        ArrayList<Ingredient> ingredients = new ArrayList<>();
        // Convert the HashMap to an ArrayList
        for (String key : ingredientHashMap.keySet()) {
            // Create a new Ingredient
            Ingredient currentIngredient = new Ingredient();

            // Get its properties from the HashMap
            currentIngredient.setName(ingredientHashMap.get(key).getName());
            currentIngredient.setComments(ingredientHashMap.get(key).getComments());
            currentIngredient.setMeasure(ingredientHashMap.get(key).getMeasure());
            currentIngredient.setQuantity(ingredientHashMap.get(key).getQuantity());
            currentIngredient.setColor(ingredientHashMap.get(key).getColor());
            currentIngredient.setIsChecked(ingredientHashMap.get(key).getIsChecked());
            currentIngredient.setIngredientId(ingredientHashMap.get(key).getIngredientId());
            currentIngredient.setDayMealId(ingredientHashMap.get(key).getDayMealId());

            // Add the ingredient_id to the ingredient
            currentIngredient.setIngredientId(key);

            if (ingredients.size() == 0) {
                // This is the first item, add it
                ingredients.add(currentIngredient);
            } else {
                // Check if the item already exists
                if (checkIfNewIngredient(ingredients, currentIngredient)) {
                    ingredients.add(currentIngredient);
                }
            }
        }
        return ingredients;
    }

    /** Checks if a shopping item exists. If so, it just updates its quantity */
    private boolean checkIfNewIngredient(ArrayList<Ingredient> ingredients, Ingredient currentIngredient) {
        boolean check = true;

        String name = currentIngredient.getName();
        int measure = currentIngredient.getMeasure();
        double quantity = currentIngredient.getQuantity();
        String ingredientId = currentIngredient.getIngredientId();

        // Iterate through the current list of items
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

    /** Removes all items from the shopping list */
    private void clearShoppingList() {
        // Display a dialog to ensure that the user wants to clear the list
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.dialog_clear_shopping_list_title);
        builder.setMessage(R.string.dialog_clear_shopping_list);

        builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mShoppingListRef.removeValue();
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
            Collections.sort(mTempListItems, Ingredient.DescendingNameComparator);
            mSortedByNameAsc = false;
        } else {
            // Sort ascending
            Collections.sort(mTempListItems, Ingredient.AscendingNameComparator);
            mSortedByNameAsc = true;
        }
        mAdapter.swapList(mTempListItems);
        mShoppingListRecyclerView.setAdapter(mAdapter);
    }

    /** Sorts the list by name */
    private void sortByColor() {
        mSortedBy = KEY_SORT_BY_COLOR;
        // Reset the sort by name toggle
        mSortedByNameAsc = false;
        mAdapter.swapList(null);
        if (mSortedByColorAsc) {
            // Sort descending
            Collections.sort(mTempListItems, Ingredient.DescendingColorComparator);
            mSortedByColorAsc = false;
        } else {
            // Sort ascending
            Collections.sort(mTempListItems, Ingredient.AscendingColorComparator);
            mSortedByColorAsc = true;
        }
        mAdapter.swapList(mTempListItems);
        mShoppingListRecyclerView.setAdapter(mAdapter);
    }

    /** Toggles the visibility of the color types */
    private void toggleColor() {
        // Toggle the boolean value
        mShowColors = !mShowColors;
        // Get the current position
        int pos = mLinearLayoutManager.findFirstVisibleItemPosition();
        // Refresh the adapter
        mAdapter = new ShoppingListAdapter(getActivity(), mTempListItems, mShowColors, true, this);
        mShoppingListRecyclerView.setAdapter(mAdapter);
        // Return to current position
        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < mTempListItems.size()) {
            mLinearLayoutManager.scrollToPosition(mScrollPosition);
        }
    }

    /** Updates the widgets with the new shopping list */
    private void updateWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext,
                ShoppingListAppWidget.class));

        ShoppingListAppWidget.mShoppingList = mTempListItems;

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_app_widget_shopping);
    }

    /** Setup the menu */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_shopping_list, menu);
    }

    /** Setup menu actions */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shopping_list_clear:
                clearShoppingList();
                return true;
            case R.id.action_shopping_list_add:
                // Add a new shopping list item
                Ingredient ingredient = new Ingredient();

                // Show the edit shopping list item dialog to insert a new shopping item
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                EditIngredientDialogFragment fragment = EditIngredientDialogFragment.newInstance(mUId,
                        ingredient, true, null);
                fragment.show(fragmentManager, null);

                return true;
            case R.id.action_shopping_list_sort_by_name:
                sortByName();
                return true;
            case R.id.action_shopping_list_sort_by_color:
                sortByColor();
                return true;
            case R.id.action_shopping_list_toggle_color:
                toggleColor();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Remove a value from the database */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ShoppingListAdapter.ViewHolder) {
            // Get the deleted position, ingredient and ingredient id
            final int deletedIndex = viewHolder.getAdapterPosition();
            final Ingredient deletedIngredient = mAdapter.mIngredients.get(deletedIndex);
            final String deletedIngredientId = deletedIngredient.getIngredientId();

            // Remove the ingredient from the adapter
            mTempListItems.remove(deletedIndex);
            mAdapter.swapList(mTempListItems);
            mIsDeleted = true;

            // Show SnackBar with undo option
            Snackbar snackbar = Snackbar.make(mConstraintLayout, getString(R.string
                    .snackbar_delete_meal),Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(ContextCompat.getColor(mContext, R.color
                    .snackBarTextColor));
            snackbar.setAction(getString(R.string.snackbar_undo), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIsDeleted = false;

                    // Restore the meal
                    mTempListItems.add(deletedIndex, deletedIngredient);
                    mAdapter.swapList(mTempListItems);
                }
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (mIsDeleted) {
                        // If the user did not press "Undo", delete the meal
                        if (deletedIngredientId.contains(",")) {
                            // This refers to more than one ingredient. Delete each of them
                            String[] splitId = deletedIngredientId.split(",");
                            for (String singleId : splitId) {
                                if (mDatabaseListItems.containsKey(singleId)) {
                                    mShoppingListRef.child(singleId)
                                            .removeValue();
                                }
                            }
                        } else {
                            // This is a single recipe. Remove it from the database
                            if (mDatabaseListItems.containsKey(deletedIngredientId)) {
                                mShoppingListRef.child(deletedIngredientId)
                                        .removeValue();
                            }
                        }
                    }
                }
            });
            snackbar.show();
        }
    }

    /** Called when a checkbox has been clicked */
    @Override
    public void onChecked(String ingredient_id, int value) {
        if (mDatabaseListItems.containsKey(ingredient_id)) {
            // Set the value for the isChecked property
            mShoppingListRef
                    .child(ingredient_id)
                    .child(Constants.NODE_IS_CHECKED)
                    .setValue(value);
        }
    }

    /** Called when a shopping item has been clicked */
    @Override
    public void onClicked(final Ingredient ingredient) {
        // Show the edit shopping list item dialog to edit the item
        android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
        EditIngredientDialogFragment fragment = EditIngredientDialogFragment.newInstance(mUId,
                ingredient, false, null);
        fragment.show(fragmentManager, null);
    }
}