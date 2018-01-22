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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.model.Ingredient;
import com.johnmagdalinos.android.shopandcook.model.Recipe;
import com.johnmagdalinos.android.shopandcook.model.RecipeFromApi;
import com.johnmagdalinos.android.shopandcook.ui.DetailActivity;
import com.johnmagdalinos.android.shopandcook.ui.DividerItemDecoration;
import com.johnmagdalinos.android.shopandcook.ui.IngredientItemTouchHelper;
import com.johnmagdalinos.android.shopandcook.ui.adapters.IngredientListAdapter;
import com.johnmagdalinos.android.shopandcook.ui.adapters.ShoppingListAdapter;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;
import com.johnmagdalinos.android.shopandcook.utilities.RecipeAsyncTask;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Fragment displaying a single recipe, either from the database or the food2fork api
 */

public class RecipeFragment extends android.support.v4.app.Fragment implements
        RecipeAsyncTask.RecipeAsyncTaskCallback,
        ShoppingListAdapter.ShoppingListAdapterCallback,
        IngredientItemTouchHelper.RecyclerItemTouchHelperListener,
        android.support.v7.view.ActionMode.Callback,
        View.OnClickListener {

    /** Keys for the Fragment args */
    private static final String KEY_RECIPE = "recipe";
    private static final String KEY_IS_NEW = "is_new";
    private static final String KEY_RECIPE_ID = "recipe_id";
    private static final String KEY_MEAL_ID = "meal_id";

    /** Key for saving instance state */
    private static final String KEY_VIEWING_MODE = "viewing_mode";
    private static final String KEY_CURRENT_RECIPE = "current_recipe";

    /** Member variables */
    private String mUId, mRecipeId, mMealId;
    private Activity mActivity;
    private RecipeFromApi mCurrentRecipeFromApi;
    private Recipe mCurrentRecipe;
    private LinearLayout mParentLayout, mAddIngredientBtnLayout;
    private TextView  mErrorTextView, mApiAttribTextView, mPrepTextView;
    private EditText mNameEditText, mServingsEditText, mTagsEditText, mDescriptionEditText;
    private ImageButton mPrepTimeBtn;
    private RecyclerView mIngredientsRecyclerView;
    private ImageView mImageView;
    private IngredientListAdapter mIngredientApiAdapter;
    private ShoppingListAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRecipesRef, mIngredientsRef;
    private HashMap<String, Ingredient> mDatabaseListItems;
    private ArrayList<Ingredient> mTempListItems;
    private android.support.v7.view.ActionMode mActionMode;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mViewingMode = true;
    private boolean mIsNew;
    private boolean mSaved = false;

    /** Class Constructor for use with the database */
    public static RecipeFragment newInstance(String userId, boolean isNew, @Nullable Recipe recipe,
                                             @Nullable String meal_id) {
        RecipeFragment fragment = new RecipeFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_RECIPE, recipe);
        args.putBoolean(KEY_IS_NEW, isNew);
        args.putString(KEY_MEAL_ID, meal_id);
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    /** Class Constructor for use with the food2fork API */
    public static RecipeFragment newInstanceForApi(String recipe_id) {
        RecipeFragment fragment = new RecipeFragment();
        Bundle args = new Bundle();
        args.putString(KEY_RECIPE_ID, recipe_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View viewRoot = inflater.inflate(R.layout.fragment_recipe, container, false);

        setHasOptionsMenu(true);

        // Get the arguments
        if (getArguments() != null) {
            mRecipeId = getArguments().getString(KEY_RECIPE_ID);
            mCurrentRecipe = getArguments().getParcelable(KEY_RECIPE);
            mIsNew = getArguments().getBoolean(KEY_IS_NEW);
            mMealId = getArguments().getString(KEY_MEAL_ID);
            mUId = getArguments().getString(Constants.KEY_USER_ID);
        }

        if (mMealId == null) {
            // This is a single recipe
            DetailActivity.mCurrentFragment = Constants.EXTRAS_SINGLE_RECIPE;
        } else {
            // This recipe is associated with a meal
            DetailActivity.mCurrentFragment = Constants.EXTRAS_MEAL_RECIPE;
        }

        mActivity = getActivity();

        // Initialize the HashMap and the ArrayList
        mDatabaseListItems = new HashMap<>();
        mTempListItems = new ArrayList<>();

        // Get the views
        mParentLayout = viewRoot.findViewById(R.id.ll_recipe);
        mAddIngredientBtnLayout = viewRoot.findViewById(R.id
                .ll_recipe_ingredients_add);
        mErrorTextView = viewRoot.findViewById(R.id.tv_recipe_error);
        mImageView = viewRoot.findViewById(R.id.iv_recipe_image);
        mIngredientsRecyclerView = viewRoot.findViewById(R.id.rv_recipe_ingredients);
        LinearLayout webButtonLayout = viewRoot.findViewById(R.id
                .ll_recipe_btn_url);
        mNameEditText = viewRoot.findViewById(R.id.et_recipe_name);
        mServingsEditText = viewRoot.findViewById(R.id.et_recipe_servings);
        mPrepTextView = viewRoot.findViewById(R.id.tv_recipe_prep);
        mTagsEditText = viewRoot.findViewById(R.id.et_recipe_tags);
        mDescriptionEditText = viewRoot.findViewById(R.id.et_recipe_description);
        mPrepTimeBtn = viewRoot.findViewById(R.id.btn_recipe_prep);
        mApiAttribTextView = getActivity().findViewById(R.id.tv_api_attribution);

        mAddIngredientBtnLayout.setOnClickListener(this);
        mPrepTimeBtn.setOnClickListener(this);

        // Get the custom font and set it on the name
        Typeface mFont = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/courgette_regular.ttf");
        mNameEditText.setTypeface(mFont);

        // Setup the Recycler View
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mIngredientsRecyclerView.setLayoutManager(linearLayoutManager);
        mIngredientsRecyclerView.setHasFixedSize(true);

        // Set the divider on the recycler view
        Drawable dividerDrawable = getActivity().getResources().getDrawable(R.drawable
                .recycler_view_divider);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration
                (dividerDrawable);
        mIngredientsRecyclerView.addItemDecoration(dividerItemDecoration);

        if (savedInstanceState != null) {
            mViewingMode = savedInstanceState.getBoolean(KEY_VIEWING_MODE);
            mCurrentRecipe = savedInstanceState.getParcelable(KEY_CURRENT_RECIPE);
            mUId = savedInstanceState.getString(Constants.KEY_USER_ID);
        }

        // Check the arguments
        if (mRecipeId != null) {
            // This is a recipe from the food2fork api
            // Get the views
            LinearLayout servingsLinearLayout = viewRoot.findViewById(R.id
                    .ll_recipe_servings);
            LinearLayout prepLinearLayout = viewRoot.findViewById(R.id
                    .ll_recipe_prep);
            LinearLayout descriptionLinearLayout = viewRoot.findViewById(R.id
                    .ll_recipe_description);
            LinearLayout tagsLinearLayout = viewRoot.findViewById(R.id
                    .ll_recipe_tags);

            // Hide/show appropriate views
            servingsLinearLayout.setVisibility(View.GONE);
            prepLinearLayout.setVisibility(View.GONE);
            mAddIngredientBtnLayout.setVisibility(View.GONE);
            descriptionLinearLayout.setVisibility(View.GONE);
            tagsLinearLayout.setVisibility(View.GONE);
            mApiAttribTextView.setVisibility(View.VISIBLE);

            // Get the recipe from the API
            new RecipeAsyncTask().setAsyncTaskListener(getActivity(), this).execute(mRecipeId);

            if (mCurrentRecipeFromApi != null) {
                mIngredientApiAdapter = new IngredientListAdapter(getActivity(), mCurrentRecipeFromApi.getIngredients());
                mIngredientsRecyclerView.setAdapter(mIngredientApiAdapter);
            }

            // Show the food2fork api web page button
            webButtonLayout.setOnClickListener(this);
        } else {
            // This is a recipe from the database
            // Hide the food2fork api web page button
            webButtonLayout.setVisibility(View.GONE);

            if (mTempListItems != null) {
                mAdapter = new ShoppingListAdapter(getActivity(), mTempListItems, false, false,
                        this);
                mIngredientsRecyclerView.setAdapter(mAdapter);
            }

            prepareDatabase();
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
        mApiAttribTextView.setVisibility(View.GONE);

        super.onDestroyView();
    }

    /** Saves the state of the action mode, the current recipe and the user id */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VIEWING_MODE, mViewingMode);
        outState.putParcelable(KEY_CURRENT_RECIPE, mCurrentRecipe);
        outState.putString(Constants.KEY_USER_ID, mUId);

    }

    /** Loads the Recipe data from the database into the views */
    private void prepareDatabase() {
        // Setup swipe actions on the Recycler View
        ItemTouchHelper.Callback callback = new IngredientItemTouchHelper(0,
                ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT, this);
        mItemTouchHelper = new ItemTouchHelper(callback);

        // Get an instance of the database
        mDatabase = FirebaseDatabase.getInstance();

        mRecipesRef = mDatabase.getReference()
                .child(Constants.NODE_RECIPES)
                .child(mUId);

        // Add a listener to update the Recycler View of recipes
        mRecipesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mCurrentRecipe = dataSnapshot.getValue(Recipe.class);
                String key = dataSnapshot.getKey();
                mCurrentRecipe.setRecipeId(key);

                if (mViewingMode) {
                    prepareViewsForViewing();
                } else {
                    prepareViewsForEditing();
                }
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

        if (mCurrentRecipe == null) {
            // This is a new recipe, temporarily save it
            mCurrentRecipe = new Recipe();
            DatabaseReference tempRef = mRecipesRef.push();
            String key = tempRef.getKey();
            mCurrentRecipe.setRecipeId(key);
            tempRef.setValue(mCurrentRecipe);
        }


        mIngredientsRef = mDatabase.getReference()
                .child(Constants.NODE_INGREDIENTS)
                .child(mUId)
                .child(mCurrentRecipe.getRecipeId());

        // Add a listener to update the Recycler View of ingredients
        mIngredientsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);
                String key = dataSnapshot.getKey();

                // Add the ingredient from the list and update the adapter
                mDatabaseListItems.put(key, ingredient);
                mTempListItems = hashMapToArray(mDatabaseListItems);
                // Sort the list by name
                Collections.sort(mTempListItems, Ingredient.AscendingNameComparator);
                // Set the list on the adapter
                mAdapter.swapList(mTempListItems);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);
                String key = dataSnapshot.getKey();

                // Add the ingredient from the list and update the adapter
                mDatabaseListItems.put(key, ingredient);
                mTempListItems = hashMapToArray(mDatabaseListItems);
                // Sort the list by name
                Collections.sort(mTempListItems, Ingredient.AscendingNameComparator);
                // Set the list on the adapter
                mAdapter.swapList(mTempListItems);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                if (mDatabaseListItems.containsKey(key)) {

                    // Remove the ingredient from the list and update the adapter
                    mDatabaseListItems.remove(key);


                    // Sort the list by name
                    Collections.sort(mTempListItems, Ingredient.AscendingNameComparator);
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

    /** Loads the Recipe data from the API into the views */
    private void displayApiRecipe() {
        if (mRecipeId != null) {
            // Display the parent layout
            mParentLayout.setVisibility(View.VISIBLE);

            // Set the title
            getActivity().setTitle(mCurrentRecipeFromApi.getName());

            mNameEditText.setText(mCurrentRecipeFromApi.getName());
            mNameEditText.setFocusable(false);
            mNameEditText.getBackground().setColorFilter(ContextCompat.getColor(mActivity, android.R.color.transparent), PorterDuff.Mode.SRC_IN);

            String imageUrl = mCurrentRecipeFromApi.getImageUrl();
            if (imageUrl != null && !TextUtils.isEmpty(imageUrl)) {
                mImageView.setVisibility(View.VISIBLE);
                Picasso.with(getActivity()).load(imageUrl).into(mImageView);
            } else {
                mImageView.setVisibility(View.GONE);
            }

            mIngredientApiAdapter = new IngredientListAdapter(getActivity(), mCurrentRecipeFromApi.getIngredients());
            mIngredientsRecyclerView.setAdapter(mIngredientApiAdapter);
        }
    }

    /** Hides the views needed only for editing/adding and sets the appropriate text */
    private void prepareViewsForViewing() {
        mViewingMode = true;

        // Set the title
        mActivity.setTitle(mCurrentRecipe.getName());

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
        mAddIngredientBtnLayout.setVisibility(View.GONE);
        mPrepTimeBtn.setVisibility(View.INVISIBLE);

        // Set the text on the Text Views & EditTexts
        mNameEditText.setText(mCurrentRecipe.getName());
        mServingsEditText.setText(String.valueOf(mCurrentRecipe.getServings()));
        mPrepTextView.setText(String.valueOf(Recipe.minToTime(mCurrentRecipe.getPrepTime())));
        mDescriptionEditText.setText(mCurrentRecipe.getDescription());
        mTagsEditText.setText(mCurrentRecipe.getTags());
    }

    /** Displays the views needed only for editing/adding and sets the appropriate text */
    private void prepareViewsForEditing() {
        mViewingMode = false;
        mSaved = false;

        // Add swipe actions on the Recycler View
        mItemTouchHelper.attachToRecyclerView(mIngredientsRecyclerView);

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

        mAddIngredientBtnLayout.setVisibility(View.VISIBLE);
        mPrepTextView.setText(String.valueOf(Recipe.minToTime(mCurrentRecipe.getPrepTime())));
        mPrepTimeBtn.setVisibility(View.VISIBLE);
    }

    /** Displays the views needed only when there is no internet connection */
    private void prepareViewsForNoInternetConnection() {
        // Hide the parent layout
        mParentLayout.setVisibility(View.INVISIBLE);

        // Show the message
        mErrorTextView.setVisibility(View.VISIBLE);

        // Check for internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            mErrorTextView.setText(R.string.error);
        } else {
            mErrorTextView.setText(R.string.no_internet);
        }
    }

    /** Saves the recipe */
    private void saveRecipe() {
        // User clicked on "Save"
        Recipe recipe = new Recipe();
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
            Toast.makeText(getActivity(), R.string.add_recipe_name, Toast.LENGTH_SHORT).show();
        } else {
            recipe.setName(name);
            recipe.setPrepTime(mCurrentRecipe.getPrepTime());
            recipe.setServings(servings);
            recipe.setTags(category);
            recipe.setDescription(description);

            // Update the value
            mRecipesRef.child(mCurrentRecipe.getRecipeId()).setValue(recipe);

            if (mMealId != null && !TextUtils.isEmpty(mMealId)) {
                // This is a recipe associated with a meal, update the association
                DatabaseReference mealRecipesRef = mDatabase.getReference()
                        .child(Constants.NODE_MEAL_RECIPES)
                        .child(mUId)
                        .child(mMealId)
                        .child(mCurrentRecipe.getRecipeId());
                mealRecipesRef.setValue(mCurrentRecipe.getRecipeId());

                DatabaseReference recipeMealsRef = mDatabase.getReference()
                        .child(Constants.NODE_RECIPE_MEALS)
                        .child(mUId)
                        .child(mCurrentRecipe.getRecipeId())
                        .child(mMealId);
                recipeMealsRef.setValue(mMealId);
            }

            // Exit the action mode
            mSaved = true;
            if (mActionMode != null) mActionMode.finish();
        }
    }

    /** Converts the HashMap of ingredients to an ArrayList of ingredients for the adapter */
    private static ArrayList<Ingredient> hashMapToArray(HashMap<String, Ingredient> ingredientHashMap) {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        // Convert the HashMap to an ArrayList
        for (String key : ingredientHashMap.keySet()) {
            Ingredient ingredient = ingredientHashMap.get(key);
            ingredient.setIngredientId(key);
            ingredients.add(ingredient);
        }
        return ingredients;
    }

    /** Get the results from the AsyncTask and display them */
    @Override
    public void onAsyncTaskCompleted(RecipeFromApi recipeFromApi) {
        mCurrentRecipeFromApi = recipeFromApi;
        if (mCurrentRecipeFromApi != null) {
            displayApiRecipe();
        } else {
            prepareViewsForNoInternetConnection();
        }
    }

    /** Creates the message to be shared via an intent */
    private String createShareMessage() {
        // Build the message to share
        StringBuilder textMessage = new StringBuilder();
        textMessage.append(getString(R.string.share_recipe_title));
        textMessage.append("\n");
        textMessage.append(mCurrentRecipe.getName());
        textMessage.append("\n\n");
        textMessage.append(getString(R.string.recipe_ingredients_title));
        textMessage.append("\n");

        // Add the ingredients
        for (Ingredient ingredient : mTempListItems) {
            String name = ingredient.getName();
            double quantity = ingredient.getQuantity();
            int measure = ingredient.getMeasure();
            String notes = ingredient.getComments();

            textMessage.append(name);
            textMessage.append(" ");
            textMessage.append(Ingredient.getQuantityString(quantity));
            textMessage.append(" ");
            textMessage.append(Ingredient.getMeasureString(getActivity(), measure, quantity));
            textMessage.append(" ");
            textMessage.append(notes);
            textMessage.append(" ");
            textMessage.append("\n");
        }

        // Add the description
        textMessage.append("\n");
        textMessage.append(getString(R.string.recipe_description));
        textMessage.append("\n");
        textMessage.append(mCurrentRecipe.getDescription());

        return textMessage.toString();
    }

    /** Called when the "Add ingredient" Button has been pressed */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_recipe_ingredients_add:
                Ingredient ingredient = new Ingredient();
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                // Show the edit ingredient dialog to add an ingredient
                EditIngredientDialogFragment fragment = EditIngredientDialogFragment.newInstance
                        (mUId, ingredient, true, mCurrentRecipe.getRecipeId());
                fragment.show(fragmentManager, null);
                break;
            case R.id.ll_recipe_btn_url:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse
                        (mCurrentRecipeFromApi.getSourceUrl()));
                startActivity(browserIntent);
                break;
            case R.id.btn_recipe_prep:
                android.support.v4.app.FragmentManager timeFragmentManager =
                        getFragmentManager();
                DialogFragment newFragment = TimePickerFragment.newInstance(mCurrentRecipe, mUId);
                // Show the dialog to set the prep time
                newFragment.show(timeFragmentManager, null);
                break;
            default:
                break;
        }
    }

    /** Not called in this fragment */
    @Override
    public void onChecked(String ingredient_id, int value) {

    }

    /** Called when an ingredient has been clicked */
    @Override
    public void onClicked(Ingredient ingredient) {
        if (!mViewingMode) {
            // Show the edit ingredient dialog to edit the ingredient
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            EditIngredientDialogFragment fragment = EditIngredientDialogFragment.newInstance(mUId,
                    ingredient, false,
                    mCurrentRecipe.getRecipeId());
            fragment.show(fragmentManager, null);
        }
    }

    /** Remove a value from the database */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ShoppingListAdapter.ViewHolder) {
            // Get the deleted position, ingredient and ingredient id
            int deletedIndex = viewHolder.getAdapterPosition();
            Ingredient deletedIngredient = mAdapter.mIngredients.get(deletedIndex);
            String deletedIngredientId = deletedIngredient.getIngredientId();

            // Remove the item from the database
            if (mDatabaseListItems.containsKey(deletedIngredientId)) {
                mIngredientsRef.child(deletedIngredientId)
                        .removeValue();
            }
        }
    }

    /** Setup the menu */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mCurrentRecipe == null) {
            if (mRecipeId == null) {
                inflater.inflate(R.menu.action_mode_menu, menu);
            }
        } else {
            inflater.inflate(R.menu.menu_single_recipe, menu);
        }

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
                saveRecipe();
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
                saveRecipe();
                return true;
            case R.id.action_context_recipe_cancel:
                // Exit the action mode
                mActionMode.finish();
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
        // If the recipe was new and the user did not press "Save", delete it
        if (mSaved) {
            // Display the appropriate views
            prepareViewsForViewing();
        } else {
            if (mIsNew) {
                mRecipesRef
                        .child(mCurrentRecipe.getRecipeId())
                        .removeValue();
                getActivity().onBackPressed();
            } else {
                prepareViewsForViewing();
            }
        }

        mActionMode = null;
    }
}
