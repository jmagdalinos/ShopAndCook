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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.johnmagdalinos.android.shopandcook.data.RecipeContract.Recipes;

/**
 * SQLiteOpenHelper for the recipes database
 */

final class RecipeDBHelper extends SQLiteOpenHelper {
    /** Database name */
    private static final String DATABASE_NAME = "recipes.db";

    /** Database version */
    private static final int DATABASE_VERSION = 1;

    /** Class constructor */
    public RecipeDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Called when the database is created */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_RECIPES_TABLE = "CREATE TABLE " + Recipes.TABLE_NAME + " (" +
                Recipes.COLUMN_RECIPE_ID + " TEXT PRIMARY KEY, " +
                Recipes.COLUMN_TITLE + " TEXT, " +
                Recipes.COLUMN_IMAGE_URL + " TEXT, " +
                Recipes.COLUMN_RANK + " REAL);";

        sqLiteDatabase.execSQL(SQL_CREATE_RECIPES_TABLE);
    }

    /** Called when the database is updated */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion == oldVersion + 1) {
            sqLiteDatabase.execSQL("ALTER TABLE " + Recipes.TABLE_NAME + " ADD COLUMN source_url " +
                    "TEXT");
        }
    }
}
