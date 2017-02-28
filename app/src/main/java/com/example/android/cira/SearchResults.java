package com.example.android.cira;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SearchResults extends Activity {

    final String BASE_URL = "http://ciraapp.mybluemix.net";
    ListView resultsList;
    ArrayList<String> results;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        resultsList = (ListView) findViewById(R.id.search_results);

        handleIntent(getIntent());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra("query");
            Log.v("query", query);
            doMySearch(query);
//        }
    }

    private void doMySearch(String query) {
        Log.v("fuck", query);
        new getContact().execute(query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    public class getContact extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String contactsJsonString = null;


            try {

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath("SearchContact")
                        .appendPath("get")
                        .appendQueryParameter("name", strings[0]).build();


                URL url = new URL(buildUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                contactsJsonString = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Error closing stream", e.getLocalizedMessage());
                    }
                }
            }
            try {
                if(contactsJsonString!= null) {
                    return parseJson(contactsJsonString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;

        }

        private String[] parseJson(String contactsJsonString) throws JSONException {
            final String Name = "name";
            final String EMAILS = "emails";
            final String PHONES = "phones";
            final String EMAIL_JSON = "email";
            final String PHONE_JSON = "phone";


            JSONArray contactsArray = new JSONArray(contactsJsonString);

            String[] results = new String[contactsArray.length()];
            for (int i = 0; i < contactsArray.length(); i++) {

                String name;
                JSONArray emailJsonArray;
                JSONArray phoneJsonArray;
                String phone = "";
                String email = "";


                JSONObject contact = contactsArray.getJSONObject(i);

                name = contact.getString(Name);

                emailJsonArray = contact.getJSONArray(EMAILS);
                if (emailJsonArray.length() != 0) {
                    for (int j = 0; j < emailJsonArray.length(); j++) {
                        JSONObject mail = emailJsonArray.getJSONObject(j);
                        if(j != emailJsonArray.length() - 1) {
                            email = email + mail.getString(EMAIL_JSON) + "\n";
                        } else {
                            email = email + mail.getString(EMAIL_JSON);
                        }
                    }
                }

                phoneJsonArray = contact.getJSONArray(PHONES);
                if (phoneJsonArray.length() != 0) {
                    for (int j = 0; j < phoneJsonArray.length(); j++) {
                        JSONObject ph = phoneJsonArray.getJSONObject(j);
                        if (j != phoneJsonArray.length() - 1) {
                            phone = phone + ph.getString(PHONE_JSON) + "\n";
                        } else {
                            phone = phone + ph.getString(PHONE_JSON);
                        }
                    }
                }



                results[i] = "Name: " + name + "\n" + "Phone: " + phone + "\n" + "Email: " + email;


            }

            return results;
        }

        @Override
        protected void onPostExecute(String[] strings) {

            if (strings != null) {
                results = new ArrayList<>(strings.length);
                for (int i = 0; i < strings.length; i++) {
                    results.add(strings[i]);
                }
                adapter = new ArrayAdapter<>(SearchResults.this, android.R.layout.activity_list_item, android.R.id.text1, results);
                resultsList.setAdapter(adapter);
            } else {
                Toast.makeText(SearchResults.this, "No Results Found", Toast.LENGTH_LONG).show();
            }

        }
    }
}
