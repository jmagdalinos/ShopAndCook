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

import java.util.ArrayList;

/**
 * Recipe object representing a single recipe from the food2fork api.
 */

public class RecipeFromApi implements Parcelable {
    /** Member variables */
    private String mName;
    private String mSourceUrl;
    private String mImageUrl;
    private ArrayList<Ingredient> mIngredients;

    /** RecipeFromApi constructor */
    public RecipeFromApi(String title, String sourceUrl, String imageUrl, ArrayList<Ingredient> ingredients) {
        mName = title;
        mSourceUrl = sourceUrl;
        mImageUrl = imageUrl;
        mIngredients = ingredients;
    }


    /** RecipeFromApi constructor using a parcel */
    private RecipeFromApi(Parcel parcel) {
        mName = parcel.readString();
        mSourceUrl = parcel.readString();
        mImageUrl = parcel.readString();
        mIngredients = parcel.readArrayList(ArrayList.class.getClassLoader());
    }

    /** Getter methods */
    public String getName() {return mName;}
    public String getSourceUrl() {return mSourceUrl;}
    public String getImageUrl() {return mImageUrl;}
    public ArrayList<Ingredient> getIngredients() {return mIngredients;}

    /** Required Creator for a Parcelable */
    public static final Creator<RecipeFromApi> CREATOR = new Creator<RecipeFromApi>() {
        @Override
        public RecipeFromApi createFromParcel(Parcel in) {
            return new RecipeFromApi(in);
        }

        @Override
        public RecipeFromApi[] newArray(int size) {
            return new RecipeFromApi[size];
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
        parcel.writeString(mSourceUrl);
        parcel.writeString(mImageUrl);
        parcel.writeList(mIngredients);
    }

    /** Returns a color for a rating */
    public static int getColorRating(int rating, Context context) {
        // Divide the rating by 20 and round it up
        // All ratings will then be integers from 1 to 5
        int finalRating = (int) Math.ceil(rating / 20.0);

        switch (finalRating) {
            case 0:
                return ContextCompat.getColor(context, R.color.rating20);
            case 1:
                return ContextCompat.getColor(context, R.color.rating20);
            case 2:
                return ContextCompat.getColor(context, R.color.rating40);
            case 3:
                return ContextCompat.getColor(context, R.color.rating60);
            case 4:
                return ContextCompat.getColor(context, R.color.rating80);
            case 5:
                return ContextCompat.getColor(context, R.color.rating100);
            default:
                return ContextCompat.getColor(context, R.color.rating20);
        }
    }

}
