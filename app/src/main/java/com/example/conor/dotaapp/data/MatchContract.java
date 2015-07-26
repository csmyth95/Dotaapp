package com.example.conor.dotaapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by root on 28/06/15.
 */
public class MatchContract {

    //content authority is a name for entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.conor.dotaapp.data.MatchProvider";
    public static final String PATH_MATCH = "match";
    public static final String PATH_PLAYER= "player";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + PATH_MATCH);

//    public static final String PATH_HERO = "hero";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /*
        Inner class that defines the contents of the player table
     */
    public static final class PlayerEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAYER).build();

        //Many item return
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + PATH_PLAYER;
        //Single item return
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + PATH_PLAYER;

        public static final String TABLE_NAME = "player";
        // Steam account id of the user
        public static final String COLUMN_ACCOUNT_ID = "account_id";
        // Id of the player's hero used for getting the hero image and name
        public static final String COLUMN_HERO_ID = "hero_id";
        //Player slot on team
        public static final String COLUMN_P_SLOT = "player_slot";

        public static Uri buildPlayerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the contents of the match table */
    public static final class MatchEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MATCH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + PATH_MATCH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + PATH_MATCH;

        public static final String TABLE_NAME = "match";
        // Column with the ID of a match of the user
        public static final String COLUMN_MATCH_KEY = "match_id";
        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_START_TIME = "start_time";
        // Player account ID to be used as foreign key to player table
        public static final String COLUMN_P_ACCOUNT_ID = "account_id";

        public static Uri buildMatchUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        //FILL IN
        public static Uri buildMatchPlayer(String steamId){
            return CONTENT_URI.buildUpon().appendPath(steamId).build();
        }
        public static Uri buildMatchPlayerWithStartTime(
                String steamId, long startDate) {
            long normalizedDate = normalizeDate(startDate);
            return CONTENT_URI.buildUpon().appendPath(steamId)
                    .appendQueryParameter(COLUMN_START_TIME, Long.toString(normalizedDate)).build();
        }

        public static Uri buildMatchPlayerWithDate(String steamId, long date) {
            return CONTENT_URI.buildUpon().appendPath(steamId)
                    .appendPath(Long.toString(normalizeDate(date))).build();
        }

        public static String getSteamIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_START_TIME);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }

    }
}
