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

import com.johnmagdalinos.android.shopandcook.R;
import com.johnmagdalinos.android.shopandcook.utilities.Constants;

import java.util.Comparator;

/**
 * Day object representing a single day in a week. Stores the meal name and ids for all
 * associated meals
 */

public class Day implements Parcelable {
    /** Member variables */
    private int mDayOfTheWeek;
    private String mBreakfast, mBreakfastName;
    private String mMorningSnack, mMorningSnackName;
    private String mLunch, mLunchName;
    private String mAfternoonSnack, mAfternoonSnackName;
    private String mDinner, mDinnerName;
    private String mDayId;

    /** Day constructor for Firebase Realtime Database */
    public Day() {}

    /** Day constructor using a parcel */
    protected Day(Parcel parcel) {
        mDayOfTheWeek = parcel.readInt();
        mBreakfast = parcel.readString();
        mBreakfastName = parcel.readString();
        mMorningSnack = parcel.readString();
        mMorningSnackName = parcel.readString();
        mLunch = parcel.readString();
        mLunchName = parcel.readString();
        mAfternoonSnack = parcel.readString();
        mAfternoonSnackName = parcel.readString();
        mDinner = parcel.readString();
        mDinnerName = parcel.readString();
        mDayId = parcel.readString();
    }

    /** Getter methods */
    public int getDayOfTheWeek() {return mDayOfTheWeek;}
    public String getBreakfast() {return mBreakfast;}
    public String getBreakfastName() {return mBreakfastName;}
    public String getMorningSnack() {return mMorningSnack;}
    public String getMorningSnackName() {return mMorningSnackName;}
    public String getLunch() {return mLunch;}
    public String getLunchName() {return mLunchName;}
    public String getAfternoonSnack() {return mAfternoonSnack;}
    public String getAfternoonSnackName() {return mAfternoonSnackName;}
    public String getDinner() {return mDinner;}
    public String getDinnerName() {return mDinnerName;}
    public String getDayId() {return mDayId;}

    /** Setter methods */
    public void setDayOfTheWeek(int dayOfTheWeek) {
        this.mDayOfTheWeek = dayOfTheWeek;
    }
    public void setBreakfast(String breakfast) {
        this.mBreakfast = breakfast;
    }
    public void setBreakfastName(String breakfastName) {
        this.mBreakfastName = breakfastName;
    }
    public void setMorningSnack(String morningSnack) {
        this.mMorningSnack = morningSnack;
    }
    public void setMorningSnackName(String morningSnackName) {
        this.mMorningSnackName = morningSnackName;
    }
    public void setLunch(String lunch) {
        this.mLunch = lunch;
    }
    public void setLunchName(String lunchName) {
        this.mLunchName = lunchName;
    }
    public void setAfternoonSnack(String afternoonSnack) {
        this.mAfternoonSnack = afternoonSnack;
    }
    public void setAfternoonSnackName(String afternoonSnackName) {
        this.mAfternoonSnackName = afternoonSnackName;
    }
    public void setDinner(String dinner) {
        this.mDinner = dinner;
    }
    public void setDinnerName(String dinnerName) {
        this.mDinnerName = dinnerName;
    }
    public void setDayId(String dayId) {
        this.mDayId = dayId;
    }

    /** Required Creator for a Parcelable */
    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel in) {
            return new Day(in);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /** Write all variables to the parcel */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mDayOfTheWeek);
        parcel.writeString(mBreakfast);
        parcel.writeString(mBreakfastName);
        parcel.writeString(mMorningSnack);
        parcel.writeString(mMorningSnackName);
        parcel.writeString(mLunch);
        parcel.writeString(mLunchName);
        parcel.writeString(mAfternoonSnack);
        parcel.writeString(mAfternoonSnackName);
        parcel.writeString(mDinner);
        parcel.writeString(mDinnerName);
        parcel.writeString(mDayId);
    }

    /** Returns the day of the week from an int */
    public static String intToDay(Context context,  int day) {
        String[] stringDays = context.getResources().getStringArray(R.array.days_of_the_week);
        return stringDays[day];
    }


    /** Returns the Meal title based the position in the adapter */
    public static String getMealTitle(int position) {
        switch (position) {
            case 1:
                return Constants.NODE_BREAKFAST;
            case 3:
                return Constants.NODE_MORNING_SNACK;
            case 5:
                return Constants.NODE_LUNCH;
            case 7:
                return Constants.NODE_AFTERNOON_SNACK;
            case 9:
                return Constants.NODE_DINNER;
            default:
                return null;
        }
    }

    /** Returns the Meal title name based the position in the adapter */
    public static String getMealTitleName(int position) {
        switch (position) {
            case 1:
                return Constants.NODE_BREAKFAST_NAME;
            case 3:
                return Constants.NODE_MORNING_SNACK_NAME;
            case 5:
                return Constants.NODE_LUNCH_NAME;
            case 7:
                return Constants.NODE_AFTERNOON_SNACK_NAME;
            case 9:
                return Constants.NODE_DINNER_NAME;
            default:
                return null;
        }
    }

    /** Comparator sorting Days by dayId */
    public static Comparator<Day> AscendingComparator = new Comparator<Day>() {
        @Override
        public int compare(Day day1, Day day2) {
            int id1 = day1.getDayOfTheWeek();
            int id2 = day2.getDayOfTheWeek();
            return id1 - id2;
        }
    };
}
