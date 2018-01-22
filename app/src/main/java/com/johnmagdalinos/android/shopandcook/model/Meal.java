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

package com.johnmagdalinos.android.shopandcook.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Comparator;

/**
 * Meal object representing a collection of recipes. Can be associated with a meal.
 */

public class Meal implements Parcelable {
    /** Member variables */
    private String mName;
    private String mDescription;
    private String mTags;
    private int mServings;
    private String mMealId;

    /** Meal constructor for Firebase Realtime Database */
    public Meal() {}

    /** Meal constructor using a parcel */
    protected Meal(Parcel parcel) {
        mName = parcel.readString();
        mDescription = parcel.readString();
        mTags = parcel.readString();
        mServings = parcel.readInt();
        mMealId = parcel.readString();
    }

    /** Getter methods */
    public String getName() {return mName;}
    public String getDescription() {return mDescription;}
    public String getTags() {return mTags;}
    public int getServings() {return mServings;}
    public String getMealId() {return mMealId;}

    /** Setter methods */
    public void setName(String name) {this.mName = name;}
    public void setDescription(String description) {this.mDescription = description;}
    public void setTags(String tags) {this.mTags = tags;}
    public void setServings(int servings) {this.mServings = servings;}
    public void setMealId(String mealId) {this.mMealId = mealId;}

    /** Required Creator for a Parcelable */
    public static final Creator<Meal> CREATOR = new Creator<Meal>() {
        @Override
        public Meal createFromParcel(Parcel in) {
            return new Meal(in);
        }

        @Override
        public Meal[] newArray(int size) {
            return new Meal[size];
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
        parcel.writeString(mMealId);
    }

    /** Comparator sorting Meals by Name Ascending */
    public static Comparator<Meal> AscendingNameComparator = new Comparator<Meal>() {
        @Override
        public int compare(Meal meal1, Meal meal2) {
            String name1, name2;
            if (meal1 == null || TextUtils.isEmpty(meal1.getName())) {
                name1 = "";
            } else {
                name1 = meal1.getName().toUpperCase();
            }

            if (meal2 == null || TextUtils.isEmpty(meal2.getName())) {
                name2 = "";
            } else {
                name2 = meal2.getName().toUpperCase();
            }

            return name1.compareTo(name2);
        }
    };

    /** Comparator sorting Meals by Name Descending */
    public static Comparator<Meal> DescendingNameComparator = new Comparator<Meal>() {
        @Override
        public int compare(Meal meal1, Meal meal2) {
            String name1, name2;
            if (meal1 == null || TextUtils.isEmpty(meal1.getName())) {
                name1 = "";
            } else {
                name1 = meal1.getName().toUpperCase();
            }

            if (meal2 == null || TextUtils.isEmpty(meal2.getName())) {
                name2 = "";
            } else {
                name2 = meal2.getName().toUpperCase();
            }

            return name2.compareTo(name1);
        }
    };

    /** Comparator sorting Meals by Tags Ascending */
    public static Comparator<Meal> AscendingTagComparator = new Comparator<Meal>() {
        @Override
        public int compare(Meal meal1, Meal meal2) {
            String name1, name2;
            if (meal1 == null || TextUtils.isEmpty(meal1.getTags())) {
                name1 = "";
            } else {
                name1 = meal1.getTags().toUpperCase();
            }

            if (meal2 == null || TextUtils.isEmpty(meal2.getTags())) {
                name2 = "";
            } else {
                name2 = meal2.getTags().toUpperCase();
            }

            return name1.compareTo(name2);
        }
    };

    /** Comparator sorting Meals by Tags Descending */
    public static Comparator<Meal> DescendingTagComparator = new Comparator<Meal>() {
        @Override
        public int compare(Meal meal1, Meal meal2) {
            String name1, name2;
            if (meal1 == null || TextUtils.isEmpty(meal1.getTags())) {
                name1 = "";
            } else {
                name1 = meal1.getTags().toUpperCase();
            }

            if (meal2 == null || TextUtils.isEmpty(meal2.getTags())) {
                name2 = "";
            } else {
                name2 = meal2.getTags().toUpperCase();
            }

            return name2.compareTo(name1);
        }
    };
}
