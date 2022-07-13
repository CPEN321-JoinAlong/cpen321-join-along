package com.joinalongapp.joinalong;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.SearchPeopleCustomAdapter;
import com.joinalongapp.controller.RequestManager;
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

public class SearchScreenActivity extends AppCompatActivity {
    private static SearchView searchView;
    private ImageView returnButton;
    private static final int SEARCH_QUERY_THRESHOLD = 1;
    private static String myUrlPath;
    private static String mySuggestionUrlPath;
    private SearchScreenActivity.LayoutManagerType layoutManagerType;
    private SearchPeopleCustomAdapter searchPeopleCustomAdapter;
    private RecyclerView recyclerView;
    private List<UserProfile> dataset;
    private FetchSearchTermSuggestionsTask fetchSearchTermSuggestionsTask;



    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        String userToken = ((UserApplicationInfo) getApplication()).getUserToken();
        UserProfile user = ((UserApplicationInfo) getApplication()).getProfile();

        Activity activity = this;
        initElements();

        setUpPageForMode();



        searchView.requestFocus();
        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                SearchScreenActivity.this, android.R.layout.simple_list_item_1, null,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
                new int[] { android.R.id.text1 }));

        layoutManagerType = SearchScreenActivity.LayoutManagerType.LINEAR_LAYOUT_MANAGER;


        if(savedInstanceState != null){
            layoutManagerType = (SearchScreenActivity.LayoutManagerType) savedInstanceState.getSerializable("layoutManager");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();



        searchPeopleCustomAdapter = new SearchPeopleCustomAdapter(dataset);
        recyclerView.setAdapter(searchPeopleCustomAdapter);




        fetchSearchTermSuggestionsTask = new FetchSearchTermSuggestionsTask(userToken, activity, getSearchMode());



        //TODO: call this for on click suggestions
        //searchView.setOnSuggestionListener();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onSearchButtonPressed(activity, userToken, query);
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= SEARCH_QUERY_THRESHOLD) {

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

        FetchSearchTermSuggestionsTask(String userToken, Context activity, SearchMode mode){
            this.userToken = userToken;
            this.activity = new WeakReference<>(activity);
            this.mode = mode;
        }

        private static final String[] sAutocompleteColNames = new String[] {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1
        };

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

                                            try {
                                                JSONArray jsonArray = new JSONArray(response.body().string());
                                                for(int i = 0; i < jsonArray.length(); i++){
                                                    UserProfile userProfile = new UserProfile();
                                                    userProfile.populateDetailsFromJson(jsonArray.get(i).toString());
                                                    outputFriends.add(userProfile);

                                                    String userName = userProfile.getFullName();

                                                    Object[] row = new Object[] { i, userName };

                                                    cursor.addRow(row);
                                                    System.out.println(i);
                                                }

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


                                                System.out.println("efwa");
                                            } catch (IOException | JSONException e) {
                                                System.out.println("OHNO");
                                            }




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

                                            try {
                                                JSONArray jsonArray = new JSONArray(response.body().string());
                                                for(int i = 0; i < jsonArray.length(); i++){
                                                    Event event = new Event();
                                                    event.populateDetailsFromJson(jsonArray.get(i).toString());
                                                    events.add(event);

                                                    String eventTitle = event.getTitle();

                                                    Object[] row = new Object[] { i, eventTitle };

                                                    cursor.addRow(row);
                                                    System.out.println(i);
                                                }

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


                                                System.out.println("efwa");
                                            } catch (IOException | JSONException e) {
                                                System.out.println("OHNO");
                                            }




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

        @Override
        protected void onPostExecute(Cursor result) {
            searchView.getSuggestionsAdapter().changeCursor(result);
        }

    }

    private void initElements() {
        searchView = findViewById(R.id.searchBar);
        returnButton = findViewById(R.id.searchBackButton);
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

    private void onSearchButtonPressed(Activity activity, String token, String query){
        RequestManager requestManager = new RequestManager();

        try {

            requestManager.get(myUrlPath + query, token, new RequestManager.OnRequestCompleteListener() {
                @Override
                public void onSuccess(Call call, Response response) {
                    List<UserProfile> outputFriends = new ArrayList<>();
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
}