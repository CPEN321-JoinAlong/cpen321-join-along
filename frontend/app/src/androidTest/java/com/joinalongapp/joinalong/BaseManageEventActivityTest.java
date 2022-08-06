package com.joinalongapp.joinalong;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Matchers;
import org.junit.Rule;

import java.util.Calendar;
import java.util.Date;

public abstract class BaseManageEventActivityTest {
    public static Intent intent;
    protected final static String testingId = "62d50cfb436fbc75c258d9eb";
    protected final static String testingToken = "110723426330439313256";
    protected static final long ONE_HOUR_IN_MILLIS = 3600000;

    static  {
        intent = new Intent(ApplicationProvider.getApplicationContext(), ManageEventActivity.class);
        intent.putExtra("testingToken", testingToken);
        intent.putExtra("testingId", testingId);
    }

    @Rule
    public ActivityScenarioRule<ManageEventActivity> activityRule =
            new ActivityScenarioRule<>(intent);

    protected void fillTitle() {
        setTitle("Test Event");
    }

    protected void setTitle(String title) {
        onView(withId(R.id.editTextEventManagementTitle)).perform(typeText(title), closeSoftKeyboard());
    }

    protected void fillLocation() {
        setLocation("2336 Main Mall, Vancouver");
    }

    protected void setLocation(String location) {
        onView(withId(R.id.editTextEventManagementLocation)).perform(typeText(location), closeSoftKeyboard());
    }

    protected void fillBeginEndDatesTimes() {
        fillBeginDate();
        fillEndDate();
        fillBeginTime();
        fillEndTime();
    }

    protected void fillBeginDate() {
        Calendar today = getOneHourFromNow();
        int year = today.get(Calendar.YEAR);
        // Add 1 because index starts at 0
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);

        setBeginDate(year, month, day);
    }

    protected void fillBeginTime() {
        Calendar today = getOneHourFromNow();
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);

        setBeginTime(hour, minute);
    }

    protected void fillEndDate() {
        Calendar today = getOneHourFromNow();
        int year = today.get(Calendar.YEAR);
        // Add 1 because index starts at 0
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);

        setEndDate(year, month, day);
    }

    protected void fillEndTime() {
        Calendar today = getOneHourFromNow();
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);

        setEndTime(hour, minute);
    }

    protected void setBeginDate(int year, int month, int day) {
        onView(withId(R.id.editTextEventManagementBeginningDate)).perform(click());

        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month, day));
        onView(withText("OK")).perform(click());
    }

    protected void setBeginTime(int hourOfDay, int minute) {
        onView(withId(R.id.editTextEventManagementBeginningTime)).perform(click());

        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(hourOfDay, minute));
        onView(withText("OK")).perform(click());
    }

    protected void setEndDate(int year, int month, int day) {
        onView(withId(R.id.editTextEventManagementEndDate)).perform(click());

        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month, day));
        onView(withText("OK")).perform(click());
    }

    protected void setEndTime(int hourOfDay, int minute) {
        onView(withId(R.id.editTextEventManagementEndTime)).perform(click());

        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(hourOfDay, minute));
        onView(withText("OK")).perform(click());
    }

    protected void setIsPublic(boolean isPublic) {
        if (isPublic) {
            onView(withText("Public")).perform(click());
        } else {
            onView(withText("protected")).perform(click());
        }
    }

    protected void setNumberPeople(int numberPeople) {
        onView(withId(R.id.eventManagementNumberOfPeople)).perform(typeText(String.valueOf(numberPeople)), closeSoftKeyboard());
    }

    protected void fillTags() {
        onView(withId(R.id.autoCompleteChipTextView)).perform(typeText("Hiking"));
        onView(withText("Hiking")).perform(click());
        onView(withId(R.id.autoCompleteChipTextView)).perform(closeSoftKeyboard());
    }

    protected void fillDescription() {
        onView(withId(R.id.eventManagementEditTextDescription)).perform(typeText("Test Description"), closeSoftKeyboard());
    }

    protected static void clearText(int elementId) {
        onView(withId(elementId)).perform(ViewActions.clearText());
    }

    protected static Calendar getOneHourFromNow() {
        Calendar retVal = Calendar.getInstance();
        long now = new Date().getTime();

        retVal.setTimeInMillis(now + ONE_HOUR_IN_MILLIS);
        return retVal;
    }
}
