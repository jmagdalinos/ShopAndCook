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

package com.example.android.shopandcook.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.shopandcook.data.RecipeContract.Recipes;

/**
 * Recipe Provider for the recipes database
 */

public class RecipeProvider extends ContentProvider {

    /** Member variables */
    private RecipeDBHelper mDbHelper;

    /** Constants for Uri Matcher */
    private static final int CODE_RECIPES = 100;
    private static final int CODE_SINGLE_RECIPE = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /** Builds the Uri Matcher and corresponds the codes to the paths */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RecipeContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, RecipeContract.PATH_RECIPES, CODE_RECIPES);
        uriMatcher.addURI(authority, RecipeContract.PATH_RECIPES + "/#", CODE_SINGLE_RECIPE);
        return uriMatcher;
    }

    /** Initializes the Uri Matcher */
    @Override
    public boolean onCreate() {
        mDbHelper = new RecipeDBHelper(getContext());
        return true;
    }

    /** Handles query requests */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_RECIPES:
                cursor = mDbHelper.getReadableDatabase().query(
                        Recipes.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_SINGLE_RECIPE:
                cursor = mDbHelper.getReadableDatabase().query(
                        Recipes.TABLE_NAME,
                        projection,
                        Recipes.COLUMN_RECIPE_ID + "=?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        // Register to watch a content URI for changes.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /** Returns the MIME type of the data */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_RECIPES:
                return Recipes.CONTENT_LIST_TYPE;
            case CODE_SINGLE_RECIPE:
                return Recipes.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri + " with match " +
                        sUriMatcher.match(uri));
        }
    }

    /** Inserts a single entry into the provider */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id;

        switch (sUriMatcher.match(uri)) {
            case CODE_RECIPES:
                id = mDbHelper.getWritableDatabase().insert(
                        Recipes.TABLE_NAME,
                        null,
                        contentValues);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        if (id > 0) {
            // The operation was successful. Notify all listeners
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    /** Inserts multiple entries into the provider */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] contentValues) {

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_RECIPES:
                database.beginTransaction();

                int rowsInserted = 0;
                try {
                    // Iterate through all content values and insert them one by one
                    for (ContentValues values : contentValues) {
                        long id = database.insert(
                                Recipes.TABLE_NAME,
                                null,
                                values);

                        if (id != -1) rowsInserted++;
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }

                if (rowsInserted > 0) {
                    // One or more rows were inserted. Notify all listeners.
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;
            default:
                return super.bulkInsert(uri, contentValues);
        }
    }

    /** Deletes one or more entries */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[]
            selectionArgs) {
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case CODE_RECIPES:
                rowsDeleted = mDbHelper.getWritableDatabase().delete(
                        Recipes.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_SINGLE_RECIPE:
                long id = ContentUris.parseId(uri);
                selectionArgs = new String[] {String.valueOf(id)};

                rowsDeleted = mDbHelper.getWritableDatabase().delete(
                        Recipes.TABLE_NAME,
                        Recipes.COLUMN_RECIPE_ID + "=?",
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown UriL" + uri);
        }

        if (rowsDeleted != 0) {
            // One or more rows were updated. Notify all listeners.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /** Updates one or more entries */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case CODE_RECIPES:
                rowsUpdated = mDbHelper.getWritableDatabase().update(
                        Recipes.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;
            case CODE_SINGLE_RECIPE:
                // Get the recipe_id from the uri
                long id = ContentUris.parseId(uri);
                selectionArgs = new String[] {String.valueOf(id)};

                rowsUpdated = mDbHelper.getWritableDatabase().update(
                        Recipes.TABLE_NAME,
                        contentValues,
                        Recipes.COLUMN_RECIPE_ID + "=?",
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown UriL" + uri);
        }

        if (rowsUpdated != 0) {
            // One or more rows were updated. Notify all listeners.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
