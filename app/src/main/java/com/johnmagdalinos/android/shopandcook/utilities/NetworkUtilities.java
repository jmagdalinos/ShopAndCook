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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.data.RecipeContract;
import com.johnmagdalinos.android.shopandcook.model.RecipeFromApi;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static android.content.Context.MODE_PRIVATE;

/**
 * Helper class containing methods for retrieving data from the food2fork api and storing it in
 * the app's recipe database.
 */

class NetworkUtilities {

    /** Keys for the API */
    private static final String API_KEY = "dcd3840fa46b8152f438d59cec04be29";
    private static final String BASE_URL = "http://food2fork.com/api";
    private static final String API_PATH_SEARCH = "search";
    private static final String API_PATH_GET = "get";
    private static final String QUERY_API_KEY = "key";
    private static final String GET_API_KEY = "rId";
    private static final String SEARCH_API_KEY = "q";


    /** Retrieves a JSON string from the API and returns a list of recipes */
    public static void searchAPIForRecipes(final Context context, String query) {
        // Create the URL
        URL url = buildUrl(context, false, query);

        // Get the JSON response
        String jsonResults;
        try {
            jsonResults = makeHttpRequest(context, url);
            ContentValues[] recipes = JSONUtilities.readRecipeListJSON(jsonResults);

            if (jsonResults == null || TextUtils.isEmpty(jsonResults)) {
                // Use a Handler to post an error message on the UI thread
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                storeRecipes(context, recipes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** Retrieves a JSON string from the API and returns a single RecipeFromApi */
    public static RecipeFromApi getRecipeFromAPI(Context context, String recipe_id) {
        // Create the URL
        URL url = buildUrl(context, true, recipe_id);

        // Get the JSON response
        String jsonResults;
        try {
            jsonResults = makeHttpRequest(context, url);
            return JSONUtilities.readRecipeJSON(jsonResults);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Builds the url based on the query parameters */
    private static URL buildUrl(Context context, Boolean getRecipe, String query) {
        Uri uri;

        // Get the user id from the preferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.KEY_USER_ID,
                MODE_PRIVATE);
        String apiKey = sharedPreferences.getString(Constants.KEY_API_KEY, null);

        if (getRecipe) {
            // Build the url to get a recipe
            uri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(API_PATH_GET)
                    .appendQueryParameter(QUERY_API_KEY, apiKey)
                    .appendQueryParameter(GET_API_KEY, query)
                    .build();
        } else {
            // Build the url to search for recipes
            uri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(API_PATH_SEARCH)
                    .appendQueryParameter(QUERY_API_KEY, apiKey)
                    .appendQueryParameter(SEARCH_API_KEY,query)
                    .build();
        }

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /** Makes an HTTP request and returns a JSON response */
    private static String makeHttpRequest(final Context context, URL url) throws IOException {
        // Create the http connection
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(15000);

        int responseCode = urlConnection.getResponseCode();
        switch (responseCode) {
            case HttpURLConnection.HTTP_OK:
                break;
            default:
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();

                    }
                });
        }

        // Read the input stream and disconnect
        try {
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            // Use a scanner to read the input stream
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            // Check if there is an input stream
            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /** Stores the parsed recipes in the database */
    private static void storeRecipes(Context context, ContentValues[] values) {
        // Clear the database
        context.getContentResolver().delete(
                RecipeContract.Recipes.CONTENT_URI,
                null,
                null);

        // Add the new recipes to the database
        context.getContentResolver().bulkInsert(
                RecipeContract.Recipes.CONTENT_URI,
                values);
    }
}
