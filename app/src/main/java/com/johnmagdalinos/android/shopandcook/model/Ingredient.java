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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;

import com.johnmagdalinos.android.shopandcook.R;

import java.util.Comparator;

/**
 * Ingredient object representing either an ingredient in a recipe or a shopping list item.
 */

public class Ingredient implements Parcelable {
    /** Member variables */
    private String mName;
    private String mComments;
    private int mMeasure;
    private double mQuantity;
    private int mColor;
    private int mIsChecked;
    private String mIngredientId;
    private String mDayMealId;

    /** Keys for the checkbox */
    public static final int KEY_UNCHECKED = 0;
    public static final int KEY_CHECKED = 1;

    /** Ingredient constructor for Firebase Realtime Database */
    public Ingredient() {}

    /** Ingredient constructor using only names (used for ingredients from the API */
    public Ingredient(String name) {
        mName = name;
    }

    /** Ingredient constructor using a parcel */
    protected Ingredient(Parcel parcel) {
        mName = parcel.readString();
        mComments = parcel.readString();
        mMeasure = parcel.readInt();
        mQuantity = parcel.readDouble();
        mColor = parcel.readInt();
        mIsChecked = parcel.readInt();
        mIngredientId = parcel.readString();
        mDayMealId = parcel.readString();
    }

    /** Getter methods */
    public String getName() {return mName;}
    public String getComments() {return mComments;}
    public int getMeasure() {return mMeasure;}
    public double getQuantity() {return mQuantity;}
    public int getColor() {return mColor;}
    public int getIsChecked() {return mIsChecked;}
    public String getIngredientId() {return mIngredientId;}
    public String getDayMealId() {return mDayMealId;}


    /** Setter methods */
    public void setName(String name) {
        this.mName = name;
    }
    public void setComments(String comments) {
        this.mComments = comments;
    }
    public void setMeasure(int measure) {
        this.mMeasure = measure;
    }
    public void setQuantity(double quantity) {
        this.mQuantity = quantity;
    }
    public void setColor(int color) {this.mColor = color;}
    public void setIsChecked(int isChecked) { this.mIsChecked = isChecked;}
    public void setIngredientId(String ingredientId) {this.mIngredientId = ingredientId;}
    public void setDayMealId(String dayMealId) {this.mDayMealId = dayMealId;}

    /** Required Creator for a Parcelable */
    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
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
        parcel.writeString(mComments);
        parcel.writeInt(mMeasure);
        parcel.writeDouble(mQuantity);
        parcel.writeInt(mColor);
        parcel.writeInt(mIsChecked);
        parcel.writeString(mIngredientId);
        parcel.writeString(mDayMealId);
    }

    /** Returns the color of the background based on the stored color int */
    public static int getBackgroundColor(Context context, int color) {
        switch (color) {
            case 0:
                return ContextCompat.getColor(context, R.color.ingredientType0);
            case 1:
                return ContextCompat.getColor(context, R.color.ingredientType1);
            case 2:
                return ContextCompat.getColor(context, R.color.ingredientType2);
            case 3:
                return ContextCompat.getColor(context, R.color.ingredientType3);
            case 4:
                return ContextCompat.getColor(context, R.color.ingredientType4);
            case 5:
                return ContextCompat.getColor(context, R.color.ingredientType5);
            default:
                return ContextCompat.getColor(context, R.color.ingredientType0);
        }
    }

    /** Returns the quantity in String format eliminating all unnecessary decimals */
    public static String getQuantityString(double quantity) {
        // Round the quantity to 2 decimals
        double quantityRounded = Math.round((quantity * 100)) / 100.0;
        int intPart = (int) quantityRounded;
        double decimals = quantityRounded - intPart;

        if (decimals == 0) {
            return String.valueOf(intPart);
        } else {
            return String.valueOf(quantityRounded);
        }
    }

    /** Returns a measure in string format */
    public static String getMeasureString(Context context, int measure, double quantity) {
        String[] measuresSingular = context.getResources().getStringArray(R.array
                .measure_spinner_singular);
        String[] measuresPlural = context.getResources().getStringArray(R.array
                .measure_spinner_plural);

        if (quantity > 1) {
            // Use the plural of the measure
            return measuresPlural[measure];
        } else {
            // Use the singular of the measure
            return measuresSingular[measure];
        }
    }

    /** Comparator sorting Ingredients by Name Ascending */
    public static Comparator<Ingredient> AscendingNameComparator = new Comparator<Ingredient>() {
        @Override
        public int compare(Ingredient ingredient1, Ingredient ingredient2) {
            String name1 = ingredient1.getName().toUpperCase();
            String name2 = ingredient2.getName().toUpperCase();
            return name1.compareTo(name2);
        }
    };

    /** Comparator sorting Ingredients by Name Descending */
    public static Comparator<Ingredient> DescendingNameComparator = new Comparator<Ingredient>() {
        @Override
        public int compare(Ingredient ingredient1, Ingredient ingredient2) {
            String name1 = ingredient1.getName().toUpperCase();
            String name2 = ingredient2.getName().toUpperCase();
            return name2.compareTo(name1);
        }
    };

    /** Comparator sorting Ingredients by Color Ascending */
    public static Comparator<Ingredient> AscendingColorComparator = new Comparator<Ingredient>() {
        @Override
        public int compare(Ingredient ingredient1, Ingredient ingredient2) {
            int color1 = ingredient1.getColor();
            int color2 = ingredient2.getColor();
            return color1 - color2;
        }
    };

    /** Comparator sorting Ingredients by Color Descending */
    public static Comparator<Ingredient> DescendingColorComparator = new Comparator<Ingredient>() {
        @Override
        public int compare(Ingredient ingredient1, Ingredient ingredient2) {
            int color1 = ingredient1.getColor();
            int color2 = ingredient2.getColor();
            return color2 - color1;
        }
    };
}
