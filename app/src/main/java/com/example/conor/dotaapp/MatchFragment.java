package com.example.conor.dotaapp;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatchFragment extends Fragment {

    private ArrayAdapter<String> mMatchAdapter;

    public MatchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.matchfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchMatch matchTask = new FetchMatch();
            matchTask.execute("144396115");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create some dummy data for the ListView.  Here's a sample weekly matches
        String[] data = {
                "Mon 6/23 20:34 - puck - 10/3",
                "Tue 6/24 21:53 - slark - 21/4",
                "Wed 6/25 18:16 - slarder - 22/17",
                "Thurs 6/26 15:23 - warlock - 8/11",
                "Fri 6/27 15:09 - zues - 21/3",
                "Sat 6/28 16:52 - weaver - 23/7",
                "Sun 6/29 16:02 - sniper - 20/7"
        };
        List<String> matchList = new ArrayList<String>(Arrays.asList(data));

        // Now that we have some dummy match data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy match) and
        // use it to populate the ListView it's attached to.
        mMatchAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_match, // The name of the layout ID.
                        R.id.list_item_match_textview, // The ID of the textview to populate.
                        matchList);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_match);

        listView.setAdapter(mMatchAdapter);

        return rootView;

    }


    public class FetchMatch extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMatch.class.getSimpleName();
        private final String LOG_MATCH_JSON = "Match JSON Object";
        private final String LOG_MATCH_ARRAY = "Match JSON Array";


        //---------------------------------------------------------------------------------------------------------------

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getMatchDataFromJson(String matchJsonStr, int numMatches)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_MATCHES = "matches";
            final String OWM_MATCHID = "match_id";
            final String OWM_START = "start_time";
            final String OWM_PLAYERS = "players";
            final String OWM_PLAYERID = "account_id";
            final String OWM_HEROID = "hero_id";

            JSONObject matchJson = new JSONObject(matchJsonStr); //Curly Brackets
            Log.v(LOG_MATCH_JSON, "match json result!!!!!  " + matchJson);

            //Navigating through matchJson object to get the matches array as a JSONArray
            final JSONArray matchArray = matchJson.getJSONObject("result").getJSONArray("matches");//CURLY brackets
            Log.v(LOG_MATCH_ARRAY, "match array result!!!!! " + matchArray);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            //String array to hold results of each match
            String[] resultStrs = new String[numMatches];
            for (int i = 0; i < matchArray.length(); i++) {
                // For now, using the format "Date, hero, match_id"
                String date, hero, match_id;

                // Get the JSON object representing a single match
                JSONObject singleMatch = matchArray.getJSONObject(i);
                Log.v(LOG_TAG, "Single Match JSONObject: " + singleMatch);

                //Get match_id of a single match
                match_id = singleMatch.getString(OWM_MATCHID);
                Log.v(LOG_TAG, "match_id: " + match_id);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                // Cheating to convert this to UTC time, which is what we want anyhow.
                // To get the starting time of the match in UTC seconds
                date = matchArray.getJSONObject(i).getString(OWM_START);

                //Array of players
                JSONArray allPlayers = matchArray.getJSONObject(i).getJSONArray(OWM_PLAYERS);
                Log.v(LOG_TAG, "allPlayers JSONArray: " + allPlayers);

                ////Array of Players in String format////
                //String A = allPlayers.toString();
                //Log.v(LOG_TAG, "A (allPlayers String): " + A);

                // to get the player hero---need to look through each player and check if its us, then use the corresponding hero
                ////Failing here: returns objects
                //JSONArray playerArray = matchArray.getJSONArray(i).getJSONArray(i);
                //Log.v(LOG_TAG, "playerArray: " + playerArray);

                //Returns single Hero Object with
                JSONObject heroObject = singleMatch.getJSONArray(OWM_PLAYERS).getJSONObject(0);
                Log.v(LOG_TAG, "heroObject: " + heroObject);
                hero = heroObject.getString("hero_id");
                Log.v(LOG_TAG, "Hero ID: " + hero);

                for (int j = 0; j < allPlayers.length(); j++) {
                    JSONObject singlePlayer = allPlayers.getJSONObject(j);
                }
                //Add strings to resultStrs at index i
                resultStrs[i] = "Date: "+date+", Hero: "+hero+", Match ID: "+match_id;
                Log.v(LOG_TAG, "ResultStrs["+i+"]: " + resultStrs[i]);
            }

            for (Object s : resultStrs) {
                Log.v(LOG_TAG, "!!!!Dota Data to Display: " + s);
            }
            String[] dummyStringArray = new String[0];
            return resultStrs;
        }

        //-------------------------------------------------------------------------------------------------------------
        @Override
        protected String[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String matchJsonStr = null;

            String format = "json";
            int numMatches = 2;
            String KEY = "11FA65AF0B794D8A574FAEE5F26A8ED2";
            int defaultID = 144396115;

            try {
                //--------------------------------
                //Query for the last 5 matches played by my account using our own key-------------------
                //--------------------------------
                final String MATCH_BASE_URL =
                        "https://api.steampowered.com/IDOTA2Match_570/GetMatchHistory/V001/?";
                final String KEY_PARAM = "key";
                final String ID_PARAM = "account_id";
                final String NUM_PARAM = "matches_requested";

                Uri builtUri = Uri.parse(MATCH_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, KEY)
                        .appendQueryParameter(ID_PARAM, params[0])
                        .appendQueryParameter(NUM_PARAM, Integer.toString(numMatches))
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                matchJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Match string: " + matchJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                //returns String[], NOT Object[]
                return getMatchDataFromJson(matchJsonStr, numMatches);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }
    }
}
