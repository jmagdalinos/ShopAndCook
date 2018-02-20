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

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.johnmagdalinos.android.shopandcook.R;


/**
 * Fragment displaying a single tutorial slide
 */
public class TutorialFragment extends Fragment{

    /** Member variables */
    private static final String KEY_POSITION = "position";


    /** Class Constructor for use with the database */
    public static TutorialFragment newInstance(int position) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tutorial, container, false);

        int position = 0;

        // Get the arguments
        if (getArguments() != null) {
            position = getArguments().getInt(KEY_POSITION);
        }

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/courgette_regular.ttf");

        ImageView imageView = rootView.findViewById(R.id.iv_tutorial);
        TextView titleTextView = rootView.findViewById(R.id.tv_tutorial_title);
        TextView textTextView = rootView.findViewById(R.id.tv_tutorial_text);

        titleTextView.setTypeface(font);

        switch (position) {
            case 0:
                imageView.setImageResource(R.drawable.tutorial_welcome);
                titleTextView.setText(R.string.tutorial_welcome_title);
                textTextView.setText(R.string.tutorial_welcome);
                break;
            case 1:
                imageView.setImageResource(R.drawable.tutorial_shopping);
                titleTextView.setText(R.string.main_shopping_list);
                textTextView.setText(R.string.tutorial_shopping_list);
                break;
            case 2:
                imageView.setImageResource(R.drawable.tutorial_planner);
                titleTextView.setText(R.string.main_meal_planner);
                textTextView.setText(R.string.tutorial_meal_planner);
                break;
            case 3:
                imageView.setImageResource(R.drawable.tutorial_recipes);
                titleTextView.setText(R.string.main_recipes);
                textTextView.setText(R.string.tutorial_recipes);
                break;
            case 4:
                imageView.setImageResource(R.drawable.tutorial_meals);
                titleTextView.setText(R.string.main_meals);
                textTextView.setText(R.string.tutorial_meals);
                break;
            case 5:
                imageView.setImageResource(R.drawable.tutorial_ideas);
                titleTextView.setText(R.string.main_recipe_ideas);
                textTextView.setText(R.string.tutorial_recipe_ideas);
                break;
        }

        return rootView;
    }
}