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

package com.example.android.shopandcook.ui.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.example.android.shopandcook.model.Recipe;
import com.example.android.shopandcook.utilities.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Time picker used to set the prep time for a recipe
 */

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    /** Member variables */
    private Recipe mCurrentRecipe;
    private String mUId;

    /** Class constructor */
    public static TimePickerFragment newInstance(Recipe recipe, String uId) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_EXTRAS, recipe);
        args.putString(Constants.KEY_USER_ID, uId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the data from the arguments
        mCurrentRecipe = getArguments().getParcelable(Constants.KEY_EXTRAS);
        mUId = getArguments().getString(Constants.KEY_USER_ID);

        long time = mCurrentRecipe.getPrepTime();
        int hours = (int) time / 60;
        int minutes = (int) time % 60;
        return new TimePickerDialog(getActivity(), this, hours, minutes, true);
    }

    /** Called when the user sets the time */
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        long time = (long) hourOfDay * 60 + minute;
        String recipeId = mCurrentRecipe.getRecipeId();

        // Save the time in the database
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child(Constants.NODE_RECIPES)
                .child(mUId)
                .child(recipeId)
                .child(Constants.NODE_PREP_TIME);

        ref.setValue(time);
    }
}