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

package com.johnmagdalinos.android.shopandcook.utilities;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * IntentService called to search for a keyword in the food2fork api.
 */

public class RecipeIntentService extends IntentService {
    /** Keys for distinguishing between searching for recipes and getting a single recipe */
    public static final String KEY_EXTRA = "extra";
    public static final String KEY_SEARCH = "search";
    public static final String KEY_SEARCH_QUERY = "search_query";

    /** Class constructor */
    public RecipeIntentService() {
        super("RecipeIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getStringExtra(KEY_EXTRA).equals(KEY_SEARCH)) {
            String searchQuery = intent.getStringExtra(KEY_SEARCH_QUERY);
            NetworkUtilities.searchAPIForRecipes(getApplicationContext(), searchQuery);
        }
    }
}
