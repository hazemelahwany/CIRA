package com.example.android.cira;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class SearchFragement extends Fragment {

    ArrayList<String> contactList;
    ArrayList<String> cl;
    Cursor cursor;
    int counter;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;
    String contacts;
    private ListView dataList;
    SuggestedListAdapter adapter;
    JSONObject contact;
    JSONArray phone;
    JSONArray emailJson;
    final String BASE_URL = "http://ciraapp.mybluemix.net";
    ArrayList<String> contactsList;
    SuggestedContactsDB suggestedContactsDB;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.search_fragement, container, false);

        contacts = "";
        dataList = (ListView) rootView.findViewById(R.id.data_list);
        suggestedContactsDB = new SuggestedContactsDB(getActivity());

        cl = new ArrayList<>();
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();
        updateBarHandler =new Handler();
        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getContacts();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for(int i = 0; i < contactList.size(); i++) {
                    if(contactList != null)
                        Log.v("Contact", contactList.get(i));
                }

            }
        }).start();

        return rootView;

    }

    public void getContacts() throws JSONException {
        contactList = new ArrayList<>();
        String phoneNumber;
        String email;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;
        StringBuffer output;
        ContentResolver contentResolver = getActivity().getContentResolver();
        cursor = contentResolver.query(CONTENT_URI, null,null, null, null);
        // Iterate every contact in the phone
        if (cursor != null && cursor.getCount() > 0) {
            counter = 0;
            while (cursor.moveToNext()) {
                output = new StringBuffer();
                // Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage("Reading contacts : " + counter++ + "/" + cursor.getCount());
                    }
                });
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    output.append("\n Name:").append(name);

                    contact = new JSONObject().put("id", contact_id);
                    contact.put("name", name);
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
                    phone = new JSONArray();
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                            output.append("\n Phone number:").append(phoneNumber);
                            phone.put(phoneNumber);
                        }
                    }
                    if (phoneCursor != null) {
                        phoneCursor.close();
                    }
                    // Read every email id associated with the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);
                    emailJson = new JSONArray();
                    if (emailCursor != null) {
                        while (emailCursor.moveToNext()) {
                            email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                            output.append("\n Email:").append(email);
                            emailJson.put(email);
                        }
                    }
                    if (emailCursor != null) {
                        emailCursor.close();
                    }
                }
                // Add the contact to the ArrayList
                contact.put("phones", phone);
                contact.put("emails", emailJson);
                Log.v("c", contact.toString());
                contactList.add(output.toString());
                new postContact().execute(contact.toString());
            }

            // Dismiss the progressbar after 500 millisecondds
            updateBarHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 2000);
        }
    }

    public void updateContact(int position) {
        Log.v("koso", String.valueOf(position));
        new putContact().execute(String.valueOf(position+1));

    }

    public void removeContact(int position) {
        cl.remove(position);
        adapter.notifyDataSetChanged();
    }

    public class putContact extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            OutputStreamWriter writer = null;

            try {

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath("SaveContact")
                        .appendPath("put")
                        .build();

                URL url = new URL(buildUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("PUT");
                urlConnection.connect();
                writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
                writer.write(suggestedContactsDB.getData(Integer.parseInt(strings[0])));
                Log.v("put", suggestedContactsDB.getData(Integer.parseInt(strings[0])));
                writer.flush();


                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                System.out.println(sb.toString());
                return null;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
    }


    public class postContact extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            OutputStreamWriter writer = null;

            try {

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath("SaveContact")
                        .appendPath("post")
                        .build();

                URL url = new URL(buildUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty( "Content-Type", "application/json" );
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();
                writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
                writer.write(strings[0]);
                writer.flush();



                BufferedReader br = new BufferedReader(new InputStreamReader( urlConnection.getInputStream(),"utf-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                System.out.println(sb.toString());
                if (sb.toString().equals("\"suggest\"")) {
                    addToDatabase(strings[0]);

                    final String Name = "name";
                    final String EMAILS = "emails";
                    final String PHONES = "phones";
                    final String EMAIL_JSON = "email";
                    final String PHONE_JSON = "phone";

                    JSONObject contact = new JSONObject(strings[0]);

                    String name;
                    JSONArray emailJsonArray;
                    JSONArray phoneJsonArray;
                    String phone = "";
                    String email = "";

                    name = contact.getString(Name);

                    emailJsonArray = contact.getJSONArray(EMAILS);
                    if (emailJsonArray.length() != 0) {
                        for (int j = 0; j < emailJsonArray.length(); j++) {
                            if(j != emailJsonArray.length() - 1) {
                                email = email + emailJsonArray.getString(j) + "\n";
                            } else {
                                email = email + emailJsonArray.getString(j);
                            }
                        }
                    }

                    phoneJsonArray = contact.getJSONArray(PHONES);
                    if (phoneJsonArray.length() != 0) {
                        for (int j = 0; j < phoneJsonArray.length(); j++) {
                            if (j != phoneJsonArray.length() - 1) {
                                phone = phone + phoneJsonArray.getString(j) + "\n";
                            } else {
                                phone = phone + phoneJsonArray.getString(j);
                            }
                        }
                    }

                    String result = "Name: " + name + "\n" + "Phone: " + phone + "\n" + "Email: " + email;
                    cl.add(result);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
        // TODO: 23-02-2017 Convert to Json Object - get Contact's ID - add id to database
        private void addToDatabase(String string) throws JSONException {
            JSONObject mcontact = new JSONObject(string);
            if(suggestedContactsDB.getData(Integer.parseInt(mcontact.getString("id"))) == null){
            suggestedContactsDB.insertContact(mcontact.getString("id"), string);
            } else {
                suggestedContactsDB.updateContact(Integer.parseInt(mcontact.getString("id")), string);
            }


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter = new SuggestedListAdapter(getActivity(), R.layout.suggested_item_list, cl, SearchFragement.this);
            adapter.notifyDataSetChanged();
            dataList.setAdapter(adapter);
        }
    }

//    public class FetchContacts extends AsyncTask<Object, Object, String[]> {
//
//
//        @Override
//        protected String[] doInBackground(Object... params) {
//
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            String contactsJsonString = null;
//
////            Object sort = params[0];
//
//
//
//            try {
//
//
//
//                Uri buildUri = Uri.parse(BASE_URL).buildUpon().appendPath("Contact").build();
//
//                URL url = new URL(buildUri.toString());
//
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuilder buffer = new StringBuilder();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line).append("\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//
//                contactsJsonString = buffer.toString();
//
//
//            } catch (IOException ioException) {
//                Log.e("Query error", ioException.getLocalizedMessage());
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e("Error closing stream", e.getLocalizedMessage());
//                    }
//                }
//            }
//            try {
//                return parseJson(contactsJsonString);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        private String[] parseJson(String contactsJsonString) throws JSONException {
//
//            final String Name = "name";
//            final String EMAILS = "email";
//            final String PHONES = "phone";
//
//
//            JSONArray contactsArray = new JSONArray(contactsJsonString);
//
//            String[] results = new String[contactsArray.length()];
//            for (int i = 0; i < contactsArray.length(); i++) {
//
//                String name;
//                String email;
//                String phone;
//
//
//                JSONObject contact = contactsArray.getJSONObject(i);
//
//                name = contact.getString(Name);
//                email = contact.getString(EMAILS);
//                phone = contact.getString(PHONES);
//
//
//
//                results[i] = "Name: " + name + "\n" + "Phone: " + phone + "\n" + "Email: " + email;
//
//
//            }
//
//            return results;
//        }
//
//
//        @Override
//        protected void onPostExecute(String[] strings) {
//            if(strings != null) {
//                contactsList = new ArrayList<>(strings.length);
//                for (int i = 0; i < strings.length; i++) {
//                    contactsList.add(strings[i]);
//                }
//                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.activity_list_item, android.R.id.text1, contactsList);
//                dataList.setAdapter(adapter);
////            json.setText(contacts+ "\n");
//            }
//        }
//    }
}
