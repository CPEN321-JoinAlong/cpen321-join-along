package com.joinalongapp.joinalong;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ManageEventActivityTest {
    @Rule
    public ActivityScenarioRule<ManageEventActivity> activityRule =
            new ActivityScenarioRule<>(ManageEventActivity.class);

    @Test
    public void testCreateEvent_SubmitEmptyForm_ShowsErrors() {
        //Given
        onView(withId(R.id.eventManagementTitle)).check(matches(withText("Create Event")));

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementTitle)).check(matches(hasErrorText("Empty Title field")));
        onView(withId(R.id.editTextEventManagementLocation)).check(matches(hasErrorText("Empty Location field")));
        onView(withId(R.id.editTextEventManagementBeginningDate)).check(matches(hasErrorText("Empty Beginning Date field")));
        onView(withId(R.id.editTextEventManagementEndDate)).check(matches(hasErrorText("Empty End Date field")));
        onView(withId(R.id.eventManagementEditTextDescription)).check(matches(hasErrorText("Empty Description field")));
    }

    @Test
    public void testCreateEvent_SubmitInvalidLocation_ShowsUnknownAddressError() {
        //Given
        onView(withId(R.id.eventManagementTitle)).check(matches(withText("Create Event")));
        onView(withId(R.id.editTextEventManagementLocation)).perform(typeText("123 45 Street, ABC City"), closeSoftKeyboard());

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementLocation)).check(matches(hasErrorText("Invalid Address")));
    }

}
