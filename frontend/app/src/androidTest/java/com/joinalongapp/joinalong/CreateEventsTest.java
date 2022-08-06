package com.joinalongapp.joinalong;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

@RunWith(AndroidJUnit4.class)
public class CreateEventsTest extends BaseManageEventActivityTest {

    @Before
    public void before() {
        onView(withId(R.id.eventManagementTitle)).check(matches(withText("Create Event")));
    }

    @Test
    public void testCreateEvent_WithEmptyTitle_ShowsEmptyFieldError() {
        //Given
        fillLocation();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementTitle)).check(matches(hasErrorText("Empty Title field")));
    }

    @Test
    public void testCreateEvent_WithNonAlphaNumericTitle_ShowsEmptyFieldError() {
        //Given
        fillLocation();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        setTitle("#Invalid@Title*!");

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementTitle)).check(matches(hasErrorText("Title contains invalid character(s).")));
    }

    @Test
    public void testCreateEvent_WithEmptyLocation_ShowsEmptyFieldError() {
        //Given
        fillTitle();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementLocation)).check(matches(hasErrorText("Empty Location field")));
    }

    @Test
    public void testCreateEvent_WithInvalidLocation_ShowsInvalidAddressError() {
        //Given
        fillTitle();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        setLocation("123 45 Street, ABC City");

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementLocation)).check(matches(hasErrorText("Invalid Address")));
    }

    @Test
    public void testCreateEvent_WithNonSpecificLocation_ShowsInvalidAddressError() {
        //Given
        fillTitle();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        setLocation("Vancouver BC");

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementLocation)).check(matches(hasErrorText("Invalid Address")));
    }

    @Test
    public void testCreateEvent_WithPastBeginningDateTime_ShowsInvalidDateError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginTime();
        fillEndDate();
        fillEndTime();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        setBeginDate(1999, 8, 1);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementBeginningDate)).check(matches(hasErrorText("Beginning date cannot be in the past.")));
    }

    @Test
    public void testCreateEvent_WithEmptyBeginningDate_ShowsEmptyDateError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginTime();
        fillEndDate();
        fillEndTime();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementBeginningDate)).check(matches(hasErrorText("Empty Beginning Date field")));
    }

    @Test
    public void testCreateEvent_WithEmptyBeginningTime_ShowsEmptyTimeError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginDate();
        fillEndDate();
        fillEndTime();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementBeginningTime)).check(matches(hasErrorText("Empty Beginning Time field")));
    }

    @Test
    public void testCreateEvent_WithPastEndDateTime_ShowsInvalidDateError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginDate();
        fillBeginTime();
        fillEndTime();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        setEndDate(2020, 11, 28);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementEndDate)).check(matches(hasErrorText("End date cannot be in the past.")));
    }

    @Test
    public void testCreateEvent_WithEmptyEndDate_ShowsEmptyDateError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginDate();
        fillBeginTime();
        fillEndTime();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementEndDate)).check(matches(hasErrorText("Empty End Date field")));
    }

    @Test
    public void testCreateEvent_WithEmptyEndTime_ShowsEmptyTimeError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginDate();
        fillBeginTime();
        fillEndDate();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementEndTime)).check(matches(hasErrorText("Empty End Time field")));
    }

    @Test
    public void testCreateEvent_WithEndBeforeBeginDateTime_ShowsInvalidDateComboError() {
        //Given
        fillTitle();
        fillLocation();
        setIsPublic(true);
        setNumberPeople(123);
        fillTags();
        fillDescription();

        Calendar today = getOneHourFromNow();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);

        setBeginDate(year + 1, month, day);
        setBeginTime(hour, minute);
        setEndDate(year, month, day);
        setEndTime(hour, minute);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementBeginningDate)).check(matches(hasErrorText("End date cannot be before beginning date.")));
        onView(withId(R.id.editTextEventManagementEndDate)).check(matches(hasErrorText("End date cannot be before beginning date.")));
    }

    @Test
    public void testCreateEvent_WithEmptyTagsList_ShowsEmptyTagsError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        setNumberPeople(123);
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.autoCompleteChipTextView)).check(matches(hasErrorText("No interest tags selected.")));
    }

    @Test
    public void testCreateEvent_WithTagTextButEmptyList_ShowsEmptyTagsError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        setNumberPeople(123);
        fillDescription();

        onView(withId(R.id.autoCompleteChipTextView)).perform(typeText("Hiking"), closeSoftKeyboard());

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.autoCompleteChipTextView)).check(matches(hasErrorText("No interest tags selected.")));
    }

    @Test
    public void testCreateEvent_WithEmptyDescription_ShowsEmptyDescriptionError() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        fillTags();
        setNumberPeople(123);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.eventManagementEditTextDescription)).check(matches(hasErrorText("Empty Description field")));
    }

    @Test
    public void testCreateEvent_WithValidFields_ShowsSuccessMessage() {
        //Given
        fillTitle();
        fillLocation();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        fillTags();
        setNumberPeople(1);
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withText("Event Created!")).check(matches(isDisplayed()));
        onView(withText("The Test Event event has been successfully created.")).check(matches(isDisplayed()));
    }
}
