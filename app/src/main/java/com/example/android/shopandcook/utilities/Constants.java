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

/**
 * Helper class containing all the keys and nodes for the app
 */

public class Constants {

    /** Extras for starting an intent to the Detail Activity */
    public static final String KEY_EXTRAS = "extra";
    public static final String KEY_DAY_ID = "day_id";
    public static final String EXTRAS_SHOPPING_LIST = "shopping_list";
    public static final String EXTRAS_MEAL_PLANNER = "meal_planner";
    public static final String EXTRAS_RECIPES = "recipes";
    public static final String EXTRAS_MEALS = "meals";
    public static final String EXTRAS_RECIPE_IDEAS = "recipe_ideas";
    public static final String EXTRAS_SIGN_OUT = "sign_out";
    public static final String EXTRAS_CURRENT_FRAGMENT = "current_fragment";
    public static final String EXTRAS_SINGLE_MEAL = "single_meal";
    public static final String EXTRAS_SINGLE_RECIPE = "single_recipe";
    public static final String EXTRAS_MEAL_RECIPE = "meal_recipe";
    public static final String EXTRAS_DAY_MEAL = "day_meal";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_API_KEY = "api_key";
    public static final String KEY_MAIN = "main";
    public static final String KEY_SCROLL_POSITION = "scroll_position";

    /** Extras for distinguishing widgets */
    public static final String WIDGET_NAME = "widget_name";
    public static final String WIDGET_SHOPPING_LIST = "shopping_list";
    public static final String WIDGET_SINGLE_DAY = "single_day";
    public static final String WIDGET_WEEK = "week";
    public static final String WIDGET_ALL = "all";

    /** Node keys for Firebase Realtime Database */
    public static final String NODE_SHOPPING_LIST = "shopping_list";
    public static final String NODE_MEALS = "meals";
    public static final String NODE_MEAL_RECIPES = "meal_recipes";
    public static final String NODE_RECIPE_MEALS = "recipe_meals";
    public static final String NODE_RECIPES = "recipes";
    public static final String NODE_DAYS = "days";
    public static final String NODE_DAY_OF_THE_WEEK = "dayOfTheWeek";
    public static final String NODE_BREAKFAST = "breakfast";
    public static final String NODE_MORNING_SNACK = "morningSnack";
    public static final String NODE_LUNCH = "lunch";
    public static final String NODE_AFTERNOON_SNACK = "afternoonSnack";
    public static final String NODE_DINNER = "dinner";
    public static final String NODE_BREAKFAST_NAME = "breakfastName";
    public static final String NODE_MORNING_SNACK_NAME = "morningSnackName";
    public static final String NODE_LUNCH_NAME = "lunchName";
    public static final String NODE_AFTERNOON_SNACK_NAME = "afternoonSnackName";
    public static final String NODE_DINNER_NAME = "dinnerName";
    public static final String NODE_MEAL_DAYS = "meal_days";
    public static final String NODE_INGREDIENTS = "ingredients";
    public static final String NODE_IS_CHECKED = "isChecked";
    public static final String NODE_PREP_TIME = "prepTime";
}
