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

package com.example.android.shopandcook.utilities;

import android.content.ContentValues;
import android.text.TextUtils;

import com.example.android.shopandcook.model.Ingredient;
import com.example.android.shopandcook.model.RecipeFromApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Helper class containing methods for parsing json data from the food2fork api.
 */

public class JSONUtilities {

    /** Keys used to parse the JSON response */
    public static final String KEY_RECIPES = "recipes";
    public static final String KEY_TITLE = "title";
    public static final String KEY_RECIPE_ID = "recipe_id";
    public static final String KEY_IMAGE_URL = "image_url";
    public static final String KEY_SOCIAL_RANK = "social_rank";
    private static final String KEY_RECIPE = "recipe";
    private static final String KEY_INGREDIENTS = "ingredients";
    private static final String KEY_SOURCE_URL = "source_url";

    /** Reads a JSON string and returns a list of recipes in the form of a ContentValues[] */
    public static ContentValues[] readRecipeListJSON(String jsonResponse) throws JSONException {
        if (jsonResponse == null || TextUtils.isEmpty(jsonResponse)) return null;

        // Get the root object
        JSONObject root = new JSONObject(jsonResponse);

        // Get the array with the recipe list
        JSONArray recipes = root.getJSONArray(KEY_RECIPES);

        ContentValues[] parsedRecipes = new ContentValues[recipes.length()];

        // Iterate through all recipes and add them to the array
        for (int i = 0 ; i < recipes.length(); i++) {
            JSONObject currentRecipe = recipes.getJSONObject(i);

            String title = currentRecipe.getString(KEY_TITLE);
            String recipeId = currentRecipe.getString(KEY_RECIPE_ID);
            String imageURL = currentRecipe.getString(KEY_IMAGE_URL);
            Double rank = currentRecipe.getDouble(KEY_SOCIAL_RANK);

            ContentValues currentValue = new ContentValues();
            currentValue.put(KEY_TITLE, title);
            currentValue.put(KEY_RECIPE_ID, recipeId);
            currentValue.put(KEY_IMAGE_URL, imageURL);
            currentValue.put(KEY_SOCIAL_RANK, rank);

            parsedRecipes[i] = currentValue;
        }

        return parsedRecipes;
    }

    /** Reads a JSON string and returns a single RecipeFromApi */
    public static RecipeFromApi readRecipeJSON(String jsonResponse) throws JSONException {
        if (jsonResponse == null || TextUtils.isEmpty(jsonResponse)) return null;

        ArrayList<Ingredient> ingredients = new ArrayList<>();
        JSONObject root = new JSONObject(jsonResponse);

        JSONObject recipe = root.getJSONObject(KEY_RECIPE);

        String title = recipe.getString(KEY_TITLE);
        String sourceUrl = recipe.getString(KEY_SOURCE_URL);
        String imageUrl = recipe.getString(KEY_IMAGE_URL);

        JSONArray ingredientsArray = recipe.getJSONArray(KEY_INGREDIENTS);

        // Iterate through all recipes and add them to the array
        for (int i = 0; i < ingredientsArray.length(); i++) {
            String ingredient = ingredientsArray.getString(i);
            ingredients.add(new Ingredient(ingredient));
        }

        return new RecipeFromApi(title, sourceUrl, imageUrl, ingredients);
    }
}
