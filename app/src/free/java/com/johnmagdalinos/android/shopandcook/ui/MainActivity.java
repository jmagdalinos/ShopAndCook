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

package com.johnmagdalinos.android.shopandcook.ui;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.model.Meal;
import com.johnmagdalinos.android.shopandcook.model.Recipe;
import com.johnmagdalinos.android.shopandcook.ui.appwidgets.DayAppWidget;
import com.johnmagdalinos.android.shopandcook.ui.appwidgets.ShoppingListAppWidget;
import com.johnmagdalinos.android.shopandcook.ui.appwidgets.WeekAppWidget;
import com.johnmagdalinos.android.shopandcook.ui.appwidgets.WidgetUpdateService;
import com.johnmagdalinos.android.shopandcook.ui.fragments.DayFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.DaySelectorFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.MainFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.MealFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.MealListFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.RecipeFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.RecipeListFragment;
import com.johnmagdalinos.android.shopandcook.ui.fragments.ShoppingListFragment;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        MainFragment.MainFragmentCallback,
        RecipeListFragment.OnRecipeFragmentCallback,
        MealListFragment.OnMealFragmentCallback,
        MealFragment.MealCallback,
        DayFragment.DayCallback {
    
    /** Member variables */
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String mExtrasFromWidget;
    private boolean mIsTablet;
    private FragmentManager mMainFragmentManager;
    private android.support.v4.app.FragmentManager mDetailFragmentManager;
    private MainFragment mMainFragment;
    private ShoppingListFragment mShoppingListFragment;
    private DaySelectorFragment mDaySelectorFragment;
    private RecipeListFragment mRecipeListFragment, mRecipeAPIListFragment;
    private MealListFragment mMealListFragment;
    private RecipeFragment mSingleRecipeFragment, mMealRecipeFragment;
    private MealFragment mSingleMealFragment, mDayMealFragment;
    private static String mCurrentFragmentName = null;
    private int mCurrentDay;
    private Bundle mSavedInstanceState;
    private static String mUId, mApiKey;

    /** Request code for Firebase Authentication */
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.app_name));

        mIsTablet = getResources().getBoolean(R.bool.tablet_mode);
        mSavedInstanceState = savedInstanceState;

        // Get the fragment managers
        mMainFragmentManager = getFragmentManager();
        mDetailFragmentManager = getSupportFragmentManager();

        // Enable offline capabilities for the database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Initialize Firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User signed in
                    mUId = user.getUid();

                    // Write the user id in the preferences
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants
                            .KEY_USER_ID, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.KEY_USER_ID, mUId);
                    editor.apply();

                    // Get the food2fork api key
                    long cacheExpiration = 60;

                    mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                    mFirebaseRemoteConfig.fetch(cacheExpiration)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>
                                    () {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // After config data is successfully fetched, it must be activated before newly fetched
                                        // values are returned.
                                        mFirebaseRemoteConfig.activateFetched();
                                        mApiKey = mFirebaseRemoteConfig.getString(Constants.KEY_API_KEY);

                                        // Save the api key in the preferences
                                        SharedPreferences sharedPreferences = getSharedPreferences(Constants
                                                .KEY_USER_ID, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(Constants.KEY_API_KEY, mApiKey);
                                        editor.apply();
                                    }
                                }
                            });

                    if (mIsTablet) {
                        setupForTablet();
                    } else {
                        setupForPhone();
                    }
                } else {
                    // User signed out
                    mUId = null;
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI
                                                            .GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN
                    );
                }
            }
        };
    }

    /** Saves the appropriate fragment as well as the mCurrentFragmentName */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mIsTablet && mUId != null) {
            // Save the appropriate fragment
            switch (mCurrentFragmentName) {
                case Constants.EXTRAS_SHOPPING_LIST:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_SHOPPING_LIST, mShoppingListFragment);
                    break;
                case Constants.EXTRAS_MEAL_PLANNER:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_MEAL_PLANNER, mDaySelectorFragment);
                    break;
                case Constants.EXTRAS_RECIPES:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_RECIPES, mRecipeListFragment);
                    break;
                case Constants.EXTRAS_MEALS:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_MEALS, mMealListFragment);
                    break;
                case Constants.EXTRAS_RECIPE_IDEAS:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_RECIPE_IDEAS, mRecipeAPIListFragment);
                    break;
                case Constants.EXTRAS_SINGLE_RECIPE:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_SINGLE_RECIPE, mSingleRecipeFragment);
                    break;
                case Constants.EXTRAS_SINGLE_MEAL:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_SINGLE_MEAL, mSingleMealFragment);
                    break;
                case Constants.EXTRAS_MEAL_RECIPE:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_MEAL_RECIPE, mMealRecipeFragment);
                    break;
                case Constants.EXTRAS_DAY_MEAL:
                    mDetailFragmentManager.putFragment(outState, Constants.EXTRAS_DAY_MEAL, mDayMealFragment);
                    break;
            }
            // Save the name of the current fragment
            outState.putString(Constants.EXTRAS_CURRENT_FRAGMENT, mCurrentFragmentName);
        }

        if (!mIsTablet && mUId!= null) {
            mMainFragmentManager.putFragment(outState, Constants.KEY_MAIN, mMainFragment);
        }
    }

    /** Called when the sign-in process has been completed */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // The user has signed in
                Toast.makeText(MainActivity.this, R.string.sign_in, Toast.LENGTH_SHORT).show();
                // Update all widgets
                updateWidgets();

            } else if (resultCode == RESULT_CANCELED){
                // The user has cancelled
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Attach the AuthStateListener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        // Get the extras sent from a widget
        if (getIntent() != null) {
            mExtrasFromWidget = getIntent().getStringExtra(Constants.KEY_EXTRAS);
            mCurrentDay = getIntent().getIntExtra(Constants.EXTRAS_MEAL_PLANNER, 0);
        }

        if (mUId != null) {
            if (mIsTablet) {
                setupForTablet();
            } else {
                setupForPhone();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Get the extras sent from a widget
        mCurrentFragmentName = intent.getStringExtra(Constants.KEY_EXTRAS);
        if (mCurrentFragmentName != null && mCurrentFragmentName.equals(Constants.EXTRAS_MEAL_PLANNER)) {
            mCurrentDay = intent.getIntExtra(Constants.EXTRAS_MEAL_PLANNER, 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detach the AuthStateListener
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    /** Called for setting up the interface for a phone */
    private void setupForPhone() {
        if (mSavedInstanceState != null) {
            mMainFragment = (MainFragment) mMainFragmentManager.getFragment(mSavedInstanceState,
                    Constants.KEY_MAIN);
        } else {
            mMainFragment = new MainFragment();
        }
        mMainFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_main, mMainFragment)
                .commit();
    }

    /** Called for setting up the interface for a tablet or large screen */
    private void setupForTablet() {
        // Initialize the ad
        MobileAds.initialize(this, "ca-app-pub-3940256099942544/5224354917");
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if (mSavedInstanceState != null) {
            mMainFragment = (MainFragment) mMainFragmentManager.getFragment(mSavedInstanceState,
                    Constants.KEY_MAIN);
        } else {
            mMainFragment = new MainFragment();
        }

        mMainFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_main, mMainFragment)
                .commit();

        if (mSavedInstanceState != null) {
            // This is a restored instance, restore the appropriate fragment
            restoreFragments();
        }

        if (mExtrasFromWidget != null) {
            // This is an instance called from a widget, display the appropriate fragment
            getFragmentFromExtra(mExtrasFromWidget);
        } else {
            if (mCurrentFragmentName != null) {
                getFragmentFromExtra(mCurrentFragmentName);
            } else {
                // This is a new instance of the Activity, display the meal planner
                getFragmentFromExtra(Constants.EXTRAS_MEAL_PLANNER);
            }
        }
    }

    /** Creates a fragment based on the intent passed to the activity */
    private void getFragmentFromExtra(String extra) {
        // Figure out which button was pressed */
        switch (extra) {
            case Constants.EXTRAS_SHOPPING_LIST:
                if (mSavedInstanceState == null || mShoppingListFragment == null) {
                    // Create a new fragment
                    mShoppingListFragment = ShoppingListFragment.newInstance(mUId);
                }
                // Set the current fragment name
                mCurrentFragmentName = Constants.EXTRAS_SHOPPING_LIST;

                // Set intent and mExtrasFromWidget to null to avoid recreating the same fragment
                setIntent(null);
                mExtrasFromWidget = null;

                // Display the fragment
                mDetailFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mShoppingListFragment)
                        .commit();
                break;
            case Constants.EXTRAS_MEAL_PLANNER:
                if (mSavedInstanceState == null || mDaySelectorFragment == null) {
                    // Create a new fragment
                    mDaySelectorFragment = DaySelectorFragment.newInstance(mUId, mCurrentDay);
                }
                // Set the current fragment name
                mCurrentFragmentName = Constants.EXTRAS_MEAL_PLANNER;

                // Set intent and mExtrasFromWidget to null to avoid recreating the same fragment
                setIntent(null);
                mExtrasFromWidget = null;

                // Display the fragment
                mDetailFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mDaySelectorFragment)
                        .commit();
                break;
            case Constants.EXTRAS_RECIPES:
                if (mSavedInstanceState == null || mRecipeListFragment == null) {
                    // Create a new fragment
                    mRecipeListFragment = RecipeListFragment.newInstance(mUId, false);
                }

                // Set the current fragment name
                mCurrentFragmentName = Constants.EXTRAS_RECIPES;

                // Set intent and mExtrasFromWidget to null to avoid recreating the same fragment
                setIntent(null);
                mExtrasFromWidget = null;

                // Display the fragment
                mDetailFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mRecipeListFragment)
                        .commit();
                break;
            case Constants.EXTRAS_MEALS:
                if (mSavedInstanceState == null || mMealListFragment == null) {
                    // Create a new fragment
                    mMealListFragment = MealListFragment.newInstance(mUId);
                }

                // Set the current fragment name
                mCurrentFragmentName = Constants.EXTRAS_MEALS;

                // Set intent and mExtrasFromWidget to null to avoid recreating the same fragment
                setIntent(null);
                mExtrasFromWidget = null;

                // Display the fragment
                mDetailFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mMealListFragment)
                        .commit();
                break;
            case Constants.EXTRAS_RECIPE_IDEAS:
                if (mSavedInstanceState == null || mRecipeAPIListFragment == null) {
                    // Create a new fragment
                    mRecipeAPIListFragment = RecipeListFragment.newInstance(mUId, true);
                }

                // Set the current fragment name
                mCurrentFragmentName = Constants.EXTRAS_RECIPE_IDEAS;

                // Set intent and mExtrasFromWidget to null to avoid recreating the same fragment
                setIntent(null);
                mExtrasFromWidget = null;

                // Display the fragment
                mDetailFragmentManager.beginTransaction()
                        .replace(R.id.cl_fragment_detail, mRecipeAPIListFragment)
                        .commit();
                break;
        }
    }

    /** Restores a fragment from the savedInstanceState */
    private void restoreFragments() {
        // Get the name of the current fragment
        mCurrentFragmentName = mSavedInstanceState.getString(Constants.EXTRAS_CURRENT_FRAGMENT);

        if (mCurrentFragmentName != null) {
            // Restore the appropriate fragment
            switch (mCurrentFragmentName) {
                case Constants.EXTRAS_SHOPPING_LIST:
                    mShoppingListFragment = (ShoppingListFragment) mDetailFragmentManager.getFragment
                            (mSavedInstanceState, Constants.EXTRAS_SHOPPING_LIST);
                    break;
                case Constants.EXTRAS_MEAL_PLANNER:
                    mDaySelectorFragment = (DaySelectorFragment) mDetailFragmentManager.getFragment(mSavedInstanceState,
                            Constants.EXTRAS_MEAL_PLANNER);
                    break;
                case Constants.EXTRAS_RECIPES:
                    mRecipeListFragment = (RecipeListFragment) mDetailFragmentManager.getFragment(mSavedInstanceState,
                            Constants.EXTRAS_RECIPES);
                    break;
                case Constants.EXTRAS_MEALS:
                    mMealListFragment = (MealListFragment) mDetailFragmentManager.getFragment(mSavedInstanceState,
                            Constants.EXTRAS_MEALS);
                    break;
                case Constants.EXTRAS_RECIPE_IDEAS:
                    mRecipeAPIListFragment = (RecipeListFragment) mDetailFragmentManager.getFragment
                            (mSavedInstanceState,Constants.EXTRAS_RECIPE_IDEAS);
                    break;
                case Constants.EXTRAS_SINGLE_RECIPE:
                    mSingleRecipeFragment = (RecipeFragment) mDetailFragmentManager.getFragment
                            (mSavedInstanceState, Constants.EXTRAS_SINGLE_RECIPE);
                    break;
                case Constants.EXTRAS_SINGLE_MEAL:
                    mSingleMealFragment = (MealFragment) mDetailFragmentManager.getFragment
                            (mSavedInstanceState, Constants.EXTRAS_SINGLE_MEAL);
                    break;
                case Constants.EXTRAS_MEAL_RECIPE:
                    mMealRecipeFragment = (RecipeFragment) mDetailFragmentManager.getFragment
                            (mSavedInstanceState, Constants.EXTRAS_MEAL_RECIPE);
                    break;
                case Constants.EXTRAS_DAY_MEAL:
                    mDayMealFragment = (MealFragment) mDetailFragmentManager.getFragment
                            (mSavedInstanceState, Constants.EXTRAS_DAY_MEAL);
                    break;
            }
        }
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
            mSingleRecipeFragment = RecipeFragment.newInstance(mUId, isNew, recipe, null);
        }

        // Set the current fragment name
        mCurrentFragmentName = Constants.EXTRAS_SINGLE_RECIPE;

        // Display the fragment
        mDetailFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_detail, mSingleRecipeFragment)
                .addToBackStack(null)
                .commit();
    }

    /** Called when a meal from the list has been clicked. Starts a new fragment */
    @Override
    public void onMealClick(boolean isNew, @Nullable Meal meal, @Nullable String day_id,
                            @Nullable String mealTitle, @Nullable String mealTitleName) {
        mSingleMealFragment = MealFragment.newInstance(mUId, isNew, meal, day_id, mealTitle, mealTitleName);

        // Set the current fragment name
        mCurrentFragmentName = Constants.EXTRAS_SINGLE_MEAL;

        // Display the fragment
        mDetailFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_detail, mSingleMealFragment)
                .addToBackStack(null)
                .commit();
    }

    /** Called when a recipe from within a Meal has been clicked. Starts a new Recipe fragment */
    @Override
    public void onMealRecipeClick(Recipe recipe, String meal_id) {
        mMealRecipeFragment = RecipeFragment.newInstance(mUId, false, recipe, meal_id);

        // Set the current fragment name
        mCurrentFragmentName = Constants.EXTRAS_MEAL_RECIPE;

        // Display the fragment
        mDetailFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_detail, mMealRecipeFragment)
                .addToBackStack(null)
                .commit();
    }

    /** Called when a meal from within a Day has been clicked. Starts a new Meal fragment */
    @Override
    public void onDayMealClick(Meal meal, String day_id, String meal_title, String
            meal_title_name) {
        mDayMealFragment = MealFragment.newInstance(mUId, false, meal, day_id, meal_title, meal_title_name);

        // Set the current fragment name
        mCurrentFragmentName = Constants.EXTRAS_DAY_MEAL;

        // Display the fragment
        mDetailFragmentManager.beginTransaction()
                .replace(R.id.cl_fragment_detail, mDayMealFragment)
                .addToBackStack(null)
                .commit();
    }

    /** Called when a navigation button is pressed from the main menu */
    @Override
    public void onNavigationSelected(String extra) {
        if (extra.equals(Constants.EXTRAS_SIGN_OUT)) {
            // Display dialog to ensure that the user wants to sign out
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_title_sign_out_title);
            builder.setMessage(R.string.dialog_title_sign_out);

            // If the user presses "Yes", sign out
            builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AuthUI.getInstance()
                            .signOut(MainActivity.this);
                    dialogInterface.dismiss();
                }
            });

            // If the user presses "Cancel", do not sign out
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            if (mIsTablet) {
                getFragmentFromExtra(extra);
            } else {
                // Create an intent to open the Detail Activity and put an extra to determine which
                // fragment the Detail Activity will use
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(Constants.KEY_EXTRAS, extra);
                intent.putExtra(Constants.KEY_USER_ID, mUId);
                intent.putExtra(Constants.KEY_API_KEY, mApiKey);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Bundle bundle = ActivityOptions
                            .makeSceneTransitionAnimation(this)
                            .toBundle();
                    startActivity(intent, bundle);
                } else {
                    startActivity(intent);
                }
            }
        }
    }

    /** Updates all widgets on sign in */
    private void updateWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        int[] shoppingWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                ShoppingListAppWidget.class));
        int[] dayWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                DayAppWidget.class));
        int[] weekWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WeekAppWidget.class));

        Intent intent = new Intent(this, WidgetUpdateService.class);
        intent.putExtra(Constants.WIDGET_NAME, Constants.WIDGET_ALL);
        intent.putExtra(Constants.WIDGET_SHOPPING_LIST, shoppingWidgetIds);
        intent.putExtra(Constants.WIDGET_SINGLE_DAY, dayWidgetIds);
        intent.putExtra(Constants.WIDGET_WEEK, weekWidgetIds);
        startService(intent);
    }
}
