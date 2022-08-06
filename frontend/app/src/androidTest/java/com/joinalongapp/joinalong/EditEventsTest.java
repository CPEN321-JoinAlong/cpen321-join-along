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

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.LocationUtils;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.Tag;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EditEventsTest extends BaseManageEventActivityTest {
    private static Event event;
    private static final long ONE_HOUR = 3600000;

    static {
        event = new Event();
        event.setTitle("Initial Title");
        event.setLocation(LocationUtils.standardizeAddress(LocationUtils.getAddressFromString("2336 Main Mall Vancouver", ApplicationProvider.getApplicationContext())));

        long now = new Date().getTime();

        Date oneHourInFuture = new Date();
        oneHourInFuture.setTime(now + ONE_HOUR);

        Date twoHoursInFuture = new Date();
        twoHoursInFuture.setTime(now + (2 * ONE_HOUR));

        event.setBeginningDate(oneHourInFuture);
        event.setEndDate(twoHoursInFuture);

        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Dancing"));
        event.setTags(tags);

        event.setNumberOfPeopleAllowed(6);
        event.setDescription("Initial Description");

        event.setPublicVisibility(Boolean.FALSE);
        event.setEventOwnerId(testingId);
        event.setEventId("62e317ac77f7ad9a56ab886b");

        intent.putExtra("EVENT", event);
    }

    @Before
    public void before() {
        onView(withId(R.id.eventManagementTitle)).check(matches(withText("Edit Event")));
    }

    @Test
    public void testEditEvent_WithEmptyTitle_ShowsEmptyFieldError() {
        //Given
        clearText(R.id.editTextEventManagementTitle);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementTitle)).check(matches(hasErrorText("Empty Title field")));
    }

    @Test
    public void testEditEvent_WithNonAlphaNumericTitle_ShowsEmptyFieldError() {
        //Given
        clearText(R.id.editTextEventManagementTitle);
        setTitle("#Invalid@Title*!");

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementTitle)).check(matches(hasErrorText("Title contains invalid character(s).")));
    }

    @Test
    public void testEditEvent_WithEmptyLocation_ShowsEmptyFieldError() {
        //Given
        clearText(R.id.editTextEventManagementLocation);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementLocation)).check(matches(hasErrorText("Empty Location field")));
    }

    @Test
    public void testEditEvent_WithInvalidLocation_ShowsInvalidAddressError() {
        //Given
        clearText(R.id.editTextEventManagementLocation);
        setLocation("123 45 Street, ABC City");

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementLocation)).check(matches(hasErrorText("Invalid Address")));
    }

    @Test
    public void testEditEvent_WithNonSpecificLocation_ShowsInvalidAddressError() {
        //Given
        clearText(R.id.editTextEventManagementLocation);
        setLocation("Vancouver BC");

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementLocation)).check(matches(hasErrorText("Invalid Address")));
    }

    @Test
    public void testEditEvent_WithPastBeginningDateTime_ShowsInvalidDateError() {
        //Given
        setBeginDate(1999, 8, 1);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementBeginningDate)).check(matches(hasErrorText("Beginning date cannot be in the past.")));
    }

    @Test
    public void testEditEvent_WithEmptyBeginningDate_ShowsEmptyDateError() {
        //Given
        clearText(R.id.editTextEventManagementBeginningDate);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementBeginningDate)).check(matches(hasErrorText("Empty Beginning Date field")));
    }

    @Test
    public void testEditEvent_WithEmptyBeginningTime_ShowsEmptyTimeError() {
        //Given
        clearText(R.id.editTextEventManagementBeginningTime);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementBeginningTime)).check(matches(hasErrorText("Empty Beginning Time field")));
    }

    @Test
    public void testEditEvent_WithPastEndDateTime_ShowsInvalidDateError() {
        //Given
        setEndDate(2020, 11, 28);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementEndDate)).check(matches(hasErrorText("End date cannot be in the past.")));
    }

    @Test
    public void testEditEvent_WithEmptyEndDate_ShowsEmptyDateError() {
        //Given
        clearText(R.id.editTextEventManagementEndDate);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementEndDate)).check(matches(hasErrorText("Empty End Date field")));
    }

    @Test
    public void testEditEvent_WithEmptyEndTime_ShowsEmptyTimeError() {
        //Given
        clearText(R.id.editTextEventManagementEndTime);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementEndTime)).check(matches(hasErrorText("Empty End Time field")));
    }

    @Test
    public void testEditEvent_WithEndBeforeBeginDateTime_ShowsInvalidDateComboError() {
        //Given

        Calendar today = getOneHourFromNow();
        int year = today.get(Calendar.YEAR);
        // Add 1 because index starts at 0
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);

        setBeginDate(year + 1, month, day);
        setEndDate(year, month, day);
        setBeginTime(hour, minute);
        setEndTime(hour, minute);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.editTextEventManagementBeginningDate)).check(matches(hasErrorText("End date cannot be before beginning date.")));
        onView(withId(R.id.editTextEventManagementEndDate)).check(matches(hasErrorText("End date cannot be before beginning date.")));
    }

    @Test
    public void testEditEvent_WithEmptyTagsList_ShowsEmptyTagsError() {
        //Given
        clearTagChipGroup();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.autoCompleteChipTextView)).check(matches(hasErrorText("No interest tags selected.")));
    }

    @Test
    public void testEditEvent_WithTagTextButEmptyList_ShowsEmptyTagsError() {
        //Given
        clearTagChipGroup();

        onView(withId(R.id.autoCompleteChipTextView)).perform(typeText("Hiking"), closeSoftKeyboard());

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.autoCompleteChipTextView)).check(matches(hasErrorText("No interest tags selected.")));
    }

    @Test
    public void testEditEvent_WithEmptyDescription_ShowsEmptyDescriptionError() {
        //Given
        clearText(R.id.eventManagementEditTextDescription);

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withId(R.id.eventManagementEditTextDescription)).check(matches(hasErrorText("Empty Description field")));
    }

    @Test
    public void testEditEvent_WithNewValidFields_ShowsSuccessMessage() {
        //Given
        clearAllTextFields();

        setTitle("DO NOT DELETE");
        fillLocation();
        fillBeginEndDatesTimes();
        setIsPublic(true);
        fillTags();
        setNumberPeople(1);
        fillDescription();

        //When
        onView(withId(R.id.submitManageEventButton)).perform(click());

        //Then
        onView(withText("Event Edited!")).check(matches(isDisplayed()));
        onView(withText("The DO NOT DELETE event has been successfully edited.")).check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());
    }

    private void clearAllTextFields() {
        clearText(R.id.editTextEventManagementTitle);
        clearText(R.id.editTextEventManagementLocation);
        clearText(R.id.editTextEventManagementBeginningDate);
        clearText(R.id.editTextEventManagementEndDate);
        clearText(R.id.editTextEventManagementBeginningTime);
        clearText(R.id.editTextEventManagementEndTime);

        clearTagChipGroup();

        clearText(R.id.eventManagementEditTextDescription);
    }

    private void clearTagChipGroup() {
        ActivityScenario activityScenario = activityRule.getScenario();
        activityScenario.onActivity(activity -> {
            Chip chip = (Chip) ((ChipGroup) activity.findViewById(R.id.eventManagementTagChipGroup)).getChildAt(0);
            chip.performCloseIconClick();
        });
    }
}
