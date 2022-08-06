package com.joinalongapp.joinalong;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JoinEventTest {

    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), SearchScreenActivity.class);
        intent.putExtra("testingToken", "110723426330439313256");
        intent.putExtra("testingId", "62d63248010a82beb388af87");
        intent.putExtra("mode", SearchScreenActivity.SearchMode.EVENT_MODE);
    }

    @Rule
    public ActivityScenarioRule<SearchScreenActivity> activityRule = new ActivityScenarioRule<>(intent);

    @Test
    public void testJoinEventOpen() throws InterruptedException {
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("very paticular title"));
        // Then
        Thread.sleep(1000);

        onView(withText("very paticular title")).inRoot(RootMatchers.isPlatformPopup()).perform(click());
        onView(withId(R.id.viewEventTitle)).check(matches(withText("very paticular title")));
        Thread.sleep(1000);
        onView(withId(R.id.joinEventButton)).perform(click());
        Thread.sleep(1000);
        onView(withText("Event Successfully Joined!")).check(matches(isDisplayed()));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.viewEventBackButton)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(replaceText(""));
        onView(isAssignableFrom(EditText.class)).perform(typeText("very paticular title"));
        Thread.sleep(1000);
        // RESET FOR REPEATABILITY OF TEST
        onView(withText("very paticular title")).inRoot(RootMatchers.isPlatformPopup()).perform(click());

        onView(withId(R.id.eventOptions)).perform(click());
        onView(withText("Leave Event")).inRoot(RootMatchers.isPlatformPopup()).perform(click());
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

    }

    @Test
    public void testJoinEventFull() throws InterruptedException {
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("Full Event"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        Thread.sleep(1000);

        onView(withId(R.id.searchPeopleRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.viewEventConstraintLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.viewEventTitle)).check(matches(withText("Full Event")));
        onView(withId(R.id.joinEventButton)).check(matches(withText("Event full!")));
    }

}
