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

package com.example.android.shopandcook.ui.appwidgets;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.shopandcook.R;
import com.example.android.shopandcook.model.Ingredient;

import java.util.ArrayList;

/**
 * RemoteViewsService for the Week widget
 */

public class ShoppingListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext());
    }
}

/** RemoteViewsFactory used to populate the list view of the Shopping list widget */
class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    /** Member variables */
    private Context mContext;
    private ArrayList<Ingredient> mShoppingList;

    /** Class constructor */
    public ListRemoteViewsFactory(Context context) {
        mContext = context;
        mShoppingList = ShoppingListAppWidget.mShoppingList;
    }

    @Override
    public void onCreate() {
    }

    /** Gets the shopping list from the widget */
    @Override
    public void onDataSetChanged() {
        mShoppingList = ShoppingListAppWidget.mShoppingList;
    }

    @Override
    public void onDestroy() {

    }

    /** Returns the size of the list */
    @Override
    public int getCount() {
        if (mShoppingList != null) {

            return mShoppingList.size();
        } else {
            return 0;
        }
    }

    /** Display all the shopping list items */
    @Override
    public RemoteViews getViewAt(int position) {
        if (mShoppingList.size() == 0) return null;

        // Get the current ingredient
        Ingredient currentIngredient = mShoppingList.get(position);

        // Get the values
        String name = currentIngredient.getName();
        double quantity = currentIngredient.getQuantity();
        int measure = currentIngredient.getMeasure();
        int isChecked = currentIngredient.getIsChecked();

        // Create a RemoteView using the ingredient_list_item as a template
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout
                .app_widget_shopping_list_item);

        // Display the values
        views.setTextViewText(R.id.tv_widget_shopping_list_item_name, name);
        views.setTextViewText(R.id.tv_widget_shopping_list_item_quantity, Ingredient.getQuantityString(quantity));
        views.setTextViewText(R.id.tv_widget_shopping_list_item_measure, Ingredient.getMeasureString(mContext, measure, quantity));

        // Replace the image view with the correct checkbox state
        if (isChecked == 1) {
            views.setImageViewResource(R.id.iv_widget_shopping_list_item_chkbox, R.drawable.ic_check_box_black_24dp);
        } else {
            views.setImageViewResource(R.id.iv_widget_shopping_list_item_chkbox, R.drawable.ic_check_box_outline_blank_black_24dp);
        }

        // Set an empty fill in intent to enable click functionality for each item
        Intent fillInIntent = new Intent();
        views.setOnClickFillInIntent(R.id.ll_widget_shopping_list_item, fillInIntent);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
