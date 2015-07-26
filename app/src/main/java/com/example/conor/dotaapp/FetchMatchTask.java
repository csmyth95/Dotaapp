/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.conor.dotaapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.conor.dotaapp.data.MatchContract;
import com.example.conor.dotaapp.data.MatchContract.MatchEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class FetchMatchTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMatchTask.class.getSimpleName();

    private final Context mContext;

    public FetchMatchTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }
    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param steamAccountId Steam account id of the player.
     * @param heroId id of the player's hero
     * @param playerSlot the position of the player on the team
     * @return the row ID of the added location.
     */
    long addPlayer(String steamAccountId, int heroId, int playerSlot) {
        long steamId;

        // First, check if the location with this city name exists in the db
        Cursor playerCursor = mContext.getContentResolver().query(
                MatchContract.PlayerEntry.CONTENT_URI,
                new String[]{MatchContract.PlayerEntry._ID},
                MatchContract.PlayerEntry.COLUMN_ACCOUNT_ID + " = ?",
                new String[]{steamAccountId},
                null);

        if (playerCursor.moveToFirst()) {
            int steamIdIndex = playerCursor.getColumnIndex(MatchContract.PlayerEntry._ID);
            steamId = playerCursor.getLong(steamIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues playerValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            playerValues.put(MatchContract.PlayerEntry.COLUMN_ACCOUNT_ID, steamAccountId);
            playerValues.put(MatchContract.PlayerEntry.COLUMN_HERO_ID, heroId);
            playerValues.put(MatchContract.PlayerEntry.COLUMN_P_SLOT, playerSlot);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    MatchContract.PlayerEntry.CONTENT_URI,
                    playerValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            steamId = ContentUris.parseId(insertedUri);
        }

        playerCursor.close();
        return steamId;
    }

//    /*
//        Students: This code will allow the FetchMatchTask to continue to return the strings that
//        the UX expects so that we can continue to test the application even once we begin using
//        the database.
//     */
//    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
//        // return strings to keep UI functional for now
//        String[] resultStrs = new String[cvv.size()];
//        for ( int i = 0; i < cvv.size(); i++ ) {
//            ContentValues matchValues = cvv.elementAt(i);
//            resultStrs[i] = getReadableDateString(matchValues.getAsLong(MatchEntry.COLUMN_START_TIME));
//        }
//        return resultStrs;
//    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMatchDataFromJson(String matchJsonStr,
                                            String steamId)
            throws JSONException {

        //LOG TAGS
        final String LOG_TAG = FetchMatchTask.class.getSimpleName();
        //final String LOG_MATCH_JSON = "Match JSON Object";
        final String LOG_MATCH_ARRAY = "Match JSON Array";

        //JSON strings
        final String OWM_MATCHES = "matches";
        final String OWM_MATCHID = "match_id";
        final String OWM_START = "start_time";
        final String OWM_PLAYERS = "players";
        //final String OWM_PLAYERID = "account_id";
        //final String OWM_HEROID = "hero_id";
        final String OWM_RESULT = "result";

        //Get number of matches to display from settings
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        String numberOfMatches = sharedPrefs.getString(
                mContext.getString(R.string.num_matches_key),
                mContext.getString(R.string.num_matches_default));

        int numMatches = Integer.parseInt(numberOfMatches);

        try {
            JSONObject matchJson = new JSONObject(matchJsonStr);
            JSONArray matchArray = matchJson.getJSONObject(OWM_RESULT).getJSONArray(OWM_MATCHES);
            Log.v(LOG_MATCH_ARRAY, "matchArray: " + matchArray);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(matchArray.length());

            //String array to hold results of each match
            String[] resultStrs = new String[numMatches];

            //For loop to loop through all matches
            //ATM: matchArray.length() = 5
            for (int i = 0; i < matchArray.length(); i++) {
                // For now, using the format "Date, hero, match_id"
                String hero, match_id, dateTime;
                long date;

                // Get the JSON object representing a single match
                JSONObject singleMatch = matchArray.getJSONObject(i);
                Log.v(LOG_TAG, "Single Match JSONObject: " + singleMatch);

                //Get match_id of a single match
                match_id = singleMatch.getString(OWM_MATCHID);
                Log.v(LOG_TAG, "match_id: " + match_id);

                //Change date from UTC to readable date. Get Long value first
                date = Long.parseLong(singleMatch.getString(OWM_START));
                //Output actual date instead of just seconds for start_time
                dateTime = getReadableDateString(date);

                /**
                 *  rest of JSON code is for match data
                 * */
                //Array of players
                JSONArray allPlayers = matchArray.getJSONObject(i).getJSONArray(OWM_PLAYERS);
                Log.v(LOG_TAG, "allPlayers JSONArray: " + allPlayers);

                //Returns single Hero Object with
                JSONObject heroObject = singleMatch.getJSONArray(OWM_PLAYERS).getJSONObject(0);
                Log.v(LOG_TAG, "heroObject: " + heroObject);
                hero = heroObject.getString("hero_id");
                Log.v(LOG_TAG, "Hero ID: " + hero);

                for (int j = 0; j < allPlayers.length(); j++) {
                    JSONObject singlePlayer = allPlayers.getJSONObject(j);
                }
                //Add strings to resultStrs at index i
                resultStrs[i] = "Date: "+dateTime+", Hero: "+hero+", Match ID: "+match_id;
                Log.v(LOG_TAG, "ResultStrs["+i+"]: " + resultStrs[i]);


                for (Object s : resultStrs) {
                    Log.v(LOG_TAG, "Dota Data to Display: " + s);
                }
                /**
                 *  END OF JSON DATA
                 */

                //Insert data into match table
                ContentValues matchValues = new ContentValues();

                matchValues.put(MatchEntry.COLUMN_MATCH_KEY, match_id);
                matchValues.put(MatchEntry.COLUMN_START_TIME, dateTime);
                matchValues.put(MatchEntry.COLUMN_P_ACCOUNT_ID, steamId);

                cVVector.add(matchValues);
            }

            int inserted = 0;
            // add to database using bulkInsert
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MatchEntry.CONTENT_URI, cvArray);
            }

            // Sort order:  Ascending, by date.
            String sortOrder = MatchEntry.COLUMN_START_TIME + " ASC";
            Uri matchForPlayerUri = MatchEntry.buildMatchPlayerWithStartTime(
                    steamId, System.currentTimeMillis());

            //display bulk insert
            Cursor cur = mContext.getContentResolver().query(matchForPlayerUri,
                    null, null, null, sortOrder);
            //Display cursor
            ////NULL CURSOR
            Log.e(LOG_TAG, "Cursor: "+cur);

            cVVector = new Vector<ContentValues>(cur.getCount());
            if ( cur.moveToFirst() ) {
                do {
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cur, cv);
                    cVVector.add(cv);
                } while (cur.moveToNext());
            }

            Log.d(LOG_TAG, "FetchMatchTask Complete. " + inserted + " Inserted");

            //String[] resultStrsActual = convertContentValuesToUXFormat(cVVector);
            //return resultStrsActual;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        //return null;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        //Used for URI builder
        String steamId = params[0];
        String numMatches = params[1];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String matchJsonStr = null;

        String format = "json"; //Never used
        String KEY = "11FA65AF0B794D8A574FAEE5F26A8ED2";
        //int defaultID = 144396115;//not used???

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
                    .appendQueryParameter(ID_PARAM, steamId)
                    .appendQueryParameter(NUM_PARAM, numMatches)//Integer.toString(numMatches))
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
//        try {
//            //returns String[], NOT Object[]
//            getMatchDataFromJson(matchJsonStr, numMatches);
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
//        }
//        //happens if error getting/parsing data from match API
        return null;
    }

//    /*
//        Use data from server
//     */
//    @Override
//    protected void onPostExecute(String[] result) {
//        if (result != null && mMatchAdapter != null) {
//            mMatchAdapter.clear();
//            for(String matchStr : result) {
//                mMatchAdapter.add(matchStr);
//            }
//            // New data is back from the server.  Hooray!
//        }
//    }
}