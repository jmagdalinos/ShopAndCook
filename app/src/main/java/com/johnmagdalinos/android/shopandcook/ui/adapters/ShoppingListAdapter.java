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

package com.johnmagdalinos.android.shopandcook.ui.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.model.Ingredient;

import java.util.ArrayList;

/**
 * Adapter used to populate a recycler view with either ingredients or shopping list items
 */

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    /** Member variables */
    private Context mContext;
    public ArrayList<Ingredient> mIngredients;
    private boolean mShowColors, mIsShoppingList;
    private ShoppingListAdapterCallback mCallback;

    /** Class constructor */
    public ShoppingListAdapter(Context context, ArrayList<Ingredient> ingredients, Boolean
            showColors, boolean isShoppingList, ShoppingListAdapterCallback callback) {
        mContext = context;
        mIngredients = ingredients;
        mShowColors = showColors;
        mIsShoppingList = isShoppingList;
        mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (mIsShoppingList) {
            // Get the view for a shopping list item
            view = LayoutInflater.from(mContext).inflate(R.layout.shopping_list_item, parent, false);
        } else {
            // Get the view for an ingredient
            view = LayoutInflater.from(mContext).inflate(R.layout.ingredient_list_item,
                    parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the current ingredient
        Ingredient currentIngredient = mIngredients.get(position);

        // Get the values
        String name = currentIngredient.getName();
        double quantity = currentIngredient.getQuantity();
        int color = currentIngredient.getColor();
        int measure = currentIngredient.getMeasure();
        int isChecked = currentIngredient.getIsChecked();

        // Display the values
        holder.mNameTextView.setText(name);

        // Setup the number of decimals for the quantity
        holder.mQuantityTextView.setText(Ingredient.getQuantityString(quantity));

        // Get the measure
        holder.mMeasureTextView.setText(Ingredient.getMeasureString(mContext, measure, quantity));

        if (mIsShoppingList) {
            // This is used for a shopping list - Set the condition of the checkbox
            holder.toggleCheckedState(isChecked);

            // Set the color for the ingredient type
            GradientDrawable colorType = (GradientDrawable) holder.mColorImageView.getBackground();
            if (!TextUtils.isEmpty(String.valueOf(color))) {
                colorType.setColor(Ingredient.getBackgroundColor(mContext, color));
            }
            if (mShowColors) {
                holder.mColorImageView.setVisibility(View.VISIBLE);
            } else {
                holder.mColorImageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mIngredients != null) {
            return mIngredients.size();
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
    public void swapList(ArrayList<Ingredient> newList) {
        mIngredients = newList;
        notifyDataSetChanged();
    }

    /** Custom ViewHolder */
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        /** Views used by the ViewHolder */
        private CheckBox mCheckBox;
        private TextView mNameTextView, mQuantityTextView, mMeasureTextView, mNotesTextView;
        private ImageView mColorImageView;
        public View mForegroundView, mBackgroundViewRight, mBackgroundViewLeft;

        public ViewHolder(View itemView) {
            super(itemView);
            if (mIsShoppingList) {
                // Get the views for a shopping list item
                mCheckBox = itemView.findViewById(R.id.chk_shopping_list);
                mNameTextView = itemView.findViewById(R.id.tv_shopping_list_item_name);
                mQuantityTextView = itemView.findViewById(R.id.tv_shopping_list_item_quantity);
                mMeasureTextView = itemView.findViewById(R.id.tv_shopping_list_item_measure);
                mColorImageView = itemView.findViewById(R.id.iv_shopping_list_color);
                mForegroundView = itemView.findViewById(R.id.ll_shopping_foreground);
                mBackgroundViewRight = itemView.findViewById(R.id.rl_shopping_background_right);
                mBackgroundViewLeft = itemView.findViewById(R.id.rl_shopping_background_left);

                mCheckBox.setOnClickListener(this);
                mColorImageView.setOnClickListener(this);
            } else {
                // Get the views for an ingredient
                mNameTextView = itemView.findViewById(R.id.tv_ingredient_list_item_name);
                mQuantityTextView = itemView.findViewById(R.id.tv_ingredient_list_item_quantity);
                mMeasureTextView = itemView.findViewById(R.id.tv_ingredient_list_item_measure);
                mForegroundView = itemView.findViewById(R.id.ll_ingredient_foreground);
                mBackgroundViewRight = itemView.findViewById(R.id.rl_ingredient_background_right);
                mBackgroundViewLeft = itemView.findViewById(R.id.rl_ingredient_background_left);
            }
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == mCheckBox) {
                int value;
                // Get the correct value for the isChecked property
                if (mCheckBox.isChecked()) {
                    value = Ingredient.KEY_CHECKED;
                } else {
                    value = Ingredient.KEY_UNCHECKED;
                }
                // Send the value to the callback
                mCallback.onChecked(mIngredients.get(getAdapterPosition()).getIngredientId(), value);
                toggleCheckedState(value);
            } else {
                // The item was clicked, open the ingredient editor
                mCallback.onClicked(mIngredients.get(getAdapterPosition()));
            }
        }

        /** Toggles a strike through effect on the text */
        public void toggleCheckedState(int isChecked) {
            if (isChecked == Ingredient.KEY_CHECKED) {
                // The checkbox is checked, set the strike-through on the text
                mCheckBox.setChecked(true);
                mNameTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                mQuantityTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                mMeasureTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                // The checkbox is unchecked, remove the strike-through from the text
                mCheckBox.setChecked(false);
                mNameTextView.setPaintFlags(0);
                mQuantityTextView.setPaintFlags(0);
                mMeasureTextView.setPaintFlags(0);
            }
        }
    }

    /** Pass the data to the callback */
    public interface ShoppingListAdapterCallback {
        void onChecked(String ingredient_id, int value);
        void onClicked(Ingredient ingredient);
    }
}
