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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.model.Ingredient;

import java.util.ArrayList;

/**
 * Adapter used to populate a recycler view with ingredients from the food2fork api
 */

public class IngredientListAdapter extends RecyclerView.Adapter<IngredientListAdapter.ViewHolder> {

    /** Views used by the ViewHolder */
    private TextView mNameTextView, mQuantityTextView, mMeasureTextView, mNotesTextView;

    /** Member variables */
    private Context mContext;
    private ArrayList<Ingredient> mIngredients;

    /** Class constructor */
    public IngredientListAdapter(Context context, ArrayList<Ingredient> ingredients) {
        mContext = context;
        mIngredients = ingredients;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ingredient_list_item, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Ingredient currentIngredient = mIngredients.get(position);
        String name = currentIngredient.getName();
        double quantity = currentIngredient.getQuantity();
        int measure = currentIngredient.getMeasure();
        String notes = currentIngredient.getComments();

        mNameTextView.setText(name);
        if (notes != null) {
            mQuantityTextView.setVisibility(View.VISIBLE);
            mMeasureTextView.setVisibility(View.VISIBLE);
            mNotesTextView.setVisibility(View.VISIBLE);

            // Setup the number of decimals for the quantity
            mQuantityTextView.setText(String.valueOf(Ingredient.getQuantityString(quantity)));
            mMeasureTextView.setText(String.valueOf(measure));
            mNotesTextView.setText(notes);
        } else {
            mQuantityTextView.setVisibility(View.GONE);
            mMeasureTextView.setVisibility(View.GONE);
            mNotesTextView.setVisibility(View.GONE);
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
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /** Custom ViewHolder */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);

            // Get all the views
            mNameTextView = itemView.findViewById(R.id.tv_ingredient_list_item_name);
            mQuantityTextView = itemView.findViewById(R.id.tv_ingredient_list_item_quantity);
            mMeasureTextView = itemView.findViewById(R.id.tv_ingredient_list_item_measure);
            mNotesTextView = itemView.findViewById(R.id.tv_ingredient_list_item_notes);
        }
    }
}
