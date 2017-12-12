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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.model.Meal;
import com.example.android.shopandcook.model.Recipe;
import com.example.android.shopandcook.ui.DetailActivity;
import com.example.android.shopandcook.ui.DividerItemDecoration;
import com.example.android.shopandcook.ui.RecipeItemTouchHelper;
import com.example.android.shopandcook.ui.adapters.RecipeListAdapter;
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
 * Fragment displaying a single meal
 */

public class MealFragment extends android.support.v4.app.Fragment implements
        RecipeListAdapter.RecipeListAdapterCallback,
        RecipeItemTouchHelper.RecyclerItemTouchHelperListener,
        android.support.v7.view.ActionMode.Callback,
        View.OnClickListener {

    /** Key for the Fragment args */
    private static final String KEY_MEAL = "meal";
    private static final String KEY_IS_NEW = "is_new";
    private static final String KEY_DAY_ID = "day_id";
    private static final String KEY_MEAL_TITLE = "meal_title";
    private static final String KEY_MEAL_TITLE_NAME = "meal_title_name";

    /** Key for saving instance state */
    private static final String KEY_VIEWING_MODE = "viewing_mode";
    private static final String KEY_CURRENT_MEAL = "current_meal";

    /** Member variables */
    private String mUId;
    private MealCallback mCallback;
    private Activity mActivity;
    private Meal mCurrentMeal;
    private LinearLayout mAddRecipeBtnLayout;
    private EditText mNameEditText, mTagsEditText, mServingsEditText, mDescriptionEditText;
    private RecyclerView mRecipeRecyclerView;
    private RecipeListAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMealsRef, mMealRecipesRef, mRecipesRef;
    private HashMap<String, Recipe> mDatabaseListItems;
    private ArrayList<Recipe> mTempListItems;
    private android.support.v7.view.ActionMode mActionMode;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mViewingMode = true;
    private boolean mIsNew;
    private String mDayId, mMealTitle, mMealTitleName;
    private boolean mSaved = false;


    public interface MealCallback {
        void onMealRecipeClick(Recipe recipe, String meal_id);
    }

    /** Class Constructor for use with the database */
    public static MealFragment newInstance(String userId, boolean isNew, @Nullable Meal meal,
                                           @Nullable String day_id, @Nullable String mealTitle,
                                           @Nullable String mealTitleName) {
        MealFragment fragment = new MealFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_MEAL, meal);
        args.putBoolean(KEY_IS_NEW, isNew);
        args.putString(KEY_DAY_ID, day_id);
        args.putString(KEY_MEAL_TITLE, mealTitle);
        args.putString(KEY_MEAL_TITLE_NAME, mealTitleName);
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    /** Override onAttach to make sure that the container activity has implemented the callback */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (MealCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MealCallback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View viewRoot = inflater.inflate(R.layout.fragment_meal, container, false);

        setHasOptionsMenu(true);

        // Get the arguments
        if (getArguments() != null) {
            mCurrentMeal = getArguments().getParcelable(KEY_MEAL);
            mIsNew = getArguments().getBoolean(KEY_IS_NEW);
            mDayId = getArguments().getString(KEY_DAY_ID);
            mMealTitle = getArguments().getString(KEY_MEAL_TITLE);
            mMealTitleName = getArguments().getString(KEY_MEAL_TITLE_NAME);
            mUId = getArguments().getString(Constants.KEY_USER_ID);
        }

        if (mDayId == null) {
            DetailActivity.mCurrentFragment = Constants.EXTRAS_SINGLE_MEAL;
        } else {
            DetailActivity.mCurrentFragment = Constants.EXTRAS_DAY_MEAL;
        }

        mActivity = getActivity();

        // Initialize the HashMap and the ArrayList
        mDatabaseListItems = new HashMap<>();
        mTempListItems = new ArrayList<>();

        // Get the views
        mAddRecipeBtnLayout = viewRoot.findViewById(R.id.ll_meal_recipes_add);
        mRecipeRecyclerView = viewRoot.findViewById(R.id.rv_meal_recipes);
        mNameEditText = viewRoot.findViewById(R.id.et_meal_name);
        mTagsEditText = viewRoot.findViewById(R.id.et_meal_tags);
        mServingsEditText = viewRoot.findViewById(R.id.et_meal_servings);
        mDescriptionEditText = viewRoot.findViewById(R.id.et_meal_description);

        mAddRecipeBtnLayout.setOnClickListener(this);

        // Get the custom font and set it on the name
        Typeface mFont = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/courgette_regular.ttf");
        mNameEditText.setTypeface(mFont);


        if (savedInstanceState != null) {
            mViewingMode = savedInstanceState.getBoolean(KEY_VIEWING_MODE);
            mCurrentMeal = savedInstanceState.getParcelable(KEY_CURRENT_MEAL);
            mUId = savedInstanceState.getString(Constants.KEY_USER_ID);
        }

        // Setup the Recycler View
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecipeRecyclerView.setLayoutManager(linearLayoutManager);
        mRecipeRecyclerView.setHasFixedSize(true);

        // Set the divider on the recycler view
        Drawable dividerDrawable = getActivity().getResources().getDrawable(R.drawable
                .recycler_view_divider);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration
                (dividerDrawable);
        mRecipeRecyclerView.addItemDecoration(dividerItemDecoration);

        if (mTempListItems != null) {
            mAdapter = new RecipeListAdapter(getActivity(), mTempListItems, this, true);
            mRecipeRecyclerView.setAdapter(mAdapter);
        }
        setupForDatabase();
        
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

    /** Saves the state of the action mode, the current meal and the user id */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VIEWING_MODE, mViewingMode);
        outState.putParcelable(KEY_CURRENT_MEAL, mCurrentMeal);
        outState.putString(Constants.KEY_USER_ID, mUId);
    }

    /** Loads the Meal data from the database into the views */
    private void setupForDatabase() {
        // Setup swipe actions on the Recycler View
        ItemTouchHelper.Callback callback = new RecipeItemTouchHelper(0,
                ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT, this);
        mItemTouchHelper = new ItemTouchHelper(callback);

        // Get an instance of the database
        mDatabase = FirebaseDatabase.getInstance();

        // Setup the reference for the meals
        mMealsRef = mDatabase.getReference()
                .child(Constants.NODE_MEALS)
                .child(mUId);

        mMealsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mCurrentMeal = dataSnapshot.getValue(Meal.class);
                String key = dataSnapshot.getKey();
                mCurrentMeal.setMealId(key);

                prepareViewsForViewing();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (mCurrentMeal == null) {
            // This is a new meal, temporarily save it
            mCurrentMeal = new Meal();
            DatabaseReference tempRef = mMealsRef.push();
            String key = tempRef.getKey();
            mCurrentMeal.setMealId(key);
            tempRef.setValue(mCurrentMeal);
        }

        // Setup the reference for the recipes of the current meal
        mMealRecipesRef = mDatabase.getReference()
                .child(Constants.NODE_MEAL_RECIPES)
                .child(mUId)
                .child(mCurrentMeal.getMealId());

        // Setup the reference for all recipes
        mRecipesRef = mDatabase.getReference()
                .child(Constants.NODE_RECIPES)
                .child(mUId);

        // Add a listener to update the Recycler View of meals
        mMealRecipesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the recipe_id from the database
                String recipe_id = dataSnapshot.getValue(String.class);

                // Add the recipe from the list and update the adapter
                getRecipe(recipe_id);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // Get the recipe_id from the database
                String recipe_id = dataSnapshot.getValue(String.class);

                // Add the recipe from the list and update the adapter
                getRecipe(recipe_id);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                if (mDatabaseListItems.containsKey(key)) {

                    // Remove the ingredient from the list and update the adapter
                    mDatabaseListItems.remove(key);

                    // Sort the list by name
                    Collections.sort(mTempListItems, Recipe.DescendingNameComparator);
                    mTempListItems = hashMapToArray(mDatabaseListItems);

                    // Update the adapter
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

        if (mIsNew || !mViewingMode) {
            // Enable action mode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);

        } else {
            // Setup the views
            prepareViewsForViewing();
        }
    }

    /** Hides the views needed only for editing/adding and sets the appropriate text */
    private void prepareViewsForViewing() {
        mViewingMode = true;

        // Set the title
        mActivity.setTitle(mCurrentMeal.getName());

        // Remove swipe actions on the Recycler View
        mItemTouchHelper.attachToRecyclerView(null);

        // Remove the underline/focusability of the edit texts
        mNameEditText.setFocusable(false);
        mNameEditText.getBackground().setColorFilter(ContextCompat.getColor(mActivity, android.R.color.transparent), PorterDuff.Mode.SRC_IN);
        mServingsEditText.setFocusable(false);
        mServingsEditText.getBackground().setColorFilter(ContextCompat.getColor(mActivity, android.R.color.transparent), PorterDuff.Mode.SRC_IN);
        mTagsEditText.setFocusable(false);
        mTagsEditText.getBackground().setColorFilter(ContextCompat.getColor(mActivity, android.R.color
                .transparent), PorterDuff.Mode.SRC_IN);
        mDescriptionEditText.setFocusable(false);
        mDescriptionEditText.getBackground().setColorFilter(ContextCompat.getColor(mActivity, android.R.color.transparent), PorterDuff.Mode.SRC_IN);
        mAddRecipeBtnLayout.setVisibility(View.GONE);

        // Set the text on the Edit Text Views
        mNameEditText.setText(mCurrentMeal.getName());
        String servings = String.valueOf(mCurrentMeal.getServings());
        mServingsEditText.setText(servings);
        mTagsEditText.setText(mCurrentMeal.getTags());
        mDescriptionEditText.setText(mCurrentMeal.getDescription());
    }

    /** Displays the views needed only for editing/adding and sets the appropriate text */
    private void prepareViewsForEditing() {
        mViewingMode = false;

        // Add swipe actions on the Recycler View
        mItemTouchHelper.attachToRecyclerView(mRecipeRecyclerView);

        // Enable the underline/focusability of the edit texts
        mNameEditText.setFocusable(true);
        mNameEditText.setFocusableInTouchMode(true);
        mNameEditText.getBackground().clearColorFilter();

        mServingsEditText.setFocusable(true);
        mServingsEditText.setFocusableInTouchMode(true);
        mServingsEditText.getBackground().clearColorFilter();

        mDescriptionEditText.setFocusable(true);
        mDescriptionEditText.setFocusableInTouchMode(true);
        mDescriptionEditText.getBackground().clearColorFilter();

        mTagsEditText.setFocusable(true);
        mTagsEditText.setFocusableInTouchMode(true);
        mTagsEditText.getBackground().clearColorFilter();

        mAddRecipeBtnLayout.setVisibility(View.VISIBLE);
    }

    /** Saves the recipe */
    private void saveMeal() {
        // User clicked on "Save"
        Meal meal = new Meal();
        String name = mNameEditText.getText().toString().trim();
        String servingsString = mServingsEditText.getText().toString().trim();
        String category = mTagsEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();

        int servings = 0;
        try {
            servings = Integer.parseInt(servingsString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Check if the user has set a name
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getActivity(), R.string.add_meal_name, Toast.LENGTH_SHORT).show();
        } else {
            meal.setName(name);
            meal.setServings(servings);
            meal.setTags(category);
            meal.setDescription(description);

            // Update the value
            mMealsRef.child(mCurrentMeal.getMealId()).setValue(meal);

            if (mDayId != null && !TextUtils.isEmpty(mDayId)) {
                // This is a meal associated with a day, update the association
                DatabaseReference dayMealsRef = mDatabase.getReference()
                        .child(Constants.NODE_DAYS)
                        .child(mUId)
                        .child(mDayId);

                // Save the the meal_id in the database under the current day_id
                dayMealsRef
                        .child(mMealTitle)
                        .setValue(mCurrentMeal.getMealId());

                // Save the the meal_id in the database under the current day_id
                dayMealsRef
                        .child(mMealTitle)
                        .setValue(mCurrentMeal.getMealId());

                // Save the meal name in the database under the current day_id
                dayMealsRef
                        .child(mMealTitleName)
                        .setValue(meal.getName());

                String key = mMealTitle + "," + mDayId;

                DatabaseReference mealDaysRef = mDatabase.getReference()
                        .child(Constants.NODE_MEAL_DAYS)
                        .child(mUId)
                        .child(mCurrentMeal.getMealId())
                        .child(key);
                mealDaysRef.setValue(key);
            }

            // Exit the action mode
            mSaved = true;
            if (mActionMode != null) mActionMode.finish();
        }
    }

    /** Converts the HashMap of meals to an ArrayList of meals for the adapter */
    private static ArrayList<Recipe> hashMapToArray(HashMap<String, Recipe> recipesHashMap) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        // Convert the HashMap to an ArrayList
        for (String key : recipesHashMap.keySet()) {
            Recipe recipe = recipesHashMap.get(key);
            if (recipe != null) {
                recipe.setRecipeId(key);
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    /** Finds a recipe given a recipe_id and adds it to the adapter list */
    private void getRecipe(final String recipe_id) {
        mRecipesRef.child(recipe_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Recipe recipe = dataSnapshot.getValue(Recipe.class);

                // Add the recipe to the list and update the adapter
                mDatabaseListItems.put(recipe_id, recipe);
                mTempListItems = hashMapToArray(mDatabaseListItems);
                // Sort the list by name
                Collections.sort(mTempListItems, Recipe.AscendingNameComparator);
                // Set the list on the adapter
                mAdapter.swapList(mTempListItems);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Called when the "Add recipe" Button has been pressed */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_meal_recipes_add:
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                // Display the select recipe dialog
                SelectRecipeDialogFragment fragment = SelectRecipeDialogFragment.newInstance
                        (mUId, mCurrentMeal.getMealId());
                fragment.show(fragmentManager, null);
                break;
            default:
                break;
        }
    }

    /** Creates the message to be shared via an intent */
    private String createShareMessage() {
        // Build the message to share
        StringBuilder textMessage = new StringBuilder();
        textMessage.append(getString(R.string.share_meal_title));
        textMessage.append("\n");
        textMessage.append(mCurrentMeal.getName());
        textMessage.append("\n");

        // Add the recipes
        if (mTempListItems.size() > 1) {
            for (int i = 0; i < mTempListItems.size(); i++) {
                int secondToLastItem = mTempListItems.size() - 2;
                int lastItem = mTempListItems.size() - 1;

                if (i == 0) {
                    textMessage.append(mTempListItems.get(i).getName());
                    textMessage.append(" ");
                    textMessage.append(getString(R.string.share_meal_with));
                    textMessage.append(" ");
                } else if (i == secondToLastItem) {
                    textMessage.append(mTempListItems.get(i).getName());
                    textMessage.append(" ");
                    textMessage.append(getString(R.string.share_meal_and));
                    textMessage.append(" ");
                } else if (i == lastItem) {
                    textMessage.append(mTempListItems.get(i).getName());
                } else {
                    textMessage.append(mTempListItems.get(i).getName());
                    textMessage.append(", ");
                }
            }
        } else if (mTempListItems.size() == 1) {
            textMessage.append(mTempListItems.get(0).getName());
        }
        return textMessage.toString();
    }

    /** Called when a recipe has been clicked */
    @Override
    public void onClicked(Recipe recipe) {
        if (!mViewingMode) {
            // Pass the current meal_id and the clicked recipe to the activity
            mCallback.onMealRecipeClick(recipe, mCurrentMeal.getMealId());
            mActionMode.finish();
        }
    }

    /** Remove a recipe from the database */
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecipeListAdapter.ViewHolder) {
            // Get the deleted position, recipe and recipe id
            int deletedIndex = viewHolder.getAdapterPosition();
            Recipe deletedRecipe = mAdapter.mRecipes.get(deletedIndex);
            String deletedRecipeId = deletedRecipe.getRecipeId();

            // Remove recipe from the database
            if (mDatabaseListItems.containsKey(deletedRecipeId)) {
                mMealRecipesRef.child(deletedRecipeId)
                        .removeValue();

                // Remove the recipe's association with the meal
                DatabaseReference recipeMealsRef = mDatabase.getReference()
                        .child(Constants.NODE_RECIPE_MEALS)
                        .child(mUId);

                recipeMealsRef
                        .child(deletedRecipeId)
                        .child(mCurrentMeal.getMealId())
                        .removeValue();
            }
        }
    }

    /** Setup the menu */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_single_recipe, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Setup menu actions */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_recipe_edit:
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
                return true;
            case R.id.action_recipe_share:
                // Create and start the intent
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, createShareMessage());
                shareIntent.setType("text/plain");
                startActivity(shareIntent);
                return true;
            case R.id.action_context_recipe_save:
                saveMeal();
                return true;
            case R.id.action_context_recipe_cancel:
                getActivity().getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Setup the ActionMode for editing/adding recipes */
    @Override
    public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.action_mode_menu, menu);

        // Display the appropriate views
        prepareViewsForEditing();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_context_recipe_save:
                // Save the recipe
                saveMeal();
                return true;
            case R.id.action_context_recipe_cancel:
                // Exit the action mode
                actionMode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(android.support.v7.view.ActionMode actionMode) {
        // If the keyboard is visible, hide it
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getView() != null) {
            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }

        // If the meal was new and the user did not press "Save", delete it
        if (mSaved) {
            // Display the appropriate views
            prepareViewsForViewing();
        } else {
            if (mIsNew) {
                mMealsRef
                        .child(mCurrentMeal.getMealId())
                        .removeValue();
                getActivity().onBackPressed();
            } else {
                prepareViewsForViewing();
            }
        }

        mActionMode = null;
    }

}
