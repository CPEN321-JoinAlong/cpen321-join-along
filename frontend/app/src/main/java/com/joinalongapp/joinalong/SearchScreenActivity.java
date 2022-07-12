package com.joinalongapp.joinalong;

import android.app.Activity;
import android.app.SearchManager;
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

import com.joinalongapp.adapter.FriendsRequestCustomAdapter;
import com.joinalongapp.adapter.SearchPeopleCustomAdapter;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.navbar.FriendsRequestFragment;
import com.joinalongapp.navbar.ViewProfileFragment;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private static String theBaseUrl;
    private SearchScreenActivity.LayoutManagerType layoutManagerType;
    private SearchPeopleCustomAdapter searchPeopleCustomAdapter;
    private RecyclerView recyclerView;
    private List<UserProfile> dataset;



    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        String token = ((UserApplicationInfo) getApplication()).getUserToken();
        UserProfile user = ((UserApplicationInfo) getApplication()).getProfile();

        Activity activity = this;
        initElements();
        initDataset(activity, token);

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





        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= SEARCH_QUERY_THRESHOLD) {
                    new FetchSearchTermSuggestionsTask().execute(newText);
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

        private static final String[] sAutocompleteColNames = new String[] {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1
        };

        @Override
        protected Cursor doInBackground(String... params) {

            MatrixCursor cursor = new MatrixCursor(sAutocompleteColNames);

            //TODO: DO A GET
            //TODO: remove me for testing only
            List<String> terms = new ArrayList<>();
            terms.add("dog");
            terms.add("cat");

            for (int index = 0; index < terms.size(); index++) {
                String term = terms.get(index);

                Object[] row = new Object[] { index, term };

                //TODO: remove this if statement and move cursor.add out, just testing search
                if (terms.get(index).startsWith(params[0])) {
                    cursor.addRow(row);
                }

            }

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
            theBaseUrl = "EVENT URL CHANGE ME";
        }
        if (theMode == SearchMode.USER_MODE) {
            searchView.setQueryHint("Search Users");
            theBaseUrl = "USER URL CHANGE ME";
        }
    }

    private SearchMode getSearchMode() {
        assert(getIntent().getExtras() != null);
        return (SearchMode) getIntent().getExtras().get("mode");
    }

    private void initDataset(Activity activity, String token){
        RequestManager requestManager = new RequestManager();

        try {

            requestManager.get("user/" , token, new RequestManager.OnRequestCompleteListener() {
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
                        }, 0, 1000);
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