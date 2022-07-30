package com.joinalongapp.joinalong;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;

import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.SearchView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class SearchScreenActivityTest {

    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), SearchScreenActivity.class);
        intent.putExtra("testingToken", "110723426330439313256");
        intent.putExtra("mode", SearchScreenActivity.SearchMode.EVENT_MODE);
    }

    @Rule
    public ActivityScenarioRule<SearchScreenActivity> activityRule = new ActivityScenarioRule<>(intent);

    @Test
    public void testSearchEmptyString(){
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        // When
        onView(withId(R.id.searchBar)).perform(pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        checkNumberOfElements(0);
        onView(withId(R.id.searchNoResults)).check(matches(withText("No Results Found")));
    }

    @Test
    public void testNonAlphanumericString() throws InterruptedException {
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        onView(isAssignableFrom(EditText.class)).perform(replaceText("?[!.,"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        Thread.sleep(1000);
        checkNumberOfElements(0);
        onView(withId(R.id.searchNoResults)).check(matches(withText("No Results Found")));
    }

    @Test
    public void testNoSearchResults() throws InterruptedException {
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("ZZZZ"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        Thread.sleep(1000);
        checkNumberOfElements(0);
        onView(withId(R.id.searchNoResults)).check(matches(withText("No Results Found")));
    }

    @Test
    public void testSingularSearchResult() throws InterruptedException {
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("Very Specific Event Title For No Particular Reason"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        Thread.sleep(1000);
        checkNumberOfElements(1);

        onView(withId(R.id.searchPeopleRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.viewEventConstraintLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.viewEventTitle)).check(matches(withText("Very Specific Event Title For No Particular Reason")));
    }

    @Test
    public void testMultipleSearchResult() throws InterruptedException {
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("testnorth"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        Thread.sleep(1000);
        checkNumberOfElements(3);

        onView(withId(R.id.searchPeopleRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.viewEventConstraintLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.viewEventTitle)).check(matches(withText("testnorth")));
    }

    @Test
    public void testOnQueryTextChange() throws InterruptedException {
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("testnorth"));
        // Then
        Thread.sleep(1000);
        checkAutofillSuggestions(3);
    }

    @Test
    public void testClickSearchResult() throws InterruptedException {
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("Another event"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        Thread.sleep(1000);
        checkNumberOfElements(1);
        onView(withId(R.id.searchPeopleRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.viewEventConstraintLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.viewEventTitle)).check(matches(withText("Another event")));
    }

    public boolean checkNumberOfElements(int target){
        ActivityScenario activityScenario = activityRule.getScenario();
        activityScenario.onActivity(activity -> {
            assert(((RecyclerView) activity.findViewById(R.id.searchPeopleRecyclerView)).getAdapter().getItemCount() == target);
        });
        return true;
    }


    public boolean checkEmptySearchView(){
        ActivityScenario activityScenario = activityRule.getScenario();
        activityScenario.onActivity(activity -> {
            assert(((SearchView) activity.findViewById(R.id.searchBar)).getQuery().toString().isEmpty());
        });
        return true;
    }

    public boolean checkAutofillSuggestions(int target){
        ActivityScenario activityScenario = activityRule.getScenario();
        activityScenario.onActivity(activity -> {
            assert(((SearchView) activity.findViewById(R.id.searchBar)).getSuggestionsAdapter().getCount() == target);
        });
        return true;
    }
}
