package com.example.android.cira;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;


public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if(savedInstanceState == null) {
            SearchFragement searchFragement = new SearchFragement();
            getFragmentManager().beginTransaction().add(R.id.search_container,
                    searchFragement).commit();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);

        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        // Suggestions
//        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(this, R.layout.auto_complete, null,
//                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
//                new int[] { R.id.tv }, 0));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchData(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;

//            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                                                  @Override
//                                                  public boolean onQueryTextSubmit(String query) {
//                                                      searchData(query);
//                                                      return true;
//                                                  }
//
//                                                  @Override
//                                                  public boolean onQueryTextChange(String query) {
//                                                      if (query.length() >= 3) {
//                                                          new FetchSearchTermSuggestionsTask().execute(query);
//                                                      } else {
//                                                          searchView.getSuggestionsAdapter().changeCursor(null);
//                                                      }
//
//                                                      return true;
//                                                  }
//                                              });
//
//            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
//
//                @Override
//                public boolean onSuggestionSelect(int position) {
//
//                    Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
//                    String term = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2));
//                    cursor.close();
//
//                    final Intent intent;
//                    intent = new Intent(getApplicationContext(), DetailActivity.class);
//                    intent.putExtra("selected_item_id", Integer.parseInt(term));
//                    intent.putExtra("selected_shop_id", selectedShopID );
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.right_to_left,R.anim.blank_anim);
//
//                    return true;
//                }
//
//                @Override
//                public boolean onSuggestionClick(int position) {
//
//                    return onSuggestionSelect(position);
//                }
//            });
    }

    private void searchData(String query) {
        Intent intent = new Intent(this, SearchResults.class);
        intent.putExtra("query", query);

        startActivity(intent);
    }

}
