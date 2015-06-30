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
package com.example.conor.dotaapp.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MatchProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MatchDbHelper mOpenHelper;

    static final int MATCH = 100; //Used to be 100
    static final int PLAYER = 300;
    static final int MATCH_WITH_PLAYER = 101;
    static final int MATCH_WITH_PLAYER_AND_DATE = 102;

    private static final SQLiteQueryBuilder sMatchBySteamIdQueryBuilder;

    static{
        sMatchBySteamIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //match INNER JOIN players ON match.pAccount_id = account_id
        sMatchBySteamIdQueryBuilder.setTables(
                MatchContract.MatchEntry.TABLE_NAME + " INNER JOIN " +
                        MatchContract.PlayerEntry.TABLE_NAME +
                        " ON " + MatchContract.MatchEntry.TABLE_NAME +
                        "." + MatchContract.MatchEntry.COLUMN_MATCH_KEY +
                        " = " + MatchContract.PlayerEntry.TABLE_NAME +
                        "." + MatchContract.PlayerEntry._ID
// + " INNER JOIN " +
//                        MatchContract.HeroEntry.TABLE_NAME + " ON " + MatchContract.HeroEntry.TABLE_NAME +
//                        "." + MatchContract.HeroEntry.COLUMN_HERO_ID +
//                        " = " + MatchContract.PlayerEntry.TABLE_NAME + "." +
//                                MatchContract.PlayerEntry.COLUMN_HERO_ID
                        );
    }

    //MIGHT NEED TO CHANGE THIS TO SUIT DOTA API JSON STRING
    //player.player_setting = ?
    private static final String sMatchSettingSelection =
            MatchContract.PlayerEntry.TABLE_NAME+
                    "." + MatchContract.PlayerEntry.COLUMN_ACCOUNT_ID + " = ? ";

    //player.steamID_setting = ? AND date >= ?
    private static final String sSteamIdWithStartDateSelection =
            MatchContract.PlayerEntry.TABLE_NAME+
                    "." + MatchContract.PlayerEntry.COLUMN_ACCOUNT_ID + " = ? AND " +
                    MatchContract.MatchEntry.COLUMN_START_TIME + " = ? ";

//    MatchContract.PlayerEntry.TABLE_NAME+
//            "." + MatchContract.PlayerEntry.COLUMN_ACCOUNT_ID + " = ? AND " +
//    MatchContract.MatchEntry.COLUMN_START_TIME + " >= ? ";

    /**COULD CHANGE TO NUMBER OF MATCHES SELECTION????? */
//    //location.location_setting = ? AND date = ?
//    private static final String sSteamIdAndDaySelection =
//            MatchContract.PlayerEntry.TABLE_NAME +
//                    "." + MatchContract.PlayerEntry.COLUMN_ACCOUNT_ID + " = ? AND " +
//                    MatchContract.MatchEntry. + " = ? ";

  private Cursor getMatchBySteamId(Uri uri, String[] projection, String sortOrder) {
        String steamId = MatchContract.MatchEntry.getSteamIdFromUri(uri);
        long startDate = MatchContract.MatchEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sMatchSettingSelection;
            selectionArgs = new String[]{steamId};
        } else {
            selectionArgs = new String[]{steamId, Long.toString(startDate)};
            selection = sSteamIdWithStartDateSelection;
        }

        return sMatchBySteamIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMatchBySteamIdAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String steamId = MatchContract.MatchEntry.getSteamIdFromUri(uri);
        long date = MatchContract.MatchEntry.getDateFromUri(uri);

        return sMatchBySteamIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sSteamIdWithStartDateSelection,
                new String[]{steamId, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    /*
        create the UriMatcher. This UriMatcher will match each URI
        to the MATCH, MATCH_WITH_PLAYER, MATCH_WITH_PLAYER_AND_DATE,
        and PLAYER and HERO integer constants defined above.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MatchContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // MatchContract to help define the types to the UriMatcher.
        matcher.addURI(authority, MatchContract.PATH_MATCH, MATCH);
        matcher.addURI(authority, MatchContract.PATH_MATCH + "/*", MATCH_WITH_PLAYER);
        matcher.addURI(authority, MatchContract.PATH_MATCH + "/*/*", MATCH_WITH_PLAYER_AND_DATE);
        //matcher.addURI(authority, MatchContract.PATH_MATCH + "/*/#", MATCH_WITH_PLAYER_AND_DATE);
        matcher.addURI(authority, MatchContract.PATH_PLAYER, PLAYER);
//        matcher.addURI(authority, MatchContract.PATH_HERO, HERO);

        // 3) Return the new matcher!
        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MatchDbHelper(getContext());
        return true;
    }

    /*
        getType function that uses the UriMatcher.
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MATCH_WITH_PLAYER_AND_DATE:
                return MatchContract.MatchEntry.CONTENT_ITEM_TYPE;
            case MATCH_WITH_PLAYER:
                return MatchContract.MatchEntry.CONTENT_TYPE;
            case MATCH:
                return MatchContract.MatchEntry.CONTENT_TYPE;
            case PLAYER:
                return MatchContract.PlayerEntry.CONTENT_TYPE;
//            case HERO:
//                return MatchContract.HeroEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "match/*/*"
            case MATCH_WITH_PLAYER_AND_DATE:
            {
                retCursor = getMatchBySteamIdAndDate(uri, projection, sortOrder);
                break;
            }
            // "match/*"
            case MATCH_WITH_PLAYER: {
                retCursor = getMatchBySteamId(uri, projection, sortOrder);
                break;
            }
            // "match"
            case MATCH: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MatchContract.MatchEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "player"
            case PLAYER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MatchContract.PlayerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );;
                break;
            }
//            // "hero"
//            case HERO: {
//                retCursor = mOpenHelper.getReadableDatabase().query(
//                        MatchContract.HeroEntry.TABLE_NAME,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder
//                );;
//                break;
//            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MATCH: {
                normalizeDate(values);
                long _id = db.insert(MatchContract.MatchEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MatchContract.MatchEntry.buildMatchUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            case PLAYER: {
                long _id = db.insert(MatchContract.PlayerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MatchContract.PlayerEntry.buildPlayerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
//            case HERO: {
//                long _id = db.insert(MatchContract.HeroEntry.TABLE_NAME, null, values);
//                if (_id > 0)
//                    returnUri = MatchContract.HeroEntry.buildHeroUri(_id);
//                else
//                    throw new android.database.SQLException("Failed to insert row into " + uri);
//                break;
//            }
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // get writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        // this makes delete all rows return the number of rows deleted
        if( null == selection ) selection = "1";

        switch ( match ) {
            case MATCH:
                rowsDeleted = db.delete(MatchContract.MatchEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PLAYER:
                rowsDeleted = db.delete(MatchContract.PlayerEntry.TABLE_NAME, selection, selectionArgs);
                break;

//            case HERO:
//                rowsDeleted = db.delete(MatchContract.HeroEntry.TABLE_NAME, selection, selectionArgs);
//                break;
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return  rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(MatchContract.MatchEntry.COLUMN_START_TIME)) {
            long dateValue = values.getAsLong(MatchContract.MatchEntry.COLUMN_START_TIME);
            values.put(MatchContract.MatchEntry.COLUMN_START_TIME, MatchContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MATCH:
                normalizeDate(values);
                rowsUpdated = db.update(MatchContract.MatchEntry.TABLE_NAME, values, selection,
                                                selectionArgs);
                break;
            case PLAYER:
                rowsUpdated = db.update(MatchContract.PlayerEntry.TABLE_NAME, values, selection,
                                                selectionArgs);
                break;
//            case HERO:
//                rowsUpdated = db.update(MatchContract.HeroEntry.TABLE_NAME, values, selection, selectionArgs);
//                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;

    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MATCH:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(MatchContract.MatchEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}