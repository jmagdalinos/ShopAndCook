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
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.data.RecipeContract.Recipes;
import com.example.android.shopandcook.model.RecipeFromApi;
import com.squareup.picasso.Picasso;

/**
 * Adapter used to populate a recycler view with recipes from the food2fork api
 */

public class RecipeListFromApiAdapter extends RecyclerView.Adapter<RecipeListFromApiAdapter.RecipeViewHolder> {
    /** Views used by the ViewHolder */
    private ImageView mImageView;
    private TextView mTitleTextView, mRankTextView;
    private FrameLayout mNoThumbnailFrameLayout;

    /** Member variables */
    private Context mContext;
    private Cursor mCursor;
    private RecyclerViewCallback mClickHandler;

    /** Interface enabling click functionality */
    public interface RecyclerViewCallback {
        void onItemClick(String recipe_id);
    }

    /** Class constructor */
    public RecipeListFromApiAdapter(Context context, RecyclerViewCallback clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /** Create an instance of a RecipeViewHolder */
    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recipe_from_api_list_item, parent, false);

        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        // Get the column indexes
        int titleColumnIndex = mCursor.getColumnIndex(Recipes.COLUMN_TITLE);
        int rankColumnIndex = mCursor.getColumnIndex(Recipes.COLUMN_RANK);
        int imageColumnIndex = mCursor.getColumnIndex(Recipes.COLUMN_IMAGE_URL);

        mCursor.moveToPosition(position);

        // Get the values
        String imageUrl = mCursor.getString(imageColumnIndex);
        String title = mCursor.getString(titleColumnIndex);
        int rating = (int) Math.ceil(mCursor.getDouble(rankColumnIndex));

        // Set the text in the Text Views
        mTitleTextView.setText(Html.fromHtml(Html.fromHtml(title).toString()));
        mRankTextView.setText(String.valueOf(rating));

        // Set the color on the rating background
        GradientDrawable circle = (GradientDrawable) mRankTextView.getBackground();
        int color = RecipeFromApi.getColorRating(rating, mContext);
        circle.setColor(color);

        if (imageUrl == null || TextUtils.isEmpty(imageUrl)) {
            mImageView.setVisibility(View.GONE);
            mNoThumbnailFrameLayout.setVisibility(View.VISIBLE);
        } else {
            mNoThumbnailFrameLayout.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);

            Picasso.with(mContext).load(imageUrl).into(mImageView);
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /** Gets the cursor from a fragment */
    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    /** Custom View Holder */
    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public RecipeViewHolder(View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.iv_recipe_list_item_image);
            mTitleTextView = itemView.findViewById(R.id.tv_recipe_list_item_title);
            mRankTextView = itemView.findViewById(R.id.tv_recipe_list_item_rank);
            mNoThumbnailFrameLayout = itemView.findViewById(R.id.fl_no_thumbnail);

            // Get the custom font and set it on the name
            Typeface mFont = Typeface.createFromAsset(mContext.getAssets(),
                    "fonts/courgette_regular.ttf");
            mTitleTextView.setTypeface(mFont);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mCursor.moveToPosition(getAdapterPosition());
            int recipeIdColumnIndex = mCursor.getColumnIndex(Recipes.COLUMN_RECIPE_ID);

            // Pass the recipe to the callback
            mClickHandler.onItemClick(mCursor.getString(recipeIdColumnIndex));
        }
    }
}
