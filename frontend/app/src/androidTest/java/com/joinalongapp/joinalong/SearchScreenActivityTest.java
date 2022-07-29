package com.joinalongapp.joinalong;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;

import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.SearchView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class SearchScreenActivityTest {

    private IdlingResource idlingResource;

    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), SearchScreenActivity.class);
        intent.putExtra("testingToken", "110723426330439313256");
        intent.putExtra("mode", SearchScreenActivity.SearchMode.EVENT_MODE);
    }


    @Before
    public void runBefore(){

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
    }

    @Test
    public void testNonAlphanumericString(){

    }

    @Test
    public void testNoSearchResults(){
        // Given
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("ZZZZ"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        checkNumberOfElements(0);
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
    }

    @Test
    public void testOnQueryTextChange(){

    }

    @Test
    public void testClickSearchResult() throws InterruptedException {
        onView(withId(R.id.searchBar)).perform(click());
        checkEmptySearchView();
        // When
        onView(isAssignableFrom(EditText.class)).perform(replaceText("Another event"), pressKey(KeyEvent.KEYCODE_ENTER));
        // Then
        Thread.sleep(1000);
        checkNumberOfElements(1);
        //onView(withId(R.id.searchPeopleRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
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

    public class SearchingIdlingResource implements IdlingResource {
        @Nullable private volatile ResourceCallback resourceCallback;

        private AtomicBoolean isIdleNow = new AtomicBoolean(true);

        @Override
        public String getName() {
            return this.getClass().getName();
        }

        @Override
        public boolean isIdleNow() {
            return isIdleNow.get();
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            resourceCallback = callback;
        }

        public void setIdleState(boolean isIdleNowInput){
            isIdleNow.set(isIdleNowInput);
            if((resourceCallback != null) && isIdleNow != null){
                resourceCallback.onTransitionToIdle();
            }
        }
    }
}
