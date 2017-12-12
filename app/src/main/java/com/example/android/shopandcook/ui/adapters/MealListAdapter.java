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
import com.example.android.shopandcook.model.Meal;

import java.util.ArrayList;

/**
 * Adapter used to populate a recycler view with meals
 */

public class MealListAdapter extends RecyclerView.Adapter<MealListAdapter.ViewHolder> {

    /** Member variables */
    private Context mContext;
    public ArrayList<Meal> mMeals;
    private MealListAdapterCallback mCallback;

    public interface MealListAdapterCallback {
        void onClicked(Meal meal);
    }

    /** Class constructor */
    public MealListAdapter(Context context, ArrayList<Meal> meals, MealListAdapterCallback
            callback) {
        mContext = context;
        mMeals = meals;
        mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.meal_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the current meal
        Meal currentMeal = mMeals.get(position);

        // Get the values
        String name = currentMeal.getName();
        String tags = currentMeal.getTags();
        String description = currentMeal.getDescription();

        // Display the values
        holder.mNameTextView.setText(name);
        holder.mTagsTextView.setText(tags);
        holder.mDescriptionTextView.setText(description);
    }

    @Override
    public int getItemCount() {
        if (mMeals != null) {
            return mMeals.size();
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
    public void swapList(ArrayList<Meal> newList) {
        mMeals = newList;
        notifyDataSetChanged();
    }

    /** Custom ViewHolder */
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        /** Views used by the ViewHolder */
        private TextView mNameTextView, mTagsTextView, mDescriptionTextView;
        public View mForegroundView, mBackgroundViewRight, mBackgroundViewLeft;

        public ViewHolder(View itemView) {
            super(itemView);

            mNameTextView = itemView.findViewById(R.id.tv_meal_list_item_name);
            mTagsTextView = itemView.findViewById(R.id.tv_meal_list_item_tags);
            mDescriptionTextView = itemView.findViewById(R.id.tv_meal_list_item_description);
            mForegroundView = itemView.findViewById(R.id.ll_meal_foreground);
            mBackgroundViewRight = itemView.findViewById(R.id.rl_meal_background_right);
            mBackgroundViewLeft = itemView.findViewById(R.id.rl_meal_background_left);
            LinearLayout descriptionLayout = itemView.findViewById(R.id
                    .ll_meal_list_item_description);
            LinearLayout tagsLayout = itemView.findViewById(R.id
                    .ll_meal_list_item_tags);

            // Get the custom font and set it on the name
            Typeface mFont = Typeface.createFromAsset(mContext.getAssets(),
                    "fonts/courgette_regular.ttf");
            mNameTextView.setTypeface(mFont);

            // Show the appropriate views
            mTagsTextView.setVisibility(View.VISIBLE);
            mDescriptionTextView.setVisibility(View.VISIBLE);
            descriptionLayout.setVisibility(View.VISIBLE);
            tagsLayout.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // The item was clicked, open the ingredient editor
            mCallback.onClicked(mMeals.get(getAdapterPosition()));
        }
    }
}
