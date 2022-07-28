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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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



    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        String userToken = ((UserApplicationInfo) getApplication()).getUserToken();

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

        fetchSearchTermSuggestionsTask = new FetchSearchTermSuggestionsTask(userToken, activity, getSearchMode());

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
                if (getSearchMode() == SearchMode.USER_MODE) {
                    onSearchButtonPressedUser(activity, userToken, query);
                } else {
                    onSearchButtonPressedEvent(activity, userToken, query);
                }

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
                            fetchSearchTermSuggestionsTask = new FetchSearchTermSuggestionsTask(userToken, activity, getSearchMode());
                            System.out.println("reset");
                        }
                        fetchSearchTermSuggestionsTask.execute(newText);
                        System.out.println("search");
                    }
                    System.out.println("changed");

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
                                    RequestManager requestManager = new RequestManager();
                                    requestManager.get(myUrlPath + params[0], userToken, new RequestManager.OnRequestCompleteListener() {
                                        @Override
                                        public void onSuccess(Call call, Response response) {
                                            List<UserProfile> outputFriends = new ArrayList<>();

                                            //TODO: HTTP

                                            try {
                                                JSONArray jsonArray = new JSONArray(response.body().string());
                                                for(int i = 0; i < jsonArray.length(); i++){
                                                    UserProfile userProfile = new UserProfile();
                                                    userProfile.populateDetailsFromJson(jsonArray.get(i).toString());
                                                    outputFriends.add(userProfile);

                                                    String userName = userProfile.getFullName();

                                                    Object[] row = new Object[] { i, userName };

                                                    cursor.addRow(row);
                                                }

                                                updateSuggestions();

                                            } catch (IOException | JSONException e) {
                                                //TODO: make error message
                                                Log.d("Search", "error");
                                            }

                                            theUserSuggestionList.clear();
                                            theUserSuggestionList = outputFriends;
                                        }

                                        @Override
                                        public void onError(Call call, IOException e) {
                                            System.out.println(call.toString());
                                        }
                                    });
                                } else {
                                    RequestManager requestManager = new RequestManager();
                                    requestManager.get(myUrlPath + params[0], userToken, new RequestManager.OnRequestCompleteListener() {
                                        @Override
                                        public void onSuccess(Call call, Response response) {
                                            List<Event> events = new ArrayList<>();

                                            //TODO: HTTP

                                            try {
                                                JSONArray jsonArray = new JSONArray(response.body().string());
                                                for(int i = 0; i < jsonArray.length(); i++){
                                                    Event event = new Event();
                                                    event.populateDetailsFromJson(jsonArray.get(i).toString());
                                                    events.add(event);

                                                    String eventTitle = event.getTitle();

                                                    Object[] row = new Object[] { i, eventTitle };

                                                    cursor.addRow(row);
                                                }

                                                updateSuggestions();

                                            } catch (IOException | JSONException e) {
                                                //TODO: make error message
                                                Log.d("Eventsearch", "error");
                                            }

                                            theEventSuggestionList.clear();
                                            theEventSuggestionList = events;
                                        }

                                        @Override
                                        public void onError(Call call, IOException e) {
                                            System.out.println(call.toString());
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

        private void updateSuggestions() {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) activity.get()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchView.getSuggestionsAdapter().notifyDataSetChanged();
                        }
                    });
                }
            }, 0);
        }

        @Override
        protected void onPostExecute(Cursor result) {
            searchView.getSuggestionsAdapter().changeCursor(result);
        }

    }

    private void initElements() {
        searchView = findViewById(R.id.searchBar);
        returnButton = findViewById(R.id.reportBackButton);
        recyclerView = findViewById(R.id.searchPeopleRecyclerView);

    }

    public enum SearchMode {
        EVENT_MODE,
        USER_MODE
    }

    private void setUpPageForMode() {
        SearchMode theMode = getSearchMode();

        if (theMode == SearchMode.EVENT_MODE) {
            searchView.setQueryHint("Search Events");
            myUrlPath = "event/title/"; //TODO: HTTP 200, 404, 500

        }
        if (theMode == SearchMode.USER_MODE) {
            searchView.setQueryHint("Search Users");
            myUrlPath = "user/name/"; //TODO: HTTP 200, 404, 500
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
                    List<UserProfile> outputFriends = new ArrayList<>();

                    //TODO: HTTP

                    try{
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        for(int i = 0; i < jsonArray.length(); i++){
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
                        System.out.println("efwa");
                    } catch(JSONException | IOException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Call call, IOException e) {
                    System.out.println(call.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private void onSearchButtonPressedEvent(Activity activity, String token, String query) {
        RequestManager requestManager = new RequestManager();

        try {

            requestManager.get(myUrlPath + query, token, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {
                    List<Event> events = new ArrayList<>();

                    //TODO: HTTP

                    try{
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        for(int i = 0; i < jsonArray.length(); i++){
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
                        System.out.println("efwa");
                    } catch(JSONException | IOException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Call call, IOException e) {
                    System.out.println(call.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}