package com.johnmagdalinos.android.shopandcook.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.model.Meal;
import com.johnmagdalinos.android.shopandcook.model.Recipe;
import com.johnmagdalinos.android.shopandcook.ui.fragments.DayFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.DaySelectorFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.MealFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.MealListFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.RecipeFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.RecipeListFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.ShoppingListFragment;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class DetailActivity extends AppCompatActivity implements
        RecipeListFragment.OnRecipeFragmentCallback,
        MealListFragment.OnMealFragmentCallback,
        MealFragment.MealCallback,
        DayFragment.DayCallback {

    /** Member variables */
    private String mUid;
    private android.support.v4.app.FragmentManager mFragmentManager;
    private ShoppingListFragment mShoppingListFragment;
    private DaySelectorFragment mDaySelectorFragment;
    private RecipeListFragment mRecipeListFragment, mRecipeAPIListFragment;
    private MealListFragment mMealListFragment;
    private RecipeFragment mSingleRecipeFragment, mMealRecipeFragment;
    private MealFragment mSingleMealFragment, mDayMealFragment;
    public static String mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the fragment manager
        mFragmentManager = getSupportFragmentManager();

        // Initialize the ad
        MobileAds.initialize(this, "ca-app-pub-2218636981314048~3547583102");
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if (savedInstanceState != null) {
            // This is a restored instance, restore the appropriate fragment
            mUid = savedInstanceState.getString(Constants.KEY_USER_ID);
            restoreFragments(savedInstanceState);
        }

        if (mCurrentFragment == null) {
            // This is a new instance of the Activity, get the intent
            getFragmentFromIntent(savedInstanceState);
        }
    }

    /** Saves the appropriate fragment, mCurrentFragmentName and user id */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the user id
        outState.putString(Constants.KEY_USER_ID, mUid);
        // Save the appropriate fragment
        switch (mCurrentFragment) {
            case Constants.EXTRAS_SHOPPING_LIST:
                if (mShoppingListFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_SHOPPING_LIST, mShoppingListFragment);
                }
                break;
            case Constants.EXTRAS_MEAL_PLANNER:
                if (mDaySelectorFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_MEAL_PLANNER, mDaySelectorFragment);
                }
                break;
            case Constants.EXTRAS_RECIPES:
                if (mRecipeListFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_RECIPES, mRecipeListFragment);
                }
                break;
            case Constants.EXTRAS_MEALS:
                if (mMealListFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_MEALS, mMealListFragment);
                }
                break;
            case Constants.EXTRAS_RECIPE_IDEAS:
                if (mRecipeAPIListFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_RECIPE_IDEAS, mRecipeAPIListFragment);
                }
                break;
            case Constants.EXTRAS_SINGLE_RECIPE:
                if (mSingleRecipeFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_SINGLE_RECIPE, mSingleRecipeFragment);
                }
                break;
            case Constants.EXTRAS_SINGLE_MEAL:
                if (mSingleMealFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_SINGLE_MEAL, mSingleMealFragment);
                }
                break;
            case Constants.EXTRAS_MEAL_RECIPE:
                if (mMealRecipeFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_MEAL_RECIPE, mMealRecipeFragment);
                }
                break;
            case Constants.EXTRAS_DAY_MEAL:
                if (mDayMealFragment != null) {
                    mFragmentManager.putFragment(outState, Constants.EXTRAS_DAY_MEAL, mDayMealFragment);
                }
                break;
        }
        // Save the name of the current fragment
        outState.putString(Constants.EXTRAS_CURRENT_FRAGMENT, mCurrentFragment);
    }

    /** Creates a fragment based on the intent passed to the activity */
    private void getFragmentFromIntent(@Nullable Bundle savedInstanceState) {
        mUid = getIntent().getStringExtra(Constants.KEY_USER_ID);
        // Figure out which button was pressed */
        switch (getIntent().getStringExtra(Constants.KEY_EXTRAS)) {
            case Constants.EXTRAS_SHOPPING_LIST:
                if (savedInstanceState == null) {
                    // Create a new fragment
                    mShoppingListFragment = ShoppingListFragment.newInstance(mUid);
                }
                // Set the current fragment name
                mCurrentFragment = Constants.EXTRAS_SHOPPING_LIST;

                // Display the fragment
                mFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mShoppingListFragment)
                        .commit();
                break;
            case Constants.EXTRAS_MEAL_PLANNER:
                if (savedInstanceState == null) {
                    // Get the day (used when a specific day is pressed in a widget)
                    int dayId = getIntent().getIntExtra(Constants.EXTRAS_MEAL_PLANNER, 0);
                    // Create a new fragment
                    mDaySelectorFragment = DaySelectorFragment.newInstance(mUid, dayId);
                }
                // Set the current fragment name
                mCurrentFragment = Constants.EXTRAS_MEAL_PLANNER;

                // Display the fragment
                mFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mDaySelectorFragment)
                        .commit();
                break;
            case Constants.EXTRAS_RECIPES:
                if (savedInstanceState == null) {
                    // Create a new fragment
                    mRecipeListFragment = RecipeListFragment.newInstance(mUid, false);
                }

                // Set the current fragment name
                mCurrentFragment = Constants.EXTRAS_RECIPES;

                // Display the fragment
                mFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mRecipeListFragment)
                        .commit();
                break;
            case Constants.EXTRAS_MEALS:
                if (savedInstanceState == null) {
                    // Create a new fragment
                    mMealListFragment = MealListFragment.newInstance(mUid);
                }

                // Set the current fragment name
                mCurrentFragment = Constants.EXTRAS_MEALS;

                // Display the fragment
                mFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mMealListFragment)
                        .commit();
                break;
            case Constants.EXTRAS_RECIPE_IDEAS:
                if (savedInstanceState == null) {
                    // Create a new fragment
                    mRecipeAPIListFragment = RecipeListFragment.newInstance(mUid, true);
                }

                // Set the current fragment name
                mCurrentFragment = Constants.EXTRAS_RECIPE_IDEAS;

                // Display the fragment
                mFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mRecipeAPIListFragment)
                        .commit();
                break;
        }
    }

    /** Restores a fragment from the savedInstanceState */
    private void restoreFragments(Bundle savedInstanceState) {
        // Get the name of the current fragment
        mCurrentFragment = savedInstanceState.getString(Constants.EXTRAS_CURRENT_FRAGMENT);

        switch (mCurrentFragment) {
            // Restore the appropriate fragment
            case Constants.EXTRAS_SHOPPING_LIST:
                mShoppingListFragment = (ShoppingListFragment) mFragmentManager.getFragment
                        (savedInstanceState, Constants.EXTRAS_SHOPPING_LIST);
                break;
            case Constants.EXTRAS_MEAL_PLANNER:
                mDaySelectorFragment = (DaySelectorFragment) mFragmentManager.getFragment(savedInstanceState,
                        Constants.EXTRAS_MEAL_PLANNER);
                break;
            case Constants.EXTRAS_RECIPES:
                mRecipeListFragment = (RecipeListFragment) mFragmentManager.getFragment(savedInstanceState,
                        Constants.EXTRAS_RECIPES);
                break;
            case Constants.EXTRAS_MEALS:
                mMealListFragment = (MealListFragment) mFragmentManager.getFragment(savedInstanceState,
                        Constants.EXTRAS_MEALS);
                break;
            case Constants.EXTRAS_RECIPE_IDEAS:
                mRecipeAPIListFragment = (RecipeListFragment) mFragmentManager.getFragment
                        (savedInstanceState,Constants.EXTRAS_RECIPE_IDEAS);
                break;
            case Constants.EXTRAS_SINGLE_RECIPE:
                mSingleRecipeFragment = (RecipeFragment) mFragmentManager.getFragment
                        (savedInstanceState, Constants.EXTRAS_SINGLE_RECIPE);
                break;
            case Constants.EXTRAS_SINGLE_MEAL:
                mSingleMealFragment = (MealFragment) mFragmentManager.getFragment
                        (savedInstanceState, Constants.EXTRAS_SINGLE_MEAL);
                break;
            case Constants.EXTRAS_MEAL_RECIPE:
                mMealRecipeFragment = (RecipeFragment) mFragmentManager.getFragment
                        (savedInstanceState, Constants.EXTRAS_MEAL_RECIPE);
                break;
            case Constants.EXTRAS_DAY_MEAL:
                mDayMealFragment = (MealFragment) mFragmentManager.getFragment
                        (savedInstanceState, Constants.EXTRAS_DAY_MEAL);
                break;
        }
    }

    /** Setup the menu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Called when a recipe from the list has been clicked. Starts a new Recipe fragment */
    @Override
    public void onRecipeClick(boolean isNew, boolean isFromApi, @Nullable Recipe recipe, @Nullable
            String recipe_id) {
        if (isFromApi) {
            // This is a recipe from the food2fork API
            mSingleRecipeFragment = RecipeFragment.newInstanceForApi(recipe_id);
        } else {
            // This is either a recipe from the database or a new recipe
            mSingleRecipeFragment = RecipeFragment.newInstance(mUid, isNew, recipe, null);
        }

        // Set the current fragment name
        mCurrentFragment = Constants.EXTRAS_SINGLE_RECIPE;

        // Display the fragment
        mFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_detail, mSingleRecipeFragment)
                .addToBackStack(null)
                .commit();
    }

    /** Called when a meal from the list has been clicked. Starts a new fragment */
    @Override
    public void onMealClick(boolean isNew, @Nullable Meal meal, @Nullable String day_id,
                            @Nullable String mealTitle, @Nullable String mealTitleName) {
        mSingleMealFragment = MealFragment.newInstance(mUid, isNew, meal, day_id, mealTitle,
                mealTitleName);

        // Set the current fragment name
        mCurrentFragment = Constants.EXTRAS_SINGLE_MEAL;

        // Display the fragment
        mFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_detail, mSingleMealFragment)
                .addToBackStack(null)
                .commit();
    }

    /** Called when a recipe from within a Meal has been clicked. Starts a new Recipe fragment */
    @Override
    public void onMealRecipeClick(Recipe recipe, String meal_id) {
        mMealRecipeFragment = RecipeFragment.newInstance(mUid, false, recipe, meal_id);

        // Set the current fragment name
        mCurrentFragment = Constants.EXTRAS_MEAL_RECIPE;

        // Display the fragment
        mFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_detail, mMealRecipeFragment)
                .addToBackStack(null)
                .commit();
    }

    /** Called when a meal from within a Day has been clicked. Starts a new Meal fragment */
    @Override
    public void onDayMealClick(Meal meal, String day_id, String mealTitle, String
            mealTitleName) {
        mDayMealFragment = MealFragment.newInstance(mUid, false, meal, day_id, mealTitle,
                mealTitleName);

        // Set the current fragment name
        mCurrentFragment = Constants.EXTRAS_DAY_MEAL;

        // Display the fragment
        mFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_detail, mDayMealFragment)
                .addToBackStack(null)
                .commit();
    }
}
