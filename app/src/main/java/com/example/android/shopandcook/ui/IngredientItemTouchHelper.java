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

package com.example.android.shopandcook.ui;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.example.android.shopandcook.ui.adapters.ShoppingListAdapter;

/**
 * ItemTouchHelper used to enable swipe deletion of ingredients and shopping list items
 */

public class IngredientItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    /** Member variables */
    private RecyclerItemTouchHelperListener mListener;

    /** Creates a Callback for the given drag and swipe allowance. */
    public IngredientItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        mListener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mListener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    /** Detects whenever there is a UI change on the view and keeps the background in a static
     * position*/
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((ShoppingListAdapter.ViewHolder) viewHolder)
                    .mForegroundView;

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ShoppingListAdapter.ViewHolder) viewHolder).mForegroundView;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((ShoppingListAdapter.ViewHolder) viewHolder).mForegroundView;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ShoppingListAdapter.ViewHolder) viewHolder).mForegroundView;
        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);

        // Get the direction of the swipe and hide/show appropriate background
        View backgroundLeft = ((ShoppingListAdapter.ViewHolder) viewHolder).mBackgroundViewLeft;
        View backgroundRight = ((ShoppingListAdapter.ViewHolder) viewHolder).mBackgroundViewRight;

        if (dX > 0) {
            // Right Swipe
            backgroundLeft.setVisibility(View.VISIBLE);
            backgroundRight.setVisibility(View.GONE);
        } else {
            // Left Swipe
            backgroundLeft.setVisibility(View.GONE);
            backgroundRight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }


    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
