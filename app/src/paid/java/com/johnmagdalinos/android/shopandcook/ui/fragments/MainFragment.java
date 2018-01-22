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

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;

/**
 * Main Fragment from which the user navigates
 */

public class MainFragment extends Fragment implements View.OnClickListener {
    /** Member variables */
    private MainFragmentCallback mCallback;

    public interface MainFragmentCallback {
        void onNavigationSelected(String extra);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (MainFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MainFragmentCallback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get the views
        FrameLayout shoppingListFrameLayout = rootView.findViewById(R.id
                .fl_shopping_list);
        FrameLayout mealPlannerListFrameLayout = rootView.findViewById(R.id
                .fl_meal_planner);
        FrameLayout recipesFrameLayout = rootView.findViewById(R.id
                .fl_recipes);
        FrameLayout mealsFrameLayout = rootView.findViewById(R.id
                .fl_meals);
        FrameLayout recipeIdeasFrameLayout = rootView.findViewById(R.id
                .fl_recipe_ideas);
        FrameLayout signOutFrameLayout = rootView.findViewById(R.id
                .fl_sign_out);

        // Set the listeners
        shoppingListFrameLayout.setOnClickListener(this);
        mealPlannerListFrameLayout.setOnClickListener(this);
        recipesFrameLayout.setOnClickListener(this);
        mealsFrameLayout.setOnClickListener(this);
        recipeIdeasFrameLayout.setOnClickListener(this);
        signOutFrameLayout.setOnClickListener(this);
        return rootView;
    }

    /** Setup the onClick functionality of the CardViews */
    @Override
    public void onClick(View view) {
        // Send the appropriate extra to the activity to start an intent
        switch (view.getId()) {
            case R.id.fl_shopping_list:
                mCallback.onNavigationSelected(Constants.EXTRAS_SHOPPING_LIST);
                break;
            case R.id.fl_meal_planner:
                mCallback.onNavigationSelected(Constants.EXTRAS_MEAL_PLANNER);
                break;
            case R.id.fl_recipes:
                mCallback.onNavigationSelected(Constants.EXTRAS_RECIPES);
                break;
            case R.id.fl_meals:
                mCallback.onNavigationSelected(Constants.EXTRAS_MEALS);
                break;
            case R.id.fl_recipe_ideas:
                mCallback.onNavigationSelected(Constants.EXTRAS_RECIPE_IDEAS);
                break;
            case R.id.fl_sign_out:
                mCallback.onNavigationSelected(Constants.EXTRAS_SIGN_OUT);
                break;
            default:
                break;
        }
    }
}
