package com.joinalongapp.joinalong;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.joinalongapp.navbar.ViewEventFragment;

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
        onView(isAssignableFrom(EditText.class)).perform(replaceText("Test"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        Thread.sleep(1000);

        onView(withId(R.id.searchPeopleRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.viewEventConstraintLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.viewEventTitle)).check(matches(withText("Test")));
        onView(withId(R.id.joinEventButton)).perform(click());
        Thread.sleep(3000);
        onView(withText("Event Successfully Joined!")).check(matches(isDisplayed()));

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
