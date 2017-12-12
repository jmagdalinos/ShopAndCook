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

import android.content.Context;
import android.os.AsyncTask;

import com.example.android.shopandcook.model.RecipeFromApi;

/**
 * AsyncTask used to retrieve a single recipe from the food2fork API.
 */

public class RecipeAsyncTask extends AsyncTask<String, Void, RecipeFromApi> {

    /** Member variables */
    private RecipeAsyncTaskCallback mCallback;
    private Context mContext;

    /** Callback passing the recipe to an activity of fragment */
    public interface RecipeAsyncTaskCallback {
        void onAsyncTaskCompleted(RecipeFromApi recipeFromApi);
    }

    /** Public constructor */
    public RecipeAsyncTask setAsyncTaskListener(Context context, RecipeAsyncTaskCallback
            callback) {
        mCallback = callback;
        mContext = context;
        return this;
    }

    /** Get the recipe from the API */
    @Override
    protected RecipeFromApi doInBackground(String... strings) {
        return NetworkUtilities.getRecipeFromAPI(mContext, strings[0]);
    }

    /** Pass the recipeFromApi to the fragment */
    @Override
    protected void onPostExecute(RecipeFromApi recipeFromApi) {
        mCallback.onAsyncTaskCompleted(recipeFromApi);
    }
}
