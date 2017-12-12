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

package com.example.android.shopandcook.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.model.Recipe;

import java.util.ArrayList;

/**
 * Adapter used to populate a recycler view with recipes either in the recipes list or inside a meal
 */

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.ViewHolder> {

    /** Member variables */
    private Context mContext;
    public ArrayList<Recipe> mRecipes;
    private RecipeListAdapterCallback mCallback;
    private boolean mIsForMeal;

    public interface RecipeListAdapterCallback {
        void onClicked(Recipe recipe);
    }

    /** Class constructor */
    public RecipeListAdapter(Context context, ArrayList<Recipe> recipes, RecipeListAdapterCallback
            callback, boolean isForMeal) {
        mContext = context;
        mRecipes = recipes;
        mCallback = callback;
        mIsForMeal = isForMeal;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recipe_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the current recipe
        Recipe currentRecipe = mRecipes.get(position);

        // Get the values
        String name = currentRecipe.getName();
        int servings = currentRecipe.getServings();
        long prepTime = currentRecipe.getPrepTime();

        // Convert the prep time to the 00:00 format
        String prepTimeString = Recipe.minToTime(prepTime);
        String category = currentRecipe.getTags();
        String description = currentRecipe.getDescription();

        // Display the values
        if (!mIsForMeal) {
            // Display all attributes
            holder.mNameTextView.setText(name);
            holder.mServingsTextView.setText(String.valueOf(servings));
            holder.mPrepTimeTextView.setText(prepTimeString);
            holder.mTagsTextView.setText(category);
            holder.mDescriptionTextView.setText(description);
        } else {
            holder.mNameForMealTextView.setText(name);
        }
    }

    @Override
    public int getItemCount() {
        if (mRecipes != null) {
            return mRecipes.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /** Swap the current ArrayList with a new one */
    public void swapList(ArrayList<Recipe> newList) {
        mRecipes = newList;
        notifyDataSetChanged();
    }

    /** Custom ViewHolder */
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        /** Views used by the ViewHolder */
        private TextView mNameTextView, mNameForMealTextView, mServingsTextView,
                mPrepTimeTextView, mTagsTextView,
        mDescriptionTextView;
        public View mForegroundView, mBackgroundViewRight, mBackgroundViewLeft;

        public ViewHolder(View itemView) {
            super(itemView);

            mNameTextView = itemView.findViewById(R.id.tv_recipe_list_item_name);
            mNameForMealTextView = itemView.findViewById(R.id
                    .tv_recipe_list_item_name_for_meal);
            mServingsTextView = itemView.findViewById(R.id.tv_recipe_list_item_servings);
            mPrepTimeTextView = itemView.findViewById(R.id.tv_recipe_list_item_prep);
            mTagsTextView = itemView.findViewById(R.id.tv_recipe_list_item_tags);
            mDescriptionTextView = itemView.findViewById(R.id.tv_recipe_list_item_description);
            mForegroundView = itemView.findViewById(R.id.ll_recipe_foreground);
            mBackgroundViewRight = itemView.findViewById(R.id.rl_recipe_background_right);
            mBackgroundViewLeft = itemView.findViewById(R.id.rl_recipe_background_left);

            // Get the custom font and set it on the name
            Typeface mFont = Typeface.createFromAsset(mContext.getAssets(),
                    "fonts/courgette_regular.ttf");
            mNameTextView.setTypeface(mFont);

            itemView.setOnClickListener(this);

            if (mIsForMeal) {
                // This recipe is inside a meal, hide all views except for the name
                mNameTextView.setVisibility(View.GONE);
                LinearLayout servingsLayout = itemView.findViewById(R.id
                        .ll_recipe_list_item_servings);
                LinearLayout prepTimeLayout = itemView.findViewById(R.id
                        .ll_recipe_list_item_prep);
                LinearLayout tagsLayout = itemView.findViewById(R.id
                        .ll_recipe_list_item_tags);
                LinearLayout descriptionLayout = itemView.findViewById(R.id
                        .ll_recipe_list_item_description);
                servingsLayout.setVisibility(View.GONE);
                prepTimeLayout.setVisibility(View.GONE);
                tagsLayout.setVisibility(View.GONE);
                descriptionLayout.setVisibility(View.GONE);

                mNameForMealTextView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            // The item was clicked, open the ingredient editor
            mCallback.onClicked(mRecipes.get(getAdapterPosition()));
        }
    }
}
