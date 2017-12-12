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

package com.example.android.shopandcook.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Comparator;

/**
 * Recipe object representing a single recipe. Can be associated with a meal.
 */

public class Recipe implements Parcelable {
    /** Member variables */
    private String mName;
    private String mDescription;
    private String mTags;
    private int mServings;
    private long mPrepTime;
    private String mRecipeId;

    /** Recipe constructor for Firebase Realtime Database */
    public Recipe() {}

    /** Recipe constructor using a parcel */
    protected Recipe(Parcel parcel) {
        mName = parcel.readString();
        mDescription = parcel.readString();
        mTags = parcel.readString();
        mServings = parcel.readInt();
        mPrepTime = parcel.readLong();
        mRecipeId = parcel.readString();
    }

    /** Getter methods */
    public String getName() {return mName;}
    public String getDescription() {return mDescription;}
    public String getTags() {return mTags;}
    public int getServings() {return mServings;}
    public long getPrepTime() {return mPrepTime;}
    public String getRecipeId() {return mRecipeId;}

    /** Setter methods */
    public void setName(String name) {this.mName = name;}
    public void setDescription(String description) {this.mDescription = description;}
    public void setTags(String tags) {this.mTags = tags;}
    public void setServings(int servings) {this.mServings = servings;}
    public void setPrepTime(long preparationTime) {this.mPrepTime = preparationTime;}
    public void setRecipeId(String recipeId) {this.mRecipeId = recipeId;}

    /** Required Creator for a Parcelable */
    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /** Write all variables to the parcel */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mDescription);
        parcel.writeString(mTags);
        parcel.writeInt(mServings);
        parcel.writeLong(mPrepTime);
        parcel.writeString(mRecipeId);
    }

    /** Converts a long with minutes to a string of format 00:00 */
    public static String minToTime(long time) {
        int hours = (int) time / 60;
        int minutes = (int) time % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    /** Comparator sorting Recipes by Name Ascending */
    public static Comparator<Recipe> AscendingNameComparator = new Comparator<Recipe>() {
        @Override
        public int compare(Recipe recipe1, Recipe recipe2) {
            String name1, name2;
            if (recipe1 == null || TextUtils.isEmpty(recipe1.getName())) {
                name1 = "";
            } else {
                name1 = recipe1.getName().toUpperCase();
            }

            if (recipe2 == null || TextUtils.isEmpty(recipe2.getName())) {
                name2 = "";
            } else {
                name2 = recipe2.getName().toUpperCase();
            }

            return name1.compareTo(name2);
        }
    };

    /** Comparator sorting Recipes by Name Descending */
    public static Comparator<Recipe> DescendingNameComparator = new Comparator<Recipe>() {
        @Override
        public int compare(Recipe recipe1, Recipe recipe2) {
            String name1, name2;
            if (recipe1 == null || TextUtils.isEmpty(recipe1.getName())) {
                name1 = "";
            } else {
                name1 = recipe1.getName().toUpperCase();
            }

            if (recipe2 == null || TextUtils.isEmpty(recipe2.getName())) {
                name2 = "";
            } else {
                name2 = recipe2.getName().toUpperCase();
            }

            return name2.compareTo(name1);
        }
    };

    /** Comparator sorting Recipes by Tags Ascending */
    public static Comparator<Recipe> AscendingTagComparator = new Comparator<Recipe>() {
        @Override
        public int compare(Recipe recipe1, Recipe recipe2) {
            String name1, name2;
            if (recipe1 == null || TextUtils.isEmpty(recipe1.getTags())) {
                name1 = "";
            } else {
                name1 = recipe1.getTags().toUpperCase();
            }

            if (recipe2 == null || TextUtils.isEmpty(recipe2.getTags())) {
                name2 = "";
            } else {
                name2 = recipe2.getTags().toUpperCase();
            }

            return name1.compareTo(name2);
        }
    };

    /** Comparator sorting Recipes by Tags Descending */
    public static Comparator<Recipe> DescendingTagComparator = new Comparator<Recipe>() {
        @Override
        public int compare(Recipe recipe1, Recipe recipe2) {
            String name1, name2;
            if (recipe1 == null || TextUtils.isEmpty(recipe1.getTags())) {
                name1 = "";
            } else {
                name1 = recipe1.getTags().toUpperCase();
            }

            if (recipe2 == null || TextUtils.isEmpty(recipe2.getTags())) {
                name2 = "";
            } else {
                name2 = recipe2.getTags().toUpperCase();
            }

            return name2.compareTo(name1);
        }
    };
}
