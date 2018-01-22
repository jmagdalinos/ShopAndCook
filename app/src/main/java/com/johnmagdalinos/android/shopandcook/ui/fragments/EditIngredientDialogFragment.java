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

package com.johnmagdalinos.android.shopandcook.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.model.Ingredient;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Displays a dialog allowing the user to edit/add an ingredient or shopping list item
 */

public class EditIngredientDialogFragment extends android.support.v4.app.DialogFragment {
    
    /** Member variables */
    private Ingredient mIngredient;
    private boolean mIsNew;
    private String mRecipeId;
    private int mMeasure;
    private DatabaseReference mMyRef;
    
    /** View used in the fragment */
    private EditText mNameEditText, mQuantityEditText, mCommentsEditText;
    private ImageButton mColor0ImageButton,mColor1ImageButton, mColor2ImageButton, 
            mColor3ImageButton, mColor4ImageButton, mColor5ImageButton;

    /** Key for the Fragment args */
    private static final String KEY_INGREDIENT = "ingredient";
    private static final String KEY_IS_NEW = "is_new";
    private static final String KEY_RECIPE_ID = "recipe_id";

    /** Class constructor */
    public static EditIngredientDialogFragment newInstance(String userId, Ingredient ingredient,
                                                           boolean isNew, @Nullable
            String recipeId) {
        EditIngredientDialogFragment fragment = new EditIngredientDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_IS_NEW, isNew);
        args.putParcelable(KEY_INGREDIENT, ingredient);
        args.putString(KEY_RECIPE_ID, recipeId);
        args.putString(Constants.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the values from the bundle
        mIngredient = getArguments().getParcelable(KEY_INGREDIENT);
        mIsNew = getArguments().getBoolean(KEY_IS_NEW);
        mRecipeId = getArguments().getString(KEY_RECIPE_ID);
        String uId = getArguments().getString(Constants.KEY_USER_ID);
        
        // Get an instance of the Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (mRecipeId == null) {
            // This is a shopping list item
            mMyRef = database.getReference()
                    .child(Constants.NODE_SHOPPING_LIST)
                    .child(uId);
        } else {
            // This is an ingredient
            mMyRef = database.getReference()
                    .child(Constants.NODE_INGREDIENTS)
                    .child(uId)
                    .child(mRecipeId);
        }

        // Inflate the dialog view
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout
                .fragment_dialog_edit_ingredient, null);
        // Setup a dialog to edit the ingredient
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);

        // Set the title
        TextView titleTextView = dialogView.findViewById(R.id.tv_edit_ingredient_title);
        if (mRecipeId != null && !TextUtils.isEmpty(mRecipeId)) {
            // This is a recipe's ingredient
            if (mIsNew) {
                titleTextView.setText(R.string.dialog_title_create_ingredient);
            } else {
                titleTextView.setText(R.string.dialog_title_edit_ingredient);
            }
        } else {
            // This is a shopping list item
            if (mIsNew) {
                titleTextView.setText(R.string.dialog_title_create_shopping_item);
            } else {
                titleTextView.setText(R.string.dialog_title_edit_shopping_item);
            }
        }

        // Setup the spinner
        Spinner measureSpinner = dialogView.findViewById(R.id.sp_edit_ingredient_measure);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.measure_spinner_singular, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measureSpinner.setAdapter(spinnerAdapter);

        measureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                mMeasure = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Setup the edit texts
        mNameEditText = dialogView.findViewById(R.id
                .et_edit_ingredient_name);
        mQuantityEditText = dialogView.findViewById(R.id
                .et_edit_ingredient_quantity);
        mCommentsEditText = dialogView.findViewById(R.id
                .et_edit_ingredient_notes);

        // Set the text in the EditTexts and Spinner
        mNameEditText.setText(mIngredient.getName());
        mQuantityEditText.setText(String.valueOf(mIngredient.getQuantity()));
        measureSpinner.setSelection(mIngredient.getMeasure());
        mCommentsEditText.setText(mIngredient.getComments());

        if (mRecipeId == null) {
            // Setup the color picker
            mColor0ImageButton = dialogView.findViewById(R.id.edit_color_0);
            mColor1ImageButton = dialogView.findViewById(R.id.edit_color_1);
            mColor2ImageButton = dialogView.findViewById(R.id.edit_color_2);
            mColor3ImageButton = dialogView.findViewById(R.id.edit_color_3);
            mColor4ImageButton = dialogView.findViewById(R.id.edit_color_4);
            mColor5ImageButton = dialogView.findViewById(R.id.edit_color_5);

            // Display the appropriate Views
            dialogView.findViewById(R.id.in_edit_ingredient).setVisibility(View.VISIBLE);
            dialogView.findViewById(R.id.tv_edit_ingredient_color_picker_title).setVisibility(View.VISIBLE);

            // Get the color from the ingredient and select the appropriate button
            switch (mIngredient.getColor()) {
                case 0:
                    mColor0ImageButton.setSelected(true);
                    break;
                case 1:
                    mColor1ImageButton.setSelected(true);
                    break;
                case 2:
                    mColor2ImageButton.setSelected(true);
                    break;
                case 3:
                    mColor3ImageButton.setSelected(true);
                    break;
                case 4:
                    mColor4ImageButton.setSelected(true);
                    break;
                case 5:
                    mColor5ImageButton.setSelected(true);
                    break;
                default:
                    mColor0ImageButton.setSelected(true);
                    mIngredient.setColor(0);
                    break;
            }
            // Create an OnClickListener to monitor the color buttons
            View.OnClickListener colorClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.edit_color_0:
                            // Set color value
                            mIngredient.setColor(0);
                            // Select current color and un-select all others
                            mColor0ImageButton.setSelected(true);
                            mColor1ImageButton.setSelected(false);
                            mColor2ImageButton.setSelected(false);
                            mColor3ImageButton.setSelected(false);
                            mColor4ImageButton.setSelected(false);
                            mColor5ImageButton.setSelected(false);
                            break;
                        case R.id.edit_color_1:
                            // Set color value
                            mIngredient.setColor(1);
                            // Select current color and un-select all others
                            mColor0ImageButton.setSelected(false);
                            mColor1ImageButton.setSelected(true);
                            mColor2ImageButton.setSelected(false);
                            mColor3ImageButton.setSelected(false);
                            mColor4ImageButton.setSelected(false);
                            mColor5ImageButton.setSelected(false);
                            break;
                        case R.id.edit_color_2:
                            // Set color value
                            mIngredient.setColor(2);
                            // Select current color and un-select all others
                            mColor0ImageButton.setSelected(false);
                            mColor1ImageButton.setSelected(false);
                            mColor2ImageButton.setSelected(true);
                            mColor3ImageButton.setSelected(false);
                            mColor4ImageButton.setSelected(false);
                            mColor5ImageButton.setSelected(false);
                            break;
                        case R.id.edit_color_3:
                            // Set color value
                            mIngredient.setColor(3);
                            // Select current color and un-select all others
                            mColor0ImageButton.setSelected(false);
                            mColor1ImageButton.setSelected(false);
                            mColor2ImageButton.setSelected(false);
                            mColor3ImageButton.setSelected(true);
                            mColor4ImageButton.setSelected(false);
                            mColor5ImageButton.setSelected(false);
                            break;
                        case R.id.edit_color_4:
                            // Set color value
                            mIngredient.setColor(4);
                            // Select current color and un-select all others
                            mColor0ImageButton.setSelected(false);
                            mColor1ImageButton.setSelected(false);
                            mColor2ImageButton.setSelected(false);
                            mColor3ImageButton.setSelected(false);
                            mColor4ImageButton.setSelected(true);
                            mColor5ImageButton.setSelected(false);
                            break;
                        case R.id.edit_color_5:
                            // Set color value
                            mIngredient.setColor(5);
                            // Select current color and un-select all others
                            mColor0ImageButton.setSelected(false);
                            mColor1ImageButton.setSelected(false);
                            mColor2ImageButton.setSelected(false);
                            mColor3ImageButton.setSelected(false);
                            mColor4ImageButton.setSelected(false);
                            mColor5ImageButton.setSelected(true);
                            break;
                    }
                }
            };

            mColor0ImageButton.setOnClickListener(colorClickListener);
            mColor1ImageButton.setOnClickListener(colorClickListener);
            mColor2ImageButton.setOnClickListener(colorClickListener);
            mColor3ImageButton.setOnClickListener(colorClickListener);
            mColor4ImageButton.setOnClickListener(colorClickListener);
            mColor5ImageButton.setOnClickListener(colorClickListener);
        }

        final int isChecked = mIngredient.getIsChecked();

        // Setup the buttons
        builder.setPositiveButton(R.string.dialog_save, null);

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked on "Cancel"
                if (dialogInterface != null) dialogInterface.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        // Set onShowListener to avoid dialog closing when pressing the positive button
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // User clicked on "Save"
                        String name = mNameEditText.getText().toString().trim();
                        String quantityString = mQuantityEditText.getText().toString().trim();
                        String comments = mCommentsEditText.getText().toString().trim();

                        double quantity = 0;
                        try {
                            quantity = Double.parseDouble(quantityString);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(getActivity(), R.string.add_ingredient_name, Toast.LENGTH_SHORT).show();
                        } else {
                            mIngredient.setName(name);
                            mIngredient.setQuantity(quantity);
                            mIngredient.setMeasure(mMeasure);
                            mIngredient.setComments(comments);
                            if (mRecipeId == null) {
                                mIngredient.setIsChecked(isChecked);
                            }

                            if (!mIsNew) {
                                // This is an old ingredient, update it
                                mMyRef.child(mIngredient.getIngredientId())
                                        .setValue(mIngredient);
                            } else {
                                // This is a new ingredient, push it
                                mMyRef.push()
                                        .setValue(mIngredient);
                            }
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }
}
