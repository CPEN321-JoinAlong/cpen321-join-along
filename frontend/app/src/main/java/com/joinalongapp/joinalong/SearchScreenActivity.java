package com.joinalongapp.joinalong;

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

import java.util.ArrayList;
import java.util.List;

public class SearchScreenActivity extends AppCompatActivity {
    private static SearchView searchView;
    private ImageView returnButton;
    private static final int SEARCH_QUERY_THRESHOLD = 1;
    private static String theBaseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        initElements();

        setUpPageForMode();

        searchView.requestFocus();
        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                SearchScreenActivity.this, android.R.layout.simple_list_item_1, null,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
                new int[] { android.R.id.text1 }));

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
}