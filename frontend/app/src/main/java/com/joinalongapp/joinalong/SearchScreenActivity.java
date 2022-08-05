package com.joinalongapp.joinalong;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.adapter.SearchEventCustomAdapter;
import com.joinalongapp.adapter.SearchPeopleCustomAdapter;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.navbar.ViewEventFragment;
import com.joinalongapp.navbar.ViewProfileFragment;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

//TODO: this class needs a lot of refactoring to remove duplication

public class SearchScreenActivity extends AppCompatActivity {
    private static SearchView searchView;
    private ImageView returnButton;
    private static final int SEARCH_QUERY_THRESHOLD = 1;
    private static String myUrlPath;
    private SearchPeopleCustomAdapter searchPeopleCustomAdapter;
    private SearchEventCustomAdapter searchEventCustomAdapter;
    private RecyclerView recyclerView;
    private List<UserProfile> dataset;
    private List<Event> datasetEvent;
    private FetchSearchTermSuggestionsTask fetchSearchTermSuggestionsTask;
    private static List<UserProfile> theUserSuggestionList = new ArrayList<>();
    private static List<Event> theEventSuggestionList = new ArrayList<>();
    private TextView noResults;
    private static final int MAX_SUGGESTIONS = 8;
    private String token;
    private String testingId;


    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Trace myTrace = FirebasePerformance.getInstance().newTrace("SearchScreenActivityUIComponents");
        //myTrace.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        token = ((UserApplicationInfo) getApplication()).getUserToken();

        if(token == null){
            token = getIntent().getStringExtra("testingToken");
        }

        if(getIntent().getExtras() != null){
            testingId = getIntent().getStringExtra("testingId");
        }


        Activity activity = this;
        initElements();

        setUpPageForMode();

        searchView.requestFocus();
        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                SearchScreenActivity.this, android.R.layout.simple_list_item_1, null,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
                new int[] { android.R.id.text1 }));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchPeopleCustomAdapter = new SearchPeopleCustomAdapter(dataset);
        searchEventCustomAdapter = new SearchEventCustomAdapter(datasetEvent);
        if (getSearchMode() == SearchMode.USER_MODE) {
            recyclerView.setAdapter(searchPeopleCustomAdapter);
        } else {
            recyclerView.setAdapter(searchEventCustomAdapter);
        }

        fetchSearchTermSuggestionsTask = new FetchSearchTermSuggestionsTask(token, activity, getSearchMode());

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                if (getSearchMode() == SearchMode.USER_MODE) {
                    ViewProfileFragment viewProfileFragment = new ViewProfileFragment();

                    Bundle info = new Bundle();
                    info.putBoolean("HIDE", false);
                    info.putSerializable("USER_INFO", theUserSuggestionList.get(position));
                    viewProfileFragment.setArguments(info);

                    AppCompatActivity activity = SearchScreenActivity.this;
                    FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayoutSearch, viewProfileFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    ViewEventFragment viewEventFragment = new ViewEventFragment();
                    Event theSelectedEvent = theEventSuggestionList.get(position);

                    Bundle info = new Bundle();
                    info.putSerializable("event", theSelectedEvent);
                    info.putString("theFrom", "search");
                    info.putString("testingId", testingId);
                    info.putString("testingToken", token);
                    viewEventFragment.setArguments(info);

                    AppCompatActivity activity = SearchScreenActivity.this;
                    FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayoutSearch, viewEventFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Trace myTrace = FirebasePerformance.getInstance().newTrace("searchQuerySubmit");
                myTrace.start();
                if (getSearchMode() == SearchMode.USER_MODE) {
                    onSearchButtonPressedUser(activity, token, query);
                } else {
                    onSearchButtonPressedEvent(activity, token, query);
                }
                myTrace.stop();
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= SEARCH_QUERY_THRESHOLD) {

                    if (getSearchMode() == SearchMode.USER_MODE) {
                        searchPeopleCustomAdapter.changeDataset(new ArrayList<>());
                    } else {
                        searchEventCustomAdapter.changeDataset(new ArrayList<>());
                    }

                    if (fetchSearchTermSuggestionsTask.getStatus() != AsyncTask.Status.RUNNING) {
                        if (fetchSearchTermSuggestionsTask.getStatus() == AsyncTask.Status.FINISHED) {
                            fetchSearchTermSuggestionsTask = new FetchSearchTermSuggestionsTask(token, activity, getSearchMode());
                            System.out.println("SUGGESTION reset");
                        }
                        fetchSearchTermSuggestionsTask.execute(newText);
                        System.out.println("SUGGESTION search");
                    }
                    System.out.println("SUGGESTION changed");

                } else {
                    searchView.getSuggestionsAdapter().changeCursor(null);
                }

                return true;
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //myTrace.stop();



    }


    //https://stackoverflow.com/questions/37657161/how-to-implement-search-view-autocomplete-in-actionbar-using-http-request/37660805#37660805
    public static class FetchSearchTermSuggestionsTask extends AsyncTask<String, Void, Cursor> {

        private String userToken;
        private WeakReference<Context> activity;
        private SearchMode mode;
        private static final String[] sAutocompleteColNames = new String[] {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1
        };

        FetchSearchTermSuggestionsTask(String userToken, Context activity, SearchMode mode){
            this.userToken = userToken;
            this.activity = new WeakReference<>(activity);
            this.mode = mode;
        }

        @Override
        protected Cursor doInBackground(String... params) {

            MatrixCursor cursor = new MatrixCursor(sAutocompleteColNames);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) activity.get()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mode == SearchMode.USER_MODE) {
                                    new RequestManager().get(myUrlPath + params[0], userToken, new RequestManager.OnRequestCompleteListener() {
                                        @Override
                                        public void onSuccess(Call call, Response response) {

                                            if (response.isSuccessful()) {

                                                List<UserProfile> outputFriends = new ArrayList<>();

                                                try {
                                                    JSONArray jsonArray = new JSONArray(response.body().string());

                                                    for (int i = 0; i < Math.min(jsonArray.length(), MAX_SUGGESTIONS); i++) {
                                                        UserProfile userProfile = new UserProfile();
                                                        userProfile.populateDetailsFromJson(jsonArray.get(i).toString());
                                                        outputFriends.add(userProfile);

                                                        String userName = userProfile.getFullName();

                                                        Object[] row = new Object[] { i, userName };

                                                        cursor.addRow(row);
                                                    }

                                                    updateSuggestions(cursor);

                                                } catch (IOException | JSONException e) {
                                                    Log.e("Search Event Suggestions", e.getMessage());
                                                }

                                                theUserSuggestionList.clear();
                                                theUserSuggestionList = outputFriends;
                                            }
                                            // ELSE: don't update the list of suggestions

                                        }

                                        @Override
                                        public void onError(Call call, IOException e) {
                                            Log.e("Search User Suggestions", e.getMessage());
                                        }
                                    });
                                } else {
                                    new RequestManager().get(myUrlPath + params[0], userToken, new RequestManager.OnRequestCompleteListener() {
                                        @Override
                                        public void onSuccess(Call call, Response response) {
                                            if (response.isSuccessful()) {
                                                List<Event> events = new ArrayList<>();

                                                try {
                                                    JSONArray jsonArray = new JSONArray(response.body().string());
                                                    for(int i = 0; i < Math.min(jsonArray.length(), MAX_SUGGESTIONS); i++){
                                                        Event event = new Event();
                                                        event.populateDetailsFromJson(jsonArray.get(i).toString());
                                                        events.add(event);

                                                        String eventTitle = event.getTitle();

                                                        Object[] row = new Object[] { i, eventTitle };

                                                        cursor.addRow(row);
                                                    }

                                                    updateSuggestions(cursor);

                                                } catch (IOException | JSONException e) {
                                                    Log.e("Search Event Suggestions", e.getMessage());
                                                }

                                                theEventSuggestionList.clear();
                                                theEventSuggestionList = events;
                                            }
                                            //ELSE: don't update the suggestions list

                                        }

                                        @Override
                                        public void onError(Call call, IOException e) {
                                            Log.e("Search Event Suggestions", e.getMessage());
                                        }
                                    });
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }, 0);


            return cursor;
        }

        private void updateSuggestions(Cursor cursor) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) activity.get()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchView.getSuggestionsAdapter().changeCursor(cursor);
                            searchView.getSuggestionsAdapter().notifyDataSetChanged();
                        }
                    });
                }
            }, 0);
        }

    }

    private void initElements() {
        searchView = findViewById(R.id.searchBar);
        returnButton = findViewById(R.id.reportBackButton);
        recyclerView = findViewById(R.id.searchPeopleRecyclerView);
        noResults = findViewById(R.id.searchNoResults);
        removeNoResultsMessage();
    }

    public enum SearchMode {
        EVENT_MODE,
        USER_MODE
    }

    private void setUpPageForMode() {
        SearchMode theMode = getSearchMode();

        if (theMode == SearchMode.EVENT_MODE) {
            searchView.setQueryHint("Search Events");
            myUrlPath = "event/title/";

        }
        if (theMode == SearchMode.USER_MODE) {
            searchView.setQueryHint("Search Users");
            myUrlPath = "user/name/";
        }
    }

    private SearchMode getSearchMode() {
        assert(getIntent().getExtras() != null);
        return (SearchMode) getIntent().getExtras().get("mode");
    }

    private void onSearchButtonPressedUser(Activity activity, String token, String query){
        RequestManager requestManager = new RequestManager();

        try {

            requestManager.get(myUrlPath + query, token, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {

                    if (response.isSuccessful()) {
                        List<UserProfile> outputFriends = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(response.body().string());

                            if (jsonArray.length() == 0) {
                                makeNoResultsMessage();
                            } else {
                                removeNoResultsMessage();
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.populateDetailsFromJson(jsonArray.get(i).toString());
                                outputFriends.add(userProfile);
                            }

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            searchPeopleCustomAdapter.changeDataset(outputFriends);
                                        }
                                    });
                                }
                            }, 0);

                        } catch(JSONException | IOException e){
                            makeNoResultsMessage();
                            FeedbackMessageBuilder.createParseError(e, "Search Events", SearchScreenActivity.this);
                        }
                    } else {
                        makeNoResultsMessage();
                    }
                }

                @Override
                public void onError(Call call, IOException e) {
                    makeNoResultsMessage();
                }
            });
        } catch (IOException e) {
            makeNoResultsMessage();
        }
        
    }

    private void onSearchButtonPressedEvent(Activity activity, String token, String query) {
        RequestManager requestManager = new RequestManager();

        try {

            requestManager.get(myUrlPath + query, token, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {

                    if (response.isSuccessful()) {
                        List<Event> events = new ArrayList<>();

                        try{
                            JSONArray jsonArray = new JSONArray(response.body().string());

                            if (jsonArray.length() == 0) {
                                makeNoResultsMessage();
                            } else {
                                removeNoResultsMessage();
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                Event event = new Event();
                                event.populateDetailsFromJson(jsonArray.get(i).toString());
                                events.add(event);
                            }

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            searchEventCustomAdapter.changeDataset(events);
                                        }
                                    });
                                }
                            }, 0);

                        } catch(JSONException | IOException e){
                            makeNoResultsMessage();
                            FeedbackMessageBuilder.createParseError(e, "Search Events", SearchScreenActivity.this);
                        }
                    } else {
                        makeNoResultsMessage();
                    }
                }

                @Override
                public void onError(Call call, IOException e) {
                    makeNoResultsMessage();
                }
            });
        } catch (IOException e) {
            makeNoResultsMessage();
        }
    }

    private void makeNoResultsMessage() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SearchScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        noResults.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 0);
    }

    private void removeNoResultsMessage() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SearchScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        noResults.setVisibility(View.GONE);
                    }
                });
            }
        }, 0);
    }
}