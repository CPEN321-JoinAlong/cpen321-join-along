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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        initElements();
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
                BaseColumns._ID,                         // necessary for adapter
                SearchManager.SUGGEST_COLUMN_TEXT_1      // the full search term
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
                cursor.addRow(row);
            }

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            searchView.getSuggestionsAdapter().changeCursor(result);
        }

    }

    public void initElements() {
        searchView = findViewById(R.id.searchBar);
        returnButton = findViewById(R.id.searchBackButton);
    }
}