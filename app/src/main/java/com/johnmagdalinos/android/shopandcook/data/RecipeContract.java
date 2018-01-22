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

package com.johnmagdalinos.android.shopandcook.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.johnmagdalinos.android.shopandcook.utilities.JSONUtilities;

/**
 * Contract for the recipes table
 */

public class RecipeContract {
    /** App's content authority */
    public static final String CONTENT_AUTHORITY = "com.example.android.shopandcook";

    /** Base content Uri to enable contact with the content provider */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" +  CONTENT_AUTHORITY);

    /** Possible Paths */
    public static final String PATH_RECIPES = "recipes";

    /** Inner class defining the columns of the table */
    public static final class Recipes implements BaseColumns {

        /** Content uri used to query the table */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RECIPES)
                .build();

        /** MIME types */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY;

        /** Table and column names */
        public static final String TABLE_NAME = JSONUtilities.KEY_RECIPES;
        public static final String COLUMN_TITLE = JSONUtilities.KEY_TITLE;
        public static final String COLUMN_RANK = JSONUtilities.KEY_SOCIAL_RANK;
        public static final String COLUMN_RECIPE_ID = JSONUtilities.KEY_RECIPE_ID;
        public static final String COLUMN_IMAGE_URL = JSONUtilities.KEY_IMAGE_URL;
    }
}
